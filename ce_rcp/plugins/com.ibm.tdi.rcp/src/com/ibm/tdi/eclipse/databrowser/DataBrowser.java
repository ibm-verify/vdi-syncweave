/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.databrowser;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.progress.UIJob;
import org.w3c.dom.NodeList;

import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.AttributeMapItem;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.LoopConfig;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.di.config.interfaces.MetamergeConfigChangeListener;
import com.ibm.di.config.interfaces.SchemaConfig;
import com.ibm.di.config.interfaces.SchemaItemConfig;
import com.ibm.di.connector.Connector;
import com.ibm.di.connector.ConnectorInterface;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.fc.FunctionInterface;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.parser.ParserImpl;
import com.ibm.di.server.ConnectorMode;
import com.ibm.di.server.ServerConstants;
import com.ibm.di.store.StoreFactory;
import com.ibm.di.util.SchemaUtils;
import com.ibm.tdi.eclipse.Activator;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.editors.BaseEditor;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.natures.TDINature;
import com.ibm.tdi.eclipse.providers.EntryContentProvider;
import com.ibm.tdi.eclipse.util.TDIToolBar;
import com.ibm.tdi.eclipse.util.TextEditorContextMenu;
import com.ibm.tdi.eclipse.widget.BaseWidget;
import com.ibm.tdi.eclipse.widget.RawConnectorWidget;

public abstract class DataBrowser extends BaseWidget implements MetamergeConfigChangeListener {

	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final String ICONS_CONTINUE = "/icons/Continue"; //$NON-NLS-1$
	private static final String ICONS_STOP_GIF = "/icons/Stop"; //$NON-NLS-1$
	private static final String ICONS_RUN_GIF = "/icons/Run"; //$NON-NLS-1$
	private static final String ICONS_CLOSE = ICONS_STOP_GIF;

	private Action discoverAction;
	private Job job;
	private TreeViewer navigator;
	private SourceViewer detailsPanel;
	private TreeViewer entryDetailsTree;
	private Connector connector;
	private boolean selectEntriesCalled;
	private Composite entryForm;
	private Action getNextAction;
	private Form navigatorForm;
	private TabFolder detailsTabFolder;
	private Action closeConnectionAction;
	private boolean mergeAttributes = false;

	private GetNextJob getnextJob;

	private RawConnectorWidget connectorWidget;

	private SashForm main;

	private TDIToolBar headClient;

	private Label formMessage;

	public DataBrowser(Composite parent, int style, BaseConfiguration editingConfig, BaseEditor editor) {
		super(parent, style, editingConfig, editor);
		createUI();
	}

	public DataBrowser(Composite parent, int style, BaseConfiguration editingConfig) {
		this(parent, style, editingConfig, null);
	}

	public DataBrowser(Composite parent, int style) {
		this(parent, style, null);
	}

	/**
	 * Returns a DataBrowser subclass for the configuration.
	 *
	 * @param parent
	 * @param config ConnectorConfig or ScriptConfig for system stores
	 * @return a DataBrowser
	 */
	public static DataBrowser getInstance(Composite parent, BaseConfiguration config) {
		return getInstance(parent, config, true);
	}

	public static DataBrowser getInstance(Composite parent, BaseConfiguration config, boolean isMapping) {
		if(config instanceof LoopConfig) {
			try {
				return getInstance(parent, ((LoopConfig)config).getLoopConnector(), isMapping);
			} catch (Exception e) {
				EclipseAppender.logerror(e.toString(), e);
				return null;
			}
		} else {
			return getInstance(parent, (ConnectorConfig)config, isMapping);
		}
	}

	/**
	 * Returns a DataBrowser subclass for the connector.
	 *
	 * @param parent
	 * @param config
	 * @return a DataBrowser
	 */
	public static DataBrowser getInstance(Composite parent, ConnectorConfig config) {
		return getInstance(parent, config, true);
	}

	/**
	 * Returns a DataBrowser subclass for the connector.
	 *
	 * @param parent
	 * @param config
	 * @param isMapping
	 * @return a DataBrowser
	 */
	public static DataBrowser getInstance(Composite parent, ConnectorConfig config, boolean isMapping) {
		int style = isMapping ? SWT.NONE : SWT.READ_ONLY;
		if ("com.ibm.di.connector.LDAPConnector".equals(config.getConnectionConfig().getJavaClass())) //$NON-NLS-1$
			return new LDAPDataBrowser(parent, style, config);
		else if ("com.ibm.di.connector.JDBCConnector".equals(config.getConnectionConfig().getJavaClass())) //$NON-NLS-1$
			return new JDBCDataBrowser(parent, style, config);
		else if (Utils.hasParserRequirements(config))
			return new StreamBasedDataBrowser(parent, style, config);
		else if (config instanceof FunctionConfig)
			return new FunctionBrowser(parent, style, config);
		else
			return new GenericDataBrowser(parent, style, config);
	}

