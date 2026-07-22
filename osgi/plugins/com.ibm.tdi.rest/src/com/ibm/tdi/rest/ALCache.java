/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibm.di.api.DIException;
import com.ibm.di.api.local.AssemblyLineHandler;

public class ALCache {
	
	public static int MAX_CACHE = 3;
	public static int MAX_AGE = 60; // after 60 seconds cached assemblylines terminate
	
	private Map<String, List<ALCacheEntry>> cache = new HashMap<String, List<ALCacheEntry>>();
	private static Boolean cacheEnabled = Boolean.getBoolean("com.ibm.tdi.rest.cache.enabled");
	
	public ALCache() {
		System.out.println("com.ibm.tdi.rest: com.ibm.tdi.rest.cache.enabled=" + cacheEnabled);
	}
	
	public ALCacheEntry initCacheEntry(String config, String al, AssemblyLineHandler handle) {
		ALCacheEntry c = new ALCacheEntry(config, al, handle);
		return c;
	}
	
	public void clearCache() {
		synchronized(cache) {
			for(List<ALCacheEntry> list : cache.values()) {
				for(ALCacheEntry c : list) {
					try {
						c.handle.close();
					} catch (Exception e) {
					}
				}
			}
			cache.clear();
		}
	}

	public void removeAssemblyLine(ALCacheEntry entry) {
		synchronized(cache) {
			String key = entry.config+"."+entry.alname;
			List<ALCacheEntry> list = cache.get(key);
			if(list != null) {
				list.remove(entry);
			}
		}
	}
	
	public void cacheAssemblyLine(ALCacheEntry entry, boolean hasFeed) {
		if(hasFeed || !cacheEnabled) {
			try {
				entry.handle.close();
			} catch (DIException e) {
				e.printStackTrace();
			}
		} else {
			synchronized(cache) {
				String key = entry.config+"."+entry.alname;
				List<ALCacheEntry> list = cache.get(key);
				if(list == null) {
					list = new ArrayList<ALCacheEntry>();
					cache.put(key, list);
				}
				if(list.size() < MAX_CACHE) {
					list.add(entry);
				} else {
					if(entry.handle != null) {
						try {
							entry.handle.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
	
	public ALCacheEntry getAssemblyLine(String config, String al) {
		String key = config+"."+al;
		ALCacheEntry entry = null;

		synchronized(cache) {
			List<ALCacheEntry> list = cache.get(key);
			while(list != null && list.size() > 0) {
				entry = list.remove(0);
				if(entry.age() > MAX_AGE) {
					System.out.println("getAssemblyLine: stale entry " + entry);
				} else {
					System.out.println("getAssemblyLine: found cached entry " + entry);
					break;
				}
			}
		}
		if(entry == null) {
			entry = initCacheEntry(config, al, null);
		}
		return entry;
	}

}
