/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.eventhandler.ldap;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Vector;

import com.ibm.di.connector.LDAPServerConnector;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.Log;
import com.ibm.di.server.ResourceHash;

/*
 */
public class LDAPMessage extends Sequence {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * LDAP Protocol OP Codes
	 */
	public final static int APPLICATION = 0x60; // For simple values, the base
	// is 0x40 rather than 0x60

	public final static int BIND_REQUEST = APPLICATION + 0;

	public final static int BIND_RESPONSE = APPLICATION + 1;

	public final static int UNBIND_REQUEST = 0x42; // APPLICATION + 2 (simple
	// value)

	public final static int SEARCH_REQUEST = APPLICATION + 3;

	public final static int SEARCH_RESULT_ENTRY = APPLICATION + 4;

	public final static int SEARCH_RESULT_DONE = APPLICATION + 5;

	public final static int MODIFY_REQUEST = APPLICATION + 6;

	public final static int MODIFY_RESPONSE = APPLICATION + 7;

	public final static int ADD_REQUEST = APPLICATION + 8;

	public final static int ADD_RESPONSE = APPLICATION + 9;

	public final static int DELETE_REQUEST = 0x4a; // APPLICATION + 10 (simple
	// value)

	public final static int DELETE_RESPONSE = APPLICATION + 11;

	public final static int MODIFYDN_REQUEST = APPLICATION + 12;

	public final static int MODIFYDN_RESPONSE = APPLICATION + 13;

	public final static int COMPARE_REQUEST = APPLICATION + 14;

	public final static int COMPARE_RESPONSE = APPLICATION + 15;

	public final static int ABANDON_REQUEST = 0x50;// APPLICATION + 16 (simple
	// value)

	public final static int SEARCH_RESULT_REFERENCE = APPLICATION + 19;

	public final static int EXTENDED_REQUEST = APPLICATION + 23;

	public final static int EXTENDED_RESPONSE = APPLICATION + 24;

	private ByteBuffer buf;

	private int messageID;

	private Request request;

	private Sequence response;

	private ByteBuffer packet;

	private Entry entry;

	private Sequence controls;

	private final static ResourceHash sResHash = LDAPServerConnector
			.getResHash();

	public LDAPMessage() {
		this(0);
	}

	public LDAPMessage(int messageID) {
		super(BER.SEQUENCE, 8000);
		this.messageID = messageID;
	}

	/**
	 * Returns the BER encoded LDAP message as a ByteBuffer
	 */
	public ByteBuffer getBuffer() {

		if (packet == null) {

			// Message ID
			BER.putInteger(buffer, messageID);

			// Response
			addBuffer(response);

			// Controls
			if (controls != null)
				addBuffer(controls);

			packet = super.getBuffer();
		}

		return packet;
	}

	/**
	 * This method is called to parse an input message. On success the
	 * getRequest() and getMessageID() methods can be called to retrieve the
	 * LDAP request.
	 */
	public boolean parse(InputStream is) throws Exception {
		String charset = "UTF-8";

		return parse(is, charset, null);
	}

