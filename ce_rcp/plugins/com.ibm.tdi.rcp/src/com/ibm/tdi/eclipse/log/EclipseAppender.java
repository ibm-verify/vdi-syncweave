/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.log;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.Bundle;

import com.ibm.tdi.eclipse.Activator;
import com.ibm.tdi.eclipse.Messages;


@Plugin(name="EclipseAppender", category ="Core", elementType=Appender.ELEMENT_TYPE, printObject=true)
public class EclipseAppender extends AbstractAppender {
	@SuppressWarnings("unused") //$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private Bundle bundle;
	private String[] suppressMessages = new String[]{
		"CTGDKE224W", "CTGDKE041W",	 // Missing keys in property stores are handled differently 	 //$NON-NLS-1$ //$NON-NLS-2$
	};
	
//	private String[] problemMessages = new String[]{
//		"CTGDKE102E", // Cannot setup inheritance
//	};
	
	public EclipseAppender(Bundle bundle, String pluginId) {
		super(pluginId, null, null, true, Property.EMPTY_ARRAY);
		this.bundle = bundle;
	}

	public void append(final LogEvent event) {
		Throwable t = event.getThrown();
		
		final int severity;
		
		if (event.getLevel().isMoreSpecificThan(Level.ERROR))
			severity = Status.ERROR;
		else if (event.getLevel().isMoreSpecificThan(Level.WARN))
			severity = Status.WARNING;
		else if (event.getLevel().isMoreSpecificThan(Level.INFO))
			severity = Status.INFO;
		else
			severity = Status.OK;

		//
		// -- Some problems are dealt with differently in the CE so we suppress these
		//
		String msg = "" + event.getMessage(); //$NON-NLS-1$
		for(String str : suppressMessages) {
			if(msg.startsWith(str))
				return;
		}
		
		//
		// -- Some problems should go in the Problems view with a proper marker
		// 
//		for(String str : problemMessages) {
//			if(msg.startsWith(str))
//				addProblem(msg);
//		}
		
		Status status = new Status(severity, getName(), 0, msg, t);
		Platform.getLog(bundle).log(status);
	}
	
	/**
	 * This method tries to identify the type of error from the message identifier and text
	 * and issue a problem marker in the Problems view. If one already exist for a given marker
	 * the message is discarded.
	 * @param msg
	 */
	public static void addProblem(String msg) {
		if(msg.startsWith("CTGDKE102E")) {
			addInheritanceProblem(msg);
		}
	}

	/**
	 * @param msg
	 */
	public static void addInheritanceProblem(String msg) {
		// CTGDKE102E rujuta.DataFlowContainer.xyz Cannot setup inheritance: /Connectors/xyz.
		String str = msg.substring(11);
		str = str.substring(0, str.indexOf("Cannot setup inheritance"));
		
		
		String[] nodes = str.split("\\.");
		if(nodes != null) {
			String name = nodes[0];
			System.out.println("Name: " + name);
		}
	}

	public static Status logerror(String msg, Throwable t) {
		try {
			Bundle bundle = ResourcesPlugin.getPlugin().getBundle();
			if (bundle == null)
				return null;
			Status status = new Status(Status.ERROR, Activator.TDI_PLUGIN_ID, 0, msg, t);
			Platform.getLog(bundle).log(status);
			return status;
		} catch (Exception e) {
			t.printStackTrace();
		}
		return null;
	}

	/**
	 * Logs the error message to the bundle log file and shows an ErrorDialog with the logged error.
	 * @param msg message
	 * @param t The exception object
	 * @param shell
	 */
	public static void logerror(String msg, Throwable t, Shell shell) {
		logerror(msg, t);
		showError(msg, t, shell);
	}
	
	/**
	 * Shows an ErrorDialog with the exception error
	 * @param msg message
	 * @param t The exception object
	 * @param shell
	 */
	public static void showError(final String msg, Throwable t, final Shell shell) {
		final Status s = statusException(t);
		final String title = Messages.getString("general.error.label"); //$NON-NLS-1$
		if (shell == null) {
			ErrorDialog.openError(shell, title, msg, s);
		} else {
			shell.getDisplay().syncExec(new Runnable() {
				public void run() {
					ErrorDialog.openError(shell, title, msg, s);
				}		
			});
		}
	}
	
	public static void loginfo(String msg) {
		try {
			Bundle bundle = ResourcesPlugin.getPlugin().getBundle();
			Status status = new Status(Status.INFO, Activator.TDI_PLUGIN_ID, 0, msg, null);
			Platform.getLog(bundle).log(status);			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void logwarning(String msg) {
		try {
			Bundle bundle = ResourcesPlugin.getPlugin().getBundle();
			Status status = new Status(Status.WARNING, Activator.TDI_PLUGIN_ID, 0, msg, null);
			Platform.getLog(bundle).log(status);			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Status statusException(Throwable e) {
		String msg = e.getLocalizedMessage();
		// toString() is arguably better than getMessage(), as it shows the errors that caused this error.
		if (msg == null || msg.equals(e.getMessage()))
			msg = e.toString(); 
		// Old Windows 2K shows some strange characters for linefeed/tab/return.
		// Just remove or replace with space
		msg = msg.replace("\r", "");
		msg = msg.replace("\n", " ");
		msg = msg.replace("\t", " ");
		return new Status(Status.ERROR, Activator.TDI_PLUGIN_ID, Status.ERROR, msg, e);
	}

	public static CoreException coreException(Throwable e) {
		return new CoreException(statusException(e));
	}
}
