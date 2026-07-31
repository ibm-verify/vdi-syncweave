/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
//
// Version.java
//
//
//
package com.ibm.di.server;

import com.ibm.di.loader.IDILoader;

/**
 * This class is used for retrieving version information.
 */
public class Version {
	/**
	 * Copyright information.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static String date = IDILoader.getModificationDate("com.ibm.di.server.RS");

	/**
	 * Returns server version information. Specifically the release number and
	 * the date of last modification of the server jar.
	 * 
	 * @return version information
	 */
	public static String version() {
		String ver = "10.0.0.6 - " + date;		
		return ver;
	}
}
