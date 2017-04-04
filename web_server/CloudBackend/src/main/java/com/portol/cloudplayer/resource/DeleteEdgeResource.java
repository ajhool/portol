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
public class DeleteEdgeResource {

	private LocalPlayerRepository localPlayerRepo;
	private BackendInstance _this;
	
	public DeleteEdgeResource(LocalPlayerRepository localPlayers, BackendInstance _this2) {
		localPlayerRepo = localPlayers;
		this._this = _this2;
	}

	@POST
	@Path("/delete")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed
	public Player deletePlayer(CloudCommand toDelete){

		if(toDelete == null){
			throw new BadRequestException();
		}

		if(!validate(toDelete.getApiToken())){
			throw new javax.ws.rs.ForbiddenException("API key invalid");
		}

		return localPlayerRepo.delete(toDelete.getPlayer());
	}

	private boolean validate(String apiToken) {

		if(apiToken.equalsIgnoreCase("REDACTED")){
			return true;
		} else return false;
	}

}
