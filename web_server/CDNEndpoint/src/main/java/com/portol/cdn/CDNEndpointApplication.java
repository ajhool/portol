package com.portol.cdn;

import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration.Dynamic;

import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.portol.cdn.config.CDNConfig;
import com.portol.cdn.repository.EdgeRepository;
import com.portol.cdn.resource.RegionalServerResource;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;


/**
 * Entry point for Portol CDN microservice
 * 
 * @author alex
 *
 */
public class CDNEndpointApplication extends Application<CDNConfig>{

	public static final String NAME = "CDNServer";
	final static Logger logger = LoggerFactory
			.getLogger(CDNEndpointApplication.class);


	public static void main(String[] args) throws Exception {
		new CDNEndpointApplication().run(args);
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void initialize(Bootstrap<CDNConfig> bootstrap) {
		//bootstrap.addBundle(new AssetsBundle(, ));
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
	

	private EdgeRepository getEdgeRepo(CDNConfig config, Environment environment){
		EdgeRepository repo = new EdgeRepository();
		
		return repo;
	}
	
	@Override
	public void run(CDNConfig config, Environment environment)
			throws Exception {

		configureCors(environment);
		
		EdgeRepository edgeRepo = this.getEdgeRepo(config, environment);

		RegionalServerResource qrResource = new RegionalServerResource(config.getIconGenConfig(), edgeRepo);

		environment.jersey().register(qrResource);

	}
	
}
