/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.connection.internal;

import com.ibm.di.api.DIException;
import com.ibm.di.api.connection.IServerAPIConnection;
import com.ibm.di.api.connection.IServerAPIConnectionService;
import com.ibm.di.nls.L10N;
import com.ibm.di.nls.L10NFactory;

/**
 * No special features (pooling, etc.) just creates new remote connection each
 * time and cache the local one. <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.2
 */
public final class ServerAPIConnectionService implements IServerAPIConnectionService {

	// Use these while developing for now to redirect to a running server
	private static final String REDIRECT_LOCAL_HOST = "com.ibm.di.api.connection.local.host";
	private static final String REDIRECT_LOCAL_PORT = "com.ibm.di.api.connection.local.port";

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	public static final L10N L10N = L10NFactory.getInstance(ServerAPIConnectionService.class);

	private String localhost;
	private Integer port;
	private LocalServerAPIConnection localConn;

	public ServerAPIConnectionService() throws DIException {
		localhost = System.getProperty(REDIRECT_LOCAL_HOST);
		port = Integer.getInteger(REDIRECT_LOCAL_PORT, 1099);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.api.connection.IServerAPIConnectionService#getConnection(java
	 * .lang.String, int)
	 */
	public IServerAPIConnection getConnection(String host, int port) throws DIException {
		return new RmiServerApiConnection(host, port);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.api.connection.IServerAPIConnectionService#getConnection()
	 */
	public synchronized IServerAPIConnection getConnection() throws DIException {
		return localhost == null ? localConn : getConnection(localhost, port);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.api.connection.IServerAPIConnectionService#isConnectionLocal
	 * (com.ibm.di.api.connection.IServerAPIConnection)
	 */
	public synchronized boolean isConnectionLocal(IServerAPIConnection conn) {
		return conn == localConn;
	}

	@SuppressWarnings("unused")
	private synchronized void activate() throws DIException {
		localConn = new LocalServerAPIConnection();
	}

	@SuppressWarnings("unused")
	private synchronized void deactivate() {
		if (localConn != null) {
			localConn.close();
			localConn = null;
		}
	}
}
