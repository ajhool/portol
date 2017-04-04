package com.portol.cloudplayer.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.UriBuilder;

import org.apache.http.client.HttpClient;

import io.dropwizard.lifecycle.Managed;

import com.portol.cloudplayer.config.BackboneServiceConfig;
import com.portol.cloudplayer.runnable.SegmentGetterThread;
import com.portol.common.model.FileInfoRequest;
import com.portol.common.model.FileInfoResponse;
import com.portol.common.model.content.Content;
import com.portol.common.model.dash.jaxb.RepresentationType;
import com.portol.common.model.instance.BackendInstance;


public class BackboneService implements Managed{
	
	private SegmentGetterThread segmentGetter; 
	private final BackboneServiceConfig config;
	private HttpClient httpClient;
	private Client jClient;
	private Content target;
	private MPDService mpdsvc;
	BackendInstance local;

	public BackboneService(BackboneServiceConfig thisConfig,
			HttpClient httpClient, Client jClient, MPDService mpdSvc) {
		super();
		this.mpdsvc = mpdSvc;
		this.config = thisConfig;
		this.httpClient = httpClient;
		this.jClient = jClient;
	}
	

	public boolean beginLiveDownload(Content target, BackendInstance src) {
		this.target = target;
		 local = src;
		segmentGetter = new SegmentGetterThread(httpClient, target.getMPDInfo(), mpdsvc, src, target.getNameScheme());
		
		segmentGetter.startDownload();
		
		return true;
		
	}

	private boolean verifyIntegrity(File toCheck, RepresentationType parent) throws IOException {
		String liveHost = local.getLocation();
		int port = local.getPort();
		String path = config.md5Path; 
		String protocol = local.getProtocol();
	
		
		UriBuilder uribuild = UriBuilder.fromUri(protocol + liveHost + ":" + port + path);
		
		FileInfoRequest req = new FileInfoRequest();
		req.setRepresentation(parent.getId());
		req.setFileName(toCheck.getName());
		
		FileInfoResponse resp = jClient.target(uribuild).request().post(Entity.json(req), FileInfoResponse.class);

		
		String local = this.calculateMD5(toCheck);
		
		if(local.compareTo(resp.getMD5()) == 0){
			//they are the same
			return true;
		} else return false;
		
		
		
	}


	public Content getCurrentContent() {
		return target;
	}

	@Override
	public void start() throws Exception {
		
	}
	
	private synchronized String calculateMD5(File ofInterest) throws IOException{
		FileInputStream fis = new FileInputStream(ofInterest);
		String md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
		fis.close();
		
		return md5;
		
	}

	@Override
	public void stop() throws Exception {
		if(segmentGetter == null){
			return;
		}
		segmentGetter.kill();
	}

}
