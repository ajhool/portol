package com.portol.mobileapi.resource;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;
import com.portol.common.model.Category;
import com.portol.common.model.CategorySearchRequest;
import com.portol.common.model.CategorySearchRequest.RequestType;
import com.portol.mobileapi.repository.CategoryRepository;



@Path("/v0/categories")
public class CategoryResource {

	private CategoryRepository catRepo;

	public CategoryResource(CategoryRepository catRepo){
		super();
		this.catRepo = catRepo;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/general")
	@Timed
	List<Category> getCurrentList(@Context HttpServletRequest req) throws IOException{
		
		CategorySearchRequest reqFake = new CategorySearchRequest();
		reqFake.setType(null);
		return getAllCategories(reqFake);
		
	}
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed
	public List<Category> getAllCategories(CategorySearchRequest req) throws IOException {

		RequestType reqType = req.getType();

		if(reqType == null){
			return getAll(req);
		}
		
		switch(reqType){

		case VIDEO:
		case MUSIC:
		case TEXT:
		default:
			//return everything
			return getAll(req);

		}


	}

	private List<Category> getVideoCats() {
		return null;
	}



	private List<Category> getAll(CategorySearchRequest req) throws IOException {
		List<Category> results = catRepo.getAllValidCategories();
		return results;
	}

}
