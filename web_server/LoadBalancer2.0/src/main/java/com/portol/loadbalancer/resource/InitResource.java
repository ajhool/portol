package com.portol.loadbalancer.resource;

import java.io.BufferedWriter;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.commons.codec.binary.Hex;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;
import com.portol.common.model.PortolPlatform;
import com.portol.common.model.content.Content;
import com.portol.common.model.content.ContentMetadata;
import com.portol.common.model.content.HistoryItem;
import com.portol.common.model.payment.Payment;
import com.portol.common.model.payment.Payment.Type;
import com.portol.common.model.player.Player;
import com.portol.common.model.player.ServerReply;
import com.portol.common.model.user.User;
import com.portol.loadbalancer.QRCodeGetter;
import com.portol.loadbalancer.manager.MongoManaged;
import com.portol.loadbalancer.repo.ContentRepository;
import com.portol.loadbalancer.repo.PlatformRepository;
import com.portol.loadbalancer.repo.PlayerRepository;
import com.portol.loadbalancer.repo.SplashRepository;
import com.portol.loadbalancer.repo.UserRepository;
import com.portol.loadbalancer.service.AddressService;
import com.portol.loadbalancer.service.MobileDeviceServerClient;
import com.portol.loadbalancer.service.QRMakerClient;

@Path("/api/v0/init")
public class InitResource {

	final static Logger logger = LoggerFactory.getLogger(InitResource.class);

	private ContentRepository contentRepo;

	private SplashRepository splashRepo;

	private PlayerRepository playerRepo;

	private PlatformRepository platformRepo;

	@Context
	HttpServletResponse resp;

	private AddressService addrSrc;

	private QRMakerClient qrSvc;
	private MobileDeviceServerClient sClient;
	private UserRepository userRepo;

	public InitResource(ContentRepository contentRepo, AddressService addrSvc, SplashRepository splashRepo,
			PlayerRepository plrRepo, QRMakerClient qrMaker, UserRepository userRepo, MobileDeviceServerClient sClient,
			PlatformRepository platformRepo) {

		logger.info("in constructor for initresource");

		this.addrSrc = addrSvc;
		this.platformRepo = platformRepo;
		this.contentRepo = contentRepo;
		this.splashRepo = splashRepo;
		this.playerRepo = plrRepo;
		this.qrSvc = qrMaker;
		this.userRepo = userRepo;
		this.sClient = sClient;
		logger.info("finished constructor for initresource");
	}

	private Cookie getPlatCookie(Cookie[] cookies) {
		for (Cookie cook : cookies) {
			if (cook.getName().equalsIgnoreCase("platID")) {
				return cook;
			}
		}
		return null;
	}

