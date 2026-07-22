/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

/**
 * This interface gives access to a configuration's external properties.
 * External properties are special in that the data is stored in an external
 * file rather than by the MetamergeConfig object's storage.
 * 
 * Use the BaseConfiguration's getXXX/setXXX methods to read and write
 * properties.
 * @deprecated use {@link TDIProperties} instead
 */
@Deprecated
public interface ExternalPropertiesConfig extends BaseConfiguration {

	/**
	 * Gets the filePath attribute of the ExternalPropertiesConfig object
	 * 
	 * @return The filePath value
	 */
	public String getFilePath();

	/**
	 * Sets the filePath attribute of the ExternalPropertiesConfig object
	 * 
	 * @param path
	 *            The new filePath value
	 */
	public void setFilePath(String path);

	/**
	 * Returns the password used when opening an encrypted file
	 */
	public String getPassword();

	/**
	 * Sets the password used when opening an encrypted file
	 */
	public void setPassword(String password);

	/**
	 * Returns the Cipher algorithm used when reading/writing an encrypted file
	 */
	public String getCipher();

	/**
	 * Sets the Cipher algorithm used when reading/writing an encrypted file
	 */
	public void setCipher(String cipher);

	/**
	 * Gets the encrypted flag of the ExternalPropertiesConfig object
	 * 
	 * @return The encrypted value
	 */
	public boolean getEncrypted();

	/**
	 * Sets the encrypted flag of the ExternalPropertiesConfig object
	 * 
	 * @param encrypted
	 *            The new encrypted value
	 */
	public void setEncrypted(boolean encrypted);

	/**
	 * Gets the saveNeeded flag of the ExternalPropertiesConfig object
	 * 
	 * @return The saveNeeded value
	 */
	public boolean getSaveNeeded();

	/**
	 * This method reads the external properties into memory for subseqent use.
	 * This is typically done automatically by the hosting MetamergeConfig
	 * object when this object is requested.
	 * 
	 * @exception Exception
	 *                if the operation does not succeed
	 */
	public void loadData() throws Exception;

	/**
	 * This method merges the contents of a file into the current list of
	 * properties.
	 * 
	 * @param path
	 *            The file path to load properties from
	 * @exception Exception
	 *                if the operation does not succeed
	 */
	public void mergeData(String path) throws Exception;

	/**
	 * This method writes back the data to the external file.
	 * 
	 * @exception Exception
	 *                if the operation does not succeed
	 */
	public void saveData() throws Exception;

}
