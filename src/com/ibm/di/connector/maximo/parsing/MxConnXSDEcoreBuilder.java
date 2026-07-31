/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.maximo.parsing;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.resource.impl.ResourceFactoryImpl;
import org.eclipse.emf.ecore.resource.impl.URIConverterImpl;
import org.eclipse.xsd.ecore.XSDEcoreBuilder;
import org.eclipse.xsd.util.XSDResourceImpl;

import com.ibm.di.connector.maximo.exception.MxConnIOException;
import com.ibm.di.connector.maximo.exception.MxConnectorRuntimeException;
import com.ibm.di.connector.maximo.util.HttpClient;
import com.ibm.di.server.Log;

/**
 * This class loads XSD files from HTTP servers using the {@link HttpClient}.
 * 
 * @since 7.1
 */
public final class MxConnXSDEcoreBuilder extends XSDEcoreBuilder {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private final SchemaConfiguration cfg;

	private final HttpClient client;
	
	private final class MxConnResourceFactoryImpl extends ResourceFactoryImpl {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Resource createResource(final URI uri) {

			// Use different resource for maximo
			return new MxConnXSDResourceImpl(uri);
		}
	}

	private final class MxConnURIConverterImpl extends URIConverterImpl {

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected InputStream createURLInputStream(final URI uri) {
			/*
			 * Added to resolve the included schema locations. For example :
			 * <xsd:includeschemaLocation=
			 * "http://192.168.11.135:9080/meaweb/schema/common/meta/MXMeta.xsd"
			 * /> \ will not be found if 192.168.11.135 is not the actual IP of
			 * the machine (it is not specified in the baseURL parameter).
			 */
			client.setTargetUrlList(transformUrl(uri.toString()));
			try {
				return client.getAsInputStream();
			} catch (final MxConnIOException e) {
				throw new MxConnectorRuntimeException("", e);
			}
		}
	}

	private final class MxConnXSDResourceImpl extends XSDResourceImpl {

		/**
		 * Constructs a new {@link MxConnXSDResourceImpl}.
		 * 
		 * @param uri
		 *            reference to the XSD
		 */
		public MxConnXSDResourceImpl(final URI uri) {
			super(uri);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected URIConverter getURIConverter() {
			// use different URI converter
			return new MxConnURIConverterImpl();
		}
	}

	/**
	 * Constructs a new {@link MxConnXSDEcoreBuilder}.
	 * 
	 * @param cfg
	 *            configuration parameters required to build load the XSD
	 */
	public MxConnXSDEcoreBuilder(final SchemaConfiguration cfg, Log log) {
		this.client = new HttpClient(log);
		this.cfg = cfg;

		client.setTimeout(this.cfg.getTimeout());
		client.setAuthenticationRequired(this.cfg.isAuthenticationRequired());
		client.setUserId(this.cfg.getUserId());
		client.setPassword(this.cfg.getPassword());
		client.setTargetUrlList(this.cfg.getUrlList());
	}

	/**
	 * Generates a collection of EMF objects representing the XSD.
	 * 
	 * @return collection of EMF objects representing the XSD
	 * @throws MxConnIOException
	 *             if any communication problem occurs
	 */
	public Collection<EObject> generate() throws MxConnIOException {
		try {
			// This string could be arbitrary because it would be
			// transformed in the createURLInputStream method
			return generate(URI.createURI("http://host" + cfg.getXsdSuffix()));
		} catch (final MxConnectorRuntimeException e) {
			if (e.getCause() instanceof MxConnIOException) {
				throw (MxConnIOException) e.getCause();
			}
			throw e;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected ResourceSet createResourceSet() {
		final ResourceSet rs;
		final Map registry;

		rs = super.createResourceSet();
		rs.setURIConverter(new MxConnURIConverterImpl());
		registry = rs.getResourceFactoryRegistry().getExtensionToFactoryMap();

		// Change default XSD resource factory with
		// maximo's resource factory implementation
		registry.put("xsd", new MxConnResourceFactoryImpl());

		return rs;
	}

	/**
	 * Make sure URLs from the Maximo Enterprise Adapter web client use one of
	 * the domains specified in the URLs from the <code>maximoBaseURL</code>
	 * parameter.
	 * 
	 * @param target
	 *            string representation of the requested URI
	 * @return transformed URL
	 */
	private List<String> transformUrl(final String target) {

		final List<String> result;
		final int index = target.indexOf(cfg.getServiceBase());

		if (index == -1) {
			result = new ArrayList<String>(1);
			result.add(target);
		} else {
			result = new ArrayList<String>(cfg.getUrlList().size());
			for (final String url : cfg.getUrlList()) {
				result.add(url + target.substring(index));
			}
		}

		return result;
	}
}
