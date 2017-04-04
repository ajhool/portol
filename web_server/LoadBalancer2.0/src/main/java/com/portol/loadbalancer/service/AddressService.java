package com.portol.loadbalancer.service;

import io.dropwizard.lifecycle.Managed;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;

import org.glassfish.jersey.server.Uri;
import org.mongojack.DBCursor;
import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;
import org.mongojack.DBQuery.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AbstractScheduledService;
import com.portol.common.model.content.Content;
import com.portol.common.model.content.ContentMetadata;
import com.portol.common.model.payment.Payment;
import com.portol.common.model.payment.PaymentInfoRequest;
import com.portol.common.model.payment.Payment.Status;
import com.portol.common.model.payment.Payment.Type;
import com.portol.loadbalancer.config.PaymentGetterConfig;
import com.portol.loadbalancer.manager.MongoManaged;
import com.portol.loadbalancer.repo.PlatformRepository;


public class AddressService extends AbstractScheduledService implements Managed{
	
	//provides methods for both updating addresses in the db async'ly AND for getting an addresse's status 
	static final Logger logger = LoggerFactory.getLogger(AddressService.class);

	final Client addrClient;

	public static final int READY_ADDRESS_POOL_SIDE = 25;
	public static final int NEW_ADDRESSES_PER_REQ = 20;
	private static final int QUERY_SIZE = 10;

	public int port;

	public String url;

	public String path;

	public String apiKey;

	public String password;
	
	public String user;
	
	private BalanceCheckerClient balCheck;

	public AddressService(Client jClient, PaymentGetterConfig addrGetter,
		 BalanceCheckerClient balCheck) {
		
		addrClient = jClient;
		
		this.port = addrGetter.port;

		this.url = addrGetter.host;

		this.path = addrGetter.path;

		this.apiKey = addrGetter.apiKey;

		this.password = addrGetter.password;

		this.user = addrGetter.user;


		
		
		this.balCheck = balCheck;
		
	}

	public Payment getNewAddressFor(ContentMetadata target) throws MalformedURLException, IllegalArgumentException, UriBuilderException, URISyntaxException {
		
		return getMorePlayerAddrs(1, new int[]{(int)target.getPrices().getPriceInBits()}).get(0);
	}
	

	public List<Payment> getMorePlayerAddrs(int numToGet, int[] requestVals) throws MalformedURLException, IllegalArgumentException, UriBuilderException, URISyntaxException{

		URI uri = new URI(url + ":" + port + path.toString());
		logger.info("Querying payment server for next master @ " + uri.toString());

		//assign a mster for this address
		PaymentInfoRequest req = new PaymentInfoRequest();
		req.setNumAddrRequested(numToGet);
		req.setValuesRequested(requestVals);

		//these will be checked server side for compliance, so set them right
		req.setApiKey(apiKey);

		List<Payment> resp = addrClient.target(uri).request().post(Entity.json(req), new GenericType<List<Payment>>(){});

		logger.info("received reply from paymentserver:\n"+resp.toString());

		return resp;
	}

	@Override
	protected void runOneIteration() throws Exception {
		logger.info("Address service background task executing");

	}

	@Override
	protected Scheduler scheduler() {
		return AbstractScheduledService.Scheduler.newFixedRateSchedule(0, 30, TimeUnit.SECONDS);
	}

	@Override
	public void start() throws Exception {
		this.startAsync().awaitRunning();

	}

	@Override
	public void stop() throws Exception {
		this.stopAsync().awaitTerminated();

	}

	public Payment checkAndUpdatePayment(
			Payment playerPayment) {
		
		if(playerPayment.getType() == Type.SHARDS){
			//then we can shortcut the balance checking process
			
			//double check that the payment is complete
			if(playerPayment.getStatus() == Status.COMPLETE){
				return playerPayment;
			}
			
		}
		
		
		balCheck.addressBalanceUpdater(playerPayment);
		
		// based on balance, update state
		
		//this is the range we accept in bits
		int epsilon = 1;
		if(playerPayment.getTotReceived() >= playerPayment.getTotRequested() - epsilon){
			//then we have a successful payment that went through
			//update status
			playerPayment.setStatus(Payment.Status.COMPLETE);
		}
		return playerPayment;
	}
	




}
