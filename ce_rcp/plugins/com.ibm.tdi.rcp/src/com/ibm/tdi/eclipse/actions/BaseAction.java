/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

public abstract class BaseAction implements IObjectActionDelegate, IWorkbenchWindowActionDelegate {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private IAction action;
	private IWorkbenchPart targetPart;
	private ISelection selection;
	private IWorkbenchWindow window;

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.action = action;
		this.targetPart = targetPart;
	}
	
	public void selectionChanged(IAction action, ISelection selection) {
		this.action = action;
		this.selection = selection;
		action.setEnabled(selection != null && !selection.isEmpty());
	}
	
	public Shell getShell() {
		return getWindow().getShell();
	}
	
	public String getTitle() {
		return action.getText();
	}

	public void setSelection(ISelection selection) {
		this.selection = selection;		
	}
	
	public ISelection getSelection() {
		return selection;
	}
	
	public Object[] getSelectionItems() {
		if(selection instanceof IStructuredSelection)
			return ((IStructuredSelection)selection).toArray();
		else
			return null;
	}
	
	public Object getFirstSelection() {
		Object[] objs = getSelectionItems();
		if(objs != null && objs.length > 0)
			return objs[0];
		else
			return null;
	}

	public IWorkbenchPart getTargetPart() {
		return targetPart;
	}

	public boolean hasSelectionItem() {
		return getSelectionItems() != null;
	}

	public IAction getAction() {
		return action;
	}

	public void dispose() {
	}

	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	public IWorkbenchWindow getWindow() {
		if(window != null)
			return window;
		else if(targetPart != null)
			return targetPart.getSite().getWorkbenchWindow();
		else
			return getWorkbench().getActiveWorkbenchWindow();
	}
	
	public IWorkbench getWorkbench() {
		if(window != null)
			return window.getWorkbench();
		else if(targetPart != null)
			return targetPart.getSite().getWorkbenchWindow().getWorkbench();
		else
			return PlatformUI.getWorkbench();
	}
}
