/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;

import com.ibm.tdi.eclipse.wizards.NewAssemblyLineWizard;

public class NewAssemblyLine extends BaseAction {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public NewAssemblyLine() {
	}

	public void run(IAction action) {
		NewAssemblyLineWizard wiz = new NewAssemblyLineWizard();
		WizardDialog dlg = null;
		wiz.init(getWindow().getWorkbench(), (IStructuredSelection) getSelection());
		dlg = new WizardDialog(getShell(), wiz);
		dlg.open();
	}
}
