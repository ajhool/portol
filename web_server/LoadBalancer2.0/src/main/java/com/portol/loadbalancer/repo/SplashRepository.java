package com.portol.loadbalancer.repo;

import org.mongojack.DBCursor;
import org.mongojack.JacksonDBCollection;

import com.mongodb.BasicDBObject;
import com.portol.common.model.content.ContentMetadata;
import com.portol.loadbalancer.manager.MongoManaged;

public class SplashRepository {
	
	//the DB we are using
	private JacksonDBCollection<ContentMetadata, String> splashMongo;

	public SplashRepository(MongoManaged splashMongoManaged){
		this.splashMongo = JacksonDBCollection.wrap(splashMongoManaged.getDB().getCollection("splash"), ContentMetadata.class, String.class);
	}

	public ContentMetadata getSplashScreenById(String splashDataId) throws Exception {
		BasicDBObject query = new BasicDBObject();
		query.put("_id", splashDataId);
		DBCursor<ContentMetadata> splashs = splashMongo.find(query);
		
		if(splashs.count() != 1){
			throw new Exception("wrong number of splash results found...");
		}
		
		return splashs.toArray().get(0);
	}
	
	
	
	
	
	

}
