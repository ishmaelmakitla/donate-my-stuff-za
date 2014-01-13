package org.rhok.pta.donate.models;

import java.io.Serializable;

/**
 * Request sent by the Donation Management User to change the flag of an Offer/Request.
 * 
 * @author Ishmael Makitla
 *			GDG/RHoK Pretoria
 *			South Africa
 *			2014
 */
public class DonationFlagChangeRequest implements Serializable{
	
	private static final long serialVersionUID = 6995139284654182310L;
	
	//id of the offer in question
		private String id;
		//id of the user requesting the status change
		private String userid;
		//the actual status to change to
		private int flag;
		
		public DonationFlagChangeRequest(String id, String userid, int _flag) {	
			this.id = id;
			this.userid = userid;
			this.flag = _flag;
		}
		
		public DonationFlagChangeRequest(){
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

		public int getFlag() {
			return flag;
		}

		public void setFlag(int flag) {
			this.flag = flag;
		}

		
}
