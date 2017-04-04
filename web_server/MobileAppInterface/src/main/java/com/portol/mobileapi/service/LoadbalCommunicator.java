package com.portol.mobileapi.service;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.UriBuilder;

import com.portol.common.model.CloudReply;
import com.portol.common.model.CloudReply.Value;
import com.portol.common.model.instance.EdgeInstance;
import com.portol.common.model.player.Player;
import com.portol.common.model.player.ServerReply;
import com.portol.common.model.user.User;
import com.portol.mobileapi.config.LoadbalCommunicatorConfig;

public class LoadbalCommunicator {

	
	private final LoadbalCommunicatorConfig conf;
	private final Client lbComm; 
	
	public LoadbalCommunicator(LoadbalCommunicatorConfig lbCommConf,
			Client jClient) {
		this.conf = lbCommConf;
		this.lbComm = jClient;
	}

	 
	public String earlyLoad(Player usersPlayer) {
		UriBuilder uribuild = UriBuilder.fromUri(conf.loadbalURL + ":" + conf.port + conf.startPath);

		ServerReply resp = lbComm.target(uribuild).request().post(Entity.json(usersPlayer), ServerReply.class);

		if(resp != null){
			return resp.getDedicatedCloudHost();
		} else return null;
		
	}
	
	public boolean alertLogin(User loggingIn) {
		UriBuilder uribuild = UriBuilder.fromUri(conf.loadbalURL + ":" + conf.port + conf.loginAlertPath);

		ServerReply resp = lbComm.target(uribuild).request().post(Entity.json(loggingIn), ServerReply.class);

		if(resp == null){
			return false;
		} else return true;
		
	}
	
	
	
}
