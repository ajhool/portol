import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.glassfish.jersey.client.ClientResponse;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.common.io.Resources;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import com.portol.common.model.Category;
import com.portol.common.model.content.CategoryReference;
import com.portol.common.model.content.Content;
import com.portol.common.model.content.ContentMetadata;
import com.portol.common.model.content.ContentSource;
import com.portol.common.model.content.meta.EPGBar;
import com.portol.common.model.content.meta.EPGItem;
import com.portol.common.model.content.meta.Pricing;
import com.portol.common.model.content.meta.Rating;
import com.portol.common.model.content.meta.SeriesInfo;
import com.portol.common.model.dash.jaxb.AdaptationSetType;
import com.portol.common.model.dash.jaxb.DescriptorType;
import com.portol.common.model.dash.jaxb.MPDtype;
import com.portol.common.model.dash.jaxb.PeriodType;
import com.portol.common.model.dash.jaxb.PresentationType;
import com.portol.common.model.dash.jaxb.RepresentationType;
import com.portol.common.model.dash.jaxb.SegmentTemplateType;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;


public class PreviewRegenerator {

	private static final String CONTENT_SERVER = "http://portol.info";
	private static final int CONTENT_PORT = 9797;

	private static CategoryReference featured = new CategoryReference("a5642ceb-4ed8-4919-833b-a668968962a0");
	private static CategoryReference onDemand = new CategoryReference("1526f1c7-9655-490b-9a2a-cc26c374cdef");
	private static CategoryReference funny = new CategoryReference("97cc3b5a-3b9f-416c-9d28-14020f5d8d3d");
	private static CategoryReference classic = new CategoryReference("37a03ac6-71d9-49ee-b9cb-6df3fdac1435");
	private static CategoryReference newVids = new CategoryReference("f92dfdc0-b9b0-4c1d-a1eb-9f34d2b183d3");
	private static CategoryReference foreign = new CategoryReference("2f1e6beb-6fe1-4003-863c-099bbb18ccce");
	private static CategoryReference silent = new CategoryReference("7d276a26-8b2c-4cf2-bf16-c4c3a80892a5");

	

	public static void main(String[] args) throws Exception{
		MongoManaged contentMongo = new MongoManaged(new MongoContentConfigPrefilled());
		
		ContentRepository contentRepo = new ContentRepository(contentMongo);
		
		String keyToRegenerate = args[0];
		
		Content content = contentRepo.findByVideoKey(keyToRegenerate);
		
		
		try {
			content.regeneratePreviewMPD();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("regenerated preview successfully");

		contentRepo.save(content);
		

	}







}
