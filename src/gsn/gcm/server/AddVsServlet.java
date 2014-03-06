package gsn.gcm.server;

import gsn.gcm.server.AndroidVS.Event;

import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.enums.EnumUtils;

/**
 * Servlet that adds a Virtual Sensor based of given parameters (regId, vs_name, field, threshold, event)
 */
@SuppressWarnings("serial")
public class AddVsServlet extends BaseServlet {

  private static final String PARAMETER_REG_ID = "regId";
  private static final Logger logger = Logger.getLogger(AddVsServlet.class.getName());
  
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException {
    String regId = getParameter(req, "regId");
    String vsName = getParameter(req, "vs_name");
    String field = getParameter(req, "field").toUpperCase();
    double threshold = Double.parseDouble(getParameter(req, "threshold"));
    String event = getParameter(req, "event");
    logger.info("Registering virtual sensor with params: "+vsName+", "+field+", "+threshold+", "+event+" for device "+regId);
    AndroidVS.create(regId, vsName, field, threshold, event);
    setSuccess(resp);
  }

}
