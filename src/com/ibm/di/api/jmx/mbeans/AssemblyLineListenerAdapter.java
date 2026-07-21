/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.jmx.mbeans;

import com.ibm.di.api.APIEngine;
import com.ibm.di.api.DIException;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.ResourceHash;

/**
 * Adapt JMX's listener for AssemblyLine events to local API's listener.
 */
class AssemblyLineListenerAdapter implements
		com.ibm.di.api.local.AssemblyLineListener {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Resource Hash used to log TMS messages.
	 */
	private final static ResourceHash resHash = APIEngine.getResHash();

	/**
	 * AssemblyLineListener
	 */
	private AssemblyLineListener mListener = null;

	/**
	 * Class constructor
	 * 
	 * @param aListener
	 */
	public AssemblyLineListenerAdapter(AssemblyLineListener aListener) {
		mListener = aListener;
	}

	/**
	 * {@inheritDoc}
	 */
	public void assemblyLineCycleDone(Entry aEntry) throws DIException {
		try {
			mListener.assemblyLineCycleDone(aEntry);
		} catch (Exception e) {
			APIEngine.logError(resHash.getString(
					"SEVER.API.EXCEPTION.ON.JMX.ASSEMBLYLINE.CYCLE.DONE", e
							.toString()));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void assemblyLineFinished() throws DIException {
		try {
			mListener.assemblyLineFinished();
		} catch (Exception e) {
			APIEngine.logError(resHash.getString(
					"SEVER.API.EXCEPTION.ON.JMX.ASSEMBLYLINE.FINISHED", e
							.toString()));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void messageLogged(String aMessage) throws DIException {
		try {
			mListener.messageLogged(aMessage);
		} catch (Exception e) {
			APIEngine.logError(resHash.getString(
					"SEVER.API.EXCEPTION.ON.JMX.MESSAGE.LOGGED", e.toString()));
		}
	}
}
