/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.rest;

import java.util.Date;

import com.ibm.di.api.local.AssemblyLineHandler;

public class ALCacheEntry {
	
	public AssemblyLineHandler handle;
	public String config;
	public String alname;
	public Date created;
	public Date lastUsed;
	public long callCount;
	public long useCount;
	
	public ALCacheEntry(String config, String alname, AssemblyLineHandler handle) {
		this.config = config;
		this.alname = alname;
		this.handle = handle;
		this.created = new Date();
	}
	
	/**
	 * Returns the number of seconds since this AL was last used
	 * 
	 * @return
	 */
	public int age() {
		if(lastUsed == null)
			return 0;
		else
			return (int) ((lastUsed.getTime() - new Date().getTime()) / 1000);
	}

	public String toString() {
		return config + "." + alname;
	}
}
