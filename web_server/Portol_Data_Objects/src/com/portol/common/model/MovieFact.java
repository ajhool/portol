package com.portol.common.model;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MovieFact implements Serializable, Comparable<MovieFact>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7973982223990794734L;

	private String factId = UUID.randomUUID().toString(); 
	
	private long startTime;
	
	private long duration; 
	
	private String url; 
	
	
	public MovieFact() {
		super();
		// TODO Auto-generated constructor stub
	}


	public MovieFact(String factId, long startTime, long duration, String url) {
		super();
		this.factId = factId;
		this.startTime = startTime;
		this.duration = duration;
		this.url = url;
	}


	@Override
	public int compareTo(MovieFact o) {
		if(this.startTime < o.getStartTime()){
			return -1;
		
		}
		
		if(this.startTime > o.getStartTime()){
			return 1;
		}
		
		return 0;
	}


	public String getFactId() {
		return factId;
	}


	public void setFactId(String factId) {
		this.factId = factId;
	}


	public long getStartTime() {
		return startTime;
	}


	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}


	public long getDuration() {
		return duration;
	}


	public void setDuration(long duration) {
		this.duration = duration;
	}


	public String getUrl() {
		return url;
	}


	public void setUrl(String url) {
		this.url = url;
	}

}
