/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.providers;

import java.util.List;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;

import com.ibm.di.config.interfaces.TDIProperties;
import com.ibm.di.entry.Entry;

public class EntryArrayContentProvider implements ITableLabelProvider, ITreeContentProvider {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		Entry e = (Entry) element;
		switch(columnIndex) {
		case 0:
			return e.getString(TDIProperties.KEY_ATTRIBUTE);
		case 1:
			return e.getString(TDIProperties.PROTECT_ATTRIBUTE);
		case 2:
			return e.getString(TDIProperties.VALUE_ATTRIBUTE);
		}
		return e.toString();
	}

	public Object[] getChildren(Object parentElement) {
		if(parentElement instanceof List)
			return ((List)parentElement).toArray();
		else
			return null;
	}

	public Object getParent(Object element) {
		return null;
	}

	public boolean hasChildren(Object element) {
		return(element instanceof List);
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	public void addListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub
		
	}

	public boolean isLabelProperty(Object element, String property) {
		// TODO Auto-generated method stub
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub
		
	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub

	}

}
