/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;

import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.editors.ConfigSettingsEditor;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.natures.TDINature;

public class ConfigureSolutionAction extends BaseAction implements IWorkbenchWindowActionDelegate {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private IResource resource;
	private IWorkbenchWindow window;
	
	public void run(IAction action) {
		FileEditorInput fei = new FileEditorInput(Utils.getSolutionProps(resource));
		try {
			if(window != null)
				window.getActivePage().openEditor(fei, ConfigSettingsEditor.ID);
			else if (getTargetPart() != null)
				getTargetPart().getSite().getPage().openEditor(fei, ConfigSettingsEditor.ID);
		} catch (PartInitException e) {
			EclipseAppender.logerror(e.toString(), e);
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		
		resource = null;
		
		super.selectionChanged(action, selection);
		if(!action.isEnabled())
			return;
		
		Object[] items = getSelectionItems();
		if(items == null || items.length == 0)
			return;
		
		if(items[0] instanceof IResource)
			resource = (IResource) items[0];
		
		if(resource != null) {
			boolean enable = false;
			try {
				if(resource instanceof IProject) {
					if (((IProject)resource).isOpen())
						enable = ((IProject)resource).hasNature(TDINature.TDI_NATURE_ID);
				} else if(resource instanceof IFile) {
					enable = ((IFile)resource).getProject().hasNature(TDINature.TDI_NATURE_ID);
				}
				if(!enable)
					resource = null;
			} catch (Exception e) {
				resource = null;
				EclipseAppender.logerror(e.toString(), e);
			}
			
		}
		action.setEnabled(resource != null);
	}

	public void dispose() {
	}

	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

}
