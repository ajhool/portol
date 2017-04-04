package com.portol.common.model.content.meta;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Pricing  implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8677364022227664401L;
	
	//prive per view for VOD, or price per hour for live
	private long priceInCents;
	private int shardPrice;
	private long priceInBits;
	
	public long getPriceInCents() {
		return priceInCents;
	}
	public void setPriceInCents(long priceInCents) {
		this.priceInCents = priceInCents;
	}
	public int getShardPrice() {
		return shardPrice;
	}
	public void setShardPrice(int shardPrice) {
		this.shardPrice = shardPrice;
	}
	public long getPriceInBits() {
		return priceInBits;
	}
	public void setPriceInBits(long priceInBits) {
		this.priceInBits = priceInBits;
	}
}
