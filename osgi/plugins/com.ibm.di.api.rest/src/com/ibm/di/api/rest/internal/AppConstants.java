/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.rest.internal;

import javax.ws.rs.core.MediaType;

import com.ibm.di.web.common.atom.AtomText;
import com.ibm.di.web.common.atom.AtomCategory;

import com.ibm.di.nls.L10N;
import com.ibm.di.nls.L10NFactory;

/**
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.2
 */
public final class AppConstants {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	private AppConstants() {
	}

	// localization
	public static final L10N L10N = L10NFactory.getInstance(ServerActivator.class);

	// Resource Categories
	public static final String SCHEME_RESOURCE = "http://www.ibm.com/xmlns/prod/tdi/rest#resource";
	public static final AtomCategory CAT_RES_CONFIG = new AtomCategory();
	static {
		CAT_RES_CONFIG.setTerm("configuration");
		CAT_RES_CONFIG.setScheme(SCHEME_RESOURCE);
	}

	public static final AtomCategory CAT_RES_CI = new AtomCategory();
	static {
		CAT_RES_CI.setTerm("config-instance");
		CAT_RES_CI.setScheme(SCHEME_RESOURCE);
	}

	public static final AtomCategory CAT_RES_SERVER = new AtomCategory();
	static {
		CAT_RES_SERVER.setTerm("server");
		CAT_RES_SERVER.setScheme(SCHEME_RESOURCE);
	}

	public static final AtomCategory CAT_RES_TOMBSTONE = new AtomCategory();
	static {
		CAT_RES_TOMBSTONE.setTerm("tombstone");
		CAT_RES_TOMBSTONE.setScheme(SCHEME_RESOURCE);
	}

	public static final AtomCategory CAT_RES_LISTENER = new AtomCategory();
	static {
		CAT_RES_LISTENER.setTerm("listener");
		CAT_RES_LISTENER.setScheme(SCHEME_RESOURCE);
	}

	public static final AtomCategory CAT_RES_PROPERTY_STORE = new AtomCategory();
	static {
		CAT_RES_PROPERTY_STORE.setTerm("property-store");
		CAT_RES_PROPERTY_STORE.setScheme(SCHEME_RESOURCE);
	}

	public static final AtomCategory CAT_RES_ASSEMBLY_LINE = new AtomCategory();
	static {
		CAT_RES_ASSEMBLY_LINE.setTerm("assembly-line");
		CAT_RES_ASSEMBLY_LINE.setScheme(SCHEME_RESOURCE);
	}

	// Server Categories
	public static final String SCHEME_SERVER = "http://www.ibm.com/xmlns/prod/tdi/rest#server";
	public static final AtomCategory CAT_SRV_INFO = new AtomCategory();
	static {
		CAT_SRV_INFO.setTerm("info");
		CAT_SRV_INFO.setScheme(SCHEME_SERVER);
	}

	public static final AtomCategory CAT_SRV_CONTROL = new AtomCategory();
	static {
		CAT_SRV_CONTROL.setTerm("control");
		CAT_SRV_CONTROL.setScheme(SCHEME_SERVER);
	}

	public static final AtomCategory CAT_SRV_NOTIFICATION = new AtomCategory();
	static {
		CAT_SRV_NOTIFICATION.setTerm("custom-notification");
		CAT_SRV_NOTIFICATION.setScheme(SCHEME_SERVER);
	}

	public static final AtomCategory CAT_SRV_COMPONENT = new AtomCategory();
	static {
		CAT_SRV_COMPONENT.setTerm("component");
		CAT_SRV_COMPONENT.setScheme(SCHEME_SERVER);
	}

	// Component Categories
	public static final String SCHEME_COMPONENT = "http://www.ibm.com/xmlns/prod/tdi/rest#component";
	public static final AtomCategory CAT_COMP_CONN = new AtomCategory();
	static {
		CAT_COMP_CONN.setTerm("connector");
		CAT_COMP_CONN.setScheme(SCHEME_COMPONENT);
	}

