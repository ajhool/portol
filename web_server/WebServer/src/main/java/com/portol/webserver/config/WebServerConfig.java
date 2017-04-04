package com.portol.webserver.config;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;


public class WebServerConfig extends Configuration{

	@Valid
	@NotNull
	@JsonProperty("contentDB")
	private MongoConfig mongoContent = new MongoConfig();
	
	@Valid
	@NotNull
	@JsonProperty("cookieDB")
	private MongoConfig mongoCookie = new MongoConfig();
	
	@Valid
	@NotNull
	@JsonProperty("metadataDB")
	private MongoConfig mongoSplash = new MongoConfig();
	
	
	@Valid
	@NotNull
	@JsonProperty("webserver")
	private WebServerGeneralConfig wsConfig = new WebServerGeneralConfig();
	
	
	public WebServerGeneralConfig getWsConfig() {
		return wsConfig;
	}

	@Valid
	@NotNull
	@JsonProperty("categoryDB")
	public MongoConfig categoryDB = new MongoConfig();
	
	@Valid
	@NotNull
	@JsonProperty("userDB")
	private MongoConfig mongoUser = new MongoConfig();
	
	@Valid
	@NotNull
	@JsonProperty("playerDB")
	private MongoConfig mongoPlayer = new MongoConfig();
	
	public MongoConfig getMongoUserConfig() {
		return mongoUser;
	}


	public MongoConfig getMongoPlayerConfig() {
		return mongoPlayer;
	}

	public MongoConfig getMongoSplashConfiguration() {
		return mongoSplash;
	}
	
	public MongoConfig getMongoContentConfiguration() {
		return mongoContent;
	}
	
	public MongoConfig getCategoryMongoConfiguration() {
		return this.categoryDB;
	}
	
	public MongoConfig getMongoCookieConfiguration() {
		return mongoCookie;
	}

	@Valid
	@NotNull
	@JsonProperty("jerseyClient")
	private JerseyClientConfiguration jerseyClient = new JerseyClientConfiguration();

	
	public JerseyClientConfiguration getJerseyClientConfiguration() {
		return jerseyClient;
	}
}
