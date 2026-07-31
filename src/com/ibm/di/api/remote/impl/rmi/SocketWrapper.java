/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.remote.impl.rmi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;

/**
 * 
 * A wrapper class of Socket class for use by the RMI. Socket class implements
 * client sockets. A socket is an endpoint for communication between two
 * machines.
 * 
 */
public class SocketWrapper extends Socket {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * <code>Socket</code> object
	 */
	private Socket mSocket = null;

	/**
	 * Creates SocketWrapper by given <code>Socket</code>.
	 * 
	 * @param aSocket
	 *            <code>Socket</code> object
	 */
	public SocketWrapper(Socket aSocket) {
		super();
		mSocket = aSocket;
	}

	/**
	 * Connects this socket to the server.
	 * 
	 * @param aEndpoint
	 *            the <code>SocketAddress</code>
	 * @throws IOException
	 *             if an error occurs during the connection
	 */
	public void connect(SocketAddress aEndpoint) throws IOException {
		mSocket.connect(aEndpoint);
	}

	/**
	 * Connects this socket to the server with a specified timeout value. A
	 * timeout of zero is interpreted as an infinite timeout. The connection
	 * will then block until established or an error occurs.
	 * 
	 * @param aEndpoint
	 *            the <code>SocketAddress</code>
	 * @param aTimeout
	 *            the timeout value to be used in milliseconds.
	 * @throws IOException
	 *             if an error occurs during the connection
	 */
	public void connect(SocketAddress aEndpoint, int aTimeout)
			throws IOException {
		mSocket.connect(aEndpoint, aTimeout);
	}

	/**
	 * Binds the socket to a local address.
	 * <p>
	 * If the address is null, then the system will pick up an ephemeral port
	 * and a valid local address to bind the socket.
	 * 
	 * @param aBindpoint
	 *            the <code>SocketAddress</code> to bind to
	 * @throws IOException
	 */
	public void bind(SocketAddress aBindpoint) throws IOException {
		mSocket.bind(aBindpoint);
	}

	/**
	 * Returns the address to which the socket is connected.
	 * 
	 * @return the remote IP address to which this socket is connected, or
	 *         <code>null</code> if the socket is not connected.
	 */
	public InetAddress getInetAddress() {
		return mSocket.getInetAddress();
	}

	/**
	 * Gets the local address to which the socket is bound.
	 * 
	 * @return the local address to which the socket is bound or
	 *         <code>InetAddress.anyLocalAddress()</code> if the socket is not
	 *         bound yet
	 */
	public InetAddress getLocalAddress() {
		return mSocket.getLocalAddress();
	}

	/**
	 * Returns the remote port to which this socket is connected.
	 * 
	 * @return the remote port number to which this socket is connected, or 0 if
	 *         the socket is not connected yet
	 */
	public int getPort() {
		return mSocket.getPort();
	}

	/**
	 * Returns the local port to which this socket is bound.
	 * 
	 * @return the local port number to which this socket is bound or -1 if the
	 *         socket is not bound yet
	 */
	public int getLocalPort() {
		return mSocket.getLocalPort();
	}

	/**
	 * Returns the address of the endpoint this socket is connected to, or null
	 * if it is unconnected.
	 * 
	 * @return a <code>SocketAddress</code> reprensenting the remote endpoint
	 *         of this socket, or null if it is not connected yet
	 * @see #getInetAddress()
	 * @see #connect(SocketAddress)
	 */
	public SocketAddress getRemoteSocketAddress() {
		return mSocket.getRemoteSocketAddress();
	}

	/**
	 * Returns the address of the endpoint this socket is bound to, or null if
	 * it is not bound yet
	 * 
	 * @return a <code>SocketAddress</code> representing the local endpoint of
	 *         this socket, or null if it is not bound yet
	 * @see #getLocalAddress()
	 * @see #bind(SocketAddress)
	 */
	public SocketAddress getLocalSocketAddress() {
		return mSocket.getLocalSocketAddress();
	}

