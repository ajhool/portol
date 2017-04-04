package com.portol.mobileapi.resource;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;
import com.portol.common.model.content.Content;
import com.portol.common.model.content.ContentMetadata;
import com.portol.common.model.content.ContentSearchRequest;
import com.portol.common.model.content.ContentSearchRequest.RequestType;
import com.portol.mobileapi.repository.ContentRepository;
import com.portol.mobileapi.repository.MetadataRepository;

//use this to seach for content on the mobile app
//can also get top videos, etc...
@Path("/v0/contentFind")
public class ContentFinderResource {

	private ContentRepository contentRepo;
	private MetadataRepository splashRepo;

	public ContentFinderResource(ContentRepository contentrepo,
			MetadataRepository splashrepo) {
		super();
		this.contentRepo = contentrepo;
		this.splashRepo = splashrepo;
	}


	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed
	public List<ContentMetadata> searchContent(ContentSearchRequest req) throws Exception{

		RequestType reqType = req.getType();

		switch(reqType){
		case LIVE:
			return getAllLive(req);
		case VOD:
			return getAllVOD(req);
		case ALL:
			return getAll(req);
			//search by view count
		case MORE_THAN:
		case LESS_THAN:
		case WITH_ID:
			return getForIds(req);
		
		default:
			//return everything
			return getAll(req);

		}

	}


	private List<ContentMetadata> getForIds(ContentSearchRequest req) throws Exception {
		
		ArrayList<ContentMetadata> metas = new ArrayList<ContentMetadata>();
		
		for(String parentContentId : req.getContentId()){
			
			ContentMetadata matching = splashRepo.getMetadataForParent(parentContentId);
			metas.add(matching);
			
			
		}
		
		return metas;
	}


	private List<ContentMetadata> getAllLive(ContentSearchRequest req) throws Exception {
		List<Content> noSplash = contentRepo.getAllWithType(req.getSortType(), req.getPageIndex(), Content.Type.LIVE);
		//add splash screen data
		ArrayList<ContentMetadata> metas = new ArrayList<ContentMetadata>();
		for(int i = 0; i < noSplash.size(); i++){
			ContentMetadata matching = splashRepo.getSplashScreenById(noSplash.get(i).getSplashDataId());
			metas.add(matching);

		}
		return metas;
	}

	//search by VOD
	private List<ContentMetadata> getAllVOD(ContentSearchRequest req) throws Exception {
		List<Content> noSplash = contentRepo.getAllWithType(req.getSortType(), req.getPageIndex(), Content.Type.VOD);
		//add splash screen data
		ArrayList<ContentMetadata> metas = new ArrayList<ContentMetadata>();
		for(int i = 0; i < noSplash.size(); i++){
			ContentMetadata matching = splashRepo.getSplashScreenById(noSplash.get(i).getSplashDataId());
			metas.add(matching);

		}
		return metas;
	}


	//get all
	private List<ContentMetadata> getAll(ContentSearchRequest req) throws Exception {
		List<Content> noSplash = contentRepo.getAllContent(req.getSortType(), req.getPageIndex());

		//add splash screen data
		ArrayList<ContentMetadata> metas = new ArrayList<ContentMetadata>();
		for(int i = 0; i < noSplash.size(); i++){
			ContentMetadata matching = splashRepo.getSplashScreenById(noSplash.get(i).getSplashDataId());
			metas.add(matching);

		}

		return metas;
	}


}
