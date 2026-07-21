/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.navigator.CommonNavigator;

public class NewProject implements IWorkbenchWindowActionDelegate {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private IWorkbenchWindow window;
	private IStructuredSelection selection = StructuredSelection.EMPTY;

	public void dispose() {
	}

	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	public void run(IAction action) {
		com.ibm.tdi.eclipse.wizards.NewProject wiz = new com.ibm.tdi.eclipse.wizards.NewProject();
		wiz.init(window.getWorkbench(), selection);
		WizardDialog dlg = new WizardDialog(window.getShell(), wiz);
		if(dlg.open() == Window.OK) {
			CommonNavigator navigator = (CommonNavigator) window.getActivePage().findView("com.ibm.tdi.rcp.navigator");
			if(navigator != null) {
				navigator.selectReveal(new StructuredSelection(wiz.getProject()));
			}
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection)
			this.selection = (IStructuredSelection)selection;
	}

}