	/**
	 * Returns the unique SocketChannel object associated with this socket, if
	 * any.
	 * 
	 * @return the socket channel associated with this socket, or
	 *         <code>null</code> if this socket was not created for a channel
	 */
	public SocketChannel getChannel() {
		return mSocket.getChannel();
	}

	/**
	 * Returns an <code>InputStreamWrapper</code> for this socket.
	 * 
	 * @return an <code>InputStreamWrapper</code> for reading bytes from this
	 *         socket
	 * @throws IOException
	 *             if an I/O error occurs when creating the input stream, the
	 *             socket is closed, the socket is not connected, or the socket
	 *             input has been shutdown using {@link #shutdownInput()}
	 */
	public InputStream getInputStream() throws IOException {
		InputStream stream = mSocket.getInputStream();
		return new InputStreamWrapper(stream, mSocket);
	}

	/**
	 * Returns an output stream for this socket.
	 * 
	 * @return an output stream for writing bytes to this socket
	 * @throws IOException
	 *             if an I/O error occurs when creating the output stream or if
	 *             the socket is not connected
	 */
	public OutputStream getOutputStream() throws IOException {
		return mSocket.getOutputStream();
	}

	/**
	 * Enable/disable TCP_NODELAY (disable/enable Nagle's algorithm).
	 * 
	 * @param aOn
	 *            <code>true</code> to enable TCP_NODELAY, <code>false</code>
	 *            to disable.
	 * @throws SocketException
	 *             if there is an error in the underlying protocol, such as a
	 *             TCP error.
	 */
	public void setTcpNoDelay(boolean aOn) throws SocketException {
		mSocket.setTcpNoDelay(aOn);
	}

	/**
	 * Tests if TCP_NODELAY is enabled.
	 * 
	 * @return a <code>boolean</code> indicating whether or not TCP_NODELAY is
	 *         enabled.
	 * @throws SocketException
	 *             if there is an error in the underlying protocol, such as a
	 *             TCP error.
	 */
	public boolean getTcpNoDelay() throws SocketException {
		return mSocket.getTcpNoDelay();
	}

	/**
	 * Enable/disable SO_LINGER with the specified linger time in seconds. The
	 * maximum timeout value is platform specific. The setting only affects
	 * socket close.
	 * 
	 * @param aOn
	 *            whether or not to linger on
	 * @param aLinger
	 *            how long to linger for, if on is true
	 * @throws SocketException
	 *             if there is an error in the underlying protocol, such as a
	 *             TCP error
	 */
	public void setSoLinger(boolean aOn, int aLinger) throws SocketException {
		mSocket.setSoLinger(aOn, aLinger);
	}

	/**
	 * Returns setting for SO_LINGER. -1 returns implies that the option is
	 * disabled. The setting only affects socket close.
	 * 
	 * @return the setting for SO_LINGER.
	 * @throws SocketException
	 *             if there is an error in the underlying protocol, such as a
	 *             TCP error
	 */
	public int getSoLinger() throws SocketException {
		return mSocket.getSoLinger();
	}

	/**
	 * Send one byte of urgent data on the socket. The byte to be sent is the
	 * lowest eight bits of the data parameter. The urgent byte is sent after
	 * any preceding writes to the socket OutputStream and before any future
	 * writes to the OutputStream.
	 * 
	 * @param aData
	 *            The byte of data to send
	 * @throws IOException
	 *             if there is an error sending the data.
	 */
	public void sendUrgentData(int aData) throws IOException {
		mSocket.sendUrgentData(aData);
	}

	/**
	 * Enable/disable OOBINLINE (receipt of TCP urgent data) By default, this
	 * option is disabled and TCP urgent data received on a socket is silently
	 * discarded.
	 * 
	 * @param aOn
	 *            <code>true</code> to enable OOBINLINE, false to disable.
	 * @throws SocketException
	 *             if there is an error in the underlying protocol, such as a
	 *             TCP error.
	 */
	public void setOOBInline(boolean aOn) throws SocketException {
		mSocket.setOOBInline(aOn);
	}

