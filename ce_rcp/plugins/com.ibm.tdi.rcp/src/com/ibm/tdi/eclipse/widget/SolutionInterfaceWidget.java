/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.widget;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.ibm.di.config.base.ExposedPropertyImpl;
import com.ibm.di.config.eclipse.MetamergeConfigCE;
import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.ExposedProperty;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.SolutionInterface;
import com.ibm.tdi.eclipse.ConfigUtils;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.providers.TDIPropertiesContentProvider;
import com.ibm.tdi.eclipse.util.TDIToolBar;

public class SolutionInterfaceWidget extends BaseWidget {
	@SuppressWarnings("unused") 
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private SolutionInterface solConfig;
	private TreeViewer propTable;
	private TableViewer exposedProps;
	private Table exposedALs;
	private CTabFolder tabs;
	private Vector<String> allALs = null;
	private boolean givePopupMessage;

	public SolutionInterfaceWidget(Composite parent, SolutionInterface editingConfig) {
		super(parent, SWT.NONE, editingConfig);
		setLayout(new FillLayout());
		solConfig = editingConfig;
		createUI();
	}

	private void createUI() {

		Form panel = createForm(this, null);		
//		panel.setText(Messages.getString("miadmin.foldernames.SolutionInterface")); //$NON-NLS-1$
		panel.setLayout(new FillLayout());
		
		TDIToolBar toolbar = new TDIToolBar(panel, SWT.TITLE);
		toolbar.setText(Messages.getString("miadmin.foldernames.SolutionInterface"));
		toolbar.addHelpButton(solConfig);
		
		Composite c = panel.getBody();
		c.setLayout(new GridLayout(2, false));

		FormToolkit tk = getFormToolKit();
		tk.createLabel(c, Messages.getString("SolutionInterfaceUI.InstanceID"));  //$NON-NLS-1$
		final Text solID = tk.createText(c, defaultInstanceID(), SWT.BORDER); 
		solID.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		solID.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				solConfig.setInstanceID(solID.getText());
			}
		});

		tk.createLabel(c, Messages.getString("SolutionInterfaceUI.Enabled"));  //$NON-NLS-1$
		final Button enabled = tk.createButton(c, "", SWT.CHECK);
		enabled.setSelection(solConfig.getEnabled());
		enabled.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				solConfig.setEnabled(enabled.getSelection());
				enableTabs();
			}
		});

		// -- User configured version & date (this will be used later ... don't remove)
//		// Label for version field
//		tk.createLabel(c, Messages.getString("ProjectPage.solution.version"));
//		Text text = tk.createText(c, getMC().getDocument().getDocumentElement().getAttribute("user.version"));
//		text.addModifyListener(new ModifyListener() {
//			public void modifyText(ModifyEvent e) {
//				updateConfigProp("user.version", ((Text)e.widget).getText());
//			}
//		});
//		
//
//		// Label for version date field
//		tk.createLabel(c, Messages.getString("ProjectPage.solution.date"));
//		text = tk.createText(c, getMC().getDocument().getDocumentElement().getAttribute("user.date"));
//		text.addModifyListener(new ModifyListener() {
//			public void modifyText(ModifyEvent e) {
//				updateConfigProp("user.date", ((Text)e.widget).getText());
//			}
//		});
		
		tabs = new CTabFolder(c, SWT.TOP|SWT.BORDER);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.horizontalSpan = 2;
		tabs.setLayoutData(gd);

		CTabItem item = new CTabItem(tabs, SWT.LEFT);
		item.setText(Messages.getString("miadmin.foldernames.AssemblyLines")); //$NON-NLS-1$
		item.setControl(alPanel());

		item = new CTabItem(tabs, SWT.LEFT);
		item.setText(Messages.getString("miadmin.foldernames.Properties")); //$NON-NLS-1$
		item.setControl(propsPanel());

		item = new CTabItem(tabs, SWT.LEFT);
		item.setText(Messages.getString("SolutionInterfaceUI.Description")); //$NON-NLS-1$
		item.setControl(descriptionPanel());
		
		tabs.setSelection(0);

		enableTabs();
	}

//	protected void updateConfigProp(String string, String text) {
//		getMC().getDocument().getDocumentElement().setAttribute(string, text);
//		getMC().setModified(true);
//	}

