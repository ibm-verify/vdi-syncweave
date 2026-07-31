/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.editors;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.naming.NameNotFoundException;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.HandlerEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.ide.IGotoMarker;
import org.eclipse.ui.operations.UndoRedoActionGroup;
import org.eclipse.ui.part.EditorActionBarContributor;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;

import com.ibm.di.config.base.InternalSchema;
import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.AttributeMapItem;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.HookConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.di.config.interfaces.MetamergeConfigChangeListener;
import com.ibm.di.function.SystemFunctions;
import com.ibm.tdi.eclipse.Activator;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.TDI;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.views.JavaScriptView;
import com.ibm.tdi.eclipse.widget.SimpleTextEditor;
import com.ibm.tdi.eclipse.wizards.NullValueBehaviorWizard;

/**
 * This is the base class used by TDI editors. It provides configuration file
 * management and a number of platform related tasks such as configuring
 * retargetable actions, responding to external changes to the editor input.
 * 
 */
public abstract class BaseEditor extends EditorPart implements MetamergeConfigChangeListener, IGotoMarker {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * The default configuration object from {@link #file}
	 */
	private BaseConfiguration configuration;

	/**
	 * This is the configuration file being edited.
	 */
	private TDIConfigurationFile file;

	/**
	 * This table contains objects that wants to be called when the editor is
	 * asked to contribute items to various parts of the UI (e.g. status line,
	 * global actions etc).
	 */
	private ArrayList<EditorActionBarContributor> editorContributors = new ArrayList<EditorActionBarContributor>();

	/**
	 * This table contains global actions. Global actions provide
	 * implementations for retargetable actions in the workbench (e.g. Copy,
	 * Paste etc)
	 */
	private HashMap<String, IAction> actionMap = new HashMap<String, IAction>();

	/**
	 * This is the selection provider we provide to the workbench. Child
	 * components typically register themselves as selection providers to the
	 * workbench through the {@link #addSelectionProvider(ISelectionProvider)}.
	 */
	private SelectionProvider selectionProvider;

	/**
	 * 
	 */
	private UndoRedoActionGroup undoRedo;

	/**
	 * This is the javascript editor that lives inside quickEditorForm
	 */
	private SimpleTextEditor editor;

	/**
	 * This is the Form with the title, toolbar and javascript editor
	 */
	private Form quickEditorForm;

	/**
	 * Flag that determines whether we broadcast property changes
	 */
	private boolean notificationsEnabled = true;

	/**
	 * The last registered menu manager
	 */
	private MenuManager menuManager;

	/**
	 * Command handler for null value behavior (quick editor)
	 */
	private NullValueHandler nvbHandler;

	/**
	 * Command handler for close editor (quick editor)
	 */
	private CloseHandler closeHandler;

	/**
	 * Command handler for test script (quick editor)
	 */
	private TestScriptHandler testScriptHandler;

	/**
	 * This list contains the tokens for command handlers when the quick editor
	 * becomes active. When the quick editor closes, we have to notify the
	 * platform that we no longer provide this tokens (command handlers)
	 */
	private ArrayList<IHandlerActivation> tokens = new ArrayList<IHandlerActivation>();

	/**
	 * The listener for modification changes from the quick editor. We don't
	 * update the configuration object for each modification but fire a
	 * PROP_DIRTY to signal that the editor is dirty.
	 */
	private ModifyListener quickEditListener;

	/**
	 * This is the resource change listener that triggers on external changes to
	 * the file we are editing.
	 */
	private IResourceChangeListener resourceListener;

	/**
	 * Every time we load or save the file we are editing we cash the
	 * modification stamp from the IFile object. We do this to track changes to
	 * the file made by other editors/views such as CVS, File/Refresh etc.
	 */
	private long lastModificationStamp;

	/**
	 * If true, the {@link #reloadEditor()} could not be run because this editor
	 * was not active at the time the editor file was changed externally. The
	 * {@link #setFocus()} method checks this flag and calls reloadEditor once
	 * this editor gets focus.
	 */
	private boolean resourceNotificationPending;

	private Label quickEditorTitle;

	private BaseConfiguration backupCopy;

	private boolean verifyInheritanceChange;

	private boolean verifyMapTypeChange;

	private boolean batchChange = false;

	private IMemento memento;

	private WorkspaceJob autoSaveJob;

	public BaseEditor() {
		super();
	}

	/**
	 * Adds an action object to the list of retargetable actions.
	 * 
	 * @param name
	 * @param action
	 */
	public void registerAction(String name, IAction action) {
		if (action != null)
			actionMap.put(name, action);
		else
			actionMap.remove(name);
	}

	/**
	 * Returns the Action object for the given name/id
	 * 
	 * @param name
	 *            The name/id for the command
	 * @return Action object that implements the command
	 */
	public IAction getActionFor(String name) {
		Object action = actionMap.get(name);
		return (IAction) action;
	}

