/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.naming.Binding;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.ALMappingConfig;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.BranchingConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;

import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.MetamergeFolder;
import com.ibm.di.config.interfaces.NamespaceConfig;
import com.ibm.di.config.interfaces.ParserConfig;
import com.ibm.di.config.interfaces.RawConnectorConfig;
import com.ibm.di.config.interfaces.RawFunctionConfig;
import com.ibm.di.config.interfaces.ScriptConfig;
import com.ibm.di.config.interfaces.SolutionInterface;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.ServerConstants;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.natures.TDINature;

public class ConfigUtils {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Reference to the dummy system attribute map used when displaying
	 */
	public final static String SYSTEM_ATTRIBUTE_MAP = "system:/" + Messages.getString("Localized.AttributeMap");
	
	// Shared static logger object
	public static Logger logger = LogManager.getLogger("com.ibm.di.admin");

	public static BaseConfiguration locateForm(String formName) {
		try {
			return (BaseConfiguration) MetamergeConfigFactory.lookup(null, formName);
		} catch (Exception ignore3) {
		}

		return null;
	}

	public static ConnectorConfig createScriptConnector(MetamergeConfig config, String name) throws Exception {
		ConnectorConfig nc = (ConnectorConfig) config.newInstanceOf(MetamergeConfig.DEFAULT_CONNECTOR_FOLDER);
		nc.setName(name);
		nc.setMode(ConnectorConfig.SCRIPT_MODE);
		return nc;
	}

	public static ParserConfig createInheritedParser(ParserConfig config) throws Exception {
		return createInheritedParser(config.getMetamergeConfig(), config);
	}

	public static ParserConfig createInheritedParser(MetamergeConfig referent, ParserConfig config) throws Exception {
		ParserConfig nc = (ParserConfig) referent.newInstanceOf(MetamergeConfig.DEFAULT_PARSER_FOLDER);
		nc.setName(config.getShortName());
		nc.init();

		updateInheritsFrom(referent, config, nc);
//		String namespace = (String) MetamergeConfigFactory.getLocalNamespaceFor(referent, config);
//		if (namespace != null)
//			nc.setInheritsFromRef(namespace + ":/" + config.getName().toString());
//		else
//			nc.setInheritsFromRef("/" + config.getName().toString());

		nc.setupInheritanceChain();
		return nc;
	}

	public static Vector<String> getAlConnectorList(javax.naming.Name name, AssemblyLineConfig parent) {
		return new Vector<String>(getAlConnectorList2(name, parent).keySet());
	}

	public static Hashtable<String,ConnectorConfig> getAlConnectorList2(javax.naming.Name name, AssemblyLineConfig parent) {
		Hashtable<String,ConnectorConfig> v = new Hashtable<String,ConnectorConfig>();

		if (parent == null)
			return v;

		for (int i = 0; i < parent.getConnectorCount(); i++) {
			try {
				ConnectorConfig c = parent.getConnector(i);
				if (c instanceof ALMappingConfig || c instanceof FunctionConfig)
					continue;
				if (c.getName() == null || c.getName().equals(name))
					continue;
				if (ConnectorConfig.SCRIPT_MODE.equals(c.getMode()))
					continue;
				RawConnectorConfig rcc = c.getConnectionConfig();
				if (rcc == null)
					continue;
				String javaClass = rcc.getJavaClass();
				if (javaClass == null || javaClass.startsWith("@"))
					continue;
				v.put("@" + c.getShortName(), c);
			} catch (Exception ignore) {
			}
		}
		return v;
	}

	public static Vector<String> getReuseConnectors(BaseConfiguration config) {
		javax.naming.Name name = null;
		while (config != null) {
			if (config instanceof ConnectorConfig) {
				name = config.getName();
			} else if (config instanceof AssemblyLineConfig) {
				return getAlConnectorList(name, (AssemblyLineConfig) config);
			}
			config = config.getParent();
		}
		return new Vector<String>();
	}

