/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.util;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.TreeColumn;

/**
 * Utility class that restores and saves the column widths in a tree viewer.
 */
public class PersistTreeViewerProperties implements ControlListener {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	private TreeViewer tree;
	private QualifiedName qname;
	private IFile resource;
	
	public PersistTreeViewerProperties(TreeViewer tree, QualifiedName qname, IFile resource) {
		super();
		this.tree = tree;
		this.qname = qname;
		this.resource = resource;
		
		try {
			String str = resource.getPersistentProperty(qname);
			if(str != null) {
				int i = 0;
				for(String s : str.split(",")) {
					tree.getTree().getColumn(i++).setWidth(Integer.valueOf(s));
				}
			} else {
				TreeColumn[] tc = tree.getTree().getColumns();
				int size = tree.getTree().getClientArea().width / tc.length;
				if(size < 1)
					size = 100;
				
				for(TreeColumn t : tc)
					t.setWidth(size);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		for(TreeColumn t : tree.getTree().getColumns())
			t.addControlListener(this);
	}
	
	public void controlMoved(ControlEvent e) {}

	public void controlResized(ControlEvent e) {
		StringBuffer buf = new StringBuffer();
		for(TreeColumn t : tree.getTree().getColumns()) {
			if(t.getWidth() < 1)
				return;
			
			if(buf.length() > 0)
				buf.append(",");
			buf.append(""+t.getWidth());
		}
		try {
			resource.setPersistentProperty(qname, buf.toString());
		} catch (Exception err) {
			err.printStackTrace();
		}
	}

}
