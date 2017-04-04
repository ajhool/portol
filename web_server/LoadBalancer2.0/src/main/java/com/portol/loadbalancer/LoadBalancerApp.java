package com.portol.loadbalancer;

import java.math.BigInteger;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.util.EnumSet;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration.Dynamic;
import javax.ws.rs.client.Client;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.http.client.HttpClient;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myjeeva.digitalocean.exception.DigitalOceanException;
import com.myjeeva.digitalocean.exception.RequestUnsuccessfulException;
import com.portol.loadbalancer.config.BalanceCheckerConfig;
import com.portol.loadbalancer.config.CloudCommunicatorConfig;
import com.portol.loadbalancer.config.CloudProviderConfig;
import com.portol.loadbalancer.config.LoadBalGeneralConfig;
import com.portol.loadbalancer.config.LoadBalancerConfig;
import com.portol.loadbalancer.config.PaymentGetterConfig;
import com.portol.loadbalancer.config.QRMakerClientConfig;
import com.portol.loadbalancer.healthchecks.MongoHealthCheck;
import com.portol.loadbalancer.manager.MongoManaged;
import com.portol.loadbalancer.repo.BackendCloudRepository;
import com.portol.loadbalancer.repo.ContentRepository;
import com.portol.loadbalancer.repo.EdgeCloudRepository;
import com.portol.loadbalancer.repo.PlatformRepository;
import com.portol.loadbalancer.repo.PlayerRepository;
import com.portol.loadbalancer.repo.SplashRepository;
import com.portol.loadbalancer.repo.UserRepository;
import com.portol.loadbalancer.resource.AsyncEventResource;
import com.portol.loadbalancer.resource.AsyncStartResource;
import com.portol.loadbalancer.resource.BackendCloudInterface;
//import com.portol.loadbalancer.resource.CookieResource;
import com.portol.loadbalancer.resource.EdgeCloudInterface;
import com.portol.loadbalancer.resource.InitResource;
import com.portol.loadbalancer.resource.PreviewResource;
import com.portol.loadbalancer.resource.StartResource;
import com.portol.loadbalancer.service.AddressService;
import com.portol.loadbalancer.service.BackendReadyQueue;
import com.portol.loadbalancer.service.BalanceCheckerClient;
import com.portol.loadbalancer.service.CDNService;
import com.portol.loadbalancer.service.CloudCommunicator;
import com.portol.loadbalancer.service.CloudProviderAPIClient;
import com.portol.loadbalancer.service.CloudProviderPool;
import com.portol.loadbalancer.service.DigitalOceanAPIClient;
import com.portol.loadbalancer.service.EdgeReadyQueue;
import com.portol.loadbalancer.service.MobileDeviceServerClient;
import com.portol.loadbalancer.service.QRMakerClient;
import com.portol.loadbalancer.service.WebSocketService;
import com.portol.loadbalancer.socket.JSR356Endpoint;

