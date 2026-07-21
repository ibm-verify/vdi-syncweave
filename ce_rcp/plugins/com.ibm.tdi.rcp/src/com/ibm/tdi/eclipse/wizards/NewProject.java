/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.wizards;

import java.net.URI;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.builders.IncrementalConfigBuilder;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.natures.TDINature;

public class NewProject extends Wizard implements INewWizard {
	@SuppressWarnings("unused") //$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private WizardNewProjectCreationPage mainPage;

	private IProject project;

	public NewProject() {
	}

	@Override
	public boolean performFinish() {
		project = mainPage.getProjectHandle();
		try {
			
			// get a project descriptor
			URI location = null;
			if (!mainPage.useDefaults()) {
				location = mainPage.getLocationURI();
			}

			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IProjectDescription description = workspace
					.newProjectDescription(project.getName());
			description.setLocationURI(location);

			// Create and open the project
			project.create(description, null);
			project.open(null);
			
			// Configure Nature - must be done to an open project (no effect during create project)
			String[] natures = description.getNatureIds();
			String[] newNatures = new String[natures.length + 1];
			System.arraycopy(natures, 0, newNatures, 0, natures.length);
			newNatures[natures.length] = TDINature.TDI_NATURE_ID;
			description.setNatureIds(newNatures);

			// Configure builder
			ICommand[] commands = description.getBuildSpec();
			ICommand command = description.newCommand();
			command.setBuilderName(IncrementalConfigBuilder.BUILDER_ID);
			ICommand[] newCommands = new ICommand[commands.length + 1];

			System.arraycopy(commands, 0, newCommands, 1, commands.length);
			newCommands[0] = command;
			description.setBuildSpec(newCommands);
			
			project.setDescription(description, null);
			return true;
			
		} catch (Exception e) {
			EclipseAppender.logerror(Messages.getString("NewProject.1") + mainPage.getProjectName(), e); //$NON-NLS-1$
			return false;
		}
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle(Messages.getString("NewProject.2")); //$NON-NLS-1$
	}

	@Override
	public void addPages() {
		mainPage = new WizardNewProjectCreationPage("basicNewProjectPage"); //$NON-NLS-1$
		mainPage.setTitle(Messages.getString("NewProject.4")); //$NON-NLS-1$
		mainPage.setDescription(Messages.getString("NewProject.5")); //$NON-NLS-1$
		addPage(mainPage);
	}

	public IProject getProject() {
		return project;
	}
}
