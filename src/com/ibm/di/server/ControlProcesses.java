/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.server;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Vector;

import com.ibm.di.event.ThreadEvent;
import com.ibm.icu.text.SimpleDateFormat;

/**
 * This class has the ability to start, stop and return AssemblyLines. It can
 * also return information such as AssemblyLine configurations in the form of
 * Hashtables.
 */
public class ControlProcesses {
	/**
	 * Copyright information.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Key for the AssemblyLine name. Used when mapping this property in a
	 * Hashtable containing the AssemblyLine information.
	 */
	public static final String AL_NAME = "Name";

	/**
	 * Key for specifying whether the AssemblyLine is running. Used when mapping
	 * this property in a Hashtable containing the AssemblyLine information.
	 */
	public static final String AL_RUNNING_BOOL = "IsRunning";

	/**
	 * Key for the AssemblyLine status. Used when mapping this property in a
	 * Hashtable containing the AssemblyLine information.
	 */
	public static final String AL_STATUS = "Status";

	/**
	 * Key for the AssemblyLine's last started parameter. Used when mapping this
	 * property in a Hashtable containing the AssemblyLine information.
	 */
	public static final String AL_LAST_STARTED = "LastStarted";

	/**
	 * Key for the AssemblyLine's last status. Used when mapping this property
	 * in a Hashtable containing the AssemblyLine information.
	 */
	public static final String AL_LAST_STATUS = "LastStatus";

	/**
	 * Key for the AssemblyLine's last exit text. Used when mapping this
	 * property in a Hashtable containing the AssemblyLine information.
	 */
	public static final String AL_LAST_EXIT = "LastExit";

	/**
	 * A helper constant that defines a value for AssemblyLine's running
	 * property.
	 */
	public static final String AL_VALUE_RUNNING_TRUE = "running";

	/**
	 * A helper constant that defines a value for AssemblyLine's running
	 * property.
	 */
	public static final String AL_VALUE_RUNNING_FALSE = "inactive";

	/**
	 * A helper constant that defines a value for AssemblyLine's status
	 * property.
	 */
	public static final String AL_VALUE_LAST_STATUS_OK = "OK";

	/**
	 * A helper constant that defines a value for AssemblyLine's last started
	 * property.
	 */
	public static final String AL_VALUE_NEVER_STARTED = "-";

	/**
	 * A helper constant that defines a value for AssemblyLine's last exit
	 * property.
	 */
	public static final String AL_VALUE_NEVER_EXIT = "-";

	/**
	 * Key for the Connector's name parameter. Used when mapping this property
	 * in a Hashtable containing the Connector information.
	 */
	public static final String CONN_NAME = "Name";

	/**
	 * Key for the Connector's enabled parameter. Used when mapping this
	 * property in a Hashtable containing the Connector information.
	 */
	public static final String CONN_ENABLED_BOOL = "IsEnabled";

	/**
	 * Key for the Connector's enabled parameter. Used when mapping this
	 * property in a Hashtable containing the Connector information.
	 */
	public static final String CONN_ENABLED = "Enabled";

	/**
	 * Key for the Connector's name parameter. Used when mapping this property
	 * in a Hashtable containing the Connector information.
	 */
	public static final String CONN_MODE = "Mode";

	/**
	 * Key for the Connector's mode parameter. Used when mapping this property
	 * in a Hashtable containing the Connector information.
	 */
	public static final String CONN_TYPE = "Type";

	/**
	 * Key for the Connector's parser parameter. Used when mapping this property
	 * in a Hashtable containing the Connector information.
	 */
	public static final String CONN_PARSER = "Parser";

	/**
	 * A helper constant that defines a value for Connector's enabled property.
	 */
	public static final String CONN_VALUE_ENABLED_TRUE = "Yes";

	/**
	 * A helper constant that defines a value for Connector's enabled property.
	 */
	public static final String CONN_VALUE_ENABLED_FALSE = "No";

	/**
	 * A helper constant that defines a value for Connector's parser property.
	 */
	public static final String CONN_VALUE_NO_PARSER = "-";

	/**
	 * Key for the Server's ip address parameter. Used when mapping this
	 * property in a Hashtable containing the Server information.
	 */
	public static final String SVR_IP_ADDRESS = "IPAddress";

	/**
	 * Key for the Server's host name parameter. Used when mapping this property
	 * in a Hashtable containing the Server information.
	 */
	public static final String SVR_HOST_NAME = "HostName";

	/**
	 * Key for the Server's operating system parameter. Used when mapping this
	 * property in a Hashtable containing the Server information.
	 */
	public static final String SVR_OPERATING_SYSTEM = "OperatingSystem";

	/**
	 * Key for the Server's version parameter. Used when mapping this property
	 * in a Hashtable containing the Server information.
	 */
	public static final String SVR_SERVER_VERSION = "ServerVersion";

	/**
	 * A helper constant that defines a value for Server's property.
	 */
	public static final String SVR_VALUE_UNKNOWN = "unknown";