import be.tomcools.dropwizard.websocket.WebsocketBundle;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.JoranException;
import io.dropwizard.Application;
import io.dropwizard.client.HttpClientBuilder;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.jersey.jackson.JacksonMessageBodyProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class LoadBalancerApp extends Application<LoadBalancerConfig> {

	public static final String NAME = "LoadBalancer";
	final static Logger logger = LoggerFactory.getLogger(LoadBalancerApp.class);

	public static void main(String[] args) throws Exception {
		new LoadBalancerApp().run(args);
	}
	private WebsocketBundle websocket = new WebsocketBundle();
	@Override
	public String getName() {
		return NAME;
	}

	private void configureCors(Environment environment) {
		Dynamic filter = environment.servlets().addFilter("CORS",
				CrossOriginFilter.class);
		filter.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class),
				true, "/*");
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
	public void initialize(Bootstrap<LoadBalancerConfig> bootstrap) {
		super.initialize(bootstrap);
		bootstrap.addBundle(websocket);
	}

	public MongoManaged addContentMongo(LoadBalancerConfig config,
			Environment environment) throws Exception {
		// init content DB
		MongoManaged contentMongoManaged = new MongoManaged(
				config.getMongoContentConfiguration());
		environment.lifecycle().manage(contentMongoManaged);
		environment.healthChecks().register("MongoContentDBHealthCheck",
				new MongoHealthCheck(contentMongoManaged));

		logger.info("Content Database Initialized");
		return contentMongoManaged;
	}

	public MongoManaged addSplashMongo(LoadBalancerConfig config,
			Environment environment) throws Exception {
		// init splash screen DB
		MongoManaged splashMongoManaged = new MongoManaged(
				config.getMongoSplashConfiguration());
		environment.lifecycle().manage(splashMongoManaged);
		environment.healthChecks().register("MongoSplashDBHealthCheck",
				new MongoHealthCheck(splashMongoManaged));
		logger.info("Splash Screen Database Initialized");
		return splashMongoManaged;
	}
	
	public MongoManaged addUserMongo(LoadBalancerConfig config, Environment environment) throws Exception{
		// init content DB
		MongoManaged userMongoManaged = new MongoManaged(config.getMongoUserConfig());
		environment.lifecycle().manage(userMongoManaged);
		environment.healthChecks().register("MongoUserDBHealthCheck",
				new MongoHealthCheck(userMongoManaged));

		logger.info("User Database Initialized");
		return userMongoManaged;
	}

	public MongoManaged addPlayerMongo(LoadBalancerConfig config,
			Environment environment) throws Exception {
		// init player DB
		MongoManaged playerMongoManaged = new MongoManaged(
				config.getMongoPlayerConfiguration());
		environment.lifecycle().manage(playerMongoManaged);
		environment.healthChecks().register("MongoPlayerDBHealthCheck",
				new MongoHealthCheck(playerMongoManaged));
		logger.info("Player Database Initialized");
		return playerMongoManaged;
	}

	public MongoManaged addEdgeMongo(LoadBalancerConfig config,
			Environment environment) throws Exception {
		// init cloud DB
		MongoManaged edgeMongoManaged = new MongoManaged(
				config.getMongoEdgeConfiguration());
		environment.lifecycle().manage(edgeMongoManaged);
		environment.healthChecks().register("MongoEdgeDBHealthCheck",
				new MongoHealthCheck(edgeMongoManaged));
		logger.info("Cloud Edge DB Initialized");
		return edgeMongoManaged;
	}
	
	public MongoManaged addBackendMongo(LoadBalancerConfig config,
			Environment environment) throws Exception {
		// init cloud DB
		MongoManaged cloudMongoManaged = new MongoManaged(
				config.getMongoBackendConfiguration());
		environment.lifecycle().manage(cloudMongoManaged);
		environment.healthChecks().register("MongoBackendDBHealthCheck",
				new MongoHealthCheck(cloudMongoManaged));
		logger.info("Cloud Backend DB Initialized");
		return cloudMongoManaged;
	}

	public MongoManaged addCookieMongo(LoadBalancerConfig config,
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

	public CDNService createCDNService(LoadBalancerConfig config,
			Environment environment) {
		logger.info("creating new cdn service...");
		final Client jClient = new JerseyClientBuilder(environment).using(
				config.getJerseyClientConfiguration()).build("CDNservice");


		CDNService cdnsvc = new CDNService(jClient, config.getCdnConfig());
		environment.lifecycle().manage(cdnsvc);
		logger.info("New address client created successfully.");

		
		return cdnsvc;
	}
	
	public WebSocketService createWSService(MobileDeviceServerClient mClient, PlayerRepository playerRepo, Environment environment) {
		logger.info("creating new cdn service...");
	
		WebSocketService wsSvc = new WebSocketService(environment.getObjectMapper(), mClient, playerRepo); 

		
		environment.lifecycle().manage(wsSvc);
		logger.info("New address client created successfully.");

		
		return wsSvc;
	}
	
	public AddressService createAddressService(LoadBalancerConfig config,
			Environment environment, 
			BalanceCheckerClient balCheck) {
		logger.info("creating new address getter client...");
		final Client jClient = new JerseyClientBuilder(environment).using(
				config.getJerseyClientConfiguration()).build("addressGetter");

		PaymentGetterConfig addrGetter = config.getAddrGetterConfiguration();

		AddressService svcAddr = new AddressService(jClient, addrGetter,
				 balCheck);
		logger.info("New address client created successfully.");

		return svcAddr;
	}
	

	public CloudCommunicator createCloudCommunicator(LoadBalancerConfig config,
			Environment environment) throws NoSuchAlgorithmException,
			KeyManagementException {
		logger.info("creating cloud communicator client...");

		CloudCommunicatorConfig thisConfig = config.getCloudCommConfiguration();
		
		final Client jClient = new JerseyClientBuilder(environment).using(
				config.getJerseyClientConfiguration()).build(
						"cloudCommunicator");

		// contains url of original content to serve, as well as DB information
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			@Override
			public void checkClientTrusted(
					java.security.cert.X509Certificate[] arg0, String arg1)
							throws CertificateException {
			}

			@Override
			public void checkServerTrusted(
					java.security.cert.X509Certificate[] arg0, String arg1)
							throws CertificateException {
			}

			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}
		} };

		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, new SecureRandom());
			HttpsURLConnection
			.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
			;
		}

		SSLContext ctx = SSLContext.getInstance("SSL");
		ctx.init(null, trustAllCerts, null);

		HostnameVerifier allHostsValid = new HostnameVerifier() {

			@Override
			public boolean verify(String arg0, SSLSession arg1) {
				return true;
			}
		};

		HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

		CloudCommunicator svcCloud = new CloudCommunicator(jClient,
				thisConfig.addPlayerPath, thisConfig.deletePlayerPath,
				thisConfig.port, thisConfig.setContentPath, thisConfig.taskPath, thisConfig.setLiveContentPath, thisConfig.loginAlertPath);

		logger.info("cloud communicator configured successfully");
		return svcCloud;
	}

	public EdgeReadyQueue addEdgeReadyQService(LoadBalancerConfig config,
			Environment environment, CloudCommunicator comm,
			EdgeCloudRepository cldRepo, CloudProviderPool cprovider) {
		EdgeReadyQueue manager = new EdgeReadyQueue(config.getrQConf(),
				comm, cldRepo, cprovider);
		environment.lifecycle().manage(manager);

		logger.info("ready queue service initialized");
		return manager;
	}

	public BackendReadyQueue addBackendReadyQService(LoadBalancerConfig config,
			Environment environment, CloudCommunicator comm,
			BackendCloudRepository cldRepo, CloudProviderPool cprovider) {
		BackendReadyQueue manager = new BackendReadyQueue(config.getrQConf(),
				comm, cldRepo, cprovider);
		environment.lifecycle().manage(manager);

		logger.info("ready queue service initialized");
		return manager;
	}
	
	public BalanceCheckerClient addBalanceCheckerClient(
			LoadBalancerConfig config, Environment environment) {
		logger.info("creating balance checker client...");

		BalanceCheckerConfig thisConfig = config.getBalcheckConf();

		final Client jClient = new JerseyClientBuilder(environment).using(
				config.getJerseyClientConfiguration()).build("balanceChecker");
		BalanceCheckerClient balCheck = new BalanceCheckerClient(jClient,
				thisConfig.host, thisConfig.unConfBalCheckPath,
				thisConfig.port, thisConfig.confBalCheckPath, thisConfig.jsonpath);

		logger.info("balance checker configured successfully");

		return balCheck;
	}

	public MobileDeviceServerClient addMobileInterfaceClient(
			LoadBalancerConfig config, Environment environment) {
		logger.info("creating mobile interface client...");

		final Client jClient = new JerseyClientBuilder(environment).using(
				config.getJerseyClientConfiguration()).build("mobile interface");
		MobileDeviceServerClient mobile = new MobileDeviceServerClient(jClient);

		logger.info("mobile interface configured successfully");

		return mobile;
	}
	
	public QRMakerClient addQRMakerClient(LoadBalancerConfig config,
			Environment environment) throws NoSuchAlgorithmException,
			KeyManagementException {
		logger.info("creating new qr code maker client");

		QRMakerClientConfig thisConfig = config.getQRMakerClientConfig();

		final Client jClient = new JerseyClientBuilder(environment).using(
				config.getJerseyClientConfiguration()).build("qrmaker");

		QRMakerClient toRet = new QRMakerClient(thisConfig, jClient);
		logger.info("qr code client configured successfully");
		return toRet;
	}

	public CloudProviderPool addCloudProviderAPIClients(
			LoadBalancerConfig config, Environment environment) throws DigitalOceanException, RequestUnsuccessfulException {
		logger.info("creating cloud API pool client...");

		CloudProviderConfig thisConfig = config.getCloudProviderConfig();

		final HttpClient hClient = new HttpClientBuilder(environment).using(config.getHttpClientConfiguration()).build("cloudProviderClient");

		CloudProviderAPIClient cloudClient = new DigitalOceanAPIClient(
				thisConfig, hClient);
		
		CloudProviderPool myClouds = new CloudProviderPool();
		myClouds.addProvider(cloudClient);

		logger.info("cloud API pool configured successfully");
		return myClouds;
	}
	
	
	
	public static interface MixIn {
        @JsonIgnore
        public void setYear(BigInteger year); 
    }

	static void reset() throws JoranException {
		  ILoggerFactory factory = LoggerFactory.getILoggerFactory();
		  LoggerContext context = (LoggerContext)factory;
		  context.reset();
		  ContextInitializer initializer = new ContextInitializer(context);
		  initializer.autoConfig(); // load logback.xml or .groovy from classpath
		}

	
	@Override
	public void run(LoadBalancerConfig config, Environment environment)
			throws Exception {


		reset();
		configureCors(environment);
		
		
		// establish database connections
		logger.info("Beginning database initialization...");
		MongoManaged contentMongoManaged = addContentMongo(config, environment);
		MongoManaged splashMongoManaged = addSplashMongo(config, environment);
		MongoManaged playerMongoManaged = addPlayerMongo(config, environment);
		MongoManaged edgeMongoManaged = addEdgeMongo(config, environment);
		MongoManaged backendMongoManaged = addBackendMongo(config, environment);
		MongoManaged platformMongoManaged = addCookieMongo(config, environment);
		MongoManaged userMongoManaged = addUserMongo(config, environment);
		
		PlatformRepository platRepo = new PlatformRepository(platformMongoManaged);
		EdgeCloudRepository cldRepo = new EdgeCloudRepository(edgeMongoManaged);
		PlayerRepository playerRepo = new PlayerRepository(playerMongoManaged);
		SplashRepository splashRepo = new SplashRepository(splashMongoManaged);
		ContentRepository contentRepo = new ContentRepository(contentMongoManaged);
		BackendCloudRepository backends = new BackendCloudRepository(backendMongoManaged);
		UserRepository userRepo = new UserRepository(userMongoManaged);
		
		logger.info("All databases initialized");

		LoadBalGeneralConfig generalSettings = config.getGeneralSettings();
		
		CloudProviderPool cprovider = addCloudProviderAPIClients(config,
				environment);

		CloudCommunicator comm = createCloudCommunicator(config, environment);
		
		EdgeReadyQueue readyQEdge = addEdgeReadyQService(config, environment, comm,
				cldRepo, cprovider);
		
		BackendReadyQueue readyQBack = addBackendReadyQService(config, environment, comm,
				backends, cprovider);

		BalanceCheckerClient balcheck = addBalanceCheckerClient(config,
				environment);

		MobileDeviceServerClient sClient = this.addMobileInterfaceClient(config, environment);
		AddressService addrSvc = createAddressService(config, environment,
				 balcheck);

		QRMakerClient qrMaker = this.addQRMakerClient(config, environment);
		
		
		createWSService(sClient, playerRepo, environment);
		createCDNService(config, environment);
		
		// endpoint for initial calls by players
		environment.jersey().register(
				new InitResource(contentRepo, addrSvc,
						splashRepo, playerRepo, qrMaker, userRepo, sClient, platRepo));

		// endpoint for incoming cloud communications
		EdgeCloudInterface cloudInt = new EdgeCloudInterface(playerRepo,
				cldRepo, contentRepo, comm, readyQEdge, sClient);
		environment.jersey().register(cloudInt);
		
		
		BackendCloudInterface backendInt = new BackendCloudInterface(playerRepo,
				contentRepo, comm, readyQBack, backends);
		environment.jersey().register(backendInt);
		
		// handles requests for previews
		environment.jersey().register(
				new PreviewResource(contentRepo, splashRepo,
						playerRepo, cldRepo, comm, readyQEdge,
						qrMaker,  generalSettings.cloudPrefetchDelay, sClient, userRepo));

		// handles requests to start video playback
		environment.jersey().register(
				new StartResource(contentRepo, splashRepo,
						playerRepo, cldRepo, backends, addrSvc, comm,
						readyQEdge, readyQBack, qrMaker,  generalSettings.cloudPrefetchDelay, sClient, userRepo));

		AsyncStartResource asyncStartResource = new AsyncStartResource(contentRepo, splashRepo,
				playerRepo, cldRepo, backends, addrSvc, comm,
				readyQEdge, readyQBack, qrMaker,  generalSettings.cloudPrefetchDelay, sClient, userRepo);
		
		environment.jersey().register(asyncStartResource);
		
		// app interface
		environment.jersey().register(
				new AsyncEventResource(playerRepo, addrSvc,
						contentRepo, cldRepo, readyQEdge, comm));
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.addMixInAnnotations(XMLGregorianCalendar.class, MixIn.class);
		environment.jersey().register(new JacksonMessageBodyProvider(mapper, environment.getValidator()));
		
		websocket.addEndpoint(JSR356Endpoint.class);

	}

	

	

}
