package com.portol.common.model.payment;

import java.io.Serializable;
import java.util.UUID;

import org.mongojack.Id;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.portol.common.model.content.ContentMetadata;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PlayerBuyResponse implements Serializable{


	@Id
	private String id = UUID.randomUUID().toString();
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8574115836638604186L;

	private String purchaseStatus;
	
	private boolean successful;
	
	private ContentMetadata purchased;
	
	public PlayerBuyResponse(){
		super();
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isSuccessful() {
		return successful;
	}

	public void setSuccessful(boolean successful) {
		this.successful = successful;
	}

	public String getPurchaseStatus() {
		return purchaseStatus;
	}

	public void setPurchaseStatus(String purchaseStatus) {
		this.purchaseStatus = purchaseStatus;
	}

	public void setPurchased(ContentMetadata usersContent) {
		this.purchased = usersContent;
	}
	
	public ContentMetadata getPurchased(){
		return this.purchased;
	}

}
