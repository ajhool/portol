package com.portol.qrserver.config;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;

public class QRServerConfig extends Configuration {

	@Valid
	@NotNull
	@JsonProperty("QR")
	private QRConfiguration qrConf = new QRConfiguration();

	
	@Valid
	@NotNull
	@JsonProperty("scrubberSettings")
	private QRScrubberConfig scrubberConf = new QRScrubberConfig();
	
	
	public QRScrubberConfig getScrubberConf(){
		return scrubberConf;
	}
	public QRConfiguration getQrConf() {
		return qrConf;
	}
}
