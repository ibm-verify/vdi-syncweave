/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.easyetl.widgets;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.ibm.di.config.base.InternalSchema;
import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.AttributeMapItem;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.FormConfig;
import com.ibm.di.config.interfaces.FormItemConfig;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.di.config.interfaces.MetamergeConfigChangeListener;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.ParserConfig;
import com.ibm.di.config.interfaces.SchemaConfig;
import com.ibm.di.config.interfaces.SchemaItemConfig;
import com.ibm.di.connector.ConnectorInterface;
import com.ibm.di.entry.Entry;
import com.ibm.di.function.SystemFunctions;
import com.ibm.tdi.eclipse.Activator;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.widget.FormWidget2;
import com.ibm.tdi.eclipse.widget.ParserWidget;

public class ConnectorWidget extends Composite {
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private FormToolkit tk;

	private Form form;

	private ConnectorConfig cc;

	protected TableViewer table;

	private Hashtable<String, Control> controls = new Hashtable<String, Control>();

	private ReadEntryJob connectorJob;

	private FormConfig formConfig;

	private FormWidget2 formWidget;

	private ParserWidget parserWidget;

	private MetamergeConfigChangeListener listener;

	private FormWidget2 deltaWidget;

	private boolean didCheckAttributes = false;

	private TabItem parserItem;

