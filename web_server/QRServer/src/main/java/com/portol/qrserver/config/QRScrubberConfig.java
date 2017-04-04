package com.portol.qrserver.config;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class QRScrubberConfig {
	@Valid
	@NotNull
	public int ttlSeconds;
	
	@Min(1)
    public int maxNumQR;
	
}
