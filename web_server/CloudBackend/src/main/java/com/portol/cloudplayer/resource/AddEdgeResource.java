package com.portol.cloudplayer.resource;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;
import com.portol.cloudplayer.repository.LocalPlayerRepository;
import com.portol.common.model.CloudCommand;
import com.portol.common.model.instance.BackendInstance;
import com.portol.common.model.instance.EdgeInstance;
import com.portol.common.model.player.Player;

@Path("/api/v0/edge")
public class AddEdgeResource {

	private LocalPlayerRepository localPlayerRepo;
	private BackendInstance _this;
	public AddEdgeResource(LocalPlayerRepository localPlayers, BackendInstance _this2) {
		localPlayerRepo = localPlayers;
		this._this = _this2;
	}
	
	@POST
	@Path("/add")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed
	public Player addPlayer(CloudCommand toAdd){
		
		if(toAdd == null){
			throw new BadRequestException();
		}
		
		if(!validate(toAdd.getApiToken())){
			throw new javax.ws.rs.ForbiddenException("API key invalid");
		}
		
		localPlayerRepo.save(toAdd.getPlayer());
		
		return localPlayerRepo.findOneById(toAdd.getPlayer().playerId);
	}

	private boolean validate(String apiToken) {
		if(apiToken.equalsIgnoreCase("bar")){
			return true;
		} else return false;
	}

}
