/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.slf4j.impl;

import org.slf4j.IMarkerFactory;
import org.slf4j.helpers.BasicMarkerFactory;
import org.slf4j.spi.MarkerFactoryBinder;

/**
 * NOOP support for markers.
 * 
 * @since 7.1
 */
public class StaticMarkerBinder implements MarkerFactoryBinder {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	/**
	 * This field is required by the SLF4J API.
	 */
	public static final StaticMarkerBinder SINGLETON = new StaticMarkerBinder();

	private final IMarkerFactory markerFactory = new BasicMarkerFactory();

	private StaticMarkerBinder() {
	}

	/**
	 * {@inheritDoc}
	 */
	public IMarkerFactory getMarkerFactory() {
		return markerFactory;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getMarkerFactoryClassStr() {
		return BasicMarkerFactory.class.getName();
	}

}
