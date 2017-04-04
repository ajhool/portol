package com.portol.cloudplayer.resource;

import java.io.IOException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dropwizard.lifecycle.Managed;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portol.cloudplayer.repository.LocalPlayerRepository;
import com.portol.cloudplayer.resource.ClientAPIResource;
import com.portol.cloudplayer.resource.PlayerAPIResource;
import com.portol.cloudplayer.service.LoadbalCommunicator;
import com.portol.common.model.CloudCommand;
import com.portol.common.model.instance.EdgeInstance;
import com.portol.common.model.payment.Payment.Status;
import com.portol.common.model.player.Player;

@Path("/api/v0/lifecycle")
public class PlayerLifecycleResource {

	private ConcurrentHashMap<String, Timer> timers = new ConcurrentHashMap<String, Timer>();

	private static final Logger logger = LoggerFactory.getLogger("playerlogger");

	private LocalPlayerRepository localPlayerRepo;
	private EdgeInstance _this;
	private LoadbalCommunicator lbComm;
	private PlayerSocketAPI playerSock;

	private SetContentResource contentRes;

	public PlayerLifecycleResource(PlayerSocketAPI sock, LoadbalCommunicator lbComm, EdgeInstance _this,
			LocalPlayerRepository locals, SetContentResource contentRes) {
		super();
		this.localPlayerRepo = locals;
		this._this = _this;
		this.contentRes = contentRes;
		this.lbComm = lbComm;
		this.playerSock = sock;

	}

	private TimerTask generateNewTimeoutTask(final String playerId) {

		TimerTask myTask = new TimerTask() {

			@Override
			public void run() {
				// do action
				logger.info("Player: " + playerId + "timed out");
				onTimeout(playerId);

			}
		};

		return myTask;
	}

	private void onTimeout(String playerId) {

		// remove defunct timer
		Timer dead = timers.remove(playerId);
		dead.cancel();

		// removed timed out player, send back to loadbal
		Player timedOut = localPlayerRepo.delete(playerId);

		if (timedOut == null) {
			// then this is a zombie - player must have already disconnected
			logger.info("fielded timeout timer for player: " + playerId + ", but had been deleted previously");
			return;
		}

		if (timedOut.getPlayerPayment().getStatus() == Status.COMPLETE) {
			// this this was a main content timeout
			timedOut.setStatus(Player.Status.TIMEOUT);
			logger.info("player: " + playerId + " had a TIMEOUT");
		} else {
			// otherwise, was a preview timeout
			timedOut.setStatus(Player.Status.PREVIEW_TIMEOUT);
			logger.info("player: " + playerId + " had a PREVIEW TIMEOUT");
		}

		lbComm.pushUpdatedPlayer(timedOut, _this);

		// signal player
		playerSock.sendTimeout(timedOut);

		// close socket
		playerSock.closeConnection(timedOut);

	}

	@Context
	HttpServletResponse resp;

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/addplayer")
	@Timed
	public Player addPlayer(CloudCommand toAdd) throws Exception {

		if (toAdd == null) {
			throw new BadRequestException();
		}

		if (!validate(toAdd.getApiToken())) {
			throw new javax.ws.rs.ForbiddenException("API key invalid");
		}

		localPlayerRepo.save(toAdd.getPlayer());

		Timer timeOut = new Timer();

		long expiryTime = toAdd.getPlayer().getTimeExpire();

		if (expiryTime == 0) {
			logger.warn("warning, no expiration time specified, attempting to infer from currently serving content...");

			if (_this.getServing() == null) {
				resp.sendError(403, "no expiration time specified, and unable to infer it");
				return null;
			} else {

				int duration = Integer.MAX_VALUE;
				// attempt to infer the time
				if (toAdd.getPlayer().getPlayerPayment().getStatus() == Status.COMPLETE) {
					// then it is paid for
					duration = _this.getServing().getLengthInSec();

				} else {
					// then it is a preview
					duration = _this.getServing().getPreviewLengthInSec();
				}

				int previewDurationMillis = (int) (duration * 1000 * _this.getServing().getGracePeriod());
				logger.info("duration until time out: " + previewDurationMillis + " ms");
				expiryTime = System.currentTimeMillis() + previewDurationMillis;
				if (toAdd.getPlayer().getPlayerPayment().getStatus() == Status.COMPLETE) {
					logger.info("MAIN CONTENT: Player: " + toAdd.getPlayer().getPlayerId() + " set to expire at: "
							+ new Date(expiryTime) + ", current time is: " + new Date(System.currentTimeMillis()));

				} else {
					logger.info("PREVIEW: Player: " + toAdd.getPlayer().getPlayerId() + " set to expire at: "
							+ new Date(expiryTime) + ", current time is: " + new Date(System.currentTimeMillis()));

				}

			}

		}

		Date expires = new Date(expiryTime);
		timeOut.schedule(this.generateNewTimeoutTask(toAdd.getPlayer().getPlayerId()), expires);

		logger.info(
				"added player: " + toAdd.getPlayer().getPlayerId() + " that will time out at: " + expires.toString());
		Timer existing = this.timers.put(toAdd.getPlayer().getPlayerId(), timeOut);

		if (existing != null) {
			logger.warn("existing timer was here for player: " + toAdd.getPlayer().getPlayerId() + ", discarding");
			existing.cancel();
		}
		Player added = localPlayerRepo.findOneById(toAdd.getPlayer().playerId);
		// if player payment is complete, forward that information to client
		if (added.getPlayerPayment().getStatus() == Status.COMPLETE) {
			// signal player
			playerSock.sendCompletePayment(added);

			// trigger full download if not already happening
			contentRes.upgradeContent();

		}

		return added;
	}

	@POST
	@Path("/deleteplayer")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed
	public Player deletePlayer(CloudCommand toDelete) {

		if (toDelete == null) {
			throw new BadRequestException();
		}

		if (!validate(toDelete.getApiToken())) {
			throw new javax.ws.rs.ForbiddenException("API key invalid");
		}

		Timer dead = this.timers.remove(toDelete.getPlayer().getPlayerId());

		dead.cancel();

		return localPlayerRepo.delete(toDelete.getPlayer());
	}

	private boolean validate(String apiToken) {
		if (apiToken.equalsIgnoreCase("bar")) {
			return true;
		} else
			return false;
	}

}
