/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.ui.easyetl.internal;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Providers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
@Path("init")
public class AppInitializer {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	// there is a SLF4J-TDI bridge so it is all in one place.
	private static final Logger log = LoggerFactory.getLogger(AppInitializer.class);

	@GET
	// custom content type
	@Consumes("tdi/init")
	public Object initApplication(@Context Providers provs) throws Exception {
		// it is sometime needed to have a initialization hook within the JAX-RS
		// runtime.

		// do some magic with the providers.
		log.info("---------> Initializing application! Providers = " + provs);

		// its a synthetic call so the response doesn't really matter as long as
		// it is not an exception.
		return Response.ok().build();
	}
}
