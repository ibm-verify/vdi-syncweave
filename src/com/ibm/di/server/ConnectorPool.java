/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.server;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.PoolDefConfig;
import com.ibm.di.connector.ConnectorInterface;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.server.Log;

import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

/**
 * This class is a pool for connectors. It stores certain amount and can give
 * them when needed. When not needed the connectors can be returned to the pool.
 * Also the pool is periodically purged, thus shrunk to a specified size.
 */
public class ConnectorPool {

	/**
	 * Copyright information.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * TMS Filename used for info, error and debug messages.
	 */
	private static final String PROPERTIES_FILE = "miserver";

	/**
	 * Connector pool name.
	 */
	private String mConnectorLibName;

	/**
	 * A default configuration for the pool.
	 */
	private PoolDefConfig mPoolDefConfig;

	/**
	 * Collection of free connectors in the pool.
	 */
	private Vector mFreeConnectors;

	/**
	 * Collection of connectors in use in the pool.
	 */
	private Vector mConnectorsInUse;

	/**
	 * The pool size.
	 */
	private int mPoolSize = 0;

	/**
	 * The maximum pool size allowed.
	 */
	private int mMaxPoolSize = 0;

	/**
	 * The minimum pool size allowed.
	 */
	private int mMinPoolSize = 0;

	/**
	 * Lock object for the pool.
	 */
	private Object mPoolLock = new Object();

	/**
	 * Count of attempts to initialize the pool.
	 */
	private int mInitializeAttempts = 0;

	/**
	 * Sleep interval used when several initialization attempts are made.
	 */
	private int mInitializeSleepInterval = 0;

	/**
	 * Specifies in what intervals should the pool be purged/cleaned.
	 */
	private int mPurgeInterval = 0;

	/**
	 * A boolean value specifying if the pool has been initialized.
	 */
	private boolean mIsInitialized = false;

	/**
	 * A boolean value specifying if the pool has been terminated.
	 */
	private boolean mIsTerminated = false;

	/**
	 * Timer used to schedule purging/ cleaning of the pool.
	 */
	private Timer mPurgeTimer;

	/**
	 * Log object used to log messages to log files.
	 */
	private Log mLog;

	/**
	 * Message Resource Hash used to access the TMS messages.
	 */
	private static ResourceHash sResHash = ResourceHash
			.getHash(PROPERTIES_FILE);

	/**
	 * Constructor of the connector pool.
	 * 
	 * @param aConnectorLibName
	 *            a connector pool name used for logging.
	 * @param aPoolDefConfig
	 *            a default configuration for the pool
	 * @param aLog
	 *            a Log object used to log messages to log files
	 * @throws Exception
	 */
	public ConnectorPool(String aConnectorLibName,
			PoolDefConfig aPoolDefConfig, Log aLog) throws Exception {

		if (aConnectorLibName == null) {
			String errorMessage = sResHash
					.getString("connector.library.name.null");
			throw new Exception(errorMessage);
		}
		if (aPoolDefConfig == null) {
			String errorMessage = sResHash.getString("connector.pool.def.null",
					aConnectorLibName);
			throw new Exception(errorMessage);
		}
		if (!aPoolDefConfig.getPoolEnabled()) {
			String errorMessage = sResHash.getString(
					"connector.pool.not.enabled", aConnectorLibName);
			throw new Exception(errorMessage);
		}
		if (aLog == null) {
			String errorMessage = sResHash.getString("connector.pool.log.null",
					aConnectorLibName);
			throw new Exception(errorMessage);
		}

		mConnectorLibName = aConnectorLibName;
		mPoolDefConfig = aPoolDefConfig;
		mLog = aLog;

		initialize();
	}

