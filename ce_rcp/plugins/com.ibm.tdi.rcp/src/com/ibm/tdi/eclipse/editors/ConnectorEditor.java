/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.editors;

import org.eclipse.core.resources.IMarker;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.actions.ActionFactory;

import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.actions.CutConfigAction;
import com.ibm.tdi.eclipse.widget.ConnectorWidget;
import com.ibm.tdi.eclipse.actions.CopyConfigAction;

public class ConnectorEditor extends BaseEditor {
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public static final String ID = "com.ibm.tdi.editors.ConnectorEditor"; //$NON-NLS-1$

	private ConnectorWidget widget;

	private String[] tabButtons;

	public ConnectorEditor() {
		super();

		//
		// -- global action handlers
		// These action handlers make it impossible to cut and copy text inside everything that is used
		// by the ConnectorEditor, since they override the normal handlers.
		// E.g. you cannot copy/paste in the hooks editor or when configuring parser parameters.
		// Better to not have them, I think.
		//registerAction(ActionFactory.CUT.getId(), new CutConfigAction("Cut", null)); //$NON-NLS-1$
		//registerAction(ActionFactory.COPY.getId(), new CopyConfigAction("Copy")); //$NON-NLS-1$
		registerAction(ActionFactory.CUT.getId(), new CutConfigAction(Messages.getString("common.Cut.name"), null)); //$NON-NLS-1$
		registerAction(ActionFactory.COPY.getId(), new CopyConfigAction(Messages.getString("common.Copy.name"))); //$NON-NLS-1$
		registerAction(ActionFactory.DELETE.getId(), new CutConfigAction(Messages.getString("general.delete.label"), null)); //$NON-NLS-1$

	}

	@Override
	public void createPartControl(Composite parent) {
		if(getTDIConfiguration() == null) {
			super.createPartControl(parent);
			return;
		}

		widget = new ConnectorWidget(parent, SWT.NONE, getTDIConfiguration(), this);
		if(tabButtons != null)
			widget.setTabButtonNames(tabButtons);
		setModified(false);

		//
		// -- selection provider
		//
		getEditorSite().setSelectionProvider(getSelectionProvider());

//		createScriptEditor(sash);
//
//		sash.setWeights(new int[] { 100, 0 });

	}

	protected void setTabButtonNames(String[] buttons) {
		tabButtons = buttons;
	}

	@Override
	public void setFocus() {
		if(widget != null)
			widget.setFocus();
	}

	@Override
	public void gotoMarker(IMarker marker) {
		if(widget != null)
			widget.gotoMarker(marker);
	}

}
