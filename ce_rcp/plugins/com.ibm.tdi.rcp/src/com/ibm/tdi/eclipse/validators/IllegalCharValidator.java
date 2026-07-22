/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.validators;

import org.eclipse.jface.dialogs.IInputValidator;

import com.ibm.tdi.eclipse.Messages;

public class IllegalCharValidator implements IInputValidator {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private final String illegalChars;
	
	private final static String DEFAULT_CHARS = ".\\\"#";
	
	public IllegalCharValidator() {
		illegalChars = DEFAULT_CHARS;
	}

	public IllegalCharValidator(String illegalChars) {
		this.illegalChars = illegalChars;
	}

	public String isValid(String newText) {
		for (int i = 0; i < newText.length(); i++) {
			if (illegalChars.indexOf(newText.charAt(i)) >=0 )
				return Messages.getMessage("NewPropertiesWizard.illegal.char", newText.substring(i, i+1));
		}
		return null;
	}

}
