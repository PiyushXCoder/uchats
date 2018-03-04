package urrsm.sng;

import javax.websocket.Session;

/**
 *
 * @author piyush
 */
public class USession
{
    Session     peer;
    String      nick = "";
    String      channel = "";
    String      trip = new String();
    String      ip = "";
    
    public USession(Session peer, String nick, String channel, String trip, long lastping)
    {
        this.peer = peer;
        this.nick = nick;
        this.channel = channel;
        this.trip = trip;
    }
    
    public USession(Session peer)
    {
        this.peer = peer;
    }
}
