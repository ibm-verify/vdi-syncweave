/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.editors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
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
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.forms.IMessage;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.ide.IGotoMarker;

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
import com.ibm.di.config.interfaces.LoopConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
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
import com.ibm.tdi.eclipse.actions.operations.InsertConfigOperation;
import com.ibm.tdi.eclipse.actions.operations.MoveConfigOperation;
import com.ibm.tdi.eclipse.builders.AssemblyLineValidator;
import com.ibm.tdi.eclipse.builders.ComponentValidator;
import com.ibm.tdi.eclipse.builders.IncrementalConfigBuilder;
import com.ibm.tdi.eclipse.commands.CommandHandlerProxy;
import com.ibm.tdi.eclipse.commands.CommandID;
import com.ibm.tdi.eclipse.dialogs.RunOptionsDialog;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.preferences.PreferenceConstants;
import com.ibm.tdi.eclipse.providers.AssemblyLineContentProvider;
import com.ibm.tdi.eclipse.providers.ConfigLabelProvider;
import com.ibm.tdi.eclipse.util.CustomEditorSettings;
import com.ibm.tdi.eclipse.views.ServerView;
import com.ibm.tdi.eclipse.widget.AttributeMapWidget;
import com.ibm.tdi.eclipse.widget.BranchWidget;
import com.ibm.tdi.eclipse.widget.LoopConfigWidget;
import com.ibm.tdi.eclipse.widget.WorkMapWidget;
import com.ibm.tdi.eclipse.wizards.ALSettingsWizard;

public class AssemblyLineEditor2 extends BaseEditor {
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private TreeViewer tree;
	private FormToolkit tk;
	private Form treeForm;
	private Button hooksCheck;
	private Button attributesCheck;
	private AssemblyLineContentProvider alcp;
	private ConfigLabelProvider clp;
	private CustomEditorSettings settings;
	private String[] runLabels = new String[] { Messages.getString("general.runnobreak.label"), //$NON-NLS-1$
			Messages.getString("assemblyline.runmode.step"), Messages.getString("assemblyline.runmode.step.on.error"), //$NON-NLS-1$ //$NON-NLS-2$
			Messages.getString("RunOptionsWidget.record"), Messages.getString("RunOptionsWidget.playback"), //$NON-NLS-1$ //$NON-NLS-2$
			Messages.getString("AssemblyLineEditor2.5") }; //$NON-NLS-1$ //$NON-NLS-2$
	private WorkMapWidget workMapWidget;
	private StackLayout editorStack;
	private Composite qeditors;
	private Action insertAction;
	private ArrayList<TDIConfigEditorInput> openEditors = new ArrayList<TDIConfigEditorInput>();

	public AssemblyLineEditor2() {
		super();

		//
		// -- global action handlers
		//
		registerAction(ActionFactory.CUT.getId(), new CutConfigAction("Cut", null)); //$NON-NLS-1$
		registerAction(ActionFactory.COPY.getId(), new CopyConfigAction("Copy")); //$NON-NLS-1$
		registerAction(ActionFactory.DELETE.getId(), new CutConfigAction("Delete", null)); //$NON-NLS-1$
		registerAction(ActionFactory.PASTE.getId(), new PasteAction());
	}

	@Override
	public void createPartControl(Composite parent) {
		if (getTDIConfiguration() == null) {
			super.createPartControl(parent);
			return;
		}

		SashForm sash = new SashForm(parent, SWT.VERTICAL);

		// -- The main al views
		createMainPanel(sash);

		// -- Create a composite to hold the quick editors
		qeditors = new Composite(sash, SWT.NONE);
		editorStack = new StackLayout();
		qeditors.setLayout(editorStack);

		// -- Create the script editor
		editorStack.topControl = createScriptEditor(qeditors);

		sash.setWeights(new int[] { 100, 0 });
		verifyEntryFeed();
	}

