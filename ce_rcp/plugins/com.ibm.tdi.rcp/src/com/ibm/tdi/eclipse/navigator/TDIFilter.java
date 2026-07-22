/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.navigator;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import com.ibm.tdi.eclipse.Utils;

/**
 * This class is used by the TDI common navigator to filter out the servers project. 
 * It can be turned off manually by customizing the navigator view (e.g. through the navigator's menu).
 */
public class TDIFilter extends ViewerFilter {
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private IProject serversProjects;

	public TDIFilter() {
		try {
			serversProjects = Utils.getTDIServersProject(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if(serversProjects != null)
			return !serversProjects.equals(element);
		else
			return true;
	}

}
