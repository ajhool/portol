package com.portol.mobileapi.resource;

import io.dropwizard.lifecycle.Managed;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.AbstractScheduledService;
import com.google.common.util.concurrent.AbstractScheduledService.Scheduler;
import com.portol.common.model.LightweightContentInfo;
import com.portol.common.model.PortolPlatform;
import com.portol.common.model.content.Content;
import com.portol.common.model.content.ContentMetadata;
import com.portol.common.model.content.HistoryItem;
import com.portol.common.model.player.Player;
import com.portol.common.model.player.Player.Status;
import com.portol.common.model.user.User;
import com.portol.mobileapi.repository.ContentRepository;
import com.portol.mobileapi.repository.MetadataRepository;
import com.portol.mobileapi.repository.PlayerRepository;
import com.portol.mobileapi.repository.UserRepository;
import com.portol.mobileapi.websocket.JSR356Endpoint;

@Path("/v0/player/async")
public class AppAsyncResource implements Managed, JSR356Endpoint.WsSimpleInterface {

	private final UserRepository userRepo;
	private JSR356Endpoint socketClient;
	private MetadataRepository metaRepo;
	private ContentRepository contentRepo;

	private ObjectMapper mapper = new ObjectMapper();

	private static final Logger logger = LoggerFactory.getLogger(AppAsyncResource.class);

	public AppAsyncResource(UserRepository userRepo, MetadataRepository metaRepo, ContentRepository contentRepo) {
		this.contentRepo = contentRepo;
		this.userRepo = userRepo;
		this.socketClient = new JSR356Endpoint();
		this.metaRepo = metaRepo;
		this.socketClient.setListener(this);

	}

	@Path("/new")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed
	public Player addNewPlayer(Player toAdd) throws Exception {

		User toUpdate = userRepo.findUserLoggedOnPlatform(toAdd.getHostPlatform().getPlatformId());

		if (toUpdate == null) {
			return null;
		}

		PortolPlatform platformOnline = toUpdate.findPlatformById(toAdd.getHostPlatform().getPlatformId());

		platformOnline.incrementActive();

		socketClient.writeTo(toUpdate.getUserId(), mapper.writeValueAsString(toAdd));

		userRepo.save(toUpdate);

		return toAdd;

	}

	@Path("/preview")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed
	public void previewAlert(Player usersPlayer, @QueryParam("userId") String previewUser)
			throws JsonProcessingException {

		socketClient.writeTo(previewUser, mapper.writeValueAsString(usersPlayer));

	}

	@Path("/delete")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed
	public Player deletePlayer(Player toDel) throws Exception {

		User toUpdate = userRepo.findUserLoggedOnPlatform(toDel.getHostPlatform().getPlatformId());
		if (toUpdate == null) {
			return null;
		}
		PortolPlatform platformOnline = toUpdate.findPlatformById(toDel.getHostPlatform().getPlatformId());

		platformOnline.decrementActive();
		socketClient.writeTo(toUpdate.getUserId(), mapper.writeValueAsString(toDel));

		userRepo.save(toUpdate);
		return toDel;

	}

	private void notifyUserOfDeath(String userId, String playerId) {
		socketClient.writeTo(userId, "Dead: " + playerId);

	}

	@Override
	public void start() throws Exception {
		// this.startAsync().awaitRunning();

	}

	@Override
	public void stop() throws Exception {
		// this.stopAsync().awaitTerminated();

	}

	@Override
	public void wsOpen(String idOfUser) {

		User pairing = null;
		try {
			pairing = userRepo.findOneById(idOfUser);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ArrayList<PortolPlatform> updated = new ArrayList<PortolPlatform>();
		for (PortolPlatform plat : pairing.getPlatforms()) {
			plat.setPaired(true);

			updated.add(plat);

		}

		pairing.setPlatforms(updated);

		int numRemaining = Math.max(pairing.getNumConnections() + 1, 0);

		pairing.setNumConnections(numRemaining);
		userRepo.save(pairing);

	}

	@Override
	public void wsClose(String idOfUser) {

		logger.info("Web socked for user: " + idOfUser + "Closed");
		User pairing = null;
		try {
			pairing = userRepo.findOneById(idOfUser);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int numRemaining = Math.max(pairing.getNumConnections() - 1, 0);

		pairing.setNumConnections(numRemaining);

		if (numRemaining == 0) {
			ArrayList<PortolPlatform> updated = new ArrayList<PortolPlatform>();
			for (PortolPlatform plat : pairing.getPlatforms()) {
				plat.setPaired(false);

				updated.add(plat);

			}

			pairing.setPlatforms(updated);
		}
		userRepo.save(pairing);
	}

	public void externalPurchase(Player usersPlayer, User buying) throws JsonProcessingException {

		socketClient.writeTo(buying.getUserId(), mapper.writeValueAsString(usersPlayer));

	}

}
