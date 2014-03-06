package gsn.gcm.server;

import gsn.gcm.server.AndroidVS.Event;

import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.enums.EnumUtils;

/**
 * Servlet that registers a device with given regId
 */
@SuppressWarnings("serial")
public class RegisterServlet extends BaseServlet {

  private static final String PARAMETER_REG_ID = "regId";
  private static final Logger logger = Logger.getLogger(RegisterServlet.class.getName());
  
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException {
    String regId = getParameter(req, "regId");
    Datastore.register(regId);
    setSuccess(resp);
  }

}
