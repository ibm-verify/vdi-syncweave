/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.editors;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.EditorActionBarContributor;


public class BaseEditorActionBarContributor extends EditorActionBarContributor {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private final String[] GLOBAL_ACTIONS = new String[] {
			ActionFactory.CUT.getId(),
			ActionFactory.COPY.getId(),
			ActionFactory.PASTE.getId(),
			ActionFactory.DELETE.getId(),
			ActionFactory.UNDO.getId(),
			ActionFactory.REDO.getId(),
			ActionFactory.SELECT_ALL.getId(),
			ActionFactory.FIND.getId(),
	};

	BaseEditor baseEditor = null;
	
	public BaseEditorActionBarContributor() {
	}

	@Override
	public void setActiveEditor(IEditorPart targetEditor) {
		if(targetEditor instanceof BaseEditor) {
			baseEditor = (BaseEditor) targetEditor;
			for(String str : GLOBAL_ACTIONS) {
				IAction action = baseEditor.getActionFor(str);
				if (action != null)
					getActionBars().setGlobalActionHandler(str, action);
			}
			if(baseEditor.getUndoRedo() != null)
				baseEditor.getUndoRedo().fillActionBars(getActionBars());

			getActionBars().updateActionBars();
			
	        contributeToStatusLine(getActionBars().getStatusLineManager());
	        contributeToMenu(getActionBars().getMenuManager());
	        contributeToToolBar(getActionBars().getToolBarManager());
		}
	}

	@Override
	public void contributeToStatusLine(IStatusLineManager statusLineManager) {
		if(baseEditor != null) {
			for(EditorActionBarContributor contributor : baseEditor.getEditorContributors()) {
				contributor.contributeToStatusLine(statusLineManager);
			}
		}
	}

	@Override
	public void contributeToCoolBar(ICoolBarManager coolBarManager) {
		if(baseEditor != null) {
			for(EditorActionBarContributor contributor : baseEditor.getEditorContributors()) {
				contributor.contributeToCoolBar(coolBarManager);
			}
		}
	}

	@Override
	public void contributeToMenu(IMenuManager menuManager) {
		if(baseEditor != null) {
			for(EditorActionBarContributor contributor : baseEditor.getEditorContributors()) {
				contributor.contributeToMenu(menuManager);
			}
		}
	}

	@Override
	public void contributeToToolBar(IToolBarManager toolBarManager) {
		if(baseEditor != null) {
			for(EditorActionBarContributor contributor : baseEditor.getEditorContributors()) {
				contributor.contributeToToolBar(toolBarManager);
			}
		}
	}

}
