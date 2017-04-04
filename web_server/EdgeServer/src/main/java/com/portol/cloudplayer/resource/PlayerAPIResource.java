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
import com.portol.common.model.PlayerEvent;
import com.portol.common.model.SeekStatus;
import com.portol.common.model.app.AppCommand;
import com.portol.common.model.content.Content;
import com.portol.common.model.content.Content.Type;
import com.portol.common.model.instance.EdgeInstance;
import com.portol.common.model.player.Player;
import com.portol.common.model.player.ServerReply;
import com.portol.common.model.player.Player.Status;

@Path("/api/v0/player")
public class PlayerAPIResource {
	//use hashmap to auth for this api
	private LocalPlayerRepository playerRepo;
	private SetContentResource setContent;
	private ClientSocketAPI clientSender;
	private EdgeInstance _this;

	public PlayerAPIResource(LocalPlayerRepository playerRepo,
			SetContentResource setContent, ClientSocketAPI clientRes, EdgeInstance _this) {
		super();
		this.playerRepo = playerRepo;
		this.clientSender = clientRes;
		this._this = _this;
		this.setContent = setContent;
		
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed
	public void recvPlayerEvent(PlayerEvent cmd) throws Exception{

		boolean valid = this.validateCommand(cmd);

		if(!valid){
			return;
		}

		switch(cmd.getEvent()){
		case SEEK_UPDATE:
			playerSeek(cmd.getPlayerId(), cmd.getsStatus());
			break;
		default:
			return;

		}


	}

	private boolean validateCommand(PlayerEvent cmd) {
		return true;
	}


	private void playerSeek(String playerId, SeekStatus toSend) throws Exception {

		clientSender.sendSeekInfo(playerId, toSend);
	

		return;
	}


}
