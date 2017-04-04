package com.portol.cloudplayer.config;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class ContentScrubberConfig {
	
	
    public List<String> javaRelativeDirs;
    
    @NotNull
    @Valid
	public int ttlSeconds;

	
    
}
