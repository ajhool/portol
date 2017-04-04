package com.portol.common.model.content.meta;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class Rating implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3996196678907503749L;
	
	private String ratingText;

	public Rating(String ratingText){
		super();
		this.ratingText = ratingText;
	}
	
	public Rating(){
		super();
	}
	public String getRatingText() {
		return ratingText;
	}

	public void setRatingText(String ratingText) {
		this.ratingText = ratingText;
	}
	
}
