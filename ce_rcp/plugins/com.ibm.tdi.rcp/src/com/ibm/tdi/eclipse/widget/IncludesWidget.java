/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.widget;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.NameAlreadyBoundException;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.forms.widgets.Form;

import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeFolder;
import com.ibm.di.config.interfaces.NamespaceConfig;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.util.TDIToolBar;
import com.ibm.tdi.eclipse.wizards.NewIncludeWizard;

public class IncludesWidget extends BaseWidget {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private TableViewer table;

	public IncludesWidget(Composite parent, int style, MetamergeFolder editingConfig) {
		super(parent, style, editingConfig);
		createUI();
	}

	private void createUI() {
		setLayout(new FillLayout());
		Form frm = createForm(this, null);
		TDIToolBar bar = new TDIToolBar(frm);
		bar.setText(Messages.getString("IncludesWidget.1")); //$NON-NLS-1$
		
		bar.add(new Action() {
			public String getText() {
				return Messages.getString("IncludesWidget.2"); //$NON-NLS-1$
			}
			public void run() {
				NewIncludeWizard wiz = new NewIncludeWizard();
				WizardDialog dlg = new WizardDialog(getShell(), wiz);
				if(dlg.open() == Window.OK) {
					try {
						MetamergeConfig mc = getEditingConfig().getMetamergeConfig();
						NamespaceConfig nc = wiz.getNamespaceConfig();
						mc.bind(nc.getName(), nc);
					} catch (NameAlreadyBoundException nabe) {
						EclipseAppender.logerror(Messages.getString("IncludesWidget.alreadyExists"),
								nabe, getShell());
					} catch (Exception e) {
						EclipseAppender.logerror(e.toString(), e, getShell());
					}
					table.refresh(true);
				}
			}
		});
		
		bar.add(new Action() {
			public String getText() {
				return Messages.getString("general.delete.label"); //$NON-NLS-1$
			}
			public void run() {
				IStructuredSelection sel = (IStructuredSelection)table.getSelection();
				MetamergeConfig mc = getEditingConfig().getMetamergeConfig();
				for(Object obj:sel.toArray() ) {
					if (obj instanceof NamespaceConfig)
						try {
							mc.unbind(((NamespaceConfig)obj).getName());
						} catch (Exception e) {
							EclipseAppender.logerror(e.getMessage(), e);
						}
				}
				table.refresh(true);
			}
		});
		
		
		bar.addHelpButton("INCLUDE"); //$NON-NLS-1$
		
		frm.getBody().setLayout(new FillLayout());
		Composite c = new Composite(frm.getBody(), SWT.NONE);
		TableColumnLayout layout = new TableColumnLayout();
		c.setLayout(layout);
		table = new TableViewer(c, SWT.FULL_SELECTION);
		TableColumn col = new TableColumn(table.getTable(), SWT.LEFT);
		col.setText(Messages.getString("IncludesWidget.3")); //$NON-NLS-1$
		layout.setColumnData(col, new ColumnWeightData(30));
		
		col = new TableColumn(table.getTable(), SWT.LEFT);
		col.setText(Messages.getString("IncludesWidget.4")); //$NON-NLS-1$
		layout.setColumnData(col, new ColumnWeightData(70));
		
		col = new TableColumn(table.getTable(), SWT.LEFT);
		col.setText(Messages.getString("ImportConfigWizard.4")); //$NON-NLS-1$
		layout.setColumnData(col, new ColumnWeightData(30));
		
		table.getTable().setHeaderVisible(true);
		
		table.setLabelProvider(new IncludeLabelProvider());
		table.setContentProvider(new IncludeProvider());
		table.setInput(getEditingConfig());
	}

	private static class IncludeLabelProvider extends LabelProvider implements ITableLabelProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			NamespaceConfig nc = (NamespaceConfig) element;
			if(columnIndex == 0)
				return nc.getShortName();
			else if (columnIndex == 1)
				return nc.getURL();
			//Password
			if (nc.getParameter(Context.SECURITY_CREDENTIALS) != null)
				return "*****";
			return "";
		}
		
	}
	
	private static class IncludeProvider implements IStructuredContentProvider {
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
		public Object[] getElements(Object inputElement) {
			List<Object> list = new ArrayList<Object>();
			try {
				Enumeration<Binding> l = ((MetamergeFolder)inputElement).list();
				while (l.hasMoreElements()) {
					list.add(l.nextElement().getObject());
				}
			} catch (Exception e) {
				EclipseAppender.logerror(e.getMessage(), e);
			}
			return list.toArray();
		}
	}
}
