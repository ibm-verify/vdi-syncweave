/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.text;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;

public class JavaScriptPartitioner extends FastPartitioner {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public JavaScriptPartitioner(IPartitionTokenScanner scanner) {
		super(scanner, 
				new String[]{
					IDocument.DEFAULT_CONTENT_TYPE,
					JavaScriptPartitionScanner.JAVASCRIPT_COMMENT,
					JavaScriptPartitionScanner.JAVASCRIPT_MULTILINE_COMMENT,
					JavaScriptPartitionScanner.JAVASCRIPT_CONST_STRING,
					JavaScriptPartitionScanner.JAVASCRIPT_CONST_KEYWORD,
					}
		);
	}

}
