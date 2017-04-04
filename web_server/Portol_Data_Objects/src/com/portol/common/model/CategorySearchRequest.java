package com.portol.common.model;

public class CategorySearchRequest {

	
	public enum RequestType {
		MUSIC, VIDEO, TEXT
	}
	
	private String apiKey;

	public RequestType getType() {
		return type;
	}

	public void setType(RequestType type) {
		this.type = type;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public CategorySearchRequest(String apiKey, RequestType type) {
		this.apiKey = apiKey;
		this.type = type;
	}

	public CategorySearchRequest() {
	}

	private RequestType type;

}
