/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.natures;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;

import com.ibm.di.config.base.ContainerConfigImpl;
import com.ibm.di.config.base.PropertyManagerImpl;
import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.ALMappingConfig;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.NamespaceConfig;
import com.ibm.di.config.interfaces.ParserConfig;
import com.ibm.di.config.interfaces.PropertyManager;
import com.ibm.di.config.interfaces.PropertyStoreConfig;
import com.ibm.di.config.interfaces.RawConnectorConfig;
import com.ibm.di.config.interfaces.SchedulerConfig;
import com.ibm.di.config.interfaces.ScriptConfig;
import com.ibm.di.config.interfaces.SequenceConfig;
import com.ibm.tdi.eclipse.Activator;
import com.ibm.tdi.eclipse.TDI;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.editors.PropertiesEditor;
import com.ibm.tdi.eclipse.log.EclipseAppender;

public class TDINature implements IProjectNature {

	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	//
	// Folder names used for a TDI project
	//
	public static final String RESOURCES_FOLDER = "Resources"; //$NON-NLS-1$

	public final static String ASSEMBLYLINES_FOLDER = "AssemblyLines"; //$NON-NLS-1$

	public static final String PROPERTIES_FOLDER = "Properties"; //$NON-NLS-1$

	public static final String SCRIPTS_FOLDER = "Scripts"; //$NON-NLS-1$

	public static final String ATTRIBUTE_MAPS_FOLDER = "AttributeMaps"; //$NON-NLS-1$

	public static final String FUNCTIONS_FOLDER = "Functions"; //$NON-NLS-1$

	public static final String PARSERS_FOLDER = "Parsers"; //$NON-NLS-1$

	public static final String CONNECTORS_FOLDER = "Connectors"; //$NON-NLS-1$

	public static final String SCHEMA_FOLDER = "Schema"; //$NON-NLS-1$

	public static final String NAMESPACE_FOLDER = "References"; //$NON-NLS-1$

	public static final String SCHEDULER_FOLDER = MetamergeConfig.DEFAULT_SCHEDULER_FOLDER;

	public static final String SEQUENCE_FOLDER = MetamergeConfig.DEFAULT_SEQUENCE_FOLDER;

	// -- The name of the default server (local dev server)
	public static final String DEFAULT_SERVER_NAME = "Default"; //$NON-NLS-1$

	// -- The name of the default server with extension
	public static final String DEFAULT_SERVER_PROPERTY = "Default.tdiserver"; //$NON-NLS-1$

	// -- The ID of the TDI project nature
	public static final String TDI_NATURE_ID = Activator.TDI_PLUGIN_ID
			+ ".TDINature"; // $NON-NLS-1$ //$NON-NLS-1$

	// -- Name to tag resources as included. Not used currently but most likely
	// in the future
	// -- when file transfers etc are included in the server api.
	public static final QualifiedName TDI_SOLUTION_INCLUDE = new QualifiedName(
			"http://www.ibm.com", Activator.TDI_PLUGIN_ID + ".solution.include"); //$NON-NLS-1$ //$NON-NLS-2$

	// -- This property contains the name of an external rs.xml file.
	public static final QualifiedName TDI_EXTERNAL_CONFIG = new QualifiedName(
			"http://www.ibm.com", Activator.TDI_PLUGIN_ID + ".external.config"); //$NON-NLS-1$ //$NON-NLS-2$
	
	// -- This property contains the modification timestamp of an external rs.xml file.
	public static final QualifiedName TDI_EXTERNAL_CONFIG_TS = new QualifiedName(
			"http://www.ibm.com", Activator.TDI_PLUGIN_ID + ".external.config.ts"); //$NON-NLS-1$ //$NON-NLS-2$
	
	private IProject project;

	private static final String[] folders = new String[] { ASSEMBLYLINES_FOLDER,
			CONNECTORS_FOLDER, PARSERS_FOLDER, FUNCTIONS_FOLDER,
			ATTRIBUTE_MAPS_FOLDER, SCRIPTS_FOLDER, PROPERTIES_FOLDER, 
			SCHEMA_FOLDER, SCHEDULER_FOLDER, SEQUENCE_FOLDER };

	public static final String SOLUTION_SETTINGS_FILE = "Log & Settings";

	public static final String SCHEMA_FILEEXT = "schema";

	public void configure() throws CoreException {
		try {
			createFolder(ASSEMBLYLINES_FOLDER);
			createFolder(NAMESPACE_FOLDER);
			IFolder fld = createFolder(RESOURCES_FOLDER);
			for (int i = 1; i < folders.length; i++) {
				IFolder f = fld.getFolder(folders[i]);
				if (!f.exists())
					f.create(true, true, null);
			}

			createPropertyStores();
			getProject().setPersistentProperty(TDI.PROJECT_PREF_SERVER_QNAME,
					DEFAULT_SERVER_PROPERTY);
			
			// -- create the project page
			getProjectPage(project);
		} catch (Exception e) {
			throw EclipseAppender.coreException(e);
		}
	}

