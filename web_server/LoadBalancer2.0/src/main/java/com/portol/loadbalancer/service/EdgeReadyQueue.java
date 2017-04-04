package com.portol.loadbalancer.service;

import io.dropwizard.lifecycle.Managed;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AbstractScheduledService;
import com.portol.common.model.instance.EdgeInstance;
import com.portol.common.model.instance.Instance;
import com.portol.common.model.instance.Instance.Status;
import com.portol.loadbalancer.config.ReadyQueueConfiguration;
import com.portol.loadbalancer.manager.MongoManaged;
import com.portol.loadbalancer.repo.EdgeCloudRepository;


public class EdgeReadyQueue extends AbstractScheduledService implements
Managed {

	private final Logger logger = LoggerFactory
			.getLogger("edgelogger");

	private final CloudCommunicator cComm;

	private static int qSize;

	private static int curSize;

	private EdgeCloudRepository edgeRepo;

	private CloudProviderPool cloudProvider;

	public EdgeReadyQueue(ReadyQueueConfiguration getrQConf,
			CloudCommunicator comm, EdgeCloudRepository cldRepo,
			CloudProviderPool cprovider) {
		qSize = getrQConf.initialSize;
		this.cComm = comm;
		this.cloudProvider = cprovider;
		this.edgeRepo = cldRepo;
		curSize = 0;
		logger.info("*********************NEW RUN BEGINNING**********************************");
	}

	public synchronized void maintainQueuedPlayers() throws Exception {
		logger.info("Ready queue updater running");
	
		// get number of queued players from db
		curSize = edgeRepo.countReadyEdge()
				+ edgeRepo.countBootingEdgeServers();
		logger.info("current size of ready players: " + curSize);


		// if queue is larger than readyQueueLength
		while (curSize > qSize) {
			logger.info("too many eleigible players! removing one... ");
			
			Thread deleteSingle = new Thread(){
				@Override
				public void run(){
					EdgeInstance toKill = edgeRepo.findEdgeToDelete();
					//update cloud info accordingly and send this to the idle pile
					toKill.setServing(null);
					toKill.setStatus(Status.DEACTIVATED);
					edgeRepo.save(toKill);
					curSize--;
					logger.info("killing off cloud @IP: " + toKill.getId());
					try {
						boolean success = gracefulKill(toKill);
						if(success){
							
							
						}
					} catch (Exception e) {
						logger.error("graceful kill failed!", e);
						curSize = edgeRepo.countReadyEdge()
								+ edgeRepo.countBootingEdgeServers();
						return;
					}
					logger.info("extra player removed ");
					curSize = edgeRepo.countReadyEdge()
							+ edgeRepo.countBootingEdgeServers();
				}
			};
			
			deleteSingle.start();
			deleteSingle.join();
			
			
			
		}

		// if queue is smaller than readyqueue length
		while (curSize < qSize) {
			logger.info("not enough players! adding one... ");
			// spin up new player
			// add to ready queue
			
			Thread addSingle = new Thread(){
				@Override
				public void run(){
			EdgeInstance newEdge = cloudProvider.getBestProviderAPI().spinUpNewEdge();
			// this will likely need to be compared with the IP of reporting in
			// clouds so that portol IDs can be matched

			if(newEdge == null){
				logger.error("null server returned from spin new edge operation");
				curSize = edgeRepo.countReadyEdge()
						+ edgeRepo.countBootingEdgeServers();
				return; 
			}
			
			logger.info("new edge server activated: " + newEdge.getId());
			// new key
			newEdge.setAdminKey(UUID.randomUUID().toString());
			newEdge.setStatus(EdgeInstance.Status.BOOTING);
			edgeRepo.save(newEdge);
			logger.info("extra player added ");
				}
			};
			
			addSingle.start();
			addSingle.join();
    		curSize++;
			
		}

	}

	private boolean gracefulKill(EdgeInstance toKill) throws Exception {

		try {
		// gracefully hose the cloud player
		EdgeInstance finalState = cComm.sigKill(toKill);

		finalState.setStatus(EdgeInstance.Status.SHUTDOWN);

		String result = cloudProvider.getBestProviderAPI().destroyCloud(finalState);

		if (result == null) {
			throw new Exception("cloud player not killed properly");
		}

//		edgeRepo.save(finalState);
		return true;
		}catch (Exception e){
			logger.error("error in graceful kill: ", e);
			return false;
		}

	}

	@Override
	protected void runOneIteration() {
		try {
			maintainQueuedPlayers();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.warn("ERROR in maintaining queued players: " + e.getMessage());
		}
		// any other periodic maintenence...
		logger.info("running update of edge cloud ready queue, in runoneiteration()");
		updateIdleClouds();

	}



	public synchronized String returnEdge(EdgeInstance toReturn) {
		curSize++;

		toReturn.setStatus(EdgeInstance.Status.QUEUED);

		String savedId = edgeRepo.save(toReturn);

		return savedId;
	}

	
	public synchronized boolean updateIdleClouds(){
		
		List<EdgeInstance> idles = edgeRepo.findIdling();
		
		//set date to compare to as 1 hour in the past
		Date idleExpiration = new Date(System.currentTimeMillis() - 1000 * 3600);
		for(EdgeInstance idle : idles){
			if(idle.getLastReport().before(idleExpiration)){
				//then cloud is idle, upgrade it to the regular queue
				idle.setStatus(Status.QUEUED);
				edgeRepo.save(idle);
				
			}
		}
		
		return true;
		
	}
	public synchronized EdgeInstance getNextEdge() throws Exception {
		
		if (curSize == 0) {
			maintainQueuedPlayers();
		}
		
		EdgeInstance retCloud = edgeRepo.findEdgeServerWithStatus(EdgeInstance.Status.QUEUED);

		

		curSize--;

		

		return retCloud;
	}

	@Override
	protected Scheduler scheduler() {
		return AbstractScheduledService.Scheduler.newFixedRateSchedule(0, 30,
				TimeUnit.SECONDS);
	}

	@Override
	public void start() throws Exception {
		logger.info("startin up via manager");
		this.startAsync().awaitRunning();

	}

	@Override
	public void stop() throws Exception {
		logger.info("shutting down via manager");
		this.stopAsync().awaitTerminated();

	}

}