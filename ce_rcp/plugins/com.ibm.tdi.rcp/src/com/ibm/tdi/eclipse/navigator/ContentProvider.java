/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.navigator;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Note: This is not yet implemented but is planned for
 * 
 * This class is used by the TDI Navigator (CommonNavigator) to provide child nodes at
 * specific trigger points in the navigator's tree.
 * <br/>
 * We provide child nodes for "*.tdiproperties" files to show the status of servers
 * defined in the TDI Servers project. When the input has changed a background job is
 * started to retrieve the status for the server. When all information has been collected
 * the class notifies the viewer of the change. Until then, we provide a "Updating..." child
 * item to give the user an indication that the branch is being refreshed.
 *
 */
public class ContentProvider implements ITreeContentProvider {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	
	public Object[] getChildren(Object parentElement) {
		return new Object[0];
	}

	public Object getParent(Object element) {
		return null;
	}

	public boolean hasChildren(Object element) {
		return false;
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

}
