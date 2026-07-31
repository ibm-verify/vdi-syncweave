/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.plugin.proxy;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.Vector;

import com.ibm.di.plugin.log.PWSyncLog;
import com.ibm.di.plugin.pwstore.PasswordChange;
import com.ibm.di.plugin.pwstore.BasePasswordChange;
import com.ibm.di.plugin.pwstore.PasswordStore;
import com.ibm.di.server.ResourceHash;

/**
 * This class is responsible for handling each client's request.
 */
public class ProxyCommandReceiver implements Runnable {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.plugin.CopyRight.OBJECT_CODE;

	/** This is the timeout of the socket */
	public static final int SOCKET_TIMEOUT = 5000;

	private static final String PREFIX = Proxy.PREFIX;

	// ---------------- commands -------------------
	@SuppressWarnings("unused")
	private static final int OPCODE_RESERVED = 0;

	private static final int OPCODE_READY_TO_SYNC = 1;

	private static final int OPCODE_SYNC_PASS = 2;

	private static final int OPCODE_ADD_PASS_VAL = 3;

	private static final int OPCODE_DEL_PASS_VAL = 4;

	private static final int OPCODE_DISCONNECT_VAL = 5;

	private static final int OPCODE_SET_EXT_DATA_VAL = 6;

	private static final int OPCODE_TERMINATE = 250;

	// ------- encoding constants ---------

	/**
	 * The name of the default charset that will be used if no BOM is found.
	 */
	public static final String DEFAULT_ENCODING = "UTF-8";

