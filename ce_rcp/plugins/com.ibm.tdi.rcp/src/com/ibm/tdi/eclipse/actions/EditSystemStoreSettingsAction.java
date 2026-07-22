/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;

import com.ibm.tdi.eclipse.editors.ConfigSettingsEditor;
import com.ibm.tdi.eclipse.log.EclipseAppender;


/**
 * Opens up a config settings editor providing a server document file as input
 */
public class EditSystemStoreSettingsAction extends BaseAction {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public EditSystemStoreSettingsAction() {
	}

	public void run(IAction action) {
		IFile resource = (IFile) getSelectionItems()[0];
		FileEditorInput fei = new FileEditorInput(resource);
		try {
			IWorkbenchPage page = null;
			if(getWindow() != null)
				page = getWindow().getActivePage();
			else if (getTargetPart() != null)
				page = getTargetPart().getSite().getPage();
			if (page != null)
				page.openEditor(fei, ConfigSettingsEditor.ID, true, IWorkbenchPage.MATCH_ID|IWorkbenchPage.MATCH_INPUT);
		} catch (PartInitException e) {
			EclipseAppender.logerror(e.toString(), e);
		}
	}
}
