/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.eclipse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.UUID;

import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.di.config.base.BaseConfigurationImpl;
import com.ibm.di.config.base.ContainerConfigImpl;
import com.ibm.di.config.base.PropertyManagerImpl;
import com.ibm.di.config.interfaces.ALMappingConfig;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.NamespaceConfig;
import com.ibm.di.config.interfaces.ParserConfig;
import com.ibm.di.config.interfaces.PropertyManager;
import com.ibm.di.config.interfaces.PropertyStoreConfig;
import com.ibm.di.config.interfaces.SchedulerConfig;
import com.ibm.di.config.interfaces.ScriptConfig;
import com.ibm.di.config.interfaces.SequenceConfig;
import com.ibm.di.config.interfaces.TDIProperties;
import com.ibm.di.config.xml.ContainerFactory;
import com.ibm.di.config.xml.Factories;
import com.ibm.di.config.xml.MetamergeConfigXML;
import com.ibm.di.server.ResourceHash;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.builders.ProjectRuntimeDirectory;

public class TDIConfigurationFile extends MetamergeConfigXML {
	@SuppressWarnings("unused") //$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final String CONFIG_ATTRIBUTE_PROJECT = "project"; //$NON-NLS-1$

	private static final String CONFIG_ATTRIBUTE_UUID = "UUID"; //$NON-NLS-1$

	public static final String RESOURCES_FOLDER = "Resources"; //$NON-NLS-1$

	public static final String REF_FILE = "name"; //$NON-NLS-1$

	public static final String REF_INTERNAL = "internal"; //$NON-NLS-1$

	public static final String REF_CYCLE = "cycle"; //$NON-NLS-1$
	
	// Name of the references container
	private static final String REFERENCES = "References"; //$NON-NLS-1$

	private TDIPropertiesCE tdiProperties;

	// Root element attribute name for version number
	public final static String CYCLE_TAG = "Cycle"; //$NON-NLS-1$

	/*
	 * Extension --> type mapping
	 */
	public final static String XT_ASSEMBLYLINE = "assemblyline"; //$NON-NLS-1$

	public final static String XT_CONNECTOR = "connector"; //$NON-NLS-1$

	public final static String XT_PARSER = "parser"; //$NON-NLS-1$

	public final static String XT_ATTRMAP = "attributemap"; //$NON-NLS-1$

	public final static String XT_FUNCTION = "function"; //$NON-NLS-1$

	public final static String XT_SCRIPT = "script"; //$NON-NLS-1$

	public final static String XT_NAMESPACE = "reference"; //$NON-NLS-1$
	
	public final static String XT_PROPSTORE = "tdiproperties"; //$NON-NLS-1$

	public final static String XT_SCHEDULER = "scheduler"; //$NON-NLS-1$
	
	public final static String XT_SEQUENCE = "sequence"; //$NON-NLS-1$
	
	public final static String MC_URLBASE = "com.ibm.di.config.urlbase"; //$NON-NLS-1$

	protected BaseConfiguration configObject;

	protected Exception configError;

	private boolean initComplete;

	private IFile file;

	private final static ResourceHash sResHash = BaseConfigurationImpl.getResHash();

	private static final String REF_PARAM_OBJECT_NAME = "%%%%"; //$NON-NLS-1$
	public static final int PARAM_OBJECT = 0;
	public static final int LOCAL_COPY = 1;
	
	private PropertyManagerImpl pm;

	private ContainerConfig refs;
	public static final String[] FILE_EXTENSIONS = new String[]{
		XT_ASSEMBLYLINE,
		XT_ATTRMAP,
		XT_CONNECTOR,
		XT_FUNCTION,
		XT_PARSER,
		XT_PROPSTORE,
		XT_SCRIPT,
		XT_NAMESPACE,
		XT_SCHEDULER,
		XT_SEQUENCE,
	};
	
	static final String[] FILE_EXTENSION_FOLDERS = new String[]{
		MetamergeConfig.DEFAULT_ASSEMBLYLINE_FOLDER,
		MetamergeConfig.DEFAULT_ATTRIBUTEMAP_FOLDER,
		MetamergeConfig.DEFAULT_CONNECTOR_FOLDER,
		MetamergeConfig.DEFAULT_FUNCTION_FOLDER,
		MetamergeConfig.DEFAULT_PARSER_FOLDER,
		MetamergeConfig.DEFAULT_PROPSTORE_FOLDER,
		MetamergeConfig.DEFAULT_SCRIPT_FOLDER,
		MetamergeConfig.DEFAULT_NAMESPACE_FOLDER,
		MetamergeConfig.DEFAULT_SCHEDULER_FOLDER,
		MetamergeConfig.DEFAULT_SEQUENCE_FOLDER,
	};

	/**
	 * This method reads the specified file into a TDIConfigurationFile instance.
	 * 
	 * @param file
	 * @return {@link TDIConfigurationFile} object
	 * @throws Exception
	 */
	public static TDIConfigurationFile loadFile(IFile file) throws Exception {
		return new TDIConfigurationFile(file);
	}
	
	public static String file2ns(IFile file) {
		return file.getFullPath().toPortableString();
	}
	
