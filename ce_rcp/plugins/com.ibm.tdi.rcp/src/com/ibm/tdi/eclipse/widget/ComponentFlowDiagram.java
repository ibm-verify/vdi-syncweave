/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.widget;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.HookConfig;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.di.config.interfaces.MetamergeConfigChangeListener;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.editors.BaseEditor;

public class ComponentFlowDiagram extends BaseWidget implements PaintListener, MetamergeConfigChangeListener {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public ComponentFlowDiagram(Composite parent, int style, BaseConfiguration editingConfig, BaseEditor editor) {
		super(parent, style, editingConfig, editor);
		setBackground(parent.getBackground());
		if((getEditingConfig() instanceof ConnectorConfig)) {
			addPaintListener(this);
			ConnectorConfig cc = (ConnectorConfig) getEditingConfig();
			cc.getHooks().addListener(this);
		}
	}

	public void paintControl(PaintEvent e) {
		ConnectorConfig cc = (ConnectorConfig) getEditingConfig();
		GC g = e.gc;
		
		int x = 0;
		int y = 0;
		
		for(Object hh : cc.getHooks().getActiveHooks()) {
			HookConfig hook = (HookConfig) hh;
			y = 12;
			g.drawLine(x, y, x+10, y);
			g.drawLine(x+8, y-2, x+10, y);
			g.drawLine(x+8, y+2, x+10, y);
			x += 15;
			
			y = 5;
			String str = Messages.getString("Hook." + hook.getHookName());
			g.drawString(str, x, y);
			x += computeSize(g, str);
			x += 5;
		}
		
	}

	private int computeSize(GC gc, String str) {
		int size = 0;
		for (int i = 0; i < str.length(); i++)
			size += gc.getAdvanceWidth(str.charAt(i));
		return size;
	}
	
	@Override
	public Point computeSize(int hint, int hint2, boolean changed) {
		return new Point(200, 20);
	}

	public void configurationChanged(MetamergeConfigChange arg0) {
		if(arg0.getSource() == ((ConnectorConfig)getEditingConfig()).getHooks())
			redraw();
	}

	@Override
	public void dispose() {
		if((getEditingConfig() instanceof ConnectorConfig)) {
			((ConnectorConfig)getEditingConfig()).getHooks().removeListener(this);
		}
	}

}
