package com.portol.mobileapi.resource;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;
import com.portol.common.model.PortolPlatform;
import com.portol.common.model.player.Player;
import com.portol.common.model.user.User;
import com.portol.mobileapi.repository.PlatformRepository;
import com.portol.mobileapi.repository.PlayerRepository;
import com.portol.mobileapi.repository.UserRepository;
import com.portol.mobileapi.service.IconService;

@Path("/v0/user")
public class PlatformResource {

	private UserRepository userRepo;
	private PlatformRepository platformRepo;
	private PlayerRepository playerRepo;

	@Context 
	HttpServletResponse resp; 

	public PlatformResource(UserRepository userrepo, PlatformRepository platformRepo, PlayerRepository playerRepo) {
		userRepo = userrepo;
		this.platformRepo = platformRepo;
		this.playerRepo = playerRepo;
	}



	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/info/me")
	@Timed
	public User whoAmI(@QueryParam("platform") String platform, @Context HttpServletRequest req) throws Exception{
		User result = null;
		
		//prefer declared platform over cookies
		if(platform == null){
			//try cookies
			logger.info("fielded query for user information using cookies");
			result = userRepo.findUserWithCookie(req.getCookies());
		} else {

			logger.info("fielded query for user information on platform: " + platform);
			//check that user doesn't exist with that email
			result = userRepo.findUserLoggedOnPlatform(platform);
		}
		
		if(result == null){
			//then user already exists
			resp.sendError(402, "no user found on the specified platform");
			return null;

		}

		result.setHashedPass("");
		return result;


	}


	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/info/platform")
	@Timed
	public User whoAmI(Player withPlat) throws Exception{
		if(withPlat.getHostPlatform() == null || withPlat.getHostPlatform().getPlatformId() == null) {
			resp.sendError(404, "no platform id specified");
			return null;
		}

		logger.info("fielded query for user information on platform: " + withPlat.getHostPlatform().getPlatformId());
		//check that user doesn't exist with that email
		User result = userRepo.findUserLoggedOnPlatform(withPlat.getHostPlatform().getPlatformId());

		if(result == null){
			//then user already exists
			resp.sendError(402, "no user found on the specified platform");
			return null;

		}

		result.setHashedPass("");
		return result;


	}

	public static final Logger logger = LoggerFactory.getLogger(PlatformResource.class);


	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/adopt/{platform}")
	@Timed
	public User adoptMe(@PathParam("platform") String platform, String userId) throws Exception{

		logger.info("adopting platform: " + platform + " by user: " + userId);
		//check that user doesn't exist with that email
		User result = userRepo.findOneById(userId);

		if(result == null){
			//then user already exists
			resp.sendError(404, "no user found on the specified platform");
			return null;

		}

		result = addPlatformToUser(platform, result);
		userRepo.save(result);

		result.setHashedPass("");
		return result;


	}

	public User addPlatformToUser(String platformId, User buying) throws Exception {

		if(buying.findPlatformById(platformId) != null){
			return buying;
		}
		PortolPlatform toAdopt = platformRepo.findByPlatformId(platformId);
		if(toAdopt == null){
			//ok, so it isnt orphaned, somebody else probably owns it. 

			User hasPlayer = userRepo.findUserLoggedOnPlatform(platformId);

			if(hasPlayer == null){
				//it doesn't exist anywhere
				throw new Exception("error adding player to user");
			}

			//otherwise, forcibly log the old user off 
			toAdopt = hasPlayer.orphanPlayer(platformId);

			//save user that lost player
			userRepo.save(hasPlayer);



		} 

		PortolPlatform adopted = buying.adopt(toAdopt);
		platformRepo.remove(toAdopt);
		playerRepo.updatePlayersOnPlatform(adopted);
		return buying;

	}



}
