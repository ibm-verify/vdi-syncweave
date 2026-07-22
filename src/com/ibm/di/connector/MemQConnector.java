/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.entry.Entry;
import com.ibm.di.queue.DBHandler;
import com.ibm.di.queue.MemBufferQ;
import com.ibm.di.queue.MemBufferQFactory;
import com.ibm.di.server.ConnectorMode;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.ServerConstants;

/**
 * This class is a connector that wraps over the Memory Buffer Queue
 * infrastructure.
 */
public class MemQConnector extends Connector implements ConnectorInterface {

	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Name of the properties file
	 */
	private static final String PROPERTIES_FILE = "memqconnector";

	/**
	 * Name of the component
	 */
	private static final String CONNECTOR_NAME = "MemQ Connector";

	/**
	 * memory buffer queue
	 */
	private MemBufferQ memq;

	/**
	 * timeout parameter
	 */
	private int timeout = 0;

	/**
	 * This is the maximum size of the queue
	 */
	private int watermark = 100;

	/**
	 * Resource hash object for accessing TMS messages
	 */
	private static ResourceHash sResHash = null;

	/**
	 * name of the lock for releasing after single read
	 */
	private static final String LOCK_RELEASE_SINGLE = "After single read";
	/**
	 * name of the lock for releasing after end of AL cycle
	 */
	private static final String LOCK_RELEASE_AL_CYCLE_END = "End of AL cycle";
	/**
	 * name of the lock for releasing on connector close
	 */
	private static final String LOCK_RELEASE_CONNECTOR_CLOSE = "Connector close";

	/**
	 * When the connector is in iterator mode, this determines when the read
	 * lock on the specified memory queue will be released.
	 */
	private String readLockRelease = null;

	/**
	 * flag for releasing lock on end of cycle
	 */
	private boolean isReleaseOnALCycleEnd = false;

	/**
	 * Flag for determining whether this queue will be deleted from memory when
	 * the terminate method is called.
	 */
	private boolean deleteQueueOnTerminate = false;

	/**
	 * mode of the connector - iterator or add only
	 */
	private int mode = -1;

	static {
		sResHash = new ResourceHash(PROPERTIES_FILE);
	}

	/**
	 * Class constructor
	 */
	public MemQConnector() {
		super();
		setName(CONNECTOR_NAME);
		setModes(new String[] { ConnectorConfig.ADDONLY_MODE,
				ConnectorConfig.ITERATOR_MODE });
	}

	/**
	 * Method reads initialization params ,creates new pipe and calls initDB if
	 * persistence is enabled. Checks if user has enabled property to use
	 * earlier behavior of MEMQConnector in iterator mode where no new queue
	 * will be created if queue does not exist.
	 * 
	 * @param o
	 *            The connector mode of the Connector. The object should be of
	 *            type ConnectorMode.
	 * 
	 * @throws Exception
	 */
	public void initialize(Object o) throws Exception {

		String prop_createQueue = null;
		boolean createNew = true;
		String paramTimeout = getParam("timeout");

		if ((null != paramTimeout) && (!"".equals(paramTimeout.trim()))) {
			try {
				timeout = new Integer(paramTimeout).intValue();
			} catch (Exception ex) {
				throw new Exception(sResHash
						.getString("CONNECTOR.MEMQ.TIMEOUT.INVALID.VALUE"));
			}

		} else
			timeout = 0;

		readLockRelease = getParam("readLockRelease");

		prop_createQueue = System.getProperty("tdi.memq.create.queue.default");// For
		// backward
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
		String instName = getParam("instanceName");
		String pipeName = getParam("queueName");
		if ("".equals(pipeName))
			pipeName = null;

		if (pipeName == null) {
			throw new Exception(sResHash
					.getString("CONNECTOR.MEMQ.QUEUENAME.MISSING"));
		}

		MemBufferQFactory memQFactory = MemBufferQFactory.getInstance(instName);

		if (o instanceof ConnectorMode) {
			mode = ((ConnectorMode) o).getMode();
		}

		if (memQFactory.queueExists(pipeName)) {
			memq = memQFactory.getPipe(pipeName);

			if (mode == ServerConstants.TYPE_ADDONLY) {
				DBHandler dbHandler = memq.getDbHandler();
				if (null == dbHandler) {// this means the queue was initialized
					// by a reader thread and so no
					// dbproperties are set.
					String tableName = getParam("jdbcTable");
					if ((tableName == null) || (("").equals(tableName))) {
						tableName = "memq" + System.currentTimeMillis();
					}
					memq.initDB(getParam("jdbcSource"), getParam("jdbcLogin"),
							getParam("jdbcPassword"), tableName);
				}
			}

		} else {
			if (mode == ServerConstants.TYPE_ITERATOR) {
				/*
				 * Check if in Iterator mode, if queue exist return else check
				 * if user has set the tdi.memq.create.queue.default property to
				 * false.
				 */
				if (createNew) {
					// default behavior which forces creation
					// of new queue if it does not exist
					createQueue(memQFactory, pipeName);
				} else {
					throw new Exception(sResHash.getString(
							"QUEUE.DOES.NOT.EXIST", pipeName));
				}

			} else {  // Addonly mode
				// Default values will be set from the SystemStoreConnector
				// for other DB properties only JDBC table has to be specified
				// if ((getParam("jdbcTable") == null)
				// || (("").equals(getParam("jdbcTable")))) {
				// throw new Exception(sResHash
				// .getString("CONNECTOR.MEMQ.JDBCTABLE.MISSING"));
				// }
				createQueue(memQFactory, pipeName);
			}
		}
	}