	public void createMainPanel(Composite parent) {
		SashForm sash = new SashForm(parent, SWT.HORIZONTAL);
		tk = new FormToolkit(parent.getDisplay());

		treeForm = tk.createForm(sash);
		treeForm.getBody().setLayout(new FillLayout());
		treeForm.setText(Messages.getString("AssemblyLineEditor2.9")); //$NON-NLS-1$
		tk.decorateFormHeading(treeForm);

		// -- Create the tools in the form toolbar
		createAssemblyLineActions();

		// -- Create the head client
		treeForm.setHeadClient(createClientComponent(treeForm.getHead()));

		//
		// -- Tree Viewer
		//
		tree = new TreeViewer(treeForm.getBody(), SWT.MULTI | SWT.FULL_SELECTION);

		alcp = new AssemblyLineContentProvider(true);
		alcp.setFeedFlowShown(true); // settings.getBoolean(CustomEditorSettings.AL_FEED_FLOW,
		// false));
		alcp.setHooksIncluded(settings.getBoolean(CustomEditorSettings.AL_SHOW_HOOKS, false));
		alcp.setSchemaShown(settings.getBoolean(CustomEditorSettings.AL_SHOW_ATTRS, false));
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
		// Double click opens separate editor window
		//
		tree.addOpenListener(new IOpenListener() {
			public void open(OpenEvent event) {
				editItem(((IStructuredSelection) event.getSelection()));
			}
		});

		//
		// INSERT key opens insert wizard
		//
		tree.getTree().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.INSERT && insertAction.isEnabled())
					insertAction.run();
			}
		});

		addDragDropSupport();

		// -- Create menu for contributions
		super.registerContextMenu(tree);

		super.addSelectionProvider(tree);

		workMapWidget = new WorkMapWidget(sash, SWT.NONE, getTDIConfiguration());
		workMapWidget.setEditor(this);

		sash.setWeights(new int[] { 40, 60 });
	}

	/**
	 * Activate the default editor for an item
	 * 
	 * @param selection The Selection
	 */
	public IEditorPart editItem(IStructuredSelection selection) {
		IEditorPart editorPart = null;

		if (!selection.isEmpty()) {
			Object obj = selection.getFirstElement();
			LoopConfig loopConfig = null;
			if (obj instanceof LoopConfig && ((LoopConfig) obj).getLoopType() == LoopConfig.LOOP_CONNECTOR_FC) {
				loopConfig = (LoopConfig) obj;
			}
			if (obj instanceof ConnectorConfig || loopConfig != null) {
				try {
					TDIConfigEditorInput editorInput = new TDIConfigEditorInput((BaseConfiguration) obj);
					ConnectorConfig cc = (ConnectorConfig) (loopConfig != null ? loopConfig.getLoopConnector() : obj);
					String editorID = null;
					if (ConnectorConfig.SCRIPT_MODE.equals(cc.getMode()))
						editorID = ScriptEditor.ID;
					else if (obj instanceof ALMappingConfig)
						editorID = AttributeMapEditor.ID;
					else
						editorID = ConnectorEditor.ID;
					IWorkbenchPage page = getSite().getWorkbenchWindow().getActivePage();
					editorPart = page.openEditor(editorInput, editorID, true, IWorkbenchPage.MATCH_INPUT | IWorkbenchPage.MATCH_ID);
					openEditors.add(editorInput);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return editorPart;
			}

			if (obj instanceof ScriptConfig) {
				try {
					TDIConfigEditorInput editorInput = new TDIConfigEditorInput((ScriptConfig) obj);
					editorPart = getSite().getWorkbenchWindow().getActivePage().openEditor(editorInput, ScriptEditor.ID, true);
					openEditors.add(editorInput);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return editorPart;
			}

			// Ignore Feed/Flow
			AssemblyLineConfig alc = (AssemblyLineConfig) getTDIConfiguration();
			if (alc.getEntryFeedComponents() == obj || alc.getDataFlowComponents() == obj)
				return editorPart;

			// -- Placeholders for empty branches
			if (obj instanceof BaseConfiguration) {
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
			if (!isEditorVisible())
				return editorPart;

			if (obj instanceof AttributeMapConfig) {
				AttributeMapWidget amw = new AttributeMapWidget(qeditors, SWT.NONE, (ConnectorConfig) Utils.getParentConfig(obj,
						ConnectorConfig.class), this);
				editorStack.topControl = amw;
			} else if (obj instanceof LoopConfig) {
				LoopConfigWidget b = new LoopConfigWidget(qeditors, SWT.NONE, (LoopConfig) obj);
				b.setEditor(this);
				editorStack.topControl = b;
			} else if (obj instanceof BranchingConfig) {
				BranchWidget b = new BranchWidget(qeditors, SWT.NONE, (BaseConfiguration) obj);
				b.setEditor(this);
				editorStack.topControl = b;
			} else {
				editorStack.topControl = getQuickEditorForm();
			}
			qeditors.layout();
		}

		return editorPart;
	}

	private Composite createClientComponent(Composite parent) {
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayout(new GridLayout(9, false));

		hooksCheck = new Button(c, SWT.CHECK);
		hooksCheck.setText(Messages.getString("AssemblyLineEditor2.11")); //$NON-NLS-1$
		hooksCheck.setToolTipText(Messages.getString("AssemblyLineEditor2.12")); //$NON-NLS-1$
		hooksCheck.setSelection(settings.getBoolean(CustomEditorSettings.AL_SHOW_HOOKS, false));
		hooksCheck.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				boolean showHooks = hooksCheck.getSelection();
				alcp.setHooksIncluded(showHooks);
				clp.setIncludeHooks(!showHooks);
				settings.setProperty(CustomEditorSettings.AL_SHOW_HOOKS, String.valueOf(showHooks), true);
				tree.refresh();
			}
		});

		attributesCheck = new Button(c, SWT.CHECK);
		attributesCheck.setSelection(settings.getBoolean(CustomEditorSettings.AL_SHOW_ATTRS, false));
		attributesCheck.setText(Messages.getString("AssemblyLineEditor2.2")); //$NON-NLS-1$
		attributesCheck.setToolTipText(Messages.getString("AssemblyLineEditor2.1")); //$NON-NLS-1$
		attributesCheck.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				alcp.setSchemaShown(attributesCheck.getSelection());
				settings.setProperty(CustomEditorSettings.AL_SHOW_ATTRS, "" + attributesCheck.getSelection(), true); //$NON-NLS-1$
				tree.refresh();
			}
		});

		// feedFlowCheck = new Button(c, SWT.CHECK);
		// feedFlowCheck.setSelection(settings.getBoolean(CustomEditorSettings.AL_FEED_FLOW,
		// false));
		// feedFlowCheck.setText(Messages.getString("AssemblyLineEditor2.3"));
		// //$NON-NLS-1$
		// feedFlowCheck.setToolTipText(Messages.getString("AssemblyLineEditor2.4"));
		// //$NON-NLS-1$
		// feedFlowCheck.addSelectionListener(new SelectionAdapter() {
		// public void widgetSelected(SelectionEvent e) {
		// alcp.setFeedFlowShown(feedFlowCheck.getSelection());
		// settings.setProperty(CustomEditorSettings.AL_FEED_FLOW, "" +
		// feedFlowCheck.getSelection(), true); //$NON-NLS-1$
		// tree.refresh();
		// }
		// });

		return c;
	}

	private void createAssemblyLineActions() {

		treeForm.getToolBarManager().add(new Action() {
			public ImageDescriptor getImageDescriptor() {
				return Activator.getImageDescriptor("icons/PropButton0.gif"); //$NON-NLS-1$
			}

			public IMenuCreator getMenuCreator() {
				return new IMenuCreator() {
					private Menu popup;

					public void dispose() {
						if (popup != null)
							popup.dispose();
					}

					public Menu getMenu(Control parent) {
						if (popup == null)
							popup = createOptionsMenu(parent);
						return popup;
					}

					public Menu getMenu(Menu parent) {
						return null;
					}
				};
			}

			public int getStyle() {
				return AS_DROP_DOWN_MENU;
			}

			public String getToolTipText() {
				return Messages.getString("assemblyline.tabs.settings.tooltip"); //$NON-NLS-1$
			}

			public void run() {
				// openWizard(null);
			}
		});

		insertAction = new Action() {
			public void run() {
				InsertComponent ic = new InsertComponent(getSite().getShell(), getTDIConfiguration(), true /* feedFlowCheck.getSelection() */);
				ContainerConfig loc = ((AssemblyLineConfig)getTDIConfiguration()).getDataFlowComponents();
				if(loc != null)
					ic.setLocation(loc);
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
		new CommandHandlerProxy(getEditorSite(), insertAction, CommandID.AL_EDITOR_ADD_COMPONENT);
		treeForm.getToolBarManager().add(insertAction);

		Action runAction = new Action() {
			private Menu popup;

			public ImageDescriptor getImageDescriptor() {
				return Activator.getImageDescriptor("icons/Run.gif"); //$NON-NLS-1$
			}

			@Override
			public String getToolTipText() {
				int defMode = Activator.getDefault().getPreferenceStore().getInt(PreferenceConstants.P_DEFAULT_RUN_MODE);
				return runLabels[settings.getInteger(CustomEditorSettings.STEP_MODE, defMode)];
			}

			public IMenuCreator getMenuCreator() {
				return new IMenuCreator() {

					public void dispose() {
						if (popup != null)
							popup.dispose();
					}

					public Menu getMenu(Control parent) {
						if (popup == null)
							popup = createRunMenu(parent);
						return popup;
					}

					public Menu getMenu(Menu parent) {
						return null;
					}

					protected Menu createRunMenu(Control parent) {
						Menu menu = new Menu(parent.getShell(), SWT.POP_UP);

						int defMode = Activator.getDefault().getPreferenceStore().getInt(PreferenceConstants.P_DEFAULT_RUN_MODE);
						int runMode = settings.getInteger(CustomEditorSettings.STEP_MODE, defMode);

						MenuItem item = new MenuItem(menu, SWT.RADIO);
						item.setText(runLabels[0]);
						item.setSelection(runMode == 0);
						item.addSelectionListener(new SelectionListener() {
							public void widgetDefaultSelected(SelectionEvent e) {
							}

							public void widgetSelected(SelectionEvent e) {
								if (((MenuItem) e.widget).getSelection()) {
									setRunMode(0);
									runAssemblyLine();
								}
							}
						});

						item = new MenuItem(menu, SWT.RADIO);
						item.setText(runLabels[1]);
						item.setSelection(runMode == 1);
						item.addSelectionListener(new SelectionListener() {
							public void widgetDefaultSelected(SelectionEvent e) {
							}

							public void widgetSelected(SelectionEvent e) {
								if (((MenuItem) e.widget).getSelection()) {
									setRunMode(1);
									runAssemblyLine();
								}
							}
						});

						item = new MenuItem(menu, SWT.RADIO);
						item.setText(runLabels[2]);
						item.setSelection(runMode == 2);
						item.addSelectionListener(new SelectionListener() {
							public void widgetDefaultSelected(SelectionEvent e) {
							}

							public void widgetSelected(SelectionEvent e) {
								if (((MenuItem) e.widget).getSelection()) {
									setRunMode(2);
									runAssemblyLine();
								}
							}
						});

						item = new MenuItem(menu, SWT.RADIO);
						item.setText(runLabels[3]);
						item.setSelection(runMode == 3);
						item.addSelectionListener(new SelectionListener() {
							public void widgetDefaultSelected(SelectionEvent e) {
							}

							public void widgetSelected(SelectionEvent e) {
								if (((MenuItem) e.widget).getSelection()) {
									setRunMode(3);
									runAssemblyLine();
								}
							}
						});

						item = new MenuItem(menu, SWT.RADIO);
						item.setText(runLabels[4]);
						item.setSelection(runMode == 4);
						item.addSelectionListener(new SelectionListener() {
							public void widgetDefaultSelected(SelectionEvent e) {
							}

							public void widgetSelected(SelectionEvent e) {
								if (((MenuItem) e.widget).getSelection()) {
									setRunMode(4);
									runAssemblyLine();
								}
							}
						});

						item = new MenuItem(menu, SWT.RADIO);
						item.setText(runLabels[5]);
						item.setSelection(false);
						item.addSelectionListener(new SelectionListener() {
							public void widgetDefaultSelected(SelectionEvent e) {
							}

							public void widgetSelected(SelectionEvent e) {
								if (((MenuItem) e.widget).getSelection()) {
									RunOptionsDialog dlg = new RunOptionsDialog(getSite().getShell(), getTDIConfiguration(),
											AssemblyLineEditor2.this, settings);
									if (dlg.open() == Window.OK) {
										settings.saveSettings();
										setRunMode(settings.getInteger(CustomEditorSettings.STEP_MODE, 0));
										runAssemblyLine();
									}
								}
							}
						});
						return menu;
					}

					private void setRunMode(int mode) {
						settings.setProperty(CustomEditorSettings.STEP_MODE, mode);
						settings.saveSettings();
						for (int i = 0; i < popup.getItemCount(); i++)
							popup.getItem(i).setSelection((mode == i));

						setToolTipText(getToolTipText());
					}

				};
			}

			public int getStyle() {
				return AS_DROP_DOWN_MENU;
			}

			public String getText() {
				return null;
			}

			public void run() {
				runAssemblyLine();
			}
		};
		new CommandHandlerProxy(getEditorSite(), runAction, CommandID.AL_EDITOR_RUN); 
		treeForm.getToolBarManager().add(runAction);	
		
		treeForm.getToolBarManager().update(true);
		treeForm.setToolBarVerticalAlignment(SWT.BOTTOM);
	}

	public void handleComponentInserted(BaseConfiguration component) {
		if(component != null) {
			tree.setSelection(new StructuredSelection(component));
			tree.setExpandedState(component, true);
			// -- branches should have children so prompt user to add one right away
			if(component instanceof BranchingConfig &&
					MessageDialog.openQuestion(getSite().getShell(),
							Messages.getString("BranchingConfig.title"), 
							Messages.getString("AddBranchComponent.label"))) {
				InsertComponent ic = new InsertComponent(getSite().getShell(), getTDIConfiguration(), true);
				ic.setLocation((BranchingConfig)component);
				ic.run();
			}
		}
	}

	@Override
	public void dispose() {
		if (tk != null) {
			tk.dispose();
			tk = null;
		}

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
					if(isDirty()) {
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
		
		ServerView view = (ServerView) getSite().getPage().findView("com.ibm.tdi.rcp.serverview");
		if(view != null) {
			IFile server;
			try {
				server = (IFile) Utils.getTDIServer(Utils.getTDIServer(getTDIConfigFile()));
			} catch (Exception e) {
				MessageDialog.openError(getSite().getShell(), Messages.getString("general.run.label"), 
						Messages.getMessage("ALFC.no.server", null));
				return;
			}
			if(!view.isServerRunning(server)) {
				MessageDialog.openError(getSite().getShell(), Messages.getString("general.run.label"), 
						Messages.getMessage("ALFC.no.server.instance", server.getName()));
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
		closeEditor();

		//
		// Make sure project builder has updated runtime config file
		//
		try {
			getTDIConfigProject().build(IncrementalConfigBuilder.INCREMENTAL_BUILD, null);
			Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD, null);
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e, getSite().getShell());
			return;
		}
		
		//
		// Check for errors
		//
		if(hasErrors()
				&& !MessageDialog.openQuestion(getSite().getShell(), 
						Messages.getString("general.error.label"), 
						Messages.getString("AssemblyLineEditor.has.errors"))) {
			try {
				getSite().getPage().showView("org.eclipse.ui.views.ProblemView");
			} catch (PartInitException e) {
				SystemFunctions.doNothing();
			}
			return;
		}
		
		RunAssemblyLineInput input = new RunAssemblyLineInput((AssemblyLineConfig) getTDIConfiguration());
		input.setStepMode(stepMode);

		// Provide work entry?
		if (settings.getBoolean(CustomEditorSettings.WORK_ENABLED, false))
			input.setWorkEntry(settings.getEntry(CustomEditorSettings.WORK_ENTRY));

		// Operation?
		if (settings.getString(CustomEditorSettings.AL_OPERATION, null) != null)
			input.setOperation(settings.getString(CustomEditorSettings.AL_OPERATION, null));

		// Simulate?
		input.setSimulateMode(settings.getBoolean(CustomEditorSettings.AL_SIMULATE, false));

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
			// -- If the config has been modified we have to do a new run through the validator
			if(isDirty()) {
				getTDIConfigFile().deleteMarkers(Utils.TDI_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE); //$NON-NLS-1$
				new AssemblyLineValidator().validate(getTDIConfiguration());
			}
			
			IMarker[] markers = getTDIConfigFile().findMarkers(Utils.TDI_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
			if(markers.length > 0) {
				int type = IMessage.WARNING;
				for(IMarker marker : markers) {
					if(marker.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING) == IMarker.SEVERITY_ERROR) {
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
			@Override
			public void dragSetData(DragSourceEvent event) {
				if (LocalSelectionTransfer.getTransfer().isSupportedType(event.dataType)) {
					IStructuredSelection sel = (IStructuredSelection) tree.getSelection();
					LocalSelectionTransfer.getTransfer().setSelection(sel);
					event.data = sel;
					event.doit = true;
				}
			}

			@Override
			public void dragStart(DragSourceEvent event) {
				IStructuredSelection sel = (IStructuredSelection) tree.getSelection();
				LocalSelectionTransfer.getTransfer().setSelection(sel);
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
					for (Iterator<Object> i = sel.iterator(); i.hasNext();) {
						Object nextDrop = i.next();
						if (nextDrop instanceof IFile) {
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
							
							if(obj instanceof AttributeMapItem) {
								// Allow Attribute maps to be dropped onto connectors
								if(target instanceof ConnectorConfig) {
									workMapWidget.addAttributeMapItem(((ConnectorConfig)target).getAttributeMap(), (AttributeMapItem)obj);
								}
								continue;
							}
							// We do not how to move anything not in a
							// Container\
							// This may change later
							if (!(obj.getParent() instanceof ContainerConfig)) {
								continue;
							}

							// No point in moving onto self
							if (isParent(obj, target))
								return;

							if (target == ef || (target != null && target.getParent() == ef)) {
								// Only want EntryFeed components here
								if (!(obj instanceof ConnectorConfig) || !Utils.isEntryFeedConnector((ConnectorConfig) obj))
									return;
								if (((ContainerConfig) obj).size() > 0)
									return;
							}

							MoveConfigOperation operation = new MoveConfigOperation(obj, target, pos);
							IUndoContext undoContext = getSite().getWorkbenchWindow().getWorkbench().getOperationSupport()
									.getUndoContext();
							operation.addContext(undoContext);
							try {
								OperationHistoryFactory.getOperationHistory().execute(operation, null, null);
							} catch (ExecutionException e) {
								e.printStackTrace();
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

		if (Utils.isEntryFeedMode(cc.getMode()))
			target = alc.getEntryFeedComponents();
		else if (targetContainer != null)
			target = targetContainer;
		else if (getSelectedContainer() != null)
			target = getSelectedContainer();
		else
			target = alc.getDataFlowComponents();

		boolean isfeed = target == alc.getEntryFeedComponents();

		// retarget to data flow
		if (isfeed && (!Utils.isEntryFeedMode(cc.getMode()) || cc.size() > 0)) {
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
	 * @return The selected ContainerConfig 
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
			} catch (Exception e) {
				EclipseAppender.logerror(e.toString(), e, getSite().getShell());
			}
		}
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
			super("Paste"); //$NON-NLS-1$
		}

		@Override
		protected void performPaste(IStructuredSelection selection) {
			for (Object obj : selection.toList()) {
				insertObject(obj, getSelectedContainer(), -1);
			}
		}

		@Override
		protected boolean validatePaste(Object obj) {
			return (obj instanceof ConnectorConfig ||
					obj instanceof ScriptConfig || 
					obj instanceof BranchingConfig ||
					obj instanceof String || 
					obj instanceof IFile);
		}

	}

	@Override
	protected void quickEdit(BaseConfiguration bc) {
		editorStack.topControl = getQuickEditorForm();
		qeditors.layout();
		super.quickEdit(bc);
	}

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
	 * @see com.ibm.tdi.eclipse.editors.BaseEditor#gotoMarker(org.eclipse.core.resources.IMarker)
	 */
	public void gotoMarker(IMarker marker) {
		try {
			String location = (String) marker.getAttribute(IMarker.LOCATION);
			BaseConfiguration loc = getTDIConfiguration().getChildForPath(location);
			if (loc == null)
				return;

			String problem = (String) marker.getAttribute(IMarker.PROBLEM);

			// Try to make the loc object current selection
			tree.setSelection(new StructuredSelection(loc), true);
			
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
			if(AssemblyLineValidator.ORPHANED_BRANCH.equals(problem)) {
				return;
			}

			// If target item is already open do nothing
			if (isEditorVisible() && getEditor() != null && getEditor().getEditingConfig() == loc)
				return;
			
			// Could be an item deep inside a widget
			BaseConfiguration bc = loc;
			while(bc != null) {
				tree.setSelection(new StructuredSelection(bc));
				if(!tree.getSelection().isEmpty()) {
					IEditorPart part = editItem(new StructuredSelection(bc));
					if (part instanceof BaseEditor) {
						((BaseEditor) part).gotoMarker(marker);
					}
					return;
				}
				bc = bc.getParent();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void configurationChanged(MetamergeConfigChange changeEvent) {
		super.configurationChanged(changeEvent);
		if (changeEvent.getSource() instanceof ConnectorConfig && InternalSchema.CONNECTOR_MODE.equals(changeEvent.getKey()))
			verifyEntryFeed();
	}

	public void verifyEntryFeed() {
		AssemblyLineConfig alc = (AssemblyLineConfig) getTDIConfiguration();
		if (alc == null)
			return;
		for (BaseConfiguration bc : alc.getEntryFeedComponents().getConfigurations(null)) {
			if (bc instanceof ConnectorConfig && Utils.isEntryFeedConnector((ConnectorConfig) bc))
				continue;
			EclipseAppender.loginfo(Messages.getMessage("AssemblyLineEditor.auto.moving", bc.getShortName()));
			MoveConfigOperation operation = new MoveConfigOperation(bc, alc.getDataFlowComponents(), TDI.INSERT_INTO);
			IUndoContext undoContext = getSite().getWorkbenchWindow().getWorkbench().getOperationSupport().getUndoContext();
			operation.addContext(undoContext);
			try {
				OperationHistoryFactory.getOperationHistory().execute(operation, null, null);
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
	}

}