	public static final AtomCategory CAT_COMP_FC = new AtomCategory();
	static {
		CAT_COMP_FC.setTerm("function");
		CAT_COMP_FC.setScheme(SCHEME_COMPONENT);
	}

	public static final AtomCategory CAT_COMP_PARSER = new AtomCategory();
	static {
		CAT_COMP_PARSER.setTerm("parser");
		CAT_COMP_PARSER.setScheme(SCHEME_COMPONENT);
	}

	// Configuration Categories
	public static final String SCHEME_CONFIGURATION = "http://www.ibm.com/xmlns/prod/tdi/rest#configuration";
	public static final AtomCategory CAT_CONFIG_DIR = new AtomCategory();
	static {
		CAT_CONFIG_DIR.setTerm("directory");
		CAT_CONFIG_DIR.setScheme(SCHEME_CONFIGURATION);
	}

	public static final AtomCategory CAT_CONFIG_FILE = new AtomCategory();
	static {
		CAT_CONFIG_FILE.setTerm("file");
		CAT_CONFIG_FILE.setScheme(SCHEME_CONFIGURATION);
	}

	public static final AtomCategory CAT_CONFIG_LOCKED = new AtomCategory();
	static {
		CAT_CONFIG_LOCKED.setTerm("locked");
		CAT_CONFIG_LOCKED.setScheme(SCHEME_CONFIGURATION);
	}

	// Property Store Categories
	public static final String SCHEME_PROPERTY_STORE = "http://www.ibm.com/xmlns/prod/tdi/rest#property-store";
	public static final AtomCategory CAT_PROPERTY_STORE_DEFAULT = new AtomCategory();
	static {
		CAT_PROPERTY_STORE_DEFAULT.setTerm("default");
		CAT_PROPERTY_STORE_DEFAULT.setScheme(SCHEME_PROPERTY_STORE);
	}

	public static final AtomCategory CAT_PROPERTY_STORE_PASSWORD = new AtomCategory();
	static {
		CAT_PROPERTY_STORE_PASSWORD.setTerm("password");
		CAT_PROPERTY_STORE_PASSWORD.setScheme(SCHEME_PROPERTY_STORE);
	}

	public static final AtomCategory CAT_PROPERTY_STORE_MODIFIED = new AtomCategory();
	static {
		CAT_PROPERTY_STORE_MODIFIED.setTerm("modified");
		CAT_PROPERTY_STORE_MODIFIED.setScheme(SCHEME_PROPERTY_STORE);
	}

	// AssemblyLine Categories
	public static final String SCHEME_AL = "http://www.ibm.com/xmlns/prod/tdi/rest#assembly-line";
	public static final AtomCategory CAT_AL_ACTIVE = new AtomCategory();
	static {
		CAT_AL_ACTIVE.setTerm("active");
		CAT_AL_ACTIVE.setScheme(SCHEME_AL);
	}

	public static final AtomCategory CAT_AL_MANUAL = new AtomCategory();
	static {
		CAT_AL_MANUAL.setTerm("manual");
		CAT_AL_MANUAL.setScheme(SCHEME_AL);
	}

	// AssemblyLine Categories
	public static final String SCHEME_LISTENER = "http://www.ibm.com/xmlns/prod/tdi/rest#listener";
	public static final AtomCategory CAT_LISTENER_EVENT = new AtomCategory();
	static {
		CAT_LISTENER_EVENT.setTerm("event");
		CAT_LISTENER_EVENT.setScheme(SCHEME_LISTENER);
	}

	public static final AtomCategory CAT_LISTENER_CONFIG_FILE = new AtomCategory();
	static {
		CAT_LISTENER_CONFIG_FILE.setTerm("config-file");
		CAT_LISTENER_CONFIG_FILE.setScheme(SCHEME_LISTENER);
	}

	public static final AtomCategory CAT_LISTENER_LOG = new AtomCategory();
	static {
		CAT_LISTENER_LOG.setTerm("log");
		CAT_LISTENER_LOG.setScheme(SCHEME_LISTENER);
	}

