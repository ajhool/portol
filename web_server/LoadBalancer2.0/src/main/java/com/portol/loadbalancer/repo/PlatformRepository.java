package com.portol.loadbalancer.repo;


import org.apache.commons.lang3.ArrayUtils;
import org.mongojack.DBCursor;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.portol.common.model.Cookie;
import com.portol.common.model.CookieHolder;
import com.portol.common.model.PortolPlatform;
import com.portol.common.model.player.Player;
import com.portol.common.model.user.User;
import com.portol.loadbalancer.manager.MongoManaged;

public class PlatformRepository {

	public static final Logger logger = LoggerFactory.getLogger(PlatformRepository.class);

	//the DB we are using
	private JacksonDBCollection<PortolPlatform, String> platformMongo;

	public PlatformRepository(MongoManaged addrMongoManaged){
		this.platformMongo = JacksonDBCollection.wrap(addrMongoManaged.getDB().getCollection("platforms"), PortolPlatform.class, String.class);
	}

	public PortolPlatform findByPlatformId(String platformId) {
		
		BasicDBObject query = new BasicDBObject("_id", platformId);
		
		
		return platformMongo.findOne(query);
	}

	public void save(PortolPlatform matchingPlatform) {
		BasicDBObject query = new BasicDBObject("platformId", matchingPlatform.getPlatformId());

		platformMongo.remove(query);
		
		platformMongo.save(matchingPlatform);

	}







}
