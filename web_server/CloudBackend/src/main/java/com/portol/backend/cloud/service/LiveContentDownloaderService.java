package com.portol.backend.cloud.service;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.portol.cloudplayer.config.LiveContentDownloaderConfig;
import com.portol.cloudplayer.service.MPDUpdaterService;
import com.portol.common.model.content.Content;
import com.portol.common.model.content.ContentSource;
import com.portol.common.model.dash.jaxb.AdaptationSetType;
import com.portol.common.model.dash.jaxb.RepresentationType;

import ffmpeg.EDASHCallback;
import ffmpeg.FFMPEGController;
import ffmpeg.ShellCallback;
import ffmpeg.StdoutCallback;
import ffmpeg.eDashController;
import ffmpeg.eDashController.StreamType;
import io.dropwizard.lifecycle.Managed;

public class LiveContentDownloaderService implements Managed {

	public static final Logger logger = LoggerFactory.getLogger(LiveContentDownloaderService.class);
	private String apiKey;

	private ContentSource target;

	private LiveContentDownloaderConfig config;

	private MPDUpdaterService mpdUpdater;

	public LiveContentDownloaderService(LiveContentDownloaderConfig config, MPDUpdaterService mpdUpdater) {
		super();
		this.config = config;
		this.apiKey = config.apiKey;
		this.mpdUpdater = mpdUpdater;
	}

	public String startLiveDownload(Content toGet) throws Exception {
		this.toGet = toGet;
		this.target = toGet.getGlobalSrc();

		switch (toGet.getGlobalSrc().getSrcTyp()) {
		case HLS:
			logger.info("Content identified as HLS source. Beginning live download...");
			return HLSLiveDownload(toGet);

		default:
			logger.error("Unrecognized content source type");
			throw new Exception("Bad content source type");

		}

	}

	private ContentSource contentInfo;
	private Content toGet;

	private String HLSLiveDownload(Content toGet) throws IOException, InterruptedException {

		this.toGet = toGet;

		ffmpeg.start();

		// create global counters
		contentInfo = toGet.getGlobalSrc();

		logger.debug("sleeping for 4s to allow buffer to build...");
		Thread.sleep(1000);
		logger.debug("Waking up from sleep");

		dasher.start();

		return null;
	}

	Thread ffmpeg = new Thread() {

		@Override
		public void run() {

			ShellCallback sc = null;
			try {
				sc = new StdoutCallback();
			} catch (SecurityException | IOException e1) {
				e1.printStackTrace();
			}
			FFMPEGController me = new FFMPEGController();

			List<String> cmds = me.generateFFMPEGCommand(null);

			try {
				int exit = me.execProcess(cmds, sc, null);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	};

	Thread dasher = new Thread() {

		@Override
		public void run() {

			EDASHCallback sc = null;
			try {
				sc = new EDASHCallback();
			} catch (SecurityException | IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			List<AdaptationSetType> adapts = toGet.getMPDInfo().getPeriod().get(0).getAdaptationSet();
			for (AdaptationSetType adapt : adapts) {
				StreamType type = null;
				if (adapt.getMimeType().contains("video")) {
					type = StreamType.VIDEO;
				} else if (adapt.getMimeType().contains("audio")) {
					type = StreamType.AUDIO;
				} else {
					try {
						throw new Exception("unknown mime type");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				for (RepresentationType rep : adapt.getRepresentation()) {
					eDashController me = new eDashController();

					List<String> cmds = null;
					try {
						cmds = me.generateEDASHCommand(toGet, rep.getId(), type);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					File repDir = new File("streams/", rep.getId());

					try {
						FileUtils.forceMkdir(repDir);
					} catch (IOException e) {
						e.printStackTrace();
					}

					// start watching
					try {
						mpdUpdater.addMediaDirectory(repDir.toPath(), rep.getId());
					} catch (IOException e1) {

						logger.error("Failure during MPD file updater dir addition", e1);
					}

					logger.info("starting dashing into dir: " + repDir.getName());
					DasherExec runner = new DasherExec(cmds, sc, repDir, me);

					new Thread(runner).start();

				}
			}
		}
	};

	private class DasherExec implements Runnable {

		private List<String> cmds;
		private ShellCallback sc;
		private File repDir;
		private eDashController me;

		public DasherExec(List<String> cmds, ShellCallback sc, File repDir, eDashController me) {
			super();
			this.cmds = cmds;
			this.sc = sc;
			this.repDir = repDir;
			this.me = me;
		}

		boolean running = true;

		@Override
		public void run() {

			try {
				int exit = me.execProcess(cmds, sc, repDir);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

	}

	@Override
	public void start() throws Exception {

	}

	@Override
	public void stop() throws Exception {
		logger.debug("SHUTDOWN LIVECONTENTSOURCE");

	}

}
