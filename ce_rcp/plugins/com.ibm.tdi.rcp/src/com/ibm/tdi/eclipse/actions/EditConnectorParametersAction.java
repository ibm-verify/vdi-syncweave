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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.TDI;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.widget.DiscoverSchemaWidget;
import com.ibm.tdi.eclipse.widget.LinkCriteriaWidget;
import com.ibm.tdi.eclipse.widget.ParserWidget;
import com.ibm.tdi.eclipse.widget.RawConnectorWidget;

public class EditConnectorParametersAction extends BaseAction {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	private DiscoverSchemaWidget dsw = null;
	
	public EditConnectorParametersAction() {
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);
		action.setEnabled(false);
		if(getFirstSelection() instanceof ConnectorConfig) {
			ConnectorConfig config = (ConnectorConfig) getFirstSelection();
			String id = action.getActionDefinitionId();
			if(TDI.ID_PARSER_CONFIG.equals(id))
				action.setEnabled(Utils.hasParserRequirements(config));
			else if(TDI.ID_LINKCRITIERIA_CONFIG.equals(id))
				action.setEnabled(Utils.hasLinkRequirements(config));
			else
				action.setEnabled(Utils.hasConnectorRequirements(config));
		}
	}

	public void run(IAction action) {
		final String id = action.getActionDefinitionId();
		Dialog dlg = new Dialog(getShell()) {
			@Override
			protected Control createDialogArea(Composite parent) {
				getShell().setText(Messages.getString("EditConnectorParametersAction.1"));
				Composite control = (Composite) super.createDialogArea(parent);
				Composite widget = getWidget(control, id);
				widget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				return control;
			}
			@Override
			protected void createButtonsForButtonBar(Composite parent) {
				createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
						true);
			}
			@Override
			protected Point getInitialSize() {
				return new Point(600,450);
			}
			@Override
			protected void okPressed() {
				setReturnCode(OK);
				if (dsw != null)
					dsw.createAttributeMaps();					
				close();
			}

		};
		dlg.open();
	}

	private Composite getWidget(Composite parent, String id) {
		ConnectorConfig cc = (ConnectorConfig) getFirstSelection();
		if (TDI.ID_DISCOVER_CONFIG.equals(id))
			return dsw = new DiscoverSchemaWidget(parent, SWT.CHECK, cc, null);
		if(TDI.ID_LINKCRITIERIA_CONFIG.equals(id))
			return new LinkCriteriaWidget(cc.getLinkCriteria(), parent, SWT.NULL);
		if(TDI.ID_PARSER_CONFIG.equals(id))
			return new ParserWidget(parent, SWT.FILL, cc.getParserConfig());

		if(cc instanceof FunctionConfig)
			return new RawConnectorWidget(parent, SWT.NONE, ((FunctionConfig)cc).getFunctionConfig(), true);
		else
			return new RawConnectorWidget(parent, SWT.NONE, cc.getConnectionConfig(), true);
	}
}
