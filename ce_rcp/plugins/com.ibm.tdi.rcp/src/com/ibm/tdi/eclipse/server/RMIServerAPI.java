/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.server;

import java.io.InputStream;
import java.rmi.Naming;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

import com.ibm.di.api.remote.AssemblyLine;
import com.ibm.di.api.remote.ConfigInstance;
import com.ibm.di.api.remote.Session;
import com.ibm.di.api.remote.SessionFactory;
import com.ibm.di.api.remote.TDIProperties;
import com.ibm.di.api.remote.impl.AssemblyLineListenerBase;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.PropertyManager;
import com.ibm.di.eclipse.http.commands.RestCommand;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.TaskCallBlock;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;

/**
 * This class extends RestServerAPI to provide the same methods over TDI RMI
 * Server API.
 * 
 */
public class RMIServerAPI extends RestServerAPI {
	@SuppressWarnings("unused") //$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private Session session;

	public RMIServerAPI(IFile file) throws Exception {
		super(file);
	}

	public RMIServerAPI(IProject project) throws Exception {
		super(project);
	}

	public RMIServerAPI(String name) throws Exception {
		super(name);
	}

	@SuppressWarnings("unchecked")
	@Override
	public String checkin(IProject project) throws Exception {
		String id = project.getName();
		String file = id + ".xml"; //$NON-NLS-1$
		MetamergeConfig mc = Utils.loadRuntimeRS(project);
		ArrayList<String> list = getSession().listAllConfigurations();
		if (!list.contains(id) && !list.contains(file))
			getSession().createNewConfiguration(file, false);

		getSession().checkInConfiguration(mc, file);
		return ""; //$NON-NLS-1$
	}

	@SuppressWarnings("unchecked")
	@Override
	public void checkin(String id, MetamergeConfig config) throws Exception {
		String file = id + ".xml"; //$NON-NLS-1$
		ArrayList<String> list = getSession().listAllConfigurations();
		if (!list.contains(id) && !list.contains(file))
			getSession().createNewConfiguration(file, false);
		getSession().checkInConfiguration(config, file);
	}

	@Override
	public MetamergeConfig checkout(IProject project) throws Exception {
		return getSession().checkOutConfiguration(project.getName()+".xml"); //$NON-NLS-1$
	}

	@Override
	public MetamergeConfig checkout(String id, MetamergeConfig config) throws Exception {
		return getSession().checkOutConfiguration(id+".xml"); //$NON-NLS-1$
	}

	@Override
	public void close() {
		super.close();
	}

	@Override
	public ArrayList<String> listAllConfigurations() throws Exception {
		ArrayList<String> list = new ArrayList<String>();
		for (Object obj : getSession().listAllConfigurations()) {
			list.add(obj.toString());
		}
		return list;
	}

	@Override
	public boolean isConfigInstanceRunning(String configID) throws Exception {
		return getSession().getConfigInstance(configID) != null;
	}

	@Override
	public String list() throws Exception {
		return null;
	}

	@Override
	public Entry ping() throws Exception {
		try {
			getSession().isSSLon();
		} catch (java.rmi.ConnectException ce) {
			session = null;
			throw new java.net.ConnectException(ce.getLocalizedMessage());
		} catch (java.rmi.ConnectIOException ce) {
			session = null;
			throw new java.net.ConnectException(ce.getLocalizedMessage());
		}
		return new Entry();
	}

	@Override
	public void setProperty(String prop, String value) throws Exception {
		String instanceID = createTempInstance(null);
		ConfigInstance cci = getSession().getConfigInstance(instanceID);
		cci.getTDIProperties().setProperty(PropertyManager.STDCOLL_JAVA, prop, value);
		cci.stop();
	}

	@Override
	public Entry getProperties(String store, InputStream mc) throws Exception {
		String instanceID = createTempInstance(mc);
		ConfigInstance cci = getSession().getConfigInstance(instanceID);
		TDIProperties props = cci.getTDIProperties();
		Entry entry = new Entry();
		for (String key : props.getPropertyStoreKeys(store)) {
			entry.setAttribute(key, props.getProperty(store, key));
			entry.setProperty(key, String.valueOf(props.isPropertyEncrypted(store, key))); //$NON-NLS-1$
		}
		stopConfigInstance(cci.getConfigId());
		return entry;
	}

	@Override
	public Entry getProperties(String store, InputStream mc, List<String> names)
			throws Exception {
		String instanceID = createTempInstance(mc);
		ConfigInstance cci = getSession().getConfigInstance(instanceID);
		TDIProperties props = cci.getTDIProperties();
		Entry entry = new Entry();
		for (String key : names) {
			Object value = props.getProperty(store, key);
			if (value != null) {
				entry.setAttribute(key, value);
				entry.setProperty(key, String.valueOf(props.isPropertyEncrypted(store, key)));
			}
		}
		stopConfigInstance(cci.getConfigId());
		return entry;
	}

