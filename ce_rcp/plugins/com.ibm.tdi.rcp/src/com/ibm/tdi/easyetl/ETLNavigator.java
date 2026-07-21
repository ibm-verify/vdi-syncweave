/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.easyetl;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.ide.undo.MoveResourcesOperation;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.swt.IFocusService;

import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.tdi.eclipse.Activator;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.actions.ExportRuntimeAction;
import com.ibm.tdi.eclipse.actions.RenameResourceAction;
import com.ibm.tdi.eclipse.editors.RunAssemblyLineEditor;
import com.ibm.tdi.eclipse.editors.RunAssemblyLineInput;
import com.ibm.tdi.eclipse.handlers.etl.ETLDeleteContentsHandler;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.natures.TDINature;
import com.ibm.tdi.eclipse.util.TDIToolBar;
import com.ibm.tdi.eclipse.widget.BaseWidget;

public class ETLNavigator extends ViewPart {
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public static final QualifiedName TDI_EASYETL_PROJECT = new QualifiedName(
			"http://www.ibm.com", Activator.TDI_PLUGIN_ID + ".easyetl"); //$NON-NLS-1$ //$NON-NLS-2$

	private BaseWidget root;

	private TableViewer table;

	public ETLNavigator() {
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());

		root = new BaseWidget(parent, SWT.NONE);
		root.setLayout(new FillLayout());
		Form form = root.createForm(root, null);
		form.getBody().setLayout(new FillLayout());

		// -- toolbar
		TDIToolBar bar = new TDIToolBar(form, SWT.SINGLE);
		bar.add(new Action() {

			@Override
			public String getText() {
				return Messages.getString("action.label.38");
			}

			@Override
			public void run() {
				createETLProject();
				table.refresh();
			}

		});

		final Action openAction = new Action() {

			@Override
			public String getText() {
				return Messages.getString("SystemStoreEditor.open");
			}

			@Override
			public void run() {
				openETL();
			}

		};
		// bar.add(openAction);

		final Action openTDIAction = new Action() {
			@Override
			public String getText() {
				return Messages.getString("ETLNavigator_openwithtdi");
			}

			@Override
			public void run() {
				IFile file = getETLAssemblyLine(getSelectedProject());
				final IWorkbenchPage page = getSite().getPage();
				final FileEditorInput input = new FileEditorInput(file);
				final IEditorPart editor = page.findEditor(input);
				getSite().getShell().getDisplay().asyncExec(new Runnable() {
					public void run() {
						if (editor instanceof ETLEditor)
							page.closeEditor(editor, false);
						try {
							IDE.openEditor(page, input, "com.ibm.tdi.eclipse.editors.AssemblyLineEditor3");
						} catch (PartInitException e) {
							EclipseAppender.logerror(e.toString(), e);
						}
					}
				});
			}
		};

		final Action exportAction = new Action() {

			private ExportRuntimeAction export = new ExportRuntimeAction();

			@Override
			public String getText() {
				return Messages.getString("ExportRuntime.tooltip");
			}

			@Override
			public void run() {
				export.setActivePart(this, getSite().getPart());
				export.setSelection(table.getSelection());
				export.run(this);
			}

		};

		final IAction runConsoleAction = getRunConsoleAction();
		final IAction runCollectAction = getRunCollectAction();

		bar.add(runCollectAction);

		Composite c = form.getBody();

		table = new TableViewer(c, SWT.FULL_SELECTION | SWT.SINGLE);
		TableColumn tc = new TableColumn(table.getTable(), SWT.LEFT);
		tc.setWidth(400);

		table.addOpenListener(new IOpenListener() {
			public void open(OpenEvent event) {
				openETL();
			}
		});

