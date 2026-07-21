/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc;

import java.util.Vector;

import com.ibm.di.entry.Entry;
import com.ibm.di.queue.MemBufferQ;
import com.ibm.di.queue.MemBufferQFactory;
import com.ibm.di.server.ResourceHash;

/**
 * @deprecated Use com.ibm.di.connector.MemQConnector or API
 * @see com.ibm.di.function.UserFunctions#newPipe(String, String, int)
 * @see com.ibm.di.function.UserFunctions#newPipe(String, String, int, int)
 * @see com.ibm.di.function.UserFunctions#getPipe(String, String)
 */
@Deprecated
public class MemBufferQFC extends Function {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "membufferqfc";

	/**
	 * The name of the pipe to be used by them MemQ
	 */
	private String pipeName;

	/**
	 * The memory queue that will be used
	 */
	private MemBufferQ memq;

	/**
	 * The factory object that creates, gets and checks for existence of the
	 * pipe
	 */
	private MemBufferQFactory memQFactory;

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash sResHash = null;

	static {
		sResHash = new ResourceHash(PROPERTIES_FILE);
	}

	/**
	 * initialize the function component. This method will be called through
	 * script. This method is/should be called once after the object has been
	 * given its configuration
	 * 
	 * @exception Exception
	 */
	public void initialize() throws Exception {

		// Call initialize of current class
		this.initialize(null);
	}

	/**
	 * initialize the function component This method is/should be called once
	 * after the object has been given its configuration
	 * 
	 * @param obj
	 *            object to initialize from
	 * 
	 * @exception Exception
	 */
	public void initialize(Object obj) throws Exception {
		super.initialize(obj);
		int size = getConfiguration().getIntegerParameter("watermark", 0);
		boolean paging = getConfiguration().getBooleanParameter("paging", true);
		String instName = getConfiguration().getStringParameter("instance");
		pipeName = getConfiguration().getStringParameter("pipes");
		memQFactory = MemBufferQFactory.getInstance(instName);

		String prop_createQueue;
		boolean createNew = true;
		prop_createQueue = (String) System
				.getProperty("tdi.memq.create.queue.default"); // Backward
		// compatibility
		if (prop_createQueue == null
				|| !prop_createQueue.equalsIgnoreCase("false")) {
			createNew = true; // New behavior introduced in TDI6.1 for
			// iterator mode.
		} else {
			createNew = false; // Switch back to old behavior of NOT
			// creating a
			// queue in iterator mode if queue doesn't exist.
		}

		if (memQFactory.queueExists(pipeName)) {
			memq = memQFactory.getPipe(pipeName);
		} else {
			if (createNew) {
				if (paging) {
					int pageSize = new Integer(getParam("pageSize").toString())
							.intValue();
					memQFactory.setDoNotInitDB(true);
					memq = memQFactory.newPipe(pipeName, size, pageSize);
					String tableName = getConfiguration().getStringParameter(
							"jdbcTable");
					if ((null == tableName) || (tableName.trim().length() == 0))
						tableName = "memq" + System.currentTimeMillis();

					memq.initDB(getConfiguration().getStringParameter(
							"jdbcSource"), getConfiguration()
							.getStringParameter("jdbcLogin"),
							getConfiguration().getStringParameter(
									"jdbcPassword"), tableName);

					String percent = (String) getParam("percentMemoryUsed");
					if ((null != percent) && !(("").equals(percent)))
						memq.setPercentMemoryUse(Integer.parseInt(percent));
					else
						memq.setPercentMemoryUse(50); // backward compatibility

				} else { // Backward compatibility
					memq = memQFactory.newPipe(pipeName, size);
					if (getConfiguration().getBooleanParameter("blockingAdd",
							true))
						memq.blockAdd(true);
					else
						memq.blockAdd(false);
				}
			} else {
				throw new Exception(sResHash.getString("QUEUE.DOES.NOT.EXIST",
						pipeName));
			}
		}

		if (getDebug()) {
			debug(sResHash.getString("FC.MEMBUFQ.WATERMARK.INFO", "" + size));
		}

	}

	/**
	 * execute the function
	 * 
	 * @param obj
	 *            input object
	 * @return output entry object
	 * @exception Exception
	 */
	public Object perform(Object obj) throws Exception {
		try {
			verifyInitialized();
		} catch (Exception e) {
			// not initialized so initilize it now
			initialize();
		}
		if (obj == null)
			return memq;
		if (obj instanceof Entry) {
			if (((Entry) obj).size() == 0)
				return memq.read();
			else {
				memq.write(obj);
				return obj;
			}
		}
		return null;

	}

	/**
	 * Gets active instances
	 * 
	 * @return active instances
	 */
	public Vector getInstances() {
		return MemBufferQFactory.getActiveInstances();
	}

	/**
	 * Gets pipes created in the specified instance
	 * 
	 * @param instName
	 *            instance name
	 * @return pipes created in the specified instance
	 */
	public Vector getPipeNames(String instName) {
		return MemBufferQFactory.getPipesForInstance(instName);
	}

	/**
	 * Return version information
	 * 
	 * @return The version value
	 */
	public String getVersion() {
		return "2.0-di7.1.1 %I% 20%E%";
	}

	/**
	 * Purges the queue. Wrapper over the MemBufferQ.purgeQueue()
	 * 
	 * @throws Exception
	 */
	public void purgeQueue() throws Exception {
		memq.purgeQueue();
	}

}
