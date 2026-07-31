/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.widget;

import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.ibm.di.config.base.InternalSchema;
import com.ibm.di.config.interfaces.ALMappingConfig;
import com.ibm.di.config.interfaces.AttributeMapItem;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.di.config.interfaces.MetamergeConfigChangeListener;
import com.ibm.di.config.interfaces.RawConnectorConfig;
import com.ibm.tdi.eclipse.editors.BaseEditor;

/**
 * This widget combines the WorkMap and DiscoverSchema widgets in a sash form.
 */
public class AttributeMapWidget extends BaseWidget {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private WorkMapWidget we;
	private DiscoverSchemaWidget dsw;
	private MetamergeConfigChangeListener listener;
	private SashForm sash;

	private SashForm mainSash;

	private AttributeMapItemEditor amEditor;

	/**
	 * 
	 */
	public AttributeMapWidget(Composite parent, int style, ConnectorConfig editingConfig, BaseEditor editor) {
		this(parent, style, editingConfig, 0, editor);
	}
	
	public AttributeMapWidget(Composite parent, int style, ConnectorConfig editingConfig, int mappingMode, BaseEditor editor) {
		super(parent, style, editingConfig, editor);
		setLayout(new FillLayout());
		mainSash = new SashForm(this, SWT.VERTICAL);

		// -- Main form
		sash = new SashForm(mainSash, SWT.HORIZONTAL);

		// -- Work map
		we = new WorkMapWidget(sash, SWT.NONE, editingConfig, true, false, mappingMode);
		we.setEditor(editor);

		// -- Schema
		if (! (editingConfig instanceof ALMappingConfig)) {
			dsw = new DiscoverSchemaWidget(sash, SWT.NONE, editingConfig, editor, mappingMode);
			sash.setWeights(new int[] { 50, 50 });
		}

		// -- Quick editor
		amEditor = new AttributeMapItemEditor(mainSash, SWT.NONE);
		amEditor.quickEdit(null);

		we.getTree().addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				if (!sel.isEmpty() && sel.toList().size() == 1) {
					Object bc = sel.getFirstElement();
					if (bc instanceof AttributeMapItem) {
						AttributeMapItem mapItem = (AttributeMapItem) bc;
						if (mainSash != null && mainSash.getWeights()[1] != 0) {
							if (amEditor.getEditingConfig() == mapItem)
								return;
							amEditor.quickEdit(mapItem);
							we.getTree().reveal(sel.getFirstElement());
						}
					}
				}
			}
		});

		we.getTree().addOpenListener(new IOpenListener() {
			public void open(OpenEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				if (sel.getFirstElement() instanceof AttributeMapItem) {
					AttributeMapItem ami = (AttributeMapItem) sel.getFirstElement();
					if(ami.getParameter("%%PLACEHOLDER%%") == null)
						amEditor.quickEdit(ami);
					we.getTree().reveal(sel.getFirstElement());
				}
			}
		});

		// -- Refresh discover schema widget on mode change
		listener = new MetamergeConfigChangeListener() {
			public void configurationChanged(MetamergeConfigChange changeEvent) {
				Object src = changeEvent.getSource(); 
				if (src  == getEditingConfig() && 
						InternalSchema.CONNECTOR_MODE == changeEvent.getKey() &&
						dsw != null) {
					dsw.setEditingConfig(getEditingConfig());
				} else if (src instanceof RawConnectorConfig && "setInheritsFrom".equals(changeEvent.getUserObject())) {
					if(dsw != null)
						dsw.setEditingConfig(getEditingConfig());
					if(we != null)
						we.setEditingConfig(getEditingConfig());
				}
			}
		};
		editingConfig.addListener(listener);
	}

	/**
	 * Returns the WorkMapWidget
	 * 
	 */
	public WorkMapWidget getWe() {
		return we;
	}

	@Override
	public void dispose() {
		if (we != null) {
			we.dispose();
			we = null;
		}
		if (dsw != null) {
			dsw.dispose();
			dsw = null;
		}
		if (listener != null) {
			getEditingConfig().removeListener(listener);
			listener = null;
		}
		super.dispose();
	}
}
