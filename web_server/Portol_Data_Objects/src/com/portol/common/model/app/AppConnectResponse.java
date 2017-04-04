package com.portol.common.model.app;

import java.io.Serializable;

import com.portol.common.model.content.ContentMetadata;

public class AppConnectResponse implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 9201169697762970231L;

	
	private ContentMetadata purchasedContent;
	
	private String source;

	private String playerID; 

	
	public AppConnectResponse(){
		super();
	}


	public String getSource() {
		return source;
	}


	public void setSource(String source) {
		this.source = source;
	}


	public ContentMetadata getPurchasedContent() {
		return purchasedContent;
	}


	public void setPurchasedContent(ContentMetadata purchasedContent) {
		this.purchasedContent = purchasedContent;
	}


	public String getPlayerID() {
		return playerID;
	}


	public void setPlayerID(String playerID) {
		this.playerID = playerID;
	}
	
	
}
