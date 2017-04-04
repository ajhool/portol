package com.portol.paymentserver.config;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class LoadbalCommunicatorConfig {

    @Min(1)
    @Max(65535)
    public int port;
    
    @NotNull
    public String startPath;
    
    @NotNull
    public String loadbalURL;
    
}
