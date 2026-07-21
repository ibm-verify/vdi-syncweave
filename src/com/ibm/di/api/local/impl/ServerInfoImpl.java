/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.local.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import javax.naming.Binding;
import javax.naming.NameNotFoundException;

import com.ibm.di.api.APIEngine;
import com.ibm.di.api.AuthorizationException;
import com.ibm.di.api.DIException;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.ParserConfig;
import com.ibm.di.config.interfaces.RawConnectorConfig;
import com.ibm.di.connector.ConnectorInterface;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.model.descriptor.ComponentDescriptor;
import com.ibm.di.model.descriptor.DescriptorUtils;
import com.ibm.di.osgi.ConnectorDelegate;
import com.ibm.di.server.RS;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.Version;
import com.ibm.di.server.VersionInfoInterface;

/**
 * This class implements various methods for getting server information.
 */
public class ServerInfoImpl implements com.ibm.di.api.local.ServerInfo {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Variable representing unknown value.
	 */
	private static final String VALUE_UNKNOWN = "unknown";

	/**
	 * Parameter name - configuration properties description.
	 */
	private static final String CFG_PROP_DESCRIPTION = "description";
	/**
	 * Parameter name - properties name.
	 */
	private static final String PROP_NAME = "Name";
	/**
	 * Parameter name - properties description.
	 */
	private static final String PROP_DESCRIPTION = "Description";
	/**
	 * Parameter name - properties version info.
	 */
	private static final String PROP_VERSION_INFO = "Version";

	/**
	 * Represents the local session.
	 */
	private SessionImpl mSession = null;

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private final static ResourceHash sResHash = APIEngine.getResHash();

