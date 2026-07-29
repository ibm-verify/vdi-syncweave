/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.util;

import java.util.Enumeration;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.ParserConfig;
import com.ibm.di.loader.IDILoader;
import com.ibm.di.server.Log;
import com.ibm.di.server.Version;

/**
 * @author Administrator
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class VersionInformation {

	private static Log logger = new Log("miserver", "server");

	private VersionInformation() {
	}

	/**
	 * getVesrionNumbers() iterators through all the installed components i.e
	 * Connectors, Parsers and Event Handlers and prints their version numbers
	 * 
	 * @return VersionInfo: String
	 */
	public static String getVersionNumbers() throws Exception {
		
		Enumeration eConnectors;
		Enumeration eParsers;
		Enumeration eFCs;
		try {
			eConnectors = MetamergeConfigFactory.getNamespace(
					MetamergeConfigFactory.SYSTEM_NAMESPACE).getDefaultFolder(
					MetamergeConfig.CONNECTOR_FOLDER).list();
			eParsers = MetamergeConfigFactory.getNamespace(
					MetamergeConfigFactory.SYSTEM_NAMESPACE).getDefaultFolder(
					MetamergeConfig.PARSER_FOLDER).list();
			eFCs = MetamergeConfigFactory.getNamespace(
					MetamergeConfigFactory.SYSTEM_NAMESPACE).getDefaultFolder(
					MetamergeConfig.FUNCTION_FOLDER).list();
		} catch (Exception e) {
			throw new Exception(logger
					.getString("header.error_on_version_numbers"));
		}
		
		StringBuffer versionInfo = new StringBuffer();
		
		versionInfo.append(logger.getString("header.version", Version
				.version()));
		versionInfo.append("\n");
		versionInfo.append(logger.getString("header.os_name", System
				.getProperty("os.name")));
		versionInfo.append("\n");
		
		String javaVersion = System.getProperty("java.vm.info");
		if (javaVersion == null)
			javaVersion = System.getProperty("java.version") + " (" + System.getProperty("java.vm.version") + ")";
		else if (javaVersion.indexOf('\n') > 0)
			javaVersion = javaVersion.substring(0, javaVersion.indexOf('\n')).trim();

		versionInfo.append(logger.getString("header.runtime", System.getProperty("java.vm.vendor"), javaVersion));
		versionInfo.append("\n");
		versionInfo.append(logger.getString("header.library", System
				.getProperty("sun.boot.library.path")));
		versionInfo.append("\n");
		versionInfo.append(logger.getString("header.workdir", System
				.getProperty("user.dir")));
		versionInfo.append("\n");

		// -- Connector Version
		String info = null;
		String javaClassName = null;
		while (eConnectors.hasMoreElements()) {
			javax.naming.Binding bObj = (javax.naming.Binding) eConnectors
					.nextElement();
			if (!(bObj.getObject() instanceof ConnectorConfig)) {
				// -- Not a Connector
				continue;
			}

			try {
				javaClassName = ((ConnectorConfig) bObj.getObject())
						.getConnectionConfig().getJavaClass();
				if (javaClassName != null && javaClassName.length() > 0) {
					info = ((com.ibm.di.connector.Connector) Class.forName(
							javaClassName).newInstance()).getVersion();
					versionInfo.append(bObj.getName());
					versionInfo.append(": ");
					versionInfo.append(javaClassName);
					versionInfo.append(": ");
					versionInfo.append(IDILoader
							.getModificationDate(javaClassName));
					versionInfo.append(": ");
					versionInfo.append(info);
					versionInfo.append("\n");
				}
			} catch (Throwable t) {
				versionInfo.append(bObj.getName());
				versionInfo.append(": ");
				versionInfo.append(javaClassName);
				versionInfo.append(": ");
				versionInfo.append(IDILoader
						.getModificationDate(javaClassName));
				versionInfo.append(": ");
				versionInfo
						.append(logger
								.getString("header.error_no_version_information_for_connector"));
				versionInfo.append("\n");
			}
		}

		// -- Parser Version
		while (eParsers.hasMoreElements()) {
			javax.naming.Binding bObj = (javax.naming.Binding) eParsers
					.nextElement();
			if (!(bObj.getObject() instanceof ParserConfig)) {
				// --Not a Parser
				continue;
			}

			try {
				javaClassName = ((ParserConfig) bObj.getObject())
						.getJavaClass();
				if (javaClassName != null && javaClassName.length() > 0) {
					info = ((com.ibm.di.parser.ParserImpl) Class.forName(
							javaClassName).newInstance()).getVersion();
					versionInfo.append(bObj.getName());
					versionInfo.append(": ");
					versionInfo.append(javaClassName);
					versionInfo.append(": ");
					versionInfo.append(IDILoader
							.getModificationDate(javaClassName));
					versionInfo.append(": ");
					versionInfo.append(info);
					versionInfo.append("\n");
				}
			} catch (Throwable t) {
				versionInfo.append(bObj.getName());
				versionInfo.append(": ");
				versionInfo.append(javaClassName);
				versionInfo.append(": ");
				versionInfo.append(IDILoader
						.getModificationDate(javaClassName));
				versionInfo.append(": ");
				versionInfo
						.append(logger
								.getString("header.error_no_version_information_for_parser"));
				versionInfo.append("\n");
			}

		}

		// -- Function Components
		while (eFCs.hasMoreElements()) {
			javax.naming.Binding bObj = (javax.naming.Binding) eFCs
					.nextElement();
			if (!(bObj.getObject() instanceof FunctionConfig)) {
				// -- Not a FC
				continue;
			}

			try {
				javaClassName = ((FunctionConfig) bObj.getObject())
						.getJavaClass();
				if (javaClassName != null && javaClassName.length() > 0) {
					info = ((com.ibm.di.fc.Function) Class.forName(
							javaClassName).newInstance()).getVersion();
					versionInfo.append(bObj.getName());
					versionInfo.append(": ");
					versionInfo.append(javaClassName);
					versionInfo.append(": ");
					versionInfo.append(IDILoader
							.getModificationDate(javaClassName));
					versionInfo.append(": ");
					versionInfo.append(info);
					versionInfo.append("\n");
				}
			} catch (Throwable t) {
				versionInfo.append(bObj.getName());
				versionInfo.append(": ");
				versionInfo.append(javaClassName);
				versionInfo.append(": ");
				versionInfo.append(IDILoader
						.getModificationDate(javaClassName));
				versionInfo.append(": ");
				versionInfo
						.append(logger
								.getString("header.error_no_version_information_for_fc"));
				versionInfo.append("\n");
			}
		}

		return versionInfo.toString();
	}

}
