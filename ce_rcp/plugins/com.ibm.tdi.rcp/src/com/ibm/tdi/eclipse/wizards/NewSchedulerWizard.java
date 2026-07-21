/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.wizards;

import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.MetamergeConfig;

public class NewSchedulerWizard extends NewComponentBaseWizard {
	@SuppressWarnings("unused") //$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public NewSchedulerWizard() {
		super(TDIConfigurationFile.XT_SCHEDULER, MetamergeConfig.DEFAULT_SCHEDULER_FOLDER, "NewSchedulerWizard.label");
		setShowTypes(false);
	}
}
