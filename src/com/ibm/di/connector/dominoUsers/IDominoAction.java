/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.dominoUsers;

import com.ibm.di.entry.Entry;

/**
 * IDominoAction provides a common interface for classes that perfom a special
 * Action with Domino.
 * <p>
 * Such classes will capsulate the necessary data to perform the operation and
 * perform the operation itself. They will act as static mechanisms used by the
 * DominoUsersConnector to perform special actions. Common pattern for all
 * IDominoAction-s is that they will take the decision if the Action has to be
 * performed and get the neccesary data to perform the Action based on a fixed
 * schema of Entry Attributes.
 */
public interface IDominoAction {

	/**
	 * This method extracts and stores data. It performs the following actions:
	 * <p>
	 * (1) Extract data from the given Entry according to the fixed schema of
	 * Attributes;
	 * <p>
	 * (2) Stores this data into local data holders;
	 * <p>
	 * 
	 * @param aEntry
	 *            The Entry object to extract data from.
	 * @return an Entry object that contains all Attributes of the aEntry
	 *         parameter except those from the fixed schema.
	 * @throws Exception
	 *             if an error that cannot be handled occurs while operating.
	 */
	public Entry extractAndStoreData(Entry aEntry) throws Exception;

	/**
	 * Inspects the Attributes of the given Entry and determines if the Domino
	 * Action has to be performed.
	 * 
	 * @param aEntry
	 *            The Entry object which Attributes hold the necessary
	 *            information whether to perform the Domino Action.
	 * @return "true" if the Domino Action must be performed; "false" otherwise.
	 * @throws Exception
	 *             if an error that cannot be handled occurs while operating.
	 */
	public boolean mustPerform(Entry aEntry) throws Exception;

	/**
	 * Inspects the data stored in the local data holders and determines if it
	 * is consistent and the Domino Action can be performed.
	 * 
	 * @return "null" if the Domino Action can be performed with the current
	 *         data;
	 *         <p>
	 *         and a non-empty string message explaining the problem, if the
	 *         data is not consistent and the Action cannot be performed.
	 */
	public String canPerform();

	/**
	 * Performs the Domino Action useing the data from the local data holders.
	 * 
	 * @throws Exception
	 *             if an error that cannot be handled occurs while operating.
	 */
	public void perform() throws Exception;

	/**
	 * Resets the local data holders to empty/default values according the
	 * business logic of the Domino Action.
	 * 
	 * @throws Exception
	 *             if an error that cannot be handled occurs while operating.
	 */
	public void resetData() throws Exception;

}
