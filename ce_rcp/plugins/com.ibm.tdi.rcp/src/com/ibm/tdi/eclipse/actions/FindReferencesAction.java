/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.ibm.di.config.interfaces.BaseConfiguration;

public class FindReferencesAction implements IWorkbenchWindowActionDelegate {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private BaseConfiguration config;
	
	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void init(IWorkbenchWindow window) {
		// TODO Auto-generated method stub

	}

	public void run(IAction action) {
		System.out.println("Find references: " + config);
		if(config == null)
			return;
	}

	public void selectionChanged(IAction action, ISelection selection) {
		config = null;
		if(selection.isEmpty()) {
			return;
		}
		
		if((selection instanceof IStructuredSelection) &&
				(((IStructuredSelection)selection).getFirstElement() instanceof BaseConfiguration)) {
			config = (BaseConfiguration) ((IStructuredSelection)selection).getFirstElement();
		}
	}

}
