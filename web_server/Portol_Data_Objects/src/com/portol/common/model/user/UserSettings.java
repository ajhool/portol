package com.portol.common.model.user;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserSettings implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 700748486954918789L;
	
	public enum Currency{
		CENTS, CREDITS, BTC
	}
	
	private Currency preferred = Currency.CREDITS;

	public Currency getPreferred() {
		return preferred;
	}

	public void setPreferred(Currency preferred) {
		this.preferred = preferred;
	}

}
