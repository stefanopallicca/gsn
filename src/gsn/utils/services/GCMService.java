package gsn.utils.services;

import gsn.gcm.server.*;
import gsn.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.jfree.util.Log;

public class GCMService{
	private static int time = 0;
	private static final transient Logger logger = Logger.getLogger(GCMService.class);
	
	public static void sendNotification(String content, String dest){
		logger.warn(content);
		/*if(time > 0) return;
		time = 1;*/
		HttpPost post = new HttpPost("http://localhost:22001/gcm/sendAll");
		List<NameValuePair> contList = new ArrayList<NameValuePair>();
		contList.add(new BasicNameValuePair("body", content));
		contList.add(new BasicNameValuePair("dest", dest));
		try{
			post.setEntity(new UrlEncodedFormEntity(contList));
			HttpClient client = new DefaultHttpClient();
			HttpResponse response = client.execute(post);
		} catch (IOException e){ e.printStackTrace(); };
	}
	
}