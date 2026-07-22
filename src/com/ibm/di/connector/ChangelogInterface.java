/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector;

/**
 * The interface object which all ChangelogConnectors should implement.
 * 
 * @see ChangelogConnector
 */
public interface ChangelogInterface {

	/**
	 * The name of the parameter which value specifies the method, used for
	 * storing the StateKey.
	 */
	public static final String CONN_PARAM_STATE_KEY_PERSISTENCE = "stateKeyPersistence";

	/**
	 * Possible {@link String} value of the
	 * {@link #CONN_PARAM_STATE_KEY_PERSISTENCE} parameter. If that parameter
	 * have this value then the {@link #getStateKeySaveMethod()} will return
	 * {@link #SAVE_STATE_AFTER_READ} ({@value #SAVE_STATE_AFTER_READ})
	 */
	public static final String PARAM_VAL_AFTER_READ = "After read";

	/**
	 * Possible {@link String} value of the
	 * {@link #CONN_PARAM_STATE_KEY_PERSISTENCE} parameter. If that parameter
	 * have this value then the {@link #getStateKeySaveMethod()} will return
	 * {@link #SAVE_STATE_END_OF_CYCLE} ({@value #SAVE_STATE_END_OF_CYCLE})
	 */
	public static final String PARAM_VAL_END_OF_CYCLE = "End of cycle";

	/**
	 * Possible {@link String} value of the
	 * {@link #CONN_PARAM_STATE_KEY_PERSISTENCE} parameter. If that parameter
	 * have this value then the {@link #getStateKeySaveMethod()} will return
	 * {@link #SAVE_STATE_MANUAL} ({@value #SAVE_STATE_MANUAL})
	 */
	public static final String PARAM_VAL_MANUAL = "Manual";

	/**
	 * Possible {@link String} value of the
	 * {@link ChangelogConnector#PARAM_MERGE_MODE} parameter. If that parameter
	 * have this value then the {@link ChangelogConnector#defaultMerge} will
	 * have a value of <code>true</code>.
	 */
	public static final String PARAM_MERGE_CHANGELOG_AND_DATA = "Merge changelog and changed data";

	/**
	 * Possible {@link String} value of the
	 * {@link ChangelogConnector#PARAM_MERGE_MODE} parameter. If that parameter
	 * have this value then the {@link ChangelogConnector#onlyChanges} will have
	 * a value of <code>true</code>.
	 */
	public static final String PARAM_MERGE_ONLY_CHANGED_DATA = "Return only changed data";

	/**
	 * Possible {@link String} value of the
	 * {@link ChangelogConnector#PARAM_MERGE_MODE} parameter. If that parameter
	 * have this value then the {@link ChangelogConnector#bothSeparated} will
	 * have a value of <code>true</code>.
	 */
	public static final String PARAM_MERGE_BOTH_NOT_MERGED = "Return both";

	/**
	 * This constant is usually used to specify that the StateKey should be
	 * persisted after each received entry.
	 */
	public static final int SAVE_STATE_AFTER_READ = 0;

	/**
	 * This constant is usually used to specify that the StatKey should be
	 * persisted after each AL cycle ends.
	 */
	public static final int SAVE_STATE_END_OF_CYCLE = 1;

	/**
	 * This constant is usually used to specify that the StatKey persistence is
	 * not done automatically by the TDI Server. The user is supposed to call
	 * the {@link #saveStateKey()} method in order to store the StateKey.
	 */
	public static final int SAVE_STATE_MANUAL = 2;

	/**
	 * Retrieves the method for storing StateKey.
	 * 
	 * @return the identifier of the method used for storing the StateKey in the
	 *         TDI Store.
	 * @see #SAVE_STATE_AFTER_READ
	 * 
	 * @see #SAVE_STATE_END_OF_CYCLE
	 * 
	 * @see #SAVE_STATE_MANUAL
	 * @throws Exception -
	 *             never
	 */
	public int getStateKeySaveMethod() throws Exception;

	/**
	 * Stores the USN values for the next synchronization. This method will skip
	 * the storing of the StateKey if the StateKey save method is set to
	 * {@link ChangelogInterface#SAVE_STATE_AFTER_READ}
	 * 
	 * @throws Exception -
	 *             never
	 */
	public void saveStateKey() throws Exception;

	/**
	 * Retrieves state key.
	 * 
	 * @return the StateKey, wrapped in some kind of object.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public Object getStateKeyObject() throws Exception;

}
