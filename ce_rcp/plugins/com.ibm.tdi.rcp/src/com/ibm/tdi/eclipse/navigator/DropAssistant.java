/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.navigator;


import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.navigator.CommonDropAdapter;
import org.eclipse.ui.navigator.CommonDropAdapterAssistant;

import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.natures.TDINature;

/**
 * This class is used by the CommonNavigator to enable dragging of BaseConfiguration objects
 * into the Resources folder. The common navigator does not understand BaseConfiguration objects
 * so we provide this class to perform the drop validation and creating files based on the
 * configuration objects being dropped.
 * <br/>
 * This class is enabled by the "org.eclipse.ui.navigator.navigatorContent" extension in the
 * "plugin.xml" file. Only the "TDI Navigator" includes this content extension so it will only
 * work for the TDI Navigator (e.g. and not the Resource Explorer etc).
 * <br/>
 */
public class DropAssistant extends CommonDropAdapterAssistant {
	
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	static final String[] FILE_EXTENSION_FOLDERS = new String[]{
		MetamergeConfig.DEFAULT_ASSEMBLYLINE_FOLDER,
		MetamergeConfig.DEFAULT_ATTRIBUTEMAP_FOLDER,
		MetamergeConfig.DEFAULT_CONNECTOR_FOLDER,
		MetamergeConfig.DEFAULT_FUNCTION_FOLDER,
		MetamergeConfig.DEFAULT_PARSER_FOLDER,
		MetamergeConfig.DEFAULT_PROPSTORE_FOLDER,
		MetamergeConfig.DEFAULT_SCRIPT_FOLDER,
		MetamergeConfig.DEFAULT_NAMESPACE_FOLDER,
	};

	/**
	 * Empty constructor 
	 */
	public DropAssistant() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.CommonDropAdapterAssistant#handleDrop(org.eclipse.ui.navigator.CommonDropAdapter,
	 *      org.eclipse.swt.dnd.DropTargetEvent, java.lang.Object)
	 */
	@Override
	public IStatus handleDrop(CommonDropAdapter aDropAdapter, DropTargetEvent dte, Object aTarget) {
		IStructuredSelection sel = (IStructuredSelection) LocalSelectionTransfer.getTransfer().getSelection();
		if (sel.isEmpty())
			return Status.CANCEL_STATUS;

		for (Object obj : sel.toArray()) {
			if(obj instanceof IFile) {
				IFile src = (IFile) obj;
				String folder = getFolderForExtension(src);
				Shell shell = Display.getCurrent().getActiveShell();
				// -- TDI Project
				try {
					IProject project = ((IResource) aTarget).getProject();
					IFolder resources = project.getFolder(TDINature.RESOURCES_FOLDER);
					if (!resources.exists())
						resources.create(true, true, null);
				
					boolean isAL = "assemblyline".equals(src.getFileExtension());
					
					// -- Subfolder in Resources directory
					IFolder subdir = resources.getFolder(folder);
					if (!subdir.exists() && !isAL)
						subdir.create(true, true, null);
					
					IFile tar = null;
					if(isAL)
						tar = project.getFolder(folder).getFile(src.getName());
					else
						tar = subdir.getFile(src.getName());
					
					if(tar.exists()) {
						if(!MessageDialog.openQuestion(
								shell,
								Messages.getString("general.save.library.label"),
								Messages.getMessage("general.resource.exists", new Object[]{tar.getFullPath().toOSString(), 
										Utils.dateToString(tar.getLocalTimeStamp())})
							))
							continue;
						tar.setContents(src.getContents(), true, false, null);
					} else {
						src.copy(tar.getFullPath(), true, null);
					}
					
					// -- copy inherited components
					try {
						TDIConfigurationFile cfg = TDIConfigurationFile.loadFile(src);
						for(String str : cfg.getDefaultConfigObject().getReferences(null)) {
							if(str.startsWith("system:"))
								continue;
							BaseConfiguration cc = (BaseConfiguration) cfg.lookup(str);
							try {
								Utils.createFileFromConfig(aTarget, Utils.getFolderForConfig(cc), cc, getShell(), false);
							} catch (Exception e) {
								EclipseAppender.logerror(e.toString(), e, getShell());
							}
						}
					} catch (Exception e) {
						EclipseAppender.logerror(e.toString(), e, getShell());
					}
					
				} catch (Exception e) {
					EclipseAppender.logerror(e.toString(), e, shell);
				}
				
			} else if (obj instanceof BaseConfiguration) {
				BaseConfiguration bc = (BaseConfiguration) obj;
				String folder = Utils.getFolderForConfig(bc);
				if (folder != null) {
					try {
						Utils.createFileFromConfig(aTarget, folder, bc, getShell());
					} catch (Exception e) {
						EclipseAppender.logerror(e.toString(), e, getShell());
					}
				}
			}
		}

		return Status.OK_STATUS;
	}

	@Override
	public IStatus validateDrop(Object target, int operation, TransferData transferType) {
		if (LocalSelectionTransfer.getTransfer().isSupportedType(transferType)) {

			// Check that dragged items are valid
			if (!validateDragItems(target))
				return Status.CANCEL_STATUS;

			// Check target container - must be TDI project and not TDI Servers project
			if (target instanceof IContainer) {
				try {
					if (((IContainer) target).getProject().hasNature(TDINature.TDI_NATURE_ID)) {
						if (target != Utils.getTDIServersProject(false))
							return Status.OK_STATUS;
					}
				} catch (Exception e) {
					return EclipseAppender.statusException(e);
				}
			}
		}
		return Status.CANCEL_STATUS;
	}

	/**
	 * Verifies that the list of dragged items all belong to the same folder.
	 * 
	 * @param target
	 * @return true if all items belong to the same folder
	 */
	private boolean validateDragItems(Object target) {
		IStructuredSelection sel = (IStructuredSelection) LocalSelectionTransfer.getTransfer().getSelection();
		if (sel == null || sel.isEmpty()) {
			return true; // Just in case ... we handle it in the drop regardless
		}

		boolean dropOnProject = target instanceof IProject;
		if(!dropOnProject && target instanceof IResource) {
			IProject p = ((IResource)target).getProject();
			if (p == null)
				return false;
			if(p.getFolder(TDINature.RESOURCES_FOLDER) == target)
				dropOnProject = true;
		}

		for (Object obj : sel.toArray()) {
			String folder = null;
			if(obj instanceof IFile) {
				folder = getFolderForExtension((IFile)obj);
			} else if (obj instanceof BaseConfiguration) {
				folder = Utils.getFolderForConfig((BaseConfiguration) obj);
			}
			
			if (folder == null)
				return false;
			
			// -- allow dropping on any TDI folder
			if(!dropOnProject && !isProjectFolder(folder))
				return false;
		}
		return true;
	}

	private boolean isProjectFolder(String folder) {
		for(String str : FILE_EXTENSION_FOLDERS) {
			if(str.equals(folder))
				return true;
		}
		return false;
	}

	private String getFolderForExtension(IFile obj) {
		String ext = ((IFile)obj).getFileExtension();
		String[] xt = TDIConfigurationFile.FILE_EXTENSIONS;
		for(int i = 0; i < xt.length; i++) {
			if(xt[i].equals(ext)) {
				return FILE_EXTENSION_FOLDERS[i];
			}
		}
		return null;
	}

}
