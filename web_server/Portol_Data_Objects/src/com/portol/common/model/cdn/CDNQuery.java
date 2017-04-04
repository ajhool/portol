package com.portol.common.model.cdn;

public class CDNQuery {

	private String apiKey;
	
	public enum QueryType{
		FAST, LOCALS, ALL
	}
	
	private QueryType type;

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public QueryType getType() {
		return type;
	}

	public void setType(QueryType type) {
		this.type = type;
	}

	public CDNQuery(String apiKey, QueryType type) {
		super();
		this.apiKey = apiKey;
		this.type = type;
	}

	public CDNQuery() {
		super();
	}
	
}
