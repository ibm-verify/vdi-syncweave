/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.osgi;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Vector;

import javax.swing.JComponent;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.connector.Connector;
import com.ibm.di.connector.ConnectorInterface;
import com.ibm.di.entry.Entry;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.parser.ParserInterface;
import com.ibm.di.script.ScriptEngine;
import com.ibm.di.server.Log;
import com.ibm.di.server.RSInterface;
import com.ibm.di.server.SearchCriteria;
import com.ibm.icu.util.StringTokenizer;

/**
 * Used to act as a way for accessing ConnectorComponent services from the OSGi
 * layer. <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.2
 */
public class ConnectorDelegate extends Connector {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	protected Connector worker;
	private String id;

	public ConnectorDelegate() {
	}

	public ConnectorDelegate(String id) throws Exception {
		this.id = id;
		this.worker = getWorkerConnector(id, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.server.VersionInfoInterface#getVersion()
	 */
	@Override
	public String getVersion() {
		try {
			Object conn = getConnectorService(id);
			if (conn != null) {
				String ver = (String) conn.getClass().getMethod("getProperty", new Class[] { String.class }).invoke(conn,
						new Object[] { "comp.version" });
				if (ver != null && (ver = ver.trim()).length() > 0) {
					return ver;
				}
			}
		} catch (Exception e) {
			SystemFunctions.doNothing();
		}
		return "unknown";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#initialize(java.lang.Object)
	 */
	@Override
	public void initialize(Object o) throws Exception {
		checkConfigured();
		worker.initialize(o);
	}

	private void setConfigurationAndCreateWorker(BaseConfiguration config) throws Exception {
		if (worker == null) {
			id = findId(config);
			if (id == null) {
				// throw exception
				checkConfigured();
			}

			worker = getWorkerConnector(id, config);
			worker.setConfiguration(config);
		}
	}

	/**
	 * @param config
	 * @return
	 */
	private String findId(BaseConfiguration config) {
		if (config != null) {
			BaseConfiguration systemCfg = config;
			while (systemCfg != null
					&& systemCfg.getMetamergeConfig() != MetamergeConfigFactory
							.getNamespace(MetamergeConfigFactory.SYSTEM_NAMESPACE)) {
				systemCfg = systemCfg.getInheritsFrom();
			}
			if (systemCfg != null) {
				return ((ConnectorConfig) systemCfg).getShortName();
			}
		}
		return null;
	}

	private Connector getWorkerConnector(String compId, BaseConfiguration config) throws Exception {
		Object connComp = getConnectorService(compId);
		if (connComp != null) {
			Class<?> connCompClass = connComp.getClass();
			Object inst = connCompClass.getMethod("newInstance", (Class[]) null).invoke(connComp, (Object[]) null);
			if (inst instanceof Connector) {
				Connector conn = (Connector) inst;
				if (config != null) {
					conn.setConfiguration(config);
				}
				conn.setContext(getContext());
				conn.setLog(getLog());
				conn.setModes(getModes());
				conn.setName(getName());
				conn.setRSInterface(getRSInterface());
				conn.setParser(getParser());
				return conn;
			} else {
				throw new RuntimeException("Instance (" + inst.getClass().getName() + ") not compatible with "
						+ Connector.class.getName());
			}
		} else {
			throw new RuntimeException("IntegrationComponent \"" + compId + "\" is not found!");
		}

	}

	private Object getConnectorService(String compId) throws Exception {
		OSGiContainerHandle handle = OSGiContainerHandle.getHandle(true);
		try {
			handle.startBundle("com.ibm.di.component");
			Object[] icServ = handle.getServices("com.ibm.di.component.ConnectorComponent", "(component.name=" + compId + ")");
			if (icServ != null && icServ.length > 0) {
				return icServ[0];
			}
		} catch (Throwable t) {
			if (t instanceof Exception) {
				throw (Exception) t;
			} else {
				throw new Exception(t);
			}
		}
		return null;
	}

	private void checkConfigured() {
		if (worker == null) {
			throw new RuntimeException("Configure the connector first");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#addFindEntry(java.lang.Object)
	 */
	@Override
	public boolean addFindEntry(Object entry) {
		checkConfigured();
		return worker.addFindEntry(entry);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#clearFindEntries()
	 */
	@Override
	public void clearFindEntries() {
		checkConfigured();
		worker.clearFindEntries();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#debug(java.lang.String)
	 */
	@Override
	public void debug(String msg) {
		if (worker != null) {
			worker.debug(msg);
		} else {
			super.debug(msg);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#debugMode()
	 */
	@Override
	public boolean debugMode() {
		return worker != null ? worker.debugMode() : super.debugMode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#deleteEntry(com.ibm.di.entry.Entry,
	 * com.ibm.di.server.SearchCriteria)
	 */
	@Override
	public void deleteEntry(Entry entry, SearchCriteria search) throws Exception {
		checkConfigured();
		worker.deleteEntry(entry, search);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#findEntry(java.lang.Object,
	 * java.lang.Object)
	 */
	@Override
	public Entry findEntry(Object arg0, Object arg1) {
		checkConfigured();
		return worker.findEntry(arg0, arg1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.connector.Connector#findEntry(com.ibm.di.server.SearchCriteria
	 * )
	 */
	@Override
	public Entry findEntry(SearchCriteria search) throws Exception {
		checkConfigured();
		return worker.findEntry(search);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#getBoolean(java.lang.Object)
	 */
	@Override
	public Boolean getBoolean(Object p1) {
		return worker != null ? worker.getBoolean(p1) : super.getBoolean(p1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#getConfiguration()
	 */
	@Override
	public Object getConfiguration() {
		return worker != null ? worker.getConfiguration() : super.getConfiguration();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#getContext()
	 */
	@Override
	public Object getContext() {
		return worker != null ? worker.getContext() : super.getContext();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#getFindEntryCount()
	 */
	@Override
	public int getFindEntryCount() {
		checkConfigured();
		return worker.getFindEntryCount();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#getFirstFindEntry()
	 */
	@Override
	public Entry getFirstFindEntry() throws Exception {
		checkConfigured();
		return worker.getFirstFindEntry();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#getLog()
	 */
	@Override
	public Log getLog() {
		return worker != null ? worker.getLog() : super.getLog();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#getMaxDuplicateEntries()
	 */
	@Override
	public int getMaxDuplicateEntries() {
		checkConfigured();
		return worker.getMaxDuplicateEntries();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#getModes()
	 */
	@Override
	public Vector<String> getModes() {
		return worker != null ? worker.getModes() : super.getModes();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.connector.Connector#getModes(com.ibm.di.config.interfaces.
	 * ConnectorConfig)
	 */
	@Override
	public Vector<String> getModes(ConnectorConfig config) {
		String compId = findId(config);
		try {
			Object connComp = getConnectorService(compId);
			if (connComp != null) {
				String modes = (String) connComp.getClass().getMethod("getProperty", new Class[] { String.class }).invoke(connComp,
						new Object[] { "conn.modes" });
				if (modes != null) {
					StringTokenizer st = new StringTokenizer(modes, ",;:");
					Vector<String> modesVector = new Vector<String>(7);
					while (st.hasMoreTokens()) {
						String mode = st.nextToken().trim();
						if (mode.length() > 0) {
							modesVector.add(mode);
						}
					}

					if (modesVector.size() > 0) {
						return modesVector;
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return super.getModes(config);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#getName()
	 */
	@Override
	public String getName() {
		return worker != null ? worker.getName() : super.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#getNextClient()
	 */
	@Override
	public ConnectorInterface getNextClient() throws Exception {
		checkConfigured();
		return worker.getNextClient();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#getNextEntry()
	 */
	@Override
	public Entry getNextEntry() throws Exception {
		checkConfigured();
		return worker.getNextEntry();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#getNextFindEntry()
	 */
	@Override
	public Entry getNextFindEntry() throws Exception {
		checkConfigured();
		return worker.getNextFindEntry();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#getParam(java.lang.String)
	 */
	@Override
	public String getParam(String param) {
		return worker != null ? worker.getParam(param) : super.getParam(param);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#getParser()
	 */
	@Override
	public ParserInterface getParser() {
		return worker != null ? worker.getParser() : super.getParser();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#getProperty(java.lang.Object)
	 */
	@Override
	public Object getProperty(Object p1) {
		return worker != null ? worker.getProperty(p1) : super.getProperty(p1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#getPushbackEntry()
	 */
	@Override
	public Entry getPushbackEntry() {
		checkConfigured();
		return worker.getPushbackEntry();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#getRawConnectorConfiguration()
	 */
	@Override
	public BaseConfiguration getRawConnectorConfiguration() {
		return worker != null ? worker.getRawConnectorConfiguration() : super.getRawConnectorConfiguration();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#getRSInterface()
	 */
	@Override
	public RSInterface getRSInterface() {
		return worker != null ? worker.getRSInterface() : super.getRSInterface();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#getUI()
	 */
	@Override
	public JComponent getUI() {
		checkConfigured();
		return worker.getUI();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#hasConfigValue(java.lang.Object)
	 */
	@Override
	public boolean hasConfigValue(Object p1) {
		return worker != null ? worker.hasConfigValue(p1) : super.hasConfigValue(p1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		return worker != null ? worker.equals(o) : super.equals(o);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return worker != null ? worker.hashCode() : super.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected Object clone() throws CloneNotSupportedException {

		Method method = null;
		try {
			method = worker.getClass().getMethod("clone", (Class[]) null);
		} catch (SecurityException e1) {
			e1.printStackTrace();
		} catch (NoSuchMethodException e1) {
			e1.printStackTrace();
		}
		if (method != null) {
			boolean access = method.isAccessible();
			try {
				method.setAccessible(true);
				return method.invoke(worker, (Object[]) null);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} finally {
				method.setAccessible(access);
			}
		}
		return super.clone();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#hasParser()
	 */
	@Override
	public boolean hasParser() {
		checkConfigured();
		return worker.hasParser();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#initParser(java.lang.Object,
	 * java.lang.Object)
	 */
	@Override
	public void initParser(Object is, Object os) throws Exception {
		checkConfigured();
		worker.initParser(is, os);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#isDeltaSupported()
	 */
	@Override
	public boolean isDeltaSupported() {
		checkConfigured();
		return worker.isDeltaSupported();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#isExceptionFatal(java.lang.Exception)
	 */
	@Override
	public boolean isExceptionFatal(Exception e) {
		checkConfigured();
		return worker.isExceptionFatal(e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#isIOException(java.lang.Throwable)
	 */
	@Override
	public boolean isIOException(Throwable e) {
		checkConfigured();
		return worker.isIOException(e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#logError(java.lang.String)
	 */
	@Override
	public void logError(String msg) {
		if (worker != null) {
			worker.logError(msg);
		} else {
			super.logError(msg);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#logmsg(java.lang.String)
	 */
	@Override
	public void logmsg(String msg) {
		if (worker != null) {
			worker.logmsg(msg);
		} else {
			super.logmsg(msg);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#modEntry(com.ibm.di.entry.Entry,
	 * com.ibm.di.server.SearchCriteria)
	 */
	@Override
	public void modEntry(Entry entry, SearchCriteria search) throws Exception {
		checkConfigured();
		worker.modEntry(entry, search);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#modEntry(com.ibm.di.entry.Entry,
	 * com.ibm.di.server.SearchCriteria, com.ibm.di.entry.Entry)
	 */
	@Override
	public void modEntry(Entry entry, SearchCriteria search, Entry old) throws Exception {
		checkConfigured();
		worker.modEntry(entry, search, old);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#pushback(com.ibm.di.entry.Entry)
	 */
	@Override
	public void pushback(Entry e) {
		checkConfigured();
		worker.pushback(e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#putEntry(com.ibm.di.entry.Entry)
	 */
	@Override
	public void putEntry(Entry entry) throws Exception {
		checkConfigured();
		worker.putEntry(entry);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.connector.Connector#queryOperations(com.ibm.di.config.interfaces
	 * .ConnectorConfig)
	 */
	@Override
	public void queryOperations(ConnectorConfig config) throws Exception {
		checkConfigured();
		worker.queryOperations(config);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#queryReply(com.ibm.di.entry.Entry)
	 */
	@Override
	public Entry queryReply(Entry entry) throws Exception {
		checkConfigured();
		return worker.queryReply(entry);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#querySchema(java.lang.Object)
	 */
	@Override
	public Object querySchema(Object source) throws Exception {
		checkConfigured();
		return worker.querySchema(source);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#queryTables()
	 */
	@Override
	public Vector<String> queryTables() throws Exception {
		checkConfigured();
		return worker.queryTables();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#reconnect()
	 */
	@Override
	public void reconnect() throws Exception {
		checkConfigured();
		worker.reconnect();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#reconnect(java.lang.Object)
	 */
	@Override
	public void reconnect(Object o) throws Exception {
		checkConfigured();
		worker.reconnect(o);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.connector.Connector#registerScriptBeans(com.ibm.di.script.
	 * ScriptEngine)
	 */
	@Override
	public void registerScriptBeans(ScriptEngine se) throws Exception {
		worker.registerScriptBeans(se);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#replyEntry(com.ibm.di.entry.Entry)
	 */
	@Override
	public void replyEntry(Entry entry) throws Exception {
		checkConfigured();
		worker.replyEntry(entry);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#selectEntries()
	 */
	@Override
	public void selectEntries() throws Exception {
		checkConfigured();
		worker.selectEntries();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#setConfiguration(java.lang.Object)
	 */
	@Override
	public void setConfiguration(Object config) {
		try {
			setConfigurationAndCreateWorker((BaseConfiguration) config);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#setContext(java.lang.Object)
	 */
	@Override
	public void setContext(Object aContext) {
		super.setContext(aContext);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#setCurrent(com.ibm.di.entry.Entry,
	 * com.ibm.di.server.SearchCriteria)
	 */
	@Override
	public void setCurrent(Entry entry, SearchCriteria search) {
		checkConfigured();
		worker.setCurrent(entry, search);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#setDebugMode(boolean)
	 */
	@Override
	public void setDebugMode(boolean debug) {
		checkConfigured();
		worker.setDebugMode(debug);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#setLog(com.ibm.di.server.Log)
	 */
	@Override
	public void setLog(Log logger) {
		super.setLog(logger);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#setMaxDuplicateEntries(int)
	 */
	@Override
	public void setMaxDuplicateEntries(int mde) {
		checkConfigured();
		worker.setMaxDuplicateEntries(mde);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#setModes(java.lang.String[])
	 */
	@Override
	public void setModes(String[] modes) {
		super.setModes(modes);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#setModes(java.util.Vector)
	 */
	@Override
	public void setModes(Vector<String> modes) {
		super.setModes(modes);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#setName(java.lang.String)
	 */
	@Override
	public void setName(String name) {
		super.setName(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#setParam(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public void setParam(String param, String value) {
		super.setParam(param, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.connector.Connector#setParser(com.ibm.di.parser.ParserInterface
	 * )
	 */
	@Override
	public void setParser(ParserInterface parser) throws IOException {
		super.setParser(parser);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#setProperty(java.lang.Object,
	 * java.lang.Object)
	 */
	@Override
	public void setProperty(Object p1, Object p2) {
		super.setProperty(p1, p2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.connector.Connector#setRSInterface(com.ibm.di.server.RSInterface
	 * )
	 */
	@Override
	public void setRSInterface(RSInterface rsi) {
		super.setRSInterface(rsi);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#terminate()
	 */
	@Override
	public void terminate() throws Exception {
		checkConfigured();
		worker.terminate();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#terminateServer()
	 */
	@Override
	public void terminateServer() throws Exception {
		checkConfigured();
		worker.terminateServer();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (worker != null) {
			return worker.toString();
		} else {
			return super.toString();
		}
	}

	public ConnectorInterface getDelegate() {
		return worker;
	}
	
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
}
