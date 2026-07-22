/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions;

import java.io.File;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.ibm.tdi.eclipse.Activator;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.log.EclipseAppender;

public class KeyManager implements IWorkbenchWindowActionDelegate {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public void dispose() {
	}

	public void init(IWorkbenchWindow window) {
	}

	public void run(IAction action) {
		final String shellCommand = System.getProperty("com.ibm.di.shellcommand", "/bin/sh -c");  
		final String jvmDir = Activator.getInstallPath() + "/jvm";

		try {
			File f = new File(jvmDir + "/bin/ikeyman");
			if ( !f.exists() )
				f = new File(jvmDir + "/bin/ikeyman.exe");
			if ( !f.exists() )
				f = new File(jvmDir + "/jre/bin/ikeyman");
			if ( !f.exists() )
				f = new File(jvmDir + "/jre/bin/ikeyman.exe");
			if ( !f.exists() ) {
				throw new Exception(
						Messages.getMessage("KEYMANAGER.IKEYMAN.NOT.FOUND", 
								jvmDir + "/bin/", jvmDir + "/jre/bin/"));
			}

			String osname = System.getProperty("os.name");
			if(osname != null && osname.indexOf("HP-UX") >= 0)
				Runtime.getRuntime().exec (shellCommand + " " + f.getAbsolutePath());
			else
				Runtime.getRuntime().exec (f.getAbsolutePath());
		} catch (Exception error) {
			EclipseAppender.logerror(error.getMessage(), error);
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

}