	/**
	 * Method creates instance of new pipe with given pipeName.
	 * 
	 * @param memQFactory
	 * @param pipeName
	 * @throws Exception
	 */
	private void createQueue(MemBufferQFactory memQFactory, String pipeName)
			throws Exception {
		// As we created this queue we must delete it
		// from memory when connector terminates
		deleteQueueOnTerminate = true;

		if (getParam("paging").equals("true")) {
			String str = getParam("watermark");

			if ((str != null) && (!("").equals(str)))
				try {
					watermark = Integer.parseInt(str);
				} catch (Exception ex) {
					throw new Exception(
							sResHash
									.getString("CONNECTOR.MEMQ.WATERMARK.INVALID.VALUE"));
				}
			else
				watermark = 1000;
			int pageSize = 0;
			if (null == getParam("pageSize")
					|| ("".equals(getParam("pageSize"))))
				pageSize = 100;
			else {
				try {
					pageSize = Integer.parseInt(getParam("pageSize"));
				} catch (Exception ex) {
					throw new Exception(sResHash
							.getString("CONNECTOR.MEMQ.PAGESIZE.INVALID.VALUE"));
				}
			}

			// if ((getParam("jdbcTable") == null)
			// || (("").equals(getParam("jdbcTable"))))
			// memQFactory.setDoNotInitDB(false);
			// else

			memQFactory.setDoNotInitDB(true);

			memq = memQFactory.newPipe(pipeName, watermark, pageSize);

			String percent = getParam("percentMemoryUsed");
			int percentInt = 0;
			if ((null != percent) && !(("").equals(percent))) {
				try {
					percentInt = Integer.parseInt(percent);

				} catch (Exception ex) {
					throw new Exception(
							sResHash
									.getString("CONNECTOR.MEMQ.PERCENT_MEM_USE.INVALID.VALUE"));
				}

				if (percentInt < 0 || percentInt > 100)
					throw new Exception(
							sResHash
									.getString("CONNECTOR.MEMQ.PERCENT_MEM_USE.INVALID.PERCENTAGE.VALUE"));
				memq.setPercentMemoryUse(percentInt);
			} else
				memq.setPercentMemoryUse(50); // backward compatibility

			// If this a connector in iterator mode,then do not initialise the
			// db properties
			if (mode == ServerConstants.TYPE_ADDONLY) {
				String tableName = getParam("jdbcTable");
				if ((tableName == null) || (("").equals(tableName))) {
					tableName = "memq" + System.currentTimeMillis();
				}
				memq.initDB(getParam("jdbcSource"), getParam("jdbcLogin"),
						getParam("jdbcPassword"), tableName);
			}
		} else {
			// This option will not be available from 7.0- backward
			// compatibility
			memq = memQFactory.newPipe(pipeName, watermark);
			if (getParam("blockingAdd").equals("true"))
				memq.blockAdd(true);
			else
				memq.blockAdd(false);
		}
	}

