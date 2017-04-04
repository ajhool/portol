package com.portol.common.model.bookmark;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Bookmark implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -2371445757464412820L;

	private String bookmarkedContentId;
	
	private String referrerId; 
	
	private Date dateBookmarked;

	public Bookmark(String bookmarkedContentId, Date dateBookmarked) {
		super();
		this.setBookmarkedContentId(bookmarkedContentId);
		this.setDateBookmarked(dateBookmarked);
	}

	public Bookmark() {
		super();
	}

	public Date getDateBookmarked() {
		return dateBookmarked;
	}

	public void setDateBookmarked(Date dateBookmarked) {
		this.dateBookmarked = dateBookmarked;
	}

	public String getBookmarkedContentId() {
		return bookmarkedContentId;
	}

	public void setBookmarkedContentId(String bookmarkedContentId) {
		this.bookmarkedContentId = bookmarkedContentId;
	}

	public String getReferrerId() {
		return referrerId;
	}

	public void setReferrerId(String referrerId) {
		this.referrerId = referrerId;
	} 
	
}
