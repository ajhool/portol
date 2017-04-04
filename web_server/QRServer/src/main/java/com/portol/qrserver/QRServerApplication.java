package com.portol.qrserver;

import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration.Dynamic;

import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.portol.qrserver.config.QRServerConfig;
import com.portol.qrserver.resource.QRResource;
import com.portol.qrserver.service.QRScrubberService;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;


public class QRServerApplication extends Application<QRServerConfig> {

	public static final String NAME = "QRServer";
	final static Logger logger = LoggerFactory
			.getLogger(QRServerApplication.class);


	public static void main(String[] args) throws Exception {
		new QRServerApplication().run(args);
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void initialize(Bootstrap<QRServerConfig> bootstrap) {
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
	
	public QRScrubberService startQRScrubberSvc(QRServerConfig config, Environment environment) {
		QRScrubberService manager = new QRScrubberService(config.getScrubberConf(), config.getQrConf().qrDirRoot);
		environment.lifecycle().manage(manager);

		logger.info("QRScrubberService initialized");
		return manager;
	}
	

	@Override
	public void run(QRServerConfig config, Environment environment)
			throws Exception {

		configureCors(environment);
		
		startQRScrubberSvc(config, environment);

		QRResource qrResource = new QRResource(config.getQrConf());

		environment.jersey().register(qrResource);

	}

}

