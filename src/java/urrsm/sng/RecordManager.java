package urrsm.sng;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author piyush
 * 
 * Record Manager 
 * 
 */
public class RecordManager {
    
    long blocktime = 3600000;
    
    public RecordManager() throws ClassNotFoundException, SQLException
    {
            Class.forName("org.postgresql.Driver");
                        
            String sql = "CREATE TABLE IF NOT EXISTS blocklist(\n" +
                                "   ip   TEXT NOT NULL,\n" +
                                "   time REAL NOT NULL\n"+
                                ");";
            
            try(Connection con = this.connect(); Statement stmt = con.createStatement())
            {
                stmt.execute(sql);
            }
            catch(SQLException ex)
            {
                Logger.getLogger(RecordManager.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
    
    /**
     * Block an ip
     * @param ip 
     */
    public void block(String ip)
    {
        String sql = "INSERT INTO blocklist VALUES ( ? , ? );";
        
        try(Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql))
        {
            pstmt.setString(1, ip);
            pstmt.setLong(2, System.currentTimeMillis());
            pstmt.executeUpdate();
        }
        catch(SQLException ex)
        {
            Logger.getLogger(RecordManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * unblock an ip
     * @param ip 
     */
    public void unblock(String ip)
    {
        String sql = "DELETE FROM blocklist WHERE ip = ? ;";
        
        try(Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql))
        {
            pstmt.setString(1, ip);
            pstmt.executeUpdate();
        }
        catch(SQLException ex)
        {
            Logger.getLogger(RecordManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * unblock those ip who have passed blocktime in blocklist
     */
    public void unblockrun()
    {
        String sql = "DELETE FROM blocklist WHERE ? >= time + ?;";
        
        try(Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql))
        {
            pstmt.setLong(1, System.currentTimeMillis());
            pstmt.setLong(2, blocktime);
            pstmt.executeUpdate();
        }
        catch(SQLException ex)
        {
            Logger.getLogger(RecordManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    /**
     * Check if user is blocked
     * @param ip
     * @return 
     */
    public boolean isUserBlocked(String ip)
    {
        String sql = "SELECT * FROM blocklist WHERE ip = ? ;";
        
        try(Connection conn = this.connect(); PreparedStatement pstmt  = conn.prepareStatement(sql))
        {
            pstmt.setString(1, ip);
            ResultSet res = pstmt.executeQuery();
            return res.next();
        }
        catch(SQLException ex)
        {
            Logger.getLogger(RecordManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    /**
     * Connect database
     * @return 
     */
    private Connection connect()
    {
        Connection con = null;
        try
        {
            con = DriverManager.getConnection(Config.dburl, Config.dbusername, Config.dbpassword);
        }
        catch(SQLException ex)
        {
            Logger.getLogger(RecordManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return con;
    }
    
}
