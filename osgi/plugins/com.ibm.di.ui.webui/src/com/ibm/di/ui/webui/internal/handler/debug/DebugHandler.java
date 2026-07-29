/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.ui.webui.internal.handler.debug;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;

import com.ibm.di.api.remote.AssemblyLineHandler;
import com.ibm.di.api.remote.ConfigInstance;
import com.ibm.di.api.remote.Session;
import com.ibm.di.server.AssemblyLine;
import com.ibm.di.server.TaskCallBlock;
import com.ibm.di.ui.webui.internal.SessionUtils;

@Path(DebugHandler.URL)

public class DebugHandler {
	public final static String URL = "debugger";

	@Path("start")
	@GET
	public String startAssemblyLine(@Context HttpServletRequest req, @PathParam("config") String config, @PathParam("assemblyline") String assemblyline) throws Exception {
		Session sess = SessionUtils.getServerApiSession(req);
		String configId = config + "_" + UUID.randomUUID().toString();
		
		ConfigInstance ci = sess.startConfigInstance(config, true, null, configId, null);
		
		
		
		TaskCallBlock tcb = new TaskCallBlock();
		tcb.setProperty(AssemblyLine.TCB_DEBUG_HOST, "localhost");
		tcb.setProperty(AssemblyLine.TCB_DEBUG_PORT, "localhost");
		
		AssemblyLineHandler al = ci.startAssemblyLineManual(assemblyline, tcb);

		req.getSession().setAttribute("alhandle", al);

		return "OK";
	}
}
