package org.rhok.pta.donate.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.rhok.pta.donate.models.LoginRequest;
import org.rhok.pta.donate.utils.DonateMyStuffConstants;
import org.rhok.pta.donate.utils.DonateMyStuffUtils;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.gson.Gson;

public class ManagerLogin extends HttpServlet{

	private static final long serialVersionUID = -5878261557185529786L;
	private static final Logger log = Logger.getLogger(ManagerLogin.class.getSimpleName());
	
	/**
	 * 
	 */
	@Override
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
				getRawPayload(req,resp);
			}
		}
		catch(IOException ioe){
			log.severe("doPost:: Error Processing Login Request: "+ioe.getLocalizedMessage());
			DonateMyStuffUtils.writeOutput(resp,DonateMyStuffUtils.asServerResponse(DonateMyStuffConstants.LOGIN_FAILED," Error: There were issues processing your donation request::"+ioe.getLocalizedMessage()));
		}
	}
	
	/**
	 * 
	 * @param request
	 * @param response
	 */
	private void getRawPayload(HttpServletRequest request, HttpServletResponse response){
						
		StringBuffer rawData = new StringBuffer();
		  String line = null;
		  try {
			  	BufferedReader reader = request.getReader();
			  	while ((line = reader.readLine()) != null){
			  		rawData.append(line);
			  	}
			  
			  	log.info("getRawPayload(...) DATA = \n"+rawData);
			  		
		  } catch (Exception e) { e.printStackTrace(); }

		  if(rawData.length()>0){
			  try {
				    String decodedPayload = URLDecoder.decode(rawData.toString(),"UTF-8");
				    doRequest(response,decodedPayload); 
			    } 
			  catch (UnsupportedEncodingException e) {
				  log.severe("Error Decoding Raw Data Stream: "+e.getLocalizedMessage());				
			    }			 
		  }
		  else{
			  log.severe("The data stream is empty - no data received");
		  }
	}
	/**
	 * 
	 * @param response
	 * @param data - JSON document used for authentication.
	 */
	private void doRequest( HttpServletResponse response, String data){
		LoginRequest loginRequest = (new Gson()).fromJson(data, LoginRequest.class);
		if(loginRequest != null){
			Entity knownUser = getManager(loginRequest.getUsername(), loginRequest.getPassword());
			
			if(knownUser == null){
				log.info("User NOT Found With ID:: "+loginRequest.getUsername());
				DonateMyStuffUtils.writeOutput(response, DonateMyStuffUtils.asServerResponse(DonateMyStuffConstants.LOGIN_FAILED, "Login Failed"));
				return;
			}
			
			Object uidProperty =  knownUser.getProperty("id");
			
			String userID = (uidProperty != null?  uidProperty.toString(): "");
			log.info("User Found With ID:: "+userID);
			
			if(!userID.trim().isEmpty()){
				DonateMyStuffUtils.writeOutput(response, DonateMyStuffUtils.asServerResponse(DonateMyStuffConstants.LOGIN_SUCCESSFULL, userID));
			}
			else{
				//user not found or login error
				DonateMyStuffUtils.writeOutput(response, DonateMyStuffUtils.asServerResponse(DonateMyStuffConstants.LOGIN_FAILED, userID));
			}			
		}
		else{
			DonateMyStuffUtils.writeOutput(response, DonateMyStuffUtils.asServerResponse(DonateMyStuffConstants.LOGIN_FAILED, "Login Failed"));
		}
	}
	
	/**
	 * This method queries the DataStore for an Entity that matches the combination of the username and password
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	private Entity getManager(String username, String password){
		
		log.info("getManager ("+username+", <password>");
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		Query query = new Query("DonationManager");
		
		//filter is same as the WHERE CLAUSE
		Filter emailFilter = new Query.FilterPredicate("email", FilterOperator.EQUAL, username);
		Filter passwordNameFilter = new Query.FilterPredicate("password", FilterOperator.EQUAL, password);
		
		CompositeFilter userpasswordCombinationFilter = new CompositeFilter(CompositeFilterOperator.AND, Arrays.asList(emailFilter, passwordNameFilter));
		
		query.setFilter(userpasswordCombinationFilter);
		
		PreparedQuery pq = datastore.prepare(query);
		Entity manager = null; 
		try{
			manager = pq.asSingleEntity();
		}catch(Exception e){ }
		
		return manager;
	}
}
