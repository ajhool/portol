package com.portol.common.model.instance;

import java.util.Date;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.portol.common.model.content.Content;

public abstract class Instance {

	@JsonProperty("_id")
	protected String id = UUID.randomUUID().toString();

	// maybe we can use this to creatively decide who to kill when reducing
	// ready queue size
	protected Date bootTime;

	// assume that the player is running (i.e., we are paying for it) unless it
	// is DEACTIVATED or SHUTDOWN
	public enum Status {
		NEW, SHUTDOWN, RUNNING, QUEUED, ERROR, DEACTIVATED, BOOTING, IDLE, LIVE_STREAM, PREVIEW //used to specify backend cloud status

	}

	// e.g. bad pay if someone double spent
	// exception if the server failed for some reason
	public enum EventCode {
		BAD_PAY, EXCEPTION, PLAYER_DISCONNECT, SOURCE_FAILURE, NO_CLIENTS, CONTENT_SWITCH, RECONNECT
	}

	protected String host_dns; 

	protected EventCode event;

	// api key for this cloud instance
	// will change on boot of each cloud instance
	protected String adminKey;
	private Content serving;

	public synchronized Content getServing() {
		return serving;
	}



	public synchronized void setServing(Content serving) {
		this.serving = serving;
	}

	//URL contents
	protected String location;

	protected String protocol = "http://";

	protected int port = 8901;

	protected String apiId;

	protected Date lastReport;

	protected Status status;

	public synchronized Date getBootTime() {
		return bootTime;
	}

	public synchronized void setBootTime(Date bootTime) {
		this.bootTime = bootTime;
	}

	public synchronized String getLocation() {
		return location;
	}

	public synchronized  void setLocation(String location) {
		this.location = location;
	}

	public synchronized String getAdminKey() {
		return adminKey;
	}

	public synchronized void setAdminKey(String adminKey) {
		this.adminKey = adminKey;
	}

	public synchronized Status getStatus() {
		return status;
	}

	public synchronized  void setStatus(Status status) {
		this.status = status;
	}

	public synchronized String getId() {
		return id;
	}

	public synchronized void setId(String id) {
		this.id = id;
	}

	public synchronized EventCode getEvent() {
		return event;
	}

	public synchronized void setEvent(EventCode event) {
		this.event = event;
	}

	public synchronized String getHost_dns() {
		return host_dns;
	}

	public synchronized void setHost_dns(String host_dns) {
		this.host_dns = host_dns;
	}

	public synchronized String getProtocol() {
		return protocol;
	}

	public synchronized void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public synchronized String getApiId() {
		return apiId;
	}

	public synchronized void setApiId(String apiId) {
		this.apiId = apiId;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public Date getLastReport() {
		return lastReport;
	}

	public void setLastReport(Date lastReport) {
		this.lastReport = lastReport;
	}


}
