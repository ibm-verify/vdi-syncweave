/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.easyetl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.CheckedTreeSelectionDialog;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.swt.IFocusService;

import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.AttributeMapItem;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.DefaultConfigChangeListener;
import com.ibm.di.config.interfaces.HookConfig;
import com.ibm.di.config.interfaces.LinkCriteriaConfig;
import com.ibm.di.config.interfaces.LinkCriteriaItem;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.di.config.interfaces.MetamergeConfigChangeListener;
import com.ibm.di.config.interfaces.SchemaConfig;
import com.ibm.di.config.interfaces.SchemaItemConfig;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.function.UserFunctions;
import com.ibm.di.script.ScriptEngine;
import com.ibm.di.server.TaskStatistics;
import com.ibm.di.util.HookTree;
import com.ibm.icu.util.StringTokenizer;
import com.ibm.jscript.IValue;
import com.ibm.tdi.easyetl.ALDebugger.ALDebuggerEvent;
import com.ibm.tdi.easyetl.ALDebugger.ALDebuggerEventListener;
import com.ibm.tdi.eclipse.Activator;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.actions.CopyTableContentsAction;
import com.ibm.tdi.eclipse.actions.TDIHelpMenuAction;
import com.ibm.tdi.eclipse.console.AssemblyLineConsole;
import com.ibm.tdi.eclipse.editors.BaseEditor;
import com.ibm.tdi.eclipse.editors.RunAssemblyLineEditor;
import com.ibm.tdi.eclipse.editors.RunAssemblyLineInput;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.natures.TDINature;
import com.ibm.tdi.eclipse.preferences.PreferenceConstants;
import com.ibm.tdi.eclipse.providers.AssemblyLineContentProvider3;
import com.ibm.tdi.eclipse.providers.ConfigLabelProvider;
import com.ibm.tdi.eclipse.server.RMIServerAPI;
import com.ibm.tdi.eclipse.views.EntryCollectorView;
import com.ibm.tdi.eclipse.widget.BaseWidget;
import com.ibm.tdi.eclipse.widget.SimpleTextEditor;

public class ColumnDataFlow extends BaseWidget implements ALDebuggerEventListener {
	/**
	 * 
	 */
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * ETL view - one iterator and one output with simple mapping
	 */
	public final static int VIEW_ETL = 0;

	/**
	 * Column view - one table with two columns for each connector (attr/value)
	 */
	public final static int VIEW_COLUMNS = 1;

	/*
	 * Column identifiers
	 */
	private final static int SOURCE_ATTRIBUTE = 0;
	private final static int SOURCE_VALUE = 1;
	private final static int TARGET_ATTRIBUTE = 2;
	private final static int XFORM_SCRIPT = 3;
	private final static int TARGET_VALUE = 4;

	/*
	 * Button labels
	 */
	private static String[] buttons = new String[] { Messages.getString("ColumnDataFlow_next_record"),
			Messages.getString("ColumnDataFlow_run"), Messages.getString("Debugger.toolbar.Stop.label") };

	private static String pauseLabel = Messages.getString("Debugger.toolbar.Pause.label");
	private static String continueLabel = Messages.getString("Debugger.toolbar.Continue.label");

	/*
	 * Button icons
	 */
	private static String[] icons = new String[] { null, "Run", "Stop" };

	/*
	 * Column labels
	 */
	private static String[] columns = new String[] { Messages.getString("ColumnDataFlow_source_attribute"),
			Messages.getString("ColumnDataFlow_source_value") };

	/*
	 * Button labels for non-ETL mode
	 */
	private static String[] ds_buttons = new String[] { Messages.getString("ColumnDataFlow_next"),
			Messages.getString("ColumnDataFlow_run"), Messages.getString("Debugger.toolbar.Stop.label") };

	private static final int NEXT_BUTTON_INDEX = 0;
	private static final int RUN_BUTTON_INDEX = 1;
	private static final int STOP_BUTTON_INDEX = 2;

	private Label stats;

	private ArrayList<Button> buttonControls = new ArrayList<Button>();

	private ProgressBar progress;

	private int viewMode = VIEW_COLUMNS;

	private Hashtable<String, TableViewer> viewers = new Hashtable<String, TableViewer>();

	private ArrayList<String> components = new ArrayList<String>();

	protected Exception lastError;

	private ScrolledComposite sc;

	private WorkListWidget workList;

	private Composite buttonRow;

	protected Entry outputConnectorEntry;

	private ETLSourceTable sourceTable;

	private ETLTargetTable targetTable;

	private ETLTransformationTable transformTable;

	private ALDebugger debugger;

	protected Entry currentEntry;

	private AssemblyLineConsole console = new AssemblyLineConsole(Messages.getString("StartLocalServerJob.title"));

	private OutlineWidget outline;

	private Button xform;

	private boolean stepping;

	// -- need this one for non-UI thread access to visibility status
	private ArrayList<String> hiddenComponents = new ArrayList<String>();

