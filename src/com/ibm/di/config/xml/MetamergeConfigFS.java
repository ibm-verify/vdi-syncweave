/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.xml;

import java.io.*;
import java.util.*;

import javax.naming.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import org.w3c.dom.*;

import com.ibm.di.config.base.BaseConfigurationImpl;
import com.ibm.di.config.interfaces.*;
import com.ibm.di.server.ResourceHash;

/**
 * This class is not used.
 * @deprecated
 */
public class MetamergeConfigFS extends MetamergeConfigXML {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final long serialVersionUID = -1613589672517047380L;

	private static TDIProperties dummyProps = new TDIProperties();

	/**
	 * Extension --> type mapping
	 */
	public final static String XT_ASSEMBLYLINE = "tda";

	public final static String XT_CONNECTOR = "tdc";

	public final static String XT_PARSER = "tdp";

	public final static String XT_ATTRMAP = "tdm";

	public final static String XT_FUNCTION = "tdf";

	public final static String XT_SCRIPT = "tds";

	public final static Class<?>[] XT_CLS = { AssemblyLineConfig.class,
			ConnectorConfig.class, ParserConfig.class, ALMappingConfig.class,
			FunctionConfig.class, ScriptConfig.class, };

	public final static String[] XT_EXT = { XT_ASSEMBLYLINE, XT_CONNECTOR,
			XT_PARSER, XT_ATTRMAP, XT_FUNCTION, XT_SCRIPT, };

	public final static String MC_URLBASE = "com.ibm.di.config.urlbase";

	protected BaseConfiguration configObject;

	protected Exception configError;

	private boolean initComplete;

	private final static ResourceHash sResHash = BaseConfigurationImpl
			.getResHash();

	public MetamergeConfigFS() {
		super();
		try {
			initializeConfig();
		} catch (Exception ignore) {
		}
		initComplete = true;
	}

	@SuppressWarnings("unchecked")
	public MetamergeConfigFS(Hashtable env) throws Exception {
		super(env);
		initComplete = true;
	}

	public static MetamergeConfigFS getInstance(String str) throws Exception {
		return getInstance(str, false);
	}

	public static MetamergeConfigFS getInstance(String str, boolean create)
			throws Exception {
		Hashtable<String,Object> env = new Hashtable<String,Object>();
		env.put(MetamergeConfigFactory.MC_URL, str);
		env.put(MetamergeConfigFactory.MC_CREATE, String.valueOf(create));
		return new MetamergeConfigFS(env);
	}

	public BaseConfiguration getDefaultConfigObject() throws Exception {
		return getDefaultConfigObject(false);
	}

	public boolean setDefaultName(String name) throws Exception {
		if (configObject == null)
			return false;
		if (configObject.getShortName().equals(name))
			return false;
		rebind(name, configObject);
		return true;
	}

	public BaseConfiguration getDefaultConfigObject(boolean standardName)
			throws Exception {
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
				obj.setName(MetamergeConfig.DEFAULT_FUNCTION_FOLDER + "/"
						+ obj.getShortName());
			else if (configObject instanceof ConnectorConfig)
				obj.setName(MetamergeConfig.DEFAULT_CONNECTOR_FOLDER + "/"
						+ obj.getShortName());
			else if (configObject instanceof ParserConfig)
				obj.setName(MetamergeConfig.DEFAULT_PARSER_FOLDER + "/"
						+ obj.getShortName());
			else if (configObject instanceof ScriptConfig)
				obj.setName(MetamergeConfig.DEFAULT_SCRIPT_FOLDER + "/"
						+ obj.getShortName());
			else if (configObject instanceof ALMappingConfig)
				obj.setName(MetamergeConfig.DEFAULT_ATTRIBUTEMAP_FOLDER + "/"
						+ obj.getShortName());
			else if (configObject instanceof AssemblyLineConfig)
				obj.setName(MetamergeConfig.DEFAULT_ASSEMBLYLINE_FOLDER + "/"
						+ obj.getShortName());

			obj.setMetamergeConfig(this);
			return obj;
		}

