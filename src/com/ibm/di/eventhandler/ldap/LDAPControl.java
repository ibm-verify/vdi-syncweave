/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.eventhandler.ldap;

import java.nio.ByteBuffer;
import java.util.Arrays;

import javax.naming.ldap.Control;

import com.ibm.di.connector.LDAPServerConnector;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.ResourceHash;

public class LDAPControl implements Control {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final long serialVersionUID = 1104675744340664221L;

	public String controlType;

	public boolean criticality = false;

	public byte[] controlValue;

	public transient ByteBuffer controlValueBuffer;

	public final static int OID_PAGED_SEARCH = 0;

	public static final String[] CONTROL_OIDS = { "1.2.840.113556.1.4.319" };

	public static final String[] CONTROL_NAMES = { "ldap.pagedsearch" };

	private final static ResourceHash sResHash = LDAPServerConnector.getResHash();

	/**
	 * Creates a new instance with the given OID. The provided OID is checked
	 * against the local names to translate it to an OID. Otherwise, it is
	 * expected that the oid value is a dotted decimal.
	 */
	public LDAPControl(String oid) {
		for (int i = 0; i < CONTROL_NAMES.length; i++) {
			if (CONTROL_NAMES[i].equals(oid)) {
				this.controlType = CONTROL_OIDS[i];
				return;
			}
		}
		this.controlType = oid;
	}

	public LDAPControl(ByteBuffer buf) throws Exception {
		ByteBuffer b = Sequence.getBuffer(buf);
		controlType = BER.getString(b);
		if (b.hasRemaining() && b.get(b.position()) == BER.BOOLEAN)
			criticality = BER.getBoolean(b);
		if (b.hasRemaining())
			controlValue = BER.getBytes(b);
	}

	public String toString() {
		if (controlValueBuffer != null)
			return "[OID: " + controlType + ", criticality=" + criticality
					+ ", value=" + controlValueBuffer + "]";
		else
			return "[OID: " + controlType + ", criticality=" + criticality
					+ ", value=" + Arrays.toString(controlValue) + "]";
	}

	/**
	 * Returns true if this class can handle the encoding/decoding of the
	 * control value.
	 */
	public boolean canHandleControl() {
		for (int i = 0; i < CONTROL_OIDS.length; i++) {
			if (CONTROL_OIDS[i].equals(controlType))
				return true;
		}
		return false;
	}

	public boolean isPagedSearchControl() {
		return CONTROL_OIDS[OID_PAGED_SEARCH].equals(controlType);
	}

	/**
	 * Returns the internal name for the control type. The control name is used
	 * as a suffix when generating an entry for its values.
	 */
	public String controlAttrPrefix() {
		for (int i = 0; i < CONTROL_OIDS.length; i++) {
			if (CONTROL_OIDS[i].equals(controlType))
				return CONTROL_NAMES[i];
		}
		return null;
	}

	/**
	 * This method parses the control value into an entry object if the control
	 * is known to this class. Otherwise, null is returned.
	 */
	public Entry getControlValueEntry() throws Exception {

		if (!canHandleControl())
			return null;

		ByteBuffer bb = ByteBuffer.allocate(controlValue.length);
		bb.put(controlValue);
		bb.rewind();

		String namePrefix = controlAttrPrefix();

		// unpack contents of sequence
		bb = Sequence.getBuffer(bb);

		Entry controlValueEntry = new Entry();
		if (isPagedSearchControl()) {
			controlValueEntry.setAttribute(namePrefix + ".pagesize",
					Integer.valueOf(BER.getInteger(bb)));
			controlValueEntry.setAttribute(namePrefix + ".cookie", BER
					.getString(bb));
		}
		return controlValueEntry;
	}

	/**
	 * This method converts the entry object to the control value sequence
	 * required by the control type. If the control type is unknown to this
	 * class an exception is thrown.
	 */
	public void setControlValueEntry(Entry entry) throws Exception {
		String namePrefix = controlAttrPrefix();
		if (isPagedSearchControl()) {
			int pageSize = Integer.parseInt(entry.getString(namePrefix
					+ ".pagesize"));
			String cookie = entry.getString(namePrefix + ".cookie");
			if (cookie == null)
				cookie = "";
			Sequence s = new Sequence(BER.SEQUENCE, cookie.length() + 20);
			BER.putInteger(s.buffer, pageSize);
			BER.putString(s.buffer, cookie);
			controlValueBuffer = s.getBuffer();
		} else {
			throw new Exception(sResHash.getString(
					"CONNECTOR.LDAPSERVER.LDAPCONTROL.UNKNOWN.CONTROL",
					controlType));
		}
	}

	/**
	 * This method returns the entire LDAP control as an encoded BER sequence.
	 */
	public ByteBuffer getBuffer() throws Exception {
		int size = (controlType.length() * 2)
				+ (controlValueBuffer == null ? 0 : controlValueBuffer
						.position()) + 10;
		Sequence s = new Sequence(BER.SEQUENCE, size);
		BER.putString(s.buffer, controlType);
		BER.putBoolean(s.buffer, criticality);
		if (controlValueBuffer != null) {
			ByteBuffer b = ByteBuffer.allocate(controlValueBuffer.limit());
			b.put(controlValueBuffer);
			BER.putBytes(s.buffer, b.array());
		}
		return s.getBuffer();
	}

	public byte[] getEncodedValue() {
		// controlValue is already encoded. Just return that. No need to encode
		// this in a BER sequence.
		return controlValue;

	}

	public String getID() {
		return controlType;
	}

	public boolean isCritical() {
		return criticality;
	}

}
