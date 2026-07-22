/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.ui.webui.internal.handler.files;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Path(FileHandler.URL)
public class FileHandler {
	
	public final static String URL = "files";

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response listFiles(@Context HttpServletRequest req, @QueryParam("path") String path, @QueryParam("filter") String filter) {
				
		boolean isLinux = System.getProperty("os.name").toLowerCase().startsWith("linux");
		String user_dir = System.getProperty("user.dir").toLowerCase();
		
		if (isLinux) {
			File f1 = new File("/etc/settings.sys");
			if(f1.exists() && !f1.isDirectory()) { 
				if (!(path.equals(".")) && (path.toLowerCase().startsWith("/userdata/directory/customin"))) { // path is valid
					//System.out.println("path is valid");
				}
				else{
					if (!(path.equals("."))){
						System.err.println("Path resticted to only use userdata/directory/CustomIn");
						System.out.println("Path resticted to only use userdata/directory/CustomIn");
						path = "/userdata/directory/CustomIn";
						//return Response.status(Status.FORBIDDEN).build();
												
					}
				}
			}
		}//end of if (isLinux)
		else{			
			if (!(path.equals(".")) && (path.toLowerCase().startsWith(user_dir))) { // path is valid
				//System.out.println("path is valid");
			}
			else{
				if (!(path.equals("."))){
					System.err.println("Path resticted to only use "+user_dir);
					System.out.println("Path resticted to only use "+user_dir);
					path = user_dir;				
					//return Response.status(Status.FORBIDDEN).build();
				}
			}
		}
		String p = path != null ? path : ".";
		File dir = new File(p);
		try {
			if (!p.equals(".") && !dir.getCanonicalPath().equals(dir.getAbsolutePath())) {
				//return Response.status(Status.FORBIDDEN).build();
			}
		} catch (Exception e) {
			return Response.status(Status.FORBIDDEN).build();
		}

		try {
			dir = new File(dir.getCanonicalPath());
		} catch (Exception e) {
			dir = new File(dir.getAbsolutePath());
		}
		
		if(!dir.exists() || !dir.isDirectory()) {
			return Response.status(Status.NOT_FOUND).build();
		}
		
		// -- list root filesystems and files in requested directory
		HashMap<String, Object> res = new HashMap<String, Object>();
		res.put("roots", getFiles(dir.listRoots()));
		res.put("files", getFiles(dir.listFiles()));
		
		// -- requested directory
		try {
			res.put("path", dir.getCanonicalPath());
		} catch (IOException e) {
			res.put("path", dir.getAbsolutePath());
		}
		
		// -- root part
		while(dir.getParentFile() != null) {
			dir = dir.getParentFile();
		}
		try {
			res.put("top", dir.getCanonicalPath());
		} catch (IOException e) {
			res.put("top", dir.getAbsolutePath());
		}
		
		return Response.ok(res).build();
	}
	
	private ArrayList<Map<String,Object>> getFiles(File[] files) {
		ArrayList<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
		for(File file : files) {
			HashMap<String, Object> map = toMap(file);
			if(map != null) {
				result.add(map);
			}
		}
		return result;
	}
	
	private HashMap<String,Object> toMap(File file) {
		if(!file.getName().startsWith(".")) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("name", file.getName());
			map.put("directory", file.isDirectory());
			if(file.isFile())
				map.put("size", file.length());
			map.put("readable", file.canRead());
			map.put("writeable", file.canWrite());
			map.put("executable", file.canExecute());
			try {
				map.put("path",file.getCanonicalPath());
			} catch (IOException e) {
				map.put("path", file.getAbsolutePath());
			}
			map.put("modified", file.lastModified());
			return map;
		}
		return null;
	}
}