	public static ConnectorConfig createReusedConnector(MetamergeConfig referent, AssemblyLineConfig alconfig, String original)
			throws Exception {
		ConnectorConfig nc = (ConnectorConfig) referent.newInstanceOf(MetamergeConfig.DEFAULT_CONNECTOR_FOLDER);
		nc.init();
		nc.setParent(alconfig);
		nc.setInheritsFromRef("@" + original);
		nc.getConnectionConfig().setInheritsFromRef(BaseConfiguration.INHERIT_PARENT);
		nc.getConnectionConfig().setJavaClass("@" + original);
		nc.getParserConfig().setInheritsFromRef(BaseConfiguration.INHERIT_PARENT);
		nc.getSchema(ConnectorConfig.SCHEMA_INPUT).setInheritsFromRef(BaseConfiguration.INHERIT_PARENT);
		nc.getSchema(ConnectorConfig.SCHEMA_OUTPUT).setInheritsFromRef(BaseConfiguration.INHERIT_PARENT);
		nc.getAttributeMap(true).setInheritsFromRef(BaseConfiguration.INHERIT_PARENT);
		nc.getAttributeMap(false).setInheritsFromRef(BaseConfiguration.INHERIT_PARENT);
		nc.getLinkCriteria().setInheritsFromRef(BaseConfiguration.INHERIT_PARENT);
		nc.getHooks().setInheritsFromRef(BaseConfiguration.INHERIT_PARENT);

		nc.setupInheritanceChain();
		return nc;
	}

	public static ConnectorConfig createInheritedConnector(ConnectorConfig config) throws Exception {
		return createInheritedConnector(config.getMetamergeConfig(), config);
	}

	public static ConnectorConfig createInheritedConnector(MetamergeConfig referent, ConnectorConfig config) throws Exception {
		ConnectorConfig nc = (ConnectorConfig) referent.newInstanceOf(MetamergeConfig.DEFAULT_CONNECTOR_FOLDER);
		nc.setName(config.getShortName());
		nc.setMode(config.getMode());
		nc.init();

		updateInheritsFrom(referent, config, nc);
//		String namespace = (String) MetamergeConfigFactory.getLocalNamespaceFor(referent, config);
//		if (namespace != null)
//			nc.setInheritsFromRef(namespace + ":/" + config.getName().toString());
//		else
//			nc.setInheritsFromRef("/" + config.getName().toString());

		nc.getConnectionConfig().setInheritsFromRef(BaseConfiguration.INHERIT_PARENT);
		nc.getParserConfig().setInheritsFromRef(BaseConfiguration.INHERIT_PARENT);
		// D919 - inherit everything but Delta settings
		nc.getSchema(ConnectorConfig.SCHEMA_INPUT).setInheritsFromRef(BaseConfiguration.INHERIT_PARENT);
		nc.getSchema(ConnectorConfig.SCHEMA_OUTPUT).setInheritsFromRef(BaseConfiguration.INHERIT_PARENT);
		nc.getAttributeMap(true).setInheritsFromRef(BaseConfiguration.INHERIT_PARENT);
		nc.getAttributeMap(false).setInheritsFromRef(BaseConfiguration.INHERIT_PARENT);
		nc.getLinkCriteria().setInheritsFromRef(BaseConfiguration.INHERIT_PARENT);
		nc.getHooks().setInheritsFromRef(BaseConfiguration.INHERIT_PARENT);

		nc.setupInheritanceChain();

		if (nc.getParameter("autoreconnect") == null)
			nc.setParameter("autoreconnect", "true");
		return nc;
	}

	public static FunctionConfig createInheritedFunction(FunctionConfig config) throws Exception {
		return createInheritedFunction(config.getMetamergeConfig(), config);
	}

	public static FunctionConfig createInheritedFunction(MetamergeConfig referent, FunctionConfig config) throws Exception {
		FunctionConfig nc = (FunctionConfig) referent.newInstanceOf(MetamergeConfig.DEFAULT_FUNCTION_FOLDER);
		nc.setName(config.getShortName());
		nc.init();

		updateInheritsFrom(referent, config, nc);
//		String namespace = (String) MetamergeConfigFactory.getLocalNamespaceFor(referent, config);
//		if (namespace != null)
//			nc.setInheritsFromRef(namespace + ":/" + config.getName().toString());
//		else
//			nc.setInheritsFromRef("/" + config.getName().toString());

		nc.getFunctionConfig().setInheritsFromRef(BaseConfiguration.INHERIT_PARENT);
		nc.getAttributeMap(true).setInheritsFromRef(BaseConfiguration.INHERIT_PARENT);
		nc.getAttributeMap(false).setInheritsFromRef(BaseConfiguration.INHERIT_PARENT);
		nc.getHooks().setInheritsFromRef(BaseConfiguration.INHERIT_PARENT);

		nc.setupInheritanceChain();
		return nc;
	}

