package com.portol.loadbalancer.repo;

import java.util.ArrayList;
import java.util.List;

import org.mongojack.DBCursor;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

import com.mongodb.BasicDBObject;
import com.portol.common.model.PortolPlatform;
import com.portol.common.model.instance.Instance.Status;
import com.portol.common.model.player.Player;
import com.portol.loadbalancer.manager.MongoManaged;

public class PlayerRepository {

	//the DB we are using
	private JacksonDBCollection<Player, String> playerMongo;

	public PlayerRepository(MongoManaged playerMongoManaged){
		this.playerMongo = JacksonDBCollection.wrap(playerMongoManaged.getDB().getCollection("player"), Player.class, String.class);
	}

	public WriteResult<Player, String> save(Player incoming) {
		return playerMongo.save(incoming);
	}

	public Player findOneById(String id) throws Exception {
		BasicDBObject query = new BasicDBObject();
		query.put("_id", id);
		DBCursor<Player> players = playerMongo.find(query);

		if(players.count() != 1){
			throw new Exception("wrong number of players found. Expected 1 player, instead found: " + players.count() + "The player id causing the error is: " + id);
		}

		return players.toArray().get(0);

	}

	public List<Player> findActivePairedPlayers(PortolPlatform plat) {

		//need to find players that:
		//1. are in an acceptable state

		BasicDBObject idling = new BasicDBObject("status", Player.Status.PREVIEW_STREAMING);
		BasicDBObject running = new BasicDBObject("status", Player.Status.SPLASH_SCREEN);
		BasicDBObject preview = new BasicDBObject("status", Player.Status.STREAMING);
		BasicDBObject preview2 = new BasicDBObject("status", Player.Status.REPEAT_SCREEN);
		BasicDBObject preview4 = new BasicDBObject("status", Player.Status.PAUSED);
		BasicDBObject preview3 = new BasicDBObject("status", Player.Status.STOPPED);

		ArrayList<BasicDBObject> myList = new ArrayList<BasicDBObject>(); 
		myList.add(preview);
		myList.add(idling);
		myList.add(running);
		myList.add(preview4);
		myList.add(preview2);
		myList.add(preview3);

		BasicDBObject ors = new BasicDBObject("$or", myList);

		//2. are paired to a cloud
		BasicDBObject existenceCriteria = new BasicDBObject("$exists", true);
		BasicDBObject cloudQuery = new BasicDBObject("currentSourceIP", existenceCriteria);

		//3. are on the platform we are looking for
		BasicDBObject platformCriteria = new BasicDBObject("hostPlatform._id", plat.getPlatformId());

		ArrayList<BasicDBObject> criterias = new ArrayList<BasicDBObject>();

		//combine the three into 
		criterias.add(cloudQuery);
		criterias.add(ors);
		criterias.add(platformCriteria);

		BasicDBObject query = new BasicDBObject("$and", criterias);


		DBCursor<Player> players = playerMongo.find(query);

		List<Player> result = players.toArray();
		return result; 

		
	}







}
