/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.stepper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.CheckedTreeSelectionDialog;
import org.eclipse.ui.forms.widgets.Form;

import com.ibm.di.config.interfaces.ALMappingConfig;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.LoopConfig;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.util.Breakpoint;
import com.ibm.tdi.eclipse.Activator;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.debugger.DebugClient;
import com.ibm.tdi.eclipse.debugger.DebugClientEvent;
import com.ibm.tdi.eclipse.debugger.DebugClientListener;
import com.ibm.tdi.eclipse.debugger.DebugClient.DebugBreak;
import com.ibm.tdi.eclipse.editors.RunAssemblyLineEditor;
import com.ibm.tdi.eclipse.editors.RunAssemblyLineInput;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.util.TDIToolBar;
import com.ibm.tdi.eclipse.views.EntryCollectorView;
import com.ibm.tdi.eclipse.widget.BaseWidget;

/**
 * This class shows a column for each component in the AssemblyLineConfig that
 * has an attribute map. The user can show/hide individual components via UI
 * controls. In addition there is a Run-to-here button in each component column.
 */
public class StepperColumnsWidget extends BaseWidget implements DebugClientListener {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private final static String PRE_EXP = "task.getConnector(\"";
	private final static String POST_EXP = "\").lastConn";
	
	/**
	 * The list of components with an attribute map
	 */
	private ArrayList<String> components = new ArrayList<String>();

	/**
	 * Maps Component-name to Column widget
	 */
	private Hashtable<String, TableViewer> viewers = new Hashtable<String, TableViewer>();

	/**
	 * List of components that are hidden (e.g. user has closed/hidden it)
	 */
	private ArrayList<String> hiddenComponents = new ArrayList<String>();

	/**
	 * This instance contains the entire UI. It is disposed and recreated every
	 * time we get a new configuration object.
	 */
	private BaseWidget base;

	/**
	 * This is the scroller that contains the component column widgets.
	 */
	private ScrolledComposite sc;

	/**
	 * This determines the toolbar we show
	 */
	private boolean advanced = false;

	/**
	 * Used for the run-until-here command in the component widgets
	 */
	private DebugClient client;
	private Action nextAction;
	private Action runAction;
	private Action stopAction;

	private boolean autoStepping;

	private Composite tools1;

	private Button advancedBtn;

	private EntryCollectorView collectorView;
	
	public StepperColumnsWidget(Composite parent, DebugClient client) {
		super(parent, 0);
		setLayout(new FillLayout());

		createButtonActions();

		this.client = client;
		this.client.addDebugListener(this);
	}

