package org.rhok.pta.donate;
/**
 * Model for Login Request. It bears the user name and password which are used to authenticate authorize the user
 * 
 * @author Ishmael Makitla
 *
 */
public class LoginRequest {
	
	private String username;
	private String password;
	
	public LoginRequest(){}

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

}
