package gsn.gcm.server;

import gsn.gcm.server.AndroidVS.Event;

import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.enums.EnumUtils;

/**
 * Servlet that removes a Virtual Sensor
 */
@SuppressWarnings("serial")
public class DelVsServlet extends BaseServlet {

  //private static final String PARAMETER_REG_ID = "regId";
  private static final Logger logger = Logger.getLogger(DelVsServlet.class.getName());
  
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException {
    String regid = getParameter(req, "regId");
    String vs_name = getParameter(req, "vs_name");
    String field_name = getParameter(req, "field_name").toUpperCase();
    logger.info("REMOVING virtual sensor with params: "+vs_name+", "+field_name);
    AndroidVS.remove(regid, vs_name, field_name);
    setSuccess(resp);
  }

}
