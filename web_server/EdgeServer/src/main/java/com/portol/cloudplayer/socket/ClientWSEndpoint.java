package com.portol.cloudplayer.socket;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portol.common.model.SeekStatus;
import com.portol.common.model.app.AppCommand;

@ClientEndpoint
@ServerEndpoint(value = "/client/ws")
public class ClientWSEndpoint {

	private static final Logger logger = LoggerFactory.getLogger(ClientWSEndpoint.class);

	private static ConcurrentHashMap<String, Session> clientSessions = new ConcurrentHashMap<String, Session>();

	public interface ClientEventInterface {
		public void onClientEvent(String targetPlayerId, AppCommand event);

		public void onClientDisconnect(String playerId);
	}

	private static ClientEventInterface callback;

	private static ObjectMapper mapper = new ObjectMapper();

	public ClientWSEndpoint() {
	};

	public void setListener(ClientEventInterface listener) {
		this.callback = listener;
	}

	@OnOpen
	public void onWebSocketConnect(EndpointConfig conf, Session sess) {
		logger.info("Socket Connected: " + sess);
		String query = sess.getQueryString();
		int start = query.indexOf("id=") + 3;
		int end = query.indexOf("&");
		String playerId;
		if (end > 0) {
			playerId = query.substring(start, end);
		} else {
			playerId = query.substring(start);
		}
		logger.info("number of keys: " + clientSessions.keySet().size());
		clientSessions.put(playerId, sess);
		logger.info("new key added, number of keys: " + clientSessions.keySet().size());
	}

	@OnMessage
	public void onWebSocketText(String message, Session session) {
		String query = session.getQueryString();
		int start = query.indexOf("id=") + 3;
		int end = query.indexOf("&");
		String playerId;
		if (end > 0) {
			playerId = query.substring(start, end);
		} else {
			playerId = query.substring(start);
		}
		logger.info("Received TEXT message: " + message + " from player with ID: " + playerId);

		// for ping ponging to keep socket open
		if (message.toLowerCase().contains("ping")) {
			try {
				session.getBasicRemote().sendText("pong");

			} catch (IOException e) {
				logger.error("error ponging ping back", e);

			}
			return;
		}

		// try to pull app command out of message
		AppCommand cmd = null;
		try {
			cmd = mapper.readValue(message, AppCommand.class);

		} catch (Exception e) {
			logger.error("error parsing appcommand out of ws message", e);
			session.getAsyncRemote().sendText("ERROR - INVALID COMMAND");
		}

		if (cmd != null && callback != null) {
			callback.onClientEvent(playerId, cmd);
		}
	}

	@OnClose
	public void onWebSocketClose(Session session, CloseReason reason) {
		logger.info("Socket Closed: " + reason);
		String query = session.getQueryString();
		int start = query.indexOf("id=") + 3;
		int end = query.indexOf("&");
		String playerId;
		if (end > 0) {
			playerId = query.substring(start, end);
		} else {
			playerId = query.substring(start);
		}

		if (callback != null) {
			callback.onClientDisconnect(playerId);
		}
	}

	@OnError
	public void onWebSocketError(Session session, Throwable cause) {
		logger.error("web socket error", cause);
		String query = session.getQueryString();
		int start = query.indexOf("id=") + 3;
		int end = query.indexOf("&");
		String playerId;
		if (end > 0) {
			playerId = query.substring(start, end);
		} else {
			playerId = query.substring(start);
		}

		if (callback != null) {
			callback.onClientDisconnect(playerId);
		}

	}

	public boolean writeTo(String playerId, String text) {
		logger.info("number of keys: " + clientSessions.keySet().size());
		Session ofInterest = clientSessions.get(playerId);
		if (ofInterest == null) {
			return false;
		}

		logger.info("sending message to endpoint: " + text + " for player with ID: " + playerId);

		try {
			ofInterest.getBasicRemote().sendText(text);
		} catch (IOException e) {
			logger.error("error sending message to client", e);
			return false;
		}

		return true;
	}
}