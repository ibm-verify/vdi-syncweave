/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.server;

/**
 * This class represents the mode of a Connector, giving it information how to
 * operate.
 */
public class ConnectorMode {
	/**
	 * Copyright information.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * The connector mode.
	 */
	int mode;

	/**
	 * Constructor.
	 * 
	 * @param type
	 *            the type of the connector mode
	 */
	public ConnectorMode(String type) {
		Trace.entrymin(this, "ConnectorMode", type);
		mode = ServerConstants.getType(type);
		Trace.exitmin(this, "ConnectorMode");
	}

	/**
	 * Constructor.
	 * 
	 * @param mode
	 *            a connector mode
	 */
	public ConnectorMode(int mode) {
		this.mode = mode;
	}

	/**
	 * Returns the connector mode.
	 * 
	 * @return the connector mode
	 */
	public int getMode() {
		Trace.entrymax(this, "getMode");
		Trace.exitmax(this, "getMode");
		return mode;
	}

	/**
	 * Returns a String representation of the object. More specifically a server
	 * object.
	 * 
	 * @return a String value
	 */
	public String toString() {
		return ServerConstants.STR_TYPES[mode];
	}
}
