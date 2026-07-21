/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Vector;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;

import com.ibm.di.config.interfaces.ALMappingConfig;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.ExternalPropertiesConfig;
import com.ibm.di.config.interfaces.ExternalPropertiesDelegator;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.HookConfig;
import com.ibm.di.config.interfaces.HooksConfig;
import com.ibm.di.config.interfaces.LinkCriteriaItem;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.MetamergeFolder;
import com.ibm.di.config.interfaces.NamespaceConfig;
import com.ibm.di.config.interfaces.ParserConfig;
import com.ibm.di.config.interfaces.PropertyManager;
import com.ibm.di.config.interfaces.ScriptConfig;
import com.ibm.di.config.interfaces.SequenceConfig;
import com.ibm.di.config.interfaces.SolutionInterface;
import com.ibm.di.config.interfaces.TDIProperties;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.server.FileConfig;
import com.ibm.di.server.Log;
import com.ibm.di.server.ResourceHash;

/**
 * Implements {@link MetamergeConfig}. This implementation stores the
 * configuration objects in a file using a private format.
 * 
 * @see com.ibm.di.config.interfaces.MetamergeConfig
 * 
 */
public class MetamergeConfigImpl extends BaseConfigurationImpl implements MetamergeConfig {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	static final long serialVersionUID = -3363695330685967904L;

	//
	// Logger
	//
	public final static Log logger = new Log("mmconfig", "com.ibm.di.config.interfaces.MetamergeConfigCFG");

	protected Hashtable env;

	protected Hashtable<String, String> classMap;

	protected ExternalPropertiesDelegator externalPropertiesDelegator;

	// protected BaseConfiguration externalProperties;

	public final static String[] DEFAULT_FOLDER_NAMES = { null, MetamergeConfig.DEFAULT_ASSEMBLYLINE_FOLDER,
			MetamergeConfig.DEFAULT_CONNECTOR_FOLDER, MetamergeConfig.DEFAULT_PARSER_FOLDER, MetamergeConfig.DEFAULT_SCRIPT_FOLDER,
			MetamergeConfig.DEFAULT_LIBRARY_FOLDER, MetamergeConfig.DEFAULT_PROPERTY_FOLDER,
			MetamergeConfig.DEFAULT_NAMESPACE_FOLDER,
			null,
			null, // MetamergeConfig.DEFAULT_EXTPROP_FOLDER,
			MetamergeConfig.DEFAULT_SERVER_FOLDER, MetamergeConfig.DEFAULT_FUNCTION_FOLDER,
			MetamergeConfig.DEFAULT_ATTRIBUTEMAP_FOLDER, MetamergeConfig.DEFAULT_PROPSTORE_FOLDER,
			MetamergeConfig.DEFAULT_LOGGER_FOLDER, MetamergeConfig.DEFAULT_SCHEDULER_FOLDER,
			MetamergeConfig.DEFAULT_SEQUENCE_FOLDER};

	private transient FileConfig file;

	private ConfigCache cache;

	private static String[] OLD_NAMES = { "task", "connectors", "parsertypes", "scripts", "include", "form", "window" };

	private static String[] NEW_NAMES = { MetamergeConfig.DEFAULT_ASSEMBLYLINE_FOLDER, MetamergeConfig.DEFAULT_CONNECTOR_FOLDER,
			MetamergeConfig.DEFAULT_PARSER_FOLDER, MetamergeConfig.DEFAULT_SCRIPT_FOLDER, MetamergeConfig.DEFAULT_NAMESPACE_FOLDER,
			MetamergeConfig.DEFAULT_FORM_FOLDER, null };

	private static String[] SETTING_NAMES = { "get_history", "set_history", "verbose", "maxread", "maxerr", "findreturncount",
			"ScriptEngine", "includeGlobalPrologs", "includePrologs", "automapattributes", "debug", "nullBehavior",
			"nullBehaviorValue" };

	private static long linkDate = new Date().getTime();

	public final static String DEFAULT_FOLDER_IMPL = "com.ibm.di.config.base.MetamergeFolderImpl";

	private Hashtable<String,String> tableName = new Hashtable<String,String>();

	private Hashtable urlToNameSpace = new Hashtable();

	private final static ResourceHash sResHash = BaseConfigurationImpl.getResHash();

	private transient boolean modTSEnabled = true;
	
	private transient Boolean encryptProtected;
	
	public MetamergeConfigImpl() {
		super();
		env = new Hashtable();
	}

	public MetamergeConfigImpl(Hashtable<String, Object> env) throws Exception {

		super();

		if (env != null)
			this.env = (Hashtable<String, Object>) env.clone();
		else
			this.env = new Hashtable<String, Object>();

		// Verify that password is not given when we run in secure mode
		if (useEncryption() && hasPassword()) {
			throw new Exception(sResHash.getString("MMCONFIG.METAMCONFIMPL.LOADING.CONFIGURATION.IN.SERVER"));
		}

		initializeConfig();
	}

	public boolean useEncryption() {

		String encrypt = (String) env.get(MetamergeConfigFactory.MC_ENCRYPT);

		if ("true".equalsIgnoreCase(encrypt)) {
			return true;
		}
		if ("false".equalsIgnoreCase(encrypt)) {
			return false;
		}
		return com.ibm.di.server.RS.isSecured();
	}

	public boolean hasPassword() {
		String str = getPassword();
		return (str != null && (str.length() > 0));
	}

	protected String getPassword() {
		return (String) env.get(javax.naming.Context.SECURITY_CREDENTIALS);
	}

	public void initializeConfig() throws Exception {

		boolean create = false;

		cache = new ConfigCache();

		// Initialize class map
		initializeClassMap();

		// Get Provider URL
		Object path = env.get(Context.PROVIDER_URL);

		if (env.get(MetamergeConfigFactory.MC_CREATE) != null && env.get(MetamergeConfigFactory.MC_CREATE).equals("true"))
			create = true;

		file = new FileConfig(null);

		// Password
		if (env.get(Context.SECURITY_CREDENTIALS) != null)
			file.setPassword((String) env.get(Context.SECURITY_CREDENTIALS));

		// Load
		if (path instanceof String) {
			File f = new File(path.toString());
			path = f.getCanonicalPath();
			file.setConfigPath((String) path);
			// If the file does not exist, we will create a new one later
			if (f.exists() || !create) {
				file.loadConfig((String) path);
			}
			setName(MetamergeConfigFactory.parseName(path));
			env.put(Context.PROVIDER_URL, path);
		} else if (path instanceof BufferedReader) {
			file.loadConfig((BufferedReader) path);
		} else if (path instanceof Reader) {
			file.loadConfig(new BufferedReader((Reader) path));
		} else if (path instanceof InputStream) {
			file.loadConfig(new BufferedReader(new InputStreamReader((InputStream) path, "UTF-8")));
		} else {
			file.loadConfig((byte[]) path);
			setName(MetamergeConfigFactory.parseName("InMemory"));
		}

		// Get FileConfig tables
		setData(file.getTables());

		if (file.getVersion() == null) {
			if (logger.isDebugEnabled()) {
				logmsg("MMCONFIG.METAMCONFIMPL.CONVERT.OLD.STYLE.CFG.FILE");
			}

			// Use new names
			convertNames();

			// Remove old templates
			if (path instanceof String && !((String) path).endsWith(".inf"))
				removeOldTemplates();

			// Remove included components
			file.removeIncludedComponents();

			// Update version tag
			file.setVersion("VERSION: 1.0");
		} else {
			if (logger.isDebugEnabled()) {
				logmsg("MMCONFIG.METAMCONFIMPL.USING.NEW.STYLE.CFG.FILE");
			}
			TreeMap tm = (TreeMap) getData().get("Java");
			if (tm != null) {
				if (tm.get(MetamergeConfig.DEFAULT_PROPERTY_FOLDER) != null)
					getData().put(MetamergeConfig.DEFAULT_PROPERTY_FOLDER, tm.get(MetamergeConfig.DEFAULT_PROPERTY_FOLDER));
				if (tm.get(MetamergeConfig.DEFAULT_LIBRARY_FOLDER) != null)
					getData().put(MetamergeConfig.DEFAULT_LIBRARY_FOLDER, tm.get(MetamergeConfig.DEFAULT_LIBRARY_FOLDER));
				getData().remove("Java");
			}
		}

		//
		// addDefaultFolders();

		// Yes, it has been modified but not by the user
		setModified(false);

	}

