/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.stepper;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.AttributeMapItem;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.HookConfig;
import com.ibm.di.config.interfaces.LoopConfig;
import com.ibm.di.config.interfaces.ParserConfig;
import com.ibm.di.config.interfaces.RawConnectorConfig;
import com.ibm.di.config.interfaces.RawFunctionConfig;
import com.ibm.di.config.interfaces.ScriptConfig;
import com.ibm.di.util.AssemblyLineScripts;
import com.ibm.di.util.Breakpoint;
import com.ibm.di.util.DebugServer;
import com.ibm.di.util.HookTree;
import com.ibm.di.util.HookTree.Phase;
import com.ibm.tdi.eclipse.Activator;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.commands.CommandHandlerProxy;
import com.ibm.tdi.eclipse.debugger.DebugClient;
import com.ibm.tdi.eclipse.debugger.DebugClientEvent;
import com.ibm.tdi.eclipse.debugger.DebugClientListener;
import com.ibm.tdi.eclipse.debugger.DebugClient.ScriptData;
import com.ibm.tdi.eclipse.editors.BaseEditor;
import com.ibm.tdi.eclipse.editors.RunAssemblyLineEditor;
import com.ibm.tdi.eclipse.editors.RunAssemblyLineInput;
import com.ibm.tdi.eclipse.editors.RunRemoteAssemblyLineInput;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.preferences.PreferenceConstants;
import com.ibm.tdi.eclipse.util.TDIToolBar;
import com.ibm.tdi.eclipse.util.TextEditorContextMenu;
import com.ibm.tdi.eclipse.widget.AssemblyLineStepComposite;
import com.ibm.tdi.eclipse.widget.BaseWidget;
import com.ibm.tdi.eclipse.widget.RunALWidget;
import com.ibm.tdi.eclipse.widget.SimpleTextEditor;

/**
 * This widget implements the outline/work-attr views of the CE debugger and
 * hosts the StepperColumnsWidget that shows the columnar view of components in
 * the AL. It toggles between simple and advanced based on notifications from
 * the StepperColumnsWidget.
 * <p>
 * In advanced mode we show the full AL outline in a checked tree where
 * breakpoints can be set at any level. In addition we show the Watch window to
 * the far right (e.g Outline | Columns | Watch).
 * <p>
 * In simple mode we show the simple AL outline (e.g. components only) and a
 * work object view below it (e.g. Outline/Work | Columns).
 * 
 */
public class StepperPanel extends BaseWidget implements ICheckStateListener, DebugClientListener {
	/**
	 * 
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final String MEMENTO_BP_EXPRESSION = "expression";

	private static final String MEMENTO_BP_ENABLED = "enabled";

	private static final String MEMENTO_BP_LOCATION = "location";

	private static final String MEMENTO_BREAKPOINT = "Breakpoint";

	private static final String MEMENTO_BREAKPOINTS = "Breakpoints";

	private static final String MEMENTO_WATCH_LIST = "WatchList";

	private static final String MEMENTO_SHOWATTRIBUTES = "showAttribute";

	private static final String MEMENTO_SHOW_ALL_HOOKS = "showAllHooks";

	private static final String MEMENTO_SASH_WEIGTHS = "StepperPanel.sash.weights";


	/**
	 * Name used to save advanced state boolean
	 */
	private final static String MEMENTO_ADVANCED = "StepperAdvanced";

	private static final String[] items = new String[] { "Step", "StepOver", "Continue", "Stop", "ClearAll", "RunToCycle" };

	private static final int STEP_INDEX = 0;
	private static final int STEP_OVER_INDEX = 1;
	private static final int CONTINUE_INDEX = 2;
	private static final int STOP_INDEX = 3;
	private static final int CLEAR_ALL_INDEX = 4;
	private static final int RUN_TO_CYCLE_INDEX = 5;

	private TDIToolBar toolbar;

	private Hashtable<String, Breakpoint> breakpoints;

	private AssemblyLineStepComposite breaktree;

	private StepperWatchList watchList;

	private Form quickEditorForm;
	private FormToolkit tk;
	private SimpleTextEditor ted;
	private TDIToolBar scriptBar;
	private SashForm splitter;
	private SimpleTextEditor bep;
	private Object editingSelection;

	private Button activeCheck;

	private Hashtable<String, Breakpoint> restoreBK;

	// -- When this is set we skip past <skipScript#lineno> breakpoints.
	public Object skipScript;

	private Button continueButton;

	private Button attmapCheck;

	private DebugClient client;

	private StepperColumnsWidget columnView;

	private Composite advancedWidget;

	private Composite simpleWidget;

	private StepperOutline outlineView;

	private TDIToolBar simpleToolbar;

	private Composite toolbarStack;

	private SashForm sash;

	private Button clearAllItem;

	private String breakAtComp; // non-null if we are doing a "Run and Break here"

	private List<CommandHandlerProxy> handlers = new ArrayList<CommandHandlerProxy>();

	private AssemblyLineScripts scripts;

	public StepperPanel(Composite parent, int style, BaseEditor editor) {
		super(parent, style, null, editor);
		setBackground(parent.getBackground());
	}

	/**
	 * Sets the debug client to use in this widget
	 * @param client
	 */
	public void setDebugClient(DebugClient client) {
		this.client = client;
		client.addDebugListener(this);
		if(outlineView != null)
			outlineView.setDebugClient(client);
	}

