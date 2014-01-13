package org.rhok.pta.donate.models;

import java.io.Serializable;
/**
 * The model representing the contact details of a user (donor or beneficiary)
 * This information is used to liaise with the person following either a donation offer or a donation request
 *  
 * @author Ishmael Makitla
 * 			
 *
 */
public class ContactInfo implements Serializable{

	private static final long serialVersionUID = -5197886196531693358L;
	
	private ResidentialAddress residentialaddress;
	
	private String mobile;
	
	private String telephone;
	
	private String email;
	
	private String twittername;
	
	private String facebookname;
	
	private String googleplusname;
	
	public ContactInfo(){
		//
	}

	public ContactInfo(ResidentialAddress residentialaddress, String mobile,
			String telephone, String email, String twittername,
			String facebookname, String googleplusname) {

		this.residentialaddress = residentialaddress;
		this.mobile = mobile;
		this.telephone = telephone;
		this.email = email;
		this.twittername = twittername;
		this.facebookname = facebookname;
		this.googleplusname = googleplusname;
	}

	public ResidentialAddress getResidentialaddress() {
		return residentialaddress;
	}

	public void setResidentialaddress(ResidentialAddress residentialaddress) {
		this.residentialaddress = residentialaddress;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getTwittername() {
		return twittername;
	}

	public void setTwittername(String twittername) {
		this.twittername = twittername;
	}

	public String getFacebookname() {
		return facebookname;
	}

	public void setFacebookname(String facebookname) {
		this.facebookname = facebookname;
	}

	public String getGoogleplusname() {
		return googleplusname;
	}

	public void setGoogleplusname(String googleplusname) {
		this.googleplusname = googleplusname;
	}
	
	
}
