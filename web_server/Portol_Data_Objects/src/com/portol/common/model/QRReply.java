package com.portol.common.model;

import java.io.Serializable;
import java.util.UUID;

public class QRReply implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1621415731902441961L;

	public final String id = UUID.randomUUID().toString();
	
	private String qrURL;

	public String getQrURL() {
		return qrURL;
	}

	public void setQrURL(String qrURL) {
		this.qrURL = qrURL;
	}

	public QRReply(String qrURL) {
		super();
		this.qrURL = qrURL;
	}
	
	public QRReply() {
		super();
	}
	
}
