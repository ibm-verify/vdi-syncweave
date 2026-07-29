/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.rest.internal.registry;

import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.ibm.di.web.common.atom.AtomText;
import javax.servlet.http.HttpServletRequest;

/**
 * A registry that provides isolation of the user state beyond the lifetime of
 * his/her session. In fact allows user data to be shared between multiple
 * sessions the same user has created. <br>
 * Note: no auto-cleaning is provided. Any user state registered here will need
 * to be manually cleaned up.<br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.2
 */
public class UserDataRegistry {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	// locks access to the outer-most map the usersMap var holds inner maps are
	// individually synchronized.
	private ReadWriteLock lock = new ReentrantReadWriteLock();

	private Map<String, Map<String, Object>> usersData = new HashMap<String, Map<String, Object>>();

	public Object setData(HttpServletRequest usrReq, String name, Object object) {
		String username = getUsername(usrReq);
		Map<String, Object> userMap = getUserMap(username);
		synchronized (userMap) {
			return userMap.put(name, object);
		}
	}

	public Object getData(HttpServletRequest usrReq, String name) {
		String username = getUsername(usrReq);
		Map<String, Object> userMap = getUserMap(username);
		synchronized (userMap) {
			return userMap.get(name);
		}
	}

	public Object removeData(HttpServletRequest usrReq, String name) {
		String username = getUsername(usrReq);
		Map<String, Object> userMap = getUserMap(username);
		synchronized (userMap) {
			return userMap.remove(name);
		}
	}

	private Map<String, Object> getUserMap(String username) {
		lock.readLock().lock();
		Map<String, Object> userMap = usersData.get(username);
		lock.readLock().unlock();
		if (userMap == null) {
			lock.writeLock().lock();
			userMap = usersData.get(username);
			if (userMap == null) {
				userMap = new HashMap<String, Object>();
				usersData.put(username, userMap);
			}
			lock.writeLock().unlock();
		}
		return userMap;
	}

	private String getUsername(HttpServletRequest r) {
		String username = null;
		String auth = r.getAuthType();
		if (auth == HttpServletRequest.BASIC_AUTH) {
			username = r.getUserPrincipal().getName();
		} else if (auth == HttpServletRequest.CLIENT_CERT_AUTH) {
			Principal p = r.getUserPrincipal();
			if (p != null) {
				username = p.getName();
			}

			if (username == null) {
				X509Certificate[] x509certs = (X509Certificate[]) r.getAttribute("javax.servlet.request.X509Certificate");
				if (x509certs != null && x509certs.length > 0 && x509certs[0].getSubjectDN() != null) {
					username = x509certs[0].getSubjectDN().toString();
				}
			}
		}

		return username;
	}
}