	public void addDefaultFolders() throws Exception {
		for (int i = 0; i < DEFAULT_FOLDER_NAMES.length; i++) {
			if (DEFAULT_FOLDER_NAMES[i] != null) {
				if (DEFAULT_FOLDER_NAMES[i].equals(MetamergeConfig.DEFAULT_PROPERTY_FOLDER)) {
					PropertyConfigImpl p = new PropertyConfigImpl();
					p.setName(MetamergeConfig.DEFAULT_PROPERTY_FOLDER);
					p.setMetamergeConfig(this);
					try {
						bind(MetamergeConfig.DEFAULT_PROPERTY_FOLDER, p);
					} catch (NameAlreadyBoundException ignore) {
					}
				} else if (DEFAULT_FOLDER_NAMES[i].equals(MetamergeConfig.DEFAULT_LIBRARY_FOLDER)) {
					LibraryConfigImpl l = new LibraryConfigImpl();
					l.setName(MetamergeConfig.DEFAULT_LIBRARY_FOLDER);
					l.setMetamergeConfig(this);
					try {
						bind(MetamergeConfig.DEFAULT_LIBRARY_FOLDER, l);
					} catch (NameAlreadyBoundException ignore) {
					}
				} else if (DEFAULT_FOLDER_NAMES[i].equals(MetamergeConfig.DEFAULT_EXTPROP_FOLDER)) {
					// EXT prop - Folder
					try {
						createFolder(DEFAULT_FOLDER_NAMES[i]);
					} catch (NameAlreadyBoundException ignore) {
					}

					// Default - External properties config
					ExternalPropertiesImpl l = new ExternalPropertiesImpl();
					l.setName(MetamergeConfig.DEFAULT_EXTPROP_FOLDER + "/" + MetamergeConfig.DEFAULT_EXTPROP_NAME);
					l.setMetamergeConfig(this);
					try {
						bind(l.getName(), l);
					} catch (NameAlreadyBoundException ignore) {
					}

				} else if (DEFAULT_FOLDER_NAMES[i].equals(MetamergeConfig.DEFAULT_SERVER_FOLDER)) {
					// Config - Folder
					try {
						createFolder(DEFAULT_FOLDER_NAMES[i]);
					} catch (NameAlreadyBoundException ignore) {
					}

					// IDIServer - logger
					LogConfigImpl l = new LogConfigImpl();
					l.setName(MetamergeConfig.DEFAULT_SERVER_FOLDER + "/" + MetamergeConfig.DEFAULT_SERVER_LOG);
					l.setMetamergeConfig(this);
					try {
						bind(l.getName(), l);
					} catch (NameAlreadyBoundException ignore) {
					}

					// IDIServer - autostart
					InstanceConfigImpl ci = new InstanceConfigImpl();
					ci.init();
					ci.setName(MetamergeConfig.DEFAULT_SERVER_FOLDER + "/" + MetamergeConfig.DEFAULT_SERVER_AUTOSTART);
					ci.setMetamergeConfig(this);
					try {
						bind(ci.getName(), ci);
					} catch (NameAlreadyBoundException ignore) {
					}

					// IDIServer - Tombstones
					TombstonesConfigImpl ts = new TombstonesConfigImpl();
					ts.init();
					ts.setName(MetamergeConfig.DEFAULT_SERVER_FOLDER + "/" + MetamergeConfig.DEFAULT_SERVER_TOMBSTONES);
					ts.setMetamergeConfig(this);
					try {
						bind(ts.getName(), ts);
					} catch (NameAlreadyBoundException ignore) {
					}

					// Solution Interface
					SolutionInterface si = new SolutionInterfaceImpl();
					si.init();
					si.setName(MetamergeConfig.DEFAULT_SERVER_FOLDER + "/" + MetamergeConfig.DEFAULT_SOLUTION_INTERFACE);
					si.setMetamergeConfig(this);
					try {
						bind(si.getName(), si);
					} catch (NameAlreadyBoundException ignore) {
					}

				} else if (DEFAULT_FOLDER_NAMES[i].equals(MetamergeConfig.DEFAULT_PROPSTORE_FOLDER)) {
					// Properties
					PropertyManagerImpl l = new PropertyManagerImpl();
					l.init();
					l.setName(MetamergeConfig.DEFAULT_PROPSTORE_FOLDER);
					l.setMetamergeConfig(this);
					try {
						bind(l.getName(), l);
					} catch (NameAlreadyBoundException ignore) {
					}

					l.addStdStore(PropertyManager.STDCOLL_SOLUTION);
					l.addStdStore(PropertyManager.STDCOLL_GLOBAL);
					l.addStdStore(PropertyManager.STDCOLL_JAVA);
					l.addStdStore(PropertyManager.STDCOLL_SYSTEM);

				} else if (i != MetamergeConfig.LOGGER_FOLDER) {
					// Create it if its not the logger folder we do not want
					// to create this folder by default.
					try {
						createFolder(DEFAULT_FOLDER_NAMES[i]);
					} catch (NameAlreadyBoundException ignore) {
					}
				}
			}
		}
	}

	public void closeConfig() throws Exception {
	}

	protected void initializeClassMap() {
		// Name --> Class mapping
		classMap = new Hashtable<String, String>();
		classMap.put(MetamergeConfig.DEFAULT_CONNECTOR_FOLDER, "com.ibm.di.config.base.ConnectorConfigImpl");
		classMap.put(MetamergeConfig.DEFAULT_ASSEMBLYLINE_FOLDER, "com.ibm.di.config.base.AssemblyLineConfigImpl");
		classMap.put(MetamergeConfig.DEFAULT_PARSER_FOLDER, "com.ibm.di.config.base.ParserConfigImpl");
		classMap.put(MetamergeConfig.DEFAULT_SCRIPT_FOLDER, "com.ibm.di.config.base.ScriptConfigImpl");
		classMap.put(MetamergeConfig.DEFAULT_LIBRARY_FOLDER, "com.ibm.di.config.base.LibraryConfigImpl");
		classMap.put(MetamergeConfig.DEFAULT_PROPERTY_FOLDER, "com.ibm.di.config.base.PropertyConfigImpl");
		classMap.put(MetamergeConfig.DEFAULT_NAMESPACE_FOLDER, "com.ibm.di.config.base.NamespaceConfigImpl");
		classMap.put(MetamergeConfig.DEFAULT_FORM_FOLDER, "com.ibm.di.config.base.FormConfigImpl");
		classMap.put(MetamergeConfig.DEFAULT_EXTPROP_FOLDER, "com.ibm.di.config.base.ExternalPropertiesImpl");
		classMap.put(MetamergeConfig.DEFAULT_SERVER_FOLDER, "com.ibm.di.config.base.LogConfigImpl");
		classMap.put(MetamergeConfig.DEFAULT_FUNCTION_FOLDER, "com.ibm.di.config.base.FunctionConfigImpl");
		classMap.put(MetamergeConfig.DEFAULT_ATTRIBUTEMAP_FOLDER, "com.ibm.di.config.base.ALMappingConfigImpl");
		classMap.put(MetamergeConfig.DEFAULT_PROPSTORE_FOLDER, "com.ibm.di.config.base.PropertyManagerImpl");
		classMap.put(MetamergeConfig.DEFAULT_SOLUTION_INTERFACE, "com.ibm.di.config.base.SolutionInterfaceImpl");
		classMap.put(MetamergeConfig.DEFAULT_LOGGER_FOLDER, "com.ibm.di.config.base.LogConfigItemImpl");
		classMap.put(MetamergeConfig.DEFAULT_SCHEDULER_FOLDER, "com.ibm.di.config.base.SchedulerConfigImpl");
		classMap.put(MetamergeConfig.DEFAULT_SEQUENCE_FOLDER, "com.ibm.di.config.base.SequenceConfigImpl");

		classMap.put("AttributeMap", "com.ibm.di.config.base.AttributeMapConfigImpl");
	}

