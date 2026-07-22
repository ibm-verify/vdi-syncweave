/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.editors;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
//import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.ide.IDE;

import com.ibm.di.api.security.CryptoUtils;
import com.ibm.di.config.base.BaseConfigurationImpl;
import com.ibm.di.config.base.ContainerConfigImpl;
import com.ibm.di.config.base.InternalSchema;
import com.ibm.di.config.base.PropertyManagerImpl;
import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.di.config.interfaces.MetamergeConfigChangeListener;
import com.ibm.di.config.interfaces.PropertyManager;
import com.ibm.di.config.interfaces.PropertyStoreConfig;
import com.ibm.di.config.interfaces.RawConnectorConfig;
import com.ibm.di.config.interfaces.TDIProperties;
import com.ibm.di.connector.ConnectorInterface;
import com.ibm.di.entry.Entry;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.function.UserFunctions;
import com.ibm.di.security.Crypto;
import com.ibm.di.server.ConnectorMode;
import com.ibm.di.util.BasePropertiesFile;
import com.ibm.di.util.PropertiesFile;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.actions.CopyConfigAction;
import com.ibm.tdi.eclipse.actions.CutConfigAction;
import com.ibm.tdi.eclipse.actions.PasteConfigAction;
import com.ibm.tdi.eclipse.actions.RenameConfigAction;
import com.ibm.tdi.eclipse.builders.ProjectRuntimeDirectory;
import com.ibm.tdi.eclipse.extensions.ExtensionPointManager;
import com.ibm.tdi.eclipse.jobs.TDIPropertiesJob;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.natures.TDINature;
import com.ibm.tdi.eclipse.providers.PropertyContentProvider;
import com.ibm.tdi.eclipse.util.TDIToolBar;
import com.ibm.tdi.eclipse.widget.BaseWidget;
import com.ibm.tdi.eclipse.widget.FormWidget2;
import com.ibm.tdi.eclipse.widget.ParserWidget;
import com.ibm.tdi.eclipse.widget.RawConnectorWidget;
import com.ibm.tdi.eclipse.wizards.NewComponentBaseWizard;
import com.ibm.tdi.eclipse.wizards.NewConnectorWizard;
import com.ibm.tdi.eclipse.wizards.NewParserWizard;

public class PropertiesEditor extends BaseEditor implements SelectionListener {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public final static String CONFIG_DIR = "{config.$directory}";
	public final static String LOCAL_VALUE = TDIProperties.VALUE_ATTRIBUTE;
	public final static String SERVER_VALUE = "serverValue";
	public final static String SERVER_NAME = "serverName";
	public final static String DELETED = "Deleted";
	public final static String PROPERTY = "Property";
	public final static String LOCAL_PROTECT = "Protect";
	public final static String SERVER_PROTECT = "serverProtect";
	public final static String PROPERTY_FILE_OBJECT = "propertyFileObject";
	public final static String ENCRYPT_PREFIX = BasePropertiesFile.PROTECT_VAL_PREFIX;

	private Text filter;
	private TableViewer viewer;
	private PropertyContentProvider pcp;
	private ExtensionPointManager xpm = new ExtensionPointManager();
	private boolean hideServerProperties;
	private TabFolder connectionTabs;
	private MetamergeConfigChangeListener listener;
	private ContainerConfig data;
	private PropertyManager pm;
	private PropertyStoreConfig ps;
	private BaseWidget base;
	private TDIToolBar bar;
	private ArrayList<BaseConfiguration> changes;

	private Action downloadAction;

	private Action uploadAction;

	private boolean wasNotified = false;

	private final static String[] falseTrue = {"false", "true" };

	private static Crypto defaultCrypto;
	static {
		try {
			defaultCrypto = CryptoUtils.getDefaultCrypto();
		} catch (Exception e) {
			EclipseAppender.logerror("defaultCrypto", e);
		}
	}
	public PropertiesEditor() {
	}

