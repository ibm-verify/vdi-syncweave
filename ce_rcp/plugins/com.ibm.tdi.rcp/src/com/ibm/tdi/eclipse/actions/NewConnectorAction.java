/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;

import com.ibm.tdi.eclipse.wizards.NewConnectorConfigWizard;

public class NewConnectorAction extends BaseAction {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	
	public NewConnectorAction() {
	}

	public void run(IAction action) {
		NewConnectorConfigWizard wiz = new NewConnectorConfigWizard(getWorkbench(), (IStructuredSelection) getSelection());
		WizardDialog dlg =  new WizardDialog(getShell(), wiz);
		dlg.addPageChangingListener(wiz);
		dlg.setPageSize(600, 400);
		dlg.open();
	}

}
