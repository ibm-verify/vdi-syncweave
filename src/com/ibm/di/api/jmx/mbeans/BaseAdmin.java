/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.jmx.mbeans;

import com.ibm.di.api.APIEngine;
import com.ibm.di.api.DIException;
import com.ibm.di.api.remote.impl.rmi.SSLRMIServerSocketFactory;
import com.ibm.di.server.ResourceHash;

/**
 * This class implements BaseAdminMBean and is extended by most of the
 * implementing classes from the management package.
 */
public abstract class BaseAdmin implements BaseAdminMBean {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private final static ResourceHash sResHash = APIEngine.getResHash();

	/**
	 * Returns the MBean key property list. If the MBean type is
	 * <code>AssemblyLine</code> and the id is <code>Hello</code>, the
	 * result of this method would be
	 * <code>&quot;type=AssemblyLine,id=Hello&quot;</code>
	 * 
	 * @return string representing the MBean attributes.
	 * @throws DIException
	 *             if an error occurs while obtaining MBean's type.
	 */
	public String getKeyPropertyList() throws DIException {
		String keyPropertyList = "type=" + getType() + ",id=" + getId();

		return keyPropertyList;
	}

	/**
	 * Retrieves the ID of the current user.
	 * @return current user ID.
	 */
	protected String getCurrentUserId() {
		if (!APIEngine.isSSLon()) {
			return null;
		}

		String userId = null;
		try {
			userId = SSLRMIServerSocketFactory.getLocalThreadPrincipal()
					.toString();
			if (APIEngine.isDebugEnabled()) {
				APIEngine
						.logDebug(sResHash.getString(
								"SEVER.API.JMX.MBEAN.CALL.FROM.REMOTE.USER.ID",
								userId));
			}
		} catch (Exception e) {
			userId = null;
			APIEngine.logError(sResHash.getString(
					"SEVER.API.ERROR.RETRIEVING.JMX.REMOTE.USER.ID", e
							.toString()));
		}

		return userId;
	}
}