		table.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				openAction.setEnabled(!table.getSelection().isEmpty());
				openTDIAction.setEnabled(!table.getSelection().isEmpty());
				exportAction.setEnabled(!table.getSelection().isEmpty());
				runConsoleAction.setEnabled(!table.getSelection().isEmpty());
				runCollectAction.setEnabled(!table.getSelection().isEmpty());
			}
		});
		openAction.setEnabled(!table.getSelection().isEmpty());
		openTDIAction.setEnabled(!table.getSelection().isEmpty());
		exportAction.setEnabled(!table.getSelection().isEmpty());
		runConsoleAction.setEnabled(!table.getSelection().isEmpty());
		runCollectAction.setEnabled(!table.getSelection().isEmpty());

		table.setLabelProvider(new ITableLabelProvider() {
			public void removeListener(ILabelProviderListener listener) {
			}

			public void dispose() {
			}

			public void addListener(ILabelProviderListener listener) {
			}

			public boolean isLabelProperty(Object element, String property) {
				return false;
			}

			public String getColumnText(Object element, int columnIndex) {
				if (element instanceof IResource)
					return ((IResource) element).getName();
				else
					return element.toString();
			}

			public Image getColumnImage(Object element, int columnIndex) {
				return Activator.getImage("Neo");
			}
		});

		table.setContentProvider(new IStructuredContentProvider() {
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}

			public void dispose() {
			}

			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof IWorkspaceRoot) {
					IWorkspaceRoot root = (IWorkspaceRoot) inputElement;
					return getETLProjects(root);
				}
				return new Object[0];
			}
		});

		table.setInput(ResourcesPlugin.getWorkspace().getRoot());

		ResourcesPlugin.getWorkspace().addResourceChangeListener(new IResourceChangeListener() {
			public void resourceChanged(IResourceChangeEvent event) {
				if (event.getResource() instanceof IProject
						|| (event.getResource() == null && event.getType() == IResourceChangeEvent.POST_CHANGE)) {
					getSite().getShell().getDisplay().asyncExec(new Runnable() {
						public void run() {
							table.refresh(true);
						}
					});
				}
			}
		});

		if (getViewSite() != null) {
			IFocusService fs = (IFocusService) getViewSite().getService(IFocusService.class);
			if (fs != null) {
				fs.addFocusTracker(table.getControl(), "com.ibm.tdi.etl.table");
			}
		}

		MenuManager mm = new MenuManager();
		table.getControl().setMenu(mm.createContextMenu(table.getControl()));
		mm.add(getRenameProjectAction());
		mm.add(openAction);
		mm.add(openTDIAction);
		mm.add(new Separator());
		mm.add(runConsoleAction);
		mm.add(runCollectAction);
		mm.add(new Separator());
		mm.add(exportAction);
		mm.add(new Separator());
		IAction delAction = getDeleteProjectAction();
		mm.add(delAction);
	}

	private IAction getRunConsoleAction() {
		return new Action() {
			@Override
			public String getText() {
				return Messages.getString("AssemblyLineEditor.run.nocollect");
			}

			@Override
			public void run() {
				try {
					TDIConfigurationFile cfg = TDIConfigurationFile.loadFile(getETLAssemblyLine(getSelectedProject()));
					RunAssemblyLineInput input = new RunAssemblyLineInput((AssemblyLineConfig) cfg.getDefaultConfigObject());
					input.setDebug(false);
					input.setCollectingWork(false);
					getSite().getPage().openEditor(input, RunAssemblyLineEditor.EDITOR_ID, true);
				} catch (Exception e) {
					EclipseAppender.logerror(e.toString(), e, getSite().getShell());
				}
			}
		};
	}

	private IAction getRunCollectAction() {
		return new Action() {
			@Override
			public String getText() {
				return Messages.getString("general.run.name");
			}

			@Override
			public void run() {
				try {
					TDIConfigurationFile cfg = TDIConfigurationFile.loadFile(getETLAssemblyLine(getSelectedProject()));
					RunAssemblyLineInput input = new RunAssemblyLineInput((AssemblyLineConfig) cfg.getDefaultConfigObject());
					input.setDebug(false);
					input.setCollectingWork(true);
					getSite().getPage().openEditor(input, RunAssemblyLineEditor.EDITOR_ID, true);
				} catch (Exception e) {
					EclipseAppender.logerror(e.toString(), e, getSite().getShell());
				}
			}
		};
	}

	private IAction getDeleteProjectAction() {
		return new Action() {
			@Override
			public String getText() {
				return Messages.getString("general.delete.label");
			}

			@Override
			public String getActionDefinitionId() {
				return ActionFactory.DELETE.getCommandId();
			}

			@Override
			public void run() {
				new ETLDeleteContentsHandler().deleteContents(table.getTable());
			}
		};
	}

	private IAction getRenameProjectAction() {
		return new Action() {
			@Override
			public String getText() {
				return Messages.getString("general.rename.name");
			}

			@Override
			public void run() {
				IProject project = getSelectedProject();
				if (project == null)
					return;
				String oldname = project.getName();

				RenameResourceAction rra = new RenameResourceAction();
				rra.setSelection(new StructuredSelection(getSelectedProject()));
				rra.run(this);

				if (rra.getNewName() != null) {
					IProject prj = project.getWorkspace().getRoot().getProject(rra.getNewName());
					IUndoContext undo = getSite().getWorkbenchWindow().getWorkbench().getOperationSupport().getUndoContext();
					try {
						IFile file = prj.getFolder(TDINature.ASSEMBLYLINES_FOLDER).getFile(
								oldname + "." + TDIConfigurationFile.XT_ASSEMBLYLINE);
						IPath newFile = file.getFullPath().removeLastSegments(1).append(
								prj.getName() + "." + TDIConfigurationFile.XT_ASSEMBLYLINE);
						MoveResourcesOperation mrf = new MoveResourcesOperation(file, newFile, getText());
						mrf.addContext(undo);
						OperationHistoryFactory.getOperationHistory().execute(mrf, null, null);
					} catch (ExecutionException e) {
						EclipseAppender.logerror(e.toString(), e, getSite().getShell());
					}
				}
			}
		};
	}

	/**
	 * Returns the currently selected project
	 * 
	 * @return
	 */
	protected IProject getSelectedProject() {
		IStructuredSelection sel = (IStructuredSelection) table.getSelection();
		if (sel.isEmpty())
			return null;
		else
			return (IProject) sel.getFirstElement();
	}

	/**
	 * Returns the assemblyline for the currently selected project
	 * 
	 * @param project
	 * @return
	 */
	protected IFile getETLAssemblyLine(IProject project) {
		return project.getFolder(TDINature.ASSEMBLYLINES_FOLDER).getFile(
				project.getName() + "." + TDIConfigurationFile.XT_ASSEMBLYLINE);
	}

	/**
	 * Opens the currently selected ETL project's assemblyline in the ETL editor
	 */
	protected void openETL() {
		IStructuredSelection sel = (IStructuredSelection) table.getSelection();
		if (sel.isEmpty())
			return;
		IProject project = (IProject) sel.getFirstElement();
		IFile file = project.getFile(TDINature.ASSEMBLYLINES_FOLDER + "/" + project.getName() + ".assemblyline");
		try {
			file.refreshLocal(IResource.DEPTH_INFINITE, null);
			IDE.openEditor(getSite().getPage(), file, ETLEditor.EDITOR_ID);
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e, getSite().getShell());
		}
	}

	/**
	 * Creates a new ETL project and opens the newly created assemblyline in the
	 * ETL Editor
	 */
	protected void createETLProject() {
		try {
			com.ibm.tdi.eclipse.wizards.NewProject wiz = new com.ibm.tdi.eclipse.wizards.NewProject();
			wiz.init(getSite().getWorkbenchWindow().getWorkbench(), null);
			WizardDialog dlg = new WizardDialog(getSite().getShell(), wiz);
			if (dlg.open() == Window.OK) {
				wiz.getProject().setPersistentProperty(TDI_EASYETL_PROJECT, "true");
				// 
				IFile file = wiz.getProject().getFile(
						TDINature.ASSEMBLYLINES_FOLDER + "/" + wiz.getProject().getName() + ".assemblyline");
				if (!file.exists()) {
					String def = "<MetamergeConfig version=\"7.1\"><AssemblyLine name=\"Default\"/></MetamergeConfig>";
					file.create(new ByteArrayInputStream(def.getBytes()), 0, null);
				}
				IDE.openEditor(getSite().getPage(), new FileEditorInput(file), ETLEditor.EDITOR_ID);
			}
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e, getSite().getShell());
		}
	}

	/**
	 * Returns the IProject(s) from the workspace that are tagged as ETL
	 * projects
	 * 
	 * @param ws
	 * @return
	 */
	protected Object[] getETLProjects(IWorkspaceRoot ws) {
		ArrayList<IResource> list = new ArrayList<IResource>();
		try {
			for (IResource res : ws.members()) {
				if (res instanceof IProject) {
					IProject project = (IProject) res;
					if (project.isOpen()) {
						String etl = res.getPersistentProperty(TDI_EASYETL_PROJECT);
						if (etl != null && etl.equals("true")) {
							list.add(project);
						}
					}
				}
			}
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e);
		}

		return list.toArray();
	}

	@Override
	public void setFocus() {
	}

}
