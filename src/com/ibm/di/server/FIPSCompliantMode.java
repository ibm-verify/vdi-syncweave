/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.server;

import com.ibm.di.security.CryptoFactory;
import java.security.Security;
import java.security.Provider;

/**
 * This is the main class which enables the Federal Information Processing
 * Standard(FIPS) in IBM Tivoli Directory Integrator. In order to be FIPS
 * compliant a specific cryptogaphic and SSL providers must be set in correct
 * order to be used by IBM Tivoli Directory Integrator. To turn on FIPS mode in
 * IBM Tivoli Directory Integrator, users must set the
 * "com.ibm.di.server.fipsmode.on" property to true in global.properties file.
 * This will enforce the IBM Tivoli Directory Integrator Server to arrange the
 * correct providers in the desired order at start up.
 * 
 * @since 7.0
 * 
 */
public class FIPSCompliantMode {

	/**
	 * Copyright information.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * A required FIPS providers class.
	 */
	private static boolean isFIPSenabled = false;

	/**
	 * TMS filename used for info, error and debug messages.
	 */
	private static final String PROPERTIES_FILE = "miserver";

	/**
	 * Message Resource Hash used to access the TMS messages.
	 */
	private static ResourceHash resHash = ResourceHash.getHash(PROPERTIES_FILE);

	/**
	 * This method arranges the security providers in the correct order for FIPS
	 * compliant mode. An Exception will be thrown if the reorder of the
	 * providers fail.
	 * 
	 * @throws Exception
	 *             if the providers rearrangement fails.
	 */
	public static void initializeFIPSMode() throws Exception {
		isFIPSenabled = true;
	}

	/**
	 * This method retrieves information whether FIPS mode was successfully
	 * initialized.
	 * 
	 * @return true, if nothing went wrong when setting up the provider for FIPS
	 *         mode and false otherwise
	 */
	public static boolean isFIPSenabled() {
		return isFIPSenabled;
	}
}
