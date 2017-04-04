package com.portol.loadbalancer.service;

import io.dropwizard.lifecycle.Managed;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AbstractScheduledService;
import com.portol.common.model.instance.BackendInstance;
import com.portol.common.model.instance.Instance;
import com.portol.loadbalancer.config.ReadyQueueConfiguration;
import com.portol.loadbalancer.manager.MongoManaged;
import com.portol.loadbalancer.repo.BackendCloudRepository;


public class BackendReadyQueue extends AbstractScheduledService implements
		Managed {

	private final Logger logger = LoggerFactory
			.getLogger(BackendReadyQueue.class);

	private final CloudCommunicator cComm;

	private int qSize;

	private int curSize;

	private BackendCloudRepository backendRepo;

	private CloudProviderPool cloudProvider;

	public BackendReadyQueue(ReadyQueueConfiguration getrQConf,
			CloudCommunicator comm, BackendCloudRepository cldRepo,
			CloudProviderPool cprovider) {
		qSize = getrQConf.initialSize;
		this.cComm = comm;
		this.cloudProvider = cprovider;
		this.backendRepo = cldRepo;
		curSize = 0;
	
	}

	public void maintainQueuedBackends() throws Exception {
		logger.info("Ready queue updater running for backends");

		// get number of queued players from db
		curSize = backendRepo.countReadyBackends() + backendRepo.countBootingBackends();

		// if queue is larger than readyQueueLength
		while (curSize > qSize) {

			BackendInstance toKill = backendRepo.findBackendToDelete();

			gracefulKill(toKill);
			curSize--;
		}

		// if queue is smaller than readyqueue length
		while (curSize < qSize) {
			// spin up new player
			// add to ready queue
			BackendInstance newBackend = cloudProvider.getBestProviderAPI().spinUpNewBackend();

			// new key
			newBackend.setAdminKey(UUID.randomUUID().toString());
			newBackend.setStatus(Instance.Status.BOOTING);
			backendRepo.save(newBackend);
			curSize++;

		}

		curSize = backendRepo.countReadyBackends() + backendRepo.countBootingBackends();

	}

	private void gracefulKill(BackendInstance toKill) throws Exception {

		// gracefully hose the cloud player
		BackendInstance finalState = cComm.sigKill(toKill);

		finalState.setStatus(Instance.Status.SHUTDOWN);

		String result = cloudProvider.getBestProviderAPI().destroyCloud(finalState);

		if (result == null) {
			throw new Exception("cloud player not killed properly");
		}

		backendRepo.save(finalState);

	}

	@Override
	protected void runOneIteration() throws Exception {
		maintainQueuedBackends();
		// any other periodic maintenence...

	}



	public String returnBackend(BackendInstance toReturn) {
		curSize++;

		toReturn.setStatus(BackendInstance.Status.QUEUED);

		String savedId = backendRepo.save(toReturn);

		return savedId;
	}

	public BackendInstance getNextBackend() throws Exception {

		if (curSize == 0) {
			maintainQueuedBackends();
		}

		curSize--;

		BackendInstance retCloud = backendRepo.findBackendWithStatus(Instance.Status.QUEUED);

		if (retCloud == null) {
			throw new Exception("unable to find queued cloud in readyqueuesvc");
		}
		
		return retCloud;
	}

	@Override
	protected Scheduler scheduler() {
		return AbstractScheduledService.Scheduler.newFixedRateSchedule(0, 30,
				TimeUnit.SECONDS);
	}

	@Override
	public void start() throws Exception {
		this.startAsync().awaitRunning();

	}

	@Override
	public void stop() throws Exception {
		this.stopAsync().awaitTerminated();

	}

}