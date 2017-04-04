package com.portol.common.model.app;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppPaymentRequest implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 9201169697762960231L;

	private String userID;
	
	private XmitPortolToken validToken;
	
	private String purchasedContentID;
	
	private String playerIdentifier;

	
	public AppPaymentRequest(){
		super();
	}
	
	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public XmitPortolToken getValidToken() {
		return validToken;
	}

	public void setValidToken(XmitPortolToken validToken) {
		this.validToken = validToken;
	}

	public String getPlayerIdentifier() {
		return playerIdentifier;
	}

	public void setPlayerIdentifier(String playerIdentifier) {
		this.playerIdentifier = playerIdentifier;
	}

	public String getPurchasedContentID() {
		return purchasedContentID;
	}

	public void setPurchasedContentID(String purchasedContentID) {
		this.purchasedContentID = purchasedContentID;
	}
}
