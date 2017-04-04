package com.portol.cloudplayer.service;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.portol.cloudplayer.config.MPDServerConfig;
import com.portol.common.model.ComponentRequest;
import com.portol.common.model.dash.jaxb.RepresentationType;
import com.portol.common.model.dash.jaxb.SegmentTimelineType;


public class MPDService {


	static final Logger logger = LoggerFactory.getLogger(MPDService.class);

	final Client cClient; 
	private final MPDServerConfig config;

	public MPDService(Client jClient, MPDServerConfig thisConfig) {
		this.cClient = jClient;
		this.config = thisConfig;
	}

	public SegmentTimelineType getSegmentTimeline(RepresentationType toFetch, String host) throws URISyntaxException{

		URI uri = new URI(config.protocol + host + ":" + config.port + config.timelinePath);
		String targetURL = uri.toString();
		logger.debug("Getting timeline, target URI: " + targetURL);
		ComponentRequest req = new ComponentRequest();
		req.setRepId(toFetch.getId());
		SegmentTimelineType resp = cClient.target(uri).request().post(Entity.json(req), SegmentTimelineType.class);


		return resp;

	}

	public XMLGregorianCalendar getAvailabilityStart(RepresentationType toFetch, String host) throws Exception {
		int tries = 0;
		XMLGregorianCalendar xml = null;
		while(tries < 10){
			URI uri = new URI(config.protocol + host + ":" + config.port + config.availabilityPath);
			String targetURL = uri.toString();
			logger.debug("getting aviability, target URI: " + targetURL);

			ComponentRequest req = new ComponentRequest();
			req.setRepId(toFetch.getId());
			String resp = cClient.target(uri).request().post(Entity.json(req), String.class);

			try {
				xml = DatatypeFactory.newInstance().newXMLGregorianCalendar(resp);
				return xml;
			} catch (IllegalArgumentException e){
				logger.error("bad string given to gregorian calendar maker. bad input: " + resp, e);
				tries++;
				Thread.sleep(100);
			}
		}
		throw new Exception("failed to parse XML date from backend  after " + tries + " tries.");
		
	}

}
