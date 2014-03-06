/**
 * 
 */
package gsn.gcm.server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Logger;

import gsn.Main;
import gsn.beans.ContainerConfig;
import gsn.beans.Modifications;

/**
 * @author Stefano Pallicca
 * 
 * 
 */
public class AndroidVS {
	
	public enum Event {ABOVE, BELOW};
	private static final String VS_URL = "virtual-sensors/";
	private static final Logger logger = Logger.getLogger(AndroidVS.class.getName());
	
	public static <E extends Enum<E>> boolean isInEnum(String value, Class<E> enumClass) {
	  for (E e : enumClass.getEnumConstants()) {
	    if(e.name().equals(value)) { return true; }
	  }
	  return false;
	}
	
	/**
	 * This method creates a new virtual sensor based on passed parameters.
	 * Beware that the name of the virtual sensor is created pseudo-randomly, since for GSN to work, every VS
	 * needs to have a unique name.
	 * In order to avoid sending too many messages, two thresholds are actually defined: a yellow threshold corresponding
	 * to the actual {@code threshold} parameter, and a red threshold set at 98% (102%) of the threshold value, when the
	 * {@code eventString} is set to BELOW (ABOVE).
	 * There must be three consecutive values below (above) the yellow threshold for the virtual sensor to trigger a notification,
	 * whereas one single value below (above) the red threshold will trigger a notification.
	 * 
	 * @param regid Device registration ID, obtained from Google Cloud
	 * @param vsName Virtual Sensor name, must be among the active set of VSs running on the GSN server
	 * @param field Field name, must exist among the fields of {@code vsName}
	 * @param threshold numeric value triggering the alert
	 * @param event Event triggering the alert (can be {@code ABOVE} or {@code BELOW}
	 */
	public static void create(String regid, String vsName, String field, double threshold, String eventString){
		if(!isInEnum(eventString, Event.class)) return;
		try{
			ContainerConfig containerConfig = Main.loadContainerConfiguration();
			int gsnPort = containerConfig.getContainerPort();
			
			Event event = Event.valueOf(eventString);
			String filename = regid+vsName+field+".xml";
			File fh = new File(VS_URL+filename);
			if(fh.exists())
				fh.delete();
			try{
				fh.createNewFile();
				FileWriter fw = new FileWriter(fh, false);
				BufferedWriter bw = new BufferedWriter(fw);
				String outputXml;
				Integer d = new Double(Math.ceil(Math.random()*1000)*11).intValue();
				SimpleDateFormat nowDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.004'Z");
				Date date = new Date();
				String now = nowDate.format(date);
				now = new StringBuilder(now).insert(now.length()-2, ":").toString();
				outputXml = 	"<virtual-sensor name=\""+vsName.toLowerCase()+field.toLowerCase()+d.toString()+"\" priority=\"10\">\n"
												+ "<processing-class>\n"
												+ "<class-name>gsn.processor.ScriptletProcessor</class-name>\n"
												+ "<init-params>\n"
												+ "<param name=\"persistant\">false</param>\n"
												+ "<param name=\"scriptlet\">\n"
												+ "<![CDATA[\n"
												+ "if ( ! isdef('yellow_threshold'))\n"
		                    + "yellow_threshold = "+threshold+";\n"
		                    + "if ( ! isdef('red_threshold'))\n"   
		                    
		                    	+"red_threshold = "+(threshold*(event == Event.ABOVE ? 1.02 : 0.98))+";\n"
		                    	
		                    +"if ( ! isdef('latest_minimum'))\n"
		                        +"latest_minimum = 0.00;\n"
		                        
		                    +"if ( ! isdef('min_timed'))\n"
		                        +"min_timed = 0;\n"
		                        
		                    +"if ( ! isdef('latest_record'))\n"
		                        +"latest_record = 0.00;\n"
		                        
		                    +"if ( ! isdef('growing_records'))\n"
		                        +"growing_records = 0;\n"
	
		                    +"if("+field+" "+(event == Event.ABOVE ? ">" : "<")+" red_threshold){\n"
		                    		+"latest_record = "+ field +";\n"
		                    		
		                        +"def content = \"Oh no! New "+(event == Event.ABOVE ? "maximum" : "minimum")+" in "+vsName+": \" + " + field + " + \"\";\n"
		                        +"sendNotification(content, \""+regid+"\");\n"
		                    +"}"
		                    +"else if ("+ field +" "+ (event == Event.ABOVE ? ">" : "<") +" yellow_threshold ||\n" 
		                    	+"("+ field +" "+ (event == Event.ABOVE ? "<" : ">") +"= yellow_threshold && growing_records > 0) ||\n" 
		                    	+"("+ field +" "+ (event == Event.ABOVE ? "<" : ">") +"= yellow_threshold && growing_records == 0 && latest_minimum > 0)) {\n"
														+"if("+ field +" "+(event == Event.ABOVE ? "<" : ">") +"= latest_record && latest_minimum > 0){\n"
															+"growing_records++;\n"
														+"}\n"
														+"else{\n"
															+"growing_records = 0;\n"
														+"}\n"
														
														+"if("+ field +" "+ (event == Event.ABOVE ? ">" : "<") +" latest_minimum || latest_minimum == 0){\n"
															+"latest_minimum = "+ field +";\n"
															+"min_timed = TIMED;\n"
														+"}\n"
															
														+"latest_record = "+ field +";\n"
		                        
		                        +"if(growing_records == 3){\n"
		                        	+"def min_date = new Date(min_timed);\n"
			                        +"def content = min_date.toString() + \": Warning, new threshold in "+vsName+": \" + latest_minimum + \"\";\n"
			                        +"sendNotification(content, \""+regid+"\");\n"
			                        +"growing_records = 0;\n"
			                        +"latest_minimum = 0;\n"
			                      +"}\n"
		                    +"}\n"
												                +"]]>\n"
															+"</param>\n"
														+"</init-params>\n"
													+"<output-structure />\n"
													+"</processing-class>\n"
													+"<description>\n"
													+"</description>\n"
													+"<addressing />\n"
													+"<storage history-size=\"1\" />\n"
													+"<streams>"
														+"<stream name=\"stream1\">\n"
															+"<source alias=\"remote_"+vsName+"\" sampling-rate=\"1\" storage-size=\"1\">\n"
															  +"<address wrapper=\"remote-rest\">\n"
															    +"<predicate key=\"query\">select * from "+vsName+"</predicate>\n"
															    +"<predicate key=\"remote-contact-point\">http://localhost:"+gsnPort+"/streaming/</predicate>\n"
															    +"<predicate key=\"start-time\">"+now+"</predicate>\n"
															  +"</address>\n"
															  +"<query>select * from wrapper</query>\n"
															+"</source>\n"
															+"<query>select * from remote_"+vsName+"</query>\n"
														+"</stream>\n"
													+"</streams>\n"
												+"</virtual-sensor>\n";
				bw.write(outputXml);
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Remove a virtual sensor triggering a notification. If no more notifications are associated with the given {@code regid},
	 * the corresponding device is removed from the list of registered devices.
	 * 
	 * @param regid Registration ID of the device
	 * @param vsName Virtual Sensor name
	 * @param field Virtual Sensor field
	 * @return
	 */
	public static boolean remove(final String regid, String vsName, String field){
		String filename = regid+vsName+field+".xml";
		File fh = new File(VS_URL+filename);
		try{
			if(fh.exists()){
				fh.setWritable(true);
				fh.delete();
				
				// Check whether there are no more virtual sensors set for this device,
				// If that is the case, remove the device from the android.devices list
			    File folder = new File(VS_URL);
			    FilenameFilter filter = new FilenameFilter() {
					
					@Override
					public boolean accept(File dir, String name) {
						return name.startsWith(regid);
					}
				};
		    File[] listOfFiles = folder.listFiles(filter);
		    
		    if(listOfFiles.length == 0)
		    	Datastore.unregister(regid);
		    return true;
			}
			return false;
		} catch (SecurityException e){
			logger.info(e.getMessage());
			return false;
		}
	}

	/**
	 * Remove all files associated with device {@code regid} and unregisters device.
	 * This method differs from the above one for this unregisters the device and removes all its associated VSs,
	 * whereas the other one removes only one VS and, in case there are no VSs left, unregisters the device, too.
	 * 
	 * @param regid Device registration ID
	 * @return {@code true} if all files are successfully deleted, {@code false} if some file was not deleted
	 */
	public static boolean remove(final String regid) {
		// Check whether there are no more virtual sensors set for this device,
		// If that is the case, remove the device from the android.devices list
    File folder = new File(VS_URL);
    final File[] files = folder.listFiles( new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith(regid);
			}
		});
    
    boolean error = false;
    
    for ( final File file : files ) {
    	try{
    		file.setWritable(true);
    		file.delete();
      } catch(SecurityException e){
      	logger.info("Unable to delete file: "+file.getName());
      	error = true;
      }
    }
    
    Datastore.unregister(regid);
    return !error;
	}
}