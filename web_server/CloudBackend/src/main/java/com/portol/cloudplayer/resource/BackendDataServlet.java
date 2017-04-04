package com.portol.cloudplayer.resource;

import io.dropwizard.servlets.assets.ByteRange;
import io.dropwizard.servlets.assets.ResourceURL;

import javax.ws.rs.client.Client;

import com.portol.backend.cloud.service.LiveContentDownloaderService;
import com.portol.cloudplayer.service.ReturnToQService;
import com.portol.common.model.instance.BackendInstance;
import com.portol.common.model.instance.EdgeInstance;
import com.portol.common.model.player.Player;
import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.hash.Hashing;
import com.google.common.io.Resources;
import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;

import javax.servlet.AsyncContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.checkArgument;

public class BackendDataServlet extends HttpServlet {

	private static final long serialVersionUID = 6493345594784987908L;
	private static final CharMatcher SLASHES = CharMatcher.is('/');
	public static final Logger logger = LoggerFactory.getLogger(BackendDataServlet.class);

	private static class CachedAsset {
		private final byte[] resource;
		private final String eTag;
		private final long lastModifiedTime;

		private CachedAsset(byte[] resource, long lastModifiedTime) {
			this.resource = resource;
			this.eTag = '"' + Hashing.murmur3_128().hashBytes(resource).toString() + '"';
			this.lastModifiedTime = lastModifiedTime;
		}

		public byte[] getResource() {
			return resource;
		}

		public String getETag() {
			return eTag;
		}

		public long getLastModifiedTime() {
			return lastModifiedTime;
		}
	}

	private static final MediaType DEFAULT_MEDIA_TYPE = MediaType.HTML_UTF_8;
	private final String resourcePath;
	private final String uriPath;
	private final String indexFile;
	private final Client jClient;
	private final Charset defaultCharset;
	private BackendInstance _this;
	private ReturnToQService returnsvc;

	private LiveContentDownloaderService liveContentDL;

	/**
	 * Creates a new {@code AssetServlet} that serves static assets loaded from
	 * {@code resourceURL} (typically a file: or jar: URL). The assets are
	 * served at URIs rooted at {@code uriPath}. For example, given a
	 * {@code resourceURL} of {@code "file:/data/assets"} and a {@code uriPath}
	 * of {@code "/js"}, an {@code AssetServlet} would serve the contents of
	 * {@code
	 * /data/assets/example.js} in response to a request for
	 * {@code /js/example.js}. If a directory is requested and {@code indexFile}
	 * is defined, then {@code AssetServlet} will attempt to serve a file with
	 * that name in that directory. If a directory is requested and {@code
	 * indexFile} is null, it will serve a 404.
	 *
	 * @param resourcePath
	 *            the base URL from which assets are loaded
	 * @param uriPath
	 *            the URI path fragment in which all requests are rooted
	 * @param indexFile
	 *            the filename to use when directories are requested, or null to
	 *            serve no indexes
	 * @param defaultCharset
	 *            the default character set
	 * @param jClient
	 * @param localPlayers
	 * @param _this2
	 * @param liveContentDL
	 * @param returnSvc
	 */
	public BackendDataServlet(String resourcePath, String uriPath, String indexFile, Charset defaultCharset,
			Client jClient, BackendInstance _this2, LiveContentDownloaderService liveContentDL,
			ReturnToQService returnSvc) {
		final String trimmedPath = SLASHES.trimFrom(resourcePath);
		this.resourcePath = trimmedPath.isEmpty() ? trimmedPath : trimmedPath + '/';
		final String trimmedUri = SLASHES.trimTrailingFrom(uriPath);
		this.uriPath = trimmedUri.isEmpty() ? "/" : trimmedUri;
		this.indexFile = indexFile;
		this.liveContentDL = liveContentDL;
		this.defaultCharset = defaultCharset;
		this.jClient = jClient;
		this.returnsvc = returnSvc;
		this._this = _this2;
	}

	public URL getResourceURL() {
		return Resources.getResource(resourcePath);
	}

	public String getUriPath() {
		return uriPath;
	}

