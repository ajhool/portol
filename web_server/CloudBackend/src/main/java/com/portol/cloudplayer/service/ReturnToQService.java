package com.portol.cloudplayer.service;

import java.io.File;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.slf4j.LoggerFactory;

import com.portol.cloudplayer.resource.SetContentResource;
import com.portol.common.model.instance.BackendInstance;
import com.portol.common.model.instance.EdgeInstance;

public class ReturnToQService {

	public final int MAX_IDLE_PERIOD_SEC;

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ReturnToQService.class);

	private LoadbalCommunicator comm;

	private Timer timeoutTimer;
	private BackendInstance _this;

	private TimerTask task = new TimerTask() {

		public void run() {
			try {
				ReturnToQService.this.handleTimeout();
			} catch (Exception e) {
				logger.error("Uncaught Exception", e);
				return;
			} catch (Throwable e) {
				logger.error("Unrecoverable error", e);
				return;
			}
		}
	};

	public ReturnToQService(LoadbalCommunicator comm, int maxIdleSeconds, BackendInstance _this) {
		super();
		this.comm = comm;
		this.MAX_IDLE_PERIOD_SEC = maxIdleSeconds;
		this._this = _this;
	}

	public synchronized boolean activateTimeout() {
		try {
			if (timeoutTimer != null) {
				timeoutTimer.cancel();
			}

			timeoutTimer = new Timer(false);
			timeoutTimer.schedule(task, MAX_IDLE_PERIOD_SEC * 1000);

		} catch (IllegalStateException e) {
			logger.warn("reset already cancelled object ");
			return false;
		}
		return true;
	}

	public synchronized boolean resetTimeout() {
		try {
			if (timeoutTimer != null) {
				timeoutTimer.cancel();
			}

			timeoutTimer = new Timer(false);

			timeoutTimer.schedule(task, MAX_IDLE_PERIOD_SEC * 1000);

		} catch (IllegalStateException e) {
			logger.warn("reset already cancelled object ");
			return false;
		}
		return true;
	}

	protected synchronized void handleTimeout() throws Exception {
		if (_this == null) {
			throw new Exception("Self reference of cloudplayer must not be null!!");
		}

		_this.setStatus(EdgeInstance.Status.IDLE);

		_this.setEvent(EdgeInstance.EventCode.NO_CLIENTS);
		logger.info("activity timeout passed, aleting loadbal to inactivity");

		comm.reportEvent(_this);
	}

	public synchronized static void scrubAllFiles() {
		Collection<File> filesAndDirs = FileUtils.listFilesAndDirs(new File(System.getProperty("user.dir")),
				TrueFileFilter.INSTANCE, DirectoryFileFilter.DIRECTORY);
		for (File cur : filesAndDirs) {
			if (cur.getName().contains(".mp4") || cur.getName().contains(".tmp") || cur.getName().contains("stream")
					|| cur.getName().contains("asset")) {
				logger.debug("Scrubbing file: " + cur.getName());
				FileUtils.deleteQuietly(cur);
			}
		}
	}

}
