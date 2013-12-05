package org.rhok.pta.donate.models;

import java.util.UUID;

import org.rhok.pta.donate.utils.DonateMyStuffUtils.RoleType;

/**
 * This is a Registration Request and it maps the JSON document the user will be submitting for registration
 * The class include a utility method for creating its JSON format.
 * @author IMakitla
 *
 */
public class RegistrationRequest {
	
	private String registrationID;
	private String name;
	private String surname;
	private String username;
	private String password;
	private String mobile;
	private String telephone;
	private String email;
	private ResidentialAddress address;
	//Donor = 0, Beneficiary = 1
	private int role = 1;
	String type; 
	
	
	public RegistrationRequest(){
		//
	}


	public RegistrationRequest(String name, String surname, String username,
			String password, String mobile, String telephone, String email,
			ResidentialAddress address) {
	
		this.name = name;
		this.surname = surname;
		this.username = username;
		this.password = password;
		this.mobile = mobile;
		this.telephone = telephone;
		this.email = email;
		this.address = address;
		
		this.registrationID = UUID.randomUUID().toString();
	}

	
	public String getType() {
		return type;
	}

	public void setType(String _type) {
		this.type = _type;
	}
	
	public int getRole() {
		return role;
	}


	public void setRole(int role) {
		this.role = role;
	}


	public String getRegistrationID() {
		return registrationID;
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


	public String getUsername() {
		return username;
	}


	public void setUsername(String username) {
		this.username = username;
	}


	public String getPassword() {
		return password;
	}


	public void setPassword(String password) {
		this.password = password;
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


	public ResidentialAddress getAddress() {
		return address;
	}


	public void setAddress(ResidentialAddress address) {
		this.address = address;
	}	

}
