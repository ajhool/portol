package com.portol.loadbalancer.config;

import javax.validation.constraints.NotNull;

import io.dropwizard.Configuration;

public class ReadyQueueConfiguration extends Configuration {
	
	@NotNull
	public int initialSize;
}
