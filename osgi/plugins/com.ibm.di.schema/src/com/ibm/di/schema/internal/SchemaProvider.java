/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.schema.internal;

import java.io.IOException;
import java.io.InputStream;

/**
 * Defines a protocol for providing schema information. <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.2
 */
public interface SchemaProvider {

	/**
	 * @return the directory path, relative to the web root, which the schema
	 *         files are located into. The path must not start with slash, don't
	 *         end with one, and also don't contain one.
	 */
	public String getContextDir();

	/**
	 * Opens the schema file for reading. <br>
	 * <br>
	 * <b>Note:</b> the returned schema can have paths with at most one
	 * parent-axis reference (".."), in order to switch to another contextDir.
	 * For example the Schema file under the "api" contextDir can have an
	 * import:<br>
	 * <br>
	 * &lt;import schemaLocation= "../config/solution.xsd"
	 * namespace="http://www.ibm.com/xmlns/prod/tdi/72/config" &gt;
	 * 
	 * <br>
	 * To refer to the "config" contextDir.
	 * 
	 * @param fileName
	 *            the name of the schema file to open. The name is relative to
	 *            the congetxtDir provided.
	 * 
	 * @return the input stream to the requested file, null otherwise. Note:
	 *         Caller is responsible to close it once done.
	 */
	public InputStream getSchema(String fileName) throws IOException;

	/**
	 * @param fileName
	 *            the name of the schema file to open. The name is relative to
	 *            the congetxtDir provided.
	 * @return true if such file exists, false otherwise.
	 */
	public boolean schemaFileExists(String fileName);
}
