/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.eventhandler.ldap;

import java.util.*;
import java.net.*;
import java.io.*;
import java.nio.*;

public class LDAPResult extends Sequence {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public LDAPResult(int tag, int status, String matchedDN, String errorMessage) {
		this(tag, status, matchedDN, errorMessage, null);
	}

	public LDAPResult(int tag, int status, String matchedDN,
			String errorMessage, Object[] referrals) {

		super(tag, 300);

		String dn = (matchedDN == null ? "" : matchedDN);
		String msg = (errorMessage == null ? "" : errorMessage);

		// Status
		BER.putEnum(buffer, status);

		// Matched DN
		BER.putString(buffer, dn);

		// Error Message
		BER.putString(buffer, msg);

		// Referrals?
		if (referrals != null) {
			Sequence seq = new Sequence(BER.SEQUENCE);
			for (int i = 0; i < referrals.length; i++) {
				BER.putString(seq.buffer, referrals[i].toString());
			}
			addBuffer(seq);
		}
	}

}
