/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.eclipse;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.naming.Binding;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

import com.ibm.di.config.base.PropertyStoreConfigImpl;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.DefaultConfigChangeListener;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.MetamergeFolder;
import com.ibm.di.config.interfaces.NamespaceConfig;
import com.ibm.di.config.interfaces.PropertyManager;
import com.ibm.di.config.interfaces.PropertyStoreConfig;
import com.ibm.di.config.interfaces.TDIProperties;
import com.ibm.di.config.interfaces.TDIPropertyStore;
import com.ibm.di.function.SystemFunctions;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.natures.TDINature;
import com.ibm.tdi.eclipse.server.RestServerAPI;

/**
 * This class provides access to the property stores defined for a specific
 * configuration instance.
 */
public final class TDIPropertiesCE extends TDIProperties implements Serializable {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	static final long serialVersionUID = -3361471837888677277L;

	private MetamergeConfig mc;

	private final static String DEFAULT_STORE = "DefaultStore";

	private final static String PWD_STORE = "PasswordStore";

	private final static String COMP_LIST = "ComponentList";

	public TDIPropertiesCE() {
	}

	/**
	 * TDIProperties constructor to create instance for a specified Config.
	 * 
	 * @param mc
	 * 
	 * @throws Exception
	 */
	public TDIPropertiesCE(MetamergeConfig mc) throws Exception {
		this.mc = mc;
		pm = (PropertyManager) mc.lookup(MetamergeConfig.DEFAULT_PROPSTORE_FOLDER);

		addProjectStores();

		//
		// -- Since each project in the CE can be associated with a different server they may
		// -- also use different install/solution directories.
		//
		for(File file : getServerPropertyStores(mc)) {
			String type = null;
			if(file.getName().equals("global.properties"))
				type = PropertyManager.STDCOLL_GLOBAL;
			else if (file.getName().equals("solution.properties"))
				type = PropertyManager.STDCOLL_SOLUTION;
			if(type != null) {
				PropertyStoreConfig psc = new PropertyStoreConfigImpl();
				psc.init();
				psc.setName(type);
				psc.getConnectionConfig().setInheritsFromRef("system:/Connectors/ibmdi.Properties");
				psc.getConnectionConfig().setParameter("collectionType", "File");
				psc.getConnectionConfig().setParameter("collection", file.getAbsolutePath());
				psc.setKeyAttribute(TDIProperties.KEY_ATTRIBUTE);
				psc.setValueAttribute(TDIProperties.VALUE_ATTRIBUTE);
				psc.getConnectionConfig().setupInheritanceChain();
				// -- do not use addPropertyStore since that will set the static global/solution store variable
				// -- and share the store between all projects in the CE.
				stores.add(new TDIPropertyStore(psc, this));
			}
		}

		pm.addListener(new DefaultConfigChangeListener() {
			public void configurationChanged(MetamergeConfigChange changeEvent) {
				if (DEFAULT_STORE.equals(changeEvent.getKey())) {
					setDefaultStore();
				} else if (PWD_STORE.equals(changeEvent.getKey())) {
					setPasswordStore();
				} else if (COMP_LIST.equals(changeEvent.getKey())) {
					removeAllStores();
				}
			}
		});
		
		setDefaultStore();
		setPasswordStore();
	}

	/**
	 * Returns the MetamergeConfig to which this object belongs.
	 * 
	 * @return
	 */
	public MetamergeConfig getMetamergeConfig() {
		return mc;
	}
	
