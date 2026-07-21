/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.ui.easyetl.internal.templates;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.datatype.DatatypeConfigurationException;

import org.apache.wink.common.model.multipart.InMultiPart;
import org.apache.wink.common.model.multipart.InPart;

import com.ibm.di.api.DIException;
import com.ibm.di.api.remote.Session;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.xml.MetamergeConfigXML;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.ui.easyetl.bind.BindUtil;
import com.ibm.di.ui.easyetl.bind.ConfigTemplate;
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
@Path(TemplatesHandler.URL)
public class TemplatesHandler implements FilenameFilter {
	private static final String DEFAULT_POINT_TO_POINT_SOLUTION = "Default point to point solution";

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	public final static String URL = "templates";

	// Grab templates directory from property
	public final static String TEMPLATES_DIR = System.getProperty("dashboard.templates.folder", "dashboard/templates");
	public final static String CONFIGS_DIR = System.getProperty("api.config.folder", "configs");

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTemplates(@Context HttpServletRequest req) throws RemoteException, DIException, NotBoundException,
			DatatypeConfigurationException {
		File dir = new File(TEMPLATES_DIR);
		if (!dir.exists() && !dir.mkdirs())
			SystemFunctions.doNothing();

		ArrayList<ConfigTemplate> tslist = new ArrayList<ConfigTemplate>();
		if (dir.exists() && dir.isDirectory()) {
			for (String str : dir.list(this)) {
				ConfigTemplate ct = new ConfigTemplate();
				ct.setName(str);
				tslist.add(ct);
			}
		}
//		ConfigTemplate ct = new ConfigTemplate();
//		ct.setName(DEFAULT_POINT_TO_POINT_SOLUTION);
//		tslist.add(0, ct);
		return Response.ok(BindUtil.fromConfigTemplates(tslist.toArray(new ConfigTemplate[tslist.size()]))).build();
	}

	@GET
	@Path("{id}/{target}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response createSolutionFromTemplate(@Context HttpServletRequest req, @PathParam("id") String id,
			@PathParam("target") String target) throws Exception {
		Session sess = SessionUtils.getServerApiSession(req);
		createConfigurationFromTemplate(sess, id, target);
		return Response.ok().build();
	}

