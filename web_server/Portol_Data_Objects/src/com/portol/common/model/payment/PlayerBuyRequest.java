package com.portol.common.model.payment;

import java.io.Serializable;
import java.util.UUID;

import org.mongojack.Id;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PlayerBuyRequest implements Serializable{


	@Id
	private String id = UUID.randomUUID().toString();
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8574115836638604186L;


	private String playerId;
	
	private String btcAddress;

	
	public PlayerBuyRequest(){
		super();
	}
	


	public String getId() {
		return id;
	}



	public void setId(String id) {
		this.id = id;
	}



	public String getBtcAddress() {
		return btcAddress;
	}

	public void setBtcAddress(String btcAddress) {
		this.btcAddress = btcAddress;
	}



	public String getPlayerId() {
		return playerId;
	}



	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}
}
