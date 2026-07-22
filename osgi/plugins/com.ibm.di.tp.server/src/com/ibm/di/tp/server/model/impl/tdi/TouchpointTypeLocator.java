/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.tp.server.model.impl.tdi;

import java.io.File;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.ibm.di.api.DIException;
import com.ibm.di.function.UserFunctions;
import com.ibm.di.tp.server.ServerActivator;
import com.ibm.di.tp.server.config.TPServerConfig;
import com.ibm.di.tp.server.context.TPServerContext;
import com.ibm.di.tp.server.model.TouchpointType;

/**
 * This class is responsible for finding and creating the {@link TouchpointType}
 * s <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public class TouchpointTypeLocator {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	public static final String SCHEME_SYSTEM = TouchpointTypeScheme.SYSTEM + ":";

	public static final String SCHEME_FILE = TouchpointTypeScheme.FILE + ":";

	public static final String SCHEME_VIRTUAL = TouchpointTypeScheme.VIRTUAL + ":";

	public static final String TYPE_VIRTUAL_INTERMEDIARY = SCHEME_VIRTUAL + "//Intermediary";

	private final File baseTemplate;

	/**
	 * This is the directory where custom configuration files are placed. These
	 * configuration files are touchpoint assemblylines with (currently) no
	 * parameters.
	 */
	private final File customTemplatesDir;

	private final Map<String, TouchpointType> stdTypesCache = new ConcurrentHashMap<String, TouchpointType>();

	private final Map<String, TouchpointType> nonStdTypesCache = new HashMap<String, TouchpointType>();

	private final ConnectivityProviderImpl cp;

	/**
	 * @param ctx
	 * @param cp
	 * @param cfgLoader
	 * @param connectivityProviderImpl
	 * @throws DIException
	 */
	public TouchpointTypeLocator(TPServerContext ctx, ConnectivityProviderImpl cp) throws DIException {
		this.cp = cp;
		TPServerConfig tpCfg = (TPServerConfig) ctx.getAttribute(TPServerConfig.class.getCanonicalName());

		if (tpCfg.getTemplateConfig().getBaseTemplate() == null
				|| !(baseTemplate = new File(tpCfg.getTemplateConfig().getBaseTemplate())).exists()) {
			throw new DIException(ServerActivator.L10N.getString("TP.SERVER.CONFIG.MISSING.PARAMETER", "baseTemplate"));
		}

		File tempDir = tpCfg.getTemplateConfig().getCustomTemplatesDir() != null ? new File(tpCfg.getTemplateConfig()
				.getCustomTemplatesDir()) : null;
		this.customTemplatesDir = tempDir != null && tempDir.exists() ? tempDir.getAbsoluteFile() : null;

		// add virtual type IDs
		String id = TYPE_VIRTUAL_INTERMEDIARY;
		nonStdTypesCache.put(id, new TouchpointTypeImpl(id, cp));

		// add custom type IDs
		if (customTemplatesDir != null) {
			for (File file : customTemplatesDir.listFiles()) {
				if (file.isFile() && UserFunctions.endsWithIC(file.getName(), ".xml")) {
					id = SCHEME_FILE + file.getName().substring(0, file.getName().length() - 4);
					nonStdTypesCache.put(id, new TouchpointTypeImpl(id, cp));
				}
			}
		}
	}

	/**
	 * Returns the File for the configuration template to use for a specific
	 * type. If a file named <i>name</i>.xml exists in the custom template
	 * directory it is used. Otherwise the default {@link #baseTemplate}
	 * (TouchpointTemplate.xml) is used (even if <code>null</code> is provided
	 * for TP Type).
	 * 
	 * @param tt
	 *            the {@link TouchpointType} which to find the config file for
	 * @return the config file for the specified TP Type.
	 */
	public File getConfigTemplateForType(TouchpointType tt) {
		if (tt != null) {
			String typeId = tt.getId();
			if (typeId != null && typeId.startsWith(SCHEME_FILE)) {
				File file = new File(customTemplatesDir, typeId.substring(SCHEME_FILE.length()) + ".xml");
				if (file.exists()) {
					return file;
				}
			}
		}
		return baseTemplate;
	}

	public Collection<TouchpointType> getTypes() throws RemoteException, DIException {
		// get the list of connector types from the TDI Server

		String[] stdTypes = cp.getSession().getServerInfo().getInstalledConnectorsNames();

		// prefix all Connector names with the system namespace
		for (int i = 0; i < stdTypes.length; i++) {
			if (!stdTypes[i].startsWith(SCHEME_SYSTEM)) {
				stdTypes[i] = SCHEME_SYSTEM + "/Connectors/" + stdTypes[i];
			}
		}

		Set<String> outersection = new HashSet<String>(stdTypesCache.keySet());

		TouchpointType tt = null;
		for (String typeId : stdTypes) {
			tt = stdTypesCache.get(typeId);
			if (tt == null) {
				// missing type... add it
				tt = new TouchpointTypeImpl(typeId, cp);
				stdTypesCache.put(typeId, tt);
			} else {
				outersection.remove(typeId);
			}
		}

		if (outersection.size() > 0 || stdTypes.length == 0) {
			// some types are removed from the server... delete them
			for (String typeId : outersection) {
				stdTypesCache.remove(typeId);
			}
		}

		List<TouchpointType> result = new LinkedList<TouchpointType>(stdTypesCache.values());
		result.addAll(nonStdTypesCache.values());

		return result;
	}

	/**
	 * Returns the Connector inheritance reference represented by this TP type.
	 * If the reference is not in the system namespace <code>null</code> is
	 * returned.
	 * 
	 * @param tt
	 *            the Touchpoint type.
	 * @return the inheritance reference associated with this type.
	 */
	public String getInheritanceRefFromType(TouchpointType tt) {
		String inheritanceRef = null;

		if (TouchpointTypeScheme.fromType(tt) == TouchpointTypeScheme.SYSTEM) {
			inheritanceRef = tt.getId();
		}

		return inheritanceRef;
	}

}
