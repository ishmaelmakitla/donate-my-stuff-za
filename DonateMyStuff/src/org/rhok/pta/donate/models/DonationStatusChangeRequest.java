package org.rhok.pta.donate.models;

import java.io.Serializable;

/**
 * Request sent by the Donation Management User to change the status of an Offer/Request.
 * 
 * @author Ishmael Makitla
 *			GDG/RHoK Pretoria
 *			South Africa
 *			2014
 */
public class DonationStatusChangeRequest implements Serializable{
	
	private static final long serialVersionUID = 1540316915393968356L;
	
	//id of the offer in question
		private String id;
		//id of the user requesting the status change
		private String userid;
		//the actual status to change to
		private int status;
		
		public DonationStatusChangeRequest(String id, String userid, int status) {	
			this.id = id;
			this.userid = userid;
			this.status = status;
		}
		
		public DonationStatusChangeRequest(){
			//
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getUserid() {
			return userid;
		}

		public void setUserid(String userid) {
			this.userid = userid;
		}

		public int getStatus() {
			return status;
		}

		public void setStatus(int status) {
			this.status = status;
		}		
}
