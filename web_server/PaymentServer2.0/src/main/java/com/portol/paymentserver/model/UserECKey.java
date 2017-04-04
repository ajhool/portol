package com.portol.paymentserver.model;


import java.util.UUID;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.mongojack.Id;


public class UserECKey extends ECKeyModel{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2091497059260622275L;
	@Id
	private String id = UUID.randomUUID().toString();
	
	
	public enum Status {
		SENT, UNUSED, CLEANED
	}
	
	private Status status;

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public UserECKey() {
	}
	
	public UserECKey(ECKey toUse, NetworkParameters params, UserECKey.Status status) {
		super(toUse, params);
		this.status = status;
		
	}
	
	
}
