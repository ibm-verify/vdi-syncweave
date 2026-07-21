/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.PropertyManager;
import com.ibm.di.config.xml.MetamergeConfigXML;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.entry.transform.XML;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.server.TaskCallBlock;
import com.ibm.tdi.eclipse.TDI;
import com.ibm.tdi.eclipse.Utils;

/**
 * This class is used by the CE to communicate with a remote TDI server. It
 * encapsulates the REST based commands and performs conversion between Entry
 * objects and the XML data that typically flow between the client and server.
 * 
 * Many commands return an Entry, which is derived from the XML data returned by
 * the remote server. If you need the raw data from a command use the
 * sendRequest method.
 * 
 * Always use the RestServerAPI.createInstance() to obtain the api instance. The server
 * document may specify RMI, in which case an RMIServerAPI is returned.
 * 
 */
public class RestServerAPI {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public static final String TDI_WORKDIR = "workdir";

	public static final String TDI_INSTALL = "install";

	public static final String TDI_ADDRESS = "address";

	public static final String TDI_API = "apion";

	public static final String TDI_SSL = "ssl";

	public static final String TDI_TYPE = "type";

	public static final String TYPE_REST = "rest";

	public static final String TYPE_RMI = "rmi";

	public static final String TDI_USERNAME = "user";

	public static final String TDI_PASSWORD = "password";

	// -- The AL log object providing logging from the AL
	public static final String ATTRIBUTE_ASSEMBLY_LINE_LOG = "AssemblyLineLog";
	
	// -- The unique ID for the assemblyline
	public static final String ATTRIBUTE_ASSEMBLY_LINE_ID = "assemblyline.id";
	
	// -- The config ID that owns the assemblyline
	public static final String ATTRIBUTE_CONFIG_ID = "config.id";

	// -- The name of the assemblyline
	public static final String ATTRIBUTE_ASSEMBLY_LINE_NAME = "assemblyline.name";

	private TDIConfigurationFile tdi;
	private String address;
	private String install;
	private String workdir;
	private String user;
	private String pwd;
	private boolean ssl;

	/**
	 * Creates a rest server api with the server associated with <i>project</i>
	 * 
	 */
	public static RestServerAPI createInstance(IProject project) throws Exception {
		String name = project.getPersistentProperty(TDI.PROJECT_PREF_SERVER_QNAME);
		return createInstance(name);
	}

	/**
	 * Creates a rest server api with the server document <i>name</i>
	 * 
	 */
	public static RestServerAPI createInstance(String name) throws Exception {
		IResource res = Utils.getTDIServer(name);
		if (res == null || !res.exists() || !(res instanceof IFile))
			throw new Exception("Server not found: " + name);

		return createInstance((IFile) res);
	}

	/**
	 * Creates a rest server api with the provided <i>file</i>
	 * 
	 */
	public static RestServerAPI createInstance(IFile file) throws Exception {
		if (isServerRMI(file))
			return new RMIServerAPI(file);
		else
			return new RestServerAPI(file);
	}

	/**
	 * Returns true if the server document is using RMI.
	 * 
	 * @param file
	 *            The server document
	 * @return true if server access is RMI
	 * @throws Exception
	 */
	private static boolean isServerRMI(IFile file) throws Exception {
		TDIConfigurationFile cfg = TDIConfigurationFile.loadFile(file);
		return TYPE_RMI.equalsIgnoreCase(cfg.getDefaultConfigObject().getStringParameter(TDI_TYPE));
	}

	/**
	 * Creates a rest server api with the server associated with <i>project</i>
	 * 
	 */
	protected RestServerAPI(IProject project) throws Exception {
		this(project.getPersistentProperty(TDI.PROJECT_PREF_SERVER_QNAME));
	}

	/**
	 * Creates a rest server api with the server document <i>name</i>
	 * 
	 */
	protected RestServerAPI(String name) throws Exception {
		IResource res = Utils.getTDIServer(name);
		if (res == null || !res.exists())
			throw new Exception("Server not found: " + name);

		loadConfiguration((IFile) res);
	}

