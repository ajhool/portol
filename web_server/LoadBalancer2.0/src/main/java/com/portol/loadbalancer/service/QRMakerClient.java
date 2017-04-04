package com.portol.loadbalancer.service;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import com.portol.common.model.QRReply;
import com.portol.common.model.QRRequest;
import com.portol.loadbalancer.config.QRMakerClientConfig;

public class QRMakerClient {

	private Client jClient;
	private QRMakerClientConfig thisConfig;
	
	public QRMakerClient(QRMakerClientConfig thisConfig,
			Client client) {
		this.jClient = client;
		this.thisConfig = thisConfig;
	}

	public String createQR(String btcPaymentAddr, int totRequested) throws URISyntaxException {

		//create QRRequest and fire if off

		QRRequest req = new QRRequest();

		req.setAddress(btcPaymentAddr);
		req.setAmount("" + totRequested);
		req.setApiKey(thisConfig.apiKey);
		req.setPaymentReqServer(thisConfig.bipPaymentServerHost);
		req.setProtocol(thisConfig.bipProtocol + ":");
		req.setComplete(false);

		URI uri = new URI(thisConfig.QRmakerProtocol + thisConfig.QRmakerURL + ":" + thisConfig.QRmakerPort + thisConfig.path);
		String targetURL = uri.toString();
		QRReply resp = jClient.target(uri).request().post(Entity.json(req), QRReply.class);

		return resp.getQrURL();
	}

	public String createQR(String completeURL) throws URISyntaxException{
		QRRequest req = new QRRequest(completeURL);


		req.setComplete(true);
		req.setApiKey(thisConfig.apiKey);
		URI uri = new URI(thisConfig.QRmakerProtocol + thisConfig.QRmakerURL + ":" + thisConfig.QRmakerPort + thisConfig.path);
		String targetURL = uri.toString();
		QRReply resp = jClient.target(uri).request().post(Entity.json(req), QRReply.class);

		return resp.getQrURL();
	}
	
	
	
	
	
	
	
}
