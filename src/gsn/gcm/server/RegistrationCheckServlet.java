package gsn.gcm.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet checks if a device was already registered with this server.
 * It provides a doGet method which requests a {@code regId} parameter, 
 * and returns a JSON string of type {found: true|false}
 *
 */
@SuppressWarnings("serial")
public class RegistrationCheckServlet extends BaseServlet {

  private static final String PARAMETER_REG_ID = "regId";
  private static final Logger logger = Logger.getLogger(RegistrationCheckServlet.class.getName());

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException {
    String regId = getParameter(req, "regId");
    resp.setContentType("application/json");
    resp.setCharacterEncoding("UTF-8");
		// Get the printwriter object from response to write the required json object to the output stream
    try{
			PrintWriter out = resp.getWriter();
			// Assuming your json object is **jsonObject**, perform the following, it will return your json object  
    
    	if(Datastore.find(regId)){
				out.println("{");
				out.println("\"found\":\"true\"");
				out.println("}");
				logger.info("Device asking for registration already in the database of known devices");
    	}
    	else{
				out.println("{");
				out.println("\"found\":\"false\"");
				out.println("}");
				logger.info("New device asking for registration");
    	}
    	
    	out.flush();
    } catch(IOException e){
    	logger.info(e.toString());
    }
  }

}