	@Override
	public void createPartControl(Composite parent) {

		if(getTDIConfiguration() == null) {
			super.createPartControl(parent);
			return;
		}

		data = (ContainerConfig) ((ContainerConfig) getTDIConfiguration()).getConfig("Data"); //$NON-NLS-1$
		pm = (PropertyManager) ((ContainerConfig) getTDIConfiguration()).getConfig("Config"); //$NON-NLS-1$
		ps = (PropertyStoreConfig) pm.getPropertyStores().getConfig(0);

		getEditorSite().setSelectionProvider(getSelectionProvider());

		base = new BaseWidget(parent, SWT.NULL, getTDIConfiguration());
		base.setLayout(new FillLayout());

		Form frm = base.createForm(base, null);
		frm.getBody().setLayout(new FillLayout());
		bar = new TDIToolBar(frm);
		bar.setText(Messages.getString("general.properties.label")); //$NON-NLS-1$

		Action addPropertyAction = new Action() {
			@Override
			public String getText() {
				return Messages.getString("outline.label.0");
			}

			@Override
			public void run() {
				addPropertyDialog();
			}
		};
		bar.add(addPropertyAction);

		downloadAction = new Action() {
			@Override
			public String getText() {
				return Messages.getString("PropertiesEditor.8"); //$NON-NLS-1$
			}

			@Override
			public String getToolTipText() {
				return Messages.getString("PropertiesEditor.9"); //$NON-NLS-1$
			}

			@Override
			public void run() {
				try {
					prepareForTransfer("PropertiesEditor.10"); //$NON-NLS-1$
					final TDIPropertiesJob job = new TDIPropertiesJob(Utils.getTDIServer(getTDIConfigFile()), getTDIConfiguration(), true);
					job.addJobChangeListener(new JobChangeAdapter() {

						@Override
						public void done(IJobChangeEvent event) {
							String message = "";
							changes = new ArrayList<BaseConfiguration>();
							if (event.getResult() == Status.OK_STATUS) {
								readPropertiesFromEntry(job.getEntry());
							} else {
								message = event.getResult().getMessage();
							}

							final String msg = message;
							viewer.getControl().getDisplay().asyncExec(new Runnable() {
								public void run() {
									endTransfer(msg);
									if (changes != null && changes.size() > 0) {
										setModified(true);
										viewer.setInput(data);
										viewer.setSelection(new StructuredSelection(changes.toArray()), true);
									} else if (msg != null){
										base.getForm().setMessage(Messages.getString("PropertiesEditor.13")); //$NON-NLS-1$
									}
									changes = null;
								}
							});
						}
					});
					job.schedule();
				} catch (Exception e) {
					EclipseAppender.logerror(Messages.getString("PropertiesEditor.14"), e); //$NON-NLS-1$
					endTransfer(null);
				}
				super.run();
			}
		};
		bar.add(downloadAction);

		uploadAction = new Action() {
			@Override
			public String getText() {
				return Messages.getString("PropertiesEditor.15"); //$NON-NLS-1$
			}

			@Override
			public String getToolTipText() {
				return Messages.getString("PropertiesEditor.16"); //$NON-NLS-1$
			}

			@Override
			public void run() {
				prepareForTransfer("PropertiesEditor.17"); //$NON-NLS-1$
				data.setModified(false);
				try {
					TDIPropertiesJob job = new TDIPropertiesJob(Utils.getTDIServer(getTDIConfigFile()), getTDIConfiguration(),
							false);
					job.setEntry(buildPropertiesEntry());
					job.addJobChangeListener(new JobChangeAdapter() {
						@Override
						public void done(IJobChangeEvent event) {
							String message = ""; //$NON-NLS-1$
							if (event.getResult() == Status.OK_STATUS) {
								message = Messages.getString("PropertiesEditor.19"); //$NON-NLS-1$
								unMarkProperties();
							} else {
								message = event.getResult().getMessage();
							}
							final String msg = message;
							viewer.getControl().getDisplay().asyncExec(new Runnable() {
								public void run() {
									endTransfer(msg);
									if (data.getModified())
										setModified(true);
									viewer.setInput(data);
								}
							});
						}
					});
					job.schedule();
				} catch (Exception e) {
					EclipseAppender.logerror(Messages.getString("PropertiesEditor.20"), e); //$NON-NLS-1$
					endTransfer(null);
				}
			}
		};
		bar.add(uploadAction);

		Composite main = frm.getBody();

		String str = getTDIConfiguration().getShortName();
		if (str.endsWith(".tdiproperties")) //$NON-NLS-1$
			str = str.substring(0, str.indexOf(".tdiproperties")); //$NON-NLS-1$

		// -- merge in contents of existing file if it's accessible
		synchWithLocalFile(data, ps);

		if (PropertyManager.STDCOLL_GLOBAL.equalsIgnoreCase(str) || PropertyManager.STDCOLL_JAVA.equalsIgnoreCase(str)
				|| PropertyManager.STDCOLL_SYSTEM.equalsIgnoreCase(str) || PropertyManager.STDCOLL_SOLUTION.equalsIgnoreCase(str)) {

			createTableEditor(main);

		} else {
			TabFolder tabs = new TabFolder(main, SWT.TOP);

			TabItem item = new TabItem(tabs, SWT.LEFT);
			item.setText(Messages.getString("PropertiesEditor.23")); //$NON-NLS-1$
			item.setControl(createTableEditor(tabs));

			item = new TabItem(tabs, SWT.LEFT);
			item.setText(Messages.getString("PropertiesEditor.24")); //$NON-NLS-1$
			item.setControl(createConnectorEditor(tabs));

			updateParserTab();
			listener = new MetamergeConfigChangeListener() {
				public void configurationChanged(MetamergeConfigChange changeEvent) {
					if (InternalSchema.INHERITS_FROM.equals(changeEvent.getKey()))
						updateParserTab();
					if (changeEvent.getSource() instanceof RawConnectorConfig 
							&& changeEvent.getOperation() == MetamergeConfigChange.MCC_REPLACE)
						synchWithLocalFile(data, ps);
				}
			};
			ps.getConnectionConfig().addListener(listener);
		}

		setModified(false);
	}

