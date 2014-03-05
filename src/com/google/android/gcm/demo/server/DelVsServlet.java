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
 * Servlet that adds a Virtual Sensor
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
