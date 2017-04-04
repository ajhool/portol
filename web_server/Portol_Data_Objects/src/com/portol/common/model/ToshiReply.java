package com.portol.common.model;

import java.io.Serializable;

public class ToshiReply implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7962538311178395356L;
	
	private String hash;
	private int balance;
	private int received;
	private int sent;
	private int unconfirmed_received;
	private int unconfirmed_sent;
	private int unconfirmed_balance;
	
	private String error = "No error";
	
	public ToshiReply(String hash, int balance, int received, int sent,
			int unconfirmed_received, int unconfirmed_sent, int unconfirmed_balance){
		super();
		this.setHash(hash);
		this.balance = balance;
		this.setReceived(received);
		this.setSent(sent);
		this.setUnconfirmed_balance(unconfirmed_balance);
		this.setUnconfirmed_received(unconfirmed_received);
		this.setUnconfirmed_sent(unconfirmed_sent);
	}
	
	public ToshiReply(){
		super();
	}
	
	public int getBalance(){
		return this.balance;
	}
	
	public String getError(){
		return this.error;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public int getReceived() {
		return received;
	}

	public void setReceived(int received) {
		this.received = received;
	}

	public int getSent() {
		return sent;
	}

	public void setSent(int sent) {
		this.sent = sent;
	}

	public int getUnconfirmed_received() {
		return unconfirmed_received;
	}

	public void setUnconfirmed_received(int unconfirmed_received) {
		this.unconfirmed_received = unconfirmed_received;
	}

	public int getUnconfirmed_balance() {
		return unconfirmed_balance;
	}

	public void setUnconfirmed_balance(int unconfirmed_balance) {
		this.unconfirmed_balance = unconfirmed_balance;
	}

	public int getUnconfirmed_sent() {
		return unconfirmed_sent;
	}

	public void setUnconfirmed_sent(int unconfirmed_sent) {
		this.unconfirmed_sent = unconfirmed_sent;
	}
}
