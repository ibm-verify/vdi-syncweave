/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.ui.webui.internal.handler.log;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.ibm.di.api.remote.Session;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.ui.webui.bind.Logfile;
import com.ibm.di.ui.webui.bind.Logfiles;
import com.ibm.di.ui.webui.bind.Logsearch;
import com.ibm.di.ui.webui.internal.CustomMedia2JaxbJSONProvider;
import com.ibm.di.ui.webui.internal.SessionUtils;

@Path(LogfilesHandler.URL)
public class LogfilesHandler {

	public static final String URL = "log";
	
	/*
	 * Max number of logfiles to return
	 */
	private int maxLogfiles = 100;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getIbmdiLogs(@Context HttpServletRequest req) throws Exception {
		File dir = new File("logs");
		File[] files = dir.listFiles(new FileFilter() {
			public boolean accept(File file) {
				String name = file.getName();
				if(name.startsWith("."))
					return false;
				else
					return name.indexOf(".log") != -1;
			}
		});
		
		Logfiles logs = new Logfiles();
		for(File f : files) {
			Logfile logfile = new Logfile();
			logfile.setName(f.getName());
			logfile.setSize(f.length());
			logfile.setModified(f.lastModified());
			logfile.setPath(f.getPath());
			logs.getItems().add(logfile);
			if(logs.getItems().size() > maxLogfiles)
				break;
		}
		
		return Response.ok(logs).build();
	}

