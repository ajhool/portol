package com.portol.common.model;

public class FileInfoRequest {

	private String apiKey = "foo";
	
	private String representation;
	
	private String fileName;
	
	
	public String getApiKey() {
	
		return apiKey;
	}


	public String getFileName() {
		return fileName;
	}


	public void setFileName(String fileName) {
		this.fileName = fileName;
	}


	public String getRepresentation() {
		return representation;
	}


	public void setRepresentation(String representation) {
		this.representation = representation;
	}

}
