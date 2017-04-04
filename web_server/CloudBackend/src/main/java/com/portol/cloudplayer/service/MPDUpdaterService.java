package com.portol.cloudplayer.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchService;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;

import com.portol.cloudplayer.Marshal;
import com.portol.common.model.dash.jaxb.MPDtype;
import com.portol.common.model.dash.jaxb.SegmentTimelineType;
import com.portol.common.model.dash.jaxb.SegmentTimelineType.S;

import java.io.File;

public class MPDUpdaterService {

	private ConcurrentHashMap<String, MPDtype> MPDs;
	private static Object lock = new Object();
	private static final String STABLE_MPD_NAME = "live_stable.mpd";

	// wont touch it until 150 MS have passed since the packager wrote it
	private static final long MPD_MINIMUM_AGE = 150;

	public static final Logger logger = LoggerFactory.getLogger(MPDUpdaterService.class);

	public MPDUpdaterService() {
		super();
		if (MPDs == null) {
			MPDs = new ConcurrentHashMap<String, MPDtype>();
		}

	}

	public boolean addMediaDirectory(Path directoryToWatch, String repId) throws IOException {

		// make a new watch service that we can register interest in
		// directories and files with.
		WatchService myWatcher = directoryToWatch.getFileSystem().newWatchService();

		// start the file watcher thread below
		MyWatchQueueReader fileWatcher = new MyWatchQueueReader(myWatcher, this, repId);
		Thread th = new Thread(fileWatcher, "FileWatcher");
		th.start();
		// register a file
		directoryToWatch.register(myWatcher, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);

		return true;
	}

