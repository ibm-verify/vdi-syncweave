/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.widget;

import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;


import com.ibm.di.config.base.InternalSchema;
import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.ALMappingConfig;
import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.AttributeMapItem;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.FormConfig;
import com.ibm.di.config.interfaces.FormItemConfig;
import com.ibm.di.config.interfaces.FormSection;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.LoopConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.di.config.interfaces.MetamergeConfigChangeListener;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.SchemaConfig;
import com.ibm.di.config.interfaces.SchemaItemConfig;
import com.ibm.di.connector.Connector;
import com.ibm.di.connector.ConnectorInterface;
import com.ibm.di.entry.Entry;
import com.ibm.di.fc.FunctionInterface;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.server.ConnectorMode;
import com.ibm.di.util.SchemaUtils;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.TDI;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.actions.AddSchemaItemAction;
import com.ibm.tdi.eclipse.actions.ChangeInheritanceAction;
import com.ibm.tdi.eclipse.actions.CopyConfigAction;
import com.ibm.tdi.eclipse.actions.CutConfigAction;
import com.ibm.tdi.eclipse.actions.PasteConfigAction;
import com.ibm.tdi.eclipse.actions.SetDesignObjectAction;
import com.ibm.tdi.eclipse.commands.CommandHandlerProxy;
import com.ibm.tdi.eclipse.commands.CommandID;
import com.ibm.tdi.eclipse.editors.BaseEditor;
import com.ibm.tdi.eclipse.editors.SchemaEditor;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.natures.TDINature;
import com.ibm.tdi.eclipse.providers.AttributeContentProvider;
import com.ibm.tdi.eclipse.util.TDIToolBar;
import com.ibm.tdi.eclipse.util.TDIToolBar.PullDownButton;

public class DiscoverSchemaWidget extends BaseWidget implements MetamergeConfigChangeListener {

	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private QueryJob job;
	private ConnectorConfig cc;
	private Action discoverAction;

	private TDIToolBar toolbar;

	private TreeViewer schema;

	private AttributeContentProvider provider;

	private IAction selectAllAction;

	private IAction deselectAllAction;

	private IAction deleteAllAction;

	private Action connectAction;

	private Action nextEntryAction;

	private Action closeConnectionAction;

	private int mappingMode;

	private boolean loopParamsMapping = false;

	private PasteAction pasteAction;

	private CommandHandlerProxy discoverProxy;
	
	private final static String LDAP_CONNECTOR = "com.ibm.di.connector.LDAPConnector";
	private final static String LDAP_SIZE_LIMIT = "ldapSizeLimit";

	private static Color BLUE = Display.getDefault().getSystemColor(SWT.COLOR_BLUE);
	private static Color BLACK= Display.getDefault().getSystemColor(SWT.COLOR_BLACK);


	public DiscoverSchemaWidget(Composite parent, int style, ConnectorConfig editingConfig, BaseEditor editor) {
		this(parent, style, editingConfig, editor, 1);
	}

	public DiscoverSchemaWidget(Composite parent, int style, ConnectorConfig editingConfig, BaseEditor editor, int mappingMode) {
		super(parent, style, editingConfig, editor);
		this.mappingMode = mappingMode;
		setLayout(new FillLayout());
		createUI(this);
	}