	/**
	 * Initializes the connector pool.
	 * 
	 * @throws Exception
	 *             if a problem occurs
	 */
	public void initialize() throws Exception {
		Trace.entrymin(this, "initialize");
		if (mIsInitialized) {
			Trace.exitmin(this, "initialize");
			return;
		}

		mFreeConnectors = new Vector();
		mConnectorsInUse = new Vector();

		mMaxPoolSize = mPoolDefConfig.getMaxPoolSize();
		if (mMaxPoolSize <= 0) {
			String errorMessage = sResHash.getString("invalid.max.pool.size",
					new Object[] { mConnectorLibName,
							Integer.toString(mMaxPoolSize) });
			throw new Exception(errorMessage);
		}

		mMinPoolSize = mPoolDefConfig.getMinPoolSize();
		if (mMinPoolSize > mMaxPoolSize) {
			mLog.warn("min.conn.pool.size.exceed.max", new Object[] {
					Integer.toString(mMinPoolSize), mConnectorLibName });

			mLog.warn("min.conn.pool.size.set.max", new Object[] {
					Integer.toString(mMaxPoolSize), mConnectorLibName });

			mMinPoolSize = mMaxPoolSize;
		}

		mInitializeAttempts = mPoolDefConfig.getInitializeAttempts();
		if (mInitializeAttempts < 1) {
			mLog.warn("invalid.init.attempts.num", new Object[] {
					Integer.toString(mInitializeAttempts), mConnectorLibName });

			mInitializeAttempts = 1;

			mLog.warn("init.attempts.num.set", new Object[] {
					Integer.toString(mInitializeAttempts), mConnectorLibName });
		}

		if (mInitializeAttempts > 1) {
			mInitializeSleepInterval = mPoolDefConfig
					.getInitializeSleepInterval();
			if (mInitializeSleepInterval < 0) {
				mLog.warn("invalid.init.sleep.interval", new Object[] {
						Integer.toString(mInitializeSleepInterval),
						mConnectorLibName });

				mInitializeSleepInterval = 0;
				mLog.warn("init.sleep.interval.set", new Object[] {
						Integer.toString(mInitializeSleepInterval),
						mConnectorLibName });
			}
			mInitializeSleepInterval = mInitializeSleepInterval * 1000;
		}

		mIsInitialized = true;

		if (mMinPoolSize > 0) {
			for (int i = 0; i < mMinPoolSize; i++) {
				try {
					ConnectorInterface conn = createNewConnector();
					addNewConnectorInPool(conn);
				} catch (Exception e) {
					String errorMessage = sResHash.getString(
							"error.prepare.conn.pool.instance", new Object[] {
									mConnectorLibName, e.toString() });
					mLog.error(errorMessage);
				}
			}
		}

		mPurgeInterval = mPoolDefConfig.getPurgeInterval();
		if ((mPurgeInterval > 0) && (mMinPoolSize < mMaxPoolSize)) {
			mPurgeTimer = new Timer();
			mPurgeTimer.schedule(new PoolShrinker(), mPurgeInterval * 1000,
					mPurgeInterval * 1000);

			mLog.debug("pool.shrinker.thread.started", new Object[] {
					mConnectorLibName, Integer.toString(mPurgeInterval) });
		}

		mLog.debug("connector.pool.inited", new Object[] { mConnectorLibName,
				Integer.toString(mPoolSize) });
		Trace.exitmin(this, "initialize");
	}

	/**
	 * Terminates the connector pool. Closes connections and frees resources
	 * taken from the free connectors in the pool. Also terminates the timer
	 * used to perform shrink operations.
	 * 
	 * @throws Exception
	 *             if a problem occurs
	 */
	public void terminate() throws Exception {
		Trace.entrymin(this, "terminate");
		if (!mIsInitialized) {
			Trace.exitmin(this, "terminate");
			return;
		}

		// terminate free Connectors
		synchronized (mPoolLock) {
			while (mFreeConnectors.size() > 0) {
				ConnectorInterface conn = (ConnectorInterface) mFreeConnectors
						.remove(0);
				mPoolSize--;
				try {
					conn.terminate();
				} catch (Exception e) {
					String errorMessage = sResHash.getString(
							"error.terminate.conn", e.toString());
					mLog.error(errorMessage);
				}
			}
			mIsTerminated = true;
		}

		// terminate the shrinker thread
		if (mPurgeTimer != null) {
			mPurgeTimer.cancel();
		}
		mLog.debug("connector.pool.terminated", mConnectorLibName);
		Trace.exitmin(this, "terminate");

	}

	/**
	 * Returns the name of the connector pool.
	 * 
	 * @return the connector pool's name
	 */
	public String getName() {
		return mConnectorLibName;
	}

	/**
	 * Returns the size of the pool (the count of connectors in it).
	 * 
	 * @return pool size
	 */
	public int getSize() {
		return mPoolSize;
	}