	/**
	 * Tests if OOBINLINE is enabled.
	 * 
	 * @return a <code>boolean</code> indicating whether or not OOBINLINE is
	 *         enabled.
	 * @throws SocketException
	 *             if there is an error in the underlying protocol, such as a
	 *             TCP error.
	 */
	public boolean getOOBInline() throws SocketException {
		return mSocket.getOOBInline();
	}

	/**
	 * Enable/disable SO_TIMEOUT with the specified timeout, in milliseconds.
	 * 
	 * @param aTimeout
	 *            the specified timeout, in milliseconds.
	 * @throws SocketException
	 *             if there is an error in the underlying protocol, such as a
	 *             TCP error.
	 * @see #getSoTimeout()
	 */
	public void setSoTimeout(int aTimeout) throws SocketException {
		mSocket.setSoTimeout(aTimeout);
	}

	/**
	 * Returns setting for SO_TIMEOUT. 0 returns implies that the option is
	 * disabled (i.e., timeout of infinity).
	 * 
	 * @return the setting for SO_TIMEOUT
	 * @throws SocketException
	 *             if there is an error in the underlying protocol, such as a
	 *             TCP error.
	 * @see #setSoTimeout(int)
	 */
	public int getSoTimeout() throws SocketException {
		return mSocket.getSoTimeout();
	}

	/**
	 * Sets the SO_SNDBUF option to the specified value for this Socket.
	 * 
	 * @param aSize
	 *            the size to which to set the send buffer size. This value must
	 *            be greater than 0.
	 * @throws SocketException
	 *             if there is an error in the underlying protocol, such as a
	 *             TCP error.
	 * @see #getSendBufferSize()
	 */
	public void setSendBufferSize(int aSize) throws SocketException {
		mSocket.setSendBufferSize(aSize);
	}

	/**
	 * Get value of the SO_SNDBUF option for this Socket, that is the buffer
	 * size used by the platform for output on this Socket.
	 * 
	 * @return the value of the SO_SNDBUF option for this Socket.
	 * @throws SocketException
	 *             if there is an error in the underlying protocol, such as a
	 *             TCP error.
	 * @see #setSendBufferSize(int)
	 */
	public int getSendBufferSize() throws SocketException {
		return mSocket.getSendBufferSize();
	}

	/**
	 * Sets the SO_RCVBUF option to the specified value for this Socket.
	 * 
	 * @param aSize
	 *            the size to which to set the receive buffer size. This value
	 *            must be greater than 0.
	 * @throws SocketException
	 *             if there is an error in the underlying protocol, such as a
	 *             TCP error.
	 * @see #getReceiveBufferSize()
	 */
	public void setReceiveBufferSize(int aSize) throws SocketException {
		mSocket.setReceiveBufferSize(aSize);
	}

	/**
	 * Gets the value of the SO_RCVBUF option for this Socket, that is the
	 * buffer size used by the platform for input on this Socket.
	 * 
	 * @return the value of the SO_RCVBUF option for this Socket.
	 * @throws SocketException
	 *             if there is an error in the underlying protocol, such as a
	 *             TCP error.
	 * @see #setReceiveBufferSize(int)
	 */
	public int getReceiveBufferSize() throws SocketException {
		return mSocket.getReceiveBufferSize();
	}

	/**
	 * Enable/disable SO_KEEPALIVE.
	 * 
	 * @param aOn
	 *            whether or not to have socket keep alive turned on.
	 * @throws SocketException
	 *             if there is an error in the underlying protocol, such as a
	 *             TCP error.
	 * @see #getKeepAlive()
	 */
	public void setKeepAlive(boolean aOn) throws SocketException {
		mSocket.setKeepAlive(aOn);
	}

