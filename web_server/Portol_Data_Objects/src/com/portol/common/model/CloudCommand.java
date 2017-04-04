package com.portol.common.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.portol.common.model.content.Content;
import com.portol.common.model.instance.BackendInstance;
import com.portol.common.model.player.Player;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CloudCommand implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5118893093378305863L;

	private String apiToken; 
	
	public enum Type {
		SET_CONTENT_ALL, SET_CONTENT_PREVIEW, CHANGE_CONTENT, STATUS_UPDATE, ADD_PLAYER, DELETE_PLAYER , START_LIVE_BACKEND, KILL, ALERT_LOGIN
	}
	
	private CloudCommand.Type type; 
	
	private Player player;
	
	private Content content;
	
	private BackendInstance source;

	public String getApiToken() {
		return apiToken;
	}

	public void setApiToken(String apiToken) {
		this.apiToken = apiToken;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public Content getContent() {
		return content;
	}

	public void setContent(Content content) {
		this.content = content;
	}

	public CloudCommand(String apiToken, Type type) {
		super();
		this.apiToken = apiToken;
		this.type = type;
	}	
	
	public CloudCommand(String apiToken, Type type, Content content, Player player) {
		super();
		this.apiToken = apiToken;
		this.type = type;
		this.content = content;
		this.player = player;
	}
	
	public CloudCommand(){
		super();
	}

	public BackendInstance getSource() {
		return source;
	}

	public void setSource(BackendInstance source) {
		this.source = source;
	}
	
	
	
}