	public static final AtomCategory CAT_LISTENER_AL = new AtomCategory();
	static {
		CAT_LISTENER_AL.setTerm("al");
		CAT_LISTENER_AL.setScheme(SCHEME_LISTENER);
	}

	public static final AtomCategory CAT_LISTENER_POLL = new AtomCategory();
	static {
		CAT_LISTENER_POLL.setTerm("poll");
		CAT_LISTENER_POLL.setScheme(SCHEME_LISTENER);
	}

	public static final AtomCategory CAT_LISTENER_PUSH = new AtomCategory();
	static {
		CAT_LISTENER_PUSH.setTerm("push");
		CAT_LISTENER_PUSH.setScheme(SCHEME_LISTENER);
	}

	// MediaTypes
	public static final String MT_SERVER_INFO_XML = "application/com.ibm.di.api.server.info+xml";
	public static final String MT_SERVER_INFO_JSON = "application/com.ibm.di.api.server.info+json";
	public static final String OBJ_JSON_ServerInfo = MT_SERVER_INFO_JSON + ";type=serverInfo";

	public static final String MT_SERVER_CONTROL_XML = "application/com.ibm.di.api.server.control+xml";
	public static final String MT_SERVER_CONTROL_JSON = "application/com.ibm.di.api.server.control+json";
	public static final String OBJ_JSON_Shutdown = MT_SERVER_CONTROL_JSON + ";type=shutdown";

	public static final String MT_SERVER_NOTIFY_XML = "application/com.ibm.di.api.server.notification+xml";
	public static final String MT_SERVER_NOTIFY_JSON = "application/com.ibm.di.api.server.notification+json";
	public static final String OBJ_JSON_CustomNotification = MT_SERVER_NOTIFY_JSON + ";type=customNotification";

	public static final String MT_COMPONENT_XML = "application/com.ibm.di.api.component+xml";
	public static final String MT_COMPONENT_JSON = "application/com.ibm.di.api.component+json";
	public static final String OBJ_JSON_ConnectorDescriptor = MT_COMPONENT_JSON + ";type=connectorDescriptor";
	public static final String OBJ_JSON_FunctionComponentDescriptor = MT_COMPONENT_JSON + ";type=functionComponentDescriptor";
	public static final String OBJ_JSON_ParserDescriptor = MT_COMPONENT_JSON + ";type=parserDescriptor";

	public static final String MT_API_CONFIG_XML = "application/com.ibm.di.api.configuration+xml";
	public static final String MT_API_CONFIG_JSON = "application/com.ibm.di.api.configuration+json";
	public static final String OBJ_JSON_StartCI = MT_API_CONFIG_JSON + ";type=startCI";
	public static final String OBJ_JSON_CreateConfig = MT_API_CONFIG_JSON + ";type=createConfig";
	public static final String OBJ_JSON_ConfigLock = MT_API_CONFIG_JSON + ";type=configLock";

	public static final String MT_CONFIG_XML = "application/com.ibm.di.configuration+xml";
	public static final String MT_CONFIG_JSON = "application/com.ibm.di.configuration+json";
	public static final String OBJ_JSON_SolutionBinding = MT_CONFIG_JSON + ";type=solution";
	public static final String OBJ_JSON_AssemblyLineBinding = MT_CONFIG_JSON + ";type=assemblyLine";
	public static final String OBJ_JSON_PropertyStoreBinding = MT_CONFIG_JSON + ";type=propertyStore";

