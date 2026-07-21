/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.eventhandler.ldap;

import java.nio.ByteBuffer;

import com.ibm.di.connector.LDAPServerConnector;
import com.ibm.di.server.Log;
import com.ibm.di.server.ResourceHash;

public class BER {
	protected static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public final static byte SEQUENCE = (byte) 0x30;

	public final static byte BOOLEAN = (byte) 0x01;

	public final static byte INTEGER = (byte) 0x02;

	public final static byte OCTET_STRING = (byte) 0x04;

	public final static byte ENUMERATED = (byte) 0x0a;

	public final static byte SET_OF = (byte) 0x31;

	public final static byte DN = (byte) 0x80;

	private final static ResourceHash sResHash = LDAPServerConnector
			.getResHash();

	public static boolean getBoolean(ByteBuffer buffer) throws Exception {
		verify(buffer.get(), BOOLEAN);
		if (buffer.get() != 1) {
			throw new Exception(
					sResHash
							.getString("CONNECTOR.LDAPSERVER.BER.BOOLEAN.WITH.MORE.THAN.ONE.BYTE"));
		}

		return (buffer.get() != 0);
	}

	public static String getFilterString(ByteBuffer buffer) throws Exception {
		return getFilterString(buffer, "UTF-8");
	}

	public static String getFilterString(ByteBuffer buffer, String charset)
			throws Exception {
		Sequence seq = new Sequence(buffer, true);
		StringBuffer str = new StringBuffer();
		str.append(getString(seq.buffer, -1, charset));

		switch (seq.tag) {
		case 0x80:
			str.append("*");
			break;
		case 0x81:
			str.insert(0, "*");
			str.append("*");
			break;
		case 0x82:
			str.insert(0, "*");
			break;
		}
		return str.toString();
	}

	public static String getDN(ByteBuffer buffer) throws Exception {
		verify(buffer.get(), DN);
		return getString(buffer, false);
	}

	public static String getString(ByteBuffer buffer, String charset)
			throws Exception {
		verify(buffer.get(), OCTET_STRING);
		int len = getSeqLen(buffer);
		return getString(buffer, len, charset);
	}

	public static String getString(ByteBuffer buffer) throws Exception {
		verify(buffer.get(), OCTET_STRING);
		int len = getSeqLen(buffer);
		return getString(buffer, len, "UTF-8");
	}

	public static String getString(ByteBuffer buffer, boolean verify)
			throws Exception {
		if (verify)
			verify(buffer.get(), OCTET_STRING);
		int len = getSeqLen(buffer);
		return getString(buffer, len, "UTF-8");
	}

	public static String getString(ByteBuffer buffer, int len) throws Exception {
		return getString(buffer, len, "UTF-8");
	}

	public static String getString(ByteBuffer buffer, int len, String charset)
			throws Exception {
		if (len == -1)
			len = buffer.remaining();

		byte[] ba = new byte[len];
		buffer.get(ba, 0, len);

		return new String(ba, charset);
	}

	public static int getInteger(ByteBuffer buffer) throws Exception {
		verify(buffer.get(), INTEGER);
		int value = 0;
		int len = getSeqLen(buffer);
		for (int i = 0; i < len; i++) {
			if (i > 0)
				value = value << 8;
			value += buffer.get() & 0xff;
		}
		return value;
	}

	public static int getEnum(ByteBuffer buffer) throws Exception {
		verify(buffer.get(), ENUMERATED);
		int value = 0;
		int len = getSeqLen(buffer);
		for (int i = 0; i < len; i++) {
			if (i > 0)
				value = value << 8;
			value += buffer.get() & 0xff;
		}
		return value;
	}

	public static byte[] getBytes(ByteBuffer buffer) throws Exception {
		return getBytes(buffer, true);
	}

	public static byte[] getBytes(ByteBuffer buffer, boolean verify)
			throws Exception {
		if (verify)
			verify(buffer.get(), OCTET_STRING);
		int len = getSeqLen(buffer);
		byte[] buf = new byte[len];
		buffer.get(buf, 0, len);
		return buf;
	}

	public static void putBoolean(ByteBuffer buffer, boolean value)
			throws Exception {
		buffer.put(BOOLEAN);
		buffer.put((byte) 1);
		if (value)
			buffer.put((byte) 1);
		else
			buffer.put((byte) 0);
	}

	public static void putEnum(ByteBuffer buffer, int value) {
		appendBuf(buffer, ENUMERATED, putRawInt(value));
	}

	public static ByteBuffer putBytes(ByteBuffer buffer, byte[] value) {
		return appendBuf(buffer, OCTET_STRING, value);
	}

	public static ByteBuffer putString(ByteBuffer buffer, String str) {
		return appendBuf(buffer, OCTET_STRING, str.getBytes());
	}

	public static ByteBuffer putString(ByteBuffer buffer, String value,
			String charsetName) throws Exception {
		return appendBuf(buffer, OCTET_STRING, value.getBytes(charsetName));
	}

	public static ByteBuffer putInteger(ByteBuffer buffer, int value) {
		return appendBuf(buffer, INTEGER, putRawInt(value));
	}