	/**
	 * Returns all registered global action objects
	 */
	public String[] getRegisteredActions() {

		Object[] arr = actionMap.keySet().toArray();
		String[] sarr = new String[arr.length];
		for (int i = 0; i < arr.length; i++)
			sarr[i] = (String) arr[i];
		return sarr;
	}

	/**
	 * Adds the object to the contributor call back list.
	 */
	public void addContributor(EditorActionBarContributor contributor) {
		if (!editorContributors.contains(contributor))
			editorContributors.add(contributor);
	}

	/**
	 * Removes the object from the contributor call back list.
	 */
	public void removeContributor(EditorActionBarContributor contributor) {
		editorContributors.remove(contributor);
	}

	/**
	 * Returns the list of contributors
	 */
	public ArrayList<EditorActionBarContributor> getEditorContributors() {
		return editorContributors;
	}

	/**
	 * Updates global action bars setting the global action handler for each
	 * action in the action list.
	 * 
	 * @see #registerAction(String, IAction)
	 */
	public void updateActionBars() {
		IActionBars bars = getEditorSite().getActionBars();
		if (bars != null) {
			for (String action : getRegisteredActions()) {
				bars.setGlobalActionHandler(action, getActionFor(action));
			}
			bars.updateActionBars();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void doSave(IProgressMonitor monitor) {

		saveQuickEditor();

		if (file != null) {
			try {
				if (resourceListener != null)
					ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceListener);
				file.commitChanges(null, true);
				lastModificationStamp = file.getFile().getModificationStamp();
			} catch (Exception e) {
				EclipseAppender.logerror(Messages.getString("BaseEditor.1"), e, getSite().getShell()); //$NON-NLS-1$
			} finally {
				if (resourceListener != null)
					ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceListener);
			}
		}
		if (configuration != null)
			configuration.setModified(false);

		firePropertyChange(PROP_DIRTY);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
	 */
	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorPart#doSaveAs()
	 */
	@Override
	public void doSaveAs() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.config.interfaces.MetamergeConfigChangeListener#configurationChanged(com.ibm.di.config.interfaces.MetamergeConfigChange)
	 */
	public void configurationChanged(MetamergeConfigChange changeEvent) {
		int op = changeEvent.getOperation();
		if (op == MetamergeConfigChange.BEGIN_CHANGES) {
			batchChange = true;
		} else if (op == MetamergeConfigChange.END_CHANGES) {
			batchChange = false;
		}

		if (batchChange)
			return;

		if (isNotificationsEnabled()) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					firePropertyChange(PROP_DIRTY);
				}
			});
		}
	}

	/**
	 * Sets the modified flag of the editing configuration and fires a prop
	 * dirty event
	 * 
	 * @param modified
	 */
	public void setModified(boolean modified) {
		getTDIConfiguration().setModified(modified);
		firePropertyChange(PROP_DIRTY);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorPart#init(org.eclipse.ui.IEditorSite,
	 *      org.eclipse.ui.IEditorInput)
	 */
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
		try {
			String partName = null;
			if (input instanceof IFileEditorInput) {

				IFile resource = ((IFileEditorInput) input).getFile();

				backupCopy = null;
				try {
					backupCopy = createBackupFromProjectMC(resource);
				} catch (NameNotFoundException nfe) {
					backupCopy = null;
				}

				// -- Make sure file is synchronized
				if (!resource.isSynchronized(1))
					resource.refreshLocal(1, null);

				if (!resource.exists() && backupCopy == null) {
					MessageDialog.openError(getSite().getShell(), Messages.getString("BaseEditor.filedeleted"), resource
							.getFullPath().toPortableString());
					return;
				} else if (!resource.exists()) {
					if (!MessageDialog.openQuestion(getSite().getShell(), Messages.getString("BaseEditor.filedeleted"), Messages
							.getString("BaseEditor.restore")))
						return;
					else
						restoreBackup(backupCopy, resource);
				} else if (getEditorSnapshot(resource).exists()) {
					IFile snap = getEditorSnapshot(resource);
					String ts = new Date(snap.getModificationStamp()).toString();
					String msg = Messages.getMessage("BaseEditor.snapshot", ts);
					if (MessageDialog.openQuestion(getSite().getShell(), Messages.getString("perspective.name.0"), msg)) {
						restoreBackup(TDIConfigurationFile.load(snap), resource);
					}
					backupCopy = null;
				} else {
					backupCopy = null;
				}

				file = TDIConfigurationFile.loadFile(((IFileEditorInput) input).getFile());
				lastModificationStamp = file.getFile().getModificationStamp();
				configuration = file.getDefaultConfigObject();
				setModified(false);

			} else if (input instanceof TDIConfigEditorInput) {
				configuration = ((TDIConfigEditorInput) input).getConfiguration();
				partName = ((TDIConfigEditorInput) input).getTitle();
				file = (TDIConfigurationFile) configuration.getMetamergeConfig();
			} else if (input instanceof RunAssemblyLineInput) {
				RunAssemblyLineInput ral = (RunAssemblyLineInput) input;
				if (ral.isSequence())
					configuration = ral.getSequence();
				else
					configuration = ral.getConfig();
			} else {
				throw new Exception(Messages.getString("BaseEditor.unsupported")); //$NON-NLS-1$
			}
			configuration.addListener(this);

			if (partName == null)
				partName = configuration.getShortName();

			super.setPartName(partName);
		} catch (Exception e) {
			throw new PartInitException(e.toString(), e);
		}

		undoRedo = new UndoRedoActionGroup(site, site.getWorkbenchWindow().getWorkbench().getOperationSupport().getUndoContext(),
				true);

		if (file != null)
			addResourceListener();

		// -- Make find work with the config file
		registerAction(ActionFactory.FIND.getId(), new Action() {
			@Override
			public void run() {
				getSelectionProvider().setSelection(new StructuredSelection(getTDIConfiguration().getMetamergeConfig()));
				NewSearchUI.openSearchDialog(getSite().getWorkbenchWindow(), "com.ibm.tdi.rcp.search");
			}
		});
		
		// -- Autosave
		if(file != null) {
			try {
				final int psi = ResourcesPlugin.getPlugin().getPluginPreferences().getInt(ResourcesPlugin.PREF_SNAPSHOT_INTERVAL);
				if(psi > 0) {
					autoSaveJob = new WorkspaceJob(getPartName()) {
						@Override
						public IStatus runInWorkspace(IProgressMonitor monitor)
								throws CoreException {
							try {
								if(file == null)
									return Status.OK_STATUS;
								
								IFile snap = getEditorSnapshot(file.getFile());
								if(snap.exists())
									snap.delete(true, null);
								TDIConfigurationFile cfg = new TDIConfigurationFile(snap);
								BaseConfiguration obj = (BaseConfiguration) getTDIConfiguration().getClone();
								cfg.setDefaultConfigObject(obj.getShortName(), obj);
								cfg.commitChanges(null, true);
							} catch (Exception e) {
								EclipseAppender.logerror(file.getFile().getName() + ": " + e.toString(), e);
							}
							autoSaveJob.schedule(psi);
							return Status.OK_STATUS;
						}
					};
					autoSaveJob.setRule(file.getProject());
					autoSaveJob.schedule(psi);
				}
				// -- at this stage we either restored the old file or ignore it ... delete it to avoid duplicate questions
				getEditorSnapshot(file.getFile()).delete(true, null);
			} catch (Exception e) {
				SystemFunctions.doNothing();
			}
		}
	}

	/**
	 * Returns the IFile object for the autosave file for this editor. If the editor doesn't have a file object
	 * null is returned. The returned file may or may not exist.
	 * 
	 * @param resource
	 * @return
	 */
	private IFile getEditorSnapshot(IFile resource) {
		IFile snap = null;
		if(resource != null) {
			IContainer parent = resource.getParent();
			if(parent instanceof IFolder)
				snap = ((IFolder)parent).getFile("." + resource.getName() + ".autosave");
			else if(parent instanceof IProject)
				snap = ((IProject)parent).getFile("." + resource.getName() + ".autosave");
			else
				snap = parent.getFile(new Path("." + resource.getProjectRelativePath().toString() + ".autosave"));
		}
		
		return snap;
	}

	/**
	 * Restores the current editor file to the contents of the <i>old</i> object.
	 * 
	 * @param old
	 * @param resource
	 * @throws Exception
	 */
	private void restoreBackup(BaseConfiguration old, IFile resource) throws Exception {
		file = TDIConfigurationFile.loadFile(resource);
		old.setMetamergeConfig(file);
		file.setDefaultConfigObject(old.getShortName(), old);
		file.commitChanges(null, true);
	}

	/**
	 * This method attempts to locate a version of this file in the project's
	 * runtime config file (rs.xml). It is used to restore this contents of this
	 * file if it was unintentionally removed.
	 * 
	 * @param resource
	 * @return null if not found or a cloned copy of the runtime config version
	 * @throws Exception
	 */
	private BaseConfiguration createBackupFromProjectMC(IFile resource) throws Exception {
		MetamergeConfig pmc = Utils.getProjectMC(resource.getProject());
		String folder = TDIConfigurationFile.getFolderForExtension(resource.getFileExtension());
		if (folder == null)
			return null;

		String name = resource.getName();
		name = name.substring(0, name.length() - (resource.getFileExtension().length() + 1));
		BaseConfiguration cached = (BaseConfiguration) pmc.lookup(folder + "/" + name);
		if (cached != null)
			cached = (BaseConfiguration) cached.getClone();

		return cached;
	}

	/**
	 * This method listens for resource changes in the workspace. When the file
	 * associated with this editor changes, we update the object's fields to
	 * reflect the new filename or close the editor in case it was removed.
	 */
	private void addResourceListener() {
		if (resourceListener == null) {
			resourceListener = new IResourceChangeListener() {
				public void resourceChanged(IResourceChangeEvent event) {
					IResourceDelta delta = event.getDelta();
					if (delta != null) {
						IResourceDelta resDelta = delta.findMember(file.getFile().getFullPath());
						if (resDelta == null)
							return;

						if (resDelta.getFlags() == 0 && resDelta.getKind() != IResourceDelta.REMOVED)
							return;

						if(getSite() == null || getSite().getShell() == null)
							return;
						
						Display display = getSite().getShell().getDisplay();
						if(display == null)
							return;

						if ((resDelta.getFlags() & IResourceDelta.MOVED_TO) > 0) {
							final IResource nfile = getTDIConfigProject().getWorkspace().getRoot().getFile(
									resDelta.getMovedToPath());
							display.asyncExec(new Runnable() {
								public void run() {
									try {
										file.setFile((IFile) nfile);
										setPartName(configuration.getShortName());
										setInput(new FileEditorInput((IFile) nfile));
										firePropertyChange(PROP_TITLE);
									} catch (Exception e) {
										EclipseAppender.logerror(e.toString(), e, getSite().getShell());
									}
								}
							});
							return;
						}

						if (resDelta.getKind() == IResourceDelta.REMOVED) {
							// -- Ignore this first notification after a restore
							if (backupCopy != null) {
								return;
							}

							display.asyncExec(new Runnable() {
								public void run() {
									getSite().getPage().closeEditor(BaseEditor.this, false);
								}
							});
						} else if (resDelta.getKind() == IResourceDelta.CHANGED) {
							if (backupCopy != null) {
								backupCopy = null;
								return;
							}
							if (lastModificationStamp != resDelta.getResource().getModificationStamp())
								reloadEditor();
							lastModificationStamp = resDelta.getResource().getModificationStamp();
							display.asyncExec(new Runnable() {
								public void run() {
									firePropertyChange(PROP_DIRTY);
								}
							});
						}
					}
				}
			};
		}
		ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceListener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorPart#isDirty()
	 */
	@Override
	public boolean isDirty() {
		if (getEditorInput() instanceof TDIConfigEditorInput)
			return false;

		if (editor != null && editor.isModified())
			return true;
		else
			return (configuration == null ? false : configuration.getModified());
	}

	@Override
	public void setFocus() {
		if (resourceNotificationPending || 
				(file != null && !file.getFile().isSynchronized(IResource.DEPTH_ZERO)))
			reloadEditor();
	}

	/**
	 * Returns the configuration object for the editor.
	 * 
	 * @return the configuration object for the editor.
	 */
	public BaseConfiguration getTDIConfiguration() {
		return configuration;
	}

	/**
	 * Returns the selection provider for this editor. The selection provider
	 * broadcasts selection changes in various components in the editor. Child
	 * components in the editor should invoke addSelectionProvider() to include
	 * itself in the list of selection providers. The selection provider object
	 * returned by this method will listen for selection changes and broadcast a
	 * selection changed event with itself as the source.
	 * 
	 * @return The selection provider for this editor.
	 */
	public SelectionProvider getSelectionProvider() {
		if (selectionProvider == null)
			selectionProvider = new SelectionProvider();
		return selectionProvider;
	}

	/**
	 * Child components of the editor should add themselves as a selection
	 * provider through this method.
	 * 
	 * @param provider
	 */
	public void addSelectionProvider(ISelectionProvider provider) {
		provider.addSelectionChangedListener(getSelectionProvider());
	}

	/**
	 * Removes a provider from the list of workbench selection providers
	 * 
	 * @param provider
	 */
	public void removeSelectionProvider(ISelectionProvider provider) {
		provider.removeSelectionChangedListener(getSelectionProvider());
	}

	/**
	 * Returns the undo/redo action group
	 * 
	 * @return the UndoRedoActionGroup
	 */
	public UndoRedoActionGroup getUndoRedo() {
		return undoRedo;
	}

	/**
	 * Returns the IFile object of the TDI configuration being edited
	 * 
	 * @return the IFile
	 */
	public IFile getTDIConfigFile() {
		return (file == null ? null : file.getFile());
	}

	/**
	 * Returns the IProject to which this TDI configruration belongs
	 * 
	 * @return the IProject
	 */
	public IProject getTDIConfigProject() {
		return (getTDIConfigFile() == null ? null : getTDIConfigFile().getProject());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		if (memento != null)
			saveMemento();
		if (configuration != null)
			configuration.removeListener(this);
		if (file != null)
			file.closeConfig();
		
		// -- remove the snapshot file in case we successfully close the editor
		if (autoSaveJob != null) {
			autoSaveJob.cancel();
			IFile snap = getEditorSnapshot(file.getFile());
			if(snap != null) {
				try {
					snap.delete(true, null);
				} catch (CoreException e) {
					SystemFunctions.doNothing();
				}
			}
		}
		super.dispose();
	}

	/**
	 * Sub classes should override this method to respond to selection changes
	 * in the workbench. E.g. if an editor can respond to selection changes in a
	 * view for example.
	 * 
	 * @param selection
	 */
	public void setSelection(StructuredSelection selection) {
	}

	/**
	 * TODO: remove this method Used to return the currently selected object in
	 * the editor.
	 * 
	 * @return null
	 */
	public BaseConfiguration getCurrentConfigObject() {
		return null;
	}

	/**
	 * Open the quick editor with the configuration object in the selection
	 * 
	 * @param selection
	 */
	public void quickEdit(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection sel = (IStructuredSelection) selection;
			Object obj = sel.getFirstElement();
			if (obj instanceof BaseConfiguration) {
				quickEdit((BaseConfiguration) obj);
			} else if (obj instanceof HookConfig) {
				quickEdit((HookConfig) obj);
			} else if (obj instanceof Widget) {
				quickEdit((BaseConfiguration) ((Widget) obj).getData("config")); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Opens the quick editor with the script from the provided configuration
	 * 
	 * @param bc
	 */
	protected void quickEdit(BaseConfiguration bc) {
		if (editor == null) {
			EclipseAppender.loginfo(Messages.getString("BaseEditor.7")); //$NON-NLS-1$
			return;
		}

		if (bc instanceof AttributeMapConfig && !(this instanceof AssemblyLineEditor2))
			return;

		if (editor.getEditingConfig() == bc) {
			closeEditor();
			return;
		}

		// -- Save current changes if any
		if (editor.isModified())
			editor.getConfig().setScript(editor.getText());

		String comp = null;
		if (Utils.getParentConfig(bc, ConnectorConfig.class) != null)
			comp = Utils.getParentConfig(bc, ConnectorConfig.class).getShortName();
		String title = bc.getShortName();

		verifyInheritanceChange = false;
		verifyMapTypeChange = false;
		if (bc instanceof AttributeMapItem) {
			AttributeMapItem ami = (AttributeMapItem) bc;
			if (ami.isSimple()) {
				editor.init(ami, Utils.getScript(
						Utils.isInputMap((AttributeMapConfig) ami.getParent()) ? "conn" : "work", ami.getSimple())); //$NON-NLS-1$ //$NON-NLS-2$
			} else if (ami.isSubstitution()) {
				editor.init(ami, ami.getSubstitution());
				verifyMapTypeChange = true;
			} else {
				editor.init(ami, ami.getScript());
				verifyInheritanceChange = !ami.isParameterLocal(InternalSchema.AMI_SCRIPT);
			}
		} else {
			editor.init(bc, bc.getScript());
			verifyInheritanceChange = !bc.isParameterLocal(InternalSchema.SCRIPT);
		}

		if (bc instanceof HookConfig)
			title = "" + ((HookConfig) bc).getHookName(); //$NON-NLS-1$

		if (quickEditorTitle != null) {
			String subtitle;
			title = (comp != null ? comp + "." + title : title);
			if (bc instanceof HookConfig) {
				subtitle = Messages.getMessage("BaseEditor.hook", title);
			} else {
				subtitle = Messages.getMessage("BaseEditor.attribute", title);
			}

			quickEditorTitle.setText(subtitle);
		}

		nvbHandler.setEnabled(editor.getEditingConfig() instanceof AttributeMapItem);
		quickEditorForm.getToolBarManager().update(true);

		showEditor(true);
		editor.setFocus();
	}

	/**
	 * Returns true if the quick editor is visible
	 * 
	 * @return true if the quick editor is visible
	 */
	public boolean isEditorVisible() {
		SashForm sash = Utils.getParentConfig(quickEditorForm, SashForm.class);
		if (sash == null || sash.getWeights()[1] == 0)
			return false;
		else
			return true;
	}

	/**
	 * This will show or hide the quick script editor by setting the weights on
	 * the parent SashForm of the editor that was created via the
	 * createScriptEditor.
	 * 
	 * @param visible
	 */
	protected void showEditor(boolean visible) {
		if (quickEditorForm == null)
			return;

		if (visible)
			editor.getSourceViewer().getTextWidget().addModifyListener(quickEditListener);
		else
			editor.getSourceViewer().getTextWidget().removeModifyListener(quickEditListener);

		SashForm sash = Utils.getParentConfig(quickEditorForm, SashForm.class);
		if (sash != null) {
			int[] weights = (visible ? new int[] { 60, 40 } : new int[] { 100, 0 });
			sash.setWeights(weights);

			//
			// Update handler service
			//
			IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class);
			if (visible) {
				tokens.add(handlerService.activateHandler("com.ibm.tdi.rcp.quickeditor.nvb", nvbHandler)); //$NON-NLS-1$
				tokens.add(handlerService.activateHandler("com.ibm.tdi.rcp.quickeditor.close", closeHandler)); //$NON-NLS-1$
				tokens.add(handlerService.activateHandler("com.ibm.tdi.rcp.testscript", testScriptHandler)); //$NON-NLS-1$
			} else {
				handlerService.deactivateHandlers(tokens);
				tokens.clear();
			}
		}
	}

	/**
	 * Creates the quick editor form for script editing.
	 * 
	 * @param parent
	 * @return The quick editor form
	 */
	protected Composite createScriptEditor(Composite parent) {

		FormToolkit tk = new FormToolkit(parent.getDisplay());
		quickEditorForm = tk.createForm(parent);
		tk.decorateFormHeading(quickEditorForm);

		quickEditorForm.getBody().setLayout(new FillLayout());
		quickEditorForm.setText(Messages.getString("BaseEditor.javascript.editor"));

		quickEditorTitle = new Label(quickEditorForm.getHead(), SWT.LEFT);
		quickEditorForm.setHeadClient(quickEditorTitle);

		editor = new SimpleTextEditor(quickEditorForm.getBody(), SWT.NONE, null);

		nvbHandler = new NullValueHandler();
		closeHandler = new CloseHandler();
		testScriptHandler = new TestScriptHandler();

		quickEditorForm.getToolBarManager().add(nvbHandler);
		quickEditorForm.getToolBarManager().add(closeHandler);

		quickEditorForm.getToolBarManager().update(true);

		MenuManager mm = new MenuManager();
		Menu menu = mm.createContextMenu(editor.getSourceViewer().getTextWidget());
		editor.getSourceViewer().getTextWidget().setMenu(menu);
		mm.add(nvbHandler);
		mm.add(testScriptHandler);
		mm.add(closeHandler);

		editor.addCloseListener(new Listener() {
			public void handleEvent(Event event) {
				closeEditor();
			}
		});

		quickEditListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (verifyMapTypeChange) {
					editor.setModified(false);
					if (confirmMapTypeChange()) {
						editor.setModified(true);
						verifyMapTypeChange = false;
					} else {
						return;
					}
				}
				if (verifyInheritanceChange) {
					editor.setModified(false);
					if (confirmInheritanceChange()) {
						editor.setModified(true);
						verifyInheritanceChange = false;
					} else {
						return;
					}
				}
				firePropertyChange(PROP_DIRTY);
			}
		};

		return quickEditorForm;
	}

	/**
	 * Sets the editor for script editing.
	 * 
	 * @param ste
	 *            The new editor
	 */
	public void setScriptEditor(SimpleTextEditor ste) {

		this.editor = ste;

		editor.addCloseListener(new Listener() {
			public void handleEvent(Event event) {
				if (editor.isModified())
					editor.updateConfiguration();
			}
		});

		editor.getSourceViewer().getTextWidget().addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				firePropertyChange(PROP_DIRTY);
			}
		});
	}

	/**
	 * Update the configuration with changes from the text editor
	 */
	protected void saveQuickEditor() {
		if (editor != null && editor.isModified())
			editor.updateConfiguration();
	}

	/**
	 * Saves changes and "closes" the editor (hiding it)
	 */
	protected void closeEditor() {
		if (editor.isModified())
			editor.getConfig().setScript(editor.getText());
		editor.setEditingConfig(null);
		editor.setModified(false);
		showEditor(false);
	}

	/**
	 * Returns the TDI configuration file being edited
	 * 
	 * @return the TDIConfigurationFile
	 */
	public TDIConfigurationFile getFile() {
		return file;
	}

	/**
	 * Sets TDI configuration being edited
	 * 
	 * @param file
	 */
	public void setFile(TDIConfigurationFile file) {
		this.file = file;
	}

	/**
	 * Returns true if this edits broadcasts property changes
	 * 
	 * @return true if this edits broadcasts property changes
	 */
	public boolean isNotificationsEnabled() {
		return notificationsEnabled;
	}

	/**
	 * Sets the notifications enabled flag
	 * 
	 * @see #isNotificationsEnabled()
	 * @param notificationsEnabled
	 */
	public void setNotificationsEnabled(boolean notificationsEnabled) {
		this.notificationsEnabled = notificationsEnabled;
	}

	/**
	 * Creates a context menu on the provided viewer. The context menu is
	 * registed to the platform through the editor site.
	 * 
	 * @param parent
	 *            The viewer
	 * @return Menu object or null if editor site is null
	 */
	public Menu registerContextMenu(Viewer parent) {
		return registerContextMenu(parent, null);
	}

	/**
	 * Registers a context menu
	 * 
	 * @param parent
	 * @param id
	 * @return the new context Menu
	 */
	public Menu registerContextMenu(Viewer parent, String id) {
		menuManager = new MenuManager();
		Menu menu = null;

		if (getSite() != null) {
			if (id != null)
				getSite().registerContextMenu(getSite().getId() + "." + id, menuManager, parent); //$NON-NLS-1$
			else
				getSite().registerContextMenu(menuManager, parent);
			menu = menuManager.createContextMenu(parent.getControl());
			parent.getControl().setMenu(menu);
			menuManager.add(new Separator(TDI.GROUP_TDI));
			menuManager.add(new Separator(TDI.GROUP_TDI + ".1"));
			menuManager.add(new Separator(TDI.GROUP_TDI + ".2"));
			menuManager.add(new Separator(TDI.GROUP_TDI + ".3"));
			menuManager.add(new Separator(TDI.GROUP_TDI + ".4"));
			menuManager.add(new Separator("group.edit"));
			menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		}
		return menu;
	}

	/**
	 * Returns the quick editor form containing the script editor
	 * 
	 * @return the quick editor form
	 */
	public Form getQuickEditorForm() {
		return quickEditorForm;
	}

	/**
	 * Returns the last registered menu manager
	 * 
	 * @see #registerContextMenu(Viewer)
	 * 
	 * @return the MenuManager
	 */
	public MenuManager getMenuManager() {
		return menuManager;
	}

	/**
	 * This method is called when the file we are editing has been changed by
	 * another editor/process (e.g. CVS update). Sub classes should override to
	 * provide different behavior than prompting the user to reload the editor.
	 */
	protected void reloadEditor() {
		try {
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (window != null && window.getActivePage() != null) {
				IEditorPart current = window.getActivePage().getActiveEditor();
				if (current == this) {
					String title = Messages.getString("BaseEditor.reload.title"); //$NON-NLS-1$
					String message = Messages.getString("BaseEditor.reload.message"); //$NON-NLS-1$
					if (MessageDialog.openQuestion(getSite().getShell(), title, message)) {
						resourceNotificationPending = false;
						closeAndReloadEditor(this);
					}
				} else {
					resourceNotificationPending = true;
				}
			} else {
				resourceNotificationPending = true;
			}
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e);
		}
	}

	/**
	 * This is the default handler that closes and reopens this editor after the
	 * file has been externally changed (e.g. CVS update).
	 * 
	 * @param editor
	 */
	protected void closeAndReloadEditor(IEditorPart editor) {
		final IEditorInput input = editor.getEditorInput();
		final String editorID = editor.getEditorSite().getId();
		try {
			getSite().getShell().getDisplay().asyncExec(new Runnable() {
				public void run() {
					getSite().getPage().closeEditor(BaseEditor.this, false);
					try {
						// Make sure we reopen this editor and not some other
						// matching the input
						getSite().getPage().openEditor(input, editorID, true, IWorkbenchPage.MATCH_NONE);
					} catch (PartInitException e) {
						EclipseAppender.logerror(e.toString(), e);
					}
				}
			});
		} catch (Throwable t) {
			EclipseAppender.logerror(t.toString(), t);
		}
	}

	/**
	 * This class is used as the selection provider for the editor. It listens
	 * for selection changes in the components that registers themselves through
	 * the addSelectionProvider method and broadcasts a selection change to
	 * listeners of this.
	 */
	public static class SelectionProvider implements ISelectionProvider, ISelectionChangedListener {
		@SuppressWarnings("unused")
		private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

		private Set<ISelectionChangedListener> listeners = new HashSet<ISelectionChangedListener>();
		private ISelection selection;

		public void addSelectionChangedListener(ISelectionChangedListener listener) {
			listeners.add(listener);
		}

		public ISelection getSelection() {
			return selection;
		}

		public void removeSelectionChangedListener(ISelectionChangedListener listener) {
			listeners.remove(listener);
		}

		public void setSelection(ISelection selection) {
			this.selection = selection;
		}

		public void selectionChanged(SelectionChangedEvent event) {
			this.selection = event.getSelection();
			SelectionChangedEvent sce = new SelectionChangedEvent(this, getSelection());
			for (ISelectionChangedListener cl : listeners)
				cl.selectionChanged(sce);

			// This messes up for other editors.
			// updateActionBars();
		}
	}

	/**
	 * This action class handles the NullValueBehavior command
	 */
	private class NullValueHandler extends Action implements IHandler {

		ArrayList<IHandlerListener> listeners = new ArrayList<IHandlerListener>();

		@Override
		public String getText() {
			return Messages.getString("TaskCallParam.table.nullbehavior.label"); //$NON-NLS-1$
		}

		@Override
		public void run() {
			NullValueBehaviorWizard wiz = new NullValueBehaviorWizard(editor.getEditingConfig());
			WizardDialog dlg = new WizardDialog(editor.getShell(), wiz);
			dlg.open();
		}

		@Override
		public String getActionDefinitionId() {
			return "com.ibm.tdi.rcp.quickeditor.nvb"; //$NON-NLS-1$
		}

		public void addHandlerListener(IHandlerListener handlerListener) {
			if (!listeners.contains(handlerListener))
				listeners.add(handlerListener);
		}

		public void dispose() {
		}

		public Object execute(ExecutionEvent event) throws ExecutionException {
			run();
			return null;
		}

		public void removeHandlerListener(IHandlerListener handlerListener) {
			listeners.remove(handlerListener);
		}

		@Override
		public void setEnabled(boolean enabled) {
			super.setEnabled(enabled);
			HandlerEvent event = new HandlerEvent(this, true, false);
			for (IHandlerListener hl : listeners) {
				hl.handlerChanged(event);
			}
		}
	}

	/**
	 * This action class handles the quickeditor close command
	 * 
	 */
	private class CloseHandler extends NullValueHandler {

		@Override
		public String getText() {
			return Messages.getString("LBL.CLOSE"); //$NON-NLS-1$
		}

		@Override
		public String getActionDefinitionId() {
			return "com.ibm.tdi.rcp.quickeditor.close"; //$NON-NLS-1$
		}

		@Override
		public void run() {
			closeEditor();
		}
	}

	/**
	 * This action class handles the test script command
	 * 
	 */
	private class TestScriptHandler extends NullValueHandler {
		@Override
		public String getText() {
			return Messages.getString("BaseEditor.20"); //$NON-NLS-1$
		}

		@Override
		public String getToolTipText() {
			return getText();
		}

		@Override
		public ImageDescriptor getImageDescriptor() {
			return Activator.getImageDescriptor(Messages.getString("BaseEditor.21")); //$NON-NLS-1$
		}

		@Override
		public String getActionDefinitionId() {
			return "com.ibm.tdi.rcp.testscript"; //$NON-NLS-1$
		}

		@Override
		public void run() {
			try {
				String script = editor.getEditingConfig().getScript();
				JavaScriptView view = (JavaScriptView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.showView(JavaScriptView.VIEW_ID);
				view.testScript(script);
			} catch (Exception e) {
				EclipseAppender.logerror(e.toString(), e);
			}
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		// TODO Auto-generated method stub

	}

	private boolean confirmMapTypeChange() {
		return MessageDialog.openConfirm(null, Messages.getString("ConfigBinding.BreakInh.Title"), Messages
				.getString("BaseEditor.ChangeMapType.Msg"));
	}

	private boolean confirmInheritanceChange() {
		return MessageDialog.openConfirm(null, Messages.getString("ConfigBinding.BreakInh.Title"), Messages
				.getString("BreakInhScript.Msg"));
	}

	/**
	 * This default implementation does nothing.
	 * 
	 * @param marker
	 */
	public void gotoMarker(IMarker marker) {
		// This should be overridden by sub classes.
	}

	/**
	 * Returns the quick editor instance used by this editor.
	 * 
	 * @return the SimpleTextEditor
	 */
	public SimpleTextEditor getEditor() {
		return editor;
	}

	//
	///////////////////// IMemento 
	//

	/**
	 * Returns the IMemento for this editor, if none exists one is created. The new memento must be explicitly saved.
	 *  
	 * @return
	 */
	public IMemento getMemento() {
		return getMemento(false);
	}
	
	/**
	 * Returns the IMemento for this editor, if none exists one is created. If clean is true, a new empty memento is created and
	 * overwrites any previous memento.
	 * 
	 * @param clean
	 * @return
	 */
	public IMemento getMemento(boolean clean) {
		if(!clean) {
			if(memento != null)
				return memento;
			
			try {
				File f = getMementoFile();
				if (f != null)
					setMemento(XMLMemento.createReadRoot(new FileReader(f)));
				else
					setMemento(XMLMemento.createWriteRoot("TDI"));
			} catch (Exception e) {
				setMemento(XMLMemento.createWriteRoot("TDI"));
			}
		} else {
			setMemento(XMLMemento.createWriteRoot("TDI"));
		}
		
		return memento;
	}
	
	/**
	 * Sets the IMemento for this widget.
	 * 
	 * @param memento
	 */
	public void setMemento(IMemento memento) {
		this.memento = memento;
	}
	
	/**
	 * Saves the memento currently associated with this Editor. If no memento is defined this method does nothing.
	 * 
	 */
	public void saveMemento() {
		if(memento == null)
			return;
		
		try {
			File f = getMementoFile();
			if(f != null)
				((XMLMemento) memento).save(new FileWriter(f));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns the memento File for this editor. It is based on the File object for the editor input and is located
	 * in the state-location directory for the plugin.
	 *  
	 * @return
	 */
	private File getMementoFile() {
		IFile file = null;

		if (getTDIConfiguration() instanceof TDIConfigurationFile) {
			file = ((TDIConfigurationFile) getTDIConfiguration()).getFile();

		} else if (getEditorInput() instanceof RunAssemblyLineInput) {
			IProject project = ((RunAssemblyLineInput) getEditorInput())
					.getProject();
			if (project != null) {
				file = project.getFolder("AssemblyLines").getFile(
						getTDIConfiguration().getShortName() + "."
								+ TDIConfigurationFile.XT_ASSEMBLYLINE);
			}

		}

		if (file != null && file.exists()) {
			IPath mempath = Activator.getDefault().getStateLocation().append(file.getProject().getName() + "_" + file.getName());
			File memfile = new File(mempath.toOSString());
			return memfile;

		}
		return null;
	}
}
