package com.portol.cloudplayer.config;

import io.dropwizard.Configuration;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class LoadbalCommunicatorConfig extends Configuration{

    @Min(1)
    @Max(65535)
    public int port;
    
    @NotNull
    public String bootPath;

    @NotNull
    public String keepAlivePath;

    @NotNull
    public String eventPath;
    
    @NotNull
    public String loadbalURL;
    
 
}
