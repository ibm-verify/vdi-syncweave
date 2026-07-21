/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.FileEditorInput;

import com.ibm.tdi.eclipse.editors.ConfigInstanceEditor;
import com.ibm.tdi.eclipse.log.EclipseAppender;

public class DebugServer extends Action implements IObjectActionDelegate {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private ISelection selection;
	private IWorkbenchPart part;

	public DebugServer() {
		super("DebugServer");
	}

	public void run(IAction action) {
		try {
			IFile file = (IFile) ((IStructuredSelection)selection).getFirstElement();
			part.getSite().getPage().openEditor(new FileEditorInput(file), ConfigInstanceEditor.EDITOR_ID);
		} catch (Exception e) {
			EclipseAppender.logerror(getText(), e, part.getSite().getShell());
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
		setEnabled(!selection.isEmpty());
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		part = targetPart;
	}

}
