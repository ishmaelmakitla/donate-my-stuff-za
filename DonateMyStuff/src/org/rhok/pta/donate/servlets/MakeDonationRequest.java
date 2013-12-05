package org.rhok.pta.donate.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.net.URLDecoder;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.rhok.pta.donate.models.DonationRequest;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.gson.Gson;
/**
 * Servlet that handles requests for donations and stores these in the Google App Engine DataStore.
 * @author Ishmael Makitla
 *
 */
@SuppressWarnings("serial")
public class MakeDonationRequest extends HttpServlet{
	private static final Logger log = Logger.getLogger(MakeDonationRequest.class.getSimpleName());
	String user = null;
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp)throws IOException {
					
			//get the parameters of the offer -JSON
			String payload = req.getParameter("payload");
			log.info("Payload Parameter = "+payload);
			
			try {
				if(payload !=null){
				     String decodedPayload = URLDecoder.decode(payload,"UTF-8");
				     log.info("Payload Parameter (DECODED) = "+decodedPayload);
					 doRequest(resp,decodedPayload);				
				}
				else{
					//look for it in the Htt-Content
					log.info("Payload Parameter NOT specified - calling: processRawDonationRequestData(...)");
					processRawDonationRequestData(req,resp);
				}
			}
			catch(IOException ioe){
				ioe.printStackTrace();
				log.severe(" Error: There were issues procesin your donation request: \n "+ioe.getLocalizedMessage());
			}
		
	}
	
	/**
	 * For content that was sent with a custom encoding. This method uses the Reader to read the raw content.
	 * @param request
	 * @param resp
	 */
	private void processRawDonationRequestData(HttpServletRequest request, HttpServletResponse resp){
		
		log.info("processRawDonationRequestData(...)");
		
		StringBuffer rawData = new StringBuffer();
		  String line = null;
		  try {
			  	BufferedReader reader = request.getReader();
			  	while ((line = reader.readLine()) != null){
			  		rawData.append(line);
			  	}
			  
			  	log.info("processRawDonationOfferData(...) DATA = \n"+rawData);
			  		
		  } catch (Exception e) { e.printStackTrace(); }

		  if(rawData.length()>0){
			  try { doRequest(resp, rawData.toString()); } 
			  catch (IOException e) { log.severe("Error While Reading RawData Payload: "+e.getLocalizedMessage()); e.printStackTrace(); }
		  }
		  else{
			  log.severe("The data stream is empty - no data received");
		  }
	}
	
	/**
	 * This method is used to process the request for donation
	 * @throws IOException 
	 */
	private void doRequest(HttpServletResponse response, String payload) throws IOException{
		String requestId = UUID.randomUUID().toString();
		Key donationRequestsKey = KeyFactory.createKey("DonationRequest", requestId);
        
        Date date = new Date();
        Entity donationRequest = new Entity("DonationRequest", donationRequestsKey);
        
        
        //payload is JSON data, parse it here and deserialize and store into DataStore
        Gson gson = new Gson();
        DonationRequest donationRequestObject = gson.fromJson(payload, DonationRequest.class);
        
        if(donationRequestObject !=null){
        	
        	donationRequest.setProperty("beneficiary", donationRequestObject.getBeneficriaryId());
            donationRequest.setProperty("date", donationRequestObject.getRequestDate());
            
            donationRequest.setProperty("id", requestId);
            donationRequest.setProperty("item_type", donationRequestObject.getRequestedDonationItem().getType());
            donationRequest.setProperty("item_name", donationRequestObject.getRequestedDonationItem().getName());
            donationRequest.setProperty("item_size", donationRequestObject.getRequestedDonationItem().getSize());
            donationRequest.setProperty("item_age", donationRequestObject.getRequestedDonationItem().getAge());
            donationRequest.setProperty("item_age_restriction", donationRequestObject.getRequestedDonationItem().getAgeRestriction());
            donationRequest.setProperty("item_gender", donationRequestObject.getRequestedDonationItem().getGenderCode());
            donationRequest.setProperty("donation_offer_id", donationRequestObject.getDonationOfferId());
            
            
            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            //save the request for donation
            datastore.put(donationRequest);
            String message = "{\"status\": 200}";
            writeOutput(response,message);
        }
        else{
        	String message = "{\"status\": 500}";
            writeOutput(response,message);
        }    
        
	}
	
	/**
	 * This method is used to write the output (JSON)
	 * @param response - response object of the incoming HTTP request
	 * @param output - message to be out-put
	 */
	private void writeOutput(HttpServletResponse response,String output){
		//send back JSON response
        String jsonResponse = new Gson().toJson(output);
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try{
        	Writer outputWriter = response.getWriter();
        	log.info("Returning :: "+jsonResponse);
        	outputWriter.write(jsonResponse);
        }
        catch(IOException ioe){
        	
        }
	}
}
