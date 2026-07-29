/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.widget;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;

import com.ibm.di.config.interfaces.BaseConfiguration;

public class ExpandableWidget extends BaseWidget {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	private Control content;
	private Label title;

	public ExpandableWidget(Composite parent, int style, BaseConfiguration editingConfig) {
		super(parent, style, editingConfig);
		setLayout(new ExpandableLayout());
		setBackground(parent.getBackground());
		title = new Label(this, SWT.LEFT|SWT.BORDER);
		title.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				setExpanded(!isExpanded());
				ExpandableWidget.this.layout();
			}
		});
	}
	
	public void setContent(Control content) {
		this.content = content;
	}
	
	public void setTitle(String str) {
		title.setText(str);
	}
	
	private class ExpandableLayout extends Layout {

		@Override
		protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
			Point p1 = title.computeSize(wHint, hHint);
			Point p2 = (content == null ? new Point(0,0) : content.computeSize(wHint, hHint));
			if(isExpanded()) {
				return new Point(Math.max(p1.x, p2.x), Math.max(p1.y, p2.y));
			} else {
				return p1;
			}
		}

		@Override
		protected void layout(Composite composite, boolean flushCache) {
			Rectangle area = composite.getClientArea();
			Point p1 = title.computeSize(area.width, SWT.DEFAULT);
			title.setBounds(0, 0, area.width, p1.y);
			if(content != null && isExpanded()) {
				content.setBounds(0, p1.y+3, area.width, area.height-(p1.y+3));
			} else if (content != null)
				content.setBounds(0,0,0,0);
			
		}
		
	}
}
