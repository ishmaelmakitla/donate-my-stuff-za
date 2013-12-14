package org.rhok.pta.donate.gcm;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.rhok.pta.donate.servlets.DonationOffers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.xml.internal.ws.api.message.Headers;

/**
 * This is class for pushing notification through the GSM server. It uses the non-blocking io library.
 * 
 * 
 * @author Ishmael Makitla
 *         GDG/RHoK Pretoria
 *         2013
 *         South Africa
 *
 */
public class DonateMyStuffHttpPushNotifier {
	
	private static final Logger log = Logger.getLogger(DonateMyStuffHttpPushNotifier.class.getSimpleName());
	private static final String GCM_APPLICATION_KEY="AIzaSyD7s6lgYnKNqJlW63yqOloUsRxtfCREpl0";
	private static final String GCM_APPLICATION_ID = "881964398257";
	private static final String GCM_SERVER_SEND_URL ="https://android.googleapis.com/gcm/send";
	private static final String GCM_HEADER_AUTHORIZATION = "Authorization";
	private static final String GCM_CONTENT_TYPE_HEADER = "application/json";
		
	public DonateMyStuffHttpPushNotifier(){
	
	}
		
	/**
	 * This is the FutureCallBack which will be called when the GCM Push POST request returns...
	 */
	private FutureCallback<HttpResponse> gcmHttpFutureCallBack = new FutureCallback<HttpResponse>() {

        public void completed(final HttpResponse response) {
            //What does the MAS return?
        	log.info("Http FutureCallback - Completed");
        	try{
        		String content = EntityUtils.toString(response.getEntity());
        		log.info("Response:: "+content);   
        		//perhaps call some processResponseMethod here...
        	}
        	catch(Exception e){ e.printStackTrace(); }
        }
        public void failed(final Exception ex) {
        	System.out.println("Http-Failed: "+ex.getMessage());
        }
        public void cancelled() {
        	System.out.println("Http-Cancelled");
        }
    };
    
    /**
     * Method for pushing the notification messages to Android devices.		
     * @param registrationIds
     * @param notificationMessage
     * @throws IOException
     */
	public void pushMessage(NotificationMessage notificationMessage) throws IOException{
	    
	    //get JSON version of the notification-content
	    JsonObject jsonGCMMessage = jsonify(notificationMessage.getRecipients(), notificationMessage.getMessage());	 
	    
	    pushMessage(jsonGCMMessage, gcmHttpFutureCallBack);
	    
	}
	
	/**
	 * This method is used to push notification to Android devices...the JSON Object is formatted according to
	 * GCM specifications for GCM HTTP
	 * @throws IOException 
	 */
   
	public static void pushMessage(JsonObject notification, FutureCallback<HttpResponse> callback) throws IOException{
		 HttpPost gcmMessagePushPostRequest = new HttpPost(GCM_SERVER_SEND_URL);	
		    
		    //setting the headers as per http://developer.android.com/google/gcm/http.html
		    gcmMessagePushPostRequest.setHeader(GCM_CONTENT_TYPE_HEADER,"application/json");
		    gcmMessagePushPostRequest.setHeader(GCM_HEADER_AUTHORIZATION, "key="+GCM_APPLICATION_KEY);
		    
		  //now put the JSON as body of the POST request	    
		    HttpEntity entity = null;
			try {
				entity = new StringEntity(notification.toString());
			} catch (UnsupportedEncodingException e) {
				log.severe("Error creating JSON String Entity..."+e.getLocalizedMessage());
			}
			
			if(entity ==null){
				//cannot sent empty content...
				log.severe("Error creating JSON String Entity...Aborting: Cannot Send Empty Message");
				//write error back to avoid client-time-out?
				return;
			}
			
			//if all is well, set the content body..		
		    gcmMessagePushPostRequest.setEntity(entity);
		    //set connection config
		    RequestConfig requestConfig = RequestConfig.custom()
		            .setSocketTimeout(3000)
		            .setConnectTimeout(3000).build();
		   	    
		    CloseableHttpAsyncClient httpclient = HttpAsyncClients.custom()
		            .setDefaultRequestConfig(requestConfig)	            
		            .build();
		   try{ 
		        httpclient.start();
		        httpclient.execute(gcmMessagePushPostRequest,callback);
		   }
		   finally{
			   httpclient.close();
			   log.fine("Done...");
		   }
	}
	
	/**
	 * Method that takes a String Array...
	 */
	public static JsonObject jsonify(String[] registrationIds, String notificationMessage){
		    Gson gson = new Gson();
		    JsonObject jsonGCMMessage = new JsonObject();
		    
		    //array of registration-ids
		    JsonArray jaRegistrationIds = new JsonArray();
		    jaRegistrationIds.add( gson.toJsonTree(registrationIds, String[].class));		    	
		    		    
		    jsonGCMMessage.add("data", gson.toJsonTree(notificationMessage, String.class));	    
		    jsonGCMMessage.add("registration_ids", jaRegistrationIds);
		    		    
		    return jsonGCMMessage;
	}
	
	
}
