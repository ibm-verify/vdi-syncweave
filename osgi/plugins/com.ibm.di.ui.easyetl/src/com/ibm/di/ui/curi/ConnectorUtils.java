/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.ui.curi;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.di.api.remote.AssemblyLineHandler;
import com.ibm.di.api.remote.ConfigInstance;
import com.ibm.di.api.remote.Session;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.AttributeMapItem;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.TaskCallBlock;
import com.ibm.di.ui.easyetl.internal.SessionUtils;

public class ConnectorUtils {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	private static final Logger log = LoggerFactory.getLogger(CuriHandler.class);

	private ConfigInstance ci;
	private AssemblyLineHandler al;
	//HttpServletRequest req = null;

	public void terminate() {
		try {
			ci.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getScriptEngineVariable(String variable) throws Exception {
		Serializable value = al.eval(variable);
		return value.toString();
	}

	public Entry getRequestHeaders(HttpServletRequest req) throws Exception {
		Entry headers = new Entry();
		for (Enumeration en = req.getHeaderNames(); en.hasMoreElements();) {
			String hdr = en.nextElement().toString();
			for (Enumeration vals = req.getHeaders(hdr); vals.hasMoreElements();) {
				headers.addAttributeValue(hdr, vals.nextElement().toString());
			}
		}
		return headers;
	}

	public void startAL(HttpServletRequest req, String config, String al, HashMap<String, String> params) throws Exception {
		TaskCallBlock tcb = new TaskCallBlock();
		for (Map.Entry<String, String> e : params.entrySet()) {
			tcb.setOperationInitParam(e.getKey(), e.getValue());
		}
		tcb.setOperationInitParam("http_headers", getRequestHeaders(req));
		tcb.setOperationInitParam("http_method", req.getMethod());
		if ("POST".equals(req.getMethod())) {
			StringBuffer buf = new StringBuffer();
			String str;
			while ((str = req.getReader().readLine()) != null) {
				buf.append(str + "\n");
			}
			tcb.setOperationInitParam("http_body", buf.toString());
		}

		Session sess = SessionUtils.getServerApiSession(req);
		String runName = config + "_" + UUID.randomUUID().toString();
		this.ci = sess.startConfigInstance(config, false, null, runName, null);
		this.al = this.ci.startAssemblyLineManual(al, tcb);
		//this.req = req;
	}

	public void startAL(HttpServletRequest req, String connectorType, HashMap<String, String> params) throws Exception {

		Hashtable<String, String> ht = new Hashtable<String, String>();
		ht.put(MetamergeConfigFactory.MC_DRIVER, "com.ibm.di.config.xml.MetamergeConfigXML");

		MetamergeConfig mc = MetamergeConfigFactory.getInstance(ht);

		ConnectorConfig cc = mc.newInstanceOf(ConnectorConfig.class);
		cc.setInheritsFromRef("system:/Connectors/" + connectorType);
		cc.init();

		for (Map.Entry<String, String> e : params.entrySet()) {
			String param = e.getKey();
			if (param.startsWith("param_")) {
				param = param.substring("param_".length());
				cc.getConnectionConfig().setParameter(param, e.getValue());
			}
		}

		cc.getConnectionConfig().setInheritsFromRef(BaseConfiguration.INHERIT_PARENT);
		cc.getConnectionConfig().setDebug(true);
		cc.setName("QuerySchema");
		cc.setMode(ConnectorConfig.ITERATOR_MODE);

		// TODO: client may send list of return attributes
		AttributeMapItem ami = cc.getAttributeMap().newAttributeMapItem("*");
		ami.setSimple("*");

		AssemblyLineConfig alc = mc.newInstanceOf(AssemblyLineConfig.class);
		alc.init();
		alc.setName("QuerySchema");
		// alc.getSettings().setBooleanParameter("automapattributes", true);
		alc.getEntryFeedComponents().addConfig(cc);

		mc.bind(MetamergeConfig.DEFAULT_ASSEMBLYLINE_FOLDER + "/" + alc.getShortName(), alc);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		mc.commitChanges(bos);
		bos.flush();
		String xmlConfig = new String(bos.toByteArray());

		Session sess = SessionUtils.getServerApiSession(req);
		String runName = "_" + UUID.randomUUID().toString();
		this.ci = sess.startTempConfigInstance(xmlConfig, false, runName, null);
		if (this.ci != null) {
			this.al = ci.startAssemblyLineManual("QuerySchema", null);
			if (this.al == null)
				throw new Exception("Cannot start assemblyline");
		} else {
			throw new Exception("Cannot start config instance");
		}
		//this.req = req;
	}

	public Entry getNextEntry() throws Exception {
		return getNextEntry(null);
	}

	public Entry getNextEntry(Entry initialWork) throws Exception {
		Entry entry = null;
		try {
			entry = al.executeCycle(initialWork);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return entry;
	}

	public static void logdebug(String msg) {
		if (log != null)
			log.debug(msg);
	}

	public static void loginfo(String msg) {
		if (log != null)
			log.info(msg);
	}

	public static void logerror(String msg) {
		logerror(msg, null);
	}

	public static void logerror(String msg, Exception err) {
		if (log != null) {
			if (err != null)
				log.error(msg, err);
			else
				log.error(msg);
		}
	}
}
