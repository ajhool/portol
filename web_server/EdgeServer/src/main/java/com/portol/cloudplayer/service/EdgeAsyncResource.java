package com.portol.cloudplayer.service;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dropwizard.lifecycle.Managed;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portol.cloudplayer.repository.LocalPlayerRepository;
import com.portol.cloudplayer.resource.ClientAPIResource;
import com.portol.cloudplayer.resource.PlayerAPIResource;
import com.portol.cloudplayer.socket.*;
import com.portol.common.model.PlayerEvent;
import com.portol.common.model.SeekStatus;
import com.portol.common.model.PlayerEvent.Event;
import com.portol.common.model.app.AppCommand;
import com.portol.common.model.instance.EdgeInstance;
import com.portol.common.model.player.Player;
import com.portol.common.model.player.Player.Status;

public class EdgeAsyncResource
implements ClientWSEndpoint.ClientEventInterface, PlayerWSEndpoint.PlayerEventInterface, Managed {

	private ClientWSEndpoint clientWS;
	private PlayerWSEndpoint playerWS;
	private ClientAPIResource clientRes;
	private PlayerAPIResource playerRes;
	private static final Logger logger = LoggerFactory.getLogger(EdgeAsyncResource.class);

	private LocalPlayerRepository playerRepo;
	private LoadbalCommunicator lbComm;
	private EdgeInstance _this;

	public EdgeAsyncResource(ObjectMapper om, ClientAPIResource res, PlayerAPIResource playerRes,
			ClientWSEndpoint client, PlayerWSEndpoint player, LocalPlayerRepository localRepo, LoadbalCommunicator lbComm, EdgeInstance _this) {
		super();
		clientWS = client;
		clientWS.setListener(this);

		playerWS = player;
		playerWS.setListener(this);

		this._this = _this; 
		this.clientRes = res;
		this.playerRes = playerRes;
		this.playerRepo = localRepo;
		this.lbComm = lbComm;

	}

	@Override
	public void onClientEvent(String targetPlayerId, AppCommand event) {
		event.setTargetPlayerId(targetPlayerId);
		try {
			this.clientRes.recvAppCommand(event);
		} catch (Exception e) {
			logger.error("Error in recvappcommand call made from client", e);
		}

	}

	@Override
	public void onClientDisconnect(String playerId) {

	}

	@Override
	public void onPlayerEvent(PlayerEvent event, String playerId) {
		event.setPlayerId(playerId);
		try {
			this.playerRes.recvPlayerEvent(event);
		} catch (Exception e) {
			logger.error("Error in recvappcommand call made from client", e);
		}
	}

	@Override
	public void onPlayerDisconnect(String playerId) {
		// player has died
		Player deleted = playerRepo.delete(playerId);
		if(deleted != null){
			deleted.setStatus(Status.DEAD);
			lbComm.pushUpdatedPlayer(deleted, _this);
			logger.info("reporting player: " + deleted.getPlayerId() + " as DEAD");
		}

	}

	@Override
	public void start() throws Exception {
	}

	@Override
	public void stop() throws Exception {

	}

	@Override
	public void onPlayerConnect(String playerId) {
		// send the most recent player available
		Player existing = playerRepo.findOneById(playerId);

		if (existing == null) {
			// then this player is not allowed
			playerWS.disconnect(playerId, 401);
		}

		// otherwise, send the most recent state of the player
		this.sendCurrent(existing);

	}

	ObjectMapper mapper = new ObjectMapper();

	public boolean sendCurrent(Player state) {
		try {
			String JSONReply = mapper.writeValueAsString(state);

			playerWS.writeTo(state.playerId, JSONReply);

		} catch (IOException e) {
			throw new RuntimeException("Error when writing the event.", e);
		}
		return true;

	}
}
