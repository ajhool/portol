package com.portol.qrserver.config;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import io.dropwizard.Configuration;

public class QRConfiguration extends Configuration{

	
	@Valid
	@NotNull
	public String hostAddr;
	
	@Min(1)
    @Max(65535)
    public int port;
	
	@Valid
	@NotNull
	public String protocol;
	
	@Valid
	@NotNull
	public String qrDirRoot;
	
}
