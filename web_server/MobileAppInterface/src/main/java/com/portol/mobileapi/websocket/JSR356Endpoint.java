package com.portol.mobileapi.websocket;

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

@ClientEndpoint
@ServerEndpoint( 
		value = "/ws"
		) 
public class JSR356Endpoint {

	private static final Logger logger = LoggerFactory.getLogger(JSR356Endpoint.class);

	private static ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<String, Session>();

	public interface WsSimpleInterface{
		public void wsOpen(String idOfPlayer);
		public void wsClose(String idOfPlayer);
	}

	private static WsSimpleInterface mCallback;



	public void setListener(WsSimpleInterface listener){
		mCallback = listener;
	}

	@OnOpen
	public void onWebSocketConnect(EndpointConfig conf, Session sess)
	{
		//TODO check against BTC for safety
		logger.info("Socket Connected: " + sess);
		String query = sess.getQueryString();
		if(query == null){

			try {
				sess.getBasicRemote().sendText("unauthorized");
				sess.close();

			} catch (IOException e) {
				logger.error("error closing unauthorized websocket" , e);
			}
			return;
		}
		int start = query.indexOf("id=") + 3;
		int end = query.indexOf("&");

		if(end < start){
			end = query.length();
		}
	
		String playerId = query.substring(start, end);
		logger.info("number of keys: " + sessions.keySet().size());
		Session existing = sessions.put(playerId, sess);

		if(existing != null){

			try {
				existing.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(mCallback != null){

			mCallback.wsOpen(playerId);
		}
		logger.info("new key added, number of keys: " + sessions.keySet().size());
	}

	@OnMessage
	public void onWebSocketText(String message, Session session)
	{
		String query = session.getQueryString();
		int start = query.indexOf("id=") + 3;
		int end = query.indexOf("&");
		if(end < start){
			end = query.length();
		}
		String playerId = query.substring(start, end);
		logger.info("Received TEXT message: " + message + " from player with ID: " + playerId);

		//for ping ponging to keep socket open
		if(message.toLowerCase().contains("ping")){
			try {
				session.getBasicRemote().sendText("pong");
			} catch (IOException e) {
				logger.error("error ponging ping back", e);
			}
		}
	}

	@OnClose
	public void onWebSocketClose(Session session, CloseReason reason)
	{
		String query = session.getQueryString();
		int start = query.indexOf("id=") + 3;
		int end = query.indexOf("&");
		if(end < start){
			end = query.length();
		}
		String playerId = query.substring(start, end);
		if(mCallback != null){

			mCallback.wsClose(playerId);
		}

		sessions.remove(playerId);

		logger.info("Socket Closed: " + reason);
	}

	@OnError
	public void onWebSocketError(Session session, Throwable cause)
	{
		String query = session.getQueryString();
		int start = query.indexOf("id=") + 3;
		int end = query.indexOf("&");
		if(end < start){
			end = query.length();
		}
		String playerId = query.substring(start, end);
		if(mCallback != null){

			mCallback.wsClose(playerId);
		}
		sessions.remove(playerId);
		logger.error("web socket error", cause);
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
}