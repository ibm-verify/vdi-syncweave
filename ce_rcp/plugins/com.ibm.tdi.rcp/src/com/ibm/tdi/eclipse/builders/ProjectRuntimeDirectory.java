/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.builders;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.PropertyManager;
import com.ibm.di.config.interfaces.PropertyStoreConfig;
import com.ibm.di.connector.ConnectorInterface;
import com.ibm.di.entry.Entry;
import com.ibm.di.function.SystemFunctions;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.editors.PropertiesEditor;
import com.ibm.tdi.eclipse.log.EclipseAppender;

/**
 * This class maintains the project runtime directory. The runtime configuration
 * file and property stores are replicated to this directory.
 * 
 */
public class ProjectRuntimeDirectory {

	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private IProject project;

	public ProjectRuntimeDirectory(IProject project) {
		super();
		this.project = project;
	}

	public IFolder getFolder() throws CoreException {
		IFolder folder = project.getFolder(Messages
				.getString("RuntimeProjectPrefix")
				+ "-" + project.getName());
		if (!folder.exists())
			folder.create(true, true, null);
		return folder;
	}

	public void updateProjectConfig(IFile source) throws Exception {
		IFolder folder = getFolder();
		source.refreshLocal(IResource.DEPTH_INFINITE, null);

		IFile target = folder.getFile(project.getName() + ".xml");
		if (target.exists())
			target.setContents(source.getContents(), true, false, null);
		else
			target.create(source.getContents(), true, null);
		target.setDerived(true, null);
	}

	public void updatePropertyStore(ContainerConfig configuration,
			boolean delete) throws Exception {
		PropertyManager pm = (PropertyManager) configuration
				.getConfig("Config"); //$NON-NLS-1$
		PropertyStoreConfig ps = (PropertyStoreConfig) pm.getPropertyStores()
				.getConfig(0).getClone();
		String type = ps.getConnectionConfig().getInheritsFromRef();
		if (!"system:/Connectors/ibmdi.Properties".equals(type))
			return;

		ContainerConfig data = (ContainerConfig) configuration
				.getConfig("Data"); //$NON-NLS-1$
		File file = getPropertyStorePath(ps);
		if (file == null)
			return;

		ps.getConnectionConfig().setStringParameter("collection",file.getAbsolutePath());
		ConnectorInterface conn = SystemFunctions.loadConnector(ps);
		try {
			conn.initialize(Boolean.TRUE);
			for (BaseConfiguration item : data.getConfigurations(null)) {
				Entry entry = new Entry();

				if (item.getBooleanParameter(PropertiesEditor.DELETED, false)) {
					continue;
				}

				String name = item.getShortName();
				if (name == null)
					continue;

				boolean prot = item.getBooleanParameter(
						PropertiesEditor.LOCAL_PROTECT, false);
				entry.setAttribute("key", name);
				entry.setAttribute("value", PropertiesEditor.getLocalPropertyValue(item));
				entry.setAttribute("protect", prot);
				conn.putEntry(entry);
			}
			conn.terminate();
		} catch (Exception e) {
			EclipseAppender.logerror(e.getMessage(), e);
		}

		getFolder().refreshLocal(IResource.DEPTH_INFINITE, null);
	}

	/**
	 * Returns a java.io.File object that points to the location of the property
	 * store data file. This method expands the CONFIG_DIR expression to the
	 * correct location in the CE.
	 * 
	 * @param project
	 * @param ps
	 * @return
	 * @throws Exception
	 */
	public File getPropertyStorePath(PropertyStoreConfig ps) throws Exception {
		String path = ps.getConnectionConfig().getParameterPropertySource(
				"collection");
		String str = getFolder().getLocation().toPortableString();
		if (path != null && path.contains(PropertiesEditor.CONFIG_DIR) && ! path.startsWith("{javascript")) {
			path = path.replace(PropertiesEditor.CONFIG_DIR, str);
		} else {
			path = ps.getConnectionConfig().getStringParameter("collection");
			if (path == null || path.length() == 0)
				return null;

			// -- prop store should remain relative to solution directory and
			// not migrated to runtime dir
			// if (! new File(path).isAbsolute())
			// path = str + "/" + path;
		}

		File file = new File(path);
		File parent = file.getParentFile();
		if (parent != null && !parent.exists() && !parent.mkdirs()) {
			throw new Exception("Cannot create directory: "
					+ parent.getAbsolutePath());
		}

		return file;
	}

}
