package com.portol.paymentserver.config;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class BalanceCheckerConfig {

    @NotNull
    public String host;

    @Min(1)
    @Max(65535)
    public int port;

    
    public String confBalCheckPath;

    
    public String unConfBalCheckPath;

    public String jsonpath;
    
    public String user;

    public String password;
    
}
