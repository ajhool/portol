package com.portol.common.model;

import java.io.Serializable;
import java.util.ArrayList;

public class CategoryAddRequest implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3187071700524095016L;
	
	private Category newCat; 
	
	private ArrayList<String> memberKeys;

	public CategoryAddRequest() {
		super();
	}

	public CategoryAddRequest(Category newCat, ArrayList<String> memberKeys) {
		super();
		this.newCat = newCat;
		this.memberKeys = memberKeys;
	}

	public ArrayList<String> getMemberKeys() {
		return memberKeys;
	}

	public void setMemberKeys(ArrayList<String> memberKeys) {
		this.memberKeys = memberKeys;
	}

	public Category getNewCat() {
		return newCat;
	}

	public void setNewCat(Category newCat) {
		this.newCat = newCat;
	}
	

}
