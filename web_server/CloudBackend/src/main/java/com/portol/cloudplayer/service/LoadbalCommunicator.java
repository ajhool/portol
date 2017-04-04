package com.portol.cloudplayer.service;

import java.util.ArrayList;
import java.util.Arrays;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.portol.common.model.CloudReply;
import com.portol.common.model.CloudReply.Value;
import com.portol.common.model.instance.BackendInstance;
import com.portol.common.model.instance.EdgeInstance;


public class LoadbalCommunicator {
	final Client cClient; 
	private String bootPath;
	private String keepAlivePath;
	private int port;
	private String eventPath;
	private String loadbalURL; 

	private static final Logger logger = LoggerFactory.getLogger(LoadbalCommunicator.class);
	
	public LoadbalCommunicator(Client jClient, String keepAlivePath,
			String bootPath, int port, String eventPath, String loadbalUrl) {
		this.cClient = jClient;
		this.loadbalURL = loadbalUrl;
		this.keepAlivePath = keepAlivePath;
		this.eventPath = eventPath;
		this.port = port;
		this.bootPath = bootPath;
	}

	public BackendInstance reportBooted(BackendInstance thisPlayer) {
		UriBuilder uribuild = UriBuilder.fromUri(loadbalURL + ":" + port + bootPath);

		CloudReply resp = cClient.target(uribuild).request().post(Entity.json(thisPlayer), CloudReply.class);

		logger.debug("reported in to loadbal");
		BackendInstance updated = resp.getNewBackendState();
		
		logger.debug("cloud backend from loadbal:" + updated);
		updated.setId(resp.getCloudPlayerId());
		updated.setAdminKey(resp.getAdminKey());
		
		logger.debug("after ID, key set: " + updated);
	
		return updated;
	}

	//returns the state the cloud player should ideally be in
	public CloudReply reportEvent(BackendInstance thisCloudPlayer) {
		
		logger.debug("REPORTING EVENT TO LOADBALANCER");
		logger.debug("State of cloudplayer reported back to loadbal: \n" + thisCloudPlayer);
		
		UriBuilder uribuild = UriBuilder.fromUri(loadbalURL + ":" + port + eventPath);

		CloudReply resp = cClient.target(uribuild).request().post(Entity.json(thisCloudPlayer), CloudReply.class);

		return resp;

	}

	public boolean keepAlive(EdgeInstance thisCloudPlayer) {
		UriBuilder uribuild = UriBuilder.fromUri(loadbalURL + ":" + port + this.keepAlivePath);

		CloudReply resp = cClient.target(uribuild).request().post(Entity.json(thisCloudPlayer), CloudReply.class);

		if(resp.getVal() == Value.KEEPALIVE_ACK){
			return true;
		} else return false;

	}
}
