package com.portol.mobileapi.resource;

import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Random;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.Arrays;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portol.common.model.LoginRequest;
import com.portol.common.model.PortolPlatform;
import com.portol.common.model.PortolToken;
import com.portol.common.model.user.User;
import com.portol.mobileapi.manager.MongoManaged;
import com.portol.mobileapi.repository.PlatformRepository;
import com.portol.mobileapi.repository.PlayerRepository;
import com.portol.mobileapi.repository.UserRepository;

@Path("/v0/user")
public class UserLogoutResource {

	private UserRepository userRepo;
	public static final Logger logger = LoggerFactory.getLogger(UserLogoutResource.class);

	public static final int token_valid_minutes = 30;

	@Context
	private HttpServletResponse resp;

	private PlatformRepository platformRepo;
	private PlayerRepository playerRepo; 

	public UserLogoutResource(UserRepository userrepo, PlatformRepository platformRepo, PlayerRepository playerRepo) {
		userRepo = userrepo;
		this.platformRepo = platformRepo;
		this.playerRepo = playerRepo;
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/logout")
	@Timed
	public User logoutSimple(User tologout, @Context HttpServletRequest req ) throws Exception{


		//Step 1: infer logout platform from cookie
		Cookie[] reqCookies = req.getCookies();
		String platformToRemove = reqCookies[0].getValue();

		User matching = userRepo.findUserLoggedOnPlatform(platformToRemove);




		if(matching == null){
			resp.sendError(404, "no user found with platform information");
			return null;
		}

		//Step 2: remove platform for user
		PortolPlatform orphaned = matching.orphanPlayer(platformToRemove);

		//Step 3: platform back to orphan pool 

		platformRepo.save(orphaned);
		userRepo.save(matching);


		//Step 4: nullify token values
		matching.setCurrentToken(null);
		matching.setHashedPass(null);
		//result.getHistory().clear();
		matching.setLastSeen(null);
		return matching;

	}

	//returns session token
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/logout/{platformId}")
	@Timed
	public User logoutExplicit( User loggingOut, @PathParam("platformId") String platformId, @Context HttpServletRequest req ) throws Exception{

		String platformToRemove = platformId;

		User matching = userRepo.findUserLoggedOnPlatform(platformToRemove);

		if(matching == null){
			resp.sendError(404, "no user found with platform information");
			return null;
		}

		//Step 2: remove platform for user
		PortolPlatform orphaned = matching.orphanPlayer(platformToRemove);

		//Step 3: platform back to orphan pool 

		platformRepo.save(orphaned);
		userRepo.save(matching);


		//Step 4: nullify token values
		matching.setCurrentToken(null);
		matching.setHashedPass(null);
		//result.getHistory().clear();
		matching.setLastSeen(null);



		return matching;

	}


}