	public String getNamespaceID() {
		return file2ns(file);
	}
	
	public TDIConfigurationFile() throws Exception {
		this(null);
	}

	public TDIConfigurationFile(IFile file) throws Exception {
		this(file, false);
	}
	
	@SuppressWarnings("unchecked")
	public TDIConfigurationFile(Object input, boolean defaultFolders) throws Exception {
		env = new Hashtable<String,Object>();
		if(input instanceof IFile) {
			this.file = (IFile)input;
			if (file.exists()) {
				env.put(MetamergeConfigFactory.MC_URL, file.getContents());
			}
		} else if (input instanceof InputStream) {
			env.put(MetamergeConfigFactory.MC_URL, input);
		}
		
		if(!defaultFolders)
			env.put(MetamergeConfigFactory.MC_NO_DEFAULT_FOLDERS, "");

		//
		// Initialize the configuration
		//
		initializeConfig();

		//
		// Refresh external references
		// Have to do this later, or we may get into a loop of refreshing files
		// updateExtComponents();

		if (getRootElement().getAttribute(CONFIG_ATTRIBUTE_UUID) == null || getRootElement().getAttribute(CONFIG_ATTRIBUTE_UUID).length() == 0) {
			getRootElement().setAttribute(CONFIG_ATTRIBUTE_UUID, "" + UUID.randomUUID().toString());  //$NON-NLS-1$
		}

		if((file != null) &&
				(getRootElement().getAttribute(CONFIG_ATTRIBUTE_PROJECT) == null || getRootElement().getAttribute(CONFIG_ATTRIBUTE_PROJECT).length() == 0)) {
			getRootElement().setAttribute(CONFIG_ATTRIBUTE_PROJECT, file.getProject().getName());
		}
		
		getDefaultConfigObject(false);
		initComplete = true;
	}
	
	public void closeConfig() {
		if(file != null)
			MetamergeConfigFactory.unregisterNamespace(file2ns(file));
	}

	public boolean setDefaultName(String name) throws Exception {
		if (configObject == null)
			return false;
		if (configObject.getShortName().equals(name))
			return false;

		setDefaultConfigObject(name, configObject);
		return true;
	}

	/**
	 * Returns the main object of this configuration file (call getDefaultConfigObject(false))
	 * 
	 * @return the main object of this configuration file
	 * @throws Exception
	 */
	public BaseConfiguration getDefaultConfigObject() throws Exception {
		return getDefaultConfigObject(false);
	}

	/**
	 * This method returns the main object of this configuration file.
	 * 
	 * @param standardName If true changes the name of the default object to include standard folder for the type
	 * @return  the main object of this configuration file.
	 * @throws Exception
	 */
	public BaseConfiguration getDefaultConfigObject(boolean standardName) throws Exception {
		if (configError != null)
			throw configError;

		if (configObject == null) {
			Element e = findDefaultObject();
			if (e != null)
				createDefaultObjectInstance(e);
		}

		if (configObject != null && standardName) {
			BaseConfiguration obj = (BaseConfiguration) configObject.getClone();
			if (configObject instanceof FunctionConfig)
				obj.setName(MetamergeConfig.DEFAULT_FUNCTION_FOLDER + "/" + obj.getShortName()); //$NON-NLS-1$
			else if (configObject instanceof ConnectorConfig)
				obj.setName(MetamergeConfig.DEFAULT_CONNECTOR_FOLDER + "/" + obj.getShortName()); //$NON-NLS-1$
			else if (configObject instanceof ParserConfig)
				obj.setName(MetamergeConfig.DEFAULT_PARSER_FOLDER + "/" + obj.getShortName()); //$NON-NLS-1$
			else if (configObject instanceof ScriptConfig)
				obj.setName(MetamergeConfig.DEFAULT_SCRIPT_FOLDER + "/" + obj.getShortName()); //$NON-NLS-1$
			else if (configObject instanceof ALMappingConfig)
				obj.setName(MetamergeConfig.DEFAULT_ATTRIBUTEMAP_FOLDER + "/" + obj.getShortName()); //$NON-NLS-1$
			else if (configObject instanceof AssemblyLineConfig)
				obj.setName(MetamergeConfig.DEFAULT_ASSEMBLYLINE_FOLDER + "/" + obj.getShortName()); //$NON-NLS-1$
			else if (configObject instanceof SchedulerConfig)
				obj.setName(MetamergeConfig.DEFAULT_SCHEDULER_FOLDER + "/" + obj.getShortName());
			else if (configObject instanceof SequenceConfig)
				obj.setName(MetamergeConfig.DEFAULT_SEQUENCE_FOLDER + "/" + obj.getShortName());
			
			obj.setMetamergeConfig(this);
			return obj;
		}

		return configObject;
	}

