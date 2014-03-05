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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Simple implementation of a data store using standard Java collections.
 * <p>
 * This class is thread-safe but not persistent (it will lost the data when the
 * app is restarted) - it is meant just as an example.
 */
public final class Datastore {

  private static final List<String> regIds = new ArrayList<String>();
  private static final Logger logger = Logger.getLogger(Datastore.class.getName());
  private static final String SAVE_FILE = "conf/android.devices";
  private static File fh;

  public Datastore() {
    throw new UnsupportedOperationException();
  }

  /**
   * Registers a device.
   */
  public static void register(String regId) {
    logger.info("Registering " + regId);
    synchronized (regIds) {
    	if(!find(regId)){
    		regIds.add(regId);
    		try{
    			FileWriter fw = new FileWriter(fh, true);
    			BufferedWriter bw = new BufferedWriter(fw);
    			bw.write(regId + '\n');
    			bw.close();
    		} catch (IOException e) {
    			logger.info(e.toString());
    		}
    	}
    }
  }

  /**
   * Unregisters a device.
   */
  public static void unregister(String regId) {
    logger.info("Unregistering " + regId);
    synchronized (regIds) {
    	if(find(regId)){
	      regIds.remove(regId);
	  		try{
	  			FileWriter fw = new FileWriter(fh);
	  			BufferedWriter bw = new BufferedWriter(fw);
	  			for(String rid : regIds)
	  				bw.write(rid + '\n');
	  			bw.close();
	  		} catch (IOException e) {
	  			logger.info(e.toString());
	  		}
    	}
    }
  }

  /**
   * Updates the registration id of a device.
   */
  public static void updateRegistration(String oldId, String newId) {
    logger.info("Updating " + oldId + " to " + newId);
    synchronized (regIds) {
      regIds.remove(oldId);
      unregister(oldId);
      regIds.add(newId);
      register(newId);
    }
  }

  /**
   * Gets all registered devices.
   */
  public static List<String> getDevices() {
    synchronized (regIds) {
    	if(regIds.isEmpty()){
  			try {
  			  fh = new File(SAVE_FILE);
  			  if(!fh.exists())
  			  	fh.createNewFile();
  			  FileReader fr = new FileReader(fh);
  			  BufferedReader br = new BufferedReader(fr);
  			  String line;
  			  while((line = br.readLine()) != null)
  			  	regIds.add(line);
  			  br.close();
  			} catch (IOException e) {
  				//e.printStackTrace();
  				logger.info(e.toString());
  			}
    	}
      return new ArrayList<String>(regIds);
    }
  }

  public static boolean find(String regId){
  	synchronized (regIds) {
  		if(regIds.isEmpty()){
  			try {
  			  fh = new File(SAVE_FILE);
  			  if(!fh.exists())
  			  	fh.createNewFile();
  			  FileReader fr = new FileReader(fh);
  			  BufferedReader br = new BufferedReader(fr);
  			  String line;
  			  while((line = br.readLine()) != null)
  			  	regIds.add(line);
  			  br.close();
  			} catch (IOException e) {
  				//e.printStackTrace();
  				logger.info(e.toString());
  			}
  		}
  		return regIds.contains(regId);
  	}
  }
}
