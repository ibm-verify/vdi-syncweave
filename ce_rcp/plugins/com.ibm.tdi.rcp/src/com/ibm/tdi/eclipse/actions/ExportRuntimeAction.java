/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.ui.dialogs.ListSelectionDialog;

import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.InstanceConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeFolder;
import com.ibm.di.function.SystemFunctions;
import com.ibm.tdi.easyetl.ETLNavigator;
import com.ibm.tdi.eclipse.Activator;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.log.EclipseAppender;

public class ExportRuntimeAction extends BaseAction {
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private DirectoryDialog dd = new DirectoryDialog(getShell());

	public ExportRuntimeAction() {
	}

	public void run(IAction action) {
		dd.setText(Messages.getString("ExportRuntime.label"));
		dd.setMessage(Messages.getString("ExportRuntime.tooltip"));
		String path = dd.open();
		if(path == null)
			return;
		
		FileOutputStream os = null;
		try {
			IProject project = (IProject) getFirstSelection();
			String projectName = project.getName();
			IFolder rt = project.getFolder(Messages.getString("RuntimeProjectPrefix") + "-" + projectName);
			IFile rsxml = rt.getFile(projectName + ".xml");
			IFile iprop = rt.getFile(projectName + ".properties");
			File target = new File(path);
			if (!target.exists() && !target.mkdirs()) {
				throw new Exception(Messages.getMessage("ServerUtils.cannot.create.directory", target.getAbsolutePath())); //$NON-NLS-1$
			}

			File ibmdisrv = new File(Activator.getInstallPath(), "ibmdisrv.bat");
			File cmdline = new File(target, projectName + ".bat");
			File config = new File(target, rsxml.getName());
			File propfile = new File(target, iprop.getName());
			
			if(!ibmdisrv.exists()) {
				ibmdisrv = new File(Activator.getInstallPath(), "ibmdisrv");
				cmdline = new File(target, projectName);
			}
			
			if(config.exists() &&
					!MessageDialog.openConfirm(getShell(), Messages.getString("ExportRuntime.label"),
							Messages.getMessage("general.resource.exists", config.getAbsolutePath(), 
									Utils.dateToString(config.lastModified())))) {
				return;
			}
			
			// -- copy config file
			copy(rsxml, config);
			
			boolean etl = Boolean.valueOf(project.getPersistentProperty(ETLNavigator.TDI_EASYETL_PROJECT));
			
			// -- create command line
			os = new FileOutputStream(cmdline);
			String str = "\"" + ibmdisrv.getAbsolutePath() + "\" -w -c \"" + config.getAbsolutePath() + "\"";

			// -- ETL projects always have this Assemblyline ... other
			// -- projects must use auto-start settings.
			if(etl) {
				str += " -r " + projectName;
			} else {
				// for non-etl show a list of assemblylines to start (minus those present in the auto-start folder)
				MetamergeConfig mc = Utils.getProjectMC(project);
				MetamergeFolder fld = (MetamergeFolder) mc.lookup(MetamergeConfig.DEFAULT_ASSEMBLYLINE_FOLDER);
				ArrayList<String> autoStart = getAutoStartItems(mc);
				ArrayList<String> names = new ArrayList<String>();
				for(String alname : fld.getNames()) {
					if(!autoStart.contains(alname))
						names.add(alname);
				}
				if(names.size() > 0) {
					ListSelectionDialog lsd = new ListSelectionDialog(getShell(), names, new ArrayContentProvider(), new LabelProvider(), getTitle());
					lsd.setMessage(Messages.getString("config.instance.autostart"));
					if(lsd.open() == Window.OK) {
						Object[] list = lsd.getResult();
						if(list != null && list.length > 0) {
							str += " -r ";
							StringBuffer buf = new StringBuffer();
							for(Object obj : list) {
								if(buf.length() > 0)
									buf.append(",");
								buf.append(obj.toString());
							}
							str += "\"" + buf.toString() + "\"";
						}
					}
				}
			}
			
			str += "\n";
			os.write(str.getBytes());

			// -- copy properties file
			if(iprop.exists()) {
				copy(iprop, propfile);
			}
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e, getShell());
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (Exception e) {
					EclipseAppender.logerror(e.toString(), e, getShell());
				}
			}
		}
	}
	
	private ArrayList<String> getAutoStartItems(MetamergeConfig serverConfig) throws Exception {
		ArrayList<String> autoStart = new ArrayList<String>();
		
		InstanceConfig ic = (InstanceConfig) serverConfig.lookup(MetamergeConfig.DEFAULT_SERVER_FOLDER + "/"
				+ MetamergeConfig.DEFAULT_SERVER_AUTOSTART);
		ContainerConfig cc = ic.getStartupItems();
		for (int i = 0; i < cc.size(); i++) {
			BaseConfiguration b = cc.getConfig(i);
			String name = b.getStringParameter(InstanceConfig.AUTOSTART_NAME);
			try {
				BaseConfiguration c = (BaseConfiguration) serverConfig.lookup(name);
				if (c instanceof AssemblyLineConfig)
					autoStart.add(c.getShortName());
			} catch (Exception t) {
				SystemFunctions.doNothing();
			}
		}
		return autoStart;
	}
	
	private void copy(IFile source, File target) throws Exception {
		OutputStream os = null;
		InputStream is = source.getContents();
		try {
			os = new FileOutputStream(target);
			byte[] buf = new byte[1024];
			int rc = 0;
			while ( (rc = is.read(buf)) > 0) {
				os.write(buf, 0, rc);
			}
		} finally {
			try {
				is.close();
			} catch (Exception e) {
				// Cannot happen
				SystemFunctions.doNothing();
			}
			if (os != null)
				os.close();
		}
	}

}
