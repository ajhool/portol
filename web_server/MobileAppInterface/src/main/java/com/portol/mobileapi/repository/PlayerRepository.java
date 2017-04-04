package com.portol.mobileapi.repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.mongojack.DBCursor;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.portol.common.model.PortolPlatform;
import com.portol.common.model.player.Player;
import com.portol.mobileapi.manager.MongoManaged;

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
			//TODO/a
			throw new Exception("wrong number of players found...");
		}

		return players.toArray().get(0);

	}

	public Player findOneByAddress(String btcAddressofTargetPlayer) throws Exception {
		BasicDBObject query = new BasicDBObject("playerPayment.btcPaymentAddr", btcAddressofTargetPlayer);
		DBCursor<Player> players = playerMongo.find(query);

		if(players.count() != 1){
			throw new Exception("wrong number of players found... btc addresses should be unique");
		}
		return players.toArray().get(0);
	}

	public Player findOneByQRContents(String QRContents) throws Exception {

		BasicDBObject query = new BasicDBObject("playerPayment.bitPayinvoice.paymentUrls.bip72", QRContents);
		DBCursor<Player> players = playerMongo.find(query);

		if(players.count() != 1){
			throw new Exception("wrong number of players found... QR contents should be unique");
		}
		return players.toArray().get(0);

	}

	public Player findOneByPartialPlayerID(String playerIdentifier) throws Exception {
		BasicDBObject q = new BasicDBObject();
		q.put("_id",  java.util.regex.Pattern.compile("^" + playerIdentifier + ".*$"));
		DBCursor<Player> players = playerMongo.find(q);


		if(players.size() > 1){
			throw new Exception("duplicate players found containing ID: " + playerIdentifier);
		}

		if(players.size() < 1)
			return null;

		else return players.toArray().get(0);
	}

	public ArrayList<Player> findPlayersAfterDate(String wantsPlayersId,
			Date oldest) {
		BasicDBObject q = new BasicDBObject("userId", wantsPlayersId);
		BasicDBObject q1 = new BasicDBObject("$gt", oldest.getTime());
		q.append("lastRequest", q1);
		q.append("platformID", new BasicDBObject("$exists", false));


		BasicDBObject sort = new BasicDBObject("lastRequest", -1);

		DBCursor<Player> results = this.playerMongo.find(q).sort(sort);

		//more than 20 devices isn't really practical, so limit sort to newest 20
		results.limit(20);

		ArrayList<Player> processedResults = new ArrayList<Player>();

		processedResults.addAll(results.toArray(20));

		return processedResults;
	}

	public ArrayList<Player> findDevicesAfterDate(String wantsPlayersId,
			Date oldest) {

		BasicDBObject q = new BasicDBObject("userId", wantsPlayersId);
		BasicDBObject q1 = new BasicDBObject("$gt", oldest.getTime());
		q.append("lastRequest", q1);
		q.append("platformID", new BasicDBObject("$exists", true));


		BasicDBObject sort = new BasicDBObject("lastRequest", -1);

		DBCursor<Player> results = this.playerMongo.find(q).sort(sort);

		//more than 20 devices isn't really practical, so limit sort to newest 20
		results.limit(20);

		ArrayList<Player> processedResults = new ArrayList<Player>();

		processedResults.addAll(results.toArray(20));

		return processedResults;
	}

	public void updatePlayersOnPlatform(PortolPlatform adopted) {

		//find all players on this platform 
		BasicDBObject query = new BasicDBObject("hostPlatform._id", adopted.getPlatformId());

		DBCursor<Player> results = this.playerMongo.find(query);

		for(Player result : results){
			result.setHostPlatform(adopted);
			playerMongo.save(result);
		}

	}

	public List<Player> findActivePlayersOnPlatform(String platformId) {
		BasicDBObject query = new BasicDBObject("hostPlatform._id", platformId);
		BasicDBObject stateQuery = new BasicDBObject("$ne", Player.Status.DEAD);
		query.append("status", stateQuery);
		
		
		DBCursor<Player> results = this.playerMongo.find(query);
		
		return results.toArray();
	}








}
