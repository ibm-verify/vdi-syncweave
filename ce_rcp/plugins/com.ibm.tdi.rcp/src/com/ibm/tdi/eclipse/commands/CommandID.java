/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.commands;

/**
 * This file contains the command identifiers listed in the plugin.xml that do not have
 * a corresponding Action defined in the plugin.xml. Widgets that programatically add actions (e.g. buttons)
 * should add an ID in this class and use the CommandHandlerProxy to create a handler object for the
 * action.
 * <p>
 * For example, the DiscoverSchemaWidget adds an action to discover the schema to its toolbar. It is
 * not possible for the user to bind a key sequence to this button unless you create a handler
 * and define the action in the "org.eclipse.ui.commands" section of the plugin.xml file.
 * <p>
 * The ID of the command definition in the plugin.xml file should be used as the action definition id
 * for the action object. The CommandHandlerProxy does all this. All you have to add is this:<br/>
 * 
 * 	new CommandHandlerProxy(getEditor().getSite(), discoverAction, CommandID.DISCOVER_SCHEMA);
 * <p>
 * Actions that are defined in the plugin.xml file should also have a corresponding command definition.
 * It is not necessary to use the CommandHandlerProxy for those actions though.
 */
public class CommandID {
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	// -- Change parser
	public static final String CHANGE_PARSER_ACTION_ID = "com.ibm.tdi.rcp.changeParser";

	// -- Discover schema
	public static final String DISCOVER_SCHEMA = "com.ibm.tdi.rcp.discoverSchema";

	// -- Add component (assemblyline editor)
	public static final String AL_EDITOR_ADD_COMPONENT = "com.ibm.tdi.rcp.al.addComponent";

	// -- Run al (assemblyline editor)
	public static final String AL_EDITOR_RUN = "com.ibm.tdi.rcp.runal.action";

	// -- Download properties
	public static final String CNFSETTINGS_EDITOR_DOWNLOAD = "com.ibm.tdi.rcp.cfgsettings.download";

	// -- Upload properties
	public static final String CNFSETTINGS_EDITOR_UPLOAD = "com.ibm.tdi.rcp.cfgsettings.upload";

	// -- Server view commands
	public static final String SERVER_VIEW_START = "com.ibm.tdi.rcp.serverview.start";
	public static final String SERVER_VIEW_STOP = "com.ibm.tdi.rcp.serverview.stop";
	public static final String SERVER_VIEW_REFRESH = "com.ibm.tdi.rcp.serverview.refresh";
	public static final String SERVER_VIEW_LOG = "com.ibm.tdi.rcp.serverview.log";
	public static final String SERVER_VIEW_ATTACH_DEBUG = "com.ibm.tdi.rcp.serverview.attachdebug";
	public static final String SERVER_VIEW_DEBUG = "com.ibm.tdi.rcp.serverview.debug";
	public static final String SERVER_VIEW_SHOW_COMP = "com.ibm.tdi.rcp.serverview.showcomp";
	public static final String SERVER_VIEW_AMC = "com.ibm.tdi.rcp.serverview.amc";
	public static final String SERVER_VIEW_EDIT_SS = "com.ibm.tdi.rcp.server.systemstore";
	public static final String SERVER_VIEW_ADD_SERVER = "com.ibm.tdi.rcp.serverview.new";
	public static final String SERVER_VIEW_DELETE_SERVER = "com.ibm.tdi.rcp.serverview.delete";
	public static final String SERVER_VIEW_RENAME_SERVER = "com.ibm.tdi.rcp.serverview.rename";
	public static final String SERVER_VIEW_BROWSE_SS = "com.ibm.tdi.eclipse.browse.system.store";
	public static final String SERVER_VIEW_DASHBOARD = "com.ibm.tdi.rcp.serverview.dashboard";
}
