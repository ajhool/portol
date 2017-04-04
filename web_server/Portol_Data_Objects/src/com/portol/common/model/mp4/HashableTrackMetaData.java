package com.portol.common.model.mp4;

import com.googlecode.mp4parser.authoring.TrackMetaData;

public class HashableTrackMetaData extends TrackMetaData{

	public HashableTrackMetaData(TrackMetaData toMakeHashable) {
		super();
		super.setLanguage(toMakeHashable.getLanguage());
		super.setTimescale(toMakeHashable.getTimescale());
		super.setModificationTime(toMakeHashable.getModificationTime());
		super.setCreationTime(toMakeHashable.getCreationTime());
		super.setMatrix(toMakeHashable.getMatrix());
		super.setWidth(toMakeHashable.getWidth());
		super.setHeight(toMakeHashable.getHeight());
		super.setVolume(toMakeHashable.getVolume());
		super.setTrackId(toMakeHashable.getTrackId());
		super.setGroup(toMakeHashable.getGroup());
		super.setLayer(toMakeHashable.getLayer());
	}
	
	@Override
	public int hashCode() {
		String contentsAsString = super.getLanguage() + super.getTimescale() + super.getWidth() + super.getHeight() + super.getVolume();
		int hash=7;
		for (int i=0; i < contentsAsString.length(); i++) {
		    hash = (hash*31) + contentsAsString.charAt(i);
		}
		System.out.println("using subclass hashcode");
		return hash;
	}
	
	@Override
	public boolean equals(Object obj){
		if(!(obj instanceof TrackMetaData)){
			return false;
		}
		
		TrackMetaData other = (TrackMetaData) obj;
		String contentsAsString = super.getLanguage() + super.getTimescale() + super.getWidth() + super.getHeight() + super.getVolume();
		String othercontentsAsString = other.getLanguage() + other.getTimescale() + other.getWidth() + other.getHeight() + other.getVolume();
		return contentsAsString.equalsIgnoreCase(othercontentsAsString);
	}

}
