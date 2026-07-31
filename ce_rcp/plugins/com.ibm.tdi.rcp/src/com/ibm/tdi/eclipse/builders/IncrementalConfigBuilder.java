/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.builders;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.Binding;
import javax.naming.Name;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.di.config.base.BaseConfigurationImpl;
import com.ibm.di.config.base.ContainerConfigImpl;
import com.ibm.di.config.base.InstanceConfigImpl;
import com.ibm.di.config.base.InternalSchema;
import com.ibm.di.config.base.LogConfigImpl;
import com.ibm.di.config.base.TombstonesConfigImpl;
import com.ibm.di.config.eclipse.MetamergeConfigCE;
import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.AttributeMapItem;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.HookConfig;
import com.ibm.di.config.interfaces.HooksConfig;
import com.ibm.di.config.interfaces.LinkCriteriaConfig;
import com.ibm.di.config.interfaces.LinkCriteriaItem;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.MetamergeFolder;
import com.ibm.di.config.interfaces.ParserConfig;
import com.ibm.di.config.interfaces.PropertyManager;
import com.ibm.di.config.interfaces.PropertyStoreConfig;
import com.ibm.di.config.interfaces.ReconnectConfig;
import com.ibm.di.config.interfaces.SchemaConfig;
import com.ibm.di.config.interfaces.SchemaItemConfig;
import com.ibm.di.config.interfaces.SolutionInterface;
import com.ibm.tdi.eclipse.Activator;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.natures.TDINature;
import org.eclipse.core.runtime.IPath;

public class IncrementalConfigBuilder extends IncrementalProjectBuilder {
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final String XT_SETTINGS = "settings";

	public static final String BUILDER_ID = Activator.TDI_PLUGIN_ID
			+ ".tdibuilder"; //$NON-NLS-1$

	private IResource rsxml;

	private MetamergeConfig mc;

	/**
	 * The queue of IFiles we will process.
	 */
	private static Deque<IFile> resourceQueue = new ArrayDeque<IFile>();

	/**
	 * Add a IFile to the queue that will be processed by this builder.
	 * This helps in sorting the fragments in the correct order.
	 * @param res
	 */
	public static void addToQueue(IFile res) {
		synchronized (resourceQueue) {
			if (!resourceQueue.contains(res))
				resourceQueue.add(res);
		}
	}
	
	private static IFile getNextRes() {
		synchronized (resourceQueue) {
			return resourceQueue.isEmpty() ? null : resourceQueue.remove();
		}
	}
	
