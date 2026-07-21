/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.server;

import com.ibm.di.config.interfaces.*;

public class ReplyChannelComponent extends AssemblyLineComponent {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final String PROPERTIES_FILE = "miserver";

	private AssemblyLineComponent comp;

	private static ResourceHash sResHash = ResourceHash.getHash(PROPERTIES_FILE);

	/**
	 * @deprecated
	 */
	public ReplyChannelComponent(ConnectorConfig config) {
		this.config = config;
	}

	public ReplyChannelComponent(AssemblyLineComponent comp) {
		this.comp = comp;
		stats = comp.getStats();
		log = comp.getLog();
		config = comp.getConfiguration();
		handler = comp.getHandler();
		parent = comp.parent;
	}

	public int getType() {
		return ServerConstants.TYPE_REPLYCHANNEL;
	}

	/**
	 * This method implements the reply operation that is used in Server mode.
	 * 
	 * @param meta
	 *            The work entry to add
	 * @exception Exception
	 *                the underlying AssemblyLineComponent is missing or raised
	 *                an error
	 */
	public void reply(com.ibm.di.entry.Entry meta) throws Exception {
		if (comp == null) {
			throw new Exception(sResHash.getString("replychanel.target.null"));
		}

		comp.reply(meta);
	}

	/**
	 * Calls the hook named oper, declaring work and conn as the corresponding
	 * beans. The trigger function calls one of the AssemblyLine hooks defined
	 * for this Connector using the provided conn/work.
	 * 
	 * @param oper
	 *            Name of the hook to call
	 * @param work
	 *            This will be the work bean in the hook
	 * @param conn
	 *            This will be the conn bean in the hook
	 * 
	 * @return True if the hook was executed, false if the hook is not defined
	 *         or disabled.
	 * @exception Exception
	 *                Any exception thrown by the execution of the hook
	 */
	public boolean trigger(String oper, com.ibm.di.entry.Entry work, com.ibm.di.entry.Entry conn)
			throws Exception {

		if (oper.equals("default_ok"))
			return false;

		if (oper.equals("before_execute"))
			oper = "before_execute_reply";

		handler.declareBean("work", work);
		handler.declareBean("conn", conn);

		return trigger(oper);
	}

	/**
	 * Calls the hook named oper, declaring work as the corresponding bean. The
	 * trigger function calls one of the AssemblyLine hooks defined for this
	 * Connector using the provided work.
	 * 
	 * @param oper
	 *            Name of the hook to call
	 * @param work
	 *            This will be the work bean in the hook
	 * 
	 * @return True if the hook was executed, false if the hook is not defined
	 *         or disabled.
	 * @exception Exception
	 *                Any exception thrown by the execution of the hook
	 */
	public boolean trigger(String oper, com.ibm.di.entry.Entry work) throws Exception {

		if (oper.equals("default_ok"))
			return false;

		if (oper.equals("before_execute"))
			oper = "before_execute_reply";

		handler.declareBean("work", work);

		return trigger(oper);
	}

	/**
	 * This method returns the name assigned to the Connector by the
	 * AssemblyLine, followed by ".reply"
	 * 
	 * @return The name of this Connector, followed by ".reply"
	 */
	public String getName() {
		if (comp != null)
			return comp.getName() + ".reply";
		if (config != null)
			return config.getShortName() + ".reply";

		return "NoName.reply";
	}

}
