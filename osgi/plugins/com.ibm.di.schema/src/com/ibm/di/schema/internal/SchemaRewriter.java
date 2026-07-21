/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.schema.internal;

import org.w3c.dom.Element;

/**
 * Defines a utility service for rewriting first level element's schemaLocation
 * attribute value to point to the actual XSDs this bundle exposes through HTTP. <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.2
 */
public interface SchemaRewriter {

	/**
	 * Rewrites the local reference to the schema within the schemaLocation
	 * attribute. Callers MUST provide the appropriate
	 * "{http://www.w3.org/2001/XMLSchema-instance}schemaLocation" attribute
	 * containing a list of pairs like:
	 * "schemaNamespace1 schemaLocation1 schemaNamespace2 schemaLocation2..."
	 * 
	 * @param original
	 *            the original DOM element
	 * @param baseUri
	 *            the base URI of the request
	 * @param contextDir
	 *            the contextDir that matches the one a {@link SchemaProvider}
	 *            has been registered with.
	 */
	public void rewriteSchema(Element original, String baseUri, String contextDir);

}
