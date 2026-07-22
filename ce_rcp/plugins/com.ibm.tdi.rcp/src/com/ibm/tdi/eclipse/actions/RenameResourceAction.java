/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions;

import java.util.List;

import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.undo.MoveResourcesOperation;

import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.actions.operations.RefactorOperation;
import com.ibm.tdi.eclipse.actions.operations.RenamePropertiesOperation;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.natures.TDINature;
import com.ibm.tdi.eclipse.wizards.NewPropertiesWizard;
import com.ibm.tdi.eclipse.wizards.RenameComponentWizard;
import com.ibm.tdi.eclipse.wizards.pages.RenameComponentPage;

/**
 * This class is used instead of the standard eclipse Rename/Move class to make
 * ctrl-c/v in the file name dialog.
 */
public class RenameResourceAction extends BaseAction {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	private String targetName;

	public RenameResourceAction() {
	}

	public void run(IAction action) {
		final IResource res = (IResource) getFirstSelection();
		final String orgExtension = res.getFileExtension();
		String orgName = res.getName();

		if (isStdExtension(orgExtension)) {
			orgName = orgName.substring(0, orgName.lastIndexOf("."));
		}

		IInputValidator validator = new IInputValidator() {
			public String isValid(String newText) {
				String newname = completeFileName(newText, orgExtension, res);
				if (res instanceof IProject && ((IProject) res).getWorkspace().getRoot().getProject(newname).exists())
					return Messages.getString("RenameConfigAction.AlreadyExists");
				else if (res.getParent() instanceof IFolder && ((IFolder) res.getParent()).getFile(newname).exists())
					return Messages.getString("RenameConfigAction.AlreadyExists");
				else if ("tdiproperties".equals(orgExtension))
					return NewPropertiesWizard.VALIDATOR.isValid(newText);
				else
					return null;
			}
		};

		String newFilename = null;
		List<IFile> refactorList = null;
		
		if (RenameComponentPage.canRefactor(res)) {
			RenameComponentWizard wiz = new RenameComponentWizard(res, validator);
			WizardDialog dlg = new WizardDialog(getShell(), wiz);
			if (dlg.open() == Window.CANCEL)
				return;

			newFilename = wiz.getNewFileName();
			refactorList = wiz.getRefactorList();
		} else {
			InputDialog idd = new InputDialog(getShell(), action.getText(),
					Messages.getString("RenameWorkAttributeItem.1"), orgName, validator); //$NON-NLS-1$
			if (idd.open() == Window.CANCEL)
				return;
			newFilename = idd.getValue();
		}

		try {
			String newname = completeFileName(newFilename, orgExtension, res);
			if (isStdExtension(orgExtension)) {
				if (!newname.endsWith("." + orgExtension))
					newname += "." + orgExtension;
			} else {
				if (newname.indexOf(".") == -1 && //$NON-NLS-1$
						res.getFileExtension() != null && res.getProject() != res)
					newname += "." + res.getFileExtension(); //$NON-NLS-1$
			}

			if (newname.equals(res.getName()))
				return;

			IPath newPath = res.getFullPath().removeLastSegments(1).append(newname);
			IResource current = res.getParent().findMember(newname);
			if (current != null) {
				String str = Messages.getMessage("general.resource.exists",
						new Object[] { newname, Utils.dateToString(current.getLocalTimeStamp()) });
				if (!MessageDialog.openConfirm(getShell(), action.getText(), str))
					return;
			}

			IUndoContext undo = PlatformUI.getWorkbench().getOperationSupport().getUndoContext();

			// -- Suspend auto-build to prevent auto-create of the files we
			// move
			Job.getJobManager().sleep(ResourcesPlugin.FAMILY_AUTO_BUILD);

			// -- When we rename a TDI project we rename the Runtime-*
			// folder and the Runtime-*/<project>.xml file as well
			if (res instanceof IProject && ((IProject) res).hasNature(TDINature.TDI_NATURE_ID)) {

				IProject project = (IProject) res;
				IFolder rt = project.getFolder(Messages.getString("RuntimeProjectPrefix") + "-" + project.getName());
				if (rt.exists()) {
					IFile rs = rt.getFile(project.getName() + ".xml");
					if (rs.exists()) {
						IPath path = rs.getFullPath().removeLastSegments(1).append(newname + ".xml");
						MoveResourcesOperation mro = new MoveResourcesOperation(rs, path, action.getText());
						mro.addContext(undo);
						try {
							OperationHistoryFactory.getOperationHistory().execute(mro, null, null);
						} catch (Exception e) {
							EclipseAppender.logerror(e.toString(), e);
						}
					}

					IPath path = rt.getFullPath().removeLastSegments(1)
							.append(Messages.getString("RuntimeProjectPrefix") + "-" + newname);
					MoveResourcesOperation mro = new MoveResourcesOperation(rt, path, action.getText());
					mro.addContext(undo);
					try {
						OperationHistoryFactory.getOperationHistory().execute(mro, null, null);
					} catch (Exception e) {
						EclipseAppender.logerror(e.toString(), e);
					}
				}

				IFolder propsFolder = project.getFolder(TDIConfigurationFile.RESOURCES_FOLDER).getFolder(
						MetamergeConfig.DEFAULT_PROPSTORE_FOLDER);
				if (propsFolder.exists()) {
					String propFileExt = "." + TDIConfigurationFile.XT_PROPSTORE;
					IFile oldPropName = propsFolder.getFile(project.getName() + propFileExt);
					if (oldPropName.exists()) {
						IPath newPropName = oldPropName.getFullPath().removeLastSegments(1).append(newname + propFileExt);
						MoveResourcesOperation mro = new MoveResourcesOperation(oldPropName, newPropName, action.getText());
						mro.addContext(undo);
						try {
							OperationHistoryFactory.getOperationHistory().execute(mro, null, null);
						} catch (Exception e) {
							EclipseAppender.logerror(e.toString(), e);
						}
					}
				}
			}

			try {
				AbstractOperation op;
				if(refactorList != null && refactorList.size() > 0) {
					op = new RefactorOperation(action.getText(), (IFile) res, ((IFolder)res.getParent()).getFile(newname), refactorList);
				} else if (TDIConfigurationFile.XT_PROPSTORE.equals(orgExtension)) {
					op = new RenamePropertiesOperation(action.getText(), (IFile) res, ((IFolder)res.getParent()).getFile(newname));
				} else {
					op = new MoveResourcesOperation(res, newPath, action.getText());
				}
				op.addContext(undo);
				OperationHistoryFactory.getOperationHistory().execute(op, null, null);
				setNewName(newname);
				
			} finally {
				// -- Resume auto-build
				Job.getJobManager().wakeUp(ResourcesPlugin.FAMILY_AUTO_BUILD);
			}

		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e, getShell());
		}
	}

	private void setNewName(String name) {
		this.targetName = name;
	}

	public String getNewName() {
		return targetName;
	}

	protected String completeFileName(String filename, String orgExtension, IResource res) {
		String newname = filename;
		if (isStdExtension(orgExtension)) {
			if (!newname.endsWith("." + orgExtension))
				newname += "." + orgExtension;
		} else {
			if (newname.indexOf(".") == -1 && //$NON-NLS-1$
					res.getFileExtension() != null && res.getProject() != res)
				newname += "." + res.getFileExtension(); //$NON-NLS-1$
		}
		return newname;
	}

	private boolean isStdExtension(String extension) {
		if ("tdiserver".equals(extension))
			return true;

		for (String str : TDIConfigurationFile.FILE_EXTENSIONS) {
			if (str.equals(extension))
				return true;
		}
		return false;
	}
}