	@Override
	public void setEditingConfig(BaseConfiguration editingConfig) {
		if (editingConfig == null)
			return;

		super.setEditingConfig(editingConfig);

		scripts = new AssemblyLineScripts((AssemblyLineConfig) editingConfig);

		// -- Create UI widgets
		if (toolbar != null)
			return;

		setLayout(new FillLayout());

		splitter = new SashForm(this, SWT.VERTICAL);

		sash = new SashForm(splitter, SWT.HORIZONTAL);

		Form frm = createForm(sash, null);
		frm.getBody().setLayout(new StackLayout());
		frm.setText(Messages.getString("ColumnDataFlow_outline"));

		//
		// -- Toolbar stack where we switch between simple/advanced mode
		//
		toolbarStack = new Composite(frm.getHead(), 0);
		toolbarStack.setLayout(new StackLayout());
		createToolbar(toolbarStack);
		createSimpleToolbar(toolbarStack);
		frm.setHeadClient(toolbarStack);

		//
		// -- First add the advanced view mode to the stack
		//
		advancedWidget = new Composite(frm.getBody(), 0);
		advancedWidget.setLayout(new FillLayout());
		createAdvancedView(advancedWidget);

		//
		// -- Next add the simple view
		//
		simpleWidget = new Composite(frm.getBody(), 0);
		simpleWidget.setLayout(new FillLayout());
		createSimpleView(simpleWidget);

		//
		// -- Finally add the Columns view to the right
		//
		try {
			columnView = new StepperColumnsWidget(sash, this.client);
			columnView.addAdvancedModeListener(new Listener() {
				public void handleEvent(Event event) {
					// -- Reset breakpoints to match advanced mode
					if (columnView.isAdvancedModeDebugging()) {
						client.clearBreakpoints();
						watchList.setEnabled(true);
						// Retransmit breakpoints to assemblyline
						sendBK();
					} else {
						watchList.setEnabled(true);
					}
					toggleViews();
				}
			});
		} catch (Exception e1) {
			EclipseAppender.logerror(e1.toString(), e1, getShell());
		}

		//
		// -- Get the advanced state from columnWidget and show proper toolbar
		//
		if (columnView.isAdvancedModeDebugging())
			((StackLayout) toolbarStack.getLayout()).topControl = toolbar;
		else
			((StackLayout) toolbarStack.getLayout()).topControl = simpleToolbar;
		toolbarStack.layout(true, true);

		//
		// -- Watch list
		//
		watchList = new StepperWatchList(sash, client);

		if (columnView.isAdvancedModeDebugging())
			sash.setWeights(new int[] { 30, 70, 30 });
		else
			sash.setWeights(new int[] { 30, 70, 0 });

		//
		// -- Create a script editor in the lower part of splitter
		//
		createScriptEditor(splitter);
		splitter.setWeights(new int[] { 100, 0 });

		// -- Refresh advanced/simple mode widgets
		toggleViews();

		layout(true);
	}

	private void createSimpleToolbar(Composite head) {
		simpleToolbar = new TDIToolBar(head, 0);
	}

