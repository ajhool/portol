package com.portol.loadbalancer.config;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class CDNConfiguration {
	
	@NotNull
	public String thisHost;
	
	@NotNull
	public String cdnHostIp;
	
	
	 @Min(1)
    @Max(65535)
    public int port;
    
    @NotNull
    public String registrationPath;

    @NotNull
    public String deletionPath;

	public String protocol = "http://";


}
