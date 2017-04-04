package com.portol.cdn.config;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;

public class CDNConfig extends Configuration{
	
	@Valid
	//@NotNull
	@JsonProperty("EdgeEndpoint")
	private EdgeServerResourceConfig edgeConfig = new  EdgeServerResourceConfig();

	public EdgeServerResourceConfig getIconGenConfig() {
		return edgeConfig;
	}

	
	
	
}
