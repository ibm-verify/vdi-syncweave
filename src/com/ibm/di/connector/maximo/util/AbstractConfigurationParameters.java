/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.maximo.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.ibm.di.connector.maximo.core.SimpleTpaeIFConnector;
import com.ibm.di.connector.maximo.exception.MxConnConfigException;
import com.ibm.di.server.Log;
import com.ibm.di.util.StringUtils;

/**
 * Collection of configuration parameters.
 * <p>
 * Note: This class is thread-safe.
 * 
 * @since 7.1
 * @see SimpleTpaeIFConnector
 */
public abstract class AbstractConfigurationParameters {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private final Map<String, String> parameters;

	/**
	 * Logger used by the TPAE IF Connector.
	 */
	protected Log logger;

	/**
	 * Constructs a new {@link AbstractConfigurationParameters}.
	 */
	public AbstractConfigurationParameters(Log log) {
		logger = log;
		parameters = Collections.synchronizedMap(new HashMap<String, String>());
	}

	private static boolean isPassword(final String key) {
		return key.indexOf("password") > -1;
	}

	private static Map<String, String> load(final InputStream in, final String prefix) throws IOException {

		final Properties props;
		final HashMap<String, String> result;

		props = new Properties();
		props.load(in);

		result = new HashMap<String, String>();
		for (final Object key : props.keySet()) {
			final String strKey = key.toString();
			if (strKey.startsWith(prefix)) {
				result.put(strKey.substring(prefix.length()), props.getProperty(strKey));
			}
		}

		return result;
	}

	/**
	 * Removes all configuration parameters.
	 */
	public final void clear() {
		logger.debug(SimpleTpaeIFConnector.getResHash().getString("MXCONN.CLEAR.CONFIG.PARAMS"));
		parameters.clear();
	}

	/**
	 * Returns the value associated to the specified <code>key</code>.
	 * 
	 * @param key
	 *            the key whose associated value is to be returned
	 * @param defaultValue
	 *            the value that should be returned if the parameter specified
	 *            by <code>key</code> is not defined
	 * @return value associated to the specified <code>key</code> or
	 *         <code>defaultValue</code> if the parameter is not previously
	 *         defined
	 */
	public final String getParameter(final String key, final String defaultValue) {
		if (!isDefined(key)) {
			return defaultValue;
		}
		return parameters.get(key);
	}

	/**
	 * Returns the value associated to the specified <code>key</code> as
	 * <code>boolean</code>.
	 * 
	 * @param key
	 *            the key whose associated value is to be returned
	 * @return <code>true</code> if value equals to <code>"true"</code>,
	 *         otherwise <code>false</code>
	 */
	public final boolean getParameterAsBoolean(final String key) {
		return "true".equals(parameters.get(key));
	}

	/**
	 * Returns the value associated to the specified <code>key</code> as
	 * <code>int</code>.
	 * 
	 * @param key
	 *            the key whose associated value is to be returned
	 * @param defaultValue
	 *            the value that should be returned if the parameter specified
	 *            by <code>key</code> is not defined
	 * @return value associated to the specified <code>key</code> or
	 *         <code>defaultValue</code> if the parameter is not previously
	 *         defined
	 */
	public final int getParameterAsInt(final String key, final int defaultValue) {
		try {
			return Integer.parseInt(parameters.get(key));
		} catch (final Exception e) {
			return defaultValue;
		}
	}

	/**
	 * Checks if the specified parameter is defined or not.
	 * 
	 * @param key
	 *            the key that identifies the parameter
	 * @return <code>true</code> if the parameter has an associated value,
	 *         otherwise <code>false</code>
	 * @see StringUtils#isBlank(String)
	 */
	protected final boolean isDefined(final String key) {
		return !StringUtils.isBlank(parameters.get(key));
	}

	protected void checkParamAndThrow(final String key) {
		if (!isDefined(key)) {
			throw new MxConnConfigException(SimpleTpaeIFConnector.getResHash().getString("MXCONN.PARAM.NOT.DEFINED", key));
		}
	}

	/**
	 * Defines a configuration parameter.
	 * 
	 * @param key
	 *            configuration parameter key
	 * @param value
	 *            configuration parameter value
	 */
	public final void setParameter(final String key, final String value) {
		parameters.put(key, value);
	}

	/**
	 * Returns a {@link String} with all defined properties.
	 * <p>
	 * <b>Note:</b> Any property identified by a key that contains the word
	 * <i>password</i> will not have its value exposed.
	 * </p>
	 */
	@Override
	public final String toString() {
		final StringBuilder sb = new StringBuilder(AbstractConfigurationParameters.class.getName() + "{");

		for (final String key : parameters.keySet()) {
			final String value = parameters.get(key);

			sb.append(key).append('=');
			if (isPassword(key) && isDefined(key)) {
				sb.append("(********)");
			} else {
				sb.append(value);
			}
			sb.append("; ");
		}
		sb.append('}');
		return sb.toString();
	}

	/**
	 * Returns the {@link Map} that stores all the configuration parameters.
	 * 
	 * @return {@link Map} that stores all the configuration parameters
	 */
	protected final Map<String, String> getParams() {
		return parameters;
	}

	/**
	 * Loads the properties from a file.
	 * 
	 * @param fileName
	 *            path to the file containing the configuration properties
	 * @param prefix
	 *            prefix of the configuration properties that should be loaded
	 * @return <code>true</code> if the configuration properties could be
	 *         loaded, otherwise <code>false</code>
	 */
	protected final boolean loadFromFile(final String fileName, final String prefix) {
		try {
			final InputStream in = new FileInputStream(fileName);
			parameters.putAll(load(in, prefix));
		} catch (final IOException e) {
			logger.debug(SimpleTpaeIFConnector.getResHash().getString("MXCONN.CANNOT.READ.FILE", new Object[] { fileName, e }));
			return false;
		}
		return true;
	}

	/**
	 * Loads the properties from a resource stream.
	 * 
	 * @param resource
	 *            path to the resource stream that contains the configuration
	 *            properties
	 * @param prefix
	 *            prefix of the configuration properties that should be loaded
	 * @return <code>true</code> if the configuration properties could be
	 *         loaded, otherwise <code>false</code>
	 */
	protected final boolean loadFromResource(final String resource, final String prefix) {
		try {
			final ClassLoader cl = AbstractConfigurationParameters.class.getClassLoader();
			final InputStream in = cl.getResourceAsStream(resource);

			parameters.putAll(load(in, prefix));
		} catch (final Exception e) {
			logger.debug(SimpleTpaeIFConnector.getResHash().getString("MXCONN.CANNOT.LOAD.RESOURCE", new Object[] { resource, e }));
			return false;
		}
		return true;
	}
}
