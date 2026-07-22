/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewPart;

import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.PropertyManager;
import com.ibm.di.function.SystemFunctions;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.navigator.CustomNavigator;

public class ToggleDefaultPropertyStore extends BaseAction {
	@SuppressWarnings("unused") //$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public ToggleDefaultPropertyStore() {
	}

	public void run(IAction action) {
		try {
			IFile file = (IFile) getFirstSelection();
			MetamergeConfig mc = Utils.getProjectMC(file.getProject());
			if(mc == null)
				return;
			
			String name = file.getName();
			String store = name.substring(0, name.length() - (file.getFileExtension().length()+1));
			
			updateStoreSetting(mc, store, action.isChecked());
			
			refreshElement();
			
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e, getShell());
		}
	}
	
	protected void refreshElement() {
		try {
			IViewPart view = getWorkbench().getActiveWorkbenchWindow().getActivePage().findView("com.ibm.tdi.rcp.navigator");
			if(view instanceof CustomNavigator) {
				IFile file = (IFile) getFirstSelection();
				((CustomNavigator)view).getCommonViewer().refresh(file.getParent(), true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void updateStoreSetting(MetamergeConfig mc, String store, boolean checked) throws Exception {
		PropertyManager pm = (PropertyManager) mc.lookup(MetamergeConfig.DEFAULT_PROPSTORE_FOLDER);
		if(checked)
			pm.setDefaultPropertyStore(pm.getPropertyStore(store));
		else
			pm.setDefaultPropertyStore(null);
		mc.commitChanges(null);
	}

	protected boolean getStoreState(MetamergeConfig mc, String store) throws Exception {
		PropertyManager pm = (PropertyManager) mc.lookup(MetamergeConfig.DEFAULT_PROPSTORE_FOLDER);
		if(pm.getDefaultPropertyStore() != null)
			return store.equals(pm.getDefaultPropertyStore().getShortName());
		else
			return false;
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);
		if(getFirstSelection() instanceof IFile) {
			try {
				IFile file = (IFile) getFirstSelection();
				MetamergeConfig mc = Utils.getProjectMC(file.getProject());
				if(mc == null)
					return;
				
				String name = file.getName();
				String store = name.substring(0, name.length() - (file.getFileExtension().length()+1));
				action.setChecked(getStoreState(mc, store));
			} catch (Exception e) {
				SystemFunctions.doNothing();
			}
		}
	}
}
