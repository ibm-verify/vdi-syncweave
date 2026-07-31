/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.server;

import com.ibm.di.entry.Entry;

/**
 * This interface is implemented by connectors and parsers that support
 * checkpoint/restart.
 * 
 * @deprecated We no longer support Checkpoint/restart
 */
@Deprecated
public interface CheckpointRestartInterface {

	/**
	 * Restart before current point.
	 */
	public final int RESTART_BEFORE = 1;

	/**
	 * Restart at current point.
	 */
	public final int RESTART_IN = 2;

	/**
	 * Restart after current point.
	 */
	public final int RESTART_AFTER = 3;

	/**
	 * Components that support checkpoint/restart for a specific mode must
	 * return TRUE from this method. If a restart has no meaning for <i>mode</i>
	 * then the connector should still return TRUE.
	 * 
	 * @param mode
	 *            The mode the connector runs in
	 * @return true if checkpoint is supported in <i>mode</i>
	 * @see ServerConstants
	 */
	public boolean isCheckpointRestartEnabled(int mode);

	/**
	 * Returns the state information for the connector. If for some reason the
	 * connector decides that a restart is impossible it must throw an
	 * exception. The method is called immediately before any component methods
	 * are called. If there is no need to save any state information a null
	 * value should be returned.
	 * 
	 * @return The entry the connector needs to do a restart or null if that is
	 *         not needed.
	 * 
	 * @throws Exception
	 *             if problem occurs
	 */
	public Entry getCheckpointInformation() throws Exception;

	/**
	 * Notifies the connector of a restart situation. This method is called
	 * after the connector has been loaded and before any other method calls are
	 * made to the connector.
	 * 
	 * If the connector decides it cannot restart it must throw an exception.
	 * 
	 * @param restartInfo
	 *            The last entry object returned by the connector in the
	 *            getCheckpointInformation method
	 * @param state
	 *            The ALState object for the AssemblyLine
	 * @param restartPoint
	 *            The relative point where restarting is taking place (e.g.
	 *            RESTART_BEFORE ...)
	 * @throws Exception
	 *             if problem occurs
	 */
	public void prepareForRestart(ALState state, Entry restartInfo,
			int restartPoint) throws Exception;

}
