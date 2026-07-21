/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.remote.impl.rmi;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import javax.net.ssl.SSLSocket;

/**
 * 
 * A wrapper class of <code>InputStream</code> class for use by the RMI.
 * 
 */
public class InputStreamWrapper extends InputStream {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * InputStream object
	 */
	private InputStream mIstream = null;

	/**
	 * desired socket to use
	 */
	private Socket mSocket = null;

	/**
	 * Constructs InputStreamWrapper by given InputStream object and Socket or
	 * SSLSocket object.
	 * 
	 * @param aIstream
	 *            InputStream object
	 * @param aSocket
	 *            desired socket to use
	 */
	public InputStreamWrapper(InputStream aIstream, Socket aSocket) {
		mIstream = aIstream;
		mSocket = aSocket;
	}

	/**
	 * Reads the next byte of data from the input stream.
	 * 
	 * @return the next byte of data, or -1 if the end of the stream is reached.
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public int read() throws IOException {
		if (mSocket instanceof SSLSocket) {
			SSLRMIServerSocketFactory.setLocalThreadSocket(mSocket);
		} else {
			RMISocketFactory.setLocalThreadSocket(mSocket);
		}
		return mIstream.read();
	}

	/**
	 * Reads some number of bytes from the input stream and stores them into the
	 * buffer array <code>aBarray</code>.
	 * 
	 * @param aBarray
	 *            the buffer into which the data is read.
	 * @return the total number of bytes read into the buffer, or -1 is there is
	 *         no more data because the end of the stream has been reached.
	 * @throws IOException
	 *             if an I/O error occurs. NullPointerException if
	 *             <code>aBarray</code> is null
	 */
	public int read(byte[] aBarray) throws IOException {
		if (mSocket instanceof SSLSocket) {
			SSLRMIServerSocketFactory.setLocalThreadSocket(mSocket);
		} else {
			RMISocketFactory.setLocalThreadSocket(mSocket);
		}
		return mIstream.read(aBarray);
	}

	/**
	 * Reads up to <code>aLen</code> bytes of data from the input stream into
	 * an array of bytes.
	 * 
	 * @param aBarray
	 *            the buffer into which the data is read.
	 * @param aOff
	 *            the start offset in array <code>aBarray</code> at which the
	 *            data is written.
	 * @param aLen
	 *            the maximum number of bytes to read.
	 * @return the total number of bytes read into the buffer, or -1 if there is
	 *         no more data because the end of the stream has been reached.
	 * 
	 * @throws IOException
	 *             if an I/O error occurs.
	 * @throws NullPointerException
	 *             if <code>aBarray</code> is null.
	 */
	public int read(byte[] aBarray, int aOff, int aLen) throws IOException {
		if (mSocket instanceof SSLSocket) {
			SSLRMIServerSocketFactory.setLocalThreadSocket(mSocket);
		} else {
			RMISocketFactory.setLocalThreadSocket(mSocket);
		}
		return mIstream.read(aBarray, aOff, aLen);
	}

	/**
	 * Skips over and discards n bytes of data from this stream.
	 * 
	 * @param aN
	 *            the number of bytes to be skipped. Returns: the actual number
	 *            of bytes skipped. Throws: IOException if an I/O error occurs.
	 * @return the actual number of bytes skipped.
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public long skip(long aN) throws IOException {
		return mIstream.skip(aN);
	}

	/**
	 * Returns the number of bytes that can be read (or skipped over) from this
	 * input stream without blocking by the next caller of a method for this
	 * input stream. The next caller might be the same thread or another thread.
	 * 
	 * @return the number of bytes that can be read from this input stream
	 *         without blocking.
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public int available() throws IOException {
		return mIstream.available();
	}

	/**
	 * Closes this input stream and releases any system resources associated
	 * with the stream.
	 * 
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public void close() throws IOException {
		mIstream.close();
	}

	/**
	 * Marks the current position in this stream. A subsequent call to the reset
	 * method repositions this stream at the last marked position so that
	 * subsequent reads re-read the same bytes.
	 * 
	 * @param aReadLimit
	 *            the maximum limit of bytes that can be read before the mark
	 *            position becomes invalid
	 * @see #reset()
	 */
	public void mark(int aReadLimit) {
		mIstream.mark(aReadLimit);
	}

	/**
	 * Repositions this stream to the position at the time the mark method was
	 * last called on this input stream
	 * 
	 * @throws IOException
	 *             if this stream has not been marked or if the mark has been
	 *             invalidated
	 * @see #mark(int)
	 */
	public void reset() throws IOException {
		mIstream.reset();
	}

	/**
	 * Tests if this stream supports the <code>mark</code> and
	 * <code>reset</code> methods. Whether or not mark and reset are supported
	 * is an invariant property of the this stream instance.
	 * 
	 * @return <code>true</code> if this stream instance supports the mark and
	 *         reset methods; <code>false</code> otherwise.
	 */
	public boolean markSupported() {
		return mIstream.markSupported();
	}
}
