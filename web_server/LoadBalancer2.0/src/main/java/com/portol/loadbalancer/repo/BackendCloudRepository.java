package com.portol.loadbalancer.repo;

import org.mongojack.DBCursor;
import org.mongojack.JacksonDBCollection;

import com.mongodb.BasicDBObject;
import com.portol.common.model.content.Content;
import com.portol.common.model.instance.BackendInstance;
import com.portol.common.model.instance.EdgeInstance;
import com.portol.common.model.instance.Instance;
import com.portol.common.model.instance.Instance.Status;
import com.portol.loadbalancer.manager.MongoManaged;

public class BackendCloudRepository {

	// the DB we are using
	private JacksonDBCollection<BackendInstance, String> backendMongo;
	
	public BackendCloudRepository(MongoManaged backendMongoManaged) {
		this.backendMongo = JacksonDBCollection.wrap(backendMongoManaged.getDB()
				.getCollection("backendClouds"), BackendInstance.class, String.class);
		
	}

	// return null if noting found
	public BackendInstance findBackendServing(Content serving) {
		BasicDBObject query = new BasicDBObject("serving._id", serving.getId());

		DBCursor<BackendInstance> cursor = backendMongo.find(query);

		if (cursor.count() < 1) {
			return null;
		} else
			return cursor.next();

	}

	public int countReadyBackends() {
		BasicDBObject query = new BasicDBObject("status",
				Instance.Status.QUEUED);

		DBCursor<BackendInstance> results = backendMongo.find(query);

		return results.count();
	}
	
	
	public int countBootingBackends() {
		BasicDBObject query = new BasicDBObject("status",
				Instance.Status.BOOTING);

		DBCursor<BackendInstance> results = backendMongo.find(query);

		return results.count();
	}

	public BackendInstance findBackendToDelete() {
		BasicDBObject query = new BasicDBObject("status",
				Instance.Status.QUEUED);

		DBCursor<BackendInstance> results = backendMongo.find(query);

		if (results.count() < 1) {

			return null;
		} else return results.next();

	}
	
	

	public String save(BackendInstance finalState) {
		return backendMongo.save(finalState).getSavedObject().getId();

	}

	public BackendInstance findBackendWithStatus(Status queued) {
		BasicDBObject query = new BasicDBObject("status",
				queued);

		DBCursor<BackendInstance> results = backendMongo.find(query);

		if (results.count() < 1) {

			return null;
		} else return results.next();

	}


	public BackendInstance findBackendWithIP(String requestIP) {
		
		BasicDBObject query = new BasicDBObject("location",
				requestIP);

		DBCursor<BackendInstance> results = backendMongo.find(query);

		if (results.count() < 1) {

			return null;
		} else return results.next();
	}

	public BackendInstance findBackendWithID(String id) {

		BackendInstance result = backendMongo.findOneById(id);

	 return result;
	}
}
