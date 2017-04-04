package com.portol.common.model.user;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Account implements Serializable{

	public enum AccountType{
		GOOGLE, FACEBOOK, VUESCAPE
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4145358013750187066L;
	
	private AccountType type;
	private String userKey; 


	boolean isVerified; 

	public boolean isVerified() {
		return isVerified;
	}

	public void setVerified(boolean isVerified) {
		this.isVerified = isVerified;
	}
	
	public AccountType getType() {
		return type;
	}

	public Account() {
		super();
	}

	public void setType(AccountType type) {
		this.type = type;
	}

	public Account(AccountType type, String userKey) {
		super();
		this.type = type;
		this.userKey = userKey;
	}

	public String getUserKey() {
		return userKey;
	}

	public void setUserKey(String userKey) {
		this.userKey = userKey;
	} 

}
