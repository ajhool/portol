package com.portol.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.UUID;

import org.mongojack.Id;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PortolPlatform implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8271368781302939617L;
	
	@Id
	private String platformId;
	
	private String platformName = "unspecified"; 
	private String platformType = "unspecified"; 
	private String platformColor; 
	private boolean paired;
	private long lastUsed;
	private int activePlayers = 0; 

	public PortolPlatform(String platformName, String platformType, String color) {
		
		
		this(platformName, platformType, UUID.randomUUID().toString(), color);
		
		
		
	}
	
	public PortolPlatform(String platformName, String platformType, String platformId, String color) {
		super();
		this.platformColor = color;
		this.platformId = platformId;
		this.platformName = platformName;
		this.platformType = platformType;
		
	}
	
	public PortolPlatform(){
		super();
	}

	public boolean isPaired() {
		return paired;
	}

	public void setPaired(boolean paired) {
		this.paired = paired;
	}

	public String getPlatformId() {
		return platformId;
	}

	public void setPlatformId(String platformId) {
		this.platformId = platformId;
	}

	public String getPlatformType() {
		return platformType;
	}

	public void setPlatformType(String platformType) {
		this.platformType = platformType;
	}

	public String getPlatformColor() {
		return platformColor;
	}

	public void setPlatformColor(String platformColor) {
		this.platformColor = platformColor;
	}

	public String getPlatformName() {
		return platformName;
	}

	public void setPlatformName(String platformName) {
		this.platformName = platformName;
	}

	public int getActivePlayers() {
		return activePlayers;
	}

	public void setActivePlayers(int activePlayers) {
		this.activePlayers = activePlayers;
	}
	
	public int incrementActive(){
		return this.activePlayers++;
	}
	public int decrementActive(){
		this.activePlayers = Math.max(0, this.activePlayers - 1);
		return this.activePlayers;
	}

	public long getLastUsed() {
		return lastUsed;
	}

	public void setLastUsed(long lastUsed) {
		this.lastUsed = lastUsed;
	}


	
}
