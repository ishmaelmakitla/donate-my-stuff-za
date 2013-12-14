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

import org.rhok.pta.donate.models.DonationOffer;
import org.rhok.pta.donate.utils.DonateMyStuffConstants;
import org.rhok.pta.donate.utils.DonateMyStuffUtils;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.gson.Gson;
/**
 * Servlet used, by Donoros, to make offers for donation. The offers are stored into the App Engine DataStore.
 * @author Ishmael Makitla
 *
 */
@SuppressWarnings("serial")
public class MakeDonationOffer extends HttpServlet{
	
	private static final Logger log = Logger.getLogger(MakeDonationOffer.class.getSimpleName());
	
	String user = null;
	public void doPost(HttpServletRequest req, HttpServletResponse resp)throws IOException {
		   //get the parameters of the offer
			String payload = req.getParameter("payload");
			log.info("Payload Parameter = "+payload);
			try {
				   if(payload != null){
					   String decodedPayload = URLDecoder.decode(payload, "UTF-8");
					   log.info("Payload Parameter (DECODED) = "+decodedPayload);
				      doOffer(resp,decodedPayload);
				  }
				  else{
					//possibly the StringEntity was used
					//read the stream
					  log.info("Payload Parameter NOT specified - calling: processRawDonationOfferData(...)");
					  processRawDonationOfferData(req,resp);
				  }
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				DonateMyStuffUtils.writeOutput(resp, DonateMyStuffUtils.asServerResponse(DonateMyStuffConstants.ERROR, "Error Processing Your Donation Offer. "+e.getLocalizedMessage()));
			}
		
	}
	/**
	 * For content that was sent with a custom encoding. This method uses the Reader to read the raw content.
	 * @param request
	 * @param resp
	 */
	private void processRawDonationOfferData(HttpServletRequest request, HttpServletResponse resp){
		
		log.info("processRawDonationOfferData(...)");
		String decodedPayload = "";
		StringBuffer rawData = new StringBuffer();
		  String line = null;
		  try {
			  	BufferedReader reader = request.getReader();
			  	while ((line = reader.readLine()) != null){
			  		rawData.append(line);
			  	}
			  	decodedPayload = URLDecoder.decode(rawData.toString(), "UTF-8");
				log.info("processRawDonationOfferData(...):: Payload Parameter (DECODED) = "+decodedPayload);			  
			  		
		  } catch (Exception e) { e.printStackTrace(); }

		  if(rawData.length()>0){
			  try { doOffer(resp, decodedPayload); } 
			  catch (JSONException e) { e.printStackTrace(); DonateMyStuffUtils.writeOutput(resp, DonateMyStuffUtils.asServerResponse(DonateMyStuffConstants.ERROR, "Error Processing Your Donation Offer. "+e.getLocalizedMessage()));}
		  }
		  else{
			  DonateMyStuffUtils.writeOutput(resp, DonateMyStuffUtils.asServerResponse(DonateMyStuffConstants.ERROR, "The data stream is empty - no data received.")); 
		  }
	}
	
	/**
	 * This method is used to process the incoming offer from the user
	 * @throws JSONException 
	 */
	private void doOffer(HttpServletResponse response, String payload) throws JSONException{		
		log.info("doOffer ()... Payload = \n "+payload+"\n");	
		String requestId = UUID.randomUUID().toString();
        Key donationOffersKey = KeyFactory.createKey("DonationOffer", requestId);
        Entity donationOffer = new Entity("DonationOffer", donationOffersKey);
        
        Date date = new Date();
      
        DonationOffer donationOfferObject = (new Gson()).fromJson(payload, DonationOffer.class);  
        
        if(donationOfferObject !=null){  
        	donationOfferObject.setOfferDate(date);
        	
            donationOffer.setProperty("id", requestId);
        	donationOffer.setProperty("donor_id", donationOfferObject.getDonorId());
        	donationOffer.setProperty("date", donationOfferObject.getOfferDate());   
        	        	
        	if(donationOfferObject.getItem()!= null){        		        	
                donationOffer.setProperty("item_name", donationOfferObject.getItem().getName());                
                donationOffer.setProperty("item_size", donationOfferObject.getItem().getSize());
                donationOffer.setProperty("item_age", donationOfferObject.getItem().getAge());
                donationOffer.setProperty("item_age_restriction", donationOfferObject.getItem().getAgeRestriction());
                donationOffer.setProperty("item_gender", donationOfferObject.getItem().getGenderCode());
                donationOffer.setProperty("item_count",donationOfferObject.getQuantity());
                donationOffer.setProperty("item_type",donationOfferObject.getItem().getType());
                donationOffer.setProperty("deliver",donationOfferObject.isDeliver());
        	}
        	        	
        	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            //save the offer
            datastore.put(donationOffer);
            DonateMyStuffUtils.writeOutput(response, DonateMyStuffUtils.asServerResponse(DonateMyStuffConstants.OK, "Donation Offer Has Been Processed."));           
        }
        else{
        	 DonateMyStuffUtils.writeOutput(response, DonateMyStuffUtils.asServerResponse(DonateMyStuffConstants.ERROR, "Donation Offer COULD NOT Be Processed."));  
        }
        
	}	
}
