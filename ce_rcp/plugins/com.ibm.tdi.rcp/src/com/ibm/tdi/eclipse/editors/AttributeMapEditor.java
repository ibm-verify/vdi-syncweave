/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.editors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.actions.ActionFactory;

import com.ibm.di.config.base.BaseConfigurationImpl;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.tdi.eclipse.actions.CopyConfigAction;
import com.ibm.tdi.eclipse.actions.CutConfigAction;
import com.ibm.tdi.eclipse.widget.AttributeMapWidget;
import com.ibm.tdi.eclipse.widget.WorkMapWidget;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.ui.forms.widgets.Form;
import com.ibm.tdi.eclipse.util.TDIToolBar;
import com.ibm.tdi.eclipse.widget.BaseWidget;

public class AttributeMapEditor extends BaseEditor {
	@SuppressWarnings("unused") //$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public static final String ID = "com.ibm.tdi.eclipse.editors.AttributeMapEditor"; //$NON-NLS-1$
	private SashForm sash;
	private TDIToolBar bar;

	public AttributeMapEditor() {
		super();
		//
		// -- global action handlers
		//
		registerAction(ActionFactory.CUT.getId(), new CutConfigAction("Cut", null)); //$NON-NLS-1$
		registerAction(ActionFactory.COPY.getId(), new CopyConfigAction("Copy")); //$NON-NLS-1$
		registerAction(ActionFactory.DELETE.getId(), new CutConfigAction("Delete", null)); //$NON-NLS-1$
	}

	@Override
	public void createPartControl(Composite parent) {
		if(getTDIConfiguration() == null) {
			super.createPartControl(parent);
			return;
		}
		//
		// -- selection provider
		//
		getEditorSite().setSelectionProvider(getSelectionProvider());
		
		sash = new SashForm(parent, SWT.VERTICAL);

		BaseWidget widget = new BaseWidget(sash, SWT.NONE, getTDIConfiguration(), this);
		widget.setLayout(new FillLayout());

		Form f = widget.createForm(widget, null);
		bar = new TDIToolBar(f);
		bar.setText(getTDIConfiguration().getShortName());
		bar.setImage(getTDIConfiguration());

		f.getBody().setLayout(new FillLayout());
		AttributeMapWidget map = new AttributeMapWidget(f.getBody(), SWT.NONE, (ConnectorConfig) getTDIConfiguration(),WorkMapWidget.MAP_MODE_INPUT, this);
		addSelectionProvider(map.getWe().getTree());

		createScriptEditor(sash);
		sash.setWeights(new int[]{100,0});
	}

	@Override
	public void setFocus() {
		super.setFocus();
		if(sash != null)
			sash.setFocus();
	}

	@Override
	public void configurationChanged(MetamergeConfigChange changeEvent) {
		if (changeEvent.getSource() == getTDIConfiguration() 
			&& BaseConfigurationImpl.NAME.equals(changeEvent.getKey()))
			bar.setText(getTDIConfiguration().getShortName());		
		super.configurationChanged(changeEvent);
	}


}
