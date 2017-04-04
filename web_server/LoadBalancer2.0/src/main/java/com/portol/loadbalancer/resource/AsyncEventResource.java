package com.portol.loadbalancer.resource;

import java.util.Date;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.codahale.metrics.annotation.Timed;
import com.portol.common.model.CloudReply;
import com.portol.common.model.CloudReply.Value;
import com.portol.common.model.PortolPlatform;
import com.portol.common.model.content.Content;
import com.portol.common.model.content.Content.Type;
import com.portol.common.model.instance.EdgeInstance;
import com.portol.common.model.payment.Payment;
import com.portol.common.model.player.Player;
import com.portol.common.model.user.User;
import com.portol.loadbalancer.manager.MongoManaged;
import com.portol.loadbalancer.repo.EdgeCloudRepository;
import com.portol.loadbalancer.repo.ContentRepository;
import com.portol.loadbalancer.repo.PlayerRepository;
import com.portol.loadbalancer.service.AddressService;
import com.portol.loadbalancer.service.CloudCommunicator;
import com.portol.loadbalancer.service.EdgeReadyQueue;

@Path("/api/v0/async")
public class AsyncEventResource {

	private PlayerRepository playerRepo;

	private AddressService addrSvc;

	private ContentRepository contentRepo;

	private EdgeCloudRepository cloudRepo;

	private EdgeReadyQueue readyQ;

	private CloudCommunicator comm;


	public AsyncEventResource(PlayerRepository playerrepo,
			AddressService addrSvc, ContentRepository contentrepo,
			EdgeCloudRepository cloudrepo, EdgeReadyQueue readyQ,
			CloudCommunicator comm) {
		super();
		this.playerRepo = playerrepo;
		this.addrSvc = addrSvc;
		this.contentRepo = contentrepo;
		this.cloudRepo = cloudrepo;
		this.readyQ = readyQ;
		this.comm = comm;
	}




	@Path("/login")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed
	public Response broadcastLogin(User loggingIn) throws Exception{

		//this method broadcasts the login event to any eligible clouds
		//it should avoid any persistent side effects
		for(PortolPlatform plat : loggingIn.getPlatforms()){
			List<Player> actives = playerRepo.findActivePairedPlayers(plat);

			for(Player active : actives){
				final Player toAlert = active; 
				Thread alerter = new Thread() {

					@Override
					public void run(){
						comm.alertLogin(toAlert);
					}
				};
				
				alerter.start();
			}

		}

		return Response.ok().build();
	}
}
