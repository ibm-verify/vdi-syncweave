/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.server;

import java.sql.SQLException;
import com.ibm.di.config.base.FunctionConfigImpl;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.SimulationConfig;
import com.ibm.di.connector.Connector;
import com.ibm.di.connector.ConnectorInterface;
import com.ibm.di.exceptions.SkipEntryException;
import com.ibm.di.fc.DeltaFC;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.parser.ParserInterface;

/**
 * This class represents a component extending the functionality of the
 * AssemblyLineComponent. It uses the Delta FC logic to commit and rollback
 * transactions to database.
 */
public class CSDeltaTaskComponent extends AssemblyLineComponent {
	/**
	 * Copyright information.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Delta FC instance used to access the Delta store.
	 */
	private DeltaFC deltaFC = new DeltaFC();

	/**
	 * Constructor.
	 *
	 * @param parent
	 *            the parent AssemblyLine
	 * @param name
	 *            the component name
	 * @param config
	 *            the connector configuration
	 * @param conn
	 *            the connector
	 * @param isRestarting
	 *            ignored
	 * @throws Exception
	 *             if a problem occurs
	 */
	public CSDeltaTaskComponent(AssemblyLine parent, String name, ConnectorConfig config, ConnectorInterface conn,
			boolean isRestarting) throws Exception {
		this(parent, name, config, conn, isRestarting, false);
	}

	/**
	 * Constructor.
	 *
	 * @param parent
	 *            the parent AssemblyLine
	 * @param name
	 *            the component name
	 * @param config
	 *            the connector configuration
	 * @param conn
	 *            the connector
	 * @param isRestarting
	 *            ignored
	 * @param forceRuntime
	 *            whether to use runtime provided connector
	 * @throws Exception
	 *             if a problem occurs
	 */
	public CSDeltaTaskComponent(AssemblyLine parent, String name, ConnectorConfig config, ConnectorInterface conn,
			boolean isRestarting, boolean forceRuntime) throws Exception {
		super(parent, name, config, conn, forceRuntime);

		// If simulation state is set to Simulated/Proxy/Scripted
		// do not commit anything automatically; leave this to the user
		if (parent.isSimulating() && !(getSimulatingState().equalsIgnoreCase(SimulationConfig.SIM_ENABLED_STATE)))
			config.getDeltaConfig().setWhenToCommit("No autocommit");

	}

	/**
	 * Initialize the delta FC.
	 */
	private void initDeltaFC() throws Exception {
		FunctionConfig fc = new FunctionConfigImpl();
		fc.setMetamergeConfig(config.getMetamergeConfig());
		fc.init();
		fc.setName(getName());

		// copy all parameters from the DeltaConfig to the FunctionConfig
		fc.getFunctionConfig().setData(config.getDeltaConfig().getData());

		fc.getFunctionConfig().setParameter("javaclass", "com.ibm.di.fc.DeltaFC");

		deltaFC = (DeltaFC) SystemFunctions.loadFunction(fc, log);
		deltaFC.setRSInterface(parent.getParent());

		// special parameter to signal the DeltaFC we are using it
		deltaFC.setParam("iteratorDelta", "true");

		deltaFC.initialize(null);
		deltaFC.setContext(parent);
	}

	/**
	 * Determines whether the delta of the given connector configuration is
	 * enable.
	 *
	 * @param config
	 *            a connector configuration
	 * @return <code>true</code> if it is enabled, otherwise <code>false</code>
	 */
	public static boolean deltaEnabled(ConnectorConfig config) {
		return config.getDeltaConfig().getEnabled();
	}

