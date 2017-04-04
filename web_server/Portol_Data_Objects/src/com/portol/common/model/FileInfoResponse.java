package com.portol.common.model;

import java.io.Serializable;

public class FileInfoResponse implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4123999659899272040L;
	
	
	private String MD5;
	
	
	
	public FileInfoResponse() {
		super();
		
	}



	public String getMD5() {
		return MD5;
	}



	public void setMD5(String mD5) {
		MD5 = mD5;
	}
	

	
	

}
