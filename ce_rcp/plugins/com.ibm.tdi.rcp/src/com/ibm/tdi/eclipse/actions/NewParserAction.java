/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;

import com.ibm.tdi.eclipse.wizards.NewParserWizard;

public class NewParserAction extends BaseAction {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	
	public NewParserAction() {
	}

	public void run(IAction action) {
		NewParserWizard wiz = new NewParserWizard();
		WizardDialog dlg = null;
		wiz.init(getWorkbench(), (IStructuredSelection) getSelection());
		dlg = new WizardDialog(getShell(), wiz);
		dlg.open();
	}

}