	/**
	 * All dates provided will be formatted in the format specified by this
	 * member.
	 */
	private static SimpleDateFormat dateFormatter = new SimpleDateFormat(
			"HH:mm:ss z ' ('yyyy.MM.dd')'");

	/**
	 * Just a pointer to the systemFunctions' static member "main".
	 */
	private RSInterface main = RS.getServer();

	/**
	 * A ResourceHash to translate strings
	 */
	private ResourceHash res = ResourceHash.getHash("miserver");

	/**
	 * Retrieves all AssemblyLines along with their status on the server.
	 * 
	 * @return Vector which elements are instances of Hashtable. Each element
	 *         (Hashtable) corresponds to a single AssemlbyLine and contains
	 *         values for the keys represented by the "AL_" constants.
	 */
	public Vector getAssemblyLines() {
		Vector alProperties = new Vector();

		// get all active AssemblyLines and put them into a Hashtable
		Hashtable assemblyLines = new Hashtable();

		Thread[] threads = new Thread[Thread.activeCount()];
		int threadCnt = Thread.enumerate(threads);
		for (int i = 0; i < threadCnt; i++) {
			if (threads[i] instanceof AssemblyLine) {
				assemblyLines.put(threads[i].getName(), threads[i]);
			}
		}

		// get all defined AssemblyLines and their properties
		TreeMap list = (TreeMap) main.getConfiguration(FileConfig.C_TASK);
		if (list != null) {
			for (Iterator iterator = list.entrySet().iterator(); iterator
					.hasNext();) {

				Hashtable prop = new Hashtable();
				java.util.Map.Entry mapEntry = (java.util.Map.Entry) iterator
						.next();
				String alName = (String) mapEntry.getKey();
				TreeMap alProps = (TreeMap) mapEntry.getValue();

				// set AL_NAME property
				prop.put(ControlProcesses.AL_NAME, alName);

				// set AL_RUNNING_BOOL property
				AssemblyLine al = (AssemblyLine) assemblyLines.get(alName);
				boolean isActive = (al != null);
				prop.put(ControlProcesses.AL_RUNNING_BOOL, Boolean
						.valueOf(isActive));

				// set AL_STATUS property
				String runningText;
				if (isActive) {
					runningText = ControlProcesses.AL_VALUE_RUNNING_TRUE;
				} else {
					runningText = ControlProcesses.AL_VALUE_RUNNING_FALSE;
				}
				prop.put(ControlProcesses.AL_STATUS, runningText);

				// set AL_LAST_STARTED property
				Date lastStarted = (Date) alProps
						.get(ThreadEvent.RT_LASTSTARTED);
				String lastStartedText;
				if (lastStarted == null) {
					lastStartedText = AL_VALUE_NEVER_STARTED;
				} else {
					lastStartedText = dateFormatter.format(lastStarted);
				}
				prop.put(ControlProcesses.AL_LAST_STARTED, lastStartedText);

				// set AL_LAST_EXIT property
				Date lastExit = (Date) alProps.get(ThreadEvent.RT_LASTEXIT);
				String lastExitText;
				if (lastExit == null) {
					lastExitText = AL_VALUE_NEVER_EXIT;
				} else {
					lastExitText = dateFormatter.format(lastExit);
				}
				prop.put(ControlProcesses.AL_LAST_EXIT, lastExitText);

				// set AL_LAST_STATUS property
				String lastStatusText;
				Exception lastStatus = (Exception) alProps
						.get(ThreadEvent.RT_LASTSTATUS);
				if (lastStatus != null) {
					lastStatusText = lastStatus.toString();
				} else {
					lastStatusText = ControlProcesses.AL_VALUE_LAST_STATUS_OK;
				}
				prop.put(ControlProcesses.AL_LAST_STATUS, lastStatusText);

				alProperties.add(prop);
			}
		}

		return alProperties;
	}

	/**
	 * Interrupts a thread from the current thread's group given its name. Note
	 * that this method actually interrupts all threads that have this name.
	 * 
	 * @param threadName
	 *            The name of the thread to be interrupted.
	 */
	private void stopThread(String threadName) {
		Thread[] threads = new Thread[Thread.activeCount()];
		int threadCnt = Thread.enumerate(threads);
		for (int i = 0; i < threadCnt; i++) {
			if (threads[i].getName().equals(threadName)) {
				threads[i].interrupt();
			}
		}
	}

	/**
	 * Stops an AssemblyLine given its name. If multiple AssemblyLines possess
	 * this name all of them will be stopped.
	 * 
	 * @param aAssemblyLineName
	 *            The name of the AssemblyLine to be stopped.
	 */
	public void stopAssemblyLine(String aAssemblyLineName) {
		stopThread(aAssemblyLineName);
	}

