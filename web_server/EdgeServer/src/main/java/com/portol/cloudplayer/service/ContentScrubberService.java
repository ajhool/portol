package com.portol.cloudplayer.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dropwizard.lifecycle.Managed;

import com.google.common.util.concurrent.AbstractScheduledService;
import com.portol.cloudplayer.config.ContentScrubberConfig;

public class ContentScrubberService extends AbstractScheduledService implements Managed {

	private final Logger logger = LoggerFactory.getLogger(ContentScrubberService.class);

	private ContentScrubberConfig config;
	private ArrayList<File> scrubRootDirs;
	private boolean enabled;

	public ContentScrubberService(ContentScrubberConfig conf) {
		super();
		this.config = conf;

		// load content dirs in at boot

		scrubRootDirs = new ArrayList<File>();

		List<String> fileNames = conf.javaRelativeDirs;
		if (fileNames != null) {
			for (String fileName : fileNames) {
				File toScrub = new File(fileName);
				scrubRootDirs.add(toScrub);
				logger.debug("controlling age of all children of: " + toScrub.getAbsolutePath());

			}
		} else {
			logger.warn("no directories specified to scrub");
		}

		this.enabled = false;

	}

	public void enable() {
		enabled = true;
	}

	public void disable() {
		enabled = false;
	}

	private boolean scrubOldContent() {

		for (File temp : scrubRootDirs) {
			if (temp.exists()) {
				this.scrubDirRecursively(temp);
			} else {
				logger.debug("File : " + temp.getName() + "not in use, so not being scrubbed");
			}
		}

		return true;
	}

	private boolean scrubDirRecursively(File root) {

		int count = 0;
		Iterator<File> fileIter = FileUtils.iterateFiles(root, null, true);

		while (fileIter.hasNext()) {

			File cur = fileIter.next();

			if (cur.isDirectory())
				continue;

			// if file is older, hose it
			if (Math.abs(System.currentTimeMillis() - cur.lastModified()) > (config.ttlSeconds * 1000)
					&& !isProtectedFile(cur.getName())) {
				logger.debug("Deleting file: " + cur.getName() + " last modified on: "
						+ new Date(cur.lastModified()).toString());
				FileUtils.deleteQuietly(cur);
			} else {
				// file is still valid, so count it
				count++;
			}

		}

		return true;
	}

	private boolean isProtectedFile(String name) {
		if (name.contains("init")) {
			return true;
		}
		return false;
	}

	@Override
	public void start() throws Exception {
		this.startAsync().awaitRunning();

	}

	@Override
	public void stop() throws Exception {
		this.stopAsync().awaitTerminated();

	}

	@Override
	protected void runOneIteration() throws Exception {
		if (enabled) {
			logger.debug("Content scrubber service running....");
			this.scrubOldContent();
		} else {
			logger.info("Content scrubber disabled, skipping...");
		}
	}

	@Override
	protected Scheduler scheduler() {
		return AbstractScheduledService.Scheduler.newFixedRateSchedule(0, 210, TimeUnit.SECONDS);
	}

}
