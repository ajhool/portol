package com.portol.mobileapi.resource;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ArrayMap;
import com.portol.common.model.LoginRequest;
import com.portol.common.model.PortolPlatform;
import com.portol.common.model.PortolToken;
import com.portol.common.model.user.Account;
import com.portol.common.model.user.User;
import com.portol.common.model.user.Account.AccountType;
import com.portol.common.model.user.UserIcon;
import com.portol.mobileapi.ImageDownloaderBase64;
import com.portol.mobileapi.manager.MongoManaged;
import com.portol.mobileapi.repository.PlatformRepository;
import com.portol.mobileapi.repository.PlayerRepository;
import com.portol.mobileapi.repository.UserRepository;
import com.portol.mobileapi.service.LoadbalCommunicator;

import facebook4j.Facebook;
import facebook4j.FacebookFactory;
import facebook4j.Reading;
import facebook4j.ResponseList;
import facebook4j.auth.AccessToken;
import facebook4j.conf.Configuration;
import facebook4j.conf.ConfigurationBuilder;

@Path("/v0/user")
public class UserLoginResource {

	private UserRepository userRepo;
	public static final Logger logger = LoggerFactory.getLogger(UserLoginResource.class);

	public static final int token_valid_minutes = 30;

	@Context
	private HttpServletResponse resp;

	private PlatformRepository platformRepo;
	private LoadbalCommunicator lbComm;
	private PlayerRepository playerRepo;
	private NewUserResource newUserRes; 

