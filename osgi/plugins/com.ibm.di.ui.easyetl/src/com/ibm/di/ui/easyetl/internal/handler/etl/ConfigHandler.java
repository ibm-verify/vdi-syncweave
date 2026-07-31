/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.ui.easyetl.internal.handler.etl;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.ibm.di.api.APIEngine;
import com.ibm.di.api.remote.Session;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.ui.easyetl.bind.Solution;
import com.ibm.di.ui.easyetl.bind.Solutions;
import com.ibm.di.ui.easyetl.internal.SessionUtils;

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
@Path("/")
public class ConfigHandler {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	@GET
	public Response defaultPage() throws Exception {
		return Response.seeOther(new URI("/static/index.html")).build();
	}

	@GET
	@Path("create/{name}")
	@Produces(MediaType.TEXT_PLAIN)
	public String createConfig(@Context HttpServletRequest req, @PathParam("name") String name) throws Exception {
		String path = name + ".xml";
		Session sess = SessionUtils.getServerApiSession(req);
		MetamergeConfig mc = sess.createNewConfiguration(path, false);
		mc.getSolutionInterface().setInstanceID(name);
		try {
			AssemblyLineConfig alc = mc.newInstanceOf(AssemblyLineConfig.class);
			alc.init();
			alc.setName(name);
			mc.bind(MetamergeConfig.DEFAULT_ASSEMBLYLINE_FOLDER + "/" + name, alc);
			
			ConnectorConfig input = mc.newInstanceOf(ConnectorConfig.class);
			input.init();
			input.setName("Input");
			input.setMode(ConnectorConfig.ITERATOR_MODE);
			alc.getEntryFeedComponents().addConfig(input);
			
			ConnectorConfig output = mc.newInstanceOf(ConnectorConfig.class);
			output.init();
			output.setName("Output");
			output.setMode(ConnectorConfig.ADDONLY_MODE);
			alc.getDataFlowComponents().addConfig(output);
			
			sess.checkInConfiguration(mc, path);
		} catch (Exception e) {
			sess.undoCheckOut(path);
			sess.deleteConfiguration(path);
			throw e;
		}
		
		return name;
	}
	
	@GET
	@Path("config")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listConfigurations(@Context HttpServletRequest req) throws Exception {
		Solutions solutions = new Solutions();
		solutions.setIdentifer("id");
		solutions.setLabel("name");
		Session session = SessionUtils.getServerApiSession(req);
		File root = new File(session.getConfigFolderPath());
		addFiles(root, root, solutions.getItems());
		return Response.ok(solutions).build();
	}
	
	@GET
	@Path("config/{id: .*}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getConfiguration(@Context HttpServletRequest req, @PathParam("id") String id) throws Exception {
		Session session = SessionUtils.getServerApiSession(req);
		File root = new File(session.getConfigFolderPath());
		File f = new File(root, id);

		if (!f.getCanonicalPath().equals(f.getAbsolutePath())) {
			return Response.status(Status.FORBIDDEN).build();
		}

		Solution sol = new Solution();
		sol.setFiletype(f.isDirectory() ? "directory" : "file");
		sol.setId(URLEncoder.encode(id));
		try {
			sol.setLabel(APIEngine.getConfigurationRegistry().getSolutionName(f));
		} catch (Exception e) {
			sol.setDescription(e.toString());
		}
		if(sol.getLabel() == null)
			sol.setLabel(URLEncoder.encode(f.getName()));
		
		return Response.ok(sol).build();
	}
	
	@DELETE
	@Path("config/{id: .*}")
	public Response deleteConfig(@Context HttpServletRequest req, @PathParam("id") String id) throws Exception {
		Session session = SessionUtils.getServerApiSession(req);
		session.deleteConfiguration(id);
		return Response.noContent().build();
	}

//	@PUT
//	@Path("config/{id}")
//	public Response createConfig(@Context HttpServletRequest req, String data) throws Exception {
//		Session session = SessionUtils.getServerApiSession(req);
//		session.deleteConfiguration(id);
//		return Response.ok().build();
//	}
//	
	private void addFiles(File root, File dir, List<Solution> items) throws IOException {
		for(File f : dir.listFiles()) {
			Solution sol = new Solution();
			sol.setFiletype(f.isDirectory() ? "directory" : "file");
			String relPath = f.getCanonicalPath().substring(root.getCanonicalPath().length()+1);
			sol.setId(relPath);
			items.add(sol);
			
			if(f.isDirectory()) {
				addFiles(root, f, sol.getItems());
			} else {
				try {
					sol.setLabel(APIEngine.getConfigurationRegistry().getSolutionName(f));
				} catch (Exception e) {
					sol.setDescription(e.toString());
				}
			}
			
			if(sol.getLabel() == null) {
				if(f.isDirectory())
					sol.setLabel("[" + f.getName() + "]");
				else
					sol.setLabel(f.getName());
			}
		}
	}

}
