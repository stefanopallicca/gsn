/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.gcm.demo.server;

import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.enums.EnumUtils;

import com.google.android.gcm.demo.server.AndroidVS.Event;

/**
 * Servlet that registers a device, whose registration id is identified by
 * {@link #PARAMETER_REG_ID}.
 *
 * <p>
 * The client app should call this servlet everytime it receives a
 * {@code com.google.android.c2dm.intent.REGISTRATION C2DM} intent without an
 * error or {@code unregistered} extra.
 */
@SuppressWarnings("serial")
public class RegisterServlet extends BaseServlet {

  private static final String PARAMETER_REG_ID = "regId";
  private static final Logger logger = Logger.getLogger(RegisterServlet.class.getName());
  
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException {
    String regId = getParameter(req, "regId");
    /*String vsName = getParameter(req, "vs_name");
    String field = getParameter(req, "field").toUpperCase();
    double threshold = Double.parseDouble(getParameter(req, "threshold"));
    String event = getParameter(req, "event");*/
    Datastore.register(regId);
    /*logger.info("Registering virtual sensor with params: "+vsName+", "+field+", "+threshold+", "+event);
    AndroidVS.create(regId, vsName, field, threshold, event);*/
    setSuccess(resp);
  }

}
