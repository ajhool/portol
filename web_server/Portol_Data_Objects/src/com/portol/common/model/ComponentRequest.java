package com.portol.common.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class ComponentRequest implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8516003635099882968L;
	
	private String apiKey;
	
	private String repId;

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getRepId() {
		return repId;
	}

	public void setRepId(String repId) {
		this.repId = repId;
	} 

}
