/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
//
// SystemFunctions.java
//
//
//
package com.ibm.di.function;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import javax.naming.Binding;

import com.ibm.di.automation.COMProxy;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.LibraryConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.ParserConfig;
import com.ibm.di.config.interfaces.PropertyStoreConfig;
import com.ibm.di.config.interfaces.RawConnectorConfig;
import com.ibm.di.config.interfaces.ScriptConfig;
import com.ibm.di.connector.Connector;
import com.ibm.di.connector.ConnectorInterface;
import com.ibm.di.fc.FunctionInterface;
import com.ibm.di.parser.ParserInterface;
import com.ibm.di.script.ScriptEngine;
import com.ibm.di.server.Log;
import com.ibm.di.server.RS;
import com.ibm.di.server.RSInterface;
import com.ibm.di.server.RecordPlaybackInterface;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.SearchCriteria;
import com.ibm.icu.util.StringTokenizer;
import java.net.Socket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLServerSocket;

public class SystemFunctions {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final String PROPERTIES_FILE = "miserver";

	private static RSInterface main = null;

	private static ResourceHash sResHash = ResourceHash
			.getHash(PROPERTIES_FILE);

	SystemFunctions() {
	}

	public static void setServer(RSInterface server) {
		main = server;
	}

    /**
     * Returns the RS instance associated with the current ThreadGroup, or the dummy RSInterface object defined by the Config Editor.
     * Although this method is public, it is meant for internal use,.
     * The usual way to get the current RS instance would be to use the <code>main</code> object in JavaScript.
     * 
     * @return the {@link RS} instance or <code>null</code> if it couldn't be found, e.g. because the current Thread was not created by the TDI framework.
     */
	public static RSInterface getServer() {
		if (main != null)
			return main;
		else
			return RS.getServer();
	}

	public static ArrayList<String> split(String str, String sep) {
		StringTokenizer st = new StringTokenizer(str, sep);
		ArrayList<String> a = new ArrayList<String>();

		while (st.hasMoreTokens()) {
			a.add(st.nextToken());
		}
		return a;
	}

	public static ScriptConfig loadScript(String name) {
		return loadScript(name, getServer());
	}

	public static ScriptConfig loadScript(String name, RSInterface server) {
		return server.getScript(name);
	}

	public static AssemblyLineConfig loadAssemblyLine(String name) {
		return loadAssemblyLine(name, getServer());
	}

	public static AssemblyLineConfig loadAssemblyLine(String name,
			RSInterface server) {
		return server.getTask(name);
	}

	public static ParserInterface loadParser(String name) throws Exception {
		return loadParser(name, getServer());
	}

	public static ParserInterface loadParser(String name, RSInterface server)
			throws Exception {
		ParserConfig config = server.getParser(name);
		if (config == null)
			throw new Exception(sResHash.getString("SYSTEM.FUNCTIONS.CANNOTGET.CONFIG.ERROR", name));

		config = (ParserConfig) config.getClone();
		if (config.getMetamergeConfig() == RS.gSysConfig)
			config.setMetamergeConfig(server.getMetamergeConfig());

		return loadParser(config);
	}

	public static ParserInterface loadParser(ParserConfig config)
			throws Exception {
		String className = (String) config.getStringParameter("class");
		if (className == null) {
			throw new Exception(sResHash
					.getString("SYSTEM.FUNCTIONS.CANNOTFIND.CLASS.ERROR"));
		}

		Class<?> t1 = Class.forName(className);
		ParserInterface parser = (ParserInterface) t1.newInstance();

		parser.setConfiguration(config);
		return parser;
	}

	public static ConnectorInterface loadConnector(String name)
			throws Exception {
		return loadConnector(name, getServer());
	}

	public static ConnectorInterface loadConnector(String name,
			RSInterface server) throws Exception {
		ConnectorConfig cc = server.getConnector(name);
		if (cc == null)
			throw new Exception(sResHash.getString("SYSTEM.FUNCTIONS.CANNOTGET.CONNCONFIG.ERROR", name));

		cc = (ConnectorConfig) cc.getClone();
		if (cc.getMetamergeConfig() == RS.gSysConfig)
			cc.setMetamergeConfig(server.getMetamergeConfig());
		cc.setupInheritanceChain();

		return loadConnector(cc, null, server);
	}

