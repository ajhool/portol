package com.portol.loadbalancer.service;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.portol.common.model.CloudCommand;
import com.portol.common.model.content.Content;
import com.portol.common.model.instance.BackendInstance;
import com.portol.common.model.instance.EdgeInstance;
import com.portol.common.model.instance.Instance;
import com.portol.common.model.player.Player;
import com.portol.common.model.user.User;

public class MobileDeviceServerClient {
	final Client cClient; 
	private String deletePlayerPath = "/api/v0/player/async/delete";
	private int port = 9000;
	private String mobileAPIHost = "localhost";
	private static final String protocol = "http://";
	private String addNewPlayerPath = "/api/v0/player/async/new";
	private String previewPath = "/api/v0/player/async/preview";


public static final Logger logger = LoggerFactory.getLogger(MobileDeviceServerClient.class);
	public MobileDeviceServerClient(Client jClient) {
		this.cClient = jClient;

	}

	public void sendContenttoApp(Player newPlayer) {

		final Player toSend = newPlayer;
		Thread sender = new Thread(){
			@Override
			public void run(){
				URI uri = null;
				try {
					uri = new URI(protocol + mobileAPIHost + ":" + port + addNewPlayerPath);
				
				String targetURL = uri.toString();

				Player resp = cClient.target(uri).request().post(Entity.json(toSend), Player.class);
				} catch (Exception e) {
					logger.warn("Error trying to update WS server", e);
					//e.printStackTrace();
				}
			}
		};
		
		sender.start();
		//return resp != null;
	}

	public void sendPreviewPlayer(Player newPlayer, User matching) {

		final Player toSend = newPlayer;
		Thread sender = new Thread(){
			@Override
			public void run(){
				URI uri = null;
				try {
					uri = new URI(protocol + mobileAPIHost + ":" + port + previewPath + "?userId=" + matching.getUserId());
				
				String targetURL = uri.toString();

				Player resp = cClient.target(uri).request().post(Entity.json(toSend), Player.class);
				} catch (Exception e) {
					logger.warn("Error trying to update WS server", e);
					//e.printStackTrace();
				}
			}
		};
		
		sender.start();
		//return resp != null;
	}
	
	public void sendClosedNotification(Player newlyClosed) {
		final Player toSend = newlyClosed;
		Thread sender = new Thread(){
			@Override
			public void run(){
				URI uri = null;
				try {
					uri = new URI(protocol + mobileAPIHost + ":" + port + deletePlayerPath);
				
				String targetURL = uri.toString();

				Player resp = cClient.target(uri).request().post(Entity.json(toSend), Player.class);
				} catch (Exception e) {
					logger.warn("Error trying to update WS server", e);
					//e.printStackTrace();
				}
			}
		};
		
		sender.start();
		
	}

	//	public boolean submitExistingClient(Player refreshing) throws URISyntaxException {
	//		//UriBuilder uribuild = UriBuilder.fromUri(readyCloudPlayer.getLocation() + ":" + port + addPlayerPath);
	//		URI uri = new URI(protocol + this.mobileAPIHost + ":" + port + this.addActivePlayerPath);
	//		String targetURL = uri.toString();
	//		Player resp = cClient.target(uri).request().post(Entity.json(refreshing), Player.class);
	//
	//		return resp != null;
	//
	//	}

}