	private void removeOldTemplates() {
		TreeMap tm = getData();
		if (tm == null)
			return;

		removeOldTemplates(tm.get(MetamergeConfig.DEFAULT_CONNECTOR_FOLDER));
		removeOldTemplates(tm.get(MetamergeConfig.DEFAULT_PARSER_FOLDER));
	}

	private void removeOldTemplates(Object folder) {

		if (!(folder instanceof TreeMap))
			return;

		for (Iterator it = ((TreeMap) folder).keySet().iterator(); it.hasNext();) {
			String s = (String) it.next();
			if (s.startsWith("metamerge.") || s.startsWith("ibmdi."))
				it.remove();
		}
	}

	private void convertNames() {
		TreeMap tm = getData();
		TreeMap tmp;

		if (tm == null) {
			if (logger.isDebugEnabled()) {
				logmsg("MMCONFIG.METAMCONFIMPL.FILECONFIG.DATA.IS.NULL");
			}
			return;
		}

		for (int i = 0; i < OLD_NAMES.length; i++) {
			tmp = (TreeMap) tm.remove(OLD_NAMES[i]);

			if (NEW_NAMES[i] != null && tm.get(NEW_NAMES[i]) == null) {
				if (NEW_NAMES[i].equals(MetamergeConfig.DEFAULT_FORM_FOLDER) && (tmp == null)) {
					if (logger.isDebugEnabled()) {
						logmsg("MMCONFIG.METAMCONFIMPL.IGNORING.CREATE.OF.FORMS.FOLDER");
					}
				} else {
					if (logger.isDebugEnabled()) {
						logger.debug("MMCONFIG.METAMCONFIMPL.MOVE.FOLDER.FROM.TO", OLD_NAMES[i], NEW_NAMES[i]);
					}
					tm.put(NEW_NAMES[i], tmp != null ? tmp : new TreeMap());
				}
			}
		}

		// Function Folder
		if (tm.get(MetamergeConfig.DEFAULT_FUNCTION_FOLDER) == null)
			tm.put(MetamergeConfig.DEFAULT_FUNCTION_FOLDER, new TreeMap());

		// Java libraries
		tmp = (TreeMap) tm.remove("libraries");
		if (tmp != null)
			tmp = (TreeMap) tmp.get("userFunctions");
		if (tmp != null && "com.architech.function.userFunctions2".equals(tmp.get("system")))
			tmp.remove("system");
		if (tmp != null && tmp.size() > 0)
			tm.put(MetamergeConfig.DEFAULT_LIBRARY_FOLDER, tmp);

		TreeMap prop = (TreeMap) tm.remove("properties");
		if (prop != null) {
			// Java properties
			tmp = (TreeMap) prop.get("java");
			if (tmp != null && tmp.size() > 0)
				tm.put(MetamergeConfig.DEFAULT_PROPERTY_FOLDER, tmp);

			// External properties
			TreeMap extp = new TreeMap();
			if (prop.get("user.properties") != null)
				extp.put(InternalSchema.EXTPROP_FILE_PATH, prop.remove("user.properties"));
			if (prop.get("user.properties.encrypted") != null)
				extp.put(InternalSchema.EXTPROP_ENCRYPTED, prop.remove("user.properties.encrypted"));
			if (extp.size() > 0)
				tm.put(MetamergeConfig.DEFAULT_EXTPROP_FOLDER, extp);
		}

		tmp = (TreeMap) tm.get("AssemblyLines");
		if (tmp != null) {
			for (Iterator e = tmp.values().iterator(); e.hasNext();) {
				fixConnectors(((TreeMap) e.next()).get("components"));
			}
		}

		fixIncludes();
		fixConnectors(tm.get("Connectors"));
		buildtableName(tm, "");
		fixInheritance(tm, "");
		fixParsers(tm.get("Parsers"));

		tmp = (TreeMap) tm.get("AssemblyLines");
		if (tmp != null) {
			for (Iterator e = tmp.entrySet().iterator(); e.hasNext();) {
				java.util.Map.Entry mapEntry = (java.util.Map.Entry) e.next();
				String al = (String) mapEntry.getKey();
				fixSettings((TreeMap) mapEntry.getValue());
				try {
					AssemblyLineConfig alc = getAssemblyLine(al);
					String lang = alc.getSettings().getStringParameter(InternalSchema.SCRIPT_ENGINE);
					for (int i = 0; i < alc.getConnectorCount(); i++)
						fixHooks(alc.getConnector(i), lang);
				} catch (Exception ignore) {
					logger.info("MMCONFIG.METAMCONFIMPL.CONVERTNAMES.FIXHOOKS.FOR.ASSEMBLYLINE", al, ignore);
				}
			}
		}

		tmp = (TreeMap) tm.get("Connectors");
		if (tmp != null) {
			for (Iterator e = tmp.keySet().iterator(); e.hasNext();) {
				try {
					fixHooks(getConnector(e.next()), null);
				} catch (Exception ignore) {
					logger.info("MMCONFIG.METAMCONFIMPL.CONVERTNAMES.FIXHOOKS.FOR.CONNECTORS", ignore);
				}
			}
		}

	}

	private void fixIncludes() {
		TreeMap tm = (TreeMap) getData().get(MetamergeConfig.DEFAULT_NAMESPACE_FOLDER);
		if (tm == null)
			return;

		Object[] keys = tm.keySet().toArray();

		for (int i = 0; i < keys.length; i++) {
			TreeMap t = (TreeMap) tm.get(keys[i]);
			if (t == null) {
				continue;
			}

			t.put(Context.PROVIDER_URL, t.remove("URL"));
			t.put(MetamergeConfigFactory.MC_DRIVER, "com.ibm.di.config.base.MetamergeConfigImpl");
			t.remove("includeFirst");
			t.remove("overwriteCurrent");
			urlToNameSpace.put(t.get(Context.PROVIDER_URL), keys[i]);
			try {
				MetamergeConfig mc = MetamergeConfigFactory.getLocalNamespace(this, keys[i]);
				String[] names = ((MetamergeFolder) mc.lookup(MetamergeConfig.DEFAULT_NAMESPACE_FOLDER)).getNames();
				for (int j = 0; j < names.length; j++) {
					if (tm.get(names[j]) != null)
						continue;
					NamespaceConfig nsc = mc.getNamespace(names[j]);
					TreeMap<String,String> tt = new TreeMap<String,String>();
					tt.put(Context.PROVIDER_URL, nsc.getURL());
					tt.put(MetamergeConfigFactory.MC_DRIVER, nsc.getDriver());
					tt.put("name", names[j]);
					tm.put(names[j], tt);
					urlToNameSpace.put(tt.get(Context.PROVIDER_URL), names[j]);
				}
			} catch (Exception ignore) {
				logger.info("MMCONFIG.METAMCONFIMPL.FIXINCLUDES", ignore);
			}
		}
	}

