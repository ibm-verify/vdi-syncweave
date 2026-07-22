/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.plugin.pwstore.itim.policy.impl;

import javax.net.ssl.SSLSession;

/**
 * Hostname verifier implementation for HTTPS/SSL integration with ITIM server.
 */
final class HostnameVerifierImpl implements javax.net.ssl.HostnameVerifier {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.plugin.CopyRight.OBJECT_CODE;

	/**
	 * @see javax.net.ssl.HostnameVerifier#verify(java.lang.String,
	 *      javax.net.ssl.SSLSession)
	 */
	public boolean verify(String hostname, SSLSession sess) {
		return true;
	}
}
