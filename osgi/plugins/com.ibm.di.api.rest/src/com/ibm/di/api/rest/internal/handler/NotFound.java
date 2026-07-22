/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.rest.internal.handler;

import javax.ws.rs.GET;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.ibm.di.web.common.atom.AtomText;
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
public class NotFound {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static NotFound instance = new NotFound();

	public static NotFound getInstance() {
		return instance;
	}

	@GET
	public Response notFound() {
		return Response.status(Status.NOT_FOUND).build();
	}
}