		return configObject;
	}

	/**
	 * This method cleans the XML config by removing all items except the one
	 * configured for this configuration. In particular, this configuration can
	 * hold only one single configuration object.
	 * 
	 * @throws Exception
	 */
	public Element findDefaultObject() throws Exception {
		Document d = getDocument();
		Element defaultNode = null;

		NodeList list = d.getDocumentElement().getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			Node n = list.item(i);
			if (!n.getNodeName().startsWith("#")) {
				if (defaultNode != null) {
					throw new Exception(sResHash.getString(
							"MMCONFIG.METAMCONFIGFS.MULTIPLE.NODES",
							new Object[] { defaultNode.getNodeName(),
									n.getNodeName() }));
				} else if (n instanceof Element)
					defaultNode = (Element) n;
			}
		}

		return defaultNode;
	}

	public void createDefaultObjectInstance(Element defaultNode)
			throws Exception {

		// Call factories to produce a java object
		configObject = Factories.getImpl(defaultNode.getNodeName());
		configObject.setMetamergeConfig(this);
		// configObject.setName(name);
		Factories.getFactory(defaultNode.getNodeName()).parse(configObject,
				defaultNode);

		// Initialize object
		configObject.init();
		configObject.setupInheritanceChain();
		configObject.setModified(false);
		// configObject.setParent(this);
	}

	public synchronized void commitChanges(Object output, boolean isSave)
			throws Exception {
		if (configObject != null)
			rebind(configObject.getShortName(), configObject);
		commitVersion();
		configObject.setModified(false);
	}

	public boolean getModified() {
		if (configObject != null)
			return configObject.getModified();
		else
			return false;
	}

	public void bind(Object name, Object obj) throws Exception {
		if (!initComplete) {
			return;
		}

		if (configObject != null) {
			throw new Exception(sResHash.getString(
					"MMCONFIG.METAMCONFIGFS.ONLY.ONE.CONFIG.ITEM.ALLOWED",
					configObject));
		}

		configObject = (BaseConfiguration) obj;
		super.bind(name, configObject);
	}

	public void rebind(Object name, Object obj) throws Exception {
		if (!initComplete) {
			return;
		}

		if (configObject != null)
			super.removeElement(configObject.getShortName());

		configObject = (BaseConfiguration) obj;
		configObject.setName("" + name);
		super.rebind(name, obj);
	}

	protected Object internalLookup(Object namex) throws Exception {
		try {
			return super.internalLookup(namex);
		} catch (NameNotFoundException nfe) {
			// Try to resolve reference through relative file system path
			nfe.printStackTrace();
			return externalLookup(namex);
		}
	}

	public Object externalLookup(Object name) throws Exception {
		Name n = MetamergeConfigFactory.parseName(name);
		System.out.println(sResHash.getString(
				"MMCONFIG.METAMCONFIGFS.EXTERNAL.LOOKUP.IN", new Object[] {
						getBase(), n }));
		String target = getBase() + "/" + n + ".tdc";
		Hashtable<String, Object> env = new Hashtable<String, Object>();
		env.put(MetamergeConfigFactory.MC_CREATE, "false");
		env.put(MetamergeConfigFactory.MC_URL, target);
		try {
			MetamergeConfigFS fs = new MetamergeConfigFS(env);
			Object obj = fs.lookup(n.getSuffix(1));
			// System.out.println ( "-- found: " + target);
			return obj;
		} catch (FileNotFoundException fnf) {
			throw new NameNotFoundException(target);
		} catch (Exception nnf) {
			// System.out.println ( "-- not found: " + target + ";" + nnf);
			throw nnf;
		}
	}

	public String getBase() {
		Object obj = env.get(MC_URLBASE);
		if (obj instanceof String || obj instanceof java.io.File) {
			File f;
			if (obj instanceof File)
				f = (File) obj;
			else
				f = new File(obj.toString());
			return f.getParentFile().getParent();
		} else {
			return "..";
		}
	}

	@SuppressWarnings("unchecked")
	public void setBase(String base) {
		System.out.println(sResHash.getString("MMCONFIG.METAMCONFIGFS.SETBASE",
				base));
		env.put(MC_URLBASE, base);
		if (getDocument() != null)
			getDocument().getDocumentElement().setAttribute("base", base);
	}

	public String getExtensionFor(BaseConfiguration config) {
		if (config instanceof AssemblyLineConfig)
			return XT_ASSEMBLYLINE;
		else if (config instanceof FunctionConfig)
			return XT_FUNCTION;
		else if (config instanceof ConnectorConfig)
			return XT_CONNECTOR;
		else if (config instanceof ParserConfig)
			return XT_PARSER;
		else if (config instanceof ALMappingConfig)
			return XT_ATTRMAP;
		else if (config instanceof ScriptConfig)
			return XT_SCRIPT;
		else
			return null;
	}

	/**
	 * This method returns the associated TDIProperties object
	 */
	public TDIProperties getTDIProperties() throws Exception {
		return dummyProps;
	}

	protected void convertExternalProperties() throws Exception {
	}

	/**
	 * Save XML tree to output stream.
	 */
	public synchronized void commitVersion() throws Exception {
		logmsg(sResHash
				.getString("MMCONFIG.METAMCONFIGFS.METAMERGECONFIGFS.COMMIT.CHANGES"));

		if (isDebugMode()) {
			debug(sResHash.getString("MMCONFIG.METAMCONFIGFS.COMMIT.CHANGES"));
		}

		// Update underlying document with entries from dirty cache
		for (String name: getCache().getDirtyList()) {
			if (isDebugMode()) {
				debug(sResHash
						.getString(
								"MMCONFIG.METAMCONFIGFS.COMMIT.CHANGES.NEXT.DIRTY.NAME",
								name));
			}

			BaseConfiguration config = getCache().getObject(name);
			rebind(name, config);
		}

		// Make sure we have the correct version
		getRootElement().setAttribute(METAMERGE_VERSION_TAG,
				METAMERGE_VERSION_ID);

		if (isDebugMode()) {
			debug(sResHash
					.getString("MMCONFIG.METAMCONFIGFS.COMMIT.CHANGES.UPDATE.MODIFIED.FLAGS"));
		}

		// Update last modified tag
		getRootElement().setAttribute(METAMERGE_MODIFIED_TAG,
				(new java.util.Date()).toString());
		getRootElement().setAttribute(METAMERGE_MODIFIEDBY_TAG,
				System.getProperty("user.name"));

		Transformer t = TransformerFactory.newInstance().newTransformer();
		t.setOutputProperty(OutputKeys.INDENT, "yes");

		String encoding = System
				.getProperty(com.ibm.di.server.RS.PROP_CONFIG_ENCODING);
		OutputStream outStream = getOutputStream(null);
		StreamResult streamResult = new StreamResult(outStream);

		if (encoding != null && encoding.length() > 0) {
			t.setOutputProperty(OutputKeys.ENCODING, encoding);
		}

		t.transform(new DOMSource(getRootElement()), streamResult);
		outStream.close();
	}

}
