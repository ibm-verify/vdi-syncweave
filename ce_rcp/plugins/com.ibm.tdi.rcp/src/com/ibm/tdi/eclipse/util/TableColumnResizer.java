/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.util;

import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

public class TableColumnResizer implements ControlListener {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private Table table;
	private Tree tree;


	public TableColumnResizer(Table table) {
		super();
		this.table = table;
		table.addControlListener(this);
	}

	public TableColumnResizer(Tree tree) {
		super();
		this.tree = tree;
		tree.addControlListener(this);
	}
	
	public void controlResized(ControlEvent e) {
//		Rectangle area = table.getClientArea();
//		Point preferredSize = table.computeSize(SWT.DEFAULT, SWT.DEFAULT);
//		int width = area.width - 2 * table.getBorderWidth();
//		if (preferredSize.y > area.height + table.getHeaderHeight()) {
//			// Subtract the scrollbar width from the total column width
//			// if a vertical scrollbar will be required
//			Point vBarSize = table.getVerticalBar().getSize();
//			width -= vBarSize.x;
//		}
		Rectangle area;
		int width;
		if(table != null) {
			area = table.getClientArea();
			width = area.width;
			
			TableColumn[] tc = table.getColumns();
			for (TableColumn t : tc)
				t.setWidth(width / tc.length);
			
			int total = 0;
			for (TableColumn t : tc) {
				total += t.getWidth();
			}
			
			if(total < width) {
				tc[0].setWidth( tc[0].getWidth() + (width-total));
			}
		}
		
		if(tree != null) {
			area = tree.getClientArea();
			width = area.width;
			TreeColumn[] tc = tree.getColumns();
			for (TreeColumn t : tc)
				t.setWidth(width / tc.length);
			
			int total = 0;
			for (TreeColumn t : tc) {
				total += t.getWidth();
			}
			
			if(total < width) {
				tc[0].setWidth( tc[0].getWidth() + (width-total));
			}
		}
		
		
		// if (oldSize.x > area.width) {
		// // table is getting smaller so make the columns
		// // smaller first and then resize the table to
		// // match the client area width
		// column1.setWidth(width/3);
		// column2.setWidth(width - column1.getWidth());
		// table.setSize(area.width, area.height);
		// } else {
		// // table is getting bigger so make the table
		// // bigger first and then make the columns wider
		// // to match the client area width
		// table.setSize(area.width, area.height);
		// column1.setWidth(width/3);
		// column2.setWidth(width - column1.getWidth());
		// }
	}

	public void controlMoved(ControlEvent e) {
		// TODO Auto-generated method stub

	}

}