	public static void synchWithLocalFile(ContainerConfig data, PropertyStoreConfig ps) {
		RawConnectorConfig conn = ps.getConnectionConfig();
		if("system:/Connectors/ibmdi.Properties".equals(conn.getInheritsFromRef())) {
			String src = ps.getConnectionConfig().getParameterPropertySource("collection");
			if (src != null) {
				// Avoid endless loop if parameter comes from a property.
				if (!src.startsWith(CONFIG_DIR) || src.substring(CONFIG_DIR.length()).contains("{"))
					return;
			}
			try {
				PropertiesFile propsFile = new PropertiesFile(defaultCrypto, ps.getConnectionConfig().getStringParameter("collection"), true);
				Iterator<String> it = propsFile.keys();
				while (it.hasNext()) {
					String key = (String) it.next();
					BaseConfiguration b = data.getConfig(key, false);
					if(b == null) {						
						b = new BaseConfigurationImpl();
						b.setName(key);						
						b.setParameter(PROPERTY, "true");						
						verifyEncrypted(b);
						data.addConfig(b);
					} else {
						b.removeParameter(DELETED);
					}
					String value = propsFile.getProperty(key);
					b.setParameter(LOCAL_VALUE, value);
					if(propsFile.isPropertyProtected(key))
						b.setParameter(LOCAL_PROTECT, "true");
				}

				//Mark deleted items
				for (BaseConfiguration item: data.getConfigurations(null)) {
					String name = item.getShortName();
					if (name != null && propsFile.getProperty(name) == null)
						item.setBooleanParameter(DELETED, true);
				}

			} catch (Exception e) {
				SystemFunctions.doNothing();
			}
		}
	}

	protected void setButtonsEnabled(boolean b) {
		uploadAction.setEnabled(b);
		downloadAction.setEnabled(b);
		bar.update();
	}

	private Control createConnectorEditor(Composite parent) {
		SashForm c = new SashForm(parent, SWT.HORIZONTAL);

		TabFolder filler = new TabFolder(c, SWT.TOP);
		TabItem item = new TabItem(filler, SWT.LEFT);
		item.setText(Messages.getString("PropertiesEditor.25")); //$NON-NLS-1$
		try {
			item.setControl(new FormWidget2(filler, SWT.TITLE, ps, "PropertyStoreConfig")); //$NON-NLS-1$
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e);
		}

		connectionTabs = new TabFolder(c, SWT.LEFT);
		connectionTabs.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		item = new TabItem(connectionTabs, SWT.LEFT);
		item.setText(Messages.getString("PropertyStore.Configuration")); //$NON-NLS-1$
		item.setControl(new RawConnectorWidget(connectionTabs, SWT.TITLE, ps.getConnectionConfig(), true));

