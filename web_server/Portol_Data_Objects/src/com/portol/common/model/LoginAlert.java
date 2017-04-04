package com.portol.common.model;

import java.io.Serializable;

 public class LoginAlert implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3490604985514743166L;
	
	public String playerId;
	public final String event = "LOGIN_ALERT";
	public String getPlayerId() {
		return playerId;
	}
	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}
	public LoginAlert() {
		super();
	}
}
