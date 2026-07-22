/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions;

import org.apache.xerces.dom.AttributeMap;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;

import com.ibm.di.config.base.InternalSchema;
import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.AttributeMapItem;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.HookConfig;
import com.ibm.di.config.interfaces.ScriptConfig;
import com.ibm.di.function.SystemFunctions;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.util.InheritanceUtil;

public class RestoreInheritanceAction extends BaseAction {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	private BaseConfiguration b = null;

	public RestoreInheritanceAction() {
	}

	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);
		if (selection.isEmpty())
			b = null;
		else
			b = InheritanceUtil.getConfigFromSelection(getSelectionItems()[0]);
		action.setEnabled(b != null);
	}
	
	public void run(IAction action) {
		if (b == null)
			return;
		
		if(!MessageDialog.openConfirm(getShell(),
				Messages.getString("RestoreInheritanceAction.1"),
				Messages.getString("RestoreInheritanceAction.2")))
			return;
		
		for (Object o:getSelectionItems())
			restoreInheritance(InheritanceUtil.getConfigFromSelection(o));
			
	}

	private void restoreInheritance(BaseConfiguration object) {
		if(object instanceof AttributeMapItem) {
			if (object.getParent() instanceof AttributeMapConfig)
				((AttributeMapConfig)object.getParent()).removeAttributeMapItem(object);
		} else if (object instanceof HookConfig) {
			HookConfig hc = (HookConfig) object;
			hc.removeParameter(InternalSchema.HC_SCRIPT);
			hc.setInheritsFromRef(null);
			try {
				hc.setupInheritanceChain();
			} catch (Exception ignore) {
				SystemFunctions.doNothing();
			}
		} else if (object instanceof AttributeMap){
			object.setInheritsFromRef(BaseConfiguration.INHERIT_PARENT);
		} else if (object instanceof ScriptConfig) {
			object.removeParameter(InternalSchema.SCRIPT);
		}
	}

}
