/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

import com.ibm.tdi.eclipse.editors.PropertiesEditor;

public class InsertPropertyAction implements IEditorActionDelegate {

	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private IEditorPart editor;

	public InsertPropertyAction() {
	}

	public void run(IAction action) {
		if(editor instanceof PropertiesEditor) {
			((PropertiesEditor)editor).addPropertyDialog();
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		this.editor = targetEditor;
		action.setEnabled(editor instanceof PropertiesEditor);
	}

}
