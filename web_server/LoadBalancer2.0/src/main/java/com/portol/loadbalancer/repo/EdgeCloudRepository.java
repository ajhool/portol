package com.portol.loadbalancer.repo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.mongojack.DBCursor;
import org.mongojack.JacksonDBCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import com.portol.common.model.content.Content;
import com.portol.common.model.instance.EdgeInstance;
import com.portol.common.model.instance.Instance.Status;
import com.portol.loadbalancer.manager.MongoManaged;

public class EdgeCloudRepository {

	// the DB we are using
	private JacksonDBCollection<EdgeInstance, String> edgeMongo;

	public EdgeCloudRepository(MongoManaged cloudMongoManaged) {
		this.edgeMongo = JacksonDBCollection.wrap(cloudMongoManaged.getDB().getCollection("edgeClouds"),
				EdgeInstance.class, String.class);

	}

	// return null if noting found
	public EdgeInstance findEdgePlaying(Content toPlay) throws InterruptedException {

		BasicDBObject query = new BasicDBObject("serving._id", toPlay.getId());

		DBCursor<EdgeInstance> cursor = edgeMongo.find(query);

		if (cursor.count() < 1) {
			return null;
		} else
			return cursor.next();

	}

	public synchronized int countReadyEdge() {
		BasicDBObject query = new BasicDBObject("status", EdgeInstance.Status.QUEUED);

		DBCursor<EdgeInstance> results = edgeMongo.find(query);

		return results.count();
	}

	public synchronized int countIdleEdge() {
		BasicDBObject query = new BasicDBObject("status", EdgeInstance.Status.IDLE);

		DBCursor<EdgeInstance> results = edgeMongo.find(query);

		return results.count();
	}

	public int countBootingEdgeServers() {
		BasicDBObject query = new BasicDBObject("status", EdgeInstance.Status.BOOTING);

		DBCursor<EdgeInstance> results = edgeMongo.find(query);

		return results.count();
	}

	public EdgeInstance findEdgeToDelete() {
		logger.info("loadbal requested cloud to delete");
		BasicDBObject query = new BasicDBObject("status", EdgeInstance.Status.QUEUED);

		DBCursor<EdgeInstance> results = edgeMongo.find(query);

		if (results.count() < 1) {

			return null;
		}

		List<EdgeInstance> potentials = results.toArray();

		ArrayList<SortHolder> sortingArr = new ArrayList<SortHolder>();

		for (int i = 0; i < potentials.size(); i++) {
			SortHolder temp = new SortHolder();
			temp.index = i;
			temp.timeLeft = computeRemainingSeconds(potentials.get(i));
			sortingArr.add(temp);
		}

		Collections.sort(sortingArr);

		logger.info("sorting completed");
		logger.info("Seconds left until next hour of cloud to be deleted: " + sortingArr.get(0).timeLeft);

		if (sortingArr.size() >= 2) {
			logger.info("Seconds left until next hour of runner-up cloud " + sortingArr.get(1).timeLeft);
		}

		return potentials.get(sortingArr.get(0).index);

	}

	public static final Logger logger = LoggerFactory.getLogger(EdgeCloudRepository.class);

	private long computeRemainingSeconds(EdgeInstance edgeInstance) {
		long lifeTime = System.currentTimeMillis() - edgeInstance.getBootTime().getTime();

		if (lifeTime < 0) {
			logger.error("negative lifetime, just setting this to be int max val");
			lifeTime = Integer.MAX_VALUE;
		}

		long lifetimeSeconds = lifeTime / 1000;

		long remainingSeconds = lifetimeSeconds % 3600;

		// remainingseconds is how long over an hour we have lived
		// so subtract from 3600
		remainingSeconds = 3600 - remainingSeconds;

		// if this cloud already has some content, bias towards keeping it
		return remainingSeconds;
	}

	private class SortHolder implements Comparable<SortHolder> {

		public long timeLeft;
		public int index;

		@Override
		public int compareTo(SortHolder o) {

			if (this.timeLeft > o.timeLeft) {
				return 1;
			} else if (this.timeLeft < o.timeLeft) {

				return -1;
			} else {
				return 0;
			}
		}
	}

	public synchronized String save(EdgeInstance finalState) {
		return edgeMongo.save(finalState).getSavedObject().getId();
	}

	public EdgeInstance findEdgeServerWithStatus(Status queued) {
		BasicDBObject query = new BasicDBObject("status", queued);

		DBCursor<EdgeInstance> results = edgeMongo.find(query);

		if (results.count() < 1) {
			return null;
		} else
			return results.next();

	}

	public EdgeInstance findEdgeWithIP(String requestIP) {

		BasicDBObject query = new BasicDBObject("location", requestIP);

		DBCursor<EdgeInstance> results = edgeMongo.find(query);

		if (results.count() < 1) {
			return null;
		} else
			return results.next();
	}

	public EdgeInstance findEdgeWithID(String id) {
		BasicDBObject query = new BasicDBObject("_id", id);

		EdgeInstance result = edgeMongo.findOne(query);

		return result;
	}

	public List<EdgeInstance> findIdling() {
		logger.info("finding all idling clouds");
		BasicDBObject query = new BasicDBObject("status", EdgeInstance.Status.IDLE);

		DBCursor<EdgeInstance> results = edgeMongo.find(query);

		if (results == null || results.size() < 1) {
			return new ArrayList<EdgeInstance>();
		}

		logger.info("found " + results.size() + " idling clouds");
		return results.toArray(20);

	}

	public EdgeInstance findEdgePreviewing(Content hasPreview) throws InterruptedException {

		// first, check if any clouds are serving this
		EdgeInstance playing = this.findEdgePlaying(hasPreview);

		if (playing != null) {
			return playing;
		}

		// next, try and find a cloud in preview mode
		BasicDBObject query = new BasicDBObject("status", EdgeInstance.Status.PREVIEW);
		query.append("serving._id", hasPreview.getId());

		DBCursor<EdgeInstance> results = edgeMongo.find(query);

		if (results == null || results.size() < 1) {
			return null;
		}

		List<EdgeInstance> allPreviewing = results.toArray();
		return allPreviewing.get(0);
	}

	public EdgeInstance findEligibleEdgeWithIP(String currentSourceIP) {

		BasicDBObject idling = new BasicDBObject("status", Status.IDLE);
		BasicDBObject running = new BasicDBObject("status", Status.RUNNING);
		BasicDBObject preview = new BasicDBObject("status", Status.PREVIEW);

		ArrayList<BasicDBObject> myList = new ArrayList<BasicDBObject>();
		myList.add(preview);
		myList.add(idling);
		myList.add(running);

		DBObject append = new BasicDBObject("$or", myList).append("location", currentSourceIP);

		DBCursor<EdgeInstance> results = edgeMongo.find(append);

		if (results.count() < 1) {
			return null;
		} else
			return results.next();
	}
}
