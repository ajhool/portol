package com.portol.webserver.repository;

import org.mongojack.DBCursor;
import org.mongojack.JacksonDBCollection;

import com.mongodb.BasicDBObject;
import com.portol.common.model.content.ContentMetadata;
import com.portol.webserver.manager.MongoManaged;

public class MetadataRepository {
	
	//the DB we are using
	private JacksonDBCollection<ContentMetadata, String> splashMongo;

	public MetadataRepository(MongoManaged splashMongoManaged){
		this.splashMongo = JacksonDBCollection.wrap(splashMongoManaged.getDB().getCollection("splash"), ContentMetadata.class, String.class);
	}

	public ContentMetadata getSplashScreenById(String splashDataId) throws Exception {

		ContentMetadata splash = splashMongo.findOneById(splashDataId);
		
		return splash;
	}
	
	
	public ContentMetadata getMetadataForParentKey(String parentContentKey) throws Exception {
		BasicDBObject query = new BasicDBObject();
		query.put("parentContentKey", parentContentKey);
		DBCursor<ContentMetadata> splashs = splashMongo.find(query);
		
		if(splashs.count() != 1){
			throw new Exception("wrong number of splash results found...");
		}
		
		return splashs.toArray().get(0);
	}
	
	public ContentMetadata getMetadataForParent(String parentContentId) throws Exception {
		BasicDBObject query = new BasicDBObject();
		query.put("parentContentId", parentContentId);
		DBCursor<ContentMetadata> splashs = splashMongo.find(query);
		
		if(splashs.count() != 1){
			throw new Exception("wrong number of splash results found...");
		}
		
		return splashs.toArray().get(0);
	}
	
	
	

}
