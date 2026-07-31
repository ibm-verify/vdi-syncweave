/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.editors;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.forms.IMessage;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.ide.IGotoMarker;

import com.ibm.di.config.base.BaseConfigurationImpl;
import com.ibm.di.config.base.InternalSchema;
import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.ALMappingConfig;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.AttributeMapItem;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.BranchingConfig;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.HookConfig;
import com.ibm.di.config.interfaces.LinkCriteriaConfig;
import com.ibm.di.config.interfaces.LoopConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.ScriptConfig;
import com.ibm.di.function.SystemFunctions;
import com.ibm.tdi.eclipse.Activator;
import com.ibm.tdi.eclipse.ConfigUtils;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.TDI;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.actions.ALSandboxAction;
import com.ibm.tdi.eclipse.actions.ALSettingsAction;
import com.ibm.tdi.eclipse.actions.ALSimulationAction;
import com.ibm.tdi.eclipse.actions.ConfigureLoggingAction;
import com.ibm.tdi.eclipse.actions.CopyConfigAction;
import com.ibm.tdi.eclipse.actions.CutConfigAction;
import com.ibm.tdi.eclipse.actions.EnableHooksAction;
import com.ibm.tdi.eclipse.actions.InsertComponent;
import com.ibm.tdi.eclipse.actions.PasteConfigAction;
import com.ibm.tdi.eclipse.actions.RenameConfigAction;
import com.ibm.tdi.eclipse.actions.operations.InsertConfigOperation;
import com.ibm.tdi.eclipse.actions.operations.MoveConfigOperation;
import com.ibm.tdi.eclipse.builders.AssemblyLineValidator;
import com.ibm.tdi.eclipse.builders.ComponentValidator;
import com.ibm.tdi.eclipse.builders.IncrementalConfigBuilder;
import com.ibm.tdi.eclipse.commands.CommandHandlerProxy;
import com.ibm.tdi.eclipse.dialogs.GenericFormDialog;
import com.ibm.tdi.eclipse.dialogs.RunOptionsDialog;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.preferences.PreferenceConstants;
import com.ibm.tdi.eclipse.providers.AssemblyLineContentProvider3;
import com.ibm.tdi.eclipse.providers.ConfigLabelProvider;
import com.ibm.tdi.eclipse.util.CustomEditorSettings;
import com.ibm.tdi.eclipse.util.TDIToolBar;
import com.ibm.tdi.eclipse.util.InheritanceUtil;
import com.ibm.tdi.eclipse.views.ServerView;
import com.ibm.tdi.eclipse.widget.AttributeMapItemEditor;
import com.ibm.tdi.eclipse.widget.BaseWidget;
import com.ibm.tdi.eclipse.widget.BranchWidget;
import com.ibm.tdi.eclipse.widget.ConnectorWidget;
import com.ibm.tdi.eclipse.widget.HookItemWidget;
import com.ibm.tdi.eclipse.widget.LinkCriteriaWidget;
import com.ibm.tdi.eclipse.widget.LoopConfigWidget;
import com.ibm.tdi.eclipse.widget.ScriptWidget;
import com.ibm.tdi.eclipse.widget.TitledAttributeMapWidget;
import com.ibm.tdi.eclipse.widget.WorkMapWidget;
import com.ibm.tdi.eclipse.wizards.ALSettingsWizard;

public class AssemblyLineEditor3 extends BaseEditor {
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private TreeViewer tree;
	private AssemblyLineContentProvider3 alcp;
	private ConfigLabelProvider clp;
	private CustomEditorSettings settings;
	private TDIToolBar header;
	private WorkMapWidget workMapWidget;
	private Action insertAction;
	private ArrayList<TDIConfigEditorInput> openEditors = new ArrayList<TDIConfigEditorInput>();

	private Composite itemEditorPanel;

	private StackLayout itemEditorStack;

	private static String LAST_USED = "LastUsed";
	private static int MAX_WIDGETS_IN_STACK = 7;
	
	public AssemblyLineEditor3() {
		super();

		//
		// -- global action handlers
		//
		registerAction(ActionFactory.CUT.getId(), new CutConfigAction(Messages.getString("common.Cut.name"), null)); //$NON-NLS-1$
		registerAction(ActionFactory.COPY.getId(), new CopyConfigAction(Messages.getString("common.Copy.name"))); //$NON-NLS-1$
		registerAction(ActionFactory.DELETE.getId(), new CutConfigAction(Messages.getString("general.delete.label"), null)); //$NON-NLS-1$
		registerAction(ActionFactory.PASTE.getId(), new PasteAction());
	}

	@Override
	public void createPartControl(Composite parent) {
		if (getTDIConfiguration() == null) {
			super.createPartControl(parent);
			return;
		}

		BaseWidget base = new BaseWidget(parent, SWT.NONE);
		base.setLayout(new FillLayout());

		Form form = base.createForm(base, null);
		form.getBody().setLayout(new FillLayout());

		// Toolbar
		header = new TDIToolBar(form);
		header.setText(getTDIConfiguration().getShortName());
		header.setImage(Activator.getImage(getTDIConfiguration()));
		createMainButtons(header);

		// -- The main al views
		createMainPanel(form.getBody());

		verifyEntryFeed();
	}

