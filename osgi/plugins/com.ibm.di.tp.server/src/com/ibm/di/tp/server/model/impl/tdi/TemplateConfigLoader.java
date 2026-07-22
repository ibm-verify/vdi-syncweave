/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.tp.server.model.impl.tdi;

import java.io.File;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibm.di.api.remote.ConfigInstance;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.PropertyManager;
import com.ibm.di.config.interfaces.PropertyStoreConfig;
import com.ibm.di.config.interfaces.RawConnectorConfig;
import com.ibm.di.config.interfaces.TDIProperties;
import com.ibm.di.tp.server.Constants;
import com.ibm.di.tp.server.ServerActivator;
import com.ibm.di.tp.server.TPServerApplication;
import com.ibm.di.tp.server.model.TouchpointDestination;
import com.ibm.di.tp.server.model.TouchpointRole;
import com.ibm.di.tp.server.model.TouchpointType;
import com.ibm.di.tp.server.model.config.Property;
import com.ibm.di.tp.server.model.config.PropertySheet;
import com.ibm.di.tp.server.model.exception.SCMPException;
import com.ibm.di.tp.server.util.TDIUtils;

/**
 * At runtime the template config is loaded as the following structure:
 * 
 * <pre>
 * CI: ProviderServer_&lt;port&gt; AL: ProviderServer Conn: HttpServer
 * 
 * CI: &lt;tpTypeId&gt;_&lt;tpInstId&gt; AL: ProviderHandler Conn:
 * ServiceConnector
 * 
 * CI: &lt;tpTypeId&gt;_&lt;tpInstId&gt; AL: Initiator Conn: ServiceConnector
 * 
 * CI: &lt;virtualTypeId&gt;_&lt;tpInstId&gt; AL: Intermediary
 * 
 * </pre>
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public final class TemplateConfigLoader {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	/**
	 * The name of the AssemblyLine implementing the Provider Touchpoint
	 * variant.
	 */
	public static final String AL_PROVIDER_HANDLER = "ProviderHandler";

	/**
	 * The name of the AssemblyLine implementing the Initiator Touchpoint
	 * variant.
	 */
	public static final String AL_INITIATOR_HANDLER = "Initiator";

	/**
	 * The name of the AssemblyLine implementing the Intermediary Touchpoint.
	 */
	private static final String AL_INTERMEDIARY_HANDLER = "IntermediaryHandler";

	/**
	 * The name of the AssemblyLine implementing the request dispatching
	 * machinery.
	 */
	public static final String AL_PROVIDER_SERVER = "ProviderServer";

	/**
	 * The name of the Property Store used for Communicating with the Touchpoint
	 * AssemblyLines at runtime.
	 */
	static final String PROPSTORE_NAME = "MemoryProperties";

	/**
	 * This is the name of the HTTP Server Connector in the template
	 * AssemblyLine. It is responsible for receiving client requests and
	 * dispatching them to configured worker AL.
	 */
	private static final String CONN_PROVIDER_SERVER = "HttpServer";

	/**
	 * This is the name of the Service Connector in the library of the template
	 * configuration. <br>
	 * <b>Note:</b> All connectors that want to expose their functionality
	 * through the Touchpoint, MUST inherit from it
	 */
	public static final String CONN_SERVICE = "GenericServiceConnector";

	/**
	 * The inheritance reference for the custom Script Connector used for
	 * working with the {@link #PROPSTORE_NAME}.
	 */
	private static final String CONN_MEMORY_PROPERTIES = "/Connectors/MemoryPropertiesConnector";

	/**
	 * Disable configuration listeners, as they are needed only by the CE.
	 */
	static {
		MetamergeConfigFactory.setUseConfigListeners(false);
	}

	/**
	 * Returns the AssemblyLine name for the provided Touchpoint Role.
	 * 
	 * @param role
	 *            Touchpoint Role.
	 * 
	 * @return the name of the AL implementing the specified role.
	 */
	public String getAlNameForRole(TouchpointRole role) {
		switch (role) {
		case PROVIDER:
			return AL_PROVIDER_HANDLER;
		case INITIATOR:
			return AL_INITIATOR_HANDLER;
		case INTERMEDIARY:
			return AL_INTERMEDIARY_HANDLER;
		default:
			return null;
		}
	}

	/**
	 * Specifies whether the AL (determined using
	 * {@link #getAlNameForRole(TouchpointRole)}) should be started on the
	 * remove server after sending the configuration.
	 * 
	 * @param role
	 *            the TP role
	 * @return <code>true</code> if the role has been implemented by an "active"
	 *         AL (one that is started in order for the TP to become available),
	 *         <code>false</code> otherwise.
	 */
	public boolean isAlActiveForRole(TouchpointRole role) {
		return role == TouchpointRole.INITIATOR;
	}

	/**
	 * Creates the configuration with the Provider Server AssemblyLine that
	 * spawns other AssemblyLines to handle Touchpoint data requests.
	 * 
	 * @param templateFile
	 *            full path to the used Touchpoint Template file.
	 * @param serverPort
	 *            the port on which the Provider Server will listen.
	 * 
	 * @return the {@link MetamergeConfig} as string.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	public String getProviderServerConfig(File templateFile, int serverPort) throws Exception {
		// get the template AssemblyLine
		MetamergeConfig tmpl = MetamergeConfigFactory.loadNamespace(templateFile.getAbsolutePath());
		AssemblyLineConfig alc = (AssemblyLineConfig) tmpl.getAssemblyLine(AL_PROVIDER_SERVER);

		ConnectorConfig http = alc.getConnectorByName(CONN_PROVIDER_SERVER);
		if (http == null) {
			throw new Exception(ServerActivator.L10N.getString("TP.SERVER.CONFIG.MISSING.CONNECTOR.FROM.TOUCHPOINT.TEMPLATE",
					new Object[] { CONN_PROVIDER_SERVER, templateFile.getAbsolutePath() }));
		} else {
			// We are making a really small change on the main template and then
			// serializing. We need to make sure the value of the port stays
			// unchanged until the config is serialized, after that it is not
			// important what the value becomes.
			synchronized (this) {
				http.getConnectionConfig().setIntegerParameter("tcpPort", serverPort);
				return TDIUtils.configToString(tmpl);
			}
		}
	}

	/**
	 * Sends the provided collection of destinations to the Touchpoint
	 * represented by the provided Config Instance in Configuration specific
	 * way.
	 * 
	 * @param destinations
	 * @param ci
	 * @throws RemoteException
	 * @throws Exception
	 */
	public void sendDestinationsToTouchpoint(Collection<TouchpointDestination> destinations, ConfigInstance ci)
			throws RemoteException, Exception {
		com.ibm.di.api.remote.TDIProperties ps = ci.getTDIProperties();
		ps.setProperty(TemplateConfigLoader.PROPSTORE_NAME, "com.ibm.di.tp.destinations", ConnectivityProviderImpl
				.getConfigLoader().getDestionationsAsPropertyValue(destinations));
		ps.commit();
	}

	/**
	 * Returns a list containing the URLs of the provided
	 * {@link TouchpointDestination} objects. <br>
	 * <b>Note:</b> The URLs contained in the configuration of the
	 * {@link TouchpointDestination} objects should have already been encoded
	 * e.g. using {@link URLEncoder};
	 * 
	 * @param destinations
	 *            a collection of {@link TouchpointDestination}.
	 * @return a List of the Destination URLs.
	 * @throws SCMPException
	 *             if a problem occurs.
	 */
	private List<Map<String, String>> getDestionationsAsPropertyValue(Collection<TouchpointDestination> destinations)
			throws SCMPException {

		List<Map<String, String>> dests = new ArrayList<Map<String, String>>(destinations.size());
		Map<String, String> dest = null;
		for (TouchpointDestination destination : destinations) {
			dest = new HashMap<String, String>(2);
			dest.put("request-out", destination.getConfiguration().getDestination().getRequestOut());
			if (destination.getConfiguration().getDestination().getRequestError() != null) {
				dest.put("request-error", destination.getConfiguration().getDestination().getRequestError());
			}
			dests.add(dest);
		}
		return dests;
	}

	/**
	 * Creates a serialized {@link MetamergeConfig} object based on the provided
	 * template and the specified by the user TP configuration template.
	 * 
	 * @param templateFile
	 *            the configuration template that the TP instance will expand.
	 * @param connectorParams
	 *            the parameters provided for each of the Connectors in the
	 *            AssemblyLine as a PropertySheet.
	 * @param role
	 *            the role that this configuration will be used for.
	 * @param tt
	 *            the TP type of the TP instance.
	 * @param locator
	 *            the {@link TouchpointTypeLocator} object.
	 * @return a String representation of the config instance.
	 * 
	 * @throws Exception
	 *             thrown while working with the {@link MetamergeConfig} object.
	 */
	public String getTouchpointConfig(File templateFile, PropertySheet connectorParams, TouchpointRole role, TouchpointType tt,
			TouchpointTypeLocator locator) throws Exception {
		// get the template configuration
		MetamergeConfig mc = (MetamergeConfig) MetamergeConfigFactory.loadNamespace(templateFile.getAbsolutePath());
		mc = TDIUtils.cloneMetamergeConfig(mc);

		// configure the Service Connector
		ConnectorConfig serviceConnector = mc.getConnector(CONN_SERVICE);
		if (serviceConnector != null) {
			// only for system TP types set the Connector's inheritance
			String inheritanceRef = locator.getInheritanceRefFromType(tt);
			if (inheritanceRef != null) {
				serviceConnector.setInheritsFromRef(inheritanceRef);
			}

			for (Property property : connectorParams.getProperty()) {
				String propertyName = property.getPropertyName();
				List<String> values = property.getValue();

				// only the first value of this list is used because in TDI
				// there are no multi-valued parameters
				if (values != null && values.size() > 0 && values.get(0) != null) {
					if (!propertyName.startsWith(Constants.SYSTEM_PROPERTY_PREFIX)) {
						// the property is a normal Connector parameter
						serviceConnector.getConnectionConfig().setParameter(propertyName, values.get(0));
					} else if (propertyName.equals(Constants.PROP_INIT_MODE)) {
						// the property is for the Mode of the Connector
						String mode = values.get(0);
						if (mode != null) {
							serviceConnector.setMode(mode);
						}
					}
				}
			}
		}

		// Configure the Properties Store used for communicating to the running
		// Touchpoint AssemblyLine. Such communication is needed only for
		// Initiator and Intermediary roles.

		if (role == TouchpointRole.INITIATOR || role == TouchpointRole.INTERMEDIARY) {
			PropertyManager pm = (PropertyManager) mc.lookup(MetamergeConfig.DEFAULT_PROPSTORE_FOLDER);
			PropertyStoreConfig psc = pm.getPropertyStore(PROPSTORE_NAME);
			if (psc == null) {
				// create a new default property store
				pm.addStdStore(PROPSTORE_NAME);
				psc = pm.getPropertyStore(PROPSTORE_NAME);
				psc.setKeyAttribute(TDIProperties.KEY_ATTRIBUTE);
				psc.setValueAttribute(TDIProperties.VALUE_ATTRIBUTE);
				psc.setInitialLoad(false);

				// set the connector used by this store to
				// MemoryPropertiesConnector
				RawConnectorConfig conn = psc.getConnectionConfig();
				conn.setParent(psc);
				conn.setInheritsFromRef(CONN_MEMORY_PROPERTIES);
			}
		}

		return TDIUtils.configToString(mc);
	}

	/**
	 * Returns the inheritance reference of the Service Connector for the
	 * provided template file.<br>
	 * When determining the Connector type, the inheritance chain will be
	 * followed, until a system type is reached (e.g.
	 * system:/Connectors/ibmdi.Properties, etc).
	 * 
	 * @param templateFile
	 *            the configuration template that the TP instance will use.
	 * @return a String representation of the Connector's type or
	 *         <code>null</code> if nothing is found.
	 * @throws Exception
	 *             thrown while working with the {@link MetamergeConfig} object.
	 */
	public String getServiceConnectorInheritanceRef(File templateFile) throws Exception {
		MetamergeConfig mc = (MetamergeConfig) MetamergeConfigFactory.loadNamespace(templateFile.getAbsolutePath());

		ConnectorConfig connectorConfig = mc.getConnector(CONN_SERVICE);
		String connectorInheritanceRef = null;
		if (connectorConfig != null) {
			connectorInheritanceRef = searchConnectorInheritanceRef(connectorConfig);
		}

		return connectorInheritanceRef;
	}

	/**
	 * Reach recursively the parent inheritance type of the provided
	 * configuration.
	 * 
	 * @param connectorConfig
	 *            a Connector configuration.
	 * @return the Connector's inheritance type.
	 */
	private String searchConnectorInheritanceRef(BaseConfiguration connectorConfig) {
		String parentRef = connectorConfig.getInheritsFromRef();
		if (parentRef == null || parentRef.startsWith(MetamergeConfigFactory.SYSTEM_NAMESPACE)) {
			return parentRef;
		} else {
			BaseConfiguration parentConfig = connectorConfig.getInheritsFrom();
			return searchConnectorInheritanceRef(parentConfig);
		}
	}

	/**
	 * Returns a list of the Touchpoint roles supported for a given template
	 * config. It checks if the template provides the AL needed for each role
	 * and if so returns them.
	 * 
	 * @param templateFile
	 *            the configuration template that the TP instance will use.
	 * @return a list with the supported Touchpoint roles.
	 * @throws Exception
	 *             thrown while working with the {@link MetamergeConfig} object.
	 */
	public List<TouchpointRole> getSupportedRolesByTemplate(File templateFile) throws Exception {
		MetamergeConfig mc = (MetamergeConfig) MetamergeConfigFactory.loadNamespace(templateFile.getAbsolutePath());

		List<TouchpointRole> supportedRoles = new ArrayList<TouchpointRole>();
		for (TouchpointRole tpRole : TouchpointRole.values()) {
			String alName = getAlNameForRole(tpRole);
			try {
				AssemblyLineConfig alc = mc.getAssemblyLine(alName);
				if (alc != null) {
					supportedRoles.add(tpRole);
				}
			} catch (Exception ex) {
				TPServerApplication.getLog().debug(
						ServerActivator.L10N.getString("TP.SERVER.RESOURCE.NO.AL.FOR.TP.ROLE",
								new Object[] { tpRole.toString() }), ex);
			}
		}

		return supportedRoles;
	}

}
