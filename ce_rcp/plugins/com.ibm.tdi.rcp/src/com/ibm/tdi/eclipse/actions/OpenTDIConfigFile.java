/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;

import com.ibm.tdi.eclipse.wizards.ImportConfigWizard;

public class OpenTDIConfigFile extends BaseAction {
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);
		action.setEnabled(true);
	}

	public void run(IAction action) {
		ImportConfigWizard wiz = new ImportConfigWizard();
		wiz.init(getWorkbench(), (IStructuredSelection) getSelection());
		WizardDialog dlg = new WizardDialog(getShell(), wiz);
		wiz.setInitialLinkFile(true);
		dlg.open();
	}
}
