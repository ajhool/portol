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

import org.apache.http.client.HttpClient;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.tomcools.dropwizard.websocket.WebsocketBundle;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.JoranException;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.portol.cloudplayer.config.BackboneServiceConfig;
import com.portol.cloudplayer.config.EdgeServerConfig;
import com.portol.cloudplayer.config.LoadbalCommunicatorConfig;
import com.portol.cloudplayer.config.MPDServerConfig;
import com.portol.cloudplayer.repository.LocalPlayerRepository;
import com.portol.cloudplayer.resource.ClientAPIResource;
import com.portol.cloudplayer.resource.ClientSocketAPI;
import com.portol.cloudplayer.resource.CloudKillResource;
import com.portol.cloudplayer.resource.CloudStatusResource;
import com.portol.cloudplayer.resource.MPDResource;
import com.portol.cloudplayer.resource.ModuleAPIResource;
import com.portol.cloudplayer.resource.PlayerAPIResource;
import com.portol.cloudplayer.resource.PlayerLifecycleResource;
import com.portol.cloudplayer.resource.SetContentResource;
import com.portol.cloudplayer.resource.PlayerSocketAPI;
import com.portol.cloudplayer.resource.UserEventResource;
import com.portol.cloudplayer.resource.servlet.CloudServlet;
import com.portol.cloudplayer.service.BackboneService;
import com.portol.cloudplayer.service.ContentScrubberService;
import com.portol.cloudplayer.service.ContentSourceController;
import com.portol.cloudplayer.service.EdgeAsyncResource;
import com.portol.cloudplayer.service.LoadbalCommunicator;
import com.portol.cloudplayer.service.MPDService;
import com.portol.cloudplayer.service.ReturnToQService;
import com.portol.cloudplayer.socket.ClientWSEndpoint;
import com.portol.cloudplayer.socket.PlayerWSEndpoint;
import com.portol.common.model.instance.EdgeInstance;

import io.dropwizard.Application;
import io.dropwizard.client.HttpClientBuilder;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.jersey.jackson.JacksonMessageBodyProvider;

public class EdgeServerApplication extends Application<EdgeServerConfig> {

	private WebsocketBundle websocket = new WebsocketBundle();
	private WebsocketBundle websocketClient = new WebsocketBundle();

	public static final String NAME = "EdgeServer";
	final static Logger logger = LoggerFactory.getLogger(EdgeServerApplication.class);

