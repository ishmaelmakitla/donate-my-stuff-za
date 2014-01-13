package org.rhok.pta.donate.models;

import java.io.Serializable;
/**
 * Class representing the registration request for a Donation-Manager.
 * These Managers are a different class of users and have more access to donation data than the mobile (public) users.
 * 
 * @author Ishmael Makitla
 *
 */
public class ManagerRegistrationRequest implements Serializable{

	private static final long serialVersionUID = -7045733516763441341L;
	
	private String name;
	private String surname;
	private String email;
	private String mobile;
	
	private String agencyname;
	private String telephone;
	private ResidentialAddress address;
	
	private String password;
	
	private String id;
	
	public ManagerRegistrationRequest(){
		//
	}

	public ManagerRegistrationRequest(String name, String surname,
			String email, String mobile, String agency_name, String telephone,
			ResidentialAddress address, String password) {

		this.name = name;
		this.surname = surname;
		this.email = email;
		this.mobile = mobile;
		this.agencyname = agency_name;
		this.telephone = telephone;
		this.address = address;
		this.password = password;
	}
	
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getAgencyName() {
		return agencyname;
	}

	public void setAgencyName(String agency_name) {
		this.agencyname = agency_name;
	}

	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	public ResidentialAddress getAddress() {
		return address;
	}

	public void setAddress(ResidentialAddress address) {
		this.address = address;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	

}
