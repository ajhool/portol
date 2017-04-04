package com.portol.cloudplayer.resource;

import java.io.IOException;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portol.cloudplayer.repository.LocalPlayerRepository;
import com.portol.cloudplayer.service.ReturnToQService;
import com.portol.cloudplayer.socket.ClientWSEndpoint;
import com.portol.cloudplayer.socket.PlayerWSEndpoint;
import com.portol.common.model.SeekStatus;
import com.portol.common.model.player.Player;
import com.portol.common.model.player.ServerReply;

public class ClientSocketAPI {

	private static Logger logger = LoggerFactory.getLogger(ClientSocketAPI.class);
	private LocalPlayerRepository localPlayers;
	private ClientWSEndpoint socketClient;
	private ObjectMapper mapper;
	private final ObjectMapper objMapper;

	public ClientSocketAPI(ObjectMapper objectMapper, LocalPlayerRepository localPlayers,
			 ClientWSEndpoint wsEnd) {
		super();
		this.localPlayers = localPlayers;
	
		this.objMapper = objectMapper;

		socketClient = wsEnd;

		mapper = new ObjectMapper();
	}

	public boolean  sendSeekInfo(String playerId, SeekStatus updated){
		try{
			String JSONReply = mapper.writeValueAsString(updated);

			socketClient.writeTo(playerId, JSONReply);

		} catch (IOException e) {
			throw new RuntimeException(
					"Error when writing the event.", e);
		} 
		return true;
	}

}
