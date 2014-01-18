package org.rhok.pta.donate.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.gson.JsonObject;

/**
 * 
 * @author Ishmael Makitla
 *
 */
public class DonateMyStuffUtils {
	
	private static final Logger log = Logger.getLogger(DonateMyStuffUtils.class.getSimpleName());

	public enum DonateMyStuffType {OFFER, REQUEST;
	public static DonateMyStuffType toDonateMyStuffType(String type){
		return DonateMyStuffType.valueOf(type.toUpperCase());
	}
	};
	
	//TYPE OF REGISTRATION
	public enum RegistrationType {DONOR, BENEFICIARY;
	public static RegistrationType toRegistrationType(String type){
		return RegistrationType.valueOf(type.toUpperCase());
	}
	};
	
	//type of donor/beneficiary
	/**
	 * Individual donor -  a person who is donating in his personal capacity
	 * Organization donor - a charitable organization donating as a collective
	 * Individual beneficiary - a person who makes donation requests or receives donation in his personal capacity
	 * Beneficiary organization - these organizations include NGOs, SOS, and other relief organizations who seek out donation
	 *                            on behalf of the marginalized and needy.
	 * @author IMakitla
	 *
	 */
	
	public enum RoleType {INDIVIDUAL_DONOR,DONOR_ORGANIZATION, INDIVIDUAL_BENEFICIARY, BENEFICIARY_ORGANIZATION;
	public static RoleType toRoleType(String type){
		return RoleType.valueOf(type.toUpperCase());
	}
	};
	
	public enum DonatedItemType {BOOK,SHOES, CLOTHES, BLANKETS, SCHOOL_UNIFORM,TROUSER, DRESS,T_SHIRT,SHIRT,SHORTS,STATIONARY;
	public static DonatedItemType toDonatedItemType(String type){
		return DonatedItemType.valueOf(type.toUpperCase());
	   }
	};
	
	/**
	 * 
	 * @param status
	 * @param message
	 * @return
	 */
	public static String asServerResponse(int status, String message){
		String response = "";
		JsonObject responseJSON = new JsonObject();
		responseJSON.addProperty("status", status);
		responseJSON.addProperty("message", message);
		response = responseJSON.toString();
		return response;
	}
	
	 /**
	 * For content that was sent with a custom encoding. This method uses the Reader to read the raw content.
	 * @param request
	 * @param resp
	 */
	public static String getRawDataPayload(HttpServletRequest request, HttpServletResponse resp){
		
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
	 * This method is used to write the output (JSON)
	 * @param response - response object of the incoming HTTP request
	 * @param output - message to be out-put
	 */
	public static void writeOutput(HttpServletResponse response,String output){
		//send back JSON response       
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try{
        	 Writer outputWriter = response.getWriter();
        	 log.info("Returning :: "+output);
        	 outputWriter.write(output);
        }
        catch(IOException ioe){
        	
        }
	}
	
	/**
	 * 
	 * @param userId
	 * @return
	 */
	public static boolean isManager(String userId){
		boolean _isManager = false;
		//check if there's a manager by this ID
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();	
		
		//so here we are checking if the offer exists
		Query managerQuery   = new Query("DonationManager");
		Filter managerIdFilter = new Query.FilterPredicate("id", FilterOperator.EQUAL, userId);
		managerQuery.setFilter(managerIdFilter);
			
		try{
			Entity manager = datastore.prepare(managerQuery).asSingleEntity();
			if(manager !=null){
				//the record exists, what role has been assigned to this user?
				_isManager = true;
			}
		}
		catch(Exception e){
			//possible some datastore errors
		}
			
			
		return _isManager;
	}
	
	
	public static boolean userExists(String username, String email){
		boolean exists = true;
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		Query query = new Query("RegistrationRequest");
		
		//filter is same as the WHERE CLAUSE
		Filter emailFilter = new Query.FilterPredicate("email", FilterOperator.EQUAL, email);
		Filter userNameFilter = new Query.FilterPredicate("username", FilterOperator.EQUAL, username);
		
		CompositeFilter userCombinationFilter = new CompositeFilter(CompositeFilterOperator.AND, Arrays.asList(userNameFilter, emailFilter));
		
		query.setFilter(userCombinationFilter);
		
		PreparedQuery pq = datastore.prepare(query);
		Entity user = pq.asSingleEntity();
		exists = (user == null? false:true);
		return exists;
	}
	
	/**
	 * Helper method to look-up a manager to check if the record already exists
	 * 
	 * @param username
	 * @param email
	 * @return
	 */
	public static boolean managerExists(String username, String email){
		boolean exists = true;
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		Query query = new Query("DonationManager");
		
		//filter is same as the WHERE CLAUSE
		Filter emailFilter = new Query.FilterPredicate("email", FilterOperator.EQUAL, email);
		Filter userNameFilter = new Query.FilterPredicate("name", FilterOperator.EQUAL, username);
		
		CompositeFilter managerCombinationFilter = new CompositeFilter(CompositeFilterOperator.OR, Arrays.asList(userNameFilter, emailFilter));
		
		query.setFilter(managerCombinationFilter);
		
		PreparedQuery pq = datastore.prepare(query);
		Entity user = pq.asSingleEntity();
		exists = (user == null? false:true);
		return exists;
	}
}
