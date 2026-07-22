/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;

import com.ibm.di.config.interfaces.ALMappingConfig;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.tdi.eclipse.ConfigUtils;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.validators.UniqueALComponentNameValidator;

public class ReuseConnector extends BaseAction {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public ReuseConnector() {
	}

	public void run(IAction action) {
		ConnectorConfig cc = (ConnectorConfig) getFirstSelection();
		AssemblyLineConfig alc = Utils.getParentConfig(cc, AssemblyLineConfig.class);
		
		try {
			InputDialog id = new InputDialog(getShell(), Messages.getString("action.label.35"), Messages.getString("ConnectorWidget3.18"), cc.getShortName(), new UniqueALComponentNameValidator(alc));
			if(id.open() == Window.OK) {
				ConnectorConfig nc = ConfigUtils.createReusedConnector(cc.getMetamergeConfig(), alc, cc.getShortName());
				nc.setName(id.getValue());
				alc.getDataFlowComponents().addConfig(nc);
			}
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e, getShell());
		}
		
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);
		ConnectorConfig cc = (ConnectorConfig) getFirstSelection();
		if (cc instanceof FunctionConfig || cc instanceof ALMappingConfig)
			action.setEnabled(false);
		else
			action.setEnabled(Utils.getParentConfig(cc, AssemblyLineConfig.class) != null);
	}

}
