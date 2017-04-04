package com.portol.cloudplayer.runnable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.portol.cloudplayer.service.MPDService;
import com.portol.common.model.content.Content.FileNamingScheme;
import com.portol.common.model.dash.jaxb.AdaptationSetType;
import com.portol.common.model.dash.jaxb.MPDtype;
import com.portol.common.model.dash.jaxb.RepresentationType;
import com.portol.common.model.dash.jaxb.SegmentTemplateType;
import com.portol.common.model.dash.jaxb.SegmentTimelineType;
import com.portol.common.model.dash.jaxb.SegmentTimelineType.S;
import com.portol.common.model.instance.BackendInstance;

public class SegmentGetterThread {

	private static final Logger logger = LoggerFactory.getLogger(SegmentGetterThread.class);
	private HttpClient httpClient;
	private final BackendInstance src;
	private boolean running = false;
	private final MPDtype mpd;
	public static final String PARENT_SERVLET_PATH = "assets/";
	private ArrayList<DownloadSingleRepresentationThread> runners = new ArrayList<DownloadSingleRepresentationThread>();
	private final MPDService mpdsvc;
	private final FileNamingScheme scheme;

	public SegmentGetterThread(HttpClient httpClient, MPDtype contentInfo, MPDService svc, BackendInstance src,
			FileNamingScheme scheme) {
		super();

		this.httpClient = httpClient;
		this.src = src;
		this.mpdsvc = svc;
		this.mpd = contentInfo;
		this.scheme = scheme;
	}

	public class DownloadSingleRepresentationThread implements Runnable {
		private final RepresentationType thisRep;

		public DownloadSingleRepresentationThread(RepresentationType target) {
			this.thisRep = target;

		}

		@Override
		public void run() {
			String id = thisRep.getId();

			logger.info("segment downloader thread started for representation: " + id);

			running = true;
			File parent = new File(PARENT_SERVLET_PATH + id);
			try {
				FileUtils.forceMkdir(parent);
			} catch (IOException e1) {
				logger.error("Mkdir failed", e1);
			}

			final String liveHost = src.getLocation();
			final int port = src.getPort();
			final String protocol = src.getProtocol();
			final String remoteParentPath = src.getContentParent();
			final String extension = scheme.getDataFileExtension();

			// download initial segment for video
			HttpResponse response = null;
			BufferedInputStream is = null;
			BufferedOutputStream bos = null;
			HttpGet get = new HttpGet(protocol + liveHost + ":" + port + remoteParentPath + "/" + id + "/" + id
					+ "_init." + extension + "?apiKey=foo&repId=" + id);
			boolean noInit = true;

			while (noInit) {
				String query = protocol + liveHost + ":" + port + remoteParentPath + "/" + id + "/" + id + "_init."
						+ extension + "?apiKey=foo&repId=" + id;
				logger.debug("Querying: " + query);

				try {
					response = httpClient.execute(get);
				} catch (IOException e) {

					logger.error("Exception caught attempting to fetch: " + query, e);
				}

				if (response != null) {
					if (response.getStatusLine().getStatusCode() == 404) {
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {

							logger.error("init sleeper interrupted", e);
						}
						noInit = true;
						continue;
					} else {
						noInit = false;
					}
				}

				try {
					is = new BufferedInputStream(response.getEntity().getContent());
				} catch (IllegalStateException | IOException e) {

					logger.error("Error reading init entity", e);
				}

				File ofInterest = new File(parent, id + "_init.mp4");
				try {
					bos = new BufferedOutputStream(new FileOutputStream(ofInterest));
				} catch (FileNotFoundException e) {

					logger.error("error opening outputstream to file @ " + ofInterest.getPath(), e);
				}

				int inByte;
				try {
					while ((inByte = is.read()) != -1) {
						bos.write(inByte);
					}

					bos.close();
					is.close();
				} catch (IOException e) {

					logger.error("error writing bytes to init file", e);
				}
			}

			// download
			for (; running == true;) {

				boolean tweaked = false;

				SegmentTimelineType vidTimes = null;
				boolean successful = false;
				int triesLeft = 10;

				while (!successful && triesLeft > 0) {
					try {
						vidTimes = mpdsvc.getSegmentTimeline(thisRep, liveHost);
						logger.info("successfully retreived timeline for rep: " + id + " @ "
								+ new Date(System.currentTimeMillis()));
					} catch (Exception e1) {

						logger.error("error getting remote MPD from backend", e1);
						triesLeft--;

						// chill out and try again later
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					} finally {
						if (vidTimes != null) {
							successful = true;
						}
					}
				}
				S latestRemote = vidTimes.getS().get(vidTimes.getS().size() - 1);

				SegmentTemplateType templ = thisRep.getSegmentTemplate();
				if (templ == null) {
					thisRep.setSegmentTemplate(new SegmentTemplateType());
				}
				templ = thisRep.getSegmentTemplate();

				SegmentTimelineType toCompare = templ.getSegmentTimeline();

				if (toCompare == null) {
					templ.setSegmentTimeline(new SegmentTimelineType());

				}
				toCompare = templ.getSegmentTimeline();

				long nextSeq = 0;

				if (toCompare.getS().size() >= 1) {
					S latestLocal = toCompare.getS().get(toCompare.getS().size() - 1);

					if (latestLocal.getT() < latestRemote.getT()) {
						// we need to get a segment

						if (toCompare.getS().size() == vidTimes.getS().size()) {
							// weird case. make a slow repeat search to find a
							// duplicate if it exists for debugging
							logger.info("local segment timeline and remote both are the same size");

							List<S> loc = toCompare.getS();
							List<S> rem = vidTimes.getS();

							for (int i = 0; i < loc.size(); i++) {
								S focus = loc.get(i);
								for (int j = 0; j < loc.size(); j++) {
									if (j != i) {
										if (focus.getT() == rem.get(j).getT()) {
											logger.warn("duplicate segmenttimeline element found for time: "
													+ focus.getT());

										}
									}
								}
							}

							// vidTimes has a later segment in a list the same
							// size as the local
							toCompare.getS().add(latestRemote);
							tweaked = true;
						} else {
							toCompare.getS().add(vidTimes.getS().get(toCompare.getS().size()));
							tweaked = true;
						}

					} else {
						// they are equal, so chill
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {

							logger.error("segment downloader equality sleeper error, for rep id: " + id, e);
						}

					}
					nextSeq = toCompare.getS().get(toCompare.getS().size() - 1).getT();

				} else {
					nextSeq = vidTimes.getS().get(0).getT();
					toCompare.getS().add(vidTimes.getS().get(0));
					tweaked = true;
				}

				// make call here
				File matching = new File(parent, id + "-" + nextSeq + "." + extension);

				if (!matching.exists()) {
					downloadSingle(protocol, liveHost, port, remoteParentPath, id, Long.toString(nextSeq), extension,
							parent);
				}

				try {
					get.releaseConnection();
					if (!tweaked) {
						long start = System.currentTimeMillis();
						// call file check method

						boolean result = this.downloadMissingFiles(vidTimes, parent, extension, protocol, liveHost,
								port, remoteParentPath, id);

						long end = System.currentTimeMillis();

						long executionTime = end - start;
						logger.info("all files in timeline found, runtime of method: " + executionTime);
						if (executionTime > 1000L) {
							logger.warn("file existence check took very long time, recommend reducing buffer");
						}

						Thread.sleep(Math.max(0, (1000 - executionTime)));
					}
				} catch (InterruptedException e) {

					logger.error("sleeper exception, segment downloader looper thread", e);
				}

			}

		}

