/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.TreeColumn;

import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.OperationConfig;
import com.ibm.di.config.interfaces.SchemaConfig;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.function.SystemFunctions;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.TDI;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.editors.BaseEditor;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.util.CustomEditorSettings;
import com.ibm.tdi.eclipse.util.TableColumnResizer;

public class RunOptionsWidget extends BaseWidget {
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	// UI variables
	private TreeViewer workEntryTree;
	private ToolBar tools;
	private Button enableWork;

	// persisted variables
	private int stepMode = 0;
	private Entry workEntry = new Entry();
	private Entry initParams = new Entry();
	private boolean workEnabled;
	private CustomEditorSettings settings;
	private String operation;
	private boolean simulate;
	private Combo combo;

	private Combo runOption;

	private Button enableInitParams;

	private TreeViewer initParamsTree;

	private ToolBar ipTools;

	private boolean initEnabled;

	// protected boolean debugMode;

	private final static String DEFAULT_OP = "";

	private Button enableRegression;
	private Button regressionWrite;
	private Text regressionFile;
	
	public RunOptionsWidget(Composite parent, int style, BaseConfiguration editingConfig, BaseEditor editor,
			CustomEditorSettings settings) {
		super(parent, style, editingConfig, editor);
		this.settings = settings;
		loadSettings();
		createUI();
	}

