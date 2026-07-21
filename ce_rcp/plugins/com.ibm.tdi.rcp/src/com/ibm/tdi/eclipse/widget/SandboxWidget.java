/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.widget;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.BranchingConfig;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.LoopConfig;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.providers.CheckboxLabelProvider;

public class SandboxWidget extends BaseWidget {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	
	private AssemblyLineConfig alc;
	private List<ConnectorConfig> connectors;
	private TableViewer viewer;

	public SandboxWidget(Composite parent, int style, AssemblyLineConfig config) {
		super(parent, style, config);
		this.alc = config;
		setLayout(new FillLayout());
		createUI();
	}

	private void createUI() {
		Form frm = createForm(this, null);
		frm.setText(Messages.getString("assemblyline.tabs.sandbox.label"));
		frm.setLayout(new FillLayout());
		
		FormToolkit tk = getFormToolKit();
	
		Composite c = frm.getBody();
		c.setLayout(new GridLayout(2, false));
		
		// -- Database name
		tk.createLabel(c, Messages.getString("assemblyline.sandbox.identifier"));  //$NON-NLS-1$
		Text text = tk.createText(c, safeValue(alc.getSandboxConfig().getIdentifier()), SWT.BORDER); 
		text.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				alc.getSandboxConfig().setIdentifier(((Text)e.widget).getText());
			}
		});
		
		Composite tableComp = new Composite(c, SWT.NONE);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.horizontalSpan = 2;
		tableComp.setLayoutData(gd);
		TableColumnLayout layout = new TableColumnLayout();
		tableComp.setLayout( layout );
		
		// -- Table of connectors
		Table table = new Table(tableComp, SWT.BORDER | SWT.FULL_SELECTION );
		
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		viewer = new TableViewer(table);

		final TableViewerFocusCellManager mgr = new TableViewerFocusCellManager(viewer,new FocusCellOwnerDrawHighlighter(viewer));
		ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(viewer) {
			@Override
			protected boolean isEditorActivationEvent(
					ColumnViewerEditorActivationEvent event) {
				return event.eventType == ColumnViewerEditorActivationEvent.MOUSE_CLICK_SELECTION
						|| event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION
						|| (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && event.character == ' ' )
						|| event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL;
			}
		};

		TableViewerEditor.create(viewer, mgr, actSupport, ColumnViewerEditor.TABBING_HORIZONTAL
				| ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR| ColumnViewerEditor.KEYBOARD_ACTIVATION);

		final CheckboxCellEditor checkboxCellEditor = new CheckboxCellEditor(table);
		
		TableViewerColumn tv = new TableViewerColumn(viewer, SWT.LEFT|SWT.CHECK);
		tv.getColumn().setText(Messages.getString("ConnectorSandboxModel.RecordEnabled")); //$NON-NLS-1$
		tv.setLabelProvider(new CheckboxLabelProvider(viewer) {		
			@Override
			protected boolean isChecked(Object element) {
				return ((ConnectorConfig) element).getSandboxConfig().getRecordEnabled();
			}
		});
		tv.setEditingSupport(new EditingSupport(viewer) {
			@Override
			protected boolean canEdit(Object element) {
				return true;
			}
			@Override
			protected CellEditor getCellEditor(Object element) {
				return checkboxCellEditor;
			}
			@Override
			protected Object getValue(Object element) {
				return ((ConnectorConfig) element).getSandboxConfig().getRecordEnabled();
			}
			@Override
			protected void setValue(Object element, Object value) {
				((ConnectorConfig) element).getSandboxConfig().setRecordEnabled((Boolean)value);
				viewer.update(element, null);
			}
		});
		layout.setColumnData( tv.getColumn(), new ColumnWeightData( 50 ) );
		
		tv = new TableViewerColumn(viewer, SWT.LEFT|SWT.CHECK);
		tv.getColumn().setText(Messages.getString("ConnectorSandboxModel.PlaybackEnabled")); //$NON-NLS-1$
		tv.setLabelProvider(new CheckboxLabelProvider(viewer) {		
			@Override
			protected boolean isChecked(Object element) {
				return ((ConnectorConfig) element).getSandboxConfig().getPlaybackEnabled();
			}
		});
		tv.setEditingSupport(new EditingSupport(viewer) {
			@Override
			protected boolean canEdit(Object element) {
				return true;
			}
			@Override
			protected CellEditor getCellEditor(Object element) {
				return checkboxCellEditor;
			}
			@Override
			protected Object getValue(Object element) {
				return ((ConnectorConfig) element).getSandboxConfig().getPlaybackEnabled();
			}
			@Override
			protected void setValue(Object element, Object value) {
				((ConnectorConfig) element).getSandboxConfig().setPlaybackEnabled((Boolean)value);
				viewer.update(element, null);
			}
		});
		layout.setColumnData( tv.getColumn(), new ColumnWeightData( 50 ) );

		viewer.setContentProvider(new SandboxCellProvider());
		viewer.setUseHashlookup(true);
		viewer.setInput(alc);		

		tk.createButton(c, Messages.getString("DiscoverSchemaWidget.6"), SWT.PUSH)
		.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				selectAll(true);
			}
			public void widgetSelected(SelectionEvent e) {
				selectAll(true);
			}		
		});
		
		tk.createButton(c, Messages.getString("DiscoverSchemaWidget.8"), SWT.PUSH)
		.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				selectAll(false);
			}
			public void widgetSelected(SelectionEvent e) {
				selectAll(false);
			}		
		});
	}	
	
	private String safeValue(String str) {
		return (str == null ? "" : str);
	}

	private void selectAll(boolean check) {
		for (ConnectorConfig cc:connectors) {
			cc.getSandboxConfig().setRecordEnabled(check);
			cc.getSandboxConfig().setPlaybackEnabled(check);
		}
		viewer.refresh();
	}
	
	private class SandboxCellProvider implements IStructuredContentProvider {

		public Object[] getElements(Object inputElement) {
			List<BaseConfiguration> list = alc.getEntryFeedComponents().getConfigurations(null);
			alc.getDataFlowComponents().getConfigurations(list);
			connectors = new ArrayList<ConnectorConfig>();
			for (BaseConfiguration bc:list) {
				if (bc instanceof LoopConfig && ((LoopConfig) bc).getLoopType() == LoopConfig.LOOP_CONNECTOR_FC) {
					// Get loop connector
					try {
						bc = ((LoopConfig) bc).getLoopConnector();
					} catch (Exception e) {
						bc = null;
					}
				}     

				if (bc instanceof ConnectorConfig) {
					connectors.add((ConnectorConfig)bc);
				}
			}
			return connectors.toArray();
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
}
