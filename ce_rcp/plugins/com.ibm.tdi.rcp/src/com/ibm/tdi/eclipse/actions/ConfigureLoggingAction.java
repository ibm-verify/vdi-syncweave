/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.widget.LoggingWidget;

public class ConfigureLoggingAction implements IObjectActionDelegate {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private IWorkbenchPart part;
	private BaseConfiguration target;

	public ConfigureLoggingAction() {
		super();
	}

	public ConfigureLoggingAction(IWorkbenchPart part, BaseConfiguration target) {
		super();
		this.part = part;
		this.target = target;
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.part = targetPart;
	}

	public void setTarget(BaseConfiguration target) {
		this.target = target;
	}
	
	public void run(IAction action) {
		Dialog dlg = new Dialog(part.getSite().getShell()) {
			@Override
			protected Control createDialogArea(Composite parent) {
				Composite c = (Composite) super.createDialogArea(parent);
				LoggingWidget widget = new LoggingWidget(c, SWT.TITLE, target);
				widget.getForm().setText(Messages.getString("assemblyline.tabs.logging.label")); 
				GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
				gd.heightHint = 400;
				gd.widthHint = 700;
				widget.setLayoutData(gd);
				getShell().setText(Messages.getString("assemblyline.tabs.settings.tooltip"));
				return c;
			}
			@Override
			protected void createButtonsForButtonBar(Composite parent) {
				// create only OK button by default
				createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
						true);
			}
		};
		dlg.open();
	}
	
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub
		
	}

}
