package com.portol.loadbalancer.resource;

import java.util.Date;

import io.dropwizard.auth.Auth;

import javax.servlet.http.Cookie;
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

import com.codahale.metrics.annotation.Timed;
import com.portol.common.model.PortolPlatform;
import com.portol.common.model.content.Content;
import com.portol.common.model.instance.EdgeInstance;
import com.portol.common.model.payment.Payment.Type;
import com.portol.common.model.player.Player;
import com.portol.common.model.player.ServerReply;
import com.portol.common.model.user.User;
import com.portol.loadbalancer.manager.MongoManaged;
import com.portol.loadbalancer.repo.EdgeCloudRepository;
import com.portol.loadbalancer.repo.ContentRepository;
import com.portol.loadbalancer.repo.PlayerRepository;
import com.portol.loadbalancer.repo.SplashRepository;
import com.portol.loadbalancer.repo.UserRepository;
import com.portol.loadbalancer.service.CloudCommunicator;
import com.portol.loadbalancer.service.QRMakerClient;
import com.portol.loadbalancer.service.EdgeReadyQueue;
import com.portol.loadbalancer.service.MobileDeviceServerClient;


@Path("/api/v0/preview")
public class PreviewResource {

	private final Logger logger = LoggerFactory.getLogger(PreviewResource.class);

	@Context HttpServletResponse response;
	private EdgeCloudRepository cloudRepo;
	private ContentRepository contentRepo;
	private SplashRepository splashRepo;
	private PlayerRepository playerRepo;
	private UserRepository userRepo; 

	private EdgeReadyQueue readyQ; 
	private CloudCommunicator comm;
	private QRMakerClient qrsvc; 
	private final int delayms; 
	private MobileDeviceServerClient mComm;
	//private MPDService mpdSvc; 


	public PreviewResource(ContentRepository contentrepo,
			SplashRepository splashrepo, PlayerRepository playerrepo,
			EdgeCloudRepository cloudrepo, CloudCommunicator comm, EdgeReadyQueue readyQ, QRMakerClient qrMaker, int delayMs, MobileDeviceServerClient client, UserRepository userRepo) {

		this.comm = comm;
		this.readyQ = readyQ;
		this.contentRepo = contentrepo;
		this.splashRepo = splashrepo;
		this.playerRepo = playerrepo;
		this.cloudRepo = cloudrepo;
		this.qrsvc = qrMaker;
		this.delayms = delayMs;
		this.mComm = client;
		this.userRepo = userRepo;
	}

	private Cookie getPlatCookie(Cookie[] cookies) {
		for(Cookie cook : cookies){
			if(cook.getName().equalsIgnoreCase("platID")){
				return cook;
			}
		}
		return null;
	}
	