	private void createPropertyStores() {
		IFolder props = getProject().getFolder(RESOURCES_FOLDER).getFolder(
				PROPERTIES_FOLDER);

		String str = getProject().getName();
		IFile file = props.getFile(str
				+ "." + TDIConfigurationFile.XT_PROPSTORE); //$NON-NLS-1$
		if (!file.exists()) {
			try {
				createStore(file, str);
			} catch (Exception e) {
				EclipseAppender.logerror(e.toString(), e);
			}
		}

		file = props.getFile(PropertyManager.STDCOLL_JAVA + "." + TDIConfigurationFile.XT_PROPSTORE); //$NON-NLS-1$
		if (!file.exists()) {
			try {
				createStore(file, str);
			} catch (Exception e) {
				EclipseAppender.logerror(e.toString(), e);
			}
		}
	}

	private void createStore(IFile file, String str) throws Exception {
		ContainerConfigImpl cc = new ContainerConfigImpl();
		cc.init();

		ContainerConfigImpl data = new ContainerConfigImpl();
		data.init();
		data.setName("Data"); //$NON-NLS-1$
		cc.addConfig(data);

		PropertyManager pm = new PropertyManagerImpl();
		pm.init();
		pm.setName("Config"); //$NON-NLS-1$
		cc.addConfig(pm);

		PropertyStoreConfig psc = new com.ibm.di.config.base.PropertyStoreConfigImpl();
		psc.init();

		RawConnectorConfig rcc = psc.getConnectionConfig();
		rcc.setParent(psc);
		rcc.setParameter("collectionType", str); //$NON-NLS-1$
		rcc.setInheritsFromRef("system:/Connectors/ibmdi.Properties"); //$NON-NLS-1$
		rcc.setParameterPropertySource("collection", PropertiesEditor.CONFIG_DIR + "/" + str + ".properties");

		psc.setName(str);
		psc.setKeyAttribute("key"); //$NON-NLS-1$
		psc.setValueAttribute("value"); //$NON-NLS-1$
		psc.setInitialLoad(true);

		pm.addPropertyStore(psc);

		TDIConfigurationFile cfg = new TDIConfigurationFile(file);
		cfg.bind(str, cc);
		cfg.commitVersion(true);

	}

	private IFolder createFolder(String str) throws CoreException {
		IFolder folder = (IFolder) getProject().findMember(str);
		if (folder == null) {
			folder = getProject().getFolder(str);
			folder.create(true, true, null);
			tagAsSolutionResource(folder);
		}
		return folder;
	}

	public static boolean isSolutionResource(IResource resource)
			throws CoreException {
		if (resource.getPersistentProperty(TDI_SOLUTION_INCLUDE) != null)
			return true;
		else
			return false;
	}

	public static void tagAsSolutionResource(IResource resource)
			throws CoreException {
		resource.setPersistentProperty(TDI_SOLUTION_INCLUDE, "true"); //$NON-NLS-1$
	}

	public void deconfigure() throws CoreException {
		IFile rs = Utils.getRuntimeRS(project);
		if (rs != null)
			MetamergeConfigFactory.unregisterNamespace(rs.getRawLocation()
					.toOSString());
	}

	public IProject getProject() {
		return project;
	}

	public void setProject(IProject project) {
		this.project = project;
	}

	public static String getDefaultFolder(BaseConfiguration bc) {
		if (bc instanceof AssemblyLineConfig)
			return ASSEMBLYLINES_FOLDER;
		else if (bc instanceof ScriptConfig)
			return SCRIPTS_FOLDER;
		else if (bc instanceof FunctionConfig)
			return FUNCTIONS_FOLDER;
		else if (bc instanceof ALMappingConfig)
			return ATTRIBUTE_MAPS_FOLDER;
		else if (bc instanceof ConnectorConfig) {
			if("Schema".equals(((ConnectorConfig)bc).getMode()))
				return SCHEMA_FOLDER;
			else
				return CONNECTORS_FOLDER;
		} else if (bc instanceof ParserConfig)
			return PARSERS_FOLDER;
		else if (bc instanceof PropertyStoreConfig)
			return PROPERTIES_FOLDER;
		else if (bc instanceof NamespaceConfig)
			return NAMESPACE_FOLDER;
		else if (bc instanceof SchedulerConfig)
			return SCHEDULER_FOLDER;
		else if (bc instanceof SequenceConfig)
			return SEQUENCE_FOLDER;
		else
			return null;

	}
	
	public static IFile getProjectPage(IProject project) throws CoreException {
		IFile file = project.getFile(SOLUTION_SETTINGS_FILE);
		if(!file.exists()) {
			file.create(new ByteArrayInputStream("".getBytes()), true, null);
		}
		return file;
	}
}
