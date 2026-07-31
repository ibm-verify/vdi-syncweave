/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.tp.server.model;

import com.ibm.di.tp.server.ServerActivator;
import com.ibm.di.tp.server.config.node.NodeConfig;
import com.ibm.di.tp.server.config.node.TdiNodeConfig;
import com.ibm.di.tp.server.context.TPServerContext;
import com.ibm.di.tp.server.model.exception.ErrorCode;
import com.ibm.di.tp.server.model.exception.SCMPException;
import com.ibm.di.tp.server.model.impl.tdi.ConnectivityProviderImpl;

/**
 * This is the factory class for creating {@link ConnectivityProvider}s.<br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public final class ConnectivityProviderFactory {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	private ConnectivityProviderFactory() {
	}

	/**
	 * Creates a connectivity provider for the provided configuration.
	 * 
	 * @param cfg
	 *            the configuration describing the remote connectivity provider.
	 * @return an instance of the connectivity provider.
	 * @throws Exception
	 */
	public static ConnectivityProvider createConnectivityProvider(NodeConfig cfg, TPServerContext ctx) throws SCMPException {
		if (cfg instanceof TdiNodeConfig) {
			return new ConnectivityProviderImpl((TdiNodeConfig) cfg, ctx);
		}
		throw new SCMPException(ErrorCode.CONNECTIVITY_UNKNOWN, ServerActivator.L10N.getString(
				"TP.SERVER.RESOURCE.UNSUPPORTED.CONNECTIVITY.PROVIDER"), 500);
	}
}
