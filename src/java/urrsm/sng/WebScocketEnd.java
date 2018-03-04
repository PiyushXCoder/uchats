package urrsm.sng;

import com.google.gson.Gson;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Timer;
import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 *
 * @author piyush
 * 
 * Websocket Manager
 * 
 */

@ServerEndpoint("/uchatsserver")
public class WebScocketEnd {
    
    public static ArrayList<USession> sessions = new ArrayList<USession>();
    public static String ipaddress = null;
    public RecordManager recordmanager;
    public Gson gson = new Gson();
    
    public WebScocketEnd() throws ClassNotFoundException, SQLException
    {
        recordmanager = new RecordManager(); 
        Timer timer  = new javax.swing.Timer(1000, new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                recordmanager.unblockrun();
            }
        });
        timer.start();
    }
    
    @OnMessage
    public String onMessage(String message, Session session) throws IOException, EncodeException, NoSuchAlgorithmException {
        JsonStructure json = gson.fromJson(message, JsonStructure.class);
        USession ses = getSessionByPeer(session);
        if(recordmanager.isUserBlocked(ipaddress))
        {
            return null;
        }
        // If user do Ping
        if(json.cmd.equals("ping"))
        {
            // Do nothing
        }
        
        // If user join
        else if(json.cmd.equals("join"))
        {
            String trip = " ";
            if(json.nick.split("#").length != 1)
            {
                trip = calcHash(json.nick.split("#")[1]);
            }
            
            if(json.nick.split("#")[0].toLowerCase().equals(Config.admin.toLowerCase()) &&
                    !trip.toLowerCase().equals(calcHash(Config.password).toLowerCase()))
            {
                JsonStructure msg = new JsonStructure();
                msg.cmd = "warn";
                msg.text = "***Impersonating Admin???***";
                session.getBasicRemote().sendText(gson.toJson(msg));
                return null;
            }
            
            if(!json.nick.split("#")[0].matches("[a-zA-Z0-9_]*") && !(json.nick.split("#")[0].length() < 25))
            {
                JsonStructure msg = new JsonStructure();
                msg.cmd = "warn";
                msg.text = "Nickname must consist of up to 24 letters, numbers, and underscores!";
                session.getBasicRemote().sendText(gson.toJson(msg));
                return null;
            }
            
            if(json.channel.trim().equals(""))
            {
                JsonStructure msg = new JsonStructure();
                msg.cmd = "warn";
                msg.text = "Channel must not be spaces";
                session.getBasicRemote().sendText(gson.toJson(msg));
                return null;
            }
            
            for(USession sess : sessions)
            {
                if(sess.channel.equals(json.channel) && sess.nick.equals(json.nick.split("#")[0]))
                {
                    JsonStructure msg = new JsonStructure();
                    msg.cmd = "warn";
                    msg.text = "Nickname already taken!";
                    session.getBasicRemote().sendText(gson.toJson(msg));
                    return null;
                }
            }
            
            ses.nick = json.nick.split("#")[0];
            ses.channel = json.channel;
            ses.trip = trip;
            ses.ip = ipaddress;
            
            JsonStructure cmdonline = new JsonStructure();
            cmdonline.cmd = "onlineAdd";
            cmdonline.nick = json.nick.split("#")[0];
            boardcast(gson.toJson(cmdonline),json.channel);
            
            ArrayList<String> nicks = new ArrayList<String>();
            for(USession sess : sessions)
            {
                if(sess.channel.endsWith(json.channel)) nicks.add(sess.nick);
            }
            
            JsonStructure onlinelist = new JsonStructure();
            onlinelist.cmd = "onlineSet";
            onlinelist.nicks = nicks;
            session.getBasicRemote().sendText(gson.toJson(onlinelist));
            
        }
        
        //user sends message
        else if(json.cmd.equals("chat"))
        {
            JsonStructure chat = new JsonStructure();
            chat.cmd = "chat";
            chat.nick = ses.nick;
            chat.text = json.text;
            chat.trip = ses.trip;
            if(isAdmin(ses)) chat.admin = true;
            if(isMod(ses)) chat.mod = true;
            boardcast(gson.toJson(chat), ses.channel);
        }
        
        //user invites user to another random channel
        else if(json.cmd.equals("invite"))
        {
            for(USession sess : sessions)
            {
                if(sess.nick.equals(json.nick))
                {
                    String channel = String.valueOf(calcHash(String.valueOf(Math.random()))).substring(0, 6);
                    
                    if(sess.peer == ses.peer)
                    {
                        return null;
                    }
                    
                    JsonStructure chat = new JsonStructure();
                    chat.cmd = "info";
                    chat.text = ses.nick + " invited you to ?"+channel;
                    sess.peer.getBasicRemote().sendText(gson.toJson(chat));
                    chat.text = "you invited "+ses.nick +" to ?"+channel;
                    ses.peer.getBasicRemote().sendText(gson.toJson(chat));
                }
            }
        }
        
        // Status
        if(json.cmd.equals("stats"))
        { 
            ArrayList<String> channels = new ArrayList<String>();
            for(USession sess : sessions)
            {
                if(channels.indexOf(sess.channel) == -1)
                {
                    channels.add(sess.channel);
                }
            }
            JsonStructure chat = new JsonStructure();
            chat.cmd = "info";
            chat.text =   " Users Connected : "+sessions.size()+"\n"
                        + " Channels        : "+channels.size();
            
            boardcast(gson.toJson(chat), ses.channel);
        }
        
        //  super power
        // Ban a user
        else if(json.cmd.equals("ban"))
        {
            if(!isAdmin(ses) && !isMod(ses))
            {
                return null;
            }
            for(USession sess : sessions)
            {
                if(ses.channel.equals(sess.channel) && json.nick.equals(sess.nick)) 
                {
                    recordmanager.block(sess.ip);
                    JsonStructure chat = new JsonStructure();
                    chat.cmd = "chat";
                    chat.nick = "Server";
                    chat.text = "Ip "+sess.ip+" is banned!";
                    chat.trip = "server";
                    ses.peer.getBasicRemote().sendText(gson.toJson(chat));
                }
            }
        }
        
        // Unban a user
        else if(json.cmd.equals("unban"))
        {
            if(!isAdmin(ses) && !isMod(ses))
            {
                return null;
            }
                recordmanager.unblock(json.ip);
                JsonStructure chat = new JsonStructure();
                chat.cmd = "chat";
                chat.nick = "Server";
                chat.text = "Ip "+json.ip+" is unbanned!";
                chat.trip = "server";
                ses.peer.getBasicRemote().sendText(gson.toJson(chat));
            
        }
       
        // VIP Super Power
        // List users and their info
        else if(json.cmd.equals("listUsers"))
        {
            if(!isAdmin(ses))
            {
                return null;
            }
            String data = "";
            for(USession sess : sessions)
            {
                data  = data + "\n"+sess.nick+"\t"+sess.channel+"\t"+sess.trip+"\t"+sess.ip;
            }
            JsonStructure chat = new JsonStructure();
            chat.cmd = "chat";
            chat.nick = "Server";
            chat.text = "Nick\tChannel\tTrip\tIp\n"+data;
            chat.trip = "server";
            ses.peer.getBasicRemote().sendText(gson.toJson(chat));
        }
        
        // bordcast a message to all channel
        else if(json.cmd.equals("broadcast"))
        {
            if(!isAdmin(ses))
            {
                return null;
            }
            JsonStructure chat = new JsonStructure();
            chat.cmd = "chat";
            chat.nick = "Server";
            chat.text = json.text;
            chat.trip = "server";
            wildspread(gson.toJson(chat));
        }
        return null;
    }
    
    @OnOpen
    public void onOpen (Session peer) throws IOException {
        // Limit number of user and filter blocked users
        if(sessions.size() < 10000 && !recordmanager.isUserBlocked(ipaddress))
        {
            sessions.add(new USession(peer, " ", " ", " ", System.currentTimeMillis()));
            peer.getBasicRemote().sendText(ipaddress);
        }
        else
        {
            peer.getBasicRemote().sendText("Unable to book your seat in chatroom!");
        }
    }

    @OnClose
    public void onClose (Session peer) {
        USession leavingsession = getSessionByPeer(peer);
        if(!leavingsession.trip.equals(" "))
        {
            JsonStructure cmdonline = new JsonStructure();
            cmdonline.cmd = "onlineRemove";
            cmdonline.nick = leavingsession.nick;
            String channel = leavingsession.channel;
            sessions.remove(leavingsession);
            boardcast(gson.toJson(cmdonline),channel);
        }
        else sessions.remove(leavingsession);
    }

    @OnError
    public void onError(Throwable t) {
        t.printStackTrace();
    }    
    
    /**
     * Get USession from Arraylist
     * @param peer
     * @return 
     */
    public USession getSessionByPeer(Session peer)
    {
        for(USession session : sessions)
        {
            if(session.peer == peer) return session;
        }
        return null;
    }
    
    /**
     * Send message to a channel
     * @param message
     * @param channel 
     */
    public void boardcast(String message, String channel)
    {
        for(USession session : sessions)
        {
            try {
                if(session.channel.equals(channel)) session.peer.getBasicRemote().sendText(message);
            } catch (IOException ex) {
                Logger.getLogger(WebScocketEnd.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * Send message to all channels
     * @param message 
     */
    public void wildspread(String message)
    {
        for(USession session : sessions)
        {
            try {
                session.peer.getBasicRemote().sendText(message);
            } catch (IOException ex) {
                Logger.getLogger(WebScocketEnd.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * Calculate SHA-1 sum with salt
     * @param passwd
     * @return
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException 
     */
    public String calcHash(String passwd) throws NoSuchAlgorithmException, UnsupportedEncodingException
    {
         MessageDigest crypt = MessageDigest.getInstance("SHA-1");
        crypt.reset();
        crypt.update(passwd.getBytes("UTF-8"));
        String hash = new BigInteger(1, crypt.digest()).toString(16);
        return hash.substring(0, 6);
    }
    
    /**
     * Check if USession is of admin
     * @param session
     * @return
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException 
     */
    public boolean isAdmin(USession session) throws NoSuchAlgorithmException, UnsupportedEncodingException
    {
        if(session.trip.toLowerCase().equals(calcHash(Config.password).toLowerCase()))
        {
            return true;
        }
        return false;
    }
    
    /**
     * Check if USession is of mod
     * @param session
     * @return
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException 
     */
    public boolean isMod(USession session) throws NoSuchAlgorithmException, UnsupportedEncodingException
    {
        for(String trip: Config.mods)
        {
            if(session.trip.toLowerCase().equals(trip.toLowerCase()))
            {
                return true;
            }
        }
        return false;
    }
}
