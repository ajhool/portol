package com.portol.common.model.user;


import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserFunds implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2371395757464454820L;
	
	private int userCredits;
	private int userCents;
	
	public int getUserCredits() {
		return userCredits;
	}
	
	public void setUserCredits(int userCredits) {
		this.userCredits = userCredits;
	}
	
	public int getUserCents() {
		return userCents;
	}
	
	public void setUserCents(int userCents) {
		this.userCents = userCents;
	}
}
