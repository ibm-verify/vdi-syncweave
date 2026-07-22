/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.editors;

import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.forms.widgets.Form;

import com.ibm.di.config.base.BaseConfigurationImpl;
import com.ibm.di.config.base.InternalSchema;
import com.ibm.di.config.base.ScriptConfigImpl;
import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.di.config.interfaces.MetamergeConfigChangeListener;
import com.ibm.di.config.interfaces.ScriptConfig;
import com.ibm.di.config.interfaces.SequenceConfig;
import com.ibm.di.function.SystemFunctions;
import com.ibm.tdi.eclipse.Activator;
import com.ibm.tdi.eclipse.ConfigUtils;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.TDI;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.actions.CutConfigAction;
import com.ibm.tdi.eclipse.actions.RenameConfigAction;
import com.ibm.tdi.eclipse.builders.IncrementalConfigBuilder;
import com.ibm.tdi.eclipse.commands.CommandHandlerProxy;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.util.TDIToolBar;
import com.ibm.tdi.eclipse.views.ServerView;
import com.ibm.tdi.eclipse.widget.BaseWidget;
import com.ibm.tdi.eclipse.widget.FormWidget2;
import com.ibm.tdi.eclipse.widget.LoggingWidget;
import com.ibm.tdi.eclipse.widget.ScriptWidget;

public class SequenceEditor extends BaseEditor {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private final static String AL = "assemblyLine";

	private TDIToolBar header;

	private SequenceConfig config;

	private TreeViewer tree;

	private SequenceProvider provider;

	private Composite itemEditorPanel;

	private StackLayout itemEditorStack;

	private Action moveUpAction;

	private Action moveDownAction;

	private Action insertAction;

	private Action insertScriptAction;

	private Action logAction;

	private Action runAction;

	private CutConfigAction cutAction;

	private LoggingWidget loggingWidget;

	@Override
	public void createPartControl(Composite parent) {
		if (getTDIConfiguration() == null) {
			super.createPartControl(parent);
			return;
		}

		config = (SequenceConfig) getTDIConfiguration();

		if (!config.hasParameter(InternalSchema.ENABLED))
			config.setEnabled(true);

		BaseWidget base = new BaseWidget(parent, SWT.NONE);
		base.setLayout(new FillLayout());

		Form form = base.createForm(base, null);
		form.getBody().setLayout(new FillLayout());

		// Toolbar
		header = new TDIToolBar(form);
		header.setText(getTDIConfiguration().getShortName());
		header.setImage(Activator.getImage("Settings"));
		createActions();

		createMainButtons(header);

		// -- The main view
		createMainPanel(form.getBody());
		addDragDropSupport();

		updateActions();
		registerAction(ActionFactory.DELETE.getId(), cutAction);
		updateActionBars();
	}