	public String getIndexFile() {
		return indexFile;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		String apiKey = req.getParameter("apiKey");
		String repId = req.getParameter("repId");

		// level 1 verification

		boolean requestValid = verifyRequest(apiKey, req);


		returnsvc.resetTimeout();

		if (!requestValid) {
			logger.info("failed verification. RequestValid: " + requestValid + " IP valid: NOT YET IMPLEMENTED");
			resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

		try {
			final StringBuilder builder = new StringBuilder(req.getServletPath());
			if (req.getPathInfo() != null) {
				builder.append(req.getPathInfo());
			}
			final CachedAsset cachedAsset = loadAsset(builder.toString());
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
						return;
					}
					if (ranges.isEmpty()) {
						resp.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
						return;
					}
					resp.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
					usingRanges = true;
					resp.addHeader(HttpHeaders.CONTENT_RANGE,
							"bytes " + Joiner.on(",").join(ranges) + "/" + resourceLength);
				}
			}
			resp.setDateHeader(HttpHeaders.LAST_MODIFIED, cachedAsset.getLastModifiedTime());
			resp.setHeader(HttpHeaders.ETAG, cachedAsset.getETag());
			String memtyp = req.getRequestURI();
			ServletContext ctxt = req.getServletContext();
			final String mimeTypeOfExtension = "video/MP2T";
			MediaType mediaType = DEFAULT_MEDIA_TYPE;
			if (mimeTypeOfExtension != null) {
				try {
					mediaType = MediaType.parse(mimeTypeOfExtension);
					if (defaultCharset != null && mediaType.is(MediaType.ANY_TEXT_TYPE)) {
						mediaType = mediaType.withCharset(defaultCharset);
					}
				} catch (IllegalArgumentException ignore) {
				}
			}
			if (mediaType.is(MediaType.ANY_VIDEO_TYPE) || mediaType.is(MediaType.ANY_AUDIO_TYPE) || usingRanges) {
				resp.addHeader(HttpHeaders.ACCEPT_RANGES, "bytes");
			}
			resp.setContentType(mediaType.type() + '/' + mediaType.subtype());
			if (mediaType.charset().isPresent()) {
				resp.setCharacterEncoding(mediaType.charset().get().toString());
			}
			resp.setContentLength(cachedAsset.getResource().length);
			ServletOutputStream output = resp.getOutputStream();

			output.write(cachedAsset.getResource(), 0, cachedAsset.getResource().length);
		
		} catch (RuntimeException | URISyntaxException | IOException ignored) {
			try {
				System.err.println(ignored);
				resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			} catch (IOException e) {
		
				e.printStackTrace();
			}
		}

	}

	private boolean verifyRequest(String apiKey, HttpServletRequest req) {
		return true;

	}

	private CachedAsset loadAsset(String key) throws URISyntaxException, IOException {
		checkArgument(key.startsWith(uriPath));
		final String requestedResourcePath = SLASHES.trimFrom(key.substring(uriPath.length()));
		final String absoluteRequestedResourcePath = SLASHES.trimFrom(this.resourcePath + requestedResourcePath);

		final String javaRootRelative =absoluteRequestedResourcePath;
		File toRead = new File(javaRootRelative);
		URL requestedResourceURL = toRead.toURI().toURL();
		if (ResourceURL.isDirectory(requestedResourceURL)) {
			if (indexFile != null) {
				requestedResourceURL = Resources.getResource(absoluteRequestedResourcePath + '/' + indexFile);
			} else {
				// directory requested but no index file defined
				return null;
			}
		}
		long lastModified = ResourceURL.getLastModified(requestedResourceURL);
		if (lastModified < 1) {
			// Something went wrong trying to get the last modified time: just
			// use the current time
			lastModified = System.currentTimeMillis();
		}
		// zero out the millis since the date we get back from If-Modified-Since
		// will not have them
		lastModified = (lastModified / 1000) * 1000;
		return new CachedAsset(Files.readAllBytes(toRead.toPath()), lastModified);
	}

	private boolean isCachedClientSide(HttpServletRequest req, CachedAsset cachedAsset) {
		return cachedAsset.getETag().equals(req.getHeader(HttpHeaders.IF_NONE_MATCH))
				|| (req.getDateHeader(HttpHeaders.IF_MODIFIED_SINCE) >= cachedAsset.getLastModifiedTime());
	}

	/**
	 * Parses a given Range header for one or more byte ranges.
	 *
	 * @param rangeHeader
	 *            Range header to parse
	 * @param resourceLength
	 *            Length of the resource in bytes
	 * @return List of parsed ranges
	 */
	private ImmutableList<ByteRange> parseRangeHeader(final String rangeHeader, final int resourceLength) {
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
