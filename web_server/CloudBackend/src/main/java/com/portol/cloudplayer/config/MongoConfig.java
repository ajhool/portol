package com.portol.cloudplayer.config;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import io.dropwizard.Configuration;


public class MongoConfig extends Configuration{


	    public String host;

	    public int port;

	    public String db;

	    @NotNull
	    public String user;

	    @NotNull
	    public String password;
	
}
