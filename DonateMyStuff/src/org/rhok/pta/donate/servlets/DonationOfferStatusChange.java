package org.rhok.pta.donate.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.rhok.pta.donate.models.DonationStatusChangeRequest;
import org.rhok.pta.donate.utils.DonateMyStuffConstants;
import org.rhok.pta.donate.utils.DonateMyStuffUtils;

import com.google.appengine.api.datastore.Key;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.gson.Gson;

/**
 * This servlet is used to handle the request to change status of an Offer.
 * 
 * @author Ishmael Makitla
 *			GDG/RHoK Pretoria
 *			South Africa
 *			2014
 *
 */
public class DonationOfferStatusChange extends HttpServlet{
	
	private static final long serialVersionUID = -5603856616455738969L;
	private static final Logger log = Logger.getLogger(DonationOfferStatusChange.class.getSimpleName());
		
	public void doPost(HttpServletRequest request, HttpServletResponse response)throws IOException {
		
		String payload = request.getParameter("payload");
		String decodedPayload = null;
		log.info("Payload Parameter = "+payload);
		if(payload != null && !payload.isEmpty()){
			//decode
			decodedPayload = URLDecoder.decode(payload, "UTF-8");			
		}
		else{
			String rawPayload = getRawDataPayload(request,response);
			//decode
			decodedPayload = URLDecoder.decode(rawPayload, "UTF-8");
		}
		
		if(decodedPayload != null){
			log.info("Donation-Bid Payload Parameter (DECODED) = "+decodedPayload);
			//deserialize incoming data into a DonationStatusChangeRequest
			DonationStatusChangeRequest statusChangeRequest = (new Gson()).fromJson(decodedPayload, DonationStatusChangeRequest.class); 
			//process the request
			processStatusChangeRequest(statusChangeRequest,response);
		}
	}
  
	/**
	 * For content that was sent with a custom encoding. This method uses the Reader to read the raw content.
	 * @param request
	 * @param resp
	 */
	private String getRawDataPayload(HttpServletRequest request, HttpServletResponse resp){
		
		log.info("getRawDataPayload(...)");
		  String rawDataString = null;
		  StringBuffer rawData = new StringBuffer();
		  String line = null;
		  try {
			  	BufferedReader reader = request.getReader();
			  	while ((line = reader.readLine()) != null){
			  		rawData.append(line);
			  	}
			  	
			  	rawDataString = URLDecoder.decode(rawData.toString(), "UTF-8");
			  	log.info("DonationOfferStatusChange.getRawDataPayload(...) DATA (Decoded) = \n"+rawDataString);
			  		
		  } catch (Exception e) { log.severe("ERROR Executing getRawDataPayload() \n"+e.getLocalizedMessage()); e.printStackTrace(); }
		  
		  return rawDataString;
	}
	
    /**
     * This method processes the request by effecting to status change
     * @param request
     * @param response
     */
    
	private void processStatusChangeRequest(DonationStatusChangeRequest request, HttpServletResponse response){
		Entity donationOfferEntity = getOffer(request.getId());
		if(donationOfferEntity == null){
		   //offer not found or status-change error
			DonateMyStuffUtils.writeOutput(response, DonateMyStuffUtils.asServerResponse(DonateMyStuffConstants.ERROR, "Specified Donation Offer Not Found"));	
			return;
		}
		
		//so we found the offer, modify and set new status and then save the entity back		
	     Entity updatedDonationOfferEntity = donationOfferEntity;
	     //set new status
	     updatedDonationOfferEntity.setProperty("status", request.getStatus());
	     //set who changed the status
	     updatedDonationOfferEntity.setProperty("status_by", request.getUserid());
	     
	     //save
	     DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();	
	     Key newEntry = datastore.put(updatedDonationOfferEntity);
	     if(newEntry != null){
	    	 DonateMyStuffUtils.writeOutput(response, DonateMyStuffUtils.asServerResponse(DonateMyStuffConstants.OK, "Status Change Successful"));	 
	     }
	     else{
	    	 DonateMyStuffUtils.writeOutput(response, DonateMyStuffUtils.asServerResponse(DonateMyStuffConstants.ERROR, "Could Not Save Status Change"));	
	     }
	}
	
	/**
	 * Get the offer in question...
	 * @param id
	 * @return
	 */
	private Entity getOffer(String id){
		
		log.info("getOffer  - Offer-ID ("+id+")");
			
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();	
		
		Query offerQuery = new Query("DonationOffer");
		//where ID is the specified ID (offer-id)
		Filter offerIdFilter = new Query.FilterPredicate("id", FilterOperator.EQUAL, id);
			
		offerQuery.setFilter(offerIdFilter);
		Entity donationOffer = datastore.prepare(offerQuery).asSingleEntity();					
		
		return donationOffer;
	}
	
}
