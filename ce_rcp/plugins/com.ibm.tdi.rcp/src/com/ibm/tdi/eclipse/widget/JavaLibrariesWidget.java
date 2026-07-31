/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.widget;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.swt.IFocusService;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.LibraryConfig;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.actions.DeleteViewerItemAction;
import com.ibm.tdi.eclipse.util.TDIToolBar;
import com.ibm.tdi.eclipse.wizards.NewLibraryWizard;

public class JavaLibrariesWidget extends BaseWidget {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * The table viewer where libraries are listed.
	 */
	private TableViewer tableViewer;

	/**
	 * Constructor.
	 * 
	 * @param parent
	 *            the parent component.
	 * @param style
	 *            style parameter.
	 * @param editingConfig
	 *            a LibraryConfig.
	 */
	public JavaLibrariesWidget(Composite parent, int style, LibraryConfig editingConfig) {
		super(parent, style, editingConfig);

		createUI();
	}
	
	private void createUI() {
		setLayout(new FillLayout());
		Form frm = createForm(this, null);
		TDIToolBar bar = new TDIToolBar(frm);
		bar.setText(Messages.getString("JavaLibrariesWidget.1")); //$NON-NLS-1$
		
		bar.add(new Action() {
			public String getText() {
				return Messages.getString("JavaLibrariesWidget.2"); //$NON-NLS-1$
			}
			public void run() {
				NewLibraryWizard wiz = new NewLibraryWizard() {
					@Override
					public String getWindowTitle() {
						return Messages.getString("JavaLibrariesWidget.1");
					}
				};
				WizardDialog dlg = new WizardDialog(getShell(), wiz);
				if(dlg.open() == Window.OK) {
					getEditingConfig().setStringParameter(wiz.getLibName(), wiz.getLibClass());
					tableViewer.refresh(true);
				}
			}
		});
		
		// this action is invoked when the 'Delete' button/DEL key is pressed
		Action deleteAction = new DeleteViewerItemAction() {

			@Override
			public void run() {
				if (tableViewer == null || tableViewer.getSelection() == null || tableViewer.getSelection().isEmpty()) {
					return;
				}

				if (!MessageDialog.openConfirm(tableViewer.getControl().getShell(), Messages.getString("JavaLibrariesWidget.1"),
						Messages.getString("CutConfigAction.Delete.optimized"))) {
					return;
				}

				IStructuredSelection sel = (IStructuredSelection) tableViewer.getSelection();
				for (Object obj : sel.toArray()) {
					getEditingConfig().removeParameter(obj);
				}
				tableViewer.refresh(true);
			}
		};
		bar.add(deleteAction);
		
		bar.addHelpButton("JAVALIB"); //$NON-NLS-1$
		
		frm.getBody().setLayout(new FillLayout());
		Composite c = new Composite(frm.getBody(), SWT.NONE);
		
		TableColumnLayout layout = new TableColumnLayout();
		c.setLayout(layout);
		tableViewer = new TableViewer(c, SWT.FULL_SELECTION);
		
		TableColumn col = new TableColumn(tableViewer.getTable(), SWT.LEFT);
		col.setText(Messages.getString("JavaLibrariesWidget.3")); //$NON-NLS-1$
		layout.setColumnData(col, new ColumnWeightData(30));
		
		col = new TableColumn(tableViewer.getTable(), SWT.LEFT);
		col.setText(Messages.getString("JavaLibrariesWidget.4")); //$NON-NLS-1$
		layout.setColumnData(col, new ColumnWeightData(70));
		
		tableViewer.setCellEditors(new CellEditor[] {
			new TextCellEditor(tableViewer.getTable()),	
			new TextCellEditor(tableViewer.getTable()),	
		});
		tableViewer.setColumnProperties(new String[]{"name","class"});
		tableViewer.setCellModifier(new ICellModifier() {

			public boolean canModify(Object element, String property) {
				return true;
			}

			public Object getValue(Object element, String property) {
				if(property.equals("class"))
					return getEditingConfig().getStringParameter(element);
				else
					return element;
			}

			public void modify(Object element, String property, Object value) {
				TableItem ti = (TableItem) element;
				String current = ti.getText();
				if(property.equals("class")) {
					getEditingConfig().setParameter(current, value);
				} else if(current != null && current.equals(value)) {
					return;
				} else {
					getEditingConfig().setStringParameter(value, getEditingConfig().getStringParameter(current));
					getEditingConfig().removeParameter(current);
				}
				tableViewer.refresh();
			}
		});
		
		tableViewer.getTable().setHeaderVisible(true);
		
		tableViewer.setLabelProvider(new LibLabelProvider());
		tableViewer.setContentProvider(new LibProvider());
		tableViewer.setInput(getEditingConfig());
		
		// register the table in the focus service and associate Handler with it
		IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench != null) {
			IFocusService focusService = (IFocusService) workbench.getService(IFocusService.class);
			if (focusService != null) {
				focusService.addFocusTracker(tableViewer.getControl(), "com.ibm.tdi.configsettings.table.delete");
				tableViewer.getControl().setData("com.ibm.tdi.action", deleteAction); //$NON-NLS-1$
			}
		}
	}

	private class LibLabelProvider extends LabelProvider implements ITableLabelProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			if(columnIndex == 0)
				return element.toString();
			else
				return getEditingConfig().getStringParameter(element);
		}
		
	}
	
	private static class LibProvider implements IStructuredContentProvider {
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
		public Object[] getElements(Object inputElement) {
			if(inputElement instanceof LibraryConfig)
				return ((LibraryConfig)inputElement).getKeys(BaseConfiguration.ONE_LEVEL).toArray();
			else
				return new Object[]{};
		}
	}
}
