package com.portol.cloudplayer.repository;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.mongojack.JacksonDBCollection;
import org.slf4j.LoggerFactory;

import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Environment;
import ch.qos.logback.classic.Logger;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import com.portol.cloudplayer.config.BackendServerConfig;
import com.portol.cloudplayer.config.MongoConfig;
import com.portol.cloudplayer.healthcheck.MongoHealthCheck;
import com.portol.cloudplayer.manager.MongoUnManaged;
import com.portol.common.model.content.Content;

public class RemoteDataRepo implements Managed {

	public static final org.slf4j.Logger logger = LoggerFactory.getLogger(RemoteDataRepo.class);
	private MongoUnManaged remoteOriginal;
	private State state;
	private MongoConfig config;
	private GridFS remoteFS;
	File root;

	private Content currentLocal;

	public Content getCurrentLocal() {
		return currentLocal;
	}

	private Object mutex = new Object();

	public enum State {
		DISCONNECTED, DOWNLOADING, IDLE, ERROR, DESTROYED
	};

	public RemoteDataRepo(MongoConfig mongoDataConfiguration) {
		this.state = State.DISCONNECTED;
		this.config = mongoDataConfiguration;
	}

	public boolean prepDB(Content toDownload) throws Exception {

		synchronized (mutex) {
			if (state == State.DOWNLOADING) {
				throw new Exception("already busy downloading!");
			}
			if (state == State.ERROR) {
				throw new Exception("already in Error state!");
			}

			// otherwise we are good
			state = State.DOWNLOADING;

		}

		currentLocal = toDownload;

		// set up mongo
		String host = toDownload.getGlobalSrc().getHost();
		int port = toDownload.getGlobalSrc().getPort();
		String DB = toDownload.getGlobalSrc().getDbName();

		remoteOriginal = new MongoUnManaged(host, port, DB, toDownload.getGlobalSrc().getDbuserName(),
				toDownload.getGlobalSrc().getDbPassword());

		String collname = toDownload.getGlobalSrc().getCollName();

		remoteFS = new GridFS(remoteOriginal.getDB(), collname);
		return true;

	}

	// expects a set up mongo + gridFS
	public Content saveContent() throws Exception {
		root = new File("assets/" + currentLocal.getGlobalSrc().getCollName());

		if (!root.exists()) {
			FileUtils.deleteDirectory(root.getParentFile());

			if (!root.mkdirs()) {
				logger.warn("old asset dir not deleted, manually deleting following file: " + root.getPath());
				File toDelete = new File(root.getPath());
				FileUtils.deleteDirectory(toDelete);

			}
			root = new File("assets/" + currentLocal.getGlobalSrc().getCollName());

		}

		BasicDBObject sort = new BasicDBObject("order", 1);
		BasicDBObject query = new BasicDBObject("contentType", "video/MP2T");

		long start = System.currentTimeMillis();
		List<GridFSDBFile> fileList = remoteFS.find(query, sort);
		long duration = System.currentTimeMillis() - start;
		int sec = (int) duration / 1000;
		for (int i = 0; i < fileList.size(); i++) {
			GridFSDBFile cur = fileList.get(i);
			String path = cur.get("path").toString();

			logger.debug("path is: " + path);
			logger.debug("making file: " + root.getPath() + "/" + path + "/" + cur.getFilename());
			File output = new File(root.getPath() + "/" + path + "/" + cur.getFilename());
			if (!output.exists()) {
				output.getParentFile().mkdirs();
				cur.writeTo(output);
			} else {
				// integrity check
				FileInputStream fis = new FileInputStream(output);
				String md5 = DigestUtils.md5Hex(fis);
				fis.close();

				if (!cur.getMD5().equalsIgnoreCase(md5)) {
					logger.error("INTEGRITY CHECK FAILED FOR FILE: " + cur.getFilename());
					logger.warn("deleting file and re-attempting download");

					FileUtils.deleteQuietly(output);

					i--;
					continue;

				} else {
					logger.debug("integrity check for file: " + cur.getFilename() + " passed");
				}

			}
		}

		this.state = State.IDLE;
		return currentLocal;
	}

	// expects a set up mongo + gridFS
	public Content savePreviewContent() throws Exception {
		// create root dir
		File root = new File("assets/" + currentLocal.getGlobalSrc().getCollName());
		BasicDBObject sort = new BasicDBObject("order", 1);
		BasicDBObject query = new BasicDBObject("contentType", "video/MP2T");

		List<GridFSDBFile> fileList = remoteFS.find(query, sort);
		for (int i = 0; i < fileList.size(); i++) {
			GridFSDBFile cur = fileList.get(i);

			String path = cur.get("path").toString();

			logger.debug("path is: " + path);
			logger.debug("making file: " + root.getPath() + "/" + path + "/" + cur.getFilename());
			File output = new File(root.getPath() + "/" + path + "/" + cur.getFilename());
			if (!output.exists()) {
				output.getParentFile().mkdirs();
				cur.writeTo(output);
			} else {
				// integrity check
				FileInputStream fis = new FileInputStream(output);
				String md5 = DigestUtils.md5Hex(fis);
				fis.close();

				if (!cur.getMD5().equalsIgnoreCase(md5)) {
					logger.error("INTEGRITY CHECK FAILED FOR FILE: " + cur.getFilename());
					logger.warn("deleting file and re-attempting download");

					FileUtils.deleteQuietly(output);

					i--;
					continue;

				} else {
					logger.debug("integrity check for file: " + cur.getFilename() + " passed");
				}

			}
		}

		this.state = State.IDLE;
		return currentLocal;
	}

	@Override
	public void start() throws Exception {
		if (remoteOriginal != null) {
			remoteOriginal.start();
		}
	}

	@Override
	public void stop() throws Exception {
		remoteOriginal.stop();
		state = State.DISCONNECTED;
	}

	public State getState() {
		return state;
	}

	public String destroy() throws Exception {
		FileUtils.deleteDirectory(root);
		remoteOriginal.stop();
		this.state = State.DESTROYED;
		return this.currentLocal.getId();
	}
}