/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

import com.ibm.di.api.DIException;
import com.ibm.di.api.remote.Session;
import com.ibm.di.server.ResourceHash;

/**
 * This class is not used anymore.
 * It was used earlier to hold information needed to access configurations on a remote server.
 * @deprecated
 *
 */
public class RemoteConfigURL implements Serializable {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final long serialVersionUID = -7176687394467120816L; 
	
	/**
	 * The default remote port is defined as a property (api.remote.naming.port)
	 * or we use 1099.
	 */
	public final static int DEFAULT_REMOTE_PORT = Integer.getInteger(
			"api.remote.naming.port", 1099).intValue();

	/**
	 * The URL "protocol"
	 */
	public final static String REMOTE_URL_PROTOCOL = "remote";

	private Session session;

	private String configID;

	private String configInstanceID;

	private URL url;

	private boolean sslOn = false;

	private final static ResourceHash sResHash = BaseConfigurationImpl
			.getResHash();

	/**
	 * RemoteConfigURL Constructor.
	 * 
	 * @param path
	 * @throws MalformedURLException
	 */
	public RemoteConfigURL(String path) throws MalformedURLException {
		if (path.matches("[0-9]{0,3}\\.[0-9]{0,3}\\.[0-9]{0,3}\\.[0-9]{0,3}")) {
			url = new URL("file://" + path + "/rs.xml ");
		} else if (path.startsWith("rmi://")) {
			url = new URL("file://" + path.split("//")[1]);
		} else if (path.startsWith("\\\\")) {
			url = new URL("file://" + path.split("\\\\")[1]);
		} else if (path.startsWith("remote:")) {
			url = new URL("http" + path.substring(6));
			configID = url.getFile();
		} else {
			throw new MalformedURLException();
		}
		configID = getFile();
	}

	public static boolean isRemoteConfigURL(String str) {
		if (str != null)
			return str.startsWith(REMOTE_URL_PROTOCOL + ":");
		else
			return false;
	}

	/**
	 * 
	 * @param ip
	 * @param port
	 * @param path
	 */
	public RemoteConfigURL(String ip, int port, String path)
			throws MalformedURLException {
		url = new URL("file", ip, port, path);
		configID = getFile();
	}

	/**
	 * Gets the hostname.
	 * 
	 * @return Returns the hostname.
	 */
	public String getHost() {
		return url.getHost();
	}

	/**
	 * Gets the port.
	 * 
	 * @return Returns the port number.
	 */
	public int getPort() {
		if (url.getPort() == -1)
			return DEFAULT_REMOTE_PORT;
		else
			return url.getPort();

	}

	/**
	 * Returns the userinfo
	 */
	public String getUserInfo() {
		return url.getUserInfo();
	}

	/**
	 * Gets the filename.
	 * 
	 * @return Returns the filename.
	 */
	public String getFile() {
		int n = url.getFile().lastIndexOf('/');
		if (n == -1)
			n = url.getFile().lastIndexOf('\\');
		if (n != -1)
			return url.getFile().substring(n + 1);
		if (url.getFile().startsWith("/"))
			return url.getFile().substring(1);
		return url.getFile();
	}

	/**
	 * 
	 * @param path
	 */
	public void setPath(String path) throws MalformedURLException {
		url = new URL(url.getProtocol(), url.getHost(), url.getPort(), path);
	}

	/**
	 * Gets the path.
	 * 
	 * @return Returns the path.
	 */
	public String getPath() {
		if (url.getPath().startsWith("/"))
			return url.getPath().substring(1);
		return url.getPath();
	}

	/**
	 * Method gets the RMI Connection URL.
	 * 
	 * @return A String containing the RMI connection URL.
	 */
	public String getRMIConnectionURL() {
		return "rmi://" + url.getHost() + ":"
				+ (url.getPort() == -1 ? DEFAULT_REMOTE_PORT : url.getPort())
				+ "/SessionFactory";
	}

	/**
	 * 
	 */
	public String toString() {
		return url.getHost() + "_"
				+ (url.getPort() == -1 ? DEFAULT_REMOTE_PORT : url.getPort())
				+ "_" + getFile();
	}

	/**
	 * Gets the Config ID.
	 * 
	 * @return A string containing the Config ID.
	 */
	public String getConfigID() {
		return configID;
	}

	/**
	 * 
	 * @param cID
	 */
	public void setConfigID(String cID) {
		configID = cID;
	}

	public boolean isSSLOn() {
		return sslOn;
	}

	public void setSSL(boolean ssl) {
		sslOn = ssl;
	}

	/**
	 * Method gets the Config Instance ID.
	 * 
	 * @return A String containing the ID of the Config Instance.
	 */
	public String getConfigInstanceID() {
		return configInstanceID;
	}

	/**
	 * @param string
	 *            The Config instance ID.
	 */
	public void setConfigInstanceID(String string) {
		configInstanceID = string;
	}

	/**
	 * Method gets the current session information.
	 * 
	 * @return A Session containing the session information.
	 * 
	 */
	public Session getSession() throws DIException {
		if (session == null) {
			throw new DIException(sResHash
					.getString("MMCONFIG.ERROR.SESSIONISNULL"));
		}
		return session;
	}

	/**
	 * @param session
	 *            The session to set.
	 */
	public void setSession(Session session) {
		this.session = session;
	}

}
