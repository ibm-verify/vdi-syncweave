/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.wizards;

import com.ibm.di.config.interfaces.MetamergeConfig;

public class NewScriptWizard extends NewComponentBaseWizard {
	@SuppressWarnings("unused") //$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public NewScriptWizard() {
		super("script", MetamergeConfig.DEFAULT_SCRIPT_FOLDER,"miadmin.menu.Object.NewScript.label"); //$NON-NLS-1$ //$NON-NLS-2$
		setShowTypes(false);
	}
}
