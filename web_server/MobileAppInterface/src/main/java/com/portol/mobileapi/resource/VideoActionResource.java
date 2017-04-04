package com.portol.mobileapi.resource;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;
import com.portol.common.model.app.AppConnectResponse;
import com.portol.common.model.app.AppConnectionRequest;
import com.portol.common.model.content.Content;
import com.portol.common.model.content.ContentMetadata;
import com.portol.common.model.player.Player;
import com.portol.common.model.user.User;
import com.portol.mobileapi.repository.ContentRepository;
import com.portol.mobileapi.repository.PlayerRepository;
import com.portol.mobileapi.repository.MetadataRepository;
import com.portol.mobileapi.repository.UserRepository;
import com.portol.mobileapi.service.LoadbalCommunicator;


//seek/pause/play/ffwd/rewind
@Path("/v0/vidcontrol")
public class VideoActionResource {

	private UserRepository userRepo;
	private PlayerRepository playerRepo;
	private LoadbalCommunicator lbComm;
	private ContentRepository contentRepo; 
	private MetadataRepository splashRepo;

	public VideoActionResource(UserRepository userrepo,
			PlayerRepository playerrepo, LoadbalCommunicator comm, ContentRepository contentrepo, MetadataRepository splashrepo) {
		super();
		this.userRepo = userrepo;
		this.playerRepo = playerrepo;
		this.splashRepo = splashrepo;
		this.lbComm = comm;
		this.contentRepo = contentrepo;
	}

	@POST
	@Path("/platformuser")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed
	public User checkForUser(Player wantsUser){

		//TODO validation
		String platformToFind = wantsUser.getHostPlatform().getPlatformId();

		User loggedIn = null;
		try {
			loggedIn = userRepo.findUserLoggedOnPlatform(platformToFind);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return loggedIn;
	}

	@POST
	@Path("/playercloud")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed
	public AppConnectResponse checkForCloud(Player wantsCloud) throws Exception{

		Player master = playerRepo.findOneById(wantsCloud.playerId);
		if(master == null){
			return null;
		}

		Content boughtContent = contentRepo.findByVideoKey(master.getVideoKey());

		ContentMetadata usersContent = splashRepo.getMetadataForParent(boughtContent.getId());

		AppConnectResponse resp = new AppConnectResponse();

		resp.setPurchasedContent(usersContent);
		resp.setSource(master.getCurrentSourceIP());
		resp.setPlayerID(master.playerId);

		return resp;
	}


	@POST
	@Path("/existing")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed
	public AppConnectResponse makeConnection(AppConnectionRequest incoming, @Context HttpServletRequest request) throws Exception{
		boolean valid = this.validateReq(incoming);
		if(!valid){
			return null;
		}
		AppConnectResponse resp = new AppConnectResponse();

		String QRContents = incoming.getQRContents();

		Player matchingQR = playerRepo.findOneByQRContents(QRContents);

		String cloudPlayerIp = matchingQR.getPlayerIP();
		Content contentPlaying = contentRepo.findByVideoKey(matchingQR.getVideoKey());
		resp.setPurchasedContent(splashRepo.getSplashScreenById(contentPlaying.getSplashDataId()));
		resp.setSource(cloudPlayerIp);
		resp.setPlayerID(matchingQR.playerId);

		return resp;
	}

	private boolean validateReq(AppConnectionRequest incoming) {
		// TODO Auto-generated method stub
		return true;
	}


}
