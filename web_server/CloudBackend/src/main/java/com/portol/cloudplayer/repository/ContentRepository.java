package com.portol.cloudplayer.repository;

import org.mongojack.DBCursor;
import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;
import org.mongojack.DBQuery.Query;





import com.portol.cloudplayer.manager.MongoManaged;
import com.portol.common.model.content.Content;

public class ContentRepository {

	//the DB we are using
	private JacksonDBCollection<Content, String> contentMongo;

	public ContentRepository(MongoManaged contentMongoManaged){
		this.contentMongo = JacksonDBCollection.wrap(contentMongoManaged.getDB().getCollection("content"), Content.class, String.class);
	}

	public Content findByVideoKey(String videoKey) throws Exception {

		Query query = DBQuery.is("contentKey", videoKey);
		DBCursor<Content> cursor = contentMongo.find(query);

		if(cursor.count() != 1){
			throw new Exception("cant find one piece of mathcing content! content matches: " + cursor.count());
		}

		return cursor.toArray().get(0);
	}

	public Content findById(String Id) {

		return contentMongo.findOneById(Id);
	}

}