	// Handle exceptions nicely
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed
	public ServerReply newPlayer(Player incoming, @Context HttpServletRequest request) throws Exception {

		ServerReply reply = new ServerReply();

		logger.info("player initialized new connection");

		Content result = null;

		String remoteip = getRemoteIp(request);

		incoming.setPlayerIP(remoteip);

		String userAgent = getUserAgent(request);

		validateInitRequest(incoming);

		try {
			result = contentRepo.findByVideoKey(incoming.getVideoKey());
		} catch (Exception e) {
			return null;
		}

		if (result == null) {
			resp.sendError(404, "no content found for content key: " + incoming.getVideoKey());
			logger.warn("bad init call, looking for key: " + incoming.getVideoKey());
			return null;
		}

		PortolPlatform matchingPlatform = null;
		// if we make it here, we can assume a valid request from the user

		// maybe do some suspicious activity checking here

		// initialize player object for this connection

		incoming.setStatus(Player.Status.SPLASH_SCREEN);

		// Generate id manually
		incoming.setId(UUID.randomUUID().toString());

		incoming.setInitialConnect(System.currentTimeMillis());
		// get an address to use from the address svc

		ContentMetadata meta = splashRepo.getSplashScreenById(result.getSplashDataId());
		Payment newAddr = addrSrc.getNewAddressFor(meta);
		User matching = null;

		QRCodeGetter qrRunnable = new QRCodeGetter(newAddr.getBitPayinvoice().getPaymentUrls().getBIP72(), qrSvc);
		Thread qrMaker = new Thread(qrRunnable);
		qrMaker.start();

		boolean usingCookies = false;
		// 2 options to find a matching user: either cookies or via the host
		// platform field
		if (request.getCookies() != null) {

			matching = userRepo.findUserOnPlatform(request.getCookies());

			if (matching != null) {

				Cookie targetPlat = this.getPlatCookie(request.getCookies());
				matchingPlatform = matching.findPlatformById(targetPlat.getValue());
				matchingPlatform.setLastUsed(System.currentTimeMillis());
				userRepo.save(matching);

				matching.setHashedPass(null);
				matching.setLastSeen(null);
				matching.setHistory(null);
			} else {
				// matching == null;
			}

			usingCookies = true;

		} else {
			// use host platform info to search

			// check platform ID
			if (incoming.getHostPlatform() != null) {

				try {
					matching = userRepo.findUserOnPlatform(incoming.getHostPlatform().getPlatformId());

				} catch (Exception e) {
					logger.debug("no match found, exception thrown", e);
					matching = null;
				}
				if (matching != null) {

					matchingPlatform = matching.findPlatformById(incoming.getHostPlatform().getPlatformId());
					matchingPlatform.setLastUsed(System.currentTimeMillis());
					userRepo.save(matching);

					matching.setHashedPass(null);
					matching.setLastSeen(null);
					matching.setHistory(null);

				} else {
					// matching == null
				}

			} else {
				// no host platform info supplied
				matching = null;
			}

		}

		// if matching == null here, we know we have an orphaned player
		PortolPlatform orphan = null;
		if (matching == null) {
			try {
				String platformId = usingCookies ? request.getCookies()[0].getValue()
						: incoming.getHostPlatform().getPlatformId();
				orphan = platformRepo.findByPlatformId(platformId);
			} catch (Exception e) {
				// doesnt existt
				logger.debug("exception thrown, unique id probably doesnt exist...", e);
				orphan = null;
			}

			if (orphan == null) {
				
				Random tokenMaker = new Random();
				byte[] bytesOfMessage = new byte[128];
				tokenMaker.nextBytes(bytesOfMessage);

				MessageDigest md = MessageDigest.getInstance("MD5");
				String newPlatformId = Hex.encodeHexString(md.digest(bytesOfMessage));

				Cookie cookie = new Cookie("platID", newPlatformId);
				cookie.setMaxAge(Integer.MAX_VALUE);
				cookie.setDomain(".portol.me");
				cookie.setPath("/");
				// cookie.setSecure(true);
				resp.addCookie(cookie);

				String name = getUserAgent(request);
				String type = "unspecified";

				// try and extract as much info as possible from supplied
				// platform
				if (incoming.getHostPlatform() != null) {

					String suppliedName = incoming.getHostPlatform().getPlatformName();
					if (suppliedName != null && suppliedName.length() > 3) {
						name = suppliedName;
					}

					String suppliedType = incoming.getHostPlatform().getPlatformType();
					if (suppliedType != null && suppliedType.length() > 1) {
						type = suppliedType;
					}
				}

				matchingPlatform = new PortolPlatform(name, type, newPlatformId, "orphaned");
				matchingPlatform.setLastUsed(System.currentTimeMillis());
				// save to repo
				platformRepo.save(matchingPlatform);

			} else {
				matchingPlatform = orphan;
				matchingPlatform.setLastUsed(System.currentTimeMillis());
				platformRepo.save(matchingPlatform);
			}

		}

		incoming.setHostPlatform(matchingPlatform);

		reply.setLoggedIn(matching);

		reply.setBtcPaymentAddr(newAddr.getBtcPaymentAddr());
		reply.setTotReceived(newAddr.getTotReceived());
		reply.setTotRequested(newAddr.getTotRequested());
		reply.setType(Type.BITCOIN);

		qrMaker.join();
		String urlOfQRImage = qrRunnable.getURLOfQR();
		incoming.setQrURL(urlOfQRImage);
		reply.setQrURL(urlOfQRImage);
		reply.setMetaData(meta);
		reply.setMpdAuthorized(false);
		reply.setVideoKey(incoming.getVideoKey());
		reply.setNewStatus(Player.Status.SPLASH_SCREEN);

		reply.setPlayerId(incoming.playerId);

		// prep player for internal storage
		incoming.setUserAgent(getUserAgent(request));
		incoming.setLastRequest(System.currentTimeMillis());
		incoming.setPlayerPayment(newAddr);
		incoming.setProfile(result.getType());
		incoming.setBtcAddress(newAddr.getBtcPaymentAddr());

		incoming.setLoggedIn(null);

		reply.setHostPlatform(matchingPlatform);
		final Player toSave = incoming;
		Thread saveIt = new Thread() {

			@Override
			public void run() {
				playerRepo.save(toSave);
			}
		};

		saveIt.start();

		return reply;
	}

	public static String getRemoteIp(HttpServletRequest request) {
		return request.getRemoteAddr();

	}

	boolean validateInitRequest(Player firstContact) throws Exception {
		firstContact.playerId = null;

		if (firstContact.playerId != null) {
			logger.error("player called init API call with non-zero id");
			throw new Exception("bad incoming id, should be cleared, but is: " + firstContact.playerId);
		}

		firstContact.playerId = UUID.randomUUID().toString();
		return true;
	}

	public String getUserAgent(HttpServletRequest request) {
		return request.getHeader("User-Agent");

	}

}
