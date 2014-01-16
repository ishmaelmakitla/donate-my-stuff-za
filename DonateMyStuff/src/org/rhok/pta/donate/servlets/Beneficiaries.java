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
 * Servlet for retrieving list of registered beneficiaries
 * @author Ishmael Makitla
 *
 */
public class Beneficiaries extends HttpServlet{

	private static final long serialVersionUID = -5529069851007173437L;
	private static final Logger log = Logger.getLogger(Beneficiaries.class.getSimpleName()); 
	
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)throws IOException {
		resp.setContentType("application/json");		
		
		//here we use the UserService to authenticate the user (not sure how this will work with Android clients?)
		
        String	name = req.getParameter("beneficiary"); 
        //TODO: a manager can only get a list of his donors/users - this value cannot be null
        String	manager = req.getParameter("managerid");  
        
        if(manager == null || !DonateMyStuffUtils.isManager(manager)){
        	DonateMyStuffUtils.writeOutput(resp,DonateMyStuffUtils.asServerResponse(DonateMyStuffConstants.ERROR, "Cannot Access Beneficiary Data: Access Denied"));
        	return;
        }
        
        log.info("doGet (...) : name = "+name);
        
        //we need to process the get request here...
        List<Entity> beneficiaries = getBeneficiaries(name);
        
        if(beneficiaries != null){
        	List<RegistrationRequest> beneficiariesObjects = convertFromEntities(beneficiaries);    
            String jsonOutput = asJsonDocument(beneficiariesObjects);
            
            DonateMyStuffUtils.writeOutput(resp,jsonOutput);
        }
        else{
        	DonateMyStuffUtils.writeOutput(resp,DonateMyStuffUtils.asServerResponse(DonateMyStuffConstants.ERROR, "No Donors Offers"));
		}
        
	}
	
	/**
	 * Method for retrieving list of offers (made by the current donor, hence donorId) - null means retrieve all offers
	 * @param userid
	 * @return
	 */
	private List<Entity> getBeneficiaries(String beneficiaryId){
						
		 log.info("getDonors (Donor-ID = "+beneficiaryId+")");
		 
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();     
		Query query = new Query("RegistrationRequest");
		
		Filter beneficiaryIdFilter = null;
		
		if(beneficiaryId != null && !beneficiaryId.trim().isEmpty()){
			beneficiaryIdFilter = new Query.FilterPredicate("id", FilterOperator.EQUAL, beneficiaryId);	
		}
		 if(beneficiaryIdFilter != null){
			query.setFilter(beneficiaryIdFilter);			
		}
		         
		List<Entity> offers = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(20));
		
		return offers;
	}
	
	/**
	 * this method converts the Entities from Datastore into RegistrationObjects objects - however in this case, usernames and password are left NULL
	 * @param offersEntities
	 * @return
	 */
	private List<RegistrationRequest> convertFromEntities(List<Entity> beneficiariesEntities){
		
		List<RegistrationRequest> beneficiaries = new ArrayList<RegistrationRequest>();
		for(Entity aDonor: beneficiariesEntities){
        	Map<String, Object> donorProperties = aDonor.getProperties();
        	String id = (String)donorProperties.get("id");
        	String surname = (String)donorProperties.get("surname");
        	String name    = (String)donorProperties.get("name");
        	String mobile  = (String)donorProperties.get("mobile");
        	String telephone = (String)donorProperties.get("telephone");
        	String email     = (String)donorProperties.get("email");
        	String addressJSON =  (String)donorProperties.get("address");  
        	
        	RegistrationRequest beneficiary = new RegistrationRequest();
        	beneficiary.setRegistrationID(id);
        	beneficiary.setEmail(email);
        	beneficiary.setMobile(mobile);
        	beneficiary.setName(name);
        	beneficiary.setSurname(surname);
        	beneficiary.setTelephone(telephone);        	
        	//address details
        	ResidentialAddress address = (new Gson()).fromJson(addressJSON, ResidentialAddress.class);        	
        	beneficiary.setAddress(address);
        	         	
        	//add to list
        	beneficiaries.add(beneficiary);   
        }
		return beneficiaries;
	}
	
	/**
	 * Method for JSONifying list of donors
	 * @param donors
	 * @return
	 */
	private String asJsonDocument(List<RegistrationRequest> beneficiaries){
		String doc = "";
		
		Gson gson =  new Gson();
		JsonObject beneficiariesJsonObject = new JsonObject();
		JsonArray jaBeneficiaries = new JsonArray();
				
		for(RegistrationRequest aBeneficiary: beneficiaries){	
			jaBeneficiaries.add(gson.toJsonTree(aBeneficiary, RegistrationRequest.class));		
		}
		
		beneficiariesJsonObject.add("beneficiaries", jaBeneficiaries);
		
		doc = beneficiariesJsonObject.toString();
		
		return doc;
	}
}
