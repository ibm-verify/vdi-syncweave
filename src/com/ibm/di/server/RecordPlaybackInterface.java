/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * This is the main class that forks off new threads to run AssemblyLines and EventHandlers.
 *
 */

package com.ibm.di.server;

public interface RecordPlaybackInterface {

	/**
	 * Returns true if the object <i>obj</i> should be recorded.
	 */
	public boolean isRecording(Object obj);

	/**
	 * Returns true if the object <i>obj</i> should be played back from server
	 * store.
	 */
	public boolean isPlaying(Object obj);

	/**
	 * Returns the database path/url where obj is recorded/played back.
	 */
	public String getDatabase();

}