	/**
	 * This method is called to parse an input message. On success the
	 * getRequest() and getMessageID() methods can be called to retrieve the
	 * LDAP request.
	 */
	public boolean parse(InputStream is, String charset, Vector binattrs)
			throws Exception {

		// Verify sequence begin
		int ch;

		try {
			ch = is.read();
		} catch (IOException ioerror) {
			return false;
		}

		if (ch == -1) {
			return false;
		}

		if (ch != BER.SEQUENCE) {
			throw new Exception(
					sResHash
							.getString(
									"CONNECTOR.LDAPSERVER.LDAPMESSAGE.NOT.A.SEQUENCE.EXCEPTION",
									Integer.valueOf(ch)));
		}

		entry = new Entry();

		// Get sequence data
		Sequence seq = new Sequence(is, false);
		buf = seq.buffer;

		// Get message ID
		messageID = BER.getInteger(buf);

		// Get protocol byte
		Sequence rseq = new Sequence(buf, true);

		// build message object
		switch (rseq.tag) {
		case BIND_REQUEST:
			request = new BindRequest(rseq.buffer, entry, charset, binattrs);
			break;
		case UNBIND_REQUEST:
			request = new UnbindRequest(rseq.buffer, entry, charset, binattrs);
			break;
		case SEARCH_REQUEST:
			request = new SearchRequest(rseq.buffer, entry, charset, binattrs);
			break;
		case ADD_REQUEST:
			request = new AddRequest(rseq.buffer, entry, charset, binattrs);
			break;
		case MODIFY_REQUEST:
			request = new ModifyRequest(rseq.buffer, entry, charset, binattrs);
			break;
		case DELETE_REQUEST:
			request = new DeleteRequest(rseq.buffer, entry, charset, binattrs);
			break;
		case MODIFYDN_REQUEST:
			request = new ModifyDNRequest(rseq.buffer, entry, charset, binattrs);
			break;
		case EXTENDED_REQUEST:
			request = new ExtendedRequest(rseq.buffer, entry, charset, binattrs);
			break;
		case ABANDON_REQUEST:
			request = new AbandonRequest(rseq.buffer, entry, charset, binattrs);
			break;
		case COMPARE_REQUEST:
			request = new CompareRequest(rseq.buffer, entry, charset, binattrs);
			break;
		default:
			/*
			 * System.out.println(sResHash.getString(
			 * "CONNECTOR.LDAPSERVER.LDAPMESSAGE.UNKNOWN.OPERATION",
			 * Integer.toHexString(rseq.tag)));
			 */
			return false;
		}

		// Any optional controls?
		if (buf.hasRemaining()) {
			ByteBuffer controls = Sequence.getBuffer(buf);
			Attribute attr = entry.newAttribute("ldap.controls");
			while (controls.hasRemaining()) {
				attr.addValue(new LDAPControl(controls));
			}

			// Expand controls into the returned entry
			for (int i = 0; i < attr.size(); i++) {
				LDAPControl c = (LDAPControl) attr.getValue(i);
				if (c.canHandleControl()) {
					Entry e = c.getControlValueEntry();
					if (e != null)
						entry.merge(e);
				} /*
					 * else {
					 * 
					 * System.out.println(sResHash.getString(
					 * "CONNECTOR.LDAPSERVER.LDAPMESSAGE.CANNOT.HANDLE.CONTROL",
					 * c.controlType)); }
					 */
			}

		}

		return true;
	}

	/**
	 * Returns the message id for this LDAP message
	 */
	public int getMessageID() {
		return messageID;
	}

	/**
	 * Sets the response (LDAPResult) message that is sent back to the client.
	 */
	public void setResponse(Sequence response) {
		this.response = response;
	}

	/**
	 * Sets the controls array (array of LDAPControl objects) that is sent back
	 * to the client.
	 */
	public void setControls(Object[] arr) throws Exception {
		if (arr != null && arr.length > 0) {
			controls = new Sequence((byte) 0xa0, 1000);
			for (int i = 0; i < arr.length; i++) {
				if (arr[i] instanceof LDAPControl) {
					/*
					 * System.out.println(sResHash.getString(
					 * "CONNECTOR.LDAPSERVER.LDAPMESSAGE.ADDING.CONTROL",
					 * arr[i]));
					 */
					controls.addBuffer(((LDAPControl) arr[i]).getBuffer());
				} else {
					throw new Exception(
							sResHash
									.getString(
											"CONNECTOR.LDAPSERVER.LDAPMESSAGE.EXPECTED.LDAPCONTROL",
											arr[i]));
				}
			}
		}
	}

	/**
	 * Sets the messageID to use in a response packet.
	 */
	public void setMessageID(int messageID) {
		this.messageID = messageID;
	}

	/**
	 * Returns the request object from the LDAP message ( call after successful
	 * parse() )
	 */
	public Request getRequest() {
		return request;
	}

	/**
	 * Returns the normalized Entry object that represents the LDAP message
	 */
	public Entry getEntry() {
		return entry;
	}

	/**
	 * Dumps the contents of the LDAP message to the log
	 */
	public void dump(Log log) {
		log.info(sResHash.getString(
				"CONNECTOR.LDAPSERVER.LDAPMESSAGE.MESSAGEID", Integer
						.valueOf(getMessageID())));
		if (buf != null)
			BER.dump(buf, log);
		else if (getBuffer() != null)
			BER.dump(getBuffer(), log);

		if (entry != null) {
			log
					.info(sResHash
							.getString("CONNECTOR.LDAPSERVER.LDAPMESSAGE.REQUEST.ENTRY"));
			log.dumpEntry(entry);
		}
	}

}