	/**
	 * Tests if SO_KEEPALIVE is enabled.
	 * 
	 * @return a <code>boolean</code> indicating whether or not SO_KEEPALIVE
	 *         is enabled.
	 * @throws SocketException
	 *             if there is an error in the underlying protocol, such as a
	 *             TCP error.
	 * @see #setKeepAlive(boolean)
	 */
	public boolean getKeepAlive() throws SocketException {
		return mSocket.getKeepAlive();
	}

	/**
	 * Sets traffic class or type-of-service octet in the IP header for packets
	 * sent from this Socket.
	 * 
	 * @param aTc
	 *            an <code>int</code> value for the bitset.
	 * @throws SocketException
	 *             if there is an error setting the traffic class or
	 *             type-of-service
	 * @see #getTrafficClass()
	 */
	public void setTrafficClass(int aTc) throws SocketException {
		mSocket.setTrafficClass(aTc);
	}

	/**
	 * Gets traffic class or type-of-service in the IP header for packets sent
	 * from this Socket
	 * 
	 * @return the traffic class or type-of-service already set
	 * @throws SocketException
	 *             if there is an error obtaining the traffic class or
	 *             type-of-service value.
	 * @see #setTrafficClass(int)
	 */
	public int getTrafficClass() throws SocketException {
		return mSocket.getTrafficClass();
	}

	/**
	 * Enable/disable the SO_REUSEADDR socket option.
	 * 
	 * @param aOn
	 *            whether to enable or disable the socket option
	 * @throws SocketException
	 *             if an error occurs enabling or disabling the SO_RESUEADDR
	 *             socket option, or the socket is closed.
	 * @see #getReuseAddress()
	 */
	public void setReuseAddress(boolean aOn) throws SocketException {
		mSocket.setReuseAddress(aOn);
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
		return mSocket.getReuseAddress();
	}

	/**
	 * Closes this socket.
	 * 
	 * @throws IOException
	 *             if an I/O error occurs when closing this socket.
	 */
	public void close() throws IOException {
		mSocket.close();
	}

	/**
	 * Places the input stream for this socket at "end of stream". Any data sent
	 * to the input stream side of the socket is acknowledged and then silently
	 * discarded.
	 * 
	 * @throws IOException
	 *             if an I/O error occurs when shutting down this socket.
	 */
	public void shutdownInput() throws IOException {
		mSocket.shutdownInput();
	}

	/**
	 * Disables the output stream for this socket. For a TCP socket, any
	 * previously written data will be sent followed by TCP's normal connection
	 * termination sequence. If you write to a socket output stream after
	 * invoking shutdownOutput() on the socket, the stream will throw an
	 * IOException.
	 * 
	 * @throws IOException
	 *             if an I/O error occurs when shutting down this socket.
	 */
	public void shutdownOutput() throws IOException {
		mSocket.shutdownOutput();
	}

	/**
	 * Converts this socket to a String.
	 * 
	 * @return string representation of this socket.
	 */
	public String toString() {
		return mSocket.toString();
	}

	/**
	 * Returns the connection state of the socket.
	 * 
	 * @return <code>true</code> if the socket successfuly connected to a
	 *         server
	 */
	public boolean isConnected() {
		return mSocket.isConnected();
	}

	/**
	 * Returns the binding state of the socket.
	 * 
	 * @return <code>true</code> if the socket successfuly bound to an address
	 */
	public boolean isBound() {
		return mSocket.isBound();
	}

	/**
	 * Returns the closed state of the socket.
	 * 
	 * @return <code>true</code> if the socket has been closed
	 * @see #close()
	 */
	public boolean isClosed() {
		return mSocket.isClosed();
	}

	/**
	 * Returns whether the read-half of the socket connection is closed.
	 * 
	 * @return <code>true</code> if the input of the socket has been shutdown
	 * @see #shutdownInput()
	 */
	public boolean isInputShutdown() {
		return mSocket.isInputShutdown();
	}

	/**
	 * Returns whether the write-half of the socket connection is closed.
	 * 
	 * @return <code>true</code> if the output of the socket has been shutdown
	 */
	public boolean isOutputShutdown() {
		return mSocket.isOutputShutdown();
	}

}