		private File downloadSingle(String protocol, String liveHost, int port, String remoteParentPath, String id,
				String nextSeq, String extension, File parent) {
			String reqContents = protocol + liveHost + ":" + port + remoteParentPath + "/" + id + "/" + id + "-"
					+ nextSeq + "." + extension + "?apiKey=foo" + "&repId=" + id;
			HttpGet get = new HttpGet(reqContents);
			HttpResponse response = null;
			BufferedInputStream is = null;
			BufferedOutputStream bos = null;
			File output = null;
			try {
				response = httpClient.execute(get);
			} catch (Exception e) {

				logger.error("error executing HTTP get request for segment: " + reqContents, e);
			}

			if (response.getStatusLine().getStatusCode() != 404) {

				try {
					is = new BufferedInputStream(response.getEntity().getContent());
				} catch (IllegalStateException | IOException e) {

					logger.error("error opening input stream around response entity for rep id: " + id, e);
				}

				output = new File(parent, id + "-" + nextSeq + "." + extension);
				try {

					bos = new BufferedOutputStream(new FileOutputStream(output));
				} catch (FileNotFoundException e) {

					logger.error("error, buffered output creation in segment downloader for rep id: " + id, e);
				}

				int inByte;
				try {
					while ((inByte = is.read()) != -1) {
						bos.write(inByte);
					}

					bos.close();
					is.close();
				} catch (IOException e) {

					logger.error("error wiritng data to file: " + output.getPath() + "for rep: " + id, e);
				}
			}
			return output;
		}

		private boolean downloadMissingFiles(SegmentTimelineType vidTimes, File parent, String extension,
				String protocol, String host, int port, String path, String repId) {

			List<File> files = (List<File>) FileUtils.listFiles(parent, new String[] { "mp4" }, false);

			for (S s : vidTimes.getS()) {

				boolean match = false;

				logger.info("searching for data for timeline element with time: " + s.getT());

				for (File file : files) {
					if (file.getName().contains("init"))
						continue;
					String name = file.getName();
					String time = name.substring(name.lastIndexOf("-") + 1, name.lastIndexOf("."));
					BigInteger toMatch = new BigInteger(time);

					if (s.getT() == toMatch.longValue()) {
						match = true;
						break;
					}
				}

				if (!match) {

					logger.warn("no file found found for element: " + s.getT());
					// download missing file
					this.downloadSingle(protocol, host, port, path, repId, Long.toString(s.getT()), extension, parent);

				}

			}
			return true;
		}

	}

	private ArrayList<Thread> downloaders = new ArrayList<Thread>();

	public void startDownload() {
		List<AdaptationSetType> adaptationSet = mpd.getPeriod().get(0).getAdaptationSet();

		for (AdaptationSetType adapt : adaptationSet) {
			List<RepresentationType> reps = adapt.getRepresentation();
			for (RepresentationType rep : reps) {

				DownloadSingleRepresentationThread downloader = new DownloadSingleRepresentationThread(rep);
				runners.add(downloader);
				Thread temp = new Thread(downloader);
				downloaders.add(temp);

				temp.start();

			}
		}
	}

	public void kill() {
		running = false;
		logger.info("killing off segment downloader");

		for (Thread t : downloaders) {
			t.interrupt();
		}
	}

}