	private static final String UTF_8_STRING = "UTF-8";
	private static final byte[] UTF_8_SEQUENCE = new byte[] { 0x00, 0x00, (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };

	private static final String UTF_16LE_STRING = "UTF-16LE";
	private static final byte[] UTF_16LE_SEQUENCE = new byte[] { 0x00, 0x00, (byte) 0xFF, (byte) 0xFE };

	private static final String UTF_16BE_STRING = "UTF-16BE";
	private static final byte[] UTF_16BE_SEQUENCE = new byte[] { 0x00, 0x00, (byte) 0xFE, (byte) 0xFF };

	private static final String UTF_32LE_STRING = "UTF-32LE";
	private static final byte[] UTF_32LE_SEQUENCE = new byte[] { 0x00, 0x00, (byte) 0xFF, (byte) 0xFE, 0x00, 0x00 };

	private static final String UTF_32BE_STRING = "UTF-32BE";
	private static final byte[] UTF_32BE_SEQUENCE = new byte[] { 0x00, 0x00, 0x00, 0x00, (byte) 0xFE, (byte) 0xFF };
	// -----------------------------------

	private PasswordStore pwstore = null;

	private Socket socket = null;

	private String mDN = null;

	private PWSyncLog log = null;

	private Proxy proxy = null;

	private String encoding = null;

	private static final ResourceHash resHash = ResourceHash.getHash("proxy");

	/**
	 * Creates an instance of this class.
	 * 
	 * @param proxy
	 *            the Proxy that created this object.
	 * @param socket
	 *            the socket on which to communicate.
	 * @param synchronizer
	 *            the password store instance object.
	 * @param log
	 *            the log in which to write.
	 */
	public ProxyCommandReceiver(Proxy proxy, Socket socket, PasswordStore synchronizer, PWSyncLog log) {
		this.proxy = proxy;
		this.socket = socket;
		this.pwstore = synchronizer;
		this.log = log;
	}

	/**
	 * {@inheritDoc}
	 */
	public void run() {

		boolean acceptCommands = true;

		PushbackInputStream socketIn = null;
		OutputStream socketOut = null;
		try {

			socket.setSoTimeout(SOCKET_TIMEOUT);

			socketIn = new PushbackInputStream(socket.getInputStream(), 6);
			socketOut = socket.getOutputStream();

			// this will set the encoding variable
			readBOM(socketIn, socketOut);
		} catch (IOException e) {
			log.error(PREFIX, resHash.getString("PWSYNC.IO.EXCEPTION"), e);
			acceptCommands = false;
		}

		command: while (acceptCommands && !proxy.proxyStopRequested()) {
			int b = 0;
			try {
				// 2 zeros
				do {
					b = socketIn.read();
					if (b == -1)
						break command;

				} while (b != 0);
				b = readZero(socketIn);
			} catch (InterruptedIOException e) {
				// timeout elapsed, check for proxy stop request
				continue;
			} catch (IOException e) {
				log.error(PREFIX, resHash.getString("PWSYNC.IO.EXCEPTION"), e);
				break command;
			}

			try {
				int opcode = socketIn.read();
				// 2 zeros
				b = readZero(socketIn);
				b = readZero(socketIn);

				log.debug(PREFIX, resHash.getString("PWSYNC.RECEIVED.OPCODE", opcode));

				switch (opcode) {
				case OPCODE_READY_TO_SYNC:
					processReadyToSync(socketIn, socketOut);
					break;
				case OPCODE_SYNC_PASS:
					processModPass(socketIn, socketOut);
					break;
				case OPCODE_ADD_PASS_VAL:
					processAddPass(socketIn, socketOut);
					break;
				case OPCODE_DEL_PASS_VAL:
					processDelPass(socketIn, socketOut);
					break;
				case OPCODE_DISCONNECT_VAL:
					// clean thread close
					sendResponse(socketOut, true);
					acceptCommands = false;
					break;
				case OPCODE_TERMINATE:
					proxy.requestProxyStop();
					acceptCommands = false;
					sendResponse(socketOut, true);
					break;
				case OPCODE_SET_EXT_DATA_VAL:
					processSetExtendedData(socketIn, socketOut);
					break;
				default:
					log.warn(PREFIX, resHash.getString("PWSYNC.INVALID.OPCODE: ", opcode));
					handleInvalidOperation(socketIn, socketOut);
					break;
				}
			} catch (IOException e) {
				log.error(PREFIX, resHash.getString("PWSYNC.IO.EXCEPTION"), e);
				sendResponse(socketOut, false);
			} catch (Throwable t) {
				log.error(PREFIX, resHash.getString("PWSYNC.INTERNAL.ERROR.IN.COMMAND.LOOP"), t);
				sendResponse(socketOut, false);
			}
		}

		try {
			socket.close();
		} catch (IOException e) {
			log.error(PREFIX, resHash.getString("PWSYNC.IO.EXCEPTION"), e);
		} finally {
			socket = null;

			// unregister this instance from the running threads list.
			proxy.removeReceiver(this);
		}
	}

	private void sendResponse(OutputStream out, boolean successResponse) {

		if (!successResponse) {
			log.warn(PREFIX, resHash.getString("PWSYNC.REJECT.OPCODE"));
		}

		try {
			out.write(successResponse ? 1 : 0);
			out.flush();
		} catch (IOException ex) {
			log.error(PREFIX, resHash.getString("PWSYNC.IO.EXCEPTION"), ex);
		}

	}

	private void processReadyToSync(InputStream aIn, OutputStream aOut) throws IOException {
		PasswordChange change = readDNPass(aIn, PasswordChange.NO_CHANGE);

		if (!bufferIsEmpty(aIn, aOut) || change == null) {
			return;
		}
		boolean ready = pwstore.isAvailable(change);

		sendResponse(aOut, ready);
	}

	private void processModPass(InputStream aIn, OutputStream aOut) throws IOException {
		PasswordChange change = readDNPass(aIn, PasswordChange.MODIFY_CHANGE);

		if (!bufferIsEmpty(aIn, aOut) || change == null) {
			return;
		}
		boolean success = pwstore.store(change);

		sendResponse(aOut, success);
	}

	private void processAddPass(InputStream aIn, OutputStream aOut) throws IOException {
		PasswordChange change = readDNPass(aIn, PasswordChange.ADD_CHANGE);

		if (!bufferIsEmpty(aIn, aOut) || change == null) {
			return;
		}
		boolean success = pwstore.store(change);

		sendResponse(aOut, success);
	}

	private void processDelPass(InputStream aIn, OutputStream aOut) throws IOException {
		PasswordChange change = readDNPass(aIn, PasswordChange.DELETE_CHANGE);

		if (!bufferIsEmpty(aIn, aOut) || change == null) {
			return;
		}
		boolean success = pwstore.store(change);

		sendResponse(aOut, success);
	}

	private void processSetExtendedData(InputStream in, OutputStream out) throws IOException {

		//We skipped the first string from the stream.
		readString(in);
		String extendedData = readString(in);

		if (!bufferIsEmpty(in, out)) {
			return;
		}

		boolean success = pwstore.setExtendedData(new BasePasswordChange(PasswordChange.MODIFY_EXTENDED_DATA_CHANGE, mDN,
				extendedData));

		sendResponse(out, success);
	}

	private PasswordChange readDNPass(InputStream aIn, int type) throws IOException {
		mDN = null;

		if (aIn.available() == 0)
			return null;

		// number of passwords
		int mPassNum = aIn.read();

		// 2 zeros
		readZero(aIn);
		readZero(aIn);

		if (aIn.available() == 0)
			return null;

		mDN = readString(aIn);
		Vector<String> passwords = new Vector<String>(mPassNum);

		for (int i = 0; i < mPassNum; i++) {
			passwords.add(readString(aIn));
		}

//		log.debug(PREFIX, "L3: Read " + mPassNum + " password for " + mDN);
		return new BasePasswordChange(type, mDN, passwords, null, System.getProperty(Proxy.PROXY_CUSTOM_DATA));
	}

	/**
	 * Read an encoded string. The first two bytes represent the length of the
	 * encoded string in bytes using big-endian (most significant byte comes
	 * first). The bytes after that represent the encoded string.
	 * 
	 * @param in
	 *            the input stream to read from
	 * @return the decoded string read from the stream
	 * @throws IOException
	 *             if a read error occurs
	 */
	private String readString(InputStream in) throws IOException {

		// read length - encoded as two bytes in big-endian
		int b1 = readByte(in);
		int b2 = readByte(in);

		int byteCount = (b1 << 8) | b2;

		byte[] stringBytes = readBytes(in, byteCount);

		if (stringBytes.length > 0) {
			return new String(stringBytes, encoding);
		} else {
			return "";
		}
	}

	private boolean bufferIsEmpty(InputStream aIn, OutputStream aOut) throws IOException {
		if (aIn.available() == 0) {
			return true;
		}

		log.debug(PREFIX, resHash.getString("PWSYNC.REJECT.FOR.DN", mDN));

		int bytesNum;
		while ((bytesNum = aIn.available()) > 0) {
			aIn.skip(bytesNum);
		}

		sendResponse(aOut, false);

		return false;
	}

	private void handleInvalidOperation(InputStream aIn, OutputStream aOut) throws IOException {

		// flush the input stream
		int bytesNum;
		while ((bytesNum = aIn.available()) > 0) {
			bytesNum = (int) aIn.skip(bytesNum);
		}

		sendResponse(aOut, false);
	}

	private int readZero(InputStream aIn) throws IOException {
		int b = aIn.read();
		if (b != 0) {
			throw new IOException(resHash.getString("PWSYNC.ERROR.READ.ZERO", Integer.toString(b, 16)));
		}
		return b;
	}

	private void readBOM(PushbackInputStream is, OutputStream os) throws IOException {
		log.debug(PREFIX, resHash.getString("PWSYNC.READING.BOM"));

		int available = 0;

		while ((available = is.available()) == 0) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				return;
			}
		}

