/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api;

import java.io.Serializable;

/**
 * Objects representing events occurring on configuration file. <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.2
 */
public class ConfigEvent implements Serializable {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final long serialVersionUID = -896911112226412276L;

	public enum Type {
		/** A configuration file has been created */
		CREATE,
		/**
		 * A configuration file has been created and the configuration file left
		 * locked
		 */
		CREATE_LOCKED,
		/** A configuration file has been checked out */
		CHECK_OUT,
		/** A configuration file has been checked in */
		CHECK_IN,
		/**
		 * A configuration file has been checked in and the configuration file
		 * left locked
		 */
		CHECK_IN_LOCKED,
		/**
		 * A configuration file has been unlocked (either administratively or by
		 * undoing all changes)
		 */
		UNLOCK,
		/** A configuration file has been deleted */
		DELETE
	}

	private final Type t;
	private final String i;
	private final String u;

	public ConfigEvent(Type t, String i, String u) {
		this.t = t;
		this.i = i;
		this.u = u;
	}

	/**
	 * @return the type of the event
	 */
	public Type getType() {
		return t;
	}

	/**
	 * @return the configuration file path relative to the Server API
	 *         configurations directory or a solution name if the configuration
	 *         has one defined.
	 */
	public String getIdentifier() {
		return i;
	}

	/**
	 * @return the username of the user performing the action on the
	 *         configuration file.
	 */
	public String getUser() {
		return u;
	}
}
