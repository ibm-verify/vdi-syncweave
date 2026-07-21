/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.ibm.di.config.base.SchemaItemConfigImpl;
import com.ibm.di.config.interfaces.SchemaConfig;
import com.ibm.di.config.interfaces.SchemaItemConfig;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.log.EclipseAppender;


public class SchemaAddChildAction implements IObjectActionDelegate, IWorkbenchWindowActionDelegate {
	@SuppressWarnings("unused") //$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private IWorkbenchPart part;
	private ISelection selection;

	public SchemaAddChildAction() {
		super();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
		if (selection.isEmpty()) {
			action.setEnabled(false);				
			return;
		}
		SchemaItemConfig sic = (SchemaItemConfig) ((IStructuredSelection)selection).getFirstElement();
		SchemaConfig sc = (SchemaConfig) Utils.getParentConfig(sic, SchemaConfig.class);
		if(sc != null && "AssemblyLineInitParams".equals(sc.getShortName()))
			action.setEnabled(false);
		else
			action.setEnabled(true);
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.part = targetPart;
	}

	public void run(IAction action) {
		SchemaItemConfig sic = (SchemaItemConfig) ((IStructuredSelection)selection).getFirstElement();
		InputDialog id = new InputDialog(part.getSite().getShell(), Messages.getString("SchemaAddChildAction.title"), Messages.getString("SchemaAddChildAction.message"), "", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if (id.open() != Window.OK)
			return;
		String name = id.getValue();
		if(name != null && name.trim().length() > 0) {
			name = name.trim();
			SchemaItemConfig s = new SchemaItemConfigImpl();
			s.setAttributeName(name);
			try {
				s.setName(name);
			} catch (Exception e) {
				EclipseAppender.logerror(name, e);
			}
			s.setPresenceFlag(SchemaItemConfig.PRESENCE_OPTIONAL);
			sic.getChildSchemaList().addConfig(s);
		}
	}


	public void dispose() {
	}


	public void init(IWorkbenchWindow window) {
	}

}
