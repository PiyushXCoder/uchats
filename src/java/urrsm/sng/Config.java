package urrsm.sng;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.URI;

/**
 *
 * @author piyush
 * 
 * Configurations for UChat Server
 * 
 */
public class Config
{
    public static String admin = ""; // name of admin
    public static String password = "bawal"; // password of admin
    public static String mods[] = {"4f26ae"}; // trips of mods
    public static String salt = "My Salt Is This"; // salt for trips
    
    public static String dburl = "jdbc:postgresql://localhost:5432/database"; // url to database
    public static String dbusername = "user"; // database username
    public static String dbpassword = "pass"; // database password
    
    static
    {
        if(System.getenv("DATABASE_URL")!=null)
        {
            try
            {
                URI dbUri = new URI(System.getenv("DATABASE_URL"));
                dbusername = dbUri.getUserInfo().split(":")[0];
                dbpassword = dbUri.getUserInfo().split(":")[1];
                dburl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();
            }
            catch(Exception ex)
            {
                Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
