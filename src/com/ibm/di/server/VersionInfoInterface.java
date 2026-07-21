/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
//
// VersionInfoInterface.java
//
//
//
package com.ibm.di.server;

/**
 * This interface is implemented by classes providing version information.
 * (connectors, parsers , etc).
 */
public interface VersionInfoInterface {

	/**
	 * Version information.
	 * 
	 * @return version information
	 */
	public String getVersion();

	/**
	 * Return version information public String getVersion () { String ver =
	 * "$Revision: 1.1.1.1 $"; String date = "$Date: 2002/08/22 09:24:30 $";
	 * return ver + "\n" + date; }
	 * 
	 */
}
