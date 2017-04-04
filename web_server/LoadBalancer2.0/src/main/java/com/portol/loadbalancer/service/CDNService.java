package com.portol.loadbalancer.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.dropwizard.lifecycle.Managed;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AbstractScheduledService;
import com.portol.common.model.ToshiReply;
import com.portol.common.model.cdn.CDNReply;
import com.portol.common.model.cdn.RegionalServer;
import com.portol.common.model.payment.Payment;
import com.portol.common.model.payment.PaymentInfoRequest;
import com.portol.loadbalancer.config.CDNConfiguration;


//used solely by address service to check balances quickly in a multithreaded environment
public class CDNService extends AbstractScheduledService implements Managed{

	private static final Logger logger = LoggerFactory.getLogger(CDNService.class);
	final Client cClient; 

	private CDNConfiguration config;

	public CDNService(Client jClient, CDNConfiguration config) {
		this.cClient = jClient;
		this.config = config;
	}

	@Override
	public void start() throws Exception {
		this.startAsync().awaitRunning();
		
	}

	@Override
	public void stop() throws Exception {
		this.stopAsync().awaitTerminated();
		
	}

	@Override
	protected void runOneIteration() throws Exception {
		logger.info("CDN service updating status...");
		
		this.refreshCDNStatus();
	}

	private void refreshCDNStatus() throws URISyntaxException {
		
		URI uri = new URI(config.protocol + config.cdnHostIp + ":" + config.port + config.registrationPath);
		logger.info("Refreshing CDN status with server @ " + uri.toString());

		RegionalServer me = new RegionalServer();
		me.setHost(config.thisHost);
		me.setId(config.thisHost);

		

		CDNReply resp = cClient.target(uri).request().post(Entity.json(me), CDNReply.class);

		logger.info("received reply from cdn:\n"+resp.toString());
		
	}

	@Override
	protected Scheduler scheduler() {
		return AbstractScheduledService.Scheduler.newFixedRateSchedule(0, 120, TimeUnit.SECONDS);
	}

	
}