	@POST
	@Consumes(CustomMedia2JaxbJSONProvider.MT_LOGSEARCH)
	@Produces(CustomMedia2JaxbJSONProvider.MT_LOGSEARCH)
	@Path("search")
	public Response getIbmdiLog(@Context HttpServletRequest req, Logsearch search) throws Exception {
		LogWrapper log = new LogWrapper(search);
		return Response.ok(log.search(SessionUtils.getServerApiSession(req))).build();
	}
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("download/{logfile: .*}")
	public Response getLogContents(@Context HttpServletRequest req, @PathParam("logfile") String logfile) throws Exception {
		// log files only
		if(logfile == null)
			return Response.status(Status.FORBIDDEN).build();

		if(!(logfile.startsWith("logs") || logfile.startsWith("system_logs")))
			return Response.status(Status.FORBIDDEN).build();

		if (!validateLogPath(logfile)) {
			return Response.status(Status.FORBIDDEN).build();
		}

		FileInputStream fis = new FileInputStream(logfile);
		return Response.ok(fis).build();
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{logfile}")
	public Response getLogDetails(@Context HttpServletRequest req, @PathParam("logfile") String logfile) throws Exception {
		File file = new File("logs/" + logfile);

		if (!validateLogPath(file.getPath())) {
			return Response.status(Status.FORBIDDEN).build();
		}

		Logfile log = new Logfile();
		log.setName(log.getName());
		log.setPath("logs/" + URLEncoder.encode(logfile));
		if(file.exists()) {
			log.setModified(file.lastModified());
			log.setSize(file.length());
		}
		return Response.ok(log).build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{configid}/{assemblyline}")
	public Response getAssemblyLineLogs(@Context HttpServletRequest req, @PathParam("configid") String config, @PathParam("assemblyline") final String assemblyline) throws Exception {
		Logfiles logs = new Logfiles();
		logs.setAssemblyline(assemblyline);
		logs.setConfig(config);
		
		File logDir = new File(LogWrapper.ROOT_LOG_DIR + config + "/" + LogWrapper.AL_LOG_DIR_PREFIX + assemblyline); 

		if (!validateLogPath(logDir.getPath())) {
			return Response.status(Status.FORBIDDEN).build();
		}

		if (logDir.exists()) {
			File[] logFiles = logDir.listFiles(new FilenameFilter() {
				public boolean accept(File aDir, String aName) {
					if (!aName.startsWith(assemblyline)) {
						return false;
					}
					if (!aName.endsWith(".log")) {
						return false;
					}
					return true;
				}
			});
			for(File f : logFiles) {
				Logfile log = new Logfile();
				log.setName(f.getName());
				log.setModified(f.lastModified());
				log.setSize(f.length());
				log.setPath(LogWrapper.ROOT_LOG_DIR + URLEncoder.encode(config) + "/" + LogWrapper.AL_LOG_DIR_PREFIX + URLEncoder.encode(assemblyline) + "/" + f.getName());
				logs.getItems().add(log);
			}
		}
		return Response.ok(logs).build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{configid}/{assemblyline}/{logfile}")
	public Response getAssemblyLineLogDetails(@Context HttpServletRequest req, @PathParam("configid") String config,
			@PathParam("assemblyline") String assemblyline, @PathParam("logfile") String logfile) throws Exception {

		File file = new File(LogWrapper.ROOT_LOG_DIR + config + "/" + LogWrapper.AL_LOG_DIR_PREFIX + assemblyline + "/" + logfile);

		if (!validateLogPath(file.getPath())) {
			return Response.status(Status.FORBIDDEN).build();
		}

		Logfile log = new Logfile();
		log.setName(log.getName());
		if(file.exists()) {
			log.setModified(file.lastModified());
			log.setSize(file.length());
			log.setName(URLEncoder.encode(file.getName()));
			log.setPath(LogWrapper.ROOT_LOG_DIR + URLEncoder.encode(config) + "/" + LogWrapper.AL_LOG_DIR_PREFIX + URLEncoder.encode(assemblyline) + "/" + URLEncoder.encode(logfile));
		}
		return Response.ok(log).build();
	}
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("{configid}/{assemblyline}/{logfile}")
	public String getAssemblyLineLogFile(@Context HttpServletRequest req, @PathParam("configid") String config,
			@PathParam("assemblyline") String assemblyline, @PathParam("logfile") String logfile,
			@DefaultValue("0") @QueryParam("size") int size, @DefaultValue("false") @QueryParam("fuzzy") boolean fuzzy)
			throws Exception {

		Session sess = SessionUtils.getServerApiSession(req);

		String logname = fuzzy ? findLogForAssemblyLine(sess, config, assemblyline, logfile) : logfile;
		if (size > 0)
			return sess.getSystemLog().getALLogLastChunk(config, assemblyline, logname, size);
		else
			return sess.getSystemLog().getALLog(config, assemblyline, logname);
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	@Path("{configid}/{assemblyline}/{logfile}")
	public String getAssemblyLineLogFileHtml(@Context HttpServletRequest req, @PathParam("configid") String config,
			@PathParam("assemblyline") String assemblyline, @PathParam("logfile") String logfile,
			@DefaultValue("0") @QueryParam("size") int size, @DefaultValue("false") @QueryParam("fuzzy") boolean fuzzy)
			throws Exception {
		StringTokenizer tok = new StringTokenizer(getAssemblyLineLogFile(req, config, assemblyline, logfile, size, fuzzy), "\r\n");
		StringBuffer html = new StringBuffer();

		// 2011-09-02 09:48:52,860 INFO
		// [com.ibm.di.api.syslog.SystemLogAppender.fb49193f-997d-4981-a444-e2a09a8a25f9]
		// - CTGDIS967I As
		Pattern pattern = Pattern.compile("(.*) (.*) (INFO|DEBUG|WARN).*\\[.*\\] (.*)");
		String lastDate = null;

		while (tok.hasMoreElements()) {
			String str = tok.nextToken();
			Matcher m = pattern.matcher(str);
			try {
				if (m.matches()) {
					String date = m.group(1);
					String time = m.group(2);
					if (time.indexOf(",") != -1)
						time = time.substring(0, time.indexOf(","));
					String type = m.group(3);
					String msg = m.group(4);
					if (!date.equals(lastDate)) {
						html.append("*** " + date + "\n");
					}
					lastDate = date;
					html.append(time);
					html.append(" ");
					html.append(type);
					html.append(" ");
					html.append(msg);
					html.append("\n");
					continue;
				}
			} catch (Exception err) {
				SystemFunctions.doNothing();
				// ignore and print entire line
			}
			int start = str.indexOf("[");
			int end = str.indexOf("]");
			if (start != -1 && end != -1) {
				html.append(str.substring(0, start));
				html.append(str.substring(end + 1));
				// 2011-09-02 09:48:54,721
			} else {
				html.append(str);
			}
			html.append("\n");
		}
		return html.toString();
	}

	private boolean validateLogPath(String logfile) {
		boolean rv = false;

		try {
			File file = new File(logfile);

			rv = file.getCanonicalPath().equals(file.getAbsolutePath());
		} catch (Exception err) {
			SystemFunctions.doNothing();
		}

		return rv;
	}

	private String findLogForAssemblyLine(Session sess, String config, String assemblyline, String timestamp) throws Exception {
		String closestMatch = null;
		long lastDiff = Long.MAX_VALUE;
		Pattern p = Pattern.compile(".*(\\d{4}_\\d{2}_\\d{2}__\\d{2}_\\d{2}_\\d{2}_\\d{3})\\.log");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd__HH_mm_ss_SSS");
		Date ts = sdf.parse(timestamp);

		for (String str : sess.getSystemLog().getALLogFileNames(config, assemblyline)) {
			// LongRunning_2011_10_11__11_39_49_621
			Matcher m = p.matcher(str);
			if (m.matches()) {
				Date d = sdf.parse(m.group(1));
				// -- Start time is never after the log filename timestamp
				if (!d.before(ts)) {
					long diff = d.getTime() - ts.getTime();
					if (diff < lastDiff) {
						closestMatch = str;
						lastDiff = diff;
					}
				}
			}
		}
		return closestMatch != null ? closestMatch : timestamp;
	}
}
