package com.portol.cloudplayer.service;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dropwizard.setup.Environment;

import com.portol.cloudplayer.config.BackendServerConfig;
import com.portol.cloudplayer.config.MongoConfig;
import com.portol.cloudplayer.repository.RemoteDataRepo;
import com.portol.common.model.content.Content;

public class ContentSourceController {


	private ConcurrentHashMap<String, RemoteDataRepo> activeRepos;
	private MongoConfig genericRepoConfig; 
	private Environment currentEnv;


	private static final Logger logger = LoggerFactory.getLogger(ContentSourceController.class);
	public ContentSourceController(BackendServerConfig config,
			Environment environment) {
		activeRepos = new ConcurrentHashMap<String, RemoteDataRepo>();
		currentEnv = environment;
	}

	public class DBPrepper implements Runnable {
		private final Content toLoad; 
		public DBPrepper(Content toLoad) {
			this.toLoad = toLoad;
		}

		public void run() {
			RemoteDataRepo remoteData = new RemoteDataRepo(genericRepoConfig);
			currentEnv.lifecycle().manage(remoteData);

			RemoteDataRepo old = activeRepos.put(toLoad.getId(), remoteData);
			
			try {
				remoteData.prepDB(toLoad);
			} catch (Exception e) {
				e.printStackTrace();
			}

			if(old != null){
				logger.info("There was a repo w/id: " + toLoad.getId() + " here before...");
			}
			logger.info("Data Database Initialized, id: " + toLoad.getId());
		}
	}
	
	public String wipeRepoAndData(String dbid) throws Exception{
		RemoteDataRepo toWipe = activeRepos.get(dbid);
		
		if(toWipe == null){
			return null;
		}
		
		String id = toWipe.destroy();
		
		if(!id.equalsIgnoreCase(dbid)){
			throw new Exception("deleted content doesn't match what we specified!");
		}
		
		this.activeRepos.remove(dbid);
		return id;
		
	}



	public String prepRemoteDB(Content toLoad) throws InterruptedException {

		Runnable r = new DBPrepper(toLoad);
		Thread t = new Thread(r);
		
		t.start();
		
		t.join();

		return toLoad.getId();
	}

	public Content getLastSaved(String dbid) {
		return activeRepos.get(dbid).getCurrentLocal();
	}
	
	
	
	public class DBLoader implements Runnable {
		private final String targetDBID;
		private final boolean isPreview; 
		
		private Content retrieved = null; 
		public DBLoader(String targetDBID, boolean isPreview) {
			this.targetDBID = targetDBID;
			this.isPreview = isPreview;
		}

		public void run() {

			RemoteDataRepo readyForNewData = activeRepos.get(targetDBID);
			
			

			if(readyForNewData == null){
				logger.info("There was a repo w/id: " + targetDBID + " here before...");
				try {
					throw new Exception("no prepared DB found!");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			retrieved = readyForNewData.getCurrentLocal();
			
			if(isPreview){
				try {
					Content actual = readyForNewData.savePreviewContent();
					if(!actual.getId().equalsIgnoreCase(retrieved.getId())){
						throw new Exception("didnt download expected content");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			} else {
				try {
					Content actual = readyForNewData.saveContent();
					if(!actual.getId().equalsIgnoreCase(retrieved.getId())){
						throw new Exception("didnt download expected content");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			logger.info("Data downloading, id: " + retrieved.getId());
		}

		public Content  getRetrieved(){
			return retrieved;
		}

	}

	public String saveContent(String dbid, boolean isPreview) throws InterruptedException {
		DBLoader r = new DBLoader(dbid, isPreview);
		Thread t = new Thread(r);
		
		t.start();

		Thread.sleep(200);
		Content downloading = r.getRetrieved();
		return downloading.getId();

	}
}
