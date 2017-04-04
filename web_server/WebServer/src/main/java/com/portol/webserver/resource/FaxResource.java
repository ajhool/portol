package com.portol.webserver.resource;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
import com.portol.webserver.CachedAsset;

import io.dropwizard.servlets.assets.ByteRange;

@Path("/moviefax")
public class FaxResource {

	File faxRoot; 

	@Context
	HttpServletResponse resp; 

	private static final MediaType DEFAULT_MEDIA_TYPE = MediaType.HTML_UTF_8;

	private final Charset defaultCharset;

	public FaxResource(File faxRoot, Charset defaultCharset) {
		super();
		this.faxRoot = faxRoot;
		this.defaultCharset = defaultCharset;
	}
	private boolean isCachedClientSide(HttpServletRequest req, CachedAsset cachedAsset) {
		return cachedAsset.getETag().equals(req.getHeader(HttpHeaders.IF_NONE_MATCH)) ||
				(req.getDateHeader(HttpHeaders.IF_MODIFIED_SINCE) >= cachedAsset.getLastModifiedTime());
	}

	private CachedAsset loadAsset(String key) throws URISyntaxException, IOException {

		if(key == null || key.equals("/")){
			key = "index.html";
		}
		final String absoluteRequestedResourcePath = faxRoot.getAbsolutePath() + "/" + key;

		long lastModified = new File(absoluteRequestedResourcePath).lastModified();
		if (lastModified < 1) {
			// Something went wrong trying to get the last modified time: just use the current time
			lastModified = System.currentTimeMillis();
		}

		// zero out the millis since the date we get back from If-Modified-Since will not have them
		lastModified = (lastModified / 1000) * 1000;

		File requested = new File(absoluteRequestedResourcePath);
		if(!requested.exists()){
			return null;
		}
		return new CachedAsset(org.apache.commons.io.FileUtils.readFileToByteArray(requested), lastModified);
	}
	
	@POST
	@Consumes("text/xml")
	@Timed
	public String addFact(@QueryParam("factId") String factId, String factHTML) throws IOException{
		
		if(factId == null){
			resp.sendError(400, "need id field");
			return null;
		}
		
		final String absoluteRequestedResourcePath = faxRoot.getAbsolutePath() + "/" + factId + ".html";
		
		File target = new File(absoluteRequestedResourcePath);
		
		if (!target.exists()) {
			target.createNewFile();
		}

		FileWriter fw = new FileWriter(target.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(factHTML);
		bw.close();
		
		return factId; 
		
	}
	
	@GET
	@Timed
	public void serveFax(@QueryParam("factId") String factId, @Context HttpServletRequest req) throws Exception{

		try {

			final CachedAsset cachedAsset = loadAsset(factId + ".html");
			if (cachedAsset == null) {
				resp.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}

			if (isCachedClientSide(req, cachedAsset)) {
				resp.sendError(HttpServletResponse.SC_NOT_MODIFIED);
				return;
			}

			final String rangeHeader = req.getHeader(HttpHeaders.RANGE);

			final int resourceLength = cachedAsset.getResource().length;
			ImmutableList<ByteRange> ranges = ImmutableList.of();

			boolean usingRanges = false;
			// Support for HTTP Byte Ranges
			// http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html
			if (rangeHeader != null) {

				final String ifRange = req.getHeader(HttpHeaders.IF_RANGE);

				if (ifRange == null || cachedAsset.getETag().equals(ifRange)) {

					try {
						ranges = parseRangeHeader(rangeHeader, resourceLength);
					} catch (NumberFormatException e) {
						resp.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
						return ;
					}

					if (ranges.isEmpty()) {
						resp.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
						return;
					}

					resp.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
					usingRanges = true;

					resp.addHeader(HttpHeaders.CONTENT_RANGE, "bytes "
							+ Joiner.on(",").join(ranges) + "/" + resourceLength);
				}
			}

			resp.setDateHeader(HttpHeaders.LAST_MODIFIED, cachedAsset.getLastModifiedTime());
			resp.setHeader(HttpHeaders.ETAG, cachedAsset.getETag());

			final String mimeTypeOfExtension = req.getServletContext()
					.getMimeType(req.getRequestURI());
			MediaType mediaType = DEFAULT_MEDIA_TYPE;

			if (mimeTypeOfExtension != null) {
				try {
					mediaType = MediaType.parse(mimeTypeOfExtension);
					if (defaultCharset != null && mediaType.is(MediaType.ANY_TEXT_TYPE)) {
						mediaType = mediaType.withCharset(defaultCharset);
					}
				} catch (IllegalArgumentException ignore) {}
			}

			if (mediaType.is(MediaType.ANY_VIDEO_TYPE)
					|| mediaType.is(MediaType.ANY_AUDIO_TYPE) || usingRanges) {
				resp.addHeader(HttpHeaders.ACCEPT_RANGES, "bytes");
			}

			resp.setContentType(mediaType.type() + '/' + mediaType.subtype());

			if (mediaType.charset().isPresent()) {
				resp.setCharacterEncoding(mediaType.charset().get().toString());
			}

			try (ServletOutputStream output = resp.getOutputStream()) {
				if (usingRanges) {
					for (final ByteRange range : ranges) {
						output.write(cachedAsset.getResource(), range.getStart(),
								range.getEnd() - range.getStart() + 1);
					}
				}
				else {
					output.write(cachedAsset.getResource());
				}
			}
			
		} catch (RuntimeException | URISyntaxException ignored) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
		}


	}




	/**
	 * Parses a given Range header for one or more byte ranges.
	 *
	 * @param rangeHeader Range header to parse
	 * @param resourceLength Length of the resource in bytes
	 * @return List of parsed ranges
	 */
	private ImmutableList<ByteRange> parseRangeHeader(final String rangeHeader,
			final int resourceLength) {
		final ImmutableList.Builder<ByteRange> builder = ImmutableList.builder();
		if (rangeHeader.indexOf("=") != -1) {
			final String[] parts = rangeHeader.split("=");
			if (parts.length > 1) {
				final List<String> ranges = Splitter.on(",").trimResults().splitToList(parts[1]);
				for (final String range : ranges) {
					builder.add(ByteRange.parse(range, resourceLength));
				}
			}
		}
		return builder.build();
	}

}
