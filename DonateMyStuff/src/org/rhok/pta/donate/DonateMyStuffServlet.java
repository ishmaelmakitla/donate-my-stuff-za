package org.rhok.pta.donate;

import java.io.IOException;
import javax.servlet.http.*;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

/**
 * This is the main DonateMyStuff Servlet. It allows the clients to retrieve list of donation-requests (requests), donation-offers
 * as well as the list of donations;
 * 
 * @author Ishmael Makitla
 * RHoK 2013 30-Nov, 1-Dec
 * Lead GDG Pretoria
 *
 */
@SuppressWarnings("serial")
public class DonateMyStuffServlet extends HttpServlet {
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)throws IOException {
		resp.setContentType("text/plain");		
		String name = "<no-name>";
		//here we use the UserService to authenticate the user (not sure how this will work with Android clients?)
		UserService userService = UserServiceFactory.getUserService();
        User user = userService.getCurrentUser();
        if(user != null){
        	//this user has logged in, so display his name
        	name = user.getNickname();
        }
        else{
        	//perhaps suggest the login...
        	resp.sendRedirect(userService.createLoginURL(req.getRequestURI()));
        }
        //we need to process the get request here...
		resp.getWriter().println(name+" {you will be retrieving donations JSON}");
	}
	
	/**
	 * This is a helper method for retrieving list of Offers from the DataStore
	 */
	private void getDonationOffers(){
		
	}
	/**
	 * Method for retriving list of donation-requests
	 */
	private void getDonationRequests(){
		//
	}
}


