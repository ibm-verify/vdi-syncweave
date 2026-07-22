/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import javax.naming.Binding;
import javax.naming.Name;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;

import com.ibm.di.config.base.BaseConfigurationImpl;
import com.ibm.di.config.base.ContainerConfigImpl;
import com.ibm.di.config.base.PropertyManagerImpl;
import com.ibm.di.config.eclipse.MetamergeConfigCE;
import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.AttributeMapItem;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.InstanceConfig;
import com.ibm.di.config.interfaces.LibraryConfig;
import com.ibm.di.config.interfaces.LogConfig;
import com.ibm.di.config.interfaces.LogConfigItem;
import com.ibm.di.config.interfaces.LoopConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.MetamergeFolder;
import com.ibm.di.config.interfaces.NamespaceConfig;
import com.ibm.di.config.interfaces.PropertyConfig;
import com.ibm.di.config.interfaces.PropertyManager;
import com.ibm.di.config.interfaces.PropertyStoreConfig;
import com.ibm.di.config.interfaces.SolutionInterface;
import com.ibm.di.config.interfaces.TDIProperties;
import com.ibm.di.config.interfaces.TDIPropertyStore;
import com.ibm.di.config.interfaces.TombstonesConfig;
import com.ibm.di.config.interfaces.RawConnectorConfig;
import com.ibm.di.config.xml.MetamergeConfigXML;
import com.ibm.di.entry.Entry;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.builders.IncrementalConfigBuilder;
import com.ibm.tdi.eclipse.editors.PropertiesEditor;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.natures.TDINature;

import java.io.File;


public class ImportUtils {

	private IProject project;
	private boolean overwrite;
	private MetamergeConfigXML sourceXML;
	private BaseConfiguration[] includes;
	private IFolder resources;
	private MetamergeConfigCE solutionProperties = null;

	public ImportUtils(IProject project, String path, String password) throws Exception {
		Hashtable<String, Object> env = new Hashtable<String, Object>();
		env.put(MetamergeConfigFactory.MC_URL, path);
		String p = password;
		if (p != null && p.length() > 0)
			env.put(javax.naming.Context.SECURITY_CREDENTIALS, p);

		sourceXML = new MetamergeConfigXML(env);
		this.project = project;
		this.resources = project.getFolder(TDINature.RESOURCES_FOLDER);
	}

	public ImportUtils(IProject project, MetamergeConfigXML mc) {
		super();
		this.project = project;
		this.sourceXML = mc;
		this.resources = project.getFolder(TDINature.RESOURCES_FOLDER);
	}

	/**
	 * @param list
	 *            List of BaseConfiguration items to import (dependencies are
	 *            auto imported)
	 * @param overwrite
	 *            Overwrite existing files
	 * @throws Exception
	 */
	public void importConfig(BaseConfiguration[] list, boolean overwrite) throws Exception {
		this.overwrite = overwrite;
		includes = list;
		if (list == null) {
			includes = getFolderContents(sourceXML, new ArrayList<BaseConfiguration>()).toArray(new BaseConfiguration[0]);
		}

		//
		// Make sure we have a TDI folder structure in the project
		//
		try {
			TDINature tn = new TDINature();
			tn.setProject(project);
			tn.configure();
		} catch (Exception e) {
			// this should really not happen since we only create missing
			// folders
			// in case it fails, keep going in case the error is non-fatal.
			// Imports below will fail if folders are missing anyway.
			EclipseAppender.logerror(e.toString(), e);
		}

		// -- Set the builder for this project
		if (!project.hasNature(TDINature.TDI_NATURE_ID)) {
			// Configure Nature - must be done to an open project
			IProjectDescription description = project.getDescription();
			boolean hasNature = false;
			for (String str : description.getNatureIds()) {
				if (TDINature.TDI_NATURE_ID.equals(str))
					hasNature = true;
			}
			
			if (!hasNature) {
				String[] natures = description.getNatureIds();
				String[] newNatures = new String[natures.length + 1];
				System.arraycopy(natures, 0, newNatures, 0, natures.length);
				newNatures[natures.length] = TDINature.TDI_NATURE_ID;
				description.setNatureIds(newNatures);

				// Configure builder
				ICommand[] commands = description.getBuildSpec();
				ICommand command = description.newCommand();
				command.setBuilderName(IncrementalConfigBuilder.BUILDER_ID);
				ICommand[] newCommands = new ICommand[commands.length + 1];

				System.arraycopy(commands, 0, newCommands, 1, commands.length);
				newCommands[0] = command;
				description.setBuildSpec(newCommands);

				project.setDescription(description, null);
			}
		}

		for (BaseConfiguration bc : includes) {
			importFragment(bc);
		}

		// We have to wait a bit for IncrementalConfigBuilder to do it's work
		Thread.sleep(300);

		saveSolutionProperties();
	}

