/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.util.InheritanceUtil;

public class ChangeInheritanceAction extends Action implements IObjectActionDelegate {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private BaseConfiguration config;

	public ChangeInheritanceAction() {
	}
	
	public ChangeInheritanceAction(BaseConfiguration config) {
		super();
		this.config = config;
		setText(Messages.getString("action.label.24"));
		setToolTipText(config.getInheritsFromRef());
	}

	@Override
	public void run() {
		InheritanceUtil.changeInheritance(config);
		setToolTipText(config.getInheritsFromRef());
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	public void run(IAction action) {
		run();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		action.setEnabled(false);
		if(selection instanceof IStructuredSelection) {
			Object obj = ((IStructuredSelection)selection).getFirstElement();
			if(obj instanceof BaseConfiguration) {
				config = (BaseConfiguration) obj;
				action.setEnabled(true);
			}
		}
	}

}
