package com.portol.cloudplayer.config;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;
import io.dropwizard.client.HttpClientConfiguration;
import io.dropwizard.client.JerseyClientConfiguration;

public class BackendServerConfig extends Configuration {

	@Valid
	@NotNull
	@JsonProperty("jerseyClient")
	private JerseyClientConfiguration jerseyClient = new JerseyClientConfiguration();

	@Valid
	@NotNull
	@JsonProperty("loadbalCommunicator")
	private LoadbalCommunicatorConfig loadbalComm = new LoadbalCommunicatorConfig();
	
	@Valid
    @NotNull
    private HttpClientConfiguration httpClient = new HttpClientConfiguration();

    @JsonProperty("httpClient")
    public HttpClientConfiguration getHttpClientConfiguration() {
        return httpClient;
    }
	
	@Valid
	@NotNull
	@JsonProperty("livedownloader")
	private LiveContentDownloaderConfig livedl = new LiveContentDownloaderConfig();
	
	@Valid
	@NotNull
	@JsonProperty("scrubber")
	private ContentScrubberConfig scrubbr = new ContentScrubberConfig();
	
	
	public LoadbalCommunicatorConfig getLoadbalConfig(){
		return loadbalComm;
	}
	public LiveContentDownloaderConfig getLiveContentDownloaderConfig() {
		return livedl;
	}
	
	public JerseyClientConfiguration getJerseyClientConfiguration() {
		return jerseyClient;
	}

	public ContentScrubberConfig getScrubberConf() {
		
		return this.scrubbr;
	}
}
