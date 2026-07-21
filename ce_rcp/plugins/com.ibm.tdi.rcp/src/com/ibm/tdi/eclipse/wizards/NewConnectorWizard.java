/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.wizards;

import com.ibm.di.config.interfaces.MetamergeConfig;

public class NewConnectorWizard extends NewComponentBaseWizard {
	@SuppressWarnings("unused") //$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public NewConnectorWizard() {
		super("connector", MetamergeConfig.DEFAULT_CONNECTOR_FOLDER, "miadmin.menu.Object.NewConnector.label"); //$NON-NLS-1$ //$NON-NLS-2$
		setModeRequested(true);
	}
}