	private void createMainButtons(TDIToolBar header) {

		Button insButton = header.addButton(SWT.PUSH);
		insButton.setText(Messages.getString("AssemblyLineEditor3.insert"));
		insButton.setImage(Activator.getImage("NewComponent"));
		insButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				insertAction.run();
			}
		});

		Button showAll = header.addButton(SWT.PUSH);
		showAll.setText(Messages.getString("AssemblyLineEditor3.showall"));
		showAll.setImage(Activator.getImage("AttributeMap"));
		showAll.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				tree.setSelection(new StructuredSelection(((AssemblyLineConfig) getTDIConfiguration()).getDataFlowComponents()),
						true);
			}
		});

		Button opt = header.addButton(SWT.PUSH);
		opt.setText(Messages.getString("AssemblyLineEditor3.options"));
		opt.setImage(Activator.getImage("Settings"));
		opt.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Menu menu = createOptionsMenu((Control) e.widget);
				menu.setVisible(true);
			}
		});

		// -- Add a separator between buttons and run controls
		header.addLabel(SWT.LEFT).setText("        ");

		final IAction runAction = new Action() {
			public void run() {
				runAssemblyLine(null, false);
			}

			public String getText() {
				return Messages.getString("AssemblyLineEditor.run.console");
			}

			public String getActionDefinitionId() {
				return "com.ibm.tdi.rcp.runal.action";
			}

			public String getToolTipText() {
				return Messages.getMessage("AssemblyLineEditor3.run.tooltip", Messages.getString("general.runnobreak.label"),
						getAssociatedServer());
			}

			public ImageDescriptor getImageDescriptor() {
				return Activator.getImageDescriptor("icons/Run.gif");
			}
		};
		header.add(runAction);
		new CommandHandlerProxy(getEditorSite(), runAction);

		IAction debugAction = new Action() {
			public ImageDescriptor getImageDescriptor() {
				return Activator.getImageDescriptor("icons/Step.gif");
			}

			public String getToolTipText() {
				return Messages.getString("AssemblyLineEditor.debug.advanced.tooltip");
			}

			public String getText() {
				return Messages.getString("AssemblyLineEditor.debug.advanced");
			}

			public String getActionDefinitionId() {
				return "com.ibm.tdi.rcp.debugal.action";
			}

			public void run() {
				runAssemblyLine(null, true);
			}
		};
		header.add(debugAction);
		new CommandHandlerProxy(getEditorSite(), debugAction);

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

	protected String getAssociatedServer() {
		try {
			return Utils.getTDIServer(getTDIConfigFile());
		} catch (CoreException e) {
			return e.toString();
		}
	}

	public void createMainPanel(Composite parent) {
		SashForm sash = new SashForm(parent, SWT.HORIZONTAL);
		// To make the divider visible, we set background to gray.
		sash.setBackground(sash.getDisplay().getSystemColor( SWT.COLOR_GRAY));
		
		// -- Create the tools in the form toolbar
		createAssemblyLineActions();

		Composite left = new Composite(sash, SWT.BORDER);
		left.setBackground(left.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		Utils.setGridLayout(left, 1, false);

		//
		// -- Tree Viewer
		//
		tree = new TreeViewer(left, SWT.MULTI | SWT.FULL_SELECTION);
		tree.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));

		alcp = new AssemblyLineContentProvider3();
		alcp.setFeedFlowShown(true);
		alcp.setHooksIncluded(true);
		alcp.setAlHooksIncluded(false);
		alcp.setAttributeMapsShown(false);
		tree.setContentProvider(alcp);

		clp = new ConfigLabelProvider();
		clp.setSimpleInlineIcons(true);
		clp.setIncludeHooks(!alcp.isHooksIncluded());
		tree.setLabelProvider(clp);
		tree.setInput(getTDIConfiguration());
		tree.expandAll();

		// -- Enable tooltip pr item
		ColumnViewerToolTipSupport.enableFor(tree);

		//
		// Single click opens item editor
		//

		tree.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				editItem(((IStructuredSelection) event.getSelection()));
			}
		});

		tree.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				editItem(((IStructuredSelection) event.getSelection()), true);
			}
		});

		//
		// INSERT key opens insert wizard
		// F2 is hardwired to rename, because sometimes Eclipse loses the binding?
		//
		tree.getTree().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.INSERT && insertAction.isEnabled())
					insertAction.run();
				if (e.keyCode == SWT.F2) {
					doRename((IStructuredSelection) tree.getSelection());
				}
			}
		});

		addDragDropSupport();

		// -- Create menu for contributions
		super.registerContextMenu(tree);
		super.addSelectionProvider(tree);

		// Add edit actions to right click menu.
		// Only need Cut, Copy and Paste, as Delete is added in plugin.xml
		for (String s: new String[] {ActionFactory.CUT.getId(), ActionFactory.COPY.getId(), ActionFactory.PASTE.getId()}) {
			getMenuManager().appendToGroup("group.tdi", new DynamicActionItem(getActionFor(s))); 
		}

		
		// -- Add move up/down menu options
		Action moveUpAction = new Action() {

			@Override
			public String getText() {
				return Messages.getString("general.moveup.label");
			}

			@Override
			public String getToolTipText() {
				return Messages.getString("general.moveup.tooltip");
			}

			@Override
			public void run() {
				BaseConfiguration source = getSelectedConfig();
				if (source != null && source.getParent() instanceof ContainerConfig) {
					ContainerConfig cc = (ContainerConfig) source.getParent();
					if (cc.indexOf(source) > 0) {
						cc.moveConfig(source, true);
					} else if (cc.getParent() instanceof ContainerConfig) {
						ContainerConfig pcc = (ContainerConfig) cc.getParent();
						cc.removeConfig(source);
						pcc.insertConfig(source, pcc.indexOf(cc));
					}
					tree.setSelection(new StructuredSelection(source));
				}
			}

			@Override
			public String getActionDefinitionId() {
				return "com.ibm.tdi.rcp.moveup";
			}

			@Override
			public boolean isEnabled() {
				BaseConfiguration source = getSelectedConfig();
				if (source == null || !( source.getParent() instanceof ContainerConfig))
					return false;
				ContainerConfig cc = (ContainerConfig) source.getParent();
				return (cc.indexOf(source) > 0 || cc.getParent() instanceof ContainerConfig);
			}
		};
		getMenuManager().appendToGroup(TDI.GROUP_TDI + ".1", new DynamicActionItem(moveUpAction)); 

		Action moveDownAction = new Action() {

			@Override
			public String getText() {
				return Messages.getString("general.movedown.label");
			}

			@Override
			public String getToolTipText() {
				return Messages.getString("general.movedown.tooltip");
			}

			@Override
			public void run() {
				BaseConfiguration source = getSelectedConfig();
				ContainerConfig cc = null;
				if (source != null && source.getParent() instanceof ContainerConfig) {
					cc = (ContainerConfig) source.getParent();
					if (cc.indexOf(source) < cc.size() - 1) {
						cc.moveConfig(source, false);
					} else if (cc.getParent() instanceof ContainerConfig) {
						ContainerConfig pcc = (ContainerConfig) cc.getParent();
						cc.removeConfig(source);
						pcc.insertConfig(source, pcc.indexOf(cc) + 1);
					}
					tree.setSelection(new StructuredSelection(source));
				}
			}

			@Override
			public String getActionDefinitionId() {
				return "com.ibm.tdi.rcp.movedown";
			}

			@Override
			public boolean isEnabled() {
				BaseConfiguration source = getSelectedConfig();
				if (source == null || !( source.getParent() instanceof ContainerConfig))
					return false;
				ContainerConfig cc = (ContainerConfig) source.getParent();
				return (cc.indexOf(source) < cc.size() - 1 || cc.getParent() instanceof ContainerConfig);
			}
		};
		
		getMenuManager().appendToGroup(TDI.GROUP_TDI + ".1", new DynamicActionItem(moveDownAction)); 
		
		// -- Create toolbar for tree
		Composite toolbar = new Composite(left, SWT.NONE);
		toolbar.setLayout(new RowLayout(SWT.HORIZONTAL));
		Button b = new Button(toolbar, SWT.PUSH);
		b.setText(Messages.getString("AssemblyLineEditor3.collapse"));
		b.setToolTipText(Messages.getString("AssemblyLineEditor3.collapse.tooltip"));
		b.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				tree.collapseAll();
			}
		});

		Button expandAll = new Button(toolbar, SWT.PUSH);
		expandAll.setText(Messages.getString("AssemblyLineEditor3.expand"));
		expandAll.setToolTipText(Messages.getString("AssemblyLineEditor3.expand.tooltip"));
		expandAll.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				tree.expandAll();
			}
		});

		Button opts = new Button(toolbar, SWT.PUSH);
		opts.setText(Messages.getString("AssemblyLineEditor3.options"));
		opts.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				createTreeOptionsMenu((Control) e.widget).setVisible(true);
			}
		});
		toolbar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// -- Create stack panel for editing items
		itemEditorStack = new StackLayout();
		itemEditorPanel = new Composite(sash, SWT.NONE);
		itemEditorPanel.setLayout(itemEditorStack);

		workMapWidget = new WorkMapWidget(itemEditorPanel, SWT.NONE, getTDIConfiguration(), true, true,
				WorkMapWidget.MAP_MODE_BOTH, this);

		itemEditorStack.topControl = workMapWidget;

		sash.setWeights(new int[] { 25, 75 });
	}

	/**
	 * Returns the current single selection. If nothing selected or multiple
	 * selected this method returns null.
	 * 
	 * @return
	 */
	public BaseConfiguration getSelectedConfig() {
		IStructuredSelection sel = (IStructuredSelection) tree.getSelection();
		if (sel.size() != 1)
			return null;
		else
			return (BaseConfiguration) sel.getFirstElement();
	}

	/**
	 * Activate the default editor for an item
	 * 
	 * @param firstElement
	 */
	public IEditorPart editItem(IStructuredSelection selection) {
		return editItem(selection, false);
	}

	public IEditorPart editItem(IStructuredSelection selection, boolean dblClick) {
		IEditorPart editorPart = null;

		if (!selection.isEmpty()) {
			Object obj = selection.getFirstElement();
			// LoopConfig loopConfig = null;
			// if (obj instanceof LoopConfig && ((LoopConfig) obj).getLoopType()
			// == LoopConfig.LOOP_CONNECTOR_FC) {
			// loopConfig = (LoopConfig) obj;
			// }

			if (addWidgetToEditorStack((BaseConfiguration) obj))
				return editorPart;

			// if (obj instanceof ConnectorConfig || loopConfig != null) {
			// try {
			// TDIConfigEditorInput editorInput = new
			// TDIConfigEditorInput((BaseConfiguration) obj);
			// ConnectorConfig cc = (ConnectorConfig) (loopConfig != null ?
			// loopConfig.getLoopConnector() : obj);
			// String editorID = null;
			// if (ConnectorConfig.SCRIPT_MODE.equals(cc.getMode()))
			// editorID = ScriptEditor.ID;
			// else if (obj instanceof ALMappingConfig)
			// editorID = AttributeMapEditor.ID;
			// else
			// editorID = null;
			// if(editorID == null) {
			// addWidgetToEditorStack(cc);
			// } else {
			// IWorkbenchPage page =
			// getSite().getWorkbenchWindow().getActivePage();
			// editorPart = page.openEditor(editorInput, editorID, true,
			// IWorkbenchPage.MATCH_INPUT | IWorkbenchPage.MATCH_ID);
			// openEditors.add(editorInput);
			// }
			// } catch (Exception e) {
			// e.printStackTrace();
			// }
			// return editorPart;
			// }

			// Ignore Feed/Flow
			AssemblyLineConfig alc = (AssemblyLineConfig) getTDIConfiguration();
			if (alc.getEntryFeedComponents() == obj || alc.getDataFlowComponents() == obj) {
				itemEditorStack.topControl = workMapWidget;
				itemEditorPanel.layout();
				return editorPart;
			}

			// -- Placeholders for empty branches
			if (obj instanceof BaseConfiguration && dblClick) {
				BaseConfiguration bc = (BaseConfiguration) obj;
				ContainerConfig cc = (ContainerConfig) bc.getParameter("%%PLACEHOLDER%%"); //$NON-NLS-1$
				if (cc != null) {
					InsertComponent ic = new InsertComponent(getSite().getShell(), getTDIConfiguration(), true);
					ic.setLocation(cc);
					ic.run();
					tree.refresh(cc);
					handleComponentInserted(ic.getComponent());
					return editorPart;
				}
			}

			quickEdit(selection);
			if (!isEditorVisible()) {
				return editorPart;
			}

		} else {
			itemEditorStack.topControl = workMapWidget;
			itemEditorPanel.layout();
		}

		return editorPart;
	}

	public boolean addWidgetToEditorStack(BaseConfiguration bc) {
		final BaseConfiguration config = bc;
		if (bc instanceof ConnectorConfig)
			Utils.verifyMode((ConnectorConfig) bc);
		BusyIndicator.showWhile(getSite().getShell().getDisplay(), new Runnable() {
			public void run() {
				addWidgetToEditorStackUI(config);
			}
		});
		return itemEditorStack.topControl != workMapWidget;
	}

	public boolean addWidgetToEditorStackUI(BaseConfiguration bc) {

		Composite widget = findEditorStackItem(bc);
		if (widget == null && bc != null) {
			if (bc instanceof ALMappingConfig)
				widget = new TitledAttributeMapWidget(itemEditorPanel, SWT.NONE, (ConnectorConfig) bc, this);
			else if (bc instanceof ConnectorConfig) {
				if (Utils.hasConnectorRequirements((ConnectorConfig) bc))
					widget = new ConnectorWidget(itemEditorPanel, SWT.NONE, bc, this);
				else
					widget = createScriptWidget(bc);
			} else if (bc instanceof ScriptConfig)
				widget = createScriptWidget(bc);
			else if (bc instanceof LoopConfig) {
				LoopConfig lc = (LoopConfig) bc;
				if (lc.getLoopType() == LoopConfig.LOOP_CONNECTOR_FC)
					widget = new ConnectorWidget(itemEditorPanel, SWT.NONE, bc, this);
				else
					widget = new LoopConfigWidget(itemEditorPanel, SWT.NONE, (LoopConfig) bc, this);
			} else if (bc instanceof BranchingConfig) {
				widget = new BranchWidget(itemEditorPanel, SWT.NONE, (BaseConfiguration) bc);
			} else if (bc instanceof HookConfig) {
				// ConnectorConfig cc = Utils.getParentConfig(bc,
				// ConnectorConfig.class);
				// if(cc != null && addWidgetToEditorStackUI(cc)) {
				// if(itemEditorStack.topControl instanceof BaseWidget) {
				// BaseWidget bw = (BaseWidget) itemEditorStack.topControl;
				// tree.setSelection(new StructuredSelection(cc));
				// return bw.revealConfigUI(bc);
				// }
				// } else {
				widget = new HookItemWidget((HookConfig) bc, itemEditorPanel, SWT.CLOSE, this);
				// }
			} else if (bc instanceof AttributeMapConfig) {
				return true;
				// tree.setSelection(new
				// StructuredSelection(Utils.getParentConfig(bc,
				// ConnectorConfig.class)));
				// widget = new ConnectorWidget(itemEditorPanel, SWT.NONE,
				// Utils.getParentConfig(bc, ConnectorConfig.class), this);
			} else if (bc instanceof AttributeMapItem) {
				widget = new AttributeMapItemEditor(itemEditorPanel, SWT.NONE);
				((AttributeMapItemEditor) widget).editAttribute(bc);
				((AttributeMapItemEditor) widget).addCloseListener(new Listener() {
					public void handleEvent(Event event) {
						addWidgetToEditorStack(null);
					}
				});
				// widget = new AttributeMapItemWidget(itemEditorPanel,
				// SWT.NONE, bc);
			} else if (bc instanceof LinkCriteriaConfig) {
				widget = new LinkCriteriaWidget((LinkCriteriaConfig) bc, itemEditorPanel, SWT.NONE);
			}

			if (widget instanceof BaseWidget)
				((BaseWidget) widget).setEditor(this);
		}

		boolean retval = widget != null;

		// Hide?
		if (widget == itemEditorStack.topControl) {
			widget = null;
		}

		if (widget != null) {
			itemEditorStack.topControl = widget;
			if (widget instanceof BranchWidget)
				((BranchWidget) widget).updateToolBar();
			widget.setData(LAST_USED, new Long(new Date().getTime()));
		} else {
			itemEditorStack.topControl = workMapWidget;
		}

		itemEditorPanel.layout();

		return retval;
	}

	private BaseWidget createScriptWidget(BaseConfiguration bc) {
		BaseWidget base = new BaseWidget(itemEditorPanel, SWT.NONE);
		base.setLayout(new FillLayout());
		base.createForm(base, null);
		base.getForm().getBody().setLayout(new FillLayout());
		TDIToolBar bar = new TDIToolBar(base.getForm());
		bar.setText(bc.getShortName());
		bar.setImage(Activator.getImage(bc));

		bar.addInheritanceButton(bc);

		bar.add(new Action() {
			public String getText() {
				return Messages.getString("LBL.CLOSE");
			}

			public void run() {
				addWidgetToEditorStack(null);
			}
		});
		new ScriptWidget(bc, base.getForm().getBody(), SWT.NONE);
		base.setEditingConfig(bc);
		return base;
	}

	private Composite findEditorStackItem(BaseConfiguration cc) {
		if (cc == null)
			return null;
		int n = 0;
		long oldestTime = new Date().getTime();
		BaseWidget oldestWidget = null;

		for (Control c : itemEditorPanel.getChildren()) {
			if (c instanceof BaseWidget) {
				BaseWidget widget = (BaseWidget) c;
				if (widget.getEditingConfig() == cc) {
					return widget;
				}

				n++;
				Long time = (Long) widget.getData(LAST_USED);
				if (time != null && time < oldestTime && widget != workMapWidget) {
					oldestWidget = widget;
					oldestTime = time;
				}
			}
		}
		
		if (n > MAX_WIDGETS_IN_STACK && oldestWidget != null)
			oldestWidget.dispose();
		
		return null;
	}

	private void createAssemblyLineActions() {

		insertAction = new Action() {
			public void run() {
				InsertComponent ic = new InsertComponent(getSite().getShell(), getTDIConfiguration(), true /*
																											 * feedFlowCheck.
																											 * getSelection
																											 * (
																											 * )
																											 */);
				ic.run();
				handleComponentInserted(ic.getComponent());
			}

			public String getToolTipText() {
				return Messages.getString("general.insert.tooltip"); //$NON-NLS-1$
			}

			public ImageDescriptor getImageDescriptor() {
				return Activator.getImageDescriptor("icons/NewComponent.gif"); //$NON-NLS-1$
			}
		};

	}

	public void handleComponentInserted(BaseConfiguration component) {
		if (component == null)
			return;
		tree.setSelection(new StructuredSelection(component));
		tree.setExpandedState(component, true);

		if (! (component instanceof BranchingConfig)
				|| ((BranchingConfig) component).getBranchType() == BranchingConfig.BRANCH_SWITCH)
			return;

		// -- branches should have children so prompt user to add one right away,
		// unless the user has specified that the dialog should not be shown.

		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		boolean add = store.getBoolean(PreferenceConstants.P_ADD_COMPONENT_NOW);
		if (store.getBoolean(PreferenceConstants.P_SHOW_ADD_COMPONENT_POPUP)) {
			MessageDialogWithToggle dialog = MessageDialogWithToggle.openYesNoQuestion(
					getSite().getShell(), 
					Messages.getString("AssemblyLineEditor3.AddComponent.title"),
					Messages.getString("AddBranchComponent.label"),
					null, false, null, null);
			add = dialog.getReturnCode() == IDialogConstants.YES_ID;
			if (dialog.getToggleState()) {
				store.setValue(PreferenceConstants.P_SHOW_ADD_COMPONENT_POPUP, false);
				store.setValue(PreferenceConstants.P_ADD_COMPONENT_NOW, add);
			}
		}
		if (add) {
			InsertComponent ic = new InsertComponent(getSite().getShell(), getTDIConfiguration(), true);
			ic.setLocation((BranchingConfig) component);
			ic.run();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void dispose() {
		final ArrayList<TDIConfigEditorInput> editors = (ArrayList<TDIConfigEditorInput>) openEditors.clone();
		getSite().getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				for (TDIConfigEditorInput input : editors) {
					try {
						IEditorPart ed = getSite().getPage().findEditor(input);
						if (ed != null)
							getSite().getPage().closeEditor(ed, false);
					} catch (Throwable t) {
						t.printStackTrace();
					}
				}
				// -- Restore the markers from file on disk if we don't save
				try {
					if (isDirty()) {
						new AssemblyLineValidator().validate(TDIConfigurationFile.load(getTDIConfigFile()));
					}
				} catch (Exception e) {
					SystemFunctions.doNothing();
				}
			}
		});
		super.dispose();
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
		if (getTDIConfiguration() == null) {
			return;
		}
		getEditorSite().setSelectionProvider(getSelectionProvider());
		settings = new CustomEditorSettings(getTDIConfiguration());
		settings.loadSettings();
	}

	/**
	 * Runs this assemblyline on the associated server
	 */
	public void runAssemblyLine() {
		runAssemblyLine(null);
	}

	public void runAssemblyLine(String runUntil) {
		runAssemblyLine(runUntil, runUntil != null);
	}

	public void runAssemblyLine(String runUntil, boolean debug) {
		runAssemblyLine(runUntil, debug, false);
	}

	public void runAssemblyLine(String runUntil, boolean debug, boolean interactive) {

		ServerView view = (ServerView) getSite().getPage().findView("com.ibm.tdi.rcp.serverview");
		if (view != null) {
			IFile server;
			try {
				server = (IFile) Utils.getTDIServer(Utils.getTDIServer(getTDIConfigFile()));
			} catch (Exception e) {
				MessageDialog.openError(getSite().getShell(), Messages.getString("general.run.label"), Messages.getMessage(
						"ALFC.no.server", null));
				return;
			}
			if (!view.isServerRunning(server)) {
				MessageDialog.openError(getSite().getShell(), Messages.getString("general.run.label"), Messages.getMessage(
						"ALFC.no.server.instance", server.getName()));
				return;
			}
		}

		int stepMode = settings.getInteger(CustomEditorSettings.STEP_MODE, 0);

		if (stepMode == RunAssemblyLineInput.RUNMODE_RECORD || stepMode == RunAssemblyLineInput.RUNMODE_PLAYBACK) {
			AssemblyLineConfig alc = (AssemblyLineConfig) getTDIConfiguration();
			if (alc.getSandboxConfig().getIdentifier() == null || alc.getSandboxConfig().getIdentifier().length() == 0) {
				MessageDialog.openError(getSite().getShell(), Messages.getString("general.error.label"), Messages.getMessage(
						"Sandbox.not.configured", null));
				return;
			}
		}

		// Save editor changes if any
		// closeEditor();

		//
		// Make sure project builder has updated runtime config file
		//
		try {
			getTDIConfigProject().build(IncrementalConfigBuilder.INCREMENTAL_BUILD, null);
			Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD, null);
		} catch (InterruptedException ie) {
			SystemFunctions.doNothing();
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e, getSite().getShell());
			return;
		}

		//
		// Check for errors
		//
		if (hasErrors()
				&& !MessageDialog.openQuestion(getSite().getShell(), Messages.getString("general.error.label"), Messages
						.getString("AssemblyLineEditor.has.errors"))) {
			try {
				getSite().getPage().showView("org.eclipse.ui.views.ProblemView");
			} catch (PartInitException e) {
				SystemFunctions.doNothing();
			}
			return;
		}

		RunAssemblyLineInput input = new RunAssemblyLineInput((AssemblyLineConfig) getTDIConfiguration());
		input.setStepMode(stepMode);
		input.setDebug(debug);
		input.setBreakPoint(runUntil);

		// Provide work entry?
		if (settings.getBoolean(CustomEditorSettings.WORK_ENABLED, false))
			input.setWorkEntry(settings.getEntry(CustomEditorSettings.WORK_ENTRY));
		
		// Provide init params?
		if (settings.getBoolean(CustomEditorSettings.INIT_PARAMS_ENABLED, false))
			input.setInitParams(settings.getEntry(CustomEditorSettings.INIT_PARAMS));

		// Operation?
		if (settings.getString(CustomEditorSettings.AL_OPERATION, null) != null)
			input.setOperation(settings.getString(CustomEditorSettings.AL_OPERATION, null));

		// Simulate?
		input.setSimulateMode(settings.getBoolean(CustomEditorSettings.AL_SIMULATE, false));

		//Regression
		if (settings.getBoolean(CustomEditorSettings.REGRESSION_ENABLED, false)) {
			String file = settings.getString(CustomEditorSettings.REGRESSION_FILE, null);
			if (file != null && file.length() > 0) {
				if (settings.getBoolean(CustomEditorSettings.REGRESSION_WRITE, false))
					input.setRegressionOutputName(file);
				else
					input.setRegressionInputName(file);
			}
		}

		// Debug/Interactive override
		if (debug)
			input.setDebugMode(0);
		if (interactive)
			input.setDebugMode(1);

		try {
			getEditorSite().getWorkbenchWindow().getActivePage().openEditor(input, RunAssemblyLineEditor.EDITOR_ID, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns true if there are error markers in this file.
	 * 
	 */
	private boolean hasErrors() {
		try {
			// Perform run through the validator
			getTDIConfigFile().deleteMarkers(Utils.TDI_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE); //$NON-NLS-1$
			new AssemblyLineValidator().validate(getTDIConfiguration());

			IMarker[] markers = getTDIConfigFile().findMarkers(Utils.TDI_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
			if (markers.length > 0) {
				int type = IMessage.WARNING;
				for (IMarker marker : markers) {
					if (marker.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING) == IMarker.SEVERITY_ERROR) {
						type = IMessage.ERROR;
					}
				}
				return type == IMessage.ERROR;
			}
		} catch (Exception e) {
			SystemFunctions.doNothing();
		}
		return false;
	}

	/**
	 * Configures drag/drop support for the AL content TreeViewer
	 */
	private void addDragDropSupport() {

		// -- Add Drag Support
		DragSourceAdapter dsl = new DragSourceAdapter() {
			private IStructuredSelection selection;

			@Override
			public void dragSetData(DragSourceEvent event) {
				if (LocalSelectionTransfer.getTransfer().isSupportedType(event.dataType)) {
					LocalSelectionTransfer.getTransfer().setSelection(selection);
					event.data = selection;
					event.doit = true;
				}
			}

			@Override
			public void dragStart(DragSourceEvent event) {
				selection = (IStructuredSelection) tree.getSelection();
				if (selection.isEmpty())
					selection = new StructuredSelection(selection.toArray());
				LocalSelectionTransfer.getTransfer().setSelection(selection);
				event.doit = true;
			}
		};
		tree.addDragSupport(DND.DROP_COPY | DND.DROP_MOVE, new Transfer[] { LocalSelectionTransfer.getTransfer() }, dsl);

		// -- Add Drop Support
		DropTargetListener dtl = new DropTargetAdapter() {
			private ContainerConfig ef = ((AssemblyLineConfig) getTDIConfiguration()).getEntryFeedComponents();
			private ContainerConfig df = ((AssemblyLineConfig) getTDIConfiguration()).getDataFlowComponents();

			public void dragEnter(DropTargetEvent event) {
				if (LocalSelectionTransfer.getTransfer().isSupportedType(event.currentDataType)) {
					event.detail = DND.DROP_COPY;
				}
			}

			private int getInsertionPoint(TreeItem item, int x, int y) {
				Rectangle rect = item.getBounds();
				BaseConfiguration b = (BaseConfiguration) item.getData();
				// Can only insert into EntryFeed or DataFlow, not before or
				// after
				if (b == ef || b == df)
					return TDI.INSERT_INTO;

				Point p = item.getDisplay().map(null, item.getParent(), new Point(x, y));
				int off = p.y - rect.y;

				if (b instanceof ContainerConfig && b.getParent() != ef) {
					// 3 choices, before, into or after
					int h = rect.height / 3;
					if (off <= h)
						return TDI.INSERT_BEFORE;
					else if (off <= (h * 2))
						return TDI.INSERT_INTO;
					else
						return TDI.INSERT_AFTER;
				} else if (b.getParent() == null) {
					// Probably a placeholder
					return TDI.INSERT_INTO;
				} else {
					// 2 choices, before or after
					int h = rect.height / 2;
					if (off <= h)
						return TDI.INSERT_BEFORE;
					else
						return TDI.INSERT_AFTER;
				}
			}

			public void dragOver(DropTargetEvent event) {
				if (LocalSelectionTransfer.getTransfer().isSupportedType(event.currentDataType)) {
					if (event.item == null) {
						event.feedback = DND.FEEDBACK_NONE;
					} else {
						switch (getInsertionPoint((TreeItem) event.item, event.x, event.y)) {
						case TDI.INSERT_BEFORE:
							event.feedback = DND.FEEDBACK_EXPAND | DND.FEEDBACK_INSERT_BEFORE;
							break;
						case TDI.INSERT_AFTER:
							event.feedback = DND.FEEDBACK_EXPAND | DND.FEEDBACK_INSERT_AFTER;
							break;
						case TDI.INSERT_INTO:
							event.feedback = DND.FEEDBACK_EXPAND | DND.FEEDBACK_SELECT;
							break;
						}

						Object target = ((TreeItem) event.item).getData();
						if ((event.feedback & DND.FEEDBACK_SELECT) > 0 && !(target instanceof ContainerConfig))
							event.feedback = DND.FEEDBACK_NONE;
						else if (target instanceof HookConfig)
							event.feedback = DND.FEEDBACK_NONE; // Do not allow dropping into Hooks

						event.detail = DND.DROP_COPY;
					}
				}
			}

			public void drop(DropTargetEvent event) {
				if (LocalSelectionTransfer.getTransfer().isSupportedType(event.currentDataType)) {
					BaseConfiguration target = null;
					int pos = -1;
					if (event.item instanceof TreeItem) {
						target = (BaseConfiguration) ((TreeItem) event.item).getData();
						pos = getInsertionPoint((TreeItem) event.item, event.x, event.y);
					}

					IStructuredSelection sel = (IStructuredSelection) event.data;

					// To help avoid moving anything out of their container when dragging multiple items,
					// we keep a list of moved configurations.
					List<BaseConfiguration> moved = new ArrayList<BaseConfiguration>();

					for (Iterator<?> i = sel.iterator(); i.hasNext();) {
						Object nextDrop = i.next();
						if (nextDrop instanceof IFile) {
							if (target instanceof HookConfig) {
								InheritanceUtil.changeInheritance(target, nextDrop);
								return;
							}							
							if (target != null) {
								BaseConfiguration parent = target.getParent();
								switch (pos) {
								case TDI.INSERT_BEFORE:
								case TDI.INSERT_AFTER:
									if (parent instanceof ContainerConfig) {
										int index = ((ContainerConfig) parent).indexOf(target);
										if (pos == TDI.INSERT_AFTER) {
											index++;
										}
										target = parent;
										pos = index;
									}
									break;
								case TDI.INSERT_INTO:
									// Check for placeholder
									if (parent == null && target.getParameter("%%PLACEHOLDER%%") instanceof BaseConfiguration)
										target = (BaseConfiguration) target.getParameter("%%PLACEHOLDER%%"); //$NON-NLS-1$

									pos = -1;
								}
							} else {
								pos = -1;
							}

							while (target != null && !(target instanceof ContainerConfig))
								target = target.getParent();

							insertObject(nextDrop, (ContainerConfig) target, pos);

						} else if (nextDrop instanceof BaseConfiguration) {
							BaseConfiguration obj = (BaseConfiguration) nextDrop;

							if (moved.contains(obj))
								continue; // Already movded

							boolean externalDrop = 
								MetamergeConfigFactory.SYSTEM_NAMESPACE.equals(MetamergeConfigFactory.getNamespaceFor(obj)) || obj.getMetamergeConfig() == null;
							
							if (obj instanceof AttributeMapItem) {
								// Allow Attribute maps to be dropped onto
								// connectors
								if (target instanceof ConnectorConfig) {
									workMapWidget.addAttributeMapItem(((ConnectorConfig) target).getAttributeMap(),
											(AttributeMapItem) obj);
								}
								continue;
							}
							// We do not how to move anything not in a
							// Container\
							// This may change later
							if (!externalDrop && !(obj.getParent() instanceof ContainerConfig)) {
								continue;
							}

							// No point in moving onto self
							if (isParent(obj, target))
								return;

							// Connectors cannot be inside Connectors, so we
							// place them after
							if (pos == TDI.INSERT_INTO && !(target instanceof ContainerConfig)
									&& (obj instanceof ConnectorConfig || obj instanceof BranchingConfig || obj instanceof ScriptConfig)) {
								while (target != null && !(target.getParent() instanceof ContainerConfig)) {
									target = target.getParent();
									pos = TDI.INSERT_AFTER;
								}
							}

							// Only Connectors in Iterator/Server mode into
							// Entry Feed
							if ((target == ef || (target != null && target.getParent() == ef))
									&& !(obj instanceof ConnectorConfig && Utils.isEntryFeedConnector((ConnectorConfig) obj)))
								return;

							// Server mode connectors can only be in Entry Feed
							if (obj instanceof ConnectorConfig && 
								ConnectorConfig.SERVER_MODE.equals(((ConnectorConfig)obj).getMode()) &&
								target != ef && target != null && target.getParent() != ef) {
								return;
							}
							
							/* Code added by L3 */
							if (pos == TDI.INSERT_INTO) {
								// Only case should be dropped into Switch
								if (ConfigUtils.isSwitch(target) && !ConfigUtils.isCase(obj))
									pos = TDI.INSERT_AFTER;
							} else if (ConfigUtils.isCase(target) && !ConfigUtils.isCase(obj)) {
								// Before or after case, can only drop a case.
								// This is some other component, drop it after
								// the switch instead
								// Other strategies might be considered, like
								// dropping it into the case.
								target = target.getParent();
								pos = TDI.INSERT_AFTER;
							}
							
							if(externalDrop) {
								// Drop from system library
								try {
									if(target == null)
										target = df;
									while(!(target instanceof ContainerConfig) && target.getParent() != null)
										target = target.getParent();
									if(target instanceof ContainerConfig)
										insertComponent(ConfigUtils.createInheritedComponent(getTDIConfiguration().getMetamergeConfig(), obj), (ContainerConfig) target, pos);
								} catch (Exception e) {
									EclipseAppender.logerror(e.toString(), e, getSite().getShell());
								}
								
							} else {
								MoveConfigOperation operation = new MoveConfigOperation(obj, target, pos);
								IUndoContext undoContext = getSite().getWorkbenchWindow().getWorkbench().getOperationSupport()
										.getUndoContext();
								operation.addContext(undoContext);
								try {
									OperationHistoryFactory.getOperationHistory().execute(operation, null, null);
								} catch (ExecutionException e) {
									e.printStackTrace();
								}
								if (pos == TDI.INSERT_AFTER)
									target = obj;
								if (obj instanceof ContainerConfig)
									((ContainerConfig)obj).getConfigurations(moved);
							}
						}
					}
				}
			}
		};
		tree.addDropSupport(DND.DROP_COPY, new Transfer[] { LocalSelectionTransfer.getTransfer() }, dtl);
	}

	private boolean isParent(BaseConfiguration a, BaseConfiguration b) {
		while (b != null) {
			if (a == b)
				return true;
			b = b.getParent();
		}
		return false;
	}

	public void showALSettingsDialog() {
		new ALSettingsAction(getSite().getPart(), (AssemblyLineConfig) getTDIConfiguration()).run(null);
	}

	public void showLogSettingsDialog() {
		new ConfigureLoggingAction(getSite().getPart(), getTDIConfiguration()).run(null);
	}

	protected Menu createTreeOptionsMenu(Control parent) {
		Menu menu = new Menu(parent.getShell(), SWT.POP_UP);

		MenuItem item = new MenuItem(menu, SWT.CHECK);
		item.setSelection(!alcp.isHooksIncluded());
		item.setText(Messages.getString("AssemblyLineEditor3.hide.enabled.hooks"));
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				alcp.setHooksIncluded(!alcp.isHooksIncluded());
				tree.refresh();
			}
		});

		item = new MenuItem(menu, SWT.CHECK);
		item.setText(Messages.getString("AssemblyLineEditor3.hide.component.attributemaps"));
		item.setSelection(!alcp.isAttributeMapsShown());
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				alcp.setAttributeMapsShown(!alcp.isAttributeMapsShown());
				tree.refresh();
			}
		});

		item = new MenuItem(menu, SWT.CHECK);
		item.setText(Messages.getString("AssemblyLineEditor3.hide.inactive.alhooks"));
		item.setSelection(!alcp.isAlHooksIncluded());
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				alcp.setAlHooksIncluded(!alcp.isAlHooksIncluded());
				tree.refresh();
			}
		});

		return menu;
	}

	protected Menu createOptionsMenu(Control parent) {
		Menu menu = new Menu(parent.getShell(), SWT.POP_UP);

		MenuItem item = new MenuItem(menu, SWT.PUSH);
		item.setText(Messages.getString("assemblyline.tabs.settings.tooltip")); //$NON-NLS-1$
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				showALSettingsDialog();
			}
		});

		item = new MenuItem(menu, SWT.PUSH);
		item.setText(Messages.getString("assemblyline.tabs.logging.tooltip")); //$NON-NLS-1$
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				showLogSettingsDialog();
			}
		});

		// AL Hooks
		item = new MenuItem(menu, SWT.PUSH);
		item.setText(Messages.getString("assemblyline.tabs.hooks.tooltip")); //$NON-NLS-1$
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				new EnableHooksAction(getSite().getPart(), getTDIConfiguration()).run(null);
			}
		});

		// AL Operations
		item = new MenuItem(menu, SWT.PUSH);
		item.setText(Messages.getString("assemblyline.tabs.callreturn.label")); //$NON-NLS-1$
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				AssemblyLineConfig alc = (AssemblyLineConfig) getTDIConfiguration();
				TDIConfigEditorInput editorInput = new TDIConfigEditorInput(alc);
				IEditorPart ed = getSite().getWorkbenchWindow().getActivePage().findEditor(editorInput);
				try {
					getSite().getWorkbenchWindow().getActivePage().openEditor(editorInput, ConnectorEditor.ID, true);
					if (ed == null)
						openEditors.add(editorInput);
				} catch (PartInitException e1) {
					EclipseAppender.logerror(e1.toString(), e1, getSite().getShell());
				}
			}
		});

		// AL Simulation Settings
		item = new MenuItem(menu, SWT.PUSH);
		item.setText(Messages.getString("ALSimulationWidget.title")); //$NON-NLS-1$
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				new ALSimulationAction(getSite().getPart(), (AssemblyLineConfig) getTDIConfiguration()).run(null);
			}
		});

		// AL Sandbox
		item = new MenuItem(menu, SWT.PUSH);
		item.setText(Messages.getString("assemblyline.tabs.sandbox.label")); //$NON-NLS-1$
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				new ALSandboxAction(getSite().getPart(), (AssemblyLineConfig) getTDIConfiguration()).run(null);
			}
		});

		// Run options
		item = new MenuItem(menu, SWT.PUSH);
		item.setText(Messages.getString("RunOptionsDialog.1"));
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				RunOptionsDialog dlg = new RunOptionsDialog(getSite().getShell(), getTDIConfiguration(), AssemblyLineEditor3.this,
						settings);
				if (dlg.open() == Window.OK) {
					settings.saveSettings();
				}
			}
		});

		// AL Pool options
		item = new MenuItem(menu, SWT.PUSH);
		item.setText(Messages.getString("ALEditor3.Define.ALPool.Options"));
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				try {
					GenericFormDialog dlg = new GenericFormDialog(getSite().getShell(),
							"ALPoolSettings", 
							((AssemblyLineConfig) getTDIConfiguration()).getThreadOptions());
					dlg.setNoCancel();
					dlg.open();
				} catch (Exception err) {
					EclipseAppender.logerror(err.toString(), err, getSite().getShell());				
				}
			}
		});

		return menu;
	}

	protected void openWizard(String initialPage) {
		ALSettingsWizard wiz = new ALSettingsWizard((AssemblyLineConfig) getTDIConfiguration(), initialPage);
		WizardDialog dlg = new WizardDialog(getSite().getShell(), wiz);
		dlg.setPageSize(600, 400);
		dlg.open();
	}

	@Override
	public void setFocus() {
		super.setFocus();
		if (tree != null)
			tree.getTree().setFocus();
	}

	/**
	 * Insert a connector
	 * 
	 * @param cc
	 */
	private void insertConnector(ConnectorConfig cc, ContainerConfig targetContainer, int position) {
		AssemblyLineConfig alc = (AssemblyLineConfig) getTDIConfiguration();
		ContainerConfig target;

		if (ConnectorConfig.SERVER_MODE.equals(cc.getMode()))
			target = alc.getEntryFeedComponents();
		else if (targetContainer != null)
			target = targetContainer;
		else if (Utils.isEntryFeedMode(cc.getMode()))
			target = alc.getEntryFeedComponents();
		else if (getSelectedContainer() != null)
			target = getSelectedContainer();
		else
			target = alc.getDataFlowComponents();

		boolean isfeed = target == alc.getEntryFeedComponents();

		// retarget to data flow
		if (isfeed && !Utils.isEntryFeedMode(cc.getMode())) {
			insertComponent(cc, alc.getDataFlowComponents(), -1);
		} else {
			if (target != targetContainer)
				position = -1;
			insertComponent(cc, target, position);
		}
	}

	/**
	 * Clones and inserts bc into the assemblyline after generating a unique
	 * name for it.
	 * 
	 * @param bc
	 * @param target
	 * @param position
	 */
	private void insertComponent(BaseConfiguration bc, ContainerConfig target, int position) {
		if (bc == target)
			return;

		AssemblyLineConfig alc = (AssemblyLineConfig) getTDIConfiguration();
		if (target == alc.getEntryFeedComponents() && !(bc instanceof ConnectorConfig))
			target = alc.getDataFlowComponents();

		try {
			if (bc.getMetamergeConfig() == null)
				bc.setMetamergeConfig(alc.getMetamergeConfig());
			BaseConfiguration c = (BaseConfiguration) bc.getClone();
			checkName(c, alc, null);

			InsertConfigOperation operation = new InsertConfigOperation(c, target, position);
			IUndoContext undoContext = getSite().getWorkbenchWindow().getWorkbench().getOperationSupport().getUndoContext();
			operation.addContext(undoContext);
			try {
				OperationHistoryFactory.getOperationHistory().execute(operation, null, null);
			} catch (ExecutionException e) {
				EclipseAppender.logerror(e.toString(), e, getSite().getShell());
			}

		} catch (Exception e1) {
			EclipseAppender.logerror(e1.toString(), e1, getSite().getShell());
		}
	}

	/**
	 * Returns the selected ContainerConfig or null if current selection is
	 * multiple or not a ContainerConfig
	 * 
	 * @return
	 */
	protected ContainerConfig getSelectedContainer() {
		IStructuredSelection sel = (IStructuredSelection) tree.getSelection();
		if (sel.isEmpty() || sel.toList().size() != 1)
			return null;
		else if (sel.getFirstElement() instanceof ContainerConfig)
			return (ContainerConfig) sel.getFirstElement();
		else
			return null;
	}

	/**
	 * Inserts an object into the assemblyline.
	 * 
	 * @param obj
	 *            Connector, Script (or String) or File
	 */
	private void insertObject(Object obj, ContainerConfig target, int position) {
		ContainerConfig defaultTarget = target;
		if (defaultTarget == null)
			defaultTarget = ((AssemblyLineConfig) getTDIConfiguration()).getDataFlowComponents();
		else if (defaultTarget.getParent() == ((AssemblyLineConfig) getTDIConfiguration()).getEntryFeedComponents())
			defaultTarget = null;

		if (obj instanceof IFile) {
			try {
				TDIConfigurationFile cfg = TDIConfigurationFile.loadFile((IFile) obj);
				BaseConfiguration bc = (BaseConfiguration) cfg.getDefaultConfigObject();
				obj = ConfigUtils.createInheritedComponent(getTDIConfiguration().getMetamergeConfig(), bc);
			} catch (Exception e) {
				EclipseAppender.logerror(e.toString(), e, getSite().getShell());
				return;
			}
		}
		/* newly added by L3 */
		if (ConfigUtils.isSwitch(defaultTarget) && !ConfigUtils.isCase(obj) && defaultTarget.getParent() instanceof ContainerConfig) {
			position = ((ContainerConfig) defaultTarget.getParent()).indexOf(defaultTarget) + 1;
			defaultTarget = (ContainerConfig) defaultTarget.getParent();
		}
		if (obj instanceof ConnectorConfig) {
			insertConnector((ConnectorConfig) obj, defaultTarget, position);

		} else if (obj instanceof ScriptConfig || obj instanceof BranchingConfig) {
			insertComponent((BaseConfiguration) obj, defaultTarget, position);

		} else if (obj instanceof String) {
			try {
				ScriptConfig scriptConfig = (ScriptConfig) ((TDIConfigurationFile) getTDIConfiguration().getMetamergeConfig())
						.newInstanceOf(MetamergeConfig.SCRIPT_FOLDER);
				scriptConfig.setName("Script"); //$NON-NLS-1$
				scriptConfig.setScript((String) obj);
				scriptConfig.setParent(null);
				scriptConfig.setMetamergeConfig(null);
				insertComponent(scriptConfig, defaultTarget, position);
				obj = scriptConfig;
			} catch (Exception e) {
				EclipseAppender.logerror(e.toString(), e, getSite().getShell());
				return;
			}
		}
		tree.setSelection(new StructuredSelection(obj), true);
		tree.setExpandedState(obj, true);

	}

	private void checkName(BaseConfiguration c, AssemblyLineConfig alc, List<String> names) {
		if (names == null)
			names = new ArrayList<String>();
		int counter = 1;
		String name = c.getShortName();
		while (alc.getComponent(name) != null || names.indexOf(name) != -1) {
			name = c.getShortName() + "_" + counter++; //$NON-NLS-1$
		}
		try {
			c.setName(name);
			names.add(name);
		} catch (Exception e) {
			EclipseAppender.logerror(e.getMessage(), e);
		}
		if (c instanceof ContainerConfig) {
			for (int i = 0; i < c.size(); i++)
				checkName(((ContainerConfig) c).getConfig(i), alc, names);
		}
	}

	private class PasteAction extends PasteConfigAction {

		public PasteAction() {
			super(Messages.getString("common.Paste.name")); //$NON-NLS-1$
		}

		@Override
		protected void performPaste(IStructuredSelection selection) {
			// Keep a list of names of items already pasted.
			// AS the items are cloned, we cannot keep the items themselves.
			List<String> alreadyPasted = new ArrayList<String>();
			ContainerConfig target = getSelectedContainer(); // Save this away, it may change as we paste.
			for (Object obj : selection.toList()) {
				if (obj instanceof BaseConfiguration && alreadyPasted.contains(((BaseConfiguration) obj).getShortName()))
					continue;
				insertObject(obj, target, -1);
				if (obj instanceof ContainerConfig)
					alreadyPasted.addAll(((ContainerConfig)obj).getChildNames());
			}
		}

		@Override
		protected boolean validatePaste(Object obj) {
			return (obj instanceof ConnectorConfig || obj instanceof ScriptConfig || obj instanceof BranchingConfig
					|| obj instanceof String || obj instanceof IFile);
		}

	}

	@Override
	protected void quickEdit(BaseConfiguration bc) {
		if (!addWidgetToEditorStack(bc))
			addWidgetToEditorStack(null);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter.equals(IGotoMarker.class)) {
			return this;
		}
		return super.getAdapter(adapter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.tdi.eclipse.editors.BaseEditor#gotoMarker(org.eclipse.core.resources
	 * .IMarker)
	 */
	public void gotoMarker(IMarker marker) {
		try {
			String location = (String) marker.getAttribute(IMarker.LOCATION);
			BaseConfiguration loc = getTDIConfiguration().getChildForPath(location);
			if (loc == null)
				return;

			String problem = (String) marker.getAttribute(IMarker.PROBLEM);

			// Try to make the loc object current selection - traverse tree
			// upwards until we find correct component
			tree.setSelection(new StructuredSelection(loc));
			BaseConfiguration parent = loc.getParent();
			while (tree.getSelection().isEmpty() && parent != null) {
				tree.setSelection(new StructuredSelection(parent), true);
				parent = parent.getParent();
			}

			// With schema problems we force the component to be opened
			// Otherwise we can use the quick editor
			if (ComponentValidator.SCHEMA_NOT_DEFINED.equals(problem)) {
				ConnectorConfig cc = (ConnectorConfig) Utils.getParentConfig(loc, ConnectorConfig.class);
				if (cc != null) {
					// Make the connector object current selection
					tree.setSelection(new StructuredSelection(cc), true);
					IEditorPart part = editItem(new StructuredSelection(cc));
					if (part instanceof BaseEditor) {
						((BaseEditor) part).gotoMarker(marker);
						return;
					}
				}
			}

			// -- No need to open an orphaned branch, just highlight it
			if (AssemblyLineValidator.ORPHANED_BRANCH.equals(problem)) {
				return;
			}

			// If target item is already open do nothing
			if (isEditorVisible() && getEditor() != null && getEditor().getEditingConfig() == loc)
				return;

			IEditorPart part = editItem((IStructuredSelection) tree.getSelection());
			if (part instanceof BaseEditor) {
				((BaseEditor) part).gotoMarker(marker);
			}

			if (!tree.getSelection().isEmpty()) {
				Object obj = ((IStructuredSelection) tree.getSelection()).getFirstElement();
				if (obj instanceof IGotoMarker)
					((IGotoMarker) obj).gotoMarker(marker);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void configurationChanged(MetamergeConfigChange changeEvent) {
		super.configurationChanged(changeEvent);
		if (changeEvent.getSource() instanceof AssemblyLineConfig && BaseConfigurationImpl.NAME.equals(changeEvent.getKey()))
			header.setText(getTDIConfiguration().getShortName());

		if (changeEvent.getSource() instanceof ConnectorConfig && InternalSchema.CONNECTOR_MODE.equals(changeEvent.getKey())) {
			ConnectorConfig cc = (ConnectorConfig) changeEvent.getSource();
			
			// While the user is creating a new Connetor, we should not move it.
			// Unfortunately, we will get an event if the mode is changed, but just ignore it.
			if (cc.getParent() instanceof ContainerConfig) {
				ContainerConfig container = (ContainerConfig) cc.getParent();
				if (container.indexOf(cc) == -1)
					return;
			}
			
			verifyEntryFeed();
			checkServerMode(cc);
		}
	}

	// If cc is a Server mode Connector, move it to EntryFeed
	private void checkServerMode(ConnectorConfig cc) {
		if (! ConnectorConfig.SERVER_MODE.equals(cc.getMode()))
			return;

		AssemblyLineConfig alc = (AssemblyLineConfig) getTDIConfiguration();
		if (alc == null)
			return;

		ContainerConfig ef = alc.getEntryFeedComponents();
		if (cc.getParent() == ef)
			return;
		
		EclipseAppender.loginfo(Messages.getMessage("AssemblyLineEditor.auto.moving", cc.getShortName()));
		MoveConfigOperation operation = new MoveConfigOperation(cc, ef, TDI.INSERT_INTO);
		IUndoContext undoContext = getSite().getWorkbenchWindow().getWorkbench().getOperationSupport().getUndoContext();
		operation.addContext(undoContext);
		try {
			OperationHistoryFactory.getOperationHistory().execute(operation, null, null);
		} catch (ExecutionException e) {
			SystemFunctions.doNothing();
		}
	}

	public void verifyEntryFeed() {
		AssemblyLineConfig alc = (AssemblyLineConfig) getTDIConfiguration();
		if (alc == null)
			return;
		BaseConfiguration firstDataComp = alc.getDataFlowComponents().getConfig(0);
		for (BaseConfiguration bc : alc.getEntryFeedComponents().getConfigurations(null)) {
			if (bc instanceof ConnectorConfig && Utils.isEntryFeedConnector((ConnectorConfig) bc))
				continue;
			EclipseAppender.loginfo(Messages.getMessage("AssemblyLineEditor.auto.moving", bc.getShortName()));
			MoveConfigOperation operation;
			if (firstDataComp != null)
				operation = new MoveConfigOperation(bc, firstDataComp, TDI.INSERT_BEFORE);
			else
				operation = new MoveConfigOperation(bc, alc.getDataFlowComponents(), TDI.INSERT_INTO);
			IUndoContext undoContext = getSite().getWorkbenchWindow().getWorkbench().getOperationSupport().getUndoContext();
			operation.addContext(undoContext);
			try {
				OperationHistoryFactory.getOperationHistory().execute(operation, null, null);
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
	}

	public void disposeWidget(BaseWidget widget) {
		tree.setSelection(StructuredSelection.EMPTY);
		addWidgetToEditorStack(null);
		widget.dispose();
	}


	private void doRename(IStructuredSelection selection) {
		if (selection == null || selection.size() != 1)
			return;
		RenameConfigAction ra = new RenameConfigAction();
		IAction action = new Action() {};
		ra.selectionChanged(action, selection);
		if (action.isEnabled())
			ra.run(action);
	}

	private static class DynamicActionItem extends ActionContributionItem {
		public DynamicActionItem(IAction action) {
			super(action);
		}

		@Override
		public boolean isDynamic() {
			return true;
		}			
	}
}
