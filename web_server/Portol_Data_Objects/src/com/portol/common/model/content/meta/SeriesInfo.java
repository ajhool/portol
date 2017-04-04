package com.portol.common.model.content.meta;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SeriesInfo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8714180518002326762L;
	
	private String seriesName;
	private int episodeNum;
	private String episodeName;
	
	public String getSeriesName() {
		return seriesName;
	}
	public void setSeriesName(String seriesName) {
		this.seriesName = seriesName;
	}
	public int getEpisodeNum() {
		return episodeNum;
	}
	public void setEpisodeNum(int episodeNum) {
		this.episodeNum = episodeNum;
	}
	public String getEpisodeName() {
		return episodeName;
	}
	public void setEpisodeName(String episodeName) {
		this.episodeName = episodeName;
	}

}
