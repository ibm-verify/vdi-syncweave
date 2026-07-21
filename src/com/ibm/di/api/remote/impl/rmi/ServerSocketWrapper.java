/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.remote.impl.rmi;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.ServerSocketChannel;

/**
 * 
 * A wrapper class of ServerSocket class for use by the RMI. ServerSocket class
 * implements server sockets. A server socket waits for requests to come in over
 * the network. It performs some operation based on that request, and then
 * possibly returns a result to the requester.
 * 
 */
public class ServerSocketWrapper extends ServerSocket {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * <code>ServerSocket</code> object
	 */
	private ServerSocket mServer = null;

	/**
	 * Creates an unbound server socket.
	 * 
	 * @param aServer
	 *            <code>ServerSocket</code> object
	 * @throws IOException
	 *             IO error when opening the socket.
	 */
	public ServerSocketWrapper(ServerSocket aServer) throws IOException {
		super();
		mServer = aServer;
	}

	/**
	 * Binds the <code>ServerSocketWrapper</code> to a specific address (IP
	 * address and port number).
	 * 
	 * @param aEndpoint
	 *            The IP address & port number to bind to.
	 * @throws IOException
	 *             if the bind operation fails, or if the socket is already
	 *             bound.
	 */
	public void bind(SocketAddress aEndpoint) throws IOException {
		mServer.bind(aEndpoint);
	}

	/**
	 * Binds the <code>ServerSocketWrapper</code> to a specific address (IP
	 * address and port number) with specified backlog length.
	 * 
	 * @param aEndpoint
	 *            The IP address & port number to bind to.
	 * @param aBacklog
	 *            The listen backlog length.
	 * @throws IOException
	 *             if the bind operation fails, or if the socket is already
	 *             bound.
	 */
	public void bind(SocketAddress aEndpoint, int aBacklog) throws IOException {
		mServer.bind(aEndpoint, aBacklog);
	}

	/**
	 * Returns the local address of this server socket.
	 * 
	 * @return the address to which this socket is bound, or <code>null</code>
	 *         if the socket is unbound.
	 */
	public InetAddress getInetAddress() {
		return mServer.getInetAddress();
	}

	/**
	 * Returns the port on which this socket is listening.
	 * 
	 * @return the port number to which this socket is listening or -1 if the
	 *         socket is not bound yet.
	 */
	public int getLocalPort() {
		return mServer.getLocalPort();
	}

	/**
	 * Returns the address of the endpoint this socket is bound to, or
	 * <code>null</code> if it is not bound yet.
	 * 
	 * @return a <code>SocketAddress</code> representing the local endpoint of
	 *         this socket, or <code>null</code> if it is not bound yet.
	 * @see #getInetAddress()
	 * @see #getLocalPort()
	 */
	public SocketAddress getLocalSocketAddress() {
		return mServer.getLocalSocketAddress();
	}

	/**
	 * Listens for a connection to be made to this socket and accepts it. The
	 * method blocks until a connection is made.
	 * 
	 * @return the new SocketWrapper
	 * @throws IOException
	 *             if an I/O error occurs when waiting for a connection.
	 */
	public Socket accept() throws IOException {
		Socket socket = mServer.accept();
		return new SocketWrapper(socket);
	}

	/**
	 * Closes this socket.
	 * 
	 * @throws IOException
	 *             if an I/O error occurs when closing the socket.
	 */
	public void close() throws IOException {
		mServer.close();
	}

	/**
	 * Returns the unique {@link java.nio.channels.ServerSocketChannel} object
	 * associated with this socket, if any.
	 * 
	 * @return the server-socket channel associated with this socket, or
	 *         <tt>null</tt> if this socket was not created for a channel
	 */
	public ServerSocketChannel getChannel() {
		return mServer.getChannel();
	}

	/**
	 * Returns the binding state of the ServerSocket.
	 * 
	 * @return true if the ServerSocketWrapper succesfuly bound to an address
	 */
	public boolean isBound() {
		return mServer.isBound();
	}

	/**
	 * Returns the closed state of the ServerSocketWrapper.
	 * 
	 * @return true if the socket has been closed
	 */
	public boolean isClosed() {
		return mServer.isClosed();
	}

	/**
	 * Enable/disable SO_TIMEOUT with the specified timeout, in milliseconds.
	 * 
	 * @param aTimeout
	 *            the specified timeout, in milliseconds.
	 * @throws SocketException
	 *             if there is an error in the underlying protocol, such as a
	 *             TCP error.
	 */
	public void setSoTimeout(int aTimeout) throws SocketException {
		mServer.setSoTimeout(aTimeout);
	}

	/**
	 * Retrive setting for SO_TIMEOUT. 0 returns implies that the option is
	 * disabled (i.e., timeout of infinity).
	 * 
	 * @return the SO_TIMEOUT value
	 * @throws IOException
	 *             if an I/O error occurs
	 * @see #setSoTimeout(int)
	 */
	public int getSoTimeout() throws IOException {
		return mServer.getSoTimeout();
	}

	/**
	 * Enable/disable the SO_REUSEADDR socket option.
	 * 
	 * @param aOn
	 *            whether to enable or disable the socket option
	 * @throws SocketException
	 *             if an error occurs enabling or disabling the
	 *             <tt>SO_RESUEADDR</tt> socket option, or the socket is
	 *             closed.
	 */
	public void setReuseAddress(boolean aOn) throws SocketException {
		mServer.setReuseAddress(aOn);
	}

	/**
	 * Tests if SO_REUSEADDR is enabled.
	 * 
	 * @return a <code>boolean</code> indicating whether or not SO_REUSEADDR
	 *         is enabled.
	 * @throws SocketException
	 *             if there is an error in the underlying protocol, such as a
	 *             TCP error.
	 * @see #setReuseAddress(boolean)
	 */
	public boolean getReuseAddress() throws SocketException {
		return mServer.getReuseAddress();
	}

	/**
	 * Returns the implementation address and implementation port of this socket
	 * as a <code>String</code>.
	 * 
	 * @return a string representation of this socket.
	 */
	public String toString() {
		return mServer.toString();
	}

	/**
	 * Sets a default proposed value for the SO_RCVBUF option for sockets
	 * accepted from this <tt>ServerSocketWrapper</tt>.
	 * 
	 * @param aSize
	 *            the size to which to set the receive buffer size. This value
	 *            must be greater than 0.
	 * @throws SocketException
	 *             if there is an error in the underlying protocol, such as a
	 *             TCP error.
	 * @see #getReceiveBufferSize()
	 */
	public synchronized void setReceiveBufferSize(int aSize)
			throws SocketException {
		mServer.setReceiveBufferSize(aSize);
	}

	/**
	 * Gets the value of the SO_RCVBUF option for this
	 * <tt>ServerSocketWrapper</tt>, that is the proposed buffer size that
	 * will be used for Sockets accepted from this <tt>ServerSocketWrapper</tt>.
	 * 
	 * @return the value of the SO_RCVBUF option for this <tt>Socket</tt>.
	 * @throws SocketException
	 *             if there is an error in the underlying protocol, such as a
	 *             TCP error.
	 * @see #setReceiveBufferSize(int)
	 */
	public synchronized int getReceiveBufferSize() throws SocketException {
		return mServer.getReceiveBufferSize();
	}

}
