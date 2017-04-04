package com.portol.cloudplayer.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;
import com.portol.common.model.CloudCommand;
import com.portol.common.model.instance.EdgeInstance;
import com.portol.common.model.player.Player;

@Path("/api/v0/event/user")
public class UserEventResource {

	private PlayerSocketAPI sockAPI;
	
	
	public UserEventResource(PlayerSocketAPI sockAPI){
		this.sockAPI = sockAPI;
	}
	
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/login")
	@Timed
	public Player loginUser(CloudCommand statusReq){
		
		Player toAlert = statusReq.getPlayer();
		
		if(toAlert == null ) return null;
		
		sockAPI.alertLogin(toAlert);
		
		return toAlert;
		
	}
	
	
	
}
