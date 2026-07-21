/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.plugin.pwstore;

/**
 * This class adapts implementations of the deprecated
 * {@link IPasswordSynchronizer} interface to the newly introduced
 * {@link PasswordStore} interface.
 */
public class PasswordStoreAdapter implements PasswordStore {

	/**
	 * Object to adapt.
	 */
	private IPasswordSynchronizer pwSynch;

	public PasswordStoreAdapter(IPasswordSynchronizer store) {
		pwSynch = store;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isAvailable(PasswordChange change) {
		return pwSynch.readyToSync(change.getID(), change.getPasswords());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean store(PasswordChange change) {
		boolean result = false;
		
		switch(change.getType()) {
		case PasswordChange.ADD_CHANGE:
			result = pwSynch.addPasswordValues(change.getID(), change.getPasswords());
			break;
		case PasswordChange.MODIFY_CHANGE:
			result = pwSynch.syncPassword(change.getID(), change.getPasswords());
			break;
		case PasswordChange.DELETE_CHANGE:
			result = pwSynch.deletePasswordValues(change.getID(), change.getPasswords());
			break;
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean setExtendedData(PasswordChange change) {
		return pwSynch.setExtendedData(change.getID(), change.getExtData());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize(Object aObj) throws Exception {
		pwSynch.initialize(aObj);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void terminate() {
		pwSynch.terminate();
	}
}
