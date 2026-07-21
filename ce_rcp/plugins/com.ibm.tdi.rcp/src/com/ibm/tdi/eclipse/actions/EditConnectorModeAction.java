/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions;

import java.util.ArrayList;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import com.ibm.di.config.interfaces.ALMappingConfig;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.tdi.eclipse.Utils;

/**
 * See plugin.xml popup-menus extension for those actions that use this class.
 */
public class EditConnectorModeAction extends BaseAction {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public EditConnectorModeAction() {
	}

	public void run(IAction action) {
		String mode = getMode(action);
		ConnectorConfig cc = (ConnectorConfig) getFirstSelection();
		cc.setMode(mode);
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);
		Object obj = getFirstSelection();
		if (obj instanceof ConnectorConfig) {
			if (obj instanceof ALMappingConfig || obj instanceof FunctionConfig)
				action.setEnabled(false);
			else
				action.setEnabled(checkMode(action));
		}
	}

	private String getMode(IAction action) {
		return action.getId().substring(action.getId().lastIndexOf(".") + 1);
	}

	private boolean checkMode(IAction action) {
		try {
			String mode = getMode(action);
			ConnectorConfig cc = (ConnectorConfig) getFirstSelection();
			action.setChecked(cc.getMode().equals(mode));
			ArrayList<String> list = Utils.getSupportedModes(cc);
			if (list.contains(mode))
				return true;
			else
				return false;
		} catch (Exception e) {
			return true;
		}
	}

}
