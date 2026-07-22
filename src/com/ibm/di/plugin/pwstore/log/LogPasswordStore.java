/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.plugin.pwstore.log;

import java.util.Date;
import java.util.Vector;

import com.ibm.di.plugin.log.PWSyncLog;
import com.ibm.di.plugin.pwstore.BasePasswordChange;
import com.ibm.di.plugin.pwstore.IPasswordSynchronizer;
import com.ibm.di.plugin.pwstore.PasswordChange;
import com.ibm.di.plugin.pwstore.PasswordStore;
import com.ibm.di.server.ResourceHash;
import com.ibm.icu.text.SimpleDateFormat;

public class LogPasswordStore implements IPasswordSynchronizer, PasswordStore {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.plugin.CopyRight.OBJECT_CODE;

	static final String PREFIX = "LogStore";

	private PWSyncLog log = null;

	private static final ResourceHash resHash = ResourceHash.getHash("logpwstore");

	public synchronized void initialize(Object aObj) throws Exception {
		if (aObj instanceof PWSyncLog)
			log = (PWSyncLog) aObj;
		log.info(PREFIX, resHash.getString("LOGPWSTORE.INIT"));
		log.info(PREFIX, resHash.getString("LOGPWSTORE.INIT.OBJ", aObj));
	}

	@Deprecated
	public boolean readyToSync(String id) {
		return readyToSync(id, null);
	}

	@Deprecated
	public boolean readyToSync(String id, Vector passwords) {
		return isAvailable(getPasswordChange(PasswordChange.NO_CHANGE, id, passwords, null));
	}

	@Deprecated
	public boolean syncPassword(String id, Vector passwords) {
		return store(getPasswordChange(PasswordChange.MODIFY_CHANGE, id, passwords, null));
	}

	@Deprecated
	public boolean addPasswordValues(String id, Vector passwords) {
		return store(getPasswordChange(PasswordChange.ADD_CHANGE, id, passwords, null));
	}

	@Deprecated
	public boolean deletePasswordValues(String id, Vector passwords) {
		return store(getPasswordChange(PasswordChange.DELETE_CHANGE, id, passwords, null));
	}

	@Deprecated
	public boolean setExtendedData(String id, String extendedData) {
		return setExtendedData(getPasswordChange(PasswordChange.MODIFY_EXTENDED_DATA_CHANGE, id, null, extendedData));
	}

	private PasswordChange getPasswordChange(int type, String id, Vector passwords, String extendedData) {
		return new BasePasswordChange(type, id, passwords, extendedData, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isAvailable(PasswordChange change) {
		log.info(PREFIX, resHash.getString("LOGPWSTORE.READY1", change.getID()));

		if (change.getPasswords() != null && !change.getPasswords().isEmpty()) {
			log.info(PREFIX, resHash.getString("LOGPWSTORE.READY2", change.getPasswords().toString()));
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean store(PasswordChange change) {
		String custData = change.getCustomData() != null ? change.getCustomData() : "";
		String timeStamp = formatDate(change.getTimestamp());

		switch (change.getType()) {
		case PasswordChange.ADD_CHANGE:
			log.debug(PREFIX, resHash.getString("LOGPWSTORE.ADD", new Object[] { change.getID(), change.getPasswords(), timeStamp,
					custData }));
			break;
		case PasswordChange.MODIFY_CHANGE:
			log.debug(PREFIX, resHash.getString("LOGPWSTORE.SYNC", new Object[] { change.getID(), change.getPasswords(), timeStamp,
					custData }));
			break;
		case PasswordChange.DELETE_CHANGE:
			log.debug(PREFIX, resHash.getString("LOGPWSTORE.DEL", new Object[] { change.getID(), change.getPasswords(), timeStamp,
					custData }));
			break;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void terminate() {
		log.debug(PREFIX, resHash.getString("LOGPWSTORE.TERM"));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean setExtendedData(PasswordChange change) {
		log.debug(PREFIX, resHash.getString("LOGPWSTORE.SET.EXT.DATA", new Object[] { change.getID(), change.getExtData() }));
		return true;
	}

	/**
	 * This method formats a date into the LDAPv3 Generalized Time Syntax.
	 * 
	 * @param date
	 *            date in milliseconds
	 * @return string representation of a date
	 */
	private String formatDate(long date) {
		return (new SimpleDateFormat("yyyyMMddHHmmss.SZ")).format(new Date(date));
	}
}
