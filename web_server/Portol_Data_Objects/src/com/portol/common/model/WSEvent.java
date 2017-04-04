package com.portol.common.model;

import java.io.Serializable;

public class WSEvent implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8291813202854828485L;

	public enum EventType{
		PING, PONG
	}
	
	public WSEvent(EventType vSnotify) {
		super();
		VSnotify = vSnotify;
	}

	public WSEvent() {
		super();
		
	}

	public EventType getVSnotify() {
		return VSnotify;
	}

	public void setVSnotify(EventType vSnotify) {
		VSnotify = vSnotify;
	}

	private EventType VSnotify; 
	
	
}
