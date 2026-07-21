/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.eclipse;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.TDIProperties;
import com.ibm.di.config.xml.MetamergeConfigXML;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.builders.ProjectRuntimeDirectory;

/**
 * This class is used to store a project's runtime configuration (rs.xml). It overrides
 * getTDIProperties to avoid committing it the default way as well as provide access to the
 * project's CE specific property stores.
 */
public class MetamergeConfigCE extends MetamergeConfigXML {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private TDIPropertiesCE tdiProperties;
	private IProject project;

	public MetamergeConfigCE(IProject project) throws Exception {
		super();
		this.project = project;
		String fileName = Utils.getRuntimeRS(project).getLocation().toOSString();
		env.put(MetamergeConfigFactory.MC_URL, fileName);
		try {
			initializeConfig();
		} catch (Exception e) {
			File f = new File(fileName);
			if (f.exists() && f.delete()) {
				project.build(IncrementalProjectBuilder.FULL_BUILD, null);
				initializeConfig();
			} else {
				throw e;
			}
		}
	}

	public MetamergeConfigCE(IFile file) throws Exception {
		super();
		this.project = file.getProject();
		env.put(MetamergeConfigFactory.MC_URL, file.getLocation().toOSString());
		initializeConfig();
	}

	public MetamergeConfigCE() {
		super();
	}

	@Override
	public TDIProperties getTDIProperties() throws Exception {
		// Manually create the stores without using the standard names. Standard names
		// cause the PropertiesConnector to act on the local environment (system store etc).
		if(tdiProperties == null) {
			tdiProperties = new TDIPropertiesCE(this);
		}
		return tdiProperties;
	}

	/**
	 * Returns the project for this MC config
	 * @return The IProject for this config
	 */
	public IProject getProject() {
		return project;
	}

	/* 
	 * Returns the the runtime directory for this configuration file. In the CE we always
	 * return "<Project>/Runtime-<Project>" as the path.
	 *  
	 * @see com.ibm.di.config.base.MetamergeConfigImpl#getDirectory()
	 */
	public String getDirectory() {
		if (getProject() == null)
			return super.getDirectory();
		
		IPath path = null;
		try {
			path = new ProjectRuntimeDirectory(getProject()).getFolder().getLocation();
		} catch (CoreException e) {
			e.printStackTrace();
		}
		if (path != null)
			return path.toOSString();
		else
			return super.getDirectory();
	}
}