	/**
	 * Default implementation
	 * 
	 * @throws Exception never
	 */
	public void selectEntries() throws Exception {

	}

	/**
	 * Returns the next entry obj
	 * 
	 * @return the next entry
	 * @throws Exception
	 *             if an error occurs
	 */
	public Entry getNextEntry() throws Exception {

		boolean singleRead = true;
		if ((null == readLockRelease) || (("").equals(readLockRelease))) // Backward
			// compatibility
			readLockRelease = LOCK_RELEASE_SINGLE;

		if (readLockRelease.trim().equals(LOCK_RELEASE_AL_CYCLE_END))
			isReleaseOnALCycleEnd = true;

		if (readLockRelease.trim().equals(LOCK_RELEASE_SINGLE))
			singleRead = true;
		// Get lock on memq
		acquireLock();

		Object obj = memq.read(timeout);
		if (singleRead)
			releaseLock();

		if (obj != null) {
			if (obj instanceof Entry) {
				return (Entry) obj;
			} else
				throw new Exception(sResHash.getString(
						"CONNECTOR.MEMQ.NONENTRYOBJECT.EXCEPTION", obj));
		} else
			return null;

	}

	/**
	 * Adds the given entry object to the MemQ
	 * 
	 * @param entry
	 *            entry to be added
	 * @throws Exception
	 *             if an error occurs
	 * 
	 */
	public void putEntry(Entry entry) throws Exception {
		memq.write(entry);
	}

	/**
	 * Sets the time out parameter
	 * 
	 * @param timeout
	 *            value to be set
	 * 
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	// public void enablePersistence(boolean enable) {
	// this.persistence = enable;
	// }

	/**
	 * Checks if the specified memq has more entries
	 * 
	 * @return true if not empty
	 */
	public boolean hasMore() {
		return !memq.isEmpty();
	}

	/**
	 * Returns version information
	 * 
	 * @return version info
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

	/**
	 * Method is called when the Connector terminates
	 * 
	 * @throws Exception
	 *             never
	 */
	public void terminate() throws Exception {
		if (getParam("readLockRelease").trim().equals(
				LOCK_RELEASE_CONNECTOR_CLOSE))
			releaseLock();
		if (deleteQueueOnTerminate) {
			// Delete the queue if it was created by this connector
			MemBufferQFactory memQFactory = MemBufferQFactory
					.getInstance(getParam("instanceName"));
			memQFactory.deleteQueue(getParam("queueName"));
		} else {
			memq.terminate(true);
		}
	}

	/**
	 * Acquire lock on memq
	 * 
	 * @throws InterruptedException
	 */
	public void acquireLock() throws InterruptedException {
		memq.acquireLock();
	}

	/**
	 * Release the lock on memq
	 * 
	 */
	public void releaseLock() {
		memq.releaseLock();
	}

	/**
	 * Checks if lock is to be released on AL cycle end
	 * 
	 * @return true if it should be, false otherwise
	 */
	public boolean isReleaseOnALEnd() {
		return isReleaseOnALCycleEnd;
	}

	/**
	 * Sets the isReleaseOnALCycleEnd variable
	 * 
	 * @param isReleaseOnALCycleEnd
	 */
	public void setReleaseOnALCycleEnd(boolean isReleaseOnALCycleEnd) {
		this.isReleaseOnALCycleEnd = isReleaseOnALCycleEnd;
	}

	/**
	 * Returns the mode of the connector
	 * 
	 * @return the number of the mode
	 */
	public int getMode() {
		return mode;
	}

	/**
	 * Sets the mode of the connector. Can be Add Only and Iterator.
	 * 
	 * @param mode
	 *            value to set
	 */
	public void setMode(int mode) {
		this.mode = mode;
	}
}