	private void addProjectStores() throws Exception {
		final IFolder fld = getPropertyStoreFolder(mc);
		if (fld == null)
			return;

		for (IResource res : fld.members()) {
			if (res.getFileExtension().equals(TDIConfigurationFile.XT_PROPSTORE))
				stores.add(new TDIPropertyStoreCE((IFile) res, this));
		}
		
		//
		// -- Listen to changes in the Properties folder for new, deleted and modified property stores
		//
		ResourcesPlugin.getWorkspace().addResourceChangeListener(new IResourceChangeListener() {
			public void resourceChanged(IResourceChangeEvent event) {
				IResourceDelta delta = event.getDelta();
				if (delta != null) {
					
					IResourceDelta resDelta = delta.findMember(fld.getFullPath());
					if (resDelta == null)
						return;
					
					IResourceDelta[] children = resDelta.getAffectedChildren();
					if(children == null)
						return;
					
					for(IResourceDelta child : children) {
						IResource res = child.getResource();
						if(res instanceof IFolder)
							continue;
						if (!res.getFileExtension().equals(TDIConfigurationFile.XT_PROPSTORE))
							continue;
						
						String storeName = res.getName().substring(0, res.getName().indexOf("."));
						try {
							switch(child.getKind()) {
							case IResourceDelta.ADDED:
								stores.add(new TDIPropertyStoreCE((IFile)res, TDIPropertiesCE.this));
								break;
							case IResourceDelta.REMOVED:
								removePropertyStore(storeName);
							}
						} catch (Exception e) {
							EclipseAppender.logerror(e.toString(), e);
						}
					}
				}
			}
		});
	}

	private void setDefaultStore() {
		// Default store
		if (pm.getDefaultPropertyStore() != null)
			setDefaultStore(getPropertyStore(pm.getDefaultPropertyStore().getShortName()));
	}

	private void setPasswordStore() {
		// Password store
		if (pm.getPasswordPropertyStore() != null)
			setPasswordStore(getPropertyStore(pm.getPasswordPropertyStore().getShortName()));
	}

	private void removeAllStores() {
		// -- This is handled by the resource notification listener.
		// -- Adding and/or removing stores should only occur as a result
		// -- of deleting the property store in the project.
	}

	/**
	 * Does a commit on all property stores
	 */
	public void commit() throws Exception {
		// -- Don't commit when running inside the CE
	}

	/**
	 * Returns the TDIProperties object associated with the key. If the key has a ref to
	 * another project/config we return that. Otherwise the defaultProps is returned.
	 * 
	 * @param key
	 * @param defaultProps
	 * @return
	 */
	protected TDIProperties selectTDIProperties(String key, TDIProperties defaultProps) {
		int at = key.lastIndexOf("@");
		if(at != -1) {
			String ref = key.substring(at+1);
			try {
				NamespaceConfig ns = (NamespaceConfig)getMetamergeConfig().lookup(MetamergeConfig.DEFAULT_NAMESPACE_FOLDER + "/" + ref);
				if(ns != null) {
					IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(ns.getURL());
					if(project.exists()) {
						return Utils.getProjectMC(project).getTDIProperties();
					}
				}
			} catch (Exception e) {
				EclipseAppender.logerror(e.toString(), e);
			}
		}
		
		// -- Let super class attempt loading etc ...
		return super.selectTDIProperties(key, defaultProps);
	}

	/**
	 * Returns a list of property stores in the config's project
	 * 
	 * @param baseConfiguration
	 * 
	 * @return  a list of property stores in the config's project
	 * @throws CoreException
	 */
	public static List<IFile> getPropertyStores(BaseConfiguration baseConfiguration) throws CoreException {
		ArrayList<IFile> list = new ArrayList<IFile>();
		IFolder folder = getPropertyStoreFolder(baseConfiguration);
		if (folder.exists()) {
			for (IResource res : folder.members()) {
				if (TDIConfigurationFile.XT_PROPSTORE.equals(res.getFileExtension()))
					list.add((IFile) res);
			}
		}
		
		// -- Also get all custom stores from projects this solution references
		try {
			list.addAll(getReferencedPropertyStores(Utils.getProjectFor(baseConfiguration)));
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e);
		}
		
