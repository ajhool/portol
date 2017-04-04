package com.portol.loadbalancer.resource;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;
import com.portol.common.model.instance.EdgeInstance;
import com.portol.common.model.instance.Instance.Status;
import com.portol.common.model.player.Player;
import com.portol.common.model.CloudReply;
import com.portol.loadbalancer.repo.BackendCloudRepository;
import com.portol.loadbalancer.repo.EdgeCloudRepository;
import com.portol.loadbalancer.repo.ContentRepository;
import com.portol.loadbalancer.repo.PlayerRepository;
import com.portol.loadbalancer.service.BackendReadyQueue;
import com.portol.loadbalancer.service.CloudCommunicator;
import com.portol.loadbalancer.service.EdgeReadyQueue;
import com.portol.loadbalancer.service.MobileDeviceServerClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.Name;
import org.xbill.DNS.ReverseMap;

//this is an endpoint for all clouds to report in to the loadbalancer
@Path("/api/v0/cloud/edge")
public class EdgeCloudInterface {

	private static final Logger logger = LoggerFactory.getLogger(EdgeCloudInterface.class);
	private PlayerRepository playerRepo;
	private EdgeCloudRepository cloudRepo;
	private ContentRepository contentRepo;
	private CloudCommunicator comm;
	private EdgeReadyQueue readyQEdge;
	private MobileDeviceServerClient mClient;

	public EdgeCloudInterface(PlayerRepository playerrepo, EdgeCloudRepository cloudrepo, ContentRepository contentrepo,
			CloudCommunicator comm, EdgeReadyQueue readyQ, MobileDeviceServerClient mClient) {

		this.playerRepo = playerrepo;
		this.cloudRepo = cloudrepo;
		this.contentRepo = contentrepo;
		this.comm = comm;
		this.readyQEdge = readyQ;
		this.mClient = mClient;
	}

	@POST
	@Timed
	@Path("/boot")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public CloudReply onBoot(EdgeInstance reportingIn, @Context HttpServletRequest request) throws Exception {
		// try to match this incoming message with the cloud we already stored
		// in the DB
		// we can use IP for this

		String requestIP = request.getRemoteHost();

		EdgeInstance readyToQueue = cloudRepo.findEdgeWithIP(requestIP);

		Name result = ReverseMap.fromAddress(requestIP);

		String host = result.canonicalize().toString();

		if (readyToQueue == null) {
			throw new Exception("no cloud with matching id found... yikes");
		}

		readyToQueue.setHost_dns(host);
		// lets use db cloud as master
		readyToQueue.setStatus(EdgeInstance.Status.QUEUED);

		// set up local source info

		CloudReply ack = new CloudReply();

		ack.setAdminKey(readyToQueue.getAdminKey());
		ack.setVal(CloudReply.Value.BOOT_ACK);
		ack.setCloudPlayerId(readyToQueue.getId());
		ack.setSent(new Date(System.currentTimeMillis()));
		ack.setNewEdgeInstance(readyToQueue);

		logger.debug("New cloud instance sent back to client: " + readyToQueue);
		cloudRepo.save(readyToQueue);

		return ack;
	}

	@POST
	@Timed
	@Path("/keepAlive")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public CloudReply keepAlive(EdgeInstance checkingIn) throws Exception {

		EdgeInstance master = cloudRepo.findEdgeWithID(checkingIn.getId());

		if (master == null) {
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
	@Path("/player/event")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public CloudReply onPlayerEvent(Player withEvent, @QueryParam("edgeId") String edgeId) throws Exception {

		Player master = playerRepo.findOneById(withEvent.getPlayerId());
		EdgeInstance reporting = cloudRepo.findEdgeWithID(edgeId);

		if (master == null || reporting == null) {
			throw new Exception("no id found for this player/cloud combo: " + withEvent.playerId);
		}

		reporting.setLastReport(new Date(System.currentTimeMillis()));

		CloudReply reply = new CloudReply();
		reply.setVal(CloudReply.Value.PLAYER_ACK);
		reply.setSent(new Date(System.currentTimeMillis()));
		reply.setCloudPlayerId(reporting.getId());

		switch (withEvent.getStatus()) {
		case DEAD:
			master.setStatus(withEvent.getStatus());
			// remove source ip, this cloud is open
			master.setCurrentSourceIP(null);
			break;
		case DONE_STREAMING:

			master.setStatus(withEvent.getStatus());

			// remove source ip, this cloud is open
			master.setCurrentSourceIP(null);

			break;
		case FAILURE_PAYMENT:
			break;
		case FAILURE_STREAM:
			break;
		case PAUSED:
			break;
		case PREVIEW_STREAMING:
			break;
		case PREVIEW_TIMEOUT:
			master.setStatus(withEvent.getStatus());

			// remove source ip, this cloud is open
			master.setCurrentSourceIP(null);

			break;
		case REPEAT_SCREEN:
			break;
		case SPLASH_SCREEN:
			break;
		case STOPPED:
			break;
		case STREAMING:
			break;
		case TIMEOUT:
			master.setStatus(withEvent.getStatus());
			break;
		case UNINITIALIZED:
			break;
		default:
			break;

		}

		if (master.getHostPlatform().getPlatformColor() != null) {
			// alert app to player event
			mClient.sendContenttoApp(master);
		}

		cloudRepo.save(reporting);
		playerRepo.save(master);
		return reply;

	}

	@POST
	@Timed
	@Path("/playerEvent")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public CloudReply cloudEvent(EdgeInstance hasEvent) throws Exception {

		EdgeInstance master = cloudRepo.findEdgeWithID(hasEvent.getId());

		if (master == null) {
			throw new Exception("no id found for this cloud player: " + hasEvent.toString());
		}

		CloudReply reply = new CloudReply();

		switch (hasEvent.getEvent()) {
		case BAD_PAY:
			break;
		case NO_CLIENTS:
			master.setStatus(Status.IDLE);
			master.setLastReport(new Date(System.currentTimeMillis()));
			cloudRepo.save(master);
			break;

		case RECONNECT:
			master.setStatus(Status.RUNNING);
			master.setLastReport(new Date(System.currentTimeMillis()));
			cloudRepo.save(master);
			break;
		default:
			throw new Exception("not yet implemented");

		}

		return reply;
	}

}
