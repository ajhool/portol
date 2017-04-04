package com.portol.cloudplayer.manager;

import java.security.cert.CertificateException;
import java.util.Arrays;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.X509Certificate;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.portol.cloudplayer.config.MongoConfig;

import io.dropwizard.lifecycle.Managed;

public class MongoUnManaged {

	private MongoClient mongo;
	private DB db;

	public SSLSocketFactory getTrustAllCertsSocketFactory(){
		//Trust all certs

		SSLSocketFactory sslSocketFactory = null;
		// Imports: javax.net.ssl.TrustManager, javax.net.ssl.X509TrustManager
		try {
			// Create a trust manager that does not validate certificate chains
			final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
				@Override
				public void checkClientTrusted(
						java.security.cert.X509Certificate[] arg0, String arg1)
								throws CertificateException {
				}
				@Override
				public void checkServerTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
								throws CertificateException {
				}
				@Override
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			} };

			// Install the all-trusting trust manager
			final SSLContext sslContext = SSLContext.getInstance( "SSL" );
			sslContext.init( null, trustAllCerts, new java.security.SecureRandom() );
			// Create an ssl socket factory with our all-trusting manager
			sslSocketFactory = sslContext.getSocketFactory();




		} catch ( final Exception e ) {
			e.printStackTrace();
		}
		return sslSocketFactory;
	}

	public MongoUnManaged(String host, int port, String DB, String user, String password) throws Exception {
		MongoCredential cred = MongoCredential.createCredential(user, DB, password.toCharArray());
		ServerAddress srvr = new ServerAddress(host, port);

		MongoClientOptions o = new MongoClientOptions.Builder()
		.socketFactory(getTrustAllCertsSocketFactory())
		.build();

		this.mongo = new MongoClient(srvr, Arrays.asList(cred), o);
		this.db = mongo.getDB(DB);
	}


	public void start() throws Exception {


	}

	public void stop() throws Exception {
		this.mongo.close();

	}

	public Mongo getMongo(){
		return this.mongo;
	}

	public DB getDB(){
		return db;
	}

}
