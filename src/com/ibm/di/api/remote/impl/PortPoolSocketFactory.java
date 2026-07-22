/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.remote.impl;

import java.io.IOException;
import java.net.ServerSocket;
import java.rmi.server.RMIServerSocketFactory;
import java.util.ArrayList;
import java.util.List;

import com.ibm.di.api.APIEngine;

/**
 * This wrapper class constructs a Socket using a port from a list of available ports.
 * @since 7.1
 *
 */
public class PortPoolSocketFactory implements RMIServerSocketFactory {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Parameter name for List of ports for listening.
	 */
	private static final String PROP_API_PORTS = "api.remote.server.ports";

	/**
	 * List of available ports
	 */
	private static List<Integer> availablePorts;

	/**
	 * Index into availablePorts
	 */
	private static int availablePortIndex = 0;

	/**
	 * Factory that is actually used for creating sockets.
	 */
	private RMIServerSocketFactory myFactory;

	static {
		parsePorts(System.getProperty(PROP_API_PORTS));
	}
	
	/**
	 * Constructor specifying the real factory to use.
	 * @param orig Factory to use.
	 */
	public PortPoolSocketFactory(RMIServerSocketFactory orig) {
		myFactory = orig;
	}

	/**
	 *  Create a ServerSocket.
	 * @see java.rmi.server.RMIServerSocketFactory#createServerSocket(int)
	 * @param port If 0, use a port number from the list of available ports.
	 */
	public ServerSocket createServerSocket(int port) throws IOException {
		if (port == 0 && availablePorts != null) {
			for (int i = 0; i < availablePorts.size(); i++) {
				try {
					return newSocket(getAvailablePort());
				} catch (IOException io) {
					continue;								
				}
			}
			throw new IOException(APIEngine.getResHash().getString("PortPoolSocketFactory.out.of.ports"));
		}
		return newSocket(port);			
	}

	/**
	 * Create a Socket using the provided factory, or the default factory.
	 * @param port The port number to use.
	 * @return The created ServerSocket.
	 * @throws IOException
	 */
	private ServerSocket newSocket( int port ) throws IOException {
		if (myFactory != null)
			return myFactory.createServerSocket(port);
		else
			return new ServerSocket(port);		
	}

	/**
	 * Return the first available port from the list, or 0
	 * @return The first available port from the list
	 */
	private static int getAvailablePort() {
		if (availablePorts == null || availablePorts.size() == 0)
			return 0;
		synchronized (availablePorts) {
			if (availablePortIndex >= availablePorts.size())
				availablePortIndex = 0;
			return availablePorts.get(availablePortIndex++);
		}
	}

	/**
	 * Parse the system property with the list of ports
	 * @return A List of Integers with all the port numbers, or null if no list was found.
	 */
	static void parsePorts(String portList) {

		if (portList == null || portList.trim().length() == 0) {
			availablePorts = null;
			return;
		}

		availablePorts = new ArrayList<Integer>();

		for (String s: portList.split(",")) {
			int i = s.indexOf('-');
			if (i <= 0) {
				int port = Integer.valueOf(s);
				if (port > 0 && port < 65536)
					availablePorts.add(port);
				continue;
			}
			int start = Integer.valueOf(s.substring(0, i).trim());
			if (start <=0)
				start = 1;
			int end = Integer.valueOf(s.substring(i+1).trim());
			if (end > 65535)
				end = 65535;
			for ( int port = start; port <= end; port++ )
				availablePorts.add( port );
		}
		availablePortIndex = 0;
	}

	/** (non-JavaDoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		int h = getClass().hashCode();
		if (myFactory == null)
			return h;
		return h ^ myFactory.hashCode();
	}

	/** (non-JavaDoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (! (obj instanceof PortPoolSocketFactory))
			return false;
		PortPoolSocketFactory other = (PortPoolSocketFactory) obj;
		if (myFactory == null)
			return other.myFactory == null;
		return myFactory.equals(other.myFactory);
	}
}
