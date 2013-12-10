package org.rhok.pta.donate.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.rhok.pta.donate.models.DonationBid;
import org.rhok.pta.donate.models.DonationOffer;
import org.rhok.pta.donate.utils.DonateMyStuffConstants;
import org.rhok.pta.donate.utils.DonateMyStuffUtils;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * This Servlet is used to handle requests for bids. A bid is placed by a beneficiary in response to a donation-offer.
 * The bid MUST include both the Original Donation-Request ID and the Original Donation-Offer ID.
 * 
 * @author Ishmael Makitla
 *         GDG/RHoK Pretoria
 *         South Africa
 *         2013
 *
 */
public class Bid extends HttpServlet{
	
	private static final Logger log = Logger.getLogger(Bid.class.getSimpleName());
	
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
			//deserialize incoming data into a BidRequest
			DonationBid bid = (new Gson()).fromJson(decodedPayload, DonationBid.class); 
			//process the bid request
			processDonationBid(bid,response);
		}
		
	}
	
	/**
	 * Method for retrieving list of Bid made by a particular bidder/beneficiary
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse resp) throws IOException {
		String beneficiary_id = request.getParameter("beneficiary_id");
		log.info("doGet::beneficiary_id Parameter = "+beneficiary_id);
		if(beneficiary_id != null && !beneficiary_id.trim().isEmpty()){
			//get bids Entities
			List<Entity> bidEntities = getUserBids(getServletInfo());
			if(bidEntities !=null && bidEntities.size() >0){
				//convert the entities into DonationBid objects
				List<DonationBid> bids = convertFromEntities(bidEntities);
				String bidsJsonDoc = asJsonDocument(bids);
				//set the JSON document over
				DonateMyStuffUtils.writeOutput(resp,bidsJsonDoc);
			}
		}
	}
	
	/**
	 * This method retrieves a list of Bids made by a user.
	 * 
	 * @param beneficiary_id - ID of the user as beneficiary who made them
	 * @return
	 */
	private List<Entity> getUserBids(String beneficiary_id){
		List<Entity> bids = null;
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();     
		Query query = new Query("DonationBid").addSort("date", Query.SortDirection.DESCENDING);
		
		if(beneficiary_id != null && !beneficiary_id.trim().isEmpty()){
			Filter beneficiaryIdFilter = new Query.FilterPredicate("beneficiary", FilterOperator.EQUAL, beneficiary_id);
			query.setFilter(beneficiaryIdFilter);
		}
		           
		bids = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(20));
		
		return bids;
	}
	
	/**
	 * 
	 * @param donationBids
	 * @return
	 */
	private List<DonationBid> convertFromEntities(List<Entity> bidEntities){
		List<DonationBid> bids = new ArrayList<DonationBid>();
		
		for(Entity bid: bidEntities){
			
			//record bid date as NOW
			Date bidDate       = (Date)bid.getProperty("date");
			// bid_id
			String bidId       = (String)bid.getProperty("bid_id");
			// bidder
			String beneficiary = (String)bid.getProperty("beneficiary");
			// offer-id
			String offerId     = (String)bid.getProperty("offer_id");
			//request-id
			String requestId   = (String)bid.getProperty("request_id");
			
			DonationBid bidObject = new DonationBid(bidId,offerId,requestId,beneficiary); 
			bidObject.setDate(bidDate);
			
			//add to list of bids for this user
			bids.add(bidObject);
		}
		
		return bids;
	}
	
	/**
	 * Method used to convert a list of DonationBids into a JSON document
	 * @param bids - the bids made by this user as beneficiary
	 * @return
	 */
	private String asJsonDocument(List<DonationBid> bids){
		String doc = "";
		
		Gson gson =  new Gson();
		JsonObject bidsJsonObject = new JsonObject();
		JsonArray jaBids = new JsonArray();
				
		for(DonationBid aBid: bids){	
			jaBids.add(gson.toJsonTree(aBid, DonationBid.class));		
		}
		
		bidsJsonObject.add("bids", jaBids);
		
		doc = bidsJsonObject.toString();
		
		return doc;
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
			  	log.info("processRawDonationOfferData(...) DATA (Decoded) = \n"+rawDataString);
			  		
		  } catch (Exception e) { log.severe("ERROR Executing getRawDataPayload() \n"+e.getLocalizedMessage()); e.printStackTrace(); }
		  
		  return rawDataString;

	}
	
	/**
	 * This method is called in order to process the bid request. Each bid is stored into datastore and can be retrieved by the owner
	 * in order to either cancel, or modify it.
	 * 
	 * @param bid - the bid being made
	 * @param response
	 */
	private void processDonationBid(DonationBid bid, HttpServletResponse response){
		
		if(bid == null){
			DonateMyStuffUtils.writeOutput(response, DonateMyStuffUtils.asServerResponse(DonateMyStuffConstants.ERROR, "Could not Process the Bid. Deserialized Bid is NULL"));	
			return;
		}
		
		//first check if this bid can be processed - the donation-offer and donation-requests MUST exist for a bid to be processed
		boolean offerExists = itExists(bid.getOfferid(), DONATION_OFFER, bid.getBeneficiaryid());
		boolean requestExists = itExists(bid.getRequestid(), DONATION_REQUEST, bid.getBeneficiaryid()); 
		
		if(offerExists && requestExists){
			//now record this as a bid
			if(bid.getId() == null || bid.getId().trim().isEmpty()){
				String bid_id = (new DonationBid()).getId();
				bid.setId(bid_id);
			}
			
			Key donationBidKey = KeyFactory.createKey("DonationBid", bid.getId());
			Entity bidEntity = new Entity("DonationBid", donationBidKey);
			
			//record bid date as NOW
			bidEntity.setProperty("date", (new Date()));
			//set bid_id
			bidEntity.setProperty("bid_id", bid.getId());
			//set bidder
			bidEntity.setProperty("beneficiary", bid.getBeneficiaryid());
			//set offer-id
			bidEntity.setProperty("offer_id", bid.getOfferid());
			//set request-id
			bidEntity.setProperty("request_id", bid.getRequestid());
			
			//save Bid into the Datastore			
			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();            
            Key newEntryKey = datastore.put(bidEntity);
            
			if(newEntryKey !=null){
				DonateMyStuffUtils.writeOutput(response, DonateMyStuffUtils.asServerResponse(DonateMyStuffConstants.OK, "Bidding for Donation Has Been Processed."));	
			}
			else{
				DonateMyStuffUtils.writeOutput(response, DonateMyStuffUtils.asServerResponse(DonateMyStuffConstants.ERROR, "Could not Process the Bid."));	
			}			
		}
		else{
			DonateMyStuffUtils.writeOutput(response, DonateMyStuffUtils.asServerResponse(DonateMyStuffConstants.ERROR, "ERROR - Bidding for Donation Could NOT Be Processed. Could not find Original Offer or Request"));
		}
		
	}
	
	private static final int DONATION_OFFER = 0;
	private static final int DONATION_REQUEST = 1;
	/**
	 * 
	 * @param id - ID or either the Offer or the Request for Donation
	 * @param type - type of ID
	 * @return - true if a record is found or false otherwise
	 */
	private boolean itExists(String id, int type, String beneficiaryId){
		boolean exists = false;
		
		log.info("itExists ("+id+") Type: "+type+" ?");
			
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();				
		
		switch(type){
		case DONATION_OFFER:
			//so here we are checking if the offer exists
			Query offersQuery   = new Query("DonationOffer");
			Filter offerIdFilter = new Query.FilterPredicate("id", FilterOperator.EQUAL, id);
			offersQuery.setFilter(offerIdFilter);
			
			Entity donationOffer = datastore.prepare(offersQuery).asSingleEntity();
			if(donationOffer !=null){
				//the record exists
				exists = true;
			}
			
			break;
		case DONATION_REQUEST:
			Query requestsQuery = new Query("DonationRequest");
			//where ID is the specified ID (request-id)
			Filter requestIdFilter = new Query.FilterPredicate("id", FilterOperator.EQUAL, id);
			//and beneficiary ID is the same as this bidder
			Filter beneficiaryIdFilter = new Query.FilterPredicate("beneficiary", FilterOperator.EQUAL, beneficiaryId);
			CompositeFilter beneficiaryDonationReqCompositeFilter = new CompositeFilter(CompositeFilterOperator.AND, Arrays.asList(requestIdFilter, beneficiaryIdFilter));
			
			requestsQuery.setFilter(beneficiaryDonationReqCompositeFilter);
			Entity donationRequest = datastore.prepare(requestsQuery).asSingleEntity();
			if(donationRequest != null){
				//the record exists
				exists = true;
			}
			break;
		}
		
		return exists;
	}
		
	
	/**
	 * This method is used to write the output (JSON)
	 * @param response - response object of the incoming HTTP request
	 * @param output - message to be out-put
	 */
	private void writeOutput(HttpServletResponse response,String output){
		//send back JSON response       
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try{
        	 Writer outputWriter = response.getWriter();
        	 log.info("Returning :: "+output);
        	outputWriter.write(output);
        }
        catch(IOException ioe){
        	log.severe("Error Writing output JSON: "+ioe.getLocalizedMessage());
        }
	}
}
