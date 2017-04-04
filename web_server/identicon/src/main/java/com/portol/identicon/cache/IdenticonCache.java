package com.portol.identicon.cache;

public interface IdenticonCache {
	public byte[] get(String key);

	public void add(String key, byte[] imageData);

	public void remove(String key);

	public void removeAll();
}
