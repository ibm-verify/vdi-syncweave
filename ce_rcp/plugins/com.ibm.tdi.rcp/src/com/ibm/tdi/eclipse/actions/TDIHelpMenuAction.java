/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions;

import java.io.File;
import java.util.Locale;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.ibm.tdi.eclipse.Activator;
import com.ibm.tdi.eclipse.ConfigUtils;
import com.ibm.tdi.eclipse.Messages;

public class TDIHelpMenuAction implements IWorkbenchWindowActionDelegate {
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	
	private static final String GOTO_DOCUMENTATION = "welcome"; //$NON-NLS-1$

	private static final String GOTO_GETTING_STARTED = "started-introducing-verify-directory-integrator"; //$NON-NLS-1$

	private IWorkbenchWindow window;

	public void dispose() {
	}

	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	public void run(IAction action) {
		String id = action.getActionDefinitionId();
		if("com.ibm.tdi.open.external.javadocs".equals(id))
			 ConfigUtils.showURL("file://" + Activator.getInstallPath() + File.separator + "docs/api/index.html"); //$NON-NLS-1$ //$NON-NLS-2$
		else if("com.ibm.tdi.open.external.gettingstarted".equals(id))
			 openDocumentation(GOTO_GETTING_STARTED);
		else if("com.ibm.tdi.open.external.docs".equals(id))
			 openDocumentation(GOTO_DOCUMENTATION);
		else if("com.ibm.tdi.open.external.javascript".equals(id))
			showJavaScriptHelp();
		else if("com.ibm.tdi.help.about.version".equals(id))
			showTDIVersionInfo();
		else if("com.ibm.tdi.help.learning.academy".equals(id))
			 ConfigUtils.showURL("https://www.securitylearningacademy.com/local/navigator/index.php?level=iadi01"); //$NON-NLS-1$
	}
	
	public void showJavaScriptHelp() {
		File file = new File(Activator.getInstallPath() + File.separator + "jscript");
		File jsf = null;
		Locale locale = Locale.getDefault();
		if (locale != null) {
			String lang = locale.getLanguage();
			String country = "_" + locale.getCountry();
			jsf = new File(file, lang + country + "/javascript.html");
			if(!jsf.exists()) {
				jsf = new File(file, lang + "/javascript.html");
				if(!jsf.exists())
					jsf = null;
			}
		}
		// -- fallback on english locale
		if(jsf == null)
			jsf = new File(file, "en/javascript.html");

		ConfigUtils.showURL("file://" + jsf.getAbsolutePath()); //$NON-NLS-1$ //$NON-NLS-2$		
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

	private void openDocumentation(String url) {
		
		String helpHost = System.getProperty("com.ibm.di.helpHost");

		if (helpHost != null && helpHost.length() > 0) {
			String helpFolder = "";
			int i = helpHost.indexOf('/');
			if (i > 0) {
				helpFolder = helpHost.substring(i);
				helpHost = helpHost.substring(0, i);
			}
			String helpPort = System.getProperty("com.ibm.di.helpPort");
			if (helpPort != null && helpPort.length() > 0)
				helpPort = ":" + helpPort;
			else
				helpPort = "";

			ConfigUtils.showURL("http://" + helpHost + helpPort + helpFolder + "?topic=" + url);
		}
	}

	private void showTDIVersionInfo() {
		String version = Activator.getDefault().getBundle().getVersion().toString();
		String msg = Messages.getString("TDI.CE.Version") + ": " + version;
		MessageDialog.openInformation(window.getShell(), Messages.getString("TDI.CE.Version"), msg); 
	}

}
