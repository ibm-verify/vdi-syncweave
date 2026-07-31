/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.widget;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class TitledListWidget extends Canvas implements PaintListener {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private String title;
	private List<String> items;
	private Display display;
	private int drawFlags = SWT.DRAW_TAB | SWT.DRAW_DELIMITER | SWT.DRAW_TRANSPARENT;
	private Color normalGradientForeground;
	private int index;
	private int titleHeight;
	private int itemHeight;

	public TitledListWidget(Composite parent, int style, String title, List<String> items, int index) {
		super(parent, style);
		this.title = title;
		this.items = items;
		this.index = index;
		this.display = parent.getDisplay();
		this.normalGradientForeground = new Color(display, 0x44, 0x88, 0x33);
		addPaintListener(this);
	}
	
	@Override
	public Point computeSize(int wHint, int hHint, boolean changed) {
		//return super.computeSize(wHint, hHint, changed);
		if(items == null)
			return new Point(0, 20);

		GC gc = new GC(this);
		
		Point pt = new Point(0, 0);
		Point p = null;
		for(String str : items) {
			p = gc.textExtent(str, drawFlags);
			if(p.x > pt.x)
				pt.x = p.x + 10;
			pt.y += p.y;
		}
		itemHeight = (p == null ? 2 : p.y + 2);
		
		
		p = gc.textExtent(getTitle(), drawFlags);
		if(p.x > pt.x)
			pt.x = p.x + 10;
		pt.y += p.y + 5;
		titleHeight = p.y + 5;
		
		
		// -- space for item separator
		pt.y += items.size() * 2;
		
		gc.dispose();
		
		return pt;
	}

	public void paintControl(PaintEvent e) {
		GC gc = e.gc;
		Point pt;
		
		int style = gc.getLineStyle();
		int size = gc.getLineWidth();
		gc.setLineStyle(SWT.LINE_DOT);
		gc.setLineWidth(1);
		
		int x = 0;
		int y = 0;
		int w = getClientArea().width;
		
		Color c = gc.getForeground();
		Color b = gc.getBackground();
		gc.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
		gc.fillRectangle(getClientArea());
		
		// -- gradient rectangle
		gc.setForeground(normalGradientForeground);
		pt = gc.textExtent(title, drawFlags);
		gc.fillGradientRectangle(x, y, w, pt.y+10, true);
		
		// -- title inside gradient rect
		gc.setForeground(c);
		gc.setBackground(b);
		gc.drawText(getTitle(), x + 3, y, true);
		y += pt.y + 5;
		
		// -- list items
		for(String name : items) {
			gc.drawLine(x, y, x + w, y);
			y += 2;
			pt = gc.textExtent(name, drawFlags);
			if(name.startsWith("|")) {
				gc.drawText(name.substring(1), x + w - pt.x - 3, y, true);
			} else {
				gc.drawText(name, x + 3, y, true);
			}
			y += pt.y;
		}

		// -- reset gc
		gc.setLineStyle(style);
		gc.setLineWidth(size);
		
		// -- frame the thing
		Rectangle r = getClientArea();
		r.width -= 1;
		r.height -= 1;
		gc.drawRectangle(r);
	}

	public String getTitle() {
		return title + " (" + (index+1) + ")";
	}

	public Point locationFor(String attribute) {
		int index = items.indexOf(attribute);
		if(index == -1)
			return new Point(0,0);

		return new Point(0, titleHeight + (index * itemHeight));
	}

	public int getItemHeight() {
		return itemHeight;
	}

}
