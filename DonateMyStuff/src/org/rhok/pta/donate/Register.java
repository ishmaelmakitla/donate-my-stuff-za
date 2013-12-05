package org.rhok.pta.donate;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.gson.Gson;

/**
 * This is the Servlet that handles registrations. The registration is done by the public with either a donor role or
 * beneficiary role.
 * Donors are those who register with an intention to donate stuff. They will submit Donation-Offers
 * Beneficiaries are those registering so they can benefit from good-hearted donors - they will submit Donation-Requests
 * @author Ishmael Makitla
 *         2013
 *         RHoK Pretoria, Google Developer Group, Pretoria
 *         CSIR
 *
 */
public class Register extends HttpServlet{
	//this servlet allows registrations that post a JSON document or parameters
	public void doPost(HttpServletRequest request, HttpServletResponse response)throws IOException {
		String payload = request.getParameter("payload");
		
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)throws IOException {
		String payload = request.getParameter("payload");
		System.out.println("Payload = "+payload);
		if(payload !=null){
			//process as JSON document			
			RegistrationRequest registration = (new Gson()).fromJson(payload, RegistrationRequest.class);
			if(registration != null){
				doRegister(registration, response);				
			}
			else{
				System.out.println("Errors Deserializing the Registartion JSON");
				response.getWriter().write("{500}");
			}
		}
	}
	
	private void doRegister(RegistrationRequest registration, HttpServletResponse response) throws IOException{
      Key registrationRequestsKey = KeyFactory.createKey("RegistrationRequest", "Za.Donate.MyStuff");
        
        Date date = new Date();
        Entity registrationRequest = new Entity("RegistrationRequest", registrationRequestsKey);
        //set reg id
        String id = registration.getRegistrationID();
        if(id == null){ id = (new RegistrationRequest()).getRegistrationID(); }
        //set id
        registrationRequest.setProperty("id", id);
        //set type
        registrationRequest.setProperty("type", registration.getType());
        //set name
        registrationRequest.setProperty("name", registration.getName());
        //set surname
        registrationRequest.setProperty("surname", registration.getSurname());
        //set username
        registrationRequest.setProperty("username", registration.getUsername());
        //set password
        registrationRequest.setProperty("password", registration.getPassword());
        //set mobile phone
        registrationRequest.setProperty("mobile", registration.getMobile());
        //set telephone number
        registrationRequest.setProperty("telephone", registration.getTelephone());
        //set email
        registrationRequest.setProperty("email", registration.getEmail());
        //set address values
        ResidentialAddress address = registration.getAddress();
        if(address !=null){
           registrationRequest.setProperty("address", address.toString());
        }
        else{
        	System.out.println("WARNING - Address Could not be found...");
        }
        //set role (donor/beneficiary)
        registrationRequest.setProperty("role", registration.getRole());      
        
        //put into data store
        try{
        	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        	//save the registration in the db
            datastore.put(registrationRequest);
        	response.getWriter().write("{200}");
        }
        catch(Exception e){
        	e.printStackTrace();
        	response.getWriter().write("{500}");
        }
        
	}

}
