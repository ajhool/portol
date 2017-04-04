package com.portol.mobileapi.resource;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.portol.common.model.PortolToken;
import com.portol.common.model.app.AppConnectResponse;
import com.portol.common.model.app.AppPaymentRequest;
import com.portol.common.model.content.Content;
import com.portol.common.model.content.ContentMetadata;
import com.portol.common.model.content.HistoryItem;
import com.portol.common.model.instance.EdgeInstance;
import com.portol.common.model.payment.Payment;
import com.portol.common.model.payment.Payment.Status;
import com.portol.common.model.payment.Payment.Type;
import com.portol.common.model.payment.PlayerBuyRequest;
import com.portol.common.model.payment.PlayerBuyResponse;
import com.portol.common.model.player.Player;
import com.portol.common.model.user.User;
import com.portol.mobileapi.manager.MongoManaged;
import com.portol.mobileapi.repository.ContentRepository;
import com.portol.mobileapi.repository.PlayerRepository;
import com.portol.mobileapi.repository.MetadataRepository;
import com.portol.mobileapi.repository.UserRepository;
import com.portol.mobileapi.service.LoadbalCommunicator;


@Path("/v0/buyvideo")
public class VideoPaymentResource {

	@Context HttpServletResponse response;
	private PlayerRepository playerRepo;

	private UserRepository userRepo;

	private final LoadbalCommunicator comm;
	private ContentRepository contentRepo;
	private MetadataRepository splashRepo;
	private PlatformResource platformResource;
	private AppAsyncResource appComm; 


