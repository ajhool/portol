package com.portol.common.model.content;

import java.io.Serializable;

public class ContentSetRequest implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1608843098358412934L;
	
	private String apiKey;
	
	public ContentSetRequest(String apiKey, ContentSource requested) {
		super();
		this.apiKey = apiKey;
		this.requested = requested;
	}
	
	public ContentSetRequest(){
		super();
	}

	private ContentSource requested;

	public ContentSource getRequested() {
		return requested;
	}

	public void setRequested(ContentSource requested) {
		this.requested = requested;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
	
	
}