	public static ALMappingConfig createInheritedALMap(ALMappingConfig config) throws Exception {
		return createInheritedALMap(config.getMetamergeConfig(), config);
	}
	
	public static void updateInheritsFrom(MetamergeConfig referent, BaseConfiguration config, BaseConfiguration target) {
		
		IProject sourceProject = null;
		IProject targetProject = null;
		String path = null;
		
		if(referent instanceof TDIConfigurationFile) {
			sourceProject = ((TDIConfigurationFile)referent).getProject();
		}
		
		if(config.getMetamergeConfig() instanceof TDIConfigurationFile) {
			targetProject = ((TDIConfigurationFile)config.getMetamergeConfig()).getProject();
		} else {
			path = config.getMetamergeConfig().toString();
		}
		
		// If they are in the same project we dont include namespace id
		String folder = TDINature.getDefaultFolder(config);
		
		if(sourceProject != null && targetProject != null) {
			if(sourceProject.equals(targetProject)) {
				target.setInheritsFromRef("/" + folder + "/" + config.getShortName());
				return;
			}
		} else if (sourceProject != null && path != null) {
			try {
				if(Utils.getRuntimeRS(sourceProject).getLocation().toOSString().equals(path)) {
					target.setInheritsFromRef("/" + folder + "/" + config.getShortName());
					return;
				}
			} catch (CoreException e) {
				EclipseAppender.logerror(e.toString(), e);
			}
		}
		
		String inheritsFrom = "/" + config.getName().toString();
		String namespace = (String) MetamergeConfigFactory.getLocalNamespaceFor(referent, config);
		if (namespace != null)
			inheritsFrom = namespace + ":" + inheritsFrom;
		if (SYSTEM_ATTRIBUTE_MAP.equals(inheritsFrom))
			inheritsFrom = BaseConfiguration.INHERIT_NONE;
		target.setInheritsFromRef(inheritsFrom);
	}

	public static ALMappingConfig createInheritedALMap(MetamergeConfig referent, ALMappingConfig config) throws Exception {
		ALMappingConfig nc = (ALMappingConfig) referent.newInstanceOf(MetamergeConfig.DEFAULT_ATTRIBUTEMAP_FOLDER);
		nc.setName(config.getShortName());
		nc.init();
		
		updateInheritsFrom(referent, config, nc);
//		String namespace = (String) MetamergeConfigFactory.getLocalNamespaceFor(referent, config);
//		if (namespace != null)
//			nc.setInheritsFromRef(namespace + ":/" + config.getName().toString());
//		else
//			nc.setInheritsFromRef("/" + config.getName().toString());

		nc.getAttributeMap().setInheritsFromRef(BaseConfiguration.INHERIT_PARENT);
		nc.setupInheritanceChain();
		return nc;
	}

	public static void addFolderNames(MetamergeConfig local, MetamergeConfig mc, int type, Vector<String> v) {
		if (mc == null)
			return;

		MetamergeFolder fld = null;
		try {
			fld = mc.getDefaultFolder(type);
		} catch (Exception err) {
			logerror("", err);
		}
		if (fld == null)
			return;

		Vector<String> names = null;
		try {
			names = new Vector<String>(Arrays.asList(fld.getNames()));
		} catch (Exception err) {
			logerror("", err);
		}
		if (names == null)
			return;

		Collections.sort(names);
		for (String name:names) {
			Object ns = MetamergeConfigFactory.getLocalNamespaceFor(local, fld);
			if (ns != null)
				v.add(ns + ":/" + fld.getName() + "/" + name);
			else if (type == MetamergeConfig.ASSEMBLYLINE_FOLDER)
				v.add(name);
			else
				v.add("/" + fld.getName() + "/" + name);
		}
	}

