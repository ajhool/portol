package com.portol.loadbalancer.service;

import java.util.concurrent.ConcurrentHashMap;

public class CloudProviderPool {
	
	private ConcurrentHashMap<String, CloudProviderAPIClient> apiClients;
	
	public CloudProviderPool() {
		this.apiClients = new ConcurrentHashMap<String, CloudProviderAPIClient>();
		
	}
	
	public ConcurrentHashMap<String, CloudProviderAPIClient> getApiClients() {
		return apiClients;
	}
	
	public CloudProviderAPIClient addProvider(CloudProviderAPIClient toAdd){
		return apiClients.put(toAdd.getProviderName(), toAdd);
	}
	
	public CloudProviderAPIClient removeProvider(CloudProviderAPIClient toRem){
		return apiClients.remove(toRem.getProviderName());
	}
	
	public CloudProviderAPIClient getBestProviderAPI(){
		String current = DigitalOceanAPIClient.NAME;
		
		return apiClients.get(current);
		
	}
}
