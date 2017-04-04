package com.portol.common.model;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.portol.common.model.user.Account;
import com.portol.common.model.user.User;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginRequest implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -421051001256623462L;
	
	
	
	private Account.AccountType type = Account.AccountType.VUESCAPE;
	private String oAuthToken;
	private User loggingIn;
	private PortolPlatform loginPlatform;
	
	public LoginRequest(){
		super();
	}
	
	public LoginRequest(User loggingIn, PortolPlatform loginPlatform) {
		super();
		this.loggingIn = loggingIn;
		this.loginPlatform = loginPlatform;
	}
	public User getLoggingIn() {
		return loggingIn;
	}
	public void setLoggingIn(User loggingIn) {
		this.loggingIn = loggingIn;
	}
	public PortolPlatform getLoginPlatform() {
		return loginPlatform;
	}
	public void setLoginPlatform(PortolPlatform loginPlatform) {
		this.loginPlatform = loginPlatform;
	}


	public String getoAuthToken() {
		return oAuthToken;
	}

	public void setoAuthToken(String oAuthToken) {
		this.oAuthToken = oAuthToken;
	}

	public Account.AccountType getType() {
		return type;
	}

	public void setType(Account.AccountType type) {
		this.type = type;
	} 

}
