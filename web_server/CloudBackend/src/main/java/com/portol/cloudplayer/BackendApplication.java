package com.portol.cloudplayer;

import java.math.BigInteger;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration.Dynamic;
import javax.ws.rs.client.Client;
import javax.xml.datatype.XMLGregorianCalendar;

import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.portol.backend.cloud.service.LiveContentDownloaderService;
import com.portol.cloudplayer.config.BackendServerConfig;
import com.portol.cloudplayer.config.LiveContentDownloaderConfig;
import com.portol.cloudplayer.config.LoadbalCommunicatorConfig;
import com.portol.cloudplayer.repository.LocalPlayerRepository;
import com.portol.cloudplayer.resource.AddEdgeResource;
import com.portol.cloudplayer.resource.BackendDataServlet;
import com.portol.cloudplayer.resource.DeleteEdgeResource;
import com.portol.cloudplayer.resource.FileInfoResource;
import com.portol.cloudplayer.resource.MPDComponentResource;
import com.portol.cloudplayer.resource.SetContentResource;
import com.portol.cloudplayer.service.ContentScrubberService;
import com.portol.cloudplayer.service.ContentSourceController;
import com.portol.cloudplayer.service.LoadbalCommunicator;
import com.portol.cloudplayer.service.MPDUpdaterService;
import com.portol.cloudplayer.service.ReturnToQService;
import com.portol.common.model.instance.BackendInstance;

import io.dropwizard.Application;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.jersey.jackson.JacksonMessageBodyProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class BackendApplication extends Application<BackendServerConfig> {

	public static final String NAME = "BackendServer";
	final static Logger logger = LoggerFactory
			.getLogger(BackendApplication.class);


	public static void main(String[] args) throws Exception {
		new BackendApplication().run(args);
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void initialize(Bootstrap<BackendServerConfig> bootstrap) {
	}

	private void configureCors(Environment environment) {
		Dynamic filter = environment.servlets().addFilter("CORS", CrossOriginFilter.class);
		filter.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
		filter.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "GET,PUT,POST,DELETE,OPTIONS");
		filter.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
		filter.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "*");
		filter.setInitParameter("allowedHeaders", "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin");
		filter.setInitParameter("allowCredentials", "true");
	}

	private ContentSourceController getCSC(BackendServerConfig config, Environment environment){
		ContentSourceController remoteData = new ContentSourceController(config, environment);
		logger.info("Content source manager created");
		return remoteData;
	}

	private LocalPlayerRepository getLocalPlayerRepo(BackendServerConfig config, Environment environment){
		LocalPlayerRepository locals = new LocalPlayerRepository();
		return locals;
	}

	private LoadbalCommunicator getLBcomm(BackendServerConfig config, Environment environment) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException{

		
		logger.info("creating loadbal communicator client...");

		LoadbalCommunicatorConfig thisConfig = config.getLoadbalConfig();

		final Client jClient = new JerseyClientBuilder(environment).using(
				config.getJerseyClientConfiguration()).build("loadbalCommunicator");
		LoadbalCommunicator svcLB = new LoadbalCommunicator(jClient, thisConfig.keepAlivePath, thisConfig.bootPath, thisConfig.port, thisConfig.eventPath, thisConfig.loadbalURL);

		logger.info("loadbal communicator configured successfully");
		return svcLB;

	}
	
	
	public static interface MixIn {
        @JsonIgnore
        public void setYear(BigInteger year);
    }
	
	private LiveContentDownloaderService getLCDS(BackendServerConfig config, Environment environment, MPDUpdaterService mpdUpdater){

		logger.info("creating new live content downloader...");

		LiveContentDownloaderConfig liveConf = config.getLiveContentDownloaderConfig();

		LiveContentDownloaderService liveDL = new LiveContentDownloaderService(liveConf, mpdUpdater);
		logger.info("New live content downloader created successfully.");

		environment.lifecycle().manage(liveDL);
		return liveDL;
	
}
	

	private ReturnToQService getReturnToQService(BackendServerConfig config, Environment environment, LoadbalCommunicator comm, BackendInstance _this){
		final int MAX_IDLE_SEC = 3600;
		
		ReturnToQService returnSvc = new ReturnToQService(comm, MAX_IDLE_SEC, _this);
		
		return returnSvc;
	}
	
	public ContentScrubberService startContentScrubberSvc(BackendServerConfig config, Environment environment) {
		ContentScrubberService manager = new ContentScrubberService(config.getScrubberConf());
		environment.lifecycle().manage(manager);

		logger.info("ContentScrubberService initialized");
		return manager;
	}
	
	
	@SuppressWarnings("deprecation")
	@Override
	public void run(BackendServerConfig config, Environment environment)
			throws Exception {

		configureCors(environment);

		ObjectMapper mapper = new ObjectMapper();
		mapper.addMixInAnnotations(XMLGregorianCalendar.class, MixIn.class);
		environment.jersey().register(new JacksonMessageBodyProvider(mapper, environment.getValidator()));
		
		
		MPDUpdaterService mpdUpdater = new MPDUpdaterService();
		LiveContentDownloaderService liveContentDL = this.getLCDS(config, environment, mpdUpdater);

		BackendInstance _this = new BackendInstance();

		LocalPlayerRepository localPlayers = getLocalPlayerRepo(config, environment);
		ContentSourceController remoteData = getCSC(config, environment);
		LoadbalCommunicator lbComm = this.getLBcomm(config, environment);
		ContentScrubberService scrubber = this.startContentScrubberSvc(config, environment);
		ReturnToQService returnSvc = this.getReturnToQService(config, environment, lbComm, _this);
		
		
		

		final Client jClient = new JerseyClientBuilder(environment).using(
				config.getJerseyClientConfiguration()).build(getName());

		BackendDataServlet Bservlet = new BackendDataServlet("/streams", "/streams", "index.html", Charsets.UTF_8, jClient, _this, liveContentDL, returnSvc);
		
		environment.servlets().addServlet("streams", Bservlet).addMapping("/streams/*");
		
		FileInfoResource liveEdge = new FileInfoResource();
		AddEdgeResource playerResource = new AddEdgeResource(localPlayers, _this);
		SetContentResource contentResource = new SetContentResource(remoteData, lbComm, _this, liveContentDL, returnSvc, scrubber, mpdUpdater);
		DeleteEdgeResource deleteResource = new DeleteEdgeResource(localPlayers, _this);
		MPDComponentResource mpdCompRes = new MPDComponentResource(_this, mpdUpdater);
		
		environment.jersey().register(mpdCompRes);
		environment.jersey().register(playerResource);
		environment.jersey().register(contentResource);
		environment.jersey().register(deleteResource);
		environment.jersey().register(liveEdge);

	}

	

}