	public UserLoginResource(UserRepository userrepo, PlatformRepository platformRepo, PlayerRepository playerRepo, LoadbalCommunicator lbComm, NewUserResource newUserRes) {
		userRepo = userrepo;
		this.platformRepo = platformRepo;
		this.playerRepo = playerRepo;
		this.lbComm = lbComm;
		this.newUserRes = newUserRes; 
		try {
			TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		} catch (Exception e) {
			logger.error("Error opening transport for google oauth", e);

		}

	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/update")
	@Timed
	public User updateUser(User newState, @Context HttpServletRequest req ) throws Exception {

		User matching = userRepo.findOneByToken(newState.getCurrentToken());

		if(matching == null){
			return null;
		}

		PortolToken toke = new PortolToken();
		toke.setExpiration(new Date(System.currentTimeMillis() + (token_valid_minutes * 60 * 1000)));

		//make the token value
		Random tokenMaker = new Random();
		byte[] bytesOfMessage = new byte[128];
		tokenMaker.nextBytes(bytesOfMessage);

		MessageDigest md = MessageDigest.getInstance("MD5");
		String token = Hex.encodeHexString(md.digest(bytesOfMessage));
		toke.setValue(token);

		matching.setCurrentToken(toke);

		//copy over settings that may have changed
		matching.setSettings(newState.getSettings());

		userRepo.save(matching);
		return matching;

	}


	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/refresh")
	@Timed
	public User refresh(User toRefresh, @Context HttpServletRequest req ) throws Exception {

		User matching = userRepo.findOneById(toRefresh.getUserId());

		if(matching == null){
			return null;
		}

		PortolToken toke = new PortolToken();
		toke.setExpiration(new Date(System.currentTimeMillis() + (token_valid_minutes * 60 * 1000)));

		//lets make the token value
		Random tokenMaker = new Random();
		byte[] bytesOfMessage = new byte[128];
		tokenMaker.nextBytes(bytesOfMessage);

		MessageDigest md = MessageDigest.getInstance("MD5");
		String token = Hex.encodeHexString(md.digest(bytesOfMessage));
		toke.setValue(token);

		matching.setCurrentToken(toke);

		userRepo.save(matching);
		return matching;

	}

	private static final String CLIENT_ID = "REDACTED";
	/**
	 * Default JSON factory to use to deserialize JSON.
	 */
	private final JacksonFactory JSON_FACTORY = new JacksonFactory();

	/**
	 * Default HTTP transport to use to make HTTP requests.
	 */
	private HttpTransport TRANSPORT;

	public static final String FB_APP_ID = "REDACTED";
	public static final String FB_APP_SECRET = "REDACTED";

	private User registerNewUserGoogle(Payload payload) throws Exception {
		String email = payload.getEmail();
		String userName = email.substring(0, email.indexOf('@'));

		Map<String, Object> vals = payload.getUnknownKeys();
		String name = null;
		String firstName = null;
		String lastName = null;
		String locale = null;
		if(vals != null){
			name = vals.get("name").toString();
			firstName = vals.get("given_name").toString();
			lastName = vals.get("family_name").toString();
			locale = vals.get("locale").toString();
		}

		User added = new User();
		added.setEmail(email);
		added.setLocale(locale);
		added.setFirstName(firstName);
		added.setLastName(lastName);
		added.setName(name);
		added.setUserName(userName);
		added.setHashedPass("null");

		//create google account
		Account google = new Account();
		google.setType(AccountType.GOOGLE);
		google.setUserKey(payload.getSubject());

		ArrayList<Account> acctHolder = new ArrayList<Account>();

		acctHolder.add(google);
		added.setUserAccounts(acctHolder);

		User fullyRegistered = newUserRes.registerNewUser(added); 

		return fullyRegistered; 
	}

	private User registerNewUserFacebook(facebook4j.User user) throws Exception {
		User vueUser = new User();

		String email = user.getEmail();
		vueUser.setEmail(email);
		vueUser.setUserName(email.substring(0, email.indexOf('@')));


		vueUser.setName(user.getName());
		vueUser.setFirstName(user.getFirstName());
		vueUser.setLastName(user.getLastName());
		vueUser.setLocale(user.getLocale().toString());

		//Account object info
		Account facebook = new Account();
		facebook.setVerified(user.isVerified());
		facebook.setType(AccountType.FACEBOOK);
		facebook.setUserKey(user.getId());
		vueUser.setHashedPass("null");
		ArrayList<Account> acctHolder = new ArrayList<Account>();

		acctHolder.add(facebook);
		vueUser.setUserAccounts(acctHolder);
		
		User fullyRegistered = null;
		
		if(user.getPicture() != null && user.getPicture().getURL() != null){
			
			//we have a fb picture to use
			ImageDownloaderBase64 imgDL = new ImageDownloaderBase64(user.getPicture().getURL().toString());
			Thread dl = new Thread(imgDL);
			dl.start();
			dl.join();
			UserIcon ic = new UserIcon();
			ic.setDescription("facebook profile");
			ic.setType("PNG");
			ic.setRawData(imgDL.getEncodedImage());
			vueUser.setUserImg(ic);
			
		}
		
		fullyRegistered = newUserRes.registerNewUser(vueUser); 

		return fullyRegistered; 
	}


	//returns session token
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/loginorregister")
	@Timed
	public User loginOrRegister( /*User loggingIn*/ LoginRequest loggingInReq, @Context HttpServletRequest req ) throws Exception{
		User result = null;
		switch(loggingInReq.getType()){

		case GOOGLE:
			GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(TRANSPORT, JSON_FACTORY).setAudience(Arrays.asList(CLIENT_ID)).build();

			// (Receive idTokenString by HTTPS POST)
			GoogleIdToken idToken = verifier.verify(loggingInReq.getoAuthToken());

			if (idToken != null) {
				Payload payload = idToken.getPayload();

				//check for existence of token in db 
				result = userRepo.findOneByAccountToken(payload.getSubject());

				//if user found, continue with process
				if(result == null){
					//otherwise, register the user
					result = this.registerNewUserGoogle(payload);
				}
			} else {
				logger.error("Invalid ID token.");

			}

			break;
		case FACEBOOK:

			ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
			configurationBuilder.setDebugEnabled(true);
			configurationBuilder.setOAuthAppId(FB_APP_ID);
			configurationBuilder.setOAuthAppSecret(FB_APP_SECRET);
			configurationBuilder.setAppSecretProofEnabled(false);
			configurationBuilder.setOAuthAccessToken(loggingInReq.getoAuthToken());
			configurationBuilder
			.setOAuthPermissions("email, id, name, first_name, last_name, gender, picture, verified, locale, generic");
			configurationBuilder.setUseSSL(true);
			configurationBuilder.setJSONStoreEnabled(true);

			// Create configuration and get Facebook instance
			Configuration configuration = configurationBuilder.build();
			FacebookFactory ff = new FacebookFactory(configuration);
			Facebook facebook = ff.getInstance();
			String name = facebook.getName();
			facebook4j.User me = facebook.getMe();

			//check for existence of token in db 
			result = userRepo.findOneByAccountToken(me.getId());

			//if user found, continue with process
			if(result == null){
				//otherwise, register the user
				facebook4j.User user = facebook.getUser(me.getId(), new Reading().fields("email", "last_name", "gender", "first_name", "picture", "name", "locale", "verified"));

				if(user.getEmail() == null){
					resp.sendError(503, "unable to register account, email required");
					return null;
				}

				result = this.registerNewUserFacebook(user);
			}

			logger.info("logged in with facebook successfully");
			break;
		default:

			break;

		}

		if(result == null){
			resp.sendError(401, "Invalid user login information supplied");
			return null;
		}

		//otherwise, we can assume that user logged in properly

		result.setLastSeen(new Date(System.currentTimeMillis()));

		PortolToken toke = new PortolToken();
		toke.setExpiration(new Date(System.currentTimeMillis() + (token_valid_minutes * 60 * 1000)));

		//lets make the token value
		Random tokenMaker = new Random();
		byte[] bytesOfMessage = new byte[128];
		tokenMaker.nextBytes(bytesOfMessage);

		MessageDigest md = MessageDigest.getInstance("MD5");
		String token = Hex.encodeHexString(md.digest(bytesOfMessage));
		toke.setValue(token);

		result.setCurrentToken(toke);

		//make cookie
		tokenMaker.nextBytes(bytesOfMessage);


		PortolPlatform matchingPlatform = null;

		User matching = result;
		boolean usingCookies = false;
		Cookie platCookie = null;
		if (req.getCookies() != null) {

			platCookie = this.getPlatCookie(req.getCookies());

			if(platCookie != null){
				if(matching != null){
					matching.setHashedPass(null);
					matching.setLastSeen(null);
					matching.setHistory(null);

					matchingPlatform = matching.findPlatformById(platCookie.getValue());
				}


				usingCookies = true;
			}

		} 

		if(!usingCookies){
			//use host platform info to search 

			//check platform ID 
			if(loggingInReq.getLoginPlatform() != null ){
				if(matching != null){
					matching.setHashedPass(null);
					matching.setLastSeen(null);
					matching.setHistory(null);

					matchingPlatform = matching.findPlatformById(loggingInReq.getLoginPlatform().getPlatformId());
				}

			} 

		}

		//if matchingPlatform == null here, we should try and adopt an orphan
		String platformId = "";
		if(matchingPlatform  == null){

			PortolPlatform orphan = null;
			try {
				platformId = usingCookies ? platCookie.getValue() : loggingInReq.getLoginPlatform().getPlatformId();
				orphan = platformRepo.findByPlatformId(platformId);
			} catch (Exception e){
				logger.debug("Excpetion while searching for orphan: ", e);
			}




			if(orphan != null){

				//we adopt this orphan
				matchingPlatform = orphan; 
				//remove it from the pool
				platformRepo.remove(orphan);

				//have this user adopt it
				PortolPlatform adopted = result.adopt(orphan);


				playerRepo.updatePlayersOnPlatform(adopted);
				userRepo.save(result);



			} else {
				//orphan == null
				//must be a totally new platform
			}

		}





		if(matchingPlatform == null){

			if(platformId == null || platformId.length() < 1){
				platformId = Hex.encodeHexString(md.digest(bytesOfMessage));
			}

			Cookie cookie = new Cookie("platID", platformId);
			cookie.setMaxAge(Integer.MAX_VALUE); 
			cookie.setDomain(".portol.me");
			cookie.setPath("/");

			resp.addCookie(cookie);

			ObjectMapper mapper = new ObjectMapper();

			String name = "unspecified";
			String type = "unspecified";

			if(loggingInReq.getLoginPlatform()!= null && loggingInReq.getLoginPlatform().getPlatformName() != null){
				name = loggingInReq.getLoginPlatform().getPlatformName(); 
			}

			if(loggingInReq.getLoginPlatform()!= null && loggingInReq.getLoginPlatform().getPlatformType() != null){
				type = loggingInReq.getLoginPlatform().getPlatformType(); 
			}

			matchingPlatform = new PortolPlatform(name, type, platformId, result.getNextColor());
			//			}

			matchingPlatform.setLastUsed(System.currentTimeMillis());
			result.getPlatforms().add(matchingPlatform);


		}
		userRepo.save(result);
		//broadcast login event
		final User snapShot = result;
		Thread bCast = new Thread(){
			@Override
			public void run(){
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				lbComm.alertLogin(snapShot);
			}
		};

		bCast.start();

		result.setHashedPass(null);
		result.setLastSeen(null);




		return result;

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
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/login")
	@Timed
	public User login( /*User loggingIn*/ LoginRequest loggingInReq, @Context HttpServletRequest req ) throws Exception{

		User loggingIn = loggingInReq.getLoggingIn();

		User result = userRepo.findExistingUser(loggingIn);

		if(result == null){
			resp.sendError(401, "Invalid user login information supplied");
			return null;
		}

		//we can assume that user logged in properly

		result.setLastSeen(new Date(System.currentTimeMillis()));

		PortolToken toke = new PortolToken();
		toke.setExpiration(new Date(System.currentTimeMillis() + (token_valid_minutes * 60 * 1000)));

		Random tokenMaker = new Random();
		byte[] bytesOfMessage = new byte[128];
		tokenMaker.nextBytes(bytesOfMessage);

		MessageDigest md = MessageDigest.getInstance("MD5");
		String token = Hex.encodeHexString(md.digest(bytesOfMessage));
		toke.setValue(token);

		result.setCurrentToken(toke);
		//make cookie
		tokenMaker.nextBytes(bytesOfMessage);


		PortolPlatform matchingPlatform = null;

		User matching = result;
		boolean usingCookies = false;
		if (req.getCookies() != null) {

			if(req.getCookies().length > 1){
				resp.sendError(400, "invalid number of cookies found: " + req.getCookies().length);
				return null;
			}

			if(matching != null){
				matching.setHashedPass(null);
				matching.setLastSeen(null);
				matching.setHistory(null);

				matchingPlatform = matching.findPlatformById(req.getCookies()[0].getValue());
			} else {
				//matching == null;
			}


			usingCookies = true;

		} else {
			//use host platform info to search 

			//check platform ID 
			if(loggingInReq.getLoginPlatform() != null ){

				if(matching != null){
					matching.setHashedPass(null);
					matching.setLastSeen(null);
					matching.setHistory(null);


					matchingPlatform = matching.findPlatformById(loggingInReq.getLoginPlatform().getPlatformId());
				} else {
					//matching == null 
				}

			} else {
			}

		}

		//if matchingPlatform == null here, we should try and adopt an orphan
		String platformId = "";
		if(matchingPlatform  == null){

			PortolPlatform orphan = null;
			try {
				platformId = usingCookies ? req.getCookies()[0].getValue() : loggingInReq.getLoginPlatform().getPlatformId();
				orphan = platformRepo.findByPlatformId(platformId);
			} catch (Exception e){
				logger.debug("Excpetion while searching for orphan: ", e);
			}




			if(orphan != null){

				//we adopt this orphan
				matchingPlatform = orphan; 
				//remove it from the pool
				platformRepo.remove(orphan);

				//have this user adopt it
				PortolPlatform adopted = result.adopt(orphan);


				playerRepo.updatePlayersOnPlatform(adopted);
				userRepo.save(result);



			} else {
				//orphan == null
				//must be a totally new platform
			}

		}



		if(platformId == null || platformId.length() < 1){
			platformId = Hex.encodeHexString(md.digest(bytesOfMessage));
		}
		
		if(matchingPlatform == null){


			Cookie cookie = new Cookie("platID", platformId);
			cookie.setMaxAge(Integer.MAX_VALUE); 
			cookie.setDomain(".portol.me");
			cookie.setPath("/");
			resp.addCookie(cookie);
			ObjectMapper mapper = new ObjectMapper();


			String name = "unspecified";
			String type = "unspecified";

			if(loggingInReq.getLoginPlatform()!= null && loggingInReq.getLoginPlatform().getPlatformName() != null){
				name = loggingInReq.getLoginPlatform().getPlatformName(); 
			}

			if(loggingInReq.getLoginPlatform()!= null && loggingInReq.getLoginPlatform().getPlatformType() != null){
				type = loggingInReq.getLoginPlatform().getPlatformType(); 
			}

			matchingPlatform = new PortolPlatform(name, type, platformId, result.getNextColor());
			//			}

			matchingPlatform.setLastUsed(System.currentTimeMillis());
			result.getPlatforms().add(matchingPlatform);
			userRepo.save(result);

		}

		//broadcast login event
		final User snapShot = result;
		Thread bCast = new Thread(){
			@Override
			public void run(){
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				lbComm.alertLogin(snapShot);
			}
		};

		bCast.start();
		result.setHashedPass(null);
		result.setLastSeen(null);
		
		return result;

	}



}
