package com.portol.common.model.app;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.portol.common.model.PortolToken;

import java.io.Serializable;
import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class XmitPortolToken implements Serializable{

	/**
	 *
	 */
	private static final long serialVersionUID = -481812811933799957L;

	private String value;

	private Date expiration;

	public XmitPortolToken(){
		super();
	}

	public XmitPortolToken(PortolToken existing){
		super();
		this.value = existing.getValue();
		this.expiration = existing.getExpiration();
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