	public static final String MT_LISTENER_XML = "application/com.ibm.di.api.listener+xml";
	public static final String MT_LISTENER_JSON = "application/com.ibm.di.api.listener+json";
	public static final String OBJ_JSON_DIEventListener = MT_LISTENER_JSON + ";type=diEventListener";
	public static final String OBJ_JSON_AlEvent = MT_LISTENER_JSON + ";type=alEvent";
	public static final String OBJ_JSON_CiEvent = MT_LISTENER_JSON + ";type=ciEvent";
	public static final String OBJ_JSON_DiEvent = MT_LISTENER_JSON + ";type=diEvent";
	public static final String OBJ_JSON_AssemblyLineListener = MT_LISTENER_JSON + ";type=assemblyLineListener";
	public static final String OBJ_JSON_AssemblyLineEvent = MT_LISTENER_JSON + ";type=assemblyLineEvent";
	public static final String OBJ_JSON_LogListener = MT_LISTENER_JSON + ";type=logListener";
	public static final String OBJ_JSON_LogEvent = MT_LISTENER_JSON + ";type=logEvent";
	public static final String OBJ_JSON_BatchEvent = MT_LISTENER_JSON + ";type=batchEvent";
	public static final String OBJ_JSON_ConfigFileListener = MT_LISTENER_JSON + ";type=configFileListener";
	public static final String OBJ_JSON_ConfigFileEvent = MT_LISTENER_JSON + ";type=configFileEvent";

	public static final String MT_PROPERTY_STORE_XML = "application/com.ibm.di.api.property-store+xml";
	public static final String MT_PROPERTY_STORE_JSON = "application/com.ibm.di.api.property-store+json";
	public static final String OBJ_JSON_Properties = MT_PROPERTY_STORE_JSON + ";type=properties";
	public static final String OBJ_JSON_Property = MT_PROPERTY_STORE_JSON + ";type=properties";

	public static final String MT_ASSEMBLY_LINE_XML = "application/com.ibm.di.api.assembly-line+xml";
	public static final String MT_ASSEMBLY_LINE_JSON = "application/com.ibm.di.api.assembly-line+json";
	public static final String OBJ_JSON_ALHandle = MT_ASSEMBLY_LINE_JSON + ";type=alHandle";
	public static final String OBJ_JSON_StartAL = MT_ASSEMBLY_LINE_JSON + ";type=startAL";
	public static final String OBJ_JSON_TaskStatistics = MT_ASSEMBLY_LINE_JSON + ";type=taskStatistics";

	public static final String MT_ENTRY_XML = "application/com.ibm.di.api.entry+xml";
	public static final String MT_ENTRY_JSON = "application/com.ibm.di.api.entry+json";
	public static final String OBJ_JSON_Entry = MT_ENTRY_JSON + ";type=entry";

	public static final String MT_TOMBSTONE_XML = "application/com.ibm.di.api.tombstone+xml";
	public static final String MT_TOMBSTONE_JSON = "application/com.ibm.di.api.tombstone+json";
	public static final String OBJ_JSON_Tombstone = "application/com.ibm.di.api.tombstone+json;type=tombstone";

	public static final String MT_ATOM_APP_SRVC_XML = "application/atomsvc+xml";
	public static final String MT_ATOM_APP_SRVC_JSON = "application/atomsvc+json";
	public static final String OBJ_JSON_AppService = MT_ATOM_APP_SRVC_JSON + ";type=service";
	public static final String OBJ_JSON_AtomEntry = MediaType.APPLICATION_JSON + ";type=entry";
	public static final String OBJ_JSON_AtomFeed = MediaType.APPLICATION_JSON + ";type=feed";

	// Relations
	public static final String REL_SELF = "self";
	public static final String REL_COMPONENT = "component";
	public static final String REL_SHUTDOWN = "shutdown";
	public static final String REL_NOTIFY = "notify";
	public static final String REL_LOCK = "lock";
	public static final String REL_CONFIGURATION = "configuration";
	public static final String REL_LISTENER = "listener";
	public static final String REL_ASSEMBLY_LINE = "assembly-line";
	public static final String REL_PROPERTY_STORE = "property-store";
	public static final String REL_TOMBSTONE = "tombstone";
	public static final String REL_PROPERTIES = "properties";
	public static final String REL_CONTENT = "content";
	public static final String REL_RESULT = "result";
	public static final String REL_STATUS = "status";
	public static final String REL_HANDLE = "handle";
	public static final String REL_LOG = "log";
	public static final String REL_POLL = "poll";
	public static final String REL_DEBUG = "debug";

}
