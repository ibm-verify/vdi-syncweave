/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.providers;

import java.util.ArrayList;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;

import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;

public class EntryContentProvider implements ITableLabelProvider, ITreeContentProvider {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public EntryContentProvider() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		if(element instanceof Entry) {
			return (columnIndex == 0 ? "Entry" : ((Entry)element).getOperation());
		} else if (element instanceof Attribute) {
			Attribute a = (Attribute) element;
			return (columnIndex == 0 ? a.getName() : a.getOperation());
		} else if (columnIndex == 1){
			return ""+element;
		} else {
			return "";
		}
	
	}

	public void addListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public boolean isLabelProperty(Object element, String property) {
		// TODO Auto-generated method stub
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

	public Object[] getChildren(Object elem) {
		ArrayList<Object> list = new ArrayList<Object>();
		if(elem instanceof Entry) {
			for(String str : ((Entry)elem).getAttributeNames())
				list.add(((Entry)elem).getAttribute(str));
		} else if (elem instanceof Attribute) {
			for(Object val : ((Attribute)elem).getValues())
				list.add(val);
		}
		
		return list.toArray();
	}

	public Object getParent(Object element) {
		// TODO: hierarchical attributes
//		if(element instanceof Attribute)
//			return ((Attribute)element).getParent();
//		else
			return null;
	}

	public boolean hasChildren(Object element) {
		return (element instanceof Entry || element instanceof Attribute);
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub

	}

}
