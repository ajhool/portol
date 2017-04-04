package com.portol.loadbalancer.config;

import javax.validation.constraints.NotNull;

import io.dropwizard.Configuration;

public class CloudProviderConfig extends Configuration {
	
	@NotNull
	public String apiKey;
	
}