	public VideoPaymentResource(AppAsyncResource appComm, UserRepository userrepo,
			PlayerRepository playerrepo, ContentRepository contentrepo, MetadataRepository splashrepo, LoadbalCommunicator comm, PlatformResource platRes) {
		playerRepo = playerrepo;
		userRepo = userrepo;
		contentRepo = contentrepo;
		this.splashRepo = splashrepo;
		this.comm = comm;
		this.appComm = appComm;
		this.platformResource = platRes;
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/embedded")
	@Timed
	public PlayerBuyResponse buyVideoFromEmbedded(Player playerBuy, @Context HttpServletRequest req) throws Exception{

		//collect all the info that we need

		final User buying = userRepo.findUserWithCookie(req.getCookies());

		if(buying == null){
			PlayerBuyResponse resp = new PlayerBuyResponse();
			resp.setPurchaseStatus("no matching user found");
			resp.setSuccessful(false);
			return resp;
		}

		//get content user is trying to pay
		final Player usersPlayer = playerRepo.findOneByAddress(playerBuy.getBtcAddress());

		//check that we havent already paid
		Payment existing = usersPlayer.getPlayerPayment();
		Content boughtContent = contentRepo.findByVideoKey(usersPlayer.getVideoKey());
		ContentMetadata usersContent = splashRepo.getMetadataForParent(boughtContent.getId());
		if(existing != null && existing.getStatus() == Payment.Status.COMPLETE){
			response.sendError(204, "content already purchased!");
		} else {

			//check that they can afford it
			if(buying.getFunds().getUserCredits() < usersContent.getPrices().getShardPrice()){
				PlayerBuyResponse resp = new PlayerBuyResponse();
				resp.setPurchaseStatus("not enough user account shards to purchase content");
				resp.setSuccessful(false);
				return resp;
			} else {

				//otherwise debit it
				buying.getFunds().setUserCredits((buying.getFunds().getUserCredits() - usersContent.getPrices().getShardPrice()));
			}

			//the user is paid in full at this point

			//update their player to reflect this fact
			Payment currentPayment = usersPlayer.getPlayerPayment();

			currentPayment.setType(Type.SHARDS);
			currentPayment.setStatus(Status.COMPLETE);

			usersPlayer.getPlayerPayment().setTotReceived(usersPlayer.getPlayerPayment().getTotRequested());

			HistoryItem thisUse = new HistoryItem();
			thisUse.setViewedContentId(boughtContent.getId());
			thisUse.setTimeViewed(System.currentTimeMillis());
			List<HistoryItem> history = buying.getHistory();

			if(history == null){
				buying.setHistory(new ArrayList<HistoryItem>());
			}
			buying.getHistory().add(thisUse); 
			usersPlayer.setPlayerPayment(currentPayment);
			usersPlayer.setStatus(Player.Status.STREAMING);
			Thread asyncRunner = new Thread(){
				@Override
				public void run(){
					userRepo.save(buying);


					//finally, ping the loadbal with the updated player so that the user's video starts loading immediately 
					String userCloud = comm.earlyLoad(usersPlayer);
					usersPlayer.setCurrentSourceIP(userCloud);
					playerRepo.save(usersPlayer);

					//send to app if necessary
					try {
						appComm.externalPurchase(usersPlayer, buying);
					} catch (JsonProcessingException e) {
						logger.error("error sending purchase to player", e);
					}
				}
			};

			asyncRunner.start();


		}
		PlayerBuyResponse resp = new PlayerBuyResponse();

		resp.setPurchased(usersContent);
		resp.setPurchaseStatus("Thank you for your purchase");
		resp.setSuccessful(true);




		return resp;

	}


	private static final Logger logger = LoggerFactory.getLogger(VideoPaymentResource.class);
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/app")
	@Timed
	public AppConnectResponse buyVideoFromApp(AppPaymentRequest request) throws Exception{

		User buying = userRepo.findOneByToken(new PortolToken(request.getValidToken()));

		if(buying == null){
			throw new BadRequestException("supplied token not found in users database");
		}

		//backup sanity check
		if(!buying.getUserId().equalsIgnoreCase(request.getUserID())){
			throw new BadRequestException("username of user requesting video does not match token user");
		}

		//otherwise, if we are here we know that the request is legit
		String identifier = request.getPlayerIdentifier();

		//get content user is trying to pay
		Player usersPlayer = null;
		if(identifier.length() > 10){
			if(identifier.contains("-")){
				//then we have a player UUID
				usersPlayer = playerRepo.findOneById(identifier);
			} else {
				//using bitcoin address
				usersPlayer = playerRepo.findOneByAddress(request.getPlayerIdentifier());
			}
		} else if(identifier.length() >= 5){
			//using pairing code
			usersPlayer = playerRepo.findOneByPartialPlayerID(request.getPlayerIdentifier().substring(0, 5));
		} else {
			//idk wtf they sent 
			return null;
		}

		Content boughtContent = contentRepo.findByVideoKey(usersPlayer.getVideoKey());
		if(request.getPurchasedContentID() != null ){
			if(request.getPurchasedContentID().length() > 1){
				boughtContent = contentRepo.findById(request.getPurchasedContentID());
				usersPlayer.setVideoKey(boughtContent.getContentKey());

			}
		}


		ContentMetadata usersContent = splashRepo.getMetadataForParent(boughtContent.getId());

		//check that they can afford it
		if(buying.getFunds().getUserCredits() < usersContent.getPrices().getShardPrice()){
			throw new BadRequestException("not enough user account shards to purchase content");
		} else {


			//otherwise debit it
			buying.getFunds().setUserCredits((buying.getFunds().getUserCredits() - usersContent.getPrices().getShardPrice()));
		}

		//the user is paid in full at this point

		//update their player to reflect this fact
		Payment currentPayment = usersPlayer.getPlayerPayment();

		currentPayment.setType(Type.SHARDS);
		currentPayment.setStatus(Status.COMPLETE);

		usersPlayer.getPlayerPayment().setTotReceived(usersPlayer.getPlayerPayment().getTotRequested());

		HistoryItem thisUse = new HistoryItem();
		thisUse.setViewedContentId(boughtContent.getId());
		thisUse.setTimeViewed(System.currentTimeMillis());
		List<HistoryItem> history = buying.getHistory();

		if(history == null){
			buying.setHistory(new ArrayList<HistoryItem>());
		}
		buying.getHistory().add(thisUse);
		usersPlayer.setPlayerPayment(currentPayment);

		usersPlayer.setStatus(Player.Status.STREAMING);

		playerRepo.save(usersPlayer);
		//finally, ping the loadbal with the updated player so that the user's video starts loading immediately 
		String userCloud = comm.earlyLoad(usersPlayer);

		//add platform to user
		User updated = platformResource.addPlatformToUser(usersPlayer.getHostPlatform().getPlatformId(), buying);

		userRepo.save(updated);
		AppConnectResponse resp = new AppConnectResponse();

		resp.setPurchasedContent(usersContent);
		resp.setSource(userCloud);
		resp.setPlayerID(usersPlayer.playerId);

		return resp;

	}

}
