/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.wizards;

import com.ibm.di.config.interfaces.MetamergeConfig;

public class NewAttributeMapWizard extends NewComponentBaseWizard {
	@SuppressWarnings("unused") //$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public NewAttributeMapWizard() {
		super("attributemap", MetamergeConfig.DEFAULT_ATTRIBUTEMAP_FOLDER, "miadmin.menu.Object.NewAttributeMap.label"); //$NON-NLS-1$ //$NON-NLS-2$
		setShowTypes(false);
	}
}
