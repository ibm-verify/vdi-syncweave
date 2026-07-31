/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.plugin.pwstore.itim.policy;

/**
 * <p>
 * Builder or policy factory objects.
 * </p>
 * <p>
 * This class is uesed to create a {@link PasswordPolicyFactory}. The builder
 * uses property value information using the System.properties.
 * </p>
 * <p>
 * The builder requires the following property names and values to be present<br>
 * <b>passwordPolicyServiceFactory<b> - the class name of a class that
 * implements the {@link PasswordPolicyFactory} interface. <br>
 * </p>
 * <p>
 * Typical usage is shown below:<br>
 * <br>
 * PasswordPolicyServiceBuilder builder =
 * PasswordPolicyServiceBuilder.newBuilder() <br>
 * builder.loadFactoryClass(); <br>
 * PasswordPolicyFactory factory = builder.getFactory(); <br>
 * <br>
 * </p>
 * 
 */
public final class PasswordPolicyServiceBuilder {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.plugin.CopyRight.OBJECT_CODE;

	public static final String PROP_NAME_FACTORY_CLASS = "passwordPolicyServiceFactory";

	private Class factoryClass;

	private PasswordPolicyServiceBuilder() {
		super();
	}

	/**
	 * Create new builder instance.
	 * 
	 * @return new building instance.
	 */
	public static PasswordPolicyServiceBuilder newBuilder() {
		return new PasswordPolicyServiceBuilder();
	}

	/**
	 * Load the factory class.
	 * 
	 * @throws ClassNotFoundException
	 *             if the class name associated with config property
	 *             <i>passwordPolicyServiceFactory</i> cannot be loaded.
	 * @throws PolicyInitializationException
	 *             if the config property
	 * @throws ClassCastException
	 *             if the loaded class is not of type
	 *             <i>passwordPolicyServiceFactory</i> is not present.
	 * @throws LinkageError -
	 *             if the linkage fails
	 * @throws ExceptionInInitializerError -
	 *             if the initialization provoked by this method fails.
	 */
	public void loadFactoryClass() throws ClassNotFoundException,
			ClassCastException, PolicyInitializationException {
		String factoryClassName = System
				.getProperty(PasswordPolicyServiceBuilder.PROP_NAME_FACTORY_CLASS);
		if (factoryClassName == null) {
			throw new PolicyInitializationException();
		}
		factoryClass = Class.forName(factoryClassName);
		if (!PasswordPolicyFactory.class.isAssignableFrom(factoryClass)) {
			String clsName = factoryClass.getName();
			factoryClass = null;
			throw new ClassCastException(PasswordPolicyFactory.class.getName()
					+ " != " + clsName);
		}

	}

	/**
	 * Get the factory object instance following successful configuration and
	 * loading using other builder methods.
	 * 
	 * @return The factory.
	 * 
	 * @throws IllegalStateException
	 *             if {@link #loadFactoryClass()} has not called successfully
	 *             previously.
	 * @throws InstantiationException
	 *             if this Class represents an abstract class, an interface, an
	 *             array class, a primitive type, or void; or if the class has
	 *             no nullary constructor; or if the instantiation fails for
	 *             some other reason
	 * @throws IllegalAccessException
	 *             if the class or its nullary constructor is not accessible.
	 * @throws ExceptionInInitializerError -
	 *             if the initialization provoked by this method fails.
	 * @throws SecurityException -
	 *             if there is no permission to create a new instance.
	 */
	public PasswordPolicyFactory getFactory()
			throws PolicyInitializationException, InstantiationException,
			IllegalStateException, IllegalAccessException {
		if (factoryClass == null) {
			throw new IllegalStateException();
		}

		PasswordPolicyFactory result;
		result = (PasswordPolicyFactory) factoryClass.newInstance();
		result.configure();

		return result;
	}

}