	@Override
	public Entry setProperties(String store, Entry entry, InputStream mc) throws Exception {
		String instanceID = createTempInstance(mc);
		ConfigInstance cci = getSession().getConfigInstance(instanceID);
		TDIProperties props = cci.getTDIProperties();
		for (String key : entry.getAttributeNames()) {
			String value = entry.getString(key);
			if (value == null) {
				props.removeProperty(store, key);				
			} else {
				boolean protect = Boolean.valueOf(""+entry.getProperty(key)); //$NON-NLS-1$
				props.setProperty(store, key, value, protect);
			}
		}
		props.commit();
		try {
			cci.stop();
		} catch (Exception e) {}
		return new Entry();
	}

	@Override
	public Entry startAssemblyLineE(String configID, String al, TaskCallBlock tcb, boolean log) throws Exception {
		return startAssemblyLineE(configID, al, tcb, log, false);
	}
	
	public Entry startAssemblyLineE(String configID, String al, TaskCallBlock tcb, boolean log, boolean getwork) throws Exception {
		ConfigInstance ci = getConfigInstance(configID);
		RMILogger alListener = new RMILogger();
		AssemblyLine ral = ci.startAssemblyLine(al, tcb, AssemblyLineListenerBase.createInstance(alListener, isSsl()), true, // getlogs
				false, // sync
				getwork // do not return entry at each AL cycle
				);
		Entry entry = new Entry();
		entry.setAttribute(ATTRIBUTE_ASSEMBLY_LINE_LOG, alListener);
		entry.setAttribute(ATTRIBUTE_ASSEMBLY_LINE_ID, ""+ral.getUniqueCode());
		entry.setAttribute(ATTRIBUTE_ASSEMBLY_LINE_NAME, ral.getName());
		entry.setAttribute(ATTRIBUTE_CONFIG_ID, ral.getConfigInstance().getConfigId());
		return entry;
	}

	@Override
	public void startConfigInstance(String configID) throws Exception {
		if (getSession().getConfigInstance(configID) == null) {
			getSession().startConfigInstance(configID);
		}
	}

	@Override
	public String startTempConfig(String configID, InputStream contents) throws Exception {
		byte[] buf = new byte[contents.available()];
		contents.read(buf);
		String cfg = new String(buf, "UTF-8");
		ConfigInstance cci = getSession().startTempConfigInstance(cfg, true, configID, null);
		return cci.getConfigId();
	}

	@Override
	public void stopAssemblyLine(String configID, String al) throws Exception {
		ConfigInstance cci = getSession().getConfigInstance(configID);
		if (cci == null)
			return;

		AssemblyLine[] arr = cci.getAssemblyLines();
		for (int i = 0; i < arr.length; i++) {
			AssemblyLine ral = arr[i];
			if(ral.getUniqueCode() == Integer.parseInt(al)) {
				ral.stop();
				return;
			}
			if(ral.getGlobalUniqueID().equals(al)) {
				ral.stop();
				return;
			}
		}
		
		for (int i = 0; i < arr.length; i++) {
			if (arr[i].getName().equals(al)) {
				arr[i].stop();
			}
		}
	}

	@Override
	public void stopConfigInstance(String configID) throws Exception {
		ConfigInstance cci = getSession().getConfigInstance(configID);
		if(cci != null)
			cci.stop();
	}

	@Override
	public void stopServer() throws Exception {
		getSession().shutDownServer();
		session = null;
	}
	
	/**
	 * Returns the session object. The session object is created if need be.
	 * @return Session object
	 * @throws Exception
	 */
	public Session getSession() throws Exception {
		try {
			if (session == null) {
				String rmi = "rmi://" + getAddress() + "/SessionFactory"; //$NON-NLS-1$ //$NON-NLS-2$
				SessionFactory sessionFactory = (SessionFactory) Naming.lookup(rmi);
				String user = getUser();
				String pwd = getPwd();
				if (user != null && user.length() > 0) {
					session = sessionFactory.createSession(user, pwd);
				} else {
					session = sessionFactory.createSession();
				}
			}
			return session;
		} catch (java.rmi.ConnectException e) {
			throw new java.net.ConnectException(e.getLocalizedMessage());
		}
	}

	private ConfigInstance getConfigInstance(String configID) throws Exception {
		return getSession().getConfigInstance(configID);
	}

