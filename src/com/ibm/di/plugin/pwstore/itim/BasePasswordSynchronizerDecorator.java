/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.plugin.pwstore.itim;

import java.util.Vector;

import com.ibm.di.plugin.pwstore.BasePasswordChange;
import com.ibm.di.plugin.pwstore.IPasswordSynchronizer;
import com.ibm.di.plugin.pwstore.PasswordChange;
import com.ibm.di.plugin.pwstore.PasswordStore;
import com.ibm.di.plugin.pwstore.PasswordStoreAdapter;
import com.ibm.di.plugin.pwstore.itim.policy.MalformedResponseException;
import com.ibm.di.plugin.pwstore.itim.policy.PasswordPolicyService;
import com.ibm.di.plugin.pwstore.itim.policy.PolicyConnectionException;

/**
 * This is a generic password synchronizer decorator. Its decorates an
 * PasswordStore with password validation functionality.
 */
public final class BasePasswordSynchronizerDecorator implements PasswordStore, IPasswordSynchronizer {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.plugin.CopyRight.OBJECT_CODE;

	PasswordStore syncImpl;

	PasswordPolicyService ppsImpl;

	Exception lastErr;

	/**
	 * Create the generic decorator by wrapping the concrete impls.
	 * 
	 * @param sync
	 *            The traditional TDI password synchronizer.
	 * @param pps
	 *            The password policy service provider.
	 */
	public BasePasswordSynchronizerDecorator(IPasswordSynchronizer sync, PasswordPolicyService pps) {
		super();
		checkParams(sync, pps);

		if (sync instanceof PasswordStore) {
			syncImpl = (PasswordStore) sync;
		} else {
			syncImpl = new PasswordStoreAdapter(sync);
		}

		ppsImpl = pps;
	}

	/**
	 * Create the generic decorator by wrapping the concrete impls.
	 * 
	 * @param sync
	 *            The traditional TDI password synchronizer.
	 * @param pps
	 *            The password policy service provider.
	 */
	public BasePasswordSynchronizerDecorator(PasswordStore sync, PasswordPolicyService pps) {
		super();
		checkParams(sync, pps);
		syncImpl = sync;
		ppsImpl = pps;
	}

	private void checkParams(Object sync, PasswordPolicyService pps) {
		if (sync == null || pps == null) {
			throw new IllegalArgumentException();
		}
	}

	// PasswordStore interface implementation

	/**
	 * @see PasswordStore#isAvailable(PasswordChange)
	 */
	@Override
	public boolean isAvailable(PasswordChange change) {
		boolean ready = false;

		if (change.getPasswords() == null || change.getPasswords().isEmpty()) {
			ready = (ppsImpl.ready() && syncImpl.isAvailable(change));
		} else {
			try {
				if (ppsImpl.ready() && ppsImpl.validatePassword(change.getID(), change.getPasswords())) {
					ready = syncImpl.isAvailable(change);
				}
			} catch (PolicyConnectionException x) {
				lastErr = x;
			} catch (MalformedResponseException x) {
				lastErr = x;
			}
		}
		return ready;
	}

	/**
	 * <p>
	 * Synchronize the users' passwords. This method first validates the
	 * password against the password policy service provider. If successful the
	 * password are then synchronized. Clients should check for errors after
	 * executing this method using {@link #getLastError()}.
	 * </p>
	 * <p>
	 * <b>NB: Although the PasswordPolicyService supports synchronization, this
	 * method does not invoke this support. Synchronization is delegated to the
	 * decorated PasswordStore only.</b>
	 * </p>
	 * 
	 * @see PasswordStore#syncPassword(PasswordChange)
	 */
	public boolean store(PasswordChange change) {
		try {
			if (change.getType() == PasswordChange.DELETE_CHANGE) {
				return syncImpl.store(change);
			} else if(ppsImpl.validatePassword(change.getID(), change.getPasswords())){
				return syncImpl.store(change);
			}
		} catch (PolicyConnectionException x) {
			lastErr = x;
		} catch (MalformedResponseException x) {
			lastErr = x;
		}

		return false;
	}

	/**
	 * @see PasswordStore#setExtendedData(PasswordChange)
	 */
	@Override
	public boolean setExtendedData(PasswordChange change) {
		return syncImpl.setExtendedData(change);
	}

	/**
	 * @see PasswordStore#initialize(java.lang.Object)
	 */
	@Override
	public void initialize(Object aObj) throws Exception {
		ppsImpl.initialize();
		syncImpl.initialize(aObj);
	}

	/**
	 * @see PasswordStore#terminate()
	 */
	@Override
	public void terminate() {
		ppsImpl.terminate();
		syncImpl.terminate();
	}

	// IPasswordSynchronizer interface implementation

	/**
	 * @see IPasswordSynchronizer#readyToSync(java.lang.String)
	 */
	@Deprecated
	public boolean readyToSync(String aId) {
		return readyToSync(aId, null);
	}

	/**
	 * @see IPasswordSynchronizer#readyToSync(java.lang.String,
	 *      java.util.Vector)
	 */
	@Deprecated
	public boolean readyToSync(String aId, Vector aPasswordValues) {
		return isAvailable(new BasePasswordChange(aId, aPasswordValues));
	}

	/**
	 * @see IPasswordSynchronizer#syncPassword(java.lang.String,
	 *      java.util.Vector)
	 */
	@Deprecated
	public boolean syncPassword(String aId, Vector aPasswordValues) {
		return store(new BasePasswordChange(PasswordChange.MODIFY_CHANGE, aId, aPasswordValues));
	}

	/**
	 * @see IPasswordSynchronizer#addPasswordValues(java.lang.String,
	 *      java.util.Vector)
	 */
	@Deprecated
	public boolean addPasswordValues(String aId, Vector aPasswordValues) {
		return store(new BasePasswordChange(PasswordChange.ADD_CHANGE, aId, aPasswordValues));
	}

	/**
	 * @see IPasswordSynchronizer#deletePasswordValues(java.lang.String,
	 *      java.util.Vector)
	 */
	@Deprecated
	public boolean deletePasswordValues(String aId, Vector aPasswordValues) {
		return store(new BasePasswordChange(PasswordChange.DELETE_CHANGE, aId, aPasswordValues));
	}

	/**
	 * @see IPasswordSynchronizer#setExtendedData(String, String)
	 */
	@Deprecated
	public boolean setExtendedData(String id, String extendedData) {
		return setExtendedData(new BasePasswordChange(PasswordChange.MODIFY_EXTENDED_DATA_CHANGE, id, extendedData));
	}

	/**
	 * Enable client to access the exception caught during invocation of object
	 * methods, if an error occurred. This method will clear the exception. This
	 * method is provided because some of the IPasswordSynchronizer methods do
	 * not declare to throw exceptions.
	 * 
	 * @return The last exception, or null. If null, the client can assume the
	 *         previous method executed successfully.
	 */
	public Exception getLastError() {
		Exception result = lastErr;
		lastErr = null;
		return result;
	}

	/**
	 * Get the status message returned from the previous invocation of
	 * {@link #addPasswordValues(PasswordChange)} or
	 * {@link #syncPassword(PasswordChange)}.
	 * 
	 * @return The status message from the service, or <code>null</code> if no
	 *         status was returned.
	 */
	public String getLastPolicyServiceMsg() {
		return ppsImpl.getLastStatusMessage();
	}

}