	private void createUI() {
		setLayout(new FillLayout());
		createForm(this, null);

		getForm().setText(quoteName(getEditingConfig().getShortName()));
		getForm().getBody().setLayout(new FillLayout());

		main = new SashForm(getForm().getBody(), SWT.HORIZONTAL);

		SashForm leftSash = new SashForm(main, SWT.VERTICAL);

		SashForm rightSash = new SashForm(main, SWT.VERTICAL);

		main.setWeights(new int[] { 25, 75 });

		//
		// -- Top Right: Entry display details
		//
		createEntryViewer(rightSash);

		//
		// -- Bottom Right: Navigator selection details
		//
		createDetailsPanel(rightSash);

		rightSash.setWeights(new int[] { 50, 50 });

		//
		// -- Top left tree viewer: Navigation
		//
		navigatorForm = getFormToolKit().createForm(leftSash);
		getFormToolKit().decorateFormHeading(navigatorForm);
		navigatorForm.setText(getNavigatorFormText());
		navigatorForm.getBody().setLayout(new FillLayout());
		navigatorForm.getToolBarManager().update(true);

		createNavigatorToolbarItems(navigatorForm);

		navigator = new TreeViewer(navigatorForm.getBody(), SWT.NONE);

		IContentProvider provider = getNavigatorContentProvider();
		if(provider != null)
			navigator.setContentProvider(provider);

		IBaseLabelProvider labelProvider = getNavigatorLabelProvider();
		if (labelProvider != null)
			navigator.setLabelProvider(labelProvider);

		Object input = getNavigatorInput();
		if(input != null)
			navigator.setInput(input);

		navigator.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				handleNavigatorSelectionChanged(event);
			}
		});

		createNavigatorContextMenu(navigator);

		leftSash.setWeights(new int[] { 100 });

		// -- browsers with no navigator (hide navigator)
		if (navigator.getInput() == null)
			main.setWeights(new int[] { 0, 100 });

		discoverData();
	}

	/**
	 * Return a String where ampersands are quoted (doubled). This seems to be needed.
	 * @param s
	 * @return
	 */
	private String quoteName(String s) {
		if (s.equals(TDINature.SOLUTION_SETTINGS_FILE))
			s = Messages.getString("miadmin.foldernames.SystemStore");
		if (s.indexOf('&')<0)
			return s;
		return s.replaceAll("&", "&&");
	}

	/**
	 * Creates the context menu for the navigator
	 *
	 * @param navigator
	 */
	protected Menu createNavigatorContextMenu(TreeViewer navigator) {
		Menu menu = new Menu(navigator.getTree());
		navigator.getTree().setMenu(menu);
		return menu;
	}

	/**
	 * Creates the toolbar items for the navigator
	 *
	 * @param form
	 */
	protected void createNavigatorToolbarItems(Form form) {

	}

	/**
	 * Adds an action to the main toolbar.
	 *
	 * @param action
	 */
	public void addToolbarAction(final IAction action) {

		headClient.add(action, (action.getStyle() == IAction.AS_CHECK_BOX ? SWT.CHECK : SWT.PUSH));

	}

	public ImageDescriptor getImageDescriptor(String path) {
		ImageDescriptor desc = Activator.getImageDescriptor(path + ".gif"); //$NON-NLS-1$
		if (desc == null)
			desc = Activator.getImageDescriptor(path + "_Enabled.gif"); //$NON-NLS-1$
		return desc;
	}

	public boolean isMergeAttributes() {
		return mergeAttributes;
	}

	public void setMergeAttributes(boolean mergeAttributes) {
		this.mergeAttributes = mergeAttributes;
	}

	@Override
	public void dispose() {
		closeConnector();

		getEditingConfig().removeListener(this);

		if (job != null)
			job.cancel();

		if (connectorWidget != null) {
			connectorWidget.dispose();
			connectorWidget = null;
		}

		super.dispose();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.ibm.di.config.interfaces.MetamergeConfigChangeListener#configurationChanged(com.ibm.di.config.interfaces.MetamergeConfigChange)
	 */
	public void configurationChanged(MetamergeConfigChange arg0) {
		// Whenever the connection config has changed we force a reselect
		// sub classes should detect changes to connection settings and close
		// the connector
		// to force a reconnect.
		selectEntriesCalled = false;
	}

	public void discoverData() {
		setEntryFormMessage(Messages.getString("DataBrowser.8")); //$NON-NLS-1$
		closeConnector();
		job = getDiscoverJob();
		job.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				if (isDisposed())
					return;

				Job job = event.getJob();
				job.removeJobChangeListener(this);
				UIJob ui = new UIJob(getDisplay(), "Discover") { //$NON-NLS-1$
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) {
						discoverAction.setImageDescriptor(getImageDescriptor(ICONS_RUN_GIF));
						if (!navigatorForm.isDisposed()) {
							navigatorForm.getToolBarManager().update(true);
							navigatorForm.setMessage(null);
							setEntryFormMessage(null);
							navigator.setInput(getNavigatorInput());
							// -- make sure to show the navigator for those that have data in it
							if(getNavigatorInput() != null)
								main.setWeights(new int[] { 25, 75 });
						}
						DataBrowser.this.job = null;
						return Status.OK_STATUS;
					}
				};
				ui.schedule();
			}
		});
		job.schedule();
	}

	protected void setEntryFormMessage(String string) {
		if(formMessage == null) {
			if(string == null)
				return;
			headClient.addRow();
			formMessage = headClient.addLabel(SWT.LEFT);
		}
		if(string != null) {
			formMessage.setText(string);
		} else {
			formMessage.dispose();
			formMessage = null;
		}
		headClient.layout(true, true);
	}

	protected Job getDiscoverJob() {
		if(job == null) {
			job = new DiscoverJob();
		}
		return job;
	}

	/**
	 * Executes a background job to read the next entry from the connector. Upon
	 * completion, the job calls setNextEntry() with the last entry read.
	 */
	public void getNextEntry() {
		try {
			if (getnextJob == null) {
				getnextJob = getGetNextJob();
				addGetNextJobListener(getnextJob);
			}
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e, getShell());
			getnextJob = null;
			return;
		}
		getNextAction.setEnabled(false);
		getnextJob.schedule();
	}

	protected GetNextJob getGetNextJob() {
		return new GetNextJob(this);
	}

	private void addGetNextJobListener(GetNextJob job) {
		job.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				UIJob ui = new UIJob(getDisplay(), "GetNext") { //$NON-NLS-1$
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) {
						getNextAction.setEnabled(true);
						Entry entry = getnextJob.getNextEntry();
						if (entry == null) {
							MessageDialog.openInformation(getShell(), Messages.getString("DataBrowser.title"), Messages.getString("DataBrowser.12")); //$NON-NLS-1$
							closeConnector();
//							if(!isMergeAttributes())
//								setNextEntry(null);
						} else {
							setEntryFormMessage(null);
							setNextEntry(entry);
						}
						return Status.OK_STATUS;
					}
				};
				ui.schedule();
			}
		});
	}

	public boolean buildSchema(Vector<Entry> v, boolean input) throws Exception {
		if (v.size() == 0)
			return false;

		final boolean schemaInput = input;
		final Vector<Entry> entries = v;
		UIJob uijob = new UIJob("") {
			public IStatus runInUIThread(IProgressMonitor monitor) {
				ConnectorConfig config = (ConnectorConfig) getEditingConfig();
				SchemaConfig sc = config.getSchema(schemaInput);
				sc.notifyChange(sc, "", MetamergeConfigChange.BEGIN_CHANGES);
				for (Entry e : entries) {
					String name = e.getString("name"); //$NON-NLS-1$
					String syntax = e.getString("syntax"); // $NON-NLS-1$
					try {
						SchemaUtils.addSchemaItem(sc, name, syntax, null);
					} catch (Exception ex) {
						EclipseAppender.logerror(ex.getMessage(), ex);
					}
				}
				sc.notifyChange(sc, "", MetamergeConfigChange.END_CHANGES);
				return Status.OK_STATUS;
			}
		};
		uijob.schedule();
		return v.size() > 0;
	}

	private class DiscoverJob extends Job {

		public DiscoverJob() {
			super("DiscoverData"); //$NON-NLS-1$
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				// -- load and initialize connector
				getConnectorInstance();

				// -- connector specific initial discovery
				doInitialDiscovery();

			} catch (Exception e) {
				// Do not show an error if we cannot find the file for a FileSystemConnector in AddOnly mode
				if (e instanceof FileNotFoundException) {
					ConnectorConfig cc = (ConnectorConfig) getEditingConfig();
					if (ConnectorConfig.ADDONLY_MODE.equals(cc.getMode()) &&
							"com.ibm.di.connector.FileConnector".equals(cc.getConnectionConfig().getJavaClass()))
							return Status.CANCEL_STATUS;
				}
				if(!isDisposed())
					return EclipseAppender.statusException(e);
			}
			return Status.OK_STATUS;
		}
	}

	@SuppressWarnings("unchecked")
	public void querySchema(FunctionInterface function) throws Exception {
		Object result = function.querySchema(Boolean.TRUE);
		boolean gotSchema = false;
		if (result instanceof Vector) {
			gotSchema = buildSchema((Vector<Entry>) result, true);
		}
		result = function.querySchema(Boolean.FALSE);
		if (result instanceof Vector) {
			gotSchema = buildSchema((Vector<Entry>) result, false);
		}
		if (gotSchema)
			return;

		result = function.querySchema(null);
		if (result instanceof Vector) {
			buildSchema((Vector<Entry>) result, true);
			buildSchema((Vector<Entry>) result, false);
		}
	}

	@SuppressWarnings("unchecked")
	public void querySchema(ConnectorInterface connector) throws Exception {

		try {
			connector.initialize(new ConnectorMode(ConnectorConfig.ITERATOR_MODE));
			connector.selectEntries();
		} catch (Throwable t) {
			EclipseAppender.logerror("querySchema", t);
			// If the connector does not want to be initialized, we can
			// still try queryschema
		}

		Object result = connector.querySchema(null);

		if (result instanceof Vector) {
			if (Utils.isInputConnector(getEditingConfig()))
				buildSchema((Vector<Entry>) result, true);
			if (Utils.isOutputConnector(getEditingConfig()))
				buildSchema((Vector<Entry>) result, false);
		}
	}

	protected static class GetNextJob extends Job {

		private Entry nextEntry;
		private ConnectorInterface client;
		private DataBrowser browser;

		public GetNextJob(DataBrowser browser) {
			super("GetNextEntry"); //$NON-NLS-1$
			this.browser = browser;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				Connector conn = browser.getConnectorInstance();
				browser.selectEntries();
				nextEntry = null;
				if (ConnectorConfig.SERVER_MODE.equals(((ConnectorConfig) browser.getEditingConfig()).getMode())) {
					if (client == null)
						client = conn.getNextClient();
					if (client != null)
						nextEntry = client.getNextEntry();
				} else {
					nextEntry = conn.getNextEntry();
					// Deserialize Entry blob
					if (nextEntry != null && nextEntry.getObject("ENTRY") instanceof byte[]) {
						try {
							nextEntry.setAttribute("ENTRY",
								StoreFactory.deserializeObject(nextEntry.getObject("ENTRY")));
						} catch (Exception ignore) {
							// Ignore it if we cannot deserialize
							SystemFunctions.doNothing();
						}
					}
				}
			} catch (Exception e) {
				return EclipseAppender.statusException(e);
			}
			return Status.OK_STATUS;
		}

		public void close() {
			if (client != null) {
				try {
					client.terminate();
				} catch (Exception e) {
					// Ignore this exception - nothing to do
					SystemFunctions.doNothing();
				}
				client = null;
			}
		}

		public Entry getNextEntry() {
			return nextEntry;
		}

		public DataBrowser getBrowser() {
			return browser;
		}
	}

	protected static class DummyParser extends ParserImpl {
		public String getVersion() {
			return null;
		}

		public Entry readEntry() throws Exception {
			return null;
		}

		public void writeEntry(Entry entry) throws Exception {
		}
	}

	/**
	 * Returns a connector instance. Connector is initialized if not already
	 * loaded.
	 *
	 * @return The Connector instance.
	 * @throws Exception
	 */
	public Connector getConnectorInstance() throws Exception {
		if (connector == null) {
			ConnectorConfig cc = (ConnectorConfig) getEditingConfig();
			if (cc instanceof FunctionConfig)
				connector = (Connector) SystemFunctions.loadFunction((FunctionConfig) cc);
			else
				connector = (Connector) SystemFunctions.loadConnector(cc);
			if (Utils.hasParserRequirements(cc) && cc.getParserConfig().getJavaClass() == null)
				connector.setParser(new DummyParser());
			connector.initialize(new ConnectorMode(ServerConstants.TYPE_ITERATOR));
			selectEntriesCalled = false;
			getDisplay().syncExec(new Runnable() {
				public void run() {
					closeConnectionAction.setEnabled(true);
					discoverAction.setEnabled(false);
				}
			});
		}
		return connector;
	}

	/**
	 * Calls selectEntries on the connector. Will only call selectEntries if the
	 * selectEntriesCalled field is false (reset when connector is initialized).
	 *
	 * @throws Exception
	 */
	public void selectEntries() throws Exception {
		if (connector == null || !selectEntriesCalled) {
			getConnectorInstance().selectEntries();
			selectEntriesCalled = true;
		}
	}

	/**
	 * Closes the connector instance.
	 *
	 */
	public void closeConnector() {
		if (connector != null) {
			try {
				connector.terminate();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (getnextJob != null) {
			getnextJob.cancel();
		}

		if (closeConnectionAction != null)
			closeConnectionAction.setEnabled(false);
		if (discoverAction != null) {
			discoverAction.setEnabled(true);
		}

		selectEntriesCalled = false;

		connector = null;
	}

	/**
	 * Returns the tree viewer to display navigator contents
	 *
	 * @return the TreeViewer
	 */
	public TreeViewer getNavigator() {
		return navigator;
	}

	/**
	 * Creates the Entry object viewer. This is a TreeViewer by default.
	 *
	 * @param parent
	 * @return The Composite containing the viewer
	 */
	protected Composite createEntryViewer(Composite parent) {

		entryForm = new Composite(parent, SWT.NONE);
		Utils.setGridLayout(entryForm, 1, false);
		entryForm.setBackground(parent.getBackground());

		headClient = new TDIToolBar(entryForm, SWT.MULTI | SWT.RIGHT | SWT.TITLE);
		headClient.setText(Messages.getString("DataBrowser.15")); //$NON-NLS-1$
		headClient.addControlListener(new ControlListener() {
			public void controlMoved(ControlEvent e) {
			}
			public void controlResized(ControlEvent e) {
				entryForm.layout(true, true);
			}
		});
		headClient.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		headClient.setBackground(parent.getBackground());

		createEntryViewerToolbar();

		Composite c = new Composite(entryForm, SWT.NONE);
		c.setLayout(new FillLayout());
		c.setLayoutData(new GridData(GridData.FILL_BOTH));
		c.setBackground(parent.getBackground());

		if(isMappingAttributes()) {
			entryDetailsTree = new CheckboxTreeViewer(c, SWT.MULTI | SWT.FULL_SELECTION);
			((CheckboxTreeViewer)entryDetailsTree).addCheckStateListener(new ICheckStateListener() {
				public void checkStateChanged(CheckStateChangedEvent event) {
					if (event.getElement() instanceof Attribute || event.getElement() instanceof SchemaItemConfig) {
						updateAttributeMap(event.getElement(), event.getChecked());
					} else {
						if (event.getChecked())
							((CheckboxTreeViewer)entryDetailsTree).setChecked(event.getElement(), false);
					}
				}
			});
		} else {
			entryDetailsTree = new TreeViewer(c, SWT.MULTI | SWT.FULL_SELECTION);
		}

		TreeColumn col = new TreeColumn(entryDetailsTree.getTree(), SWT.LEFT);
		col.setText(Messages.getString("DataBrowser.20")); //$NON-NLS-1$
		col.setWidth(300);

		col = new TreeColumn(entryDetailsTree.getTree(), SWT.LEFT);
		col.setText(Messages.getString("DataBrowser.21")); //$NON-NLS-1$
		col.setWidth(500);

		entryDetailsTree.getTree().setHeaderVisible(true);

		entryDetailsTree.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection().isEmpty())
					return;
				dumpObject(((IStructuredSelection) event.getSelection()).getFirstElement());
			}
		});

		EntryContentProvider ecp = new EntryContentProvider() {

			@Override
			public Object[] getChildren(Object elem) {
				ArrayList<Object> list = new ArrayList<Object>();
				if (elem instanceof Attribute) {
					Attribute a = (Attribute) elem;
					NodeList nl = a.getChildNodes();
					for (int i = 0, n = nl.getLength(); i<n; i++) {
						list.add(nl.item(i));
					}
					return list.toArray();
				} else if (elem instanceof Entry) {
					ArrayList<String> names = new ArrayList<String>();
					NodeList nl = ((Entry) elem).getChildNodes();
					for (int i = 0, n = nl.getLength(); i<n; i++) {
						if (nl.item(i) instanceof Attribute)
							names.add(((Attribute)nl.item(i)).getName());
					}
					Collections.sort(names);

					for (String str : names)
						list.add(((Entry) elem).getAttribute(str));

				} else if (elem instanceof SchemaConfig) {
					SchemaConfig sc = (SchemaConfig) elem;
					for (String str : sc.getItemNames())
						list.add(sc.getItem(str));

					Collections.sort(list, new Comparator<Object>() {
						public int compare(Object p1, Object p2) {
							SchemaItemConfig o1 = (SchemaItemConfig) p1;
							SchemaItemConfig o2 = (SchemaItemConfig) p2;
							if (o1.getSample() != null && o2.getSample() != null)
								return o1.getAttributeName().compareTo(o2.getAttributeName());
							else if (o1.getSample() == null && o2.getSample() == null)
								return o1.getAttributeName().compareTo(o2.getAttributeName());
							else if (o1.getSample() != null)
								return -1;
							else
								return 1;
						}

					});

				} else if (elem instanceof SchemaItemConfig) {
					SchemaItemConfig sic = (SchemaItemConfig) elem;
					for (BaseConfiguration bc : sic.getChildSchemaList().getConfigurations(null))
						list.add(bc);

					Collections.sort(list, new Comparator<Object>() {
						public int compare(Object p1, Object p2) {
							SchemaItemConfig o1 = (SchemaItemConfig) p1;
							SchemaItemConfig o2 = (SchemaItemConfig) p2;
							if (o1.getSample() != null && o2.getSample() != null)
								return o1.getAttributeName().compareTo(o2.getAttributeName());
							else if (o1.getSample() != null)
								return -1;
							else
								return 1;
						}

					});

				} else if (elem instanceof List) {
					return ((List<?>) elem).toArray();

				} else {
					return super.getChildren(elem);
				}

				return list.toArray();
			}

			@Override
			public String getColumnText(Object element, int columnIndex) {
				if (element instanceof Attribute) {
					Attribute a = (Attribute) element;
					if (columnIndex == 0)
						return a.getName();
					String s = a.toString();
					if (s == null)
						return "";
					//Clean up for Win2K3. Newlines are just annoying anyway
					// Also need to remove the Attribute Name, which for some reason is prepended.
					int i = s.indexOf(": ")+1;
					if (i>0 && i < s.length())
						s = s.substring(i);
					return s.replace("\n", "").replace("\r", "");
				} else if (element instanceof Entry) {
					if (columnIndex == 0)
						return "Entry (" + ((Entry) element).getOperation() + ")";
					else
						return ""; //$NON-NLS-1$

				} else if (element instanceof SchemaConfig) {
					if (columnIndex == 0)
						return ((SchemaConfig) element).getShortName();
					else
						return ""; //$NON-NLS-1$

				} else if (element instanceof SchemaItemConfig) {
					SchemaItemConfig sic = (SchemaItemConfig) element;
					if (columnIndex == 0)
						return sic.getAttributeName();
					else
						return sic.getSample() == null ? "" : sic.getSample().toString(); //$NON-NLS-1$

				} else if (columnIndex == 1) {
					return "" + element; //$NON-NLS-1$
				} else {
					return super.getColumnText(element, columnIndex);
				}
			}

			@Override
			public boolean hasChildren(Object element) {
				if (element instanceof Attribute) {
					Attribute a = (Attribute) element;
					NodeList nl = a.getChildNodes();
					if (nl.getLength() > 1)
						return true;
					if (nl.getLength() == 0)
						return false;
					return nl.item(0) instanceof Attribute;
				}
				if (element instanceof SchemaItemConfig) {
					return ((SchemaItemConfig) element).getChildSchemaList().size() > 0;
				}
				if (element instanceof SchemaConfig) {
					return true;
				}
				if (element instanceof Entry) {
					return ((Entry)element).size() > 0;
				}
				return false;
			}

		};
		entryDetailsTree.setContentProvider(ecp);
		entryDetailsTree.setLabelProvider(ecp);

		return c;
	}

	/**
	 * Creates the toolbar items for the entry viewer toolbar
	 *
	 */
	protected void createEntryViewerToolbar() {

		discoverAction = new Action() {
			public void run() {
				if (job == null || (job.getState() != Job.RUNNING))
					doConnect();
				else
					job.cancel();
			}
		};
		discoverAction.setText(Messages.getString("DiscoverSchemaWidget.connect"));
		discoverAction.setToolTipText(Messages.getString("DiscoverSchemaWidget.connect.tooltip"));//		discoverAction.setImageDescriptor(getImageDescriptor(ICONS_RUN_GIF));
		addToolbarAction(discoverAction);

		Action selectAllAction = new Action() {
			public void run() {
				toggleAllAttributes(true);
			}
		};
		selectAllAction.setText(Messages.getString("DiscoverSchemaWidget.6")); //$NON-NLS-1$
		selectAllAction.setToolTipText(Messages.getString("DataBrowser.toggle.tooltip")); //$NON-NLS-1$
		if(isMappingAttributes())
			addToolbarAction(selectAllAction);

		Action deselectAllAction = new Action() {
			public void run() {
				toggleAllAttributes(false);
			}
		};
		deselectAllAction.setText(Messages.getString("DiscoverSchemaWidget.8")); //$NON-NLS-1$
		deselectAllAction.setToolTipText(Messages.getString("DataBrowser.toggle.tooltip")); //$NON-NLS-1$
		if(isMappingAttributes())
			addToolbarAction(deselectAllAction);

		getNextAction = new Action() {
			public void run() {
				getNextEntry();
			}
		};
		getNextAction.setText(Messages.getString("DiscoverSchemaWidget.next"));
		getNextAction.setToolTipText(Messages.getString("DiscoverSchemaWidget.next.tooltip"));
		getNextAction.setImageDescriptor(getImageDescriptor(ICONS_CONTINUE));
		addToolbarAction(getNextAction);

		closeConnectionAction = new Action() {
			public void run() {
				closeConnector();
			}
		};
		closeConnectionAction.setText(Messages.getString("DiscoverSchemaWidget.close"));
		closeConnectionAction.setToolTipText(Messages.getString("DiscoverSchemaWidget.close.tooltip"));
		closeConnectionAction.setImageDescriptor(getImageDescriptor(ICONS_CLOSE));
		addToolbarAction(closeConnectionAction);

		Action mergeAttributesAction = new Action(Messages.getString("DataBrowser.16"), Action.AS_CHECK_BOX) {
			public void run() {
				setMergeAttributes(isChecked());
			}
		};
		mergeAttributesAction.setToolTipText(Messages.getString("DataBrowser.17")); //$NON-NLS-1$
		if(isMappingAttributes())
			addToolbarAction(mergeAttributesAction);

	}

	void doConnect() {
		discoverData();
	}

	protected void toggleAllAttributes(boolean map) {
		EntryContentProvider ecp = (EntryContentProvider) entryDetailsTree.getContentProvider();
		ConnectorConfig cc = (ConnectorConfig) getEditingConfig();
		AttributeMapConfig imap = cc.getAttributeMap(true);
		AttributeMapConfig omap = cc.getAttributeMap(false);
		imap.notifyChange(imap, "", MetamergeConfigChange.BEGIN_CHANGES);
		omap.notifyChange(omap, "", MetamergeConfigChange.BEGIN_CHANGES);

		if (map)
			entryDetailsTree.expandAll();
		toggleAllAttributes(ecp, entryDetailsTree.getInput(), map);

		imap.notifyChange(imap, "", MetamergeConfigChange.END_CHANGES);
		omap.notifyChange(omap, "", MetamergeConfigChange.END_CHANGES);
	}

	private void toggleAllAttributes(EntryContentProvider ecp, Object input, boolean map) {
		for (Object element: ecp.getChildren(input) ) {
			((CheckboxTreeViewer)entryDetailsTree).setChecked(element, map);
			if (element instanceof Attribute)
				updateAttributeMap((Attribute)element, map);
			else if (element instanceof SchemaItemConfig)
				updateAttributeMap((SchemaItemConfig)element, map);
			if (ecp.hasChildren(element))
				toggleAllAttributes(ecp, element, map);
		}
	}

	protected void dumpObject(Object obj) {
		StringBuffer buf = new StringBuffer();
		if (obj instanceof Entry)
			buf.append(((Entry) obj).toDeltaString());
		else if (obj instanceof Attribute)
			dumpAttribute((Attribute) obj, buf);
		else
			buf.append("" + obj); //$NON-NLS-1$

		setDetailsData(Messages.getString("DataBrowser.dump"), buf.toString());
	}

	private void dumpAttribute(Attribute attr, StringBuffer buf) {
		for (int i = 0; i < attr.size(); i++) {
			Object val = attr.getValue(i);
			buf.append(attr.getName() + "[" + i + "] (" + val.getClass().getName() + ") = "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			dumpObjectValue(val, buf);
			buf.append("\n\n"); //$NON-NLS-1$
		}

		if (attr.getOper() != Attribute.ATTRIBUTE_REPLACE) {
			buf.append("\n\nAttribute.toDeltaString():\n\n"); //$NON-NLS-1$
			buf.append(attr.toDeltaString());
		}
	}

	private void dumpObjectValue(Object obj, StringBuffer buf) {
		buf.append("" + obj); //$NON-NLS-1$
	}

	/**
	 * Update the attribute map for the selected object (obj).
	 *
	 * @param obj Attribute or SchemaConfigItem
	 * @param checked Add (checked) or Remove the mapping
	 */
	protected void updateAttributeMap(Object obj, boolean checked) {
		ConnectorConfig cc = (ConnectorConfig) getEditingConfig();
		AttributeMapConfig imap = cc.getAttributeMap(true);
		AttributeMapConfig omap = cc.getAttributeMap(false);

		String name = obj instanceof Attribute ? Utils.getScriptName(obj) :
				obj instanceof SchemaItemConfig ? ((SchemaItemConfig) obj).getAttributeName() :
					obj.toString();

		try {
			boolean input = true;
			boolean output = true;
			// -- For schemas we know whether it's input or output
			if(obj instanceof SchemaItemConfig) {
				SchemaConfig sc = (SchemaConfig) Utils.getParentConfig(obj, SchemaConfig.class);
				if(sc != null && sc.getShortName() != null) {
					if(sc.getShortName().indexOf("Input") != -1)
						output = false;
					else
						input = false;
				}
			}

			// -- Remove current input mapping
			if(input)
				imap.removeAttributeMapItem(name);

			// -- Remove current output mapping
			if(output)
				omap.removeAttributeMapItem(name);

			// -- Add mapping for input/output
			if (checked) {
				AttributeMapItem map;
				String longName = Utils.getScriptName(obj);

				if(input) {
					map = cc.getAttributeMap(true).newAttributeMapItem(name);
					map.setScript(Utils.getScript("conn", longName)); //$NON-NLS-1$
				}
				if(output) {
					map = cc.getAttributeMap(false).newAttributeMapItem(name);
					map.setScript(Utils.getScript("work", longName)); //$NON-NLS-1$
				}
			}

		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e);
		}
	}

