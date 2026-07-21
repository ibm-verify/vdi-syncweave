/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.console;

import java.io.IOException;
import java.net.SocketTimeoutException;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import com.ibm.icu.util.Calendar;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.natures.TDINature;
import com.ibm.tdi.eclipse.server.RestServerLogger;

public class AssemblyLineConsole implements Runnable {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private MessageConsole console;
	private MessageConsoleStream out;
	private String name;
	private Thread logThread;
	private String str;
	private RestServerLogger logger;
	private int waterMark;

	private static boolean stopped;
	
	/**
	 * Returns the console for the specified server.
	 * 
	 * @param serverName 
	 * @return
	 */
	public static AssemblyLineConsole getConsole(String serverName) {
		AssemblyLineConsole console;
		String defname = TDINature.DEFAULT_SERVER_NAME + ".tdiserver";
		if(defname.equals(serverName))
			console = new AssemblyLineConsole(Messages.getString("StartLocalServerJob.title")); //$NON-NLS-1$
		else
			console = new AssemblyLineConsole(serverName);
		return console;
	}
	
	public AssemblyLineConsole(String name) {
		super();
		this.name = name;
	}

	private MessageConsole findConsole(String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++) {
			if (name.equals(existing[i].getName()))
				return (MessageConsole) existing[i];
		}
		// -- no console found, create a new one
		final MessageConsole myConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[] { myConsole });
		final IPreferenceStore store = org.eclipse.debug.internal.ui.DebugUIPlugin.getDefault().getPreferenceStore();
		waterMark = store.getInt("Console.lowWaterMark");
		if (waterMark >= 1000) {
			myConsole.setWaterMarks(waterMark, (int) (waterMark*1.1));		
		}

		store.addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (console == null) {
					store.removePropertyChangeListener(this);
					return;
				}
				int newMark = store.getInt("Console.lowWaterMark");
				if (newMark != waterMark && newMark >= 1000) {
					waterMark = newMark;
					Display.getDefault().asyncExec(new Runnable(){
						public void run() {
							myConsole.setWaterMarks(waterMark, (int) (waterMark*1.1));		
						}					
					});
				}
				
			}		
		});

		return myConsole;
	}

	public MessageConsole getAssemblylineConsole() {
		if (console == null) {
			console = findConsole(name); // + "(" + (logCounter++) + ")");
			out = console.newMessageStream();
		}
		return console;
	}

	public MessageConsoleStream getAssemblylineLog() {
		if (out == null)
			out = getAssemblylineConsole().newMessageStream();
		return out;
	}
	
	public void logmsg(String msg) throws IOException {
		if (!isStopped())
			getAssemblylineLog().write(msg);
	}

	public void activate() {
		getAssemblylineConsole().activate();
	}

	public void consumeLog(IProject project, RestServerLogger logger) throws Exception {
		this.logger = logger;
		logThread = new Thread(this);
		logThread.start();
	}
	
	public void dispose() {
		if(logThread != null) {
			logThread.interrupt();
		}
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		conMan.removeConsoles(new IConsole[]{console});
		console = null;
	}
	
	public void run() {
		try {
			String prefix = " " + Calendar.getInstance().get(Calendar.YEAR);
			while (true) {
				try {
					str = logger.getNextMessage();
					if (str == null)
						break;

					// 2007-10-24 11:01:15,569 INFO
					// [AssemblyLine.AssemblyLines/RunOptions.assemblyline.1]
					// - CTGDIS080I Terminated successfully (0 errors).
					int b1 = str.indexOf("[AssemblyLine.AssemblyLines/");
					int b2 = str.indexOf("]");
					if (str.startsWith(prefix)) {
						if (b1 > 0 && b2 > b1) {
							str = str.substring(11, b1) + str.substring(b2 + 2);
						} else {
							str = str.substring(11);
						}
					}
					
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							try {
								logmsg(str+"\n");
							} catch(Exception ignore){}
						}
					});
				} catch (SocketTimeoutException tmo) {
				}
			}
		} catch (Exception e) {
			try {
				logmsg(Utils.exceptionText(e));
			} catch(Exception ignore){}
		} finally {
			logger.close();
		}
	}

	private static boolean isStopped() {
		return stopped;
	}
	
	public static void setStopped() {
		stopped = true;
	}
}
