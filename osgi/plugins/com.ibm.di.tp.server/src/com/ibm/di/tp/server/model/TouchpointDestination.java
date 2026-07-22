/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.tp.server.model;

import com.ibm.di.tp.server.model.config.DestinationData;
import com.ibm.di.tp.server.model.exception.SCMPException;

/**
 * Represents a Touchpoint Destination in the terms defined by the SCMP/CaaS
 * specification. <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public interface TouchpointDestination {

	public TouchpointInstance getTouchpointInstance();

	public DestinationData getConfiguration() throws SCMPException;

	public void setConfiguration(DestinationData cfg) throws SCMPException;
}
