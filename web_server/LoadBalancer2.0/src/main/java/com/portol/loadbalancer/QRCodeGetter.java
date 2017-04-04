package com.portol.loadbalancer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.portol.loadbalancer.service.QRMakerClient;

public class QRCodeGetter implements Runnable{

	private static final Logger logger = LoggerFactory.getLogger(QRCodeGetter.class);
	private final String contents;
	
	private String urlOfQR;

	private QRMakerClient qrSvc; 
	
	public QRCodeGetter(String contentsToQRify, QRMakerClient toUse){
		super();
		this.contents = contentsToQRify;
		this.qrSvc = toUse; 
		
	}
	public String getURLOfQR(){
		return urlOfQR;
	}
	
	@Override
	public void run() {
		try {
			urlOfQR = qrSvc.createQR(contents);
		} catch (Exception e) {
			logger.error("QR code creation failure", e);
		}
		
	}

}
