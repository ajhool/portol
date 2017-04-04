package com.portol.common.model.cdn;

import java.io.Serializable;
import java.util.Date;

import java.util.UUID;

import org.mongojack.Id;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RegionalServer implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5302490562519301320L;

	@Id 
	private String id = UUID.randomUUID().toString();
	
	private String host;
	
	private Date expiration; 
	
	private String serverLocation;
	
	private String apiKey; 

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Date getExpiration() {
		return expiration;
	}

	public void setExpiration(Date expiration) {
		this.expiration = expiration;
	}

	public String getServerLocation() {
		return serverLocation;
	}

	public void setServerLocation(String serverLocation) {
		this.serverLocation = serverLocation;
	}

	public RegionalServer(String id, String host, Date expiration,
			String serverLocation) {
		super();
		this.id = id;
		this.host = host;
		this.expiration = expiration;
		this.serverLocation = serverLocation;
	} 
	
	
	public RegionalServer(){
		
	}
}