	public static ByteBuffer appendBuf(ByteBuffer buf, byte tag, byte[] value) {
		ByteBuffer tmp = buf;
		if (tmp.remaining() < value.length + 6)
			tmp = realloc(buf, value.length + tmp.limit());

		tmp.put(tag);
		putSeqLen(tmp, value.length);
		tmp.put(value);
		return tmp;
	}

	public static ByteBuffer realloc(ByteBuffer buf, int size) {
		ByteBuffer tmp = ByteBuffer.allocate(buf.flip().limit() + size);
		tmp.put(buf);
		return tmp;
	}

	public static byte[] putRawInt(int value) {
		byte[] b;
		if (value <= 0x7f) {
			b = new byte[1];
			b[0] = (byte) value;
		} else if (value < 0x7fff) {
			b = new byte[2];
			b[0] = (byte) (value >> 8);
			b[1] = (byte) (value & 0xff);
		} else if (value < 0x7fffff) {
			b = new byte[3];
			b[0] = (byte) (value >> 16);
			b[1] = (byte) ((value >> 8) & 0xff);
			b[2] = (byte) (value & 0xff);
		} else {
			b = new byte[4];
			b[0] = (byte) (value >> 24);
			b[1] = (byte) ((value >> 16) & 0xff);
			b[2] = (byte) ((value >> 8) & 0xff);
			b[3] = (byte) (value & 0xff);
		}
		return b;
	}

	public static void putSeqLen(ByteBuffer buffer, int value) {
		if (value <= 0x7f) {
			buffer.put((byte) value);
		} else if (value < 0x7fff) {
			buffer.put((byte) 0x82);
			buffer.put((byte) (value >> 8));
			buffer.put((byte) (value & 0xff));
		} else if (value < 0x7fffff) {
			buffer.put((byte) 0x83);
			buffer.put((byte) (value >> 16));
			buffer.put((byte) ((value >> 8) & 0xff));
			buffer.put((byte) (value & 0xff));
		} else {
			buffer.put((byte) 0x84);
			buffer.put((byte) (value >> 24));
			buffer.put((byte) ((value >> 16) & 0xff));
			buffer.put((byte) ((value >> 8) & 0xff));
			buffer.put((byte) (value & 0xff));
		}
	}

	public static void verify(byte a, byte b) throws Exception {
		if (a != b) {
			throw new Exception(sResHash.getString(
					"CONNECTOR.LDAPSERVER.BER.EXPECTED.EXCEPTION",
					new Object[] { hex(b & 0xff, 2), hex(a & 0xff, 2) }));
		}
	}

	public static void dump(ByteBuffer buf, Log log) {
		
		StringBuilder str = new StringBuilder();
		StringBuilder strhex = new StringBuilder();
//		String strhex = "";
		int ch;

		log.info(sResHash
				.getString("CONNECTOR.LDAPSERVER.BER.BEGIN.PACKET.DUMP"));
		for (int i = 0; i < buf.limit(); i++) {
			ch = buf.get(i) & 0xff;
			if ((i % 16) == 0) {
				if (str.length() > 0)
					log.info(sResHash.getString(
							"CONNECTOR.LDAPSERVER.BER.HEX.INFO", strhex)
							+ "   "
							+ sResHash.getString(
									"CONNECTOR.LDAPSERVER.BER.DECIMAL.INFO",
									str));

				strhex.append((char)' ');
				strhex.append((char)' ');
				strhex.append((char)' ');
				strhex.append((char)' ');
				strhex.append(hex(i, 4));
				strhex.append((char)':');
				
				str.delete(0, str.length());
			}

			strhex.append((char)' ');
			strhex.append(hex(ch, 2));
			
			if (ch > 32 && ch <= 126)
				str.append((char) ch);
			else
				str.append((char)'.');
		}
		if (str.length() > 0) {
			int count = 16 - (buf.limit() % 16);
			while (count-- > 0) {
				strhex.append((char)' ');
				strhex.append((char)' ');
				strhex.append((char)' ');
			}
			log
					.info(sResHash.getString(
							"CONNECTOR.LDAPSERVER.BER.HEX.AFTERSOMEWORK.INFO",
							strhex)
							+ "   "
							+ sResHash
									.getString(
											"CONNECTOR.LDAPSERVER.BER.DECIMAL.AFTERSOMEWORK.INFO",
											str));
		}
		log
				.info(sResHash
						.getString("CONNECTOR.LDAPSERVER.BER.END.PACKET.DUMP"));
	}

	public static String hex(int ch, int len) {
		String s = Integer.toHexString(ch);
		while (s.length() < len)
			s = "0" + s;

		while (s.length() > len)
			s = s.substring(1);

		return s;
	}

	public static int getSeqLen(ByteBuffer buffer) {
		// Get sequence size
		int ch = buffer.get() & 0xff;
		if (ch > 0x7f) {
			int len = (ch & 0x7f);
			ch = 0;
			while (len-- > 0) {
				ch = ch << 8;
				int b = buffer.get() & 0xff;
				ch += b;
			}
		}

		return ch;
	}

}