	/**
	 * Starts an AssemlbyLine given its name.
	 * 
	 * @param aAssemblyLineName
	 *            The name of the AssemlbyLine to be started.
	 * @return "true" if the AssemblyLine has been started successfully; "false" -
	 *         otherwise.
	 */
	public boolean startAssemblyLine(String aAssemblyLineName) {
		try {
			main.startAL(aAssemblyLineName);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * Given an AssemblyLine's name this method retrieves information about its
	 * Connectors.
	 * 
	 * @param aAssemblyLineName
	 *            The name of the AssemblyLine which configuration will be
	 *            retrieved.
	 * @return Vector which elements are instances of Hashtable. Each element
	 *         (Hashtable) corresponds to a single Connector from the
	 *         AssemblyLine and contains values for the keys represented by the
	 *         "CONN_" constants.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	public Vector getAssemblyLineConfiguration(String aAssemblyLineName)
			throws Exception {
		Vector connectors = new Vector();

		TreeMap list = (TreeMap) main.getConfiguration(FileConfig.C_TASK);
		TreeMap alProps = (TreeMap) list.get(aAssemblyLineName);

		if (alProps == null) {
			throw new Exception(res.getString("cannot.find.assemblyline",
					aAssemblyLineName));
		}

		Vector componentList = (Vector) alProps.get("componentlist");
		TreeMap components = (TreeMap) alProps.get("components");

		if ((componentList != null) && (components != null)) {
			for (int i = 0; i < componentList.size(); i++) {
				String compName = (String) componentList.get(i);
				TreeMap comp = (TreeMap) components.get(compName);

				if (comp == null) {
					throw new Exception(res.getString(
							"no.config.entry.for.component", compName));
				}

				Hashtable compProp = new Hashtable();

				// set CONN_NAME property
				compProp.put(ControlProcesses.CONN_NAME, compName);

				// set CONN_ENABLED_BOOL property
				String enabledValue = (String) comp.get("enabled");
				boolean compIsEnabled = ((enabledValue == null) || (enabledValue
						.equalsIgnoreCase("true")));
				compProp.put(ControlProcesses.CONN_ENABLED_BOOL, Boolean
						.valueOf(compIsEnabled));

				// set CONN_ENABLED property
				String enabledText;
				if (compIsEnabled) {
					enabledText = ControlProcesses.CONN_VALUE_ENABLED_TRUE;
				} else {
					enabledText = ControlProcesses.CONN_VALUE_ENABLED_FALSE;
				}
				compProp.put(ControlProcesses.CONN_ENABLED, enabledText);

				// set CONN_MODE property
				String compMode = (String) comp.get("type");
				compProp.put(ControlProcesses.CONN_MODE, compMode);

				if (compMode != null
						&& compMode.equals(ServerConstants
								.getTypeString(ServerConstants.TYPE_SCRIPT))) {
					compProp.put(ControlProcesses.CONN_TYPE, "-");
					compProp.put(ControlProcesses.CONN_PARSER, "-");
				} else {
					// set CONN_TYPE property
					TreeMap connConfig = (TreeMap) comp.get("connectorConfig");

					String connInheritFrom = null;

					if (connConfig != null) {
						connInheritFrom = (String) connConfig
								.get("inheritFrom");
					}

					if (connInheritFrom == null) {
						connInheritFrom = ServerConstants.VIRTUAL_CONNECTOR_NAME;
					}
					compProp.put(ControlProcesses.CONN_TYPE, connInheritFrom);

					// set CONN_PARSER property
					String connParser = null;

					if (connConfig != null) {
						connParser = (String) connConfig.get("parser");

					}

					if (connParser == null) {
						connParser = ControlProcesses.CONN_VALUE_NO_PARSER;
					}
					compProp.put(ControlProcesses.CONN_PARSER, connParser);
				}

				connectors.add(compProp);
			}
		}

		return connectors;
	}

	/**
	 * Retrieves general properties about the MI Server.
	 * 
	 * @return Hashtable which elements contain values for the keys represented
	 *         by the "SVR_" constants.
	 */
	public Hashtable getServerData() {
		Hashtable serverProps = new Hashtable();

		// set SVR_IP_ADDRESS property
		String ipAddress;
		try {
			ipAddress = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			ipAddress = ControlProcesses.SVR_VALUE_UNKNOWN;
		}
		serverProps.put(ControlProcesses.SVR_IP_ADDRESS, ipAddress);

		// set SVR_HOST_NAME property
		String hostName;
		try {
			hostName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			hostName = ControlProcesses.SVR_VALUE_UNKNOWN;
		}
		serverProps.put(ControlProcesses.SVR_HOST_NAME, hostName);

		// set SVR_OPERATING_SYSTEM property
		String operatingSystem = System.getProperty("os.name");
		if (operatingSystem == null) {
			operatingSystem = ControlProcesses.SVR_VALUE_UNKNOWN;
		} else {
			String osVersion = System.getProperty("os.version");
			if (osVersion != null) {
				operatingSystem = operatingSystem + " " + osVersion;
			}
		}
		serverProps.put(ControlProcesses.SVR_OPERATING_SYSTEM, operatingSystem);

		// set SVR_SERVER_VERSION property
		String serverVersion = Version.version();
		if (serverVersion == null) {
			serverVersion = ControlProcesses.SVR_VALUE_UNKNOWN;
		}
		serverProps.put(ControlProcesses.SVR_SERVER_VERSION, serverVersion);

		return serverProps;
	}

}