	private synchronized MPDtype getSafeMPD(String repToGet) throws InterruptedException, IOException {
		boolean success = false;

		while (!success) {
			MPDtype MPD = null;
			File requested = null;

			requested = new File("streams/" + repToGet + "/live.mpd");

			// live file exists, and hasnt been taken

			long delta = Math.abs(System.currentTimeMillis() - requested.lastModified());

			if (delta < MPD_MINIMUM_AGE) {

				logger.debug("MPD wasn't old enough, delta is: " + delta);
				Thread.sleep(75);
				continue;
			}

			FileInputStream fisTargetFile = null;

			try {
				fisTargetFile = new FileInputStream(requested);
			} catch (FileNotFoundException e1) {

				logger.error("error opening file input stream", e1);
			}

			String targetFileStr = null;

			try {
				targetFileStr = IOUtils.toString(fisTargetFile, "UTF-8");
			} catch (IOException e1) {
				logger.error("error using ioutils", e1);
			}

			try {
				MPD = Marshal.parseMPD(targetFileStr);
				success = true;
			} catch (org.xml.sax.SAXParseException e) {
				logger.warn("Premature end of file, trying again...", e);
				success = false;
			} catch (SAXException e) {
				logger.error("SAX exception generated, trying again", e);
				success = false;
			} catch (IOException e) {
				logger.error("IO excpetion generated, trying again", e);
				success = false;
			} catch (ParserConfigurationException e) {
				logger.error("parse config exception, trying again", e);
				success = false;
			} catch (DatatypeConfigurationException e) {
				logger.error("data type configuration, trying again", e);
				success = false;
			}

			try {
				fisTargetFile.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (!success) {
				Thread.sleep(100);
			} else {

				MPDtype existing = MPDs.get(repToGet);

				if (existing == null) {
					MPDs.put(repToGet, MPD);
					break;
				} else {
					// update MPD

					SegmentTimelineType existingTimeline = existing.getPeriod().get(0).getAdaptationSet().get(0)
							.getSegmentTemplate().getSegmentTimeline();
					SegmentTimelineType newTimeline = MPD.getPeriod().get(0).getAdaptationSet().get(0)
							.getSegmentTemplate().getSegmentTimeline();

					if (newTimeline.getS().get(newTimeline.getS().size() - 1)
							.getT() > (existingTimeline.getS().get(existingTimeline.getS().size() - 1).getT())) {
						// new timeline has later piece, add it to existing MPD
						existingTimeline.getS().add(newTimeline.getS().get(newTimeline.getS().size() - 1));

						S newest = existingTimeline.getS().get(existingTimeline.getS().size() - 1);

					}

					File parent = new File(requested.getParent());
					boolean verified = verifyTimeline(parent, existingTimeline, repToGet);

					// check out file ordering
					if (!verified) {
						logger.info("attempting repair of manifest");
						logger.info("existingTimeline before repair:");

						StringBuffer buf = new StringBuffer();

						for (int i = 0; i < existingTimeline.getS().size(); i++) {
							S atFocus = existingTimeline.getS().get(i);
							buf.append("element " + i + ": <S t=\"" + atFocus.getT() + "\" d=\"" + atFocus.getD()
									+ "\" />\n");

						}

						logger.warn(buf.toString());

						for (int i = newTimeline.getS().size() - 1; i >= 0; i--) {
							List<S> newSList = this.insertIntoS(newTimeline.getS().get(i), existingTimeline.getS());

							existingTimeline.setS(newSList);
						}

						logger.info("Repairs completed. new state of segmentTimeline:");
						buf = new StringBuffer();
						for (int i = 0; i < existingTimeline.getS().size(); i++) {
							S atFocus = existingTimeline.getS().get(i);
							buf.append("element " + i + ": <S t=\"" + atFocus.getT() + "\" d=\"" + atFocus.getD()
									+ "\" />\n");

						}

						logger.warn(buf.toString());

					}

					existing.getPeriod().get(0).getAdaptationSet().get(0).getSegmentTemplate()
							.setSegmentTimeline(existingTimeline);
					MPDs.put(repToGet, existing);
					break;
				}
			}

		}

		return MPDs.get(repToGet);

	}

	private List<S> insertIntoS(S toAdd, List<S> s) {

		// find S that is larger
		int index = 0;
		S higher = null;

		for (S possibleMatch : s) {
			if (possibleMatch.getT() == toAdd.getT()) {
				return s;
			}
		}

		// at this point, whe know it is missing

		while (index < s.size()) {

			if (s.get(index).getT() > toAdd.getT()) {
				logger.info("first larger item found");
				higher = s.get(index);
				break;
			}

			if (index == s.size() - 1) {
				logger.info("inserting into end of list");
				higher = s.get(index);
				break;
			}

			index++;

		}

		// store in timeline
		s.add(index, toAdd);

		return s;
	}

	public boolean verifyTimeline(File parentDir, SegmentTimelineType assumedComplete, String rep) throws IOException {

		boolean matchingSize = false;
		for (int i = 0; i < 3; i++) {
			List<File> files = (List<File>) FileUtils.listFiles(parentDir, new String[] { "mp4" }, false);

			if (assumedComplete.getS().size() == (files.size() - 1)) {
				matchingSize = true;
				logger.info("correct size match found for " + rep + " try number: " + (i + 1));
				break;
			}
			logger.info("Size mismatch for: " + rep + ", try number: " + (i + 1));
			try {
				Thread.sleep(600);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		List<File> files = null;
		if (matchingSize) {
			logger.info("number of files in dir matches number of segs in fimeline for rep: " + rep);
			return true;
		} else {
			files = (List<File>) FileUtils.listFiles(parentDir, new String[] { "mp4" }, false);
			logger.warn("size mismatch between number of MPD files and segment length for rep: " + rep);
			logger.warn("there are " + files.size() + "MP4 files in the dir, but " + assumedComplete.getS().size()
					+ " segments in the timeline");
		}

		File currentMPD = new File(parentDir, "live.mpd");

		if (currentMPD.exists()) {
			String toPrint = new String(Files.readAllBytes(currentMPD.toPath()));

			logger.info("Contents of original MPD file:");
			logger.info(toPrint + "\n\n");

		}

		// compare each one to every time
		for (File cur : files) {
			if (cur.getName().contains("init"))
				continue;
			boolean match = false;
			String name = cur.getName();
			String time = name.substring(name.lastIndexOf("-") + 1, name.lastIndexOf("."));
			BigInteger toMatch = new BigInteger(time);
			logger.info("searching for data for file: " + cur.getName());

			for (S s : assumedComplete.getS()) {
				if (s.getT() == toMatch.longValue()) {
					match = true;
					logger.info(
							"match found for file: " + cur.getName() + " with timeline segment @ time: " + s.getT());
					break;
				}
			}

			if (!match) {

				logger.warn("no timeline element found for file: " + cur.getName());
				logger.warn("actual contents of current mpd file:\n");

				StringBuffer buf = new StringBuffer();

				for (int i = 0; i < assumedComplete.getS().size(); i++) {
					S atFocus = assumedComplete.getS().get(i);
					buf.append("element " + i + ": <S t=\"" + atFocus.getT() + "\" d=\"" + atFocus.getD() + "\" />\n");

				}

				logger.warn(buf.toString());
				return false;
			}

		}
		return true;

	}

	public void fileEvent(String repId) {
		try {
			this.getSafeMPD(repId);
		} catch (IOException | InterruptedException e) {

			logger.error(" error while getting mpd for rep: " + repId, e);
		}

	}

	public MPDtype getMPDfor(String repId) {
		return this.MPDs.get(repId);
	}
}
