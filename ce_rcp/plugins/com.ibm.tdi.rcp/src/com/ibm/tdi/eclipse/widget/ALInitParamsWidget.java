/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.widget;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.di.config.interfaces.MetamergeConfigChangeListener;
import com.ibm.di.config.interfaces.SchemaConfig;
import com.ibm.di.config.interfaces.SchemaItemConfig;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.providers.AttributeContentProvider;
import com.ibm.tdi.eclipse.providers.CheckboxLabelProvider;

public class ALInitParamsWidget extends BaseWidget implements MetamergeConfigChangeListener {
	@SuppressWarnings("unused") 
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	
	private SchemaConfig config;
	private TreeViewer schema;
	private AttributeContentProvider provider;
	private boolean batchChange = false;
	
	private final static String NAME = "name";
	private final static String SYNTAX = "syntax";
	private final static String REQUIRED = "required";
	private final static String DESCRIPTION = "description";

	private final static String[] items = new String[]{
		"String",
		"TextArea", 
		"Boolean", 
		"Password"};
	
	private final static String[] translatedItems = new String[] {
		Messages.getString("FormEditorWidget.syntax.string"),
		Messages.getString("FormEditorWidget.syntax.text"),
		Messages.getString("FormEditorWidget.syntax.boolean"),
		Messages.getString("FormEditorWidget.syntax.password"),		
	};
	
	public ALInitParamsWidget(SchemaConfig config, Composite parent, int style) {
		super(parent, style, config);
		this.config = config;
		setLayout(new FillLayout());
		
		// -- Schema tree
		createSchemaTree(this);
		
		if(config != null)
			config.addListener(this);
	}
	
	private void createSchemaTree(Composite parent) {
		schema = new TreeViewer(parent, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		schema.getTree().setHeaderVisible(true);
		provider = new AttributeContentProvider();
		schema.setContentProvider(provider);
		schema.setLabelProvider(new PAIPLabelProvider(schema));
		schema.setSorter(new SchemaSorter());

		// Table columns
		TreeColumn tc = new TreeColumn(schema.getTree(), SWT.LEFT);
		tc.setText(Messages.getString("SchemaConfigWidget.1")); //$NON-NLS-1$
		tc.setWidth(200);
		
		tc = new TreeColumn(schema.getTree(), SWT.LEFT);
		tc.setText(Messages.getString("SchemaEditor.4")); //$NON-NLS-1$
		tc.setWidth(100);
		
		tc = new TreeColumn(schema.getTree(), SWT.LEFT);
		tc.setText(Messages.getString("SchemaConfigWidget.3")); //$NON-NLS-1$
		tc.setWidth(75);
		
		tc = new TreeColumn(schema.getTree(), SWT.LEFT);
		tc.setText(Messages.getString("SolutionInterfaceUI.Description")); //$NON-NLS-1$
		tc.setWidth(500);
		
		// -- simple name sorting
		setEditable();
		schema.setInput(config);
	}
	
	private void setEditable() {
		String[] props = new String[]{NAME, SYNTAX, REQUIRED, DESCRIPTION};
		
		ComboBoxCellEditor cce = new ComboBoxCellEditor(schema.getTree(), translatedItems);
		final CheckboxCellEditor check = new CheckboxCellEditor(schema.getTree());
		TextCellEditor tce = new TextCellEditor(schema.getTree());
		schema.setCellEditors(new CellEditor[]{tce,cce,check,tce});
		schema.setColumnProperties(props);
		schema.setCellModifier(new ICellModifier() {
			public boolean canModify(Object element, String property) {
				return ! property.equals(NAME);
			}
			public Object getValue(Object element, String property) {
				SchemaItemConfig sic = (SchemaItemConfig) element;
				if (property.equals(NAME))
					return sic.getShortName();
				if (property.equals(SYNTAX))
					return getItemIndex(sic.getExternalSyntax());
				if (property.equals(REQUIRED))
					return sic.isRequired();
				// Only description left
				String s = sic.getUserComment();
				return s == null ? "" : s;
			}	
			public void modify(Object element, String property, Object value) {
				TreeItem ti = (TreeItem) element;
				SchemaItemConfig sic = (SchemaItemConfig) ti.getData();
				if (property.equals(SYNTAX)) {
					Integer index = (Integer)value;
					sic.setExternalSyntax((index < 0 ? "" : items[index]));
				} else if (property.equals(REQUIRED)) {
					if ((Boolean)value)
						sic.setPresenceFlag(SchemaItemConfig.PRESENCE_REQUIRED);
					else
						sic.setPresenceFlag(SchemaItemConfig.PRESENCE_OPTIONAL);
							
				} else {
					sic.setUserComment((String)value);
				}
			}
		});
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
					return a1.compareTo(a2);
			}
			return super.compare(viewer, e1, e2);
		}
		
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
		
		if(Utils.getParentConfig(source, SchemaConfig.class) != config)
			return;
		
		if (isDisposed()) {
			if (config != null)
				config.removeListener(this);
			return;
		}
		
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

	public TreeViewer getViewer() {
		return schema;
	}
	
	@Override
	public void dispose() {
		if(config != null)
			config.removeListener(this);
		provider.dispose();
		super.dispose();
	}
	
	private static int getItemIndex(String str) {
		if (str == null)
			return 0;
		for(int i = 0; i < items.length; i++) {
			if(items[i].equals(str))
				return i;
		}
		return 0;
	}
	
	private static class PAIPLabelProvider implements ITableLabelProvider {

		private CheckboxLabelProvider clp;
		
		public PAIPLabelProvider(TreeViewer schema) {
			clp = new CheckboxLabelProvider(schema) {
			    @Override
				protected boolean isChecked(Object element) {
			    	return ((SchemaItemConfig)element).isRequired();
			    }
			};
		}
		public Image getColumnImage(Object element, int columnIndex) {
			if (columnIndex != 2)
				return null;
			return clp.getImage(element);
		}

		public String getColumnText(Object element, int columnIndex) {
			SchemaItemConfig sic = (SchemaItemConfig) element;
			if (columnIndex == 0)
				return sic.getShortName();
			if (columnIndex == 1)
				return translatedItems[getItemIndex(sic.getExternalSyntax())];
			if (columnIndex == 2)
				return "";
			// Only description left
			String s = sic.getUserComment();
			return s == null ? "" : s;
		}

		public void dispose() {
			clp.dispose();
		}

		public boolean isLabelProperty(Object element, String property) {
			return true;
		}

		public void addListener(ILabelProviderListener listener) {
		}

		public void removeListener(ILabelProviderListener listener) {
		}
		
	}
}
