package com.portol.cloudplayer.resource;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.codahale.metrics.annotation.Timed;
import com.portol.cloudplayer.Marshal;
import com.portol.cloudplayer.repository.ContentRepository;
import com.portol.cloudplayer.repository.LocalPlayerRepository;
import com.portol.cloudplayer.service.MPDService;
import com.portol.common.model.ComponentRequest;
import com.portol.common.model.MovieFact;
import com.portol.common.model.content.Content;
import com.portol.common.model.content.Content.Type;
import com.portol.common.model.dash.jaxb.AdaptationSetType;
import com.portol.common.model.dash.jaxb.BaseURLType;
import com.portol.common.model.dash.jaxb.MPDtype;
import com.portol.common.model.dash.jaxb.ObjectFactory;
import com.portol.common.model.dash.jaxb.RepresentationType;
import com.portol.common.model.dash.jaxb.SegmentTimelineType;
import com.portol.common.model.instance.EdgeInstance;
import com.portol.common.model.player.Player;

@Path("/api/v0/module")
public class ModuleAPIResource {

	private EdgeInstance _this;
	private LocalPlayerRepository playerRepo;
	private static Logger logger = LoggerFactory.getLogger(ModuleAPIResource.class);



	public ModuleAPIResource(EdgeInstance _this, LocalPlayerRepository playerRepo){
		this._this = _this;
		this.playerRepo = playerRepo;

	}

	@Context HttpServletResponse resp;
	
	@GET
	@Path("/moviefax")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed
	public List<MovieFact> sendvodMPD(@QueryParam("playerId") String playerId, @Context HttpServletRequest request) throws Exception{
		
		if(playerRepo.findOneById(playerId) == null){
			resp.sendError(401);
			return null;
		}
		
		return _this.getServing().getMovieFax();
	}



}
