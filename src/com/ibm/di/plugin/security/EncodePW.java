/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.plugin.security;

import com.ibm.di.server.ResourceHash;

/**
 * Run the main of this class to return an encoded and converted-to-ascii result
 * is written to System.out
 * 
 * @author Gerald Borrelli
 */
public class EncodePW {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.plugin.CopyRight.OBJECT_CODE;

	private static final ResourceHash resHash = ResourceHash.getHash("proxy");

	/**
	 * GenPropertiesFile constructor comment.
	 */
	public EncodePW() {
		super();
	}

	/**
	 * usage: java com.ibm.ldaptim.util.EncodePW password where: password is
	 * string to be encoded and converted to ascii example: java
	 * com.ibm.ldaptim.util.EncodePW secret
	 */
	public static void main(String[] argv) {

		// is there anything to do?
		if (argv.length == 0) {
			printUsage();
			System.exit(1);
		}

		EncodePW encPW = new EncodePW();
		// check parameters
		for (int i = 0; i < argv.length; i++) {
			String arg = argv[i];

			String argENC = SecurityHelper.encode(arg);
			argENC = SecurityHelper.convertToASCI(argENC);
			encPW.printResults(argENC);

		}

	} // main(String[])

	/** Prints the results of the transformation. */
	private void printResults(String argENC) {

		System.out.println();
		System.out.print(argENC);

		System.out.println();

	}

	/** Prints the usage. */
	private static void printUsage() {

		System.err.println(resHash.getString("ENCODEPW.USAGE"));
		System.err.println();

	} // printUsage()
}
