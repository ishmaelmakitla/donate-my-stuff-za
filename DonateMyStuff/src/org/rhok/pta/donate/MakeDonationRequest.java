package org.rhok.pta.donate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.net.URLDecoder;
import java.util.Date;
import java.util.UUID;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
	String user = null;
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp)throws IOException {
					
			//get the parameters of the offer -JSON
			String payload = req.getParameter("payload");			
			try {
				if(payload !=null){
				    String decodedPayload = URLDecoder.decode(payload,"UTF-8");
					 doRequest(resp,decodedPayload);				
				}
				else{
					//look for it in the Htt-Content
					processRawDonationRequestData(req,resp);
				}
			}
			catch(IOException ioe){
				ioe.printStackTrace();
				writeOutput(resp," Error: There were issues procesin your donation request");
			}
		
	}
	
	/**
	 * For content that was sent with a custom encoding. This method uses the Reader to read the raw content.
	 * @param request
	 * @param resp
	 */
	private void processRawDonationRequestData(HttpServletRequest request, HttpServletResponse resp){
		
		System.out.println("processRawDonationOfferData(...)");
		
		StringBuffer rawData = new StringBuffer();
		  String line = null;
		  try {
			  	BufferedReader reader = request.getReader();
			  	while ((line = reader.readLine()) != null){
			  		rawData.append(line);
			  	}
			  
			  	System.out.println("processRawDonationOfferData(...) DATA = \n"+rawData);
			  		
		  } catch (Exception e) { e.printStackTrace(); }

		  if(rawData.length()>0){
			  try { doRequest(resp, rawData.toString()); } 
			  catch (IOException e) { e.printStackTrace(); }
		  }
		  else{
			  System.err.println("The data stream is empty - no data received");
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
        	outputWriter.write(jsonResponse);
        }
        catch(IOException ioe){
        	
        }
	}
}
