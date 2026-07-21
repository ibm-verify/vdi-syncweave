/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.migration.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ibm.di.migration.BaseMigrationUtility;
import com.ibm.di.migration.ChangeDescription;
import com.ibm.di.plugin.proxy.Proxy;

/**
 * This utility is used to migrate the pwsync.props file read by both the native
 * plugins and the JavaProxy - {@link Proxy}.
 * 
 * @since TDI 7.1
 */
public final class PluginMigrationUtility extends BaseMigrationUtility {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final String PLUGIN_CUSTOM_DATA_VALUE = "machine1";

	private static final String PLUGIN_CUSTOM_DATA_COMMENT = " Custom data that will be send with each password change.";

	private static final String PLUGIN_CUSTOM_DATA_COMMENT1 = " This string can be used to uniquely identify the machine or product that generates the changes (e.g. machine IP, application name and version).";

	private static final String PLUGIN_JAVA_LOG_FILE = "javaLogFile";
	
//	private static final String PLUGIN_RETRY_ATTEMPTS_COMMENT = " Number of Attempts to retry Proxy connection.";

	/**
	 * @param args
	 * @param log
	 */
	public PluginMigrationUtility(String[] args, Logger log) {
		super(args, log);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.migration.BaseMigrationUtility#interpretCommandLineOptions()
	 */
	@Override
	protected void interpretCommandLineOptions() {
		super.interpretCommandLineOptions();
		if (isVerboseMode()) {
			setLog(Logger.getLogger("com.ibm.di.migration.verbose"));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.migration.BaseMigrationUtility#defineChanges(java.util.Map)
	 */
	@Override
	protected List<ChangeDescription> defineChanges(Map<String, String> props) {
		List<ChangeDescription> changes = new ArrayList<ChangeDescription>();

		// Add customData property added since TDI 7.2
		// Below line will add the new property along with comments below PLUGIN_JAVA_LOG_FILE property in pwsync.props file
		changes.add(new ChangeDescription(Proxy.PROXY_CUSTOM_DATA, ChangeDescription.TYPE_ADD, PLUGIN_CUSTOM_DATA_VALUE,
				PLUGIN_JAVA_LOG_FILE, new String[] { PLUGIN_CUSTOM_DATA_COMMENT, PLUGIN_CUSTOM_DATA_COMMENT1 }, 1, 0));		
		//Below line will comment the newly added property.
		changes.add(new ChangeDescription(Proxy.PROXY_CUSTOM_DATA, ChangeDescription.TYPE_COMMENT));
		
		// Add ProxyRetryAttempts property added in SDI 72 : Refer RTC task 42406
		// Below line will add the new property along with comments below PLUGIN_JAVA_LOG_FILE property in pwsync.props file
//		changes.add(new ChangeDescription(Proxy.RETRY_ATTEMPTS, ChangeDescription.TYPE_ADD, Integer.toString(Proxy.DEFAULT_RETRY_ATTEMPTS),
//				PLUGIN_JAVA_LOG_FILE, new String[] { PLUGIN_RETRY_ATTEMPTS_COMMENT }, 1, 0));
		
		// Refer RTC Defect 42547
		String proxyExeDir = System.getProperty("user.dir");
		if(System.getProperty("os.name").toLowerCase().contains("win")){
			proxyExeDir = proxyExeDir.replace("\\", "\\\\") + "/bin/startProxy.bat";
		} else {
			proxyExeDir = proxyExeDir + "/bin/startProxy.sh";
		}
		//System.out.println("\n Proxy Exe Dir = "+proxyExeDir);
		changes.add(new ChangeDescription("proxyStartExe", ChangeDescription.TYPE_MODIFY, proxyExeDir,
				"", new String[] { "" }, 1, 0));
		
		
		return changes;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BaseMigrationUtility bmu = new PluginMigrationUtility(args, Logger.getLogger(PluginMigrationUtility.class));
		try {
			bmu.migrateFile();
		} catch (Exception e) {
			if (!bmu.isVerboseMode()) {
				System.err.println(e.getLocalizedMessage());
			}
			bmu.getLog().error(resHash.getString("MIGPWSYNC.MIGRATION.FAILED"), e);
			System.exit(1);
		}

		if (!bmu.isHelpRequested()) {
			bmu.getLog().info(resHash.getString("MIGPWSYNC.MIGRATION.SUCCESSFULL"));
		}
	}
}