//	String getLongName(Object obj) {
//		if (obj instanceof Attribute)
//			return getLongName((Attribute) obj);
//		if (obj instanceof SchemaItemConfig)
//			return getLongName((SchemaItemConfig) obj);
//		return obj.toString();
//	}
//
//	String getLongName(Attribute attr) {
//		StringBuilder ret = new StringBuilder(getSafe(attr.getName()));
//		Node n = attr.getParentNode();
//		while (n instanceof Attribute) {
//			ret.insert(0, '.');
//			ret.insert(0, getSafe(((Attribute) n).getName()));
//			n = n.getParentNode();
//		}
//		return ret.toString();
//	}
//
//	String getSafe(String s) {
//		if (Utils.isIdentifier(s))
//			return s;
//		else
//			return "getAttribute(\"" + s + "\")";
//	}
//
//	String getLongName(SchemaItemConfig sic) {
//
//		Name name = sic.getName();
//		StringBuilder ret = new StringBuilder();
//		for (int i = 0; i < name.size(); i++) {
//			if (i > 0)
//				ret.append(".");
//			ret.append(getSafe(name.get(i)));
//		}
//
//		return ret.toString();
//	}

	/**
	 * Updates the check for each Attribute in the entry tree viewer to show
	 * which attributes are mapped 1-1.
	 *
	 * @param entry
	 */
	private void updateCheckState(Entry entry) {
		if (entry == null)
			return;
		if (!(entryDetailsTree instanceof CheckboxTreeViewer))
			return;

		ConnectorConfig cc = (ConnectorConfig) getEditingConfig();
		for (String str : entry.getAttributeNames()) {

			boolean check = false;
			if (Utils.isInputConnector(cc)&& cc.getAttributeMap(true).hasAttributeMapItem(str) )
				check = true;
			if (Utils.isOutputConnector(cc)&& cc.getAttributeMap(false).hasAttributeMapItem(str) )
				check = true;
			if ( check )
			{
				Attribute attr = entry.getAttribute(str);
				((CheckboxTreeViewer)entryDetailsTree).setChecked(attr, true);
			}
		}
	}

	/**
	 * Updates the entry viewer with an Entry object
	 *
	 * @param entry
	 */
	protected void setNextEntry(Entry entry) {

		// -- Compute the deepest level of expansion
		int depth = 0;
		for(TreePath tp : entryDetailsTree.getExpandedTreePaths()) {
			if(tp.getSegmentCount() > depth)
				depth = tp.getSegmentCount();
		}

		if (isMergeAttributes()&& entryDetailsTree.getInput() instanceof Entry) {
			Entry old = (Entry) entryDetailsTree.getInput();
			if (old != null && entry != null)
				entry.merge(old, true);
			else if (entry == null)
				entry = old;
		}

		// -- Update schema with data from entry

		if (entry != null) {
			ConnectorConfig config = (ConnectorConfig) getEditingConfig();
			try {
				SchemaUtils.convertEntryToSchema(entry, config, true);
			} catch (Exception ex) {
				EclipseAppender.logerror(ex.getMessage(), ex);
			}
			try {
				SchemaUtils.convertEntryToSchema(entry, config, false);
			} catch (Exception ex) {
				EclipseAppender.logerror(ex.getMessage(), ex);
			}
		}

		entryDetailsTree.setInput(entry);
		updateCheckState(entry);

		entryDetailsTree.expandToLevel(depth+1);
	}

	/**
	 * Creates the composite where details are provided. This is a TextViewer by
	 * default in a tab labeled "Details".
	 *
	 * @param parent
	 */
	protected void createDetailsPanel(Composite parent) {
		detailsTabFolder = new TabFolder(parent, SWT.TOP);

		// -- default details panel for system info etc
		detailsPanel = new SourceViewer(detailsTabFolder, null, SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL);
		detailsPanel.getTextWidget().setFont(JFaceResources.getTextFont());

		new TextEditorContextMenu(detailsPanel.getTextWidget(), detailsPanel.getFindReplaceTarget());

		setDetailsTabContents(Messages.getString("DataBrowser.32"), detailsPanel.getTextWidget()); //$NON-NLS-1$

		// -- Connection panel for current configuration
		resetConnectorForm();
	}

	public void resetConnectorForm() {
		if (connectorWidget != null) {
			connectorWidget.dispose();
		}

		connectorWidget = new RawConnectorWidget(detailsTabFolder, SWT.NONE, ((ConnectorConfig) getEditingConfig())
				.getConnectionConfig(), true, true);
		setDetailsTabContents(Messages.getString("ConnectorTreeUI.Localized.Connection"), connectorWidget); //$NON-NLS-1$
		((ConnectorConfig) getEditingConfig()).getConnectionConfig().addListener(this);
	}

	/**
	 * This method changes the content of the TabItem with the specific tab
	 * title. If no such tab exists one will be created.
	 *
	 * @param tabTitle
	 * @param content
	 */
	protected void setDetailsTabContents(String tabTitle, Control content) {
		setDetailsTabContents(tabTitle, content, false);
	}

	/**
	 * This method changes the content of the TabItem with the specific tab
	 * title. If no such tab exists one will be created.
	 *
	 * @param tabTitle
	 * @param content
	 */
	protected void setDetailsTabContents(String tabTitle, Control content, boolean show) {
		for (TabItem item : detailsTabFolder.getItems()) {
			if (tabTitle.equals(item.getText())) {
				item.setControl(content);
				return;
			}
		}

		TabItem tabItem = new TabItem(detailsTabFolder, SWT.LEFT);
		tabItem.setText(tabTitle);
		tabItem.setControl(content);
		if(show)
			detailsTabFolder.setSelection(tabItem);
	}

	public TabFolder getDetailsTabFolder() {
		return detailsTabFolder;
	}

	/**
	 * Returns the default text viewer for the details panel.
	 *
	 * @return the SourceViewer
	 */
	public SourceViewer getDetailsPanel() {
		return detailsPanel;
	}

	/**
	 * Sets the contents of the default details component. The default details
	 * is a text viewer to show the data.
	 *
	 * @param data
	 */
	protected void setDetailsData(String title, String data) {
		setDetailsData(title, data, false);
	}

	/**
	 * Sets the contents of the default details component. The default details
	 * is a text viewer to show the data.
	 *
	 * @param data
	 */
	protected void setDetailsData(String title, String data, boolean show) {
		detailsPanel.setDocument(new Document("[" + title + "]\n" + data));
		if(show)
			detailsTabFolder.setSelection(0);
	}

	/**
	 * Returns the content provider for the navigator
	 *
	 * @return the IContentProvider
	 */
	protected abstract IContentProvider getNavigatorContentProvider();

	/**
	 * Returns the label provider for the navigator
	 *
	 * @return IBaseLabelProvider
	 */
	protected abstract IBaseLabelProvider getNavigatorLabelProvider();

	/**
	 * Returns the input for the navigator
	 *
	 * @return the input object
	 */
	protected abstract Object getNavigatorInput();

	/**
	 * This method is called initially to perform discovery of the connector.
	 * When the job completes it will refresh all viewers with new inputs. This
	 * method is also called from a non-UI thread so updating the UI must be
	 * handled correctly.
	 */
	protected abstract void doInitialDiscovery() throws Exception;

	/**
	 * Called when the selection in the navigator tree has changed. Browser
	 * should update the details panel with relevant info for the selection.
	 *
	 * @param event
	 */
	protected abstract void handleNavigatorSelectionChanged(SelectionChangedEvent event);

	protected String getNavigatorFormText() {
		return Messages.getString("DataBrowser.34"); //$NON-NLS-1$
	}

	public boolean isMappingAttributes() {
		return (getStyle() & SWT.READ_ONLY) == 0;
	}
}
