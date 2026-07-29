/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.HooksConfig;
import com.ibm.di.config.interfaces.SchemaItemConfig;
import com.ibm.di.util.HookTree;

public class EnableConfigAction extends BaseAction {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public EnableConfigAction() {
	}

	public void run(IAction action) {
		boolean enabled = action.isChecked();
		for (Object o: getSelectionItems())  {
			BaseConfiguration config = getConfig(o);
			if(config instanceof ConnectorConfig && !enabled) {
				ConnectorConfig cc = (ConnectorConfig) config;
				cc.setState(ConnectorConfig.DISABLED_STATE);
			} else {
				config.setEnabled(enabled);
			}
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);
		action.setEnabled(false);
		action.setChecked(false);

		for (Object o: getSelectionItems())  {
			BaseConfiguration bc = getConfig(o);
			if (bc == null)
				return;
			if(bc instanceof ContainerConfig
					&& bc.getParent() instanceof AssemblyLineConfig)
				return;
			if (bc instanceof HooksConfig 
					|| bc instanceof SchemaItemConfig
					|| bc instanceof AttributeMapConfig)
				return;
		}
		
		BaseConfiguration bc = getConfig(getFirstSelection());
		if (bc == null)
			return;
		
		action.setEnabled(true);
		action.setChecked(bc.getEnabled());
	}
	
	private BaseConfiguration getConfig(Object obj) {
		if(obj instanceof BaseConfiguration) {
			return (BaseConfiguration) obj;
		} else if (obj instanceof HookTree) {
			return ((HookTree)obj).getHookConfig(false);
		}
		return null;
	}
}