	private ArrayList<BaseConfiguration> getFolderContents(MetamergeFolder element, ArrayList<BaseConfiguration> list)
			throws Exception {
		for (Enumeration<?> en = element.list(); en.hasMoreElements();) {
			Object next = en.nextElement();
			BaseConfiguration config;
			if (next instanceof Binding)
				config = (BaseConfiguration) ((Binding) next).getObject();
			else
				config = (BaseConfiguration) next;

			if (config instanceof MetamergeFolder)
				getFolderContents((MetamergeFolder) config, list);
			else
				list.add(config);
		}
		return list;
	}

	private void importFragment(BaseConfiguration bc) throws Exception {
		IFolder folder = null;

		// -- 
		if (bc instanceof MetamergeFolder)
			return;
		
		// -- Ancient artifact in some files (JavaProperties tag)
		if (bc instanceof PropertyConfig)
			return;

		if (bc instanceof TombstonesConfig || bc instanceof SolutionInterface || isSystemStoreSettings(bc)) {
			if (overwrite)
				importSolutionProps(bc);
			return;
		}

		if (bc instanceof LibraryConfig) {
			mergeLibraryConfig((LibraryConfig)bc);
			return;
		}

		if (bc instanceof LogConfig) {
			mergeLogConfig((LogConfig)bc);
			return;
		}

		if (bc instanceof InstanceConfig) {
			mergeInstanceConfig((InstanceConfig)bc);
			return;
		}

		if (isPackageInformation(bc))
			return;
		
		if (bc instanceof AssemblyLineConfig) {
			folder = project.getFolder(TDINature.ASSEMBLYLINES_FOLDER);
		} else {
			String fld = TDINature.getDefaultFolder(bc);
			if (fld != null) {
				if(bc instanceof NamespaceConfig)
					folder = project.getFolder(fld);
				else
					folder = resources.getFolder(TDINature.getDefaultFolder(bc));
			} else {
				folder = resources;
			}
		}
		
		bc = (BaseConfiguration) bc.getClone();

		String extension = "." + TDIConfigurationFile.getExtensionFor(bc);
		String name = bc.getShortName();
		if (!name.endsWith(extension))
			name += extension;
		IFile file = folder.getFile(name);
		
		// -- Don't overwrite existing files?
		//if(file.exists() && !overwrite) Commented out  to fix a import related defect. DI01266
		if (existsFile(folder, name, file, overwrite))
			return;
		
		TDIConfigurationFile cfg = new TDIConfigurationFile(file);

		//
		// Special case of prop stores
		//
		if (bc instanceof PropertyStoreConfig) {
			bc = createLocalPropStore((PropertyStoreConfig) bc);

		} else if (bc instanceof AssemblyLineConfig) {
			AssemblyLineConfig alc = (AssemblyLineConfig) bc;
			for (Object obj : alc.getDataFlowComponents().getConfigurations(null)) {
				if (obj instanceof LoopConfig)
					convertLoopConfig((LoopConfig) obj);
			}
		}

		String newname = file.getName();
		if (file.getFileExtension() != null)
			newname = newname.substring(0, newname.lastIndexOf('.'));
		cfg.setDefaultConfigObject(newname, bc);
		cfg.commitVersion();

		IncrementalConfigBuilder.addToQueue(file); // Try to force the correct order of fragments
	}

	private boolean isSystemStoreSettings(BaseConfiguration bc) {
		return (bc instanceof ContainerConfig && "SystemStore".equals(bc.getShortName()));
	}
	
	private boolean isPackageInformation(BaseConfiguration bc) {
		return (bc instanceof ContainerConfig && "Package".equals(bc.getShortName()));
	}

