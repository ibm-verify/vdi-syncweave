/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.wizards;

import org.eclipse.ui.INewWizard;

import com.ibm.di.config.interfaces.MetamergeConfig;

public class NewFunctionWizard extends NewComponentBaseWizard implements INewWizard {
	@SuppressWarnings("unused") //$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public NewFunctionWizard() {
		super("function", MetamergeConfig.DEFAULT_FUNCTION_FOLDER, "miadmin.menu.Object.NewFunction.label"); //$NON-NLS-1$ //$NON-NLS-2$
	}

}