	public static ConnectorInterface loadConnector(PropertyStoreConfig psc)
			throws Exception {
		com.ibm.di.config.base.ConnectorConfigImpl cc = new com.ibm.di.config.base.ConnectorConfigImpl();
		cc.setParent(psc);
		RawConnectorConfig rc = psc.getConnectionConfig();
		if (rc != null) {
			cc.setConnectionConfig(rc);
			rc.setParent(psc);
		}
		ParserConfig pc = psc.getParserConfig();
		if (pc != null) {
			cc.setParserConfig(pc);
			pc.setParent(psc);
		}

		return loadConnector(cc, null);
	}

	public static ConnectorInterface loadConnector(ConnectorConfig config)
			throws Exception {
		return loadConnector(config, null);
	}

	public static ConnectorInterface loadConnector(ConnectorConfig config,
			Object parentConfig) throws Exception {
		return loadConnector(config, parentConfig, getServer());
	}

	public static ConnectorInterface loadConnector(ConnectorConfig config,
			Object parentConfig, RSInterface server) throws Exception {
		ConnectorInterface conn = null;
		ConnectorConfig cfg = config;
		RawConnectorConfig cc = cfg.getConnectionConfig();

		String type = cc.getJavaClass();
		if (type == null) {
			throw new Exception(
					sResHash
							.getString("SYSTEM.FUNCTIONS.CANNOTFIND.CONNTYPEORCLASS.ERROR"));
		}

		if (type.indexOf(".") == -1) {
			if (type.startsWith("@")) {
				BaseConfiguration al = cfg.getParent();
				while (al != null) {
					if (al instanceof AssemblyLineConfig) {
						cfg = ((AssemblyLineConfig) al)
								.getConnectorByName(type.substring(1));
						if (cfg != null) {
							type = cfg.getConnectionConfig().getJavaClass();
							break;
						}
					}
					al = al.getParent();
				}
			} else {
				type = "com.ibm.di.connector." + type;
			}
		}

		if (type.equals("com.ibm.di.connector.ConsumerProducer")) {
			throw new Exception(sResHash
					.getString("SYSTEM.FUNCTIONS.DEPRECCONN.ERROR"));
		}

		Class<?> t1 = Class.forName(type);
		conn = (Connector) t1.newInstance();
		conn.setRSInterface(server);
		conn.setConfiguration(cfg);
		conn.setName(cfg.getShortName());

		if (Thread.currentThread() instanceof RecordPlaybackInterface) {
			String tname = Thread.currentThread().getName();
			RecordPlaybackInterface rpi = (RecordPlaybackInterface) Thread
					.currentThread();
			Object obj = null;
			if (rpi.isRecording(conn))
				obj = com.ibm.di.util.RecordAL.newInstance(tname, conn, rpi
						.getDatabase(), true);
			else if (rpi.isPlaying(conn))
				obj = com.ibm.di.util.RecordAL.newInstance(tname, conn, rpi
						.getDatabase(), false);
			else
				return conn;

			getServer().logmsg(
					sResHash.getString("SYSTEM.FUNCTIONS.CONNECTOR.INFO",
							new Object[] {
									config.getName(),
									tname,
									rpi.isRecording(conn) ? "recorded"
											: "played back" }));
			return (ConnectorInterface) obj;
		}

		return conn;
	}

	public static FunctionInterface loadFunction(String name) throws Exception {
		return loadFunction(name, getServer());
	}

	public static FunctionInterface loadFunction(String name, RSInterface server)
			throws Exception {
		FunctionConfig fc = server.getFunction(name);
		if (fc == null)
			throw new Exception(sResHash.getString("SYSTEM.FUNCTIONS.CANNOTGET.CONNCONFIG.ERROR", name));

		fc = (FunctionConfig) fc.getClone();
		if (fc.getMetamergeConfig() == RS.gSysConfig)
			fc.setMetamergeConfig(server.getMetamergeConfig());

		return loadFunction(fc, null);
	}

	public static FunctionInterface loadFunction(FunctionConfig config)
			throws Exception {
		return loadFunction(config, null);
	}

