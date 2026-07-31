/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.navigator;

import java.util.ArrayList;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ResourceTransfer;

import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.ParserConfig;
import com.ibm.di.config.interfaces.ScriptConfig;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.natures.TDINature;
import org.eclipse.jface.dialogs.IInputValidator;

public class PasteHandler extends AbstractHandler {
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private Clipboard cb = new Clipboard(Display.getDefault());
	private ArrayList<BaseConfiguration> cbConfigs;
	private ArrayList<IFile> cbFiles;
	private IResource targetResource;

	public Object execute(ExecutionEvent event) throws ExecutionException {
		if(cbConfigs != null && cbConfigs.size() > 0) {
			pasteConfigObjects(targetResource.getProject());
		}
		if(cbFiles != null && cbFiles.size() > 0) {
			pasteFiles();
		}
		return null;
	}

	@Override
	public boolean isEnabled() {
		return 
			(cbConfigs != null && cbConfigs.size() > 0) ||
			(cbFiles != null && cbFiles.size() > 0);
	}

	@Override
	public boolean isHandled() {
		return isEnabled();
	}

	@Override
	public void setEnabled(Object evaluationContext) {
		super.setEnabled(evaluationContext);
		checkClipboardData(evaluationContext);
	}

	/**
	 * Update the cbConfigs array with BaseConfiguration objects found in the
	 * clipboard that we can create files from.
	 * 
	 * @return true if objects were found
	 */
	boolean checkClipboardData() {
		LocalSelectionTransfer transfer = LocalSelectionTransfer.getTransfer();
		ResourceTransfer resxfer = ResourceTransfer.getInstance();
		cbConfigs = new ArrayList<BaseConfiguration>();
		cbFiles = new ArrayList<IFile>();
		for (TransferData type : cb.getAvailableTypes()) {
			if (transfer.isSupportedType(type)) {
				IStructuredSelection selection = (IStructuredSelection) cb.getContents(transfer);
				if(selection != null) {
					for (Object obj : selection.toArray()) {
						if (validatePaste(obj))
							cbConfigs.add((BaseConfiguration) obj);
					}
				}
			} else if (resxfer.isSupportedType(type)) {
				IResource[] resources = (IResource[]) cb.getContents(resxfer);
				for(IResource res : resources) {
					if (validatePaste(res))
						cbFiles.add((IFile)res);
				}
			}
		}
		
		return (cbConfigs.size() > 0 || cbFiles.size() > 0);
	}

