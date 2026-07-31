/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.tp.server.model;

/**
 * Represents a role that a {@link TouchpointType} supports and that a
 * Touchpoint Instance is configured with. <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public enum TouchpointRole {
	/**
	 * Represents the "provider-tp" role
	 */
	PROVIDER,
	/**
	 * Represents the "initiator-tp" role
	 */
	INITIATOR,
	/**
	 * Represents the "intermediary-tp" role
	 */
	INTERMEDIARY
}
