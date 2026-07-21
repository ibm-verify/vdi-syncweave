/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.osgi;

import com.ibm.di.connector.ChangelogInterface;

/**
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.2
 */
public class ChangeDetectionConnectorDelegate extends ConnectorDelegate implements ChangelogInterface {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public ChangeDetectionConnectorDelegate() {
	}

	public ChangeDetectionConnectorDelegate(String id) throws Exception {
		super(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.ChangelogInterface#getStateKeyObject()
	 */
	public Object getStateKeyObject() throws Exception {
		return ((ChangelogInterface) worker).getStateKeyObject();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.ChangelogInterface#getStateKeySaveMethod()
	 */
	public int getStateKeySaveMethod() throws Exception {
		return ((ChangelogInterface) worker).getStateKeySaveMethod();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.ChangelogInterface#saveStateKey()
	 */
	public void saveStateKey() throws Exception {
		((ChangelogInterface) worker).saveStateKey();
	}
}
