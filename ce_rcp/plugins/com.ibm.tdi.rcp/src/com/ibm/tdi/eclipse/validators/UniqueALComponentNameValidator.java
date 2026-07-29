/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.validators;

import org.eclipse.jface.dialogs.IInputValidator;

import com.ibm.di.config.interfaces.AssemblyLineConfig;

public class UniqueALComponentNameValidator implements IInputValidator {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private AssemblyLineConfig alc;
	
	public UniqueALComponentNameValidator(AssemblyLineConfig alc) {
		super();
		this.alc = alc;
	}

	public String isValid(String newText) {
		if(alc.getComponent(newText) == null)
			return null;
		else
			return "Duplicate name: " + newText;
	}

}
