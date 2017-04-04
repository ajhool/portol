package com.portol.cloudplayer.config;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.ws.rs.client.Client;

public class MPDServerConfig {

    @Min(1)
    @Max(65535)
    @NotNull
    public int port;

  
    public String timelinePath;

    
    public String availabilityPath;


    public String apiKey;
	
	public String protocol;
	
    
}