	public static void addConnectorNames(MetamergeConfig local, Vector<String> v, boolean parser) {
		if (local == null)
			return;

		MetamergeFolder fld = null;
		try {
			fld = local.getDefaultFolder(MetamergeConfig.CONNECTOR_FOLDER);
		} catch (Exception err) {
			logerror("", err);
		}
		if (fld == null)
			return;

		Vector<String> names = null;
		try {
			names = new Vector<String>(Arrays.asList(fld.getNames()));
		} catch (Exception err) {
			logerror("", err);
		}
		if (names == null)
			return;

		Collections.sort(names);
		for (int i = 0; i < names.size(); i++) {
			String connectorName = "/" + fld.getName() + "/" + names.get(i);
			ConnectorConfig cc = null;
			try {
				cc = local.getConnector(connectorName);
			} catch (Exception err) {
				logerror("", err);
			}
			if (cc == null)
				continue;
			if (parser) {
				ParserConfig pc = cc.getParserConfig();
				if (pc == null || pc.getJavaClass() == null)
					continue;
			}
			v.add(connectorName);
		}
	}

	public static Vector<String> getAvailableSystemComponents(BaseConfiguration config, int type) {
		return getAvailableSystemComponents(config, type, true);
	}
	
	public static Vector<String> getAvailableSystemComponents(BaseConfiguration config, int type, 
			boolean includeSystem) {
		Vector<String> v = new Vector<String>();
		try {

			MetamergeConfig local;
			if (config instanceof MetamergeConfig)
				local = (MetamergeConfig) config;
			else
				local = config.getMetamergeConfig();
			
			// Get the project XML file
			if(local instanceof TDIConfigurationFile) {
				local = Utils.getProjectMC(((TDIConfigurationFile)local).getProject());
			}
			addFolderNames(local, local, type, v);
			
			// Hard to say where the logic should be for deciding to e.g. show connectors
			// when we want parsers. For now, let the logic be somewhere else.
			//if (type == MetamergeConfig.PARSER_FOLDER || type == MetamergeConfig.ATTRIBUTEMAP_FOLDER)
			//	addConnectorNames(local, v, type == MetamergeConfig.PARSER_FOLDER);
			//if (type == MetamergeConfig.ATTRIBUTEMAP_FOLDER)
			//	addFolderNames(local, local, MetamergeConfig.FUNCTION_FOLDER, v);

			if (config.getName() != null)
				v.remove("/" + config.getName().toString());

			// Get names from packages
			List<MetamergeConfig> list = MetamergeConfigFactory.getPackages();
			for (int i = 0; i < list.size(); i++) {
				addFolderNames(local, list.get(i), type, v);
			}

			try {
				MetamergeFolder mf = (MetamergeFolder) local.lookup(MetamergeConfig.DEFAULT_NAMESPACE_FOLDER);
				Enumeration<Binding> l = mf.list();
				while (l.hasMoreElements()) {
					NamespaceConfig nc = (NamespaceConfig) l.nextElement().getObject();
					addFolderNames(local, MetamergeConfigFactory.loadNamespace(nc), type, v);
				}			
			} catch (Exception e) {
				SystemFunctions.doNothing();
			}

			// Get names from system namespace
			// (we don't release scripts in the system namespace but we add some in the CE for convenience)
			if(includeSystem &&
					type != MetamergeConfig.SCRIPT_FOLDER && 
					type != MetamergeConfig.ASSEMBLYLINE_FOLDER &&
					type != MetamergeConfig.ATTRIBUTEMAP_FOLDER)
				addFolderNames(local, MetamergeConfigFactory.getNamespace(MetamergeConfigFactory.SYSTEM_NAMESPACE), type, v);

		} catch (Exception error) {
			messageError(error);
		}

		return v;
	}

	public static Vector<String> getComponentsAsStrings(BaseConfiguration config, int type) {
		return getComponentsAsStrings(config, type, true);
	}
	
	public static Vector<String> getComponentsAsStrings(BaseConfiguration config, int type,
			boolean includeSystem) {
		Vector<String> ret = new Vector<String>();

		TDIConfigurationFile local = null;
		if (config instanceof TDIConfigurationFile)
			local = (TDIConfigurationFile) config;
		else if ( config.getMetamergeConfig() instanceof TDIConfigurationFile )
			local = (TDIConfigurationFile) config.getMetamergeConfig();

		for( Object obj:getAvailableSystemComponents(config, type, includeSystem)) {
			if ( obj instanceof String)
				ret.add((String)obj);
			if ( obj instanceof IFile && local != null ) {
				try {
					ret.add(local.addReference((IFile)obj, null));
				} catch (Exception e) {
					ret.add(((IFile)obj).getName());
				}
			}
		}
		return ret;
	}

