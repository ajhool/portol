package com.portol.mobileapi.resource;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.ArrayUtils;
import org.mongojack.WriteResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;
import com.portol.common.model.PortolPlatform;
import com.portol.common.model.user.Account;
import com.portol.common.model.user.User;
import com.portol.common.model.user.UserFunds;
import com.portol.common.model.user.UserIcon;
import com.portol.common.utils.EscapeChars;
import com.portol.common.utils.PasswordUtils;
import com.portol.mobileapi.manager.MongoManaged;
import com.portol.mobileapi.repository.UserRepository;
import com.portol.mobileapi.service.IconService;

@Path("/v0/user/newuser")
public class NewUserResource {

	private UserRepository userRepo;
	private IconService icSvc;

	@Context 
	HttpServletResponse resp; 

	public NewUserResource(UserRepository userrepo, IconService icSvc) {
		userRepo = userrepo;
		this.icSvc = icSvc;
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed
	public User registerNewUser(User newUser) throws Exception{

		//check that user doesn't exist with that email
		User result = userRepo.findOneByEmail(newUser);

		if(result != null){
			//then user already exists
			resp.sendError(502, "user exists for this email address.");
			return null;

		}

		//we expect user password to be hashed client side for security 
		boolean valid = this.validateNewUser(newUser);

		if(!valid){
			throw new javax.ws.rs.ProcessingException("User failed validation");
		}

		if(newUser.getUserImg() == null || newUser.getUserImg().getRawData() == null){
			UserIcon ic = icSvc.getNewIcon(newUser.getUserName());
			newUser.setUserImg(ic);
		}
		//save the user and return...
		Date now = new Date(System.currentTimeMillis());
		newUser.setLastSeen(now);
		newUser.setSignUpDate(now);

		//give new users free money automatically
		UserFunds init = new UserFunds();

		Account userAcct = newUser.getUserAccounts().get(0);

		if(userAcct.isVerified()){
			init.setUserCredits(750);
		}
		newUser.setFunds(init);

		String salted = PasswordUtils.getSaltedHash(newUser.getHashedPass());
		newUser.setHashedPass(salted);
		User saved = userRepo.save(newUser).getSavedObject();

		//scrub potentially sensitive info
		saved.setHashedPass(null);
		saved.setLastSeen(null);

		return saved;


	}


	public static final Logger logger = LoggerFactory.getLogger(NewUserResource.class);

	private boolean validateNewUser(User newUser) {

		if(newUser.getEmail().length() < 2){
			return false;
		}

		if(newUser.getUserName().length() < 2){
			return false;
		}

		if(newUser.getHashedPass().length() < 2){
			return false;
		}

		boolean hasInvalid = EscapeChars.hasSpecial(newUser.getUserName());

		if(hasInvalid){
			logger.info("invalid character in username");
			return false;
		}

		return true;
	}



}
