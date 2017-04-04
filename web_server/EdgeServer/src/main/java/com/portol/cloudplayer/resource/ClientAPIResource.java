package com.portol.cloudplayer.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;
import com.portol.cloudplayer.repository.ContentRepository;
import com.portol.cloudplayer.repository.LocalPlayerRepository;
import com.portol.cloudplayer.service.MPDService;
import com.portol.common.model.SeekStatus;
import com.portol.common.model.app.AppCommand;
import com.portol.common.model.content.Content;
import com.portol.common.model.content.Content.Type;
import com.portol.common.model.instance.EdgeInstance;
import com.portol.common.model.player.Player;
import com.portol.common.model.player.ServerReply;
import com.portol.common.model.player.Player.Status;

@Path("/api/v0/client")
public class ClientAPIResource {
	// use hashmap to auth for this api
	private LocalPlayerRepository playerRepo;
	private ContentRepository contentRepo;
	private SetContentResource setContent;
	private PlayerSocketAPI wsSender;
	private MPDService MPDprinter;
	private EdgeInstance _this;

	public ClientAPIResource(LocalPlayerRepository playerRepo, ContentRepository contentRepo,
			SetContentResource setContent, PlayerSocketAPI wsRes, MPDService MPDPrinter, EdgeInstance _this) {
		super();
		this.playerRepo = playerRepo;
		this.MPDprinter = MPDPrinter;
		this.wsSender = wsRes;
		this._this = _this;
		this.setContent = setContent;
		this.contentRepo = contentRepo;
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed
	public Player recvAppCommand(AppCommand cmd) throws Exception {

		boolean valid = this.validateCommand(cmd);

		if (!valid) {
			return null;
		}

		switch (cmd.getCmd()) {
		case CHANGE_CONTENT:
			return appSetContent(cmd.getNewContentKey(), cmd.getTargetPlayerId());
		case PAUSE:
			return appPause(cmd.getTargetPlayerId());
		case PLAY:
			return appResume(cmd.getTargetPlayerId());
		case CHANGE_SOURCE:
		case SEEK:
			return appSeek(cmd.getNewStatus(), cmd.getTargetPlayerId());
		case STOP:
			return appStop(cmd.getTargetPlayerId());
		case SUBTITLE:
		case VOL_DOWN:
		case VOL_UP:
		case GET_INDEX_INFO:
		case STATUS_CHECK:
			return createStatusUpdate(cmd.getTargetPlayerId());
		default:
			return null;

		}

	}

	private Player appSeek(SeekStatus newStatus, String gettargetPlayerId) {

		wsSender.sendSeek(gettargetPlayerId, newStatus);

		return playerRepo.findOneById(gettargetPlayerId);
	}

	private Player createStatusUpdate(String playerId) {
		Player target = playerRepo.findOneById(playerId);
		return target;
	}

	private boolean validateCommand(AppCommand cmd) {
		return true;
	}

	// set content
	private Player appSetContent(String newContentKey, String playerId) throws Exception {
		Content toPlay = contentRepo.findByVideoKey(newContentKey);

		if (toPlay == null) {
			return null;
		}

		Player target = playerRepo.findOneById(playerId);
		target.setVideoKey(newContentKey);

		// actually make the content change
		if (toPlay.getType() == Type.VOD) {
			this.setContent.setContentImpl(toPlay, null);
		} else
			throw new Exception("not yet implemented");

		playerRepo.save(target);

		return target;
	}

	// pause
	private Player appPause(String playerId) throws Exception {

		Player target = playerRepo.findOneById(playerId);
		target.setStatus(Status.PAUSED);

		wsSender.sendPause(target, target);
		playerRepo.save(target);

		return target;
	}

	// play/resume
	private Player appResume(String playerId) throws Exception {

		Player target = playerRepo.findOneById(playerId);
		target.setStatus(Status.STREAMING);

		ServerReply resumeContent = new ServerReply();

		resumeContent.setNewStatus(Status.STREAMING);
		resumeContent.setPlayerId(playerId);

		wsSender.sendResume(target);
		playerRepo.save(target);

		return target;
	}

	// play/resume
	private Player appStop(String playerId) throws Exception {

		Player target = playerRepo.findOneById(playerId);
		target.setStatus(Status.STOPPED);

		ServerReply resumeContent = new ServerReply();

		resumeContent.setNewStatus(Status.STREAMING);
		resumeContent.setPlayerId(playerId);

		wsSender.sendResume(target);
		playerRepo.save(target);

		return target;
	}

}
