package com.portol.loadbalancer.config;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;
import io.dropwizard.client.HttpClientConfiguration;
import io.dropwizard.client.JerseyClientConfiguration;


public class LoadBalancerConfig extends Configuration{

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
	@JsonProperty("QRServer")
	private QRMakerClientConfig qrClient = new QRMakerClientConfig();

	@Valid
	@NotNull
	@JsonProperty("metadataDB")
	private MongoConfig mongoSplash = new MongoConfig();

	@Valid
	@NotNull
	@JsonProperty("playerDB")
	private MongoConfig mongoPlayer = new MongoConfig();

	@Valid
	@NotNull
	@JsonProperty("edgeDB")
	private MongoConfig mongoEdge = new MongoConfig();
	
	@Valid
	@NotNull
	@JsonProperty("backendDB")
	private MongoConfig mongoBackend = new MongoConfig();
	

	@Valid
	@NotNull
	@JsonProperty("paymentServer")
	private PaymentGetterConfig addrGetter = new PaymentGetterConfig();

	@Valid
	@NotNull
	@JsonProperty("cloudConnections")
	private CloudCommunicatorConfig cloudCommConf = new CloudCommunicatorConfig();

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
	@JsonProperty("readyQ")
	private ReadyQueueConfiguration rQConf = new ReadyQueueConfiguration();

	@Valid
	@NotNull
	@JsonProperty("providerInfo")
	private CloudProviderConfig providerConf = new CloudProviderConfig();

	@Valid
	@NotNull
	@JsonProperty("httpClient")
	private HttpClientConfiguration httpClient = new HttpClientConfiguration();
	
	@Valid
	@NotNull
	@JsonProperty("loadBalSettings")
	private LoadBalGeneralConfig generalSettings = new LoadBalGeneralConfig();
	
	
	@Valid
	@NotNull
	@JsonProperty("userDB")
	private MongoConfig mongoUser = new MongoConfig();
	
	@Valid
	@NotNull
	@JsonProperty("CDNSettings")
	private CDNConfiguration cdnConfig = new CDNConfiguration();

	public MongoConfig getMongoUserConfig() {
		return mongoUser;
	}
	public MongoConfig getMongoEdgeConfiguration() {
		
		return mongoEdge;
	}
	
	
public MongoConfig getMongoBackendConfiguration() {
		
		return mongoBackend;
	}
	
	public CDNConfiguration getCdnConfig(){
		return this.cdnConfig;
	}
	
	
	public LoadBalGeneralConfig getGeneralSettings(){
		return this.generalSettings;
	}

	public HttpClientConfiguration getHttpClientConfiguration() {
		return httpClient;
	}

	public CloudProviderConfig getCloudProviderConfig(){
		return providerConf;
	}

	public JerseyClientConfiguration getJerseyClientConfiguration() {
		return jerseyClient;
	}


	public MongoConfig getMongoContentConfiguration() {
		return mongoContent;
	}

	public QRMakerClientConfig getQRMakerClientConfig() {
		return this.qrClient;
	}

	public MongoConfig getMongoCookieConfiguration() {
		return mongoCookie;
	}


	public MongoConfig getMongoSplashConfiguration() {
		return mongoSplash;
	}


	public MongoConfig getMongoPlayerConfiguration() {
		return mongoPlayer;
	}

	public PaymentGetterConfig getAddrGetterConfiguration() {
		return addrGetter;
	}


	public CloudCommunicatorConfig getCloudCommConfiguration() {
		return cloudCommConf;
	}


	public ReadyQueueConfiguration getrQConf() {
		return rQConf;
	}

	public BalanceCheckerConfig getBalcheckConf() {
		return balcheckConf;
	}


	public void setBalcheckConf(BalanceCheckerConfig balcheckConf) {
		this.balcheckConf = balcheckConf;
	}

	

}