	private void createUI() {
		GridLayout layout = new GridLayout(1, false);
		setLayout(layout);

		Label label;

		Group group = new Group(this, SWT.NULL);
		group.setText(Messages.getString("RunOptionsWidget.1")); //$NON-NLS-1$
		group.setLayout(new GridLayout(3, false));
		group.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));

		String modes[] = new String[] { Messages.getString("RunOptionsWidget.2"), Messages.getString("RunOptionsWidget.record"),
				Messages.getString("RunOptionsWidget.playback") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		};

		runOption = new Combo(group, SWT.DROP_DOWN | SWT.READ_ONLY);
		for (String str : modes)
			runOption.add(str);
		runOption.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				stepMode = ((Combo) e.widget).getSelectionIndex();
			}
		});
		runOption.select(0);
		setStepMode(settings.getInteger(CustomEditorSettings.STEP_MODE, 0));

		Button sim = new Button(group, SWT.CHECK);
		sim.setText(Messages.getString("RunOptionsWidget.7"));
		sim.setSelection(simulate);
		sim.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				simulate = ((Button) e.widget).getSelection();
			}
		});

		Button log = new Button(group, SWT.CHECK);
		log.setText(Messages.getString("RunOptionsWidget.detailed.log"));
		log.setSelection(((AssemblyLineConfig) getEditingConfig()).getDebug());
		log.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				((AssemblyLineConfig) getEditingConfig()).setDebug(((Button) e.widget).getSelection());
			}
		});

		new Label(group, SWT.LEFT).setText(Messages.getString("RunOptionsWidget.server"));
		final Combo targetServer = new Combo(group, SWT.DROP_DOWN | SWT.READ_ONLY);
		targetServer.setLayoutData(new GridData(SWT.DEFAULT, SWT.DEFAULT));
		targetServer.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (targetServer.getSelectionIndex() != -1) {
					String srv = targetServer.getItem(targetServer.getSelectionIndex()) + ".tdiserver";
					try {
						// -- remove prop if it's equal to the project default
						String projectDefault = Utils.getProjectFor(getEditingConfig()).getPersistentProperty(
								TDI.PROJECT_PREF_SERVER_QNAME);
						if (srv.equals(projectDefault)) {
							settings.removeProperty(CustomEditorSettings.TARGET_SERVER);
							settings.saveSettings();
							return;
						}
					} catch (CoreException e1) {
						SystemFunctions.doNothing();
					}
					settings.setProperty(CustomEditorSettings.TARGET_SERVER, srv, true);
				}
			}
		});
		((GridData) targetServer.getLayoutData()).horizontalSpan = 2;
		updateServerCombo(targetServer);
		String defaultServer = "Default";
		try {
			defaultServer = Utils.getProjectFor(getEditingConfig()).getPersistentProperty(TDI.PROJECT_PREF_SERVER_QNAME);
		} catch (CoreException e1) {
			SystemFunctions.doNothing();
		}

		String prefserver = settings.getString(CustomEditorSettings.TARGET_SERVER, defaultServer);
		if (prefserver == null)
			prefserver = defaultServer;

		if (prefserver.endsWith(".tdiserver"))
			prefserver = prefserver.substring(0, prefserver.lastIndexOf(".tdiserver"));

		int index = targetServer.indexOf(prefserver);
		if (index == -1)
			index = targetServer.indexOf(defaultServer);

		targetServer.select(index);

		group = new Group(this, SWT.NULL);
		group.setText(Messages.getString("RunOptionsWidget.5")); //$NON-NLS-1$
		group.setLayout(new GridLayout(1, false));
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		label = new Label(group, SWT.LEFT);
		label.setText(Messages.getString("RunOptionsWidget.6")); //$NON-NLS-1$
		label.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));

		combo = new Combo(group, SWT.DROP_DOWN | SWT.READ_ONLY);
		for (Object oc : ((AssemblyLineConfig) getEditingConfig()).getOperations().getConfigurations(null)) {
			combo.add(((BaseConfiguration) oc).getShortName());
		}

		if (combo.getItemCount() > 0) {
			int selindex = combo.indexOf(operation);
			if (selindex == -1) {
				if (!DEFAULT_OP.equals(operation))
					combo.add(operation, 0);
				selindex = 0;
			}
			combo.select(selindex);
		}
		combo.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		combo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateWorkFromOperation();
			}
		});
		updateWorkFromOperation();

		enableWork = new Button(group, SWT.CHECK);
		enableWork.setText(Messages.getString("RunOptionsWidget.9")); //$NON-NLS-1$
		enableWork.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		enableWork.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateEnabled();
			}
		});
		enableWork.setSelection(settings.getBoolean(CustomEditorSettings.WORK_ENABLED, false));

		workEntryTree = new TreeViewer(group, SWT.BORDER | SWT.FULL_SELECTION);
		TreeColumn column = new TreeColumn(workEntryTree.getTree(), SWT.LEFT);
		column.setText(Messages.getString("RunOptionsWidget.10")); //$NON-NLS-1$

		TreeViewerColumn tvc = new TreeViewerColumn(workEntryTree, column);
		tvc.setLabelProvider(new CellLabelProvider() {
			private Color defaultFg;

			@Override
			public void update(ViewerCell cell) {
				Object element = cell.getElement();
				String str = "";
				if (defaultFg == null)
					defaultFg = cell.getForeground();

				cell.setForeground(defaultFg);
				if (element instanceof Attribute) {
					str = ((Attribute) element).getName();
					String operation = combo.getSelectionIndex() != -1 ? combo.getText() : null;
					if (operation != null) {
						OperationConfig oc = ((AssemblyLineConfig) getEditingConfig()).getOperation(operation);
						if (oc.getSchema(true).getItemNames().size() > 0 && oc.getSchema(true).getItem(str) == null)
							cell.setForeground(getDisplay().getSystemColor(SWT.COLOR_GRAY));
						else if (oc.getSchema(true).getItemNames().size() == 0
								&& oc.getAttributeMap(true).getAttributeMapItem(str) == null)
							cell.setForeground(getDisplay().getSystemColor(SWT.COLOR_GRAY));
					}
				} else if (element instanceof Entry) {
					str = "Entry";
				}
				cell.setText(str);
			}
		});

		column = new TreeColumn(workEntryTree.getTree(), SWT.LEFT);
		column.setText(Messages.getString("RunOptionsWidget.11")); //$NON-NLS-1$
		tvc = new TreeViewerColumn(workEntryTree, column);
		tvc.setLabelProvider(new AttributeCellLabelProvider());
		tvc.setEditingSupport(new EntryEditorSupport(workEntryTree));

		new TableColumnResizer(workEntryTree.getTree());
		workEntryTree.getTree().setHeaderVisible(true);
		workEntryTree.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		workEntryTree.setContentProvider(new EntryCP());
		workEntryTree.setInput(workEntry);
		workEntryTree.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				if (sel.isEmpty()) {
					tools.getItem(1).setEnabled(false);
					tools.getItem(2).setEnabled(false);
				} else {
					tools.getItem(1).setEnabled(sel.getFirstElement() instanceof Attribute);
					tools.getItem(2).setEnabled(true);
				}
			}
		});

		tools = new ToolBar(group, SWT.FLAT);
		ToolItem item = new ToolItem(tools, SWT.PUSH);
		item.setText(Messages.getString("RunOptionsWidget.12")); //$NON-NLS-1$
		item.setToolTipText(Messages.getString("RunOptionsWidget.13")); //$NON-NLS-1$
		item.setData("cmd", "add"); //$NON-NLS-1$ //$NON-NLS-2$
		item.addSelectionListener(this);

		item = new ToolItem(tools, SWT.PUSH);
		item.setText(Messages.getString("RunOptionsWidget.16")); //$NON-NLS-1$
		item.setToolTipText(Messages.getString("RunOptionsWidget.17")); //$NON-NLS-1$
		item.setData("cmd", "value"); //$NON-NLS-1$ //$NON-NLS-2$
		item.setEnabled(false);
		item.addSelectionListener(this);

		item = new ToolItem(tools, SWT.PUSH);
		item.setText(Messages.getString("RunOptionsWidget.20")); //$NON-NLS-1$
		item.setToolTipText(Messages.getString("RunOptionsWidget.21")); //$NON-NLS-1$
		item.setData("cmd", "del"); //$NON-NLS-1$ //$NON-NLS-2$
		item.addSelectionListener(this);

		tools.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));

		group = new Group(this, SWT.NULL);
		group.setText(Messages.getString("Operations.initialize.label")); //$NON-NLS-1$
		group.setLayout(new GridLayout(1, false));
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		addInitParams(group);

		group = new Group(this, SWT.NULL);
		group.setText(Messages.getString("RunOptionsWidget.Regression.testing")); //$NON-NLS-1$
		group.setLayout(new GridLayout(2, false));
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		addRegressionsParams(group);

		updateEnabled();
		updateInitEnabled();
		updateRegressionEnabled();
	}

	protected void updateWorkFromOperation() {
		String operation = combo.getText();
		if (operation == null)
			return;
		OperationConfig oc = ((AssemblyLineConfig) getEditingConfig()).getOperation(operation);
		if (oc == null)
			return;

		List<String> list = oc.getSchema(true).getItemNames();
		if (list.size() == 0)
			list = oc.getAttributeMap(true).getAttributeNames();

		for (String str : list) {
			if (workEntry.getAttribute(str) == null)
				workEntry.newAttribute(str);
		}

		if (workEntryTree != null)
			workEntryTree.refresh();

	}

	private void addInitParams(Composite group) {
		enableInitParams = new Button(group, SWT.CHECK);
		enableInitParams.setText(Messages.getString("Localized.Enabled")); //$NON-NLS-1$
		enableInitParams.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		enableInitParams.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateInitEnabled();
			}
		});
		enableInitParams.setSelection(settings.getBoolean(CustomEditorSettings.INIT_PARAMS_ENABLED, false));

		initParamsTree = new TreeViewer(group, SWT.BORDER | SWT.FULL_SELECTION);

		TreeColumn column = new TreeColumn(initParamsTree.getTree(), SWT.LEFT);
		column.setText(Messages.getString("RunOptionsWidget.10")); //$NON-NLS-1$
		TreeViewerColumn tvc = new TreeViewerColumn(initParamsTree, column);
		tvc.setLabelProvider(new CellLabelProvider() {
			private Color defaultFg;

			@Override
			public void update(ViewerCell cell) {
				Object element = cell.getElement();
				String str = "";
				if (defaultFg == null)
					defaultFg = cell.getForeground();

				cell.setForeground(defaultFg);
				if (element instanceof Attribute) {
					str = ((Attribute) element).getName();
					SchemaConfig pip = ((AssemblyLineConfig) getEditingConfig()).getPublishedInitParams();
					if (pip != null && pip.size() > 0 && pip.getItem(str) == null) {
						cell.setForeground(getDisplay().getSystemColor(SWT.COLOR_GRAY));
					}
				} else if (element instanceof Entry) {
					str = "Entry";
				} else {
					str = "";
				}
				cell.setText(str);
			}
		});

		column = new TreeColumn(initParamsTree.getTree(), SWT.LEFT);
		column.setText(Messages.getString("RunOptionsWidget.11")); //$NON-NLS-1$

		tvc = new TreeViewerColumn(initParamsTree, column);
		tvc.setLabelProvider(new AttributeCellLabelProvider());
		tvc.setEditingSupport(new EntryEditorSupport(initParamsTree));

		new TableColumnResizer(initParamsTree.getTree());
		
		initParamsTree.getTree().setHeaderVisible(true);
		initParamsTree.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		initParamsTree.setContentProvider(new EntryCP());
		initParamsTree.setInput(initParams);
		initParamsTree.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				if (sel.isEmpty()) {
					ipTools.getItem(1).setEnabled(false);
					ipTools.getItem(2).setEnabled(false);
				} else {
					ipTools.getItem(1).setEnabled(sel.getFirstElement() instanceof Attribute);
					ipTools.getItem(2).setEnabled(true);
				}
			}
		});

		ipTools = new ToolBar(group, SWT.FLAT);
		ToolItem item = new ToolItem(ipTools, SWT.PUSH);
		item.setText(Messages.getString("RunOptionsWidget.12")); //$NON-NLS-1$
		item.setToolTipText(Messages.getString("RunOptionsWidget.13")); //$NON-NLS-1$
		item.setData("cmd", "add"); //$NON-NLS-1$ //$NON-NLS-2$
		item.addSelectionListener(this);

		item = new ToolItem(ipTools, SWT.PUSH);
		item.setText(Messages.getString("RunOptionsWidget.16")); //$NON-NLS-1$
		item.setToolTipText(Messages.getString("RunOptionsWidget.17")); //$NON-NLS-1$
		item.setData("cmd", "value"); //$NON-NLS-1$ //$NON-NLS-2$
		item.setEnabled(false);
		item.addSelectionListener(this);

		item = new ToolItem(ipTools, SWT.PUSH);
		item.setText(Messages.getString("RunOptionsWidget.20")); //$NON-NLS-1$
		item.setToolTipText(Messages.getString("RunOptionsWidget.21")); //$NON-NLS-1$
		item.setData("cmd", "del"); //$NON-NLS-1$ //$NON-NLS-2$
		item.addSelectionListener(this);

		tools.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
	}

	private void addRegressionsParams(Group group) {
		enableRegression = new Button(group, SWT.CHECK);
		enableRegression.setText(Messages.getString("Localized.Enabled"));
		enableRegression.setToolTipText(Messages.getString("RunOptionsWidget.Regression.enabled.tooltip"));
		enableRegression.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false, 2, 1));
		enableRegression.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateRegressionEnabled();
			}
		});
		enableRegression.setSelection(settings.getBoolean(CustomEditorSettings.REGRESSION_ENABLED, false));

		regressionWrite = new Button(group, SWT.CHECK);
		regressionWrite.setText(Messages.getString("RunOptionsWidget.Regression.writing")); //$NON-NLS-1$
		regressionWrite.setToolTipText(Messages.getString("RunOptionsWidget.Regression.writing.tooltip"));
		regressionWrite.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false, 2, 1));
		regressionWrite.setSelection(settings.getBoolean(CustomEditorSettings.REGRESSION_WRITE, false));
		
		Label label = new Label(group, SWT.NONE);
		label.setText(Messages.getString("RunOptionsWidget.Regression.filename"));
		regressionFile = new Text(group, SWT.BORDER);
		regressionFile.setText(settings.getString(CustomEditorSettings.REGRESSION_FILE, ""));
		regressionFile.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
	}


	protected void updateServerCombo(Combo targetServer) {
		try {
			targetServer.removeAll();
			for (IResource res : Utils.getTDIServersProject(true).members()) {
				if (res instanceof IFile && "tdiserver".equals(res.getFileExtension()))
					targetServer.add(res.getName().substring(0, res.getName().lastIndexOf(".")));
			}
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e);
		}
	}

	protected void updateEnabled() {
		workEnabled = enableWork.getSelection();
		workEntryTree.getTree().setEnabled(isWorkEnabled());
		tools.setEnabled(isWorkEnabled());
	}

	protected void updateInitEnabled() {
		initEnabled = enableInitParams.getSelection();
		initParamsTree.getTree().setEnabled(isInitParamsEnabled());
		ipTools.setEnabled(isInitParamsEnabled());
	}

	private void updateRegressionEnabled() {
		boolean enabled = enableRegression.getSelection();
		regressionWrite.setEnabled(enabled);
		regressionFile.setEnabled(enabled);
	}

	private boolean isInitParamsEnabled() {
		return initEnabled;
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		String cmd = (String) ((ToolItem) (e.widget)).getData("cmd"); //$NON-NLS-1$
		TreeViewer tree = ((ToolItem) e.widget).getParent() == ipTools ? initParamsTree : workEntryTree;
		Entry entry = ((ToolItem) e.widget).getParent() == ipTools ? initParams : workEntry;

		if ("add".equals(cmd)) { //$NON-NLS-1$
			InputDialog id = new InputDialog(getShell(),
					Messages.getString("RunOptionsWidget.26"), Messages.getString("RunOptionsWidget.27"), "", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			if (id.open() == Window.OK) {
				entry.newAttribute(id.getValue());
				tree.refresh();
			}
		} else if ("value".equals(cmd)) { //$NON-NLS-1$

			InputDialog id = new InputDialog(getShell(),
					Messages.getString("RunOptionsWidget.30"), Messages.getString("RunOptionsWidget.31"), "", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			if (id.open() == Window.OK) {
				Attribute a = (Attribute) ((IStructuredSelection) tree.getSelection()).getFirstElement();
				a.addValue(id.getValue());
				tree.refresh();
			}
		} else if ("del".equals(cmd)) { //$NON-NLS-1$
			Object obj = ((IStructuredSelection) tree.getSelection()).getFirstElement();
			if (obj instanceof Attribute) {
				entry.removeAttribute(((Attribute) obj).getName());
				tree.remove(obj);
			} else if(obj instanceof AttributeValue) {
				AttributeValue av = (AttributeValue) obj;
				av.attribute.removeValueAt(av.index);
				tree.remove(obj);
			} else {
				Object parent = tree.getTree().getSelection()[0].getParentItem().getData();
				if (parent instanceof Attribute) {
					((Attribute) parent).removeValue(obj);
					tree.remove(obj);
				}
			}
		}
	}

	public int getStepMode() {
		return stepMode;
	}

	public void setStepMode(int stepMode) {
		if (stepMode < runOption.getItemCount()) {
			runOption.select(stepMode);
			this.stepMode = stepMode;
		}
	}

	public Entry getWorkEntry() {
		return workEntry;
	}

	public void setWorkEntry(Entry workEntry) {
		this.workEntry = workEntry;
		workEntryTree.setInput(workEntry);
	}

	public boolean isWorkEnabled() {
		return workEnabled;
	}

	public void setWorkEnabled(boolean workEnabled) {
		enableWork.setSelection(workEnabled);
	}

	public void saveSettings() {
		settings.setProperty(CustomEditorSettings.STEP_MODE, stepMode);
		settings.setProperty(CustomEditorSettings.WORK_ENABLED, workEnabled);
		settings.setProperty(CustomEditorSettings.WORK_ENTRY, workEntry);
		settings.setProperty(CustomEditorSettings.INIT_PARAMS_ENABLED, initEnabled);
		settings.setProperty(CustomEditorSettings.INIT_PARAMS, initParams);
		settings.setProperty(CustomEditorSettings.AL_OPERATION, combo.getText());
		settings.setProperty(CustomEditorSettings.AL_SIMULATE, String.valueOf(simulate));
		// settings.setProperty(CustomEditorSettings.DEBUG_MODE, debugMode);
		settings.setProperty(CustomEditorSettings.REGRESSION_ENABLED, enableRegression.getSelection());
		settings.setProperty(CustomEditorSettings.REGRESSION_WRITE, regressionWrite.getSelection());
		settings.setProperty(CustomEditorSettings.REGRESSION_FILE, regressionFile.getText());
	}

	public boolean isSimulate() {
		return simulate;
	}

	private void loadSettings() {
		stepMode = settings.getInteger(CustomEditorSettings.STEP_MODE, 0);

		workEnabled = settings.getBoolean(CustomEditorSettings.WORK_ENABLED, false);
		workEntry = settings.getEntry(CustomEditorSettings.WORK_ENTRY);
		if (workEntry == null)
			workEntry = new Entry();

		initEnabled = settings.getBoolean(CustomEditorSettings.INIT_PARAMS_ENABLED, false);
		initParams = settings.getEntry(CustomEditorSettings.INIT_PARAMS);
		if (initParams == null)
			initParams = new Entry();

		SchemaConfig pip = ((AssemblyLineConfig) getEditingConfig()).getPublishedInitParams();
		if (pip != null) {
			for (String str : pip.getItemNames()) {
				if (initParams.getAttribute(str) == null) {
					String value = pip.getItem(str).getExternalSyntax();
					if (value == null)
						value = "string";
					initParams.newAttribute(str).addValue(value);
				}
			}
		}

		operation = settings.getString(CustomEditorSettings.AL_OPERATION, DEFAULT_OP); //$NON-NLS-1$
		simulate = settings.getBoolean(CustomEditorSettings.AL_SIMULATE, false);
	}

	private static class EntryCP implements ITreeContentProvider {

		public Object[] getElements(Object elem) {
			ArrayList<Object> list = new ArrayList<Object>();
			if (elem instanceof Entry) {
				ArrayList<String> names = new ArrayList<String>();
				for (String str : ((Entry) elem).getAttributeNames())
					names.add(str);
				Collections.sort(names);
				for(String str : names)
					list.add(((Entry) elem).getAttribute(str));
			} else if (elem instanceof Attribute) {
				Attribute attr = (Attribute) elem;
				// first value is shared with attribute name row
				for (int i = 1; i < attr.size(); i++) {
					list.add(new AttributeValue(attr, i, attr.getValue(i)));
				}
			}

			return list.toArray();
		}

		public Object[] getChildren(Object parentElement) {
			return getElements(parentElement);
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			if (element instanceof Entry)
				return ((Entry) element).size() > 0;

			if (element instanceof Attribute)
				return ((Attribute) element).size() > 1;

			return false;
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// TODO Auto-generated method stub

		}

	}

	private static class AttributeValue {
		public Attribute attribute;
		public int index;
		public Object value;

		public AttributeValue(Attribute attribute, int index, Object value) {
			super();
			this.attribute = attribute;
			this.index = index;
			this.value = value;
		}

		@Override
		public int hashCode() {
			return attribute.hashCode() ^ index;
		}

		@Override
		public boolean equals(Object obj) {
			if(obj instanceof AttributeValue) {
				AttributeValue av = (AttributeValue) obj;
				return av.attribute.equals(attribute) && av.index == index;
			} else {
				return false;
			}
		}
	}
	
	private static class EntryEditorSupport extends EditingSupport {
		
		public EntryEditorSupport(ColumnViewer viewer) {
			super(viewer);
		}

		private TextCellEditor editor;
		@Override
		protected void setValue(Object element, Object value) {
			if(element instanceof Attribute) {
				Attribute attr = (Attribute) element;
				if(attr.size() > 0)
					attr.setValue(0, value);
				else if(attr.size() == 0)
					attr.addValue(value);
			} else if (element instanceof AttributeValue) {
				AttributeValue av = (AttributeValue) element;
				av.attribute.setValue(av.index, value);
			}
			getViewer().refresh(element, true);
		}

		@Override
		protected Object getValue(Object element) {
			if(element instanceof Attribute) {
				Attribute attr = (Attribute) element;
				if(attr.size() > 0)
					return attr.getValue(0).toString();
			} else if (element instanceof AttributeValue) {
				return ((AttributeValue)element).value.toString();
			}
			return "";
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			if(editor == null)
				editor = new TextCellEditor((Composite) getViewer().getControl());
			return editor;
		}

		@Override
		protected boolean canEdit(Object element) {
			return element instanceof Attribute || element instanceof AttributeValue;
		}
		
	}

	private static class AttributeCellLabelProvider extends CellLabelProvider {
		@Override
		public void update(ViewerCell cell) {
			Object element = cell.getElement();
			String str = "";
			if (element instanceof Attribute) {
				Attribute attr = (Attribute) element;
				if (attr.size() > 0)
					str = attr.getValue();
			} else if (element instanceof AttributeValue) {
				AttributeValue attr = (AttributeValue) element;
				str = attr.value.toString();
			} else if (element instanceof Entry) {
				str = "";
			} else {
				str = element.toString();
			}
			cell.setText(str);
		}
	}
}
