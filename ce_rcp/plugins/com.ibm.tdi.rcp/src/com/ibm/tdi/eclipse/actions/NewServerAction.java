/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;

import com.ibm.tdi.eclipse.wizards.NewServerWizard;

public class NewServerAction extends BaseAction {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public NewServerAction() {
	}

	public void run(IAction action) {
		NewServerWizard wiz = new NewServerWizard();
		WizardDialog dlg = null;
		wiz.init(getWorkbench(), (IStructuredSelection) getSelection());
		dlg = new WizardDialog(getShell(), wiz);
		dlg.open();
	}

}
