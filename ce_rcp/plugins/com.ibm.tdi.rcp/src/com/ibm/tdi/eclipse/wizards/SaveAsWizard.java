/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.wizards;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

public class SaveAsWizard extends Wizard {
	@SuppressWarnings("unused") //$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	@Override
	public void addPages() {
		addPage(new WizardNewFileCreationPage("File", StructuredSelection.EMPTY)); //$NON-NLS-1$
	}

	@Override
	public boolean performFinish() {
		return true;
	}

}
