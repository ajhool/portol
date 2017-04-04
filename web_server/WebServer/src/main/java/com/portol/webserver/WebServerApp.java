package com.portol.webserver;

import java.io.File;
import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration.Dynamic;

import org.apache.commons.io.Charsets;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.portol.webserver.auth.BasicUser;
import com.portol.webserver.auth.SimpleAuthenticator;
import com.portol.webserver.config.WebServerConfig;
import com.portol.webserver.healthchecks.MongoHealthCheck;
import com.portol.webserver.manager.MongoManaged;
import com.portol.webserver.repository.CategoryRepository;
import com.portol.webserver.repository.ContentRepository;
import com.portol.webserver.repository.MetadataRepository;
import com.portol.webserver.repository.UserRepository;
import com.portol.webserver.resource.FaxResource;
import com.portol.webserver.resource.IndexResource;
import com.portol.webserver.resource.WebServlet;

import io.dropwizard.Application;
import io.dropwizard.auth.AuthFactory;
import io.dropwizard.auth.basic.BasicAuthFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class WebServerApp extends Application<WebServerConfig> {

	public static final String NAME = "WebServerApp";
	final static Logger logger = LoggerFactory.getLogger(WebServerApp.class);

	public static void main(String[] args) throws Exception {
		new WebServerApp().run(args);
	}

	@Override
	public String getName() {
		return NAME;
	}

	public MongoManaged addCatMongo(WebServerConfig config, Environment environment) throws Exception {
		// init category DB
		MongoManaged catMongoManaged = new MongoManaged(config.getCategoryMongoConfiguration());
		environment.lifecycle().manage(catMongoManaged);
		environment.healthChecks().register("MongoCatDBHealthCheck", new MongoHealthCheck(catMongoManaged));

		logger.info("Category Database Initialized");
		return catMongoManaged;
	}

	@Override
	public void initialize(Bootstrap<WebServerConfig> bootstrap) {
		super.initialize(bootstrap);
	}

	public MongoManaged addUserMongo(WebServerConfig config, Environment environment) throws Exception {
		// init content DB
		MongoManaged userMongoManaged = new MongoManaged(config.getMongoUserConfig());
		environment.lifecycle().manage(userMongoManaged);
		environment.healthChecks().register("MongoUserDBHealthCheck", new MongoHealthCheck(userMongoManaged));

		logger.info("User Database Initialized");
		return userMongoManaged;
	}

	public MongoManaged addContentMongo(WebServerConfig config, Environment environment) throws Exception {
		// init content DB
		MongoManaged contentMongoManaged = new MongoManaged(config.getMongoContentConfiguration());
		environment.lifecycle().manage(contentMongoManaged);
		environment.healthChecks().register("MongoContentDBHealthCheck", new MongoHealthCheck(contentMongoManaged));

		logger.info("Content Database Initialized");
		return contentMongoManaged;
	}

	public MongoManaged addSplashMongo(WebServerConfig config, Environment environment) throws Exception {
		// init splash screen DB
		MongoManaged splashMongoManaged = new MongoManaged(config.getMongoSplashConfiguration());
		environment.lifecycle().manage(splashMongoManaged);
		environment.healthChecks().register("MongoSplashDBHealthCheck", new MongoHealthCheck(splashMongoManaged));
		logger.info("Splash Screen Database Initialized");
		return splashMongoManaged;
	}

	private void configureCors(Environment environment) {
		Dynamic filter = environment.servlets().addFilter("CORS", CrossOriginFilter.class);
		filter.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
		filter.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "GET,PUT,POST,DELETE,OPTIONS");
		filter.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_METHODS_HEADER, "GET,PUT,POST,DELETE,OPTIONS");
		filter.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
		filter.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "*");
		filter.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_HEADERS_HEADER,
				"Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin,Cookies");
		filter.setInitParameter("allowCredentials", "true");
		filter.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER, "true");
	}

	@Override
	public void run(WebServerConfig config, Environment environment) throws Exception {

		configureCors(environment);

		// establish database connections
		logger.info("Beginning database initialization...");
		MongoManaged userMongoManaged = addUserMongo(config, environment);
		MongoManaged contentMongoManaged = addContentMongo(config, environment);
		MongoManaged splashMongoManaged = addSplashMongo(config, environment);
		MongoManaged catMongoManaged = addCatMongo(config, environment);

		UserRepository userRepo = new UserRepository(userMongoManaged);
		MetadataRepository splashRepo = new MetadataRepository(splashMongoManaged);
		ContentRepository contentRepo = new ContentRepository(contentMongoManaged);
		CategoryRepository catRepo = new CategoryRepository(catMongoManaged);
		logger.info("All databases initialized");

		String webRootPath = config.getWsConfig().webRoot;
		File webRoot = new File(webRootPath);
		File studioRoot = new File(webRoot, "studio");
		File playerRoot = new File(webRoot, "player");
		File faxRoot = new File(webRoot, "moviefax");
		environment.jersey().register(AuthFactory.binder(new BasicAuthFactory<BasicUser>(
				new SimpleAuthenticator("portol", "portol"), "SECURITY REALM", BasicUser.class)));

		IndexResource index = new IndexResource(webRoot, Charsets.UTF_8);
		environment.jersey().register(index);

		BasicUser authd = new BasicUser("porto" + "l", "portol");
		WebServlet playerServlet = new WebServlet("/webroot/player", "/player", "index.html", Charsets.UTF_8, authd);
		environment.servlets().addServlet("player", playerServlet).addMapping("/player/*");

		WebServlet studioServlet = new WebServlet("/webroot/studio", "/studio", "index.html", Charsets.UTF_8, authd);
		environment.servlets().addServlet("studio", studioServlet).addMapping("/studio/*");

		FaxResource fax = new FaxResource(faxRoot, Charsets.UTF_8);
		environment.jersey().register(fax);
	}

}