	/**
	 * This method locates the main object in the XML tree. If the root element has more than one
	 * element an exception is thrown. This does not include the presence of the references container.
	 *
	 * @return The XML Element of the main object
	 * @throws Exception
	 */
	public Element findDefaultObject() throws Exception {
		Document d = getDocument();
		Element defaultNode = null;

		NodeList list = d.getDocumentElement().getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			Node n = list.item(i);
			if (ContainerFactory.CONTAINER_TAG.equals(n.getNodeName()) && ((Element)n).getAttribute(REF_FILE).equals(REFERENCES)) {
				continue;
			} else if (!n.getNodeName().startsWith("#")) { //$NON-NLS-1$
				if (defaultNode != null) {
					throw new Exception(sResHash.getString("MMCONFIG.METAMCONFIGFS.MULTIPLE.NODES", //$NON-NLS-1$
							new Object[] { defaultNode.getNodeName(), n.getNodeName() }));
				} else if (n instanceof Element) {
					defaultNode = (Element) n;
				}
			}
		}
		
		//
		// Update name attribute to match filename (in case it was renamed)
		//
		if(defaultNode != null && file != null) {
			String name = file.getName();
			if(file.getFileExtension() != null)
				name = name.substring(0, name.lastIndexOf("."));
			if(!defaultNode.getAttribute("name").equals(name)) //$NON-NLS-1$
				defaultNode.setAttribute("name", name); //$NON-NLS-1$
		}

