package org.rhok.pta.donate.gcm;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * This is the servlet used to send notifications to running instances of the Donate-My-Stuff Android App.
 * This servlets uses the Google Cloud Messaging Platform to send out notifications.
 * The notifications are sent under the following conditions:
 * 1. When Status of Donation-Request is changed (beneficiary is notified)
 * 2. When a Donation-Offer is made that matches Donation-Request 
 *    and it is not in response to the same or any other Donation-Request (beneficiary is notified)
 * 3.When status of Donation-Offer changes (donor is notified)
 * 4.When a Donation-Request is made that matches the offer 
 *   and is not made in response to this Request nor any other request (donor is notified)   
 * 
 * @author Ishmael Makitla 
 *         GDG/RHoK Pretoria, South Africa
 *         2013
 *
 */
public class Notify extends HttpServlet{
	
	private static final Logger log = Logger.getLogger(Notify.class.getSimpleName());
 
	public void doPost(HttpServletRequest request, HttpServletResponse response)throws IOException {
		//TODO: code -
		
	}
}
