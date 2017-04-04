package com.portol.cloudplayer.service;

import java.io.File;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portol.cloudplayer.resource.SetContentResource;
import com.portol.common.model.instance.EdgeInstance;
import com.portol.common.model.instance.Instance.Status;


public class ReturnToQService {

	public final int MAX_IDLE_PERIOD_SEC;

	private static final org.slf4j.Logger logger = LoggerFactory
			.getLogger("lifecyclelogger");


	private LoadbalCommunicator comm;

	private Timer timeoutTimer;
	private EdgeInstance _this;

	public ReturnToQService(LoadbalCommunicator comm, int maxIdleSeconds,
			EdgeInstance _this) {
		super();
		this.comm = comm;
		this.MAX_IDLE_PERIOD_SEC = maxIdleSeconds;
		this._this = _this;
		logger.info("ready queue service started");
	}

	public synchronized boolean activateTimeout(){
		if(_this.getStatus() == Status.IDLE){
			//then there is some activity, we are no longer idle	
			updateNotIdle();
		}
		try{
			if(timeoutTimer != null){
				timeoutTimer.cancel();
			}

			TimerTask task = new TimerTask () {

				public void run () {
					try {
						handleTimeout();
					}catch (Exception e){
						logger.error("Uncaught Exception",e);
						return;
					}catch (Throwable e){
						logger.error("Unrecoverable error",e);
						return;
					}
				}
			};   

			timeoutTimer = new Timer(false);

			timeoutTimer.schedule(task, MAX_IDLE_PERIOD_SEC * 1000);

		} catch(IllegalStateException e){
			logger.warn("reset already cancelled object ");
			return false;
		}
		return true;
	}

	public synchronized boolean resetTimeout(){

		if(_this.getStatus() == Status.IDLE){
			//then there is some activity, we are no longer idle	
			updateNotIdle();
		}

		logger.info("resetting return to queue timeout");
		try{
			if(timeoutTimer != null){
				timeoutTimer.cancel();
			} 

			timeoutTimer = new Timer(false);
			TimerTask task = new TimerTask () {

				public void run () {
					try {
						handleTimeout();
					}catch (Exception e){
						logger.error("Uncaught Exception",e);
						return;
					}catch (Throwable e){
						logger.error("Unrecoverable error",e);
						return;
					}
				}
			};   

			timeoutTimer.schedule(task, MAX_IDLE_PERIOD_SEC * 1000);

		} catch(IllegalStateException e){
			logger.warn("reset already cancelled object ");
			return false;
		}
		return true;
	}


	private void updateNotIdle(){
		_this.setStatus(EdgeInstance.Status.RUNNING);

		Thread notIdle = new Thread() {
			@Override
			public void run(){


				_this.setEvent(EdgeInstance.EventCode.RECONNECT);
				logger.info("activity timeout passed, alerting loadbal to inactivity");
				try {
					logger.info("Current state: " + (new ObjectMapper()).writeValueAsString(_this));
				} catch (JsonProcessingException e) {
					logger.error("error updating not idle", e);
				}


				comm.pushToLoadbalancer(_this);
			}
		};
		notIdle.start();
	}



	protected synchronized void handleTimeout() throws Exception {
		if(_this == null){
			throw new Exception("Self reference of cloudplayer must not be null!!");
		}


		_this.setStatus(EdgeInstance.Status.IDLE);

		_this.setEvent(EdgeInstance.EventCode.NO_CLIENTS);
		logger.info("activity timeout passed, alerting loadbal to inactivity");
		logger.info("Current state: " + (new ObjectMapper()).writeValueAsString(_this));

		comm.pushToLoadbalancer(_this);



	}

	public synchronized static void scrubAllFiles() {
		Collection<File> filesAndDirs = FileUtils.listFilesAndDirs(new File(System.getProperty("user.dir")), TrueFileFilter.INSTANCE, DirectoryFileFilter.DIRECTORY);
		for(File cur : filesAndDirs){
			if(cur.getName().contains(".mp4") ||
					cur.getName().contains(".tmp") ||
					cur.getName().contains("stream") ||
					cur.getName().contains("asset")){
				logger.debug("Scrubbing file: " + cur.getName());
				FileUtils.deleteQuietly(cur);
			}
		}
	}


}
