package com.portol.common.model;

import java.io.Serializable;
import java.util.Date;

public class LightweightContentInfo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6611164445343469317L;

	
	private String parentContentId;
	private Date dateFound;
	private String title;
	private String summary;
	private String iconURL;
	private String playerId;
	
	
	
	public LightweightContentInfo() {
		super();
	}
	
	
	
	public LightweightContentInfo(String parentContentId, Date dateFound,
			String title, String summary, String iconURL, String playerId) {
		super();
		this.parentContentId = parentContentId;
		this.dateFound = dateFound;
		this.title = title;
		this.summary = summary;
		this.iconURL = iconURL;
		this.playerId = playerId;
	}



	public String getParentContentId() {
		return parentContentId;
	}
	public void setParentContentId(String parentContentId) {
		this.parentContentId = parentContentId;
	}
	public Date getDateFound() {
		return dateFound;
	}
	public void setDateFound(Date dateFound) {
		this.dateFound = dateFound;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	public String getIconURL() {
		return iconURL;
	}
	public void setIconURL(String iconURL) {
		this.iconURL = iconURL;
	}
	
	
}
