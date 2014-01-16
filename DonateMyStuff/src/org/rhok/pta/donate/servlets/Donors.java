package org.rhok.pta.donate.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.rhok.pta.donate.models.RegistrationRequest;
import org.rhok.pta.donate.models.ResidentialAddress;
import org.rhok.pta.donate.utils.DonateMyStuffConstants;
import org.rhok.pta.donate.utils.DonateMyStuffUtils;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
/**
 * A Servlet for retrieving a list of registered donors
 * 
 * @author Ishmael Makitla
 *
 */
public class Donors extends HttpServlet{
	private static final Logger log = Logger.getLogger(Donors.class.getSimpleName()); 
	
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)throws IOException {
		resp.setContentType("application/json");		
		
        String	name = req.getParameter("donorid"); 
        //TODO: a manager can only get a list of his donors/users - this value cannot be null
        String	manager = req.getParameter("managerid");  
        
        if(manager == null || !DonateMyStuffUtils.isManager(manager)){
        	DonateMyStuffUtils.writeOutput(resp,DonateMyStuffUtils.asServerResponse(DonateMyStuffConstants.ERROR, "Cannot Access Donors Data: Access Denied"));
        	return;
        }
        
        log.info("doGet (...) : name = "+name);
        
        //we need to process the get request here...
        List<Entity> donors = getDonors(name);
        
        if(donors != null){
        	List<RegistrationRequest> donorsObjects = convertFromEntities(donors);    
            String jsonOutput = asJsonDocument(donorsObjects);
            
            DonateMyStuffUtils.writeOutput(resp,jsonOutput);
        }
        else{
        	DonateMyStuffUtils.writeOutput(resp,DonateMyStuffUtils.asServerResponse(DonateMyStuffConstants.ERROR, "No Donors Offers"));
		}
        
	}
	
	/**
	 * Method for retrieving list of donors
	 * @param userid - if specified, retrieve only the details for this donor
	 * @return
	 */
	private List<Entity> getDonors(String donorId){
						
		 log.info("getDonors (Donor-ID = "+donorId+")");
		 
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();     
		Query query = new Query("RegistrationRequest");
		
		Filter donorIdFilter = null;
		
		if(donorId != null && !donorId.trim().isEmpty()){
			donorIdFilter = new Query.FilterPredicate("id", FilterOperator.EQUAL, donorId);	
		}
		 if(donorIdFilter != null){
			query.setFilter(donorIdFilter);			
		}
		         
		List<Entity> offers = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(20));
		
		return offers;
	}
	
	/**
	 * this method converts the Entities from Datastore into RegistrationObjects objects - however in this case, usernames and password are left NULL
	 * @param offersEntities
	 * @return
	 */
	private List<RegistrationRequest> convertFromEntities(List<Entity> donorsEntities){
		
		List<RegistrationRequest> donors = new ArrayList<RegistrationRequest>();
		for(Entity aDonor: donorsEntities){
        	Map<String, Object> donorProperties = aDonor.getProperties();
        	String donorId = (String)donorProperties.get("id");
        	String surname = (String)donorProperties.get("surname");
        	String name    = (String)donorProperties.get("name");
        	String mobile  = (String)donorProperties.get("mobile");
        	String telephone = (String)donorProperties.get("telephone");
        	String email     = (String)donorProperties.get("email");
        	String addressJSON =  (String)donorProperties.get("address");  
        	
        	RegistrationRequest donor = new RegistrationRequest();
        	donor.setRegistrationID(donorId);
        	donor.setEmail(email);
        	donor.setMobile(mobile);
        	donor.setName(name);
        	donor.setSurname(surname);
        	donor.setTelephone(telephone);        	
        	//address details
        	ResidentialAddress address = (new Gson()).fromJson(addressJSON, ResidentialAddress.class);        	
        	donor.setAddress(address);
        	         	
        	//add to list
        	donors.add(donor);        	
        	log.info("convertFromEntities (...) Offers = \n "+Arrays.asList(donors).toString());        	
        }
		
		return donors;
	}
	
	/**
	 * Method for JSONifying list of donors
	 * @param donors
	 * @return
	 */
	private String asJsonDocument(List<RegistrationRequest> donors){
		String doc = "";
		
		Gson gson =  new Gson();
		JsonObject offersJsonObject = new JsonObject();
		JsonArray jaDonors = new JsonArray();
				
		for(RegistrationRequest aDonor: donors){	
			jaDonors.add(gson.toJsonTree(aDonor, RegistrationRequest.class));		
		}
		
		offersJsonObject.add("donors", jaDonors);
		
		doc = offersJsonObject.toString();
		
		return doc;
	}
 
}