//	private MetamergeConfigXML getMC() {
//		try {
//			return (MetamergeConfigXML) Utils.getProjectMC(((MetamergeConfigCE)getEditingConfig().getMetamergeConfig()).getProject());
//		} catch (Exception e) {
//			EclipseAppender.logerror(e.toString(), e, getShell());
//			return null;
//		}
//	}
	
	private Control descriptionPanel() {
		Form form = getFormToolKit().createForm(tabs);
		FormToolkit tk = getFormToolKit();

		tk.decorateFormHeading(form);

		form.setText(Messages.getString("SolutionInterfaceUI.Description")); //$NON-NLS-1$
		form.getBody().setLayout(new FillLayout());
		final Text comment = tk.createText(form.getBody(), safeValue(solConfig.getUserComment()), SWT.BORDER | SWT.MULTI | SWT.WRAP);
		comment.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				solConfig.setUserComment(comment.getText());
			}
		});
		return form;
	}

	/**
	 * Panel for modifying assemblyline exposed state.
	 * 
	 * @return UI with assemblylines and their exposed state.
	 */
	private Control alPanel() {
		Form form = getFormToolKit().createForm(tabs);
		FormToolkit tk = getFormToolKit();

		tk.decorateFormHeading(form);

		form.setText(Messages.getString("SolutionInterfaceWidget.AL2Expose")); //$NON-NLS-1$

		Composite c = form.getBody();
		c.setLayout(new GridLayout(2, false));

		tk.createLabel(c, Messages.getString("SolutionInterfaceUI.HealthAL"));  //$NON-NLS-1$

		final Combo healthAL = new Combo(c, SWT.DROP_DOWN);
		for (String al : getALs())
			healthAL.add(al);
		healthAL.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		healthAL.setText(safeValue(solConfig.getHealthAssemblyLine()));
		healthAL.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (!healthAL.getText().equals(solConfig.getHealthAssemblyLine()))
					solConfig.setHealthAssemblyLine(healthAL.getText());
			}
		});

		tk.createLabel(c, Messages.getString("SolutionInterfaceUI.PollInterval"));  //$NON-NLS-1$
		final Text pollInt = tk.createText(c, "" + solConfig.getHealthPollInterval(), SWT.BORDER); 
		pollInt.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		pollInt.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				try {
					solConfig.setHealthPollInterval(Integer.valueOf(pollInt.getText()));
				} catch (NumberFormatException nfe) {
					EclipseAppender.logerror(nfe.getMessage(), nfe);
				}
			}
		});

		Composite tableComp = tk.createComposite(c);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.horizontalSpan = 2;
		tableComp.setLayoutData(gd);
		TableColumnLayout layout = new TableColumnLayout();
		tableComp.setLayout(layout);
		exposedALs = new Table(tableComp, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);

		exposedALs.setHeaderVisible(true);

		TableColumn tc = new TableColumn(exposedALs, SWT.NULL);
		tc.setText(Messages.getString("SolutionInterfaceUI.Exposed")); //$NON-NLS-1$

		for (String name: getALs()) {
			TableItem item = new TableItem(exposedALs, SWT.NULL);
			item.setText(name);
			item.setText(0, name);
			item.setChecked(solConfig.getExposedAssemblyLines().getConfig(name) != null);
		}

		tc.pack();

		exposedALs.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (event.detail == SWT.CHECK && event.item instanceof TableItem) {
					TableItem item = (TableItem) event.item;
					boolean expose = item.getChecked();
					if (expose) {
						try{
							solConfig.addExposedAssemblyLine(item.getText());
						} catch (Exception e) {
							EclipseAppender.logerror(e.getMessage(), e);
						}
					} else {
						solConfig.getExposedAssemblyLines().removeConfig(item.getText(), false);
					}
				}
			}
		});

		layout.setColumnData( tc, new ColumnWeightData( 50 ) );

		tk.createButton(c, Messages.getString("SolutionInterfaceWidget.ExposeAll"), SWT.PUSH)
		.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				selectAll(true);
			}
			public void widgetSelected(SelectionEvent e) {
				selectAll(true);
			}		
		});

		tk.createButton(c, Messages.getString("SolutionInterfaceWidget.HideAll"), SWT.PUSH)
		.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				selectAll(false);
			}
			public void widgetSelected(SelectionEvent e) {
				selectAll(false);
			}		
		});

		return form;
	}

	private void enableTabs() {
//		tabs.setVisible( solConfig.getEnabled() );
	}

	private String safeValue(String str) {
		return (str == null ? "" : str);
	}

	private String defaultInstanceID() {
		String s = solConfig.getInstanceID();
		if (s != null && s.length() > 0)
			return s;
		IProject p = Utils.getProjectFor(solConfig);
		if ( p != null) {
			s = p.getName();
			solConfig.setInstanceID(s);
			return s;
		}
		return "";
	}
	private String storeName(IFile f) {
		String store = f.getName();
		if (store.endsWith(TDIConfigurationFile.XT_PROPSTORE))
			store = store.substring(0, store.length() - TDIConfigurationFile.XT_PROPSTORE.length() - 1);
		return store;
	}

	private List<String> getALs() {
		if (allALs != null)
			return allALs;

		allALs = new Vector<String>();
		try {
			MetamergeConfigCE mc = (MetamergeConfigCE)solConfig.getMetamergeConfig();
			MetamergeConfig local = Utils.getProjectMC(mc.getProject());
			ConfigUtils.addFolderNames(local, local, MetamergeConfig.ASSEMBLYLINE_FOLDER, allALs);
		} catch (Exception e) {
			EclipseAppender.logerror(e.getMessage(), e);
		}
		return allALs;
	}

	private void selectAll(boolean expose) {
		int counter = 0;
		for (String element:allALs) {
			if(expose) {
				try {
					solConfig.addExposedAssemblyLine(element);
				} catch (Exception e) {
					EclipseAppender.logerror(e.getMessage(), e);
				}
			} else {
				solConfig.getExposedAssemblyLines().removeConfig(element, false);
			}
			exposedALs.getItem(counter++).setChecked(expose);
		}
	}

	/**
	 * Panel for modifying exposed properties state.
	 * 
	 * @return UI with properties and their exposed state.
	 */
	public Control propsPanel() {
		Form form = getFormToolKit().createForm(tabs);
		FormToolkit tk = getFormToolKit();

		tk.decorateFormHeading(form);

		form.setText(Messages.getString("SolutionInterfaceWidget.PropertiesToExpose")); //$NON-NLS-1$
		form.getBody().setLayout(new FillLayout());
		// -- property store and key selection
		SashForm sash = new SashForm(form.getBody(), SWT.HORIZONTAL);
		addStoreTable(sash);
		addPropertiesTable(sash);
		sash.setWeights(new int[] {30,70});

		return form;
	}

	private void addStoreTable(Composite sash) {
		Composite comp = new Composite(sash, SWT.NONE);
		comp.setLayout(new GridLayout(1, false));
		Button addButton = new Button(comp, SWT.PUSH);
		addButton.setText(Messages.getString("SolutionInterfaceWidget.ExposeProperty"));
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				givePopupMessage = true;
				IStructuredSelection sel = (IStructuredSelection)propTable.getSelection();
				for (Iterator<?> i = sel.iterator(); i.hasNext();)
					exposeProperty(i.next());
				exposedProps.refresh();
			}			
		});

		new Label(comp, SWT.NONE).setText(Messages.getString("SolutionInterfaceWidget.PropertyNames")); //$NON-NLS-1$

		// -- Table of properties

		propTable = new TreeViewer(comp, SWT.BORDER|SWT.MULTI);
		propTable.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof BaseConfiguration)
					return ((BaseConfiguration) element).getShortName();
				if (element instanceof IFile)
					return storeName((IFile)element);
				return super.getText(element);
			}
		}); 
		propTable.setContentProvider(new TDIPropertiesContentProvider());
		propTable.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		propTable.setInput(solConfig);
	}

	// Exposed properties table
	private void addPropertiesTable(Composite sash) {
		Composite c = new Composite(sash, SWT.NONE);
		c.setLayout(new GridLayout(1, false));
		Composite buttons = new Composite(c, SWT.NONE);
		buttons.setLayout(new FillLayout());

		Button newButton = new Button(buttons, SWT.PUSH);
		newButton.setText(Messages.getString("SolutionInterfaceWidget.Add"));
		newButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ExposedProperty p = new ExposedPropertyImpl();
				p.setPropertyName("*");
				solConfig.getExposedProperties().addConfig(p);
				exposedProps.refresh();
				exposedProps.setSelection(new StructuredSelection (p));
			}			
		});

		Button delButton = new Button(buttons, SWT.PUSH);
		delButton.setText(Messages.getString("general.delete.label"));
		delButton.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection sel = (IStructuredSelection)exposedProps.getSelection();
				for (Iterator<BaseConfiguration> i = sel.iterator(); i.hasNext();) {
					solConfig.getExposedProperties().removeConfig(i.next());
				}
				exposedProps.refresh();
			}			
		});
		Composite tableComp = new Composite(c, SWT.NONE);
		tableComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		TableColumnLayout layout = new TableColumnLayout();
		tableComp.setLayout(layout);
		exposedProps = new TableViewer(tableComp, SWT.FULL_SELECTION | SWT.MULTI| SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		exposedProps.getTable().setHeaderVisible(true);
		exposedProps.getTable().setLinesVisible(true);

		final TextCellEditor cellEditor = new TextCellEditor(exposedProps.getTable());

		final TableViewerFocusCellManager mgr = new TableViewerFocusCellManager(exposedProps,new FocusCellOwnerDrawHighlighter(exposedProps));
		ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(exposedProps) {
			@Override
			protected boolean isEditorActivationEvent(
					ColumnViewerEditorActivationEvent event) {
				return event.eventType == ColumnViewerEditorActivationEvent.MOUSE_CLICK_SELECTION
				|| event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION
				|| event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL;
			}
		};

		TableViewerEditor.create(exposedProps, mgr, actSupport, ColumnViewerEditor.TABBING_HORIZONTAL
				| ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR| ColumnViewerEditor.KEYBOARD_ACTIVATION);


		TableViewerColumn tv = new TableViewerColumn(exposedProps, SWT.LEFT);
		tv.getColumn().setText(Messages.getString("SolutionInterfaceUI.Name")); //$NON-NLS-1$
		tv.setLabelProvider( new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((ExposedProperty) element).getPropertyName();
			}		
		});
		tv.setEditingSupport(new EditingSupport(exposedProps) {
			@Override
			protected boolean canEdit(Object element) {
				return true;
			}
			@Override
			protected CellEditor getCellEditor(Object element) {
				return cellEditor;
			}
			@Override
			protected Object getValue(Object element) {
				return ((ExposedProperty) element).getPropertyName();
			}
			@Override
			protected void setValue(Object element, Object value) {
				((ExposedProperty) element).setPropertyName((String)value);
				exposedProps.update(element, null);
			}
		});
		layout.setColumnData( tv.getColumn(), new ColumnWeightData( 20 ) );

		tv = new TableViewerColumn(exposedProps, SWT.LEFT);
		tv.getColumn().setText(Messages.getString("SolutionInterfaceUI.Store")); //$NON-NLS-1$
		tv.setLabelProvider( new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return safeValue(((ExposedProperty) element).getStoreName());
			}		
		});
		tv.setEditingSupport(new EditingSupport(exposedProps) {
			@Override
			protected boolean canEdit(Object element) {
				return true;
			}
			@Override
			protected CellEditor getCellEditor(Object element) {
				return cellEditor;
			}
			@Override
			protected Object getValue(Object element) {
				return safeValue(((ExposedProperty) element).getStoreName());
			}
			@Override
			protected void setValue(Object element, Object value) {
				((ExposedProperty) element).setStoreName((String)value);
				exposedProps.update(element, null);
			}
		});
		layout.setColumnData( tv.getColumn(), new ColumnWeightData( 20 ) );

		tv = new TableViewerColumn(exposedProps, SWT.LEFT);
		tv.getColumn().setText(Messages.getString("SolutionInterfaceUI.Label")); //$NON-NLS-1$
		tv.setLabelProvider( new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return safeValue(((ExposedProperty) element).getLabel());
			}		
		});
		tv.setEditingSupport(new EditingSupport(exposedProps) {
			@Override
			protected boolean canEdit(Object element) {
				return true;
			}
			@Override
			protected CellEditor getCellEditor(Object element) {
				return cellEditor;
			}
			@Override
			protected Object getValue(Object element) {
				return safeValue(((ExposedProperty) element).getLabel());
			}
			@Override
			protected void setValue(Object element, Object value) {
				((ExposedProperty) element).setLabel((String)value);
				exposedProps.update(element, null);
			}
		});
		layout.setColumnData( tv.getColumn(), new ColumnWeightData( 20 ) );

		tv = new TableViewerColumn(exposedProps, SWT.LEFT);
		tv.getColumn().setText(Messages.getString("SolutionInterfaceUI.HelpText")); //$NON-NLS-1$
		tv.setLabelProvider( new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return safeValue(((ExposedProperty) element).getUserComment());
			}		
		});
		tv.setEditingSupport(new EditingSupport(exposedProps) {
			@Override
			protected boolean canEdit(Object element) {
				return true;
			}
			@Override
			protected CellEditor getCellEditor(Object element) {
				return cellEditor;
			}
			@Override
			protected Object getValue(Object element) {
				return safeValue(((ExposedProperty) element).getUserComment());
			}
			@Override
			protected void setValue(Object element, Object value) {
				((ExposedProperty) element).setUserComment((String)value);
				exposedProps.update(element, null);
			}
		});
		layout.setColumnData( tv.getColumn(), new ColumnWeightData( 20 ) );

		tv = new TableViewerColumn(exposedProps, SWT.LEFT);
		tv.getColumn().setText(Messages.getString("SolutionInterfaceUI.Category")); //$NON-NLS-1$
		tv.setLabelProvider( new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return safeValue(((ExposedProperty) element).getCategory());
			}		
		});
		tv.setEditingSupport(new EditingSupport(exposedProps) {
			@Override
			protected boolean canEdit(Object element) {
				return true;
			}
			@Override
			protected CellEditor getCellEditor(Object element) {
				return cellEditor;
			}
			@Override
			protected Object getValue(Object element) {
				return safeValue(((ExposedProperty) element).getCategory());
			}
			@Override
			protected void setValue(Object element, Object value) {
				((ExposedProperty) element).setCategory((String)value);
				exposedProps.update(element, null);
			}
		});
		layout.setColumnData( tv.getColumn(), new ColumnWeightData( 20 ) );

		exposedProps.setContentProvider(new ContainerConfigProvider());
		exposedProps.setInput(solConfig.getExposedProperties());
	}

	private void exposeProperty(Object o) {
		String name;
		String store = "";
		if (o instanceof IFile) {
			name = "*";
			store = storeName((IFile)o);		
		} else {
			BaseConfiguration b = (BaseConfiguration)o;
			name = b.getShortName();
			ContainerConfig cc = (ContainerConfig) b.getParent();
			if (cc != null && cc.getParent() != null)
				store = cc.getParent().getShortName();
		}

		if(solConfig.getExposedProperty(name, store) != null) {
			if ( givePopupMessage )
				MessageDialog.openInformation(getShell(), Messages.getString("miadmin.title.information"), 
						Messages.getString("SolutionInterfaceUI.OnlyAddOnce"));
			givePopupMessage = false;
			return;
		}

		try {
			ExposedProperty e = solConfig.addExposedProperty(name, store);
			e.setLabel(name);
			e.setCategory("Default");
		} catch (Exception e) {
			EclipseAppender.logerror(e.getMessage(), e);
		}
	}

	private static class ContainerConfigProvider implements IStructuredContentProvider {

		public Object[] getElements(Object element) {
			if (element instanceof ContainerConfig) {
				List<BaseConfiguration> list = ((ContainerConfig)element).getConfigurations(null);
				Collections.sort(list, new Comparator<BaseConfiguration>() {
					public int compare(BaseConfiguration o1, BaseConfiguration o2) {
						return o1.getShortName().compareToIgnoreCase(o2.getShortName());
					}
				});
				return list.toArray();
			}
			return new Object[0];
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// do nothing.
		}

		public void dispose() {
			// do nothing.
		}
	}
}
