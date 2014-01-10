package org.rhok.pta.donate.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
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
import com.google.gson.JsonObject;

/**
 * This is the servlet used to look-up the user-id of a user given the username and password.
 * If a match is found, the user-id is returned, otherwise error message is returned. Both these responses are in JSON format.
 * 
 * In later versions, this servlet will use secure communication and will support encryption.
 * 
 * @author Ishmael Makitla
 *         GDG Pretoria, RHoK Pretoria
 *         2013
 *         South Africa
 *
 */
@SuppressWarnings("serial")
public class Login extends HttpServlet{
	
	private static final Logger log = Logger.getLogger(Login.class.getSimpleName());
	
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)throws IOException {
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
			ioe.printStackTrace();
			log.severe("doGet:: Error Processing Login Request: "+ioe.getLocalizedMessage());
			DonateMyStuffUtils.writeOutput(resp,DonateMyStuffUtils.asServerResponse(DonateMyStuffConstants.LOGIN_FAILED," Error: There were issues processing your donation request ::"+ioe.getLocalizedMessage()));
		}
	}
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
			Entity knownUser = getUser(loginRequest.getUsername(), loginRequest.getPassword());
			
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
	private Entity getUser(String username, String password){
		
		log.info("getUser ("+username+", <password>");
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		Query query = new Query("RegistrationRequest");
		
		//filter is same as the WHERE CLAUSE
		Filter userNameFilter = new Query.FilterPredicate("username", FilterOperator.EQUAL, username);
		Filter passwordNameFilter = new Query.FilterPredicate("password", FilterOperator.EQUAL, password);
		
		CompositeFilter userpasswordCombinationFilter = new CompositeFilter(CompositeFilterOperator.AND, Arrays.asList(userNameFilter, passwordNameFilter));
		
		query.setFilter(userpasswordCombinationFilter);
		
		PreparedQuery pq = datastore.prepare(query);
		Entity user = pq.asSingleEntity();
		
		return user;
	}

}
