/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.wizards;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.ide.IDE;

import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.natures.TDINature;
import com.ibm.tdi.eclipse.wizards.pages.SelectComponentPage;

public class NewConnectorConfigWizard extends ConnectorConfigWizard {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	protected IWorkbench workbench;
	protected IStructuredSelection selection;
	protected TDIConfigurationFile tdiConfigurationFile;
	private final static String EXTENSION = "." + TDIConfigurationFile.XT_CONNECTOR;
	
	public NewConnectorConfigWizard(IWorkbench workbench, IStructuredSelection selection) {
		super(null, null);
		this.workbench = workbench;
		this.selection = selection;
		createConfigObject();
		setWindowTitle(Messages.getString("miadmin.menu.Object.NewConnector.label"));
	}
	
	public void createConfigObject() {
		try {
			tdiConfigurationFile = new TDIConfigurationFile() {
				final static long serialVersionUID = 3L;
				@Override
				public IProject getProject() {
					return getSelectionProject();
				}
			};
			setLocation( tdiConfigurationFile.newInstanceOf(getType()));
		} catch (Exception e) {
			// This cannot happen?
			EclipseAppender.logerror("Error", e, getShell()); //$NON-NLS-1$
		}
	}
	
	/**
	 * Returns the IProject in which the selection is located
	 * 
	 * @return IProject
	 */
	public IProject getSelectionProject() {
		IStructuredSelection sel = getSelection();
		IProject project = null;
		if(sel != null && !sel.isEmpty()) {
			Object obj = sel.getFirstElement();
			if(obj instanceof BaseConfiguration) {
				BaseConfiguration bc = (BaseConfiguration) obj;
				if(bc.getMetamergeConfig() instanceof TDIConfigurationFile)
					project = ((TDIConfigurationFile)bc.getMetamergeConfig()).getProject();
			} else if (obj instanceof IResource) {
				project = ((IResource)obj).getProject();
			} else if (obj instanceof IAdaptable) {
				IResource res = (IResource) ((IAdaptable) obj).getAdapter(IResource.class);
				if (res != null)
					project = res.getProject();
			}
		}
		return project;
	}
	
	public IStructuredSelection getSelection() {
		return selection;
	}

	private String getType() {
		return MetamergeConfig.DEFAULT_CONNECTOR_FOLDER;
	}
	
	@Override
	public void hookPages() {
		if(getPage(MAIN_PAGE) instanceof SelectComponentPage) {
			((SelectComponentPage)getPage(MAIN_PAGE)).setShowButtons(false);
		}
		super.hookPages();
	}


	@Override
	public boolean performFinish() {

		try {
			IFile file = createNewFile(getComponentName());
			if (file != null)
				IDE.openEditor(workbench.getActiveWorkbenchWindow().getActivePage(), file);
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e, getShell());
			return false;
		}
		return true;
	}
	
	
	/**
	 *  Create the new file
	 */
	private IFile createNewFile(String name) throws Exception {
		
		BaseConfiguration bc = getConfigObject();
			
		if(name.endsWith(EXTENSION))
			name = name.substring(0, name.length() - EXTENSION.length());

		IFile file;

		// -- TDI Project
		IProject project = getSelectionProject();
		if (project == null)
			return null;

		IFolder path = null;
		// -- Subfolder in Resources directory
		path = project.getFolder(TDINature.RESOURCES_FOLDER);
		if (!path.exists())
			path.create(true, true, null);
		String folder = TDINature.getDefaultFolder(bc);
		path = path.getFolder(folder);
		if (!path.exists())
			path.create(true, true, null);		
		file = path.getFile(name + EXTENSION); 

		// -- Check if file exists and open confirmation dialog
		if(file.exists() &&
			!MessageDialog.openQuestion(
				getShell(),
				Messages.getString("general.save.library.label"),
				Messages.getMessage("general.resource.exists", 
					file.getFullPath().toOSString(), 
					Utils.dateToString(file.getLocalTimeStamp()))))
			return null;
		
		// -- Create/Overwrite the file
		TDIConfigurationFile cfg = new TDIConfigurationFile(file);
		bc.setParent(null);
		cfg.setDefaultConfigObject(name, bc);
		cfg.commitVersion(true);
		/**
		 * For some strange reason, commitVersion() may return before the new content
		 * is flushed. Add a small sleep here to help with the problem.
		 */
		Thread.sleep(200); 
		return file;
	}


}