		byte[] bom = new byte[available];
		is.read(bom);

		byte[] seq = null;

		if (available == UTF_8_SEQUENCE.length) {
			seq = UTF_8_SEQUENCE;
			encoding = UTF_8_STRING;
		} else if (available == UTF_16LE_SEQUENCE.length) {
			seq = UTF_16LE_SEQUENCE;
			encoding = UTF_16LE_STRING;
		} else if (available == UTF_16BE_SEQUENCE.length) {
			seq = UTF_16BE_SEQUENCE;
			encoding = UTF_16BE_STRING;
		} else if (available == UTF_32LE_SEQUENCE.length) {
			seq = UTF_32LE_SEQUENCE;
			encoding = UTF_32LE_STRING;
		} else if (available == UTF_32BE_SEQUENCE.length) {
			seq = UTF_32BE_SEQUENCE;
			encoding = UTF_32BE_STRING;
		}

		if (seq == null || !Arrays.equals(bom, seq)) {
			encoding = DEFAULT_ENCODING;
			log.debug(PREFIX, resHash.getString("PWSYNC.BOM.NOT.FOUND", DEFAULT_ENCODING));
			// this means that we have read something that is not BOM (probably
			// opcode) so we should bring it back.
			is.unread(bom);
		} else {

			// Acknowledge the BOM sequence...
			log.debug(PREFIX, resHash.getString("PWSYNC.BOM.FOUND", encoding));
			log.debug(PREFIX, resHash.getString("PWSYNC.RESPONSE.POSITIVE"));
			os.write(1);
			os.flush();
		}
	}

	/**
	 * Read exactly one byte or throw if the stream is empty.
	 * 
	 * @param in
	 *            Input stream.
	 * @return The read byte. The value will always be between 0 and 255.
	 * @throws EOFException
	 *             If there is not enough available data in the stream.
	 * @throws IOException
	 *             I/O related error.
	 */
	int readByte(InputStream in) throws EOFException, IOException {

		int b = in.read();
		if (b == -1) {
			throw new EOFException(resHash.getString("PWSYNC.INSUFFICIENT.INPUT.DATA"));
		}

		return b;
	}

	/**
	 * Read exactly the specified number of bytes or throw.
	 * 
	 * @param in
	 *            Input stream.
	 * @param byteCount
	 *            Number of bytes to read.
	 * @return The read bytes. Will never be null.
	 * @throws EOFException
	 *             If there is not enough available data in the stream.
	 * @throws IOException
	 *             I/O related error.
	 */
	byte[] readBytes(InputStream in, int byteCount) throws EOFException, IOException {

		byte[] bytes = new byte[byteCount];

		int readByteCount = in.read(bytes);
		if (readByteCount < byteCount) {
			throw new EOFException(resHash.getString("PWSYNC.INSUFFICIENT.INPUT.DATA"));
		}

		return bytes;
	}
}
