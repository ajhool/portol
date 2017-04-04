package com.portol.mobileapi.resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;
import com.portol.common.model.bookmark.Bookmark;
import com.portol.common.model.bookmark.BookmarkResponse;
import com.portol.common.model.content.Content;
import com.portol.common.model.player.Player;
import com.portol.common.model.user.User;
import com.portol.mobileapi.repository.ContentRepository;
import com.portol.mobileapi.repository.UserRepository;

@Path("/v0/bookmark")
public class BookmarkResource {

	private UserRepository userRepo;
	private ContentRepository contentRepo;
	private static final Logger logger = LoggerFactory.getLogger(BookmarkResource.class);
	@Context HttpServletResponse resp;

	public BookmarkResource(UserRepository userRepo,
			ContentRepository contentRepo) {
		super();
		this.userRepo = userRepo;
		this.contentRepo = contentRepo;
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/quickmark")
	@Timed
	public List<Bookmark> syncBookmarks(Bookmark newMark, @QueryParam("userId") String userId) throws Exception{

		//get user
		User updating = null;
		try {
			updating = userRepo.findOneById(userId);
		} catch (Exception e) {
			logger.error("error retrieving user", e);
		}

		if(updating == null){
			resp.sendError(404, "no user found for speicifed ID");
			return null;
		}

		//create merged bookmark list
		List<Bookmark> existing = updating.getBookmarked();
		int initialSize = updating.getBookmarked().size();
		//hacky, but easy to fix
		for(Bookmark exists:existing){
			if(exists.getBookmarkedContentId().equalsIgnoreCase(newMark.getBookmarkedContentId())){
				resp.sendError(Response.Status.NOT_MODIFIED.getStatusCode(), "user already has bookmark");
				return existing; 
			}
		}

		existing.add(newMark);
		int finalSize = updating.getBookmarked().size();

		//sanity check
		if(finalSize != initialSize + 1){
			throw new Exception("bookmark not added, server error");
		}

		//save user
		userRepo.save(updating);
		return existing;

	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed
	public BookmarkResponse bookmark(Player bReq, @Context HttpServletRequest req) throws Exception{

		//collect all the info that we need

		User wantsToBookmark = userRepo.findOneById(bReq.getLoggedIn().getUserId());
		//TODO work cookies in 
		if(wantsToBookmark == null){
			return new BookmarkResponse(false, "specified user not found");
		}


		Content toBookmark = contentRepo.findByVideoKey(bReq.getVideoKey());


		if(toBookmark == null){
			return new BookmarkResponse(false, "specified content not found");
		}


		List<Bookmark> userBookmarks = wantsToBookmark.getBookmarked();

		if(userBookmarks == null){
			userBookmarks = new ArrayList<Bookmark>();
		}

		for(Bookmark cur: userBookmarks){
			if(cur.getBookmarkedContentId().equalsIgnoreCase(toBookmark.getId())){
				return new BookmarkResponse(false, "content already bookmarked");
			}
		}

		//if we are still here, that means it doesnt exist in bookmarks so we are safe to add
		//userBookmarks = ArrayUtils.add(userBookmarks, new Bookmark(toBookmark.getId(), new Date(System.currentTimeMillis())));

		userBookmarks.add(new Bookmark(toBookmark.getId(), new Date(System.currentTimeMillis())));
		wantsToBookmark.setBookmarked(userBookmarks);
		userRepo.save(wantsToBookmark);

		return new BookmarkResponse(true, "new bookmark added");


	}


	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/{userId}/{videoKey}")
	@Timed
	public BookmarkResponse delete( @PathParam("userId") String userId,
			@PathParam("videoKey") String videoKey, @Context HttpServletRequest req) throws Exception{

		//collect all the info that we need

		User wantsToBookmark = userRepo.findOneById(userId);

		if(wantsToBookmark == null){
			return new BookmarkResponse(false, "specified user not found");
		}


		Content toBookmark = contentRepo.findByVideoKey(videoKey);


		if(toBookmark == null){
			return new BookmarkResponse(false, "specified content not found");
		}


		List<Bookmark> userExistingBookmarks = wantsToBookmark.getBookmarked();

		if(userExistingBookmarks == null|| userExistingBookmarks.size() == 0){
			return new BookmarkResponse(true, "user does not have bookmark");
		}


		for(int i = 0; i < userExistingBookmarks.size(); i++){
			if(userExistingBookmarks.get(i).getBookmarkedContentId().equalsIgnoreCase(toBookmark.getId())){
				//then we have found the bookmark that we want to delete

				userExistingBookmarks.remove(i);

				wantsToBookmark.setBookmarked(userExistingBookmarks);
				userRepo.save(wantsToBookmark);


				return new BookmarkResponse(true, "bookmark deleted successfully");

			}
		}


		return new BookmarkResponse(false, "bookmark not found");

	}

}
