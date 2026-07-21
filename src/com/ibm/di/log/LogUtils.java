/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.log;

import java.lang.ref.WeakReference;
import java.util.*;

import com.ibm.di.api.syslog.SystemLogAppender;
import com.ibm.di.config.base.LogConfigItemImpl;
import com.ibm.di.config.interfaces.*;
import com.ibm.di.server.*;

public class LogUtils {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public static String[] LEVELS = new String[] { "OFF", "INFO", "WARN",
		"ERROR", "FATAL", "DEBUG", "ALL" };

	public static final String LOG_LEVEL = "com.ibm.di.log.level";
	public static final String LAYOUT = "com.ibm.di.log.layout";
	public static final String APPENDER = "com.ibm.di.log.appender";
	public static final String LOGGING_INTERFACE = "com.ibm.di.log.interface";

	public static final String CATEGORY_BASED = "categoryBased";
	public static final String CATEGORY_NAME = "categoryName";

	private static final String ASSEMBLY_LINE = "AssemblyLine";
	private static final String SYSTEM_LOG = "SystemLog";

	private static Map<RSInterface, WeakReference<LogConfigItem>> serverSLOG = new WeakHashMap<RSInterface, WeakReference<LogConfigItem>>();

	public static synchronized boolean addLoggers(String type, String name,
			Log log, LogConfig config, RSInterface configInstance)
	throws Exception {

		HashMap<String, Object> map = new HashMap<String, Object> ();
		map.put(LogInterface.TYPE, type);
		map.put(LogInterface.NAME, name);
		map.put(LogInterface.CONFIG_INSTANCE, configInstance);
		map.put(LogInterface.TIME, "" + System.currentTimeMillis());

		boolean didAdd = false;
		boolean addedSystemLog = false;
		List<LogConfigItem> list = new ArrayList<LogConfigItem> (config.getItems());

		for (LogConfigItem lci: list) {

			if (lci.getEnabled()) {

				// Special code for System Log Appender
				String strAppender = lci.getStringParameter(APPENDER);
				if ( SYSTEM_LOG.equals( strAppender ) ) {
					if (type == null)
						continue;
					addedSystemLog = true;
					if ( type.length() == 0 ) { // RS
						if (configInstance != null)
							serverSLOG.put(configInstance, new WeakReference<LogConfigItem>(lci));
					} else if (ASSEMBLY_LINE.equals(type)){
						addSystemLogAppender(log, lci, map);
					}
					continue;
				}

				LogInterface logger = log.getClassLogger(lci.getStringParameter(LOGGING_INTERFACE));

				if ( lci.getBooleanParameter(CATEGORY_BASED, false) )
					logger.setCategory(lci.getStringParameter(CATEGORY_NAME));
				else
					logger.addAppender(lci, map);

				didAdd = true;
			}
		}

		// Add System logger if specified for the configInstance
		if (!addedSystemLog && ASSEMBLY_LINE.equals(type) ) {
			LogConfigItem lci = getSLOG( configInstance, config.getMetamergeConfig() );
			if ( lci != null ) {
				addSystemLogAppender(log, lci, map);
				didAdd = true;
			}
		}

		return didAdd;
	}

	private static void addSystemLogAppender(Log log, LogConfigItem lci, Map<String, Object> map) throws Exception {

		if (! ASSEMBLY_LINE.equals(map.get(LogInterface.TYPE)))
			return;

		String name = (String) map.get(LogInterface.NAME);
		if (name == null || name.length() == 0)
			return;

		TDILog4j logger = new TDILog4j();
		
		SystemLogAppender appender = logger.addSystemLogAppender(lci, map);
		if (appender != null) {
			log.addLogger(logger);
			log.setSystemLogAppender(appender);
		}
	}

	private static LogConfigItem getSLOG(RSInterface rs, MetamergeConfig mc) {
		if ( rs != null ) {
			WeakReference<LogConfigItem> w = serverSLOG.get(rs);
			if (w != null && w.get() != null)
				return w.get();
		}
		if (! Boolean.getBoolean("SystemLog.defaultCreateLog"))
			return null;

		LogConfigItem lci = new LogConfigItemImpl();
		try {
			lci.setName("SystemLogAppender");
			lci.setMetamergeConfig(mc);
			lci.updateInheritsFrom("system:/Loggers/ibmdi.SystemLogAppender");
		} catch (Exception e) {
			return null;
		}
		lci.setLogEnabled(true);
		lci.setLogLevel(System.getProperty("SystemLog.defaultLevel", "INFO"));
		String pattern = System.getProperty("SystemLog.defaultLogPattern");
		if (pattern != null && pattern.length() > 0)
			lci.setParameter("SystemLog.LogPattern", pattern);
		return lci;
	}
}
