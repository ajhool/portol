package com.portol.cloudplayer.resource;

import java.io.File;
import java.util.Collection;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement.ConstrainedElementKind;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

import com.codahale.metrics.annotation.Timed;
import com.portol.backend.cloud.service.LiveContentDownloaderService;
import com.portol.cloudplayer.service.ContentScrubberService;
import com.portol.cloudplayer.service.ContentSourceController;
import com.portol.cloudplayer.service.LoadbalCommunicator;
import com.portol.cloudplayer.service.MPDUpdaterService;
import com.portol.cloudplayer.service.ReturnToQService;
import com.portol.common.model.CloudCommand;
import com.portol.common.model.content.Content;
import com.portol.common.model.content.Content.Type;
import com.portol.common.model.content.ContentSetRequest;
import com.portol.common.model.content.ContentSource;
import com.portol.common.model.instance.BackendInstance;
import com.portol.common.model.instance.EdgeInstance;

@Path("/api/v0/setcontent")
public class SetContentResource {

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SetContentResource.class);
	private ContentSourceController remoteDataController;
	private LoadbalCommunicator lbComm;
	private BackendInstance _this;
	private LiveContentDownloaderService liveContentDL;

	private ReturnToQService returnsvc;
	private ContentScrubberService scrubber;
	private MPDUpdaterService mpdUpdatr;

	public SetContentResource(ContentSourceController remoteDataCSC, LoadbalCommunicator lbComm, BackendInstance me,
			LiveContentDownloaderService liveContentDL, ReturnToQService returnSvc, ContentScrubberService scrubber,
			MPDUpdaterService mpdUpdatr) {
		this.remoteDataController = remoteDataCSC;
		this.lbComm = lbComm;
		this.scrubber = scrubber;
		this._this = me;
		this.liveContentDL = liveContentDL;

		this.returnsvc = returnSvc;
		this.mpdUpdatr = mpdUpdatr;

		try {
			logger.debug("*****State of cloud before report to loadbal: ");
			logger.debug(_this.toString());
			logger.debug("Reporting to loadbal... ");
			BackendInstance newState = lbComm.reportBooted(_this);

			_this.setAdminKey(newState.getAdminKey());
			_this.setApiId(newState.getApiId());
			_this.setBootTime(newState.getBootTime());
			_this.setHost_dns(newState.getHost_dns());
			_this.setId(newState.getId());
			_this.setLocation(newState.getLocation());

			logger.debug("******Reported into loadbal. New Cloud state returned:");
			logger.debug(_this.toString());
		} catch (Exception e) {

			logger.warn("connection to load balancer refused");
		}
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/live")
	@Timed
	public BackendInstance setLiveContent(CloudCommand toLoad) throws Exception {

		if (toLoad == null) {
			throw new BadRequestException();
		}

		if (!validate(toLoad.getApiToken())) {
			throw new javax.ws.rs.ForbiddenException("API key invalid");
		}

		// always want to start with fresh files on live backbone
		ReturnToQService.scrubAllFiles();

		// start the clock
		returnsvc.activateTimeout();

		Content toGet = toLoad.getContent();

		// start download
		liveContentDL.startLiveDownload(toGet);

		scrubber.enable();

		_this.setProtocol("http://");

		_this.setServing(toGet);

		return _this;
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed
	public Content setContent(CloudCommand toLoad) throws Exception {

		if (toLoad == null) {
			throw new BadRequestException();
		}

		if (!validate(toLoad.getApiToken())) {
			throw new javax.ws.rs.ForbiddenException("API key invalid");
		}

		// start the clock
		returnsvc.activateTimeout();
		Content target = toLoad.getContent();
		return setContentImpl(target);
	}

	public Content setContentImpl(Content target) throws Exception {

		// scrub existing files
		if (!target.getId().equalsIgnoreCase(_this.getServing().getId())) {
			logger.info("set content request for new content. Scrubbing old files...");

			ReturnToQService.scrubAllFiles();

		}

		if (target.getType() == Type.LIVE) {
			throw new Exception("cannot set live content at this URL");
		}

		scrubber.disable();

		// phase 1: init content repo
		String dbid = remoteDataController.prepRemoteDB(target);

		// phase 2: begin download

		remoteDataController.saveContent(dbid, false);

		_this.setServing(target);
		return remoteDataController.getLastSaved(dbid);

	}

	private boolean validate(String apiToken) {

		if (apiToken.equalsIgnoreCase("REDACTED")) {
			return true;
		} else
			return false;
	}

}
