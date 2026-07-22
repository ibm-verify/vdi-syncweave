/*
 * IBM Confidential
 *
 *  OCO Source Materials
 *
 * 5724-D49
 *
 * (C) Copyright IBM Corporation. 2010
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 *
 */
define({
	// configInstanceSubject: String
	//		The publish subject used to broadcast CI run status
	configInstanceSubject : "/tdi/config/status",

	// startTempConfigSubject: String
	//		The publish subject used to request start temp config instances
	// params:
	//		Object{config, assemblyline}
	runAssemblyLineSubject : "/tdi/start/assemblyline",

	// startTempConfigSubject: String
	//		The publish subject used to request start temp config instances
	// params:
	//		Object{config, assemblyline}
	openEditorWithCommandSubect : "/tdi/edit/command",
	
	// serverEventsSubject: String
	//		The publish subject for server events (ci, al and server start/stop events)
	serverEventsSubject: "/tdi/server/events",
	
	// recentFilesSubject: String
	//		The publish subject for recent files changes
	recentFilesSubject: "/tdi/recentfiles"
});
