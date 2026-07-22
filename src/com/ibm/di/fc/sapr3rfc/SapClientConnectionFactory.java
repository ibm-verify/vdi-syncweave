/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.sapr3rfc;

import java.util.Properties;

/**
 * Allow different mechanism to establish a client connection. Currently the
 * only ones supported ones are: {@link SapClientConnectionPoolImpl}
 */
final class SapClientConnectionFactory {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private SapClientConnectionFactory() {
		super();
	}

	public static SapClientConnection create(String poolName,
			int maxConnections, Properties jcoProperties) {
		return new SapClientConnectionPoolImpl(poolName, maxConnections,
				jcoProperties);
	}

	public static SapClientConnection create(Properties jcoProperties) {
		return new SapClientConnectionDirectImpl(jcoProperties);
	}

}