	private void createUI(Composite parent) {
		cc = (ConnectorConfig) getEditingConfig();

		// -- Output map for loop connector should have connector params as schema
		setLoopParamMapping();

		Form f = createForm(parent, null);

		toolbar = new TDIToolBar(f, SWT.LEFT|SWT.SINGLE);
		toolbar.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));

		f.getBody().setLayout(new FillLayout());
		createSchemaTree(f.getBody());

		// -- schema title
		Label title = new Label(toolbar, SWT.LEFT);
		if(loopParamsMapping)
			title.setText(Messages.getString("LoopConfig.connectorParams.label"));
		else
			title.setText(Messages.getString("DiscoverSchemaWidget.0"));
		title.setLayoutData(new GridData(SWT.DEFAULT, SWT.CENTER, false, false));

		//
		// -- Discover ToolBar button
		//
		if (cc != null && !(cc instanceof ALMappingConfig)) {
			discoverAction = new Action() {
				public String getText() {
					return Messages.getString("DiscoverSchemaWidget.5"); //$NON-NLS-1$
				}

				public void run() {
					runQueryJob(false);
				}
			};
			enableAction(discoverAction, true);

			// Add a command handler to enable key bindings
			// This will create too many handlers in the AssemblyLineEditor, disable it for now.
//			if (getEditor() != null) {
//				discoverProxy = new CommandHandlerProxy(getEditor().getSite(), discoverAction, CommandID.DISCOVER_SCHEMA);
//			}
		}

		//
		// --
		//
		if (isChecked()) {
			//
			// -- Check all ToolBar button
			//
			selectAllAction = new Action() {
				public String getText() {
					return Messages.getString("DiscoverSchemaWidget.6"); //$NON-NLS-1$
				}

				public String getToolTipText() {
					return Messages.getString("DiscoverSchemaWidget.7"); //$NON-NLS-1$
				}

				public void run() {
					if (schema instanceof CheckboxTreeViewer)
						((CheckboxTreeViewer) schema).setSubtreeChecked(schema.getTree().getItems(), true);
					else
						schema.getTree().selectAll();
				}
			};

			//
			// -- Clear All ToolBar button
			//
			deselectAllAction = new Action() {
				public String getText() {
					return Messages.getString("DiscoverSchemaWidget.8"); //$NON-NLS-1$
				}

				public String getToolTipText() {
					return Messages.getString("DiscoverSchemaWidget.9"); //$NON-NLS-1$
				}

				public void run() {
					if (schema instanceof CheckboxTreeViewer) {
						((CheckboxTreeViewer) schema).setSubtreeChecked(schema.getTree().getItems(), false);
					} else {
						schema.getTree().deselectAll();
					}
				}
			};

			//
			// -- Delete All ToolBar button
			//
			deleteAllAction = new Action() {
				public String getText() {
					return Messages.getString("DiscoverSchemaWidget.10"); //$NON-NLS-1$
				}

				public String getToolTipText() {
					return Messages.getString("DiscoverSchemaWidget.11"); //$NON-NLS-1$
				}

				public void run() {
					if (Utils.isInputConnector(cc)) {
						for (Object str : cc.getSchema(true).getItemNames())
							cc.getSchema(true).removeItem(str);
					}
					if (Utils.isOutputConnector(cc)) {
						for (Object str : cc.getSchema(false).getItemNames())
							cc.getSchema(false).removeItem(str);
					}
					schema.refresh();
				}
			};
		}

		//
		// -- Connect & Discover
		//
		connectAction = new Action() {
			public String getText() {
				return Messages.getString("DiscoverSchemaWidget.connect"); //$NON-NLS-1$
			}

			public String getToolTipText() {
				return Messages.getString("DiscoverSchemaWidget.connect.tooltip"); //$NON-NLS-1$
			}

			public void run() {
				runQueryJob(false);
			}
		};
		enableAction(connectAction, ! ConnectorConfig.SERVER_MODE.equals(cc.getMode()));

		//
		// -- Next
		//
		nextEntryAction = new Action() {
			public String getText() {
				return Messages.getString("DiscoverSchemaWidget.next"); //$NON-NLS-1$
			}

			public String getToolTipText() {
				return Messages.getString("DiscoverSchemaWidget.next.tooltip"); //$NON-NLS-1$
			}

			public void run() {
				runQueryJob(true);
			}
		};
		enableAction(nextEntryAction, false);

		//
		// -- Close
		//
		closeConnectionAction = new Action() {
			public String getText() {
				return Messages.getString("DiscoverSchemaWidget.close"); //$NON-NLS-1$
			}

			public String getToolTipText() {
				return Messages.getString("DiscoverSchemaWidget.close.tooltip"); //$NON-NLS-1$
			}

			public void run() {
				closeConnection();
			}
		};
		enableAction(closeConnectionAction, false);

		// -- for the loop params output map we don't show connect/next etc
		if(loopParamsMapping)
			return;

		for (IAction action : new IAction[] { selectAllAction, deselectAllAction, deleteAllAction, connectAction, nextEntryAction,
				closeConnectionAction }) {
			if (action == null)
				continue;

			toolbar.add(action);
		}

		PullDownButton more = toolbar.addMoreButton(Messages.getString("ComponentOptionsWidget.more"), null, null);
		more.addMenuOption(discoverAction);
		more.addMenuOption(new AddSchemaItemAction(getShell(), cc.getSchema(mappingMode == WorkMapWidget.MAP_MODE_INPUT)));
		more.addMenuOption(new ChangeInheritanceAction(cc.getSchema(mappingMode == WorkMapWidget.MAP_MODE_INPUT)));
		more.addMenuOption(new SetDesignObjectAction(cc.getSchema(mappingMode == WorkMapWidget.MAP_MODE_INPUT)));
	}

	public TDIToolBar getToolbar() {
		return toolbar;
	}

	/**
	 * Check if the connector is in a LoopConfig, and is in output mode.
	 */
	private void setLoopParamMapping() {
		loopParamsMapping = (mappingMode == WorkMapWidget.MAP_MODE_OUTPUT) && isLoopConnector();
	}

	protected void closeConnection() {
		if (job != null)
			job.dispose();
		job = null;
		enableAction(closeConnectionAction, false);
		enableAction(nextEntryAction, false);
		enableAction(connectAction, true);
		enableAction(discoverAction, true);
	}

	private void enableAction(Action action, boolean enable) {
		if (action == null)
			return;

		action.setEnabled(enable);
	}

	protected void runQueryJob(boolean getnext) {
		cc = (ConnectorConfig) getEditingConfig();

		//
		// Save current schema selection
		//
		saveSchemaSelection();

		try {
			if (!isJobConnected()) {
				job = new QueryJob((ConnectorConfig) cc.getClone(), cc.getShortName(), getnext); //$NON-NLS-1$
			} else {
				job.setIsGetNext(getnext);
			}
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e, getShell());
			return;
		}

		enableAction(discoverAction, false);
		enableAction(connectAction, false);
		enableAction(nextEntryAction, false);
		enableAction(closeConnectionAction, false);

		ProgressMonitorDialog pmd = new ProgressMonitorDialog(getShell()) {
			protected void cancelPressed() {
				super.cancelPressed();
				try {
					job.terminate();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		try {
			pmd.run(true, true, job);
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e, getShell());
		}
	}

	private boolean isJobConnected() {
		if (job == null)
			return false;
		else
			return job.isConnected();
	}

//	private void clearSchema(SchemaConfig sc) {
//		//
//		// Clear current schema
//		//
//		sc.notifyChange(sc, "", MetamergeConfigChange.BEGIN_CHANGES);
//
//		for (Object obj : sc.getItemNames())
//			sc.removeItem(obj);
//
//		sc.notifyChange(sc, "", MetamergeConfigChange.END_CHANGES);
//	}
//
	private ArrayList<String> savedSchemaSelection;
	private void saveSchemaSelection() {
		savedSchemaSelection = new ArrayList<String>();
		IStructuredSelection sel = (IStructuredSelection) schema.getSelection();
		if(!sel.isEmpty()) {
			for(Object sic : sel.toArray()) {
				if(sic instanceof SchemaItemConfig)
					savedSchemaSelection.add(((SchemaItemConfig)sic).getAttributeName());
			}
		}
	}

	private void restoreSchemaSelection() {
		if(savedSchemaSelection == null)
			return;

		Object input = schema.getInput();
		ArrayList<Object> list = new ArrayList<Object>();
		if(input instanceof SchemaConfig) {
			SchemaConfig sc = (SchemaConfig) input;
			for(String str : savedSchemaSelection) {
				SchemaItemConfig sic = sc.getItem(str);
				if(sic != null)
					list.add(sic);
			}
		}

		StructuredSelection sel = StructuredSelection.EMPTY;
		if(list.size() > 0)
			sel = new StructuredSelection(list.toArray());
		schema.setSelection(sel);
		if(list.size() > 0)
			schema.reveal(sel.getFirstElement());
	}

	protected void updateResult() {
		if (job.getError() != null) {
			MessageDialog.openError(getShell(), Messages.getString("DiscoverSchemaWidget.13"), Utils.exceptionText(job.getError())); //$NON-NLS-1$
			EclipseAppender.logerror(job.getError().getMessage(), job.getError());

		} else if (job.isGetnext()) {
			Entry entry = job.getEntry();
			if (entry != null) {
				if (entry.size() > 0) {

					try {
						SchemaUtils.convertEntryToSchema(entry, cc, true);
					} catch (Exception e) {
						EclipseAppender.logerror(e.getMessage(), e);
					}

					try {
						if (!isLoopConnector())
							SchemaUtils.convertEntryToSchema(entry, cc, false);
					} catch (Exception e) {
						EclipseAppender.logerror(e.getMessage(), e);
					}

					schema.refresh(true);
					setCheckedItems();
				} else {
					MessageDialog.openInformation(getShell(),
							Messages.getString("DiscoverSchemaWidget.14"), Messages.getString("DiscoverSchemaWidget.15")); //$NON-NLS-1$ //$NON-NLS-2$
				}
			} else {
				MessageDialog.openInformation(getShell(),
						Messages.getString("DiscoverSchemaWidget.14"), Messages.getString("DiscoverSchemaWidget.End.Of.Data")); //$NON-NLS-1$ //$NON-NLS-2$
			}
			schema.refresh();

		} else if (job.gotSchema()) {
			ConnectorConfig tmpcc = job.getConnectorConfig();
			if (Utils.isInputConnector(cc))
				copySchema(cc, tmpcc.getSchema(true), true);
			if (Utils.isOutputConnector(cc))
				copySchema(cc, tmpcc.getSchema(false), false);

			schema.refresh();

		}

		// -- restore previous selection
		restoreSchemaSelection();

	}

	/**
	 * Check if this Connector is the Connector in a Loop Component.
	 * @return true if this Connector is the Connector in a Loop Component.
	 */
	private boolean isLoopConnector(){
		if (cc != null && cc.getParent() instanceof LoopConfig) {
			LoopConfig lc = (LoopConfig) cc.getParent();
			if (lc.getLoopType()==LoopConfig.LOOP_CONNECTOR_FC) {
				try {
					if (lc.getLoopConnector() == cc)
						return true;
				} catch (Exception ignore) {
					return false;
				}
			}
		}
		return false;
	}

	private void copySchema(ConnectorConfig config, SchemaConfig schema, boolean input) {
		SchemaConfig sc = config.getSchema(input);
		sc.notifyChange(sc, "", MetamergeConfigChange.BEGIN_CHANGES);
		SchemaItemConfig newItem = null;
		for (Object obj : schema.getItemNames()) {
			SchemaItemConfig sic = schema.getItem(obj);
			try {
				if (isSchemaItemHierarchical(sic)) {
					sc.setItem(sic.getName(), sic);
				} else {
					newItem = SchemaUtils.addSchemaItem(sc, sic.getAttributeName(), sic.getExternalSyntax(), sic.getSample());
					copyParameter(sic, newItem, InternalSchema.SCHEMA_INTERNAL_SYNTAX);
					copyParameter(sic, newItem, InternalSchema.SCHEMA_PRESENCE);
					copyParameter(sic, newItem, InternalSchema.SCHEMA_OCCURS_MIN);
					copyParameter(sic, newItem, InternalSchema.SCHEMA_OCCURS_MAX);
				}
			} catch (Exception e) {
				EclipseAppender.logerror(e.getMessage(), e);
			}
		}
		sc.notifyChange(sc, "", MetamergeConfigChange.END_CHANGES);
		setCheckedItems();
	}

	private void copyParameter(SchemaItemConfig source, SchemaItemConfig dest, String name) {
		Object value = source.getParameterRaw(name);
		if (value != null && ! value.equals(dest.getParameterRaw(name)))
			dest.setParameter(name, value);
	}

	/**
	 * Checks if the SchemaItemConfig is hierarchical.
	 *
	 * @param schemaItemConfig
	 *            the SchemaItemConfig to be checked.
	 * @return <code>true</code> if the SchemaItemConfig is hierarchical,
	 *         otherwise <code>false</code>.
	 */
	private boolean isSchemaItemHierarchical(SchemaItemConfig schemaItemConfig) {
		return schemaItemConfig.getChildSchemaList().getChildNames().size() > 0;
	}

	public void setConfiguration(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection sel = (IStructuredSelection) selection;
			if (sel.getFirstElement() instanceof BaseConfiguration) {
				BaseConfiguration bc = (BaseConfiguration) sel.getFirstElement();
				if (bc instanceof ConnectorConfig && schema != null) {
					cc = (ConnectorConfig) bc;
					updateSchemaContentInput(cc);
					setCheckedItems();
				}
			}
		}
	}

	@Override
	public void setEditingConfig(BaseConfiguration editingConfig) {
		super.setEditingConfig(editingConfig);
		if (editingConfig != null) {
			setConfiguration(new StructuredSelection(editingConfig));
		}
	}

	protected class QueryJob implements IRunnableWithProgress {
		private Exception error;
		private Entry entry;
		private boolean getnext;
		private ConnectorConfig config;
		private boolean gotSchema = false;
		private Object connection = null;
		private boolean didSelect = false;

		public QueryJob(ConnectorConfig config, String name, boolean getnext) {
			this.getnext = getnext;
			this.config = config;
			try {
				if (config instanceof FunctionConfig)
					SystemFunctions.loadFunction((FunctionConfig) config);
				else
					SystemFunctions.loadConnector(config);
			} catch (Exception e) {
			}
		}

		public void terminate() {
			try {
				if (connection instanceof ConnectorInterface)
					((ConnectorInterface) connection).terminate();
				else if (connection instanceof FunctionInterface)
					((FunctionInterface) connection).terminate();
			} catch (Exception e) {
				e.printStackTrace();
			}
			connection = null;
		}

		public void setIsGetNext(boolean getnext) {
			this.getnext = getnext;
		}

		public boolean isConnected() {
			return connection != null;
		}

		public boolean gotSchema() {
			return gotSchema;
		}

		public ConnectorConfig getConnectorConfig() {
			return config;
		}

		public boolean isGetnext() {
			return getnext;
		}

		public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
			gotSchema = false;
			ConnectorInterface connector = null;
			FunctionInterface function = null;
			// if (getThread() != null)
			// getThread().setContextClassLoader(Entry.class.getClassLoader());

			monitor.beginTask(Messages.getString("DiscoverSchemaWidget.job.name"), 1);

			try {
				if (config instanceof FunctionConfig) {
					if (connection instanceof FunctionInterface) {
						function = (FunctionInterface) connection;
					} else {
						monitor.subTask(Messages.getString("DiscoverSchemaWidget.job.connect"));
						function = SystemFunctions.loadFunction((FunctionConfig) config);
						function.initialize(null);
						connection = function;
					}
					if (isGetnext()) {
						monitor.subTask(Messages.getString("DiscoverSchemaWidget.job.read"));
						Object o = function.perform(null);
						if (o instanceof Entry)
							entry = (Entry) o;
						else
							entry = null;
					} else {
						monitor.subTask(Messages.getString("DiscoverSchemaWidget.job.query"));
						querySchema(function);
						if (Utils.isAssemblyLine(config))
							connection = null;
					}

				} else if (isGetnext()) {
					if (connection instanceof ConnectorInterface) {
						connector = (ConnectorInterface) connection;
						// -- sometimes a connector will consume the first and only entry
						// -- during query schema, so we make sure we do a selectEntries before
						// -- the first getnext call on the connection.
						if(!didSelect) {
							connector.terminate();
							connector.initialize(new ConnectorMode(ConnectorConfig.ITERATOR_MODE));
							connector.selectEntries();
							didSelect = true;
						}
					} else {
						monitor.subTask(Messages.getString("DiscoverSchemaWidget.job.connect"));
						connector = (Connector) SystemFunctions.loadConnector(config);
						connector.initialize(new ConnectorMode(ConnectorConfig.ITERATOR_MODE));
						connector.selectEntries();
						connection = connector;
					}
					monitor.subTask(Messages.getString("DiscoverSchemaWidget.job.read"));
					entry = connector.getNextEntry();
				} else {
					monitor.subTask(Messages.getString("DiscoverSchemaWidget.job.connect"));
					connector = (Connector) SystemFunctions.loadConnector(config);
					connection = connector;
					monitor.subTask(Messages.getString("DiscoverSchemaWidget.job.query"));
					querySchema(connector);
				}
			} catch (Exception e) {
				error = e;
			}

			monitor.done();

			getDisplay().syncExec(new Runnable() {
				public void run() {
					updateResult();
					enableAction(nextEntryAction, isJobConnected());
					enableAction(closeConnectionAction, isJobConnected());
					enableAction(discoverAction, !isJobConnected());
					enableAction(connectAction, !isJobConnected());
				}

			});
			// return Status.OK_STATUS;
		}

		public Exception getError() {
			return error;
		}

		public Entry getEntry() {
			return entry;
		}

		@SuppressWarnings("unchecked")
		public void querySchema(FunctionInterface function) throws Exception {
			// If a FC has handled schema population on its own, it will send null
			// as a response. Otherwise it will send the items to be displayed as a Vector.
			// In the future each FC will handle its schema, so only this line will be needed
			// (schema population logic will not be used in the CE).
			Object result = function.querySchema(Boolean.TRUE);

			if (result == null) {
				// the FC has handled the attribute mapping on its own
				gotSchema = true;
			} else {
				// the FC has NOT handled the attribute mapping on its own
				if (result instanceof Vector) {
					buildSchema((Vector<Entry>) result, true);
				}
				result = function.querySchema(Boolean.FALSE);
				if (result instanceof Vector) {
					buildSchema((Vector<Entry>) result, false);
				}
				if (gotSchema)
					return;
				result = function.querySchema(null);
				if (result instanceof Vector) {
					buildSchema((Vector<Entry>) result, true);
					buildSchema((Vector<Entry>) result, false);
				}
			}
		}

		@SuppressWarnings("unchecked")
		public void querySchema(ConnectorInterface connector) throws Exception {
			Throwable selectErr = null;
			if (Utils.isAssemblyLine(cc)) {
				gotSchema = true;
				connection = null;
			} else {
				BaseConfiguration LDAPconfig = null;
				if(connector.getClass().getName().equals(LDAP_CONNECTOR))
					LDAPconfig = connector.getRawConnectorConfiguration();
				Object saveParam = null;
				if (LDAPconfig != null) {
					saveParam = LDAPconfig.getParameterRaw(LDAP_SIZE_LIMIT);
					LDAPconfig.setParameter(LDAP_SIZE_LIMIT, "1");
				}
				try {
					connector.initialize(new ConnectorMode(ConnectorConfig.ITERATOR_MODE));
					connector.selectEntries();
				} catch (Throwable t) {
					EclipseAppender.logerror("querySchema", t);
					selectErr = t;
					// If the connector does not want to be initialized, we can
					// still try queryschema
				} finally {
					if (LDAPconfig != null) {
						if (saveParam != null)
							LDAPconfig.setParameter(LDAP_SIZE_LIMIT, saveParam);
						else
							LDAPconfig.removeParameter(LDAP_SIZE_LIMIT);
					}
				}
			}

			// If a Connector has handled schema population on its own, it will send null
			// as a response. Otherwise it will send the items to be displayed as a Vector.
			// In the future each Connector will handle its schema, so only this line will be needed
			// (schema population logic will not be used in the CE).
			Object result = null;
			try {
				result = connector.querySchema(null);
			} catch (Throwable t) {
				if (selectErr == null)
					selectErr = t;
			}

			if (result == null) {
				if ( !config.getSchema(true).getItemNames().isEmpty() ||
						!config.getSchema(false).getItemNames().isEmpty())
				// the Connector has handled schema discovery on its own (unless we got an error)
					gotSchema = selectErr == null;
			} else {
				// the Connector has NOT handled schema discovery on its own
				if (Utils.isInputConnector(config)) {
					buildSchema((Vector<Entry>) result, true);
				}
				if (Utils.isOutputConnector(config)) {
					buildSchema((Vector<Entry>) result, false);
				}
			}
			if (!gotSchema && selectErr != null) {
				final Throwable err = selectErr;
				getDisplay().syncExec(new Runnable() {
					public void run() {
						EclipseAppender.showError(Messages.getString("DiscoverSchemaWidget.13"), err, getShell());
					}
				});
				terminate();
			}
		}

		public void buildSchema(Vector<Entry> v, boolean input) throws Exception {
			if (v.size() == 0)
				return;
			SchemaConfig sc = config.getSchema(input);
			sc.notifyChange(sc, "", MetamergeConfigChange.BEGIN_CHANGES);
			for (Entry e : v) {
				try {
					SchemaUtils.addSchemaItem(sc, e.getString("name"), e.getString("syntax"), null);
				} catch(Exception ex) {
					EclipseAppender.logerror(ex.getMessage(), ex);
				}
			}
			sc.notifyChange(sc, "", MetamergeConfigChange.END_CHANGES);
			gotSchema = true;
		}

		public void dispose() {
			try {
				if (connection instanceof FunctionInterface)
					((FunctionInterface) connection).terminate();
				else if (connection instanceof ConnectorInterface)
					((ConnectorInterface) connection).terminate();
				connection = null;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void createAttributeMaps() {
		if (cc == null)
			return;

		if (Utils.isInputConnector(cc))
			createAttributeMap(true);

		if (Utils.isOutputConnector(cc))
			createAttributeMap(false);

	}

	private void createAttributeMap(boolean input) {
		if ((schema instanceof CheckboxTreeViewer))
			return;

		CheckboxTreeViewer ct = (CheckboxTreeViewer) schema;
		AttributeMapConfig map = cc.getAttributeMap(input);
		map.notifyChange(map, "", MetamergeConfigChange.BEGIN_CHANGES);
		for (TreeItem item : ct.getTree().getItems()) {
			String name = item.getText();
			if (!item.getChecked()) {
				map.removeAttributeMapItem(name);
			} else if (!map.hasAttributeMapItem(name)) {
				try {
					AttributeMapItem ami = map.newAttributeMapItem(name);
					ami.setSimple(name);
				} catch (Exception e) {
					SystemFunctions.doNothing();
				}
			}
		}
		map.notifyChange(map, "", MetamergeConfigChange.END_CHANGES);
	}

	private void setCheckedItems() {
		if (!(schema instanceof CheckboxTreeViewer))
			return;

		List<String> names = cc.getAttributeMap(true).getAttributeNames();
		CheckboxTreeViewer ct = (CheckboxTreeViewer) schema;
		for (TreeItem item : ct.getTree().getItems()) {
			if (names.indexOf(item.getText()) >= 0) {
				item.setChecked(true);
			}
		}
		names = cc.getAttributeMap(false).getAttributeNames();
		for (TreeItem item : ct.getTree().getItems()) {
			if (names.indexOf(item.getText()) >= 0) {
				item.setChecked(true);
			}
		}
		schema.refresh(true);
	}

	public boolean isChecked() {
		return (getStyle() & SWT.CHECK) > 0;
	}

	public void configurationChanged(MetamergeConfigChange changeEvent) {
		if (isDisposed())
			return;
		if (changeEvent.getSource() instanceof ConnectorConfig && InternalSchema.CONNECTOR_MODE.equals(changeEvent.getKey())) {
			if (discoverProxy != null) {
				discoverProxy.dispose();
				discoverProxy = null;
			}
			createUI(this);
			layout();
		}
	}

	private void createSchemaTree(Composite parent) {
		if ((getStyle() & SWT.CHECK) > 0)
			schema = new CheckboxTreeViewer(parent, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		else
			schema = new TreeViewer(parent, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		schema.getTree().setHeaderVisible(true);
		provider = new AttributeContentProvider();
		schema.setContentProvider(new SchemaContentProvider(schema));

		final BaseEditor editor = getEditor();
		if (editor != null) {
			editor.registerContextMenu(schema, getEditingConfig().getShortName());
			editor.addSelectionProvider(schema);
			editor.getMenuManager().appendToGroup(TDI.GROUP_TDI, new AddSchemaItemAction(getShell(), cc.getSchema(mappingMode == WorkMapWidget.MAP_MODE_INPUT)));

			// -- Append the standard edit operations
			final IAction cut = editor.getActionFor(ActionFactory.CUT.getId());
			if (cut != null)
				editor.getMenuManager().appendToGroup("group.edit", cut);

			final IAction copy = editor.getActionFor(ActionFactory.COPY.getId());
			if ( copy != null)
				editor.getMenuManager().appendToGroup("group.edit", copy);

			pasteAction = new PasteAction();
			editor.getMenuManager().appendToGroup("group.edit", pasteAction);

			schema.getTree().addFocusListener(new FocusListener() {
				private IAction saveAction;

				public void focusGained(FocusEvent e) {
					saveAction = editor.getActionFor(ActionFactory.PASTE.getId());
					editor.registerAction(ActionFactory.PASTE.getId(), pasteAction);
					if (cut instanceof CutConfigAction)
						((CutConfigAction) cut).selectionChanged(editor, schema.getSelection());
					if (copy instanceof CopyConfigAction)
						((CopyConfigAction) copy).selectionChanged(editor, schema.getSelection());
					editor.updateActionBars();
				}

				public void focusLost(FocusEvent e) {
					editor.registerAction(ActionFactory.PASTE.getId(), saveAction);
					editor.updateActionBars();
				}
			});
		}

		// -- Drag support
		DragSourceListener dsl = new DragSourceListener() {
			public void dragStart(DragSourceEvent e) {
				e.data = getSelectedSchemaItems();
			}

			public void dragFinished(DragSourceEvent e) {
			}

			public void dragSetData(DragSourceEvent e) {
				if (LocalSelectionTransfer.getTransfer().isSupportedType(e.dataType)) {
					LocalSelectionTransfer.getTransfer().setSelection(getSelectedSchemaItems());
					e.data = getSelectedSchemaItems();
				}
			};
		};
		schema.addDragSupport(DND.DROP_MOVE | DND.DROP_COPY, new Transfer[] { LocalSelectionTransfer.getTransfer() }, dsl);

		// -- Drop support
		DropTargetListener dtl = new DropTargetListener() {
			public void dragEnter(DropTargetEvent event) {
				if(getSchemaDrop(event) != null)
					event.detail = DND.DROP_COPY;
				else
					event.detail = DND.DROP_NONE;
			}
			public void dragLeave(DropTargetEvent event) {
			}
			public void dragOperationChanged(DropTargetEvent event) {
			}
			public void dragOver(DropTargetEvent event) {
			}
			public void drop(DropTargetEvent event) {
				IFile schemaFile = getSchemaDrop(event);
				if(schemaFile != null) {
					SchemaEditor.setDesignSchemaName((BaseConfiguration) schema.getInput(), schemaFile.getName());
				}
			}
			public void dropAccept(DropTargetEvent event) {
			}
		};
		schema.addDropSupport(DND.DROP_MOVE | DND.DROP_COPY, new Transfer[] { LocalSelectionTransfer.getTransfer()}, dtl);

		CellLabelProvider delegateProvider = new CellLabelProvider() {
			SchemaConfig parent;
			@Override
			public void update(ViewerCell cell) {
				Object element = cell.getElement();
				cell.setText(provider.getColumnText(element, cell.getColumnIndex()));
				cell.setImage(provider.getColumnImage(element, cell.getColumnIndex()));
				if (parent == null)
					parent = Utils.getParentConfig(element, SchemaConfig.class);
				if (element instanceof SchemaItemConfig && parent != null) {
					if (parent.hasParameter(((SchemaItemConfig) element).getShortName()))
						cell.setForeground(BLACK);
					else
						cell.setForeground(BLUE);
				}
			}
		};

		// Table columns
		TreeViewerColumn tc = new TreeViewerColumn(schema, SWT.LEFT);
		tc.getColumn().setText(Messages.getString("SchemaConfigWidget.1")); //$NON-NLS-1$
		tc.getColumn().setWidth(200);
		tc.setLabelProvider(delegateProvider);

		tc = new TreeViewerColumn(schema, SWT.LEFT);
		tc.getColumn().setText(Messages.getString("SchemaConfigWidget.2")); //$NON-NLS-1$
		tc.getColumn().setWidth(100);
		tc.setEditingSupport(new EditingSupport(schema) {
			@Override
			protected boolean canEdit(Object element) {
				return true;
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				return new TextCellEditor((Composite) schema.getControl());
			}

			@Override
			protected Object getValue(Object element) {
				SchemaItemConfig sc = (SchemaItemConfig) element;
				return sc.getSample() == null ? "" : sc.getSample();
			}

			@Override
			protected void setValue(Object element, Object value) {
				SchemaItemConfig sc = (SchemaItemConfig) element;
				sc.setSample(value);
			}

		});
		tc.setLabelProvider(delegateProvider);

		tc = new TreeViewerColumn(schema, SWT.LEFT);
		tc.getColumn().setText(Messages.getString("SchemaConfigWidget.3")); //$NON-NLS-1$
		tc.getColumn().setWidth(100);
		tc.setEditingSupport(new EditingSupport(schema) {
			ComboBoxCellEditor celleditor = new ComboBoxCellEditor((Composite) schema.getControl(), new String[]{
					SchemaItemConfig.PRESENCE_OPTIONAL, SchemaItemConfig.PRESENCE_REQUIRED
			}, SWT.READ_ONLY);


			@Override
			protected boolean canEdit(Object element) {
				return true;
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				return celleditor;
			}

			@Override
			protected Object getValue(Object element) {
				SchemaItemConfig sc = (SchemaItemConfig) element;
				String str = sc.getPresenceFlag();
				if(str == null || str.equals("") || str.equals(SchemaItemConfig.PRESENCE_OPTIONAL))
					return 0;
				else
					return 1;
			}

			@Override
			protected void setValue(Object element, Object value) {
				SchemaItemConfig sc = (SchemaItemConfig) element;
				if((Integer)value == 0)
					sc.setPresenceFlag(SchemaItemConfig.PRESENCE_OPTIONAL);
				else
					sc.setPresenceFlag(SchemaItemConfig.PRESENCE_REQUIRED);
			}

		});
		tc.setLabelProvider(delegateProvider);

		tc = new TreeViewerColumn(schema, SWT.LEFT);
		tc.getColumn().setText(Messages.getString("SchemaEditor.3")); //$NON-NLS-1$
		tc.getColumn().setWidth(100);
		tc.setEditingSupport(new EditingSupport(schema) {
			ComboBoxCellEditor celleditor = new ComboBoxCellEditor((Composite) schema.getControl(), new String[]{
					"java.lang.String", "java.lang.Integer", "java.lang.Boolean", "java.util.Date"
			}) {

				private CCombo combo;

				protected Object doGetValue() {
					Object value = super.doGetValue();
					if (value instanceof Integer && ((Integer) value) == -1)
						value = combo.getText();
					return value;
				}

				protected Control createControl(Composite parent) {
					combo = (CCombo) super.createControl(parent);
					combo.addKeyListener(new KeyListener() {
						public void keyPressed(KeyEvent e) {
							if (e.keyCode == SWT.DEL) {
								// -- SWT again ... doesn't
								// understand DEL button ...
								combo.cut();
								e.doit = false;
							}
						}

						public void keyReleased(KeyEvent e) {
							if (e.keyCode == SWT.DEL) {
								e.doit = false;
							}
						}
					});
					return combo;
				}
			};

			@Override
			protected boolean canEdit(Object element) {
				return true;
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				return celleditor;
			}

			@Override
			protected Object getValue(Object element) {
				SchemaItemConfig sc = (SchemaItemConfig) element;
				String cls = sc.getJavaClass() == null ? "" : sc.getJavaClass();
				ArrayList<String> list = new ArrayList<String>();
				for(String str : celleditor.getItems())
					list.add(str);
				if(!list.contains(cls)) {
					list.add(cls);
					celleditor.setItems(list.toArray(new String[list.size()]));
				}
				return list.indexOf(cls);
			}

			@Override
			protected void setValue(Object element, Object value) {
				SchemaItemConfig sc = (SchemaItemConfig) element;
				if(value instanceof Integer) {
					sc.setJavaClass(celleditor.getItems()[(Integer)value]);
				} else {
					sc.setJavaClass(value.toString());
				}
			}

		});
		tc.setLabelProvider(delegateProvider);

		tc = new TreeViewerColumn(schema, SWT.LEFT);
		tc.getColumn().setText(Messages.getString("SchemaEditor.4")); //$NON-NLS-1$
		tc.getColumn().setWidth(200);
		tc.setEditingSupport(new EditingSupport(schema) {
			@Override
			protected boolean canEdit(Object element) {
				return true;
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				return new TextCellEditor((Composite) schema.getControl());
			}

			@Override
			protected Object getValue(Object element) {
				SchemaItemConfig sc = (SchemaItemConfig) element;
				return sc.getExternalSyntax() == null ? "" : sc.getExternalSyntax();
			}

			@Override
			protected void setValue(Object element, Object value) {
				SchemaItemConfig sc = (SchemaItemConfig) element;
				sc.setExternalSyntax(value.toString());
			}

		});

		tc.setLabelProvider(delegateProvider);

		// Add tooltips with wrapping
		new WrappedToolTipSupport(schema, ToolTip.NO_RECREATE, false);

		// -- simple name sorting
		schema.setSorter(new SchemaSorter());

		updateSchemaContentInput(cc);
	}

	private class WrappedToolTipSupport extends ColumnViewerToolTipSupport {

		protected WrappedToolTipSupport(ColumnViewer viewer, int style,	boolean manualActivation) {
			super(viewer, style, manualActivation);
		}

		@Override
		protected Composite createToolTipContentArea(Event event, Composite parent) {
			StyledText label = new StyledText(parent, SWT.MULTI | SWT.WRAP | SWT.READ_ONLY) {
				@Override
				public Point computeSize(int wHint, int hHint, boolean changed) {
					Point p = super.computeSize(wHint, hHint, changed);
					int w = Math.min(getDisplay().getClientArea().width, getDisplay().getBounds().width);
					if (p.x > w)
						p = super.computeSize(w - 50, SWT.DEFAULT, changed);
					return p;
				}
			};
			label.setForeground(getForegroundColor(event));
			label.setBackground(getBackgroundColor(event));
			label.setText(event.text != null ? event.text : "");
			return label;
		}

		@Override
		protected boolean shouldCreateToolTip(Event event) {
			Object o = getToolTipArea(event);
			if (! (o instanceof ViewerCell))
				return false;
			ViewerCell cell = (ViewerCell) o;
			if (cell.getElement() == null)
				return false;
			event.text = provider.getColumnText(cell.getElement(), cell.getColumnIndex());
			return event.text != null;
		}

	}

	protected IFile getSchemaDrop(DropTargetEvent event) {
		if (! LocalSelectionTransfer.getTransfer().isSupportedType(event.currentDataType) ||
			! (event.data instanceof IStructuredSelection))
			return null;
		IStructuredSelection sel = (IStructuredSelection) event.data;
		if(sel.size() == 1 && sel.getFirstElement() instanceof IFile) {
			IFile file = (IFile) sel.getFirstElement();
			if(TDINature.SCHEMA_FILEEXT.equals(file.getFileExtension()))
				return file;
		}

		return null;
	}

	private void updateSchemaContentInput(ConnectorConfig cc) {
		switch (mappingMode) {
		case WorkMapWidget.MAP_MODE_INPUT:
			schema.setInput(cc.getSchema(true));
			break;
		case WorkMapWidget.MAP_MODE_OUTPUT:
			setLoopParamMapping();
			if(loopParamsMapping) {
				try {
					updateOutputSchema();
				} catch (Exception e) {
					EclipseAppender.logerror(e.toString(), e, getShell());
				}
			}
			schema.setInput(cc.getSchema(false));
			break;
		default:
			schema.setInput(cc);
			break;
		}
		schema.expandAll();
		// Should not do this when we are using FillLayout...
		//schema.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	}

	public ISelection getSelectedSchemaItems() {
		return schema.getSelection();
	}

	private static class SchemaSorter extends ViewerSorter {

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			if (e1 instanceof BaseConfiguration && e2 instanceof BaseConfiguration) {
				BaseConfiguration o1 = (BaseConfiguration) e1;
				BaseConfiguration o2 = (BaseConfiguration) e2;
				String a1 = o1.getShortName();
				String a2 = o2.getShortName();
				if (a1 != null)
					return a1.compareToIgnoreCase(a2);
			}
			return super.compare(viewer, e1, e2);
		}

	}

	private class SchemaContentProvider implements ITreeContentProvider, MetamergeConfigChangeListener {

		private TreeViewer viewer;
		private boolean batchChanges;

		public SchemaContentProvider(TreeViewer viewer) {
			super();
			this.viewer = viewer;
		}

		public Object[] getChildren(Object parentElement) {
			ArrayList<BaseConfiguration> list = new ArrayList<BaseConfiguration>();
			if (parentElement instanceof ConnectorConfig) {
				ConnectorConfig cc = (ConnectorConfig) parentElement;
				if (Utils.isInputConnector(cc))
					list.add(cc.getSchema(true));
				if (Utils.isOutputConnector(cc))
					list.add(cc.getSchema(false));
			} else if (parentElement instanceof SchemaConfig) {
				SchemaConfig sc = (SchemaConfig) parentElement;
				for (Object obj : sc.getItemNames()) {
					SchemaItemConfig sci = sc.getItem(obj);
					if(!SchemaEditor.SCHEMA_DESIGN_NAME.equals(obj))
						list.add(sci);
				}

				SchemaItemConfig sci = sc.getItem(SchemaEditor.SCHEMA_DESIGN_NAME);
				if(sci != null) {
					SchemaConfig sc2 = SchemaEditor.getDesignSchema(Utils.getProjectFor(sc), sci.getExternalSyntax());
					if(sc2 != null) {
						for (Object obj : sc2.getItemNames()) {
							sci = sc2.getItem(obj);
							SchemaItemConfig realItem = sc.getItem(sci.getShortName());
							if(realItem == null || !list.contains(realItem)) {
								sci.setParent(sc);
								list.add(sci);
							}
						}
					}
				}

			} else if (parentElement instanceof SchemaItemConfig) {
				for (BaseConfiguration obj : ((SchemaItemConfig) parentElement).getChildSchemaList().getConfigurations(null))
					list.add(obj);
			}
			return list.toArray();
		}

		public Object getParent(Object element) {
			BaseConfiguration parent = ((BaseConfiguration) element).getParent();

			// -- We dont include the SchemaItemConfig.childSchemaList as an
			// element in the
			// -- list returned from getChildren().
			if (parent instanceof ContainerConfig && element instanceof SchemaItemConfig)
				parent = parent.getParent();

			return parent;
		}

		public boolean hasChildren(Object element) {
			if (element instanceof ConnectorConfig)
				return true;
			else if (element instanceof SchemaConfig)
				return ((SchemaConfig) element).getItemNames().size() > 0;
			else if (element instanceof SchemaItemConfig)
				return ((SchemaItemConfig) element).getChildSchemaList().size() > 0;
			else
				return false;
		}

		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if (oldInput instanceof BaseConfiguration)
				((BaseConfiguration) oldInput).removeListener(this);
			if (newInput instanceof BaseConfiguration)
				((BaseConfiguration) newInput).addListener(this);
		}

		public void configurationChanged(final MetamergeConfigChange changeEvent) {
			if (viewer == null || viewer.getControl().isDisposed())
				return;

			if(changeEvent.getOperation() == MetamergeConfigChange.BEGIN_CHANGES) {
				batchChanges = true;
			} else if(changeEvent.getOperation() == MetamergeConfigChange.END_CHANGES) {
				batchChanges = false;
				getDisplay().asyncExec(new Runnable() {
					public void run() {
						viewer.refresh();
					}
				});
				return;
			}

			if(batchChanges)
				return;

			getDisplay().asyncExec(new Runnable() {

				public void run() {
					Object source = changeEvent.getSource();

					// -- Child schema item added/removed
					if (source instanceof ContainerConfig && changeEvent.getUserObject() instanceof Object[]) {
						BaseConfiguration parent = ((ContainerConfig) source).getParent();
						if (parent instanceof SchemaItemConfig) {
							Object[] user = (Object[]) changeEvent.getUserObject();
							if (user.length == 2) {
								int position = (Integer) user[0];
								SchemaItemConfig sic = (SchemaItemConfig) user[1];
								if (changeEvent.getOperation() == MetamergeConfigChange.MCC_SET) {
									insertTreeElement(parent, sic, position);
								} else {
									if(sic != null && sic.getShortName().equals(SchemaEditor.SCHEMA_DESIGN_NAME))
										viewer.refresh();
									else
										deleteTreeElement(parent, sic);
								}

								return;
							}
						}
					}

					if ((source instanceof AttributeMapConfig || source instanceof SchemaConfig)) {
						if (InternalSchema.INHERITS_FROM.equals(changeEvent.getKey())) {
							((TreeViewer) viewer).refresh();
							return;
						}

						Object child;
						if (source instanceof AttributeMapConfig)
							child = ((AttributeMapConfig) source).getAttributeMapItem(changeEvent.getKey());
						else
							child = ((SchemaConfig) source).getItem(changeEvent.getKey());

						// Removed object no longer exists
						if (child == null) {
							BaseConfiguration conf = findConfigObject(source, changeEvent.getKey());
							if (conf != null)
								((TreeViewer) viewer).remove(source, new Object[] { conf });

							if(SchemaEditor.SCHEMA_DESIGN_NAME.equals(changeEvent.getKey()))
								viewer.refresh();

							return;
						}

						switch (changeEvent.getOperation()) {
						case MetamergeConfigChange.MCC_ADD:
						case MetamergeConfigChange.MCC_SET:
							((TreeViewer) viewer).insert(source, child, findPosition(source, child));
							viewer.refresh(child);
							break;

						case MetamergeConfigChange.MCC_DELETE:
						case MetamergeConfigChange.MCC_REMOVE:
							((TreeViewer) viewer).remove(source, new Object[] { child });
							break;
						}

					} else if (source instanceof AttributeMapItem || source instanceof SchemaItemConfig) {
						viewer.refresh(source);
					}

					if(source instanceof SchemaItemConfig && ((SchemaItemConfig)source).getShortName().equals(SchemaEditor.SCHEMA_DESIGN_NAME))
						viewer.refresh();
				}

			});

		}

		private BaseConfiguration findConfigObject(Object source, Object key) {
			TreeItem[] items = ((TreeViewer) viewer).getTree().getItems();
			if (items == null)
				return null;

			return findConfigObject(items, source, key);
		}

		private BaseConfiguration findConfigObject(TreeItem[] items, Object source, Object key) {
			for (TreeItem ti : items) {
				if (ti.getData() instanceof BaseConfiguration) {
					BaseConfiguration bc = (BaseConfiguration) ti.getData();
					if (key.equals(bc.getShortName()) && bc.getParent() == source)
						return bc;
				}
				if (ti.getItemCount() > 0) {
					BaseConfiguration bc = findConfigObject(ti.getItems(), source, key);
					if (bc != null)
						return bc;
				}
			}
			return null;
		}

		private void insertTreeElement(Object parent, SchemaItemConfig child, int position) {
			// -- Believe or not, TreeViewer won't insert an item unless the
			// parent already has children
			// -- Container already has the new child so we check accordingly
			if (child.getParent() instanceof ContainerConfig && ((ContainerConfig) child.getParent()).size() < 2) {
				((TreeViewer) viewer).refresh(parent, true);
			} else {
				((TreeViewer) viewer).insert(parent, child, position);
			}

			// -- Expand parent to reveal new item
			((TreeViewer) viewer).setExpandedState(parent, true);
		}

		private void deleteTreeElement(BaseConfiguration parent, SchemaItemConfig sic) {
			((TreeViewer) viewer).remove(parent, new Object[] { sic });
		}

		private int findPosition(Object source, Object child) {
			String str = ((BaseConfiguration) child).getShortName();
			List<String> cc;
			if (source instanceof AttributeMapConfig) {
				cc = ((AttributeMapConfig) source).getAttributeNames();
			} else {
				cc = ((SchemaConfig) source).getItemNames();
			}
			for (int i = 0; i < cc.size(); i++) {
				if (cc.get(i).compareTo(str) > -1)
					return i;
			}
			return 0;
		}
	}

	private void updateOutputSchema() throws Exception {
		SchemaConfig sc = cc.getSchema(false);

		// Remove any inheritance, that will only mess up the connector parameters.
		if (!BaseConfiguration.INHERIT_NONE.equals(sc.getInheritsFromRef()))
			sc.updateInheritsFrom(BaseConfiguration.INHERIT_NONE);

		List<String> list = sc.getItemNames();

		Map<String, FormItemConfig> params = getItemNames();

		if(!setDiffers(list, params.keySet()))
			return;

		for (int i = 0; i < list.size(); i++)
			sc.removeItem(list.get(i));

		for (Map.Entry<String, FormItemConfig> entry:params.entrySet()) {
			String str = entry.getKey();

			SchemaItemConfig sic = sc.newItem(str);
			FormItemConfig fic = entry.getValue();
			if (fic == null)
				continue;

			if (fic.getToolTip() != null)
				sic.setSample(fic.getToolTip());

			if (fic.isIndexBased())
				sic.setExternalSyntax("index based");
			else
				sic.setExternalSyntax(fic.getSyntax());
		}
	}

	private boolean setDiffers(List<String> list, Set<String> params) {
		if(list == null || params == null)
			return true;
		if(list.size() != params.size())
			return true;

		for(String str : list) {
			if(!params.contains(str))
				return true;
		}

		return false;
	}

	private Map<String, FormItemConfig> getItemNames() {
		Map<String, FormItemConfig> ret = new HashMap<String, FormItemConfig>();
		if (cc == null)
			return ret;

		try {
			FormConfig global = Utils.getSystemForm("__GLOBAL__");
			MetamergeConfig system = MetamergeConfigFactory.getNamespace(MetamergeConfigFactory.SYSTEM_NAMESPACE);
			BaseConfiguration bc = cc.getConnectionConfig();
			String mode = cc.getMode();

			// First get parameters from custom form
			String customForm = bc.getStringParameter("$form$");
			if (customForm!= null) {
				TDIConfigurationFile mc = new TDIConfigurationFile(new ByteArrayInputStream(customForm.trim().getBytes()), false);
				BaseConfiguration fc = mc.getDefaultConfigObject();
				if (fc instanceof FormConfig)
					getItemNames(ret, (FormConfig)fc, mode, global);
			}

			String javaClass = cc.getConnectionConfig().getJavaClass();
			if (javaClass == null || javaClass.length() == 0)
				return ret;

			//Then all parameters from special forms for this connector
			while (bc != null) {
				try {
					BaseConfiguration curr = bc.getParent();
					if (curr == null)
						break;
					String name = javaClass + "." + curr.getShortName();
					Object o = system.lookup("/Forms/" + name);
					if (o instanceof FormConfig)
						getItemNames(ret, (FormConfig)o, mode, global);
				} catch (Exception notFound) {
					SystemFunctions.doNothing();
				}
				bc = bc.getInheritsFrom();
			}

			//Finally get the parameters corresponding to the javaClass
			getItemNames(ret, Utils.getSystemForm(javaClass), mode, global);
		} catch (Exception err) {
			EclipseAppender.logerror(Messages.getString("LoopConfig.connectorParams.label"), err);
		}

		return ret;
	}

	private void getItemNames(Map<String, FormItemConfig> map, FormConfig formConfig, String mode, FormConfig global) throws Exception {
		if (formConfig == null)
			return;

		for (String section : formConfig.getSectionNames()) {

			String sectionName = section;
			if (sectionName.startsWith("$Mode-"))
				sectionName = mode + sectionName.substring(5);

			FormSection fsec = formConfig.getSection(sectionName);
			if (fsec == null)
				continue;

			for (String str : fsec.getNames()) {
				FormItemConfig fic = fsec.getFormItem(str);
				if (fic == null)
					fic = formConfig.getFormItem(str);
				if (fic == null && str.startsWith("$GLOBAL.")) {
					str = str.substring(8);
					fic = global.getFormItem(str);
				}
				if(!map.containsKey(str))
					map.put(str, fic);
			}
		}

		for (String str : formConfig.getFormItemNames()) {
			FormItemConfig fic = formConfig.getFormItem(str);
			if (fic == null && str.startsWith("$GLOBAL.")) {
				str = str.substring(8);
				fic = global.getFormItem(str);
			}
			if (! map.containsKey(str))
				map.put(str, fic);
		}

		for (String str : formConfig.getTabNames()) {
			getItemNames(map, Utils.getSystemForm(str), mode, global);
		}
	}

	@Override
	public void dispose() {
		if (job != null)
			job.dispose();
		job = null;
		if (discoverProxy != null) {
			discoverProxy.dispose();
			discoverProxy = null;
		}
		super.dispose();
	}

	private class PasteAction extends PasteConfigAction {

		public PasteAction() {
			super(Messages.getString("common.Paste.name")); //$NON-NLS-1$
		}

		@Override
		protected void performPaste(IStructuredSelection selection) {
			SchemaConfig schema = cc.getSchema(mappingMode == WorkMapWidget.MAP_MODE_INPUT);
			for (Iterator<?> i = selection.iterator(); i.hasNext();) {
				Object obj = i.next();
				if (obj instanceof BaseConfiguration) {
					BaseConfiguration b = (BaseConfiguration) obj;
					try {
						if (b instanceof SchemaItemConfig) {
							SchemaItemConfig source = (SchemaItemConfig) b;
							SchemaItemConfig target = schema.newItem(b.getName());
							if (source.getExternalSyntax() != null)
								target.setExternalSyntax(source.getExternalSyntax());
							if (source.getJavaClass() != null)
								target.setJavaClass(source.getJavaClass());
							if (source.getPresenceFlag() != null)
								target.setPresenceFlag(source.getPresenceFlag());
						} else {
							schema.newItem(b.getShortName());
						}
					} catch (Exception e) {
						EclipseAppender.logerror(e.toString(), e, getShell());
					}
				}
			}
		}

		@Override
		protected boolean validatePaste(Object obj) {
			boolean valid = (obj instanceof SchemaItemConfig || obj instanceof AttributeMapItem);
			if (!valid && obj instanceof BaseConfiguration) {
				BaseConfiguration b = (BaseConfiguration) obj;
				if (b.getShortName() != null) {
					valid = true;
				}
			}
			return valid;
		}
	}
}
