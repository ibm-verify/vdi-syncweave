/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.plugin.pwstore.ldap;

import java.util.Vector;

import com.ibm.di.plugin.log.PWSyncLog;
import com.ibm.di.plugin.pwstore.BasePasswordChange;
import com.ibm.di.plugin.pwstore.IPasswordSynchronizer;
import com.ibm.di.plugin.pwstore.PasswordChange;
import com.ibm.di.plugin.pwstore.PasswordStore;
import com.ibm.di.server.ResourceHash;

public class LDAPPasswordStore implements PasswordStore, IPasswordSynchronizer {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.plugin.CopyRight.OBJECT_CODE;

	private static final ResourceHash resHash = ResourceHash.getHash("ldappwstore");

	static final String PREFIX = "LDAPStore";

	private PWSyncLog log = null;
	private IDIPasswordStore mPasswordStore = null;

	// IPasswordSynchronizer interface implementation

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

	// PasswordStore interface implementation

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void initialize(Object aObj) throws Exception {

		if (aObj instanceof PWSyncLog)
			log = (PWSyncLog) aObj;

		try {
			mPasswordStore = new IDIPasswordStore(log);
		} catch (Throwable e) {
			throw new Exception(e.toString(), e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized boolean isAvailable(PasswordChange change) {
		return mPasswordStore.readyToSync();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized boolean store(PasswordChange change) {
		boolean success = false;

		if (change.getID() == null) {
			log.warn(PREFIX, resHash.getString("PWSTORE.LDAP.STORE.PASSWORD.NULL.USER"));
			return false;
		}

		String userName = getUserName(change.getID());

		switch (change.getType()) {
		case PasswordChange.ADD_CHANGE:
			success = mPasswordStore.addPasswordValues(new BasePasswordChange(PasswordChange.ADD_CHANGE, userName, change
					.getPasswords(), change.getExtData(), change.getCustomData()));
			break;
		case PasswordChange.MODIFY_CHANGE:
			success = mPasswordStore.modifyPassword(new BasePasswordChange(PasswordChange.MODIFY_CHANGE, userName, change
					.getPasswords(), change.getExtData(), change.getCustomData()));
			break;
		case PasswordChange.DELETE_CHANGE:
			success = mPasswordStore.deletePasswordValues(new BasePasswordChange(PasswordChange.DELETE_CHANGE, userName, change
					.getPasswords(), change.getExtData(), change.getCustomData()));
			break;
		}

		return success;
	}

	private String getUserName(String aDn) {
		if (aDn == null) {
			return null;
		}

		// supports LDAP distinguished names, Windows simple names,
		// Domino abreviated FullNames and Domino canonical FullNames
		int startInd = aDn.indexOf('=');
		int endInd = aDn.indexOf(',');
		if (endInd == -1) {
			endInd = aDn.indexOf('/');
			if (endInd == -1) {
				endInd = aDn.length();
			}
		}

		return aDn.substring(startInd + 1, endInd);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void terminate() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized boolean setExtendedData(PasswordChange change) {

		if (change.getID() == null) {
			log.warn(PREFIX, resHash.getString("PWSTORE.LDAP.SET.EXT.DATA.NULL.USER"));
			return false;
		}

		String userName = getUserName(change.getID());

		boolean success = mPasswordStore.setExtendedData(new BasePasswordChange(PasswordChange.MODIFY_EXTENDED_DATA_CHANGE,
				userName, change.getExtData()));

		return success;
	}

}