	public static FunctionInterface loadFunction(FunctionConfig config, Log log)
			throws Exception {
		FunctionInterface function = (FunctionInterface) Class.forName(
				config.getJavaClass()).newInstance();
		if (log != null)
			function.setLog(log);
		function.setConfiguration(config.getFunctionConfig());

		if (Thread.currentThread() instanceof RecordPlaybackInterface) {
			Class<?>[] interfaces = new Class[] { FunctionInterface.class };
			String tname = Thread.currentThread().getName();
			RecordPlaybackInterface rpi = (RecordPlaybackInterface) Thread
					.currentThread();
			Object obj = null;
			if (rpi.isRecording(function))
				obj = com.ibm.di.util.RecordAL.newInstance(tname, function, rpi
						.getDatabase(), true, interfaces);
			else if (rpi.isPlaying(function))
				obj = com.ibm.di.util.RecordAL.newInstance(tname, function, rpi
						.getDatabase(), false, interfaces);
			else
				return function;

			getServer().logmsg(
					sResHash.getString("SYSTEM.FUNCTIONS.FUNCTION.INFO",
							new Object[] {
									config.getName(),
									tname,
									rpi.isRecording(function) ? "recorded"
											: "played back" }));

			return (FunctionInterface) obj;
		}

		return function;
	}

	public static SearchCriteria loadCriteria(Vector<String> m) throws Exception {
		SearchCriteria sc = new SearchCriteria();

		for (String str:m) {
			int index = str.indexOf(":");
			String key = str.substring(0, index);
			String value = str.substring(index + 1);

			StringTokenizer st = new StringTokenizer(value, "|");
			String v = (String) st.nextToken();
			String v2 = (String) st.nextToken();
			sc.addTemplate(key, (int) v.charAt(0), v2);
		}
		return sc;
	}

	public static Object getClassInstance(String className) throws Exception {
		String cls = className;

		if (className.indexOf(".") == -1)
			cls = "com.ibm.di.connector." + className;

		Class<?> t1 = Class.forName(cls);
		return t1.newInstance();
	}

	public static void declareUserFunctions(ScriptEngine se) throws Exception {
		declareUserFunctions(se, getServer());
	}

	public static void declareUserFunctions(ScriptEngine se, RSInterface main)
			throws Exception {
		// Always declare system object
		if (main == null)
			main = getServer();
		se.declareStaticBean("system", new UserFunctions(main),
				UserFunctions.class);

		// declare COMProxy object if it a windows os
		if (System.getProperty("os.name").startsWith("Windows"))
			se.declareStaticBean("COMProxy", COMProxy.create(), COMProxy.class);

		if (main == null)
			return;

		LibraryConfig lib = main.getLibraries();
		if (lib == null)
			return;

		for (Iterator<String> i = lib.getDataIterator(); i.hasNext();) {
			String key = i.next();
			String cls = lib.getStringParameter(key);

			Object c1 = Class.forName(cls).newInstance();
			getServer().logmsg(
					sResHash.getString("SYSTEM.FUNCTIONS.DECLRBEAN.INFO",
							new Object[] { key, c1.getClass().getName() }));

			// se.undeclareBean (key);
			se.declareStaticBean(key, c1);
		}
	}

	/**
	 * This method renames old logs to be one version older and add current log
	 * with version '1'. The total number of the kept log files is specified by
	 * the system property <b>com.ibm.di.server.rotatelogs</b>.
	 * <p>
	 * <b>Note:</b> If this property is not set, this method will not do
	 * anything.
	 * <p>
	 * <b>See Also:</b> <br>
	 * Online documentation for help about setting com.ibm.di.server.rotatelogs
	 * property.
	 * 
	 * @param parent
	 *            object used for getting the system property -
	 *            com.ibm.di.server.rotatelogs.
	 * @param logfile
	 *            current log file
	 * @throws Exception
	 */
	public static void rotatelog(RSInterface parent, String logfile)
			throws Exception {
		File f;
		String rot = parent.getSysProp("com.ibm.di.server.rotatelogs");
		if (rot == null || rot.length() < 1)
			return;

		int rotc;
		try {
			rotc = Integer.parseInt(rot);
		} catch (Exception e) {
			throw new Exception(sResHash.getString(
					"SYSTEM.FUNCTIONS.ROTLOG.ERROR", rot));
		}

		try {
			f = new File(logfile + "." + rotc);
			if (f.exists()) {
				if (!f.delete()) {
					getServer().logmsg(
							sResHash.getString("SYSTEM.FUNCTIONS.CANNOT.DELETE.FILE", f
									.getName()));
				}
			}
		} catch (Exception ignore) {
		}

		for (int i = rotc - 1; i > 0; i--) {
			try {
				f = new File(logfile + "." + i);
				File f2 = new File(logfile + "." + (i + 1));
				if (f.exists()) {
					if (!f.renameTo(f2)) {
						getServer().logmsg(
								sResHash.getString(
										"SYSTEM.FUNCTIONS.CANNOT.RENAME.FILE1", f
												.getName()));
					}
				}
			} catch (Exception ignore) {
			}
		}

		f = new File(logfile);
		File f2 = new File(logfile + ".1");
		if (f.exists()) {
			if (!f.renameTo(f2)) {
				getServer().logmsg(
						sResHash.getString("SYSTEM.FUNCTIONS.CANNOT.RENAME.FILE2", f
								.getName()));
			}
		}

	}

