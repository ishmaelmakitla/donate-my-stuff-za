package org.rhok.pta.donate.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.net.URLDecoder;
import java.util.Date;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.rhok.pta.donate.gcm.PushNotificationRegistration;
import org.rhok.pta.donate.models.RegistrationRequest;
import org.rhok.pta.donate.models.ResidentialAddress;
import org.rhok.pta.donate.utils.DonateMyStuffConstants;
import org.rhok.pta.donate.utils.DonateMyStuffUtils;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * This is the Servlet that handles registrations. The registration is done by the public with either a donor role or
 * beneficiary role.
 * Donors are those who register with an intention to donate stuff. They will submit Donation-Offers
 * Beneficiaries are those registering so they can benefit from good-hearted donors - they will submit Donation-Requests
 * @author Ishmael Makitla
 *         2013
 *         RHoK Pretoria, Google Developer Group, Pretoria
 *         CSIR
 *
 */
public class Register extends HttpServlet{
	
	private static final long serialVersionUID = 7097392428548590292L;
	
	private static final Logger log = Logger.getLogger(Register.class.getSimpleName());
	
	//this servlet allows registrations that post a JSON document or parameters
	/**
	 * Method that handles the POST request
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)throws IOException {
		String payload = request.getParameter("payload");
		log.info("Payload Parameter = "+payload);
		
			if(payload != null){
				String decodedPayload = URLDecoder.decode(payload, "UTF-8");
				log.info("Payload Parameter (DECODED) = "+decodedPayload);
				RegistrationRequest registration = (new Gson()).fromJson(decodedPayload, RegistrationRequest.class);
					
				    if(registration != null){
					     doRegister(registration, response);				
				       }
				   else{
					     log.severe("Was Unable To Deserialize POST-Data :: "+decodedPayload);
					     DonateMyStuffUtils.writeOutput(response, DonateMyStuffUtils.asServerResponse(DonateMyStuffConstants.REGISTRATION_FAILED, "Errors Deserializing the Registartion JSON"));
				       }
				  
			  }
			  else{
				  log.info("Payload Parameter NOT specified - calling: processRawRegisterData(...)");
				  processRawRegisterData(request,response);
			  }
		
	}
	/**
	 * Method that ahandles the GET request
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)throws IOException {
		String payload = request.getParameter("payload");
		log.info("doGet(...):: Payload = "+payload);
		
		if(payload !=null){
			//process as JSON document	
			String decodedPayload = URLDecoder.decode(payload, "UTF-8");
			log.info("Payload Parameter (DECODED) = "+decodedPayload);
			RegistrationRequest registration = (new Gson()).fromJson(decodedPayload, RegistrationRequest.class);
			if(registration != null){
				doRegister(registration, response);				
			}
			else{
				log.severe("Errors Deserializing the Registartion JSON");
				DonateMyStuffUtils.writeOutput(response, DonateMyStuffUtils.asServerResponse(DonateMyStuffConstants.REGISTRATION_FAILED, "Errors Deserializing the Registartion JSON"));
			}
		}
		
	}
	
	/**
	 * For content that was sent with a custom encoding. This method uses the Reader to read the raw content.
	 * @param request
	 * @param resp
	 */
	private void processRawRegisterData(HttpServletRequest request, HttpServletResponse resp){
		
		log.info("processRawRegisterData(...)");
		
		StringBuffer rawData = new StringBuffer();
		  String line = null;
		  try {
			  	BufferedReader reader = request.getReader();
			  	while ((line = reader.readLine()) != null){
			  		rawData.append(line);
			  	}
			  
			 log.info("processRawRegisterData(...) DATA = \n"+rawData);
			  		
		  } catch (Exception e) { e.printStackTrace(); }

		  if(rawData.length()>0){
			  
			  try{
			  			String decodedPayload = URLDecoder.decode(rawData.toString(), "UTF-8");
			  			log.info("Payload Parameter (DECODED) = "+decodedPayload);
			  			RegistrationRequest registration = (new Gson()).fromJson(decodedPayload, RegistrationRequest.class);
			  			if(registration != null){
			  				doRegister(registration, resp);				
			  			}
			  			else{
			  				String msg = "Errors Deserializing the Registartion JSON";
			  				log.severe(msg);
			  				DonateMyStuffUtils.writeOutput(resp, DonateMyStuffUtils.asServerResponse(DonateMyStuffConstants.REGISTRATION_FAILED, msg));
			  			}
			  }
			  catch(IOException ioe){ log.severe("Error Processing Raw Register Data : "+ioe.getLocalizedMessage()); }
		  }
		  else{
			  log.severe("The data stream is empty - no data received");
			  DonateMyStuffUtils.writeOutput(resp, DonateMyStuffUtils.asServerResponse(DonateMyStuffConstants.REGISTRATION_FAILED, "Registration Failed"));
		  }
	}
	
	
	/**
	 * 
	 * @param registration
	 * @param response
	 * @throws IOException
	 */
	private void doRegister(RegistrationRequest registration, HttpServletResponse response) throws IOException{
     
		if(DonateMyStuffUtils.userExists(registration.getUsername(), registration.getEmail())){
			String error = "User Registration Failed: User Already Exists";
        	DonateMyStuffUtils.writeOutput(response, DonateMyStuffUtils.asServerResponse(DonateMyStuffConstants.REGISTRATION_FAILED, error));
			return;
		}
        
        Date date = new Date();
        
        //set reg id
        String id = registration.getRegistrationID();
        if(id == null){ id = (new RegistrationRequest()).getRegistrationID(); }
        
        Key registrationRequestsKey = KeyFactory.createKey("RegistrationRequest", id);
        Entity registrationRequest = new Entity("RegistrationRequest", registrationRequestsKey);
        
        //set id
        registrationRequest.setProperty("id", id);
        //set type
        registrationRequest.setProperty("type", registration.getType());
        //set name
        registrationRequest.setProperty("name", registration.getName());
        //set surname
        registrationRequest.setProperty("surname", registration.getSurname());
        //set username
        registrationRequest.setProperty("username", registration.getUsername());
        //set password
        registrationRequest.setProperty("password", registration.getPassword());
        //set mobile phone
        registrationRequest.setProperty("mobile", registration.getMobile());
        //set telephone number
        registrationRequest.setProperty("telephone", registration.getTelephone());
        //set email
        registrationRequest.setProperty("email", registration.getEmail());
        //set address values
        ResidentialAddress address = registration.getAddress();
        if(address !=null){
           registrationRequest.setProperty("address", address.toString());
        }
        else{
        	log.info("WARNING - Address Could not be found...");
        }
        //set role (donor/beneficiary)
        registrationRequest.setProperty("role", registration.getRole());      
        
        registrationRequest.setProperty("creation_date", date);      
        
        //put into data store
        String result = "Registration Successful";
        try{
        	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        	//save the registration in the db
            datastore.put(registrationRequest);
            
            
            //now check if the GCM registration  was part of this registration as well...
            PushNotificationRegistration gcmRegistration = registration.getGcmregistration();
            if(gcmRegistration !=null){
            	//do GCM_Registration
            	Key newEntryKey = doGCMRegistration(gcmRegistration, response);
            	if(newEntryKey !=null){
        			result += ",Registration on GCM Server (DMS) Successful.";	
        		}
        		else{
        			result += ", Could not Process the GCM Registration on Server (DMS).";	
        		}	
            }
            DonateMyStuffUtils.writeOutput(response, DonateMyStuffUtils.asServerResponse(DonateMyStuffConstants.REGISTRATION_SUCCESSFULL, result));
        }
        catch(Exception e){
        	e.printStackTrace();
        	log.severe("Error Registering: "+e.getLocalizedMessage());
        	result = "Registration Failed";
        	DonateMyStuffUtils.writeOutput(response, DonateMyStuffUtils.asServerResponse(DonateMyStuffConstants.REGISTRATION_FAILED, result));
        }
        
	}
	
	/**
	 * Utility method for storing the registration details for this beneficiary/donor as part of GCM-Push Notification Registration
	 * 
	 * @param registration
	 */
	private Key doGCMRegistration(PushNotificationRegistration registration, HttpServletResponse response){
		
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
		
		return newEntryKey;
	}

}
