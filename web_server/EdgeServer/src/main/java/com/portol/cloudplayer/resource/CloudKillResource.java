package com.portol.cloudplayer.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;
import com.portol.cloudplayer.socket.ClientWSEndpoint;
import com.portol.cloudplayer.socket.PlayerWSEndpoint;
import com.portol.common.model.CloudCommand;
import com.portol.common.model.CloudCommand.Type;
import com.portol.common.model.instance.EdgeInstance;

@Path("/api/v0/kill")
public class CloudKillResource {

	private EdgeInstance _this;
	
	
	
	public CloudKillResource(EdgeInstance thisRef){
		this._this = thisRef; 
	}
	
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed
	public EdgeInstance die(CloudCommand killReq){
		
		if(killReq.getType() == Type.KILL){
			return _this;
			
		} else return null;
	
		
		
	}
	
}
