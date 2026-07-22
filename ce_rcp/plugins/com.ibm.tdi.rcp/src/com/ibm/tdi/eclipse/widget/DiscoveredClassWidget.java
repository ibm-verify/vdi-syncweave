/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.SchemaConfig;
import com.ibm.di.config.interfaces.SchemaItemConfig;

public class DiscoveredClassWidget extends BaseWidget {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	
	private TreeViewer tree;

	public DiscoveredClassWidget(Composite parent, int style, SchemaConfig config) {
		super(parent, style, config);
		setLayout(new FillLayout());
		tree = new TreeViewer(this, SWT.FILL);
		tree.setContentProvider(new ContentProvider());
		tree.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				if(e1 instanceof BaseConfiguration && e2 instanceof BaseConfiguration)
					return ((BaseConfiguration)e1).getShortName().compareTo(((BaseConfiguration)e2).getShortName());
				else
					return super.compare(viewer, e1, e2);
			}
		});
	}
	
	@Override
	public void setEditingConfig(BaseConfiguration editingConfig) {
		super.setEditingConfig(editingConfig);
		tree.setInput(editingConfig);
	}

	private class ContentProvider implements IStructuredContentProvider {

		public Object[] getElements(Object inputElement) {
			SchemaConfig sc = (SchemaConfig) getEditingConfig();
			ArrayList<SchemaItemConfig> list = new ArrayList<SchemaItemConfig>();
			for(Object obj : sc.getItemNames()) {
				list.add(sc.getItem(obj));
			}
			
			Collections.sort(list, new Comparator<SchemaItemConfig>() {
				public int compare(SchemaItemConfig o1, SchemaItemConfig o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
			
			return list.toArray();
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	
}
