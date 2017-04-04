package com.portol.common.model;

import java.io.Serializable;
import java.util.UUID;

public class QRRequest implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3788512393858749742L;

	public final String id = UUID.randomUUID().toString();
	
	private String address;  //mqereru783734.....
	private String protocol; //bitcoin:
	private String amount; //0.04
	private String paymentReqServer; //server address that handles API calls related to bip72
	private String serverParams; //parameters to indentify the request at server
	private String apiKey; 
	private String completeURL;
	private boolean hasComplete;
	
	
	public QRRequest(String address, String protocol, String amount,
			String paymentReqServer, String serverParams) {
		super();
		this.address = address;
		this.protocol = protocol;
		this.amount = amount;
		this.paymentReqServer = paymentReqServer;
		this.serverParams = serverParams;
		this.hasComplete = false;
	}
	
	public QRRequest(String completeURL){
		this.setCompleteURL(completeURL);
		this.hasComplete = true;
	}
	
	public QRRequest() {
		
	}
	
	public boolean isComplete(){
		return hasComplete;
		
	}
	
	public void setComplete(boolean complete){
		this.hasComplete = complete;
	}
	
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getPaymentReqServer() {
		return paymentReqServer;
	}
	public void setPaymentReqServer(String paymentReqServer) {
		this.paymentReqServer = paymentReqServer;
	}
	public String getServerParams() {
		return serverParams;
	}
	public void setServerParams(String serverParams) {
		this.serverParams = serverParams;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getCompleteURL() {
		return completeURL;
	}

	public void setCompleteURL(String completeURL) {
		this.completeURL = completeURL;
	}
	
	
	
	
}