	private void createActions() {
		insertAction = new Action() {
			@Override
			public void run() {
				BaseConfiguration bc = new BaseConfigurationImpl();
				bc.setEnabled(true);
				int i = config.indexOf(getSelectedConfig(null));
				if (i >= 0)
					config.insertConfig(bc, i + 1);
				else
					config.addConfig(bc);
				tree.refresh();
				tree.setSelection(new StructuredSelection(bc), true);
			}
		};
		insertAction.setText(Messages.getString("SequenceEditor.Add.label"));
		insertAction.setImageDescriptor(ImageDescriptor.createFromImage(Activator.getImage("AssemblyLine")));

		insertScriptAction = new Action() {
			@Override
			public void run() {
				ScriptConfig sc = new ScriptConfigImpl();
				sc.setEnabled(true);
				try {
					sc.setName(Messages.getString("BaseEditor.21"));
				} catch (Exception ignore) {
					//Cannot happen
					SystemFunctions.doNothing();
				}
				int i = config.indexOf(getSelectedConfig(null));
				if (i >= 0)
					config.insertConfig(sc, i + 1);
				else
					config.addConfig(sc);
				tree.refresh();
				tree.setSelection(new StructuredSelection(sc), true);
			}
		};
		insertScriptAction.setText(Messages.getString("SequenceEditor.AddScript.label"));
		insertScriptAction.setImageDescriptor(ImageDescriptor.createFromImage(Activator.getImage("Script_16")));

		cutAction = new CutConfigAction(Messages.getString("Localized.Delete"), null);
		cutAction.setActivePart(null, this);

		moveUpAction = new Action() {
			@Override
			public void run() {
				BaseConfiguration source = getSelectedConfig(null);
				if (source != null && source.getParent() == config) {
					if (config.indexOf(source) > 0) {
						config.moveConfig(source, true);
					}
					tree.setSelection(new StructuredSelection(source));
				}
			}

			@Override
			public boolean isEnabled() {
				BaseConfiguration source = getSelectedConfig(null);
				if (source == null || source.getParent() != config)
					return false;
				return config.indexOf(source) > 0;
			}
		};
		moveUpAction.setText(Messages.getString("general.moveup.label"));
		moveUpAction.setToolTipText(Messages.getString("general.moveup.tooltip"));
		moveUpAction.setActionDefinitionId("com.ibm.tdi.rcp.moveup");

		moveDownAction = new Action() {
			@Override
			public void run() {
				BaseConfiguration source = getSelectedConfig(null);
				if (source != null && source.getParent() == config) {
					if (config.indexOf(source) < config.size() - 1) {
						config.moveConfig(source, false);
					}
					tree.setSelection(new StructuredSelection(source));
				}
			}

			@Override
			public boolean isEnabled() {
				BaseConfiguration source = getSelectedConfig(null);
				if (source == null || source.getParent() != config)
					return false;
				return (config.indexOf(source) < config.size() - 1);
			}
		};
		moveDownAction.setText(Messages.getString("general.movedown.label"));
		moveDownAction.setToolTipText(Messages.getString("general.movedown.tooltip"));
		moveDownAction.setActionDefinitionId("com.ibm.tdi.rcp.movedown");

		logAction = new Action() {
			@Override
			public void run() {
				if (loggingWidget == null) {
					loggingWidget = new LoggingWidget(itemEditorPanel, SWT.TITLE, config.getLogConfig());
					loggingWidget.getForm().setText(Messages.getString("assemblyline.tabs.logging.label")); 
				}
				itemEditorStack.topControl = loggingWidget;
				itemEditorPanel.layout();
			}
		};
		logAction.setText(Messages.getString("assemblyline.tabs.logging.label"));
		logAction.setToolTipText(Messages.getString("assemblyline.tabs.logging.tooltip"));

		runAction = new Action() {
			public void run() {
				runSequence();
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
		new CommandHandlerProxy(getEditorSite(), runAction);
	}

	private void createMainPanel(Composite parent) {
		SashForm sash = new SashForm(parent, SWT.HORIZONTAL);

		Composite left = new Composite(sash, SWT.NONE);
		left.setBackground(left.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		Utils.setGridLayout(left, 1, false);

		//
		// -- Tree Viewer
		//
		tree = new TreeViewer(left, SWT.MULTI | SWT.FULL_SELECTION);
		tree.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));

		provider = new SequenceProvider();
		tree.setContentProvider(provider);
		tree.setLabelProvider(provider);
		tree.setInput(config);
		tree.expandAll();

		//
		// Single click opens item editor
		//

		tree.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateActions();
				editItem(((IStructuredSelection) event.getSelection()));
			}

		});

		tree.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				editItem(((IStructuredSelection) event.getSelection()));
			}
		});

		tree.getTree().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.INSERT && insertAction.isEnabled())
					insertAction.run();
				if (e.keyCode == SWT.F2) {
					doRename((IStructuredSelection) tree.getSelection());
				}
			}
		});


		// -- Create menu for contributions
		registerContextMenu(tree);
		addSelectionProvider(tree);

		// -- Add move up/down menu options
		getMenuManager().appendToGroup(TDI.GROUP_TDI + ".1", new ActionContributionItem(moveUpAction) {
			@Override
			public boolean isDynamic() {
				return true;
			}			
		}); 
		getMenuManager().appendToGroup(TDI.GROUP_TDI + ".1", new ActionContributionItem(moveDownAction) {
			@Override
			public boolean isDynamic() {
				return true;
			}			
		}); 
		getMenuManager().appendToGroup(TDI.GROUP_TDI + ".1", new ActionContributionItem(cutAction) {
			@Override
			public boolean isDynamic() {
				return true;
			}			
		}); 

		// -- Create stack panel for editing items
		itemEditorStack = new StackLayout();
		itemEditorPanel = new Composite(sash, SWT.NONE);
		itemEditorPanel.setLayout(itemEditorStack);

		// TODO:		itemEditorStack.topControl = sequenceWidget;

		sash.setWeights(new int[] { 25, 75 });
	}

	private void createMainButtons(TDIToolBar header2) {
		header.add(insertAction);
		header.add(insertScriptAction);
		header.add(cutAction);
		//		header.add(moveUpAction);
		//		header.add(moveDownAction);
		header.add(logAction);
		header.add(runAction);
	}

	public BaseConfiguration getSelectedConfig(IStructuredSelection sel) {
		if (sel == null && tree != null)
			sel = (IStructuredSelection) tree.getSelection();
		if (sel == null || sel.size() != 1)
			return null;
		else
			return (BaseConfiguration) sel.getFirstElement();
	}

	private void editItem(IStructuredSelection selection) {
		editItem(getSelectedConfig(selection));
	}

	private void editItem(BaseConfiguration bc) {
		Composite widget = findEditorStackItem(bc);
		if (widget == null && bc != null) {
			try {
				if (bc instanceof ScriptConfig)
					widget = createScriptWidget(bc);
				else
					widget = new FormWidget2(itemEditorPanel, SWT.NONE, bc, "SequenceElement");
			} catch (Exception e) {
				EclipseAppender.logerror("editItem", e);
				return;
			}
		}

		// Hide?
		if (widget == itemEditorStack.topControl) {
			widget = null;
		}

		if (widget != null) {
			itemEditorStack.topControl = widget;
		} else if (bc == null) {
			// TODO: itemEditorStack.topControl = sequenceWidget;
			itemEditorStack.topControl = null;
		}

		itemEditorPanel.layout();
	}

	private BaseWidget createScriptWidget(final BaseConfiguration bc) {
		BaseWidget base = new BaseWidget(itemEditorPanel, SWT.NONE);
		base.setLayout(new FillLayout());
		base.createForm(base, null);
		base.getForm().getBody().setLayout(new FillLayout());
		final TDIToolBar bar = new TDIToolBar(base.getForm());
		bar.setText(bc.getShortName());
		bar.setImage(Activator.getImage(bc));
		bar.addInheritanceButton(bc);

		new ScriptWidget(bc, base.getForm().getBody(), SWT.NONE);
		base.setEditingConfig(bc);
		
		MetamergeConfigChangeListener l = new MetamergeConfigChangeListener() {
			
			public void configurationChanged(MetamergeConfigChange changeEvent) {
				if (bar.isDisposed()) {
					bc.removeListener(this);
					return;
				}
				if (changeEvent.getSource() == bc && "name".equals(changeEvent.getKey())) {
					bar.setText(bc.getShortName());					
				}
			}
		};
		base.setData("LISTENER", l);
		bc.addListener(l);
		return base;
	}

	private Composite findEditorStackItem(BaseConfiguration cc) {
		if (cc == null)
			return null;

		for (Control c : itemEditorPanel.getChildren()) {
			if (c instanceof BaseWidget) {
				BaseWidget widget = (BaseWidget) c;
				if (widget.getEditingConfig() == cc) {
					return widget;
				}
			}
		}
		return null;
	}

	@Override
	public void configurationChanged(final MetamergeConfigChange changeEvent) {
		super.configurationChanged(changeEvent);
		if (tree == null || tree.getControl().isDisposed())
			return;
		getSite().getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				if ("name".equals(changeEvent.getKey()))
					header.setText(config.getShortName());
				tree.refresh();
			}
		});
	}

	private void updateActions() {
		moveUpAction.setEnabled(moveUpAction.isEnabled());
		moveDownAction.setEnabled(moveDownAction.isEnabled());
		cutAction.selectionChanged(cutAction, tree.getSelection());
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

	@Override
	public void setFocus() {
		super.setFocus();
		if (tree != null)
			tree.getTree().setFocus();
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
		getEditorSite().setSelectionProvider(getSelectionProvider());
	}

	/**
	 * Configures drag/drop support for the TreeViewer
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
				LocalSelectionTransfer.getTransfer().setSelection(selection);
				event.doit = true;
			}
		};
		tree.addDragSupport(DND.DROP_COPY | DND.DROP_MOVE, new Transfer[] { LocalSelectionTransfer.getTransfer() }, dsl);

		// -- Add Drop Support
		DropTargetListener dtl = new DropTargetAdapter() {
			public void dragEnter(DropTargetEvent event) {
				if (LocalSelectionTransfer.getTransfer().isSupportedType(event.currentDataType)) {
					event.detail = DND.DROP_COPY;
				}
			}

			private int getInsertionPoint(TreeItem item, int x, int y) {
				Rectangle rect = item.getBounds();

				Point p = item.getDisplay().map(null, item.getParent(), new Point(x, y));
				int off = p.y - rect.y;

				// 2 choices, before or after
				int h = rect.height / 2;
				if (off <= h)
					return TDI.INSERT_BEFORE;
				else
					return TDI.INSERT_AFTER;
			}

			public void dragOver(DropTargetEvent event) {
				if (LocalSelectionTransfer.getTransfer().isSupportedType(event.currentDataType)) {
					if (event.item == null) {
						event.feedback = DND.FEEDBACK_EXPAND | DND.FEEDBACK_INSERT_AFTER;
					} else {
						switch (getInsertionPoint((TreeItem) event.item, event.x, event.y)) {
						case TDI.INSERT_BEFORE:
							event.feedback = DND.FEEDBACK_EXPAND | DND.FEEDBACK_INSERT_BEFORE;
							break;
						case TDI.INSERT_AFTER:
							event.feedback = DND.FEEDBACK_EXPAND | DND.FEEDBACK_INSERT_AFTER;
							break;
						}
					}
					event.detail = DND.DROP_COPY;
				}
			}

			public void drop(DropTargetEvent event) {
				if (!LocalSelectionTransfer.getTransfer().isSupportedType(event.currentDataType))
					return;
				int index;
				if (event.item instanceof TreeItem) {
					TreeItem item = (TreeItem) event.item;
					index = config.indexOf((BaseConfiguration)item.getData());
					if (getInsertionPoint(item, event.x, event.y) == TDI.INSERT_AFTER)
						index++;
				} else {
					index = config.size();
				}

				IStructuredSelection sel = (IStructuredSelection) event.data;

				BaseConfiguration obj = null;
				BaseConfiguration newItem = null;
				for (Iterator<?> i = sel.iterator(); i.hasNext();) {
					Object nextDrop = i.next();
					try {
						if (nextDrop instanceof IFile) {
							TDIConfigurationFile cfg = TDIConfigurationFile.loadFile((IFile) nextDrop);
							obj = cfg.getDefaultConfigObject();
						} else if (nextDrop instanceof BaseConfiguration) {					
							obj = (BaseConfiguration) nextDrop;
						} else {
							continue;
						}

						if(obj.getParent() != config) {
							// Drop from system library
							newItem = createObject(obj);
							if (newItem != null) {
								config.insertConfig(newItem, index);
								index++;
							}
						} else {
							if (config.indexOf(obj) < index)
								index--;
							config.removeConfig(obj);
							config.insertConfig(obj, index);
							index++;
						}
					} catch (Exception e) {
						EclipseAppender.logerror(e.toString(), e, getSite().getShell());
					}
				}
				tree.refresh();
				if (newItem != null)
					tree.setSelection(new StructuredSelection(newItem));
			}
		};
		tree.addDropSupport(DND.DROP_COPY, new Transfer[] { LocalSelectionTransfer.getTransfer() }, dtl);
	}

	private BaseConfiguration createObject(BaseConfiguration obj) throws Exception {
		if (obj instanceof AssemblyLineConfig || obj instanceof SequenceConfig) {
			BaseConfiguration ret = new BaseConfigurationImpl();
			String al = obj.getShortName();
			if (obj instanceof SequenceConfig)
				al = "/" + MetamergeConfig.DEFAULT_SEQUENCE_FOLDER + "/" + al;
			ret.setParameter(AL, al);

			IProject sourceProject = Utils.getProjectFor(obj);
			if(sourceProject != null && ! sourceProject.equals(Utils.getProjectFor(config))) {
				ret.setParameter("config", sourceProject.getName());
			}
			ret.setEnabled(true);
			return ret;
		}
		if (obj instanceof ScriptConfig) {
			return ConfigUtils.createInheritedComponent(config.getMetamergeConfig(), obj);
		}
		if (obj.hasParameter(AL)) {
			return (BaseConfiguration) obj.getClone();
		}
		return null;
	}

	private String getAssociatedServer() {
		try {
			return Utils.getTDIServer(getTDIConfigFile());
		} catch (CoreException e) {
			return e.toString();
		}
	}

	private void runSequence() {

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

		RunAssemblyLineInput input = new RunAssemblyLineInput(config);

		try {
			getEditorSite().getWorkbenchWindow().getActivePage().openEditor(input, RunAssemblyLineEditor.EDITOR_ID, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private class SequenceProvider implements ILabelProvider, ITreeContentProvider {

		public Object[] getChildren(Object parentElement) {
			if (parentElement == config) {
				int n = config.size();
				Object[] ret = new Object[n];
				for (int i = 0; i<n; i++)
					ret[i] = config.getConfig(i);
				return ret;
			}
			return null;
		}

		public Object getParent(Object element) {
			if (element == config)
				return null;
			else
				return config;
		}

		public boolean hasChildren(Object element) {
			return element == config;
		}

		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		public Image getImage(Object element) {
			if (element instanceof ScriptConfig) {
				return Activator.getImage((ScriptConfig)element);
			}
			if (element instanceof BaseConfiguration) {
				boolean enabled = ((BaseConfiguration) element).getEnabled();
				return Activator.getImage("AssemblyLine", enabled);
			}
			return null;
		}

		public String getText(Object element) {
			if (element instanceof BaseConfiguration) {
				BaseConfiguration b = (BaseConfiguration) element;			
				String s = b.getShortName();
				if (s != null && s.trim().length() > 0)
					return s;
				s = b.getStringParameter(AL);
				if (s != null && s.trim().length() > 0)
					return s;
				return Messages.getString("SequenceEditor.NoName.label");
			}
			return null;
		}

		public void dispose() {
		}

		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		public void addListener(ILabelProviderListener listener) {
		}

		public void removeListener(ILabelProviderListener listener) {
		}

	}
}
