package com.portol.cloudplayer.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;
import com.portol.backend.cloud.service.LiveContentDownloaderService;
import com.portol.common.model.FileInfoRequest;
import com.portol.common.model.FileInfoResponse;
@Path("/api/v0/file")
public class FileInfoResource {
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(FileInfoResource.class);
	
	public FileInfoResource(){
	}
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed
	@Path("/info")
	public FileInfoResponse getInfo(FileInfoRequest req) throws IOException{
		boolean valid = validateRequest(req);
		
		if(!valid){
			throw new javax.ws.rs.ForbiddenException("API key invalid");
		}
		FileInfoResponse resp = new FileInfoResponse();
		
		File ofInterest = new File("streams/" + req.getRepresentation() + "/" + req.getFileName());
		
		if(!ofInterest.exists()){
			return null;
		}
		
		//calculate MD5
		FileInputStream fis = new FileInputStream(ofInterest);
		String md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
		fis.close();
		
		resp.setMD5(md5);
	
		return resp;
		
	}



	private boolean validateRequest(FileInfoRequest req) {
		
		if(!req.getApiKey().equalsIgnoreCase("REDACTED")) {
		return false;
		} else return true;
	}
	
	
}
