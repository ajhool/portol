package com.portol.loadbalancer.resource;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.Master;

import com.codahale.metrics.annotation.Timed;
import com.portol.common.model.PortolPlatform;
import com.portol.common.model.content.Content;
import com.portol.common.model.content.ContentMetadata;
import com.portol.common.model.instance.BackendInstance;
import com.portol.common.model.instance.EdgeInstance;
import com.portol.common.model.instance.Instance;
import com.portol.common.model.payment.Payment;
import com.portol.common.model.payment.Payment.Type;
import com.portol.common.model.player.Player;
import com.portol.common.model.player.Player.Status;
import com.portol.common.model.player.ServerReply;
import com.portol.common.model.user.User;
import com.portol.loadbalancer.manager.MongoManaged;
import com.portol.loadbalancer.repo.BackendCloudRepository;
import com.portol.loadbalancer.repo.EdgeCloudRepository;
import com.portol.loadbalancer.repo.ContentRepository;
import com.portol.loadbalancer.repo.PlayerRepository;
import com.portol.loadbalancer.repo.SplashRepository;
import com.portol.loadbalancer.repo.UserRepository;
import com.portol.loadbalancer.service.AddressService;
import com.portol.loadbalancer.service.BackendReadyQueue;
import com.portol.loadbalancer.service.CloudCommunicator;
import com.portol.loadbalancer.service.MobileDeviceServerClient;
import com.portol.loadbalancer.service.QRMakerClient;
import com.portol.loadbalancer.service.EdgeReadyQueue;

@Path("/api/v0/async/start")
public class AsyncStartResource {

	private ContentRepository contentRepo;
	private SplashRepository splashRepo;
	private EdgeCloudRepository edgeRepo;
	private BackendCloudRepository backendRepo;
	private UserRepository userRepo; 
	private PlayerRepository playerRepo;

	// addrsvc provides the API to check balances in real time
	private AddressService addrSvc;
	private EdgeReadyQueue readyQ;
	private CloudCommunicator comm;
	private QRMakerClient qrsvc;
	private MobileDeviceServerClient sClient;
	private BackendReadyQueue readyQBack;
	private final int delayms;

	@Context private HttpServletResponse response;


	private static Logger logger = LoggerFactory.getLogger(AsyncStartResource.class);
	public AsyncStartResource(ContentRepository contentrepo,
			SplashRepository splashrepo, PlayerRepository playerrepo,
			EdgeCloudRepository cloudrepo, BackendCloudRepository backends,
			AddressService addrSvc, CloudCommunicator comm,
			EdgeReadyQueue readyQ, BackendReadyQueue readyQBack,
			QRMakerClient qrMaker, int delayMs, MobileDeviceServerClient sClient, UserRepository userRepo) {

		// set up DBS
		contentRepo = contentrepo;
		splashRepo = splashrepo;
		edgeRepo = cloudrepo;
		playerRepo = playerrepo;

		// set up services
		this.addrSvc = addrSvc;
		this.readyQ = readyQ;
		this.readyQBack = readyQBack;
		this.comm = comm;
		this.qrsvc = qrMaker;
		this.delayms = delayMs;
		this.backendRepo = backends;
		this.sClient = sClient;
		this.userRepo = userRepo;
	}