	/**
	 * Return true if a file with a name similar the given name exists in the folder, and should not be overwritten.
	 * If a file exists and should be overwritten, remove it if necessary.
	 * @param folder The folder to look in.
	 * @param name The name to look for (casing may be ignored).
	 * @param file An IFile with the given name, for a quick check.
	 * @param overwrite If true, allow files to be overwritten.
	 * @return true if a file exists that should not be overwritten
	 * @throws Exception
	 */
	private boolean existsFile(IFolder folder, String name, IFile file, boolean overwrite) throws Exception {
		IFile existing = null;
		if (file != null && file.exists()) {
			existing = file;
		} else {
			File fname = new File(name);
			for (IResource res:folder.members()) {
				if (res instanceof IFile && fname.equals(new File(res.getName()))) {
					existing = (IFile) res;
					break;
				}
			}
		}
		if (existing == null)
			return false;
		
		/**
		 * Overwrite anyway in a special case:
		 * 1) Prop store and name == project name
		 * 2) Existing prop store is empty
		 * 3) Prop store file path == "{$config.directory}/<projectname>.properties"
		 */
		if (!overwrite && existing.getName().equals(project.getName()+ ".tdiproperties")) {
			TDIConfigurationFile cfg = new TDIConfigurationFile(existing);
			BaseConfiguration bc = cfg.getDefaultConfigObject();
			if (bc instanceof ContainerConfig) {
				ContainerConfig cc = (ContainerConfig) bc;
				ContainerConfig data = (ContainerConfig) cc.getConfig("Data");
				PropertyStoreConfig psc = (PropertyStoreConfig)((PropertyManager)cc.getConfig("Config")).getPropertyStores().getConfig(0);
				RawConnectorConfig rcc = psc.getConnectionConfig();
				String s = rcc.getParameterPropertySource("collection");
				if (data.size() == 0 && s != null && s.equals("{config.$directory}/" + project.getName() + ".properties"))
					overwrite = true;
			}
		}

		if (!overwrite)
			return true;
		
		// Remove the existing file if the name is not completely the same,
		// otherwise eclipse will throw an error when we try to create the file.
		if (existing != file)
			existing.delete(true, null);
		
		return false;
	}

	/**
	 * This method converts the assignments from the Output attribute map to
	 * parameter property source strings in the loop connector's configuration.
	 * 
	 * @param lc
	 * @throws Exception
	 */
	private void convertLoopConfig(LoopConfig lc) throws Exception {
		// Convert connector param map to proper substitutions
		if (lc.getLoopType() == LoopConfig.LOOP_CONNECTOR_FC) {
			AttributeMapConfig amc = lc.getLoopConnector().getAttributeMap(false);
			for (String str : amc.getAttributeNames()) {
				AttributeMapItem ami = amc.getAttributeMapItem(str);
				String pps;
				String script = null;
				if (ami.isSubstitution()) {
					pps = ami.getSubstitution();
				} else {
					if (ami.isSimple())
						script = Utils.getScript("work", ami.getSimple());
					else
						script = ami.getScript();

					String JS_PREFIX = "{javascript "; //$NON-NLS-1$
					String JS_SUFFIX = "}"; //$NON-NLS-1$
					pps = JS_PREFIX + script + JS_SUFFIX;
				}
				lc.getLoopConnector().getConnectionConfig().setParameterPropertySource(str, pps);
			}
		}
	}

	private void importSolutionProps(BaseConfiguration bc) {
		if (!loadSolutionProperties())
			return;
		try {
			solutionProperties.rebind(bc.getName(), bc);
		} catch (Exception e) {
			EclipseAppender.logerror("Import", e);
		}
	}

	private boolean loadSolutionProperties() {
		if (solutionProperties != null)
			return true;

		try {
			solutionProperties = new MetamergeConfigCE(Utils.getSolutionProps(resources));
			return true;
		} catch (Exception e) {
			EclipseAppender.logerror("Import", e);
			return false;
		}
	}

	private void mergeLogConfig(LogConfig bc) {
		String serverLogName = MetamergeConfig.DEFAULT_SERVER_FOLDER + "/" + MetamergeConfig.DEFAULT_SERVER_LOG;

		if (!bc.getName().toString().equals(serverLogName) || bc.getItems().size() == 0)
			return;

		if (overwrite) {
			importSolutionProps(bc);
			return;
		}
		
		if (!loadSolutionProperties())
			return;

		LogConfig logconfig = null;
		try {
			logconfig = (LogConfig) solutionProperties.lookup(serverLogName);
		} catch (Exception e) {}
		
		if (logconfig == null || logconfig.getItems().size() == 0) {
			importSolutionProps(bc);
			return;			
		}

		try {
			Set<Name> oldNames = new HashSet<Name>();
			for (LogConfigItem item:logconfig.getItems()) {
				oldNames.add(item.getName());
			}

			for (LogConfigItem item:bc.getItems()) {
				if (!oldNames.contains(item.getName()))
					logconfig.addItem((LogConfigItem) item.getClone());
			}
		} catch (Exception e) {
			EclipseAppender.logerror("Import", e);
		}
	}

