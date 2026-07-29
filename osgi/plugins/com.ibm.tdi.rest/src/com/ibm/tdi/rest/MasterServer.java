/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.rest;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author stadheim
 *
 */
@Path("admin")
public class MasterServer {
	
	public static final String SERVER_HOSTNAME = "serverHostname";
	public static final String SERVER_IP_ADDRESS = "serverIPAddress";
	public static final String SERVER_ID = "serverId";
	public static final String MEMORY_MAX = "memoryMax";
	public static final String MEMORY_FREE = "memoryFree";
	public static final String MEMORY_TOTAL = "memoryTotal";
	
	private Map<String,Map<String,Object>> map = new HashMap<String, Map<String,Object>>();

	public MasterServer() {
		System.out.println("Master server enabled at /admin");
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getStatus(@Context HttpServletRequest request) throws Exception {
		Map<String,Object> map = new HashMap<String, Object>();
		map.put(SERVER_ID, getMasterID());
		map.put(SERVER_IP_ADDRESS, request.getLocalAddr());
		map.put(SERVER_HOSTNAME, request.getLocalName());
		updateServerInfo(map);
		return Response.ok(this.map).build();
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response postStatus(@Context HttpServletRequest request, Map<String,Object> postData) throws Exception {
		updateServerInfo(postData);
		return Response.ok().build();
	}
	
	private void updateServerInfo(Map<String,Object> data) {
		String serverId = (String) data.get(SERVER_ID);
		System.out.println("[master] received data from " + serverId );
		map.put(serverId , data);
	}
	
	private String getMasterID() {
		String serverId = System.getProperty("com.ibm.di.server.id");
		String masterId = System.getProperty("com.ibm.tdi.rest.master.id");
		if(masterId != null && masterId.trim().length() > 0)
			return masterId;
		else
			return serverId;
	}
	
}