		c.setWeights(new int[] { 30, 70 });
		return c;
	}

	public void updateParserTab() {
		if (connectionTabs.getItemCount() > 1)
			connectionTabs.getItem(1).dispose();

		if (ps.getConnectionConfig().getParserOption() != RawConnectorConfig.PARSER_USELESS) {
			TabItem item = new TabItem(connectionTabs, SWT.LEFT);
			item.setText(Messages.getString("PropertiesEditor.28")); //$NON-NLS-1$
			item.setControl(new ParserWidget(connectionTabs, SWT.NONE, ps.getParserConfig()));
		}
	}

	public void changeConnector() {
		NewComponentBaseWizard wiz = new NewConnectorWizard();
		wiz.init(null, null);
		wiz.setChooseFileName(false);

		WizardDialog dlg = new WizardDialog(getSite().getShell(), wiz);
		if (dlg.open() == Window.OK) {
			String inherit = wiz.getConfigObject().getInheritsFromRef();
			ps.getConnectionConfig().setInheritsFromRef(inherit);
			try {
				ps.getConnectionConfig().setupInheritanceChain();
				connectionTabs.getItem(0).setControl(new RawConnectorWidget(connectionTabs, SWT.NONE, ps.getConnectionConfig()));
				updateParserTab();
			} catch (Exception e) {
				EclipseAppender.logerror(e.toString(), e, getSite().getShell());
			}
		}
	}

	public void changeParser() {
		NewComponentBaseWizard wiz = new NewParserWizard();
		wiz.init(null, null);
		wiz.setChooseFileName(false);

		WizardDialog dlg = new WizardDialog(getSite().getShell(), wiz);
		if (dlg.open() == Window.OK) {
			String inherit = wiz.getConfigObject().getInheritsFromRef();
			ps.getConnectionConfig().setInheritsFromRef(inherit);
			try {
				ps.getParserConfig().setupInheritanceChain();
				connectionTabs.getItem(1).setControl(new ParserWidget(connectionTabs, SWT.NONE, ps.getParserConfig()));
			} catch (Exception e) {
				EclipseAppender.logerror(e.toString(), e, getSite().getShell());
			}
		}
	}

	private Control createTableEditor(Composite parent) {

		Composite c = new Composite(parent, SWT.NULL);
		c.setLayout(new GridLayout(1, false));

		//
		// -- Header composite
		//
		createHeader(c);

		//
		// -- Table viewer
		//
		Control table = createViewer(c);

		//
		// -- Context menu
		//
		Menu outlineMenu = super.registerContextMenu(viewer);
		super.getMenuManager().add(new Separator("group.tdi")); //$NON-NLS-1$
		Action cutAction = new CutConfigAction(Messages.getString("common.Cut.name"), null); //$NON-NLS-1$
		super.getMenuManager().add(cutAction);
		Action copyAction = new CopyConfigAction(Messages.getString("common.Copy.name")); //$NON-NLS-1$
		super.getMenuManager().add(copyAction); 
		Action pasteAction = new PasteConfigAction(Messages.getString("common.Paste.name")) { //$NON-NLS-1$
			@Override
			protected void performPaste(IStructuredSelection selection) {
				ArrayList<BaseConfiguration> list = new ArrayList<BaseConfiguration>();
				if (selection.size() == 1) {
					BaseConfiguration b = (BaseConfiguration) selection.getFirstElement();
					b.removeParameter(DELETED);
					list.add(b);
				} else {
					for (Object obj : selection.toArray()) {
						BaseConfiguration b = (BaseConfiguration) obj;
						if (!isDeleted(b))
							list.add(b);
					}
				}

				IUndoableOperation operation = new UndoablePasteOperation(Messages.getString("common.Paste.name"), list); //$NON-NLS-1$
				try {
					IUndoContext undoContext = getSite().getWorkbenchWindow().getWorkbench().getOperationSupport().getUndoContext();
					operation.addContext(undoContext);
					OperationHistoryFactory.getOperationHistory().execute(operation, null, null);
				} catch (Exception e) {
					EclipseAppender.logerror(e.toString(), e, getSite().getShell());
				}
				viewer.refresh();
			}
			@Override
			protected boolean validatePaste(Object obj) {
				if (obj instanceof BaseConfiguration) {
					BaseConfiguration b = (BaseConfiguration) obj;
					return b.getShortName() != null && isProperty(b);
				}
				return false;
			}
		};
		super.getMenuManager().add(pasteAction);
		Action deleteAction = new CutConfigAction(Messages.getString("general.delete.label"), null);
		super.getMenuManager().add(deleteAction); //$NON-NLS-1$

		//registerAction(ActionFactory.CUT.getId(), cutAction);
		//registerAction(ActionFactory.COPY.getId(), copyAction);
		//registerAction(ActionFactory.PASTE.getId(), pasteAction);
		//registerAction(ActionFactory.DELETE.getId(), deleteAction);

		table.setMenu(outlineMenu);

		return c;
	}

	/**
	 * Creates the header composite with search filter and toolbar
	 * 
	 * @param parent
	 * @return The header Composite
	 */
	private Composite createHeader(Composite parent) {

		Composite header = new Composite(parent, SWT.NONE);
		header.setLayout(new GridLayout(4, false));

		// -- Search label
		Label label = new Label(header, SWT.LEFT);
		label.setText(Messages.getString("PropertyStoreUI.Search")); //$NON-NLS-1$

		// -- Search control
		filter = new Text(header, SWT.BORDER);
		filter.setLayoutData(new GridData(200, SWT.DEFAULT));
		filter.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				viewer.refresh();
			}
		});

		// -- Local only
		Button local = new Button(header, SWT.CHECK);
		local.setText(Messages.getString("PropertiesEditor.31")); //$NON-NLS-1$
		local.setToolTipText(Messages.getString("PropertiesEditor.32")); //$NON-NLS-1$
		local.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setHideServer(((Button) e.getSource()).getSelection());
			}
		});

		return header;
	}

	protected void setHideServer(boolean selection) {
		this.hideServerProperties = selection;
		viewer.refresh();
	}

	/**
	 * Creates the table viewer to show the contents of the property store
	 * config
	 * 
	 * @param parent
	 * @return The Table shown by this viewer
	 */
	private Control createViewer(Composite parent) {

		Composite tableComp = new Composite(parent, SWT.NONE);
		tableComp.setLayoutData(new GridData(GridData.FILL_BOTH));
		TableColumnLayout layout = new TableColumnLayout();
		tableComp.setLayout(layout);
		Table table = new Table(tableComp, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);

		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		viewer = new TableViewer(table);
		pcp = new PropertyContentProvider();
		viewer.setContentProvider(pcp);
		viewer.setLabelProvider(pcp);
		viewer.addFilter(new NameFilter());
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(true);

		TableColumn t = new TableColumn(viewer.getTable(), SWT.LEFT);
		t.setText(Messages.getString("PropertiesEditor.33")); //$NON-NLS-1$
		layout.setColumnData( t, new ColumnWeightData( 30 ) );
		t.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				viewer.setInput(data);
			}		
		});

		t = new TableColumn(viewer.getTable(), SWT.LEFT);
		t.setText(Messages.getString("PropertiesEditor.protected")); //$NON-NLS-1$
		layout.setColumnData( t, new ColumnWeightData( 10 ) );

		t = new TableColumn(viewer.getTable(), SWT.LEFT);
		t.setText(Messages.getString("PropertiesEditor.34")); //$NON-NLS-1$
		layout.setColumnData( t, new ColumnWeightData( 30 ) );

		t = new TableColumn(viewer.getTable(), SWT.LEFT);
		t.setText(Messages.getString("PropertiesEditor.35")); //$NON-NLS-1$
		layout.setColumnData( t, new ColumnWeightData( 30 ) );

		viewer.setColumnProperties(new String[] { "name",  //$NON-NLS-1$
				LOCAL_PROTECT, LOCAL_VALUE, SERVER_VALUE}
		);

		viewer.setCellModifier(new ICellModifier() {
			public boolean canModify(Object element, String property) {
				return property.equals(LOCAL_VALUE) || property.equals(LOCAL_PROTECT);
			}

			public Object getValue(Object element, String property) {
				String value = ((BaseConfiguration) element).getStringParameter(property);
				if (value == null)
					value = ""; //$NON-NLS-1$
				if(property.equals(LOCAL_PROTECT))
					return Boolean.valueOf(value) ? 1 : 0;
				else
					return value;
			}

			public void modify(Object element, String property, Object value) {
				BaseConfiguration bc = (BaseConfiguration) ((TableItem) element).getData();
				String current = bc.getStringParameter(property);
				if (LOCAL_PROTECT.equals(property) && value instanceof Integer) {
					int i = (Integer) value;
					if (i >=0 && i <= 1)
						value = falseTrue[i];
					else
						return;
				}
				if(current != null && current.equals(value))
					return;
				else
					bc.setParameter(property, value.toString());
				verifyEncrypted(bc);
			}
		});

		CellEditor[] editors = new CellEditor[4];
		editors[0] = new TextCellEditor(viewer.getTable());
		editors[1] = new ComboBoxCellEditor(viewer.getTable(), falseTrue );
		editors[2] = new TextCellEditor(viewer.getTable());
		editors[3] = new TextCellEditor(viewer.getTable());
		viewer.setCellEditors(editors);

		viewer.addOpenListener(new IOpenListener() {
			public void open(OpenEvent event) {
				if (viewer.isCellEditorActive())
					return;
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				if (selection.isEmpty()) {
					return;
				} else {
					viewer.editElement(selection.getFirstElement(), 2);
				}
			}
		});

		addSelectionProvider(viewer);

		viewer.setInput(data);

		viewer.getTable().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.F2)
					doRename(viewer.getSelection());
			}
		});

		return viewer.getTable();
	}

	public class NameFilter extends ViewerFilter {
		@SuppressWarnings("unused")
		private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			BaseConfiguration e = (BaseConfiguration) element;
			String serverValue = e.getStringParameter(SERVER_VALUE);
			String localValue = e.getStringParameter(LOCAL_VALUE);

			if (hideServerProperties && (localValue == null || localValue.equals(serverValue)))
				return false;

			String str = filter.getText().toLowerCase();
			if (str.length() == 0)
				return true;

			boolean b = e.getShortName().toLowerCase().indexOf(str) != -1;
			if (!b)
				b = (localValue != null && localValue.toLowerCase().indexOf(str) != -1);
			if (!b)
				b = (serverValue != null && serverValue.toLowerCase().indexOf(str) != -1);

			return b;
		}
	}

	public void widgetSelected(SelectionEvent e) {
		xpm.setEditingConfigObject(data);
		xpm.invokeActionHandler(e.getSource());
	}

	public void widgetDefaultSelected(SelectionEvent e) {
	}

	@Override
	public void setFocus() {
		super.setFocus();
		if (viewer != null)
			viewer.getControl().setFocus();
	}

	public void addPropertyDialog() {

		IInputValidator inputValidator = new IInputValidator() {
			public String isValid(String newText) {
				if (newText == null)
					return null;
				BaseConfiguration b = data.getConfig(newText.trim(), false);
				if (b == null || isDeleted(b))
					return null;
				return Messages.getString("RenameConfigAction.AlreadyExists");
			}
		};
		InputDialog id = new InputDialog(Display.getCurrent().getActiveShell(), Messages.getString("outline.label.0"), Messages
				.getString("PropertyStoreUI.Localized.Name"), "", inputValidator);
		if (id.open() == Window.OK) {
			String name = id.getValue().trim();
			BaseConfiguration b = data.getConfig(name, false);
			if (b != null) {
				b.removeParameter(DELETED);
				return;
			}
			try {
				b = new BaseConfigurationImpl();
				b.setName(name);
				b.setParameter(LOCAL_VALUE, "");
				b.setParameter(PROPERTY, "true");
				data.addConfig(b);
			} catch (Exception e) {
				EclipseAppender.logerror(e.toString(), e, Display.getCurrent().getActiveShell());
			}
		}

	}

	/**
	 * This method adds a property to the current list of properties (or overwrites the current value).
	 * It does not affect the DIRTY bit of the editor.
	 * 
	 * @param key
	 * @param value
	 * @throws Exception
	 */
	public void setProperty(String key, Object value) throws Exception {
		boolean saveModified = getTDIConfiguration().getModified();

		if(value == null) {
			data.removeConfig(key, false);
		} else {
			BaseConfiguration b = data.getConfig(key);
			if(b == null) {
				b = new BaseConfigurationImpl();
				b.setName(key);
				data.addConfig(b);
			} else {
				b.removeParameter(DELETED);
			}
			b.setParameter(LOCAL_VALUE, value.toString());
			b.setParameter(PROPERTY, "true");
		}
		wasNotified = true;

		getTDIConfiguration().setModified(saveModified);
	}

	private class UndoablePasteOperation extends AbstractOperation {

		private List<BaseConfiguration> items;
		private List<BaseConfiguration> itemsAdded;
		private List<BaseConfiguration> itemsRemoved;

		public UndoablePasteOperation(String label, List<BaseConfiguration> items) {
			super(label);
			this.items = items;
		}

		@Override
		public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			itemsAdded = new ArrayList<BaseConfiguration>();
			itemsRemoved = new ArrayList<BaseConfiguration>();
			for (BaseConfiguration b : items) {
				int index = data.indexOf(b.getShortName());
				if (index != -1) {
					BaseConfiguration oldProp = data.removeConfig(index);
					itemsRemoved.add(oldProp);
					b.setParameter(SERVER_NAME, oldProp.getParameter(SERVER_NAME));
					b.setParameter(SERVER_VALUE, oldProp.getParameter(SERVER_VALUE));
					b.setParameter(SERVER_PROTECT, oldProp.getParameter(SERVER_PROTECT));
					if (oldProp.getBooleanParameter(LOCAL_PROTECT, false))
						b.setBooleanParameter(LOCAL_PROTECT, true);
				} else {
					itemsAdded.add(b);
					b.removeParameter(SERVER_NAME);
					b.removeParameter(SERVER_VALUE);
					b.removeParameter(SERVER_PROTECT);
				}
				data.addConfig(b);
			}
			return Status.OK_STATUS;
		}

		@Override
		public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			return null;
		}

		@Override
		public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			for (BaseConfiguration b : itemsAdded) {
				data.removeConfig(b);
			}
			for (BaseConfiguration b : itemsRemoved) {
				int index = data.indexOf(b.getShortName());
				if (index != -1)
					data.removeConfig(index);
				data.addConfig(b);
			}
			itemsAdded = null;
			itemsRemoved = null;
			return Status.OK_STATUS;
		}

		@Override
		public boolean canExecute() {
			return items.size() > 0 && itemsAdded == null && itemsRemoved == null;
		}

		@Override
		public boolean canRedo() {
			return false;
		}

		@Override
		public boolean canUndo() {
			return itemsAdded != null && itemsRemoved != null;
		}

	}

	private void prepareForTransfer(String msgKey) {
		setButtonsEnabled(false);
		setNotificationsEnabled(false);
		pcp.setNotificationsEnabled(false);
		base.getForm().setMessage(Messages.getString(msgKey));
		if (ps != null && ps.getModified()) {
			doSave(null); // Save properties connector description
			Utils.nap(100);   // Sleep a bit, since save sometimes returns too soon
			ps.setModified(false);
		}		
	}

	private void endTransfer(String msg) {
		if (msg != null)
			base.getForm().setMessage(msg);
		setNotificationsEnabled(true);
		pcp.setNotificationsEnabled(true);
		setButtonsEnabled(true);
	}

	private void readPropertiesFromEntry(Entry e) {
		for (String name : e.getAttributeNames()) {
			BaseConfiguration bc = data.getConfig(name);
			if (bc == null) {
				bc = new BaseConfigurationImpl();
				try {
					bc.init();
					bc.setName(name);
					data.addConfig(bc);
					changes.add(bc);
				} catch (Exception e1) {
					EclipseAppender.logerror(name, e1);
					continue;
				}
			}

			bc.setParameter(SERVER_NAME, name);
			bc.setParameter(PROPERTY, "true");
			bc.removeParameter(DELETED);
			String value = e.getString(name);
			if (value == null)
				value = ""; //$NON-NLS-1$
			if (! value.equals(bc.getStringParameter(SERVER_VALUE))) {
				bc.setParameter(SERVER_VALUE, value);
				if (!changes.contains(bc))
					changes.add(bc);
			}
			boolean prot = Boolean.valueOf(""+e.getProperty(name));
			bc.setParameter(SERVER_PROTECT, String.valueOf(prot));
			verifyEncrypted(bc);
			if (value.equals(bc.getStringParameter(LOCAL_VALUE))
					&& prot == bc.getBooleanParameter(LOCAL_PROTECT, false))
				continue;

			bc.setStringParameter(LOCAL_VALUE, value);
			bc.setBooleanParameter(LOCAL_PROTECT, prot);
			verifyEncrypted(bc);
			if (!changes.contains(bc))
				changes.add(bc);
		}
		// Mark deleted items
		for (BaseConfiguration item: data.getConfigurations(null)) {
			String name = item.getShortName();
			if (name != null && e.getAttribute(name) == null && !isDeleted(item)) {
				item.setBooleanParameter(DELETED, true);
				changes.add(item);
			}
		}
	}

	private Entry buildPropertiesEntry() {
		Entry e = new Entry();
		for (BaseConfiguration item: data.getConfigurations(null)) {
			String serverName = item.getStringParameter(SERVER_NAME);
			if (isDeleted(item)) {
				if (serverName != null)
					e.newAttribute(serverName);
				continue;
			}
			String name = item.getShortName();
			if (name == null)
				continue;
			boolean changes = false;
			if (serverName != null && !name.equals(serverName)) {
				e.newAttribute(serverName);
				changes = true;
			}

			String localValue = item.getStringParameter(LOCAL_VALUE);
			if (localValue != null && !localValue.equals(item.getStringParameter(SERVER_VALUE)))
				changes = true;
			boolean prot = item.getBooleanParameter(LOCAL_PROTECT, false);
			if ( prot != item.getBooleanParameter(SERVER_PROTECT, false))
				changes = true;
			if (changes) {
				e.setAttribute(name, getLocalPropertyValue(item));
				e.setProperty(name, String.valueOf(prot));
			}
		}
		return e;
	}

	private void unMarkProperties() {
		for (BaseConfiguration item: data.getConfigurations(null)) {
			if (isDeleted(item)) {
				data.removeConfig(item);
				continue;
			}
			setIfChanged(item, SERVER_NAME, item.getShortName());
			setIfChanged(item, SERVER_VALUE, item.getParameter(LOCAL_VALUE));
			setIfChanged(item, SERVER_PROTECT, item.getParameter(LOCAL_PROTECT));
		}
	}

	private static void setIfChanged(BaseConfiguration item, String name, Object value) {
		if (value == null || value.equals(item.getParameter(name)))
			return;
		item.setParameter(name, value);
	}
	
	public static boolean isProperty(BaseConfiguration b) {
		return b != null && b.getBooleanParameter(PROPERTY, false);
	}

	public static boolean isDeleted(BaseConfiguration b) {
		return b != null && b.getBooleanParameter(DELETED, false);
	}

	@Override
	protected void reloadEditor() {
		// -- If a property is added (setProperty() method) when this editor is open we
		// -- suppress the reload editor prompt and reset the notify flag to false.
		if(wasNotified) {
			wasNotified = false;
		} else {
			super.reloadEditor();
		}
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		// -- Check if the user opened a ".properties" data file instead of the ".tdiproperties" file
		if(input instanceof IFileEditorInput) {
			if("properties".equals(((IFileEditorInput)input).getFile().getFileExtension())) {
				try {
					//super.init(site, getPropEditorInput((IFileEditorInput) input));
					IFile redir = getPropEditorInput((IFileEditorInput) input);
					IDE.openEditor(site.getPage(), redir);
					setSite(site);
					setInput(input);
					site.getShell().getDisplay().asyncExec(new Runnable() {
						public void run() {
							getSite().getPage().closeEditor(PropertiesEditor.this, false);
						}
					});
				} catch (Exception e) {
					throw new PartInitException(e.toString(), e);
				}
			} else {
				super.init(site, input);
			}
		} else {
			super.init(site, input);
		}
	}

	private IFile getPropEditorInput(IFileEditorInput input) throws Exception {
		String filename = input.getName().substring(0, input.getName().lastIndexOf(".")) + ".tdiproperties";
		IFile file = input.getFile().getProject().getFile(TDINature.RESOURCES_FOLDER + "/" + TDINature.PROPERTIES_FOLDER + "/" + filename);
		if(!file.exists()) {
			createPropertyStoreConfig(file);
		}
		return file;
	}

	private void createPropertyStoreConfig(IFile file) throws Exception {
		String name = file.getName().substring(0, file.getName().lastIndexOf("."));

		ContainerConfigImpl cc = new ContainerConfigImpl();
		cc.init();

		ContainerConfigImpl data = new ContainerConfigImpl();
		data.init();
		data.setName("Data"); //$NON-NLS-1$
		cc.addConfig(data);

		PropertyManager pm = new PropertyManagerImpl();
		pm.init();
		pm.setName("Config"); //$NON-NLS-1$
		cc.addConfig(pm);

		PropertyStoreConfig psc = new com.ibm.di.config.base.PropertyStoreConfigImpl();
		psc.init();

		RawConnectorConfig rcc = psc.getConnectionConfig();
		rcc.setParent(psc);
		rcc.setParameter("collectionType", "Default"); //$NON-NLS-1$ //$NON-NLS-2$
		rcc.setInheritsFromRef("system:/Connectors/ibmdi.Properties"); //$NON-NLS-1$
		rcc.setParameterPropertySource("collection", CONFIG_DIR + "/" + name + ".properties");

		psc.setName("Default"); //$NON-NLS-1$
		psc.setKeyAttribute("key"); //$NON-NLS-1$
		psc.setValueAttribute("value"); //$NON-NLS-1$
		psc.setInitialLoad(true);

		pm.addPropertyStore(psc);

		ProjectRuntimeDirectory prd = new ProjectRuntimeDirectory(file.getProject());

		// -- Read the current datafile
		File datafile = prd.getPropertyStorePath(psc);
		if (datafile != null) {
			rcc.setParameterPropertySource("collection", datafile.getAbsolutePath());
			ConnectorInterface conn = SystemFunctions.loadConnector(psc);
			try {
				conn.initialize(new ConnectorMode(ConnectorConfig.ITERATOR_MODE));
				conn.selectEntries();
				Entry entry;
				while( (entry = conn.getNextEntry()) != null) {
					BaseConfiguration bc = new BaseConfigurationImpl();
					data.addConfig(bc);
					bc.setName(entry.getString(TDIProperties.KEY_ATTRIBUTE));
					bc.setParameter(PropertiesEditor.PROPERTY, "true");
					String value = entry.getString(TDIProperties.VALUE_ATTRIBUTE);
					if (value == null)
						value = ""; //$NON-NLS-1$
					bc.setParameter(PropertiesEditor.SERVER_VALUE, value);
					bc.setStringParameter(PropertiesEditor.LOCAL_VALUE, value);
					String prot = entry.getString(TDIProperties.PROTECT_ATTRIBUTE);
					if (prot != null) {
						bc.setParameter(PropertiesEditor.SERVER_PROTECT, prot);
						bc.setParameter(PropertiesEditor.LOCAL_PROTECT, prot);
						verifyEncrypted(bc);
					}
				}
				conn.terminate();
			} catch (Exception e) {
				EclipseAppender.logerror(e.getLocalizedMessage(), e);
			}

			// -- reset config to expression
			rcc.setParameterPropertySource("collection", CONFIG_DIR + "/" + name + ".properties");
		}

		// -- Create/Overwrite the file
		TDIConfigurationFile cfg = new TDIConfigurationFile(file);
		cfg.setDefaultConfigObject(name, cc);
		cfg.commitVersion(true);

	}

	/**
	 * Check that the BaseConfiguration representing a property is encrypted
	 * @param bc
	 */
	public static void verifyEncrypted(BaseConfiguration bc) {
		boolean protect = bc.getBooleanParameter(LOCAL_PROTECT, false);
		String value = bc.getStringParameter(LOCAL_VALUE);
		if (value != null) {
			if (!isEncrypted(value)) {
				if (protect)
					bc.setParameter(LOCAL_VALUE, encrypt(value));
			} else if (!bc.getBooleanParameter(LOCAL_PROTECT, true)) {
				bc.setParameter(LOCAL_VALUE, getLocalPropertyValue(bc));
			}
		}

		protect = protect || bc.getBooleanParameter(SERVER_PROTECT, protect);
		value = bc.getStringParameter(SERVER_VALUE);
		if (value != null && protect && ! isEncrypted(value))
			bc.setParameter(SERVER_VALUE, encrypt(value));
	}

	private static boolean isEncrypted(String s) {
		return s != null && s.startsWith(ENCRYPT_PREFIX);
	}

	private static Object encrypt(String value) {
		if (defaultCrypto == null)
			return value;

		try {
			byte[] encryptedBytes = defaultCrypto.encrypt(value.getBytes());
			return ENCRYPT_PREFIX + UserFunctions.base64Encode(encryptedBytes);
		} catch (Exception e) {
			return value;
		}
	}

	public static String getLocalPropertyValue(BaseConfiguration bc) {
		String value = bc.getStringParameter(LOCAL_VALUE);
		if (!isEncrypted(value) || defaultCrypto == null)
			return value;

		String encryptedPayload = value.substring(ENCRYPT_PREFIX.length());
		byte[] encryptedBytes = UserFunctions.base64Decode(encryptedPayload);
		try {
			return new String(defaultCrypto.decrypt(encryptedBytes));
		} catch (Exception e) {
			return value;
		}
	}
	
	private RenameConfigAction renameConfigAction = new RenameConfigAction();
	private void doRename(ISelection selection) {
		Action a = new Action(){};
		a.setEnabled(true);
		renameConfigAction.selectionChanged(a, selection);
		renameConfigAction.run(a);
	}
}
