/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.util;

import java.util.ArrayList;
import java.util.Arrays;

import javax.naming.NameNotFoundException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;

import com.ibm.di.config.base.BaseConfigurationImpl;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.PropertyManager;
import com.ibm.di.entry.Entry;
import com.ibm.di.function.SystemFunctions;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.editors.ConfigSettingsEditor;
import com.ibm.tdi.eclipse.natures.TDINature;
import com.ibm.tdi.eclipse.server.RMIServerAPI;
import com.ibm.tdi.eclipse.server.RestServerAPI;

/**
 * A class for system store utilities.
 * 
 */
public class SystemStore {
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Returns a list of resources that have system stores defined. The
	 * resources are either IProject or IFile. An IProject is a TDI project with
	 * a custom store whereas an IFile is a TDI server from the servers project.
	 * 
	 * @return
	 * @throws Exception
	 */
	public static ArrayList<IResource> getAvailableSystemStores() throws Exception {
		ArrayList<IResource> result = new ArrayList<IResource>();

		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		for (IResource r : root.members()) {
			if (r instanceof IProject) {
				IProject p = (IProject) r;
				if (!p.isOpen())
					continue;
				if (p.hasNature(TDINature.TDI_NATURE_ID)) {
					MetamergeConfig rsxml = Utils.getProjectMC(p);
					try {
						BaseConfiguration ss = (BaseConfiguration) rsxml.lookup(MetamergeConfig.DEFAULT_SERVER_FOLDER + "/"
								+ "SystemStore");
						if (ss instanceof ContainerConfig)
							ss = ((ContainerConfig) ss).getConfig(0);
						if (ss != null && ss.getEnabled() && 
								ss.getParameter("com.ibm.di.store.jdbc.driver") != null &&
								ss.getParameter( "com.ibm.di.store.database") != null) {
							result.add(p);
						}
					} catch (NameNotFoundException nfe) {
						//If not found, don't add anything
						SystemFunctions.doNothing();
					}
				} else if ("true".equals(p.getPersistentProperty(Utils.TDI_SERVERS_PROJECT))) {
					for (IResource server : p.members()) {
						if ("tdiserver".equals(server.getFileExtension())) {
							result.add(server);
						}
					}
				}
			}
		}
		return result;
	}

	/**
	 * Returns the system store configuration for the provided resource. 
	 * 
	 * @param server
	 *            An IProject or IFile resource
	 * @return The system store configuration for the specified resource
	 * @throws Exception
	 */
	public static BaseConfiguration getServerStore(IResource server) throws Exception {
		// -- For projects with custom store we have to start a temp config
		// instance
		// -- with the properties of the project. Otherwise we can just start an
		// empty config
		// -- instance to access the system store properties.
		if (server instanceof IProject) {
			BaseConfiguration ss = (BaseConfiguration) Utils.getProjectMC((IProject) server).lookup(MetamergeConfig.DEFAULT_SERVER_FOLDER + "/" + "SystemStore");
			if (ss instanceof ContainerConfig)
				ss = ((ContainerConfig) ss).getConfig(0);
			return ss;
		}

		RMIServerAPI api = (RMIServerAPI) RestServerAPI.createInstance((IFile) server);
		Entry entry = api.getProperties(PropertyManager.STDCOLL_JAVA, null, Arrays.asList(ConfigSettingsEditor.PROPERTY_KEYS));
		BaseConfiguration config = new BaseConfigurationImpl();
		config.init();
		for (String str : entry.getAttributeNames())
			config.setParameter(str, entry.getString(str));
		return config;
	}
}
