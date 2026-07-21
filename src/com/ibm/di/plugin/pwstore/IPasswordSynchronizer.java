/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.plugin.pwstore;

import java.util.Vector;

@Deprecated
public interface IPasswordSynchronizer{
	public boolean readyToSync(String aId);

	// APAR - IO07550
	//
	public boolean readyToSync(String aId, Vector aPasswordValues);

	public boolean syncPassword(String aId, Vector aPasswordValues);

	public boolean addPasswordValues(String aId, Vector aPasswordValues);

	public boolean deletePasswordValues(String aId, Vector aPasswordValues);
	
	public boolean setExtendedData(String id, String extendedData);

	public void initialize(Object aObj) throws Exception;

	public void terminate();

}
