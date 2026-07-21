/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.jobs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Locale;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.ibm.di.function.SystemFunctions;
import com.ibm.icu.text.DateFormat;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.console.AssemblyLineConsole;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.natures.TDINature;
import com.ibm.tdi.eclipse.server.RestServerAPI;

public class StartLocalServerJob extends Job {
	@SuppressWarnings("unused") //$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public static final String ID = "StartLocalServerJob";
	
	public static final int SERVER_STARTING = 1;

	public static final int SERVER_RUNNING = 2;

	public static final int SERVER_STOPPED = 3;

	public static final String TDI_WORKDIR = "workdir"; //$NON-NLS-1$

	public static final String TDI_INSTALL = "install"; //$NON-NLS-1$

	public static final String TDI_ADDRESS = "address"; //$NON-NLS-1$

	public static final String TDI_API = "apion"; //$NON-NLS-1$

	public static final String TDI_SSL = "ssl"; //$NON-NLS-1$

	private IFile server = null;

	private RestServerAPI api;

	// -- Current status, Starting, Running and Stopped
	private int status = 0;

	// -- This is the process handle for the launched TDI server 
	private Process tdiProcess;
	
	// This is the last process we tried to start
	private static Process lastProcessStarted;

	// -- Set by the process stream reader when the process has terminated
	private boolean loggerTerminated = false;

	private DateFormat dateFormatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
	
