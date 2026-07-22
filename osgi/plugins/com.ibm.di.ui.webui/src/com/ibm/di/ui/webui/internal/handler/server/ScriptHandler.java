/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.ui.webui.internal.handler.server;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path(ScriptHandler.URL)

public class ScriptHandler {
	
	public final static String URL = "script";
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response generateClassContentAssist(@Context HttpServletRequest req, @QueryParam("class")String javaClass) throws Exception {
		List<HashMap<String, String>> sigmap = new ArrayList<HashMap<String,String>>();
		HashMap<String,Object> map = new HashMap<String, Object>();
		map.put("className", URLEncoder.encode(javaClass));
		map.put("items", sigmap);
		
		Class<?> classForObject = null;
		try {
			classForObject = Class.forName(javaClass != null ? javaClass : "java.lang.Object");
		} catch(Exception ignore) {}
		
		if (classForObject == null)
			return Response.ok(map).build();
		
		// Javascript always wraps these to internal objects
//		if(classForObject == String.class)
//			classForObject = FBSString.class;
//		else if(classForObject == Boolean.class)
//			classForObject = FBSBoolean.class;
//
//		if (FBSValue.class.isAssignableFrom(classForObject)) {
//			classForObject = addJavascriptObjectProperties(classForObject, sigmap);
//		}

		addClassFields(classForObject, sigmap);

		for (Method m : classForObject.getMethods()) {
			String str = m.getName() + "(";
			String display = "";
			for (Class<?> p : m.getParameterTypes()) {
				if (display.length() > 0) {
					display += ", ";
					str += ",";
				}
				display += shortClassName(p);
				
				// Java methods where a param is assignable from a string/int
				// will cause the javascript method to be used instead.
				if(Integer.class.isAssignableFrom(p))
					str += "int";
				else if(String.class.isAssignableFrom(p))
					str += "string";
				else
					str += shortClassName(p);
			}
			str += ")";
			display = m.getName() + "(" + display + ") ";
			if(m.getReturnType() != null) {
				display += " - " + shortClassName(m.getReturnType());
			}

			addItem(sigmap, m.getName(), str, m.getReturnType(), display);
		}
		
		return Response.ok(map).build();
	}

	private void addClassFields(Class<?> classForObject, List<HashMap<String, String>> sigmap) {
		
		ArrayList<Field> fields = new ArrayList<Field>();
		for (Field f : classForObject.getFields()) {
			fields.add(f);
		}
		
		/*
		 * Special case for this type. The constants are the keywords for the get method
		 * so from a javascript point of view we really want the keywords and not the field per se.
		 */
		if(classForObject.getName().indexOf("AssemblyLineComponent") != -1) {
			for (Field m : fields) {
				try {
					Object value = m.get(null);
					if(value instanceof String) {
						String str = value.toString();
						if(str.startsWith("$")) {
							continue;
						}
						String display = str + " - " + shortClassName(m.getType());
						addItem(sigmap, m.getName(), str, m.getType(), display);
						continue;
					}
				} catch (Exception e) {
					// fall through to providing the field as normal
				}
				String str = m.getName();
				String cls = shortClassName(m.getType());
				String display = str + " - " + cls;
				addItem(sigmap, m.getName(), str, m.getType(), display);
			}
		} else {
			for (Field m : fields) {
				String str = m.getName();
				String cls = shortClassName(m.getType());
				String display = str + " - " + cls;
				addItem(sigmap, m.getName(), str, m.getType(), display);
			}
		}
	}
	
	private void addItem(List<HashMap<String, String>> sigmap, String name, String str, Class<?> returnType, String description) {
		HashMap<String, String> item = new HashMap<String, String>();
		item.put("name", name);
		item.put("content", str);
		item.put("description", description);
		if(returnType != null) {
			item.put("returnType", returnType.getName());
		}
		sigmap.add(item);
	}

	private String shortClassName(Class<?> clazz) {
		String name = clazz.getName();
		if (clazz.isArray()) {
			name = clazz.getComponentType().getName();
		}
		if (name.indexOf(".") != -1)
			name = name.substring(name.lastIndexOf(".") + 1);

		if (clazz.isArray())
			return name + "[]";
		else
			return name;
	}
}

