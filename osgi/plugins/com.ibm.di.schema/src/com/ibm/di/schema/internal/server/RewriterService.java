/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.schema.internal.server;

import java.net.URI;
import java.net.URISyntaxException;

import org.w3c.dom.Element;

import com.ibm.di.schema.internal.SchemaRewriter;

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
public class RewriterService implements SchemaRewriter {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	public void rewriteSchema(Element original, String baseUri, String contextDir) {
		URI bUri = URI.create(baseUri);
		URI contextUri = null;
		try {
			contextUri = new URI(bUri.getScheme(), bUri.getUserInfo(), bUri.getHost(), bUri.getPort(), SchemaServer.ROOT_CONTEXT
					+ "/" + contextDir, null, null);
			SchemaRemoteView.updateXsiSchemaLocation(original, contextUri.toString());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
