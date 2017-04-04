package com.portol.mobileapi;

import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration.Dynamic;
import javax.ws.rs.client.Client;

import org.apache.commons.io.Charsets;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.portol.mobileapi.config.MobileAPIConfig;
import com.portol.mobileapi.healthchecks.MongoHealthCheck;
import com.portol.mobileapi.manager.MongoManaged;
import com.portol.mobileapi.repository.CategoryRepository;
import com.portol.mobileapi.repository.ContentRepository;
import com.portol.mobileapi.repository.MetadataRepository;
import com.portol.mobileapi.repository.PlatformRepository;
import com.portol.mobileapi.repository.PlayerRepository;
import com.portol.mobileapi.repository.UserRepository;
import com.portol.mobileapi.resource.AppAsyncResource;
import com.portol.mobileapi.resource.BookmarkResource;
import com.portol.mobileapi.resource.CategoryResource;
import com.portol.mobileapi.resource.ContentFinderResource;
import com.portol.mobileapi.resource.ContentResource;
import com.portol.mobileapi.resource.NewUserResource;
import com.portol.mobileapi.resource.PlatformResource;
import com.portol.mobileapi.resource.PlayerInfoResource;
//import com.portol.mobileapi.resource.SetVideoResource;
import com.portol.mobileapi.resource.UserLoginResource;
import com.portol.mobileapi.resource.UserLogoutResource;
import com.portol.mobileapi.resource.VideoActionResource;
import com.portol.mobileapi.resource.VideoPaymentResource;
import com.portol.mobileapi.resource.WebServlet;
import com.portol.mobileapi.service.IconService;
import com.portol.mobileapi.service.LoadbalCommunicator;
import com.portol.mobileapi.websocket.JSR356Endpoint;

