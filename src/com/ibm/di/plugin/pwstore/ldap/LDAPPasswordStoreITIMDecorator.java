/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.plugin.pwstore.ldap;

import java.util.Vector;

import com.ibm.di.plugin.log.PWSyncLog;
import com.ibm.di.plugin.pwstore.PasswordChange;
import com.ibm.di.plugin.pwstore.IPasswordSynchronizer;
import com.ibm.di.plugin.pwstore.BasePasswordChange;
import com.ibm.di.plugin.pwstore.PasswordStore;
import com.ibm.di.plugin.pwstore.itim.BasePasswordSynchronizerDecorator;
import com.ibm.di.plugin.pwstore.itim.policy.PasswordPolicyFactory;
import com.ibm.di.plugin.pwstore.itim.policy.PasswordPolicyService;
import com.ibm.di.plugin.pwstore.itim.policy.PasswordPolicyServiceBuilder;
import com.ibm.di.plugin.pwstore.itim.policy.PolicyConnectionException;
import com.ibm.di.plugin.pwstore.itim.policy.PolicyInitializationException;

/**
 * <p>
 * The concrete ITIM password policy validation decorator. This class name may
 * be used as the value of the password plugin configuration property named
 * <b>syncClassname</b>.
 * </p>
 * <p>
 * This class decorates an PasswordStore password policy validation checking
 * functions. The validation function is provided by ITIM. The concrete
 * PasswordStore decorated is the existing {@link LDAPPasswordStore}.
 * </p>
 */
public final class LDAPPasswordStoreITIMDecorator implements PasswordStore, IPasswordSynchronizer {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.plugin.CopyRight.OBJECT_CODE;

	private static final String DEFAULT_PASSWORD_POLICY_FACTORY_CLASS = "com.ibm.di.plugin.pwstore.itim.policy.impl.ITIMPasswordPolicyFactoryImpl";

	private static final String PREFIX = LDAPPasswordStore.PREFIX;

	BasePasswordSynchronizerDecorator impl;

	PasswordPolicyFactory factory;

	private PWSyncLog log = null;

	private static PasswordPolicyServiceBuilder getPolicyBuilder() throws ClassNotFoundException, PolicyInitializationException {
		PasswordPolicyServiceBuilder builder = PasswordPolicyServiceBuilder.newBuilder();

		String factClassProp = System.getProperty(PasswordPolicyServiceBuilder.PROP_NAME_FACTORY_CLASS);
		if (factClassProp == null || factClassProp.length() == 0) {
			System.setProperty(PasswordPolicyServiceBuilder.PROP_NAME_FACTORY_CLASS,
					LDAPPasswordStoreITIMDecorator.DEFAULT_PASSWORD_POLICY_FACTORY_CLASS);
		}

		builder.loadFactoryClass();

		return builder;
	}

	/**
	 * <p>
	 * Create new Decorator.
	 * </p>
	 * <p>
	 * Uses {@link PasswordPolicyServiceBuilder} to create an ITIM specfic
	 * {@link PasswordPolicyFactory}. The class name of this factory can be
	 * defined in the configuration file named <b>idipwsync.props</b>. This file
	 * must be in the classpath.
	 * </p>
	 * 
	 * @throws ClassNotFoundException
	 *             if class defined by the configuration property
	 *             <b>passwordPolicyServiceFactory</b> cannot be found.
	 * @throws PolicyInitializationException
	 *             if configuration properties needed by the factory are
	 *             incorrect.
	 * @throws PolicyConnectionException
	 *             if an error occurs creating or opening the connection the
	 *             actual password policy service provider.
	 * @throws IllegalAccessException
	 *             if the factory class cannot be accessed.
	 * @throws InstantiationException
	 *             if the factory class cannot be instantiated.
	 */
	public LDAPPasswordStoreITIMDecorator() throws ClassNotFoundException, PolicyInitializationException,
			PolicyConnectionException, IllegalAccessException, InstantiationException {
		super();

		PasswordPolicyServiceBuilder builder = LDAPPasswordStoreITIMDecorator.getPolicyBuilder();

		factory = builder.getFactory();
		PasswordPolicyService pps = factory.newPasswordPolicyService();
		PasswordStore sync = new LDAPPasswordStore();

		impl = new BasePasswordSynchronizerDecorator(sync, pps);
	}

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
	 * @see PasswordStore#isAvailable(PasswordChange)
	 */
	@Override
	public boolean isAvailable(PasswordChange change) {
		return handleErrors(impl.isAvailable(change));
	}

	/**
	 * @see PasswordStore#syncPassword(jBasePasswordChange)
	 */
	@Override
	public boolean store(PasswordChange change) {
		return handleErrors(impl.store(change));
	}

	/**
	 * @see PasswordStore#initialize(java.lang.Object)
	 */
	@Override
	public void initialize(Object aObj) throws Exception {
		if (aObj instanceof PWSyncLog)
			log = (PWSyncLog) aObj;

		impl.initialize(aObj);
	}

	/**
	 * @see PasswordStore#terminate()
	 */
	@Override
	public void terminate() {
		impl.terminate();
	}

	private boolean handleErrors(boolean success) {
		if (!success) {
			if (impl.getLastPolicyServiceMsg() != null) {
				log.warn(PREFIX, impl.getLastPolicyServiceMsg());
			}

			Exception e = impl.getLastError();
			if (e != null) {
				log.error(PREFIX, e);
			}
		}

		return success;
	}

	/**
	 * @see PasswordStore#setExtendedData(String, String)
	 */
	@Override
	public boolean setExtendedData(PasswordChange change) {
		return handleErrors(impl.setExtendedData(change));
	}

}