	public static Vector<String> getLibConnectors(BaseConfiguration config) {
		return getComponentsAsStrings(config, MetamergeConfig.CONNECTOR_FOLDER, false);		
	}

	public static void addFiles(IContainer container, List<Object> files, String extension) throws Exception {
		for(IResource res : container.members()) {
			if(res instanceof IFile) {
				IFile file = (IFile)res;
				if(extension == null || extension.equals(file.getFileExtension()))
					files.add(file);
			} else if (res instanceof IContainer) {
				addFiles((IContainer)res, files, extension);
			}
		}
	}

	public static void getAllFiles(BaseConfiguration config, Vector<Object>v, int type) throws Exception {
		v.addAll(getAvailableSystemComponents(config, type));
		// Get names from local project (This will be IFiles
//		String extension = TDIConfigurationFile.getExtensionFor(type);
//		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
//		for (IResource res : root.members()) {
//			if (res instanceof IProject) {
//				IProject p = (IProject) res;
//				if (p.isOpen() && p.hasNature(TDINature.TDI_NATURE_ID))
//					addFiles(p, v, extension);
//			}
//		}
	}
	
	private static void messageError(Exception error) {
		EclipseAppender.logerror(error.getMessage(), error);
	}


	public static void logmsg(String msg) {
		logger.info(msg);
	}

	public static void logerror(String msg, Throwable error) {
		logger.error(msg, error);
	}

	public static void logdebug(String msg) {
		logger.debug(msg);
	}

	public static void logwarn(String msg) {
		logger.warn(msg);
	}

	public static BaseConfiguration createInheritedComponent(MetamergeConfig mc, BaseConfiguration obj) throws Exception {
		if(obj instanceof FunctionConfig)
			return createInheritedFunction(mc, (FunctionConfig) obj);
		else if(obj instanceof ALMappingConfig)
			return createInheritedALMap(mc, (ALMappingConfig) obj);
		else if(obj instanceof ScriptConfig)
			return createInheritedScript(mc, (ScriptConfig) obj);
		else if(obj instanceof ConnectorConfig)
			return createInheritedConnector(mc, (ConnectorConfig) obj);
		else if(obj instanceof ParserConfig)
			return createInheritedParser(mc, (ParserConfig) obj);
		else
			return obj;
	}

	private static BaseConfiguration createInheritedScript(MetamergeConfig mc, ScriptConfig config) throws Exception {
		BaseConfiguration obj = mc.newInstanceOf(MetamergeConfig.SCRIPT_FOLDER);
		obj.setName(config.getShortName());
		if (config.getMetamergeConfig() != null && config.getMetamergeConfig() != mc) {
			updateInheritsFrom(mc, config, obj);
			obj.setupInheritanceChain();
		}
		
		if (obj.getScript() == null )
			obj.setScript(config.getScript());

		obj.init();
		return obj;
	}

//	private static BaseConfiguration createInheritedMap(MetamergeConfig mc, ALMappingConfig config) throws Exception {
//		ALMappingConfig obj = (ALMappingConfig) mc.newInstanceOf(MetamergeConfig.ATTRIBUTEMAP_FOLDER);
//		obj.setName(config.getShortName());
//		obj.init();
//
//		String ns = (String) MetamergeConfigFactory.getNamespaceFor(config);
//		if("system".equals(ns) || ns == null) {
//			for(Object str : config.getAttributeMap().getAttributeNames()) {
//				AttributeMapItem ami = config.getAttributeMap().getAttributeMapItem(str);
//				ami = AttributeMapItemImpl.clone(ami);
//				ami.setName((String)str);
//				obj.getAttributeMap().setAttributeMapItem(ami);
//			}
//		} else if (config.getMetamergeConfig() != mc) {
//			updateInheritsFrom(mc, config, obj);
//			obj.setupInheritanceChain();
//		}
//		return obj;
//	}

