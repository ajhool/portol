import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.core.MediaType;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.portol.common.model.MovieFact;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;

public class FaxAdder {

	public static final String FAX_URL = "http://159.203.91.129";

	public static final String FAX_ADD_URL = FAX_URL + "/moviefax";
	public static final String FAX_ADD_QUERY_PARAM = "?factId=";
	
	private static final String CONTENT_SERVER = "http://portol.info";
	private static final int CONTENT_PORT = 9797;
	
	
	private static MovieFact createWelcomeFact(String movieName){
		MovieFact welcome = new MovieFact();
		welcome.setStartTime(0);
		welcome.setDuration(10);

		String welcomeMessage = "Welcome to moviefax for " + movieName; 
		String HTML = wrapFactInHtml(welcomeMessage);

		String extension = "";
		try {
			extension = pushMovieFactData(welcome, HTML);
		} catch (Exception e) {

			e.printStackTrace();
			System.exit(-1);
		} 

		String url = FAX_ADD_URL +  FAX_ADD_QUERY_PARAM + extension;
		welcome.setUrl(url);
		return welcome; 
	}

	private static MovieFact createFact(String contents, int startTime, int duration){
		MovieFact welcome = new MovieFact();
		welcome.setStartTime(startTime);
		welcome.setDuration(duration);

		String HTML = wrapFactInHtml(contents);

		String extension = "";
		try {
			extension = pushMovieFactData(welcome, HTML);
		} catch (Exception e) {

			e.printStackTrace();
			System.exit(-1);
		} 

		String url = FAX_ADD_URL + FAX_ADD_QUERY_PARAM + extension;
		welcome.setUrl(url);
		return welcome; 
	}

	private static String wrapFactInHtml(String fact){
		StringBuffer builder = new StringBuffer();
		builder.append("<html>\n");

		builder.append("<head>\n");
		builder.append("<title> ViewScape MovieFax </title>\n");
		builder.append("</head>\n");

		builder.append("<body bgcolor=\"white\" text=\"blue\">\n");
		builder.append(fact);
		builder.append("</body>\n");

		builder.append("</html>\n");
		return builder.toString();
	}