	@POST
	@Timed
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public ServerReply getPreview(Player wantsPreview, @Context HttpServletRequest request) throws Exception{

		ServerReply preview_reply = new ServerReply();
		//sanity checks - verify player ID exists in system
		String remoteip = InitResource.getRemoteIp(request);

		wantsPreview.setPlayerIP(remoteip);

		boolean valid = validatePreviewRequest(wantsPreview);

		if(!valid){
			return null;
		}

		User matching = null;
		PortolPlatform matchingPlatform = null;
		boolean usingCookies = false;
		//2 options to find a matching user: either cookies or via the host platform field
		if (request.getCookies() != null) {

			matching = userRepo.findUserOnPlatform(request.getCookies());

			if(matching != null){

				Cookie targetPlat = this.getPlatCookie(request.getCookies());
				matchingPlatform = matching.findPlatformById(targetPlat.getValue());
				matchingPlatform.setLastUsed(System.currentTimeMillis());
				userRepo.save(matching);

				matching.setHashedPass(null);
				matching.setLastSeen(null);
				matching.setHistory(null);
			} else {
				//matching == null;
			}


			usingCookies = true;

		} else {
			//use host platform info to search 

			//check platform ID 
			if(wantsPreview.getHostPlatform() != null ){

				try {
					matching = userRepo.findUserOnPlatform(wantsPreview.getHostPlatform().getPlatformId());

				} catch (Exception e){
					logger.debug("no match found, exception thrown", e);
					matching = null;
				}
				if(matching != null){

					matchingPlatform = matching.findPlatformById(wantsPreview.getHostPlatform().getPlatformId());
					matchingPlatform.setLastUsed(System.currentTimeMillis());
					userRepo.save(matching);

					matching.setHashedPass(null);
					matching.setLastSeen(null);
					matching.setHistory(null);





				} else {
					//matching == null 
				}

			} else {
				//no host platform info supplied
				matching = null;
			}

		}

		//ignore the transmitted player now, since we know it wants a preview
		Player master = playerRepo.findOneById(wantsPreview.playerId);

		//update master's state
		master.setNumPlayersUsed(master.getNumPlayersUsed() + 1);
		master.setLastRequest(System.currentTimeMillis());
		master.setStatus(Player.Status.PREVIEW_STREAMING);

		//get video preview from video key in POST body
		Content hasPreview = contentRepo.findByVideoKey(master.getVideoKey());

		//Content preview = contentRepo.findById(hasPreview.getPreviewId());

		//provision preview player/find existing
		EdgeInstance readyCloudPlayer = cloudRepo.findEdgePreviewing(hasPreview);


		if(readyCloudPlayer == null){
			//then we need to provision a new cloud
			readyCloudPlayer = readyQ.getNextEdge();
			if(readyCloudPlayer == null){
				response.sendError(503, "No servers available right now. Please try again later");
				preview_reply.setNewStatus(Player.Status.SPLASH_SCREEN);
				preview_reply.setBtcPaymentAddr(master.getPlayerPayment().getBtcPaymentAddr());
				preview_reply.setTotReceived(master.getPlayerPayment().getTotReceived());
				preview_reply.setTotRequested(master.getPlayerPayment().getTotRequested());
				preview_reply.setDedicatedCloudHost(readyCloudPlayer.getLocation());
				preview_reply.setType(Type.BITCOIN);
				preview_reply.setHostPlatform(master.getHostPlatform());
				preview_reply.setVideoKey(master.getVideoKey());
				preview_reply.setPlayerId(master.playerId);
				return preview_reply; 
			}
			comm.initEdgePreviewCloud(readyCloudPlayer, hasPreview);
			readyCloudPlayer.setStatus(EdgeInstance.Status.PREVIEW);
			readyCloudPlayer.setServing(hasPreview);
			//stop and breathe
			Thread.sleep(delayms);
		} 

		comm.addnewClient(readyCloudPlayer, master);

		//at this point, the cloud should be downloading the preview, or at least expecting a new client

		readyCloudPlayer.addActivePlayer(master);

		//load MPD into serverReply body
		preview_reply.setNewStatus(Player.Status.PREVIEW_STREAMING);
		preview_reply.setBtcPaymentAddr(master.getPlayerPayment().getBtcPaymentAddr());
		preview_reply.setTotReceived(master.getPlayerPayment().getTotReceived());
		preview_reply.setTotRequested(master.getPlayerPayment().getTotRequested());
		preview_reply.setDedicatedCloudHost(readyCloudPlayer.getLocation());
		preview_reply.setType(Type.BITCOIN);
		preview_reply.setHostPlatform(master.getHostPlatform());
		preview_reply.setVideoKey(master.getVideoKey());
		preview_reply.setPlayerId(master.playerId);
		master.setCurrentSourceIP(readyCloudPlayer.getLocation());
		master.setCurrentCloudPlayerId(readyCloudPlayer.getId());
		master.addPlayerUsed(readyCloudPlayer.getId());
		playerRepo.save(master);

		if(matching != null){
			//alert app that player state has changed
			mComm.sendPreviewPlayer(master, matching);
		}
		cloudRepo.save(readyCloudPlayer);
		

		return preview_reply;
	}


	boolean validatePreviewRequest(Player wantsPreview){
		return true;
	}



}
