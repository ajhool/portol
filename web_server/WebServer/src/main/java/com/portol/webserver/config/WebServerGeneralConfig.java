package com.portol.webserver.config;

import javax.validation.constraints.NotNull;

import io.dropwizard.Configuration;


public class WebServerGeneralConfig extends Configuration{
	    @NotNull
	    public String webRoot;

	 
}