	public static void main(String[] args) throws Exception {
		new EdgeServerApplication().run(args);
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void initialize(Bootstrap<EdgeServerConfig> bootstrap) {
		super.initialize(bootstrap);
		bootstrap.addBundle(websocket);
		bootstrap.addBundle(websocketClient);
	}

	static void reset() throws JoranException {
		ILoggerFactory factory = LoggerFactory.getILoggerFactory();
		LoggerContext context = (LoggerContext) factory;
		context.reset();
		ContextInitializer initializer = new ContextInitializer(context);
		initializer.autoConfig(); // load logback.xml or .groovy from classpath
	}

	private void configureCors(Environment environment) {
		Dynamic filter = environment.servlets().addFilter("CORS", CrossOriginFilter.class);
		filter.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
		filter.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "GET,PUT,POST,DELETE,OPTIONS");
		filter.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
		filter.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "*");
		filter.setInitParameter("allowedHeaders",
				"Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin");
		filter.setInitParameter("allowCredentials", "true");
	}

	private ContentSourceController getCSC(EdgeServerConfig config, Environment environment) {
		ContentSourceController remoteData = new ContentSourceController(config, environment);
		logger.info("Content source manager created");
		return remoteData;
	}

	private LocalPlayerRepository getLocalPlayerRepo(EdgeServerConfig config, Environment environment) {
		LocalPlayerRepository locals = new LocalPlayerRepository();
		return locals;
	}

	private LoadbalCommunicator getLBcomm(EdgeServerConfig config, Environment environment)
			throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

		logger.info("creating loadbal communicator client...");

		LoadbalCommunicatorConfig thisConfig = config.getLoadbalConfig();

		final Client jClient = new JerseyClientBuilder(environment).using(config.getJerseyClientConfiguration())
				.build("loadbalCommunicator");
		LoadbalCommunicator svcLB = new LoadbalCommunicator(jClient, thisConfig.keepAlivePath, thisConfig.bootPath,
				thisConfig.port, thisConfig.eventPath, thisConfig.loadbalURL);

		logger.info("loadbal communicator configured successfully");
		return svcLB;

	}

	private MPDService getMPDService(EdgeServerConfig config, Environment environment) {
		logger.info("creating mpd communicator client...");

		MPDServerConfig thisConfig = config.getMPDConfig();

		final Client jClient = new JerseyClientBuilder(environment).using(config.getJerseyClientConfiguration())
				.build("MPDService");
		MPDService svcMPD = new MPDService(jClient, thisConfig);

		logger.info("mpd communicator configured successfully");
		return svcMPD;
	}

	public static interface MixIn {
		@JsonIgnore
		public void setYear(BigInteger year);
	}

	private BackboneService getBBSvc(EdgeServerConfig config, Environment environment, MPDService mpdSvc) {
		logger.info("creating new backbone downloader...");
		// httpclient for gets
		final HttpClient httpClient = new HttpClientBuilder(environment).using(config.getHttpClientConfiguration())
				.build("backbone_service_http");

		// client for api calls
		final Client jClient = new JerseyClientBuilder(environment).using(config.getJerseyClientConfiguration())
				.build("backbone_service_jersey");

		BackboneServiceConfig thisConfig = config.getBackboneServiceConfig();

		BackboneService bbSvc = new BackboneService(thisConfig, httpClient, jClient, mpdSvc);

		environment.lifecycle().manage(bbSvc);
		logger.info("created new backbone downloader...");
		return bbSvc;

	}

	private ReturnToQService getReturnToQService(EdgeServerConfig config, Environment environment,
			LoadbalCommunicator comm, EdgeInstance _this) {
		// TODO add to config
		final int MAX_IDLE_SEC = 1800;

		ReturnToQService returnSvc = new ReturnToQService(comm, MAX_IDLE_SEC, _this);

		return returnSvc;
	}

	public ContentScrubberService startContentScrubberSvc(EdgeServerConfig config, Environment environment) {
		ContentScrubberService manager = new ContentScrubberService(config.getScrubberConf());
		environment.lifecycle().manage(manager);

		logger.info("ContentScrubberService initialized");
		return manager;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void run(EdgeServerConfig config, Environment environment) throws Exception {
		reset();
		configureCors(environment);

		ObjectMapper mapper = new ObjectMapper();
		mapper.addMixInAnnotations(XMLGregorianCalendar.class, MixIn.class);
		environment.jersey().register(new JacksonMessageBodyProvider(mapper, environment.getValidator()));
		MPDService mpdSvc = this.getMPDService(config, environment);
		EdgeInstance _this = new EdgeInstance();

		LocalPlayerRepository localPlayers = getLocalPlayerRepo(config, environment);
		ContentSourceController remoteData = getCSC(config, environment);
		LoadbalCommunicator lbComm = this.getLBcomm(config, environment);
		BackboneService bbsvc = this.getBBSvc(config, environment, mpdSvc);
		ContentScrubberService scrubber = this.startContentScrubberSvc(config, environment);
		ReturnToQService returnSvc = this.getReturnToQService(config, environment, lbComm, _this);

		CloudKillResource killResource = new CloudKillResource(_this);

		PlayerWSEndpoint playerWS = new PlayerWSEndpoint();
		ClientWSEndpoint clientWS = new ClientWSEndpoint();

		final Client jClient = new JerseyClientBuilder(environment).using(config.getJerseyClientConfiguration())
				.build(getName());

		CloudServlet servlet = new CloudServlet("/assets", "/assets", "index.html", Charsets.UTF_8, jClient,
				localPlayers, _this, returnSvc);

		environment.servlets().addServlet("assets", servlet).addMapping("/assets/*");

		SetContentResource contentResource = new SetContentResource(remoteData, lbComm, _this, bbsvc, returnSvc,
				scrubber);

		CloudStatusResource cloudStatus = new CloudStatusResource(_this);
		PlayerSocketAPI wsRes = new PlayerSocketAPI(environment.getObjectMapper(), localPlayers, returnSvc, playerWS);
		ClientSocketAPI wsClientRes = new ClientSocketAPI(environment.getObjectMapper(), localPlayers, clientWS);
		ClientAPIResource clientRes = new ClientAPIResource(localPlayers, null, contentResource, wsRes, null, _this);
		PlayerAPIResource playerRes = new PlayerAPIResource(localPlayers, contentResource, wsClientRes, _this);
		MPDResource mpdRes = new MPDResource(_this, mpdSvc, localPlayers);
		ModuleAPIResource modApiRes = new ModuleAPIResource(_this, localPlayers);
		PlayerLifecycleResource lifeCycleRes = new PlayerLifecycleResource(wsRes, lbComm, _this, localPlayers,
				contentResource);
		EdgeAsyncResource async = new EdgeAsyncResource(environment.getObjectMapper(), clientRes, playerRes, clientWS,
				playerWS, localPlayers, lbComm, _this);
		UserEventResource userRes = new UserEventResource(wsRes);

		environment.lifecycle().manage(async);

		environment.jersey().register(userRes);
		environment.jersey().register(cloudStatus);
		environment.jersey().register(playerRes);
		environment.jersey().register(modApiRes);
		environment.jersey().register(mpdRes);
		environment.jersey().register(clientRes);
		environment.jersey().register(lifeCycleRes);
		environment.jersey().register(contentResource);
		environment.jersey().register(killResource);

		websocket.addEndpoint(PlayerWSEndpoint.class);
		websocketClient.addEndpoint(ClientWSEndpoint.class);

	}

}
