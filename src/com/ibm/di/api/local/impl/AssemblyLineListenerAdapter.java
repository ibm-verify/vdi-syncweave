/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.local.impl;

import com.ibm.di.api.APIEngine;
import com.ibm.di.api.DIException;
import com.ibm.di.entry.Entry;
import com.ibm.di.log.LogInterface;
import com.ibm.di.server.AssemblyLine;
import com.ibm.di.server.ResourceHash;

/**
 * Adapt API's AssemblyLine listener to Server's AssemblyLine listener. Also
 * keep a reference to a log4j appender, because the Server's listeners do not
 * receive logged messages.
 * 
 * @since 7.0
 */
class AssemblyLineListenerAdapter implements com.ibm.di.server.AssemblyLine.AssemblyLineListener {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Resource Hash used to log TMS messages.
	 */
	private final static ResourceHash resHash = APIEngine.getResHash();

	/**
	 * The API's AssemblyLine listener.
	 */
	private com.ibm.di.api.local.AssemblyLineListener listener;

	/**
	 * The logger associated with the listener.
	 */
	private LogInterface logger;

	/**
	 * Determines whether the current entry will be returned on each
	 * AssemblyLine cycle.
	 */
	private boolean getEntryOnEachCycle;

	/**
	 * Create an adapter.
	 * 
	 * @param listener
	 *            The API's AssemblyLine listener.
	 */
	AssemblyLineListenerAdapter(com.ibm.di.api.local.AssemblyLineListener listener, LogInterface logger, boolean getEntryOnEachCycle) {
		this.listener = listener;
		this.logger = logger;
		this.getEntryOnEachCycle = getEntryOnEachCycle;
	}

	/**
	 * Create an adapter.
	 * 
	 * @param listener
	 *            The API's AssemblyLine listener.
	 */
	AssemblyLineListenerAdapter(com.ibm.di.api.local.AssemblyLineListener listener) {
		this(listener, null, false);
	}

	/**
	 * @return The associated log4j appender. May be null.
	 */
	LogInterface getLogger() {
		return logger;
	}

	/**
	 * {@inheritDoc}
	 */
	public void assemblyLineCycleEnded(AssemblyLine al, Entry work) throws Exception {

		try {
			// Send the End of Cycle data only if explicitly specified (sending
			// serialized data
			// slows down execution dramatically).
			if (getEntryOnEachCycle) {
				listener.assemblyLineCycleDone(work);
			}
		} catch (DIException ex) {
			handleExpectedListenerError(ex);
		} catch (Throwable t) {
			handleUnexpectedListenerError(al, t);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @since 7.1
	 */
	public void assemblyLineStarted(AssemblyLine al) {
		// No propagation of this event as this would require change in the
		// Server API AssemblyLineListener interface, which is not backward
		// compatible. In addition to this, manual mode ALs are started through
		// ConfigInstance#startAssemblyLineManual(String, Entry) which does not
		// allow listeners, so in that case the start event would be lost by the
		// time that call returns.
	}

	/**
	 * {@inheritDoc}
	 */
	public void assemblyLineTerminated(AssemblyLine al) {
		try {
			listener.assemblyLineFinished();
		} catch (DIException ex) {
			handleExpectedListenerError(ex);
		} catch (Throwable t) {
			if (al != null)
				handleUnexpectedListenerError(al, t);
		}
	}

	private void handleExpectedListenerError(DIException ex) {
		APIEngine.logWarn(resHash.getString("SERVER.API.AL.LISTENER.ERROR", ex));
	}

	private void handleUnexpectedListenerError(AssemblyLine al, Throwable t) {
		/*
		 * Don't let unchecked exceptions propagate because they will stop the
		 * AssemblyLine. Unregister the misbehaving listener.
		 */
		APIEngine.logWarn(resHash.getString("SERVER.API.AL.LISTENER.UNEXPECTED.ERROR", t));
		al.removeListener(this);
		if (logger != null && al.getLog() != null) {
			al.getLog().removeLogger(logger);
		}
	}

	// Note: Local API is pushing local listeners and we need to be able to
	// recognize the same objects without persisting maps for loggers of
	// different layers. Here is why we are making sure the adapters are
	// identical if the local listeners they adapt from are also identical.
	@Override
	public int hashCode() {
		return listener.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof AssemblyLineListenerAdapter && listener.equals(((AssemblyLineListenerAdapter) o).listener);
	}
}
