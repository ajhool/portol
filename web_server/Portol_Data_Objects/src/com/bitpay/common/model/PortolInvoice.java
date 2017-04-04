package com.bitpay.common.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;






import com.bitpay.common.controller.BitPayException;


public class PortolInvoice implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -906091658528439947L;
	
	public static final String STATUS_NEW = "new";
	public static final String STATUS_PAID = "paid";
	public static final String STATUS_CONFIRMED = "confirmed";
	public static final String STATUS_COMPLETE = "complete";
	public static final String STATUS_INVALID = "invalid";
	public static final String EXSTATUS_FALSE = "false";
	public static final String EXSTATUS_PAID_OVER = "paidOver";
	public static final String EXSTATUS_PAID_PARTIAL = "paidPartial";

	private Long _nonce = 0L;
	private String _guid = "";
	private String _token = "";

	private Double _price;
	private String _currency;
	private String _posData = "";
	private String _notificationURL = "";
	private String _transactionSpeed = "";
	private boolean _fullNotifications = false;
	private String _notificationEmail = "";
	private String _redirectURL = "";
	private String _orderId = "";
	private String _itemDesc = "";
	private String _itemCode = "";
	private boolean _physical = false;
	private PortolInvoiceBuyer _buyer;

	private String _id;
	private String _url;
	private String _status;
	private String _btcPrice;
	private String _invoiceTime;
	private long _expirationTime;
	private long _currentTime;
	private String _btcPaid;
	private String _btcDue;
	private List<PortolInvoiceTransaction> _transactions;
	private String _rate;
	private Hashtable<String, String> _exRates;
	private String _exceptionStatus;
	private PortolInvoicePaymentUrls _paymentUrls;

	public PortolInvoice() {}

	//copy constructor from bitpay invoce without all the shitty jackson serialization BS
	public PortolInvoice (Invoice original){
		this._nonce = original.getNonce();
		this._guid = original.getGuid(); 
		this._token = original.getToken();
		this._price = original.getPrice();
		this._currency = original.getCurrency();
		this._posData = original.getPosData();
		this._notificationURL = original.getNotificationURL();
		this._transactionSpeed = original.getTransactionSpeed();
		this._fullNotifications = original.getFullNotifications();
		this._notificationEmail = original.getNotificationEmail();
		this._redirectURL = original.getRedirectURL();
		this._orderId = original.getId();
		this._itemDesc = original.getItemDesc();
		this._itemCode = original.getItemCode();
		this._physical = original.getPhysical();
		this._buyer = new PortolInvoiceBuyer(original.getBuyer());
		this._id = original.getId();
		this._url = original.getUrl();
		this._status = original.getStatus();
		this._btcPrice = original.getBtcPrice();
		this._invoiceTime = original.getInvoiceTime();
		this._expirationTime = original.getExpirationTime();
		this._currentTime = original.getCurrentTime();
		this._btcPaid = original.getBtcPaid();
		this._btcDue = original.getBtcDue();

		if(original.getTransactions() != null) {
			ArrayList<PortolInvoiceTransaction> trans = new ArrayList<PortolInvoiceTransaction>();
			for(InvoiceTransaction orig : original.getTransactions()){
				trans.add(new PortolInvoiceTransaction(orig));
			}
			this._transactions = trans;
		}
		
		this._rate = original.getRate();
		this._exRates = original.getExRates();
		this._exceptionStatus = original.getExceptionStatus();
		this._paymentUrls = new PortolInvoicePaymentUrls(original.getPaymentUrls());
	}

	public PortolInvoice(Double _price, String _currency)
	{
		this._price = _price;
		this._currency = _currency;
	}

	// API fields
	//

	public String getGuid() {
		return _guid;
	}

	public void setGuid(String _guid) {
		this._guid = _guid;
	}

	public Long getNonce() {
		return _nonce;
	}

	public void setNonce(Long _nonce) {
		this._nonce = _nonce;
	}

	public String getToken() {
		return _token;
	}

	public void setToken(String _token) {
		this._token = _token;
	}

	// Required fields
	//

	public Double getPrice() {
		return _price;
	}

	public void setPrice(Double _price) {
		this._price = _price;
	}

	public String getCurrency() {
		return _currency;
	}

	public void setCurrency(String _currency) throws BitPayException {
		if (_currency.length() != 3)
		{
			throw new BitPayException("Error: currency code must be exactly three characters");
		}
		this._currency = _currency;
	}

	// Optional fields
	//

	public String getOrderId() {
		return _orderId;
	}

	public void setOrderId(String _orderId) {
		this._orderId = _orderId;
	}

	public String getItemDesc() {
		return _itemDesc;
	}

	public void setItemDesc(String _itemDesc) {
		this._itemDesc = _itemDesc;
	}

	public String getItemCode() {
		return _itemCode;
	}

	public void setItemCode(String _itemCode) {
		this._itemCode = _itemCode;
	}

	public String getPosData() {
		return _posData;
	}

	public void setPosData(String _posData) {
		this._posData = _posData;
	}

	public String getNotificationURL() {
		return _notificationURL;
	}

	public void setNotificationURL(String _notificationURL) {
		this._notificationURL = _notificationURL;
	}

	public String getTransactionSpeed() {
		return _transactionSpeed;
	}

	public void setTransactionSpeed(String _transactionSpeed) {
		this._transactionSpeed = _transactionSpeed;
	}

	public boolean getFullNotifications() {
		return _fullNotifications;
	}

	public void setFullNotifications(boolean _fullNotifications) {
		this._fullNotifications = _fullNotifications;
	}

	public String getNotificationEmail() {
		return _notificationEmail;
	}

	public void setNotificationEmail(String _notificationEmail) {
		this._notificationEmail = _notificationEmail;
	}

	public String getRedirectURL() {
		return _redirectURL;
	}

	public void setRedirectURL(String _redirectURL) {
		this._redirectURL = _redirectURL;
	}

	public boolean getPhysical() {
		return _physical;
	}

	public void setPhysical(boolean _physical) {
		this._physical = _physical;
	}

	public PortolInvoiceBuyer getBuyer() {
		return _buyer;
	}

	public void setBuyer(PortolInvoiceBuyer _buyer) {
		this._buyer = _buyer;
	}


	// Response fields
	//

	public String getId() {
		return _id;
	}

	public void setId(String _id) {
		this._id = _id;
	}

	public String getUrl() {
		return _url;
	}

	public void setUrl(String _url) {
		this._url = _url;
	}

	public String getStatus() {
		return _status;
	}

	public void setStatus(String _status) {
		this._status = _status;
	}

	public String getBtcPrice() {
		return _btcPrice;
	}

	public void setBtcPrice(String _btcPrice) {
		this._btcPrice = _btcPrice;
	}

	public String getInvoiceTime() {
		return _invoiceTime;
	}

	public void setInvoiceTime(String _invoiceTime) {
		this._invoiceTime = _invoiceTime;
	}

	public long getExpirationTime() {
		return _expirationTime;
	}

	public void setExpirationTime(long _expirationTime) {
		this._expirationTime = _expirationTime;
	}

	public long getCurrentTime() {
		return _currentTime;
	}

	public void setCurrentTime(long _currentTime) {
		this._currentTime = _currentTime;
	}

	public String getBtcPaid() {
		return _btcPaid;
	}

	public void setBtcPaid(String _btcPaid) {
		this._btcPaid = _btcPaid;
	}

	public String getBtcDue() {
		return _btcDue;
	}

	public void setBtcDue(String _btcDue) {
		this._btcDue = _btcDue;
	}

	public List<PortolInvoiceTransaction> getTransactions() {
		return _transactions;
	}

	public void setTransactions(List<PortolInvoiceTransaction> _transactions) {
		this._transactions = _transactions;
	}

	public String getRate() {
		return _rate;
	}

	public void setRate(String _rate) {
		this._rate = _rate;
	}

	public Hashtable<String, String> getExRates() {
		return _exRates;
	}

	public void setExRates(Hashtable<String, String> _exRates) {
		this._exRates = _exRates;
	}


	public String getExceptionStatus() {
		return _exceptionStatus;
	}

	public void setExceptionStatus(String _exceptionStatus) {
		this._exceptionStatus = _exceptionStatus;
	}

	public PortolInvoicePaymentUrls getPaymentUrls() {
		return _paymentUrls;
	}

	public void setPaymentUrls(PortolInvoicePaymentUrls _paymentUrls) {
		this._paymentUrls = _paymentUrls;
	}
}
