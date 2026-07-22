/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.providers;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;

import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.HooksConfig;

public class CollapsedHookContentProvider extends AbstractConfigProvider implements ITreeContentProvider, IStructuredContentProvider {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public Object[] getChildren(Object parentElement) {
		return getElements(parentElement);
	}

	public Object getParent(Object element) {
		return null;
	}

	public boolean hasChildren(Object element) {
		return false;
	}

	public Object[] getElements(Object inputElement) {
		HooksConfig hc = null;
		if(inputElement instanceof ConnectorConfig)
			hc = ((ConnectorConfig)inputElement).getHooks();
		else if (inputElement instanceof AssemblyLineConfig)
			hc = ((AssemblyLineConfig)inputElement).getHooks();
		if (hc == null)
			return new Object[]{};
		
		return hc.getActiveHooks().toArray();
	}

}
