package com.portol.common.model.user;


import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserIcon implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1975973849109588927L;
	
	private String type;
	private String rawData;
	private String description;
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getRawData() {
		return rawData;
	}
	
	public void setRawData(String rawData) {
		this.rawData = rawData;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	 
}