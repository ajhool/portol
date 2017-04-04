package com.portol.identicon.resource;


import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;
import com.portol.common.model.icon.IconReply;
import com.portol.common.model.icon.IconRequest;
import com.portol.identicon.IdenticonRenderer;
import com.portol.identicon.IdenticonUtil;
import com.portol.identicon.NineBlockIdenticonRenderer2;
import com.portol.identicon.cache.IdenticonCache;
import com.portol.identicon.config.IconGeneratorConfig;

@Path("/api/v0/icongen")
public class IdenticonResource {



	private static final Logger logger = LoggerFactory.getLogger(IdenticonResource.class);

	public IdenticonResource(IconGeneratorConfig conf) {

	}

	@SuppressWarnings("unused")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/newicon")
	@Timed
	public IconReply printMainMPD(IconRequest iconReq, @Context HttpServletRequest req) throws Exception{

		IconReply reply = new IconReply();
		if(iconReq == null){
			throw new BadRequestException();
		}

		//TODO move to service
		if(false){
			throw new ForbiddenException();
		}

		byte[] encodedImg = makeIdenticon(iconReq.getSeedData());

		reply.setEncodedImage(encodedImg);
		return reply;


	}
	
	private IdenticonRenderer renderer = new NineBlockIdenticonRenderer2();

	private IdenticonCache cache;
	
	private int version = 1;
	
	private static final String IDENTICON_IMAGE_FORMAT = "PNG";

	private static final String IDENTICON_IMAGE_MIMETYPE = "iicmage/png";
	
	private byte[] makeIdenticon(String seedData) throws Exception {
		
		int code = IdenticonUtil.getIdenticonCode(seedData);
		int size = 256;
		
		String identiconETag = IdenticonUtil.getIdenticonETag(code, size,
				version);

			byte[] imageBytes = null;
			// retrieve image bytes from either cache or renderer
			if (cache == null
					|| (imageBytes = cache.get(identiconETag)) == null) {
				ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
				RenderedImage image = renderer.render(code, size);
				ImageIO.write(image, IDENTICON_IMAGE_FORMAT, byteOut);
				imageBytes = byteOut.toByteArray();
				if (cache != null) {
					cache.add(identiconETag, imageBytes);
				}
			}
		
		return imageBytes;
	}

	


}
