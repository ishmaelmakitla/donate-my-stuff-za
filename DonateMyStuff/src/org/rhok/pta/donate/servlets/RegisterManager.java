package org.rhok.pta.donate.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.rhok.pta.donate.models.ManagerRegistrationRequest;
import org.rhok.pta.donate.models.ResidentialAddress;
import org.rhok.pta.donate.utils.DonateMyStuffConstants;
import org.rhok.pta.donate.utils.DonateMyStuffUtils;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.gson.Gson;

public class RegisterManager extends HttpServlet{

	private static final long serialVersionUID = 1157837689152346680L;
	
	private static final Logger log = Logger.getLogger(RegisterManager.class.getSimpleName());
	
	
	/**
	 * Method that handles the POST request for manager registrations
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)throws IOException {
		String payload = request.getParameter("payload");
		log.info("Payload Parameter = "+payload);
		
			if(payload != null){
				String decodedPayload = URLDecoder.decode(payload, "UTF-8");
				log.info("Payload Parameter (DECODED) = "+decodedPayload);
				ManagerRegistrationRequest registration = (new Gson()).fromJson(decodedPayload, ManagerRegistrationRequest.class);
					
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
			  			ManagerRegistrationRequest registration = (new Gson()).fromJson(decodedPayload, ManagerRegistrationRequest.class);
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
	 * Method for doing Manager Registration process
	 * @param registration
	 * @param response
	 * @throws IOException
	 */
	private void doRegister(ManagerRegistrationRequest registration, HttpServletResponse response) throws IOException{
     
        
        Date date = new Date();
        
        //set reg id
        String id  = UUID.randomUUID().toString(); 
        registration.setId(id);
        Key registrationRequestsKey = KeyFactory.createKey("DonationManager", id);
        Entity registrationRequest = new Entity("DonationManager", registrationRequestsKey);
        
        //set id
        registrationRequest.setProperty("id", id);
        //set type
        registrationRequest.setProperty("agency_name", registration.getAgencyName());
        //set name
        registrationRequest.setProperty("name", registration.getName());
               
        //set mobile phone
        registrationRequest.setProperty("mobile", registration.getMobile());
        //set telephone number
        registrationRequest.setProperty("telephone", registration.getTelephone());
        //set email
        registrationRequest.setProperty("email", registration.getEmail());
        //set password
        registrationRequest.setProperty("password", registration.getPassword());
        
        //set address values
        ResidentialAddress address = registration.getAddress();
        if(address !=null){
           registrationRequest.setProperty("address", address.toString());
        }
        else{
        	log.info("WARNING - Address Could not be found...");
        }
       
        registrationRequest.setProperty("creation_date", date);      
        
        //put into data store
        String result = "Registration Successful";
        try{
        	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        	//save the registration in the db
            datastore.put(registrationRequest);            
            DonateMyStuffUtils.writeOutput(response, DonateMyStuffUtils.asServerResponse(DonateMyStuffConstants.REGISTRATION_SUCCESSFULL, result));
        }
        catch(Exception e){
        	e.printStackTrace();
        	log.severe("Error Registering: "+e.getLocalizedMessage());
        	result = "Manager Registration Failed";
        	DonateMyStuffUtils.writeOutput(response, DonateMyStuffUtils.asServerResponse(DonateMyStuffConstants.REGISTRATION_FAILED, result));
        }
        
	}

}
