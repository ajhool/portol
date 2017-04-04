package com.portol.cloudplayer.resource;

import java.io.IOException;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portol.cloudplayer.repository.LocalPlayerRepository;
import com.portol.cloudplayer.service.ReturnToQService;
import com.portol.cloudplayer.socket.PlayerWSEndpoint;
import com.portol.common.model.LoginAlert;
import com.portol.common.model.SeekStatus;
import com.portol.common.model.player.Player;
import com.portol.common.model.player.ServerReply;
import com.portol.common.model.player.Player.Status;
import com.portol.common.model.user.User;

public class PlayerSocketAPI {

	private static Logger logger = LoggerFactory.getLogger(PlayerSocketAPI.class);
	private LocalPlayerRepository localPlayers;
	private ReturnToQService returnsvc;
	private PlayerWSEndpoint socketClient;
	private ObjectMapper mapper;
	private final ObjectMapper objMapper;

	public PlayerSocketAPI(ObjectMapper objectMapper, LocalPlayerRepository localPlayers, ReturnToQService returnsvc,
			PlayerWSEndpoint wsEnd) {
		super();
		this.localPlayers = localPlayers;
		this.returnsvc = returnsvc;
		this.objMapper = objectMapper;

		socketClient = wsEnd;

		mapper = new ObjectMapper();
	}

	public boolean sendPause(Player toChange, Player actionToTake) {
		try {
			String JSONReply = mapper.writeValueAsString(actionToTake);

			socketClient.writeTo(toChange.playerId, JSONReply);

		} catch (IOException e) {
			throw new RuntimeException("Error when writing the event.", e);
		}
		return true;
	}

	public boolean sendStop(Player stop) {
		try {
			String JSONReply = mapper.writeValueAsString(stop);

			socketClient.writeTo(stop.playerId, JSONReply);

		} catch (IOException e) {
			throw new RuntimeException("Error when writing the event.", e);
		}
		return true;

	}

	public boolean sendCurrent(Player state) {
		try {
			String JSONReply = mapper.writeValueAsString(state);

			socketClient.writeTo(state.playerId, JSONReply);

		} catch (IOException e) {
			throw new RuntimeException("Error when writing the event.", e);
		}
		return true;

	}

	public boolean sendResume(Player resumeContent) {
		try {
			String JSONReply = mapper.writeValueAsString(resumeContent);

			socketClient.writeTo(resumeContent.playerId, JSONReply);

		} catch (IOException e) {
			throw new RuntimeException("Error when writing the event.", e);
		}
		return true;

	}

	public boolean sendTimeout(Player timedOut) {
		timedOut.setStatus(Status.PREVIEW_TIMEOUT);
		try {
			String JSONReply = mapper.writeValueAsString(timedOut);

			socketClient.writeTo(timedOut.playerId, JSONReply);

		} catch (IOException e) {
			throw new RuntimeException("Error when writing the event.", e);
		}
		return true;

	}

	public boolean sendCompletePayment(Player added) {
		added.setStatus(Status.STREAMING);
		try {
			String JSONReply = mapper.writeValueAsString(added);

			socketClient.writeTo(added.playerId, JSONReply);

		} catch (IOException e) {
			throw new RuntimeException("Error when writing the event.", e);
		}
		return true;

	}

	public boolean alertLogin(Player hasLogin) {
		LoginAlert alert = new LoginAlert();
		alert.playerId = hasLogin.getPlayerId();

		try {
			String JSONReply = mapper.writeValueAsString(alert);

			socketClient.writeTo(hasLogin.playerId, JSONReply);

		} catch (IOException e) {
			throw new RuntimeException("Error when writing the event.", e);
		}
		return true;

	}

	public boolean sendSeek(String playerToSeekId, SeekStatus desiredSeek) {

		try {
			String JSONReply = mapper.writeValueAsString(desiredSeek);

			socketClient.writeTo(playerToSeekId, JSONReply);

		} catch (IOException e) {
			throw new RuntimeException("Error when writing the event.", e);
		}
		return true;

	}

	public boolean closeConnection(Player timedOut) {
		return socketClient.disconnect(timedOut.playerId);

	}

	public boolean closeUnauthorizedConnection(Player unauthorized) {
		return socketClient.disconnect(unauthorized.playerId, 401);

	}

}