	// -- used to enable/disable run and next buttons
	private MetamergeConfigChangeListener configListener;

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            parent container
	 * @param style
	 *            styles
	 * @param editingConfig
	 *            assemblyline config
	 * @param editor
	 *            Editor in which this widget runs (must be RunALEditor or
	 *            ETLEditor)
	 */
	public ColumnDataFlow(Composite parent, int style, BaseConfiguration editingConfig, BaseEditor editor) {
		super(parent, style, editingConfig, editor);
		try {
			String server = null;
			if (editor.getTDIConfigFile() != null)
				server = Utils.getTDIServer(editor.getTDIConfigFile());
			else if (editingConfig.getMetamergeConfig() instanceof TDIConfigurationFile)
				server = Utils.getTDIServer(((TDIConfigurationFile) editingConfig.getMetamergeConfig()).getFile());
			else
				server = TDINature.DEFAULT_SERVER_NAME;

			RMIServerAPI api = (RMIServerAPI) RMIServerAPI.createInstance(server);
			debugger = new ALDebugger(api);
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e, getShell());
		}
		createUI();
	}

	/**
	 * Returns the debugger object used to run/step assemblylines
	 * 
	 * @return
	 */
	public ALDebugger getDebugger() {
		return debugger;
	}

	/**
	 * Returns the current assemblyline config
	 * 
	 * @return
	 */
	private AssemblyLineConfig getALC() {
		return (AssemblyLineConfig) getEditingConfig();
	}

	/**
	 * Create the UI for ETL or ColumnView
	 */
	private void createUI() {
		setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
		setLayout(new FillLayout());

		Composite g = new Composite(this, SWT.BORDER);
		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = 15;
		layout.marginWidth = 15;
		g.setLayout(layout);
		g.setBackground(getBackground());

		AssemblyLineConfig alc = (AssemblyLineConfig) getEditingConfig();

		if (getEditor() instanceof ETLEditor) {
			viewMode = VIEW_ETL;
		} else {
			viewMode = VIEW_COLUMNS;
		}

		switch (viewMode) {
		case VIEW_ETL:
			createButtonRow(g);
			createETLView(g);
			break;
		case VIEW_COLUMNS:
			SashForm gg = new SashForm(g, SWT.HORIZONTAL);
			gg.setLayoutData(new GridData(GridData.FILL_BOTH));

			final SashForm left = new SashForm(gg, SWT.VERTICAL);

			outline = new OutlineWidget(left, getALC());

			workList = new WorkListWidget(left, SWT.NONE);
			workList.setLayoutData(new GridData(SWT.DEFAULT, 100));
			workList.setBackground(getBackground());

			left.addControlListener(new ControlListener() {
				public void controlResized(ControlEvent e) {
					int height = outline.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
					int max = left.getClientArea().height / 2;
					if (height > max)
						height = max;

					left.setWeights(new int[] { height, max });
					left.removeControlListener(this);
				}

				public void controlMoved(ControlEvent e) {
				}
			});
			left.setWeights(new int[] { 50, 50 });

			createColumnsView(alc, gg);

			gg.setWeights(new int[] { 20, 80 });
			break;
		}

		// -- Let debugger now where to break and what to watch
		setBreakpoints();

		// -- add the stats label to the right of buttons
		stats = new Label(buttonRow, SWT.LEFT);
		stats.setText("");
		stats.setBackground(buttonRow.getBackground());
		RowData rd = new RowData();
		rd.width = 300;
		stats.setLayoutData(rd);

		// -- indeterminate progress bar when AL is running
		progress = new ProgressBar(buttonRow, SWT.SMOOTH | SWT.INDETERMINATE | SWT.HORIZONTAL);
		progress.setVisible(false);

		// -- fill horizontal
		buttonRow.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	/**
	 * Sets breakpoints based on the list of components and the watch list
	 */
	private void setBreakpoints() {
		// -- set a break at default_ok so we can update the lastConn object
		for (String str : components)
			debugger.addBreakpoint(str + ".default_ok");

		// -- add lastConn for all components
		for (String str : components)
			debugger.addWatchExpression(str + ".lastConn");

		debugger.addWatchExpression("work");

		debugger.addEventListener(this);
	}

	/**
	 * Processes the debugger event by updating tables and enabling/disabling
	 * buttons.
	 * 
	 * @param event
	 */
	public void handleEvent(final ALDebuggerEvent event) {

		String componentName = event.getName();
		if (componentName != null && componentName.indexOf(".") != -1)
			componentName = componentName.substring(0, componentName.indexOf("."));
		final String comp = componentName;

		switch (event.getEvent()) {
		case ALDebuggerEvent.BREAKPOINT:
			highlightComponent(componentName);
			getDisplay().syncExec(new Runnable() {
				public void run() {
					setInput(getALC());
					stats.setText(Messages.getMessage("ColumnDataFlow_al_stats", debugger.cycleCounter + 1, (System
							.currentTimeMillis() - debugger.getStartTime()) / 1000));
					if (isComponentVisible(comp) && isStepping() && debugger.getRunUntil() == null)
						enableButtons(true);
					else
						runContinue(isStepping(), debugger.getRunUntil());
				}
			});
			break;

		case ALDebuggerEvent.ERROR:
			final Exception error = (Exception) event.getValue();
			getDisplay().syncExec(new Runnable() {
				public void run() {
					enableButtons(true);
					setInput(getALC());
					String msg = error.toString();
					if (msg == null)
						msg = "";
					else if (msg.startsWith("java.lang.Exception: "))
						msg = msg.substring(21);
					else if (msg.startsWith("java.lang."))
						msg = msg.substring(10);
					EclipseAppender.logerror(Messages.getMessage("ColumnDataFlow_al_failed", msg), error, getShell());
					terminateDebugger();
				}
			});

			break;

		case ALDebuggerEvent.EXPRESSION:
			Object value = event.getValue();
			if (value instanceof Entry && isComponentVisible(comp)) {
				getDisplay().syncExec(new Runnable() {
					public void run() {
						appendWorkEntry(event);
					}
				});
			}

			if ("work".equals(comp) && value instanceof Entry) {
				currentEntry = (Entry) value;
				if (getViewMode() == VIEW_ETL) {
					getDisplay().syncExec(new Runnable() {
						public void run() {
							sourceTable.getTable().refresh();
						}
					});
				} else {
					getDisplay().syncExec(new Runnable() {
						public void run() {
							workList.setInput(currentEntry);
						}
					});
				}
			} else if (getViewMode() == VIEW_ETL) {
				String outConn = getALC().getDataFlowComponents().getConfig(0).getShortName();
				if (outConn.equals(comp) && value instanceof Entry) {
					outputConnectorEntry = (Entry) value;
					getDisplay().syncExec(new Runnable() {
						public void run() {
							targetTable.getTable().refresh();
						}
					});
				}
			} else if (getViewMode() == VIEW_COLUMNS && comp != null && viewers.get(comp) != null) {
				getDisplay().syncExec(new Runnable() {
					public void run() {
						viewers.get(comp).setInput(event.getValue());
					}
				});
			}
			break;

		case ALDebuggerEvent.MESSAGE:
			try {
				String str = event.getValue() + "\n";
				console.logmsg(str);
				if (logger != null)
					logger.logmessage(str);
			} catch (IOException e) {
				SystemFunctions.doNothing();
			}
			break;

		case ALDebuggerEvent.TERMINATED:
			debugger.removeEventListener(this);
			getDisplay().syncExec(new Runnable() {
				public void run() {
					enableButtons(true);
					TaskStatistics alstats = null;
					try {
						alstats = debugger.getAssemblyLineHandle().getStatistics();
					} catch (Exception e) {
						alstats = new TaskStatistics();
						alstats.exception(e);
					}
					String err = "";
					if (alstats.ex != null) {
						err = "\n" + alstats.ex.getMessage();
					}
					String msg;
					
					// -- Don't show adds when we simulate (simulate -> script connector with dump entry)
					int adds = alstats.add;
					try {
						ConnectorConfig cc = (ConnectorConfig) getALC().getDataFlowComponents().getConfig(0);
						if (ConnectorConfig.DISABLED_STATE.equals(cc.getState())) {
							adds = 0;
						}
					} catch(Throwable t) {
						SystemFunctions.doNothing();
					}
					
					// -- Generate end-of-run message
					if(alstats.lookup > 0)
						msg = Messages.getMessage("ColumnDataFlow_al_finished_update", new Object[] { alstats.get, adds,
							alstats.mod, alstats.nochange, alstats.err, err });
					else
						msg = Messages.getMessage("ColumnDataFlow_al_finished", new Object[] { alstats.get, adds,
								alstats.err, err });
						
					// -- If user hit Stop then we don't show the end dialog
					if (err.indexOf("CTGDIS590E") == -1)
						MessageDialog.openInformation(getShell(), getALC().getShortName(), msg);
				}
			});
			debugger.terminate();
			if (logger != null) {
				logger.dispose();
				logger = null;
			}
			getDisplay().syncExec(new Runnable() {
				public void run() {
					buttonControls.get(RUN_BUTTON_INDEX).setText(buttons[RUN_BUTTON_INDEX]);
					updateRunNextButtons();
				}
			});
			break;
		}
	}

	protected void terminateDebugger() {
		debugger.terminate();
	}

	/**
	 * Appends the work entry in event to the collector view (if visible)
	 * 
	 * @param event
	 */
	protected void appendWorkEntry(ALDebuggerEvent event) {
		try {
			EntryCollectorView view = (EntryCollectorView) getEditor().getSite().getPage().findView(EntryCollectorView.VIEW_ID);
			if (view != null) {
				if (getViewMode() == VIEW_ETL) {
					view.setComponentDisplayName(getEditor(), "work", "Input");
				}
				view.addEntry(getEditor(), event, debugger.cycleCounter);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Clears the collector view (if visible)
	 * 
	 * @param event
	 */
	protected void clearAll() {
		try {
			EntryCollectorView view = (EntryCollectorView) getEditor().getSite().getPage().findView(EntryCollectorView.VIEW_ID);
			if (view != null) {
				view.clearAll(getEditor());
			}
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e);
		}
	}

	
	/**
	 * Returns true if there is an active assemblyline running/paused.
	 * 
	 * @return
	 */
	public boolean isRunning() {
		return debugger.isStarted();
	}

	/**
	 * Creates the buttons for the current view (etl, column)
	 * 
	 * @param parent
	 */

	private void createButtonRow(Composite parent) {
		buttonRow = new Composite(parent, SWT.NONE);
		buttonRow.setLayout(new RowLayout(SWT.HORIZONTAL));
		String[] btns = buttons;
		if (getViewMode() != VIEW_ETL)
			btns = ds_buttons;
		else
			buttonRow.setBackground(getBackground());

		for (int i = 0; i < btns.length; i++) {
			String str = btns[i];
			Button b = new Button(buttonRow, SWT.PUSH);
			b.setText(str);
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					executeCommand(((Button) e.widget).getText());
				}
			});
			if (icons[i] != null)
				b.setImage(Activator.getImage(icons[i]));
			buttonControls.add(b);
		}

		enableButtons(true);
		updateRunNextButtons();

		configListener = new MetamergeConfigChangeListener() {
			public void configurationChanged(MetamergeConfigChange arg0) {
				if (arg0.getSource() instanceof ConnectorConfig) {
					getDisplay().syncExec(new Runnable() {
						public void run() {
							updateRunNextButtons();
						}
					});
				}
			}
		};
		getALC().addListener(configListener);
	}

	/**
	 * Disables Run/Next if the configuration isn't complete enough to run the
	 * AL.
	 */
	private void updateRunNextButtons() {
		buttonControls.get(STOP_BUTTON_INDEX).setEnabled(isRunning());
		// -- only update run/next when we are idle (e.g. not already running)
		if (getViewMode() == VIEW_ETL && !isRunning()) {
			boolean disableRunNext = true;
			try {
				disableRunNext = getALC().getEntryFeedComponents().getConfig(0).getInheritsFromRef().equals(
						BaseConfiguration.INHERIT_PARENT);
			} catch (Exception e) {
				disableRunNext = true;
			}
//			if (!disableRunNext) {
//				try {
//					disableRunNext = getALC().getDataFlowComponents().getConfig(0).getInheritsFrom().equals(
//							BaseConfiguration.INHERIT_PARENT);
//				} catch (Exception e) {
//					disableRunNext = true;
//				}
//			}
			buttonControls.get(RUN_BUTTON_INDEX).setEnabled(!disableRunNext);
			buttonControls.get(NEXT_BUTTON_INDEX).setEnabled(!disableRunNext);
		}
	}

	/**
	 * Returns true if the component is visible (column view mode)
	 * 
	 * @param str
	 * @return
	 */
	protected boolean isComponentVisible(String str) {
		if (getViewMode() == VIEW_ETL)
			return true;

		if (str == null)
			return false;

		return !hiddenComponents.contains(str);
	}

	/**
	 * Echoes the selection index from the source table to the two other ETL
	 * view tables
	 * 
	 * @param source
	 */
	private boolean ignoreSync = false;

	private ALFileLogger logger;

	protected void syncTables(Object source, int selectionIndex) {
		if (ignoreSync)
			return;
		ignoreSync = true;
		if (source != sourceTable)
			sourceTable.select(selectionIndex);
		if (source != transformTable)
			transformTable.select(selectionIndex);
		if (source != targetTable)
			targetTable.select(selectionIndex);
		ignoreSync = false;
	}

	/**
	 * Changes the visibility of the named component
	 * 
	 * @param component
	 * @param visible
	 */
	protected void setComponentVisible(String component, boolean visible) {
		Composite comp = viewers.get(component).getTable().getParent();
		GridData rd = (GridData) comp.getLayoutData();
		rd.exclude = !visible;
		comp.setVisible(visible);
		comp.getParent().layout(true, true);
		if (!visible)
			hiddenComponents.add(component);
		else
			hiddenComponents.remove(component);
	}

	/**
	 * Sets the background color of all components, focusComponent gets
	 * COLOR_INFO_BACKGROUP and components after focusComponent are grayed.
	 * 
	 * @param focusComponent
	 */
	protected void updateBackgroundColors(String focusComponent) {
		Control[] children = ((Composite) sc.getContent()).getChildren();
		for (Control c : children) {
			c.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
			if (c == viewers.get(focusComponent).getTable().getParent()) {
				c.setBackground(getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
				c.setFocus();
			}
		}

		boolean afterFocus = false;
		for (int i = 0; i < components.size(); i++) {
			TableViewer comp = viewers.get(components.get(i));
			comp.getTable().setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_BLACK));
			if (components.get(i).equals(focusComponent))
				afterFocus = true;
			else if (afterFocus)
				comp.getTable().setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_GRAY));
		}
	}

	/**
	 * Creates the button and handler to select which components are visible.
	 */
	protected void createColumnsViewCompSelectorButton(Composite parent) {
		Button hide = new Button(parent, SWT.CLOSE);
		hide.setText(Messages.getString("ColumnDataFlow_show_hide"));
		hide.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				LabelProvider labelProvider = new LabelProvider();
				ITreeContentProvider contentProvider = new ITreeContentProvider() {
					public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
					}

					public void dispose() {
					}

					public Object[] getElements(Object inputElement) {
						if (inputElement == components)
							return components.toArray();
						else
							return new Object[0];
					}

					public boolean hasChildren(Object element) {
						return element == components;
					}

					public Object getParent(Object element) {
						return null;
					}

					public Object[] getChildren(Object parentElement) {
						return new Object[0];
					}
				};
				CheckedTreeSelectionDialog dlg = new CheckedTreeSelectionDialog(getShell(), labelProvider, contentProvider);
				dlg.setInput(components);
				dlg.setTitle(Messages.getString("ColumnDataFlow_show_hide"));
				dlg.setMessage(Messages.getString("ColumnDataFlow_show_hide_tooltip"));

				// Provide initial selections
				ArrayList<String> initialSelections = new ArrayList<String>();
				for (String str : components) {
					if (isComponentVisible(str)) {
						initialSelections.add(str);
					}
				}
				dlg.setInitialElementSelections(initialSelections);
				if (dlg.open() == Window.OK) {
					ArrayList<String> temp = new ArrayList<String>();
					temp.addAll(components);
					for (Object obj : dlg.getResult()) {
						temp.remove(obj);
						setComponentVisible(obj.toString(), true);
					}
					for (String str : temp) {
						setComponentVisible(str, false);
					}
				}
			}
		});
	}

	/**
	 * Creates the UI controls for VIEW_COLUMN mode.
	 * 
	 * @param alc
	 * @param parent
	 */
	private void createColumnsView(AssemblyLineConfig alc, Composite parent) {
		List<ConnectorConfig> connectorList = new ArrayList<ConnectorConfig>();
		List<BaseConfiguration> list = alc.getEntryFeedComponents().getConfigurations(null);
		alc.getDataFlowComponents().getConfigurations(list);
		for (BaseConfiguration bc : list) {
			if (bc instanceof ConnectorConfig && bc.getEnabled()) {
				connectorList.add((ConnectorConfig) bc);
			}
		}

		BaseWidget base = new BaseWidget(parent, 0);
		base.setLayout(new FillLayout());
		base.setLayoutData(new GridData(GridData.FILL_BOTH));

		Form frm = base.createForm(base, null);
		frm.setText(Messages.getString("ColumnDataFlow_dataflow_and_mapping"));
		frm.getBody().setLayout(new FillLayout());

		createButtonRow(frm.getHead());
		frm.setHeadClient(buttonRow);

		sc = new ScrolledComposite(frm.getBody(), SWT.V_SCROLL | SWT.H_SCROLL);
		sc.setLayout(new FillLayout());
		sc.setShowFocusedControl(true);

		Composite content = new Composite(sc, SWT.NONE);
		GridLayout layout = new GridLayout(connectorList.size(), true);
		layout.marginWidth = 25;
		layout.marginHeight = 25;
		layout.horizontalSpacing = 25;
		content.setLayout(layout);
		content.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));

		for (ConnectorConfig cc : connectorList) {
			Composite col = createColumnTable(cc, content);
			col.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		}

		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		sc.setContent(content);
		sc.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				updateMinSize();
			}
		});

		updateMinSize();
	}

	/**
	 * Updates the min size of the ScrolledComposite to refresh scroll bars
	 */
	private void updateMinSize() {
		Point size = sc.getContent().computeSize(SWT.DEFAULT, sc.getClientArea().height);
		sc.setMinSize(size);
	}

	/**
	 * Creates a Group with controls to display a component in column view.
	 * 
	 * @param cc
	 * @param parent
	 * @return
	 */
	private Composite createColumnTable(ConnectorConfig cc, Composite parent) {

		final Group g = new Group(parent, SWT.SHADOW_OUT);
		g.setText(cc.getShortName() + " (" + Utils.externalMode(cc.getMode()) + ")");
		g.setLayout(new GridLayout(1, false));

		ToolBar bar = new ToolBar(g, SWT.HORIZONTAL);
		bar.setLayoutData(new GridData(SWT.RIGHT, SWT.DEFAULT, true, false));
		ToolItem runHere = new ToolItem(bar, SWT.PUSH);
		runHere.setImage(Activator.getImage("runtoline"));
		runHere.setToolTipText(Messages.getString("Debugger.Run.and.break"));
		runHere.setData("name", cc.getShortName());
		runHere.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				runAssemblyLine(true, (String) e.widget.getData("name") + ".default_ok");
			}
		});

		ToolItem hide = new ToolItem(bar, SWT.PUSH);
		hide.setToolTipText(Messages.getString("ColumnDataFlow_hide_button_tooltip"));
		hide.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_DELETE));
		hide.setData("name", cc.getShortName());
		hide.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setComponentVisible((String) e.widget.getData("name"), false);
			}
		});

		TableViewer table = new TableViewer(g, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);

		table.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		table.getTable().setHeaderVisible(true);
		table.getTable().setLinesVisible(true);

		ArrayList<TableViewerColumn> columns = new ArrayList<TableViewerColumn>();
		TableViewerColumn tvc;
		tvc = new TableViewerColumn(table, SWT.LEFT);
		if (Utils.isOutputConnector(cc))
			tvc.getColumn().setText(Messages.getString("ColumnDataFlow_target_attribute"));
		else
			tvc.getColumn().setText(Messages.getString("ColumnDataFlow_source_attribute"));
		tvc.getColumn().setWidth(100);
		columns.add(tvc);

		tvc = new TableViewerColumn(table, SWT.LEFT);
		if (Utils.isOutputConnector(cc))
			tvc.getColumn().setText(Messages.getString("ColumnDataFlow_target_value"));
		else
			tvc.getColumn().setText(Messages.getString("ColumnDataFlow_source_value"));
		tvc.getColumn().setWidth(100);
		columns.add(tvc);

		table.setData("columns", columns);

		table.getTable().setFont(JFaceResources.getTextFont());

		table.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof Entry) {
					Entry e = (Entry) inputElement;
					List<String> coll = new ArrayList<String>();
					coll.addAll(e.getAttributeCollection());
					Collections.sort(coll);
					ArrayList<Attribute> list = new ArrayList<Attribute>();
					for (String str : coll) {
						list.add(e.getAttribute(str));
					}
					return list.toArray();
				}
				return new Object[] {};
			}

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
		});

		table.setLabelProvider(new ITableLabelProvider() {
			public String getColumnText(Object element, int columnIndex) {
				Attribute attr = (Attribute) element;
				if (columnIndex == 0)
					return attr.getName();
				else
					return attr.getValue();
			}

			public Image getColumnImage(Object element, int columnIndex) {
				return null;
			}

			public boolean isLabelProperty(Object element, String property) {
				return false;
			}

			public void addListener(ILabelProviderListener listener) {
			}

			public void dispose() {
			}

			public void removeListener(ILabelProviderListener listener) {
			}
		});

		viewers.put(cc.getShortName(), table);
		components.add(cc.getShortName());

		return g;
	}

	/**
	 * Creates the ETL view for simple two-component assemblylines.
	 * 
	 * @param parent
	 */
	private void createETLView(Composite parent) {

		AssemblyLineConfig alc = (AssemblyLineConfig) getEditingConfig();
		ConnectorConfig cc = (ConnectorConfig) alc.getDataFlowComponents().getConfig(0);

		// -- add input/output to components list
		components.add(cc.getShortName());

		final Composite etlContainer = new Composite(parent, 0);
		etlContainer.setBackground(parent.getBackground());
		etlContainer.setLayout(new StackLayout());
		etlContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		final Composite nomap = new Composite(etlContainer, 0);
		nomap.setLayout(new GridLayout(1,false));
		nomap.setBackground(parent.getBackground());
		Label pl = new Label(nomap, SWT.CENTER);
		pl.setText(Messages.getString("ColumnDataFlow_config_msg"));
		pl.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));

		final Composite c = new Composite(etlContainer, 0);
		c.setBackground(parent.getBackground());
		Utils.setGridLayout(c, 3, true);
		c.setLayoutData(new GridData(GridData.FILL_BOTH));
		((GridLayout) c.getLayout()).horizontalSpacing = 15;
		((GridLayout) c.getLayout()).marginWidth = 10;
		((GridLayout) c.getLayout()).marginHeight = 10;

		sourceTable = new ETLSourceTable(c, alc);
		sourceTable.setLayoutData(new GridData(GridData.FILL_BOTH));

		transformTable = new ETLTransformationTable(c, alc);
		transformTable.setLayoutData(new GridData(GridData.FILL_BOTH));

		targetTable = new ETLTargetTable(c, alc);
		targetTable.setLayoutData(new GridData(GridData.FILL_BOTH));

		targetTable.setInput(getALC());
		transformTable.setInput(getALC());
		sourceTable.setInput(getALC());

		Composite bottomRow = new Composite(parent, SWT.NONE);
		bottomRow.setLayout(new RowLayout(SWT.HORIZONTAL));
		bottomRow.setBackground(parent.getBackground());
		((RowLayout) bottomRow.getLayout()).marginWidth = 10;

		// -- Add a transform checkbox to show/hide transformation columns
		xform = new Button(bottomRow, SWT.CHECK | SWT.CENTER);
		xform.setText(Messages.getString("ColumnDataFlow_show_xform"));
		xform.setToolTipText(Messages.getString("ColumnDataFlow_show_xform.tooltip"));
		xform.setBackground(bottomRow.getBackground());
		xform.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				toggleTransformationColumns(xform.getSelection());
			}
		});

		// -- if target has custom xforms then show the headers
		toggleTransformationColumns(false);
		for (String str : cc.getAttributeMap(false).getAttributeNames()) {
			if (cc.getAttributeMap(false).getAttributeMapItem(str).isAdvanced()) {
				toggleTransformationColumns(true);
				xform.setSelection(true);
			}
		}

		getALC().addListener(new DefaultConfigChangeListener() {
			@Override
			public void configurationChanged(MetamergeConfigChange mcc) {
				getDisplay().syncExec(new Runnable() {
					public void run() {
						updateETLStackControl(etlContainer, c, nomap);
					}
				});
			}
		});
		updateETLStackControl(etlContainer, c, nomap);

	}

	private void updateETLStackControl(Composite container, Composite composite, Composite nomap) {
		AssemblyLineConfig alc = (AssemblyLineConfig) getEditingConfig();
		ConnectorConfig cc = (ConnectorConfig) alc.getEntryFeedComponents().getConfig(0);
		boolean hasData = cc.getAttributeMap(true).getAttributeNames().size() > 0;
		if (!hasData)
			hasData = cc.getSchema(true).getItemNames().size() > 0;

		if (hasData) {
			show(container, composite);
		} else {
			show(container, nomap);
		}
		if(xform != null)
			xform.setVisible(hasData);
		if(buttonRow != null)
			buttonRow.setVisible(hasData);
	}

	private void show(Composite container, Composite control) {
		StackLayout stack = (StackLayout) container.getLayout();
		if(stack.topControl == control)
			return;
		
		stack.topControl = control;
		container.layout(true);
	}

	/**
	 * Returns the attribute map for the connector
	 * 
	 * @param input
	 *            true if the input connector map is returned, otherwise the
	 *            output connector's map is returned
	 * @return
	 */
	protected AttributeMapConfig getConnectorMap(boolean input) {
		AssemblyLineConfig alc = (AssemblyLineConfig) getEditingConfig();
		ConnectorConfig cc = input ? (ConnectorConfig) alc.getEntryFeedComponents().getConfig(0) : (ConnectorConfig) alc
				.getDataFlowComponents().getConfig(0);
		return cc.getAttributeMap(input);
	}

	/**
	 * Returns true if the attribute map belongs to the output connector
	 * 
	 * @param amc
	 * @return
	 */
	protected boolean isOutputConnectorMap(AttributeMapConfig amc) {
		return amc.getShortName().equals(ConnectorConfig.OUTPUT_MAP_NAME);
	}

	/**
	 * Opens a dialog where the user can edit the custom javascript for
	 * transformations
	 * 
	 * @param ami
	 */
	protected void editTransformation(final AttributeMapItem ami) {
		final AttributeMapItem targetAMI = getOutputConnectorMapItem(ami);
		if (targetAMI == null) {
			MessageDialog.openError(getShell(), "ETL",
					"Cannot find target attribute name - target attribute map script must begin with '// attrname'");
			return;
		}

		AttributeMapConfig amc = (AttributeMapConfig) ami.getParent();

		final StringBuffer buf = new StringBuffer();
		// -- add header that links this map to its input attribute map item
		if (targetAMI != ami)
			buf.append("// " + ami.getShortName() + "\n");
		buf.append("function map_" + targetAMI.getShortName() + " (");

		final StringBuffer call = new StringBuffer();
		call.append("return map_" + targetAMI.getShortName() + "(");

		// -- generate function header and call string
		// -- Attributes are pulled from work so Input connector att map must be used
		List<String> list = getConnectorMap(true).getAttributeNames();
		for (int i = 0; i < list.size(); i++) {
			if (i > 0) {
				buf.append(", ");
				call.append(", ");
			}
			buf.append(list.get(i));
			call.append("work.getString(\"" + list.get(i) + "\")");
		}
		call.append(");");
		buf.append(")");

		Dialog dlg = new Dialog(getShell()) {

			private SimpleTextEditor editor;
			private Form frm;

			@Override
			protected Control createDialogArea(Composite parent) {
				Composite c = (Composite) super.createDialogArea(parent);

				BaseWidget base = new BaseWidget(c, SWT.NONE);
				base.setLayout(new FillLayout());
				base.setLayoutData(new GridData(GridData.FILL_BOTH));
				frm = base.createForm(base, null);
				frm.setText(Messages.getString("ColumnDataFlow_xform"));
				getShell().setText(Messages.getString("ColumnDataFlow_xform"));
				frm.getBody().setLayout(new GridLayout());

				editor = new SimpleTextEditor(frm.getBody(), SWT.BORDER);
				if (targetAMI.isSimple())
					editor.setText("return " + ami.getShortName());
				else
					editor.setText(getScriptBody(targetAMI.getScript()));
				editor.setLayoutData(new GridData(GridData.FILL_BOTH));
				editor.setEditor(getEditor());

				ConnectorConfig cc = (ConnectorConfig) ((AssemblyLineConfig) getEditingConfig()).getEntryFeedComponents()
						.getConfig(0);
				AttributeMapConfig amc = cc.getAttributeMap(true);
				for (String str : amc.getAttributeNames()) {
					Class<?> clazz = String.class;
					if (currentEntry != null && currentEntry.getObject(str) != null)
						clazz = currentEntry.getObject(str).getClass();
					editor.getSVC().getCAP().addTopLevelObject(str, clazz);
				}

				return c;
			}

			@Override
			protected void createButtonsForButtonBar(Composite parent) {
				createButton(parent, 98, Messages.getString("Debugger.toolbar.Evaluate.label"), false);
				createButton(parent, 99, Messages.getString("intro.section.learning.5.label"), false);
				super.createButtonsForButtonBar(parent);
			}

			@Override
			protected void buttonPressed(int buttonId) {
				super.buttonPressed(buttonId);
				if (buttonId == 98) {
					testScript(targetAMI.getShortName(), editor);
				} else if (buttonId == 99) {
					new TDIHelpMenuAction().showJavaScriptHelp();
				}
			}

			@Override
			protected Point getInitialSize() {
				return new Point(600, 600);
			}

			@Override
			public boolean close() {
				if (frm != null)
					frm.dispose();
				return super.close();
			}

			@Override
			protected void okPressed() {
				String str = editor.getText().trim();
				if (str.equals("") || str.equals(targetAMI.getShortName()) || str.equals("return " + targetAMI.getShortName()))
					targetAMI.setSimple(targetAMI.getShortName());
				else
					targetAMI.setScript(generateScript(editor.getText()));
				super.okPressed();
			}

			private String generateScript(String text) {
				return buf.toString() + " {\n" + text.trim() + "\n}\n" + call.toString();
			}

			@Override
			protected int getShellStyle() {
				return super.getShellStyle() | SWT.RESIZE;
			}
		};
		dlg.open();
		transformTable.getTable().refresh();
	}

	/**
	 * Evaluates the text in the editor providing the last input entry or a
	 * consctructed entry if none exists.
	 * 
	 * @param string
	 * 
	 * @param editor
	 */
	public Object value;
	
	protected void testScript(String attr, SimpleTextEditor editor) {
		Entry work = new Entry();
		try {
			ScriptEngine se = new ScriptEngine(null);
			ConnectorConfig cc = (ConnectorConfig) ((AssemblyLineConfig) getEditingConfig()).getEntryFeedComponents().getConfig(0);
			AttributeMapConfig amc = cc.getAttributeMap(true);
			for (String str : amc.getAttributeNames()) {
				String value = str;
				if (currentEntry != null)
					value = currentEntry.getString(str);
				else
					work.setAttribute(str, value);
				se.declareBean(str, value);
			}
			se.declareBean("ret", this);
			
			se.declareBean("system", new UserFunctions());
			
			String oldValue = currentEntry != null ? currentEntry.getString(attr) : work.getString(attr);
			if (oldValue == null)
				oldValue = "[NULL]";

			se.declareBean("work", currentEntry != null ? currentEntry : work);
			value = null;
			Object result = se.eval(editor.getText());
			if(value instanceof IValue)
				result = ((IValue)result).toString();
			String res = (result == null ? "[NULL]" : result.toString());
			MessageDialog.openInformation(getShell(), Messages.getString("Debugger.toolbar.Evaluate.label") + ": " + attr, Messages
					.getMessage("ColumnDataFlow_oldnew", new Object[] { oldValue, res }));
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e, getShell());
		}

	}

	/**
	 * Returns the script body inside the "function map_*" function.
	 * 
	 * @param script
	 * @return
	 */
	protected String getScriptBody(String script) {
		int start = script.indexOf("function map_");
		if (start != -1)
			start = script.indexOf("\n", start) + 1;
		int end = script.lastIndexOf("}");
		if (start == -1 || end == -1)
			return script;
		else
			return script.substring(start, end);
	}

	/**
	 * Returns the first function header matching "map_*" in the provided script
	 * 
	 * @param script
	 * @return
	 */
	protected String getFunctionHeader(String script) {
		StringTokenizer st = new StringTokenizer(script, "\n");
		while (st.hasMoreTokens()) {
			String str = st.nextToken();
			if (str.startsWith("function map_"))
				return str;
		}
		return "";
	}

	/**
	 * Toggles the visibility of the transformation columns for ETL view
	 * 
	 * @param show
	 *            show/hide
	 */
	protected void toggleTransformationColumns(boolean show) {
		((GridData) transformTable.getLayoutData()).exclude = !show;
		transformTable.setVisible(show);

		Composite parent = transformTable.getParent();
		((GridLayout) parent.getLayout()).numColumns = show ? 3 : 2;
		parent.layout(true);
	}

	/**
	 * This method first equally divides number of pixels between each column.
	 * Then if some columns need more space the difference is substracted
	 * equally from each of the other columns. This provides a good enough
	 * redistribution of pixels when the table can accomodate all columns.
	 * 
	 * @param table
	 */
	private void adjustTableColumnSizes(TableViewer table) {
		GC gc = new GC(table.getTable().getDisplay());
		gc.setFont(table.getTable().getFont());

		int[] sizes = new int[table.getTable().getColumnCount()];

		// -- compute length of image/label and distribute pixels among the
		// columns
		int div = (table.getTable().getClientArea().width - 5) / table.getTable().getColumnCount();
		for (int i = 0; i < table.getTable().getColumnCount(); i++)
			sizes[i] = div;

		for (int i = 0; i < table.getTable().getColumnCount(); i++) {
			TableColumn tc = table.getTable().getColumn(i);
			int x = gc.textExtent(tc.getText()).x;
			if (tc.getImage() != null)
				x += tc.getImage().getBounds().width;

			if (x > div) {
				sizes[i] = x;
				int diff = (x - div) / (sizes.length - 1);
				for (int j = 0; j < sizes.length; j++) {
					if (i != j)
						sizes[j] -= diff;
				}
			}
		}

		for (int i = 0; i < table.getTable().getColumnCount(); i++) {
			TableColumn tc = table.getTable().getColumn(i);
			tc.setWidth(sizes[i]);
		}
		gc.dispose();
	}

	/**
	 * Executes the command derived from the button text
	 * 
	 * @param text
	 */
	protected void executeCommand(String text) {
		if (Messages.getString("ColumnDataFlow_run").equals(text) || continueLabel.equals(text)) {
			buttonControls.get(RUN_BUTTON_INDEX).setText(pauseLabel);
			runAssemblyLine(false, null);
		} else if (text.startsWith(Messages.getString("ColumnDataFlow_next"))) {
			buttonControls.get(RUN_BUTTON_INDEX).setText(continueLabel);
			runAssemblyLine(true, null);
		} else if (text.startsWith(Messages.getString("ColumnDataFlow_next_record"))) {
			runAssemblyLine(true, null);
		} else if (text.startsWith(pauseLabel)) {
			buttonControls.get(RUN_BUTTON_INDEX).setText(continueLabel);
			terminateAL(true);
		} else if (text.startsWith(Messages.getString("Debugger.toolbar.Stop.label"))) {
			buttonControls.get(RUN_BUTTON_INDEX).setText(buttons[RUN_BUTTON_INDEX]);
			terminateAL(false);
		}
		buttonRow.layout(true, true);
	}

	/**
	 * Sends a stop event to the debugger
	 */
	private void terminateAL(boolean pause) {
		if (!pause || isStepping()) {
			terminateDebugger();
		} else {
			debugger.setRunUntil(null);
			setStepping(true);
		}
	}

	/**
	 * Creates a clone of the current config and adds a console logger if output
	 * connector is disabled.
	 * 
	 * @return
	 * @throws Exception
	 */
	private AssemblyLineConfig prepareRuntimeAssemblyLineConfig() throws Exception {
		AssemblyLineConfig alc = (AssemblyLineConfig) getEditingConfig().getClone();
		if(getViewMode() == VIEW_ETL) {
			ConnectorConfig cc = (ConnectorConfig) alc.getDataFlowComponents().getConfig(0);
			if (ConnectorConfig.DISABLED_STATE.equals(cc.getState())) {
				cc.setInheritsFromRef("system:/Connectors/ibmdi.ScriptConnector");
				cc.getConnectionConfig().setScript("function putEntry() {\ntask.dumpEntry(entry)\n}");
				cc.setEnabled(true);
			}
		}
		return alc;
	}

	/**
	 * Returns the input map item that corresponds to the provided output map
	 * item
	 * 
	 * @param ami
	 * @return
	 */
	protected AttributeMapItem getInputConnectorMapItem(AttributeMapItem ami) {
		// -- no need to search if it is the input map item already
		if (!isOutputConnectorMap((AttributeMapConfig) ami.getParent()))
			return ami;

		AssemblyLineConfig alc = (AssemblyLineConfig) getEditingConfig();
		ConnectorConfig cc = (ConnectorConfig) alc.getEntryFeedComponents().getConfig(0);
		AttributeMapConfig amc = cc.getAttributeMap(true);

		String inputName = null;
		if (ami.isSimple())
			inputName = ami.getSimple();
		else if (ami.getScript().startsWith("// "))
			inputName = ami.getScript().substring(3);
		else
			return null;

		return amc.getAttributeMapItem(inputName);
	}

	/**
	 * Returns the output map item that corresponds to the provided input map
	 * item
	 * 
	 * @param ami
	 * @return
	 */
	protected AttributeMapItem getOutputConnectorMapItem(AttributeMapItem ami) {
		// -- no need to search if it is the output map item already
		if (isOutputConnectorMap((AttributeMapConfig) ami.getParent()))
			return ami;

		AssemblyLineConfig alc = (AssemblyLineConfig) getEditingConfig();
		ConnectorConfig cc = (ConnectorConfig) alc.getDataFlowComponents().getConfig(0);
		AttributeMapConfig amc = cc.getAttributeMap(false);
		for (String str : amc.getAttributeNames()) {
			AttributeMapItem amiOut = amc.getAttributeMapItem(str);
			if (amiOut.isSimple() && ami.getShortName().equals(amiOut.getSimple()))
				return amiOut;
			else if (amiOut.isAdvanced() && amiOut.getScript().startsWith("// " + ami.getShortName()))
				return amiOut;
		}
		return null;
	}

	/**
	 * Returns the simple/advanced mapping for the output map that corresponds
	 * to the provided input map item
	 * 
	 * @param ami
	 * @return
	 */
	protected String getOutputConnectorMapScript(AttributeMapItem ami) {
		AttributeMapItem amiOut = getOutputConnectorMapItem(ami);
		if (amiOut == null)
			return "";
		else if (amiOut.isSimple())
			return amiOut.getShortName();
		else if (amiOut.isAdvanced())
			return amiOut.getScript();
		else
			return "";
	}

	/**
	 * Returns the target attribute name based on the input map item.
	 * 
	 * @param ami
	 * @return
	 */
	protected String getOutputConnectorMap(AttributeMapItem ami) {
		AttributeMapItem amiOut = getOutputConnectorMapItem(ami);
		if (amiOut != null)
			return amiOut.getShortName();

		return ""; //$NON-NLS-1$
	}

	/**
	 * Sets the target attribute name for the output connector
	 * 
	 * @param ami
	 * @param targetAttribute
	 */
	protected void setOutputConnectorMap(AttributeMapItem ami, String targetAttribute) {
		AssemblyLineConfig alc = (AssemblyLineConfig) getEditingConfig();
		ConnectorConfig cc = (ConnectorConfig) alc.getDataFlowComponents().getConfig(0);
		AttributeMapConfig amc = cc.getAttributeMap(false);
		String old = getOutputConnectorMap(ami);
		AttributeMapItem inputMapItem = getInputConnectorMapItem(ami);
		AttributeMapItem oldItem = null;

		try {
			if (amc.hasAttributeMapItem(old))
				oldItem = amc.getAttributeMapItem(old);
			if (oldItem != null)
				amc.removeAttributeMapItem(old);
		} catch (Exception e) {
			SystemFunctions.doNothing();
		}
		if (targetAttribute != null && targetAttribute.length() > 0) {
			AttributeMapItem outAmi;
			try {
				outAmi = amc.newAttributeMapItem(targetAttribute);
				if (oldItem != null && oldItem.isAdvanced())
					outAmi.setScript(oldItem.getScript());
				else
					outAmi.setSimple(inputMapItem.getShortName());
				outAmi.setEnabled(true);
				amc.setAttributeMapItem(outAmi);
			} catch (Exception e) {
				EclipseAppender.showError(e.toString(), e, getShell());
			}
		}
	}

	/**
	 * Returns the output schema for the output connector
	 * 
	 * @param ami
	 * @return
	 */
	protected SchemaConfig getOutputConnectorSchema(AttributeMapItem ami) {
		AssemblyLineConfig alc = (AssemblyLineConfig) getEditingConfig();
		ConnectorConfig cc = (ConnectorConfig) alc.getDataFlowComponents().getConfig(0);
		return cc.getSchema(false);
	}

	/**
	 * Sets the editing config to the provided al config and updates tables
	 * 
	 * @param alc
	 */
	public void setInput(AssemblyLineConfig alc) {
		setEditingConfig(alc);
		if (sourceTable != null) {
			sourceTable.setInput(alc);
			targetTable.setInput(alc);
			transformTable.setInput(alc);
		}
	}

	/**
	 * Call this from the handler code to prevent a new AL to be started.
	 * 
	 * @param step
	 * @param untilComponent
	 */
	protected void runContinue(boolean step, String untilComponent) {
		// -- left over messages after a stop
		if(!debugger.isStarted())
			return;
		
		// -- remember this so we can auto-skip
		setStepping(step);
		
		// -- disable buttons
		enableButtons(false);
		
		// -- continue execution
		try {
			debugger.runAssemblyLine(untilComponent);
		} catch (Exception e) {
			enableButtons(true);
			EclipseAppender.logerror(e.toString(), e, getShell());
		}
	}

	/**
	 * Call this from the command handlers (e.g. Run buttons etc)
	 * 
	 * @param step
	 * @param untilComponent
	 */
	private void runAssemblyLine(boolean step, String untilComponent) {
		
		// -- remember this so we can auto-skip
		setStepping(step);

		// -- start/continue the al
		try {
			// -- disable buttons
			enableButtons(false);

			if (!debugger.isStarted()) {
				setBreakpoints();
				debugger.setRunUntil(untilComponent);
				logger = ALFileLogger.getInstance(Utils.getProjectFor(getEditingConfig()), getEditingConfig().getShortName());
				clearAll();
				debugger.startAssemblyLine(prepareRuntimeAssemblyLineConfig(), true, untilComponent);
			} else {			
				debugger.runAssemblyLine(untilComponent);
			}
		} catch (Exception e) {
			buttonControls.get(RUN_BUTTON_INDEX).setText(buttons[RUN_BUTTON_INDEX]);
			terminateAL(false);
			enableButtons(true);
			EclipseAppender.logerror(e.toString(), e, getShell());
		}
	}

	/**
	 * Enable/disable buttons and progress bar. Progress bar is visible on
	 * false.
	 * 
	 * @param enable
	 */
	protected void enableButtons(boolean enable) {
		if (progress != null)
			progress.setVisible(!enable);
		buttonControls.get(NEXT_BUTTON_INDEX).setEnabled(enable);
		buttonControls.get(RUN_BUTTON_INDEX).setEnabled(true);
		buttonControls.get(STOP_BUTTON_INDEX).setEnabled(debugger.isStarted());
	}

	/**
	 * Returns true if last run command was step
	 * 
	 * @return
	 */
	public boolean isStepping() {
		return stepping;
	}

	/**
	 * Updates the stepping flag.
	 * 
	 * @param stepping
	 */
	public void setStepping(boolean stepping) {
		this.stepping = stepping;
	}

	@Override
	public void dispose() {
		if (debugger != null) {
			debugger.clearEventListeners();
			debugger.terminate();
		}
		super.dispose();
	}

	/**
	 * Give the named component input focus (e.g. focus rect painted). Component
	 * is automatically scrolled into view by the ScrolledComponent.
	 * 
	 * @param component
	 */
	protected String highlightComponent(String component) {
		final int index = components.indexOf(component);
		if (outline != null)
			outline.selectComponent(component);
		if (index != -1 && viewers.get(component) != null) {
			getDisplay().syncExec(new Runnable() {
				public void run() {
					updateBackgroundColors(components.get(index));
				}
			});
			return component;
		}
		return null;
	}

	/**
	 * Returns an entry with those attributes mapped by a component from the
	 * input entry.
	 * 
	 * @param obj
	 * @param component
	 * @return
	 */
	protected Entry getMappedAttributes(Entry obj, String component) {
		try {
			ConnectorConfig cc = ((AssemblyLineConfig) getEditingConfig()).getConnectorByName(component);
			Entry mapped = new Entry();
			if (((AssemblyLineConfig) getEditingConfig()).autoMapAllAttributes(component)) {
				return obj;
			}
			for (String str : cc.getAttributeMap(Utils.isInputConnector(cc)).getAttributeNames()) {
				mapped.setAttribute(obj.getAttribute(str));
			}
			return mapped;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return obj;
	}

	/**
	 * Returns the view mode (e.g. VIEW_ETL)
	 * 
	 * @return
	 */
	public int getViewMode() {
		return viewMode;
	}

	private static class ALFileLogger {

		private BufferedWriter logStream;
		private IProject project;

		private static ALFileLogger getInstance(IProject project, String name) throws Exception {
			int savecount = Activator.getPrefs().getInt(PreferenceConstants.P_SAVE_AL_LOGS_COUNT);
			if (savecount == 0 || project == null)
				return null;
			else
				return new ALFileLogger(project, Utils.getALLogFile(project, name));
		}

		public ALFileLogger(IProject project, File logFile) throws IOException {
			this.project = project;
			logStream = new BufferedWriter(new FileWriter(logFile));
		}

		/**
		 * Writes a log message to the assemblyline's project log file. This
		 * call is ignored if the setting for al logs to keep is disabled.
		 * 
		 * @param string
		 *            Message to log
		 */
		public void logmessage(String string) {
			if (logStream != null) {
				try {
					logStream.write(string);
				} catch (IOException e) {
					EclipseAppender.logerror(e.toString(), e);
				}
			}
		}

		public void dispose() {
			if (logStream != null) {
				try {
					logStream.close();
				} catch (IOException e) {
					EclipseAppender.logerror(e.toString(), e);
				}
				logStream = null;
			}
			if (project != null) {
				try {
					project.getFolder("Logs").refreshLocal(IResource.DEPTH_INFINITE, null);
				} catch (CoreException e) {
					SystemFunctions.doNothing();
				}
			}
		}
	}

	private class OutlineWidget extends BaseWidget {

		private TreeViewer tree;

		public OutlineWidget(Composite parent, AssemblyLineConfig alc) {
			super(parent, 0);
			setLayout(new FillLayout());
			Form frm = createForm(this, null);
			frm.setText(Messages.getString("ColumnDataFlow_outline"));

			Composite hc = new Composite(frm.getHead(), SWT.NONE);
			hc.setLayout(new RowLayout(SWT.HORIZONTAL));
			createColumnsViewCompSelectorButton(hc);
			frm.setHeadClient(hc);

			frm.getBody().setLayout(new FillLayout());
			tree = new TreeViewer(frm.getBody(), SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
			tree.setContentProvider(new AssemblyLineContentProvider3());
			tree.setLabelProvider(new ConfigLabelProvider());
			tree.setInput(alc);
			tree.expandAll();

			MenuManager mm = new MenuManager();
			Menu menu = mm.createContextMenu(tree.getControl());
			tree.getTree().setMenu(menu);
			final Action runAndBreak = new Action() {

				@Override
				public String getText() {
					return Messages.getString("ColumnDataFlow_relaunch");
				}

				@Override
				public void run() {
					String breakpoint = getBreakpoint();
					if (breakpoint != null) {
						RunAssemblyLineInput input = new RunAssemblyLineInput(getALC());
						input.setDebug(true);
						input.setBreakPoint(breakpoint);
						input.setDebugMode(0);
						try {
							getEditor().getSite().getPage().openEditor(input, RunAssemblyLineEditor.EDITOR_ID, true);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}

				public boolean isEnabled() {
					setEnabled(getBreakpoint() != null);
					return super.isEnabled();
				}

				private String getBreakpoint() {
					String breakpoint = null;
					Object obj = ((IStructuredSelection) tree.getSelection()).getFirstElement();
					if (obj instanceof HookTree) {
						breakpoint = Utils.getParentConfig(((HookTree) obj).getHooksConfig(), ConnectorConfig.class).getShortName()
								+ "." + ((HookTree) obj).getName();
					} else if (obj instanceof BaseConfiguration) {
						BaseConfiguration element = (BaseConfiguration) obj;
						if (element instanceof HookConfig) {
							BaseConfiguration granny = element.getParent().getParent();
							if (granny instanceof AssemblyLineConfig) {
								breakpoint = (String) ((HookConfig) element).getHookName();
							} else {
								breakpoint = granny.getShortName() + "." + ((HookConfig) element).getHookName();
							}
						} else if (element instanceof AttributeMapItem) {
							breakpoint = element.getParent().getParent().getShortName() + "." + element.getParent().getShortName()
									+ "." + element.getShortName();
						} else if (element == getALC().getDataFlowComponents() || element == getALC().getEntryFeedComponents()) {
							breakpoint = null;
						} else {
							breakpoint = element.getShortName();
						}
					}
					return breakpoint;
				}

			};

			mm.add(runAndBreak);

			tree.addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					runAndBreak.isEnabled();
				}
			});
		}

		public void selectComponent(String component) {
			AssemblyLineConfig alc = (AssemblyLineConfig) tree.getInput();
			final Object comp = alc.getComponent(component);
			if (comp != null) {
				getDisplay().syncExec(new Runnable() {
					public void run() {
						tree.setSelection(new StructuredSelection(comp));
					}
				});
			}
		}
	}

	private static class WorkListWidget extends BaseWidget {

		private TableViewer table;

		public WorkListWidget(Composite parent, int style) {
			super(parent, style);

			setLayout(new FillLayout());

			Form frm = createForm(this, null);
			frm.setText(Messages.getString("ColumnDataFlow.WorkBucket"));
			frm.getBody().setLayout(new FillLayout());

			table = new TableViewer(frm.getBody(), SWT.BORDER | SWT.FULL_SELECTION);
			table.getTable().setFont(JFaceResources.getTextFont());

			TableColumn tc = new TableColumn(table.getTable(), SWT.LEFT);
			tc.setText(Messages.getString("RunOptionsWidget.10"));
			tc.setWidth(100);

			tc = new TableColumn(table.getTable(), SWT.LEFT);
			tc.setText(Messages.getString("RunOptionsWidget.11"));
			tc.setWidth(200);

			table.getTable().setHeaderVisible(true);

			table.setContentProvider(new ArrayContentProvider() {
				@Override
				public Object[] getElements(Object inputElement) {
					if (inputElement instanceof Entry) {
						Entry entry = (Entry) inputElement;
						List<String> coll = new ArrayList<String>();
						coll.addAll(entry.getAttributeCollection());
						Collections.sort(coll);
						ArrayList<Object> list = new ArrayList<Object>();
						for (String str : coll) {
							list.add(entry.getAttribute(str));
						}
						return list.toArray();
					}
					return super.getElements(inputElement);
				}

			});

			table.setLabelProvider(new ITableLabelProvider() {
				public void removeListener(ILabelProviderListener listener) {
				}

				public boolean isLabelProperty(Object element, String property) {
					return false;
				}

				public void dispose() {
				}

				public void addListener(ILabelProviderListener listener) {
				}

				public Image getColumnImage(Object element, int columnIndex) {
					return null;
				}

				public String getColumnText(Object element, int columnIndex) {
					Attribute a = (Attribute) element;
					if (columnIndex == 0)
						return a.getName();
					else
						return a.getValue();
				}
			});
		}

		public void setInput(Entry work) {
			table.setInput(work);
			table.refresh();
		}

	}

	private class CustomCellLabelProvider extends CellLabelProvider {

		@Override
		public boolean useNativeToolTip(Object object) {
			return false;
		}

		private int column;

		public CustomCellLabelProvider(int column) {
			super();
			this.column = column;
		}

		@Override
		public void update(ViewerCell cell) {

			if (cell.getElement() instanceof SchemaItemConfig) {
				SchemaItemConfig sic = (SchemaItemConfig) cell.getElement();
				switch (column) {
				case SOURCE_ATTRIBUTE:
				case TARGET_ATTRIBUTE:
					cell.setText(sic.getAttributeName());
					cell.setForeground(getDisplay().getSystemColor(SWT.COLOR_GRAY));
					break;
				}
				return;
			}

			cell.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));

			AttributeMapItem ami = (AttributeMapItem) cell.getElement();

			// -- unmapped output items have no corresponding input value
			boolean isOutputMapItem = isOutputConnectorMap((AttributeMapConfig) ami.getParent());

			// -- grab schema so we can check if the mapped attribute isn't in
			// the connector's schema
			SchemaConfig schema = Utils.getParentConfig(ami, ConnectorConfig.class).getSchema(!isOutputMapItem);
			if (schema != null && schema.getItemNames().size() == 0)
				schema = null;

			switch (column) {
			case SOURCE_ATTRIBUTE:
				cell.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
				if (isOutputMapItem) {
					cell.setText("");
				} else {
					cell.setText("" + ami.getShortName());
					// -- if we have a schema and the attribute is not part of
					// it tag it red
					if (schema != null && schema.getItem(ami.getShortName()) == null)
						cell.setForeground(getDisplay().getSystemColor(SWT.COLOR_RED));
				}

				break;

			case SOURCE_VALUE:
				String str = null;
				if (isOutputMapItem)
					str = null;
				else if (currentEntry != null && currentEntry.getString(ami.getShortName()) != null)
					str = currentEntry.getString(ami.getShortName());

				if (str == null)
					cell.setText("");
				else
					cell.setText(str);
				break;

			case XFORM_SCRIPT:
				AttributeMapItem targetAMI = getOutputConnectorMapItem(ami);
				if (targetAMI == null) {
					cell.setText("");
				} else if (targetAMI.isSimple()) {
					cell.setText("--->");
				} else {
					cell.setText(getScriptBody(getOutputConnectorMapScript(ami)).trim().replaceAll("\n", "."));
				}
				break;

			case TARGET_VALUE:
				AttributeMapItem target = getOutputConnectorMapItem(ami);
				if (target == null)
					target = ami;

				if (outputConnectorEntry != null && target != null) {
					String val = outputConnectorEntry.getString(target.getShortName());
					if (val != null)
						cell.setText(val);
					else
						cell.setText("");
				} else {
					cell.setText("");
				}
				break;

			case TARGET_ATTRIBUTE:
				// -- we either get the Source attribute map (3 column etl) or
				// the Target attribute map (3 table view)
				AttributeMapItem targ = getOutputConnectorMapItem(ami);
				if(isLinkCriteria(targ))
					cell.setImage(Activator.getImage("Connector_Lookup_Enabled"));
				else
					cell.setImage(null);
				cell.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
				if (targ == null) {
					cell.setText(ami.getShortName());
				} else if (!targ.getEnabled()) {
					cell.setText("");
				} else {
					cell.setText(getOutputConnectorMap(ami));
					if (schema != null && schema.getItem(targ.getShortName()) == null)
						cell.setForeground(getDisplay().getSystemColor(SWT.COLOR_RED));
				}

				break;

			default:
				cell.setText("");
			}
		}

		@Override
		public String getToolTipText(Object element) {
			if (element instanceof SchemaItemConfig) {
				return ((SchemaItemConfig) element).getAttributeName();
			} else {
				AttributeMapItem ami = (AttributeMapItem) element;

				if (column == TARGET_ATTRIBUTE || column == SOURCE_ATTRIBUTE) {
					// -- unmapped output items have no corresponding input
					// value
					boolean isOutputMapItem = isOutputConnectorMap((AttributeMapConfig) ami.getParent());

					// -- grab schema so we can check if the mapped attribute
					// isn't in the connector's schema
					SchemaConfig schema = Utils.getParentConfig(ami, ConnectorConfig.class).getSchema(!isOutputMapItem);
					if (schema != null && schema.getItemNames().size() == 0)
						schema = null;

					if (schema != null && schema.getItem(ami.getShortName()) == null)
						return "The schema for the connector does not have an entry for: " + ami.getShortName();
				} else {
					if (ami.isSimple())
						return ami.getSimple();
					else
						return ami.getScript().trim();
				}
			}
			return null;
		}

	}

	private class ETLSourceTable extends Composite {

		private TableViewer table;
		private AssemblyLineConfig alc;

		public ETLSourceTable(Composite parent, AssemblyLineConfig alc) {
			super(parent, 0);
			this.alc = alc;
			setLayout(new FillLayout());
			setBackground(parent.getBackground());

			table = new TableViewer(this, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
			table.getTable().setHeaderVisible(true);
			table.getTable().setLinesVisible(true);
			table.getTable().setFont(JFaceResources.getTextFont());

			table.getTable().addControlListener(new ControlListener() {
				private boolean isAdjusting;
				
				public void controlResized(ControlEvent e) {
					if (isAdjusting)
						return;
					isAdjusting = true;
					try {
						adjustTableColumnSizes(table);
					} finally {
						isAdjusting = false;
					}
				}

				public void controlMoved(ControlEvent e) {
				}
			});

			createColumns();
			setContentProvider();
			addContextMenu();
		}

		protected ConnectorConfig getCC() {
			return (ConnectorConfig) alc.getEntryFeedComponents().getConfig(0);
		}

		protected BaseConfiguration getSelectedItem() {
			Object item = ((IStructuredSelection) table.getSelection()).getFirstElement();
			if (item instanceof BaseConfiguration)
				return (BaseConfiguration) item;
			else
				return null;
		}

		protected List<BaseConfiguration> getSelectedItems() {
			IStructuredSelection sel = (IStructuredSelection) table.getSelection();
			ArrayList<BaseConfiguration> list = new ArrayList<BaseConfiguration>();
			for(Object obj : sel.toArray()) {
				if(obj instanceof BaseConfiguration)
					list.add((BaseConfiguration) obj);
			}
			return list;
		}

		public void select(int index) {
			StructuredSelection sel = StructuredSelection.EMPTY;
			Object obj = null;
			if (index != -1 && getTable().getTable().getItemCount() > index)
				obj = getTable().getElementAt(index);
			if (obj != null)
				sel = new StructuredSelection(obj);
			getTable().setSelection(sel, true);
		}

		protected void addContextMenu() {
			table.addDoubleClickListener(new IDoubleClickListener() {
				public void doubleClick(DoubleClickEvent event) {
					if (getSelectedItem() instanceof SchemaItemConfig) {
						addAttributeMap(getSelectedItem().getShortName());
					} else {
						deleteAttributeMap(getSelectedItem().getShortName());
					}
				}
			});

			// -- sync with transform/target tables
			table.addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					if (((IStructuredSelection) table.getSelection()).getFirstElement() instanceof AttributeMapItem) {
						syncTables(ETLSourceTable.this, table.getTable().getSelectionIndex());
					}
				}
			});

			final Action deleteAttr = new Action() {
				@Override
				public void run() {
					getCC().getAttributeMap(true).notifyChange(getCC(), "", MetamergeConfigChange.BEGIN_CHANGES);
					for(BaseConfiguration bc : getSelectedItems()) {
						String str = bc.getShortName();
						if (bc instanceof AttributeMapItem)
							deleteAttributeMap(str);
					}
					getCC().getAttributeMap(true).notifyChange(getCC(), "", MetamergeConfigChange.END_CHANGES);
				}
				
				@Override
				public String getText() {
					return Messages.getString("AttributeMap.toolbar.Remove.name");
				}
			};
			
			final Action mapAttr = new Action() {
				@Override
				public void run() {
					getCC().getAttributeMap(true).notifyChange(getCC(), "", MetamergeConfigChange.BEGIN_CHANGES);
					for(BaseConfiguration bc : getSelectedItems()) {
						String str = bc.getShortName();
						if (bc instanceof SchemaItemConfig)
							addAttributeMap(str);
					}
					getCC().getAttributeMap(true).notifyChange(getCC(), "", MetamergeConfigChange.END_CHANGES);
				}

				@Override
				public String getText() {
					return Messages.getString("action.label.3");
				}
			};
			
			MenuManager mm = new MenuManager();
			Menu menu = mm.createContextMenu(table.getTable());
			table.getTable().setMenu(menu);
			mm.add(mapAttr);
			mm.add(deleteAttr);
			mm.add(new CopyTableContentsAction(table.getTable()));

			mm.addMenuListener(new IMenuListener() {
				public void menuAboutToShow(IMenuManager manager) {
					boolean enableAdd = false;
					boolean enableDel = false;
					for(BaseConfiguration bc : getSelectedItems()) {
						if (bc instanceof SchemaItemConfig)
							enableAdd = true;
						else if (bc instanceof AttributeMapItem)
							enableDel = true;
					}					
					mapAttr.setEnabled(enableAdd);
					deleteAttr.setEnabled(enableDel);
				}
			});

			table.getTable().addKeyListener(new KeyListener() {
				public void keyReleased(KeyEvent e) {
				}
				public void keyPressed(KeyEvent e) {
					if(e.keyCode == SWT.DEL)
						deleteAttr.run();
					else if (e.keyCode == SWT.INSERT)
						mapAttr.run();
				}
			});
			
			((IFocusService)getEditor().getSite().getService(IFocusService.class)).addFocusTracker(table.getTable(), "com.ibm.tdi.etl.table");
		}

		protected void addAttributeMap(String name) {
			try {
				AttributeMapItem map = getCC().getAttributeMap(true).newAttributeMapItem(name);
				map.setSimple(name);
				setInput(alc);
				targetTable.setInput(alc);
				transformTable.setInput(alc);
			} catch (Exception e) {
				return;
			}
		}

		public void deleteAttributeMap(String name) {
			try {
				AttributeMapItem ami = getCC().getAttributeMap(true).getAttributeMapItem(name);
				if (ami != null) {
					getCC().getAttributeMap(true).removeAttributeMapItem(name);
					targetTable.removeAttributeMap(ami);
					transformTable.setInput(alc);
				}
				setInput(alc);
			} catch (Exception e) {
				return;
			}
		}

		public void setInput(AssemblyLineConfig alc) {
			table.setInput(alc);
		}

		public TableViewer getTable() {
			return table;
		}

		protected void createColumns() {
			TableViewerColumn tvc = null;
			for (int i = 0; i < columns.length; i++) {
				String str = columns[i];
				tvc = new TableViewerColumn(table, SWT.LEFT);
				tvc.getColumn().setText(str);
				tvc.setLabelProvider(new CustomCellLabelProvider(i));
			}
		}

		protected void setContentProvider() {
			table.setContentProvider(new IStructuredContentProvider() {
				public Object[] getElements(Object inputElement) {
					if (inputElement instanceof AssemblyLineConfig) {
						ConnectorConfig cc = (ConnectorConfig) ((AssemblyLineConfig) inputElement).getEntryFeedComponents()
								.getConfig(0);
						AttributeMapConfig amc = cc.getAttributeMap(true);
						ArrayList<BaseConfiguration> list = new ArrayList<BaseConfiguration>();

						// -- First list is mapped items
						List<String> names = amc.getAttributeNames();
						Collections.sort(names);
						for (String str : names) {
							list.add(amc.getAttributeMapItem(str));
						}

						// -- Next part is list of manual maps on target
						list.addAll(targetTable.getManuallyMappedItems());

						// -- Next part is unmapped schema items
						if (includeUnmappedItems()) {
							names = cc.getSchema(true).getItemNames();
							Collections.sort(names);
							for (String str : names) {
								if (!amc.hasAttributeMapItem(str))
									list.add(cc.getSchema(true).getItem(str));

							}
						}

//						Collections.sort(list, new Comparator<BaseConfiguration>() {
//							public int compare(BaseConfiguration o1, BaseConfiguration o2) {
//								if (o1 instanceof AttributeMapItem || o2 instanceof AttributeMapItem) {
//									if (o2 instanceof SchemaItemConfig)
//										return -1;
//									else if (o1 instanceof SchemaItemConfig)
//										return 1;
//								} else {
//									if()
//								}
//								return o1.getShortName().compareTo(o2.getShortName());
//							}
//						});

						return list.toArray();
					}
					return new Object[] {};
				}

				public void dispose() {
				}

				public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				}
			});

		}

		protected boolean includeUnmappedItems() {
			return true;
		}

	}

	private class ETLTargetTable extends Composite {

		private TableViewer table;
		private AssemblyLineConfig alc;

		public ETLTargetTable(Composite parent, AssemblyLineConfig alc) {
			super(parent, 0);
			setLayout(new FillLayout());
			setBackground(parent.getBackground());

			this.alc = alc;

			table = new TableViewer(this, SWT.BORDER | SWT.FULL_SELECTION);
			table.getTable().setHeaderVisible(true);
			table.getTable().setLinesVisible(true);
			table.getTable().setFont(JFaceResources.getTextFont());

			table.getTable().addControlListener(new ControlListener() {
				private boolean isAdjusting;
				
				public void controlResized(ControlEvent e) {
					if (isAdjusting)
						return;
					isAdjusting = true;
					try {
						adjustTableColumnSizes(table);
					} finally {
						isAdjusting = false;
					}
				}

				public void controlMoved(ControlEvent e) {
				}
			});

			// -- sync with transform/target tables
			table.addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					if (((IStructuredSelection) table.getSelection()).getFirstElement() instanceof AttributeMapItem) {
						syncTables(ETLTargetTable.this, table.getTable().getSelectionIndex());
					}
				}
			});

			TableViewerColumn tvc = null;
			String[] columns = new String[] { Messages.getString("ColumnDataFlow_target_value"),
					Messages.getString("ColumnDataFlow_target_attribute") };

			for (int i = 0; i < columns.length; i++) {
				String str = columns[i];
				tvc = new TableViewerColumn(table, SWT.LEFT);
				tvc.getColumn().setText(str);
				tvc.getColumn().setWidth(i == 1 ? 300 : 80);
				tvc.setLabelProvider(new CustomCellLabelProvider(i == 0 ? TARGET_VALUE : TARGET_ATTRIBUTE));
			}

			// -- target attribute is modifiable
			tvc.setEditingSupport(new EditingSupport(table) {

				private ComboBoxCellEditor cbe;
				private SchemaConfig schema;

				@Override
				protected boolean canEdit(Object element) {
					return element instanceof AttributeMapItem;
				}

				@Override
				protected CellEditor getCellEditor(Object element) {
					if (cbe == null) {
						cbe = new ComboBoxCellEditor((Composite) getViewer().getControl(), new String[] {}, SWT.DROP_DOWN) {
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
					}
					AttributeMapItem ami = (AttributeMapItem) element;
					String val = getElementValue(element);
					schema = getOutputConnectorSchema(ami);
					List<String> items = getValidItems(schema, val);
					cbe.setItems(items.toArray(new String[0]));
					return cbe;
				}

				@Override
				protected Object getValue(Object element) {
					String value = getElementValue(element);
					return getValidItems(schema, value).indexOf(value);
				}

				@Override
				protected void setValue(Object element, Object value) {
					String newval = null;
					if (value instanceof Integer) {
						newval = getValidItems(schema, getElementValue(element)).get((Integer) value);
					} else {
						newval = value.toString();
					}
					setOutputConnectorMap((AttributeMapItem) element, newval);
					table.refresh(true);
				}

				private String getElementValue(Object element) {
					AttributeMapItem ami = (AttributeMapItem) element;
					return getOutputConnectorMap(ami);
				}

				private List<String> getValidItems(SchemaConfig schema, String value) {
					List<String> list = schema.getItemNames();
					if (value != null && list.indexOf(value) == -1)
						list.add(value);
					Collections.sort(list);
					return list;
				}

			});

			table.setContentProvider(new IStructuredContentProvider() {
				public Object[] getElements(Object inputElement) {
					if (inputElement instanceof AssemblyLineConfig) {
						ConnectorConfig out = (ConnectorConfig) ((AssemblyLineConfig) inputElement).getDataFlowComponents()
								.getConfig(0);
						AttributeMapConfig amc = getConnectorMap(true);
						AttributeMapConfig amout = getConnectorMap(false);

						// -- add the map that corresponds to the the input map
						// of the Iterator (sorted)
						ArrayList<BaseConfiguration> list = new ArrayList<BaseConfiguration>();
						List<String> inputList = amc.getAttributeNames();
						Collections.sort(inputList);
						for (String str : inputList) {
							AttributeMapItem ami = amc.getAttributeMapItem(str);
							AttributeMapItem map = getOutputConnectorMapItem(ami);

							// -- mapping in from the source auto-creates the
							// output attribute
							if (map == null) {
								try {
									map = amout.newAttributeMapItem(str);
									map.setSimple(str);
									// if output conn has no schema we auto-map
									// otherwise it's disabled
									if (out.getSchema(false).getItemNames().size() > 0)
										map.setEnabled(false);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							if (map != null)
								list.add(map);
						}

						// -- add the remaining attribute map items (with no
						// corresponding input map)
						inputList = amout.getAttributeNames();
						Collections.sort(inputList);
						for (String str : inputList) {
							AttributeMapItem ami = amout.getAttributeMapItem(str);
							if (!list.contains(ami))
								list.add(ami);
						}

						// -- add the remaining schema items that are not mapped
						inputList = out.getSchema(false).getItemNames();
						Collections.sort(inputList);
						for (String str : inputList) {
							if (!amout.hasAttributeMapItem(str))
								list.add(out.getSchema(false).getItem(str));
						}

						return list.toArray();
					}
					return new Object[] {};
				}

				public void dispose() {
				}

				public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				}
			});

			addContextMenu();
		}

		/**
		 * Returns a list of attribute map items for which there are no
		 * corresponding ref to an input attribute.
		 * 
		 * @return
		 */
		public Collection<? extends BaseConfiguration> getManuallyMappedItems() {
			List<BaseConfiguration> list = new ArrayList<BaseConfiguration>();
			AttributeMapConfig amout = getConnectorMap(false);
			List<String> inputList = amout.getAttributeNames();
			Collections.sort(inputList);
			for (String str : inputList) {
				AttributeMapItem ami = amout.getAttributeMapItem(str);
				if (ami.isAdvanced() && ami.getScript() != null && !ami.getScript().startsWith("// "))
					list.add(ami);
			}
			return list;
		}

		protected List<BaseConfiguration> getSelectedItems() {
			IStructuredSelection sel = (IStructuredSelection) table.getSelection();
			ArrayList<BaseConfiguration> list = new ArrayList<BaseConfiguration>();
			for(Object obj : sel.toArray()) {
				if(obj instanceof BaseConfiguration)
					list.add((BaseConfiguration) obj);
			}
			return list;
		}

		private void addContextMenu() {

			final Action addAttr = new Action() {
				@Override
				public void run() {

					for(BaseConfiguration item : getSelectedItems()) {
						if (item instanceof AttributeMapItem) {
							AttributeMapItem ami = (AttributeMapItem)item;
							sourceTable.deleteAttributeMap(ami.getShortName());
							AttributeMapConfig amc = (AttributeMapConfig) ami.getParent();
							if (amc.hasAttributeMapItem(ami.getShortName())) {
								amc.removeAttributeMapItem(ami);
								removeLinkItem(ami);
							}
							table.refresh();
							transformTable.getTable().refresh();
							sourceTable.getTable().refresh();
						} else {
							String str = item.getShortName();
							AssemblyLineConfig alc = (AssemblyLineConfig) getEditingConfig();
							ConnectorConfig cc = (ConnectorConfig) alc.getDataFlowComponents().getConfig(0);
							AttributeMapConfig amc = cc.getAttributeMap(false);
							if (amc.hasAttributeMapItem(str))
								return;
	
							AttributeMapItem ami;
							try {
								ami = amc.newAttributeMapItem(str);
								ami.setScript("return \"\"");
								ami.setEnabled(true);
								table.refresh();
								transformTable.getTable().refresh();
								sourceTable.getTable().refresh();
	
								xform.setSelection(true);
								toggleTransformationColumns(true);
	
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			};

			final Action newAttr = new Action() {
				@Override
				public void run() {
					AssemblyLineConfig alc = (AssemblyLineConfig) getEditingConfig();
					ConnectorConfig cc = (ConnectorConfig) alc.getDataFlowComponents().getConfig(0);
					final AttributeMapConfig amc = cc.getAttributeMap(false);

					IInputValidator validator = new IInputValidator() {
						public String isValid(String newText) {
							if (amc.hasAttributeMapItem(newText))
								return Messages.getMessage("attributemap.attribute.already.mapped.err", newText);
							else
								return null;
						}
					};
					InputDialog id = new InputDialog(getShell(), getText(), Messages.getString("ConnectorWidget3.18"), "",
							validator);
					if (id.open() == Window.CANCEL)
						return;

					String str = id.getValue();
					if (amc.hasAttributeMapItem(str))
						return;

					AttributeMapItem ami;
					try {
						ami = amc.newAttributeMapItem(str);
						ami.setScript("return \"\"");
						ami.setEnabled(true);
						table.refresh();
						transformTable.getTable().refresh();
						sourceTable.getTable().refresh();

						xform.setSelection(true);
						toggleTransformationColumns(true);

					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				@Override
				public String getText() {
					return Messages.getString("AttributeMap.toolbar.Add.name");
				}
			};
			
			final Action linkAction = new Action() {

				@Override
				public String getText() {
					return Messages.getString("ColumnDataFlow.key");
				}

				@Override
				public void run() {
					updateLinkCriteria(true);
				}
			};

			final Action linkRemoveAction = new Action() {

				@Override
				public String getText() {
					return Messages.getString("ColumnDataFlow.key.remove");
				}

				@Override
				public void run() {
					updateLinkCriteria(false);
				}
			};

			MenuManager mm = new MenuManager();
			Menu menu = mm.createContextMenu(table.getTable());
			table.getTable().setMenu(menu);
			mm.add(addAttr);
			mm.add(newAttr);
			mm.add(linkAction);
			mm.add(linkRemoveAction);
			mm.add(new CopyTableContentsAction(table.getTable()));
			mm.addMenuListener(new IMenuListener() {
				public void menuAboutToShow(IMenuManager manager) {
					linkAction.setEnabled(outputSupportsUpdate());
					linkRemoveAction.setEnabled(linkAction.isEnabled());
					if (getSelectedItem() instanceof SchemaItemConfig)
						addAttr.setText(Messages.getString("action.label.3"));
					else
						addAttr.setText(Messages.getString("AttributeMap.toolbar.Remove.name"));
					addAttr.setEnabled(getSelectedItem() != null);
				}
			});

			((IFocusService)getEditor().getSite().getService(IFocusService.class)).addFocusTracker(table.getTable(), "com.ibm.tdi.etl.table");
		}

		protected void removeLinkItem(AttributeMapItem ami) {
			AssemblyLineConfig alc = (AssemblyLineConfig) getEditingConfig();
			ConnectorConfig cc = (ConnectorConfig) alc.getDataFlowComponents().getConfig(0);
			LinkCriteriaConfig link = cc.getLinkCriteria();
			for(Object obj : link.getCriteriaNames()) {
				LinkCriteriaItem lci = link.getCriteria(obj);
				if(ami.getShortName().equals(lci.getAttribute())) {
					link.removeCriteria(obj);
				}
			}
		}

		protected void updateLinkCriteria(boolean setCriteria) {
			AssemblyLineConfig alc = (AssemblyLineConfig) getEditingConfig();
			ConnectorConfig cc = (ConnectorConfig) alc.getDataFlowComponents().getConfig(0);
			LinkCriteriaConfig link = cc.getLinkCriteria();
			ArrayList<LinkCriteriaItem> list = new ArrayList<LinkCriteriaItem>();
			for(Object obj : link.getCriteriaNames()) {
				list.add(link.getCriteria(obj));
			}
			
			for(BaseConfiguration bc : getSelectedItems()) {
				if(bc instanceof SchemaItemConfig)
					continue;
				
				AttributeMapItem ami = (AttributeMapItem) bc;
				AttributeMapItem source = getInputConnectorMapItem(ami);
				if(source == null)
					continue;
				
				String key = bc.getShortName();
				// -- remove existing one(s)
				for(LinkCriteriaItem lci : list) {
					if(key.equals(lci.getAttribute())) {
						link.removeCriteria(lci.getShortName());
					}
				}
				
				if (setCriteria) {
					try {
						LinkCriteriaItem lci = link.newCriteria(null);
						lci.setAttribute(key);
						lci.setOper(LinkCriteriaItem.EXACT);
						lci.setValue("$" + source.getShortName());
					} catch (Exception e) {
						EclipseAppender.logerror(e.toString(), e, getShell());
					}
				}
			}
			
			// -- toggle between addonly/update mode based on link criteria
			if(link.getCriteriaNames().size() > 0) {
				cc.setMode(ConnectorConfig.UPDATE_MODE);
				cc.getHooks().getHook("update_multiple").setEnabled(true);
			} else {
				cc.setMode(ConnectorConfig.ADDONLY_MODE);
			}
			
			targetTable.getTable().refresh(true);
		}

		private BaseConfiguration getSelectedItem() {
			IStructuredSelection sel = (IStructuredSelection) table.getSelection();
			if (sel.isEmpty())
				return null;
			else
				return (BaseConfiguration) sel.getFirstElement();
		}

		public void removeAttributeMap(AttributeMapItem ami) {
			AttributeMapItem amo = getOutputConnectorMapItem(ami);
			if (amo != null) {
				getCC().getAttributeMap(false).removeAttributeMapItem(amo.getShortName());
			}
			table.refresh();
		}

		public void select(int index) {
			StructuredSelection sel = StructuredSelection.EMPTY;
			Object obj = null;
			if (index != -1 && getTable().getTable().getItemCount() > index)
				obj = getTable().getElementAt(index);
			if (obj != null)
				sel = new StructuredSelection(obj);
			getTable().setSelection(sel, true);
		}

		public ConnectorConfig getCC() {
			return (ConnectorConfig) alc.getDataFlowComponents().getConfig(0);
		}

		public TableViewer getTable() {
			return table;
		}

		public void setInput(AssemblyLineConfig alc) {
			table.setInput(alc);
		}
	}

	/**
	 * This is the table between source and target showing the transformation
	 * scripts.
	 * 
	 */
	private class ETLTransformationTable extends ETLSourceTable {

		public ETLTransformationTable(Composite parent, AssemblyLineConfig alc) {
			super(parent, alc);
			getTable().addDoubleClickListener(new IDoubleClickListener() {
				public void doubleClick(DoubleClickEvent event) {
					editTransformation((AttributeMapItem) ((IStructuredSelection) getTable().getSelection()).getFirstElement());
				}
			});
			// -- sync with transform/target tables
			getTable().addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					if (((IStructuredSelection) getTable().getSelection()).getFirstElement() instanceof AttributeMapItem) {
						syncTables(ETLTransformationTable.this, getTable().getTable().getSelectionIndex());
					}
				}
			});
		}

		public void select(int index) {
			StructuredSelection sel = StructuredSelection.EMPTY;
			Object obj = null;
			if (index != -1 && getTable().getTable().getItemCount() > index)
				obj = getTable().getElementAt(index);
			if (obj != null)
				sel = new StructuredSelection(obj);
			getTable().setSelection(sel, true);
		}

		protected void createColumns() {
			TableViewerColumn tvc = new TableViewerColumn(getTable(), SWT.LEFT);
			tvc.getColumn().setText(Messages.getString("ColumnDataFlow_etl_transformation"));
			tvc.getColumn().setWidth(80);
			tvc.setLabelProvider(new CustomCellLabelProvider(XFORM_SCRIPT));
		}

		protected boolean includeUnmappedItems() {
			return false;
		}

		@Override
		protected void addContextMenu() {
		}

	}

	public boolean isLinkCriteria(AttributeMapItem ami) {
		if(!outputSupportsUpdate())
			return false;
		
		AssemblyLineConfig alc = (AssemblyLineConfig) getEditingConfig();
		ConnectorConfig cc = (ConnectorConfig) alc.getDataFlowComponents().getConfig(0);
		LinkCriteriaConfig link = cc.getLinkCriteria();
		for(Object obj : link.getCriteriaNames()) {
			LinkCriteriaItem lci = link.getCriteria(obj);
			if(ami.getShortName().equals(lci.getAttribute()))
				return true;
		}
		return false;
	}

	protected boolean outputSupportsUpdate() {
		AssemblyLineConfig alc = (AssemblyLineConfig) getEditingConfig();
		ConnectorConfig cc = (ConnectorConfig) alc.getDataFlowComponents().getConfig(0);
		return Utils.getSupportedModes(cc).contains(ConnectorConfig.UPDATE_MODE);
	}

}