	public ConnectorWidget(Composite parent, ConnectorConfig cc) {
		super(parent, 0);
		this.cc = cc;
		try {
			formConfig = (FormConfig) MetamergeConfigFactory.lookup(null, "system:/Forms/"
					+ Utils.getFormName(cc.getConnectionConfig()));
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e, getShell());
			return;
		}
		setLayout(new FillLayout());
		tk = new FormToolkit(parent.getDisplay());
		form = tk.createForm(this);
		tk.decorateFormHeading(form);
		form.getBody().setLayout(createLayout());
		createBody(form.getBody());
	}

	@Override
	public void dispose() {
		if(connectorJob != null)
			connectorJob.terminate();
		if (formWidget != null)
			formWidget.dispose();
		if (parserWidget != null)
			parserWidget.dispose();
		if (listener != null) {
			cc.getConnectionConfig().removeListener(listener);
			cc.getParserConfig().removeListener(listener);
			listener = null;
		}
		super.dispose();
	}

	/**
	 * Returns the connector configuration this widget operates one
	 * @return
	 */
	public ConnectorConfig getCc() {
		return cc;
	}

	/**
	 * Creates the default table viewer in the widget's form
	 * @return
	 */
	public TableViewer createDefaultTableViewer() {
		return createDefaultTableViewer(form.getBody());
	}

	/**
	 * Creates the default table viewer
	 * 
	 * @param parent The parent composite
	 * @return
	 */
	public TableViewer createDefaultTableViewer(Composite parent) {

		Composite searchc = new Composite(parent, SWT.NONE);
		searchc.setLayout(new GridLayout(2, false));
		searchc.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		new Label(searchc, SWT.LEFT).setText(Messages.getString("WorkEntryWidget.9"));

		final Text filter = new Text(searchc, SWT.BORDER | SWT.SINGLE);
		filter.setLayoutData(new GridData(300, SWT.DEFAULT));
		filter.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				table.refresh();
			}
		});

		ViewerFilter viewerFilter = new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				SchemaItemConfig sic = (SchemaItemConfig) element;
				String str = filter.getText();
				if (sic == null || sic.getShortName() == null)
					return false;
				if (str.length() == 0 || sic.getShortName().toLowerCase().contains(str.toLowerCase()))
					return true;
				else
					return false;
			}
		};

		if (Utils.isInputConnector(cc))
			table = createTableViewer(parent, SWT.BORDER | SWT.CHECK);
		else
			table = createTableViewer(parent, SWT.BORDER);

		table.setFilters(new ViewerFilter[] { viewerFilter });

		TableColumn tc = new TableColumn(table.getTable(), SWT.LEFT);
		tc.setText(Messages.getString("SchemaEditor.1"));
		tc.setWidth(200);

		tc = new TableColumn(table.getTable(), SWT.LEFT);
		tc.setText(Messages.getString("BranchWidget.8"));
		tc.setWidth(400);

		// -- make headers visible
		table.getTable().setHeaderVisible(true);

		table.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof List<?>)
					return ((List<?>) inputElement).toArray();
				if (inputElement instanceof SchemaConfig) {
					SchemaConfig sc = (SchemaConfig) inputElement;
					List<String> itemNames = sc.getItemNames();
					SchemaItemConfig[] list = new SchemaItemConfig[itemNames.size()];
					for (int i = 0; i < list.length; i++)
						list[i] = sc.getItem(itemNames.get(i));
					return list;
				}
				return null;
			}

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}

		});

		table.setLabelProvider(new ITableLabelProvider() {

			public Image getColumnImage(Object element, int columnIndex) {
				return null;
			}

			public String getColumnText(Object element, int columnIndex) {
				if (element instanceof Entry) {
					Entry e = (Entry) element;
					if (columnIndex == 0)
						return e.getString("name");
					else
						return e.getString("value");
				} else if (element instanceof SchemaItemConfig) {
					SchemaItemConfig sic = (SchemaItemConfig) element;
					if (columnIndex == 0)
						return sic.getAttributeName();
					else
						return sic.getSample() == null ? "" : sic.getSample().toString();
				}
				return "" + element;
			}

			public boolean isLabelProperty(Object element, String property) {
				return false;
			}

			public void addListener(ILabelProviderListener listener) {
			}

			public void removeListener(ILabelProviderListener listener) {
			}

			public void dispose() {
			}
		});

		// -- only for checked table
		// DiscoverSchemaWidget.6=Select All
		// DiscoverSchemaWidget.7=Select all attributes
		// DiscoverSchemaWidget.8=Clear All
		// DiscoverSchemaWidget.9=Uncheck all attributes
		if (table instanceof CheckboxTableViewer) {
			Composite c = new Composite(parent, SWT.NONE);
			c.setLayout(new RowLayout(SWT.HORIZONTAL));
			c.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

			// -- select all
			Button butt = new Button(c, SWT.PUSH);
			butt.setText(Messages.getString("DiscoverSchemaWidget.6"));
			butt.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					((CheckboxTableViewer) table).setAllChecked(true);
					didCheckAttributes = false;
				}
			});

			// -- clear all
			butt = new Button(c, SWT.PUSH);
			butt.setText(Messages.getString("DiscoverSchemaWidget.9"));
			butt.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					((CheckboxTableViewer) table).setAllChecked(false);
					didCheckAttributes = true;
				}
			});
		}

		// -- update table input
		SchemaConfig sc = cc.getSchema(Utils.isEntryFeedConnector(cc));
		if (sc != null)
			table.setInput(sc);

		// -- update check state
		if (table instanceof CheckboxTableViewer) {
			CheckboxTableViewer ctable = (CheckboxTableViewer) table;
			AttributeMapConfig amc = cc.getAttributeMap(Utils.isEntryFeedConnector(cc));
			for (String str : amc.getAttributeNames()) {
				SchemaItemConfig sic = sc.getItem(str);
				if (sic != null)
					ctable.setChecked(sic, true);
			}

			ctable.addCheckStateListener(new ICheckStateListener() {
				public void checkStateChanged(CheckStateChangedEvent event) {
					if (event.getElement() instanceof SchemaItemConfig) {
						mapAttribute(((SchemaItemConfig) event.getElement()).getAttributeName(), event.getChecked());
						didCheckAttributes = true;
					}
				}
			});
		}

		return table;
	}

	protected void mapAttribute(String name, boolean map) {
		AttributeMapConfig amc = cc.getAttributeMap(Utils.isEntryFeedConnector(cc));
		if (map) {
			if (amc.hasAttributeMapItem(name))
				return;
			try {
				AttributeMapItem ami = amc.newAttributeMapItem(name);
				ami.setSimple(name);
				amc.setAttributeMapItem(ami);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			amc.removeAttributeMapItem(name);
		}
	}

	public Combo createCombo(boolean editable) {
		return new Combo(form.getBody(), SWT.DROP_DOWN | (editable ? 0 : SWT.READ_ONLY));
	}

	public Group createGroup(String title, Object layoutData) {
		Group group = new Group(form.getBody(), SWT.SHADOW_IN);
		if (title != null)
			group.setText(title);
		if (layoutData != null)
			group.setLayoutData(layoutData);
		return group;
	}

	public Text createTextField(String value, Object layoutData) {
		Text text = tk.createText(form.getBody(), value, SWT.LEFT);
		if (layoutData != null)
			text.setLayoutData(layoutData);
		return text;

	}

	public Text createTextField(String value, Object layoutData, boolean password) {
		if (!password)
			return createTextField(value, layoutData);

		Text text = tk.createText(form.getBody(), value, SWT.LEFT | SWT.PASSWORD);
		if (layoutData != null)
			text.setLayoutData(layoutData);
		return text;

	}

	public Button createRadio(String text, String tooltip) {
		Button b1 = tk.createButton(form.getBody(), text, SWT.RADIO);
		if (tooltip != null)
			b1.setToolTipText(tooltip);
		return b1;
	}

	public Button createCheckBox(String text, String tooltip) {
		Button b1 = tk.createButton(form.getBody(), text, SWT.CHECK);
		if (tooltip != null)
			b1.setToolTipText(tooltip);
		return b1;
	}

	public Button createPushButton(String text, String tooltip) {
		Button b1 = tk.createButton(form.getBody(), text, SWT.PUSH);
		if (tooltip != null)
			b1.setToolTipText(tooltip);
		return b1;
	}

	public Label createLabel(String text) {
		return tk.createLabel(form.getBody(), text);
	}

	public TableViewer createTableViewer(Composite parent, int flags) {
		if ((flags & SWT.CHECK) > 0)
			return CheckboxTableViewer.newCheckList(parent, SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		else
			return new TableViewer(parent, SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
	}

	public String getParam(String param) {
		return cc.getConnectionConfig().getStringParameter(param);
	}

	protected void closeConnector() {
		if (connectorJob != null) {
			connectorJob.terminate();
			connectorJob = null;
		}
	}

	protected ReadEntryJob readConnectorEntry() {
		if (connectorJob != null)
			connectorJob.terminate();

		ReadEntryJob job = new ReadEntryJob("ETLReader");
		job.addJobChangeListener(new IJobChangeListener() {
			public void sleeping(IJobChangeEvent event) {
			}

			public void scheduled(IJobChangeEvent event) {
			}

			public void running(IJobChangeEvent event) {
			}

			public void done(IJobChangeEvent event) {
				for (Listener l : completeListener)
					l.handleEvent(new Event());
			}

			public void awake(IJobChangeEvent event) {
			}

			public void aboutToRun(IJobChangeEvent event) {
			}
		});
		job.schedule();
		connectorJob = job;
		return job;
	}

	protected void updateTableContent(Entry e) {
		final SchemaConfig sc = cc.getSchema(Utils.isEntryFeedConnector(cc));
		if (e == null)
			return;
		for (String str : e.getAttributeNames()) {
			try {
				SchemaItemConfig sic = sc.getItem(str);
				if (sic == null)
					sic = sc.newItem(str);
				sic.setSample(e.getString(str));
			} catch (Exception err) {
				// No problem if the item already exists
				SystemFunctions.doNothing();
			}
		}
		
		getDisplay().syncExec(new Runnable() {
			public void run() {
				if (table != null && !table.getTable().isDisposed())
					table.setInput(sc);
			}
		});
	}

	protected void addModifyListener(Text control, String param, BaseConfiguration config) {
		final String p = param;
		final BaseConfiguration bc = config;
		control.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				bc.setParameter(p, ((Text) e.widget).getText());
				closeConnector();
			}
		});
	}

	public void setField(String key, Control control) {
		controls.put(key, control);
	}

	public Control getField(String key) {
		return controls.get(key);
	}

	public String getLabel(String field) {
		if (formConfig == null)
			return field;

		FormItemConfig fic = formConfig.getFormItem(field);
		if (fic == null)
			return field;

		String str = fic.getLabel();
		if (str == null)
			return field;

		return str;
	}

	public String getLabel(String clazz, String field) {
		try {
			FormConfig fc = (FormConfig) MetamergeConfigFactory.lookup(null, "system:/Forms/" + clazz);
			FormItemConfig fic = fc.getFormItem(field);
			return fic.getLabel();
		} catch (Exception e) {
			return "***" + field;
		}
	}

	protected Layout createLayout() {
		return new GridLayout(1, true);
	}

	protected void createBody(Composite parent) {
		try {
			SashForm sash = new SashForm(parent, SWT.VERTICAL);

			final TabFolder tabs = new TabFolder(sash, SWT.TOP);
			TabItem item = new TabItem(tabs, SWT.LEFT);
			formWidget = new FormWidget2(tabs, 0, cc.getConnectionConfig(), null, true);
			item.setText(Messages.getString("ConnectorTreeUI.Localized.Connection"));
			item.setControl(formWidget);
			
			if (Utils.hasParserRequirements(cc)) {
				parserItem = new TabItem(tabs, SWT.LEFT);
				parserWidget = new ParserWidget(tabs, SWT.NONE, cc.getParserConfig());
				parserItem.setText(Messages.getString("ConnectorTreeUI.Localized.Parser"));
				parserItem.setControl(parserWidget);
			}

			if (Utils.isInputConnector(cc)) {
				deltaWidget = new FormWidget2(tabs, SWT.NONE, cc.getDeltaConfig(), "Delta Configuration");
				item = new TabItem(tabs, SWT.LEFT);
				item.setText(Messages.getString("ConnectorTreeUI.Localized.Delta"));
				item.setControl(deltaWidget);
			}

			Composite c = new Composite(sash, SWT.NONE);
			c.setLayout(new GridLayout(1, false));
			Label l = new Label(c, SWT.LEFT);
			l.setText(Messages.getString("ETL.ConnectorWidget.Select.Attributes"));
			table = createDefaultTableViewer(c);
			table.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));

			sash.setWeights(new int[] { 60, 40 });
			sash.setLayoutData(new GridData(GridData.FILL_BOTH));

			listener = new MetamergeConfigChangeListener() {
				private boolean updating;
				public void configurationChanged(MetamergeConfigChange mcc) {
					if (updating)
						return;
					updating = true;
					if(mcc.getSource() instanceof ParserConfig && ("setInheritsFrom".equals(mcc.getUserObject()) || InternalSchema.INHERITS_FROM.equals(mcc.getKey()))) {
						if(parserWidget != null)
							parserWidget.dispose();
						parserWidget = new ParserWidget(tabs, SWT.NONE, cc.getParserConfig());
						parserItem.setControl(parserWidget);
					} else {
						//TODO: It cannot be correct to remove all schema items for any change?
						SchemaConfig sc = cc.getSchema(Utils.isEntryFeedConnector(cc));
						for (String str : sc.getItemNames()) {
							sc.removeItem(str);
						}
						updateTableContent(new Entry());
						closeConnector();
						notifyCompleteListeners();
					}
					updating = false;
				}
			};
			cc.getConnectionConfig().addListener(listener);
			cc.getParserConfig().addListener(listener);

		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e, getShell());
		}
	}

	private class ReadEntryJob extends Job {

		private ConnectorInterface conn;

		public ReadEntryJob(String name) {
			super(name);
		}

		public void terminate() {
			if (conn != null) {
				try {
					conn.terminate();
				} catch (Exception e) {
					SystemFunctions.doNothing();
				}
				conn = null;
			}
		}

		@SuppressWarnings("unchecked")
		protected IStatus run(IProgressMonitor monitor) {
			try {
				Object schema = null;
				if (conn == null) {
					conn = SystemFunctions.loadConnector(getCc());
					conn.initialize(null);
					if(monitor.isCanceled())
						return Status.OK_STATUS;
					conn.selectEntries();
					if(monitor.isCanceled())
						return Status.OK_STATUS;
					schema = conn.querySchema(null);
				}
				if(monitor.isCanceled())
					return Status.OK_STATUS;
				Entry e = conn.getNextEntry();
				if (e == null) {
					e = new Entry();
					terminate();
				}

				if (schema instanceof Vector) {
					Vector<Entry> v = (Vector<Entry>) schema;
					for (Entry entry : v) {
						String name = entry.getString("name");
						if (e.getAttribute(name) == null)
							e.setAttribute(name, "");
					}
				}
				if(monitor.isCanceled())
					return Status.OK_STATUS;
				updateTableContent(e);
			} catch (Exception e) {
				return new Status(Status.ERROR, Activator.TDI_PLUGIN_ID, Status.ERROR, e.toString(), e);
			}
			return Status.OK_STATUS;
		}

	}

	private ArrayList<Listener> completeListener = new ArrayList<Listener>();

	/**
	 * Adds a listener to receive events when the connector's canClose state has
	 * changed.
	 * 
	 * @param listener
	 */
	public void addCompleteListener(Listener listener) {
		if (!completeListener.contains(listener))
			completeListener.add(listener);
	}

	/**
	 * Removes listener from the complete listener list
	 * 
	 * @param listener
	 */
	public void removeCompleteListener(Listener listener) {
		completeListener.remove(listener);
	}

	/**
	 * Notifies complete listeners that the canClose state has changed.
	 * 
	 */
	protected void notifyCompleteListeners() {
		for (Listener l : completeListener)
			l.handleEvent(new Event());
	}

	/**
	 * Returns true if the connector either has schema items present or that the
	 * user pressed the test connection button.
	 * 
	 * @return
	 */
	public boolean canClose() {
		Object[] items = ((IStructuredContentProvider) table.getContentProvider()).getElements(table.getInput());
		return connectorJob != null || (items != null && items.length > 0);
	}

	/**
	 * Creates an attribute map for all items in the schema list if the user did
	 * not manually check/uncheck the list of available attributes. If the
	 * connector has attribute map items already we don't map anything.
	 */
	public void mapAllAttributes() {
		if (didCheckAttributes)
			updateAttributeMap();
		
		if (cc.getAttributeMap(Utils.isEntryFeedConnector(cc)).getAttributeNames().size() > 0)
			return;

		Object[] items = ((IStructuredContentProvider) table.getContentProvider()).getElements(table.getInput());
		for (Object item : items) {
			if (item instanceof SchemaItemConfig) {
				mapAttribute(((SchemaItemConfig) item).getAttributeName(), true);
			} else if (item instanceof Entry) {
				for (String str : ((Entry) item).getAttributeNames())
					mapAttribute(str, true);
			}
		}
	}

	private void updateAttributeMap() {
		if(table instanceof CheckboxTableViewer) {
			CheckboxTableViewer ct = (CheckboxTableViewer) table;
			AttributeMapConfig map = cc.getAttributeMap(true);
			Object[] items = ((IStructuredContentProvider) table.getContentProvider()).getElements(table.getInput());
			for (Object item : items) {
				if(!ct.getChecked(item))
					continue;
				if (item instanceof SchemaItemConfig) {
					SchemaItemConfig sic = (SchemaItemConfig) item;
					if(!map.hasAttributeMapItem(sic.getAttributeName()))
						mapAttribute(((SchemaItemConfig) item).getAttributeName(), true);
				} else if (item instanceof Entry) {
					for (String str : ((Entry) item).getAttributeNames()) {
						if(!map.hasAttributeMapItem(str))
							mapAttribute(str, true);
					}
				}
			}
		}
	}
}