	/**
	 * Returns the count of the free connectors in the pool.
	 * 
	 * @return free connectors number
	 */
	public int getFreeConnectorsNum() {
		if (mFreeConnectors == null) {
			return 0;
		}

		return mFreeConnectors.size();
	}

	/**
	 * Returns the pool configuration.
	 * 
	 * @return the default pool configuration
	 */
	public PoolDefConfig getPoolConfig() {
		return mPoolDefConfig;
	}

	/**
	 * Adds a new connector to the pool.
	 * 
	 * @param aConnector
	 *            the connector to be added
	 * @throws Exception
	 *             if a problem occurs
	 */
	private void addNewConnectorInPool(ConnectorInterface aConnector)
			throws Exception {
		if (!mIsInitialized) {
			String errorMessage = sResHash.getString("conn.pool.not.init",
					mConnectorLibName);
			throw new Exception(errorMessage);
		}
		if (aConnector == null) {
			return;
		}

		if (mPoolSize == mMaxPoolSize) {
			String errorMessage = sResHash.getString("conn.pool.reached.max",
					mConnectorLibName);
			throw new Exception(errorMessage);
		}

		synchronized (mPoolLock) {
			mFreeConnectors.add(aConnector);
			mPoolSize++;
			if (mLog.isDebugEnabled()) {
				mLog.debug("connector.added.pool", new Object[] {
						mConnectorLibName, Integer.toString(mPoolSize),
						Integer.toString(mFreeConnectors.size()) });
			}
			mPoolLock.notifyAll();
		}
	}

	/**
	 * Returns a connector from the pool. Waits until a connector is returned to
	 * the pool and uses it.
	 * 
	 * @return a connector
	 * @throws Exception
	 *             if a problem occurs
	 */
	public ConnectorInterface getConnector() throws Exception {
		if (!mIsInitialized) {
			String errorMessage = sResHash.getString("conn.pool.not.init",
					mConnectorLibName);
			throw new Exception(errorMessage);
		}

		return getConnector(true);
	}

	/**
	 * Returns a connector from the pool if there is an available free one. If
	 * the <code>aWaitOnExhausted</code> option is used, this method will wait
	 * for a connector to be returned to the pool and use it. When a connector
	 * is taken from the pool it is no longer a free connector but a used one.
	 * 
	 * @param aWaitOnExhausted
	 *            whether to wait for a connector to be returned to the pool, or
	 *            take one directly
	 * @return a connector.
	 * @throws Exception
	 *             if a problem occurs
	 */
	public ConnectorInterface getConnector(boolean aWaitOnExhausted)
			throws Exception {
		if (!mIsInitialized) {
			String errorMessage = sResHash.getString("conn.pool.not.init",
					mConnectorLibName);
			throw new Exception(errorMessage);
		}

		ConnectorInterface conn = null;

		synchronized (mPoolLock) {
			while (aWaitOnExhausted && (mFreeConnectors.size() == 0)
					&& (mPoolSize == mMaxPoolSize)) {
				try {
					mPoolLock.wait();
				} catch (InterruptedException e) {
					mLog.debug("interupt.exception.wait.free.conn", e
							.toString());
				}
			}

			if (mFreeConnectors.size() > 0) {
				conn = (ConnectorInterface) mFreeConnectors.remove(0);
				mConnectorsInUse.add(conn);
				if (mLog.isDebugEnabled()) {
					mLog.debug("conn.taken.from.pool", new Object[] {
							mConnectorLibName, Integer.toString(mPoolSize),
							Integer.toString(mFreeConnectors.size()) });
				}
			} else if (mPoolSize < mMaxPoolSize) {
				conn = createNewConnector();
				mConnectorsInUse.add(conn);
				mPoolSize++;
				if (mLog.isDebugEnabled()) {
					mLog.debug("connector.added.pool.request", new Object[] {
							mConnectorLibName, Integer.toString(mPoolSize),
							Integer.toString(mFreeConnectors.size()) });
				}
			}
		}
		return conn;
	}

