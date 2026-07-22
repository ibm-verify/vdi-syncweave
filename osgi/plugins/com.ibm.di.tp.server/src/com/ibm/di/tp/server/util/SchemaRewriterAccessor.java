/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.tp.server.util;

import com.ibm.di.schema.internal.SchemaRewriter;

/**
 * Provides synchronized access to dynamic component. <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.2
 */
public class SchemaRewriterAccessor {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	private SchemaRewriter rewriter;

	public SchemaRewriterAccessor() {
	}

	/**
	 * Sets the {@link SchemaRewriter} object
	 * 
	 * @param r
	 */
	public void setSchemaRewriter(SchemaRewriter r) {
		synchronized (this) {
			this.rewriter = r;
		}
	}

	/**
	 * @return the {@link SchemaRewriter} object or null, if it has been unset
	 *         dynamically.
	 */
	public SchemaRewriter getSchemaRewriter() {
		synchronized (this) {
			return this.rewriter;
		}
	}
}
