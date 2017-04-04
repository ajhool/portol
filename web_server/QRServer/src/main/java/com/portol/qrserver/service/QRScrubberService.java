package com.portol.qrserver.service;

import io.dropwizard.lifecycle.Managed;

import java.io.File;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AbstractScheduledService;
import com.portol.qrserver.config.QRScrubberConfig;

public class QRScrubberService extends AbstractScheduledService implements
Managed {

	private final Logger logger = LoggerFactory
			.getLogger(QRScrubberService.class);

	private final QRScrubberConfig thisConfig; 

	private final String qrDirRoot;

	public QRScrubberService(QRScrubberConfig scrubberConf, String qrDirRoot) {
		super();
		this.thisConfig = scrubberConf;
		this.qrDirRoot = qrDirRoot;
	}

	@Override
	protected void runOneIteration() throws Exception {
		logger.debug("QR scrubber running");
		//perform periodic tasks

		//regardless, perform normal deletion of old QRs
		int numQRs = this.scrubOldQRs();

		if(numQRs >= thisConfig.maxNumQR){
			logger.warn("WARNING: MAX QR COUNT EXCEEDED. Forcibly deleting oldest QRs");
			this.emergencyTrimQRs();
		} 

	}

	private int scrubOldQRs() {
		File root = new File(qrDirRoot);
		int count = 0;
		Iterator<File> fileIter = FileUtils.iterateFiles(root, null, true);


		while (fileIter.hasNext()) {

			File cur = fileIter.next();
			logger.debug("Iterating over file: " + cur.getName());

			//if file is older, hose it
			if( Math.abs(System.currentTimeMillis() - cur.lastModified()) > (thisConfig.ttlSeconds * 1000) ){
				logger.debug("Deleting file: " + cur.getName() + " last modified on: " + new Date(cur.lastModified()).toString());
				FileUtils.deleteQuietly(cur);
			} else {
				//file is still valid, so count it
				count++;
			}
		}

		return count;

	}

	public class FileAgeComparator implements Comparator<File>{

		@Override
		public int compare(File file1, File file2) {
			if(file1.lastModified() < file2.lastModified()){
				return -1;
			}

			if(file1.lastModified() > file2.lastModified()){
				return 1;
			}

			return 0;
		}
	}

	private void emergencyTrimQRs() {
		File root = new File(qrDirRoot);
		Collection<File> fileColl = FileUtils.listFiles(root, null, true);

		PriorityQueue<File> PQ = new PriorityQueue<File>(new FileAgeComparator());

		PQ.addAll(fileColl);
		
		while(PQ.size() >= thisConfig.maxNumQR){
			File deleted = PQ.poll();
			
			logger.warn("Warning: deleted file : " + deleted.getName() + " last modified: " + new Date(deleted.lastModified()).toString() + "...");
			logger.warn("this file may still be in use - increase QR code storage space!");
		}

	}

	@Override
	protected Scheduler scheduler() {
		return AbstractScheduledService.Scheduler.newFixedRateSchedule(0, 210,
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