	/**
	 * Returns a connector to the pool and enlists it again in the free
	 * connectors.
	 * 
	 * @param aConnector
	 *            the returned connector
	 * @throws Exception
	 *             if a problem occurs
	 */
	public void returnConnector(ConnectorInterface aConnector) throws Exception {
		if (!mIsInitialized) {
			String errorMessage = sResHash.getString("conn.pool.not.init",
					mConnectorLibName);
			throw new Exception(errorMessage);
		}

		if (!mConnectorsInUse.contains(aConnector)) {
			return;
		}

		synchronized (mPoolLock) {
			mConnectorsInUse.remove(aConnector);

			if (!mIsTerminated) {
				mFreeConnectors.add(aConnector);
				if (mLog.isDebugEnabled()) {
					mLog.debug("connector.returned.pool", new Object[] {
							mConnectorLibName, Integer.toString(mPoolSize),
							Integer.toString(mFreeConnectors.size()) });
				}
				mPoolLock.notifyAll();
			} else {
				try {
					aConnector.terminate();
				} catch (Exception e) {
					String errorMessage = sResHash.getString(
							"error.terminate.conn", e.toString());
					mLog.error(errorMessage);
				}
				mPoolSize--;

				mFreeConnectors.add(aConnector);
				if (mLog.isDebugEnabled()) {
					mLog.debug("connector.returned.pool.termination", new Object[] {
							mConnectorLibName, Integer.toString(mPoolSize) });
				}
			}
		}
	}

	/**
	 * Creates a new connector. If there if a problem in the initialization of
	 * the connector the method may wait and try again until the maximin
	 * attempts count is reached.
	 * 
	 * @return the created connector
	 * @throws Exception
	 *             if a problem occurs
	 */
	private ConnectorInterface createNewConnector() throws Exception {
		if (!mIsInitialized) {
			String errorMessage = sResHash.getString("conn.pool.not.init",
					mConnectorLibName);
			throw new Exception(errorMessage);
		}

		ConnectorInterface conn = SystemFunctions.loadConnector(
				(ConnectorConfig) mPoolDefConfig.getParent(), null);

		int attempts = 0;
		Exception initException = null;
		boolean initialized = false;
		do {
			try {
				attempts++;
				conn.initialize(null);
				initialized = true;
			} catch (Exception e) {
				initException = e;
				String errorMessage = sResHash.getString(
						"error.init.conn.attempt", new Object[] {
								Integer.toString(attempts), e.toString() });
				mLog.error(errorMessage);

				if ((attempts < mInitializeAttempts)
						&& (mInitializeSleepInterval > 0)) {
					try {
						Thread.sleep(mInitializeSleepInterval);
					} catch (InterruptedException exInt) {
						mLog.debug("interupt.exception.init.attept", exInt
								.toString());
					}
				}
			}
		} while (!initialized && (attempts < mInitializeAttempts));

		if (!initialized) {
			String errorMessage = sResHash.getString(
					"could.not.init.pooled.conn", new Object[] {
							mConnectorLibName, Integer.toString(attempts) });
			mLog.error(errorMessage);
			if (initException != null) {
				errorMessage += ": " + initException.toString();
			}
			throw new Exception(errorMessage);
		}

		mLog.debug("new.pooled.conn.created", mConnectorLibName);
		return conn;
	}

	/**
	 * Removes connectors from the pool and the free connectors collection until
	 * the minimum size of the pool is reached.
	 */
	public void purge() {
		Trace.entrymax(this, "purge");
		synchronized (mPoolLock) {
			int oldPoolSize = mPoolSize;
			int oldFreeConnectors = mFreeConnectors.size();

			while ((mPoolSize > mMinPoolSize) && (mFreeConnectors.size() > 0)) {
				ConnectorInterface conn = (ConnectorInterface) mFreeConnectors
						.remove(0);
				mPoolSize--;
				try {
					conn.terminate();
				} catch (Exception e) {
					String errorMessage = sResHash.getString(
							"error.terminate.conn", e.toString());
					mLog.error(errorMessage);
				}
			}

			if (mLog.isDebugEnabled()) {
				mLog.debug("conn.pool.purge", new Object[] { mConnectorLibName,
						Integer.toString(oldPoolSize),
						Integer.toString(oldFreeConnectors),
						Integer.toString(mPoolSize),
						Integer.toString(mFreeConnectors.size()) });
			}
		}
		Trace.exitmax(this, "purge");
	}

	/**
	 * A Timer class used to shrink the size of the pool, purging its content on
	 * some schedule.
	 */
	private class PoolShrinker extends TimerTask {
		/**
		 * {@inheritDoc}
		 * 
		 */
		public void run() {
			purge();
		}
	}

}
