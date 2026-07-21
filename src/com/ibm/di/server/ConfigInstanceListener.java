/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.server;

/**
 * <p>
 * This interface is for internal use only. Users must not rely on it.
 * </p>
 * 
 * <p>
 * Listener for configuration instance notifications.
 * </p>
 * 
 * @since 7.0
 */
public interface ConfigInstanceListener {

	/**
	 * The configuration instance started - the log is created and the thread is
	 * running, but it may not be fully initialized yet. This method will be
	 * invoked for listeners which are registered at the startup of the
	 * configuration instance. Listeners added at runtime are highly unlikely to
	 * be registered early enough to get this notification.
	 * 
	 * @param configInstance
	 *            The configuration instance.
	 */
	void configInstanceStarted(RSInterface configInstance);

	/**
	 * AssemblyLine started from the configuration instance.
	 * 
	 * @param assemblyLine
	 *            AssemblyLine.
	 */
	void assemblyLineStarted(AssemblyLine assemblyLine);

	/**
	 * AssemblyLine stopped. The AssemblyLine was started from this
	 * configuration instance.
	 * 
	 * @param assemblyLine
	 *            AssemblyLine.
	 */
	void assemblyLineStopped(AssemblyLine assemblyLine);

	/**
	 * The configuration instance stopped.
	 * 
	 * @param configInstance
	 *            The configuration instance.
	 */
	void configInstanceStopped(RSInterface configInstance);
}
