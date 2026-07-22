/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.jobs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.PropertyManager;
import com.ibm.di.config.interfaces.PropertyStoreConfig;
import com.ibm.di.config.xml.MetamergeConfigXML;
import com.ibm.di.entry.Entry;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.server.RestServerAPI;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.builders.ProjectRuntimeDirectory;

public class TDIPropertiesJob extends Job {
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private String server;
	private BaseConfiguration config;
	private boolean download;
	private Entry entry;

	/**
	 * This job transfers properties between the CE and the remote server. If
	 * the configuration is a private property store, we transfer the
	 * configuration and start a temporary instance to properly read/write the
	 * property store. For shared property stores (e.g. global, system etc) we
	 * simply set the properties followed by a commit.
	 * <p>
	 * The provided configuration can either be a ContainerConfig that contains
	 * BaseConfiguration objects providing the name, local and remote value. In
	 * this case we only transfer those properties where the local value is
	 * different from the remote value.
	 * 
	 * If you pass a BaseConfiguration object we cycle through each member using
	 * the name/value as the property name and value.
	 * 
	 * @param server
	 *            The server to exchange property values
	 * @param config
	 *            This can be a ContainerConfig with (Name,Local,remote)
	 *            contents or a BaseConfig with name/value pairs
	 * @param download
	 *            Specify true to download or false to upload
	 */
	public TDIPropertiesJob(String server, BaseConfiguration config, boolean download) {
		super(server);
		this.server = server;
		this.config = config;
		this.download = download;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		if (! download && entry.size() == 0)
			return Status.OK_STATUS;
		try {
			RestServerAPI api = RestServerAPI.createInstance(server);
			String store = config.getShortName();
			if (store.endsWith(".tdiproperties")) //$NON-NLS-1$
				store = store.substring(0, store.indexOf(".tdiproperties")); //$NON-NLS-1$

			boolean transferConfig = true;
			
			String[] stdStores = new String[]{
					PropertyManager.STDCOLL_GLOBAL,
					PropertyManager.STDCOLL_JAVA,
					PropertyManager.STDCOLL_SYSTEM,
					PropertyManager.STDCOLL_SOLUTION
			};

			// -- CE is case insensitive
			for(String str : stdStores) {
				if(str.equalsIgnoreCase(store)) {
					store = str;
					transferConfig = false;
				}
			}
			
			//
			// Create URL
			//
			InputStream mc = null;
			if (transferConfig) {
				mc = getMC(store);
			}

			if (download) {
				entry = api.getProperties(store, mc);
			} else {
				entry = api.setProperties(store, entry, mc);
			}
			
		} catch (Exception e) {
			return EclipseAppender.statusException(e);
		}
		return Status.OK_STATUS;
	}

	/**
	 * Create a temporary instance to do custom property store updates
	 * 
	 * @param store
	 *            The store name
	 * @return The instance id
	 * @throws Exception
	 */
	private InputStream getMC(String store) throws Exception {
		MetamergeConfigXML mc = new MetamergeConfigXML();
		mc.initializeConfig();
		PropertyManager pm = (PropertyManager) ((ContainerConfig) config).getConfig("Config"); //$NON-NLS-1$
		PropertyStoreConfig psc = (PropertyStoreConfig) pm.getPropertyStores().getConfig(0).getClone();
//		PropertyStoreConfig psc = (PropertyStoreConfig) pm.getPropertyStore("Default").getClone(); //$NON-NLS-1$
		psc.setName(store);

		PropertyManager mc_pm = (PropertyManager) mc.lookup(MetamergeConfig.DEFAULT_PROPSTORE_FOLDER);
		mc_pm.addPropertyStore(psc);
		
		//defect 13212
		mc.getRootElement().setAttribute(MetamergeConfigFactory.MC_CONFIG_DIRECTORY,
				new ProjectRuntimeDirectory(Utils.getProjectFor(config)).getFolder().getLocation().toPortableString());

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		mc.commitChanges(bos);
		return new ByteArrayInputStream(bos.toByteArray());
	}

	public Entry getEntry() {
		return entry;
	}
	
	public void setEntry(Entry e) {
		entry = e;
	}
}
