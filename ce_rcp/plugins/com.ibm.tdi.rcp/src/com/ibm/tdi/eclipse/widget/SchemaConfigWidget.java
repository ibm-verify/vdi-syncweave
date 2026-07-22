/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.widget;

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeColumn;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.di.config.interfaces.MetamergeConfigChangeListener;
import com.ibm.di.config.interfaces.SchemaConfig;
import com.ibm.di.config.interfaces.SchemaItemConfig;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.providers.AttributeContentProvider;

public class SchemaConfigWidget extends BaseWidget implements MetamergeConfigChangeListener {
	@SuppressWarnings("unused") //$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	
	private SchemaConfig config;
	private TreeViewer schema;
	private AttributeContentProvider provider;
	private boolean batchChange = false;
	
	public SchemaConfigWidget(SchemaConfig config, Composite parent, int style) {
		super(parent, style, config);
		this.config = config;
		setLayout(new FillLayout());
		
		// -- Schema tree
		createSchemaTree(this);
		
		if(config != null)
			config.addListener(this);
	}
	
	public void configurationChanged(MetamergeConfigChange mcc) {
		int op = mcc.getOperation();
		if (op == MetamergeConfigChange.BEGIN_CHANGES) {
			batchChange = true;
		} else if (op == MetamergeConfigChange.END_CHANGES) {
			batchChange = false;
		}
		
		if (batchChange) {
			return;
		}

		Object source = mcc.getSource();
		if(schema == null || !(source instanceof BaseConfiguration))
			return;
		
		SchemaConfig sc = (SchemaConfig) Utils.getParentConfig(source, SchemaConfig.class);
		if(sc != config)
			return;
		
		if(mcc.getSource() instanceof SchemaItemConfig) {
			SchemaItemConfig sic = (SchemaItemConfig) mcc.getSource();
			switch(mcc.getOperation()) {
			case MetamergeConfigChange.MCC_ADD:
			case MetamergeConfigChange.MCC_DELETE:
			case MetamergeConfigChange.MCC_REMOVE:
				schema.refresh(sic.getParent());
				break;
			default:
				schema.update(sic, null);
			}
		} else {
			schema.refresh();
		}
	}

	public void removeAllItems() {
		if(config == null)
			return;
		
		schema.setInput(null);
		config.notifyChange(config, "", MetamergeConfigChange.BEGIN_CHANGES);
		for(Object str : config.getItemNames())
			config.removeItem(str);
		config.notifyChange(config, "", MetamergeConfigChange.END_CHANGES);
		schema.setInput(config);
	}

	public TreeViewer getSchemaTree() {
		return schema;
	}

	private void createSchemaTree(Composite parent) {
		if((getStyle() & SWT.CHECK) > 0)
			schema = new CheckboxTreeViewer(parent, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		else
			schema = new TreeViewer(parent, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		schema.getTree().setHeaderVisible(true);
		provider = new AttributeContentProvider();
		schema.setContentProvider(provider);
		schema.setLabelProvider(provider);

		DragSourceListener dsl = new DragSourceListener() {
			public void dragStart(DragSourceEvent e) {
				e.data = getSelectedSchemaItems();
			}
			public void dragFinished(DragSourceEvent e) {}
			public void dragSetData(DragSourceEvent e) {
				if (LocalSelectionTransfer.getTransfer().isSupportedType(e.dataType)) {
					LocalSelectionTransfer.getTransfer().setSelection(getSelectedSchemaItems());
					e.data = getSelectedSchemaItems();
				}
			}
		;};
		schema.addDragSupport(DND.DROP_MOVE | DND.DROP_COPY, new Transfer[] { LocalSelectionTransfer.getTransfer() }, dsl);

		// Table columns
		TreeColumn tc = new TreeColumn(schema.getTree(), SWT.LEFT);
		tc.setText(Messages.getString("SchemaConfigWidget.1")); //$NON-NLS-1$
		tc.setWidth(200);

		tc = new TreeColumn(schema.getTree(), SWT.LEFT);
		tc.setText(Messages.getString("SchemaConfigWidget.2")); //$NON-NLS-1$
		tc.setWidth(100);

		tc = new TreeColumn(schema.getTree(), SWT.LEFT);
		tc.setText(Messages.getString("SchemaConfigWidget.3")); //$NON-NLS-1$
		tc.setWidth(100);
		
		tc = new TreeColumn(schema.getTree(), SWT.LEFT);
		tc.setText(Messages.getString("SchemaEditor.3")); //$NON-NLS-1$
		tc.setWidth(100);
		
		tc = new TreeColumn(schema.getTree(), SWT.LEFT);
		tc.setText(Messages.getString("SchemaEditor.4")); //$NON-NLS-1$
		tc.setWidth(200);
		
		// -- simple name sorting
		schema.setSorter(new SchemaSorter());
		
		schema.setInput(config);
	}
	
	@Override
	public void setEditingConfig(BaseConfiguration editingConfig) {
		super.setEditingConfig(editingConfig);
		config = (SchemaConfig) editingConfig;
		if(schema != null)
			schema.setInput(config);
	}

	public ISelection getSelectedSchemaItems() {
		return schema.getSelection();
	}
	
	public void setAllChecked(boolean state) {
		if(!(schema instanceof CheckboxTreeViewer))
			return;
		
		CheckboxTreeViewer ct = (CheckboxTreeViewer) schema;
		ct.setAllChecked(state);
	}
	
	private static class SchemaSorter extends ViewerSorter {

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			if(e1 instanceof BaseConfiguration && e2 instanceof BaseConfiguration) {
				BaseConfiguration o1 = (BaseConfiguration) e1;
				BaseConfiguration o2 = (BaseConfiguration) e2;
				String a1 = o1.getShortName();
				String a2 = o2.getShortName();
				if(a1 != null)
					return a1.compareToIgnoreCase(a2);
			}
			return super.compare(viewer, e1, e2);
		}
		
	}

	public void removeListener() {
		if(config != null)
			config.removeListener(this);
		provider.dispose();
	}
	
	@Override
	public void dispose() {
		removeListener();
		super.dispose();
	}
}
