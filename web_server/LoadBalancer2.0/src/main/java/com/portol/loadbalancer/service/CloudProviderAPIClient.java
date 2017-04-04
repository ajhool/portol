package com.portol.loadbalancer.service;

import org.apache.http.client.HttpClient;

import com.portol.common.model.instance.BackendInstance;
import com.portol.common.model.instance.EdgeInstance;
import com.portol.common.model.instance.Instance;
import com.portol.loadbalancer.config.CloudProviderConfig;

public abstract class CloudProviderAPIClient {
	
	private String apiKey; 
	protected final HttpClient hClient; 
	private final String providerName;
	
	public CloudProviderAPIClient(CloudProviderConfig config, HttpClient hClient, String providerName){
		this.apiKey = config.apiKey; 
		this.hClient = hClient;
		this.providerName = providerName;
		
	}

	//return null on failure
	public abstract String destroyCloud(Instance toKill);

	//fill in as much info as possible in the returned object
	public abstract EdgeInstance spinUpNewEdge();
	
	public abstract BackendInstance spinUpNewBackend();
	
	public abstract EdgeInstance shutdownAndResize(EdgeInstance toresize, int sizelevel);

	public String getProviderName() {
		return providerName;
	}

	public String getApiKey() {
		return apiKey;
	}

		
	
	
}
