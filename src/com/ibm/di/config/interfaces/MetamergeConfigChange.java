/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

/**
 * A class describing a change that has happened to the configuration.
 * This is used by the Configuration Editor to update the screen.
 *
 */
public class MetamergeConfigChange {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Unknown change event
	 */
	public final static int MCC_UNSPECIFIED = -1;

	/**
	 * Attribute was removed
	 */
	public final static int MCC_REMOVE = 1;

	/**
	 * Attribute was added
	 */
	public final static int MCC_SET = 2;

	/**
	 * Config object was added
	 */
	public final static int MCC_ADD = 3;

	/**
	 * Config object was modified
	 */
	public final static int MCC_MODIFY = 4;

	/**
	 * Config object was deleted
	 */
	public final static int MCC_DELETE = 5;

	/**
	 * Attribute was replaced
	 */
	public final static int MCC_REPLACE = 6;

	/**
	 * There will be a batch of changes to this source, so no need to update yet
	 */
	public final static int BEGIN_CHANGES = 7;

	/**
	 * Finished doing the batch of changes
	 */
	public final static int END_CHANGES = 8;

	private Object _source;

	private Object _key;

	private int _operation;

	private Object _user;

	public MetamergeConfigChange(Object source, Object key, int operation) {
		this(source, key, operation, null);
	}

	public MetamergeConfigChange(Object source, Object key, int operation,
			Object userObject) {
		_source = source;
		_key = key;
		_operation = operation;
		_user = userObject;
	}

	public int getOperation() {
		return _operation;
	}

	public Object getKey() {
		return _key;
	}

	public Object getSource() {
		return _source;
	}

	public Object getUserObject() {
		return _user;
	}

	public String toString() {
		return "[MetamergeConfigChange, key=" + _key + ", oper=" + _operation
				+ ", source=" + _source + ", user=" + _user + "]";
	}

}
