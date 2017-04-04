package com.portol.common.model.content.meta;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EPGBar  implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -581001646174001159L;
	
	private long currentTime;
	private ArrayList<EPGItem> programInfos;
	
	public long getCurrentTime() {
		return currentTime;
	}
	public void setCurrentTime(long currentTime) {
		this.currentTime = currentTime;
	}
	public ArrayList<EPGItem> getProgramInfos() {
		return programInfos;
	}
	public void setProgramInfos(ArrayList<EPGItem> programInfos) {
		this.programInfos = programInfos;
	}
}
