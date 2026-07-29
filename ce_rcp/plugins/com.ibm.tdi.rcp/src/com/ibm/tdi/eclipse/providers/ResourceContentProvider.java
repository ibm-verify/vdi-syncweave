/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.providers;

import java.util.ArrayList;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class ResourceContentProvider implements ITreeContentProvider, ITableLabelProvider {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub

	}

	public Object[] getElements(Object inputElement) {
		try {
			if(inputElement instanceof IContainer)
				return ((IContainer)inputElement).members();
		} catch (Exception e) {
			return new Object[]{e};
		}
		return new Object[]{};
	}

	public Image getColumnImage(Object element, int columnIndex) {
		if(element instanceof IContainer)
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
		else if(element instanceof IFile)
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
		else
			return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		if(element instanceof IResource)
			return ((IResource)element).getName();
		else
			return element.getClass().getName();
	}

	private ArrayList<ILabelProviderListener> listeners = new ArrayList<ILabelProviderListener>();
	public void addListener(ILabelProviderListener listener) {
		if(!listeners.contains(listener))
			listeners.add(listener);
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
		listeners.remove(listener);
	}

	public Object[] getChildren(Object parentElement) {
		return getElements(parentElement);
	}

	public Object getParent(Object element) {
		if(element instanceof IResource)
			return ((IResource)element).getParent();
		else
			return null;
	}

	public boolean hasChildren(Object element) {
		return (element instanceof IContainer);
	}

}
