/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.navigator;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.navigator.ILinkHelper;
import org.eclipse.ui.part.FileEditorInput;

import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.tdi.eclipse.editors.BaseEditor;

/**
 * This class links the active editor to the TDI provided objects in the common navigator.
 * For all non-TDI editors it simply links the file input file. 
 */
public class LinkHelper implements ILinkHelper {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private IWorkbenchPage page;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.ILinkHelper#activateEditor(org.eclipse.ui.IWorkbenchPage, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void activateEditor(IWorkbenchPage aPage, IStructuredSelection aSelection) {
		this.page = aPage;
		if (aSelection == null || aSelection.isEmpty())
			return;
		
		Object sel = aSelection.getFirstElement();
		IEditorInput fileInput = null;
		BaseConfiguration config = null;
		
		if(sel instanceof BaseConfiguration) {
			config = (BaseConfiguration)sel;
			fileInput = new FileEditorInput(((TDIConfigurationFile)config.getMetamergeConfig()).getFile());

		} else if (sel instanceof IFile) {
			fileInput = new FileEditorInput((IFile)sel);
		}
		
		if (fileInput != null) {
			IEditorPart editor = null;
			if ((editor = aPage.findEditor(fileInput)) != null) {
				aPage.bringToTop(editor);
			}
			if(editor instanceof BaseEditor && config != null)
				((BaseEditor)editor).setSelection(new StructuredSelection(config));
			else if(editor instanceof BaseEditor && config == null)
				((BaseEditor)editor).setSelection(StructuredSelection.EMPTY);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.ILinkHelper#findSelection(org.eclipse.ui.IEditorInput)
	 */
	public IStructuredSelection findSelection(IEditorInput anInput) {
		if (anInput instanceof IFileEditorInput) {
			if(page != null) {
				IEditorPart editor = page.findEditor(anInput);
				if(editor instanceof BaseEditor) {
					BaseConfiguration bc = ((BaseEditor)editor).getCurrentConfigObject();
					if(bc != null)
						return new StructuredSelection(bc);
				}
			}
			return new StructuredSelection(((IFileEditorInput) anInput).getFile());
		}

		return StructuredSelection.EMPTY;
	}

}