	/**
	 * This method calls the appropriate hooks and the connector's initialize
	 * method. The Delta Engine is also initialized.
	 *
	 * @throws Exception
	 *             if the connector's initialization fails or an error in one of
	 *             the executed hooks occurred.
	 */
	void doInitialize() throws Exception {
		Trace.entrymax(this, "doInitialize");

		if (is_initialized) {
			return;
		}

		String limit = config.getLimitOption();
		if (limit != null && limit.length() > 0) {
			maxRead = Integer.parseInt(limit);
		} else {
			maxRead = 0;
		}

		boolean exceptionHandled = false;

		try {
			handler.pushStackFrame(this);

			trigger("before_initialize", parent.getCurrentWork());

			if (connector != this.input_connector) {
				if (!pooledConnector) {
					log.debug("initialize.connector");
					try {
						shouldTerminate = true;
						connector.initialize(new ConnectorMode(getType()));
					} catch (Throwable error) {
						exceptionHandled = true;
						handleException(INITIALIZE, error, parent.getCurrentWork());
						exceptionHandled = false;
					}
				}
			} else {
				log.debug("assemblyline.comp.dontinit.connector.info");
			}

			try {
				initDeltaFC();
			} catch (Throwable error) {
				exceptionHandled = true;
				handleException(INITIALIZE, error, parent.getCurrentWork());
				exceptionHandled = false;
			}

			connector.registerScriptBeans(parent.getScriptEngine());

			log.debug("initialize.iterator");
			try {
				doConnectorSelectEntries();
			} catch (Throwable error) {
				exceptionHandled = true;
				handleException(SELECT, error, parent.getCurrentWork());
				exceptionHandled = false;
			}

			if (connector instanceof Connector
					&& connector != this.input_connector) {
				log.debug("assemblyline.comp.connector.info", connector
						.getClass().getName(), ((Connector) connector)
						.getVersion());
				ParserInterface p = ((Connector) connector).getParser();
				if (p instanceof VersionInfoInterface)
					log
					.debug("assemblyline.comp.parser.info", p
							.getClass().getName(),
							((VersionInfoInterface) p).getVersion());
			}

			trigger("after_initialize", parent.getCurrentWork());

			is_initialized = true;
			log.debug("end.initialize");
			initializeCount++;
		} catch (Throwable error) {
			if (!exceptionHandled)
				handleException(INITIALIZE_HOOKS, error, parent.getCurrentWork());
			else if (error instanceof Exception)
				throw (Exception) error;
			else
				throw new Exception(error);
		} finally {
			handler.popStackFrame();
		}
		Trace.exitmax(this, "doInitialize");
	}

	//Defect #15365
	public void doConnectorInitialize() throws Exception {
			try {
				handler.pushStackFrame(this);
				trigger("before_initialize", parent.getCurrentWork());
				if (!pooledConnector) {
					try {
						shouldTerminate = true;
						connector.initialize(new ConnectorMode(getType()));
					} catch (Throwable error) {
						handleException(INITIALIZE, error, parent.getCurrentWork());
					}
				}

				try {
					initDeltaFC();
				} catch (Throwable error) {
					handleException(INITIALIZE, error, parent.getCurrentWork());
				}

				connector.registerScriptBeans(parent.getScriptEngine());

				try {
					doConnectorSelectEntries();
				} catch (Throwable error) {
					handleException(SELECT, error, parent.getCurrentWork());
				}

				trigger("after_initialize", parent.getCurrentWork());
				is_initialized = true;

			} catch (Throwable error) {
				handleException(INITIALIZE_HOOKS, error, parent.getCurrentWork());
			} finally {
				handler.popStackFrame();
			}
		}

	/**
	 * This method calls the appropriate hooks and the connector's terminate
	 * method. The DeltaStore is also closed.
	 *
	 * @throws Exception
	 *             if a problem occurs
	 */
	public void doConnectorTerminate() throws Exception {
		if (handler == null || connector == null)
			return; // No handler or no connector means nothing to terminate.

		try {
			handler.pushStackFrame(this);
			trigger("before_close", parent.getResult(), null);

			if (is_initialized && deltaFC != null) {
				log.loginfo(deltaFC.getStatisticsString());
				deltaFC.closeDelta();
			}

			is_initialized = false;
			if (pooledConnector) {
				connPool.returnConnector(connector);
			} else if (shouldTerminate) {
				shouldTerminate = false;
				connector.terminate();
			}
			trigger("after_close", parent.getResult(), null);
		} catch (Exception err) {
			handleException("close", err, parent.getResult());
		} finally {
			handler.popStackFrame();
		}
	}

