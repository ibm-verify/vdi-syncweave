/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

import java.util.*;

/**
 * This interface describes the methods provided by a folder object. A folder is
 * a container for other folders and leaf configuration objects.
 */
public interface MetamergeFolder extends BaseConfiguration {

	/**
	 * Returns a list of javax.naming.Binding objects for each of this folders
	 * child entries. The Binding object contains the name of the object as well
	 * as the object itself.
	 * 
	 * @return Enumeration of Binding objects
	 * @exception Exception
	 */
	public Enumeration list() throws Exception;

	/**
	 * This method returns an array of names contained in this folder.
	 * 
	 * @return The names value
	 * @exception Exception
	 */
	public String[] getNames() throws Exception;

	/**
	 * This method creates a sub-folder in this folder with the given name.
	 * 
	 * @param name
	 *            Name of sub-folder
	 * 
	 * @return The MetamergeFolder object representing the new sub-folder
	 * @exception Exception
	 */
	public MetamergeFolder createFolder(Object name) throws Exception;
}
