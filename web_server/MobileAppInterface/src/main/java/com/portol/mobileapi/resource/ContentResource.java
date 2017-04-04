package com.portol.mobileapi.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;
import com.portol.common.model.content.Content;
import com.portol.common.model.content.ContentMetadata;
import com.portol.common.model.content.ContentSearchRequest;
import com.portol.common.model.content.ContentSearchRequest.RequestType;
import com.portol.mobileapi.repository.ContentRepository;
import com.portol.mobileapi.repository.MetadataRepository;

//use this to seach for content on the mobile app
//can also get top videos, etc...
@Path("/v0/content")
public class ContentResource {

	private ContentRepository contentRepo;
	private MetadataRepository splashRepo;

	private static final Logger logger = LoggerFactory.getLogger(ContentResource.class);
	
	public ContentResource(ContentRepository contentrepo,
			MetadataRepository splashrepo) {
		super();
		this.contentRepo = contentrepo;
		this.splashRepo = splashrepo;
	}

	@GET
	@Path("/category/{categoryKey}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed
	public List<ContentMetadata> byCategory(@PathParam("categoryKey") String categoryKey) throws Exception{
	
		List<ContentMetadata> results = splashRepo.getContentInCategory(categoryKey);
		return results;
	}
	
	@GET
	@Path("/key/{contentKey}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed
	public ContentMetadata codeInfo(@PathParam("contentKey") String contentKey) throws Exception{

		return splashRepo.getMetadataForParentKey(contentKey);
	}
	
	@GET
	@Path("/key")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed
	public ContentMetadata codeInfoQuery(@QueryParam("contentKey") String contentKey) throws Exception{

		return splashRepo.getMetadataForParentKey(contentKey);
	}
	
	@GET
	@Path("/user")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed
	public List<ContentMetadata> byUser(@QueryParam("userId") String userKey) throws Exception{
	
		List<Content> owned = contentRepo.findByOwnerKey(userKey);
		
		ArrayList<ContentMetadata> metas = new ArrayList<ContentMetadata>();
		
		for(Content cont : owned){
			metas.add(splashRepo.getMetadataForParent(cont.getId()));
		}
		
		return metas;
	}
	
	
	@GET
	@Path("/quickpick")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed
	public List<ContentMetadata> quickPick(@QueryParam("num") String numberToPick) throws Exception{

		List<ContentMetadata> all = splashRepo.getAll();
		
		List<ContentMetadata> toRet = new ArrayList<ContentMetadata>();
		
		int numToReturn = 10;
		try {
			numToReturn = Integer.parseInt(numberToPick);
		} catch (Exception e){
			logger.warn("invalid numToPick: " + numberToPick, e);
		}
		
		Random rand = new Random();
		while(toRet.size() <= numToReturn && all.size() > 0){
			
			int next = rand.nextInt(all.size());
			
			ContentMetadata toAdd = all.remove(next);
			toRet.add(toAdd);
			
			
		}
		
		return toRet; 
	}
	

}
