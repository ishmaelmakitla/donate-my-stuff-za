package org.rhok.pta.donate.servlets;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.rhok.pta.donate.models.DonatedItem;
import org.rhok.pta.donate.models.DonationRequest;
import org.rhok.pta.donate.utils.DonateMyStuffConstants;
import org.rhok.pta.donate.utils.DonateMyStuffUtils;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * This is the Servlet for retrieving list of Donation Requests
 * @author Ishmael Makitla
 *
 */
@SuppressWarnings("serial")
public class DonationRequests extends HttpServlet {
	
	private static final Logger log = Logger.getLogger(DonationRequests.class.getSimpleName());

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		
		// check if the user specified the requests-by-beneficiary
		String beneficiaryId = req.getParameter("beneficiary");
        log.info("doGet(...) Beneficiary-ID = "+beneficiaryId);
		// we need to process the get request here...
		List<Entity> requests = getDonationOffers(beneficiaryId);
		if(requests != null){
			List<DonationRequest> donationRequests = convertFromEntities(requests);
			String jsonDonationRequests = asJsonDocument(donationRequests);
			//return a JSON containing an array of donation-requests
			writeOutput(resp,jsonDonationRequests);
		}
		else{
			writeOutput(resp,DonateMyStuffUtils.asServerResponse(DonateMyStuffConstants.DONATION_REQUEST_FAILURE, "No Donation Requests"));
		}
		
	}

	/**
	 * This is a helper method for retrieving list of Donation Requests from the
	 * DataStore - if beneficiaryId is not null, then only donation requests made by the specified beneficiary are retrieved, otherwise
	 * all requests are retrieved.
	 */
	private List<Entity> getDonationOffers(String beneficiaryId) {
		List<Entity> offers = null;

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		Query query = new Query("DonationRequest");
				
		if (beneficiaryId != null && !beneficiaryId.trim().isEmpty()) {			
			//filter is same as the WHERE CLAUSE
			Filter donationRequestFilter = new Query.FilterPredicate("beneficiary", FilterOperator.EQUAL, beneficiaryId);
			query.setFilter(donationRequestFilter);			
		   }
		
		query.addSort("date", Query.SortDirection.DESCENDING);
		offers = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(20));		
        
		return offers;
	}
	/**
	 * this method converts the Entities from Datastore into DinationOffer objects
	 * @param offersEntities
	 * @return
	 */
	private List<DonationRequest> convertFromEntities(List<Entity> offersEntities){
				
		List<DonationRequest> donationRequests = new ArrayList<DonationRequest>();
		for(Entity anOffer: offersEntities){
        	Map<String, Object> donationRequestProperties = anOffer.getProperties();
        	String beneficiaryId = (String)donationRequestProperties.get("beneficiary");
        	String id = (String)donationRequestProperties.get("id");
        	Date date = (Date)donationRequestProperties.get("date");
        	        	
        	DonationRequest donationRequest = new DonationRequest();
        	donationRequest.setId(id);
        	donationRequest.setBeneficriaryId(beneficiaryId);
        	donationRequest.setRequestDate(date);
        	
        	String offerId = (donationRequestProperties.get("donation_offer_id") != null? donationRequestProperties.get("donation_offer_id").toString():"0"); 
        	donationRequest.setDonationOfferId(offerId);
        	        	
        	//donated/offered item
        	DonatedItem item = new DonatedItem();
        	item.setId((String)donationRequestProperties.get("item_id"));
        	
        	String age = (donationRequestProperties.get("item_age") != null? donationRequestProperties.get("item_age").toString():"0"); 
        	item.setAge(Integer.parseInt(age));
        	
        	String ageRestriction = (donationRequestProperties.get("item_age_restriction") != null? donationRequestProperties.get("item_age_restriction").toString():"0");
        	item.setAgeRestriction(Integer.parseInt(ageRestriction));
        	
        	String gender = (donationRequestProperties.get("item_gender") != null? donationRequestProperties.get("item_gender").toString():"0");
        	item.setGenderCode(Integer.parseInt(gender));
        	
        	String itemName = (donationRequestProperties.get("item_name") != null? donationRequestProperties.get("item_name").toString():"no-name"); 
        	item.setName(itemName);
        	
        	String size = (donationRequestProperties.get("item_size") != null? donationRequestProperties.get("item_size").toString():"0");        	
        	item.setSize(Integer.parseInt(size));
        	
        	donationRequest.setRequestedDonationItem(item);
        	
        	//add to list
        	donationRequests.add(donationRequest);
        	
        }
		return donationRequests;
	}
	
	/**
	 * 
	 * @param offers
	 * @return
	 */
	private String asJsonDocument(List<DonationRequest> donationRequests){
		String doc = "";
		
		Gson gson =  new Gson();
		JsonObject requestsJsonObject = new JsonObject();
		JsonArray jaOffers = new JsonArray();
		for(DonationRequest anOffer: donationRequests){
			jaOffers.add(gson.toJsonTree(anOffer, DonationRequest.class));		
		}
		
		requestsJsonObject.add("requests", jaOffers);
		doc = requestsJsonObject.toString();
		
		return doc;
	}
	
	/**
	 * This method is used to write the output (JSON)
	 * @param response - response object of the incoming HTTP request
	 * @param output - message to be out-put
	 */
	private void writeOutput(HttpServletResponse response,String output){
		
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try{
        	Writer outputWriter = response.getWriter();
        	outputWriter.write(output);
        }
        catch(IOException ioe){
        	
        }
	}
}
