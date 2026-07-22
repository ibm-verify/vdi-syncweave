/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ide.IDE;

import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.SchedulerConfig;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.natures.TDINature;

/**
 * This class creates a schedule for an AssemblyLine.
 * @since 7.2
 */
public class NewALSchedule extends BaseAction {

	public void run(IAction action) {
		IStructuredSelection sel = (IStructuredSelection) getSelection();		
		if (! (sel.getFirstElement() instanceof IFile))
			return;
		
		// The AssemblyLine we create a schedule for.
		IFile al = (IFile)sel.getFirstElement();
		try {
			createNewFile(al.getProject(), al.getName());
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e);
		}
	}

	/**
	 * Create the new Schedule and open the editor
	 */
	private void createNewFile(final IProject project, String name) throws Exception {

		//Remove the extension
		int i = name.lastIndexOf('.');
		if (i > 0)
			name = name.substring(0, i);
		
		// -- Resources folder
		IFolder path = project.getFolder(TDINature.RESOURCES_FOLDER);
		if (!path.exists())
			path.create(true, true, null);

		// Scheduler folder
		path = path.getFolder(TDINature.SCHEDULER_FOLDER);
		if (!path.exists())
			path.create(true, true, null);

		String extension = "." + TDIConfigurationFile.XT_SCHEDULER;

		// Create unique file name
		String fileName = name;
		IFile file = path.getFile(fileName + extension);
		i = 0;
		while (file.exists()) {
			fileName = name + "_" + ++i;
			file = path.getFile(fileName + extension);
		}

		// -- Create the file
		TDIConfigurationFile cfg = new TDIConfigurationFile(file);
		SchedulerConfig sc = (SchedulerConfig) cfg.newInstanceOf(MetamergeConfig.DEFAULT_SCHEDULER_FOLDER);
		sc.setScheduledName(name);
		cfg.setDefaultConfigObject(fileName, sc);
		cfg.commitVersion(true);

		/**
		 * For some strange reason, commitVersion() may return before the new
		 * content is flushed. Add a small sleep here to help with the problem.
		 */
		Thread.sleep(200);
		IDE.openEditor(getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
	}

}
