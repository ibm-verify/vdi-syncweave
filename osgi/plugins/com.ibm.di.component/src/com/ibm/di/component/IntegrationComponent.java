/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.component;

import com.ibm.di.component.base.BaseIntegrationComponent;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.FormConfig;

/**
 * This is an IntegrationComponent which is used within complicated integration
 * scenarios. Each integration component is backed up by a Java Class which
 * client code can instantiate and use. <br>
 * <br>
 * Each IntegrationComponent can have a configuration object that defines the
 * default configuration data which descendants can make use of. Configuration
 * of a component instance is specific for each component, and thus not
 * performed on instantiation.<br>
 * <br>
 * In order for a component to be recognized by the TDI server it must be
 * registered as an OSGi Service. A client is expected to use the interface of
 * the Service registration in order to distinguish between various
 * {@link IntegrationComponent} types. Here is why you need to use the most
 * concrete subclass (for example, {@link ConnectorComponent},
 * {@link FunctionComponent} , {@link ParserComponent}, etc.) when registering
 * the service. Implementations SHOULD inherit from
 * {@link BaseIntegrationComponent} in order to simplify configuration parsing
 * and stay forward compatible when this interface changes. <br>
 * <br>
 * 
 * 
 * @since 7.2
 */
public interface IntegrationComponent {

	/**
	 * @return the keys of the properties specified during component
	 *         registration.
	 */
	public String[] getPropertyKeys();

	/**
	 * Obtains a value for the specified key.
	 * 
	 * @param key
	 *            the key of the property
	 * @return the value of the property
	 */
	public Object getProperty(String key);

	/**
	 * Creates a new instance of this component.
	 * 
	 * @return
	 * @throws Throwable
	 */
	public Object newInstance() throws Throwable;

	/**
	 * Provides the form configuration of this component.
	 * 
	 * @return the {@link FormConfig} of this component or null if not defined.
	 */
	public FormConfig getFormConfig();

	/**
	 * Provides the default configuration object of this component.
	 * 
	 * @return the {@link BaseConfiguration} of this component or null if not
	 *         defined.
	 */
	public BaseConfiguration getDefaultConfig();
}
