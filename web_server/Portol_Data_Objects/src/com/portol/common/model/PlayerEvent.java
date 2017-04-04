package com.portol.common.model;

import java.io.Serializable;

public class PlayerEvent implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1202977369470091215L;

	public enum Event{
		SEEK_UPDATE;
	}
	
	private Event event; 
	
	private String playerId; 
	
	private SeekStatus sStatus;

	public PlayerEvent(String eventId, SeekStatus sStatus) {
		super();
		this.setPlayerId(eventId);
		this.sStatus = sStatus;
	}

	public PlayerEvent() {
		super();
		// TODO Auto-generated constructor stub
	}

	public SeekStatus getsStatus() {
		return sStatus;
	}

	public void setsStatus(SeekStatus sStatus) {
		this.sStatus = sStatus;
	}

	public Event getEvent() {
		return event;
	}

	public void setEvent(Event event) {
		this.event = event;
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	} 
	
	
}
