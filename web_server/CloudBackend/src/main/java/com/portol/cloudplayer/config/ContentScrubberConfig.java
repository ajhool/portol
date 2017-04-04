package com.portol.cloudplayer.config;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.ws.rs.client.Client;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ContentScrubberConfig {
	
	
    public List<String> javaRelativeDirs;
    
    @NotNull
    @Valid
	public int ttlSeconds;

	
    
}
