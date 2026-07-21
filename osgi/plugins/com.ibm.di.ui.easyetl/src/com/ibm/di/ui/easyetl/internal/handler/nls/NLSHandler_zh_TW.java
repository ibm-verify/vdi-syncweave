/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.ui.easyetl.internal.handler.nls;

import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * This handler converts the java language resource files to JSON objects.
 */
@Path("tdinls")

public class NLSHandler_zh_TW {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	@GET
	@Path("nls/{lang}/{file}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMessageBundle(@Context HttpServletRequest req, @PathParam("lang") String language, @PathParam("file") String file) throws Exception {
		String propfile = getFilename(file);
		if(propfile == null)
			return Response.status(Status.NOT_FOUND).build();
		
		String lang = language.replaceAll("-", "_");
		int index = lang.indexOf("_");
		if(index != -1) {
			lang = lang.substring(0, index) + lang.substring(index).toUpperCase();
		}
		
		Properties p = new Properties();
		try {
			p.load(getClass().getResourceAsStream("/nls/" + propfile + "_" + lang + ".properties"));
			// Due to the way template expansion in dojo works we add a version of
			// a property if the name contains dots.
			addAlternateNames(p);
		} catch(NullPointerException npe) {
			return Response.status(Status.NOT_FOUND).build();
		}
		return Response.ok(p).build();
	}
	
	@GET
	@Path("nls/{file}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDefaultMessageBundle(@Context HttpServletRequest req, @PathParam("file") String file) throws Exception {
		String propfile = getFilename(file);
		if(propfile == null)
			return Response.status(Status.NOT_FOUND).build();
		
		Properties p = new Properties();
		p.load(getClass().getResourceAsStream("/nls/" + propfile + ".properties"));
		
		// Due to the way template expansion in dojo works we add a version of
		// a property if the name contains dots.
		addAlternateNames(p);
		
		return Response.ok(p).build();
	}

	private void addAlternateNames(Properties p) {
		Properties add = new Properties();
		for(Map.Entry<Object, Object> e : p.entrySet()) {
			String key = e.getKey().toString();
			if(key.indexOf(".") != -1) {
				String newkey = key.replaceAll("\\.", "_");
				add.setProperty(newkey, p.getProperty(key));
			}
		}
		for(Map.Entry<Object, Object> e : add.entrySet()) {
			p.setProperty(e.getKey().toString(), e.getValue().toString());
		}
	}

	private String getFilename(String file) {
		String propfile = file;
		if(propfile == null)
			return null;
		if(propfile.endsWith(".js"))
			propfile = propfile.substring(0, propfile.lastIndexOf(".js"));
		return propfile;
	}
}
