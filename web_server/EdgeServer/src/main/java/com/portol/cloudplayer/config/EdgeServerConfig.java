package com.portol.cloudplayer.config;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;
import io.dropwizard.client.HttpClientConfiguration;
import io.dropwizard.client.JerseyClientConfiguration;

public class EdgeServerConfig extends Configuration {

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
	@JsonProperty("backbone")
	private BackboneServiceConfig backend = new BackboneServiceConfig();
	
	
	@Valid
	@NotNull
	@JsonProperty("scrubber")
	private ContentScrubberConfig scrubbr = new ContentScrubberConfig();
	
	@Valid
	@NotNull
	@JsonProperty("mpdComponents")
	private MPDServerConfig mpd = new MPDServerConfig();
	
	
	public MPDServerConfig getMPDConfig(){
		return mpd;
	}
	
	public LoadbalCommunicatorConfig getLoadbalConfig(){
		return loadbalComm;
	}
	
	public JerseyClientConfiguration getJerseyClientConfiguration() {
		return jerseyClient;
	}
	public BackboneServiceConfig getBackboneServiceConfig() {
		
		return backend;
	}
	public ContentScrubberConfig getScrubberConf() {
		
		return this.scrubbr;
	}
}