import be.tomcools.dropwizard.websocket.WebsocketBundle;
import io.dropwizard.Application;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class MobileAPIApp extends Application<MobileAPIConfig> {
	private WebsocketBundle websocket = new WebsocketBundle();
	public static final String NAME = "MobileAPIApp";
	final static Logger logger = LoggerFactory
			.getLogger(MobileAPIApp.class);

	public static void main(String[] args) throws Exception {
		new MobileAPIApp().run(args);
	}

	@Override
	public String getName() {
		return NAME;
	}

	public MongoManaged addCatMongo(MobileAPIConfig config, Environment environment) throws Exception{
		// init category DB
		MongoManaged catMongoManaged = new MongoManaged(config.getCategoryMongoConfiguration());
		environment.lifecycle().manage(catMongoManaged);
		environment.healthChecks().register("MongoCatDBHealthCheck",
				new MongoHealthCheck(catMongoManaged));

		logger.info("Category Database Initialized");
		return catMongoManaged;
	}
	
	@Override
	public void initialize(Bootstrap<MobileAPIConfig> bootstrap) {
		super.initialize(bootstrap);
		bootstrap.addBundle(websocket);
	}

	public MongoManaged addUserMongo(MobileAPIConfig config, Environment environment) throws Exception{
		// init content DB
		MongoManaged userMongoManaged = new MongoManaged(config.getMongoUserConfig());
		environment.lifecycle().manage(userMongoManaged);
		environment.healthChecks().register("MongoUserDBHealthCheck",
				new MongoHealthCheck(userMongoManaged));

		logger.info("User Database Initialized");
		return userMongoManaged;
	}


	public MongoManaged addPlayerMongo(MobileAPIConfig config, Environment environment) throws Exception{
		// init player DB
		MongoManaged playerMongoManaged = new MongoManaged(config.getMongoPlayerConfig());
		environment.lifecycle().manage(playerMongoManaged);
		environment.healthChecks().register("MongoPlayerDBHealthCheck",
				new MongoHealthCheck(playerMongoManaged));
		logger.info("Player Database Initialized");
		return playerMongoManaged;
	}



	public LoadbalCommunicator createLoadbalCommunicator(MobileAPIConfig config, Environment environment){
		logger.info("creating loadbal communicator client...");
		final Client jClient = new JerseyClientBuilder(environment).using(
				config.getJerseyClientConfiguration()).build("loadbal Communicator");
		LoadbalCommunicator comms = new LoadbalCommunicator(config.getLBCommConf(), jClient);

		return comms;
	}

	public MongoManaged addContentMongo(MobileAPIConfig config, Environment environment) throws Exception{
		// init content DB
		MongoManaged contentMongoManaged = new MongoManaged(config.getMongoContentConfiguration());
		environment.lifecycle().manage(contentMongoManaged);
		environment.healthChecks().register("MongoContentDBHealthCheck",
				new MongoHealthCheck(contentMongoManaged));

		logger.info("Content Database Initialized");
		return contentMongoManaged;
	}

	public MongoManaged addSplashMongo(MobileAPIConfig config, Environment environment) throws Exception{
		// init splash screen DB
		MongoManaged splashMongoManaged = new MongoManaged(config.getMongoSplashConfiguration());
		environment.lifecycle().manage(splashMongoManaged);
		environment.healthChecks().register("MongoSplashDBHealthCheck",
				new MongoHealthCheck(splashMongoManaged));
		logger.info("Splash Screen Database Initialized");
		return splashMongoManaged;
	}
	
	
	public MongoManaged addCookieMongo(MobileAPIConfig config,
			Environment environment) throws Exception {
		// init addr db
		MongoManaged addrMongoManaged = new MongoManaged(
				config.getMongoCookieConfiguration());
		environment.lifecycle().manage(addrMongoManaged);
		environment.healthChecks().register("MongoCookieDBHealthCheck",
				new MongoHealthCheck(addrMongoManaged));

		logger.info("Cookie Database Initialized");
		return addrMongoManaged;
	}

	public IconService getIconService(MobileAPIConfig config, Environment environment){
		logger.info("creating icon server client...");
		final Client jClient = new JerseyClientBuilder(environment).using(
				config.getJerseyClientConfiguration()).build("Icon Client");

		IconService icSvc = new IconService(config.getIconServiceConfig(), jClient);

		return icSvc;
	}

	private void configureCors(Environment environment) {
		Dynamic filter = environment.servlets().addFilter("CORS", CrossOriginFilter.class);
		filter.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
		filter.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM,
				"GET,PUT,POST,DELETE,OPTIONS");
		filter.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_METHODS_HEADER, "GET,PUT,POST,DELETE,OPTIONS");
		filter.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
		filter.setInitParameter(
				CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "*");
		filter.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_HEADERS_HEADER,
				"Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin,Cookies");
		filter.setInitParameter("allowCredentials", "true");
		filter.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER, "true");
	}

	@Override
	public void run(MobileAPIConfig config, Environment environment)
			throws Exception {

		configureCors(environment);
		websocket.addEndpoint(JSR356Endpoint.class);
		//environment.jersey().setUrlPattern("/api/*");
		//establish database connections 
		logger.info("Beginning database initialization...");
		MongoManaged userMongoManaged = addUserMongo(config, environment);
		MongoManaged playerMongoManaged = addPlayerMongo(config, environment);
		MongoManaged contentMongoManaged = addContentMongo(config, environment);
		MongoManaged splashMongoManaged = addSplashMongo(config, environment);
		MongoManaged platformMongoManaged = addCookieMongo(config, environment);
		MongoManaged catMongoManaged = addCatMongo(config, environment);
		
		UserRepository userRepo = new UserRepository(userMongoManaged);
		PlayerRepository playerRepo = new PlayerRepository(playerMongoManaged);
		MetadataRepository splashRepo = new MetadataRepository(splashMongoManaged);
		ContentRepository contentRepo = new ContentRepository(contentMongoManaged);
		CategoryRepository catRepo = new CategoryRepository(catMongoManaged);
		PlatformRepository platRepo = new com.portol.mobileapi.repository.PlatformRepository(platformMongoManaged);
		logger.info("All databases initialized");

		LoadbalCommunicator comm = createLoadbalCommunicator(config, environment);
		IconService icSvc = this.getIconService(config, environment);

		AppAsyncResource async = new AppAsyncResource( userRepo, splashRepo, contentRepo);
		environment.lifecycle().manage(async);
		environment.jersey().register(async);
		UserLogoutResource logout = new UserLogoutResource(userRepo, platRepo, playerRepo);
		environment.jersey().register(logout);
		environment.jersey().register(new BookmarkResource(userRepo, contentRepo)); 
		environment.jersey().register(new CategoryResource(catRepo));
		
		NewUserResource nuUser = new NewUserResource(userRepo, icSvc); 
		environment.jersey().register(nuUser);
		environment.jersey().register(new PlayerInfoResource(contentRepo, playerRepo, userRepo));
		environment.jersey().register(new UserLoginResource(userRepo, platRepo, playerRepo, comm, nuUser));
		ContentResource contentRes = new ContentResource(contentRepo, splashRepo);
		environment.jersey().register(contentRes);
		environment.jersey().register(new ContentFinderResource(contentRepo, splashRepo));
		environment.jersey().register(new VideoActionResource(userRepo, playerRepo , comm, contentRepo, splashRepo));
		
		PlatformResource platRes = new PlatformResource(userRepo, platRepo, playerRepo);
		environment.jersey().register(platRes);
		
		
		environment.jersey().register(new VideoPaymentResource(async, userRepo, playerRepo, contentRepo, splashRepo, comm, platRes));
		
		WebServlet servlet = new WebServlet("/www/html", "/", "portolMimic.html", Charsets.UTF_8);
		environment.servlets().addServlet("www", servlet).addMapping("/" + '*');
	
	}

}
