package com.portol.paymentserver;

import javax.ws.rs.client.Client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.portol.paymentserver.config.BalanceCheckerConfig;
import com.portol.paymentserver.config.PaymentServerConfig;
import com.portol.paymentserver.healthchecks.MongoHealthCheck;
import com.portol.paymentserver.manager.MongoManaged;
import com.portol.paymentserver.repository.PlayerRepository;
import com.portol.paymentserver.resource.PaymentAsyncResource;
import com.portol.paymentserver.resource.PaymentResource;
import com.portol.paymentserver.service.BalanceCheckerClient;
import com.portol.paymentserver.service.BalanceMonitor;
import com.portol.paymentserver.service.LoadbalCommunicator;

import io.dropwizard.Application;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class PaymentServerApplication extends Application<PaymentServerConfig> {

	public static final String NAME = "Payment_Server";
	final static Logger logger = LoggerFactory
			.getLogger(PaymentServerApplication.class);

	public static void main(String[] args) throws Exception {
		new PaymentServerApplication().run(args);
	}

	@Override
	public String getName() {
		return NAME;
	}

	public BalanceCheckerClient addBalanceCheckerClient(
			PaymentServerConfig config, Environment environment) {
		logger.info("creating balance checker client...");

		BalanceCheckerConfig thisConfig = config.getBalcheckConf();

		final Client jClient = new JerseyClientBuilder(environment).using(
				config.getJerseyClientConfiguration()).build("balanceChecker");
		BalanceCheckerClient balCheck = new BalanceCheckerClient(jClient,
				thisConfig.host, thisConfig.unConfBalCheckPath,
				thisConfig.port, thisConfig.confBalCheckPath,
				thisConfig.jsonpath);

		logger.info("balance checker configured successfully");

		return balCheck;
	}

	private BalanceMonitor getBalanceMonitor(PaymentServerConfig config,
			Environment environment) {
		logger.info("creating balancemonitor...");

		BalanceMonitor monitor = new BalanceMonitor();
		environment.lifecycle().manage(monitor);
		logger.info("balance monitor created successfully.");

		return monitor;
	}
	
	private PaymentAsyncResource getAsyncPaymentResource(PaymentServerConfig config,
			Environment environment, BalanceMonitor monitor, PlayerRepository playerRepo, LoadbalCommunicator lbComm, BalanceCheckerClient balcheck) {
		logger.info("creating balancemonitor...");

		PaymentAsyncResource asyncPaymentRes = new PaymentAsyncResource(monitor, playerRepo, lbComm, balcheck);
		environment.lifecycle().manage(asyncPaymentRes);
		logger.info("balance monitor created successfully.");

		return asyncPaymentRes;
	}
	

	public MongoManaged addPlayerMongo(PaymentServerConfig config,
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

	@Override
	public void initialize(Bootstrap<PaymentServerConfig> bootstrap) {
	}

	public LoadbalCommunicator createLoadbalCommunicator(PaymentServerConfig config, Environment environment){
		logger.info("creating loadbal communicator client...");
		final Client jClient = new JerseyClientBuilder(environment).using(
				config.getJerseyClientConfiguration()).build("loadbal Communicator");
		LoadbalCommunicator comms = new LoadbalCommunicator(config.getLBCommConf(), jClient);

		return comms;
	}
	
	
	@Override
	public void run(PaymentServerConfig config, Environment environment)
			throws Exception {

		MongoManaged playerMongoManaged = addPlayerMongo(config, environment);
		PlayerRepository playerRepo = new PlayerRepository(playerMongoManaged);

		BalanceMonitor monitor = this.getBalanceMonitor(config, environment);
		LoadbalCommunicator lbComm = this.createLoadbalCommunicator(config, environment);
		BalanceCheckerClient balcheck = addBalanceCheckerClient(config,
				environment);
		

		PaymentAsyncResource asyncRes = getAsyncPaymentResource(config, environment, monitor, playerRepo,  lbComm, balcheck);
		

		

		final Client jClient = new JerseyClientBuilder(environment).using(
				config.getJerseyClientConfiguration()).build(getName());

		PaymentResource paymentRes = new PaymentResource(balcheck, jClient, monitor);
		environment.jersey().register(paymentRes);

	}

}
