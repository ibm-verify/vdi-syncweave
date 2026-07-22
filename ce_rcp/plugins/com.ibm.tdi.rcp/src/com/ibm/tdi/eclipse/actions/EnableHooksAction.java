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
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.LoopConfig;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.widget.HooksWidget;

public class EnableHooksAction implements IObjectActionDelegate {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private IWorkbenchPart part;
	private BaseConfiguration target;

	public EnableHooksAction() {
	}

	public EnableHooksAction(IWorkbenchPart part, BaseConfiguration target) {
		super();
		this.part = part;
		this.target = target;
	}

	public void setTarget(BaseConfiguration target) {
		this.target = target;
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.part = targetPart;
	}

	public void run(IAction action) {
		Dialog dlg = new Dialog(part.getSite().getShell()) {
			@Override
			protected Control createDialogArea(Composite parent) {
				Composite c = (Composite) super.createDialogArea(parent);
				HooksWidget hooks = new HooksWidget(target, parent, SWT.RESIZE);
				GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
				gd.heightHint = 400;
				gd.widthHint = 700;
				hooks.setLayoutData(gd);
				String msg = Messages.getString("EnableHoooksAction.1");
				if (target instanceof AssemblyLineConfig)
					msg = Messages.getString("assemblyline.tabs.settings.tooltip");
				getShell().setText(msg);
				return c;
			}
			@Override
			protected void createButtonsForButtonBar(Composite parent) {
				// create only OK button by default
				createButton(parent, IDialogConstants.OK_ID, IDialogConstants.CLOSE_LABEL,
						true);
			}
		};
		dlg.open();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		action.setEnabled(false);
		if(selection instanceof IStructuredSelection) {
			IStructuredSelection sel = (IStructuredSelection)selection;
			if (sel.getFirstElement() instanceof ConnectorConfig) {
				target = (BaseConfiguration) sel.getFirstElement();
				action.setEnabled(true);
			} else if (sel.getFirstElement() instanceof LoopConfig) {
				LoopConfig lc = (LoopConfig) sel.getFirstElement();
				if (lc.getLoopType() == LoopConfig.LOOP_CONNECTOR_FC) {
					try {
						target = lc.getLoopConnector();
						action.setEnabled(true);
					} catch (Exception e) {
						// Could actually do nothing, but findbugs will complain...
						action.setEnabled(false);						
					}
				}
			}
		}
	}
}
