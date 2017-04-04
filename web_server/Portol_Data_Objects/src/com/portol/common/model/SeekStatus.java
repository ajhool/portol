package com.portol.common.model;

import java.io.Serializable;

public class SeekStatus implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -981585054275904690L;
	
	private long streamDuration;
	private long progress;
	private long remaining; 
	
	@Override 
	public String toString(){
		StringBuffer buf = new StringBuffer();
		buf.append("StreamDuration: " + streamDuration +"\n");
		buf.append("progress: " + progress +"\n");
		buf.append("remaining: " + remaining +"\n");
		
		return buf.toString();
	}
	public SeekStatus() {
		super();
		
	}
	public SeekStatus(long streamDuration, long progress, long remaining,
			String playerId) {
		super();
		this.streamDuration = streamDuration;
		this.progress = progress;
		this.remaining = remaining;
	}
	public long getStreamDuration() {
		return streamDuration;
	}
	public void setStreamDuration(long streamDuration) {
		this.streamDuration = streamDuration;
	}
	public long getProgress() {
		return progress;
	}
	public void setProgress(long progress) {
		this.progress = progress;
	}
	public long getRemaining() {
		return remaining;
	}
	public void setRemaining(long remaining) {
		this.remaining = remaining;
	}

	
}
