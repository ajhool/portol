package com.portol.paymentserver.repository;

import org.mongojack.DBCursor;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

import com.mongodb.BasicDBObject;
import com.portol.common.model.player.Player;
import com.portol.paymentserver.manager.MongoManaged;

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
			//TODO
			throw new Exception("wrong number of players found. Expected 1 player, instead found: " + players.count() + "The player id causing the error is: " + id);
		}
		
		return players.toArray().get(0);

	}
	
	public Player findOneByAddress(String btcAddressofTargetPlayer) throws Exception {
		BasicDBObject query = new BasicDBObject("playerPayment.btcPaymentAddr", btcAddressofTargetPlayer);
		DBCursor<Player> players = playerMongo.find(query);

		if(players.count() != 1){
			//TODO
			throw new Exception("wrong number of players found... btc addresses should be unique");
		}
		return players.toArray().get(0);
	}
	
	
	
	
	
	

}
