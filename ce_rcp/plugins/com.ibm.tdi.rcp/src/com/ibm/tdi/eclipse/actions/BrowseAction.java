/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;

import com.ibm.di.config.interfaces.ALMappingConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.BranchingConfig;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.LoopConfig;
import com.ibm.di.server.BranchingComponent;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.editors.BaseEditor;
import com.ibm.tdi.eclipse.editors.DataBrowserEditor;
import com.ibm.tdi.eclipse.editors.TDIConfigEditorInput;

public class BrowseAction extends BaseAction {

	@SuppressWarnings("unused") 
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public BrowseAction() {
	}

	public void run(IAction action) {
		try {

			Object sel = getFirstSelection();
			IWorkbenchPage page = getTargetPart().getSite().getPage();
			if (page == null)
				return;
			if(sel instanceof IFile) {
				IEditorPart editor = IDE.openEditor(page, (IFile) sel, DataBrowserEditor.EDITOR_ID);
				if(editor instanceof DataBrowserEditor)
					return;
				if(editor instanceof BaseEditor) {
					BaseConfiguration ec = ((BaseEditor)editor).getTDIConfiguration();
					TDIConfigEditorInput input = new TDIConfigEditorInput(ec, DataBrowserEditor.EDITOR_ID);
					page.openEditor(input, DataBrowserEditor.EDITOR_ID, true, IWorkbenchPage.MATCH_INPUT | IWorkbenchPage.MATCH_ID );
				}
			} else if (sel instanceof BaseConfiguration) {
				TDIConfigEditorInput input = new TDIConfigEditorInput((BaseConfiguration) sel, DataBrowserEditor.EDITOR_ID);
				page.openEditor(input, DataBrowserEditor.EDITOR_ID, true, IWorkbenchPage.MATCH_INPUT | IWorkbenchPage.MATCH_ID );
			}
				
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);
		Object sel = getFirstSelection();
		if (sel instanceof IFile)
			action.setEnabled(true);
		else if (sel instanceof ConnectorConfig && ! (sel instanceof ALMappingConfig))
			action.setEnabled(!Utils.isAssemblyLine((ConnectorConfig)sel));
		else if (sel instanceof LoopConfig)
			action.setEnabled(((LoopConfig)sel).getLoopType() == LoopConfig.LOOP_CONNECTOR_FC);
		else
			action.setEnabled(false);
	}

}
