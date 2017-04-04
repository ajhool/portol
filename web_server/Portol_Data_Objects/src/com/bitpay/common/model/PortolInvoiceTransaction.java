package com.bitpay.common.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PortolInvoiceTransaction implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4156413916881938331L;
	
	private String _txid;
	private String _type;
	private double _amount;
	
    public PortolInvoiceTransaction() {}
    
    public PortolInvoiceTransaction(InvoiceTransaction orig) {
		this._amount = orig.getAmount();
		this._txid = orig.getTxid();
		this._type = orig.getType();
	}

	//@JsonIgnore
	public String getTxid() {
		return _txid;
	}
    
    @JsonProperty("txid")
	public void setTxid(String _txid) {
		this._txid = _txid;
	}

    //@JsonIgnore
	public String getType() {
		return _type;
	}
    
    @JsonProperty("type")
	public void setType(String _type) {
		this._type = _type;
	}

   // @JsonIgnore
	public double getAmount() {
		return _amount;
	}
    
    @JsonProperty("amount")
	public void setAmount(double _amount) {
		this._amount = _amount;
	}	
}
