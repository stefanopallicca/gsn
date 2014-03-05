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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
				logger.info("FOUND!!!");
    	}
    	else{
				out.println("{");
				out.println("\"found\":\"false\"");
				out.println("}");
				logger.info("NOT FOUND!!!");
    	}
    	
    	out.flush();
    } catch(IOException e){
    	logger.info(e.toString());
    }
			
    //setSuccess(resp);
  }

}