	public static ConnectorConfig createRuntimeConnector() throws Exception {
		ConnectorConfig cc = (ConnectorConfig) getServer().getMetamergeConfig()
				.newInstanceOf(MetamergeConfig.DEFAULT_CONNECTOR_FOLDER);
		cc.init();
		cc.setupInheritanceChain();
		return cc;
	}

	/**
	 * Set system properties from properties object in config. If recursive, run
	 * through included configs as well.
	 */
	public static void setSystemProperties(MetamergeConfig config,
			boolean recursive) throws Exception {
		if (recursive) {
			try {
				BaseConfiguration bc = (BaseConfiguration) config
						.getDefaultFolder(MetamergeConfig.NAMESPACE_FOLDER);
				for (String key:bc.getKeys(BaseConfiguration.ONE_LEVEL)) {
					setSystemProperties(MetamergeConfigFactory
							.getLocalNamespace(config, key));
				}
			} catch (javax.naming.NameNotFoundException ignore) {
			}
		}

		// "main" configuration overwrites included configs
		setSystemProperties(config);
	}

	public static void setSystemProperties(MetamergeConfig mc) throws Exception {
		try {
			BaseConfiguration pc = (BaseConfiguration) mc
					.lookup(MetamergeConfig.DEFAULT_PROPERTY_FOLDER);
			for (String key:pc.getKeys(BaseConfiguration.ONE_LEVEL))
				System.setProperty(key, pc.getStringParameter(key));
		} catch (javax.naming.NameNotFoundException ignore) {
		}
	}
	
	/**
	 * Do nothing.
	 * This method intentionally does nothing.
	 * @since 7.0
	 */
	public static void doNothing() {		
	}
	
	/**
	 * Set up inheritance for system Connectors.
	 * @since 7.1
	 */
	public static void setupSystemConnectorInheritance() {
		MetamergeConfig sys = MetamergeConfigFactory.getNamespace(MetamergeConfigFactory.SYSTEM_NAMESPACE);
		try {
			Enumeration<Binding> list = sys.list(MetamergeConfig.DEFAULT_CONNECTOR_FOLDER);
			while (list.hasMoreElements()) {
				Object o = list.nextElement().getObject();
				if (o instanceof ConnectorConfig)
					((ConnectorConfig)o).setupInheritanceChain();
			}
		} catch (Exception e) {
			doNothing();
		}
	}
	
	/**
	 * Verify that a Socket is using the correct SSL protocols.
	 * If the socket is an SSLSocket, set the protocols as defined by a system property.
	 * @param socket
	 */
	public static void verifySSLProtocols(Socket socket) {
		if (! (socket instanceof SSLSocket))
			return;
	
		String s = System.getProperty("com.ibm.di.SSLProtocols");
		if (s != null) {
			((SSLSocket) socket).setEnabledProtocols(s.trim().split(" *, *"));
		}
	}
	
	/**
	 * Verify that a ServerSocket is using the correct SSL protocols.
	 * If the socket is an SSLServerSocket, set the protocols as defined by a system property.
	 * @param socket
	 */
	
	public static void verifySSLProtocols(SSLServerSocket socket) {
		if (! (socket instanceof SSLServerSocket))
			return;

		String s = System.getProperty("com.ibm.di.SSLProtocols");
		if (s != null) {
			socket.setEnabledProtocols(s.trim().split(" *, *"));
		}
	}   

}
