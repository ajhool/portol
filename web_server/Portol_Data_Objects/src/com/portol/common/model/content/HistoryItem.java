package com.portol.common.model.content;

import java.io.Serializable;

import java.util.UUID;

import org.mongojack.Id;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class HistoryItem implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4565279989140828664L;
	
	@Id
	private String id = UUID.randomUUID().toString();
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	private String color; 
	
	private String platform; 
	
	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public HistoryItem(){
		super();
	}
	public HistoryItem(String viewedContentId) {
		super();
		this.timeViewed = System.currentTimeMillis();
		this.viewedContentId = viewedContentId;
	}

	public HistoryItem(long timeViewed, String viewedContentId) {
		super();
		this.timeViewed = timeViewed;
		this.viewedContentId = viewedContentId;
	}

	private long timeViewed = 0;
	
	private String viewedContentId;

	public long getTimeViewed() {
		return timeViewed;
	}

	public void setTimeViewed(long timeViewed) {
		this.timeViewed = timeViewed;
	}

	public String getViewedContentId() {
		return viewedContentId;
	}

	public void setViewedContentId(String viewedContentId) {
		this.viewedContentId = viewedContentId;
	}

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

}
