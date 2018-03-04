package urrsm.sng;

import java.util.ArrayList;

/**
 *
 * @author piyush
 * 
 * Structure of json send between user and server
 * It is required because we are using Gson
 * 
 */
public class JsonStructure {
    String cmd;
    String nick;
    String channel;
    String text;
    boolean admin;
    boolean mod;
    String trip;
    String ip;
    ArrayList<String> nicks;
}
