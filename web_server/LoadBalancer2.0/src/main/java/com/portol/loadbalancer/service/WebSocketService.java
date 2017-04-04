package com.portol.loadbalancer.service;

import io.dropwizard.lifecycle.Managed;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portol.common.model.player.Player;
import com.portol.common.model.player.Player.Status;
import com.portol.common.model.player.ServerReply;
import com.portol.loadbalancer.repo.PlayerRepository;
import com.portol.loadbalancer.socket.JSR356Endpoint;

public class WebSocketService implements JSR356Endpoint.WsSimpleInterface, Managed{

	private static Logger logger = LoggerFactory.getLogger(WebSocketService.class);
	private JSR356Endpoint socketClient;
	private PlayerRepository playerRepo;
	MobileDeviceServerClient mClient;
	private final ObjectMapper objMapper;

	public WebSocketService(ObjectMapper objectMapper, MobileDeviceServerClient mClient, PlayerRepository playerRepo) {
		super();
		this.objMapper = objectMapper;

		this.playerRepo = playerRepo;
		socketClient = new JSR356Endpoint();
		socketClient.setListener(this);
		this.mClient = mClient;
		
	}


	@Override
	public void wsOpen(String idOfPlayer){
		Player newlyOpened = null;
		try {
			newlyOpened = playerRepo.findOneById(idOfPlayer);
		} catch (Exception e) {
			logger.error("error finding newly opened player", e);
		}

		mClient.sendContenttoApp(newlyOpened);

	}



	@Override
	public void wsClose(String idOfPlayer) {
		Player newlyClosed = null;
		try {
			newlyClosed = playerRepo.findOneById(idOfPlayer);
		} catch (Exception e) {
			logger.error("error finding newly opened player", e);
		}

		newlyClosed.setStatus(Status.DEAD);
		mClient.sendClosedNotification(newlyClosed);
		playerRepo.save(newlyClosed);



	}


	@Override
	public void start() throws Exception {
		logger.info("starting web socket service");
		
	}


	@Override
	public void stop() throws Exception {
		logger.info("stopping web socket service");
		
	}

}
