package org.rhok.pta.donate.servlets;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.rhok.pta.donate.models.DonationOffer;
import org.rhok.pta.donate.utils.DonateMyStuffConstants;
import org.rhok.pta.donate.utils.DonateMyStuffUtils;
import org.rhok.pta.donate.utils.DonateMyStuffUtils.DonatedItemType;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
/**
 * This is the servlet used to retrieve list of donation offers
 * @author Ishmael Makitla
 *
 */
@SuppressWarnings("serial")
public class DonationOffers extends HttpServlet{
	
	private static final Logger log = Logger.getLogger(DonationOffers.class.getSimpleName()); 
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)throws IOException {
		resp.setContentType("application/json");		
		
		//here we use the UserService to authenticate the user (not sure how this will work with Android clients?)
		
        String	name = req.getParameter("donorid");  
        String itemType = req.getParameter("type");
        
        log.info("doGet (...) : name = "+name);
        
        //we need to process the get request here...
        List<Entity> offers = getDonationOffers(name, itemType);
        
        //reduce to only VALID and OPEN offers - unless the user is the manager -then return all
        List<Entity> validOffers = returnValidDonationOffers(offers, name);
        if(validOffers != null){
        	List<DonationOffer> donationOffers = convertFromEntities(validOffers);    
            String jsonOutput = asJsonDocument(donationOffers);
            
            DonateMyStuffUtils.writeOutput(resp,jsonOutput);
        }
        else{
        	DonateMyStuffUtils.writeOutput(resp,DonateMyStuffUtils.asServerResponse(DonateMyStuffConstants.DONATION_REQUEST_FAILURE, "No Donation Offers"));
		}
        
	}
	
	/**
	 * Method for retrieving ONLY Valid Offers (Status = OPEN and Flag = VALID)
	 * @param allOffers
	 * @return
	 */
	private List<Entity> returnValidDonationOffers(List<Entity> allOffers, String userId){
		
		if(isManager(userId)){ return allOffers; }
		
		List<Entity> validOffers = new ArrayList<Entity>();
		for(Entity anOffer: allOffers){
        	Map<String, Object> offerProperties = anOffer.getProperties();
        	if(offerProperties.get("status") != null && offerProperties.get("flag") != null){
        	   int status = (int)offerProperties.get("status");
        	   int flag = (int)offerProperties.get("flag");
        	   if(status == DonateMyStuffConstants.STATUS_OPEN && flag == DonateMyStuffConstants.FLAG_VALID){
        		 validOffers.add(anOffer);
        	   }
        	}
		}
		
		return validOffers;
	}
	/**
	 * Method for retrieving list of offers (made by the current donor, hence donorId) - null means retrieve all offers
	 * @param userid
	 * @return
	 */
	private List<Entity> getDonationOffers(String donorId, String type){
						
		 log.info("getDonationOffers (Donor-ID = "+donorId+")");
		 
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();     
		Query query = new Query("DonationOffer").addSort("date", Query.SortDirection.DESCENDING);
		
		
		Filter itemTypeFilter = null;
		Filter donorIdFilter = null;
		
		if(donorId != null && !donorId.trim().isEmpty()){
			donorIdFilter = new Query.FilterPredicate("donor_id", FilterOperator.EQUAL, donorId);	
		}
		//make a filter for donation-types (item)
		if(type != null && !type.trim().isEmpty()){
			itemTypeFilter = new Query.FilterPredicate("item_type", FilterOperator.EQUAL, type);
		}
		
		if(donorIdFilter != null && itemTypeFilter !=null){
			CompositeFilter userItemTypeCombinationFilter = new CompositeFilter(CompositeFilterOperator.AND, Arrays.asList(itemTypeFilter, donorIdFilter));
			query.setFilter(userItemTypeCombinationFilter);
		}
		else if(donorIdFilter == null && itemTypeFilter !=null){
			query.setFilter(itemTypeFilter);
		}
		           
		List<Entity> offers = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(20));
		
		return offers;
	}
	
	/**
	 * this method converts the Entities from Datastore into DinationOffer objects
	 * @param offersEntities
	 * @return
	 */
	private List<DonationOffer> convertFromEntities(List<Entity> offersEntities){
		
		List<DonationOffer> offers = new ArrayList<DonationOffer>();
		for(Entity anOffer: offersEntities){
        	Map<String, Object> offerProperties = anOffer.getProperties();
        	String donorId = (String)offerProperties.get("donor_id");
        	String id = (String)offerProperties.get("id");
        	Date date = (Date)offerProperties.get("date");
        	String numOfferedItems = (offerProperties.get("item_count") != null? offerProperties.get("item_count").toString():"0");
        	int quantity = Integer.parseInt(numOfferedItems);
        	boolean willDeliver = (Boolean)offerProperties.get("deliver");
        	
        	DonationOffer donationOffer = new DonationOffer();
        	donationOffer.setId(id);
        	donationOffer.setDonorId(donorId);
        	donationOffer.setOfferDate(date);
        	donationOffer.setQuantity(quantity);
        	donationOffer.setDeliver(willDeliver);
        	
        	//donated/offered item
        	DonatedItem item = new DonatedItem();
        	
        	item.setId((String)offerProperties.get("item_id"));
        	String age = (offerProperties.get("item_age") != null? offerProperties.get("item_age").toString():"0");        	
        	item.setAge(Integer.parseInt(age));
        	
        	String ageRestriction = (offerProperties.get("item_age_restriction") != null? offerProperties.get("item_age_restriction").toString():"0");
        	item.setAgeRestriction(Integer.parseInt(ageRestriction));
        	
        	String gender = (offerProperties.get("item_gender") != null? offerProperties.get("item_gender").toString():"0");
        	item.setGenderCode(Integer.parseInt(gender));
        	
        	String name = (offerProperties.get("item_name") != null? offerProperties.get("item_name").toString():"no-name");
        	item.setName(name);
        	
        	String itemSize = (offerProperties.get("item_size") != null? offerProperties.get("item_size").toString():"0");
        	item.setSize(Integer.parseInt(itemSize));
        	
        	//set donated-item type
        	String type = (offerProperties.get("item_type") != null? offerProperties.get("item_type").toString():"unknown");
        	item.setType(type);
        	
        	donationOffer.setItem(item);
        	
        	//add to list
        	offers.add(donationOffer);        	
        	 log.info("convertFromEntities (...) Offers = \n "+Arrays.asList(offers).toString());
        	
        }
		return offers;
	}
	
	/**
	 * 
	 * @param offers
	 * @return
	 */
	private String asJsonDocument(List<DonationOffer> offers){
		String doc = "";
		
		Gson gson =  new Gson();
		JsonObject offersJsonObject = new JsonObject();
		JsonArray jaOffers = new JsonArray();
				
		for(DonationOffer anOffer: offers){	
			jaOffers.add(gson.toJsonTree(anOffer, DonationOffer.class));		
		}
		
		offersJsonObject.add("offers", jaOffers);
		
		doc = offersJsonObject.toString();
		
		return doc;
	}
	
	/**
	 * Check if the specified user is indeed a manager - TODO: simply call the static method directly...
	 * @param userId
	 * @return
	 */
	private boolean isManager(String userId){
		boolean _isManager = false;
		//check if there's a manager by this ID
		_isManager = DonateMyStuffUtils.isManager(userId);
		//TEMP FIX: Until the Portal does the login and registration...
		return true;
		//return _isManager;
	}
	
}