	@Context HttpServletResponse resp; 
	@POST
	@Timed
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Player startPlay(Player mostRecent) throws Exception {

		//assume that if a player is being sent here, that it is paid, so don't support the unpaid case well
		if (mostRecent.getPlayerPayment().getStatus() != Payment.Status.COMPLETE){
			return null;
		}

		// if balance is right, prep cloud player + send back an MPD

		// update master's state
		mostRecent.setNumPlayersUsed(mostRecent.getNumPlayersUsed() + 1);
		mostRecent.setStatus(Player.Status.STREAMING);

		// get video from video key in POST body
		Content toPlay = contentRepo.findByVideoKey(mostRecent.getVideoKey());
		EdgeInstance readyCloudPlayer = null;

		if(mostRecent.getCurrentSourceIP() != null){
			//then this player most likely has been previewing on a cloudlet
			//try and find that cloud to maintain session affinity 
			readyCloudPlayer = edgeRepo.findEligibleEdgeWithIP(mostRecent.getCurrentSourceIP());

		} else if(mostRecent.getCurrentCloudPlayerId() != null){
			readyCloudPlayer = edgeRepo.findEdgeWithID(mostRecent.getCurrentCloudPlayerId());
		}

		if(readyCloudPlayer == null){
			readyCloudPlayer = edgeRepo.findEdgePlaying(toPlay);
		}


		if (toPlay.getType() == Content.Type.LIVE) {
			BackendInstance serving = backendRepo
					.findBackendServing(toPlay);

			if (readyCloudPlayer == null) {

				// try to find a local source to use
				if (serving == null) {

					// start the backend streaming server
					serving = readyQBack.getNextBackend();
					serving = comm
							.initLiveStreamingBackend(serving, toPlay);
					serving.setServing(toPlay);
					serving.setStatus(Instance.Status.LIVE_STREAM);
					// readyCloudPlayer.setContent(toPlay);
					backendRepo.save(serving);

				}

				// start the front end serving cloud
				readyCloudPlayer = readyQ.getNextEdge();

				//if null, we can't provide a cloud, so alert the client
				if(readyCloudPlayer == null){
					response.sendError(503, "No servers available right now. Please try again later");
					return null;
				}

				readyCloudPlayer.setLocalSource(serving);
				comm.initEdgePreviewCloud(readyCloudPlayer, toPlay);
				readyCloudPlayer.setStatus(EdgeInstance.Status.RUNNING);

			}

			if (readyCloudPlayer.getLocalSource() == null) {
				readyCloudPlayer.setLocalSource(serving);
			}
			edgeRepo.save(readyCloudPlayer);

		} else { // VOD
			// provision preview player/find existing
			// similar process to previewing

			if (readyCloudPlayer == null) {
				// then we need to provision a new cloud to play this main
				// content

				readyCloudPlayer = edgeRepo.findEdgePreviewing(toPlay);

				if(readyCloudPlayer == null){

					readyCloudPlayer = readyQ.getNextEdge();
					//if null, we can't provide a cloud, so alert the client
					if(readyCloudPlayer == null){
						response.sendError(503, "No servers available right now. Please try again later");

						return null;
					}
				}
			}
			
			comm.initEdgeFullCloud(readyCloudPlayer, toPlay);

			readyCloudPlayer.setServing(toPlay);
			Thread.sleep(delayms);

			readyCloudPlayer.setStatus(Instance.Status.RUNNING);
			edgeRepo.save(readyCloudPlayer);

		}

		comm.addnewClient(readyCloudPlayer, mostRecent);
		mostRecent.setStatus(Player.Status.STREAMING);

		ContentMetadata meta = this.splashRepo.getSplashScreenById(toPlay.getSplashDataId());
		mostRecent.setCurrentSourceIP(readyCloudPlayer.getLocation());

		mostRecent.setCurrentCloudPlayerId(readyCloudPlayer.getId());
		mostRecent.addPlayerUsed(readyCloudPlayer.getId());

		edgeRepo.save(readyCloudPlayer);
		playerRepo.save(mostRecent);

		mostRecent.setLocked(false);
		mostRecent.setLastRequest(System.currentTimeMillis());
		playerRepo.save(mostRecent);
		return mostRecent;
	}


	private ServerReply formSplashResult(Player master){
		// otherwise update response with amt of bitcoin received
		// update master's state
		ServerReply start_reply = new ServerReply();

		if(master.getStatus() == Status.DEAD){
			logger.warn("warning, dead player still querying start");
			start_reply.setNewStatus(Status.DEAD);
		} else {
			master.setStatus(Player.Status.SPLASH_SCREEN);

			start_reply.setNewStatus(Player.Status.SPLASH_SCREEN);
		}

		start_reply.setBtcPaymentAddr(master.getPlayerPayment()
				.getBtcPaymentAddr());
		start_reply.setTotReceived(master.getPlayerPayment()
				.getTotReceived());
		start_reply.setTotRequested(master.getPlayerPayment()
				.getTotRequested());
		start_reply.setType(Type.BITCOIN);
		start_reply.setMpdAuthorized(false);
		start_reply.setVideoKey(master.getVideoKey());
		start_reply.setMetaData(null);

		return start_reply;
	}
	private boolean validateStartRequest(Player wantsToPlay) {
		// check valid player
		// etc
		return true;
	}

}
