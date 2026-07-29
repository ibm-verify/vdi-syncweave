/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import com.ibm.di.server.AssemblyLine;
import com.ibm.di.server.RSInterface;
import com.ibm.di.server.ResourceHash;

/**
 * This class is a tracker object, responsible for tracking the state of a
 * configInstance, assemblyLines objects.
 */
public class ProcessRegistry {
	/**
	 * Vector holding the config instances.
	 */
	private Vector<RSInterface> mConfigInstances = null;

	/**
	 * A Hashtable whose key elements are the Configuration Instances currently
	 * started, and the values are vectors containing all Assembly Lines
	 * currently started in the corresponding Configuration Instance.
	 * 
	 */
	// key is Config Instance (CI), value is Vector of the AssemblyLines running
	// on the CI
	private Hashtable<RSInterface, Vector<AssemblyLine>> mAssemblyLines = null;

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private final static ResourceHash sResHash = APIEngine.getResHash();

	/**
	 * Default constructor for this object.
	 */
	public ProcessRegistry() {
		mConfigInstances = new Vector<RSInterface>();

		mAssemblyLines = new Hashtable<RSInterface, Vector<AssemblyLine>>();
	}

	// notification calls

	/**
	 * Marks that the configuration instance has been started.
	 * 
	 * @param aConfigInstance
	 *            the started configInstance object.
	 * @throws DIException
	 *             if the <code>aConfigInstance</code> parameter is
	 *             <code>null</code>.
	 */
	public synchronized void configInstanceStarted(RSInterface aConfigInstance) throws DIException {
		if (aConfigInstance == null) {
			throw new DIException(sResHash.getString("SEVER.API.CONFIG.INSTANCE.IS.NULL.1"));
		}

		mConfigInstances.add(aConfigInstance);

		mAssemblyLines.put(aConfigInstance, new Vector<AssemblyLine>());
	}

	/**
	 * Marks that the configuration instance has been stopped.
	 * 
	 * @param aConfigInstance
	 *            the stopped configInstance object.
	 * @throws DIException
	 *             if the <code>aConfigInstance</code> parameter is
	 *             <code>null</code>.
	 */
	public synchronized void configInstanceStopped(RSInterface aConfigInstance) throws DIException {
		if (aConfigInstance == null) {
			throw new DIException(sResHash.getString("SEVER.API.CONFIG.INSTANCE.IS.NULL.2"));
		}

		mConfigInstances.remove(aConfigInstance);

		mAssemblyLines.remove(aConfigInstance);
	}

	/**
	 * Marks that the AssemblyLine instance has been started.
	 * 
	 * @param aAssemblyLine
	 *            the started AssemblyLine object.
	 * @throws DIException
	 *             if the provided <code>aAssemblyLine</code> parameter is
	 *             <code>null</code> or the configInstance parent of that
	 *             parameter is not registered.
	 */
	public synchronized void assemblyLineStarted(AssemblyLine aAssemblyLine) throws DIException {
		if (aAssemblyLine == null) {
			throw new DIException(sResHash.getString("SEVER.API.ASSEMBLYLINE.IS.NULL.1"));
		}

		RSInterface configInstance = aAssemblyLine.getParent();
		Vector<AssemblyLine> ciAssemblyLines = (Vector<AssemblyLine>) mAssemblyLines.get(configInstance);
		if (ciAssemblyLines == null) {
			throw new DIException(sResHash.getString("SEVER.API.CONFIG.INSTANCE.FOR.ASSEMBLYLINE.NOT.REGISTERED", aAssemblyLine
					.getName()));
		}

		ciAssemblyLines.add(aAssemblyLine);
	}

	/**
	 * Marks that the AssemblyLine instance has been terminated.
	 * 
	 * @param aAssemblyLine
	 *            the started AssemblyLine object.
	 * @throws DIException
	 *             if the provided <code>aAssemblyLine</code> parameter is
	 *             <code>null</code> or the configInstance parent of that
	 *             parameter is not registered.
	 */
	public synchronized void assemblyLineTerminated(AssemblyLine aAssemblyLine) throws DIException {
		if (aAssemblyLine == null) {
			throw new DIException(sResHash.getString("SEVER.API.ASSEMBLYLINE.IS.NULL.2"));
		}

		RSInterface configInstance = aAssemblyLine.getParent();
		Vector<AssemblyLine> ciAssemblyLines = mAssemblyLines.get(configInstance);
		if (ciAssemblyLines == null) {
			APIEngine.logInfo(sResHash.getString("SEVER.API.ON.AL.TERMINATE.CONFIG.INSTANCE.FOR.ASSEMBLYLINE.NOT.REGISTERED",
					aAssemblyLine.getName()));
		} else {
			ciAssemblyLines.remove(aAssemblyLine);
		}
	}

	// accessor calls

	/**
	 * Returns a vector containing all configuration instances currently
	 * started.
	 * 
	 * @return the list with the running config instance objects.
	 */

	@SuppressWarnings("unchecked")
	public synchronized Vector<RSInterface> getConfigInstances() {
		return (Vector<RSInterface>) mConfigInstances.clone();
	}

	/**
	 * @return a list containing the IDs of all the configuration instances
	 *         currently started.
	 */
	public synchronized List<String> getConfigInstanceIDs() {
		List<String> result = new ArrayList<String>(mConfigInstances.size());
		for (RSInterface rs : mConfigInstances) {
			result.add(rs.getName());
		}
		return result;
	}

	/**
	 * Returns a hashtable whose key elements are the Configuration Instances
	 * currently started, and the values are vectors containing all Assembly
	 * Lines currently started in the corresponding Configuration Instance.
	 * 
	 * @return the map between config instances and their AssemblyLines.
	 * 
	 */
	public synchronized Hashtable<RSInterface, Vector<AssemblyLine>> getAssemblyLines() {
		return cloneRunningTasks(mAssemblyLines);
	}

	/**
	 * Clones the AssemblyLine running on the CI
	 * 
	 * @param <T>
	 *            AssemblyLine
	 * @param aTasks
	 *            Hashtable whose key elements are the Configuration Instances
	 *            currently started, and the values are vectors containing all
	 *            AssemblyLine's currently started in the corresponding
	 *            Configuration Instance.
	 * @return a Hashtable with duplicate values of the provided by the
	 *         parameter.
	 */
	@SuppressWarnings("unchecked")
	private <T> Hashtable<RSInterface, Vector<T>> cloneRunningTasks(Hashtable<RSInterface, Vector<T>> aTasks) {

		Hashtable<RSInterface, Vector<T>> tasks = new Hashtable<RSInterface, Vector<T>>();

		Enumeration<RSInterface> configInstances = aTasks.keys();

		while (configInstances.hasMoreElements()) {
			RSInterface rawConfigInstance = configInstances.nextElement();
			Vector<T> configInstanceTasks = aTasks.get(rawConfigInstance);
			tasks.put(rawConfigInstance, (Vector<T>) configInstanceTasks.clone());
		}

		return tasks;
	}
}