	private void fixConnectors(Object list) {
		if (!(list instanceof TreeMap))
			return;
		for (Iterator e = ((TreeMap) list).values().iterator(); e.hasNext();) {
			TreeMap tm = (TreeMap) e.next();
			String type = (String) tm.get("type");
			if (type == null) {
				type = "template";
			} else if (type.equals(ConnectorConfig.SCRIPT_MODE)) {
				continue;
			} else if (type.equals("Passive")) {
				// Convert to AddOnly, just as good a guess as anything
				tm.put(InternalSchema.CONNECTOR_MODE, "AddOnly");
				tm.put(InternalSchema.CONNECTOR_STATE, ConnectorConfig.PASSIVE_STATE);
			}
			TreeMap attr = (TreeMap) tm.remove("updateAttributes");
			if (attr == null) {
				attr = new TreeMap();
			}
			Object inheritAM = tm.remove("inheritAM");
			if (inheritAM != null && !inheritAM.equals("")) {
				attr.put(InternalSchema.INHERITS_FROM, inheritAM);
			}
			changeFlowToScript(attr, type);
			if (type.equals("Passive") || type.equals("template")) {
				tm.put("outputAttributeMap", attr);
				tm.put("inputAttributeMap", attr.clone());
			} else if (type.equals("Update") || type.equals("AddOnly")) {
				tm.put("outputAttributeMap", attr);
				tm.put("inputAttributeMap", new TreeMap());
			} else if (!type.equals("template")) {
				tm.put("inputAttributeMap", attr);
				tm.put("outputAttributeMap", new TreeMap());
			}
			if (type.equals("Update") || type.equals("Lookup") || type.equals("Delete") || type.equals("Passive")
					|| type.equals("template")) {
				TreeMap link = new TreeMap();
				link.put(InternalSchema.CONNECTOR_ADVANCED_LINK_CRITERIA, tm.remove("linkScript"));
				link.put(InternalSchema.CONNECTOR_LINK_MODE, tm.remove("advancedLink"));
				// link.put(InternalSchema.CONNECTOR_LINK_CRITERIA,
				// tm.remove("link"));
				TreeMap sublink = new TreeMap();
				fixLinkCriteria(sublink, (Vector) tm.remove("link"));
				link.put(InternalSchema.CONNECTOR_LINK_CRITERIA, sublink);
				tm.put(InternalSchema.CONNECTOR_LINK_CONFIG, link);
			}
			TreeMap events = (TreeMap) tm.get("ALEvent");
			if (events == null) {
				events = new TreeMap();
				tm.put("ALEvent", events);
			} else {
				changeFlowToScript(events, null);
			}
			Object inheritEH = tm.remove("inheritEH");
			if (inheritEH != null && !inheritEH.equals("")) {
				events.put(InternalSchema.INHERITS_FROM, inheritEH);
			}
			TreeMap parserConfig = (TreeMap) tm.get("parserConfig");
			if (parserConfig != null && parserConfig.get(InternalSchema.INHERITS_FROM) == null) {
				parserConfig.put(InternalSchema.INHERITS_FROM, "[parent]");
			}

			if ("false".equals(tm.remove("compute_changes")))
				tm.put(InternalSchema.CONNECTOR_COMPUTE_CHANGES, "false");

			// Special hack for fileConnector
			if ("append".equals(tm.get("fileMode")))
				tm.put("fileAppend", "true");
		}
	}

	private void fixParsers(Object list) {
		if (!(list instanceof TreeMap))
			return;
		for (Iterator e = ((TreeMap) list).values().iterator(); e.hasNext();) {
			TreeMap tm = (TreeMap) e.next();
			TreeMap cfg = (TreeMap) tm.remove("parserConfig");
			if (cfg != null)
				tm.putAll(cfg);
		}
	}

	private void fixLinkCriteria(TreeMap dest, Vector link) {
		if (link == null)
			return;
		for (int k = 0; k < link.size(); k++) {
			String str = link.elementAt(k).toString();
			TreeMap ntree = new TreeMap();
			int i = str.indexOf(":");
			if (i != -1) {
				ntree.put(InternalSchema.LC_ATTRIBUTE, str.substring(0, i));
				ntree.put(InternalSchema.LC_VALUE, str.substring(i + 3));
				switch (str.charAt(i + 1)) {
				case LinkCriteriaItem.EXACT:
					ntree.put(InternalSchema.LC_OPERATOR, LinkCriteriaItem.LC_EXACT);
					break;
				case LinkCriteriaItem.SUBSTRING:
					ntree.put(InternalSchema.LC_OPERATOR, LinkCriteriaItem.LC_SUBSTRING);
					break;
				case LinkCriteriaItem.INITIAL_STRING:
					ntree.put(InternalSchema.LC_OPERATOR, LinkCriteriaItem.LC_INITIAL);
					break;
				case LinkCriteriaItem.FINAL_STRING:
					ntree.put(InternalSchema.LC_OPERATOR, LinkCriteriaItem.LC_FINAL);
					break;
				case LinkCriteriaItem.NOT_STRING:
					ntree.put(InternalSchema.LC_OPERATOR, LinkCriteriaItem.LC_NOT);
					break;
				}
				dest.put(Long.toHexString(linkDate++), ntree);
			}
		}
	}

	private void changeFlowToScript(TreeMap events, String mode) {

		String match = null;
		if (mode == null)
			match = null;
		else if (mode.equals("Iterator") || mode.equals("Lookup"))
			match = "ret.value = conn.getAttribute(\"";
		else if (mode.equals("Update") || mode.equals("AddOnly"))
			match = "ret.value = work.getAttribute(\"";

		for (Iterator it = events.values().iterator(); it.hasNext();) {
			Object h = it.next();
			if (!(h instanceof TreeMap))
				continue;
			TreeMap hook = (TreeMap) h;

			Object o = hook.remove("flow");
			if (!(o instanceof String))
				continue;
			hook.put("script", o);
			String str = (String) o;

			if (match != null && str.startsWith(match) && str.endsWith("\");")) {
				String attr = str.substring(match.length(), str.length() - 3);
				if (attr.indexOf('"') == -1) {
					hook.put(InternalSchema.AMI_TYPE, "simple");
					hook.put(InternalSchema.AMI_SIMPLE, attr);
				}
			}
		}
	}

	private void buildtableName(TreeMap tm, String context) {

		for (Iterator e = tm.keySet().iterator(); e.hasNext();) {
			String key = e.next().toString();
			Object val = tm.get(key);

			if (key.equals("%%EXTERNAL_PATH%%")) {
				String s = val.toString();
				if (s.equals("") || context.equals(""))
					return;

				tableName.put(context, (String) urlToNameSpace.get(s));
			} else if (val instanceof TreeMap) {
				String c = context + "/" + key;
				if (key.equals("connectorConfig"))
					c = "/Connectors";
				else if (key.equals("parserConfig"))
					c = "/Parsers";
				buildtableName((TreeMap) val, c);
			}
		}
	}

	private Object fixInheritance(TreeMap tm, String context) {
		Object newInherit = null;

		for (Iterator e = tm.keySet().iterator(); e.hasNext();) {
			String key = e.next().toString();
			Object val = tm.get(key);

			if (val instanceof TreeMap) {
				String c = context;
				if (key.equals("connectorConfig"))
					c = "connectorconfig";
				else if (key.equals("parserConfig"))
					c = "/Parsers/";
				else if (key.endsWith("AttributeMap") || key.equals("ALEvent"))
					c = "/Connectors/";
				val = fixInheritance((TreeMap) val, c);
				if (val != null)
					newInherit = val;
			} else if (val instanceof Vector) {
				for (int i = 0; i < ((Vector) val).size(); i++) {
					Object o = ((Vector) val).get(i);
					if (o instanceof TreeMap)
						fixInheritance((TreeMap) o, "");
				}
			}
		}

		if (newInherit != null)
			tm.put(InternalSchema.INHERITS_FROM, newInherit);

		if (tm.get("scriptEngine") != null)
			tm.put("ScriptEngine", tm.remove("scriptEngine"));

		fixInheritanceString(tm, "parser", "/Parsers/");
		return fixInheritanceString(tm, InternalSchema.INHERITS_FROM, context);
	}

