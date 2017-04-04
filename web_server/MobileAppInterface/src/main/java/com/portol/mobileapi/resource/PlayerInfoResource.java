package com.portol.mobileapi.resource;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;
import com.portol.common.model.PortolPlatform;
import com.portol.common.model.content.Content;
import com.portol.common.model.content.ContentMetadata;
import com.portol.common.model.content.ContentSearchRequest;
import com.portol.common.model.content.ContentSearchRequest.RequestType;
import com.portol.common.model.player.Player;
import com.portol.common.model.player.PlayerGetRequest;
import com.portol.common.model.user.User;
import com.portol.mobileapi.repository.ContentRepository;
import com.portol.mobileapi.repository.MetadataRepository;
import com.portol.mobileapi.repository.PlayerRepository;
import com.portol.mobileapi.repository.UserRepository;

//use this to seach for content on the mobile app
//can also get top videos, etc...
@Path("/v0")
public class PlayerInfoResource {

	private ContentRepository contentRepo;
	private PlayerRepository playerRepo;
	private UserRepository userRepo;

	public PlayerInfoResource(ContentRepository contentrepo,
			PlayerRepository splashrepo, UserRepository userRepo) {
		super();
		this.contentRepo = contentrepo;
		this.playerRepo = splashrepo;
		this.userRepo = userRepo;
	}


	@POST
	@Path("/player/code")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed
	public Player getCode(PlayerGetRequest req) throws Exception{

		String wantsPlayersId = req.getParingCode();

		Player matching = playerRepo.findOneByPartialPlayerID(wantsPlayersId);

		return matching;
	}




	@POST
	@Path("/player/address")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed
	public Player getAddress(PlayerGetRequest req) throws Exception{

		String wantsPlayersId = req.getParingCode();

		Player matching = playerRepo.findOneByAddress(wantsPlayersId);

		return matching;
	}
	@POST
	@Path("/player/playerId")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed
	public Player getPlayerId(PlayerGetRequest req) throws Exception{

		String wantsPlayersId = req.getParingCode();

		Player matching = playerRepo.findOneById(wantsPlayersId);

		return matching;
	}
	@GET
	@Path("/player/active/{platform}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed
	public List<Player> getAllActivePlayers(@PathParam("platform") String platformId) throws Exception{



		List<Player> results = playerRepo.findActivePlayersOnPlatform(platformId);

		return results;
	}


	private List<PortolPlatform> findCurrentDevicesForUser(String wantsPlayersId, int maxAgeInSec) throws Exception {

		if(wantsPlayersId == null || wantsPlayersId.length() < 1){
			return new ArrayList<PortolPlatform>();
		}

		Date oldest = new Date(System.currentTimeMillis() - (maxAgeInSec * 1000));

		User hasDevices = userRepo.findOneById(wantsPlayersId);
		return hasDevices.getPlatforms();



	}

}
