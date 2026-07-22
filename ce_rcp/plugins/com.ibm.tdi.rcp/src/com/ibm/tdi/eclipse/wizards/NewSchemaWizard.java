/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.wizards;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;

public class NewSchemaWizard extends NewComponentBaseWizard {
	@SuppressWarnings("unused") //$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public NewSchemaWizard() {
		super("schema", MetamergeConfig.DEFAULT_CONNECTOR_FOLDER,"miadmin.menu.Object.NewSchema.label"); //$NON-NLS-1$ //$NON-NLS-2$
		setShowTypes(false);
	}

	@Override
	public void createConfigObject() {
		super.createConfigObject();
		if (getConfigObject() instanceof ConnectorConfig)
			((ConnectorConfig)getConfigObject()).setMode("Schema");
	}


}
