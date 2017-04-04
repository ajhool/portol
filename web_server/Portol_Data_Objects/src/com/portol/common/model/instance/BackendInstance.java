package com.portol.common.model.instance;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

public class BackendInstance extends Instance implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4237485234749878979L;
	
	private String accessToken;
	private String contentParent = "/streams";
	ConcurrentHashMap<String, EdgeInstance> clientEdges;
	
	public ConcurrentHashMap<String, EdgeInstance> getClientEdges() {
		return clientEdges;
	}

	public void setClientEdges(ConcurrentHashMap<String, EdgeInstance> clientEdges) {
		this.clientEdges = clientEdges;
	}

	public BackendInstance(){
		super();
		setStatus(Status.NEW);
	}

	public BackendInstance(String accessToken,
			String protocol, int port) {
		super();
		
		this.accessToken = accessToken;
		this.protocol = protocol;
		this.port = port;
		if(clientEdges == null){
			clientEdges = new ConcurrentHashMap<String, EdgeInstance>();
		}
	
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getContentParent() {
		return contentParent;
	}

	public void setContentParent(String contentParent) {
		this.contentParent = contentParent;
	}

	@Override
	public String toString(){
		StringBuffer buf = new StringBuffer();
		
		buf.append("ID: " + id);
		buf.append("location: " + location);
		return buf.toString();
		
	}
	
}