	private Object fixInheritanceString(TreeMap tm, String key, String context) {

		Object val = tm.get(key);
		if (!(val instanceof String))
			return null;

		String s = (String) val;
		if (s.equals("") || context.equals("") || s.equals("[parent]"))
			return null;

		boolean moveUp = false;

		if (context.equals("connectorconfig")) {
			moveUp = true;
			context = "/Connectors/";
		}

		String name;

		if (s.startsWith("metamerge.")) {
			s = s.substring(10);
			if (s.equals("FileSystem")) {
				if ("append".equals(tm.remove("fileMode")))
					tm.put("fileAppend", "true");
			} else if (s.equals("ADSI"))
				s = "NT4";
			else if (s.equals("HTTPClient"))
				s = "OldHTTPClient";
			else if (s.equals("HTTPClient2"))
				s = "HTTPClient";
			else if (s.equals("HTTPServer"))
				s = "OldHTTPServer";
			else if (s.equals("HTTPServer2"))
				s = "HTTPServer";
			else if (s.equals("SecureWayChangelog"))
				s = "IBMDirectoryServerChangelog";
			else if (s.equals("DominoUsers"))
				s = "DominoUsersConnector";
			else if (s.equals("Script1"))
				s = "ScriptConnector";
			else if (s.equals("TIB/Adapter"))
				s = "TIBAdapter";
			else if (s.equals("FioranoMQ") || s.equals("SonicMQ"))
				s = "JMS";
			else if (s.equals("Cronjob"))
				s = "Timer";
			else if (s.startsWith("Oracle8")) {
				s = "JDBC";
				if (tm.get("jdbcDriver") == null)
					tm.put("jdbcDriver", "oracle.jdbc.driver.OracleDriver");
			} else if (s.equals("MySQL")) {
				s = "JDBC";
				if (tm.get("jdbcDriver") == null)
					tm.put("jdbcDriver", "org.gjt.mm.mysql.Driver");
			} else if (s.equals("AdminPort") || s.equals("DNS") || s.equals("GSMPhone") || s.equals("EMI_UCP") || s.equals("SMPP")
					|| s.equals("LDAPListener") || s.equals("MailboxListener") || s.equals("TCPListener") || s.equals("CDS-EDB")
					|| s.equals("NTAccountMgr") || s.equals("XMLSAX") || s.equals("COMPort")) {
				tm.remove(key);
				return null;
			}

			name = MetamergeConfigFactory.SYSTEM_NAMESPACE + ":" + context + "ibmdi." + s;

		} else if (s.startsWith("@") && moveUp) {
			name = s.substring(1);
		} else if (s.equalsIgnoreCase("(runtime provided)")) {
			name = s;
		} else {
			s = context + s;
			name = tableName.get(s);
			if (name != null) {
				name += ":" + s;
			} else {
				// heuristic
				try {
					name = MetamergeConfigFactory.SYSTEM_NAMESPACE + ":" + s;
					lookup(name);
				} catch (Exception notfound) {
					name = s;
				}
			}
		}

		if (moveUp) {
			tm.put(key, "[parent]");
			return name;
		} else {
			if (!name.equals(val))
				logger.info("MMCONFIG.HAS.BEEN.RENAMED", val, name);
			tm.put(key, name);
			return null;
		}
	}

	/*
	 * Try to add code to Hooks to make them behave like they used to do.
	 */
	private void fixHooks(ConnectorConfig c, String lang) {
		if (c == null)
			return;

		String mode = (String) c.getMode();

		if (mode == null || mode.equals(c.SCRIPT_MODE))
			return;

		String skipEntry = "system.skipEntry(\"No Entries Found\");";
		String ignoreEntry = "system.ignoreEntry(\"No Entries Found\");";
		String setCurrent = "thisConnector.setCurrent(thisConnector.getNextDuplicateEntry());";
		if (lang != null) {
			if (lang.equals("vbscript")) {
				skipEntry = "system.skipEntry";
				ignoreEntry = "system.ignoreEntry";
				setCurrent = "thisConnector.setCurrent thisConnector.getNextDuplicateEntry()";
			} else if (lang.equals("perlscript")) {
				skipEntry = "$system->skipEntry();";
				ignoreEntry = "$system->ignoreEntry();";
				setCurrent = "$thisConnector->setCurrent($thisConnector->getNextDuplicateEntry());";
			}
		}

		HooksConfig hooks = c.getHooks();
		try {
			hooks.setupInheritanceChain();
		} catch (Exception exc) {
			logmsg(sResHash.getString("MMCONFIG.LOG.EXCEPTION.MESSAGE", exc.getMessage()));
		}

		if (mode.equals(c.LOOKUP_MODE)) {
			if (isDisabled(hooks, "lookup_fail"))
				hooks.setHook(new HookConfigImpl("lookup_nomatch", skipEntry));

			HookConfig hook;
			if (c.getBooleanParameter("allow_duplicates", false)) {
				hook = new HookConfigImpl("lookup_multiple", setCurrent);
				c.removeParameter("allow_duplicates");
			} else {
				hook = hooks.getHook("lookup_multiple");
				if (hook.getEnabled()) {
					String script = (String) hook.getScript();
					if (script == null || script.equals(""))
						hook.setScript(setCurrent);
					// else if (script.indexOf ("setCurrent") == -1 )
					// hook.setScript( script + "\n" + setCurrent);
				} else if (isDisabled(hooks, "lookup_fail")) {
					hook = new HookConfigImpl("lookup_multiple", skipEntry);
				}
			}
			hooks.setHook(hook);
		}
		if ((mode.equals(c.UPDATE_MODE)) && (isDisabled(hooks, "update_multiple"))) {
			hooks.setHook(new HookConfigImpl("update_multiple", skipEntry));
		}
		if ((mode.equals(c.DELETE_MODE)) && (isDisabled(hooks, "delete_fail"))) {
			hooks.setHook(new HookConfigImpl("delete_nomatch", ignoreEntry));
		}
	}

	private boolean isDisabled(HooksConfig hooks, String name) {
		return (!hooks.getHook(name).getEnabled());
	}

	private void fixSettings(TreeMap al) {
		if (al == null)
			return;
		TreeMap settings = new TreeMap();
		for (int i = 0; i < SETTING_NAMES.length; i++) {
			Object o = al.remove(SETTING_NAMES[i]);
			if (o != null)
				settings.put(SETTING_NAMES[i], o);
		}
		al.put(InternalSchema.AL_SETTINGS, settings);
	}

	protected Object internalLookup(Object name) throws Exception {

		logger.debug("MMCONFIG.METAMCONFIMPL.LOOKUP", name, name.getClass().getName());

		Object obj = cache.getObject(name);
		if (obj != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("MMCONFIG.METAMCONFIMPL.CACHE.HIT.FOR", new Object[] { name, obj.getClass().getName(),
						Integer.valueOf(obj.hashCode()) });
			}
			return obj;
		}

		if (!MetamergeConfigFactory.isNameLocal(this, name))
			return MetamergeConfigFactory.lookup(this, name);

		obj = getData();
		Name st;

		if (name instanceof Name)
			st = (Name) name;
		else
			st = MetamergeConfigFactory.parseName(name.toString());

		String enclosingClass = getEnclosingClass(st);
		String nt = null;

