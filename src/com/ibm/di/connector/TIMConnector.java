/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.server.ResourceHash;

/**
 * This class provides the ability to work with TIM Systems using the JNDI
 * interface. Most of the functionality this class provides is inherited from
 * the {@link JNDIConnector} class.
 */
public class TIMConnector extends JNDIConnector implements ConnectorInterface {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Component name.
	 */
	private static final String myName = "TIM DSMLv2 Connector";

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "timconnector";
	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash sResHash = null;

	static {
		sResHash = new ResourceHash(PROPERTIES_FILE);
	}

	/**
	 * Constructs this object and sets its supported modes.
	 */
	public TIMConnector() {

		super();
		setName(myName);
		setModes(new String[] { ConnectorConfig.ADDONLY_MODE,
				ConnectorConfig.DELETE_MODE, ConnectorConfig.ITERATOR_MODE,
				ConnectorConfig.LOOKUP_MODE, ConnectorConfig.UPDATE_MODE });
		setSearchFilterAll("(objectClass=*)");
	}

	/**
	 * This method only set the class for the initial context and leave the rest
	 * of the execution to the super class.
	 * 
	 * @param o -
	 *            ignored
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void initialize(Object o) throws Exception {
		setParam("java.naming.factory.initial",
				"com.ibm.dsml2.jndi.DSML2InitialContextFactory");
		if (debugMode()) {
			debug(sResHash.getString("INITIAL.CONTEXT.SET"));
		}
		super.initialize(o);
	}

	/**
	 * Version information.
	 * 
	 * @return the version information.
	 */
	public String getVersion() {
		return "2.0-di7.1.1 %I%, 20%E%";
	}
}
