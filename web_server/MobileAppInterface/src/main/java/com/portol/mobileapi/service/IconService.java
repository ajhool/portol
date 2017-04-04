package com.portol.mobileapi.service;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Base64;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.portol.common.model.icon.IconReply;
import com.portol.common.model.icon.IconRequest;
import com.portol.common.model.user.UserIcon;
import com.portol.mobileapi.config.IconServiceConfig;



public class IconService {

	private final IconServiceConfig conf;
	private final Client icComm; 
	
	
	public IconService(IconServiceConfig conf, Client icClient){
		this.conf = conf;
		this.icComm = icClient;
	}
	
	private static Logger logger = LoggerFactory.getLogger(IconService.class);
	
	public UserIcon getNewIcon(String key) throws IOException{
		
		
		IconRequest req = new IconRequest();
		req.setSeedData(key);
		req.setApiKey("bar");
		
		UriBuilder uribuild = UriBuilder.fromUri(conf.iconHost + ":" + conf.port + conf.newIconPath);
		IconReply resp = null;
		try {
		 resp = icComm.target(uribuild).request().post(Entity.json(req), IconReply.class);
		} catch (Exception e){
			logger.error("error retrieving icon from icon microservice. Using default Icon instead", e);
			 resp = new IconReply();
			resp.setUsingDefault(true);
			
			URL icon = getClass().getResource("/local/defaultUserIcon.png");
			byte[] defaultEncoded = Resources.toByteArray(icon);
			resp.setEncodedImage(defaultEncoded);
			
			
		}
		
		UserIcon ic = new UserIcon();
		if(!resp.isUsingDefault()){
		ic.setDescription("Icon for key: " + key);
		} else {
			ic.setDescription("DEFAULT ICON");
		}
		
		String strEncoded1 = Base64.getEncoder().encodeToString( resp.getEncodedImage());

		
		ic.setRawData(strEncoded1);
		ic.setType("image/png");
		
		return ic;
		
	}
	
	public byte[] getRawIcon(String key) throws IOException{
		IconRequest req = new IconRequest();
		req.setSeedData(key);
		req.setApiKey("bar");
		
		UriBuilder uribuild = UriBuilder.fromUri(conf.iconHost + ":" + conf.port + conf.newIconPath);
		
		IconReply resp = null;
		try{
			resp = icComm.target(uribuild).request().post(Entity.json(req), IconReply.class);
	} catch (Exception e){
		logger.error("error retrieving icon from icon microservice. Using default Icon instead", e);
		 resp = new IconReply();
		resp.setUsingDefault(true);
		
		URL icon = getClass().getResource("/local/defaultUserIcon.png");
		byte[] defaultEncoded = Resources.toByteArray(icon);
		resp.setEncodedImage(defaultEncoded);
		
		
	}
		return resp.getEncodedImage();
	}
	
	
}
