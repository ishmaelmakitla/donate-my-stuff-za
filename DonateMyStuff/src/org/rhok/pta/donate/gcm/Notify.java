package org.rhok.pta.donate.gcm;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.rhok.pta.donate.models.DonationBid;
import org.rhok.pta.donate.utils.DonateMyStuffConstants;
import org.rhok.pta.donate.utils.DonateMyStuffUtils;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.gson.Gson;


/**
 * This is the servlet used to send notifications to running instances of the Donate-My-Stuff Android App.
 * This servlets uses the Google Cloud Messaging Platform to send out notifications.
 * The notifications are sent under the following conditions:
 * 1. When Status of Donation-Request is changed (beneficiary is notified)
 * 2. When a Donation-Offer is made that matches Donation-Request 
 *    and it is not in response to the same or any other Donation-Request (beneficiary is notified)
 * 3.When status of Donation-Offer changes (donor is notified)
 * 4.When a Donation-Request is made that matches the offer 
 *   and is not made in response to this Request nor any other request (donor is notified)   
 * 
 * @author Ishmael Makitla 
 *         GDG/RHoK Pretoria, South Africa
 *         2013
 * private static final String GCM_APPLICATION_KEY="AIzaSyD7s6lgYnKNqJlW63yqOloUsRxtfCREpl0";
 * private static final String GCM_APPLICATION_ID = "881964398257";
 */
public class Notify extends HttpServlet{
	
	private static final Logger log = Logger.getLogger(Notify.class.getSimpleName());
 
	public void doPost(HttpServletRequest request, HttpServletResponse response)throws IOException {
		String payload = request.getParameter("payload");
		String decodedPayload = null;
		log.info("Payload Parameter = "+payload);
		if(payload != null && !payload.isEmpty()){
			//decode
			decodedPayload = URLDecoder.decode(payload, "UTF-8");			
		}
		else{
			String rawPayload = DonateMyStuffUtils.getRawDataPayload(request,response);
			//decode
			decodedPayload = URLDecoder.decode(rawPayload, "UTF-8");
		}
		
		if(decodedPayload != null){
			log.info("Donation-Bid Payload Parameter (DECODED) = "+decodedPayload);
			//deserialize incoming data into a PushNotificationRegistration
			PushNotificationRegistration gcmRegistration = (new Gson()).fromJson(decodedPayload, PushNotificationRegistration.class); 
			doRequest(gcmRegistration, response);
		}
		else{
			//registration failed, write back to requestor
			DonateMyStuffUtils.writeOutput(response, DonateMyStuffUtils.asServerResponse(DonateMyStuffConstants.ERROR, "No Data Received. GCM Registration on Server Cannot Continue"));	
		}
		
	}
	
	/**
	 * Utility method for storing the registration details for this beneficiary/donor
	 * @param registration
	 */
	private void doRegistration(PushNotificationRegistration registration, HttpServletResponse response){
		
		//at this stage everything went well...
		Key gcmRegistrationKey = KeyFactory.createKey("PushNotificationRegistration", registration.getRegistration_id());
		Entity gcmRegistrationEntity = new Entity("PushNotificationRegistration", gcmRegistrationKey);
		
		//record Registration date as NOW (on the GCM Server)
		gcmRegistrationEntity.setProperty("date", (new Date()));
		//set reg_id
		gcmRegistrationEntity.setProperty("registration_id",registration.getRegistration_id());
		//set beneficiary/donor ID as dms_id
		gcmRegistrationEntity.setProperty("dms_id", registration.getDms_id());
		//set beneficiary/donor name as handle
		gcmRegistrationEntity.setProperty("handle", registration.getHandle());
				
		//save Registartion into the Datastore			
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();            
        Key newEntryKey = datastore.put(gcmRegistrationEntity);
        
		if(newEntryKey !=null){
			DonateMyStuffUtils.writeOutput(response, DonateMyStuffUtils.asServerResponse(DonateMyStuffConstants.OK, "Registration on GCM Server (DMS) Has Been Processed."));	
		}
		else{
			DonateMyStuffUtils.writeOutput(response, DonateMyStuffUtils.asServerResponse(DonateMyStuffConstants.ERROR, "Could not Process the GCM Registration on Server (DMS)."));	
		}			
	}
	
	/**
	 * Method for sending out a notification - 
	 * @param payload
	 * @param response
	 */
	private void sendNotification(PushNotificationRegistration notification, HttpServletResponse response){
		log.info(" sendNotification () :: Message = "+notification.getMessage());
		//instantiate notifier to push the message-
		DonateMyStuffHttpPushNotifier notifier = new DonateMyStuffHttpPushNotifier();
		NotificationMessage message = notification.getMessage();
		
		try {
			notifier.pushMessage(message);
		  } catch (IOException e) {
			log.severe("Unable to send Notification:: "+e.getLocalizedMessage());
			DonateMyStuffUtils.writeOutput(response, DonateMyStuffUtils.asServerResponse(DonateMyStuffConstants.ERROR, "Could not Send Notification. Reason is "+e.getLocalizedMessage()));	
		 }
	}
	
	/**
	 * Utility method for processing the incoming request - to check if this a Registration request or notification push request
	 * 
	 * @param payload
	 * @param response
	 */
	private void doRequest(PushNotificationRegistration payload, HttpServletResponse response){
		if(payload == null){
			DonateMyStuffUtils.writeOutput(response, DonateMyStuffUtils.asServerResponse(DonateMyStuffConstants.ERROR, "Could not Process the GCM Request. Deserialized PushNotificationRegistration is NULL"));	
			return;
		}
		//otherwise check what is requested - new registration or sending out of a message
		switch(payload.getOpcode()){
		case PushNotificationRegistration.REGISTER:
			doRegistration(payload, response);
			break;
		case PushNotificationRegistration.SEND:
			sendNotification(payload,response);
			break;
		}
	}
}
