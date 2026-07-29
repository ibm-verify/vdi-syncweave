/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.ui.webui.internal.tdi;

import java.io.IOException;
import java.net.URL;
import java.util.Dictionary;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.http.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.di.web.common.internal.auth.LocalApiAuthHttpContext;

public class TDIAuthHttpContext extends LocalApiAuthHttpContext {
	
	/**
	 * Logger.
	 */
	private static final Logger log = LoggerFactory.getLogger(TDIAuthHttpContext.class);
	
	/**
	 * 
	 */
	private boolean debug = Boolean.getBoolean("tdiservlet.debug");
	
	public TDIAuthHttpContext(HttpContext defaultHttpContext, Dictionary<Object, Object> props) {
		super(defaultHttpContext, props);
	}
	
	public void refresh(Map<String, String> props) {
	}
	
	/**
	 * @return null
	 */
	@Override
	public URL getResource(String name) {
		if(name != null && name.indexOf("//") != -1) {
			name = name.replaceAll("//", "/");
		}
		return super.getResource(name);
	}

	@Override
	public boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException {
		if(debug) {
			log.debug("request=" + request.getRequestURL());
		}
		return true;
	}

}
