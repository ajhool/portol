package com.portol.loadbalancer.config;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class QRMakerClientConfig {
	
	
		//data that gets baked into every QR
	    @NotNull
	    public String bipPaymentServerHost;
	    
	    @NotNull
	    public String bipPaymentServerprotocol;

	    @NotNull
	    public String bipProtocol;
	    
	    @Min(1)
	    @Max(65535)
	    public int port;

	    
	    //QR maker server specifc info
	    @Min(1)
	    @Max(65535)
	    public String QRmakerPort;

	    @NotNull
	    public String QRmakerURL;
	    
	    @NotNull
	    public String QRmakerProtocol;
	    
	    @NotNull
	    public String apiKey;
	    
	    @NotNull
	    public String path;
}
