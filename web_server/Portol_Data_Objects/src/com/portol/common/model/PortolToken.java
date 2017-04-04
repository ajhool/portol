package com.portol.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.portol.common.model.app.XmitPortolToken;

import java.io.Serializable;
import java.util.Date;
@JsonIgnoreProperties(ignoreUnknown = true)
public class PortolToken  implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -481812811933799957L;

	private String value;
	
	private Date expiration;

	public PortolToken(XmitPortolToken xmitPortolToken){
		super();
		this.value = xmitPortolToken.getValue();
		this.expiration = xmitPortolToken.getExpiration();
		
	}
	
	public PortolToken() {
		// TODO Auto-generated constructor stub
	}

	public Date getExpiration() {
		return expiration;
	}

	public void setExpiration(Date expiration) {
		this.expiration = expiration;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	} 
}
