package com.portol.common.model;

import java.io.Serializable;
import java.util.UUID;

import org.mongojack.Id;

public class CookieHolder implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8795079585300679603L;

	@Id
	private String id = UUID.randomUUID().toString();

	private String[] matchingPlayerIds;

	private Cookie cookie;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String[] getMatchingPlayerIds() {
		return matchingPlayerIds;
	}

	public void setMatchingPlayerIds(String[] matchingPlayerId) {
		this.matchingPlayerIds = matchingPlayerId;
	}

	public Cookie getCookie() {
		return cookie;
	}

	public void setCookie(Cookie cookie) {
		this.cookie = cookie;
	}

	public CookieHolder(){
		super();
		if(this.matchingPlayerIds == null){
			this.matchingPlayerIds = new String[1];
		}
	}

	public CookieHolder(String matchingPlayerId, Cookie cookie) {
		super();
		if(this.matchingPlayerIds == null){
			this.matchingPlayerIds = new String[1];
		}
		matchingPlayerIds[0] = matchingPlayerId;
		this.cookie = cookie;
	}


}
