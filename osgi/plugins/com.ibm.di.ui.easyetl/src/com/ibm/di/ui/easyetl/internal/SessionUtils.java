/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.ui.easyetl.internal;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.di.api.DIException;
import com.ibm.di.api.connection.IServerAPIConnection;
import com.ibm.di.api.connection.IServerAPIConnectionService;
import com.ibm.di.api.remote.Session;

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
public class SessionUtils {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	private static final Logger log = LoggerFactory.getLogger(SessionUtils.class);

	public static Session getServerApiSession(HttpServletRequest req) throws DIException, RemoteException, NotBoundException {
		return getServerApiSession(req.getSession());
	}

	public static Session getServerApiSession(HttpSession sess) throws DIException, RemoteException, NotBoundException {
		Session s = (Session) sess.getAttribute(Session.class.getCanonicalName());
		if (s == null) {
			log.debug("Missing {} in the ServeletContext. Will try to obtain one through {}", Session.class.getCanonicalName(),
					IServerAPIConnection.class.getCanonicalName());

			// no authentication and thus no local session.
			ServletContext ctx = sess.getServletContext();
			IServerAPIConnection conn = (IServerAPIConnection) ctx.getAttribute(IServerAPIConnection.class.getCanonicalName());

			if (conn == null) {
				log.debug("Missing {} in the ServeletContext. Will try to obtain one through {}", IServerAPIConnection.class
						.getCanonicalName(), IServerAPIConnectionService.class.getCanonicalName());

				IServerAPIConnectionService srvc = (IServerAPIConnectionService) ctx.getAttribute(IServerAPIConnectionService.class
						.getCanonicalName());
				if (srvc == null) {
					log.debug("Could not find a {}", IServerAPIConnectionService.class.getCanonicalName());
					throw new InternalError(IServerAPIConnectionService.class.getCanonicalName());
				}

				conn = srvc.getConnection();
				if (conn == null) {
					log.debug("Could not find a {}", IServerAPIConnection.class.getCanonicalName());
					throw new InternalError(IServerAPIConnection.class.getCanonicalName());
				}

				ctx.setAttribute(IServerAPIConnection.class.getCanonicalName(), conn);
			}

			// create an anonymous session to the server api.
			s = conn.getSessionFactory().createSession();
			if (s == null) {
				log.debug("Could not create a {}", Session.class.getCanonicalName());
				throw new InternalError(Session.class.getCanonicalName());
			}

			ctx.setAttribute(Session.class.getCanonicalName(), s);
		}
		return s;
	}
}