	public static void showHelp(BaseConfiguration bc){
		if (bc instanceof SolutionInterface) {
			showHelp("SOLUTIONINTERFACE");
			return;
		}
		
		String className = null;
		if (bc instanceof RawConnectorConfig)
			className = ((RawConnectorConfig) bc).getJavaClass();
		else if (bc instanceof RawFunctionConfig)
			className = ((RawFunctionConfig)bc).getJavaClass();
		else if (bc instanceof ParserConfig)
			className = ((ParserConfig) bc).getJavaClass();
		else if (bc instanceof FunctionConfig)
			className = ((FunctionConfig) bc).getJavaClass();
		else if (bc instanceof ConnectorConfig)
			className = ((ConnectorConfig) bc).getConnectionConfig().getJavaClass();
		
		if (className == null) {      	
			EclipseAppender.loginfo(Messages.getMessage("miadmin.error.help.not.found", bc.toString()));
			return;
		}
		
		// Some special connector cases,		
		if (bc instanceof RawConnectorConfig) {
			if (className.startsWith("@")) {
				showHelp("REUSING_CONNECTOR");
				return;
			}

			while (bc != null && bc.getParent() != null) {
				String longName = className + "." + bc.getParent().getShortName();
				String url = ResourceHash.getHash("helplocations").getString(longName);
				if (url != null && ! url.equals(longName)) {
					showURLHelp(url);
					return;
				}
				bc = bc.getInheritsFrom();
			}
		}
		
		showHelp(className);
	}

	public static void showHelp(String name){
		String url = ResourceHash.getHash("helplocations").getString(name);
		if (url == null || name.equals(url)) {
			String msg = Messages.getMessage("miadmin.error.help.not.found", name);
			EclipseAppender.logerror(msg, new Exception(msg), Display.getCurrent().getActiveShell());
			return;
		}
		showURLHelp(url);
	}

	public static void showURLHelp(String url){
		if (url.indexOf('/') != 0)
			url = "/" + url;

		String helpHost = System.getProperty("com.ibm.di.helpHost");

		if (helpHost != null && helpHost.length() > 0) {
			String helpFolder = "";
			int i = helpHost.indexOf('/');
			if (i > 0) {
				helpFolder = helpHost.substring(i);
				helpHost = helpHost.substring(0, i);
			}
			String helpPort = System.getProperty("com.ibm.di.helpPort");
			if (helpPort != null && helpPort.length() > 0)
				helpPort = ":" + helpPort;
			else
				helpPort = "";

			showURL("http://" + helpHost + helpPort + helpFolder + "?topic=" + url);
		} else {
			// TODO: Use a more correct error message
			EclipseAppender.loginfo(Messages.getMessage("miadmin.error.help.not.found", url));
		}
	}
	
	public static void showURL(String url) {

		// --
		if(url.startsWith("file://")) {
			try {
				File file = new File(url.substring(7));
				if (! file.isDirectory()) {
					IFileStore fileStore = EFS.getLocalFileSystem().fromLocalFile(file);
					if (fileStore != null) {
						IDE.openEditorOnFileStore(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), fileStore);
						return;
					}
				}
			} catch (Exception e) {
				EclipseAppender.logerror(e.toString(), e);
			}
			url = url.replaceAll(" ", "%20");
		}

		String os = System.getProperty("os.name");
		if (os == null)
			os = "";

		if (os.indexOf("Windows") >= 0)
			tryToOpenOnWindows(url);
		else
			tryToOpenOnUnix(url);
	}

	public static void tryToOpenOnWindows(String url) {
		Program.launch(url);
	}
	
	public static void tryToOpenOnUnix(String url) {

		String browser = "mozilla";
		try {
			Runtime.getRuntime().exec(new String[] { browser, url });
			return;
		} catch (Exception e2) {
		}

		browser = "netscape";
		try {
			Runtime.getRuntime().exec(new String[] { browser, url });
			return;
		} catch (Exception e3) {
		}
		
		browser = "firefox";
		try {
			Runtime.getRuntime().exec(new String[] { browser, url });
			return;
		} catch (Exception e3) {
		}

		try {
			Program.launch(url);
		} catch (Throwable t) {
			EclipseAppender.logerror(t.toString(), t);
		}
		
	}

	public static String getStdMode(ConnectorConfig cc) {
		String s = cc.getMode();
		return ServerConstants.getTypeString(ServerConstants.getType(s));
	}

	public static boolean isSwitch(Object b) {
		if (! (b instanceof BranchingConfig))
			return false;
		return ((BranchingConfig)b).getBranchType() == BranchingConfig.BRANCH_SWITCH;
	}
	
	public static boolean isCase(Object b) {
		if (! (b instanceof BranchingConfig))
			return false;
		return ((BranchingConfig)b).getBranchType() == BranchingConfig.BRANCH_CASE;
	}
}
