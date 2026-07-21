/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.rest.internal.handler.ci.al;

import java.rmi.RemoteException;

import com.ibm.di.web.common.atom.AtomText;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.ibm.di.api.DIException;
import com.ibm.di.api.bind.ALHandle;
import com.ibm.di.api.bind.BindUtil;
import com.ibm.di.api.rest.internal.AppConstants;
import com.ibm.di.api.rest.internal.al.ManualAssemblyLineDriver;

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
public class AssemblyLineHandle {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private final ManualAssemblyLineDriver alh;

	public AssemblyLineHandle(ManualAssemblyLineDriver alh) {
		this.alh = alh;
	}

	@PUT
	@Consumes( { AppConstants.OBJ_JSON_ALHandle, AppConstants.MT_ASSEMBLY_LINE_XML })
	@Produces(AppConstants.OBJ_JSON_ALHandle)
	public Response createAsJson(ALHandle cycle) throws RemoteException, DIException {
		boolean exec = alh.executeCycle(BindUtil.toEntry(cycle.getWorkEntry()), cycle.isProcessTcb());
		return (exec ? Response.ok(alh.getStatus()) : Response.status(Status.CONFLICT)).build();
	}

	@PUT
	@Consumes( { AppConstants.OBJ_JSON_ALHandle, AppConstants.MT_ASSEMBLY_LINE_XML })
	@Produces(AppConstants.MT_ASSEMBLY_LINE_XML)
	public Response createAsXml(ALHandle cycle) throws RemoteException, DIException {
		boolean exec = alh.executeCycle(BindUtil.toEntry(cycle.getWorkEntry()), cycle.isProcessTcb());
		return (exec ? Response.ok(alh.getStatus()) : Response.status(Status.CONFLICT)).build();
	}

	@GET
	@Produces( { AppConstants.OBJ_JSON_ALHandle, AppConstants.MT_ASSEMBLY_LINE_XML })
	public ALHandle status() throws RemoteException, DIException {
		return alh.getStatus();
	}

	@DELETE
	public void close() throws RemoteException, DIException {
		alh.close();
	}
}
