/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

import com.ibm.di.api.DIException;

/**
 * 
 * This interface provides methods for creating a Session.
 * 
 */
public interface SessionFactory extends Remote {

	/**
	 * Creates a session object.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var sf = java.rmi.Naming.lookup(&quot;rmi://127.0.0.1:1099/SessionFactory&quot;);
	 * var session = sf.createSession();
	 * task.logmsg(&quot;OS: &quot; + session.getServerInfo().getOperatingSystem());
	 * </pre>
     *
	 * @return The Session object.
	 * @throws DIException
	 *             if an error occurs while creating Session.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public Session createSession() throws DIException, RemoteException;

	/**
	 * Creates a session object with the specified username and password.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var nick = &quot;Username&quot;;
	 * var pass = &quot;Difficult password&quot;;
	 * var sf = java.rmi.Naming.lookup("rmi://127.0.0.1:1099/SessionFactory");
	 * var session = sf.createSession();
	 * task.logmsg(&quot;OS: &quot; + session.getServerInfo().getOperatingSystem());
	 * </pre>
     *
	 * @param aUserName
	 *            the username for authentication.
	 * @param aPassword
	 *            the password for authentication.
	 * @return The Session object.
	 * @throws DIException
	 *             if an error occurs while creating Session.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public Session createSession(String aUserName, String aPassword)
			throws DIException, RemoteException;

}