	public StartLocalServerJob(IFile server) {
		super(server.getName());
		this.server = server;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {

		try {

			this.api = RestServerAPI.createInstance(server);

			status = SERVER_STARTING;

			// Default server
			if (server == null)
				server = (IFile) Utils.getTDIServer(TDINature.DEFAULT_SERVER_NAME);

			if (!server.exists())
				throw new FileNotFoundException(server.getProjectRelativePath().toPortableString());

			// Try a ping to the server
			try {
				api.ping();
				setStatus(SERVER_RUNNING);
				return Status.OK_STATUS;
			} catch (java.net.ConnectException ignore) {
			}

			startServerCMD(api);

			while(true) {
				try {
					api.ping();
					setStatus(SERVER_RUNNING);
					break;
				} catch (Exception e) {
					Thread.sleep(5000);
				}
				
				// -- If the logger thread terminates we have nothing more to do
				// -- since the process most likely has terminated.
				if(loggerTerminated)
					return Status.OK_STATUS;
				
				// -- Check if the user has canceled this job
				if(monitor != null && monitor.isCanceled()) {
					// -- no reason to kill the shell command, the shell starts a new java process so killing the shell won't
					// -- make a difference. If/when we launch the server with a java command we can activate this code.
//					new Thread() {
//						public void run() {
//							// -- kill the server 
//							killServer();
//						}
//					}.start();
					return Status.OK_STATUS;
				}
			}
		} catch (Exception e) {
			return EclipseAppender.statusException(e);
		}

		return Status.OK_STATUS;
	}

	/**
	 * Creates the system queue (createDefaultMQConfig) and launches the TDI server in daemon mode (-d param).
	 * A new AssemblyLineConsole is created to display the command output in a separate thread.
	 * 
	 * @param api
	 * @throws Exception
	 */
	public void startServerCMD(final RestServerAPI api) throws Exception {

		// Program Arguments
		ProcessBuilder builder;
		if (System.getProperty("os.name").toLowerCase().indexOf("windows") == -1) { //$NON-NLS-1$ //$NON-NLS-2$
			builder = new ProcessBuilder(api.getInstall() + File.separator + "ibmdisrv", "-d"); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			builder = new ProcessBuilder("cmd.exe", "/c", api.getInstall() + File.separator + "ibmdisrv.bat", "-d"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}
		
		builder.environment().put("TDI_SOLDIR", "."); //$NON-NLS-1$ //$NON-NLS-2$
		builder.environment().put("TDI_API_EXITONERR", "true"); //$NON-NLS-1$ //$NON-NLS-2$
		builder.directory(new File(api.getWorkdir()));
		builder.redirectErrorStream(true);

		// Start process
		tdiProcess = builder.start();
		setLastProcessStarted(tdiProcess);
		
		new Thread() {

			@Override
			public void run() {
				AssemblyLineConsole console;
				String defname = TDINature.DEFAULT_SERVER_NAME + ".tdiserver";
				if(defname.equals(api.getName()))
					console = new AssemblyLineConsole(Messages.getString("StartLocalServerJob.title")); //$NON-NLS-1$
				else
					console = new AssemblyLineConsole(api.getName());
					
				// -- Don't activate - if welcome page is open this will mess up the display
				// console.activate();
				
				BufferedReader buf = null;
				if (System.getProperty("os.name").toLowerCase().indexOf("windows") != -1) {
					// Windows has two different codepages.
					// Under some circumstances java will write out in cp850, but try to read back e.g. in cp1252.
					// Try to work around this.
					String encoding = System.getProperty("console.encoding");
					if (encoding == null) {
						// Try to guess, impossible to really know what to use...
						Locale locale = Locale.getDefault();
						if (locale != null) { 
							String lang = locale.getLanguage();
							if ("fr".equals(lang) || "es".equals(lang) || "pt".equals(lang) )
								encoding = "cp858";
							else if ("tr".equals(lang))
								encoding = "cp857";
						}
					}
					if (encoding != null) {
						try {
							buf = new BufferedReader(new InputStreamReader(tdiProcess.getInputStream(), encoding));
						} catch (UnsupportedEncodingException ignore) {
							// Fall into code below.
							SystemFunctions.doNothing();
						}
					}
				}
				if (buf == null)
					buf = new BufferedReader(new InputStreamReader(tdiProcess.getInputStream()));
				
				try {
					console.logmsg(timeStamp(Messages.getString("StartLocalServerJob.launch_message"))); //$NON-NLS-1$
					String str;
					while ((str = buf.readLine()) != null) {
						console.logmsg(timeStamp(str));
					}
					try {
						// -- on unix hosts the process is often not terminated after the null from the input stream
						tdiProcess.waitFor();
						// -- at this point we can safely say the process has terminated
						setStatus(SERVER_STOPPED);
					} catch (Exception e) {
						SystemFunctions.doNothing();
					}
					console.logmsg(timeStamp(Messages.getMessage("StartLocalServerJob.exit_code", String.valueOf(tdiProcess.exitValue())))); //$NON-NLS-1$
				} catch (Exception e) {
					EclipseAppender.logerror(e.toString(), e);
				} finally {
					try {
						buf.close();
					} catch (IOException ignore) {
					}
				}
				setLoggerTerminated(true);
			}

		}.start();
		
	}

	protected void setLoggerTerminated(boolean terminated) {
		this.loggerTerminated = terminated;
	}

	private String timeStamp(String msg) {
		if (msg.length() == 0)
			return "\n";
		return dateFormatter.format(new Date()) + " " + msg + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void setStatus(int status) {
		this.status = status;
	}

	public int getStatus() {
		return status;
	}

	public boolean isStarting() {
		return getStatus() == SERVER_STARTING;
	}

	public boolean isRunning() {
		return getStatus() == SERVER_RUNNING;
	}

	public void killServer() {
		if (tdiProcess != null)
			tdiProcess.destroy();
	}

	public Process getTdiProcess() {
		return tdiProcess;
	}
	
	/**
	 * Return true if at least one server was started by this class.
	 */
	public static boolean hasStartedServer() {
		return lastProcessStarted != null;
	}

	@Override
	public boolean belongsTo(Object family) {
		return ID.equals(family);
	}
	
	public static void setLastProcessStarted(Process p) {
		lastProcessStarted = p;
	}
	
	public static void forciblyTerminateLastProcess() {
		if (lastProcessStarted != null)
			lastProcessStarted.destroy();
	}
}
