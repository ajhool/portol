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

@Path("/api/v0/status")
public class CloudStatusResource {

	private EdgeInstance _this;
	
	
	
	public CloudStatusResource(EdgeInstance thisRef){
		this._this = thisRef; 
	}
	
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed
	public EdgeInstance getStatus(CloudCommand statusReq){

		return _this;
		
	}
	
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed
	public EdgeInstance getStatus(){
		return _this;
		
	}
	
}