	/**
	 * Creates a rest server api with the provided <i>file</i>
	 * 
	 */
	protected RestServerAPI(IFile file) throws Exception {
		loadConfiguration(file);
	}

	/**
	 * Reads the server configuration into class variables
	 * 
	 * @param file
	 * @throws Exception
	 */
	private void loadConfiguration(IFile file) throws Exception {
		tdi = TDIConfigurationFile.loadFile(file);
		this.address = tdi.getDefaultConfigObject().getStringParameter(TDI_ADDRESS);
		this.install = tdi.getDefaultConfigObject().getStringParameter(TDI_INSTALL);
		this.workdir = tdi.getDefaultConfigObject().getStringParameter(TDI_WORKDIR);
		

		this.user = tdi.getDefaultConfigObject().getStringParameter(TDI_USERNAME);
		if (this.user != null && this.user.length() == 0)
			this.user = null;

		pwd = tdi.getDefaultConfigObject().getStringParameter(TDI_PASSWORD);
		ssl = tdi.getDefaultConfigObject().getBooleanParameter(TDI_SSL, false);
				
		// Sync with solution/global props if we are connecting to a local server
		/*additional check has been added by L3 for defect 14375*/
		if(getInstall() != null && getInstall().length() > 0 && workdir !=null && workdir.length() >0) {
			try {
				BaseConfiguration config = tdi.getDefaultConfigObject();
				config.setModified(false);
				ServerUtils.readSolutionDirectory(config);
				if(config.getModified()) {
					setAddress(config.getStringParameter(RestServerAPI.TDI_ADDRESS));
					setSsl(config.getBooleanParameter(RestServerAPI.TDI_SSL, true));
				}
			} catch (Exception e) {
				SystemFunctions.doNothing();
			}
		}
		/*This part of code has been modified by L3 for defect 14375*/
		if (workdir == null || workdir.equals(""))
			workdir = install;
	}

	/**
	 * Returns the file name of the server
	 * 
	 */
	public String getName() {
		return tdi.getFile().getName();
	}
	
	/**
	 * Returns server status Entry containing installed and running configs/assemblylines.
	 * 
	 */
	public Entry getServerStatus() throws Exception {
		return sendCommand("list", null);
	}
	/**
	 * Checks in the runtime configuration (Utils.getRuntimeRS())
	 * 
	 */
	public String checkin(IProject project) throws Exception {
		String id = project.getName();
		String data = readStream(Utils.getRuntimeRS(project).getContents());
		return sendRequest(address + "/checkin/" + id, data);
	}

	/**
	 * Checks in the provided configuration.
	 * 
	 * @param id
	 *            The config instance ID
	 * @param config
	 *            the configuration
	 * @throws Exception
	 */
	public void checkin(String id, MetamergeConfig config) throws Exception {
		StringWriter out = new StringWriter();
		config.commitChanges(out, false);
		sendRequest(address + "/checkin/" + config.getSolutionInterface().getInstanceID(), out.toString());
	}

	/**
	 * Checks out a configuration file
	 * 
	 * @param project
	 *            The project to check out (project name is identifier)
	 * @return Configuration object
	 * @throws Exception
	 */
	public MetamergeConfig checkout(IProject project) throws Exception {
		String id = project.getName();
		String str = sendRequest(address + "/checkout/" + id, null);
		return newConfigFile(str);
	}

	/**
	 * Checks out a configuration file
	 * 
	 * @param id
	 *            The config id to check out
	 * @return Configuration object
	 * @throws Exception
	 */
	public MetamergeConfig checkout(String id, MetamergeConfig config) throws Exception {
		String str = sendRequest(address + "/checkout/" + config.getSolutionInterface().getInstanceID(), null);
		return newConfigFile(str);
	}