	@DELETE
	@Produces(MediaType.TEXT_PLAIN)
	public Response deleteTemplate(@Context HttpServletRequest req, @PathParam("id") String id) throws Exception {
		File file = new File(TEMPLATES_DIR, id + ".xml");
		if (!file.getCanonicalPath().equals(file.getAbsolutePath())) {
			return Response.status(Status.FORBIDDEN).build();
		}

		if (!file.exists())
			return Response.status(Status.NOT_FOUND).build();
		if (file.delete())
			return Response.ok().build();
		else
			return Response.status(Status.FORBIDDEN).build();
	}

	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.TEXT_HTML)
	public String createNewTemplate(@Context HttpServletRequest req, InMultiPart inMP) throws Exception {
		StringBuffer buf = new StringBuffer();
		buf.append("<html>");
		buf.append("<head>");
		buf.append("<title>SDI Upload Solution</title>");
		buf.append("</head>");
		buf.append("<body>");
		try {
			_createNewTemplate(req, inMP);
			buf.append("OK");
		} catch (Exception e) {
			buf.append("*** " + e.toString());
		}
		buf.append("</body></html>");
		return buf.toString();
	}

	private void _createNewTemplate(@Context HttpServletRequest req, InMultiPart inMP) throws Exception {
		String fileData = null;
		String solutionName = null;
		String templateName = null;
		boolean template = false;
		boolean replace = false;

		Session sess = SessionUtils.getServerApiSession(req);

		while (inMP.hasNext()) {
			InPart part = inMP.next();
			String disp = part.getHeaders().getFirst("Content-Disposition");
			if (disp != null) {
				Pattern p = Pattern.compile("filename=\"(.*)\"");
				Matcher m = p.matcher(disp);
				if (m.find()) {
					if (solutionName == null) {
						solutionName = m.group(1);
					}
					fileData = readStream(part.getInputStream());
				} else {
					Pattern p2 = Pattern.compile("name=\"(.*)\"");
					Matcher m2 = p2.matcher(disp);
					if (m2.find()) {
						String key = m2.group(1);
						String value = readStream(part.getInputStream());
						if ("template".equals(key)) {
							template = "on".equals(value) || "true".equals(value);
						} else if ("replace".equals(key)) {
							replace = "on".equals(value) || "true".equals(value);
						} else if ("solutionname".equals(key) && value != null && value.length() > 0) {
							solutionName = value;
						} else if ("templateName".equals(key)) {
							templateName = "T:" + value;
						} else if ("configName".equals(key)) {
							templateName = "P:" + value;
						}
					}
				}
			}
		}

		//
		// -- Solution and template names provided
		//
		if(solutionName == null && templateName == null)
			templateName = DEFAULT_POINT_TO_POINT_SOLUTION;
		
		if(solutionName != null && templateName == null && fileData == null)
			templateName = DEFAULT_POINT_TO_POINT_SOLUTION;
		
		if (solutionName != null && templateName != null) {
			createConfigurationFromTemplate(sess, templateName, solutionName);
			return;
		}

		if (solutionName == null || fileData == null)
			throw new Exception("Solution or file data missing");

		if (solutionName.endsWith(".xml"))
			solutionName = solutionName.substring(0, solutionName.lastIndexOf(".xml"));

		solutionName = solutionName.trim();
		if (solutionName.length() == 0)
			throw new Exception("No filename specified");

		File file;
		if (template)
			file = new File(TEMPLATES_DIR, solutionName + ".xml");
		else
			file = new File(CONFIGS_DIR, solutionName + ".xml");

		if (file.exists() && !replace)
			throw new Exception("Solution already exists: " + file.getAbsolutePath());

		FileOutputStream out = new FileOutputStream(file);
		try {
			out.write(fileData.getBytes("UTF-8"));
		} finally {
			out.close();
		}

		try {
			MetamergeConfigFactory.unregisterNamespace(file.getAbsolutePath());
			MetamergeConfig mc = MetamergeConfigFactory.loadNamespace(file.getAbsolutePath());
			mc.getSolutionInterface().setInstanceID(solutionName);
			if (mc instanceof MetamergeConfigXML) {
				((MetamergeConfigXML)mc).setNoBackupOfOldVersion();
			}
			mc.commitChanges(null);
			if (!template) {
				// Remove from configs before checking in
				if (!file.delete())
					SystemFunctions.doNothing();
				sess.createNewConfiguration(solutionName + ".xml", replace);
				sess.checkInConfiguration(mc, solutionName + ".xml");
			}
		} catch (Exception e) {
			if (!file.delete())
				SystemFunctions.doNothing();
			throw new Exception("The submitted file is not a recognized SDI configuration file", e);
		}

	}

	private String readStream(InputStream inputStream) throws Exception {
		if (inputStream == null)
			return null;

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		int ch;
		try {
			while ((ch = inputStream.read()) != -1)
				bos.write(ch);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bos.toString("UTF-8");
	}

	private void createConfigurationFromTemplate(Session sess, String id, String target) throws Exception {
		MetamergeConfig mc = null;
		boolean template = id.startsWith("T:");
		String templateName = id;
		if (id.startsWith("T:") || id.startsWith("P:"))
			templateName = id.substring(2);

		if (DEFAULT_POINT_TO_POINT_SOLUTION.equals(templateName)) {
			Hashtable<String, Object> env = new Hashtable<String, Object>();
			env.put(MetamergeConfigFactory.MC_URL, getClass().getResourceAsStream("/DefaultSolution.xml"));
			env.put(MetamergeConfigFactory.MC_DRIVER, "com.ibm.di.config.xml.MetamergeConfigXML");
			mc = MetamergeConfigFactory.getInstance(env);
			String templateAL = MetamergeConfig.DEFAULT_ASSEMBLYLINE_FOLDER + "/P2P";
			AssemblyLineConfig al = (AssemblyLineConfig) mc.lookup(templateAL);
			mc.unbind(templateAL);
			al.setName(target);
			mc.bind(MetamergeConfig.DEFAULT_ASSEMBLYLINE_FOLDER + "/" + target, al);
		} else {
			if (!templateName.endsWith(".xml"))
				templateName = templateName + ".xml";
			File file = new File(template ? TEMPLATES_DIR : sess.getConfigFolderPath(), templateName);

			if (!file.getCanonicalPath().equals(file.getAbsolutePath())) {
				throw new FileNotFoundException(templateName);
			}

			if (!file.exists())
				throw new FileNotFoundException(templateName);
			mc = MetamergeConfigFactory.loadNamespace(file.getAbsolutePath());
			try {
				// rename "main" assemblyline in case it is an EasyETL AL
				AssemblyLineConfig alc = mc.getAssemblyLine(mc.getSolutionInterface().getInstanceID());
				if(alc != null) {
					mc.rename(alc.getName(), target);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		mc.getSolutionInterface().setInstanceID(target);
		sess.createNewConfiguration(target + ".xml", false);
		sess.checkInConfiguration(mc, target + ".xml");
	}

	public boolean accept(File dir, String name) {
		return name != null && name.endsWith(".xml");
	}

}
