package com.portol.loadbalancer.config;

import io.dropwizard.Configuration;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class CloudCommunicatorConfig extends Configuration{

    @Min(1)
    @Max(65535)
    public int port;
    
    @NotNull
    public String setContentPath;

    @NotNull
    public String addPlayerPath;

    @NotNull
    public String deletePlayerPath;
    
    public String taskPath;

	public String setLiveContentPath;

	@NotNull
	public String loginAlertPath;
}
