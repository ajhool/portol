package com.portol.cdn.repository;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.portol.common.model.cdn.RegionalServer;

public class EdgeRepository {

	final static Logger logger = LoggerFactory.getLogger(EdgeRepository.class);

	DB currentEdges;
	HTreeMap<String, RegionalServer> serverMap;

	private boolean openDB() {
		currentEdges = DBMaker.newFileDB(new File("edgeservers.db")).closeOnJvmShutdown()
				.encryptionEnable("stringLiteral!").make();
		serverMap = currentEdges.getHashMap("edgeServers");
		logger.debug("Database open: " + !currentEdges.isClosed());
		return true;
	}

	public EdgeRepository() {
		this.openDB();
	}

	// returns ID of saved edge server
	public String save(RegionalServer incoming) {

		if (serverMap == null) {
			this.openDB();
		}
		RegionalServer old = serverMap.put(incoming.getId(), incoming);
		if (old != null) {
			logger.warn("exisiting id found, deleting old occupant of this edgerepo");
		}
		logger.debug("saving edge server with key: " + incoming.getId() + " : " + incoming);
		return incoming.getId();

	}

	public RegionalServer findOneById(String id) {

		if (serverMap == null) {
			this.openDB();
		}

		RegionalServer nextPlayer = serverMap.get(id);
		logger.debug("player with id: " + id + " found: " + nextPlayer);
		return nextPlayer;
	}

	public List<RegionalServer> getAll() {
		if (serverMap == null) {
			this.openDB();
		}

		ArrayList<RegionalServer> results = new ArrayList<RegionalServer>(serverMap.values());
		return results;

	}

	public RegionalServer delete(RegionalServer toDelete) {

		if (serverMap == null) {
			this.openDB();
		}

		RegionalServer removed = serverMap.remove(toDelete.getId());
		return removed;
	}

}