	private String createTempInstance(InputStream mc) throws Exception {
		InputStream input = mc;
		if (input == null)
			input = createEmptyConfigFile();
		String configID = UUID.randomUUID().toString();
		return startTempConfig(configID, input);
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.tdi.eclipse.server.RestServerAPI#createLogger()
	 */
	public RestServerLogger createLogger(String id) throws Exception {		
		String configID = id.substring(0, id.indexOf(":"));
		String uniqueID = id.substring(id.indexOf(":") + 1);
		ConfigInstance ci = getSession().getConfigInstance(configID);
		if(ci == null)
			return null;
		
		for (com.ibm.di.api.remote.AssemblyLine al : ci.getAssemblyLines()) {
			if (al.getUniqueCode() == Integer.parseInt(uniqueID)) {
				RMILogger alListener = new RMILogger();
				AssemblyLineListenerBase proxy = AssemblyLineListenerBase.createInstance(alListener, isSsl());
				al.addListener(proxy, true, false);
				alListener.setProxy(proxy);
				return alListener;
			}
		}
		
		return null;
	}

	@Override
	public Entry getServerStatus() throws Exception {
		Entry entry = new Entry();
		ArrayList<String> arr = getSession().listAllConfigurations();
		Attribute a = entry.newAttribute(RestCommand.RES_CONFIGURATION);
		for (int i = 0; i < arr.size(); i++) {
			a.addValue(arr.get(i));
		}
		ConfigInstance[] runs = getSession().getConfigInstances();
		a = entry.newAttribute(RestCommand.RES_CONFIG_INSTANCE + "s"); //$NON-NLS-1$
		for (int i = 0; i < runs.length; i++) {
			Attribute inst = new Attribute(RestCommand.RES_CONFIG_INSTANCE);
			a.addValue(inst);
			inst.addValue(new Attribute("Name", runs[i].getConfigId())); //$NON-NLS-1$
			AssemblyLine[] alruns = runs[i].getAssemblyLines();
			for (int j = 0; j < alruns.length; j++) {
				Attribute alinst = new Attribute(RestCommand.RES_CONFIG_RUNAL);
				alinst.addValue(new Attribute("Name", alruns[j].getName())); //$NON-NLS-1$
				alinst.addValue(new Attribute("UniqueName", ""+alruns[j].getUniqueCode())); //$NON-NLS-1$
				alinst
						.addValue(new Attribute("LogURL", "/log/" + runs[i].getGlobalUniqueID() + Messages.getString("RMIServerAPI.18") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
								+ alruns[j].getGlobalUniqueID()));
				inst.addValue(alinst);
			}

			Attribute als = new Attribute(RestCommand.RES_CONFIG_AL);
			inst.addValue(als);
			String[] names = runs[i].getAssemblyLineNames();
			for (int k = 0; k < names.length; k++)
				als.addValue(new Attribute("Name", names[k])); //$NON-NLS-1$


		}
		return entry;
	}

	@Override
	public MetamergeConfig getConfiguration(String cid) throws Exception {
		ConfigInstance ci = getSession().getConfigInstance(cid);
		if(ci == null)
			return null;
		else
			return ci.getConfiguration();
	}

	@Override
	public AssemblyLineConfig getAssemblyLineConfiguration(String cId, String alId) throws Exception {
		ConfigInstance ci = getSession().getConfigInstance(cId);
		if(ci == null)
			return null;
		
		for(AssemblyLine al : ci.getAssemblyLines()) {
			if(al.getUniqueCode() == Integer.parseInt(alId)) {
				return ci.getConfiguration().getAssemblyLine(al.getName());
			}
		}
		return null;
	}
	
	private AssemblyLine findConfigAL(String id) throws Exception {
		String configID = id.substring(0, id.indexOf(":"));
		String uniqueID = id.substring(id.indexOf(":") + 1);
		ConfigInstance ci = getSession().getConfigInstance(configID);
		if(ci == null)
			return null;
		
		for (com.ibm.di.api.remote.AssemblyLine al : ci.getAssemblyLines()) {
			if (al.getUniqueCode() == Integer.parseInt(uniqueID)) {
				return al;
			}
		}
		return null;
	}
	
	@Override
	public boolean attachDebugger(String id, String host, int port) throws Exception {
		AssemblyLine ral = findConfigAL(id);
		if(ral == null)
			return false;
		
		ral.attachDebugger(port, host, false);
		return true;
	}

	/**
	 * Sends a request to terminate the debugger session on the specified AL.
	 * @param id The ocnfigID:alID 
	 * @return true if AL was found and request sent
	 * @throws Exception
	 */
	public boolean detachDebugger(String id) throws Exception {
		AssemblyLine ral = findConfigAL(id);
		if(ral == null)
			return false;
		ral.detachDebugger("");
		return true;
	}
}