	private MetamergeConfig newConfigFile(String str) throws Exception {
		Hashtable<String, Object> env = new Hashtable<String, Object>();
		env.put(MetamergeConfigFactory.MC_URL, new ByteArrayInputStream(str.getBytes()));
		return new MetamergeConfigXML(env);
	}

	/**
	 * Sends a list command to the remote server
	 * 
	 * @return Command response (XML data)
	 * @throws Exception
	 */
	public String list() throws Exception {
		return sendRequest(address + "/list", null);
	}

	/**
	 * Sends a ping command to the remote server.
	 * 
	 * @return Command response
	 * @throws Exception
	 */
	public Entry ping() throws Exception {
		String str = sendRequest(address + "/ping", null);
		return XML.fromXML(str);
	}

	/**
	 * Returns true if a specific configuration instance is running (configID)
	 * 
	 * @param configID
	 *            The identifier to check
	 * @return true if configID is running, false otherwise
	 * @throws Exception
	 */
	public boolean isConfigInstanceRunning(String configID) throws Exception {
		String str = list();
		Entry entry = XML.fromXML(str);
		checkError(entry);
		Attribute a = entry.getAttribute("ConfigInstances");
		for (Object obj : a.getValues()) {
			if (obj instanceof Attribute && ((Attribute) obj).getName().equals("ConfigInstance")) {
				Attribute attr = (Attribute) obj;
				for (Object v : attr.getValues()) {
					if ((v instanceof Attribute) && (((Attribute) v).getName().equals("Name"))
							&& (configID.equals(((Attribute) v).getValue()))) {
						return true;
					}

				}
			}
		}
		return false;
	}

	/**
	 * Sends a start command to the remote server to start the specified
	 * configuration id.
	 * 
	 * @param configID
	 *            The configuration id to start
	 * @throws Exception
	 */
	public void startConfigInstance(String configID) throws Exception {
		sendRequest(address + "/start/" + configID, null);
	}

	/**
	 * Sends a stop command to the remote server to stop the specified
	 * configuration instance.
	 * 
	 * @param configID
	 *            The instance to stop
	 * @throws Exception
	 */
	public void stopConfigInstance(String configID) throws Exception {
		sendRequest(address + "/stop/" + configID, null);
	}

	/**
	 * Sends a start/"al" command to the server to start an AssemblyLine for an
	 * already running configuration instance. The started assemblyline is
	 * provided a TCB as well.
	 * 
	 * @param configID
	 *            The running configuration instance id
	 * @param al
	 *            The assemblyline to start
	 * @param tcb
	 *            The TCB to pass on to the assemblyline
	 * @param log
	 *            true if a log URL is wanted
	 * @return The log URL for the started AssemblyLine or null if log==false.
	 * @throws Exception
	 */
	public Entry startAssemblyLineE(String configID, String al, TaskCallBlock tcb, boolean log) throws Exception {

		String post = null;
		if (tcb != null) {
			post = XML.toXML(tcb);
		}

		String url = address + "/start/" + Utils.encodeURI(configID) + "/" + Utils.encodeURI(al);
		if (log)
			url += "?log=true";

		String str = sendRequest(url, post);
		Entry entry = XML.fromXML(str);
		String logurl = entry.getString(ATTRIBUTE_ASSEMBLY_LINE_LOG);
		if (logurl != null && logurl.length() > 0) {
			entry.setAttribute(ATTRIBUTE_ASSEMBLY_LINE_LOG, new RestServerLogger(getAddress() + "/" + Utils.encodeURI(logurl)));
		}
		return checkError(entry);
	}

	/**
	 * Starts a temporary configuration instance.
	 * 
	 * @param configID
	 *            The identifier of the configuration
	 * @return The configuration id of the temporary configuration instance
	 * @throws Exception
	 */
	public String startTempConfig(String configID, InputStream contents) throws Exception {
		String post = null;
		if (contents != null)
			post = readStream(contents);

		String str = sendRequest(address + "/starttemp/" + configID, post);
		Entry entry = XML.fromXML(str);
		checkError(entry);
		return entry.getString("ConfigInstance");
	}