		return defaultNode;
	}
	
	/**
	 * This method creates the main object instance from the provided XML element
	 * @param defaultNode The element of the object to be instantiated
	 * 
	 * @throws Exception
	 */
	public void createDefaultObjectInstance(Element defaultNode) throws Exception {

		boolean save =isModTSEnabled();
		setModTSEnabled(false);
		
		// Call factories to produce a java object
		configObject = Factories.getImpl(defaultNode.getNodeName());
		configObject.setMetamergeConfig(this);
		Factories.getFactory(defaultNode.getNodeName()).parse(configObject, defaultNode);

		// Initialize object		
		configObject.init();
		configObject.setupInheritanceChain();
		configObject.setModified(false);
		setModTSEnabled(save);
	}

	/**
	 * This method returns the XML data for this configuration file
	 * 
	 * @return The XML data as a String
	 */
	public String toXML() {
		// Constructing this data should not cause the modifications flags to change.
		// Don't bother fixing everything, just the top nodes
		boolean confWasMod = (configObject == null ? false : configObject.getModified());
		boolean iWasMod = getModified();
		
		try {
			ByteArrayOutputStream bos = commitVersion(false);
			//Try to use the same encoding that was used when writing the ByteArrayOutputStream
			String encoding = System.getProperty(com.ibm.di.server.RS.PROP_CONFIG_ENCODING);
			if (encoding == null || encoding.length() == 0)
				encoding = "UTF-8"; //$NON-NLS-1$
			return new String(bos.toByteArray(), encoding);
		} catch (Exception err) {
			return err.toString();
		} finally {
			//Reset the modified flags
			if (configObject != null)
				configObject.setModified(confWasMod);
			setModified(iWasMod);
		}
	}

	/* (non-Javadoc)
	 * @see com.ibm.di.config.xml.MetamergeConfigXML#newInstanceOf(java.lang.Object)
	 */
	@Override
	public BaseConfiguration newInstanceOf(Object typeName) throws Exception {
		// We have to create a new TDIConfigurationFile object as well
		BaseConfiguration b = super.newInstanceOf(typeName);
		if(b instanceof ConnectorConfig)
			setDefaultConnectorInheritance((ConnectorConfig) b);
		TDIConfigurationFile c = new TDIConfigurationFile();
		c.setDefaultConfigObject(typeName.toString(), b);
		return b;
	}

	/* (non-Javadoc)
	 * @see com.ibm.di.config.xml.MetamergeConfigXML#commitChanges(java.lang.Object, boolean)
	 */
	public synchronized void commitChanges(Object output, boolean isSave) throws Exception {
		if (configObject != null)
			setDefaultConfigObject(configObject.getShortName(), configObject);
		commitVersion(true);
		if (isSave && configObject != null)
			configObject.setModified(false);
	}

	/* (non-Javadoc)
	 * @see com.ibm.di.config.xml.MetamergeConfigXML#getModified()
	 */
	public boolean getModified() {
		if (configObject != null)
			return configObject.getModified();
		else
			return false;
	}

	public void setDefaultConfigObject(String name, BaseConfiguration obj) throws Exception {
		if (!initComplete) {
			return;
		}

		boolean save =isModTSEnabled();
		setModTSEnabled(false);
		
		if(obj != refs) {
			if (configObject != null)
				super.removeElement(configObject.getShortName());
	
			configObject = obj;
			configObject.setName(name);
		}

		super.rebind(name, obj);
		setModTSEnabled(save);
		
		//Trim away some excess blank lines that may occur due to the configObject being removed and added back in
		Node n = getRootElement().getFirstChild();
		while (n instanceof CharacterData && ((CharacterData)n).getData().trim().length() == 0) {
			getRootElement().removeChild(n);
			n = getRootElement().getFirstChild();
		}
	}

	/* (non-Javadoc)
	 * @see com.ibm.di.config.xml.MetamergeConfigXML#internalLookup(java.lang.Object)
	 */
	protected Object internalLookup(Object namex) throws Exception {
		if ("Default".equals(namex) && configObject != null) //$NON-NLS-1$
			return configObject;
		
		if(MetamergeConfig.DEFAULT_PROPSTORE_FOLDER.equals(namex)) {
			return getPropertyManager();
		}
		
		try {
			return super.internalLookup(namex);
		} catch (NameNotFoundException nfe) {}
		
		if(REFERENCES.equals(namex))
			throw new NameNotFoundException(REFERENCES);
		else
			return externalLookup(namex);
	}

	/**
	 * This method creates a default set of dummy property stores. It is needed to avoid
	 * runtime errors when dealing with property expansion in the CE.
	 * 
	 * @return The property manager with the dummy stores.
	 * @throws Exception
	 */
	public PropertyManager getPropertyManager() throws Exception {
		if(pm == null) {
			String[] stores = new String[]{"system", "java", "solution"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			pm = new PropertyManagerImpl();
			pm.init();
			for(String str : stores) {
				pm.addStdStore(str);
				pm.getPropertyStore(str).getConnectionConfig().setParameter("collection", file.getProject().getFile("Resources/" + str + ".properties").getLocation().toPortableString()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				pm.getPropertyStore(str).getConnectionConfig().setParameter("collectionType", "file"); //$NON-NLS-1$ //$NON-NLS-2$
				pm.getPropertyStore(str).getConnectionConfig().setParameter("createCollection", "true"); //$NON-NLS-1$ //$NON-NLS-2$
				pm.getPropertyStore(str).setInitialLoad(true);
				pm.getPropertyStore(str).setCacheTimeout(1000);
			}
		}
		return pm;
	}

	/**
	 * This method searches this config's external references
	 *  
	 * @param name Name of the config object to search for
	 * @return The config object with the given name
	 * @throws Exception if nothing found
	 */
	public Object externalLookup(Object name) throws Exception {
		
		// Try project's runtime config
		try {
			MetamergeConfig ns = Utils.getProjectMC(getProject());
			if(ns != null)
				return ns.lookup(name);
		} catch (NameNotFoundException e) {}

		// At this point the config is missing so we get it 
		ContainerConfig cc = getReference(name.toString());
		if(cc != null && cc.size() > LOCAL_COPY)
			return cc.getConfig(LOCAL_COPY);

		throw new NameNotFoundException(name.toString());
	}

	/**
	 * Returns the file extension for a given config or null if not known
	 *  
	 * @param config
	 * @return the file extension
	 */
	public static String getExtensionFor(BaseConfiguration config) {
		if (config instanceof AssemblyLineConfig)
			return XT_ASSEMBLYLINE;
		else if (config instanceof ALMappingConfig)
			return XT_ATTRMAP;
		else if (config instanceof FunctionConfig)
			return XT_FUNCTION;
		else if (config instanceof ConnectorConfig)
			return XT_CONNECTOR;
		else if (config instanceof ParserConfig)
			return XT_PARSER;
		else if (config instanceof ScriptConfig)
			return XT_SCRIPT;
		else if (config instanceof PropertyStoreConfig)
			return XT_PROPSTORE;
		else if (config instanceof NamespaceConfig)
			return XT_NAMESPACE;
		else if (config instanceof SchedulerConfig)
			return XT_SCHEDULER;
		else if (config instanceof SequenceConfig)
			return XT_SEQUENCE;
		else
			return null;
	}
	
	/**
	 * Returns the file extension for a given config type or null if not known
	 * @see MetamergeConfig
	 * @param type
	 * @return  the file extension for the given config type
	 */
	public static String getExtensionFor(int type) {
		switch (type) {
		case MetamergeConfig.ASSEMBLYLINE_FOLDER:
			return XT_ASSEMBLYLINE;
		case MetamergeConfig.CONNECTOR_FOLDER:
			return XT_CONNECTOR;
		case MetamergeConfig.PARSER_FOLDER:
			return XT_PARSER;
		case MetamergeConfig.SCRIPT_FOLDER:
			return XT_SCRIPT;
		case MetamergeConfig.FUNCTION_FOLDER:
			return XT_FUNCTION;
		case MetamergeConfig.ATTRIBUTEMAP_FOLDER:
			return XT_ATTRMAP;
		case MetamergeConfig.PROPSTORE_FOLDER:
			return XT_PROPSTORE;
		case MetamergeConfig.SCHEDULER_FOLDER:
			return XT_SCHEDULER;
		case MetamergeConfig.SEQUENCE_FOLDER:
			return XT_SEQUENCE;
		}
		return null;
	}
	
	/**
	 * Returns the target folder for a file extension (e.g. ".assemblyline" -> "AssemblyLines" folder)
	 * @param extension
	 * @return the target folder for the file extension
	 */
	public static String getFolderForExtension(String extension) {
		if ( extension == null)
			return null;
		if (extension.startsWith(".")) //$NON-NLS-1$
				extension = extension.substring(1);
		
		for(int i = 0; i < FILE_EXTENSIONS.length; i++) {
			if(FILE_EXTENSIONS[i].equalsIgnoreCase(extension))
				return FILE_EXTENSION_FOLDERS[i];
		}
		return null;
	}

	/**
	 * This method returns the associated TDIProperties object
	 */
	public TDIProperties getTDIProperties() throws Exception {
		// Manually create the stores without using the standard names. Standard names
		// cause the PropertiesConnector to act on the local environment (system store etc).
		if(tdiProperties == null) {
			tdiProperties = new TDIPropertiesCE(this);
		}
		return tdiProperties;
	}

	/**
	 * Override so we don't make any ext props conversion here
	 * 
	 * @throws Exception
	 */
	protected void convertExternalProperties() throws Exception {
	}

	/**
	 * Save XML tree to output stream.
	 */
	public synchronized void commitVersion() throws Exception {
		commitVersion(true);
	}

	public synchronized ByteArrayOutputStream commitVersion(boolean tofile) throws Exception {
		logmsg(sResHash.getString("MMCONFIG.METAMCONFIGFS.METAMERGECONFIGFS.COMMIT.CHANGES")); //$NON-NLS-1$

		// Update underlying document with entries from dirty cache
		debug(sResHash.getString("MMCONFIG.METAMCONFIGFS.COMMIT.CHANGES")); //$NON-NLS-1$
		for (Object name:getCache().getDirtyList()) {
			debug(sResHash.getString("MMCONFIG.METAMCONFIGFS.COMMIT.CHANGES.NEXT.DIRTY.NAME", name)); //$NON-NLS-1$
			
			super.rebind(name, getCache().getObject(name));
		}

		// Make sure we have the correct version
		getRootElement().setAttribute(METAMERGE_VERSION_TAG, METAMERGE_VERSION_ID);

		debug(sResHash.getString("MMCONFIG.METAMCONFIGFS.COMMIT.CHANGES.UPDATE.MODIFIED.FLAGS")); //$NON-NLS-1$
		// Update last modified tag
		getRootElement().setAttribute(METAMERGE_MODIFIED_TAG, (new java.util.Date()).toString());
		getRootElement().setAttribute(METAMERGE_MODIFIEDBY_TAG, System.getProperty("user.name")); //$NON-NLS-1$
		
		// Update delta version counter (increments by 1 for each save)
		incrementCycleNumber();
		
		// Tag the main object
		if (configObject != null)
				getRootElement().setAttribute("main", configObject.getShortName()); //$NON-NLS-1$
		
		Transformer t = TransformerFactory.newInstance().newTransformer();
		t.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
        t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

		String encoding = System.getProperty(com.ibm.di.server.RS.PROP_CONFIG_ENCODING);
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		StreamResult streamResult = new StreamResult(outStream);

		if (encoding != null && encoding.length() > 0) {
			t.setOutputProperty(OutputKeys.ENCODING, encoding);
		}

		t.transform(new DOMSource(getRootElement()), streamResult);
		outStream.flush();

		if (tofile) {
			if(file.exists())
				file.setContents(new ByteArrayInputStream(outStream.toByteArray()), 0, null);
			else
				file.create(new ByteArrayInputStream(outStream.toByteArray()), false, null);
		}

		return outStream;
	}

	public String getUUID() {
		return getRootElement().getAttribute(CONFIG_ATTRIBUTE_UUID);
	}

	public IFile getFile() {
		return file;
	}

	public static BaseConfiguration load(IFile file) throws Exception {
		TDIConfigurationFile c = new TDIConfigurationFile(file);
		return c.getDefaultConfigObject();
	}

	public static BaseConfiguration createInheritedComponent(BaseConfiguration configuration, IFile file) throws Exception {
		BaseConfiguration c = load(file);
		TDIConfigurationFile mc = (TDIConfigurationFile) configuration.getMetamergeConfig();
		BaseConfiguration inherited = null;
		if (c instanceof FunctionConfig) {
			inherited = createInheritedFunction(mc, file);
		} else if (c instanceof ConnectorConfig) {
			inherited = createInheritedConnector(mc, file); //(ConnectorConfig) c);
		} else if (c instanceof ParserConfig) {
			inherited = createInheritedParser((ParserConfig) c);
		} else if (c instanceof ALMappingConfig) {
			inherited = createInheritedALMap((ALMappingConfig) c);
		}

		return inherited;
	}

	public static ConnectorConfig createInheritedConnector(TDIConfigurationFile mc, IFile file) throws Exception {
		ConnectorConfig nc = (ConnectorConfig) mc.newInstanceOf(MetamergeConfig.DEFAULT_CONNECTOR_FOLDER);
		String name = file.getName();
		if(name.indexOf(".") != -1) //$NON-NLS-1$
			name = name.substring(0, name.lastIndexOf(".")); //$NON-NLS-1$
		nc.setName(name);
		nc.init();
		
		// Update references section
		mc.updateReference(nc, file);
		
		return setDefaultConnectorInheritance(nc);
	}

	public static FunctionConfig createInheritedFunction(TDIConfigurationFile mc, IFile file) throws Exception {
		FunctionConfig nc = (FunctionConfig) mc.newInstanceOf(MetamergeConfig.DEFAULT_FUNCTION_FOLDER);
		String name = file.getName();
		if(name.indexOf(".") != -1) //$NON-NLS-1$
			name = name.substring(0, name.lastIndexOf(".")); //$NON-NLS-1$
		nc.setName(name);
		nc.init();
		
	
		// Update references section
		mc.updateReference(nc, file);
		
		return (FunctionConfig) setDefaultConnectorInheritance(nc);
	}
	
	public static FunctionConfig createInheritedFunction(FunctionConfig config) throws Exception {
		return createInheritedFunction(config.getMetamergeConfig(), config);
	}

	public static FunctionConfig createInheritedFunction(MetamergeConfig referent, FunctionConfig config) throws Exception {
		FunctionConfig nc = (FunctionConfig) referent.newInstanceOf(MetamergeConfig.DEFAULT_FUNCTION_FOLDER);
		nc.setName(config.getShortName());
		nc.init();

		String namespace = (String) MetamergeConfigFactory.getLocalNamespaceFor(referent, config);
		if (namespace != null)
			nc.setInheritsFromRef(namespace + ":/" + config.getName().toString()); //$NON-NLS-1$
		else
			nc.setInheritsFromRef("/" + config.getName().toString()); //$NON-NLS-1$

		nc.getFunctionConfig().setInheritsFromRef(BaseConfiguration.INHERIT_PARENT);
		nc.getAttributeMap(true).setInheritsFromRef(BaseConfiguration.INHERIT_PARENT);
		nc.getAttributeMap(false).setInheritsFromRef(BaseConfiguration.INHERIT_PARENT);
		nc.getHooks().setInheritsFromRef(BaseConfiguration.INHERIT_PARENT);

		nc.setupInheritanceChain();
		return nc;
	}

	/**
	 * Refreshes the contents of the referenced objects
	 * 
	 * @throws Exception
	 */
	private void updateReference(BaseConfiguration config, IFile ref) throws Exception {
		String inheritRef = addReference(ref, null);
		config.setInheritsFromRef(inheritRef);
	}

	public static ConnectorConfig setDefaultConnectorInheritance(ConnectorConfig nc) throws Exception {
		
		// Shared by connector and function
		nc.getSchema(ConnectorConfig.SCHEMA_INPUT).setInheritsFromRef(BaseConfiguration.INHERIT_PARENT);
		nc.getSchema(ConnectorConfig.SCHEMA_OUTPUT).setInheritsFromRef(BaseConfiguration.INHERIT_PARENT);
		nc.getAttributeMap(true).setInheritsFromRef(BaseConfiguration.INHERIT_PARENT);
		nc.getAttributeMap(false).setInheritsFromRef(BaseConfiguration.INHERIT_PARENT);
		nc.getHooks().setInheritsFromRef(BaseConfiguration.INHERIT_PARENT);

		if(nc instanceof FunctionConfig) {
			((FunctionConfig)nc).getFunctionConfig().setInheritsFromRef(BaseConfiguration.INHERIT_PARENT);
		} else {
			nc.getConnectionConfig().setInheritsFromRef(BaseConfiguration.INHERIT_PARENT);
			nc.getParserConfig().setInheritsFromRef(BaseConfiguration.INHERIT_PARENT);
			nc.getLinkCriteria().setInheritsFromRef(BaseConfiguration.INHERIT_PARENT);
			if (nc.getParameter("autoreconnect") == null) //$NON-NLS-1$
				nc.setParameter("autoreconnect", "true"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		nc.setupInheritanceChain();

		return nc;
	}

	public static ALMappingConfig createInheritedALMap(ALMappingConfig config) throws Exception {
		return createInheritedALMap(config.getMetamergeConfig(), config);
	}

	public static ALMappingConfig createInheritedALMap(MetamergeConfig referent, ALMappingConfig config) throws Exception {
		ALMappingConfig nc = (ALMappingConfig) referent.newInstanceOf(MetamergeConfig.DEFAULT_ATTRIBUTEMAP_FOLDER);
		nc.setName(config.getShortName());
		nc.init();

		String namespace = (String) MetamergeConfigFactory.getLocalNamespaceFor(referent, config);
		if (namespace != null)
			nc.setInheritsFromRef(namespace + ":/" + config.getName().toString()); //$NON-NLS-1$
		else
			nc.setInheritsFromRef("/" + config.getName().toString()); //$NON-NLS-1$

		nc.getAttributeMap().setInheritsFromRef(BaseConfiguration.INHERIT_PARENT);

		nc.setupInheritanceChain();
		return nc;
	}

	public static ParserConfig createInheritedParser(ParserConfig config) throws Exception {
		return createInheritedParser(config.getMetamergeConfig(), config);
	}

	public static ParserConfig createInheritedParser(MetamergeConfig referent, ParserConfig config) throws Exception {
		ParserConfig nc = (ParserConfig) referent.newInstanceOf(MetamergeConfig.DEFAULT_PARSER_FOLDER);
		nc.setName(config.getShortName());
		nc.init();

		String namespace = (String) MetamergeConfigFactory.getLocalNamespaceFor(referent, config);
		if (namespace != null)
			nc.setInheritsFromRef(namespace + ":/" + config.getName().toString()); //$NON-NLS-1$
		else
			nc.setInheritsFromRef("/" + config.getName().toString()); //$NON-NLS-1$

		nc.setupInheritanceChain();
		return nc;
	}

	/**
	 * Returns the IProject object to which this configuration belongs.
	 * 
	 * @return the IProject to which this configuration belongs.
	 */
	public IProject getProject() {
		if(getFile() != null)
			return getFile().getProject();
		else
			return null;
	}

	/**
	 * Returns the internal references container used to store external references.
	 * 
	 * @return the internal references container.
	 * @throws Exception
	 */
	public ContainerConfig getReferences() throws Exception {
		if(refs != null)
			return refs;
		
		refs = null;
		try {
			refs = (ContainerConfig) lookup(REFERENCES);
		} catch(NameNotFoundException nfe) {}
		if(refs == null) {
			refs = new ContainerConfigImpl();
			refs.setName(REFERENCES);
			refs.init();
			super.bind(refs.getName(), refs);
		}
		return refs;
	}
	
	/**
	 * Check if this configuration has a reference to the IFile. The resource must be located
	 * in a Resources directory.
	 * 
	 * @param res
	 * @return true if this configuration has a reference to the IFile.
	 */
	public boolean hasReferenceTo(IFile res) throws Exception {
		return getReferenceByFile(res) != null;
	}
	
	/**
	 * Adds an external reference to this configuration. If the reference already exists
	 * the current internal name is returned.
	 * 
	 * @param file The file being added
	 * @param internal The suggested internal reference (e.g. /Connectors/abc). If null, a random internal name will be generated
	 * @return The internal name (can be different if already present)
	 * @throws Exception
	 */
	public String addReference(IFile file, String internal) throws Exception {

		BaseConfiguration current = getReferenceByFile(file);
		if(current != null)
			return current.getStringParameter(REF_INTERNAL);
		
		if(file.getProject() != getProject())
			throw new NameAlreadyBoundException(internal);
		
		//
		// Add a reference to the external file
		//
		TDIConfigurationFile cf = TDIConfigurationFile.loadFile(file);

		BaseConfiguration obj = cf.getDefaultConfigObject();
		if ( internal == null ) {
			String folder = getTargetFolderName(obj);
			if (folder == null)
				folder = getFolderForExtension(file.getFileExtension());
			if ( folder != null) {
				try {
					// Do lookup until we get an Exception, then we have a unique name
					String shortName = obj.getShortName();
					internal = "/" + folder + "/" + shortName; //$NON-NLS-1$ //$NON-NLS-2$
					internalLookup(internal);
					
					return internal;
//					
//					if (file.getProject() != getProject()) {
//						internal = "/" + folder + "/" + file.getProject().getName() + "_" + shortName; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
//						internalLookup(internal);
//					}
//					for (int i=1; i< 1000; i++) {
//						internal = internal + "_" + i; //$NON-NLS-1$
//						internalLookup(internal);
//					}
				} catch (NameNotFoundException nnfe) {
					// An Exception means we have a good name
				}
			} else {
//				internal =  "/" + file.getProject().getName() + "/" + file.getProjectRelativePath().toPortableString(); //$NON-NLS-1$ //$NON-NLS-2$
				internal = file.getParent().getName() + "/" + file.getName().substring(0, file.getName().lastIndexOf("."));
			}
		}
		// 
		// Container with Params and Object
		ContainerConfig cc = new ContainerConfigImpl();
		cc.init();
		refs.addConfig(cc);
		
		BaseConfiguration bc = new BaseConfigurationImpl();
		bc.setName(REF_PARAM_OBJECT_NAME);
		bc.init();
		bc.setParameter(REF_FILE, file.getFullPath().toPortableString());		
		bc.setParameter(REF_CYCLE, cf.getCycleNumber());		
		bc.setParameter(REF_INTERNAL, internal);
		cc.addConfig(bc);
		
		addExtComponent(cc, obj, internal);
		return internal;
	}
	
	/**
	 * Removes a referenced object from the references section
	 * 
	 * @param internal The internal name (e.g. inheritFrom value)
	 * @return The Container with the removed object or null if not found
	 * @throws Exception
	 */
	public ContainerConfig removeReference(String internal) throws Exception {
		ContainerConfig ref = getReference(internal);
		if(ref != null) {
			getReferences().removeConfig(ref);
		}
		return ref;
	}
	
	/**
	 * Returns the container for an external reference
	 * 
	 * @param internal The internal name (e.g. inheritFrom value)
	 * @return ContainerConfig with possibly one child component for the ref'd object or null if not found
	 * @throws Exception 
	 */
	public ContainerConfig getReference(String internal) throws Exception {
		// We want to compare names without leading slash
		String intname = internal;
		if(intname.startsWith("/")) //$NON-NLS-1$
			intname = intname.substring(1);
		
		ContainerConfig refs = getReferences();
		for(int i = 0; i < refs.size(); i++) {
			BaseConfiguration b = refs.getConfig(i);
			if(b instanceof ContainerConfig) {
				ContainerConfig cc = (ContainerConfig) b;
				BaseConfiguration bc = cc.getConfig(PARAM_OBJECT);
				String name = bc.getStringParameter(REF_INTERNAL);
				if(name.startsWith("/")) //$NON-NLS-1$
					name = name.substring(1);
	
				if(name.equals(intname))
					return cc;
			}
		}
		return null;
	}
	
	/**
	 * Returns the BaseConfiguration for an existing reference.
	 * 
	 * @param file The file (IFile or String)
	 * @return the BaseConfiguration for the reference.
	 * @throws Exception
	 */
	public BaseConfiguration getReferenceByFile(Object file) throws Exception {
		ContainerConfig refs = getReferences();
		String path = null;
		if(file instanceof IFile)
			path = ((IFile)file).getFullPath().toPortableString();
		else
			path = file.toString();

		for(int i = 0; i < refs.size(); i++) {
			BaseConfiguration b = refs.getConfig(i);
			if(b instanceof ContainerConfig) {
				ContainerConfig cc = (ContainerConfig) b;
				BaseConfiguration bc = cc.getConfig(PARAM_OBJECT);
				if(path.equals(bc.getStringParameter(REF_FILE))) {
					return bc;
				}
			}
		}
		return null;
	}
	
	private void addExtComponent(ContainerConfig cc, BaseConfiguration config, String internal) throws Exception {
		
		
		//
		// Create a flattened clone of the target configuration and rename it to
		// the internal reference name
		//
//		BaseConfiguration target = (BaseConfiguration) config.getClone();
//		target.flatten(new ArrayList());
//		target.setName(internal);
//		if (cc.size() > LOCAL_COPY)
//			cc.removeConfig(LOCAL_COPY);
//		cc.insertConfig(target, LOCAL_COPY);
	}
	
	final static Class<?>[] TARGET_FOLDER_CLASS = {
		AssemblyLineConfig.class,
		ParserConfig.class,
		ScriptConfig.class,
		FunctionConfig.class,
		ALMappingConfig.class,
		ConnectorConfig.class,
		NamespaceConfig.class,
		SchedulerConfig.class,
		SequenceConfig.class,
	};
	
	final static String[] TARGET_FOLDER_NAMES = {
		MetamergeConfig.DEFAULT_ASSEMBLYLINE_FOLDER,
		MetamergeConfig.DEFAULT_PARSER_FOLDER,
		MetamergeConfig.DEFAULT_SCRIPT_FOLDER,
		MetamergeConfig.DEFAULT_FUNCTION_FOLDER,
		MetamergeConfig.DEFAULT_ATTRIBUTEMAP_FOLDER,
		MetamergeConfig.DEFAULT_CONNECTOR_FOLDER,
		MetamergeConfig.DEFAULT_NAMESPACE_FOLDER,
		MetamergeConfig.DEFAULT_SCHEDULER_FOLDER,
		MetamergeConfig.DEFAULT_SEQUENCE_FOLDER,
	};

	public String getTargetFolderName(BaseConfiguration obj) {
		for(int i = 0; i < TARGET_FOLDER_CLASS.length; i++) {
			if(TARGET_FOLDER_CLASS[i].isAssignableFrom(obj.getClass()))
				return TARGET_FOLDER_NAMES[i];
		}
		return null;
	}

	public String getCycleNumber() {
		return getRootElement().getAttribute(CYCLE_TAG);
	}
	private void incrementCycleNumber(){
		String version = getRootElement().getAttribute(CYCLE_TAG);
		if(version == null || version.length() == 0)
			version = "1"; //$NON-NLS-1$
		else
			version = ""+(Integer.valueOf(version).intValue() + 1); //$NON-NLS-1$
		getRootElement().setAttribute(CYCLE_TAG, version);
	}
	public String getCreatedDate() {
		return getRootElement().getAttribute(MetamergeConfigXML.METAMERGE_CREATED_TAG);
	}
	public String getCreatedBy() {
		return getRootElement().getAttribute(MetamergeConfigXML.METAMERGE_CREATEDBY_TAG);
	}
	public String getModifiedDate() {
		return getRootElement().getAttribute(MetamergeConfigXML.METAMERGE_MODIFIED_TAG);
	}
	public String getModifiedBy() {
		return getRootElement().getAttribute(MetamergeConfigXML.METAMERGE_MODIFIEDBY_TAG);
	}

	/**
	 * Changes the file for this configuration. It also renames the default object
	 * if the file name changed.
	 * 
	 * @param file
	 * @throws Exception 
	 */
	public void setFile(IFile file) throws Exception {
		String newname = file.getName();
		if (file.getFileExtension() != null)
			newname = newname.substring(0, newname.lastIndexOf('.'));
		String oldname = configObject.getShortName();
		if(!newname.equals(oldname))
			setDefaultConfigObject(newname, configObject);
		this.file = file;
	}
	
	public String getDirectory() {
		if (file == null)
			return super.getDirectory();
		IPath path = null;
		try {
			path = new ProjectRuntimeDirectory(file.getProject()).getFolder().getLocation();
		} catch (CoreException e) {
			e.printStackTrace();
		}
		if (path != null)
			return path.toOSString();
		else
			return super.getDirectory();
	}
}
