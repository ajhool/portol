package com.portol.paymentserver.service;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.portol.common.model.ToshiReply;
import com.portol.common.model.payment.Payment;


//used solely by address service to check balances quickly in a multithreaded environment
public class BalanceCheckerClient {

	private static final Logger logger = LoggerFactory.getLogger(BalanceCheckerClient.class);
	final Client cClient; 
	private int port;
	private String confBalCheckPath;
	private String unConfBalCheckPath;
	private String jsonPath;
	private String host;
	private String hash;
	private UriBuilder uribuild;

	public BalanceCheckerClient(Client jClient, String host,
			String unConfBalCheckPath, int port, String confBalCheckPath, String jsonPath) {

		this.cClient = jClient;
		this.port = port;
		this.confBalCheckPath = confBalCheckPath;
		this.unConfBalCheckPath = unConfBalCheckPath;
		this.jsonPath = jsonPath;
		this.host = host;

	}

	public int addressBalanceGetter(String addrToCheck){
		int ret = 0;

		this.uribuild = UriBuilder.fromUri(host+ ":" + port + jsonPath + addrToCheck);

		try {
			ToshiReply resp = this.cClient.target(uribuild).request().get(ToshiReply.class);

			if(!resp.getError().equals("Not Found")){
				ret = resp.getBalance();

			} else {
				ret = -1;
			}

		} catch (NotFoundException e){
			logger.info("caught address not found exception from toshi, assuming balance then is 0...");
			ret = 0;
		}

		return ret;
	}


	public int addressBalanceUpdater(Payment payment){
		int ret = 0;

		this.hash = payment.getBtcPaymentAddr();
		this.uribuild = UriBuilder.fromUri(host+ ":" + port + jsonPath + this.hash);
		try {
			ToshiReply resp = this.cClient.target(uribuild).request().get(ToshiReply.class);

			if(!resp.getError().equals("Not Found")){
				int balance = (resp.getUnconfirmed_balance() / 1000 + resp.getBalance() / 1000) - payment.getOldBalance();
				payment.setTotReceived(balance);
				ret = balance;
			} else {
				ret = -1;
			}
		} catch (NotFoundException e){
			logger.info("caught address not found exception from toshi, assuming balance then is 0...");
			ret = 0;
		}
		return ret;
	}
}
