package com.portol.common.model.app;

import java.io.Serializable;

public class AppConnectionRequest implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 9101169697762960231L;

	private String apiKey;
	private String QRContents;

	
	public AppConnectionRequest(){
		super();
	}


	public String getApiKey() {
		return apiKey;
	}


	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}


	public String getQRContents() {
		return QRContents;
	}


	public void setQRContents(String qRContents) {
		QRContents = qRContents;
	}
	
	
}