		for (int i = 0; i < st.size(); i++) {
			nt = st.get(i);

			if (nt.equals("")) {
				if (i == 0 && st.size() > 1)
					continue;// Ignore leading slash ...
				else
					break;
			}

			else if (obj instanceof BaseConfiguration)
				obj = ((BaseConfiguration) obj).getParameter(nt);
			else if (obj instanceof TreeMap)
				obj = ((TreeMap) obj).get(nt);
			else {
				throw new Exception(sResHash.getString("MMCONFIG.METAMCONFIMPL.NOT.A.CONTEXT.TREEMAP", name));
			}

			/*
			 * logmsg( " obj is " + (obj == null ? "null pointer" :
			 * obj.getClass().getName() ) ); if ( obj != null && obj instanceof
			 * TreeMap ) { logmsg( " Dump keys in obj" ); for ( Iterator ks =
			 * ((TreeMap)obj).keySet().iterator(); ks.hasNext(); ) { logmsg( "
			 * obj key: " + ks.next() ); } logmsg( " ***" ); }
			 */

			if (obj == null) {
				throw new NameNotFoundException(sResHash.getString("MMCONFIG.METAMCONFIMPL.NAME.NOT.FOUND.INTERNALLOOKUP",
						new Object[] { name, nt }));
			}
		}

		if (obj instanceof BaseConfiguration) {
			if (logger.isDebugEnabled()) {
				logger.debug("MMCONFIG.METAMCONFIMPL.ADD.BASECONFIG.CACHE", new Object[] { name, obj.getClass().getName(),
						Integer.valueOf(obj.hashCode()) });
			}
			cache.addObject(name, (BaseConfiguration) obj);
			((BaseConfiguration) obj).setMetamergeConfig(this);
			return obj;
		}

		BaseConfiguration base = (BaseConfiguration) Class.forName(enclosingClass).newInstance();
		base.setMetamergeConfig(this);
		base.setData((TreeMap) obj);
		base.setName(st);

		// Save in cache
		if (logger.isDebugEnabled()) {
			logger.debug("MMCONFIG.METAMCONFIMPL.ADD.OBJECT.CACHE", new Object[] { name, base.getClass().getName(),
					Integer.valueOf(base.hashCode()) });
		}
		cache.addObject(name, base);

		// Must be in cache before we call these methods!
		base.init();
		base.setupInheritanceChain();