	private void mergeInstanceConfig(InstanceConfig bc) {
		ContainerConfig newItems = bc.getStartupItems();
		if (newItems.size() == 0)
			return;

		if (!loadSolutionProperties())
			return;

		InstanceConfig ic = null;
		try {
			ic = (InstanceConfig) solutionProperties.lookup(MetamergeConfig.DEFAULT_SERVER_FOLDER + "/" + MetamergeConfig.DEFAULT_SERVER_AUTOSTART);
		} catch (Exception e) {}
		
		if (ic == null || ic.getStartupItems().size() == 0) {
			importSolutionProps(bc);
			return;			
		}
		
		try {
			Set<String> oldNames = new HashSet<String>();
			ContainerConfig cc = ic.getStartupItems();
			for (int i = 0; i < cc.size(); i++) {
				oldNames.add(cc.getConfig(i).getStringParameter(InstanceConfig.AUTOSTART_NAME));
			}

			for (int i = 0; i < newItems.size(); i++) {
				BaseConfiguration item = newItems.getConfig(i);
				if (!oldNames.contains(item.getStringParameter(InstanceConfig.AUTOSTART_NAME)))
					cc.addConfig((BaseConfiguration)item.getClone());
			}
		} catch (Exception e) {
			EclipseAppender.logerror("Import", e);
		}
	}

	private void mergeLibraryConfig(LibraryConfig bc) {
		if (bc.size() == 0)
			return;
		
		if (!loadSolutionProperties())
			return;

		LibraryConfig lc = null;
		try {
			lc = (LibraryConfig) solutionProperties.lookup(MetamergeConfig.DEFAULT_LIBRARY_FOLDER);
		} catch (Exception e) {}

		if (lc == null || lc.size() == 0) {
			importSolutionProps(bc);
			return;
		}

		try {
			for (Iterator<String> i = bc.getDataIterator(); i.hasNext();) {
				String key = i.next();
				if (overwrite || lc.getParameter(key) == null)
					lc.setParameter(key, bc.getParameter(key));
			}
		} catch (Exception e) {
			EclipseAppender.logerror("Import", e);
		}
	}

	private BaseConfiguration createLocalPropStore(PropertyStoreConfig psc) throws Exception {
		ContainerConfig cc = new ContainerConfigImpl();
		cc.init();

		ContainerConfig data = new ContainerConfigImpl();
		data.init();
		data.setName("Data"); //$NON-NLS-1$
		cc.addConfig(data);

		PropertyManager pm = new PropertyManagerImpl();
		pm.init();
		pm.setName("Config"); //$NON-NLS-1$
		cc.addConfig(pm);

		if (psc.getName() == null)
			psc.setName("Default");
		pm.addPropertyStore(psc);
		
		// -- Read property store data into local store file
		try {
			TDIPropertyStore store = new TDIPropertyStore(psc);
			for (Iterator<Entry> i = store.entries(); i.hasNext();) {
				Entry entry = i.next();
				String name = entry.getString(TDIProperties.KEY_ATTRIBUTE);
				BaseConfiguration bc = new BaseConfigurationImpl();
				data.addConfig(bc);
				bc.setName(name);
				bc.setParameter(PropertiesEditor.SERVER_NAME, name);
				bc.setParameter(PropertiesEditor.PROPERTY, "true");
				String value = entry.getString(TDIProperties.VALUE_ATTRIBUTE);
				if (value == null)
					value = ""; //$NON-NLS-1$
				bc.setParameter(PropertiesEditor.SERVER_VALUE, value);
				bc.setStringParameter(PropertiesEditor.LOCAL_VALUE, value);
				String prot = entry.getString(TDIProperties.PROTECT_ATTRIBUTE);
				if (prot != null) {
					bc.setParameter(PropertiesEditor.SERVER_PROTECT, prot);
					bc.setParameter(PropertiesEditor.LOCAL_PROTECT, prot);
					PropertiesEditor.verifyEncrypted(bc);
				}
			}
			store.terminate();
		} catch (Exception e) {
			EclipseAppender.logerror(e.getMessage(), e);
		}
		return cc;
	}

	private void saveSolutionProperties() {
		if (solutionProperties == null)
			return;
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			solutionProperties.commitChanges(bos, true);
			IFile file = Utils.getSolutionProps(resources);
			if (file.exists())
				file.setContents(new ByteArrayInputStream(bos.toByteArray()), IResource.FORCE, null);
			else
				file.create(new ByteArrayInputStream(bos.toByteArray()), IResource.FORCE, null);
		} catch (Exception e) {
			EclipseAppender.logerror(TDINature.SOLUTION_SETTINGS_FILE, e);
		}
	}

}