	/**
	 * 
	 * @param aSession
	 */
	public ServerInfoImpl(SessionImpl aSession) {
		mSession = aSession;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getServerVersion() throws DIException {
		// everyone is allowed to execute this method

		String serverVersion = Version.version();
		if (serverVersion == null) {
			serverVersion = VALUE_UNKNOWN;
		}
		return serverVersion;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getIPAddress() throws DIException {
		// everyone is allowed to execute this method

		return getLocalIPAddress();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getHostName() throws DIException {
		// everyone is allowed to execute this method

		String hostName;
		try {
			hostName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			hostName = VALUE_UNKNOWN;
		}
		return hostName;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getOperatingSystem() throws DIException {
		// everyone is allowed to execute this method

		String operatingSystem = System.getProperty("os.name");
		if (operatingSystem == null) {
			operatingSystem = VALUE_UNKNOWN;
		} else {
			String osVersion = System.getProperty("os.version");
			if (osVersion != null) {
				operatingSystem = operatingSystem + " " + osVersion;
			}
		}
		return operatingSystem;
	}

	/**
	 * {@inheritDoc}
	 */
	public Date getServerBootTime() throws DIException {
		// everyone is allowed to execute this method

		return new Date(RS.gRS.mmServerStarted);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getServerID() {
		// everyone is allowed to execute this method

		String id = System.getProperty("com.ibm.di.server.id");

		if (id != null && id.trim().length() > 0) {
			return id;
		}
		return "";
	}

	// Connectors information

	private String[] getConnectorNames() throws Exception {
		List<String> list = new LinkedList<String>();
		synchronized (this) {
			for (Enumeration<Binding> e = getSysConfig().list(MetamergeConfig.DEFAULT_CONNECTOR_FOLDER); e.hasMoreElements();) {
				Binding b = e.nextElement();

				// Copy of the summary of the discussion about this decision
				// with both Ashlesha and Bjron,

				// It turns out that there is discrepancy between what
				// getInstalledConnectors() returns and what
				// getInstalledComponentDescriptor expects to receive. The first
				// one would return the connector name, e.g.
				// system:/Connectors/ibmdi.LogConnector. Which is the default
				// configuration for a LogConnector.

				// However in order for the second method to return successfully
				// it must have a default FormConfig to correspond to a name
				// from the system namespace (e.g.
				// system:/Forms/com.ibm.di.connector.LogConnector).

				// For this particular ConnectorConfig there is no default
				// FormConfig corresponding. In fact there are several other
				// FormConfigs (e.g, system:/Forms/ibmdi.ConsoleAppender,
				// system:/Forms/ibmdi.Log4jAppender, etc.) which are indirectly
				// referred to by the derivatives of the default LogConnector
				// config object, which are not defined in the system namespace.

				// Here is why we either need to look at those indirectly
				// referenced FormConfigs as separate components (provide
				// separate ConnectorConfg for each of them) and return their
				// descriptors instead or create a default FormConfig (which
				// would not be that useful as it cannot be mapped to all
				// derivative FormConfigs at once) for the base one to get
				// exposed.

				// Bottom line is: Its getting harder and harder for me to
				// provide an unified interface of components which are not
				// designed to play nice together, so until we clean up our
				// design I am removing this connector from being exposed
				// through any API.
				if (!b.getName().endsWith("ibmdi.LogConnector")) {
					list.add(b.getName());
				}
			}
		}
		return list.toArray(new String[list.size()]);
	}

	/**
	 * {@inheritDoc}
	 */
	public Hashtable<?, ?>[] getInstalledConnectors() throws DIException {
		if (!mSession.getIdentity().isAdmin()) {
			throw new AuthorizationException();
		}

		Hashtable<?, ?>[] connectors = null;
		String[] connNames = null;
		try {
			connNames = getConnectorNames();
		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.COULD.NOT.RETRIEVE.CONNECTORS.NAMES"), e);
		}

		connectors = new Hashtable[connNames.length];
		for (int i = 0; i < connNames.length; i++) {
			Hashtable<String, String> connData = new Hashtable<String, String>();

			String connName = connNames[i];
			connData.put(PROP_NAME, connName);

			try {
				String description = getConnectorDescription(connName);
				if (description != null) {
					connData.put(PROP_DESCRIPTION, description);
				}
			} catch (DIException e) {
				APIEngine.logInfo(sResHash.getString("SEVER.API.COULD.NOT.RETRIEVE.CONN.DESCRIPTION", new Object[] { connName,
						e.toString() }));
			}

			try {
				String versionInfo = getConnectorVersionInfo(connName);
				if (versionInfo != null) {
					connData.put(PROP_VERSION_INFO, versionInfo);
				}
			} catch (DIException e) {
				APIEngine.logInfo(sResHash.getString("SEVER.API.COULD.NOT.RETRIEVE.CONN.VERSION", new Object[] { connName,
						e.toString() }));
			}

			connectors[i] = connData;
		}

		return connectors;
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getInstalledConnectorsNames() throws DIException {
		if (!mSession.getIdentity().isAdmin()) {
			throw new AuthorizationException();
		}

		String[] connNames = null;
		try {
			connNames = getConnectorNames();
		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.COULD.NOT.RETRIEVE.CONNECTORS.NAMES.1"), e);
		}
		return connNames;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getConnectorDescription(String aConnectorName) throws DIException {
		if (!mSession.getIdentity().isAdmin()) {
			throw new AuthorizationException();
		}

		MetamergeConfig sysCfg = getSysConfig();
		String description = null;
		try {
			ConnectorConfig connCfg = sysCfg.getConnector(aConnectorName);
			description = connCfg.getStringParameter(CFG_PROP_DESCRIPTION);
		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.COULD.NOT.RETRIEVE.DESCRIPTION.FOR.CONNECTOR.NAME.1",
					aConnectorName), e);
		}
		return description;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getConnectorVersionInfo(String aConnectorName) throws DIException {
		if (!mSession.getIdentity().isAdmin()) {
			throw new AuthorizationException();
		}

		MetamergeConfig sysCfg = getSysConfig();
		String versionInfo = null;
		String className = null;
		try {
			RawConnectorConfig rawConnCfg = sysCfg.getConnector(aConnectorName).getConnectionConfig();
			className = rawConnCfg.getJavaClass();
		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.COULD.NOT.RETRIEVE.CLASS.NAME.FOR.CONNECTOR.NAME.2",
					aConnectorName), e);
		}

		if (className != null) {
			versionInfo = getComponentVersionInfo(className,aConnectorName);
		}

		return versionInfo;
	}

	// Parsers information

	/**
	 * {@inheritDoc}
	 */
	public Hashtable<?, ?>[] getInstalledParsers() throws DIException {
		if (!mSession.getIdentity().isAdmin()) {
			throw new AuthorizationException();
		}

		MetamergeConfig sysCfg = getSysConfig();
		Hashtable<?, ?>[] parsers = null;
		String[] parserNames = null;
		try {
			parserNames = sysCfg.getDefaultFolder(MetamergeConfig.PARSER_FOLDER).getNames();
		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.COULD.NOT.RETRIEVE.PARSERS.NAMES"), e);
		}

		parsers = new Hashtable[parserNames.length];
		for (int i = 0; i < parserNames.length; i++) {
			Hashtable<String, String> parserData = new Hashtable<String, String>();

			String parserName = parserNames[i];
			parserData.put(PROP_NAME, parserName);

			try {
				String description = getParserDescription(parserName);
				if (description != null) {
					parserData.put(PROP_DESCRIPTION, description);
				}
			} catch (DIException e) {
				APIEngine.logInfo(sResHash.getString("SEVER.API.COULD.NOT.RETRIEVE.PARSER.DESCRIPTION.2", new Object[] {
						parserName, e.toString() }));
			}

			try {
				String versionInfo = getParserVersionInfo(parserName);
				if (versionInfo != null) {
					parserData.put(PROP_VERSION_INFO, versionInfo);
				}
			} catch (DIException e) {
				APIEngine.logInfo(sResHash.getString("SEVER.API.COULD.NOT.RETRIEVE.PARSER.VERSION", new Object[] { parserName,
						e.toString() }));
			}
			parsers[i] = parserData;
		}

		return parsers;
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getInstalledParsersNames() throws DIException {
		if (!mSession.getIdentity().isAdmin()) {
			throw new AuthorizationException();
		}

		MetamergeConfig sysCfg = getSysConfig();
		String[] parserNames = null;
		try {
			parserNames = sysCfg.getDefaultFolder(MetamergeConfig.PARSER_FOLDER).getNames();
		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.COULD.NOT.RETRIEVE.PARSERS.NAMES.1"), e);
		}
		return parserNames;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getParserDescription(String aParserName) throws DIException {
		if (!mSession.getIdentity().isAdmin()) {
			throw new AuthorizationException();
		}

		MetamergeConfig sysCfg = getSysConfig();
		String description = null;
		try {
			ParserConfig parserCfg = sysCfg.getParser(aParserName);
			description = parserCfg.getStringParameter(CFG_PROP_DESCRIPTION);
		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.COULD.NOT.RETRIEVE.DESCRIPTION.FOR.PARSER",
					aParserName), e);
		}
		return description;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getParserVersionInfo(String aParserName) throws DIException {
		if (!mSession.getIdentity().isAdmin()) {
			throw new AuthorizationException();
		}

		MetamergeConfig sysCfg = getSysConfig();
		String versionInfo = null;
		String className = null;
		try {
			ParserConfig parserCfg = sysCfg.getParser(aParserName);
			className = parserCfg.getJavaClass();
		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.COULD.NOT.RETRIEVE.CLASS.NAME.FOR.PARSER",
					aParserName), e);
		}

		if (className != null) {
			versionInfo = getComponentVersionInfo(className, null);
		}

		return versionInfo;
	}

	// Function Components information

	/**
	 * {@inheritDoc}
	 */
	public Hashtable<?, ?>[] getInstalledFunctionComponents() throws DIException {
		if (!mSession.getIdentity().isAdmin()) {
			throw new AuthorizationException();
		}

		MetamergeConfig sysCfg = getSysConfig();
		Hashtable<?, ?>[] funcs = null;
		String[] funcNames = null;
		try {
			funcNames = sysCfg.getDefaultFolder(MetamergeConfig.FUNCTION_FOLDER).getNames();
		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.COULD.NOT.RETRIEVE.FUNCTION.COMPONENTS.NAMES.1"), e);
		}

		funcs = new Hashtable[funcNames.length];
		for (int i = 0; i < funcNames.length; i++) {
			Hashtable<String, String> funcData = new Hashtable<String, String>();

			String parserName = funcNames[i];
			funcData.put(PROP_NAME, parserName);

			try {
				String description = getFunctionComponentDescription(parserName);
				if (description != null) {
					funcData.put(PROP_DESCRIPTION, description);
				}
			} catch (DIException e) {
				APIEngine.logInfo(sResHash.getString("SEVER.API.COULD.NOT.RETRIEVE.PARSER.DESCRIPTION.1", new Object[] {
						parserName, e.toString() }));
			}

			try {
				String versionInfo = getFunctionComponentVersionInfo(parserName);
				if (versionInfo != null) {
					funcData.put(PROP_VERSION_INFO, versionInfo);
				}
			} catch (DIException e) {
				APIEngine.logInfo(sResHash.getString("SEVER.API.COULD.NOT.RETRIEVE.PARSER.VERSION.1", new Object[] { parserName,
						e.toString() }));
			}
			funcs[i] = funcData;
		}

		return funcs;
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getInstalledFunctionComponentsNames() throws DIException {
		if (!mSession.getIdentity().isAdmin()) {
			throw new AuthorizationException();
		}

		MetamergeConfig sysCfg = getSysConfig();
		String[] funcNames = null;
		try {
			funcNames = sysCfg.getDefaultFolder(MetamergeConfig.FUNCTION_FOLDER).getNames();
		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.COULD.NOT.RETRIEVE.FUNCTION.COMPONENTS.NAMES.2"), e);
		}
		return funcNames;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getFunctionComponentDescription(String aFunctionComponentName) throws DIException {
		if (!mSession.getIdentity().isAdmin()) {
			throw new AuthorizationException();
		}

		MetamergeConfig sysCfg = getSysConfig();
		String description = null;
		try {
			FunctionConfig funcCfg = sysCfg.getFunction(aFunctionComponentName);
			description = funcCfg.getStringParameter(CFG_PROP_DESCRIPTION);
		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash.getString(
					"SEVER.API.COULD.NOT.RETRIEVE.DESCRIPTION.FOR.FUNCTION.COMPONENT", aFunctionComponentName), e);
		}
		return description;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getFunctionComponentVersionInfo(String aFunctionComponentName) throws DIException {
		if (!mSession.getIdentity().isAdmin()) {
			throw new AuthorizationException();
		}

		MetamergeConfig sysCfg = getSysConfig();
		String versionInfo = null;
		String className = null;
		try {
			FunctionConfig funcCfg = sysCfg.getFunction(aFunctionComponentName);
			className = funcCfg.getJavaClass();
		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash.getString(
					"SEVER.API.COULD.NOT.RETRIEVE.CLASS.NAME.FOR.FUNCTION.COMPONENT", aFunctionComponentName), e);
		}

		if (className != null) {
			versionInfo = getComponentVersionInfo(className, null);
		}

		return versionInfo;
	}

	// ***************************************
	// PRIVATE METHODS
	// ***************************************

	/**
	 * Retrieves the local IP address.
	 * 
	 * @return local IP address.
	 */
	private String getLocalIPAddress() {
		String inetAddress;
		try {
			inetAddress = InetAddress.getLocalHost().getHostAddress();
			if (inetAddress != null && inetAddress.indexOf(':') > -1) {
				inetAddress = inetAddress.replace(':', '_');
			}
		} catch (UnknownHostException e) {
			inetAddress = VALUE_UNKNOWN;
		}
		return inetAddress;
	}

	/**
	 * Retrieves the templates configuration file loaded from the rs.jar file .
	 * 
	 * @return MetamergeConfig
	 */
	private MetamergeConfig getSysConfig() {
		return RS.gSysConfig;
	}

	/**
	 * Retrieves Component's version information
	 * 
	 * @param aComponentClass
	 *            class of the component
	 * @return String
	 * @throws DIException
	 */
	private String getComponentVersionInfo(String aComponentClass, String connectorName) throws DIException {
		if (aComponentClass == null) {
			return null;
		}

		String versionInfo = null;
		try {
			if (connectorName != null) {
				ConnectorInterface conn = (ConnectorInterface) Class.forName(aComponentClass).newInstance();
				// in the case of osgi based connector the instance will be a ConnectorDelegate 
				// instance which requires a connector name (for ex: ibmdi.TADDMConnector) 
				// to get the version of the connector. 
				if (conn instanceof ConnectorDelegate) {
					ConnectorDelegate connDel = (ConnectorDelegate) Class.forName(aComponentClass).newInstance();
					connDel.setId(connectorName);
					versionInfo = connDel.getVersion();
					return versionInfo;
				}
			}
			VersionInfoInterface component = (VersionInfoInterface) Class.forName(aComponentClass).newInstance();
			versionInfo = component.getVersion();
		} catch (ClassNotFoundException e) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.COMPONENT.CLASS.NOT.FOUND", aComponentClass), e);
		} catch (IllegalAccessException e) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.ACCESS.ERROR.ON.LOADING.CLASS", aComponentClass), e);
		} catch (InstantiationException e) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.COULD.NOT.INSTANTIATE.CLASS", aComponentClass), e);
		} catch (Throwable e) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.COULD.NOT.RETRIEVE.VERSION.INFO.FOR.CLASS",
					aComponentClass), e);
		}

		return versionInfo;
	}

	/**
	 * {@inheritDoc}
	 */
	public Vector<String> getPasswordParameterNames(String aJavaClassName) throws DIException {
		Vector<String> passwordParameters = new Vector<String>();
		BaseConfiguration baseConfiguration = null;
		try {
			baseConfiguration = (BaseConfiguration) MetamergeConfigFactory.lookup(null, "system:/Forms/" + aJavaClassName);
		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash.getString(
					"SEVER.API.COULD.NOT.RETRIEVE.PASSWORD.PARAMETER.NAMES.FOR.CLASS", aJavaClassName), e);
		}
		if (baseConfiguration != null) {
			Vector vector = (Vector) baseConfiguration.getParameter("parameterlist");
			TreeMap treemap = (TreeMap) baseConfiguration.getParameter("parameter");
			for (int i = 0; i < vector.size(); i++) {
				String parameterName = (String) vector.elementAt(i);
				TreeMap paramInfo = (TreeMap) treemap.get(parameterName);
				if (paramInfo != null) {
					String syntax = (String) paramInfo.get("syntax");
					if (syntax != null && syntax.equalsIgnoreCase("password")) {
						passwordParameters.add(parameterName);
					}
				}
			}
		}

		return passwordParameters;
	}

	/**
	 * {@inheritDoc}
	 */
	public ComponentDescriptor getInstalledComponentDescriptor(String componentName) throws DIException {
		// For LogConnector check out the big comment in getConnectorNames()
		if (componentName != null && componentName.endsWith("ibmdi.LogConnector")) {
			throw new DIException(new NameNotFoundException(componentName));
		}
		
		
		try {
			BaseConfiguration cfg;
			MetamergeConfig sysCfg = getSysConfig();

			// quick check for exact name:
			try {
				cfg = (BaseConfiguration) sysCfg.lookup(componentName);
			} catch (NameNotFoundException nfe) {
				cfg = null;
			}
			
			//
			// -- Try non-system namespace
			// 
			//	namespace:name -> namespace:/Connectors/name
			//
			if(cfg == null) {
				int colon = componentName.indexOf(":");
				if(colon > 0) {
					String ns = componentName.substring(0, colon);
					String path = APIEngine.getConfigurationRegistry().getConfigFilePath(ns);
					String comp = componentName.substring(colon+1);
					MetamergeConfig mc = MetamergeConfigFactory.getNamespace(path);
					boolean didload = false;
					//
					// -- Make sure namespace is loaded
					if(mc == null) {
						try {
							mc = MetamergeConfigFactory.loadNamespace(path);
							didload = true;
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					
					String[] names = new String[] { "Connectors", "Parsers", "Functions"};
					for(String name : names) {
						try {
							cfg = (BaseConfiguration) mc.lookup(name + "/" + comp);
							break;
						} catch (Exception e) {
							cfg = null;
						}
					}
					
					if(didload)
						MetamergeConfigFactory.removeNamespace(path);
				}
			}
			

			// 
			// -- Try system namespace
			//
			if (cfg == null) {
				String[] names = new String[] { "system:/Connectors/" + componentName, "system:/Functions/" + componentName,
						"system:/Parsers/" + componentName };

				for (String name : names) {
					try {
						cfg = (BaseConfiguration) sysCfg.lookup(name);
						componentName = name;
						break;
					} catch (NameNotFoundException nfe) {
						SystemFunctions.doNothing();
					}
				}
			}

			ComponentDescriptor desc = null;
			if (cfg instanceof FunctionConfig) {
				desc = DescriptorUtils.getFunctionComponentDescriptor(componentName, (FunctionConfig) cfg);
			} else if (cfg instanceof ConnectorConfig) {
				desc = DescriptorUtils.getConnectorDescriptor(componentName, (ConnectorConfig) cfg);
			} else if (cfg instanceof ParserConfig) {
				desc = DescriptorUtils.getParserDescriptor(componentName, (ParserConfig) cfg);
			}

			if (desc != null) {
				return desc;
			}

			throw new NameNotFoundException(componentName);
		} catch (Throwable error) {
			error.printStackTrace();
			throw new DIException(error.toString());
		}
	}
}
