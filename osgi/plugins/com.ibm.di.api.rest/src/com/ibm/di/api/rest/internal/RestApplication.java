/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.rest.internal;

import org.glassfish.jersey.server.ResourceConfig;

import com.ibm.di.web.common.atom.AtomText;
import com.ibm.di.api.rest.internal.handler.InternalAccessor;
import com.ibm.di.api.rest.internal.handler.ServiceDocument;
import com.ibm.di.api.rest.internal.handler.ci.ConfigInstanceFeed;
import com.ibm.di.api.rest.internal.handler.config.ConfigurationDir;
import com.ibm.di.api.rest.internal.handler.listener.ListenerFeedDelegate;
import com.ibm.di.api.rest.internal.handler.server.ServerFeed;
import com.ibm.di.api.rest.internal.handler.tombstone.TsCiFeed;
import com.ibm.di.api.rest.internal.provider.CustomMediaTypeToJaxbJSONProviderDelegator;
import com.ibm.di.api.rest.internal.provider.RestBindingContextResolver;

/**
 * The JAX-RS {@link ResourceConfig} entry point. <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.2
 */
public class RestApplication extends ResourceConfig {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	public RestApplication() {
		// Providers
		register(CustomMediaTypeToJaxbJSONProviderDelegator.class);
		register(RestBindingContextResolver.class);

		// Resources (singletons)
		register(new ServiceDocument());
		register(new ServerFeed());
		register(new ConfigurationDir());
		register(new ConfigInstanceFeed());
		register(new ListenerFeedDelegate());
		register(new TsCiFeed());
		register(new InternalAccessor());
	}
}
