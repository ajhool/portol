package com.portol.common.model.content;


import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.portol.common.model.content.meta.EPGBar;
import com.portol.common.model.content.meta.Pricing;
import com.portol.common.model.content.meta.Rating;
import com.portol.common.model.content.meta.SeriesInfo;


@JsonIgnoreProperties(ignoreUnknown = true)
public class ContentMetadata implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4996791562233328507L;

	@Id
	private String metadataId = UUID.randomUUID().toString();
	
	public String getMetadataId() {
		return metadataId;
	}
	
	private String jsonData; 
	
	public void setMetadataId(String id){
		this.metadataId = id;
		
	}
	

	private long availabilityTime;
	
	public long getAvailabilityTime() {
		return availabilityTime;
	}

	public void setAvailabilityTime(long availabilityTime) {
		this.availabilityTime = availabilityTime;
	}


	private String type;
	
	
	private String CurrentTitle;

	private List<CategoryReference> memberOf;

	private int categoryRanking;

	public Pricing getPrices() {
		return prices;
	}
	public void setPrices(Pricing prices) {
		this.prices = prices;
	}

	//common
	private double ratingDouble;
	private long numLoads;
	//number of actual paid plays
	private long numPlays;
	
	//live only
	private long currentViewers;
	
	private Pricing prices;

	private String subtitle; 
	//if live
	private EPGBar epg;

	//if vod
	private SeriesInfo seriesInfo;


	private List<Rating> ratingList;
	public List<Rating> getRatingList() {
		return ratingList;
	}


	public void setRatingList(List<Rating> ratingList) {
		this.ratingList = ratingList;
	}

	private String info;
	
	private String channelOrVideoTitle;
	
	private String creatorInfo; 

	private String splashURL; 
	
	private int secondsFree = -1;

	private String parentContentId;
	private String parentContentKey;
	
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
	
	public String getSplashURL() {
		return splashURL;
	}
	public void setSplashURL(String splashURL) {
		this.splashURL = splashURL;
	}

	public String getCreatorInfo() {
		return creatorInfo;
	}
	public void setCreatorInfo(String creatorInfo) {
		this.creatorInfo = creatorInfo;
	}

	public long getNumLoads() {
		return numLoads;
	}
	public void setNumLoads(long numLoads) {
		this.numLoads = numLoads;
	}
	public long getNumPlays() {
		return numPlays;
	}
	public void setNumPlays(long numPlays) {
		this.numPlays = numPlays;
	}

	public double getRating() {
		return ratingDouble;
	}

	public void setRating(double rating) {
		this.ratingDouble = rating;
	}
	public String getParentContentId() {
		return parentContentId;
	}
	public void setParentContentId(String parentContentId) {
		this.parentContentId = parentContentId;
	}
	public long getCurrentViewers() {
		return currentViewers;
	}
	public void setCurrentViewers(long currentViewers) {
		this.currentViewers = currentViewers;
	}

	public String getCurrentTitle() {
		return CurrentTitle;
	}
	public void setCurrentTitle(String currentTitle) {
		CurrentTitle = currentTitle;
	}
	public String getChannelOrVideoTitle() {
		return channelOrVideoTitle;
	}
	public void setChannelOrVideoTitle(String channelOrVideoTitle) {
		this.channelOrVideoTitle = channelOrVideoTitle;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public EPGBar getEpg() {
		return epg;
	}
	public void setEpg(EPGBar epg) {
		this.epg = epg;
	}
	public SeriesInfo getSeriesInfo() {
		return seriesInfo;
	}
	public void setSeriesInfo(SeriesInfo seriesInfo) {
		this.seriesInfo = seriesInfo;
	}

	public List<CategoryReference> getMemberOf() {
		return memberOf;
	}

	public void setMemberOf(List<CategoryReference> memberOf) {
		this.memberOf = memberOf;
	}

	public int getCategoryRanking() {
		return categoryRanking;
	}

	public void setCategoryRanking(int categoryRanking) {
		this.categoryRanking = categoryRanking;
	}

	public String getParentContentKey() {
		return parentContentKey;
	}

	public void setParentContentKey(String parentContentKey) {
		this.parentContentKey = parentContentKey;
	}

	public String getSubtitle() {
		return subtitle;
	}

	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}

	public String getJsonData() {
		return jsonData;
	}

	public void setJsonData(String jsonData) {
		this.jsonData = jsonData;
	}

	public int getSecondsFree() {
		return secondsFree;
	}

	public void setSecondsFree(int secondsFree) {
		this.secondsFree = secondsFree;
	}
}