	/**
	 * Create the simple viewer - This is just an outline with the work entry
	 * table below it
	 * 
	 * @param parent
	 */
	private void createSimpleView(Composite parent) {
		try {
			outlineView = new StepperOutline(parent, client);
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e, getShell());
		}
	}

	/**
	 * Create the advanced view - This is the detailed checkbox outline that
	 * shows hooks and attribute maps where the user can set breakpoints and
	 * step through scripts.
	 * 
	 * @param parent
	 */
	private void createAdvancedView(Composite parent) {

		Composite flow = new Composite(parent, SWT.BORDER);
		Utils.setGridLayout(flow, 1, false);

		//
		// -- AssemblyLine Outline
		//
		breaktree = new AssemblyLineStepComposite(flow, SWT.NULL, getEditingConfig(), this);
		breaktree.addCheckStateListener(this);
		registerContextMenu(breaktree.getStepTree());
		breaktree.setInput((AssemblyLineConfig) getEditingConfig());
		breaktree.setLayoutData(new GridData(GridData.FILL_BOTH));

		breaktree.getStepTree().addOpenListener(new IOpenListener() {
			public void open(OpenEvent event) {
				if (event.getSelection().isEmpty())
					return;
				Object obj = ((IStructuredSelection) event.getSelection()).getFirstElement();
				editBreakpoint(obj, true);
			}
		});

		createOptionControls(flow);
	}

	private void createOptionControls(Composite parent) {
		Composite clientBottom = new Composite(parent, 0);
		Utils.setGridLayout(clientBottom, 2, false);
		clientBottom.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		activeCheck = new Button(clientBottom, SWT.CHECK);
		activeCheck.setText(Messages.getString("AssemblyLineEditor2.11"));
		activeCheck.setToolTipText(Messages.getString("StepperPanel.2"));
		Boolean b = Activator.getDefault().getPreferenceStore().getBoolean(MEMENTO_SHOW_ALL_HOOKS);
		activeCheck.setSelection(b != null && b.booleanValue());
		breaktree.setShowAllHooks(b != null && b.booleanValue());
		activeCheck.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				breaktree.setShowAllHooks(((Button) e.widget).getSelection());
			}
		});

		attmapCheck = new Button(clientBottom, SWT.CHECK);
		b = Activator.getDefault().getPreferenceStore().getBoolean(MEMENTO_SHOWATTRIBUTES);
		attmapCheck.setSelection(b != null && b.booleanValue());
		breaktree.setShowAttributes(b != null && b.booleanValue());
		attmapCheck.setText(Messages.getString("AssemblyLineEditor2.2"));
		attmapCheck.setToolTipText(Messages.getString("AssemblyLineEditor2.1"));
		attmapCheck.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				breaktree.setShowAttributes(((Button) e.widget).getSelection());
			}
		});
	}

	private void createToolbar(Composite parent) {
		toolbar = new TDIToolBar(parent, 0, true, false, false);
		Button lastButton = null;
		for (String str : items) {
			IAction item = new Action() {
				@Override
				public void run() {
					executeCommand(toolbar.indexOf(this));
				}

			};
			if (str.equals("RunToCycle"))
				item.setText(">>|");
			else
				item.setImageDescriptor(Activator.getImageDescriptorRelative(str));
			item.setToolTipText(Messages.getString("Debugger.toolbar." + str + ".tooltip"));
			item.setEnabled(false);
			if (getEditor() != null)
				handlers.add(new CommandHandlerProxy(getEditor().getEditorSite(), item, "com.ibm.tdi.debug." + str.toLowerCase()));
			lastButton = toolbar.add(item);
			if (str.equals("Continue"))
				continueButton = lastButton;
			else if(str.equals("ClearAll"))
				clearAllItem = lastButton;
		}
	}

	@SuppressWarnings("unchecked")
	public void handleEvent(final DebugClientEvent event) {

		if(isDisposed()) {
			client.removeDebugListener(this);
			return;
		}

		switch (event.getCommand()) {
		case StepperEvent.CONFIG:
			getDisplay().syncExec(new Runnable() {
				public void run() {
					setEditingConfig((AssemblyLineConfig) event.getData());
					breaktree.setInput((AssemblyLineConfig) event.getData());
					columnView.setEditingConfig(getEditingConfig());
					outlineView.setEditingConfig(getEditingConfig());
					restoreState(getMemento());

					if(restoreBK != null) {
						mergeBK();
						sendBK();
						breaktree.setBreakpoints(breakpoints);
					}

					updateClearAllTooltip();
				}
			});
			break;

		case DebugClientEvent.STATE_CHANGE:
			if (toolbar != null && !toolbar.isDisposed()) {
				getDisplay().syncExec(new Runnable() {
					public void run() {
						if (client.isRunning()) {
							toolbar.getItem(STEP_INDEX).setEnabled(false);
							toolbar.getItem(STEP_OVER_INDEX).setEnabled(false);
							toolbar.getItem(RUN_TO_CYCLE_INDEX).setEnabled(false);
							toolbar.getItem(STOP_INDEX).setEnabled(true);
							toolbar.getItem(CONTINUE_INDEX).setEnabled(true);
							toolbar.getItem(CONTINUE_INDEX).setImageDescriptor(Activator.getImageDescriptor("icons/Pause.gif"));
							continueButton.setToolTipText(Messages.getString("Debugger.toolbar.Pause.tooltip"));
						} else if (client.isWaiting()) {
							toolbar.getItem(STEP_INDEX).setEnabled(true);
							toolbar.getItem(STEP_OVER_INDEX).setEnabled(true);
							toolbar.getItem(RUN_TO_CYCLE_INDEX).setEnabled(true);
							toolbar.getItem(STOP_INDEX).setEnabled(true);
							toolbar.getItem(CONTINUE_INDEX).setEnabled(true);
							toolbar.getItem(CONTINUE_INDEX).setImageDescriptor(Activator.getImageDescriptor("icons/Continue.gif"));
							continueButton.setToolTipText(Messages.getString("Debugger.toolbar.Continue.tooltip"));
							watchList.setEnabledEdit(true);
						} else if (client.isIdle()) {
							toolbar.getItem(STEP_INDEX).setEnabled(true);
							toolbar.getItem(STEP_OVER_INDEX).setEnabled(true);
							toolbar.getItem(RUN_TO_CYCLE_INDEX).setEnabled(true);
							toolbar.getItem(STOP_INDEX).setEnabled(false);
							toolbar.getItem(CONTINUE_INDEX).setEnabled(true);
							toolbar.getItem(CONTINUE_INDEX).setImageDescriptor(Activator.getImageDescriptor("icons/Run.gif"));
							watchList.setEnabledEdit(false);
						}
					}
				});
			}
			break;

		case StepperEvent.BREAKPOINTS:
			breakpoints = (Hashtable<String, Breakpoint>) event.getData();
			mergeBK();
			break;

		case StepperEvent.INIT:
			if (breaktree == null)
				Utils.nap(100); // timing issue, have not finished creating
			if (breaktree == null)
				break;
			breaktree.setBreakpoints(breakpoints);
			breaktree.getBreakpoint(DebugServer.INIT_BREAK).setEnabled(true);
			break;

		case StepperEvent.BREAK:
			// -- Ignore breaks here when we're not advanced mode
			if(shouldBreakOnEvent(event))
				Display.getDefault().syncExec(new BreakThread(event));
			break;

		case StepperEvent.SS_DISCONNECT:
			assemblyLineFinished();
			break;

		}
	}

	/**
	 * Returns true if we should break on the debug event
	 * 
	 * @param event
	 * @return
	 */
	private boolean shouldBreakOnEvent(DebugClientEvent event) {
		// -- Break in advanced mode only 
		if (!columnView.isAdvancedModeDebugging())
			return false;
		else
			return true;
	}

	private String getBreakpointName(Object data) {
		String str = "" + data;
		if (str.indexOf("#") != -1) {
			return str.substring(0, str.indexOf("#"));
		}
		return str;
	}

	private String getBreakpointLine(Object data) {
		String str = "" + data;
		if (str.indexOf("#") != -1) {
			return str.substring(str.indexOf("#") + 1);
		}
		return null;
	}

	public void assemblyLineFinished() {
		if (isDisposed())
			return;
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				if (toolbar == null || toolbar.isDisposed())
					return;

				for (int i = 0; i < toolbar.getItemCount(); i++)
					toolbar.getItem(i).setEnabled(false);
				// expression.setEnabled(false);
				breaktree.setEnabled(false);
				activeCheck.setEnabled(false);
				attmapCheck.setEnabled(false);

				closeEditor();

				if (getEditor() instanceof RunAssemblyLineEditor
						&& !(getEditor().getEditorInput() instanceof RunRemoteAssemblyLineInput)) {
					continueButton.setToolTipText(Messages.getString("RunAL.restart.tooltip"));
					continueButton.setEnabled(true);
				}

			}
		});
	}

	public class BreakThread implements Runnable {
		@SuppressWarnings("unused")
		private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

		private DebugClientEvent event;

		public BreakThread(DebugClientEvent event) {
			super();
			this.event = event;
		}

		public void run() {

			String comp = null;
			if (event.getError() != null) {
				boolean b = Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.P_SHOW_DEBUG_ERRORS);
				if (!b) {
					final StringBuilder msg = new StringBuilder();
					String loc = getLastKnownLocation();
					if (loc != null) {
						if (loc.endsWith(".initialize_fail")) {
							msg.append(Messages.getMessage("StepperPanel.error.init.comp", loc.substring(0,loc.length()-16)));
						} else if (loc.endsWith(".default_fail")) {
							msg.append(Messages.getMessage("StepperPanel.error.comp", loc.substring(0,loc.length()-13)));
						} else if (loc.indexOf('.')<0) {
							// Hook or Script Component
							String s = HookTree.getHookLabel(loc);
							if (s.startsWith("Hook."))
								msg.append(Messages.getMessage("StepperPanel.error.comp", loc)); //Script Component
							else
								msg.append(Messages.getMessage("StepperPanel.error.al.hook", s)); //AL Hook
						}
						msg.append("\n");
					}
					msg.append(Utils.exceptionText(event.getError()));

					MessageDialogWithToggle tad = new MessageDialogWithToggle(getShell(),
							Messages.getString("general.error.label"), null, event.getError().getLocalizedMessage(),
							MessageDialog.ERROR, new String[] { IDialogConstants.OK_LABEL }, 0, Messages
							.getString("StepperPanel.dont.show.debug.error"), false) {

						@Override
						protected Button createToggleButton(Composite parent) {
							StyledText text = new StyledText(parent, SWT.READ_ONLY | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
							text.setLayoutData(new GridData(GridData.FILL_BOTH));
							text.setText(msg.toString());
							new TextEditorContextMenu(text);
							return super.createToggleButton(parent);
						}

					};
					tad.create();
					tad.getShell().setText(Messages.getString("general.error.label"));
					tad.open();
					Activator.getDefault().getPreferenceStore().setValue(PreferenceConstants.P_SHOW_DEBUG_ERRORS,
							tad.getToggleState());
				} else {
					fireEvent(StepperEvent.LOGMSG, Utils.exceptionText(event.getError()));
				}
				comp = client.getState().getCurrentLocation();

			} else {
				comp = event.getDebugBreak().getBreakpoint();
			}

			enableButtons(true, false);

			//
			// -- Update breaktree to reflect the current/last known location
			//
			String name = getBreakpointName(comp);
			String strline = getBreakpointLine(comp);
			BaseConfiguration b = getConfigForPath(name);
			breaktree.setBreak(b);

			// -- On error we just return
			if (event.getError() != null) {
				return;
			}

			//
			// If previous break was a component with script, we check if the
			// stepper event was step over in case we are not to step into the
			// script itself. We also get a script#line for each subsequent line
			// in the script so we have to remember this also.
			//
			if (skipScript != null && skipScript.equals(name) && strline != null) {
				try {
					executeCommand(STEP_OVER_INDEX);
					return;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			skipScript = null;

			String prevComp = getBreakpointName(getPrevKnownLocation());
			String prevLine = getBreakpointLine(getPrevKnownLocation());
			if (client.getLastCommand() == StepperEvent.STEP_OVER && 
					prevLine == null && strline != null &&
					name != null && name.equals(prevComp)) {
				try {
					skipScript = name;
					executeCommand(STEP_OVER_INDEX);
					return;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			// -- If the breakpoint is hidden (e.g. hide inactive hooks) then
			// -- keep going
			ISelection sel = breaktree.getStepTree().getSelection();
			if (strline == null && (sel == null || sel.isEmpty())) {
				switch (client.getLastCommand()) {
				case StepperEvent.STEP:
				case StepperEvent.RUN_TO_CYCLE:
					executeCommand(STEP_INDEX);
					return;
				case StepperEvent.STEP_OVER:
					executeCommand(STEP_OVER_INDEX);
					return;
				case StepperEvent.CONT:
					executeCommand(CONTINUE_INDEX);
					return;
				}
			}

			// If breakAtComp is non-null, we are probably just starting,
			// and want to run to the specified component
			if (breakAtComp != null) {
				String tmp = breakAtComp;
				breakAtComp = null;				
				breakAt(tmp);
				return;
			}

			// we may have moved into other scripts from our current break
			// location script
			ScriptData currentScriptData = null;
			if (event.getDebugBreak() != null && event.getDebugBreak().isScript())
				currentScriptData = event.getDebugBreak().getScriptData();
			String currentScript = null;
			if (currentScriptData != null) {
				currentScript = currentScriptData.getScript();
				ted.setText(currentScript);
				scriptBar.setText(currentScriptData.getSourceRef());
				// TODO: This is ugly code to try to show breakpoints set
				// in the current component.
				// This should be improved. 
				if (b != null && b.getScript() != null && currentScript != null && currentScript.equals(b.getScript().trim())) {
					fixAnnotations(b);
				}
			}

			if (strline != null) {
				if (editingSelection != b && !editBreakpoint(b, false, currentScript))
					return;
				if (!"0".equals(strline))
					ted.gotoLine(Integer.parseInt(strline), true);
			} else {
				if (hasScript(b)) {
					if (b != editingSelection)
						editBreakpoint(b, false, currentScript);
				} else {
					closeEditor();
				}
			}

			// -- Select again in case editor overlaps current selection
			if (b != null)
				breaktree.setBreak(b, true);
		}

	}

	private boolean hasScript(BaseConfiguration b) {
		if (b instanceof HookConfig || b instanceof HookTree || b instanceof ScriptConfig)
			return true;
		if (b instanceof RawConnectorConfig || b instanceof ParserConfig || b instanceof RawFunctionConfig)
			return b.getStringParameter("script") != null;
		return false;
	}

	// @Override
	// public void widgetSelected(SelectionEvent e) {
	// ToolItem item = (ToolItem) e.getSource();
	// int index = toolbar.indexOf(item);
	// executeCommand(index);
	// }
	//
	private void executeCommand(int command) {
		int index = command;
		try {
			switch (index) {
			case STEP_INDEX:
			case STEP_OVER_INDEX:
			case CONTINUE_INDEX:

				enableButtons(false, index != STEP_INDEX);
				if (index == STEP_INDEX) {
					client.stepAssemblyLine();
				} else if (index == STEP_OVER_INDEX) {
					client.stepOverAssemblyLine();
				} else {
					// Pause
					if (client.isRunning())
						client.pauseAssemblyLine();
					// Continue
					else if (client.isWaiting())
						client.continueAssemblyLine();
					// Restart
					else if(client.isIdle() && getEditor() instanceof RunAssemblyLineEditor)
						((RunAssemblyLineEditor) getEditor()).restart((RunAssemblyLineInput) getEditor().getEditorInput());
				}

				break;
			case RUN_TO_CYCLE_INDEX:
				runToCycle();
				break;
			case STOP_INDEX:
				if (client.isWaiting()) {
					client.stopAssemblyLine();
				} else if (client.isRunning()) {
					client.pauseAssemblyLine();
				} else if (getEditor() instanceof RunAssemblyLineEditor) {
					((RunAssemblyLineEditor) getEditor()).stopAL(true);
				}
				break;

			case CLEAR_ALL_INDEX:
				for (Breakpoint b : breakpoints.values()) {
					enableBreakpoint(b, false);
				}
				breaktree.setBreakpoints(breakpoints);
				ted.removeAnnotations();
				break;

			}
		} catch (Exception err) {
			EclipseAppender.logerror(err.toString(), err, getShell());
		}
	}

	private void runToCycle() throws Exception {
		final long currentCycle = client.getState().getCycleCounter();

		InputDialog dlg = new InputDialog(getShell(), 
				Messages.getString("StepperPanel.runToCycle.title"),
				Messages.getMessage("StepperPanel.runToCycle.prompt", String.valueOf(currentCycle)),
				"",
				new IInputValidator() {
					public String isValid(String newText) {
						try {
							long l = Long.valueOf(newText);
							return l > currentCycle ? null : "";
						} catch (Exception e) {
							return "";
						}
					}
		});
		
		if (dlg.open() != Window.OK)
			return;
		
		String val = dlg.getValue();
		if (val == null || val.length() == 0)
			return;

		long cycle = Long.valueOf(val);
		enableButtons(false, true);
		client.runToCycle(cycle);
	}

	public void fireEvent(int type, String message) {
		Event event = new Event();
		event.type = type;
		event.text = message;
		for (Listener listener : breakListener) {
			listener.handleEvent(event);
		}
	}

	public String getLastKnownLocation() {
		return client.getState().getCurrentLocation();
	}

	public String getPartName() {
		String name = Messages.getString("StepperPanel.8");
		if (getEditingConfig() != null)
			name = getEditingConfig().getShortName();
		if (name.endsWith(".assemblyline"))
			name = name.substring(0, name.indexOf(".assemblyline"));
		return name;
	}

	public Hashtable<String, Breakpoint> getBreakpoints() {
		return breakpoints;
	}

	public void checkStateChanged(CheckStateChangedEvent event) {
		BaseConfiguration element = null;
		if (event.getElement() instanceof BaseConfiguration) {
			element = (BaseConfiguration) event.getElement();
		} else if (event.getElement() instanceof HookTree) {
			HookTree ht = (HookTree) event.getElement();
			element = ht.getConfig();
			if (element == null && !ht.getName().endsWith("attribute_map"))
				element = ht.getHookConfig(true);
		}
		// -- disable top level containers
		if (isTopLevelContainers(element)) {
			breaktree.getStepTree().setChecked(element, false);
			return;
		}

		if (element != null)
			enableBreakpoint(element, event.getChecked());
	}

	private boolean isTopLevelContainers(BaseConfiguration element) {
		return (element instanceof ContainerConfig && element.getParent() instanceof AssemblyLineConfig);
	}

	private boolean toggleBreakpoint(String breakpoint) {

		Breakpoint bp = breakpoints.get(breakpoint);
		if (bp == null) {
			bp = new Breakpoint(breakpoint, true, null);
			breakpoints.put(breakpoint, bp);
		} else {
			bp.setEnabled(!bp.isEnabled());
		}

		try {
			if(bp.isEnabled())
				client.addBreakpoint(bp);
			else
				client.removeBreakpoint(bp);
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e, getShell());
		}

		updateClearAllTooltip();

		return bp.isEnabled();
	}

	private void enableBreakpoint(BaseConfiguration element, boolean check) {

		if (element == null)
			return;
		
		Breakpoint bp = breakpointForConfig(element);
		if (bp != null)
			enableBreakpoint(bp, check);

		// Set the user comment to the BP expression (used by tree to render
		// and show tooltip)
		breaktree.getStepTree().refresh(element, true);
		if (bp != null)
			element.setUserComment(bp.getExpression());
	}

	private void enableBreakpoint(Breakpoint bp, boolean check) {
		bp.setEnabled(check);
		try {
			if(check)
				client.addBreakpoint(bp);
			else
				client.removeBreakpoint(bp);
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e, getShell());
		}

		try {
			BaseConfiguration element = getConfigForPath(bp.getLocation());
			if(element != null) {
				breaktree.setChecked(element, bp.isEnabled());
				element.setUserComment(bp.getExpression());
			}
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e, getShell());
		}

		updateClearAllTooltip();
	}

	private Breakpoint breakpointForConfig(BaseConfiguration element) {
		String breakpoint = getCompNameFromConf(element);
		if (breakpoint == null)
			return null;

		Breakpoint bp = breakpoints.get(breakpoint);
		if (bp == null) {
			bp = new Breakpoint(breakpoint, false, null);
			breakpoints.put(breakpoint, bp);
		}

		return bp;
	}

	public BaseConfiguration getConfigForPath(String str) {
		String comp = str;
		String hook = null;

		if (str == null || str.indexOf('#') != -1)
			return null;

		if (str.indexOf(".") != -1) {
			comp = str.substring(0, str.indexOf("."));
			hook = str.substring(str.indexOf(".") + 1);
		}

		AssemblyLineConfig config = (AssemblyLineConfig) getEditingConfig();
		BaseConfiguration bc = config.getComponent(comp);

		if (bc != null) {
			ConnectorConfig cc = null;
			if (bc instanceof ConnectorConfig)
				cc = (ConnectorConfig) bc;
			else if (bc instanceof LoopConfig && ((LoopConfig) bc).getLoopType() == LoopConfig.LOOP_CONNECTOR_FC) {
				try {
					cc = ((LoopConfig) bc).getLoopConnector();
				} catch (Exception ignore) {
					cc = null; // Need a statement to stop findbugs from complaining.
				}
			}

			if (hook != null && cc != null) {
				if (hook.startsWith("Input."))
					return attributeMapItem(cc.getAttributeMap(true), hook.substring(6));
				if (hook.startsWith("Output."))
					return attributeMapItem(cc.getAttributeMap(false), hook.substring(7));
				if (hook.equals("Connector"))
					return cc.getConnectionConfig();
				if (hook.equals("Parser"))
					return cc.getParserConfig();
				if (hook.equals("Function") && cc instanceof FunctionConfig)
					return ((FunctionConfig)cc).getFunctionConfig();

				HookConfig hc = cc.getHooks().getHook(hook, false);
				if (hc != null)
					bc = hc;
				else
					return null;
			}
		} else if (comp != null && hook == null) {
			bc = scripts.getScript(comp);
			if (bc == null)
				bc = config.getHooks().getHook(comp, true);
		}

		return bc;
	}

	private AttributeMapItem attributeMapItem(AttributeMapConfig map, String name) {
		if (map.hasAttributeMapItem(name))
			return map.getAttributeMapItem(name);
		return null;
	}

	@Override
	public void dispose() {
		if (tk != null)
			tk.dispose();
		for (CommandHandlerProxy chp:handlers)
			chp.dispose();
		super.dispose();
	}

	private boolean editBreakpoint(Object obj, boolean force) {
		return editBreakpoint(obj, force, null);
	}

	private boolean editBreakpoint(Object obj, boolean force, String script) {
		if (obj == editingSelection) {
			closeEditor();
			return false;
		}
		editingSelection = obj;
		BaseConfiguration bc = null;
		if (obj instanceof BaseConfiguration) {
			bc = (BaseConfiguration) obj;
		} else if (obj instanceof HookTree) {
			HookTree ht = (HookTree) obj;
			if (ht.getConfig() != null || ht.getName().endsWith("attribute_map")) {
				closeEditor();
				return false;
			}
			bc = ((HookTree) obj).getHookConfig(true);
		} else if (obj instanceof Phase) {
			closeEditor();
			return false;
		}

		if (script == null && bc != null)
			script = bc.getScript();

		if (bc == null || (!force && (script == null || script.length() == 0))) {
			closeEditor();
			return false;
		}

		boolean hasScript = (script != null);
		if (hasScript) {
			ted.init(bc, script.trim());
			ted.getSourceViewer().setEditable(false);
			fixAnnotations(bc);
		}
		btnScript.setEnabled(hasScript);
		btnScript.setChecked(hasScript);
		btnCondition.setEnabled(hasScript);
		btnCondition.setChecked(! hasScript);

		Breakpoint bp = breakpointForConfig(bc);
		if (bp != null) {
			bep.init(bc, bp.getExpression() == null ? "" : bp.getExpression());
		} else {
			bep.init(null, null);
		}
		bep.getSourceViewer().setEditable(bp != null);

		updateTabs();

		if (bc instanceof HookConfig)
			scriptBar.setText(Messages.getString("Hook." + bc.getShortName()));
		else
			scriptBar.setText(bc.getShortName());

		splitter.setWeights(new int[] { 50, 50 });
		return true;
	}

	private Action btnScript;
	private Action btnCondition;
	private Composite tabs;

	private void updateTabs() {
		if (btnScript.isChecked())
			((StackLayout) tabs.getLayout()).topControl = ted;
		else
			((StackLayout) tabs.getLayout()).topControl = bep;
		tabs.layout(true);
	}

	private Composite createScriptEditor(Composite parent) {

		tk = new FormToolkit(parent.getDisplay());
		quickEditorForm = tk.createForm(parent);
		tk.decorateFormHeading(quickEditorForm);
		scriptBar = new TDIToolBar(quickEditorForm, SWT.SINGLE | SWT.TITLE);

		btnScript = new Action() {
			public String getText() {
				return Messages.getString("StepperPanel.3");
			}

			public void run() {
				if (btnScript.isChecked()) {
					btnCondition.setChecked(false);
				}
				updateTabs();
			}
		};
		scriptBar.add(btnScript, SWT.TOGGLE);

		btnCondition = new Action() {
			public String getText() {
				return Messages.getString("StepperPanel.4");
			}

			public void run() {
				if (btnCondition.isChecked()) {
					btnScript.setChecked(false);
				}
				updateTabs();
			}
		};
		scriptBar.add(btnCondition, SWT.TOGGLE);

		quickEditorForm.getBody().setLayout(new FillLayout());

		tabs = new Composite(quickEditorForm.getBody(), SWT.NONE);
		tabs.setLayout(new StackLayout());
		// TabFolder tabs = new TabFolder(quickEditorForm.getBody(), SWT.TOP);

		// TabItem item = new TabItem(tabs, SWT.LEFT);
		// item.setText(Messages.getString("StepperPanel.3"));
		ted = new SimpleTextEditor(tabs, SWT.NONE, null);
		ted.getSourceViewer().setEditable(false);
		// item.setControl(ted);

		addBreakPointMenus(ted.getRuler());

		// item = new TabItem(tabs, SWT.LEFT);
		// item.setText(Messages.getString("StepperPanel.4"));
		bep = new SimpleTextEditor(tabs, SWT.NONE, null);
		// -- set these to false so we don't update the script
		bep.setAutoUpdate(false);
		bep.setUpdateOnFocusOut(false);
		bep.getSourceViewer().getTextWidget().addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				Breakpoint bp = breakpointForConfig(bep.getConfig());
				bp.setExpression(bep.getText());
				enableBreakpoint(bep.getConfig(), true);
			}
		});
		// tabs.setSelection(0);
		// item.setControl(bep);

		scriptBar.add(new Action() {
			@Override
			public String getText() {
				return Messages.getString("StepperPanel.5");
			}

			@Override
			public void run() {
				closeEditor();
			}
		});

		ted.addCloseListener(new Listener() {
			public void handleEvent(Event event) {
				closeEditor();
			}
		});

		btnScript.setChecked(true);
		updateTabs();

		return quickEditorForm;
	}

	protected void closeEditor() {
		if (ted != null) {
			ted.setEditingConfig(null);
			splitter.setWeights(new int[] { 100, 0 });
		}
		editingSelection = null;
	}

	public String getCompNameFromConf(BaseConfiguration element) {
		if (element == null)
			return null;
		if (element instanceof HookConfig) {
			BaseConfiguration granny = element.getParent().getParent();
			if (granny instanceof AssemblyLineConfig) {
				return (String) ((HookConfig) element).getHookName();
			} else {
				return granny.getShortName() + "." + ((HookConfig) element).getHookName();
			}
		} else if (element instanceof AttributeMapItem) {
			return element.getParent().getParent().getShortName() + "." + element.getParent().getShortName() + "."
			+ element.getShortName();
		} else if (element instanceof RawConnectorConfig) {
			return element.getParent().getShortName() + ".Connector";
		} else if (element instanceof ParserConfig ) {
			return element.getParent().getShortName() + ".Parser";
		} else if (element instanceof RawFunctionConfig) {
			return element.getParent().getShortName() + ".Function";
		} else if (element instanceof ScriptConfig) {
			String s = scripts.getName((ScriptConfig)element);
			if (s != null)
				return s;
		}
		return element.getShortName();
	}

	private void fixAnnotations(BaseConfiguration b) {
		ted.removeAnnotations();
		String comp = getCompNameFromConf(b);
		if (comp == null)
			return;

		comp += "#";
		for (Map.Entry<String, Breakpoint> entry: breakpoints.entrySet()) {
			String key = entry.getKey();
			Breakpoint bp = entry.getValue();
			if (key.startsWith(comp) && bp.isEnabled()) {
				int line = Integer.valueOf(key.substring(comp.length())) - 1;
				if (! ted.toggleBreakpointAnnotation(key, line, true))
					bp.setEnabled(false);
			}
		}
	}

	public void restoreBK(Hashtable<String, Breakpoint> saveBK) {
		restoreBK = saveBK;
	}

	private void mergeBK() {
		if (restoreBK == null)
			return;

		for (String str : restoreBK.keySet()) {
			int i = str.indexOf(".");
			String name = (i > 0) ? str.substring(0, i) : str;
			i = name.indexOf("#");
			if (i > 0)
				name = name.substring(0, i);
			if (breakpoints.get(name) == null)
				continue;
			Breakpoint bp = restoreBK.get(str);
			if (bp != null)
				breakpoints.put(str, bp);
		}
	}

	private void sendBK() {
		if (breakpoints == null)
			return;

		for(Breakpoint bp : breakpoints.values()) {
			if (bp.isEnabled()) {
				try {
					client.addBreakpoint(bp);
				} catch (Exception e) {
					EclipseAppender.logerror(e.toString(), e, getShell());
				}
			}
		}
	}

	private ArrayList<Listener> breakListener = new ArrayList<Listener>();

	public void addStepListener(Listener listener) {
		if (!breakListener.contains(listener))
			breakListener.add(listener);
	}

	private void addBreakPointMenus(final CompositeRuler ruler) {
		//
		// -- double click in ruler toggles breakpoint
		//
		ruler.getControl().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				toggleLineBreakpoint(ruler.getLineOfLastMouseButtonActivity(), ted.getConfig());
			}

		});

		Menu menu = new Menu(ruler.getControl());
		MenuItem item1 = new MenuItem(menu, SWT.NONE);
		item1.setText(Messages.getString("StepperPanel.toggle.break"));
		item1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				toggleLineBreakpoint(ruler.getLineOfLastMouseButtonActivity(), ted.getConfig());
			}
		});
		MenuItem item2 = new MenuItem(menu, SWT.NONE);
		item2.setText(Messages.getString("Debugger.Run.and.break"));
		item2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int line = ruler.getLineOfLastMouseButtonActivity();
				String comp = getCompNameFromConf(ted.getConfig());
				if (comp != null)
					breakAt(comp + "#" + (line + 1));
			}
		});

		ruler.getControl().setMenu(menu);

		ted.getMenuManager().add(new Action() {

			@Override
			public String getText() {
				return Messages.getString("StepperPanel.toggle.break");
			}

			@Override
			public void run() {
				int line = ted.getSourceViewer().getTextWidget().getLineAtOffset(
						ted.getSourceViewer().getTextWidget().getCaretOffset());
				toggleLineBreakpoint(line, ted.getConfig());
			}

		});

		ted.getMenuManager().add(new Action() {

			@Override
			public String getText() {
				return Messages.getString("Debugger.Run.and.break");
			}

			@Override
			public void run() {
				int line = ted.getSourceViewer().getTextWidget().getLineAtOffset(
						ted.getSourceViewer().getTextWidget().getCaretOffset());
				String comp = getCompNameFromConf(ted.getConfig());
				if (comp != null)
					breakAt(comp + "#" + (line + 1));
			}

		});

	}

	private void toggleLineBreakpoint(int line, BaseConfiguration bc) {
		String comp = getCompNameFromConf(bc);
		if (comp != null) {
			String text = comp + "#" + (line + 1);
			ted.toggleBreakpointAnnotation(text, line, toggleBreakpoint(text));
			breaktree.getStepTree().refresh(bc, true);
			ConnectorConfig cc = Utils.getParentConfig(bc, ConnectorConfig.class);
			if (cc != null) {
				breaktree.getStepTree().refresh(cc, true);
				if (cc.getParent() instanceof LoopConfig)
					breaktree.getStepTree().refresh(cc.getParent(), true);
			}
		}

	}

	public void breakAt(String bp) {
		try {
			client.runUntilAssemblyLine(bp);
			enableButtons(false, true);
		} catch (Exception e) {
			EclipseAppender.logerror(e.getMessage(), e);
		}
	}

	private void enableButtons(boolean step, boolean pause) {
		if (toolbar != null) {
			toolbar.getItem(STEP_INDEX).setEnabled(step);
			toolbar.getItem(STEP_OVER_INDEX).setEnabled(step);
			toolbar.getItem(CONTINUE_INDEX).setEnabled(step);
			// toolbar.getItem(EVAL_INDEX).setEnabled(step);
			// toolbar.getItem(PAUSE_INDEX).setEnabled(pause);
			toolbar.getItem(CLEAR_ALL_INDEX).setEnabled(step);
		}
		if(watchList != null) {
			watchList.setEnabledEdit(step);
		}
	}

	private void registerContextMenu(final Viewer viewer) {

		Menu menu = new Menu(viewer.getControl());
		final MenuItem item1 = new MenuItem(menu, SWT.NONE);
		item1.setText(Messages.getString("Debugger.Run.and.break"));
		item1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ISelection sel = viewer.getSelection();
				if (sel instanceof IStructuredSelection) {
					Object o = ((IStructuredSelection) sel).getFirstElement();
					if (o instanceof BaseConfiguration) {
						String comp = getCompNameFromConf((BaseConfiguration) o);
						if (comp != null)
							breakAt(comp);
					}
				}
			}
		});

		menu.addMenuListener(new MenuAdapter() {

			@Override
			public void menuShown(MenuEvent e) {
				item1.setEnabled(false);
				ISelection sel = viewer.getSelection();
				if (sel instanceof IStructuredSelection) {
					Object o = ((IStructuredSelection) sel).getFirstElement();
					if (o instanceof BaseConfiguration) {
						String comp = getCompNameFromConf((BaseConfiguration) o);
						if (isTopLevelContainers((BaseConfiguration) o))
							item1.setEnabled(false);
						else if (comp != null)
							item1.setEnabled(true);
					}
				}
			}

		});
		viewer.getControl().setMenu(menu);
	}

	public String getPrevKnownLocation() {
		return client.getState().getPrevLocation();
	}

	@Override
	public void restoreState(IMemento memento) {
		if (memento == null)
			return;

		IMemento mbp = memento.getChild(MEMENTO_BREAKPOINTS);
		if (mbp != null) {
			if (restoreBK == null)
				restoreBK = new Hashtable<String, Breakpoint>();
			for (IMemento m : mbp.getChildren(MEMENTO_BREAKPOINT)) {
				Breakpoint bp = new Breakpoint(m.getString(MEMENTO_BP_LOCATION), m.getBoolean(MEMENTO_BP_ENABLED), m
						.getString(MEMENTO_BP_EXPRESSION));
				restoreBK.put(bp.getLocation(), bp);
			}
		}

		if (watchList != null)
			watchList.restoreState(memento.getChild(MEMENTO_WATCH_LIST));

		if (columnView != null) {
			Boolean b = Activator.getDefault().getPreferenceStore().getBoolean(MEMENTO_ADVANCED);
			if (breakAtComp != null)
				b = Boolean.TRUE;
			columnView.setAdvancedMode(b);
		}

		RunALWidget.getSashSettings(sash, MEMENTO_SASH_WEIGTHS);
	}

	@Override
	public void saveState(IMemento memento) {
		if (memento == null)
			return;

		IMemento mbp = memento.getChild(MEMENTO_BREAKPOINTS);
		if (mbp == null)
			mbp = memento.createChild(MEMENTO_BREAKPOINTS);
		if(breakpoints != null) {
			for (String str : breakpoints.keySet()) {
				if (DebugServer.INIT_BREAK.equals(str))
					continue;
				Breakpoint bp = breakpoints.get(str);
				if (bp.isEnabled() || bp.getExpression() != null || (restoreBK != null && restoreBK.get(str) != null)) {
					IMemento bp2 = getBPChild(mbp, bp.getLocation());
					bp2.putBoolean(MEMENTO_BP_ENABLED, bp.isEnabled());
					bp2.putString(MEMENTO_BP_EXPRESSION, bp.getExpression());
				}
			}
		}

		if (watchList != null) {
			IMemento watch = memento.getChild(MEMENTO_WATCH_LIST);
			if (watch == null)
				watch = memento.createChild(MEMENTO_WATCH_LIST);
			watchList.saveState(watch);
		}

		if (columnView != null) {
			Activator.getDefault().getPreferenceStore().setValue(MEMENTO_ADVANCED, columnView.isAdvancedModeDebugging());
		}

		if (activeCheck != null && !activeCheck.isDisposed()) {
			Activator.getDefault().getPreferenceStore().setValue(MEMENTO_SHOW_ALL_HOOKS, activeCheck.getSelection());
		}

		if (attmapCheck != null && !attmapCheck.isDisposed()) {
			Activator.getDefault().getPreferenceStore().setValue(MEMENTO_SHOWATTRIBUTES, attmapCheck.getSelection());
		}

		RunALWidget.saveSashSettings(sash, MEMENTO_SASH_WEIGTHS);

	}

	private IMemento getBPChild(IMemento mbp, String location) {
		for (IMemento m : mbp.getChildren(MEMENTO_BREAKPOINT)) {
			if (location.equals(m.getString(MEMENTO_BP_LOCATION)))
				return m;
		}
		IMemento child = mbp.createChild(MEMENTO_BREAKPOINT);
		child.putString(MEMENTO_BP_LOCATION, location);
		return child;
	}

	/**
	 * Swap between simple and advanced views in the left hand side of the
	 * panel. Component outliner does its own swapping.
	 */
	private void toggleViews() {
		Composite c = getForm().getBody();
		StackLayout sl = (StackLayout) c.getLayout();

		sl.topControl = columnView.isAdvancedModeDebugging() ? advancedWidget : simpleWidget;
		c.layout(true, true);

		if(columnView.isAdvancedModeDebugging()) {
			if(outlineView.getBreak() != null)
				breaktree.setBreak(outlineView.getBreak());
		} else {
			outlineView.syncBreakpoint(breaktree.getBreak());
		}

		if (columnView.isAdvancedModeDebugging()) {
			((StackLayout) toolbarStack.getLayout()).topControl = toolbar;
			sash.setWeights(new int[] { 30, 70, 30 });
		} else {
			((StackLayout) toolbarStack.getLayout()).topControl = simpleToolbar;
			sash.setWeights(new int[] { 30, 70, 0 });
		}
		getForm().getHead().layout(true, true);
	}

	/**
	 * This method updates the ClearAll button's tooltip to show all current breakpoints 
	 */
	private void updateClearAllTooltip() {
		StringBuffer buf = new StringBuffer();
		for(Breakpoint bp : breakpoints.values()) {
			if(bp.isEnabled()) {
				if(buf.length() == 0)
					buf.append("\n-------------------------------");

				String loc = bp.getLocation();
				buf.append("\n");
				if(loc.indexOf(".") != -1) {
					buf.append(loc.substring(0, loc.indexOf(".")));
					String xlate = Messages.getString("Hook." + loc.substring(loc.indexOf(".")+1));
					if(xlate == null)
						xlate = loc.substring(loc.indexOf(".")+1);
					buf.append(".");
					buf.append(xlate);
				} else {
					if(((AssemblyLineConfig)getEditingConfig()).getComponent(bp.getLocation()) != null) {
						buf.append(bp.getLocation());
					} else {
						String xlate = Messages.getString("Hook." + bp.getLocation());
						if(xlate == null)
							xlate = bp.getLocation();
						buf.append(xlate);
					}
				}
			}
		}
		buf.insert(0, Messages.getString("Debugger.toolbar.ClearAll.tooltip"));
		if(clearAllItem != null)
			clearAllItem.setToolTipText(buf.toString());
	}

	/**
	 * Marks the named component as the current in the breaktree
	 * @param db contains the name that is to be marked
	 */
	public void setBreak(String name) {
		breaktree.setBreak(getConfigForPath(name));
	}

	/**
	 * Sets the name of the component we will run to. Used for "Run and Break Here".
	 * @param until
	 */
	public void setRunUntil(String until) {
		breakAtComp = until;
		if (until != null && columnView != null && !columnView.isAdvancedModeDebugging()) {
			columnView.setAdvancedMode(true);
			toggleViews();
		}
	}
}
