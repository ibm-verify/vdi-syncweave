/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.views;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;

import com.ibm.di.api.ALEvent;
import com.ibm.di.api.DIEvent;
import com.ibm.di.api.DIException;
import com.ibm.di.api.remote.AssemblyLine;
import com.ibm.di.api.remote.ConfigInstance;
import com.ibm.di.api.remote.DIEventListener;
import com.ibm.di.api.remote.Session;
import com.ibm.di.api.remote.impl.DIEventListenerBase;
import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.entry.Entry;
import com.ibm.di.function.SystemFunctions;
import com.ibm.tdi.eclipse.Activator;
import com.ibm.tdi.eclipse.ConfigUtils;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.actions.OpenTDIConfigFile;
import com.ibm.tdi.eclipse.actions.RenameResourceAction;
import com.ibm.tdi.eclipse.actions.ViewServerComponents;
import com.ibm.tdi.eclipse.commands.CommandHandlerProxy;
import com.ibm.tdi.eclipse.commands.CommandID;
import com.ibm.tdi.eclipse.console.AssemblyLineConsole;
import com.ibm.tdi.eclipse.editors.ConfigInstanceEditor;
import com.ibm.tdi.eclipse.editors.ConfigSettingsEditor;
import com.ibm.tdi.eclipse.editors.RunAssemblyLineEditor;
import com.ibm.tdi.eclipse.editors.RunAssemblyLineInput;
import com.ibm.tdi.eclipse.editors.RunRemoteAssemblyLineInput;
import com.ibm.tdi.eclipse.editors.ServerEditor;
import com.ibm.tdi.eclipse.editors.SystemStoreEditor;
import com.ibm.tdi.eclipse.jobs.StartLocalServerJob;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.natures.TDINature;
import com.ibm.tdi.eclipse.server.RMIServerAPI;
import com.ibm.tdi.eclipse.server.RestServerAPI;
import com.ibm.tdi.eclipse.wizards.ExportRuntimeWizard;
import com.ibm.tdi.eclipse.wizards.NewServerWizard;

/**
 * This view shows the list of servers defined in the "TDI Servers" project. For
 * each server we keep a session object open at all times to listen for server
 * events, which in turn updates the status for each server (e.g. config
 * start/stop and al start/stop). In addition, the events are printed to the
 * server's console window showing basic start/stop information.
 * <p>
 * The view also provides a number of commands that operate on servers, config
 * instances and assemblylines.
 */
public class ServerView extends ViewPart {
	/**
	 * 
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public final static String VIEW_ID = "com.ibm.tdi.rcp.serverview";

	private final static String XT = "tdiserver";

	private final static String DASHBOARD = "/dashboard";
	/**
	 * The status map contains an object that contains the status for the server
	 * (e.g. Exception object, Start job or Entry with details).
	 */
	// private Hashtable<IFile, Object> status = new Hashtable<IFile, Object>();

	/**
	 * This is the actual tree displaying servers, configs and assemblylines
	 */
	private TreeViewer tree;

	/**
	 * This is a "disabled" Run image we use when a server is being launched and
	 * the server api is not yet available.
	 */
	private Image startingImage;

	/**
	 * The form hosts the header/toolbar and the tree view
	 */
	private Form form;
	private FormToolkit tk;

	/**
	 * Button controls in the header panel
	 */
	private HashMap<String, Button> buttons;

	/**
	 * Map for Session objects we have with each server.
	 */
	private HashMap<IFile, SessionEntry> sessions = new HashMap<IFile, SessionEntry>();

	/**
	 * This job runs at 15 second intervals to refresh server status.
	 */
	private Job timedRefreshJob;

	/*
	 * Action objects
	 */
	private Action newAction;

	private Action startAction;

	private Action stopAction;

	private Action refreshAction;

	private Action viewLogAction;

	private Action debugAction;

	private Action showCompAction;

	private Action attachDebugAction;

	private Action launchAMCAction;

	private Action deleteAction;

	private Action editSSS;

	private Action renameServer;

	private Action browseStore;

	private Action openServer;

	private Action importAction;

	private Action exportAction;

	private Action startConfigAction;

	private Action startALAction;
	
	private Action openDashboard;

	// Set this to true if you want to log Start and Stop messages
	private static boolean logStartAndStopMessages;
	
	private Set<IFile> loggedSolutionError = new HashSet<IFile>();
	
	/**
	 * Constructor
	 */
	public ServerView() {
	}

	/**
	 * We use this class to map a config instance to its server. In addition we
	 * need an equals() method to properly compare two ConfigInstance objects.
	 * The server api returns a new object every time you ask for a
	 * getConfigInstance even though they return the same instance. Having the
	 * equals() method makes it easier to maintain the tree representation when
	 * start/stop events occur on config instances.
	 * 
	 */
	private static class RunningCI {
		private IFile server;
		private ConfigInstance ci;
		private String id;

		public RunningCI(IFile server, ConfigInstance ci) throws Exception {
			super();
			this.server = server;
			this.ci = ci;
			this.id = ci.getConfigId();
		}

		public boolean isConfig(String id) {
			return this.id.equals(id);
		}

		public String getConfigId() {
			return id;
		}

		public String toString() {
			return getConfigId();
		}

		@Override
		public int hashCode() {
			return id.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this)
				return true;	
		if (obj instanceof RunningCI) {
				RunningCI rci = (RunningCI) obj;
				try {
					return id.equals(rci.id) && server.equals(rci.server);
				} catch (Exception e) {
					return false;
				}
			} else {
				return super.equals(obj);
			}
		}

