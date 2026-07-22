/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.wizards;

import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.tdi.eclipse.Messages;

public class NewSequenceWizard extends NewComponentBaseWizard {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public NewSequenceWizard() {
		super(TDIConfigurationFile.XT_SEQUENCE, MetamergeConfig.DEFAULT_SEQUENCE_FOLDER, Messages.getString("NewSequenceWizard.label"));
		setShowTypes(false);
	}
}
