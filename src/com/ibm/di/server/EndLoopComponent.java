/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.server;

/**
 * This class is used by the Assemblyline to mark the end of a loop
 */
public class EndLoopComponent extends AssemblyLineComponent {

	/**
	 * A LoopComponent that is null for null for the StartLoop instance and
	 * non-null for the EndLoop instance.
	 */
	private LoopComponent startLoop;

	/**
	 * TMS Filename used for info, error and debug messages.
	 */
	private static final String PROPERTIES_FILE = "miserver";

	/**
	 * ResourceHash used for access of the TMS messages.
	 */
	private static ResourceHash sResHash = ResourceHash
			.getHash(PROPERTIES_FILE);

	/**
	 * This constructor is used to insert the EndLoop component in the AL
	 * 
	 * @param parent
	 *            The AssemblyLine this EndLoopComponent is part of
	 * @param loop
	 *            The LoopComponent that we end
	 */
	public EndLoopComponent(AssemblyLine parent, LoopComponent loop) {
		setName(loop.getName() + ".endloop");
		this.startLoop = loop;
		this.parent = parent;
		this.log = parent.getLog();
	}

	/**
	 * This method does nothing.
	 */
	public void initialize() {
	}

	/**
	 * This method always returns true.
	 * 
	 * @param work
	 *            an Entry object
	 * @return <code>true</code>
	 */
	public boolean willExecute(com.ibm.di.entry.Entry work) {
		return true;
	}

	/**
	 * Return the type of this component
	 * 
	 * @return ServerConstants.TYPE_LOOP
	 */
	public int getType() {
		return ServerConstants.TYPE_LOOP;
	}

	/**
	 * Tells the AssemblyLine to go back to the start of the loop
	 * 
	 * @param meta
	 *            The work Entry. Ignored
	 * @throws Exception
	 *             if we cannot go back to the start of the loop
	 */
	public void add(com.ibm.di.entry.Entry meta) throws Exception {
		log.debug("loopend.set.next.connector", startLoop);
		if (!parent.setNextConnector(startLoop)) {
			throw new Exception(sResHash.getString("cannot.set.next.connector"));
		}
		startLoop.setFirstLoop(false);
	}

	/**
	 * Handles Exceptions by rethrowing them
	 * 
	 * @param oper
	 *            Ignored
	 * @param e
	 *            The Throwable that is rethrown
	 * @param meta
	 *            The work Entry. Ignored
	 * @throws Exception
	 *             e
	 */
	public void handleException(String oper, Throwable e,
			com.ibm.di.entry.Entry meta) throws Exception {
		if (e instanceof Exception)
			throw (Exception) e;
		else
			throw new Exception(e);
	}

	/**
	 * Calls Hooks. There are no Hooks.
	 * 
	 * @param oper
	 *            a Srting for the operation
	 * @param work
	 *            an Entry object
	 * @param conn
	 *            an Entry object
	 * @return always <code>false</code>
	 */
	public boolean trigger(String oper, com.ibm.di.entry.Entry work,
			com.ibm.di.entry.Entry conn) {
		return false;
	}

	/**
	 * Calls Hooks. There are no Hooks.
	 * 
	 * @param oper
	 *            an Entry object
	 * @param work
	 *            an Entry object
	 * @return always <code>false</code>
	 */
	public boolean trigger(String oper, com.ibm.di.entry.Entry work) {
		return false;
	}

	/**
	 * Releases resources
	 * 
	 * @throws Exception
	 *             if a problem occurs
	 */
	public void close() throws Exception {
		startLoop = null;
		parent = null;
		log = null;
	}

}
