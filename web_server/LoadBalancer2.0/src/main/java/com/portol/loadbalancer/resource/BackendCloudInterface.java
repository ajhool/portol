package com.portol.loadbalancer.resource;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;
import com.portol.common.model.instance.BackendInstance;
import com.portol.common.model.instance.Instance;
import com.portol.common.model.instance.Instance.Status;
import com.portol.common.model.CloudReply;
import com.portol.loadbalancer.repo.BackendCloudRepository;
import com.portol.loadbalancer.repo.EdgeCloudRepository;
import com.portol.loadbalancer.repo.ContentRepository;
import com.portol.loadbalancer.repo.PlayerRepository;
import com.portol.loadbalancer.service.BackendReadyQueue;
import com.portol.loadbalancer.service.CloudCommunicator;
import com.portol.loadbalancer.service.EdgeReadyQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.Name;
import org.xbill.DNS.ReverseMap;
//this is an endpoint for all backend clouds to report in to the loadbalancer
@Path("/api/v0/cloud/backend")
public class BackendCloudInterface {

	private static final Logger logger = LoggerFactory.getLogger(BackendCloudInterface.class);
	private PlayerRepository playerRepo;
	private ContentRepository contentRepo;
	private CloudCommunicator comm;
	private BackendCloudRepository backendRepo;
	private BackendReadyQueue readyQBack; 

	public BackendCloudInterface(PlayerRepository playerrepo,
			 ContentRepository contentrepo, CloudCommunicator comm,
			BackendReadyQueue readyQBack, BackendCloudRepository backends) {

		this.playerRepo = playerrepo;
		this.contentRepo = contentrepo;
		this.comm = comm;
		this.readyQBack = readyQBack;
		this.backendRepo = backends;


	}
	
	@POST
	@Timed
	@Path("/boot")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public CloudReply onBoot(BackendInstance reportingIn, @Context HttpServletRequest request) throws Exception{
		//try to match this incoming message with the cloud we already stored in the DB 
		//we can use IP for this
		String requestIP = request.getRemoteHost();

		BackendInstance readyToQueue = backendRepo.findBackendWithIP(requestIP);
		
		Name result = ReverseMap.fromAddress(requestIP);
		
		String host = result.canonicalize().toString();
		
		if(readyToQueue == null){
			throw new Exception("no cloud with matching id found");
		}

		readyToQueue.setHost_dns(host);
		
		// use db cloud as master
		readyToQueue.setStatus(Instance.Status.QUEUED);

		//set up local source info
		
		CloudReply ack = new CloudReply();

		ack.setAdminKey(readyToQueue.getAdminKey());
		ack.setVal(CloudReply.Value.BOOT_ACK);
		ack.setCloudPlayerId(readyToQueue.getId());
		ack.setSent(new Date(System.currentTimeMillis()));
		ack.setNewBackendState(readyToQueue);
		
		logger.debug("New cloud instance sent back to client: " + readyToQueue);
		backendRepo.save(readyToQueue);

		return ack;
	}

	@POST
	@Timed
	@Path("/keepAlive")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public CloudReply keepAlive(BackendInstance checkingIn) throws Exception{

		BackendInstance master = backendRepo.findBackendWithID(checkingIn.getId());

		if(master == null){
			throw new Exception("no id found for this cloud player: " + checkingIn.toString());
		}

		master.setLastReport(new Date(System.currentTimeMillis()));

		CloudReply reply = new CloudReply();
		reply.setVal(CloudReply.Value.KEEPALIVE_ACK);
		reply.setSent(new Date(System.currentTimeMillis()));
		reply.setCloudPlayerId(master.getId());

		return reply;

	}

	@POST
	@Timed
	@Path("/event")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public CloudReply cloudEvent(BackendInstance hasEvent) throws Exception{

		BackendInstance master = backendRepo.findBackendWithID(hasEvent.getId());

		//sum hax??
		if(master == null){
			throw new Exception("no id found for this cloud player: " + hasEvent.toString());
		}

		CloudReply reply = new CloudReply();

		switch(hasEvent.getEvent()){
		case BAD_PAY:
			break;
		case NO_CLIENTS:
			master.setStatus(Status.QUEUED);
			master.setLastReport(new Date(System.currentTimeMillis()));
			master.setServing(null);
			backendRepo.save(master);
			break;
		default:
			throw new Exception("not yet implemented");

		}

		return reply;
	}




}
