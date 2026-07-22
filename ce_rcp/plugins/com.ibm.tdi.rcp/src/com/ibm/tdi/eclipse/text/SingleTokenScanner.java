/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.text;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;

public class SingleTokenScanner extends RuleBasedScanner {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	public SingleTokenScanner(TextAttribute attribute) {
		setDefaultReturnToken(new Token(attribute));
	}
}
