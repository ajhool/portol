package com.portol.loadbalancer.config;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class LoadBalGeneralConfig {
	
	@NotNull
	@Min(100)
	public int cloudPrefetchDelay;
}
