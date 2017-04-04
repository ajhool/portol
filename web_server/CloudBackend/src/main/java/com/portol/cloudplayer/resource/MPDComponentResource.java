package com.portol.cloudplayer.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.codahale.metrics.annotation.Timed;
import com.google.common.io.Files;
import com.portol.cloudplayer.Marshal;
import com.portol.cloudplayer.repository.LocalPlayerRepository;
import com.portol.cloudplayer.service.MPDUpdaterService;
import com.portol.common.model.CloudCommand;
import com.portol.common.model.ComponentRequest;
import com.portol.common.model.dash.jaxb.MPDtype;
import com.portol.common.model.dash.jaxb.SegmentTimelineType;
import com.portol.common.model.instance.BackendInstance;
import com.portol.common.model.instance.EdgeInstance;
import com.portol.common.model.player.Player;

@Path("/api/v0/component")
public class MPDComponentResource {

	private LocalPlayerRepository localPlayerRepo;
	private BackendInstance _this;
	private MPDUpdaterService mpdUpdater;

	private static final Logger logger = LoggerFactory.getLogger(MPDComponentResource.class);

	public MPDComponentResource(BackendInstance _this2, MPDUpdaterService updaterSvc) {
		this._this = _this2;
		this.mpdUpdater = updaterSvc;

	}

	@POST
	@Path("/timeline")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed
	public SegmentTimelineType sendTimeline(ComponentRequest wantsTimeline) throws SAXException, IOException,
			ParserConfigurationException, DatatypeConfigurationException, InterruptedException {

		MPDtype MPD = this.mpdUpdater.getMPDfor(wantsTimeline.getRepId());

		if (MPD == null) {
			logger.error("local MPD updater service returned null MPD");
			return null;
		}

		SegmentTimelineType times = MPD.getPeriod().get(0).getAdaptationSet().get(0).getSegmentTemplate()
				.getSegmentTimeline();

		return times;
	}

	@POST
	@Path("/availabilityStart")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed
	public String sendstartTime(ComponentRequest wantsStart) throws SAXException, IOException,
			ParserConfigurationException, DatatypeConfigurationException, InterruptedException {

		MPDtype MPD = this.mpdUpdater.getMPDfor(wantsStart.getRepId());

		if (MPD == null) {
			logger.error("local MPD updater service returned null MPD");
			return null;
		}
		this.mpdUpdater.getMPDfor(wantsStart.getRepId());
		XMLGregorianCalendar startTime = MPD.getAvailabilityStartTime();

		return startTime.toXMLFormat();

	}

	private boolean validate(String apiToken) {
		if (apiToken.equalsIgnoreCase("REDACTED")) {
			return true;
		} else
			return false;
	}

}
