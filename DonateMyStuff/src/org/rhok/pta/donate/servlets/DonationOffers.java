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
        log.info("doGet (...) : name = "+name);
        
        //we need to process the get request here...
        List<Entity> offers = getDonationOffers(name);
        List<DonationOffer> donationOffers = convertFromEntities(offers);    
        String jsonOutput = asJsonDocument(donationOffers);
        
        writeOutput(resp,jsonOutput);
	}
		
	/**
	 * Method for retrieving list of offers (made by the current donor, hence donorId) - null means retrieve all offers
	 * @param userid
	 * @return
	 */
	private List<Entity> getDonationOffers(String donorId){
		 log.info("getDonationOffers (Donor-ID = "+donorId+")");
		 
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();     
		Query query = new Query("DonationOffer").addSort("date", Query.SortDirection.DESCENDING);
		
		if(donorId != null && !donorId.trim().isEmpty()){
			Filter donorIdFilter = new Query.FilterPredicate("donor_id", FilterOperator.EQUAL, donorId);
			query.setFilter(donorIdFilter);
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
        	
        	DonationOffer donationOffer = new DonationOffer();
        	donationOffer.setId(id);
        	donationOffer.setDonorId(donorId);
        	donationOffer.setOfferDate(date);
        	donationOffer.setQuantity(quantity);
        	
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
	 * This method is used to write the output (JSON)
	 * @param response - response object of the incoming HTTP request
	 * @param output - message to be out-put
	 */
	private void writeOutput(HttpServletResponse response,String output){
		//send back JSON response
		 log.info("writeOutput()...returning : "+output);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try{
        	Writer outputWriter = response.getWriter();
        	outputWriter.write(output);
        }
        catch(IOException ioe){
        	log.severe(ioe.getLocalizedMessage());
        }
	}
}
