package com.portol.common.model;

import java.io.Serializable;

public class StreamState implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -409453707095322628L;
	
	private int numsegments; 
	private int maxSent;
	private long segmentDuration;
	
	public int getNumsegments() {
		return numsegments;
	}
	public StreamState(int numsegments, int maxSent, long segmentDuration) {
		super();
		this.numsegments = numsegments;
		this.maxSent = maxSent;
		this.segmentDuration = segmentDuration;
	}
	
	public StreamState() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
	public void setNumsegments(int numsegments) {
		this.numsegments = numsegments;
	}
	public int getMaxSent() {
		return maxSent;
	}
	public void setMaxSent(int maxSent) {
		this.maxSent = maxSent;
	}
	public long getSegmentDuration() {
		return segmentDuration;
	}
	public void setSegmentDuration(long segmentDuration) {
		this.segmentDuration = segmentDuration;
	}
	
	
}