/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.tp.server.model;

import java.util.Collection;

import com.ibm.di.tp.server.model.exception.SCMPException;

/**
 * This class represents a connectivity provider instance in the terms the
 * SCMP/CaaS specification defines. <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public interface ConnectivityProvider {

	/**
	 * @return the touchpoint types for this connectivity provider.
	 * @throws SCMPException
	 */
	public Collection<TouchpointType> getTypes() throws SCMPException;

	/**
	 * @return the raw id of the provider. This is generated based on the
	 *         provider config. Note this id has no limitation to what
	 *         characters it might contain.
	 */
	public String getId();
}