	private static String pushMovieFactData(MovieFact toAdd, String HTML) throws NoSuchAlgorithmException, KeyManagementException{

		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager(){
			@Override
			public void checkClientTrusted(
					java.security.cert.X509Certificate[] arg0, String arg1)
							throws CertificateException {
				// TODO Auto-generated method stub

			}
			@Override
			public void checkServerTrusted(
					java.security.cert.X509Certificate[] arg0, String arg1)
							throws CertificateException {
				// TODO Auto-generated method stub

			}
			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				// TODO Auto-generated method stub
				return null;
			}
		}};

		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, new SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
			;
		}

		ClientConfig clientc = new DefaultClientConfig();
		SSLContext ctx = SSLContext.getInstance("SSL");
		ctx.init(null, trustAllCerts, null);



		HostnameVerifier allHostsValid = new HostnameVerifier() {


			@Override


			public boolean verify(String arg0, SSLSession arg1) {
				// TODO Auto-generated method stub
				return true;
			}
		};

		HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
		clientc.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, new HTTPSProperties(allHostsValid, ctx));
		ObjectMapper mapper = new ObjectMapper();
		//mapper.getDeserializationConfig().addMixInAnnotations(XMLGregorianCalendar.class, MixIn.class);
		clientc.getSingletons().add(mapper);
		clientc.getClasses().add(JacksonJsonProvider.class);

		Client client = Client.create(clientc);

		WebResource webResource = client.resource(FAX_ADD_URL + FAX_ADD_QUERY_PARAM + toAdd.getFactId());
		String response = webResource.accept(MediaType.TEXT_HTML).entity(HTML, MediaType.TEXT_XML).post(String.class);
		return response;


	}

	public static ArrayList<MovieFact> addCostaRicaFax(){
		
		ArrayList<MovieFact> fax = new ArrayList<MovieFact>();
		//total length 257

		MovieFact fact1 = createWelcomeFact("Aidan Costa Rica Movie");
		fax.add(fact1);
		
		MovieFact fact2 = createFact("Costa Rica is very vulnerable to invasion since it has no military", 11, 10);
		fax.add(fact2);
		
		//free after 21
		MovieFact fact3 = createFact("Drugs can pass through Costa Rica on their way to the United States", 25, 10);
		fax.add(fact3);
		
		//free after 35
		MovieFact fact4 = createFact("Running water is considered a luxury in this wild land", 36, 10);
		fax.add(fact4);
		
		//free after 46
		MovieFact fact5 = createFact("This was a very dangerous trip - Costa Ricans have robbed tourists, and even committed murders!", 48, 10);
		fax.add(fact5);
		
		//free after 59
		MovieFact fact6 = createFact("Wild dogs roam at will through the streets in Costa Rica, a good source of food for many families", 59, 10);
		fax.add(fact6);
		
		//free after 70
		MovieFact fact7 = createFact("A street riot, simply a part of life here.", 100, 10);
		fax.add(fact7);
		
		//free after 110
		MovieFact fact8 = createFact("Wild animals still kill people in this rough land.", 120, 10);
		fax.add(fact8);
		
		//free after 130
		MovieFact fact9 = createFact("A lack of safety and regulatory services allows for extreme exploration of the terrain by tourists. ", 150, 10);
		fax.add(fact9);
		
		//free after 160
		MovieFact fact10 = createFact("This is Zorbosphere 10, a spheroid best known for its prominent znark generators.", 170, 10);
		fax.add(fact10);
		
		//free after 180
		MovieFact fact11 = createFact("For many years, Costa Rica has failed to assist the United States in its war on terror.", 200, 10);
		fax.add(fact11);
	
		//free after 210
		MovieFact fact12 = createFact("Costa Rica does not have any valuable natural resources, such as oil or gold.", 220, 10);
		fax.add(fact12);
		
		//free after 230
		MovieFact fact13 = createFact("Thanks for using movieFax! for similar travel destinations, see North Korea, Afganistan, and Iran", 233, 10);
		fax.add(fact13);
		
		return fax;
		

	}

	private static List<MovieFact> addFaxToContent(ArrayList<MovieFact> toAdd, String contentId) throws NoSuchAlgorithmException, KeyManagementException, JsonParseException, JsonMappingException, IOException, JSONException{

		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager(){
			@Override
			public void checkClientTrusted(
					java.security.cert.X509Certificate[] arg0, String arg1)
							throws CertificateException {
				// TODO Auto-generated method stub

			}
			@Override
			public void checkServerTrusted(
					java.security.cert.X509Certificate[] arg0, String arg1)
							throws CertificateException {
				// TODO Auto-generated method stub

			}
			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				// TODO Auto-generated method stub
				return null;
			}
		}};

		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, new SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
			;
		}

		ClientConfig clientc = new DefaultClientConfig();
		SSLContext ctx = SSLContext.getInstance("SSL");
		ctx.init(null, trustAllCerts, null);



		HostnameVerifier allHostsValid = new HostnameVerifier() {


			@Override


			public boolean verify(String arg0, SSLSession arg1) {
				// TODO Auto-generated method stub
				return true;
			}
		};

		HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
		clientc.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, new HTTPSProperties(allHostsValid, ctx));
		ObjectMapper mapper = new ObjectMapper();
		//mapper.getDeserializationConfig().addMixInAnnotations(XMLGregorianCalendar.class, MixIn.class);
		clientc.getSingletons().add(mapper);
		clientc.getClasses().add(JacksonJsonProvider.class);

		Client client = Client.create(clientc);

		WebResource webResource = client.resource(CONTENT_SERVER + ":" + CONTENT_PORT + "/api/v0/addcontent/moviefax?targetContentId=" + contentId);
		MovieFact[] response = webResource.accept(MediaType.APPLICATION_JSON).entity(toAdd, MediaType.APPLICATION_JSON).post(MovieFact[].class);
		List<MovieFact> fax = Arrays.asList(response);
		
		return fax;


	}
	
	public static void main(String[] args) throws KeyManagementException, NoSuchAlgorithmException, JsonParseException, JsonMappingException, IOException, JSONException {
		if(args[0].equalsIgnoreCase("costaRicaFax")){
			ArrayList<MovieFact> facts = addCostaRicaFax();
			
			String costaRicaId = "f6bd99fd-1107-4591-ac81-1587200cb30d";
			addFaxToContent(facts, costaRicaId);
		}

	}

}
