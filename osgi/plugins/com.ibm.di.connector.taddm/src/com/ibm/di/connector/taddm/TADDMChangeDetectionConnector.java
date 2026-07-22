/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.taddm;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.connector.ChangelogInterface;
import com.ibm.di.server.SearchCriteria;

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
public class TADDMChangeDetectionConnector extends TADDMConnector implements ChangelogInterface {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public TADDMChangeDetectionConnector() {
		setName("TADDM Change Detection Connector");
		setModes(new String[] { ConnectorConfig.ITERATOR_MODE });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.taddm.TADDMConnector#getConnectorClass()
	 */
	@Override
	protected String getConnectorClass() {
		return "com.ibm.di.connector.taddm.TADDMChangeDetectionWorkerConnector";
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

	public Date getServerTime() throws Exception {
		Date serverTime = null;
		try {
			serverTime = (Date) worker.getClass().getMethod("getServerTime", (Class[]) null).invoke(worker, (Object[]) null);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
			return serverTime;
	}
}