	/**
	 * Callback from client debugger. We monitor state changes to enable/disable
	 * buttons as well as changing the label on the Run/Continue/Pause button.
	 * 
	 * @see com.ibm.tdi.eclipse.debugger.DebugClientListener#handleEvent(com.ibm.tdi.eclipse.debugger.DebugClientEvent)
	 */
	public void handleEvent(DebugClientEvent event) {
		
		if(isDisposed()) {
			client.removeDebugListener(this);
			return;
		}

		switch (event.getCommand()) {
		case DebugClientEvent.STATE_CHANGE:
			if (advanced)
				break;
			// In simple mode we have next, run and stop buttons.
			// In this case we update label and icon when we change states.
			// To avoid flickering buttons we only change buttons when we
			// enter a "permanent" state. That is a state where we don't
			// expect an immediate change. There are three conditions. One
			// is when we get a break and we are not autoStepping (e.g. user
			// has to press next/continue to keep running). The second
			// is when the user hit Continue where we enter a Running state
			// with autoStep off. Finally, when the AL enters idle state we
			// have to update as well.
			// In all cases we have to change the text
			// and icon for the Run button (e.g. Continue/Pause/Run).
			if ((client.isWaiting() && !isAutoStepping()) || (client.isRunning() && isAutoStepping()) || client.isIdle()) {
				getDisplay().syncExec(new Runnable() {
					public void run() {
						updateActionProperties();
					}
				});
			}
			break;

		case DebugClientEvent.EVAL:
			// In this case we check the lastConn property of the connectors and
			// update the component tables.
			String expr = event.getEval();
			if (expr.startsWith(PRE_EXP) && expr.endsWith(POST_EXP)) {
				final String comp = expr.substring(PRE_EXP.length(), expr.length() - POST_EXP.length());
				final Object value = client.getWatchValue(expr);
				if (viewers.containsKey(comp)) {
					getDisplay().syncExec(new Runnable() {
						public void run() {
							if (value instanceof Entry)
								viewers.get(comp).setInput((Entry) value);
							else
								viewers.get(comp).setInput(null);
							viewers.get(comp).refresh();
							if(collectorView == null)
								collectorView = (EntryCollectorView) getEditor().getSite().getPage().findView(EntryCollectorView.VIEW_ID);
							if (collectorView != null  && value instanceof Entry)
								collectorView.addEntry(getEditor(), comp, (Entry)value, client.getCurrentCycle());
						}
					});
				}
			}
			break;

		case DebugClientEvent.BREAK:
			// In this case we send a continue if the user hit Continue;
			// otherwise we break and highlight the current component.
			if (isAutoStepping()) {
				try {
					client.continueAssemblyLine();
				} catch (Exception e) {
					EclipseAppender.logerror(e.toString(), e);
				}
			} else if (event.getData() instanceof DebugBreak){
				DebugBreak db = (DebugBreak) event.getData();
				highlightComponent(db);
			}
			break;
			
		case DebugClientEvent.SS_DISCONNECT:
			setAutoStepping(false);
			break;
			
		}
	}

	/**
	 * Sets the enabled property on the actions to force an update to the UI.
	 */
	protected void updateActionProperties() {
		// Force a prop change on visuals and enabled state. TDIToolbar will
		// respond to the property changes on the action objects.
		nextAction.setEnabled(nextAction.isEnabled());
		runAction.setEnabled(client.isWaiting() || client.isRunning());
		runAction.setText(runAction.getText());
		runAction.setImageDescriptor(runAction.getImageDescriptor());
		stopAction.setEnabled(client.isWaiting() || client.isRunning());
		
		base.getForm().getHead().layout(true, true);
	}

	/**
	 * Returns true if the component is visible (column view mode)
	 * 
	 * @param str
	 * @return
	 */
	public boolean isComponentVisible(String str) {
		if (str == null)
			return false;

		return !hiddenComponents.contains(str);
	}

	/**
	 * Changes the visibility of the named component
	 * 
	 * @param component
	 * @param visible
	 */
	public void setComponentVisible(String component, boolean visible) {
		Composite comp = viewers.get(component).getTable().getParent();
		GridData rd = (GridData) comp.getLayoutData();
		rd.exclude = !visible;
		comp.setVisible(visible);
		comp.getParent().layout(true, true);
		if (!visible)
			hiddenComponents.add(component);
		else
			hiddenComponents.remove(component);
		
		updateActionProperties();
	}

	@Override
	/**
	 * Overridden to recreate the columns for each component in the config.
	 */
	public void setEditingConfig(BaseConfiguration editingConfig) {
		super.setEditingConfig(editingConfig);
		if (editingConfig instanceof AssemblyLineConfig) {
			createColumnsView((AssemblyLineConfig) getEditingConfig(), this);
			addWatchVariables();
		}
	}

