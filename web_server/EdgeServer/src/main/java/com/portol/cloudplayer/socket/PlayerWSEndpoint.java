package com.portol.cloudplayer.socket;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portol.cloudplayer.socket.ClientWSEndpoint.ClientEventInterface;
import com.portol.common.model.PlayerEvent;
import com.portol.common.model.WSEvent;

@ClientEndpoint
@ServerEndpoint( 
		value = "/ws"
		) 
public class PlayerWSEndpoint {

	public interface PlayerEventInterface {
			public void onPlayerEvent(PlayerEvent event, String playerId);
			public void onPlayerDisconnect(String playerId);
			public void onPlayerConnect(String playerId);
	}

	private static ObjectMapper mapper = new ObjectMapper();
	private static final Logger logger = LoggerFactory.getLogger(PlayerWSEndpoint.class);

	private static ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<String, Session>();
	
	private static PlayerEventInterface callback;
	
	public PlayerWSEndpoint(){};
	
	public void setListener(PlayerEventInterface listener){
		this.callback = listener; 
	}
	
	@OnOpen
	public void onWebSocketConnect(EndpointConfig conf, Session sess)
	{
		logger.info("Socket Connected: " + sess);
		String query = sess.getQueryString();
		int start = query.indexOf("id=") + 3;
		int end = query.indexOf("&");
		String playerId = query.substring(start, end);
		logger.info("number of keys: " + sessions.keySet().size());
		Session existing = sessions.put(playerId, sess);
		logger.info("new key added, number of keys: " + sessions.keySet().size());
		if(callback != null){
			callback.onPlayerConnect(playerId);
		}
		
		if(existing != null){
			logger.warn("player opened two Websockets, closing existing socket...");
			try {
				existing.getBasicRemote().sendText("new websocket opened by player - closing this outdated one");
				existing.close(new CloseReason(CloseCodes.RESERVED, "Closing outdated socket"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@OnMessage
	public void onWebSocketText(String message, Session session)
	{
		String query = session.getQueryString();
		int start = query.indexOf("id=") + 3;
		int end = query.indexOf("&");
		String playerId = query.substring(start, end);
		logger.info("Received TEXT message: " + message + " from player with ID: " + playerId);
		
		//for ping ponging to keep socket open
		if(message.toLowerCase().contains("VSnotify")){
			
			try {
				WSEvent event = mapper.readValue(message, WSEvent.class);
			} catch (Exception e1) {
				logger.error("error parsing ping message", e1);
			} 
			
			WSEvent pong = new WSEvent(WSEvent.EventType.PONG);
			try {
				session.getBasicRemote().sendText(mapper.writeValueAsString(pong));
				return;
			} catch (IOException e) {
				logger.error("error ponging ping back", e);
			}
		}
		
		//try to pull app command out of message
				PlayerEvent cmd = null;
				try {
					cmd = mapper.readValue(message, PlayerEvent.class);
					
				} catch (Exception e){
					logger.error("error parsing PlayerEvent out of ws message",e );
					session.getAsyncRemote().sendText("ERROR - INVALID REQUEST");
				}
				  
				if(cmd != null && callback != null){
					callback.onPlayerEvent(cmd, playerId);
				}
	}

	@OnClose
	public void onWebSocketClose(Session session, CloseReason reason)
	{
		logger.info("Socket Closed: " + reason);
		String query = session.getQueryString();
		int start = query.indexOf("id=") + 3;
		int end = query.indexOf("&");
		String playerId = query.substring(start, end);
		if(callback != null){
			callback.onPlayerDisconnect(playerId);
		}
	}

	@OnError
	public void onWebSocketError(Session session, Throwable cause)
	{
		logger.error("web socket error", cause);
		String query = session.getQueryString();
		int start = query.indexOf("id=") + 3;
		int end = query.indexOf("&");
		String playerId = query.substring(start, end);
		if(callback != null){
			callback.onPlayerDisconnect(playerId);
		}
	}
	
	public boolean writeTo(String playerId, String text){
		logger.info("number of keys: " + sessions.keySet().size());
		Session ofInterest = sessions.get(playerId);
		if(ofInterest == null){
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

	public boolean disconnect(String playerId) {
		Session toDie = sessions.remove(playerId);
		try {
			toDie.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
		
	}

	public boolean disconnect(String playerId, int disconnectCode) {
		
		Session unAuth = sessions.remove(playerId);
		
		try {
			unAuth.close(new CloseReason(CloseCodes.CLOSED_ABNORMALLY, "Closure code: " + disconnectCode));
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
		
	}
}