	public IncrementalConfigBuilder() {
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
		try {
			IProject project = getProject();
			rsxml = Utils.getRuntimeRS(project);
			mc = Utils.getProjectMC(project);

			project.deleteMarkers("com.ibm.tdi.rcp.tdiproblem", true, IResource.DEPTH_INFINITE); //$NON-NLS-1$
			setID(mc.getSolutionInterface(), project.getName());

			if (kind == IncrementalProjectBuilder.FULL_BUILD) {
				fullBuild(monitor);
			} else {
				IResourceDelta delta = getDelta(getProject());
				if (delta == null) {
					fullBuild(monitor);
				} else {
					incrementalBuild(delta, monitor);
				}
			}

			if (mc != null) {
				mc.commitChanges(null, true);
				updateExternalConfigFile(project, rsxml, mc);
				updateRuntimeDirectory((IFile)rsxml);
			}
			rsxml.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (Exception e) {
			EclipseAppender.logerror(Messages.getMessage(
					"IncrementalConfigBuilder.1", e.getMessage()), e); //$NON-NLS-1$
			throw EclipseAppender.coreException(e);
		}

		// No need to include project for next-time builds
		return null;
	}

	private void updateRuntimeDirectory(IFile rsxml) throws Exception {
		new ProjectRuntimeDirectory(getProject()).updateProjectConfig(rsxml);
	}

	/**
	 * If the project has the external config path set we update that file.
	 * 
	 * This property is set when the user opens an external tdi configuration file.
	 * 
	 * @param project
	 * @param rsxml2
	 */
	private void updateExternalConfigFile(IProject project, IResource source, MetamergeConfig mc) throws Exception {
		String extPath = project.getPersistentProperty(TDINature.TDI_EXTERNAL_CONFIG);
		if(extPath == null || extPath.length() == 0)
			return;
		
		/* #13180
		// -- If this is an import/refresh then don't write it back
		String ts = project.getPersistentProperty(TDINature.TDI_EXTERNAL_CONFIG_TS);
		if(ts == null || ts.length() == 0)
			return;
		
		File file = new File(extPath);
		if(!file.exists())
			return;
		
		*/
		mc.commitChanges(new File(extPath), true);
	}

	private void setID(SolutionInterface sol, String name) {
		String id = sol.getInstanceID();
		if (id == null || id.length() == 0)
			sol.setInstanceID(name);
		sol.setEnabled(true);
	}

	protected void fullBuild(final IProgressMonitor monitor)
			throws CoreException {
		try {
			getProject().accept(new FullBuildVisitor());
		} catch (CoreException e) {
		}
	}

	protected void incrementalBuild(IResourceDelta delta,
			IProgressMonitor monitor) throws CoreException {
		delta.accept(new DeltaBuildVisitor());

		for (IFile file = getNextRes(); file != null; file = getNextRes()) {
//			EclipseAppender.loginfo("Adding " + file.getName());
			updateObject(file);
		}
	}

	class FullBuildVisitor implements IResourceVisitor {
		public boolean visit(IResource res) throws CoreException {
			if (res instanceof IProject)
				return true;
			else if (res instanceof IFolder)
				updateFolder((IFolder) res);
			else if (res.equals(rsxml)
					|| !isKnownFileExtension(res))
				return (res instanceof IContainer);
			else
				updateObject((IFile) res);

			return true;
		}
	}

	class DeltaBuildVisitor implements IResourceDeltaVisitor {

		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource res = delta.getResource();
			if (res.equals(rsxml)
					|| !isKnownFileExtension(res))
				return (res instanceof IContainer);

			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				if (res instanceof IFolder)
					updateFolder((IFolder) res);
				else
					addToQueue(addedFile((IFile) res));
				break;
			case IResourceDelta.REMOVED:
				if (res instanceof IFile)
					removeObject((IFile) res);
				break;
			case IResourceDelta.CHANGED:
				if (res instanceof IFile)
					addToQueue((IFile) res);
				break;

			}
			return true;
		}
	}

		/**
		 * A file was added. Do some checking.
		 * @param res
		 */
		private IFile addedFile(IFile res) throws CoreException
		{
			String folder = TDIConfigurationFile.getFolderForExtension(res.getFileExtension());
			if (folder != null && ! folder.equals(res.getParent().getName())) 
			{
				// File added in the wrong folder, try to move it.
				IPath path = res.getProject().getFullPath();
				if (!folder.equals(MetamergeConfig.DEFAULT_ASSEMBLYLINE_FOLDER)) {
					if(folder.equals(MetamergeConfig.DEFAULT_NAMESPACE_FOLDER)) {
						folder = TDINature.NAMESPACE_FOLDER;
					} else {
						path = path.append(TDINature.RESOURCES_FOLDER);
					}
				}
				path = path.append(folder);
				path = path.append(res.getName());
				if(!res.getFullPath().equals(path)) {
					res.move(path, true, null);
					res = res.getWorkspace().getRoot().getFile(path);
				}
			}

			return res;
		}




	/**
	 * This method creates the folder structure in the project config file
	 * 
	 * @param resource
	 */
	private void updateFolder(IFolder resource) {

		// Don't permit custom folders - not yet anyway
		// if (false) {
		// String path = resource.getProjectRelativePath().toPortableString();
		//			StringTokenizer st = new StringTokenizer(path, "/"); //$NON-NLS-1$
		// MetamergeFolder folder = mc;
		//
		// while (st.hasMoreTokens()) {
		// String name = st.nextToken();
		// BaseConfiguration current = folder.getChild(name);
		// if (current == null) {
		// try {
		// MetamergeFolder newFolder = new MetamergeFolderImpl();
		//						newFolder.setName(folder.getName() + "/" + name); //$NON-NLS-1$
		// mc.bind(newFolder.getName(), newFolder);
		// folder = newFolder;
		// } catch (NameAlreadyBoundException nnfe) {
		// folder = (MetamergeFolder) folder.getChild(name);
		// } catch (Exception e) {
		//						EclipseAppender.logerror(Messages.getMessage("IncrementalConfigBuilder.2", name, e.getMessage()), e); //$NON-NLS-1$
		// }
		// } else if (current instanceof MetamergeFolder) {
		// folder = (MetamergeFolder) current;
		// } else {
		//					String msg = Messages.getMessage("IncrementalConfigBuilder.3", name); //$NON-NLS-1$
		// EclipseAppender.logerror(msg, new Exception(msg));
		// return;
		// }
		// }
		// }
	}

	public boolean isKnownFileExtension(IResource file) {
		
		// -- various solution config settings
		if(TDINature.SOLUTION_SETTINGS_FILE.equals(file.getName()))
			return true;
		
		String fileExtension = file.getFileExtension();
		if (XT_SETTINGS.equals(fileExtension))
			return true;

		for (String str : TDIConfigurationFile.FILE_EXTENSIONS) {
			if (str.equals(fileExtension))
				return true;
		}
		return false;
	}

	private void removeObject(IResource resource) {
		String extension = resource.getFileExtension();
		if (extension == null)
			return;
		String name = resource.getName();
		if (extension.length() > 0)
			name = name.substring(0, name.length() - extension.length() - 1);

		try {
			if (extension.equals(TDIConfigurationFile.XT_PROPSTORE)) {
				PropertyManager tpm = (PropertyManager) mc
						.lookup(MetamergeConfig.DEFAULT_PROPSTORE_FOLDER);
				tpm.getPropertyStores().removeConfig(name, true);

			} else if (extension.equals(TDIConfigurationFile.XT_NAMESPACE)) {
				mc.unbind(MetamergeConfig.DEFAULT_NAMESPACE_FOLDER + "/" + name); //$NON-NLS-1$
				
			} else if (extension.equals(XT_SETTINGS)) {
				MetamergeConfigCE mce = new MetamergeConfigCE();
				mce.initializeConfig();
				createEmptySettingsDocument(mce);
				updateSettings(mce);

			} else {
				String folder = resource.getParent().getName();
				mc.unbind(folder + "/" + name); //$NON-NLS-1$

			}
		} catch (NameNotFoundException nnfe) {
		} catch (Exception e) {
			EclipseAppender.logerror(Messages.getMessage(
					"IncrementalConfigBuilder.4", e.getMessage()), e); //$NON-NLS-1$
		}
	}

	private void updateObject(IFile file) throws CoreException {
		try {
			if (file.getName().equals(TDINature.SOLUTION_SETTINGS_FILE) || file.getFileExtension().equals(XT_SETTINGS)) {
				updateSettings(new MetamergeConfigCE(file));
				return;
			}

			TDIConfigurationFile cfg = TDIConfigurationFile.loadFile(file);
			cfg.setModTSEnabled(false);
			BaseConfiguration obj = cfg.getDefaultConfigObject();
			String folder = file.getParent().getName();
			MetamergeFolder target = null;
			try {
				target = (MetamergeFolder) mc.lookup(folder);
			} catch (NameNotFoundException nfe) {
			}

			if (obj instanceof AssemblyLineConfig) {
				new AssemblyLineValidator().validate((AssemblyLineConfig) obj);
			}

			if (file.getFileExtension().equals(
					TDIConfigurationFile.XT_PROPSTORE)) {
				PropertyManager pm = (PropertyManager) ((ContainerConfig) obj)
						.getConfig("Config"); //$NON-NLS-1$
				PropertyStoreConfig ps = (PropertyStoreConfig) pm
						.getPropertyStores().getConfig(0).getClone();
				String storeName = file.getName().substring(0,
						file.getName().indexOf(file.getFileExtension()) - 1);
				PropertyManager tpm = (PropertyManager) mc
						.lookup(MetamergeConfig.DEFAULT_PROPSTORE_FOLDER);

				try {
					tpm.getPropertyStores().removeConfig(storeName, true);
				} catch (Exception ignore) {
				}

				boolean isRenamed = !storeName.equalsIgnoreCase(ps
						.getShortName());
				boolean isStdProperty = false;

				ps.setName(storeName);

				if (isRenamed) {
					for (String stdName : PropertyManager.STDCOLL_PROPERTY_NAMES) {
						if (storeName.equalsIgnoreCase(stdName)) {
							ps.getConnectionConfig().setParameter(
									"collectionType", stdName);
							isStdProperty = true;
							break;
						}
					}

					if (!isStdProperty) {
						ps.getConnectionConfig().setParameter("collectionType",
								"Default");
					}
				}
				tpm.addPropertyStore(ps);
				
				String type = ps.getConnectionConfig().getInheritsFromRef();
				if ("system:/Connectors/ibmdi.Properties".equals(type)) {
					ProjectRuntimeDirectory prd = new ProjectRuntimeDirectory(getProject());
					File propFile = prd.getPropertyStorePath(ps);
					if (propFile != null && file.getLocalTimeStamp() > propFile.lastModified())
						prd.updatePropertyStore((ContainerConfig) obj, false);
				}

			} else {
				if (target == null) {
					folder = cfg.getTargetFolderName(obj);
					if (folder == null)
						throw new Exception(Messages.getMessage(
								"IncrementalConfigBuilder.5", obj)); //$NON-NLS-1$
				}

				obj.setName(folder + "/" + obj.getShortName());
				doUpdateMC(obj);
			}

			// Update all referenced components
			// ContainerConfig refs = cfg.getReferences();
			// for (int i = 0; i < refs.size(); i++) {
			// if (!(refs.getConfig(i) instanceof ContainerConfig))
			// continue;
			// ContainerConfig cc = (ContainerConfig) refs.getConfig(i);
			// BaseConfiguration c =
			// cc.getConfig(TDIConfigurationFile.PARAM_OBJECT);
			// String internal =
			// c.getStringParameter(TDIConfigurationFile.REF_INTERNAL);
			// String fname =
			// c.getStringParameter(TDIConfigurationFile.REF_FILE);
			// if (internal == null || fname == null)
			// continue;
			//
			// //
			// // -- Grab a fresh version of the referenced component
			// //
			// BaseConfiguration ref;
			// try {
			// TDIConfigurationFile ext =
			//TDIConfigurationFile.loadFile(getProject().getWorkspace().getRoot(
			// ).getFile(
			// new Path(fname)));
			// ref = (BaseConfiguration)
			// ext.getDefaultConfigObject().getClone();
			// ref.setName(internal);
			// ref.flatten(new ArrayList<String>());
			// } catch (Exception e) {
			// Utils.logProblem(IMarker.SEVERITY_WARNING, obj,
			// Messages.getMessage("incremental.configbuilder.load.warn",
			// //$NON-NLS-1$
			// fname));
			// EclipseAppender.logerror(Messages.getMessage(
			// "FormUI.Localized.Select.Database",
			// fname), e); //$NON-NLS-1$
			// if (cc.size() <= TDIConfigurationFile.LOCAL_COPY) {
			// Utils.logProblem(IMarker.SEVERITY_INFO, obj,
			// Messages.getMessage("IncrementalConfigBuilder.6", fname));
			// //$NON-NLS-1$
			// continue;
			// }
			// ref = cc.getConfig(TDIConfigurationFile.LOCAL_COPY);
			// Utils.logProblem(IMarker.SEVERITY_INFO, obj,
			// Messages.getMessage("IncrementalConfigBuilder.7", fname));
			// //$NON-NLS-1$
			// }
			//
			// mc.rebind(internal, ref);
			// }

		} catch (NameNotFoundException e) {
		} catch (Exception e) {
			EclipseAppender.logerror(e.getMessage(), e);
			throw EclipseAppender.coreException(e);
		}
	}

	private void updateSettings(MetamergeConfig settings) throws Exception {
		String[] items = new String[] {
				MetamergeConfig.DEFAULT_LIBRARY_FOLDER,
				MetamergeConfig.DEFAULT_SERVER_FOLDER + "/"
						+ MetamergeConfig.DEFAULT_SERVER_TOMBSTONES,
				MetamergeConfig.DEFAULT_SERVER_FOLDER + "/"
						+ MetamergeConfig.DEFAULT_SERVER_LOG,
				MetamergeConfig.DEFAULT_SERVER_FOLDER + "/"
						+ MetamergeConfig.DEFAULT_SERVER_AUTOSTART,
				MetamergeConfig.DEFAULT_SERVER_FOLDER + "/"
						+ MetamergeConfig.DEFAULT_SOLUTION_INTERFACE,
				MetamergeConfig.DEFAULT_SERVER_FOLDER + "/" + "SystemStore" //$NON-NLS-1$
		};

		for (String name : items) {
			BaseConfiguration config = (BaseConfiguration) settings
					.lookup(name);
			mc.rebind(name, config);
			// For some reason, we need this ugly code to copy all includes
			if (config instanceof MetamergeFolder && 
					MetamergeConfig.DEFAULT_NAMESPACE_FOLDER.equals(config.getShortName())) {
				Enumeration<Binding> l = ((MetamergeFolder)config).list();
				while (l.hasMoreElements()) {
					BaseConfiguration b = (BaseConfiguration) l.nextElement().getObject();
					mc.rebind(b.getName(), b);
				}
			}
		}
	}

	@Override
	protected void startupOnInitialize() {
		super.startupOnInitialize();
		try {
			rsxml = Utils.getRuntimeRS(getProject());
			if (mc == null) {
				mc = Utils.getProjectMC(getProject());
			}
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e);
		}
		
		//
		// -- We don't get notified when the project is deleted so
		// -- listen for changes and remove the namespace when it gets deleted to avoid zombie objects.
		//
		IResourceChangeListener listener = new IResourceChangeListener() {
			public void resourceChanged(IResourceChangeEvent event) {
				try {
					if(event.getResource().equals(getProject()) && rsxml != null) {
						String id = rsxml.getRawLocation().toOSString();
						MetamergeConfigFactory.removeNamespace(id);
						getProject().getWorkspace().removeResourceChangeListener(this);
					}
				} catch (Exception e) {
					EclipseAppender.logerror(e.toString(), e);
				}
			}
		};
		getProject().getWorkspace().addResourceChangeListener(listener, IResourceChangeEvent.PRE_DELETE);
	}

	private void createEmptySettingsDocument(MetamergeConfig mc)
			throws Exception {

		// System store
		ContainerConfigImpl cc = new ContainerConfigImpl();
		cc.setName("SystemStore"); //$NON-NLS-1$
		cc.init();
		BaseConfigurationImpl bc = new BaseConfigurationImpl();
		bc.setName("Default"); //$NON-NLS-1$
		cc.addConfig(bc);
		cc.setMetamergeConfig(mc);
		mc.rebind(MetamergeConfig.DEFAULT_SERVER_FOLDER + "/SystemStore", cc); //$NON-NLS-1$

		// IDIServer - logger
		LogConfigImpl l = new LogConfigImpl();
		l.setName(MetamergeConfig.DEFAULT_SERVER_FOLDER + "/"
				+ MetamergeConfig.DEFAULT_SERVER_LOG);
		l.setMetamergeConfig(mc);
		try {
			mc.rebind(l.getName(), l);
		} catch (NameAlreadyBoundException ignore) {
		}

		// IDIServer - autostart
		InstanceConfigImpl ci = new InstanceConfigImpl();
		ci.init();
		ci.setName(MetamergeConfig.DEFAULT_SERVER_FOLDER + "/"
				+ MetamergeConfig.DEFAULT_SERVER_AUTOSTART);
		ci.setMetamergeConfig(mc);
		try {
			mc.rebind(ci.getName(), ci);
		} catch (NameAlreadyBoundException ignore) {
		}

		// IDIServer - Tombstones
		TombstonesConfigImpl ts = new TombstonesConfigImpl();
		ts.init();
		ts.setName(MetamergeConfig.DEFAULT_SERVER_FOLDER + "/"
				+ MetamergeConfig.DEFAULT_SERVER_TOMBSTONES);
		ts.setMetamergeConfig(mc);
		try {
			mc.rebind(ts.getName(), ts);
		} catch (NameAlreadyBoundException ignore) {
		}

	}

	private void doUpdateMC(BaseConfiguration bc) throws Exception {
		Name name = bc.getName();
		BaseConfiguration current = null;
		try {
			current = (BaseConfiguration) mc.lookup(name);
		} catch (NameNotFoundException nnfe) {
			// The name does not exist, just do a bind.
			mc.bind(name, bc);
			return;
		}
		if (bc instanceof FunctionConfig && current instanceof FunctionConfig) {
			doUpdate((FunctionConfig) bc, (FunctionConfig) current);
		} else if (bc instanceof ConnectorConfig
				&& current instanceof ConnectorConfig) {
			doUpdate((ConnectorConfig) bc, (ConnectorConfig) current);
		} else if (bc instanceof ParserConfig
				&& current instanceof ParserConfig) {
			doUpdate((ParserConfig) bc, (ParserConfig) current);
		} else if (bc instanceof AttributeMapConfig
				&& current instanceof AttributeMapConfig) {
			doUpdate((AttributeMapConfig) bc, (AttributeMapConfig) current);
		} else {
			mc.rebind(bc.getName(), bc);
			if (current != null)
				current.notifyChange(bc, "", MetamergeConfigChange.MCC_REPLACE);
		}
	}

	private void doUpdate(FunctionConfig source, FunctionConfig target)
			throws Exception {
		Set<String> oldKeys = getSet(target);
		for (Iterator<String> i = source.getDataIterator(); i.hasNext();) {
			String s = i.next();
			if (s.equals(InternalSchema.CONNECTOR_ATTRIBUTE_MAP_IN))
				doUpdate(source.getAttributeMap(true), target.getAttributeMap(true));
			else if (s.equals(InternalSchema.CONNECTOR_ATTRIBUTE_MAP_OUT))
				doUpdate(source.getAttributeMap(false), target.getAttributeMap(false));
			else if (s.equals(InternalSchema.CONNECTOR_HOOKS))
				doUpdate(source.getHooks(), target.getHooks());
			else if (s.equals(InternalSchema.CONNECTOR_SCHEMA_INPUT))
				doUpdate(source.getSchema(true), target.getSchema(true));
			else if (s.equals(InternalSchema.CONNECTOR_SCHEMA_OUTPUT))
				doUpdate(source.getSchema(false), target.getSchema(false));
			else if (s.equals(InternalSchema.CONNECTOR_SANDBOX_CONFIG))
				doUpdate(source.getSandboxConfig(), target.getSandboxConfig());
			else if (s.equals(InternalSchema.CONNECTOR_RECONNECT_CONFIG))
				doUpdate(source.getReconnectConfig(), target.getReconnectConfig());
			else if (s.equals(InternalSchema.FUNCTION_CONFIG))
				doUpdate(source.getFunctionConfig(), target.getFunctionConfig());
			else
				doUpdate(source, target, s);

			oldKeys.remove(s);
		}
		for (Iterator<String> i = oldKeys.iterator(); i.hasNext();) {
			target.removeParameter(i.next());
		}

		target.setupInheritanceChain();
		target.setModTS(source.getModTS());
	}

	private void doUpdate(ConnectorConfig source, ConnectorConfig target)
			throws Exception {
		Set<String> oldKeys = getSet(target);
		for (Iterator<String> i = source.getDataIterator(); i.hasNext();) {
			String s = i.next();
			if (s.equals(InternalSchema.CONNECTOR_ATTRIBUTE_MAP_IN))
				doUpdate(source.getAttributeMap(true), target.getAttributeMap(true));
			else if (s.equals(InternalSchema.CONNECTOR_ATTRIBUTE_MAP_OUT))
				doUpdate(source.getAttributeMap(false), target.getAttributeMap(false));
			else if (s.equals(InternalSchema.CONNECTOR_LINK_CONFIG))
				doUpdate(source.getLinkCriteria(), target.getLinkCriteria());
			else if (s.equals(InternalSchema.CONNECTOR_HOOKS))
				doUpdate(source.getHooks(), target.getHooks());
			else if (s.equals(InternalSchema.CONNECTOR_SCHEMA_INPUT))
				doUpdate(source.getSchema(true), target.getSchema(true));
			else if (s.equals(InternalSchema.CONNECTOR_SCHEMA_OUTPUT))
				doUpdate(source.getSchema(false), target.getSchema(false));
			else if (s.equals(InternalSchema.CONNECTOR_CONNECTOR_CONFIG))
				doUpdate(source.getConnectionConfig(), target.getConnectionConfig());
			else if (s.equals(InternalSchema.CONNECTOR_PARSER_CONFIG))
				doUpdate(source.getParserConfig(), target.getParserConfig());
			else if (s.equals(InternalSchema.CONNECTOR_DELTA_CONFIG))
				doUpdate(source.getDeltaConfig(), target.getDeltaConfig());
			else if (s.equals(InternalSchema.CONNECTOR_SANDBOX_CONFIG))
				doUpdate(source.getSandboxConfig(), target.getSandboxConfig());
			else if (s.equals(InternalSchema.AL_OPERATIONS))
				doUpdate(source.getOperations(), target.getOperations());
			else if (s.equals(InternalSchema.CONNECTOR_POOL_DEF_CONFIG))
				doUpdate(source.getPoolDefConfig(), target.getPoolDefConfig());
			else if (s.equals(InternalSchema.CONNECTOR_POOL_INSTANCE_CONFIG))
				doUpdate(source.getPoolInstanceConfig(), target.getPoolInstanceConfig());
			else if (s.equals(InternalSchema.CONNECTOR_RECONNECT_CONFIG))
				doUpdate(source.getReconnectConfig(), target.getReconnectConfig());
			else
				doUpdate(source, target, s);

			oldKeys.remove(s);
		}
		for (Iterator<String> i = oldKeys.iterator(); i.hasNext();) {
			target.removeParameter(i.next());
		}

		target.setupInheritanceChain();
		target.setModTS(source.getModTS());
	}

	private void doUpdate(LinkCriteriaConfig source, LinkCriteriaConfig target)
			throws Exception {
		Set<String> oldKeys = getSet(target);
		for (Iterator<String> i = source.getDataIterator(); i.hasNext();) {
			String s = i.next();
			if (! s.equals(InternalSchema.CONNECTOR_LINK_CRITERIA))
				doUpdate(source, target, s);
			oldKeys.remove(s);
		}
		for (Iterator<String> i = oldKeys.iterator(); i.hasNext();) {
			target.removeParameter(i.next());
		}

		oldKeys = getSet(target.getCriteria());
		for (Iterator<String> i = source.getCriteria().getDataIterator(); i.hasNext();) {
			String s = i.next();
			LinkCriteriaItem item = target.getCriteria(s);
			if (item == null)
				item = target.newCriteria(s);
			doUpdate(source.getCriteria(s), item);
			oldKeys.remove(s);
		}
		for (Iterator<String> i = oldKeys.iterator(); i.hasNext();) {
			target.removeCriteria(i.next());
		}

		target.setupInheritanceChain();
		target.setModTS(source.getModTS());
	}

	private void doUpdate(ReconnectConfig source, ReconnectConfig target)
			throws Exception {
		Set<String> oldKeys = getSet(target);
		for (Iterator<String> i = source.getDataIterator(); i.hasNext();) {
			String s = i.next();
			if (s.equals(InternalSchema.CONNECTOR_RECONNECT_RULES))
				doUpdate(source.getReconnectRules(), target.getReconnectRules());
			else
				doUpdate(source, target, s);
			oldKeys.remove(s);
		}
		for (Iterator<String> i = oldKeys.iterator(); i.hasNext();) {
			target.removeParameter(i.next());
		}
		target.setupInheritanceChain();
		target.setModTS(source.getModTS());
	}

	private void doUpdate(ParserConfig source, ParserConfig target)
			throws Exception {
		Set<String> oldKeys = getSet(target);
		for (Iterator<String> i = source.getDataIterator(); i.hasNext();) {
			String s = i.next();
			if (s.equals(InternalSchema.CONNECTOR_SCHEMA_INPUT))
				doUpdate(source.getSchema(true), target.getSchema(true));
			else if (s.equals(InternalSchema.CONNECTOR_SCHEMA_OUTPUT))
				doUpdate(source.getSchema(false), target.getSchema(false));
			else
				doUpdate(source, target, s);
			oldKeys.remove(s);
		}
		for (Iterator<String> i = oldKeys.iterator(); i.hasNext();) {
			target.removeParameter(i.next());
		}
		target.setupInheritanceChain();
		target.setModTS(source.getModTS());
	}

	private void doUpdate(AttributeMapConfig source, AttributeMapConfig target)
			throws Exception {
		Set<String> oldKeys = getSet(target);
		target.notifyChange(target, "", MetamergeConfigChange.BEGIN_CHANGES);
		for (Iterator<String> i = source.getDataIterator(); i.hasNext();) {
			String s = i.next();
			if (s.equals(InternalSchema.INHERITS_FROM)||s.equals(InternalSchema.NULL_BEHAVIOR) ||
					s.equals(InternalSchema.NULL_BEHAVIOR_VALUE) ||
					s.equals(InternalSchema.NULL_DEFINITION) ||
					s.equals(InternalSchema.NULL_DEFINITION_VALUE))
				doUpdate(source, target, s);
			else
				doUpdate(source.getAttributeMapItem(s), target
						.getAttributeMapItem(s));
			oldKeys.remove(s);
		}
		for (Iterator<String> i = oldKeys.iterator(); i.hasNext();) {
			target.removeAttributeMapItem(i.next());
		}
		target.notifyChange(target, "", MetamergeConfigChange.END_CHANGES);
		target.setupInheritanceChain();
		target.setModTS(source.getModTS());
	}

	private void doUpdate(AttributeMapItem source, AttributeMapItem target)
			throws Exception {
		Set<String> oldKeys = getSet(target);
		for (Iterator<String> i = source.getDataIterator(); i.hasNext();) {
			String s = i.next();
			if (!s.equals("childAttributeMaps"))
				doUpdate(source, target, s);
			oldKeys.remove(s);
		}
		for (Iterator<String> i = oldKeys.iterator(); i.hasNext();) {
			target.removeParameter(i.next());
		}
		doUpdate(source.getChildAttributeMaps(), target.getChildAttributeMaps());
		target.setModTS(source.getModTS());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void doUpdate(List source, List target) {
		// TODO: Do this better
		while (target.size() > 0)
			target.remove(0);
		for (int i = 0; i < source.size(); i++)
			target.add(source.get(i));
	}

	private void doUpdate(HooksConfig source, HooksConfig target)
			throws Exception {
		Set<String> oldKeys = getSet(target);
		for (Iterator<String> i = source.getDataIterator(); i.hasNext();) {
			String s = i.next();
			if (s.equals(InternalSchema.INHERITS_FROM))
				doUpdate(source, target, s);
			else
				doUpdate(source.getHook(s), target.getHook(s));
			oldKeys.remove(s);
		}
		for (Iterator<String> i = oldKeys.iterator(); i.hasNext();) {
			String s = i.next();
			if (s.equals(InternalSchema.INHERITS_FROM)) {
				target.removeParameter(s);
			} else {
				// No need to remove an old hook that only has a name.
				boolean remove = false;
				for (Iterator<String> hookParam = getSet(target.getHook(s)).iterator(); hookParam.hasNext();) {
					if (!"name".equals(hookParam.next()))
						remove = true;
				}
				if (remove)
					target.removeHook(s);
			}
		}
		target.setupInheritanceChain();
		target.setModTS(source.getModTS());
	}

	private void doUpdate(HookConfig source, HookConfig target)
			throws Exception {
		Set<String> oldKeys = getSet(target);
		for (Iterator<String> i = source.getDataIterator(); i.hasNext();) {
			String s = i.next();
			doUpdate(source, target, s);
			oldKeys.remove(s);
		}
		for (Iterator<String> i = oldKeys.iterator(); i.hasNext();) {
			target.removeParameter(i.next());
		}
		if (target.getInheritsFromRef() != null
				&& !target.getInheritsFromRef().equals(
						BaseConfiguration.INHERIT_PARENT))
			target.setupInheritanceChain();
		target.setModTS(source.getModTS());
	}

	private void doUpdate(SchemaConfig source, SchemaConfig target)
			throws Exception {
		Set<String> oldKeys = getSet(target);
		target.notifyChange(target, "", MetamergeConfigChange.BEGIN_CHANGES);
		for (Iterator<String> i = source.getDataIterator(); i.hasNext();) {
			String s = i.next();
			if (s.equals(InternalSchema.INHERITS_FROM))
				doUpdate(source, target, s);
			else if (target.hasParameter(s))
				doUpdate(source.getItem(s), target.getItem(s));
			else
				target.setItem(s, source.getItem(s));
			oldKeys.remove(s);
		}
		for (Iterator<String> i = oldKeys.iterator(); i.hasNext();) {
			String s = i.next();
			if (s.equals(InternalSchema.INHERITS_FROM))
				target.removeParameter(s);
			else
				target.removeItem(s);
		}
		target.notifyChange(target, "", MetamergeConfigChange.END_CHANGES);
		target.setupInheritanceChain();
		target.setModTS(source.getModTS());
	}

	private void doUpdate(SchemaItemConfig source, SchemaItemConfig target)
			throws Exception {
		Set<String> oldKeys = getSet(target);
		for (Iterator<String> i = source.getDataIterator(); i.hasNext();) {
			String s = i.next();
			doUpdate(source, target, s);
			oldKeys.remove(s);
		}
		for (Iterator<String> i = oldKeys.iterator(); i.hasNext();) {
			target.removeParameter(i.next());
		}
		doUpdate(source.getChildSchemaList(), target.getChildSchemaList());
		target.setModTS(source.getModTS());
	}

	private void doUpdate(ContainerConfig source, ContainerConfig target) {
		// TODO: Try to do this better
		while (target.size() > 0)
			target.removeConfig(0);
		for (int i = 0; i < source.size(); i++)
			target.addConfig(source.getConfig(i));
		target.setModTS(source.getModTS());
	}

	private void doUpdate(BaseConfiguration source, BaseConfiguration target)
			throws Exception {
		Set<String> oldKeys = getSet(target);
		for (Iterator<String> i = source.getDataIterator(); i.hasNext();) {
			String s = i.next();
			doUpdate(source, target, s);
			oldKeys.remove(s);
		}
		for (Iterator<String> i = oldKeys.iterator(); i.hasNext();) {
			target.removeParameter(i.next());
		}
		target.setupInheritanceChain();
		target.setModTS(source.getModTS());
	}

	private void doUpdate(BaseConfiguration source, BaseConfiguration target,
			String paramName) {
		Object newVal = source.getParameterRaw(paramName);
		if (!newVal.equals(target.getParameterRaw(paramName)))
			target.setParameter(paramName, newVal);
		if (source.isProtectedParameter(paramName))
			target.setProtectedParameter(paramName);
	}

	@SuppressWarnings("unchecked")
	private Set<String> getSet(BaseConfiguration bc) {
		if (bc.getData() == null)
			return new HashSet<String>();
		else
			return new HashSet<String>(bc.getData().keySet());
	}
}
