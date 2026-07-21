/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.bind.provider;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import com.ibm.di.schema.internal.SchemaProvider;

/**
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.2
 */
public class ApiSchemaProvider implements SchemaProvider {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.schema.internal.SchemaProvider#getContextDir()
	 */
	public String getContextDir() {
		return "api";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.schema.internal.SchemaProvider#getSchema(java.lang.String)
	 */
	public InputStream getSchema(String fileName) throws IOException {
		URL url = getResourceUrl(fileName);
		return url != null ? url.openStream() : null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.schema.internal.SchemaProvider#schemaFileExists(java.lang.
	 * String)
	 */
	public boolean schemaFileExists(String fileName) {
		return getResourceUrl(fileName) != null;
	}

	static URL getResourceUrl(String fileName) {
		if (fileName == null) {
			throw new NullPointerException();
		}
		final Class<?> clazz = ApiSchemaProvider.class;
		String schemaPath = "schema/" + fileName;
		URL schemaLocation = clazz.getResource(schemaPath);
		if (schemaLocation == null) {
			schemaLocation = clazz.getResource("/" + schemaPath);
		}
		return schemaLocation;
	}
}
