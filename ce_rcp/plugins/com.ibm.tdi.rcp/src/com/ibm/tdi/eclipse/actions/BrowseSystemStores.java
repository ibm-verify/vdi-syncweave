/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.ide.IDE;

import com.ibm.tdi.eclipse.editors.SystemStoreEditor;
import com.ibm.tdi.eclipse.log.EclipseAppender;

public class BrowseSystemStores implements IWorkbenchWindowActionDelegate {
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private IWorkbenchWindow window;

	public void dispose() {
	}

	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	public void run(IAction action) {
		try {
			IDE.openEditor(window.getActivePage(), SystemStoreEditor.createEditorInput(), SystemStoreEditor.EDITOR_ID);
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e, window.getShell());
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

}
