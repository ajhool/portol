package com.portol.cdn.resource;

import java.util.ArrayList;
import java.util.Date;

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
import com.portol.cdn.config.EdgeServerResourceConfig;
import com.portol.cdn.repository.EdgeRepository;
import com.portol.common.model.cdn.CDNQuery;
import com.portol.common.model.cdn.CDNReply;
import com.portol.common.model.cdn.CDNReply.Status;
import com.portol.common.model.cdn.RegionalServer;

@Path("/api/v0/cdn")
public class RegionalServerResource {

	private static final Logger logger = LoggerFactory.getLogger(RegionalServerResource.class);

	private EdgeRepository edgeRepo;
	private EdgeServerResourceConfig config;

	public RegionalServerResource(EdgeServerResourceConfig conf, EdgeRepository edgeRepo) {
		this.edgeRepo = edgeRepo;
		this.config = conf;
	}

	@SuppressWarnings("unused")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/addupdateedge")
	@Timed
	public CDNReply registerEdgeServer(RegionalServer registering, @Context HttpServletRequest req) throws Exception {

		CDNReply reply = new CDNReply();
		if (registering == null) {
			throw new BadRequestException();
		}

		if (false) {
			throw new ForbiddenException();
		}

		Date expiry = new Date(System.currentTimeMillis() + (1000 * config.serverTTLseconds));

		reply.setExpirationDate(expiry);

		RegionalServer existing = edgeRepo.findOneById(registering.getId());

		if (existing == null) {
			reply.setStatus(CDNReply.Status.ADDED);
		} else {
			reply.setStatus(CDNReply.Status.UPDATED);
		}

		registering.setExpiration(expiry);
		edgeRepo.save(registering);

		return reply;

	}

	@SuppressWarnings("unused")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/edges")
	@Timed
	public CDNReply getEdges(CDNQuery cdnreq, @Context HttpServletRequest req) throws Exception {

		ArrayList<RegionalServer> results = new ArrayList<RegionalServer>();

		if (cdnreq == null) {
			throw new BadRequestException();
		}

		// TODO move to service
		if (false /* !mpdReq.getApiKey().equalsIgnoreCase("foo") */) {
			throw new ForbiddenException();
		}

		switch (cdnreq.getType()) {
		case ALL:
			results.addAll(edgeRepo.getAll());
			break;
		case FAST: // single CDN, make best guess
			break;
		case LOCALS:
			break;
		default:
			break;

		}

		CDNReply reply = new CDNReply();
		reply.setExpirationDate(new Date(System.currentTimeMillis() + 900 * 1000));
		reply.setResults(results);
		reply.setStatus(Status.QUERY_SUCCESS);

		return reply;
	}

}
