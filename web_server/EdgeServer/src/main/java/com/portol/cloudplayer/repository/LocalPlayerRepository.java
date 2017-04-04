package com.portol.cloudplayer.repository;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentNavigableMap;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.portol.common.model.player.Player;




public class LocalPlayerRepository {
	
	final static Logger logger = LoggerFactory.getLogger(LocalPlayerRepository.class);

	private DB currentPlayers;
	private HTreeMap<String,Player> playerMap;

	private boolean openDB(){
		currentPlayers = DBMaker.newFileDB(new File("players.db")).closeOnJvmShutdown().encryptionEnable("REACTED").make();
		playerMap = currentPlayers.getHashMap("collectionName");
		logger.debug("Database open: " + !currentPlayers.isClosed());
		return true;
	}

	public LocalPlayerRepository(){
		this.openDB();
		
	}

	//returns ID of saved player
	public String save(Player incoming){
		
		if(playerMap == null){
			this.openDB();
		}
		playerMap.put(incoming.playerId, incoming);
		logger.debug("saving player with id: " + incoming.playerId + " : " + incoming);
		return incoming.playerId;
		
	}

	public Player findOneById(String id){
		
		if(playerMap == null){
			this.openDB();
		}
		
		Player nextPlayer = playerMap.get(id);
		logger.debug("player with id: " + id + " found: " + nextPlayer);
		return nextPlayer;
	}

	public Player delete(Player toDelete) {
		
		if(playerMap == null){
			this.openDB();
		}
		
		Player removed = playerMap.remove(toDelete.playerId);
		return removed;
	}

	public Player delete(String playerId) {
		if(playerMap == null){
			this.openDB();
		}
		
		Player removed = playerMap.remove(playerId);
		return removed;
	}
	
	
	
}
