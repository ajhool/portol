package com.bitpay.common.model;

import java.io.Serializable;

public class PortolInvoicePaymentUrls implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7003462713274772588L;
	
	private String _BIP21 = "";
	private String _BIP72 = "";
	private String _BIP72b = "";
	private String _BIP73 = "";
	
    public PortolInvoicePaymentUrls() {}

	public PortolInvoicePaymentUrls(InvoicePaymentUrls paymentUrls) {
		this._BIP21 = paymentUrls.getBIP21();
		this._BIP72 = paymentUrls.getBIP72();
		this._BIP72b = paymentUrls.getBIP72b();
		this._BIP73 = paymentUrls.getBIP73();
	}

	public String getBIP21() {
		return _BIP21;
	}
        
	public void setBIP21(String _BIP21) {
		this._BIP21 = _BIP21;
	}

	public String getBIP72() {
		return _BIP72;
	}
        
	public void setBIP72(String _BIP72) {
		this._BIP72 = _BIP72;
	}

	public String getBIP72b() {
		return _BIP72b;
	}
        
	public void setBIP72b(String _BIP72b) {
		this._BIP72b = _BIP72b;
	}

	public String getBIP73() {
		return _BIP73;
	}
        
	public void setBIP73(String _BIP73) {
		this._BIP73 = _BIP73;
	}
}
