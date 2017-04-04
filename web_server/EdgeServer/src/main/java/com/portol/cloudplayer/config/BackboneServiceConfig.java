package com.portol.cloudplayer.config;

import io.dropwizard.Configuration;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class BackboneServiceConfig extends Configuration{

  
    public String apiKey;
	
	public String md5Path = "/api/v0/file/info";



  
    
 
}
