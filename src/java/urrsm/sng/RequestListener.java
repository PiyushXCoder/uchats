package urrsm.sng;

/**
 *
 * @author piyush
 */

import javax.servlet.annotation.WebListener;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;

@WebListener()
public class RequestListener implements ServletRequestListener {
            
        @Override
	public void requestDestroyed(ServletRequestEvent event) {
		
	}

        @Override
	public void requestInitialized(ServletRequestEvent event) {
		WebScocketEnd.ipaddress = event.getServletRequest().getRemoteAddr();
	}

}