		return list;
	}

	/**
	 * Returns a list of property stores referenced by the provided configuration.
	 * 
	 * @param baseConfiguration Config object used to obtain the Namespace folder
	 * @return
	 * @throws Exception
	 */
	public static List<IFile> getReferencedPropertyStores(IProject project) throws Exception {
		ArrayList<IFile> list = new ArrayList<IFile>();
		if(project == null)
			return list;
		
		MetamergeFolder refs = (MetamergeFolder) Utils.getProjectMC(project).lookup(MetamergeConfig.DEFAULT_NAMESPACE_FOLDER);
		if(refs == null)
			return list;
		
		for(Enumeration<Binding> l = refs.list(); l.hasMoreElements(); ) {
			NamespaceConfig nsc = (NamespaceConfig) l.nextElement().getObject();
			String url = nsc.getURL();
			
			// -- Check if there is a TDI project with this name
			try {
				IProject refp = ResourcesPlugin.getWorkspace().getRoot().getProject(url);
				if(refp.exists() && refp.hasNature(TDINature.TDI_NATURE_ID)) {
					IFolder folder = refp.getFolder(new Path(TDINature.RESOURCES_FOLDER + "/" + TDINature.PROPERTIES_FOLDER));
					for (IResource res : folder.members()) {
						if (TDIConfigurationFile.XT_PROPSTORE.equals(res.getFileExtension()))
							list.add((IFile) res);
					}
					continue;
				}
			} catch (Exception e) {
				SystemFunctions.doNothing();
			}
			
			// -- Try the URL as is
			try {
				MetamergeConfig mc = MetamergeConfigFactory.loadNamespace(nsc);
				for(String str : mc.getTDIProperties().getPropertyStoreNames()) {
					if(str.equals(PropertyManager.STDCOLL_GLOBAL) ||
							str.equals(PropertyManager.STDCOLL_JAVA) ||
							str.equals(PropertyManager.STDCOLL_SOLUTION) ||
							str.equals(PropertyManager.STDCOLL_SYSTEM))
						continue;
					
					TDIPropertyStore store = mc.getTDIProperties().getPropertyStore(str);
				}
			} catch (Exception e) {
				SystemFunctions.doNothing();
			}
		}
		
		return list;
	}

	/**
	 * Returns the IFolder object for the config's IProject. If it does not
	 * exist it is created.
	 * 
	 * @param config
	 * @return the IFolder object for the config's IProject. 
	 * @throws CoreException
	 */
	public static IFolder getPropertyStoreFolder(BaseConfiguration config) throws CoreException {
		IProject project = null;
		if (config.getMetamergeConfig() instanceof TDIConfigurationFile)
			project = ((TDIConfigurationFile) config.getMetamergeConfig()).getProject();
		else if (config instanceof TDIConfigurationFile)
			project = ((TDIConfigurationFile) config).getProject();
		else if (config.getMetamergeConfig() instanceof MetamergeConfigCE)
			project = ((MetamergeConfigCE)config.getMetamergeConfig()).getProject();
		else if (config instanceof MetamergeConfigCE)
			project = ((MetamergeConfigCE)config).getProject();
		
		if (project == null)
			return null;

		IFolder folder = project.getFolder(new Path(TDINature.RESOURCES_FOLDER + "/" + TDINature.PROPERTIES_FOLDER));
		if (!folder.exists())
			folder.create(true, true, null);

		return folder;
	}

	/**
	 * This method loads the property store located in the project of the
	 * "config" object.
	 * 
	 * @throws Exception
	 * 
	 */
	public static TDIConfigurationFile loadPropertyStore(BaseConfiguration config, String storeName) throws Exception {
		IFolder folder = getPropertyStoreFolder(config);
		if (folder == null)
			return null;

		IFile file = folder.getFile(storeName);
		if (!file.exists() && !storeName.endsWith("." + TDIConfigurationFile.XT_PROPSTORE))
			file = folder.getFile(storeName + "." + TDIConfigurationFile.XT_PROPSTORE);
		return TDIConfigurationFile.loadFile(file);
	}

	/**
	 * Returns a list of server property stores. These are the solution and global properties files if the
	 * provided configuration's project is associated with a server reachable via the local file system.
	 * 
	 * @param baseConfiguration
	 * @return
	 * @throws Exception
	 */
	public static List<File> getServerPropertyStores(BaseConfiguration baseConfiguration) throws Exception {
		// -- if project is running on a local server then we can add global/solution to this list
		ArrayList<File> list = new ArrayList<File>();
		IFolder folder = getPropertyStoreFolder(baseConfiguration);
		if(folder.exists()) {
			RestServerAPI api = RestServerAPI.createInstance(folder.getProject());
			File global = new File(api.getInstall() + File.separator + "etc" + File.separator + "global.properties");
			if(global.exists()) {
				list.add(global);
			}
			
			File solution = new File(api.getWorkdir() + File.separator + "solution.properties");
			if(solution.exists()) {
				list.add(solution);
			}
		}
		return list;
	}

}
