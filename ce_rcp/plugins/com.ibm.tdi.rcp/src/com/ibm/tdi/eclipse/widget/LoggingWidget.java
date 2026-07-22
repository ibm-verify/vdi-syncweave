/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.widget;

import java.util.Hashtable;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.swt.IFocusService;

import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.LogConfig;
import com.ibm.di.config.interfaces.LogConfigItem;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.actions.DeleteViewerItemAction;
import com.ibm.tdi.eclipse.util.TDIToolBar;
import com.ibm.tdi.eclipse.wizards.NewLoggerWizard;

public class LoggingWidget extends BaseWidget {
	@SuppressWarnings("unused") //$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private LogConfig logConfig;
	private TableViewer tableViewer;
	private SashForm form;
	private Composite container;
	private Hashtable<String, FormWidget2> forms = new Hashtable<String, FormWidget2>();
	
	public LoggingWidget(Composite parent, int style, BaseConfiguration editingConfig) {
		super(parent, style, editingConfig);
		createUI();
	}

	private void createUI() {
		
		Form panel = createForm(this, null);
		TDIToolBar bar = new TDIToolBar(panel);
		
		setLayout(new FillLayout());
		Utils.setGridLayout(panel.getBody(), 1, true);
		
		if(getEditingConfig() instanceof LogConfig)
			logConfig = (LogConfig)getEditingConfig();
		else
			logConfig = ((AssemblyLineConfig)getEditingConfig()).getLogConfig();
		
		if((getStyle() & SWT.TITLE) == 0)
			bar.setText(Messages.getString("assemblyline.tabs.logging.label")); //$NON-NLS-1$
		
		Action addAction = new Action() {
			public String getText() {
				return Messages.getString("general.insert.label"); //$NON-NLS-1$
			}
			public String getToolTipText() {
				return Messages.getString("general.insert.tooltip"); //$NON-NLS-1$
			}
			public void run() {
				NewLoggerWizard wiz = new NewLoggerWizard();
				wiz.init(null, null);
				WizardDialog dlg = new WizardDialog(getShell(), wiz);
				if(dlg.open() == Window.OK) {
					BaseConfiguration cc = wiz.getConfigObject();
					if(cc instanceof LogConfigItem) {
						try {
							cc.setName(wiz.getName());
						} catch (Exception e1) {}
						cc.setEnabled(true);
						logConfig.addItem((LogConfigItem) cc);
						tableViewer.refresh();
					}
				}
			}
		};

		Action deleteAction = new DeleteViewerItemAction() {

			@Override
			public void run() {
				if (tableViewer == null || tableViewer.getSelection() == null || tableViewer.getSelection().isEmpty()) {
					return;
				}

				if (!MessageDialog.openConfirm(tableViewer.getControl().getShell(), Messages
						.getString("miadmin.foldernames.Logging"), Messages.getString("CutConfigAction.Delete.optimized"))) {
					return;
				}

				IStructuredSelection sel = (IStructuredSelection) tableViewer.getSelection();
				for (Object obj : sel.toArray()) {
					int index = logConfig.getItems().indexOf(obj);
					if (index != -1) {
						logConfig.removeItem(index);
						FormWidget2 ff = forms.get("" + obj.hashCode()); //$NON-NLS-1$
						if (ff != null) {
							ff.dispose();
						}
					}
					((StackLayout) container.getLayout()).topControl = null;
					container.layout();
					tableViewer.refresh();
				}
			}
		};
		
		bar.add(addAction);
		bar.add(deleteAction);
		bar.addHelpButton("SERVERLOGGING"); //$NON-NLS-1$


		form = new SashForm(panel.getBody(), SWT.HORIZONTAL|SWT.BORDER);
		form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		tableViewer = new TableViewer(form);
		tableViewer.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				Object[] objs = logConfig.getItems().toArray();
				return objs;
			}
			public void dispose() {}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
		});
		tableViewer.setLabelProvider(new ITableLabelProvider() {
			public String getColumnText(Object element, int columnIndex) {
				String str = ((LogConfigItem)element).getShortName();
				if(str == null)
					str = ((LogConfigItem)element).getInheritsFromRef();
				return str;
			}
			public Image getColumnImage(Object element, int columnIndex) {
				return null;
			}
			public boolean isLabelProperty(Object element, String property) {
				return false;
			}

			public void addListener(ILabelProviderListener listener) {}
			public void dispose() {}
			public void removeListener(ILabelProviderListener listener) {}
		});
		tableViewer.setInput(getEditingConfig());
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				try {
					LogConfigItem lci = (LogConfigItem) ((IStructuredSelection)event.getSelection()).getFirstElement();
					if(lci == null)
						return;
					FormWidget2 fw = forms.get(""+lci.hashCode()); //$NON-NLS-1$
					if(fw == null) {
						fw = new FormWidget2(container, SWT.TITLE, lci, null);
						forms.put(""+lci.hashCode(), fw); //$NON-NLS-1$
					}
					((StackLayout)container.getLayout()).topControl = fw;
					container.layout();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		container = new Composite(form, SWT.NONE);
		container.setLayout(new StackLayout());

		form.setWeights(new int[]{30,70});
		
		if(logConfig.getItems().size() > 0) {
			tableViewer.setSelection(new StructuredSelection(logConfig.getItem(0)));
		} 
		
		// register the tableViewer in the focus service and associate Handler with it
		IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench != null) {
			IFocusService focusService = (IFocusService) workbench.getService(IFocusService.class);
			if (focusService != null) {
				focusService.addFocusTracker(tableViewer.getControl(), "com.ibm.tdi.configsettings.table.delete");
				tableViewer.getControl().setData("com.ibm.tdi.action", deleteAction); //$NON-NLS-1$
			}
		}
	}

}