	/**
	 * The method returns the next entry from the connector. It is called by the
	 * {@link #getnext()} method if we are working in Iterator mode.
	 *
	 * @param work
	 *            the work entry to fill in
	 * @return the work entry filled with the next input entry
	 * @throws Exception
	 *             if a problem occurs
	 */
	public com.ibm.di.entry.Entry getnext(com.ibm.di.entry.Entry work) throws Exception {
		com.ibm.di.entry.Entry tmp = null;
		while (true) {
			// First do delta, later merge the info into work.
			tmp = deltaFC.isReadingDeleted() ? null : super.getnext(new com.ibm.di.entry.Entry());
			try {
				tmp = (com.ibm.di.entry.Entry) deltaFC.perform(tmp);
				break;
			} catch (SkipEntryException e) {
				stats.skip();
				numRead --;
			}
		}
		if (tmp == null)
			return null;

		// Do we need to remove old Attributes if they were not found?
		boolean shouldRemove = (work.size() > 0);

		// Merge the delta info into work
		work.merge(tmp, false);
		char op = tmp.getOp();
		work.setOp(op);
		
		// Set the delta.old property if needed
		if (op == com.ibm.di.entry.Entry.OP_DEL 
				|| op == com.ibm.di.entry.Entry.OP_UNCHANGED)
			work.setProperty("delta.old", tmp);
		else if (op == com.ibm.di.entry.Entry.OP_ADD)
			work.setProperty("delta.old", null);

		if (shouldRemove) {
			// Remove attributes from work that are in the map, but not in tmp.
			for (String attrName:config.getAttributeMap(true).getAttributeNames()) {
				if (tmp.getAttribute(attrName) == null)
					work.removeAttribute(attrName);
			}
		}

		// TODO: Change this to use a new counter in TaskStatistics
		// Add the fact that we read a "deleted" entry to the stats
		if (deltaFC.isReadingDeleted())
			stats.get();
		
		return work;
	}

	/**
	 * Return the next deleted entry.
	 *
	 * @param work
	 *            the work entry to fill in
	 * @return the work entry filled with the next input entry
	 * @throws Exception
	 *             if a problem occurs
	 */
	public com.ibm.di.entry.Entry nextDeletedEntry(com.ibm.di.entry.Entry work) throws Exception {
		return deltaFC.nextDeletedEntry(work);
	}

	/**
	 * Commit the last transactions in Derby database
	 *
	 * @exception SQLException
	 *                Thrown if an error occurs
	 */
	public void commitDeltaState() throws SQLException {
		if (deltaFC != null)
			deltaFC.commitDeltaState();
	}

	/**
	 * Rollback the last transactions in Derby database. The operation will NOT be executed if at
	 * the moment the Delta Store is iterating deleted entries.
	 * 
	 * @see #markEntryInDeltaStore(com.ibm.di.entry.Entry)
	 * @exception SQLException
	 *                Thrown if an error occurs
	 */
	public void rollbackDeltaState() throws SQLException {
		if (deltaFC != null)
			deltaFC.rollbackDeltaState();
	}

	/**
	 * Commit the last transactions in Derby database (alias for
	 * commitDeltaState)
	 *
	 * @exception SQLException
	 *                Thrown if an error occurs
	 */
	public void saveDeltaState() throws SQLException {
		if (deltaFC != null)
			deltaFC.saveDeltaState();
	}

	/**
	 * Commit if in commit mode "On end of AL cycle"
	 *
	 * @exception SQLException
	 *                Thrown if an error occurs
	 */
	public void commitOnEndIter() throws SQLException {
		deltaFC.commitOnEndIter();
	}

	/**
	 * Marks an Entry in the Delta Store. This can be useful if the current
	 * change can not be propagated properly, and you want to roll back the
	 * delta state. You can then use code like this, assuming this Component is
	 * called MyIterator:
	 *
	 * <pre>
	 * MyIterator.rollbackDeltaState();
	 * MyIterator.markEntryInDeltaStore(work);
	 * MyIterator.commitDeltaState();
	 * </pre>
	 *
	 * @param work
	 *            The Entry that contains the key information
	 * @return true if the Entry contained a meaningful key and could be marked
	 *         in the delta store
	 */
	public boolean markEntryInDeltaStore(com.ibm.di.entry.Entry work) {
		return deltaFC.markEntryInDeltaStore(work);
	}
}
