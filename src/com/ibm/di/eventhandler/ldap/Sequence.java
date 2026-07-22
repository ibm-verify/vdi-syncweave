/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.eventhandler.ldap;

import java.io.*;
import java.nio.*;

/**
 * This class implements reading and writing sequences. Instantiate with an
 * InputStream or ByteBuffer to populate the internal buffer with sequence data.
 * Instantiate with a tag and optionally initial buffer size to create a
 * sequence. The internal buffer automatically adjusts when adding data to it.
 * 
 * NOTE! Call getBuffer() only ONCE! The call to getBuffer() updates the entire
 * ByteBuffer with sequence tag and size followed by the sequence data.
 */

public class Sequence {
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public ByteBuffer buffer;

	public int tag;

	public Sequence(InputStream is, boolean gettype) throws Exception {

		// Sequence type
		if (gettype)
			tag = is.read();

		// Get sequence size
		int ch = is.read();
		if (ch > 0x7f) {
			int len = (ch & 0x7f);
			ch = 0;
			while (len-- > 0) {
				ch = ch << 8;
				int b = is.read();
				ch += b;
			}
		}

		// Read message into buf
		buffer = ByteBuffer.allocate(ch);
		while (ch-- > 0) {
			buffer.put((byte) is.read());
		}
		buffer.rewind();

	}

	public Sequence(ByteBuffer is, boolean gettype) throws Exception {

		// Sequence type
		if (gettype)
			tag = is.get() & 0xff;

		// Get sequence size
		int ch = is.get() & 0xff;
		if (ch > 0x7f) {
			int len = (ch & 0x7f);
			ch = 0;
			while (len-- > 0) {
				ch = ch << 8;
				int b = is.get() & 0xff;
				ch += b;
			}
		}

		// copy message, while trying to optimize. Number 16 found by
		// experiments
		buffer = ByteBuffer.allocate(ch);
		if (ch < 16) {
			while (ch-- > 0)
				buffer.put(is.get());
		} else {
			byte[] ba = new byte[ch];
			is.get(ba, 0, ch);
			buffer.put(ba);
		}
		buffer.rewind();

	}

	public Sequence(int tag) {
		this(tag, 1000);
	}

	public Sequence(int tag, int size) {
		this(tag, size, true);
	}

	public Sequence(int tag, int size, boolean addlen) {
		buffer = ByteBuffer.allocate(size);
		if (addlen) {
			buffer.put((byte) tag);
			buffer.put((byte) 0x0);
		}
	}

	public void addBuffer(ByteBuffer buf) {
		// Not needed
		// if (buf.position > 0)
		// buf.flip();

		if (buffer.remaining() < buf.limit()) {
			ByteBuffer tmp = ByteBuffer.allocate(buffer.capacity() * 2
					+ buf.limit());
			buffer.flip();
			tmp.put(buffer);
			buffer = tmp;
		}

		buffer.put(buf);
	}

	public void addBuffer(Sequence seq) {
		if (seq != null)
			addBuffer(seq.getBuffer());
	}

	public static ByteBuffer getBuffer(ByteBuffer buf) throws Exception {
		return getBuffer(buf, true);
	}

	public static ByteBuffer getBuffer(ByteBuffer buf, boolean readtag)
			throws Exception {
		return new Sequence(buf, readtag).buffer;
	}

	public ByteBuffer getBuffer() {
		int size = buffer.position() - 2;
		if (size < 0x80) {
			buffer.put(1, (byte) size);
			return flip();
		}

		// need to reallocate here ...
		ByteBuffer tmp = ByteBuffer.allocate(size + 30);

		// Copy tag
		tmp.put(buffer.get(0));

		// encode size
		BER.putSeqLen(tmp, size);

		// copy old buffer
		tmp.put(buffer.array(), 2, size);
		buffer = tmp;

		return flip();
	}

	public ByteBuffer flip() {
		return (ByteBuffer) buffer.flip();
	}
}
