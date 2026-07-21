/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;

import com.ibm.tdi.eclipse.wizards.NewAttributeMapWizard;

public class NewAttributeMapAction extends BaseAction {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	
	public NewAttributeMapAction() {
	}

	public void run(IAction action) {
		NewAttributeMapWizard wiz = new NewAttributeMapWizard();
		WizardDialog dlg = null;
		wiz.init(getWorkbench(), (IStructuredSelection) getSelection());
		dlg = new WizardDialog(getShell(), wiz);
		dlg.open();
	}

}
