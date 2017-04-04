package com.portol.common.model.content;

import java.io.Serializable;

public class ContentSource implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4234910509861328148L;
	
	//CONTENT SOURCE
	private String host;
	private String protocol;
	private int port;
	private String path;
	private SourceType srcTyp; 
	private String contentId;
	//contains url of original content to serve, as well as DB information
	private String dbName;
	private String collName;
	private String dbuserName;
	private String dbPassword;
	
	private String apiKey;

	public enum SourceType {
		HLS, PORTOL, UDP, RTMP, RTSP, VOD_MONGO
	}

	public ContentSource() {
		super();
	}


	public ContentSource(String host, String protocol, int port, String path,
			String apiKey, SourceType srcTyp) {
		super();
		this.host = host;
		this.protocol = protocol;
		this.port = port;
		this.path = path;
		this.apiKey = apiKey;
		this.srcTyp = srcTyp;
	}


	public String getHost() {
		return host;
	}


	public void setHost(String host) {
		this.host = host;
	}


	public String getProtocol() {
		return protocol;
	}


	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}


	public int getPort() {
		return port;
	}


	public void setPort(int port) {
		this.port = port;
	}


	public String getPath() {
		return path;
	}


	public void setPath(String path) {
		this.path = path;
	}


	public String getApiKey() {
		return apiKey;
	}


	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}


	public SourceType getSrcTyp() {
		return srcTyp;
	}


	public void setSrcTyp(SourceType srcTyp) {
		this.srcTyp = srcTyp;
	}


	public String getContentId() {
		return contentId;
	}


	public void setContentId(String contentId) {
		this.contentId = contentId;
	}


	public String getDbName() {
		return dbName;
	}


	public void setDbName(String dbName) {
		this.dbName = dbName;
	}


	public String getCollName() {
		return collName;
	}


	public void setCollName(String collName) {
		this.collName = collName;
	}


	public String getDbuserName() {
		return dbuserName;
	}


	public void setDbuserName(String dbuserName) {
		this.dbuserName = dbuserName;
	}


	public String getDbPassword() {
		return dbPassword;
	}


	public void setDbPassword(String dbPassword) {
		this.dbPassword = dbPassword;
	}
	
	
	
}
