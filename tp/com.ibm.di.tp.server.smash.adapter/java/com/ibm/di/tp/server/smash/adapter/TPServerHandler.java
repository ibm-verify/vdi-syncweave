/*
 *
 * OCO Source Materials
 *
 * Copyright contributors to the SyncWeave project
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 *
 *
 * @version     %I%, %G%
 * @owner
 * @history
 */
package com.ibm.di.tp.server.smash.adapter;

import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import zero.core.context.GlobalContext;

import com.ibm.di.tp.server.servlet.TPServerServlet;

/**
 * This class adapts the events received from the sMash's Dispatcher Engine to
 * requests in the Servlet API form. <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public class TPServerHandler {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final HttpServlet servlet = new TPServerServlet();

	public TPServerHandler() {
		setSystemProparties();

		try {
			servlet.init(new TPServerServletConfig());
		} catch (ServletException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private void setSystemProparties() {
		Map<String, String> map = GlobalContext.zget("/config/com/ibm/di/tp/server/properties");

		for (Entry<String, String> prop : map.entrySet()) {
			if (System.getProperty(prop.getKey()) == null) {
				System.setProperty(prop.getKey(), prop.getValue());
			}
		}
	}

	private void doService() throws Exception {
		SmashResponseAdapter response = new SmashResponseAdapter(servlet);
		servlet.service(new SmashRequestAdapter(servlet), response);
		response.flushBuffer();
	}

	public void onGET() throws Exception {
		doService();
	}

	public void onPUT() throws Exception {
		doService();
	}

	public void onPOST() throws Exception {
		doService();
	}

	public void onDELETE() throws Exception {
		doService();
	}
}
