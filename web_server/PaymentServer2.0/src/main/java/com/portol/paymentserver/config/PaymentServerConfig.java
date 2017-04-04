package com.portol.paymentserver.config;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;


public class PaymentServerConfig extends Configuration{

	@Valid
	@NotNull
	@JsonProperty("addrBalanceChecker")
	private BalanceCheckerConfig balcheckConf = new BalanceCheckerConfig();

	@Valid
	@NotNull
	@JsonProperty("jerseyClient")
	private JerseyClientConfiguration jerseyClient = new JerseyClientConfiguration();

	@Valid
	@NotNull
	@JsonProperty("playerDB")
	private MongoConfig mongoPlayer = new MongoConfig();

	@Valid
	@NotNull
	@JsonProperty("loadbalConnections")
	private LoadbalCommunicatorConfig loadbalCommConf = new LoadbalCommunicatorConfig();

	public LoadbalCommunicatorConfig getLBCommConf() {
		return loadbalCommConf;
	}


	public MongoConfig getMongoPlayerConfiguration() {
		return mongoPlayer;
	}


	public JerseyClientConfiguration getJerseyClientConfiguration() {
		return jerseyClient;
	}

	public BalanceCheckerConfig getBalcheckConf() {
		return balcheckConf;
	}
}
