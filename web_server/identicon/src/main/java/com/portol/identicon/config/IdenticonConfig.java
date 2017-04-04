package com.portol.identicon.config;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;

public class IdenticonConfig extends Configuration{
	
	@Valid
	@JsonProperty("icon")
	private IconGeneratorConfig iconGenConfig = new  IconGeneratorConfig();

	public IconGeneratorConfig getIconGenConfig() {
		return iconGenConfig;
	}

	
	
	
}