	/**
	 * Update the cbConfigs array with BaseConfiguration objects found in the
	 * clipboard that we can create files from.
	 * @param evaluationContext 
	 * 
	 * @return true if objects can be pasted in to selection's project
	 */
	private boolean checkClipboardData(Object evaluationContext) {
		if (!checkClipboardData())
			return false;
		
		// -- Check if we have a target to which the paste can go
		try {
			Object activePart = ((IEvaluationContext)evaluationContext).getVariable("activePart");
			if(activePart instanceof CustomNavigator) {
				CustomNavigator nav = (CustomNavigator) activePart;
				if (setTarget(nav.getCommonViewer().getSelection()))
					return true;
			}
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e);
		}
		cbConfigs = null;
		cbFiles = null;
		return false;
	}

	/**
	 * Return true if the selection could be used to set the target
	 * @param selection
	 * @return
	 */
	boolean setTarget(ISelection selection) {
		targetResource = null;
		if (! (selection instanceof IStructuredSelection))
			return false;

		IStructuredSelection sel = (IStructuredSelection) selection;
		if(sel.getFirstElement() instanceof IResource)
			targetResource = (IResource) sel.getFirstElement();

		return targetResource != null;
	}
	
	IResource getTarget() {
		return targetResource;
	}
	
	private boolean validatePaste(Object obj) {
		if(obj instanceof BaseConfiguration) {
			return (obj instanceof ConnectorConfig || obj instanceof ScriptConfig || obj instanceof ParserConfig);
		}
		return obj instanceof IFile;
	}

	private void pasteConfigObjects(IProject project) {
		for (BaseConfiguration cfg : cbConfigs) {
			String folder = Utils.getFolderForConfig(cfg);
			try {
				Utils.createFileFromConfig(project, folder, cfg, getShell());
			} catch (Exception e) {
				EclipseAppender.logerror(e.toString(), e, getShell());
			}
		}
	}

	private void pasteFiles() {
		IContainer targetFolder = null;
		IProject project = null;
		
		// -- target folder
		if(targetResource instanceof IContainer)
			targetFolder = (IContainer) targetResource;
		else if(targetResource instanceof IFile)
			targetFolder = targetResource.getParent();
		
		// -- target TDI project (if applicable)
		try {
			if(targetResource.getProject().hasNature(TDINature.TDI_NATURE_ID))
				project = targetResource.getProject();
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e);
		}
	
		// -- create files
		for(IFile file : cbFiles) {
			// -- redirect paste to proper folder for tdi projects
			IFile newfile = null;
			if(project != null) {
				String defaultFolder = TDIConfigurationFile.getFolderForExtension(file.getFileExtension());
				if(defaultFolder != null) {
					if(defaultFolder.equals(MetamergeConfig.DEFAULT_ASSEMBLYLINE_FOLDER))
						newfile = project.getFolder(defaultFolder).getFile(file.getName());
					else
						newfile = project.getFolder(TDINature.RESOURCES_FOLDER).getFolder(defaultFolder).getFile(file.getName());
				}
			}
			
			if(newfile == null) {
				if (targetFolder instanceof IFolder)
					newfile = ((IFolder)targetFolder).getFile(file.getName());
				else if (targetFolder instanceof IProject)
					newfile = ((IProject)targetFolder).getFile(file.getName());
				else
					continue;
			}
			
			while(newfile.exists()) {
				String displayName = newfile.getName();
				String extension = null;
				if(project != null && TDIConfigurationFile.getFolderForExtension(file.getFileExtension()) != null) {
					displayName = newfile.getName().substring(0, newfile.getName().lastIndexOf(".")); 
					extension = "." + newfile.getFileExtension(); 
				}
				
				InputDialog id = new InputDialog(
						getShell(),
						newfile.getParent().getProjectRelativePath().toPortableString() + "/" + displayName,
						Messages.getString("RenameWorkAttributeItem.1"),
						displayName,
						new FileInputValidator(newfile, extension));
				if(id.open() == Window.CANCEL) {
					return;
				}
				
				String name = id.getValue();
				if(extension != null && !name.endsWith(extension))
					name += extension;
				
				if(newfile.getParent() instanceof IFolder)
					newfile = ((IFolder)newfile.getParent()).getFile(name);
				else if(newfile.getParent() instanceof IProject)
					newfile = ((IProject)newfile.getParent()).getFile(name);
			}
			
			// -- copy inherited components if from another project
			try {
				TDIConfigurationFile cfg = TDIConfigurationFile.loadFile(file);
				if (cfg.getProject() != newfile.getProject()) {
					for(String str : cfg.getDefaultConfigObject().getReferences(null)) {
						if(str.startsWith("system:"))
							continue;
						BaseConfiguration cc = (BaseConfiguration) cfg.lookup(str);
						try {
							Utils.createFileFromConfig(newfile.getProject(), Utils.getFolderForConfig(cc), cc, getShell(), false);
						} catch (Exception e) {
							EclipseAppender.logerror(e.toString(), e, getShell());
						}
					}
				}
			} catch (Exception e) {
				EclipseAppender.logerror(e.toString(), e, getShell());
			}

			try {
				newfile.create(file.getContents(), true, null);
			} catch (Exception e) {
				EclipseAppender.logerror(e.toString(), e, getShell());
			}
		}
	}

	private Shell getShell() {
		return PlatformUI.getWorkbench().getDisplay().getActiveShell();
	}

	@Override
	public void dispose() {
		if (cb != null) {
			cb.dispose();
			cb = null;
		}
		super.dispose();
	}

	private static class FileInputValidator implements IInputValidator {

		private IContainer parent;
		private String extension;
		
		public FileInputValidator(IFile file, String extension) {
			this.parent = file.getParent();
			this.extension = extension;
		}

		public String isValid(String newText) {
			String name = newText;

			if (name.length() == 0)
				return Messages.getString("ConfigTypePage.5");
			
			if(extension != null && !name.endsWith(extension))
				name += extension;
			
			IFile newfile = null;
			if (parent instanceof IFolder)
				newfile = ((IFolder)parent).getFile(name);
			else if (parent instanceof IProject)
				newfile = ((IProject)parent).getFile(name);

			return (newfile != null && newfile.exists()) ? Messages.getString("RenameConfigAction.AlreadyExists") : null;
		}
	}
}
