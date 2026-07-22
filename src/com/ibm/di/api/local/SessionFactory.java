/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.local;

import com.ibm.di.api.DIException;

/**
 * 
 * This interface provides methods for creating a Session.
 * 
 */
public interface SessionFactory {

	/**
	 * Creates a session object.
	 * <p>
	 * <b>Example:</b>
	 * </p>
	 * <pre>
	 * var session = APIEngine.getLocalSession();
	 * var runningConfigs = session.getConfigInstances();
	 * </pre>
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * 	var session = (new com.ibm.di.api.local.impl.SessionFactoryImpl).createSession();
	 * 	var serverInfo = session.getServerInfo();
	 * 	if (serverInfo == null) {
	 * 	throw new Exception(&quot;Server version information is not available!&quot;);
	 * 	}
	 * 
	 * 	var serverVersion = serverInfo.getServerVersion();
	 * 	if (serverVersion.startsWith(&quot;7.1&quot;)) { 
	 * 	// TDI 7.1 specific code
	 * 	}
	 * 	else if (serverVersion.startsWith(&quot;7.0&quot;)) {
	 * 	// TDI 7.0 specific code
	 * 	}
	 * 	else if (serverVersion.startsWith(&quot;6.1&quot;)) {
	 * 	// TDI 6.1 specific code
	 * 	}
	 * 	else {
	 * 	throw new Exception(&quot;Unsupported TDI server version: &quot; + serverVersion);
	 * 	}
	 * </pre>
	 * 
	 * @return The Session object.
	 * @throws DIException
	 *             if an error occurs while creating Session.
	 */
	public Session createSession() throws DIException;

	/**
	 * Creates a session object with the specified username and password.
	 * <p>
	 * <b>Example:</b>
	 * </p>
	 * <pre>
	 * var session = APIEngine.getLocalSession(&quot;username&quot;, &quot;password&quot;);
	 * var runningConfigs = session.getConfigInstances();
	 * </pre> 
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * 	var nick = &quot;Username&quot;;
	 * 	var pass = &quot;Difficult password&quot;
	 * </pre>
	 * 	//<b>Note</b>custom authentication must be enabled
	 * <pre>
	 * 	var session = (new com.ibm.di.api.local.impl.SessionFactoryImpl).createSession(nick , pass);
	 * 	var serverInfo = session.getServerInfo();
	 * 	if (serverInfo == null) {
	 * 	throw new Exception(&quot;Server version information is not available!&quot;);
	 * 	}
	 * 
	 * 	var serverVersion = serverInfo.getServerVersion();
	 * 	if (serverVersion.startsWith(&quot;7.1&quot;)) { 
	 * 	// TDI 7.1 specific code
	 * 	}
	 * 	else if (serverVersion.startsWith(&quot;7.0&quot;)) {
	 * 	// TDI 7.0 specific code
	 * 	}
	 * 	else if (serverVersion.startsWith(&quot;6.1&quot;)) {
	 * 	// TDI 6.1 specific code
	 * 	}
	 * 	else {
	 * 	throw new Exception(&quot;Unsupported TDI server version: &quot; + serverVersion);
	 * 	}
	 * </pre>
	 * 
	 * @param aUserName
	 *            the username for authentication.
	 * @param aPassword
	 *            the password for authentication.
	 * @return The Session object.
	 * @throws DIException
	 *             if an error occurs while creating Session.
	 */
	public Session createSession(String aUserName, String aPassword)
			throws DIException;

}
