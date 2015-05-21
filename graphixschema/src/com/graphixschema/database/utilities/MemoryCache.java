package com.graphixschema.database.utilities;

import java.util.HashMap;
import java.util.Map;

public enum MemoryCache {

	INSTANCE;
	
	private Map<String, Object> memoryStore;
	
	private MemoryCache() {
		memoryStore = new HashMap<String, Object>();
	}
	/**
	 * 
	 * @param key
	 * @param value
	 */
	public void putMemory(String key, Object value) {
		memoryStore.put(key, value);
	}
	/**
	 * 
	 * @param key
	 * @return
	 */
	public Object getFromMemory(String key) {
		return memoryStore.get(key);
	}
	
	public void clearCache() {
		memoryStore.clear();
	}
}