		public void stop() throws Exception {
			ci.stop();
		}
	}

	/**
	 * This class wraps an assemblyline handle.
	 */
	private static class RunningAL {
		private RunningCI parent;
		private AssemblyLine al;
		private int id;
		private String name;
		private boolean useId;

		public RunningAL(RunningCI parent, AssemblyLine al) throws Exception {
			super();
			this.parent = parent;
			this.al = al;
			this.name = al.getName();
			this.id = al.getUniqueCode();
		}

		public RunningAL(RunningCI parent, String name, int id) throws Exception {
			super();
			this.parent = parent;
			this.name = name;
			this.id = id;
		}

		public String toString() {
			String s = name;
			if (name.startsWith("AssemblyLines/"))
				s = name.substring("AssemblyLines/".length());
			if (useId)
				s += "." + id;
			return s;
		}

		public String getName() {
			return name;
		}

		void setUseId() {
			useId = true;
		}

		public int getUniqueCode() {
			return id;
		}

		@Override
		public int hashCode() {
			return name.hashCode() ^ id;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this)
				return true;	
			if (obj instanceof RunningAL) {
				RunningAL ral = (RunningAL) obj;
				return ral.name.equals(name) && ral.id == id &&
					ral.parent.equals(parent);
			}
			return false;
		}

		public AssemblyLine getAL() {
			if (al == null) {
				try {
					for (AssemblyLine a : parent.ci.getAssemblyLines()) {
						if (name.equals(a.getName()) && id == a.getUniqueCode()) {
							al = a;
							break;
						}
					}
				} catch (Exception e) {
					al = null;
				}
			}
			return al;
		}
		
		public void stop() throws Exception {
			if (getAL() != null)
				getAL().stop();
		}

	}

	@Override
	public void createPartControl(Composite parent) {

		Image img = Activator.getImage("Run");
		startingImage = new Image(img.getDevice(), img, SWT.IMAGE_DISABLE);

		//
		// -- Create Form to host toolbar and contents
		//
		tk = new FormToolkit(parent.getDisplay());
		form = tk.createForm(parent);
		tk.decorateFormHeading(form);
		form.getBody().setLayout(new FillLayout());
		//form.setText(Messages.getString("serverview.title"));;

		//
		// -- Tree viewer with server status
		//
		tree = new TreeViewer(form.getBody(), SWT.NONE);
		tree.setContentProvider(new ServerContentProvider());
		tree.setLabelProvider(new ServerLabelProvider());
		try {
			tree.setInput(Utils.getTDIServersProject(true));
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e, getSite().getShell());
		}

		DropTargetAdapter dtl = new DropTargetAdapter() {
			private IFile targetServer = null;
			@Override
			public void dragOver(DropTargetEvent event) {
				event.detail = DND.DROP_NONE;
				if (LocalSelectionTransfer.getTransfer().isSupportedType(event.currentDataType)) {
					ISelection sel = LocalSelectionTransfer.getTransfer().getSelection();
					if (sel instanceof IStructuredSelection) {
						Object obj = ((IStructuredSelection) sel).getFirstElement();
						try {
							if (obj instanceof IProject && ((IProject) obj).hasNature(TDINature.TDI_NATURE_ID)) {
								event.detail = DND.DROP_COPY;
								if(event.item != null && event.item.getData() instanceof IFile)
									targetServer = (IFile) event.item.getData();
							}
						} catch (CoreException e) {
							SystemFunctions.doNothing();
						}
					}
				}
			}

			@Override
			public void drop(DropTargetEvent event) {
				if (LocalSelectionTransfer.getTransfer().isSupportedType(event.currentDataType)) {
					ISelection sel = LocalSelectionTransfer.getTransfer().getSelection();
					if (sel instanceof IStructuredSelection) {
						Object obj = ((IStructuredSelection) sel).getFirstElement();
						try {
							if (obj instanceof IProject && ((IProject) obj).hasNature(TDINature.TDI_NATURE_ID)) {
								ExportRuntimeWizard wiz = new ExportRuntimeWizard();
								wiz.init(PlatformUI.getWorkbench(), (IStructuredSelection) sel);
								if (targetServer != null)
									wiz.setTargetServer(targetServer);
								WizardDialog dlg = new WizardDialog(getShell(), wiz);
								dlg.open();
							}
						} catch (Exception e) {
							SystemFunctions.doNothing();
						}
					}
				}
				super.drop(event);
			}

		};
		tree.addDropSupport(DND.DROP_COPY, new Transfer[] { LocalSelectionTransfer.getTransfer() }, dtl);

		ColumnViewerToolTipSupport.enableFor(tree);

		//
		// -- Double click server opens server document
		//
		tree.addOpenListener(new IOpenListener() {
			public void open(OpenEvent event) {
				if (getSelectedObject() instanceof IFile)
					openDashboardOrServerDocument();
				else if (getSelectedObject() instanceof RunningCI)
					startALAction.run();
				else if (getSelectedObject() instanceof RunningAL)
					viewLogAction.run();
			}
		});

		//
		// -- Update action enabled status on selection change
		//
		tree.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateActions();
			}
		});

		//
		// -- Forward selection changes to workbench
		//
		getSite().setSelectionProvider(tree);

		//
		// -- Create drop down menu for the tree
		//
		MenuManager mm = new MenuManager();
		tree.getControl().setMenu(mm.createContextMenu(tree.getControl()));

		//
		// -- Create action objects
		//
		createActionObjects();

		//
		// -- Populate the context menu
		// 
		IAction[] actions = new IAction[] { startAction, stopAction, refreshAction, viewLogAction, startConfigAction,
				startALAction, null, importAction, exportAction, null, editSSS, browseStore, null, attachDebugAction, debugAction,
				null, launchAMCAction, openDashboard, null, openServer, renameServer, deleteAction, null, showCompAction, };

		for (IAction a : actions) {
			if (a == null) {
				mm.add(new Separator());
			} else {
				mm.add(new ActionContributionItem(a) {
					@Override
					public boolean isDynamic() {
						return true;
					}
				});
				if (a.getActionDefinitionId() != null)
					new CommandHandlerProxy(getSite(), a);
			}
		}

		//
		// -- Show a few actions in the toolbar
		//
		Composite toolbar = new Composite(form.getHead(), SWT.NONE);
		toolbar.setLayout(new GridLayout(99, false));

		buttons = new HashMap<String, Button>();

		IAction[] toolitems = new IAction[] { newAction, startAction, stopAction, refreshAction, viewLogAction };
		for (IAction action : toolitems) {
			Button button = new Button(toolbar, SWT.PUSH);
			button.setText(action.getText());
			button.setData("action", action);
			button.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					((IAction) ((Button) e.widget).getData("action")).run();
				}
			});
			buttons.put(action.getActionDefinitionId(), button);

			action.addPropertyChangeListener(new IPropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent event) {
					if (event.getProperty().equals("enabled")) {
						IAction a = (IAction) event.getSource();
						if (buttons.get(a.getActionDefinitionId()) != null)
							buttons.get(a.getActionDefinitionId()).setEnabled(a.isEnabled());
					}
				}
			});
		}
		form.setHeadClient(toolbar);

		startAction.setEnabled(false);
		stopAction.setEnabled(false);
		debugAction.setEnabled(false);
		viewLogAction.setEnabled(false);
		showCompAction.setEnabled(false);
		launchAMCAction.setEnabled(false);
		openDashboard.setEnabled(false);
		deleteAction.setEnabled(false);
		editSSS.setEnabled(false);
		renameServer.setEnabled(false);
		browseStore.setEnabled(false);

		//
		// -- Check if a server is added/deleted
		//
		IResourceChangeListener resourceListener = new IResourceChangeListener() {
			public void resourceChanged(final IResourceChangeEvent event) {
				UIJob job = new UIJob("") {
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) {
						try {
							event.getDelta().accept(new IResourceDeltaVisitor() {
								public boolean visit(IResourceDelta delta) throws CoreException {
									IFile file = null;
									if (delta.getResource() instanceof IFile)
										file = (IFile) delta.getResource();
									if (file == null || !XT.equals(file.getFileExtension()))
										return true;

									switch (delta.getKind()) {
									case IResourceDelta.ADDED:
										// Refresh tree and schedule a session connect
										tree.refresh();
										refreshServer(file);
										break;
										
									case IResourceDelta.REMOVED:
										// Refresh tree
										tree.refresh();
										break;
										
									case IResourceDelta.CHANGED:
										// Remove current session and schedule a refresh
										removeSessionEntry(file);
										tree.refresh();
										refreshServer(file);
										break;
										
									}
									return true;
								}
							});
						} catch (Exception e) {
							if (tree != null && tree.getTree() != null && !tree.getTree().isDisposed()) {
								tree.refresh();
								refreshServers();
							}
						}
						return Status.OK_STATUS;
					}
				};
				job.schedule();
			}
		};
		ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceListener);

		startTimedRefreshJob();
	}

	/**
	 * Starts the timed refresh job to refresh server status every 15 seconds.
	 */
	private void startTimedRefreshJob() {
		timedRefreshJob = new Job("") {
			protected IStatus run(IProgressMonitor monitor) {
				refreshServers();
				return Status.OK_STATUS;
			}
		};

		// -- poll status every 15 seconds for server availability
		timedRefreshJob.schedule();
		timedRefreshJob.addJobChangeListener(new JobChangeAdapter() {
			public void done(IJobChangeEvent event) {
				super.done(event);
				if (timedRefreshJob != null) {
					timedRefreshJob.schedule(15 * 1000);
				}
			}
		});
	}

	/**
	 * Create action objects used in the context menu and the toolbar
	 */
	private void createActionObjects() {
		newAction = new Action() {
			public String getText() {
				return Messages.getString("action.label.13");
			}

			public void run() {
				NewServerWizard wiz = new NewServerWizard();
				try {
					wiz.init(getSite().getWorkbenchWindow().getWorkbench(), new StructuredSelection(Utils
							.getTDIServersProject(true)));
					WizardDialog dlg = new WizardDialog(getShell(), wiz);
					dlg.open();
				} catch (Exception e) {
					EclipseAppender.logerror(e.toString(), e, getShell());
				}
			}

			public String getActionDefinitionId() {
				return CommandID.SERVER_VIEW_ADD_SERVER;
			}
		};

		deleteAction = new Action() {
			public String getText() {
				return Messages.getString("ServerView.file.delete");
			}

			public void run() {
				try {
					IFile file = (IFile) ((IStructuredSelection) tree.getSelection()).getFirstElement();
					if (MessageDialog.openConfirm(getShell(), Messages.getString("general.delete.label"), file.getName()))
						file.delete(true, null);
				} catch (Exception e) {
					EclipseAppender.logerror(e.toString(), e, getShell());
				}
			}

			public String getActionDefinitionId() {
				return CommandID.SERVER_VIEW_DELETE_SERVER;
			}
		};

		browseStore = new Action() {
			public String getText() {
				return Messages.getString("ServerView.browse.systemstore");
			}

			public void run() {
				IFile resource = (IFile) getSelectedFile();
				if (resource == null)
					return;

				IEditorInput fei = SystemStoreEditor.createEditorInput(resource);
				try {
					getSite().getPage().openEditor(fei, SystemStoreEditor.EDITOR_ID, true,
							IWorkbenchPage.MATCH_ID | IWorkbenchPage.MATCH_INPUT);
				} catch (PartInitException e) {
					EclipseAppender.logerror(e.toString(), e);
				}
			}

			@Override
			public String getActionDefinitionId() {
				return CommandID.SERVER_VIEW_BROWSE_SS;
			}
		};

		editSSS = new Action() {
			public String getText() {
				return Messages.getString("action.label.19");
			}

			public void run() {
				IFile resource = (IFile) getSelectedFile();
				if (resource == null)
					return;
				FileEditorInput fei = new FileEditorInput(resource);
				try {
					getSite().getPage().openEditor(fei, ConfigSettingsEditor.ID, true,
							IWorkbenchPage.MATCH_ID | IWorkbenchPage.MATCH_INPUT);
				} catch (PartInitException e) {
					EclipseAppender.logerror(e.toString(), e);
				}
			}

			@Override
			public String getActionDefinitionId() {
				return CommandID.SERVER_VIEW_EDIT_SS;
			}
		};

		renameServer = new Action() {
			public String getText() {
				return Messages.getString("ServerView.file.rename");
			}

			public void run() {
				RenameResourceAction rra = new RenameResourceAction();
				rra.setSelection(new StructuredSelection(getSelectedFile()));
				rra.run(this);
			}

			public String getActionDefinitionId() {
				return "org.eclipse.ui.edit.rename";
			}
		};

		startAction = new Action() {
			public String getText() {
				return Messages.getString("serverview.start");
			}

			public void run() {
				startServer();
			}

			public String getActionDefinitionId() {
				return CommandID.SERVER_VIEW_START;
			}
		};

		stopAction = new Action() {
			public String getText() {
				return Messages.getString("serverview.stop");
			}

			public void run() {
				stopSelectedObject();
			}

			public String getActionDefinitionId() {
				return CommandID.SERVER_VIEW_STOP;
			}
		};

		refreshAction = new Action() {
			public String getText() {
				return Messages.getString("serverview.refresh");
			}

			public void run() {
				refreshServers();
			}

			public String getActionDefinitionId() {
				return CommandID.SERVER_VIEW_REFRESH;
			}
		};

		viewLogAction = new Action() {
			public String getText() {
				return Messages.getString("ServerView.viewlog");
			}

			public void run() {
				attachAssemblyLine(false);
			}

			public String getActionDefinitionId() {
				return CommandID.SERVER_VIEW_LOG;
			}
		};

		debugAction = new Action() {
			public String getText() {
				return Messages.getString("action.label.18");
			}

			public void run() {
				openServerDebugger();
			}

			public String getActionDefinitionId() {
				return CommandID.SERVER_VIEW_DEBUG;
			}
		};

		showCompAction = new Action() {
			public String getText() {
				return Messages.getString("actions.viewservercomponents");
			}

			public void run() {
				showInstalledComponents();
			}

			public String getActionDefinitionId() {
				return CommandID.SERVER_VIEW_SHOW_COMP;
			}
		};

		attachDebugAction = new Action() {
			public String getText() {
				return Messages.getString("serverview.attach.debugger");
			}

			public void run() {
				attachAssemblyLine(true);
			}

			public String getActionDefinitionId() {
				return CommandID.SERVER_VIEW_ATTACH_DEBUG;
			}
		};

		launchAMCAction = new Action() {
			public String getText() {
				return Messages.getString("serverview.launchAMC");
			}

			public void run() {
				Object obj = ((IStructuredSelection) tree.getSelection()).getFirstElement();
				if (obj instanceof IFile)
					launchAMC((IFile) obj);
			}

			public String getActionDefinitionId() {
				return CommandID.SERVER_VIEW_AMC;
			}
		};

		openDashboard = new Action() {
			public String getText() {
				return Messages.getString("serverview.dashboard");
			}

			public void run() {
				Object obj = ((IStructuredSelection) tree.getSelection()).getFirstElement();
				if (obj instanceof IFile)
					openDashboardEditor((IFile) obj);
			}

			public String getActionDefinitionId() {
				return CommandID.SERVER_VIEW_DASHBOARD;
			}
		};

		openServer = new Action() {

			@Override
			public String getText() {
				return Messages.getString("open.configuration.title");
			}

			@Override
			public void run() {
				openServerDocument();
			}

		};

		importAction = new Action() {
			public String getText() {
				return Messages.getString("ServerView.import.config");
			}

			public void run() {
				OpenTDIConfigFile cfg = new OpenTDIConfigFile();
				cfg.setSelection(tree.getSelection());
				cfg.run(null);
			}
			@Override
			public boolean isEnabled() {
				return super.isEnabled() && getSession(getSelectedFile()) != null;
			}
			
		};

		exportAction = new Action() {
			public String getText() {
				return Messages.getString("ServerView.export.config");
			}

			public void run() {
				ExportRuntimeWizard wiz = new ExportRuntimeWizard();
				wiz.init(PlatformUI.getWorkbench(), (IStructuredSelection) tree.getSelection());
				WizardDialog dlg = new WizardDialog(getShell(), wiz);
				dlg.open();
			}
			@Override
			public boolean isEnabled() {
				return super.isEnabled() && getSession(getSelectedFile()) != null;
			}
			
		};

		startConfigAction = new Action() {
			public String getText() {
				return Messages.getString("ServerView.start.config");
			}

			public void run() {
				try {
					Session sess = getSession(getSelectedFile());
					ListSelectionDialog ld = new ListSelectionDialog(getShell(), sess.listAllConfigurations(),
							new ArrayContentProvider(), new LabelProvider(), getText());
					if (ld.open() == Window.OK && ld.getResult().length > 0) {
						for (Object obj : ld.getResult())
							sess.startConfigInstance(sess.getConfigFolderPath() + "/" + obj.toString());
					}
				} catch (Exception e) {
					EclipseAppender.logerror(e.toString(), e, getShell());
				}
			}

			@Override
			public boolean isEnabled() {
				return super.isEnabled() && getSession(getSelectedFile()) != null;
			}
			
			
		};

		startALAction = new Action() {
			public String getText() {
				return Messages.getString("ServerView.start.assemblyline");
			}

			public void run() {
				try {

					ConfigInstance ci = ((RunningCI) ((IStructuredSelection) tree.getSelection()).getFirstElement()).ci;
					if (ci != null) {
						ListSelectionDialog ld = new ListSelectionDialog(getShell(), ci.getAssemblyLineNames(),
								new ArrayContentProvider(), new LabelProvider(), getText());
						if (ld.open() == Window.OK && ld.getResult().length > 0) {
							for (Object obj : ld.getResult())
								ci.startAssemblyLine(obj.toString());
						}
					}
				} catch (Exception e) {
					EclipseAppender.logerror(e.toString(), e, getShell());
				}
			}
		};

	}

	protected void openDashboardEditor(IFile file) {
		// For now, try opening in a separate browser instead of inside Eclipse.
		
//		try {
//			getSite().getPage().openEditor(new FileEditorInput(file), DashboardEditor.EDITOR_ID, true, 
//					IWorkbenchPage.MATCH_ID | IWorkbenchPage.MATCH_INPUT);
//		} catch (PartInitException e) {
//			EclipseAppender.logerror(e.toString(), e, getShell());
//		}
		
		String url = null;
		try {
			RMIServerAPI api = (RMIServerAPI) RestServerAPI.createInstance(file);
			String port = api.getSession().getJavaProperty("web.server.port");
			url = "https://" + api.getSession().getServerInfo().getHostName() + ":" + port + DASHBOARD;
		} catch (Exception e) {
			EclipseAppender.logerror(e.getLocalizedMessage(), e);
		}
		if (url == null) {
			// Unable to establish a session, try reading from the server config file.
			try {
				BaseConfiguration bc = TDIConfigurationFile.loadFile(file).getDefaultConfigObject();
				url = bc.getStringParameter(RMIServerAPI.TDI_ADDRESS);
				if(url.indexOf(":") != -1)
					url = url.substring(0, url.indexOf(":"));
				url = "https://" + url + ":1098" + DASHBOARD;
			} catch (Exception e) {
				EclipseAppender.logerror(e.getLocalizedMessage(), e, getShell());
				return;
			}
		}
		ConfigUtils.showURL(url);
	}

	/**
	 * Returns the currently selected object or null if selection is empty
	 */
	private Object getSelectedObject() {
		IStructuredSelection sel = (IStructuredSelection) tree.getSelection();
		if (sel.isEmpty())
			return null;
		else
			return sel.getFirstElement();
	}

	/**
	 * Returns the currently selected IFile object or null if current selection
	 * is empty or not an IFile object.
	 * 
	 * @return
	 */
	protected IFile getSelectedFile() {
		Object obj = getSelectedObject();
		if (obj instanceof IFile)
			return (IFile) obj;
		else
			return null;
	}

	/**
	 * Opens the dashboard if the server is running or the server editor if not.
	 */
	protected void openDashboardOrServerDocument() {
		IFile file = getSelectedFile();
		if (file != null) {
			if(isServerRunning(file)) {
				openDashboardEditor(file);
			} else {
				openServerDocument();
			}
		}		
	}
	
	/**
	 * Opens the currently selected server file
	 */
	protected void openServerDocument() {
		IFile file = getSelectedFile();
		if (file != null) {
			try {
				getSite().getPage().openEditor(new FileEditorInput(file), ServerEditor.ID, true, 
						IWorkbenchPage.MATCH_ID | IWorkbenchPage.MATCH_INPUT);
			} catch (PartInitException e) {
				EclipseAppender.logerror(e.toString(), e, getShell());
			}
		}
	}

	/**
	 * Updates the enabled property on all action objects.
	 */
	protected void updateActions() {
		Object st = getSelectedObject();
		if (st == null) {
			startAction.setEnabled(false);
			stopAction.setEnabled(false);
			debugAction.setEnabled(false);
			viewLogAction.setEnabled(false);
			showCompAction.setEnabled(false);
			attachDebugAction.setEnabled(false);
			launchAMCAction.setEnabled(false);
			deleteAction.setEnabled(false);
			editSSS.setEnabled(false);
			renameServer.setEnabled(false);
			browseStore.setEnabled(false);
			openServer.setEnabled(false);
			startALAction.setEnabled(false);
			startConfigAction.setEnabled(false);
		} else {
			if (st instanceof IFile) {
				IFile server = (IFile) st;
				startAction.setEnabled(!isServerRunning(server) && canStartServer(server));
				launchAMCAction.setEnabled(isAMCInstalled(server));
				viewLogAction.setEnabled(isServerLocal(server));
				stopAction.setEnabled(isServerRunning(server));
				openDashboard.setEnabled(isServerRunning(server));
				debugAction.setEnabled(isServerRunning(server));
				showCompAction.setEnabled(debugAction.isEnabled());
				attachDebugAction.setEnabled(false);
				deleteAction.setEnabled(true);
				editSSS.setEnabled(isServerLocal(server) || isServerRunning(server));
				renameServer.setEnabled(true);
				browseStore.setEnabled(isServerLocal(server) || isServerRunning(server));
				openServer.setEnabled(true);
				startALAction.setEnabled(false);
				startConfigAction.setEnabled(true);
			} else {
				stopAction.setEnabled(true);
				viewLogAction.setEnabled(st instanceof RunningAL);
				attachDebugAction.setEnabled(st instanceof RunningAL);
				debugAction.setEnabled(false);
				showCompAction.setEnabled(false);
				launchAMCAction.setEnabled(false);
				deleteAction.setEnabled(false);
				editSSS.setEnabled(false);
				renameServer.setEnabled(false);
				browseStore.setEnabled(false);
				openServer.setEnabled(false);
				startALAction.setEnabled(st instanceof RunningCI);
				startConfigAction.setEnabled(false);
				startAction.setEnabled(st instanceof RunningCI);
			}
		}
	}

	/**
	 * Returns true if AMC is installed for the specified server
	 * 
	 * @param server
	 * @return
	 */
	private boolean isAMCInstalled(IFile server) {
		try {
			RestServerAPI api = RestServerAPI.createInstance(server);
			return new File(api.getInstall() + "/bin/amc/launchAMC.html").exists();
		} catch (Exception err) {
			EclipseAppender.logerror(err.toString(), err, getShell());
		}
		return false;
	}

	/**
	 * Launches the AMC URL for the specified server
	 * 
	 * @param server
	 */
	private void launchAMC(IFile server) {
		try {
			RestServerAPI api = RestServerAPI.createInstance(server);
			File logFile = new File(api.getInstall() + "/bin/amc/launchAMC.html");
			IFileStore fileStore = EFS.getLocalFileSystem().fromLocalFile(logFile);
			if (!fileStore.fetchInfo().isDirectory() && fileStore.fetchInfo().exists()) {
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				IDE.openEditorOnFileStore(page, fileStore);
			}
		} catch (Exception err) {
			EclipseAppender.logerror(err.toString(), err, getShell());
		}
	}

	/**
	 * Opens the server log file in a text editor
	 * 
	 * @param server
	 */
	private void openServerLog(IFile server) {
		RestServerAPI api;
		try {
			api = RestServerAPI.createInstance(server);
			File logFile = new File(api.getWorkdir() + "/logs/ibmdi.log");
			IFileStore fileStore = EFS.getLocalFileSystem().fromLocalFile(logFile);
			if (!fileStore.fetchInfo().isDirectory() && fileStore.fetchInfo().exists()) {
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				IDE.openEditorOnFileStore(page, fileStore);
			}
		} catch (Exception err) {
			EclipseAppender.logerror(err.toString(), err, getShell());
		}
	}

	/**
	 * Returns true if the specified server has a working and install directory
	 * specified.
	 * 
	 * @param server
	 * @return
	 */
	private boolean isServerLocal(IFile server) {
		RestServerAPI api;
		try {
			api = RestServerAPI.createInstance(server);
			String wk = api.getWorkdir();
			String id = api.getInstall();
			return (wk != null && wk.trim().length() > 0 && id != null && id.trim().length() > 0);
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Returns true if we are able to launch a server.
	 * 
	 * @param server
	 * @return
	 */
	private boolean canStartServer(IFile server) {
		
		if (getServerStatus(server) instanceof StartLocalServerJob)
			return false;
		
		RestServerAPI api;
		try {
			api = RestServerAPI.createInstance((IFile) server);
			if (api.getInstall() == null || api.getInstall().length() == 0)
				return false;
			String address = api.getAddress();
			if (address == null || address.length() == 0 || address.indexOf(':') == -1)
				return false;
			if (address.indexOf("//") > 0) {
				// try to handle the case with a url like address
				address = address.substring(address.lastIndexOf('/') + 1);
			}
			int i = address.lastIndexOf(':');
			if ( i < 0 )
				return false;
			address = address.substring(0, i); //Remove port number
			return isHostLocalAddress(address);
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e);
			return false;
		}
	}

	/**
	 * Return true if the address is local to this host
	 * @param address
	 * @return
	 */
	private static boolean isHostLocalAddress(String address) {
		if ("localhost".equals(address))
			return true;
		try {
			InetAddress ia = InetAddress.getByName(address);
			Enumeration<NetworkInterface> e1 = NetworkInterface.getNetworkInterfaces();
			while (e1.hasMoreElements()) {
				Enumeration<InetAddress> e2 = e1.nextElement().getInetAddresses();
				while (e2.hasMoreElements()) {
					if (ia.equals(e2.nextElement()))
						return true;
				}	
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Starts a thread to refresh the server status for the specified server.
	 * 
	 * @param file
	 */
	public void refreshServer(IFile file) {
		new RefreshStatus(file).schedule();
	}

	/**
	 * Starts a refresh job for each server in our list
	 */
	protected void refreshServers() {
		try {
			for (IResource res : Utils.getTDIServersProject(true).members()) {
				if (XT.equals(res.getFileExtension()))
					new RefreshStatus((IFile) res).schedule();
			}
		} catch (Exception e) {
			SystemFunctions.doNothing();
		}
	}

	/**
	 * Shows the installed components for the currently selected server.
	 */
	protected void showInstalledComponents() {
		Object obj = ((IStructuredSelection) tree.getSelection()).getFirstElement();
		if (obj instanceof IFile) {
			ViewServerComponents vsc = new ViewServerComponents();
			vsc.selectionChanged(showCompAction, new StructuredSelection(obj));
			vsc.run(showCompAction);
		}
	}

	/**
	 * Starts the currently selected server.
	 */
	protected void startServer() {
		Object obj = ((IStructuredSelection) tree.getSelection()).getFirstElement();
		if (obj instanceof IFile) {
			if (isServerRunning((IFile) obj))
				startConfigAction.run();
			else
				startServer((IFile) obj);
		} else if (obj instanceof RunningCI) {
			startALAction.run();
		}
	}

	/**
	 * Stops the currently selected object
	 */
	protected void stopSelectedObject() {
		Object obj = ((IStructuredSelection) tree.getSelection()).getFirstElement();
		if (obj instanceof IFile) {
			try {
				RestServerAPI api = RestServerAPI.createInstance((IFile) obj);
				try {
					api.ping();
				} catch (Exception e) {
					return;
				}

				String prompt = Messages.getMessage("StartLocalServerAction.localserverrunning", api.getAddress()); //$NON-NLS-1$
				if (MessageDialog.openQuestion(getShell(), Messages.getString("StartLocalServerAction.stop"), prompt)) { //$NON-NLS-1$
					api.stopServer();
					setServerStatus((IFile) obj, null);
					new RefreshStatus((IFile) obj).schedule(3000);
				}
			} catch (Exception e) {
				EclipseAppender.logerror(e.toString(), e, getShell());
			}

		} else if (obj instanceof RunningCI) {
			try {
				//TODO: Use a better message
				String msg = Messages.getString("serverview.stop") + "?\n" + ((RunningCI) obj).getConfigId();
				if (MessageDialog.openConfirm(getShell(), stopAction.getText(), msg)) {
					((RunningCI) obj).stop();
				}
			} catch (Exception e) {
				EclipseAppender.logerror(e.toString(), e, getShell());
			}

		} else if (obj instanceof RunningAL) {
			try {
				//TODO: Improve the message. Must wait until we are allowed to add new Messages
				String msg = Messages.getString("GotoComponent.assemblyline.stop") + "?\n" + ((RunningAL) obj).getName();
				if (MessageDialog.openConfirm(getShell(), stopAction.getText(), msg)) {
					((RunningAL) obj).stop();
				}
			} catch (Exception e) {
				EclipseAppender.logerror(e.toString(), e, getShell());
			}

		}
	}

	/**
	 * Opens the server debugger editor for the selected server.
	 */
	protected void openServerDebugger() {
		Object obj = ((IStructuredSelection) tree.getSelection()).getFirstElement();
		if (obj instanceof IFile) {
			final IFile file = (IFile) obj;
			final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			UIJob job = new UIJob(file.getName()) {
				public IStatus runInUIThread(IProgressMonitor monitor) {
					try {
						page.openEditor(new FileEditorInput(file), ConfigInstanceEditor.EDITOR_ID, true,
								IWorkbenchPage.MATCH_ID|IWorkbenchPage.MATCH_INPUT);
					} catch (PartInitException e) {
						return EclipseAppender.statusException(e);
					}
					return Status.OK_STATUS;
				}
			};
			job.schedule();
		}
	}

	/**
	 * Attaches to the currently selected assemblyline. If debug is specified we
	 * attach the debugger, otherwise we only attach to the logger.
	 * 
	 * @param debug
	 */
	protected void attachAssemblyLine(boolean debug) {
		Object obj = getSelectedObject();
		if (obj instanceof RunningAL) {
			RunningAL ral = (RunningAL) obj;
			try {
				RunningCI rci = ral.parent;
				String cid = rci.getConfigId();
				String alid = String.valueOf(ral.getUniqueCode());
				RunAssemblyLineInput input = new RunRemoteAssemblyLineInput(rci.server, cid, alid, debug);
				IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), input,
						RunAssemblyLineEditor.EDITOR_ID, true);
			} catch (Exception e1) {
				EclipseAppender.logerror(e1.toString(), e1, getShell());
			}
		} else if (obj instanceof IFile) {
			openServerLog((IFile) obj);
		}
	}

	@Override
	public void setFocus() {
	}

	/**
	 * Label provider for elements returned by ServerContentProvider.
	 */
	private class ServerLabelProvider extends CellLabelProvider {

		public Image getImage(Object element) {
			if (element instanceof IFile) {
				if (isSessionAlive((IFile) element))
					return Activator.getImage("Run");

				Object obj = getServerStatus((IFile) element);
				if (obj instanceof StartLocalServerJob || obj instanceof JobCL)
					return startingImage;
				else
					return Activator.getImage("Stop");

			} else if (element instanceof RunningAL) {
				return Activator.getImage("AssemblyLine");

			} else if (element instanceof RunningCI) {
				return Activator.getImage("Neo_16");

			}
			return null;
		}

		public String getText(Object element) {
			try {
				if (element instanceof IFile) {
					String name = ((IFile) element).getName();
					if (name.indexOf(".tdiserver") != -1)
						name = name.substring(0, name.lastIndexOf(".tdiserver"));

					return name;
				}
				return "" + element;
			} catch (Exception e) {
				EclipseAppender.logerror(e.toString(), e);
				return e.getLocalizedMessage();
			}
		}

		public void addListener(ILabelProviderListener listener) {
		}

		public void dispose() {
		}

		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
		}

		@Override
		public void update(ViewerCell cell) {
			cell.setText(getText(cell.getElement()));
			cell.setImage(getImage(cell.getElement()));
		}

		@Override
		public String getToolTipText(Object element) {
			if (element instanceof IFile) {
				Object obj = getServerStatus((IFile) element);
				if (obj instanceof Exception) {
					String msg = ((Exception) obj).getLocalizedMessage();
					if (msg == null)
						msg = ((Exception) obj).getMessage();
					if (msg == null)
						msg = obj.toString();
					if (msg == null)
						return null;
					return msg.replaceAll("\\s+", " ");
				}
				return null;
			}
			try {
				if (element instanceof RunningAL) {
					AssemblyLine al = ((RunningAL) element).getAL();
					if (al != null)
						return al.getStatistics().getMsg();
				} else if (element instanceof RunningCI) {
					return ((RunningCI) element).ci.getConfigPath() + " @ " + ((RunningCI) element).ci.getInstanceBootTime();
				}
			} catch (Exception e) {
				return null;
			}
			return null;
		}
	}

	/**
	 * Model for active servers, config instances and assemblylines. Since the
	 * server api returns a new object for every get we have to keep a list of
	 * identifiers to correctly insert/remove objects in the model.
	 */
	private class ServerContentProvider implements ITreeContentProvider {

		private HashMap<IFile, ArrayList<RunningCI>> ciList = new HashMap<IFile, ArrayList<RunningCI>>();
		private HashMap<String, ArrayList<RunningAL>> alList = new HashMap<String, ArrayList<RunningAL>>();

		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof IProject) {
				ArrayList<IResource> list = new ArrayList<IResource>();
				try {
					for (IResource res : ((IProject) parentElement).members()) {
						if (XT.equalsIgnoreCase(res.getFileExtension()))
							list.add(res);
					}
				} catch (Exception e) {
					EclipseAppender.logerror(e.toString(), e);
				}
				return list.toArray();

			} else if (parentElement instanceof IFile) {
				Session sess = getSession((IFile) parentElement);
				ArrayList<RunningCI> list = new ArrayList<RunningCI>();
				ciList.put((IFile) parentElement, list);
				if (sess != null) {
					try {
						for (ConfigInstance ci : sess.getConfigInstances()) {
							RunningCI rci = new RunningCI((IFile) parentElement, ci);
							if (!list.contains(rci))
								list.add(rci);
						}
					} catch (Exception e) {
						EclipseAppender.logerror(e.toString(), e);
					}
				}
				return list.toArray();

			} else if (parentElement instanceof RunningCI) {
				final RunningCI rci = (RunningCI) parentElement;
				ConfigInstance ci = rci.ci;
				ArrayList<RunningAL> list = new ArrayList<RunningAL>();
				alList.put(rci.getConfigId(), list);
						
				AssemblyLine[] als;
				try {
					als = ci.getAssemblyLines();
				} catch (Exception e) {
					EclipseAppender.logerror(e.toString(), e);
					new UIJob(rci.id) {
						public IStatus runInUIThread(IProgressMonitor monitor) {
							try {
								removeCI(rci.server, rci.id);
							} catch (Exception e) {
								EclipseAppender.logerror(e.toString(), e);							
							}
							return Status.OK_STATUS;
						}
					}.schedule(100);
					return new Object[0];
				}
				Hashtable<String, Boolean> dupNames = new Hashtable<String, Boolean>();
				try {
					for (AssemblyLine al : als) {
						RunningAL ral = new RunningAL(rci, al);
						if (!list.contains(ral)) {
							list.add(ral);
							String name = ral.getName();
							dupNames.put(name, dupNames.get(name) != null);
						}
					}
					for (RunningAL ral: list) {
						Boolean b = dupNames.get(ral.getName());
						if (b != null && b)
							ral.setUseId();
					}
				} catch (Exception e) {
					EclipseAppender.logerror(e.toString(), e);
				}
				return list.toArray();
			}
			return new Object[] {};
		}

		/**
		 * Remove the config instance from the view.
		 * 
		 * @param server
		 * @param id
		 * @throws Exception
		 */
		public void removeCI(IFile server, String id) throws Exception {
			RunningCI ci = getCI(server, id);
			if (ci != null) {
				ciList.get(server).remove(ci);
				tree.remove(ci);
			} else {
				tree.refresh(server);
			}
		}

		public RunningCI getCI(IFile server, String id) throws Exception {
			ArrayList<RunningCI> list = ciList.get(server);
			if (list != null) {
				for (RunningCI ci : list) {
					if (ci.isConfig(id)) {
						return ci;
					}
				}
			}
			return null;
		}

		public void addCI(IFile server, ConfigInstance ci) {
			ArrayList<RunningCI> list = ciList.get(server);
			if (list == null) {
				list = new ArrayList<RunningCI>();
				ciList.put(server, list);
			}
			try {
				RunningCI rci = new RunningCI(server, ci);
				alList.remove(rci.id);
				if (!list.contains(rci)) {
					list.add(rci);
					tree.add(server, rci);
					if (!tree.getExpandedState(server))
						tree.setExpandedState(server, true);
				}
			} catch (Exception e) {
				// If the config terminates before we have time to get its
				// config id just ignore it
				SystemFunctions.doNothing();
			}
		}

		public void addAL(IFile file, String configID, String name, int id) throws Exception {
			ArrayList<RunningAL> list = alList.get(configID);
			if (list == null) {
				list = new ArrayList<RunningAL>();
				alList.put(configID, list);
			}
			RunningCI rci = getCI(file, configID);
			RunningAL ral = new RunningAL(rci, name, id);
			if (!list.contains(ral)) {
				list.add(ral);
				tree.insert(rci, ral, 0);
				if (!tree.getExpandedState(file))
					tree.setExpandedState(file, true);
				if (!tree.getExpandedState(rci))
					tree.setExpandedState(rci, true);
			}
		}

		/**
		 * Remove the config instance from the view.
		 * 
		 * @param server
		 * @param id
		 * @throws Exception
		 */
		public void removeAL(IFile server, String id, String al, int alid) throws Exception {
			RunningCI ci = getCI(server, id);
			if (ci == null) {
				tree.refresh(server);
			} else {
				ArrayList<RunningAL> list = alList.get(id);
				RunningAL remove = null;
				if (list != null) {
					for (RunningAL ral : list) {
						if (ral.getName().equals(al) && ral.getUniqueCode() == alid) {
							remove = ral;
							break;
						}
					}
				}
				if (remove != null) {
					list.remove(remove);
					tree.remove(ci, new Object[] { remove });
				} else {
					tree.refresh(ci);
				}
			}
		}

		public Object getParent(Object element) {
			if (element instanceof IResource) {
				return ((IResource) element).getParent();
			} else if (element instanceof AssemblyLine) {
				try {
					return ((AssemblyLine) element).getConfigInstance();
				} catch (Exception e) {
					EclipseAppender.logerror(e.toString(), e);
				}
			} else if (element instanceof RunningAL) {
				return ((RunningAL) element).parent;

			} else if (element instanceof RunningCI) {
				return ((RunningCI) element).server;

			}
			return null;
		}

		public boolean hasChildren(Object element) {
			try {
				if (element instanceof IFile) {
					Session sess = getSession((IFile) element);
					return (sess != null && sess.getConfigInstances().length > 0);
				} else if (element instanceof RunningCI) {
					return ((RunningCI) element).ci.getAssemblyLines().length > 0;
				}
			} catch (Exception e) {
				// -- typically due to the session being close or the config
				// inst terminated
				return false;
			}
			return false;
		}

		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

	}

	private class RefreshStatus extends Job {
		private IFile file;

		public RefreshStatus(IFile file) {
			super(file.getName());
			this.file = file;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				if (isSessionAlive(file)) {
					if (!(getServerStatus(file) instanceof Entry))
						setServerStatus(file, this);
					getShell().getDisplay().asyncExec(new Runnable() {
						public void run() {
							tree.refresh(file, true);
						}
					});
					return Status.OK_STATUS;
				}

				// Verify that the address is filled in
				TDIConfigurationFile cfg = TDIConfigurationFile.loadFile(file);
				BaseConfiguration cfgObj = cfg.getDefaultConfigObject();
				if (cfgObj == null || cfgObj.getStringParameter(RestServerAPI.TDI_ADDRESS) == null)
					return Status.CANCEL_STATUS;

				// -- No session yet, try to connect
				RMIServerAPI api = (RMIServerAPI) RMIServerAPI.createInstance(file);
				Entry entry = api.getServerStatus();
				setServerStatus(file, entry);
				updateSessionEventListener(file, api);
			} catch (Exception e) {
				setServerStatus(file, e);
			}
			UIJob ref = new UIJob(getName()) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					if (tree == null || tree.getTree() == null || tree.getTree().isDisposed())
						return Status.CANCEL_STATUS;
					tree.refresh(file);
					tree.expandAll();
					updateActions();
					return Status.OK_STATUS;
				}
			};
			ref.schedule();
			return Status.OK_STATUS;
		}

	}

	public Shell getShell() {
		return getSite().getShell();
	}

	public void startServer(IFile server) {
		startServer(server, true);
	}

	/**
	 * Attempts to start a non-running server. If interactive is true the user
	 * is presented a dialog where he/she can terminate the server it is already
	 * running.
	 * 
	 * @param server
	 * @param interactive
	 */
	public void startServer(final IFile server, boolean interactive) {

		Job job = null;
		try {
			job = new StartLocalServerJob(server);
			setServerStatus(server, job);
		} catch (Exception e) {
			EclipseAppender.logerror(Messages.getString("StartLocalServerAction.start"), e); //$NON-NLS-1$
			return;
		}

		UIJob refresh = new UIJob(server.getName()) {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if (tree == null || tree.getTree() == null || tree.getTree().isDisposed())
					return Status.CANCEL_STATUS;
				updateActions();
				tree.refresh(server);
				tree.expandAll();
				return Status.OK_STATUS;
			}
		};
		refresh.schedule();

		try {
			RestServerAPI api = RestServerAPI.createInstance(server);
			try {
				api.ping();
				if (interactive) {
					String prompt = Messages.getMessage("StartLocalServerAction.localserverrunning", api.getAddress()); //$NON-NLS-1$
					if (MessageDialog.openQuestion(getShell(), Messages.getString("StartLocalServerAction.stop"), prompt)) { //$NON-NLS-1$
						api.stopServer();
					}
				}
				new RefreshStatus(server).schedule(2000);
				return;
			} catch (java.net.ConnectException ignore) {
			}
		} catch (Exception e) {
			EclipseAppender.logerror(Messages.getString("StartLocalServerAction.localserver"), e, getShell()); //$NON-NLS-1$
			return;
		}

		job.addJobChangeListener(new JobCL(server));
		job.schedule();
	}

	private class JobCL extends JobChangeAdapter {

		private IFile server;

		public JobCL(IFile server) {
			this.server = server;
		}

		@Override
		public void done(IJobChangeEvent event) {
			StartLocalServerJob job = (StartLocalServerJob) event.getJob();
			if (job.getStatus() == StartLocalServerJob.SERVER_STOPPED)
				setServerStatus(server, "Stopped");
			RefreshStatus refresh = new RefreshStatus(server);
			setServerStatus(server, refresh);
			refresh.schedule();
		}

	}

	@Override
	public void dispose() {
		if (timedRefreshJob != null) {
			timedRefreshJob.cancel();
			timedRefreshJob = null;
		}
		if (startingImage != null)
			startingImage.dispose();
		if (form != null)
			form.dispose();
		if (tk != null)
			tk.dispose();
		super.dispose();
	}

	/**
	 * Returns true if the server is up and running from a CE perspective.
	 * 
	 * @param server
	 * @return
	 */
	public boolean isServerRunning(IFile server) {
		Object obj = getServerStatus(server);
		if (obj instanceof Entry)
			return true;
		
		if (obj instanceof StartLocalServerJob) {
			return false; //Not running yet
		}

		if (obj instanceof Job) {
			return true;
		}
		
		return isSessionAlive(server);
	}

	/**
	 * Callback from session listener
	 * 
	 * @param event
	 * @throws DIException
	 * @throws RemoteException
	 */
	public void handleEvent(final IFile file, final DIEvent event) throws DIException, RemoteException {
		getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				Session session = getSession(file);
				if (session == null)
					return;
				String type = event.getType();
				AssemblyLineConsole console = getConsole(file);
				ServerContentProvider sp = (ServerContentProvider) tree.getContentProvider();
				try {
					if (type == null || sp == null) {
						SystemFunctions.doNothing();
					} else if (type.equals(DIEvent.EVT_CI_START)) {
						ConfigInstance ci = session.getConfigInstance(event.getConfigInstanceId());
						sp.addCI(file, ci);
					} else if (type.equals(DIEvent.EVT_CI_STOP)) {
						sp.removeCI(file, event.getConfigInstanceId());
					} else if (type.equals(DIEvent.EVT_AL_START)) {
						RunningCI ci = sp.getCI(file, event.getConfigInstanceId());
						if (ci != null) {
							sp.addAL(file, ci.getConfigId(), event.getId(), (Integer) event.getData());
						} else {
							tree.refresh();
						}
					} else if (type.equals(DIEvent.EVT_AL_STOP)) {
						sp.removeAL(file, event.getConfigInstanceId(), event.getId(), (Integer) event.getData());
					} else if (type.equals(DIEvent.EVT_SRV_STOP)) {
						tree.refresh(file);
					}

					if (logStartAndStopMessages && console != null) {
						StringBuffer buf = new StringBuffer();
						// TODO: We should get proper translated started and stopped messages.
						String opLabel = event.getType().endsWith(".start") ? Messages.getString("serverview.start") : Messages
								.getString("serverview.stop");
						buf.append(event.getDateCreated());
						buf.append(" ");

						if (DIEvent.EVT_SRV_STOP.equals(type))
							buf.append(file.getName());
						else
							buf.append(event.getConfigInstanceId());

						if (event instanceof ALEvent) {
							ALEvent alevent = (ALEvent) event;
							buf.append(".");
							String alname = alevent.getId();
							if (alname.startsWith("AssemblyLines/"))
								alname = alname.substring("AssemblyLines/".length());
							buf.append(alname);
							buf.append("(");
							buf.append(event.getData());
							buf.append(") ");
							buf.append(opLabel);
							if (type != null && type.endsWith(".stop")) {
								String msg = alevent.getStatistics().getMsg();
								if (msg.indexOf(":") != -1) {
									buf.append(" - ");
									buf.append(msg);
								}
							}
						} else {
							buf.append(" " + opLabel);
						}
						buf.append("\n");
						console.logmsg(buf.toString());
					}

				} catch (Exception e) {
					EclipseAppender.logerror(e.toString(), e);
				}
			}
		});
	}

	/**
	 * Returns true if we have a working Session object for the specified
	 * server.
	 * 
	 * @param file
	 * @return
	 */
	public boolean isSessionAlive(IFile file) {
		SessionEntry old = sessions.get(file);
		if (old == null || old.status instanceof Exception)
			return false;

		try {
			old.session.isSSLon();
			return true;
		} catch (Exception e) {
			if (! (old.status instanceof StartLocalServerJob))
				removeSessionEntry(file);
			return false;
		}
	}

	/**
	 * Returns the Session object for the specified server or null if no session
	 * is established.
	 * 
	 * @param file
	 * @return
	 */
	public Session getSession(IFile file) {
		if (isServerRunning(file)) {
			SessionEntry entry = sessions.get(file);
			if (entry != null)
				return entry.session;
		}
		return null;
	}

	/**
	 * Returns the console for the specified server
	 * 
	 * @param file
	 *            Server path
	 * @return
	 */
	public AssemblyLineConsole getConsole(IFile file) {
		SessionEntry entry = sessions.get(file);
		AssemblyLineConsole console = null;
		if (entry != null) {
			console = entry.console;
		}

		if (console == null)
			console = AssemblyLineConsole.getConsole(file.getName());

		if (entry != null && console != null)
			entry.console = console;

		return console;
	}

	/**
	 * Creates a listener object for a specific session. We keep the session
	 * object for each active server in this view to avoid repeated connection
	 * attempts to the server. During periodic refresh we do a dummy operation
	 * to verify the connection and recreate the session if the server is
	 * not responding.
	 * 
	 * @param file
	 * @param api
	 */
	private void updateSessionEventListener(final IFile file, RMIServerAPI api) {
		removeSessionEntry(file);
		SessionEntry entry = new SessionEntry();
		try {
			entry.session = api.getSession();
			String workDir = api.getWorkdir();
			if (workDir != null && workDir.length() > 0) {
				String solDir = entry.session.getJavaProperty("user.dir");
				if (solDir != null && solDir.length() > 1 &&
						! new File(solDir).getCanonicalFile().equals(new File(workDir).getCanonicalFile())) {
					if (loggedSolutionError.contains(file))
						return;
					loggedSolutionError.add(file);
					String name = api.getName();
					if (name.endsWith(".tdiserver"))
						name = name.substring(0, name.lastIndexOf(".tdiserver"));
					String errmsg = Messages.getMessage("ServerView.server.no.match", 
							new Object[] {name, api.getAddress(), solDir});
					Exception e = new Exception(errmsg);
					setServerStatus(file, e);
					EclipseAppender.logerror(errmsg, e, getShell());
					return;
				}
			}
			String installDir = api.getInstall();
			if (installDir != null && installDir.length() > 0) {
				String iDir = entry.session.getJavaProperty("com.ibm.di.installdir");
				if (iDir != null && iDir.length() > 1 &&
						! new File(iDir).getCanonicalFile().equals(new File(installDir).getCanonicalFile())) {
					// TODO: do not construct message from parts
					Exception e = new Exception(Messages.getString("ServerWidget.21") + " " + installDir + " != " + iDir);
					setServerStatus(file, e);
					return;
				}
			}
			DIEventListener listener = new DIEventListener() {
				public void handleEvent(DIEvent event) throws DIException, RemoteException {
					ServerView.this.handleEvent(file, event);
				}
			};
			entry.base = DIEventListenerBase.createInstance(listener, api.isSsl());
			entry.session.addEventListener(entry.base, "*", "*");
			sessions.put(file, entry);
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e);
		}
	}

	/**
	 * Removes the session entry and deregisters the listener for a given server
	 * 
	 * @param server
	 */
	private void removeSessionEntry(IFile server) {
		SessionEntry old = sessions.remove(server);
		if (old != null && old.session != null && old.base != null) {
			try {
				old.session.removeEventListener(old.base);
			} catch (Exception e) {
				SystemFunctions.doNothing();
			}
		}
	}

	/**
	 * Updates the status field for a server
	 * 
	 * @param file
	 * @param status
	 */
	private void setServerStatus(IFile file, Object status) {
		SessionEntry entry = sessions.get(file);
		if (entry == null) {
			entry = new SessionEntry();
			sessions.put(file, entry);
		}
		entry.status = status;

	}

	/**
	 * Returns the status field for a server
	 * 
	 * @param file
	 * @return
	 */
	private Object getServerStatus(IFile file) {
		SessionEntry entry = sessions.get(file);
		if (entry != null)
			return entry.status;
		else
			return null;
	}

	/**
	 * Per server session data
	 */
	private static class SessionEntry {
		/**
		 * Session handle
		 */
		public Session session;

		/**
		 * The registered event listener
		 */
		public DIEventListenerBase base;

		/**
		 * The console to receive event messages
		 */
		public AssemblyLineConsole console;

		/**
		 * The status object for the session
		 */
		public Object status;
	}

}
