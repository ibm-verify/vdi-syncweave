/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;

import com.ibm.tdi.eclipse.wizards.NewScriptWizard;

public class NewScriptAction extends BaseAction {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public NewScriptAction() {
	}

	public void run(IAction action) {
		NewScriptWizard wiz = new NewScriptWizard();
		WizardDialog dlg = null;
		wiz.init(getWorkbench(), (IStructuredSelection) getSelection());
		dlg = new WizardDialog(getShell(), wiz);
		dlg.open();
	}

}