	/**
	 * If the entry has an error attribute this method throws an exception with
	 * the error text.
	 * 
	 */
	private Entry checkError(Entry entry) throws Exception {
		if (entry.getString("error") != null) {
			if (entry.getString("http.body") != null)
				throw new Exception(entry.getString("http.body"));
			else
				throw new Exception(entry.getString("error"));
		}
		return entry;
	}

	/**
	 * Sends a stop command to stop the named assemblyline.
	 * 
	 * @param configID
	 *            The configuration instance
	 * @param al
	 *            The assemblyline name
	 * @throws Exception
	 */
	public void stopAssemblyLine(String configID, String al) throws Exception {
		sendRequest(address + "/stop/" + configID + "/" + al, null);
	}

	/**
	 * Sends a command and optional data to the remote server.
	 * 
	 * @param relpath
	 *            The command (don't include http:// etc)
	 * @param data
	 *            Optional data to post in the request
	 * @return The Entry generated from the XML data the server returned
	 * @throws Exception
	 */
	public Entry sendCommand(String relpath, String data) throws Exception {
		String str = sendRequest(address + "/" + relpath, data);
		Entry entry = XML.fromXML(str);
		return checkError(entry);
	}

	/**
	 * Sends a REST API request and optional data to a remote server.
	 * 
	 * @param url
	 *            The complete command URL (http://host/command)
	 * @param data
	 *            Optional data to post with the request
	 * @return The response string
	 * @throws Exception
	 */
	public String sendRequest(String url, String data) throws Exception {
		String encoded = url.replaceAll(" ", "+");
		HttpURLConnection conn = (HttpURLConnection) new URL(encoded).openConnection();
		if (data != null) {
			conn.addRequestProperty("content-type", "text/xml");
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.getOutputStream().write(data.getBytes());
		}
		conn.connect();
		conn.setReadTimeout(30000);
		try {
			Object result = conn.getContent();
			if (result instanceof InputStream) {
				return readStream((InputStream) result);
			}
			return "" + result;
		} catch (Exception err) {
			String str = conn.getHeaderField("status");
			if (str != null && str.length() > 0)
				throw new Exception(str);
			else
				throw err;
		} finally {
			try {
				conn.disconnect();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * Returns a list of all installed configurations
	 * 
	 */
	public ArrayList<String> listAllConfigurations() throws Exception {
		return new ArrayList<String>();
	}

	private String readStream(InputStream is) throws IOException {
		StringBuffer buf = new StringBuffer();
		int ch;
		while ((ch = is.read()) != -1) {
			buf.append((char) ch);
		}
		return buf.toString();
	}

	/**
	 * Sends a stop server command to the remote server. This will shutdown the
	 * entire server.
	 * 
	 * @throws Exception
	 */
	public void stopServer() throws Exception {
		sendRequest(address + "/server/stop", null);
	}

	/**
	 * Sets a Java property on the server side.
	 * 
	 * @param prop
	 *            The property name
	 * @param value
	 *            The property value
	 * @throws Exception
	 */
	public void setProperty(String prop, String value) throws Exception {
		sendCommand("properties/" + PropertyManager.STDCOLL_JAVA + "/" + Utils.encodeURI(prop) + "/" + Utils.encodeURI(value), null);
	}

	/**
	 * Return all properties for a given store.
	 * 
	 * @param store
	 *            The name of the property store
	 * @param mc
	 *            Required if store is not a standard store (e.g. global,system
	 *            etc)
	 * @return An entry with property names and values
	 * @throws Exception
	 */
	public Entry getProperties(String store, InputStream mc) throws Exception {
		String configID;
		if (mc != null) {
			configID = startTempConfig(UUID.randomUUID().toString(), mc);
			Entry e = sendCommand("properties/" + configID + ":" + store, null);
			stopConfigInstance(configID);			
			return e;
		} else {
			return sendCommand("properties/" + store, null);
		}
	}

	/**
	 * Return some properties for a given store.
	 * 
	 * @param store
	 *            The name of the property store
	 * @param mc
	 *            Required if store is not a standard store (e.g. global,system
	 *            etc)
	 * @param names 
	 * 			   The names of the properties we are interested in.
	 * @return An entry with property names and values
	 * @throws Exception
	 */
	public Entry getProperties(String store, InputStream mc, List<String> names) throws Exception {
		Entry e = getProperties(store, mc);
		for (String key:e.getAttributeNames()) {
			if (!names.contains(key))
				e.removeAttribute(key);
		}
		return e;
	}

	/**
	 * Sets properties in the given store store
	 * 
	 * @param store
	 *            The name of the property store
	 * @param entry
	 *            The list of properties and values to set
	 *            (Attribute.name/value)
	 * @param mc
	 *            Required if store is not a standard store (e.g. global,system
	 *            etc)
	 */
	public Entry setProperties(String store, Entry entry, InputStream mc) throws Exception {
		String configID;
		String xmldata = XML.toXML(entry);
		if (mc != null) {
			configID = startTempConfig(UUID.randomUUID().toString(), mc);
			Entry e = sendCommand("properties/" + configID + ":" + store, xmldata);
			stopConfigInstance(configID);
			return e;
		} else {
			return sendCommand("properties/" + store, xmldata);
		}
	}

	public InputStream createEmptyConfigFile() throws Exception {
		MetamergeConfigXML mc = new MetamergeConfigXML();
		mc.initializeConfig();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		mc.commitChanges(bos);
		return new ByteArrayInputStream(bos.toByteArray());
	}

	public String getURLBase() {
		return address;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getInstall() {
		return install;
	}

	public void setInstall(String install) {
		this.install = install;
	}

	public String getWorkdir() {
		return workdir;
	}

	public void setWorkdir(String workdir) {
		this.workdir = workdir;
	}

	public void close() {
	}

	public String getUser() {
		return user;
	}

	public String getPwd() {
		return pwd;
	}

	/**
	 * Returns the ssl flag used by the remote server
	 * 
	 * @return
	 */
	public boolean isSsl() {
		return ssl;
	}
	
	/**
	 * Sets the ssl flag used by the remote server.
	 * 
	 * @param ssl
	 */
	public void setSsl(boolean ssl) {
		this.ssl = ssl;
	}

	/**
	 * Create a logger object that reads the log of the remote AL.
	 * 
	 * @param id Must have the value "configID:alID"
	 */
	public RestServerLogger createLogger(String id) throws Exception {
		return null;
	}

	/**
	 * Returns the configuration for the specified config instance ID
	 * 
	 */
	public MetamergeConfig getConfiguration(String cid) throws Exception {
		return null;
	}

	/**
	 * Returns the TDI configuration object for this API instance.
	 * 
	 */
	public TDIConfigurationFile getTDIConfigurationFile() {
		return tdi;
	}

	/**
	 * Returns the configuration for the specified al instance ID
	 * 
	 * @param cId Config instance id
	 * @param alId AL instance id
	 */
	public AssemblyLineConfig getAssemblyLineConfiguration(String cId, String alId) throws Exception {
		return null;
	}
	
	/**
	 * Sends a request to start a debugger session on the specified AL.
	 * @param id The ocnfigID:alID 
	 * @param host The host of the CE debug session
	 * @param port The port of the CE debug session
	 * @return true if AL was found and request sent
	 * @throws Exception
	 */
	public boolean attachDebugger(String id, String host, int port) throws Exception {
		throw new Exception("");
	}

	/**
	 * Sends a request to terminate the debugger session on the specified AL.
	 * @param id The ocnfigID:alID 
	 * @return true if AL was found and request sent
	 * @throws Exception
	 */
	public boolean detachDebugger(String id) throws Exception {
		throw new Exception("");
	}

}
