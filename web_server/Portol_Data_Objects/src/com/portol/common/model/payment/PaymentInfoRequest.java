package com.portol.common.model.payment;

import java.io.Serializable;

public class PaymentInfoRequest implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -229295093085791219L;

	private String apiKey; 
	
	private int numAddrRequested;
	
	private int[] values;

	public int getNumAddrRequested() {
		return numAddrRequested;
	}

	public void setNumAddrRequested(int numAddrRequested) {
		this.numAddrRequested = numAddrRequested;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	} 
	
	public PaymentInfoRequest(){
		
	}
	
	@Override
	public String toString(){
		StringBuffer buf = new StringBuffer();
		buf.append("Printing info for PaymentInfoRequest object @ " + this.hashCode() +"\n");
		buf.append("apiKey: " + apiKey + "\n");
		buf.append("num of addrs requested: " + numAddrRequested + "\n");
		
		return buf.toString();
	}

	public void setValuesRequested(int[] requestVals) {
		this.values = requestVals;
		
	}
	
	public int[] getValuesRequested(){
		return this.values;
	}
	
}
