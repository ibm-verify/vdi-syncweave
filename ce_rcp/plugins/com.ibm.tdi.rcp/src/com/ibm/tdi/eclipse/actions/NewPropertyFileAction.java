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
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.ibm.tdi.eclipse.wizards.NewPropertiesWizard;

public class NewPropertyFileAction implements IObjectActionDelegate {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private IWorkbenchPart targetPart;
	private ISelection selection;

	public NewPropertyFileAction() {
		// TODO Auto-generated constructor stub
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.targetPart = targetPart;
	}

	public void run(IAction action) {

		NewPropertiesWizard wiz = new NewPropertiesWizard();
		wiz.init(targetPart.getSite().getWorkbenchWindow().getWorkbench(), (IStructuredSelection) selection);
		WizardDialog dlg = new WizardDialog(targetPart.getSite().getShell(), wiz);
		dlg.open();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		action.setEnabled(true);
		this.selection = selection;
	}

}
