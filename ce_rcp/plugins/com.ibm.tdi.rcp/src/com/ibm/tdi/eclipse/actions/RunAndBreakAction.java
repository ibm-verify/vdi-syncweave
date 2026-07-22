/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;

import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.AttributeMapItem;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.BranchingConfig;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.HookConfig;
import com.ibm.di.config.interfaces.ScriptConfig;
import com.ibm.di.util.HookTree;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.editors.AssemblyLineEditor3;

public class RunAndBreakAction extends BaseAction {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public RunAndBreakAction() {
	}

	public void run(IAction action) {

		IEditorPart editor = getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if(!(editor instanceof AssemblyLineEditor3))
			return;

		String breakpoint = null;
		Object obj = getFirstSelection();

		if(obj instanceof HookTree) {
			breakpoint = Utils.getParentConfig(((HookTree)obj).getHooksConfig(), ConnectorConfig.class).getShortName() + "." + ((HookTree)obj).getName();
		} else if(obj instanceof BaseConfiguration) {
			BaseConfiguration element = (BaseConfiguration) obj;
			if (element instanceof HookConfig) {
				BaseConfiguration granny = element.getParent().getParent();
				if (granny instanceof AssemblyLineConfig) {
					breakpoint = (String) ((HookConfig) element).getHookName();
				} else {
					breakpoint = granny.getShortName() + "." + ((HookConfig) element).getHookName();
				}
			} else if (element instanceof AttributeMapItem) {
				breakpoint = element.getParent().getParent().getShortName() + "." + element.getParent().getShortName() + "."
				+ element.getShortName();
			} else {
				breakpoint = element.getShortName();
			}
		}
		if(breakpoint != null)
			((AssemblyLineEditor3)editor).runAssemblyLine(breakpoint);
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);
		if (action.isEnabled()) {
			Object o = getFirstSelection();
			boolean enabled = 
				o instanceof AttributeMapConfig ||
				o instanceof HookConfig ||
				o instanceof ConnectorConfig ||
				o instanceof BranchingConfig ||
				o instanceof ScriptConfig ||
				o instanceof HookTree;
			if (!enabled)
				action.setEnabled(false);
		}
	}


}
