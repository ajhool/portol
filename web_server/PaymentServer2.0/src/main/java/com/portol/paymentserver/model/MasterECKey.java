package com.portol.paymentserver.model;

import java.util.UUID;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.mongojack.Id;



public class MasterECKey extends ECKeyModel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4085116230584682341L;

	@Id 
	private String id = UUID.randomUUID().toString();
	
	public enum Status {
		INUSE, UNUSED, ENDOFLIFE
	}
	
	//so  that we can sort them by preference
	private int rank;
	
	//number of times this has been used
	private int counter;  
	
	private Status status;

	public MasterECKey(ECKey toUse, NetworkParameters params, MasterECKey.Status status) {
		super(toUse, params);
		this.status = status;
		
	}
	
	public MasterECKey(){
	
	}
	
	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	
	
	public int getUsage() {
	return counter;
		
	}

	public void setUsage(int usage) {
	this.counter = usage;
	}


	
	
	
	
}
