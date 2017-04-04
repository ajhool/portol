package com.portol.cloudplayer.resource;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.slf4j.LoggerFactory;







import com.codahale.metrics.annotation.Timed;
import com.portol.cloudplayer.service.BackboneService;
import com.portol.cloudplayer.service.ContentScrubberService;
import com.portol.cloudplayer.service.ContentSourceController;
import com.portol.cloudplayer.service.LoadbalCommunicator;
import com.portol.cloudplayer.service.ReturnToQService;
import com.portol.common.model.CloudCommand;
import com.portol.common.model.CloudCommand.Type;
import com.portol.common.model.content.Content;
import com.portol.common.model.instance.BackendInstance;
import com.portol.common.model.instance.EdgeInstance;
import com.portol.common.model.instance.Instance.Status;

@Path("/api/v0/setcontent")
public class SetContentResource {

	private static final org.slf4j.Logger logger = LoggerFactory
			.getLogger(SetContentResource.class);
	private ContentSourceController remoteDataController;
	private LoadbalCommunicator lbComm;
	private EdgeInstance _this;

	private BackboneService backbonesvc;
	private ReturnToQService returnsvc;
	private ContentScrubberService scrubber;

	@Context HttpServletResponse resp;

	public SetContentResource(ContentSourceController remoteDataCSC,
			LoadbalCommunicator lbComm, EdgeInstance me,
			BackboneService bbsvc, ReturnToQService returnSvc, ContentScrubberService scrubber) {
		this.remoteDataController = remoteDataCSC;
		this.lbComm = lbComm;
		this.scrubber = scrubber;
		this._this = me;

		this.backbonesvc = bbsvc;
		this.returnsvc = returnSvc;

		try {
			logger.debug("*****State of cloud before report to loadbal: ");
			logger.debug(_this.toString());
			logger.debug("Reporting to loadbal... ");
			EdgeInstance newState = lbComm.reportBooted(_this);

			_this.setAdminKey(newState.getAdminKey());
			_this.setApiId(newState.getApiId());
			_this.setBootTime(newState.getBootTime());
			_this.setData_cap_mb(newState.getData_cap_mb());
			_this.setHost_dns(newState.getHost_dns());
			_this.setId(newState.getId());
			_this.setLocation(newState.getLocation());


			logger.debug("******Reported into loadbal. New Cloud state returned:" );
			logger.debug(_this.toString());
		} catch (Exception e) {
			// TODO handle this better
			logger.warn("connection to load balancer refused");
		}
	}




	public boolean upgradeContent() throws Exception {
		
		if(_this.getServing() == null){
			logger.error("Error - attempting to upgrade non-existent content!");
		return false;
		}
		CloudCommand faked = new CloudCommand();
		faked.setApiToken("bar");
		faked.setContent(_this.getServing());
		faked.setType(Type.SET_CONTENT_ALL);
		
		this.setContent(faked);
		return true;
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

		//start the clock
		returnsvc.activateTimeout();
		Content target = toLoad.getContent();


		switch(toLoad.getType()){

		case SET_CONTENT_ALL:
			return setContentImpl(target, toLoad.getSource());
		case SET_CONTENT_PREVIEW:
			return setPreviewContentImpl(target, toLoad.getSource());
		case START_LIVE_BACKEND:
			return setContentImpl(target, toLoad.getSource());

		default:
			resp.sendError(403, "Error - invalid command for this endpoint");
			return null; 

		}

	}

	public Content setPreviewContentImpl(Content target, BackendInstance src) throws InterruptedException{

		if(_this.getServing() != null){
			if ( !target.getId().equalsIgnoreCase(_this.getServing().getId())) {
				logger.info("set content request for new content. Scrubbing old files...");

				ReturnToQService.scrubAllFiles();

			}
		}

		try {
			backbonesvc.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}

		//we want to keep our content!
		scrubber.disable();

		boolean exists = false;
		try {
			if(_this.getServing().getId().equalsIgnoreCase(target.getId())){
				exists = true;
			}
		} catch (Exception e){
			exists = false;
		}

		if(exists){
			logger.info("content already downloaded here, skipping re-download process");
			return target;
		}
		String dbid = null;
		if(!remoteDataController.isPreviewHandled(target)){
		// phase 1: init content repo
		 dbid = remoteDataController
				.prepRemoteDB(target);

		// phase 2: begin download

		remoteDataController.saveContent(dbid, true);

		_this.setServing(target);
		_this.setLocalSource(src);
		_this.setStatus(Status.PREVIEW);
		} else {
			//we've already handled it
			 dbid = target.getId();
		}
		return remoteDataController.getLastSaved(dbid);
	
	}


	public Content setContentImpl(Content target, BackendInstance src) throws InterruptedException{

		if(_this.getServing() != null){
			if ( !target.getId().equalsIgnoreCase(_this.getServing().getId())) {
				logger.info("set content request for new content. Scrubbing old files...");

				ReturnToQService.scrubAllFiles();

			}
		}

		try {
			backbonesvc.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// operate differently based on whether we are serving
		if (src != null
				&& target.getType() == Content.Type.LIVE) {
			scrubber.enable();
			try {
				backbonesvc.start();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// get live content
			backbonesvc.beginLiveDownload(target, src);

			_this.setServing(target);
			_this.setLocalSource(src);
			_this.setStatus(Status.RUNNING);
			return backbonesvc.getCurrentContent();

		} else { // VOD, use pre-exisiting segment downloading infastructure

		
			scrubber.disable();

			boolean exists = false;
			try {
				if(_this.getServing().getId().equalsIgnoreCase(target.getId()) && _this.getStatus() != Status.PREVIEW){
					exists = true;
				}
			} catch (Exception e){
				exists = false;
			}

			if(exists){
				logger.info("content already downloaded here, skipping re-download process");
				return target;
			}

			String dbid = null;
			if(!remoteDataController.isMainHandled(target)){
				
			
			// phase 1: init content repo
		 dbid = remoteDataController
					.prepRemoteDB(target);

			// phase 2: begin download


			remoteDataController.saveContent(dbid, false);

			_this.setServing(target);
			//_this.setLocalSource(src);
			_this.setStatus(Status.RUNNING);
			} else {
				//we've already handled it
				 dbid = target.getId();
			}
			return remoteDataController.getLastSaved(dbid);
		}
	}



	private boolean validate(String apiToken) {
		if (apiToken.equalsIgnoreCase("bar")) {
			return true;
		} else
			return false;
	}

}
