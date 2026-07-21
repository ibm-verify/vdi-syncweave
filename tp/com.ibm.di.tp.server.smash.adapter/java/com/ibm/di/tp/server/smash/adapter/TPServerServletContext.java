/*
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * (C) Copyright IBM Corporation. 2009, 2011
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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.di.tp.server.servlet.TPServerServlet;

import zero.core.context.GlobalContext;

/**
 * This class represents the context the {@link TPServerServlet} is running
 * into. <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public class TPServerServletContext implements ServletContext {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	private static final Logger log = LoggerFactory.getLogger("tp-server");

	private final ServletConfig servletConfig;
	private final Map<String, Object> attributes = new HashMap<String, Object>();

	/**
	 * @param servletConfig
	 */
	public TPServerServletContext(ServletConfig servletConfig) {
		this.servletConfig = servletConfig;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContext#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String name) {
		return attributes.get(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContext#getAttributeNames()
	 */
	@SuppressWarnings("unchecked")
	public Enumeration getAttributeNames() {
		return Collections.enumeration(attributes.keySet());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContext#getContext(java.lang.String)
	 */
	public ServletContext getContext(String uripath) {
		log.info("ServletContext.getContext(\"" + uripath + "\") is not implemented!");
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContext#getContextPath()
	 */
	public String getContextPath() {
		return GlobalContext.zget("/config/com/ibm/di/tp/server/context/path", "");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContext#getInitParameter(java.lang.String)
	 */
	public String getInitParameter(String name) {
		return servletConfig.getInitParameter(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContext#getInitParameterNames()
	 */
	@SuppressWarnings("unchecked")
	public Enumeration getInitParameterNames() {
		return servletConfig.getInitParameterNames();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContext#getMajorVersion()
	 */
	public int getMajorVersion() {
		return 2;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContext#getMimeType(java.lang.String)
	 */
	public String getMimeType(String file) {
		log.info("ServletContext.getMimeType(\"" + file + "\") is not implemented!");
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContext#getMinorVersion()
	 */
	public int getMinorVersion() {
		return 4;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContext#getNamedDispatcher(java.lang.String)
	 */
	public RequestDispatcher getNamedDispatcher(String name) {
		log.info("ServletContext.getNamedDispatcher(\"" + name + "\") is not implemented!");
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContext#getRealPath(java.lang.String)
	 */
	public String getRealPath(String path) {
		log.info("ServletContext.getRealPath(\"" + path + "\") is not implemented!");
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContext#getRequestDispatcher(java.lang.String)
	 */
	public RequestDispatcher getRequestDispatcher(String path) {
		log.info("ServletContext.getRequestDispatcher(\"" + path + "\") is not implemented!");
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContext#getResource(java.lang.String)
	 */
	public URL getResource(String path) throws MalformedURLException {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		URL res = cl.getResource(path);

		if (res == null) {
			cl = TPServerServletContext.class.getClassLoader();
			res = cl.getResource(path);

			if (res == null) {
				res = ClassLoader.getSystemResource(path);
			}
		}

		return res;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContext#getResourceAsStream(java.lang.String)
	 */
	public InputStream getResourceAsStream(String path) {
		URL u = null;
		try {
			u = getResource(path);
		} catch (MalformedURLException e) {
		}

		if (u != null) {
			try {
				return u.openStream();
			} catch (IOException e) {
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContext#getResourcePaths(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public Set getResourcePaths(String path) {
		log.info("ServletContext.getResourcePaths(\"" + path + "\") is not implemented!");
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContext#getServerInfo()
	 */
	public String getServerInfo() {
		return "sMash ServletAdapter for JAX-RS";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContext#getServlet(java.lang.String)
	 */
	public Servlet getServlet(String name) throws ServletException {
		log.info("ServletContext.getServlet(\"" + name + "\") calling a deprecated method!");
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContext#getServletContextName()
	 */
	public String getServletContextName() {
		return "com.ibm.di.tp.server";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContext#getServletNames()
	 */
	@SuppressWarnings("unchecked")
	public Enumeration getServletNames() {
		log.info("ServletContext.getServletNames() calling a deprecated method!");
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContext#getServlets()
	 */
	@SuppressWarnings("unchecked")
	public Enumeration getServlets() {
		log.info("ServletContext.getServlets() calling a deprecated method!");
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContext#log(java.lang.String)
	 */
	public void log(String msg) {
		log.info(msg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContext#log(java.lang.Exception,
	 * java.lang.String)
	 */
	public void log(Exception exception, String msg) {
		log.info("ServletContext.log() calling a deprecated method!");
		log.error(msg, exception);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContext#log(java.lang.String,
	 * java.lang.Throwable)
	 */
	public void log(String message, Throwable throwable) {
		log.error(message, throwable);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContext#removeAttribute(java.lang.String)
	 */
	public void removeAttribute(String name) {
		attributes.remove(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContext#setAttribute(java.lang.String,
	 * java.lang.Object)
	 */
	public void setAttribute(String name, Object object) {
		attributes.put(name, object);
	}
}