		return base;
	}

	private String getEnclosingClass(Name name) {
		String cls = DEFAULT_FOLDER_IMPL;
		if (name.size() < 2) {
			if (name.size() == 1
					&& (name.get(0).equals(MetamergeConfig.DEFAULT_PROPERTY_FOLDER)
							|| name.get(0).equals(MetamergeConfig.DEFAULT_LIBRARY_FOLDER) || name.get(0).equals(
							MetamergeConfig.DEFAULT_EXTPROP_FOLDER))) {
				return classMap.get(name.get(0));
			} else {
				return cls;
			}
		}

		String str = classMap.get(name.get(0));
		if (str != null)
			cls = str;

		return cls;
	}

	public void commitChanges(Object output) throws Exception {
		commitChanges(output, true);
	}

	public void commitChangesNoEncryption(Object output) throws Exception {
		Boolean oldValue = encryptProtected;
		encryptProtected = Boolean.FALSE;
		try {
			commitChanges(output, false);
		} finally {
			encryptProtected = oldValue;
		}
	}

	public void commitChanges(Object output, boolean isSave) throws Exception {
		// throw new Exception ( "Cannot save old style config files" );
		if (output instanceof OutputStream)
			file.saveConfig((OutputStream) output);
		else if (output == null)
			file.saveConfig();
		else {
			throw new Exception(sResHash.getString("MMCONFIG.METAMCONFIMPL.CANNOT.SAVE.TO", output.getClass().getName()));
		}

		if (isSave)
			setModified(false);
	}

	public boolean isCommittable() {
		return true;
	}

	public boolean isReadOnly() {
		return true;
	}

	public void copy(BaseConfiguration input, Object destination, boolean copyRefs) throws Exception {
	}

	public MetamergeFolder createFolder(Object name) throws Exception {
		throw new Exception(sResHash.getString("MMCONFIG.METAMCONFIMPL.CANNOT.CREATE.FOLDER"));
	}

	public void setOutput(Object output) throws Exception {
		commitChanges(output, true);
	}

	public MetamergeFolder getDefaultFolder(int folder) throws Exception {
		return (MetamergeFolder) lookup(DEFAULT_FOLDER_NAMES[folder]);
	}

	public synchronized Object lookupInFolder(String folder, Object name) throws Exception {
		if (name instanceof Name)
			return internalLookup(name);

		// If the input object name is a simple name then prepend the folder
		// name. Else,
		// the input name is a complete path for lookup
		Name cn = MetamergeConfigFactory.parseName(folder);
		Name objname = MetamergeConfigFactory.parseName(name);
		if (objname.size() > 1) {
			cn = objname;
		} else {
			cn.addAll(objname);
		}

		return internalLookup(cn);
	}

	public synchronized Object lookup(Object name) throws Exception {
		if (name.equals("system:/Connectors/ibmdi.DSMLv2Connector")) {
			name = "system:/Connectors/ibmdi.TIMDSMLv2Connector";
			logger.debug("MMCONFIG.FOUND.OLD.CONNECTOR.NAME.DSMLV2", "ibmdi.DSMLv2Connector", "ibmdi.TIMDSMLv2Connector");
		}
		try {
			return internalLookup(name);
		} catch (NameNotFoundException nfe) {
			// Ignore. A general Exception is thrown at the end of the method.
		}

		// Migration code - try "ibmdi." instead of "metamerge."
		try {
			Name n = MetamergeConfigFactory.parseName(name);
			String last = n.get(n.size() - 1);
			if (last.startsWith("metamerge.")) {
				last = last.substring(10);
				if (last.equals("ADSI"))
					last = "NT4";
				n.remove(n.size() - 1);
				n.add("ibmdi." + last);
				return internalLookup(n);
			}
		} catch (Exception any) {
			// Ignore. A general Exception is thrown at the end of the method.
		}

		// Do not throw an Exception if we cannot find items in SYSTEM_NAMESPACE
		// before it exists
		if (name.toString().startsWith("system:")
				&& MetamergeConfigFactory.getNamespace(MetamergeConfigFactory.SYSTEM_NAMESPACE) == null)
			return null;

		throw new NameNotFoundException(sResHash.getString("MMCONFIG.METAMCONFIMPL.NAME.NOT.FOUND.LOOKUP", new Object[] { name
				.toString() }));
	}

	public Enumeration list(Object name) throws Exception {
		Object obj = lookup(name);
		return new TreeMapEnumeration(this, name, (BaseConfiguration) obj);
	}

	public Enumeration list() throws Exception {
		// return list ( getName() );
		return new TreeMapEnumeration(this, null, this);
	}

	public String[] getNames() throws Exception {
		String[] names;
		int i = 0;
		for (Enumeration e = list(); e.hasMoreElements(); e.nextElement(), i++) {
		}
		names = new String[i];

		i = 0;
		for (Enumeration e = list(); e.hasMoreElements(); i++) {
			names[i] = e.nextElement().toString();
		}

		return names;
	}

	public void unbind(Object name) throws Exception {
		throw new Exception(sResHash.getString("MMCONFIG.METAMCONFIMPL.BIND.NOT.SUPPORTED.BY.THIS.CONFIG.DRIVER"));
	}

	public void bind(Object name, Object obj) throws Exception {
		throw new Exception(sResHash.getString("MMCONFIG.METAMCONFIMPL.BIND.NOT.SUPPORTED.BY.THIS.CONFIG.DRIVER"));
	}

	public void rebind(Object name, Object obj) throws Exception {
		throw new Exception(sResHash.getString("MMCONFIG.METAMCONFIMPL.BIND.NOT.SUPPORTED.BY.THIS.CONFIG.DRIVER"));
	}

	public void rename(Object name, Object newname) throws Exception {
		throw new Exception(sResHash.getString("MMCONFIG.METAMCONFIMPL.RENAME.NOT.SUPPORTED.BY.THIS.CONFIG.DRIVER"));
	}

	public Object addNameComponent(Object name, String component, boolean prefix) {

		try {
			if (name instanceof Name) {
				if (prefix)
					((Name) name).add(0, component);
				else
					((Name) name).add(component);
				return name;
			}
		} catch (Exception error) {
			logmsg(error.toString());
		}

		if (!prefix) {
			if (name != null)
				return name + "/" + component;
			else
				return component;
		} else {
			if (name != null)
				return component + "/" + name;
			else
				return component;
		}
	}

	public static Vector getVector(BaseConfiguration base, Object key) {
		Object obj = base.getParameter(key);
		if (obj == null) {
			obj = new Vector();
			base.setParameter(key, obj);
		}
		return (Vector) obj;
	}

	public static TreeMap getTreeMap(BaseConfiguration base, Object key) {
		Object obj = base.getParameter(key);
		if (obj == null) {
			obj = new TreeMap();
			base.setParameter(key, obj);
		}
		return (TreeMap) obj;
	}

	public AssemblyLineConfig getAssemblyLine(Object name) throws Exception {
		try {
			Object obj = lookupInFolder(MetamergeConfig.DEFAULT_ASSEMBLYLINE_FOLDER, name);
			if (obj instanceof AssemblyLineConfig)
				return (AssemblyLineConfig) obj;
		} catch (Exception e1) {
			// Ignore. A general Exception is thrown at the end of the method.
		}

		MetamergeFolder ns = (MetamergeFolder) lookup(MetamergeConfig.DEFAULT_NAMESPACE_FOLDER);
		String[] names = ns.getNames();
		for (int i = 0; i < names.length; i++) {
			try {
				AssemblyLineConfig al = MetamergeConfigFactory.getLocalNamespace(this, names[i]).getAssemblyLine(name);
				logger.info("MMCONFIG.METAMCONFIMPL.ASSEMBLYLINE.FOUND.IN", name, names[i]);
				return al;
			} catch (Exception e2) {
				// Ignore. A general Exception is thrown at the end of the
				// method.
			}
		}

		throw new Exception(sResHash.getString("MMCONFIG.METAMCONFIMPL.IS.NOT.AN.ASSEMBLYLINECONFIG", name));
	}

	public SequenceConfig getSequence(Object name) throws Exception {
		try {
			Object obj = lookupInFolder(MetamergeConfig.DEFAULT_SEQUENCE_FOLDER, name);
			if (obj instanceof SequenceConfig)
				return (SequenceConfig) obj;
		} catch (Exception e1) {
			// Ignore. Return null if nothing is found.
		}

		MetamergeFolder ns = (MetamergeFolder) lookup(MetamergeConfig.DEFAULT_NAMESPACE_FOLDER);
		String[] names = ns.getNames();
		for (int i = 0; i < names.length; i++) {
			try {
				SequenceConfig sc = MetamergeConfigFactory.getLocalNamespace(this, names[i]).getSequence(name);
				if (sc != null)
					return sc;	
			} catch (Exception e2) {
				// Ignore. Return null if nothing is found.
			}
		}

		return null;
	}

	public ConnectorConfig getConnector(Object name) throws Exception {
		try {
			Object obj = lookupInFolder(MetamergeConfig.DEFAULT_CONNECTOR_FOLDER, name);
			if (obj instanceof ConnectorConfig)
				return (ConnectorConfig) obj;
		} catch (Exception e1) {
			// Ignore. A general Exception is thrown at the end of the method.
		}

		MetamergeFolder ns = (MetamergeFolder) lookup(MetamergeConfig.DEFAULT_NAMESPACE_FOLDER);
		String[] names = ns.getNames();
		for (int i = 0; i < names.length; i++) {
			try {
				ConnectorConfig cc = MetamergeConfigFactory.getLocalNamespace(this, names[i]).getConnector(name);
				logger.info("MMCONFIG.METAMCONFIMPL.CONNECTORCONFIG.FOUND.IN", name, names[i]);
				return cc;
			} catch (Exception e2) {
				// Ignore. A general Exception is thrown at the end of the
				// method.
			}
		}

		throw new Exception(sResHash.getString("MMCONFIG.METAMCONFIMPL.IS.NOT.A.CONNECTORCONFIG", name));
	}

	public ParserConfig getParser(Object name) throws Exception {
		try {
			Object obj = lookupInFolder(MetamergeConfig.DEFAULT_PARSER_FOLDER, name);
			if (obj instanceof ParserConfig)
				return (ParserConfig) obj;
		} catch (Exception e1) {
			// Ignore. A general Exception is thrown at the end of the method.
		}

		MetamergeFolder ns = (MetamergeFolder) lookup(MetamergeConfig.DEFAULT_NAMESPACE_FOLDER);
		String[] names = ns.getNames();
		for (int i = 0; i < names.length; i++) {
			try {
				ParserConfig pc = MetamergeConfigFactory.getLocalNamespace(this, names[i]).getParser(name);
				logger.info("MMCONFIG.METAMCONFIMPL.PARSERCONFIG.FOUND.IN", name, names[i]);
				return pc;
			} catch (Exception e2) {
				// Ignore. A general Exception is thrown at the end of the
				// method.
			}
		}

		throw new Exception(sResHash.getString("MMCONFIG.METAMCONFIMPL.IS.NOT.A.PARSER", name));
	}

	public ScriptConfig getScript(Object name) throws Exception {
		try {
			Object obj = lookupInFolder(MetamergeConfig.DEFAULT_SCRIPT_FOLDER, name);
			if (obj instanceof ScriptConfig)
				return (ScriptConfig) obj;
		} catch (Exception e1) {
			// Ignore. A general Exception is thrown at the end of the method.
		}

		MetamergeFolder ns = (MetamergeFolder) lookup(MetamergeConfig.DEFAULT_NAMESPACE_FOLDER);
		String[] names = ns.getNames();
		for (int i = 0; i < names.length; i++) {
			try {
				ScriptConfig sc = MetamergeConfigFactory.getLocalNamespace(this, names[i]).getScript(name);
				logger.info("MMCONFIG.METAMCONFIMPL.SCRIPT.FOUND.IN", name, names[i]);
				return sc;
			} catch (Exception e2) {
				// Ignore. A general Exception is thrown at the end of the
				// method.
			}
		}

		throw new Exception(sResHash.getString("MMCONFIG.METAMCONFIMPL.IS.NOT.A.SCRIPT", name));
	}

	public FunctionConfig getFunction(Object name) throws Exception {
		try {
			Object obj = lookupInFolder(MetamergeConfig.DEFAULT_FUNCTION_FOLDER, name);
			if (obj instanceof FunctionConfig)
				return (FunctionConfig) obj;
		} catch (Exception e1) {
			// Ignore. A general Exception is thrown at the end of the method.
		}

		MetamergeFolder ns = (MetamergeFolder) lookup(MetamergeConfig.DEFAULT_NAMESPACE_FOLDER);
		String[] names = ns.getNames();
		for (int i = 0; i < names.length; i++) {
			try {
				FunctionConfig sc = MetamergeConfigFactory.getLocalNamespace(this, names[i]).getFunction(name);
				logger.info("MMCONFIG.METAMCONFIMPL.FUNCTION.FOUND.IN", name, names[i]);
				return sc;
			} catch (Exception e2) {
				// Ignore. A general Exception is thrown at the end of the
				// method.
			}
		}

		throw new Exception(sResHash.getString("MMCONFIG.METAMCONFIMPL.IS.NOT.A.FUNCTION", name));
	}

	public AttributeMapConfig getAttributeMap(Object name) throws Exception {
		try {
			Object obj = lookupInFolder(MetamergeConfig.DEFAULT_ATTRIBUTEMAP_FOLDER, name);
			if (obj instanceof ALMappingConfig)
				return ((ALMappingConfig)obj).getAttributeMap();
		} catch (Exception ignore) {
			// Ignore. We throw general Exception later
			SystemFunctions.doNothing();
		}

		MetamergeFolder ns = (MetamergeFolder) lookup(MetamergeConfig.DEFAULT_NAMESPACE_FOLDER);
		for (String nameSpace: ns.getNames()) {
			try {
				return MetamergeConfigFactory.getLocalNamespace(this, nameSpace).getAttributeMap(name);
			} catch (Exception ignore) {
				// Ignore. We throw general Exception later
				SystemFunctions.doNothing();
			}
		}

		throw new Exception(sResHash.getString("MMCONFIG.METAMCONFIMPL.IS.NOT.ATTRIBUTEMAP", name));
	}

	public NamespaceConfig getNamespace(Object name) throws Exception {
		Object obj = lookupInFolder(MetamergeConfig.DEFAULT_NAMESPACE_FOLDER, name);
		if (obj instanceof NamespaceConfig)
			return (NamespaceConfig) obj;
		else {
			throw new Exception(sResHash.getString("MMCONFIG.METAMCONFIMPL.IS.NOT.A.NAMESPACECONFIG", name));
		}
	}

	/**
	 * Create new instance of object
	 */
	public BaseConfiguration newInstanceOf(int type) throws Exception {
		return newInstanceOf(DEFAULT_FOLDER_NAMES[type]);
	}

	public BaseConfiguration newInstanceOf(Object typeName) throws Exception {
		String cls = classMap.get(typeName);
		if (cls == null)
			cls = DEFAULT_FOLDER_IMPL;

		Object obj = Class.forName(cls).newInstance();
		((BaseConfiguration) obj).setMetamergeConfig(this);
		((BaseConfiguration) obj).init();
		return (BaseConfiguration) obj;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.config.interfaces.MetamergeConfig#newInstanceOf(java.lang.
	 * Class)
	 */
	public <T extends BaseConfiguration> T newInstanceOf(Class<T> cls) throws Exception {
		if ("com.ibm.di.config.interfaces".equals(cls.getPackage().getName()) && !cls.getName().endsWith("Impl")) {
			cls = (Class<T>) Class.forName("com.ibm.di.config.base." + cls.getSimpleName() + "Impl");
		}
		return cls.newInstance();
	}

	public String toString() {
		Object str = env.get(MetamergeConfigFactory.MC_NAMESPACE);

		if (str == null) {
			str = env.get(Context.PROVIDER_URL);
		}

		if (str == null)
			return "[no name]";
		else
			return str.toString();
	}

	/**
	 * Returns the external properties delegator object for this configuration.
	 * 
	 * @return The ExternalPropertiesConfig value
	 * @exception Exception
	 */
	public ExternalPropertiesConfig getExternalProperties() throws Exception {
		if (externalPropertiesDelegator == null)
			externalPropertiesDelegator = new ExternalPropertiesDelegator(this);

		return externalPropertiesDelegator;
	}

	/**
	 * Returns the ExternalPropertiesConfig object for the named external
	 * properties object. Name can either be a simple name or a fully qualified
	 * name. For simple names, the config driver will lookup the object in the
	 * default folder for external properties.
	 * 
	 * @param name
	 *            The external property object name
	 * 
	 * @return The ExternalPropertiesConfig object
	 */
	public ExternalPropertiesConfig getExternalProperties(Object name) throws Exception {
		return (ExternalPropertiesConfig) lookup(MetamergeConfig.DEFAULT_EXTPROP_FOLDER + "/" + name);
	}

	public void logmsg(String msg) {
		logger.debug(msg);
	}

	/**
	 * This method returns the driver parameters as a BaseConfiguration object.
	 * 
	 * @return The newly created object.
	 * @exception Exception
	 */
	public BaseConfiguration getDriverParameters() throws Exception {
		BaseConfiguration b = new BaseConfigurationImpl();
		for (Enumeration e = env.keys(); e.hasMoreElements();) {
			Object key = e.nextElement();
			if (javax.naming.Context.SECURITY_CREDENTIALS.equals(key))
				continue;
			b.setParameter(key, env.get(key));
		}
		return b;
	}

	/**
	 * This method sets the driver parameters from a BaseConfiguration object.
	 * 
	 * @param driverParams
	 *            The driver parameters
	 * @exception Exception
	 */
	public void setDriverParameters(BaseConfiguration driverParams) throws Exception {
		java.util.List l = driverParams.getKeys(BaseConfiguration.ONE_LEVEL);
		for (int i = 0; i < l.size(); i++) {
			env.put(l.get(i), driverParams.getParameter(l.get(i)));
		}
	}

	/**
	 * This method returns the value for a given driver parameter.
	 * 
	 * @param name
	 *            The name of the driver parameter to retrieve.
	 * 
	 * @return The driver parameter value
	 * @exception Exception
	 */
	public Object getDriverParameter(Object name) throws Exception {
		/*
		 * if ( javax.naming.Context.SECURITY_CREDENTIALS.equals(name) ) throw
		 * new Exception ( "Access Denied"); else
		 */
		return env.get(name);
	}

	/**
	 * This method sets a driver parameter.
	 * 
	 * @param name
	 *            The driver parameter name
	 * @param value
	 *            The driver parameter value
	 * @exception Exception
	 */
	public void setDriverParameter(Object name, Object value) throws Exception {
		if (value != null)
			env.put(name, value);
		else
			env.remove(name);
	}

	public void setFileConfig(FileConfig aFileConfig) {
		file = aFileConfig;
	}

	public FileConfig getFileConfig() {
		return file;
	}

	public boolean isRemote() {
		return false;
	}

	/**
	 * This method iterates the entire configuration to create java objects from
	 * the config drivers underlying store. This is needed when all references
	 * to other namespaces must be resolved (system and others).
	 * 
	 * @exception Exception
	 */
	public void instantiateAllObjects() throws Exception {
		instantiateAllObjects(this);
	}

	public void instantiateAllObjects(MetamergeFolder folder) throws Exception {
		for (Enumeration e = folder.list(); e.hasMoreElements();) {
			Binding b = (Binding) e.nextElement();
			Object o = b.getObject();
			if (o instanceof MetamergeFolder && !(o instanceof PropertyManager)) {
				instantiateAllObjects((MetamergeFolder) o);
			}
		}
	}

	/**
	 * This method returns the associated TDIProperties object
	 */
	public synchronized TDIProperties getTDIProperties() throws Exception {
		TDIProperties tdi = (TDIProperties) getParameter("TDI-PROPERTIES");
		if (tdi == null) {
			tdi = new TDIProperties(this, false); // Defect # 11367
			setParameter("TDI-PROPERTIES", tdi, false);
			tdi.initStores();
		}
		return tdi;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.config.interfaces.MetamergeConfig#getSolutionInterface()
	 */
	public SolutionInterface getSolutionInterface() {
		try {
			Object obj = lookup(MetamergeConfig.DEFAULT_SERVER_FOLDER + "/" + MetamergeConfig.DEFAULT_SOLUTION_INTERFACE);
			if (obj instanceof SolutionInterface)
				return (SolutionInterface) obj;
		} catch (Exception e1) {
			// We did not find a SI. No need to throw or log an Exception.
		}
		return null;
	}

	public String getConfigVersion() {
		return "0";
	}

	public String getDirectory() {
		try {
			Object path = getDriverParameter(MetamergeConfigFactory.MC_URL);
			if (path instanceof File)
				return ((File) path).getParentFile().getAbsolutePath();
			if (path instanceof String)
				return new File(path.toString()).getParentFile().getAbsolutePath();
		} catch (Exception e) {
			return ".";
		}
		return ".";
	}

	public void setModTSEnabled(boolean value) {
		modTSEnabled = value;
	}

	public boolean isModTSEnabled() {
		return modTSEnabled;
	}

	public boolean shouldEncryptProtected() {
		return encryptProtected != null ? encryptProtected : true;
	}
}