	private void addWatchVariables() {
		try {
			for (String str : components) {
				client.addWatch(PRE_EXP + str + POST_EXP);
			}
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e);
		}
	}

	/**
	 * Create the UI controls for each component in the AL
	 * 
	 * @param alc
	 * @param parent
	 */
	private void createColumnsView(AssemblyLineConfig alc, Composite parent) {
		List<ConnectorConfig> connectorList = new ArrayList<ConnectorConfig>();
		List<BaseConfiguration> list = alc.getEntryFeedComponents().getConfigurations(null);
		alc.getDataFlowComponents().getConfigurations(list);
		for (BaseConfiguration bc : list) {
			if (bc instanceof ConnectorConfig && bc.getEnabled() && !(bc instanceof ALMappingConfig) && 
					!ConnectorConfig.SCRIPT_MODE.equals(((ConnectorConfig)bc).getMode())) {
				connectorList.add((ConnectorConfig) bc);
			} else if (bc instanceof LoopConfig) {
				LoopConfig lc = (LoopConfig) bc;
				if(lc.getLoopType() == LoopConfig.LOOP_CONNECTOR_FC) {
					try {
						connectorList.add(lc.getLoopConnector());
					} catch (Exception e1) {
						EclipseAppender.logerror(e1.toString(), e1);
					}
				}
			}
		}

		if (base != null && !base.isDisposed()) {
			base.dispose();
		}

		base = new BaseWidget(parent, 0);
		base.setLayout(new FillLayout());

		Form frm = base.createForm(base, null);
		frm.setText(Messages.getString("ColumnDataFlow_dataflow_and_mapping"));

		//
		// -- Toolbar
		//
		Composite toolbar = new Composite(frm.getHead(), 0);
		Utils.setGridLayout(toolbar, 2, false);

		tools1 = new Composite(toolbar, 0);
		RowLayout rlayout = new RowLayout(SWT.HORIZONTAL);
		rlayout.marginBottom = 0;
		rlayout.marginLeft = 0;
		rlayout.marginRight = 0;
		rlayout.marginTop = 0;
		tools1.setLayout(rlayout);
		tools1.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite tools2 = new Composite(toolbar, 0);
		Utils.setGridLayout(tools2, 1, false);
		advancedBtn = new Button(tools2, SWT.PUSH);
		advancedBtn.setText(Messages.getString("AssemblyLineEditor.debug.advanced"));
		advancedBtn.setToolTipText(Messages.getString("AssemblyLineEditor.debug.advanced.tooltip"));
		//advancedBtn.setSelection(this.advanced);
		advancedBtn.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		advancedBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				//toggleToolbars(tools1, ((Button) e.widget).getSelection());
				toggleToolbars(tools1, ((Button) e.widget).getText().equals(Messages.getString("AssemblyLineEditor.debug.advanced")));
				fireAdvancedModeChanged();
			}
		});

		toggleToolbars(tools1, advanced);
		
		frm.setHeadClient(toolbar);

		//
		// -- Create scrolled composite in the form body to hold the columns
		//
		frm.getBody().setLayout(new FillLayout());

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

		layout(true, true);
	}

	/**
	 * Called to swap the advanced/simple toolbar in the header
	 * @param advancedButton 
	 * 
	 * @param parent
	 * @param advanced
	 */
	protected void toggleToolbars(Composite parent, boolean advanced) {

		this.advanced = advanced;

		// -- first clear the current list of buttons
		for (Control c : parent.getChildren()) {
			c.dispose();
		}

		TDIToolBar tb = new TDIToolBar(parent, 0, true, false, false);

		// -- If it is not advanced we display Next, Run buttons
		if (!advanced) {
			tb.add(nextAction);
			tb.add(runAction);
			tb.add(stopAction);
			updateActionProperties();
		}

		createColumnsViewCompSelectorButton(tb);

		if(!advanced) {
			advancedBtn.setText(Messages.getString("AssemblyLineEditor.debug.advanced"));
			advancedBtn.setToolTipText(Messages.getString("AssemblyLineEditor.debug.advanced.tooltip"));
		} else {
			advancedBtn.setText(Messages.getString("AssemblyLineEditor.debug.simple"));
			advancedBtn.setToolTipText(Messages.getString("AssemblyLineEditor.debug.simple.tooltip"));
		}

		base.getForm().getHead().layout(true, true);
	}

	/**
	 * Creates the action objects to step, run and stop the assemblyline.
	 */
	private void createButtonActions() {
		nextAction = new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return null;
			}

			@Override
			public String getText() {
				return Messages.getString("ColumnDataFlow_next");
			}

			@Override
			public String getToolTipText() {
				return getText();
			}

			@Override
			public void run() {
				try {
					setColumnViewBreakpoints();
					setAutoStepping(false);
					client.continueAssemblyLine();
				} catch (Exception e) {
					EclipseAppender.logerror(e.toString(), e, getShell());
				}
			}

			@Override
			public boolean isEnabled() {
				return client.isWaiting() && components.size() > hiddenComponents.size();
			}
			
		};

		runAction = new Action() {

			/**
			 * Button labels we use when the assemblyline is running/paused
			 * (Pause when running and Continue when paused)
			 */
			private String pauseLabel = Messages.getString("Debugger.toolbar.Pause.label");
			private String continueLabel = Messages.getString("Debugger.toolbar.Continue.label");
			private String runLabel = Messages.getString("ColumnDataFlow_run");
			private String restartTT= Messages.getString("RunAL.restart.tooltip");

			private ImageDescriptor runIcon = Activator.getImageDescriptor("icons/Run.gif");
			private ImageDescriptor pauseIcon = Activator.getImageDescriptor("icons/Pause.gif");

			@Override
			public ImageDescriptor getImageDescriptor() {
				if (client.isIdle() || client.isWaiting())
					return runIcon;
				else
					return pauseIcon;
			}

			@Override
			public String getText() {
				if (client.isWaiting())
					return continueLabel;
				else if (client.isRunning())
					return pauseLabel;
				else
					return runLabel;
			}

			@Override
			public String getToolTipText() {
				if(client.isIdle())
					return restartTT;
				else
					return getText();
			}

			@Override
			public void run() {
				try {
					// We have to be careful here. The debugger can switch states quickly
					// so we simply turn off autoStepping to stop at the next break.
					if(isAutoStepping()) {
						setAutoStepping(false);
					} else if (client.isWaiting()) {
						setColumnViewBreakpoints();
						setAutoStepping(true);
						client.continueAssemblyLine();
					} else if (client.isIdle() && getEditor() instanceof RunAssemblyLineEditor) {
							((RunAssemblyLineEditor) getEditor()).restart((RunAssemblyLineInput) getEditor().getEditorInput());
					}
				} catch (Exception e) {
					EclipseAppender.logerror(e.toString(), e, getShell());
				}
			}

			@Override
			public boolean isEnabled() {
				return client.isWaiting() || client.isRunning() || (client.isIdle() && getEditor() instanceof RunAssemblyLineEditor);
			}
		};

		stopAction = new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return Activator.getImageDescriptor("icons/Stop.gif");
			}

			@Override
			public String getText() {
				return Messages.getString("Debugger.toolbar.Stop.label");
			}

			@Override
			public String getToolTipText() {
				return getText();
			}

			@Override
			public void run() {
				try {
					client.stopAssemblyLine();
				} catch (Exception e) {
					EclipseAppender.logerror(e.toString(), e, getShell());
				}
			}

			@Override
			public boolean isEnabled() {
				return client.isWaiting() || client.isRunning();
			}
		};
	}

	/**
	 * When true the stepper will auto-continue each breakpoint
	 * 
	 * @param autoStep
	 */
	protected void setAutoStepping(boolean autoStepping) {
		this.autoStepping = autoStepping;
	}

	protected boolean isAutoStepping() {
		return this.autoStepping;
	}

	/**
	 * Clear the <component>.default_ok for all components and then sets the
	 * same breakpoint for those components that are visible. The user may
	 * change component visibility in the UI.
	 * 
	 * @throws Exception
	 */
	protected void setColumnViewBreakpoints() throws Exception {
		// -- first remove all prior ones
		for (String str : components) {
			client.removeBreakpoint(str + ".default_ok");
		}

		// -- Now add all visible components
		for (String str : components) {
			if (!hiddenComponents.contains(str)) {
				client.addBreakpoint(new Breakpoint(str + ".default_ok", true, null));
			}
		}
	}

	/**
	 * Updates the min size of the ScrolledComposite to refresh scroll bars
	 */
	private void updateMinSize() {
		Point size = sc.getContent().computeSize(SWT.DEFAULT, sc.getClientArea().height);
		sc.setMinSize(size);
	}

	/**
	 * Creates a Group with controls to display a component in the column view.
	 * 
	 * @param cc
	 * @param parent
	 * @return
	 */
	private Composite createColumnTable(ConnectorConfig cc, Composite parent) {

		final Group g = new Group(parent, SWT.SHADOW_OUT);
		g.setText(cc.getShortName() + " (" + cc.getMode() + ")");
		g.setLayout(new GridLayout(1, false));

		ToolBar bar = new ToolBar(g, SWT.HORIZONTAL);
		bar.setLayoutData(new GridData(SWT.RIGHT, SWT.DEFAULT, true, false));

		//
		// -- Run to here button
		//
		ToolItem runHere = new ToolItem(bar, SWT.PUSH);
		runHere.setImage(Activator.getImage("runtoline"));
		runHere.setToolTipText(Messages.getString("client.Run.and.break"));
		runHere.setData("name", cc.getShortName());
		runHere.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				try {
					if (client.isWaiting())
						client.runUntilAssemblyLine((String) e.widget.getData("name") + ".default_ok");
				} catch (Exception err) {
					EclipseAppender.logerror(err.toString(), err, getShell());
				}
			}
		});

		//
		// -- Hide button
		//
		ToolItem hide = new ToolItem(bar, SWT.PUSH);
		hide.setToolTipText(Messages.getString("ColumnDataFlow_hide_button_tooltip"));
		hide.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_DELETE));
		hide.setData("name", cc.getShortName());
		hide.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setComponentVisible((String) e.widget.getData("name"), false);
			}
		});

		//
		// -- Table viewer for the lastConn entry
		//
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
	 * Give the named component input focus (e.g. focus rect painted). Component
	 * is automatically scrolled into view by the ScrolledComponent.
	 * 
	 * @param component
	 */
	protected String highlightComponent(final DebugBreak db) {
		String component = db.getComponent();
		if (component == null || hiddenComponents.contains(component))
			return null;
		
		final int index = components.indexOf(component);
		if (index != -1 && viewers.get(component) != null) {
			getDisplay().syncExec(new Runnable() {
				public void run() {
					updateBackgroundColors(components.get(index));
					StepperPanel p = Utils.getParentConfig(getParent(), StepperPanel.class);
					String bp = db.getBreakpoint();
					if (p != null && bp != null && !bp.contains("#"))
						p.setBreak(bp);
				}
			});
			return component;
		}
		return null;
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
	protected void createColumnsViewCompSelectorButton(TDIToolBar toolbar) {
		Button hide = toolbar.addButton(SWT.PUSH);
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
	 * Returns true if user has chosen advanced mode debugging
	 * 
	 * @return
	 */
	public boolean isAdvancedModeDebugging() {
		return advanced;
	}

	/**
	 * Sets the view mode to advanced(true) or simple(false).
	 * 
	 * @param advanced
	 */
	public void setAdvancedMode(Boolean advanced) {
		//advancedBtn.setSelection(advanced);
		if (advanced != null) {
			toggleToolbars(tools1, advanced);
			fireAdvancedModeChanged();
		}
	}

	//
	// -- For those interested in whether advanced mode debugging is true/false
	//
	private ArrayList<Listener> listeners = new ArrayList<Listener>();

	public void addAdvancedModeListener(Listener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}

	public void removeAdvancedModeListener(Listener listener) {
		listeners.remove(listener);
	}

	private void fireAdvancedModeChanged() {
		ArrayList<Listener> copy = new ArrayList<Listener>(listeners);
		Event e = new Event();
		e.widget = this;
		for (Listener l : copy)
			l.handleEvent(e);
	}

}
