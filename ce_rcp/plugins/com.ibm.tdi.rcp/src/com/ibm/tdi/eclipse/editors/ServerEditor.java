/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.editors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.widget.ServerWidget;

public class ServerEditor extends BaseEditor {
	@SuppressWarnings("unused") //$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	private ServerWidget widget;

	public final static String ID = "com.ibm.tdi.editors.ServerEditor";
	
	public ServerEditor() {
	}

	@Override
	public void createPartControl(Composite parent) {
		if(getTDIConfiguration() == null) {
			super.createPartControl(parent);
			return;
		}
		widget = new ServerWidget(parent, SWT.NULL, getTDIConfiguration(), this);
		setModified(false);
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		// Called from ServerWidget
		if(monitor != null) {
			if(isDirty() && widget.hasSolutionDirectory()) {
				if(MessageDialog.openQuestion(widget.getShell(), 
						Messages.getString("editor.name.7"),
						Messages.getMessage("ServerEditor.synch.properties", null))) {
					widget.updateSolutionDirectory();
				}
			}
		}
		super.doSave(monitor);
	}

	@Override
	public void setFocus() {
		super.setFocus();
		if(widget != null)
			widget.setFocus();
	}

}
