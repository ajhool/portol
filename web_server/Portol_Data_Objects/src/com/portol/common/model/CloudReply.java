package com.portol.common.model;

import java.io.Serializable;
import java.util.Date;

import com.portol.common.model.instance.BackendInstance;
import com.portol.common.model.instance.EdgeInstance;


public class CloudReply implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 349488831863223151L;

	public enum Value{
		BOOT_ACK, KEEPALIVE_ACK, EVENT_ACK, ERR, RESEND, PRIORITY_ACK, PLAYER_ACK
	}
	
	private Value val;

	private Date sent; 
	
	//contains the ID of the cloud player that wanted this request (for error checking purposes)
	private String cloudPlayerId;
	
	private String adminKey; 
	
	private BackendInstance newBackendState;
	private EdgeInstance newEdgeInstance;
	
	
	public CloudReply(){
		
	}
	
	public Value getVal() {
		return val;
	}

	public void setVal(Value val) {
		this.val = val;
	}

	public Date getSent() {
		return sent;
	}

	public void setSent(Date sent) {
		this.sent = sent;
	}

	public String getCloudPlayerId() {
		return cloudPlayerId;
	}

	public void setCloudPlayerId(String cloudPlayerId) {
		this.cloudPlayerId = cloudPlayerId;
	}

	public String getAdminKey() {
		return adminKey;
	}

	public void setAdminKey(String adminKey) {
		this.adminKey = adminKey;
	}

	public BackendInstance getNewBackendState() {
		return newBackendState;
	}

	public void setNewBackendState(BackendInstance newBackendState) {
		this.newBackendState = newBackendState;
	}

	public EdgeInstance getNewEdgeInstance() {
		return newEdgeInstance;
	}

	public void setNewEdgeInstance(EdgeInstance newEdgeInstance) {
		this.newEdgeInstance = newEdgeInstance;
	}




}
