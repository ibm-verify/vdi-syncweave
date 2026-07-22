/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.rest.internal.util;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Map.Entry;

import com.ibm.di.web.common.atom.AtomText;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.di.api.DIException;
import com.ibm.di.api.connection.IServerAPIConnection;
import com.ibm.di.api.connection.IServerAPIConnectionService;
import com.ibm.di.api.remote.Session;
import com.ibm.di.api.rest.internal.AppConstants;
import com.ibm.di.api.rest.internal.listener.ListenerFactory;
import com.ibm.di.api.rest.internal.registry.ListenerRegistry;
import com.ibm.di.api.rest.internal.registry.UserDataRegistry;
import com.ibm.di.systemqueue.driver.JMSDriver;

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
public class EnvUtils {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	private static final Logger log = LoggerFactory.getLogger(EnvUtils.class);

	public static Session getServerApiSession(HttpServletRequest req) throws DIException, RemoteException, NotBoundException {
		return getServerApiSession(req.getSession());
	}

	public static Session getServerApiSession(HttpSession sess) throws DIException, RemoteException, NotBoundException {
		Session s = (Session) sess.getAttribute(Session.class.getCanonicalName());
		if (s == null) {
			ServletContext ctx = sess.getServletContext();
			log.debug("Missing {} in the HttpSession. Will try to obtain one through {}", Session.class.getCanonicalName(),
					IServerAPIConnection.class.getCanonicalName());

			// create an anonymous session to the server api.
			s = getServerApiConnection(ctx).getSessionFactory().createSession();
			if (s == null) {
				log.debug("Could not create a {}", Session.class.getCanonicalName());
				throw new InternalError(Session.class.getCanonicalName());
			}

			sess.setAttribute(Session.class.getCanonicalName(), s);
		}
		return s;
	}

	public static IServerAPIConnectionService getServerApiConnectionService(ServletContext ctx) throws DIException {
		IServerAPIConnectionService srvc = (IServerAPIConnectionService) ctx.getAttribute(IServerAPIConnectionService.class
				.getCanonicalName());
		if (srvc == null) {
			log.debug("Could not find a {}", IServerAPIConnectionService.class.getCanonicalName());
			throw new InternalError(IServerAPIConnectionService.class.getCanonicalName());
		}

		return srvc;
	}

	public static IServerAPIConnection getServerApiConnection(ServletContext ctx) throws DIException {
		IServerAPIConnection conn = (IServerAPIConnection) ctx.getAttribute(IServerAPIConnection.class.getCanonicalName());

		if (conn == null) {
			log.debug("Missing {} in the ServletContext. Will try to obtain one through {}", IServerAPIConnection.class
					.getCanonicalName(), IServerAPIConnectionService.class.getCanonicalName());

			conn = getServerApiConnectionService(ctx).getConnection();
			if (conn == null) {
				log.debug("Could not find a {}", IServerAPIConnection.class.getCanonicalName());
				throw new InternalError(IServerAPIConnection.class.getCanonicalName());
			}

			ctx.setAttribute(IServerAPIConnection.class.getCanonicalName(), conn);
		}

		return conn;
	}

	public static ListenerRegistry getListenerRegistry(ServletContext ctx) {
		return (ListenerRegistry) ctx.getAttribute(ListenerRegistry.class.getName());
	}

	public static ListenerFactory getListenerFactory(ServletContext ctx) {
		return (ListenerFactory) ctx.getAttribute(ListenerFactory.class.getName());
	}

	public static UserDataRegistry getUserDataRegistry(ServletContext ctx) {
		return (UserDataRegistry) ctx.getAttribute(UserDataRegistry.class.getName());
	}

	public static QueueConnection getQueueConnection(ServletContext sctx) throws Exception {
		QueueConnection qc = (QueueConnection) sctx.getAttribute(QueueConnection.class.getName());
		if (qc == null) {
			synchronized (sctx) {
				qc = (QueueConnection) sctx.getAttribute(QueueConnection.class.getName());
				if (qc == null) {
					String user = System.getProperty("api.rest.jmsdriver.auth.username");
					qc = user == null ? getQueueConnectionFactory(sctx).createQueueConnection() : getQueueConnectionFactory(sctx)
							.createQueueConnection(user, System.getProperty("api.rest.jmsdriver.auth.password", ""));
					sctx.setAttribute(QueueConnection.class.getName(), qc);
					qc.start();
				}
			}
		}
		return qc;
	}

	private static QueueConnectionFactory getQueueConnectionFactory(ServletContext sctx) throws Exception {
		QueueConnectionFactory qcf = (QueueConnectionFactory) sctx.getAttribute(QueueConnectionFactory.class.getName());
		if (qcf == null) {
			synchronized (sctx) {
				qcf = (QueueConnectionFactory) sctx.getAttribute(QueueConnectionFactory.class.getName());
				if (qcf == null) {
					qcf = getJmsDriver(sctx).getQueueFactory();
					sctx.setAttribute(QueueConnectionFactory.class.getName(), qcf);
				}
			}
		}

		return qcf;
	}

	private static JMSDriver getJmsDriver(ServletContext sctx) {
		JMSDriver driver = (JMSDriver) sctx.getAttribute(JMSDriver.class.getName());
		if (driver == null) {
			synchronized (sctx) {
				driver = (JMSDriver) sctx.getAttribute(JMSDriver.class.getName());
				if (driver == null) {
					String driverName = System.getProperty("api.rest.jmsdriver.name");
					if (driverName == null) {
						throw new IllegalStateException(AppConstants.L10N.getString("REST.API.JMS.CONFIG.MISSING"));
					}

					Hashtable<String, String> dict = new Hashtable<String, String>();

					Properties props = System.getProperties();
					for (Entry<Object, Object> e : props.entrySet()) {
						if (e.getKey().toString().startsWith("api.rest.jmsdriver.param.")) {
							dict
									.put(e.getKey().toString().substring("api.rest.jmsdriver.param.".length()), e.getValue()
											.toString());
						}
					}

					try {
						driver = (JMSDriver) EnvUtils.class.getClassLoader().loadClass(driverName).newInstance();
						driver.initialize(dict);
						sctx.setAttribute(JMSDriver.class.getName(), driver);
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
				}
			}
		}
		return driver;
	}
}
