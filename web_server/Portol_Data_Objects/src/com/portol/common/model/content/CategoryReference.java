package com.portol.common.model.content;

import java.io.Serializable;

import org.mongojack.Id;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CategoryReference implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8306288323038988298L;
	
	@Id
	private String categoryId;

	
	public CategoryReference(String categoryId) {
		super();
		this.categoryId = categoryId;
	}

	public CategoryReference() {
		super();
	}

	public String getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(String cat) {
		this.categoryId = cat;
	} 

}
