/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.tp.server.model.impl.tdi;

import com.ibm.di.tp.server.Constants;
import com.ibm.di.tp.server.ServerActivator;
import com.ibm.di.tp.server.model.TouchpointDestination;
import com.ibm.di.tp.server.model.TouchpointInstance;
import com.ibm.di.tp.server.model.config.DestinationData;
import com.ibm.di.tp.server.model.exception.ErrorCode;
import com.ibm.di.tp.server.model.exception.SCMPException;

/**
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public class TouchpointDestinationImpl implements TouchpointDestination {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;
	private DestinationData cfg;
	private final TouchpointInstanceImpl ti;

	/**
	 * @param cfg
	 */
	public TouchpointDestinationImpl(DestinationData cfg, TouchpointInstanceImpl ti) {
		internalSetConfiguration(cfg);
		this.ti = ti;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.tp.server.model.TouchpointDestination#getConfiguration()
	 */
	public DestinationData getConfiguration() {
		return cfg;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.tp.server.model.TouchpointDestination#getTouchpointInstance()
	 */
	public TouchpointInstance getTouchpointInstance() {
		return ti;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.tp.server.model.TouchpointDestination#setConfiguration(com
	 * .ibm.di.tp.server.model.config.DestinationData)
	 */
	public void setConfiguration(DestinationData cfg) throws SCMPException {
		internalSetConfiguration(cfg);
		try {
			ti.destinationChanged();
		} catch (Exception e) {
			throw new SCMPException(ErrorCode.CONNECTIVITY_UNKNOWN, e.getMessage(), -1, e);
		}
	}

	private void internalSetConfiguration(DestinationData cfg) {
		if (cfg == null) {
			throw new NullPointerException();
		}

		if (cfg.getDestination() == null || cfg.getDestination().getRequestOut() == null) {
			throw new IllegalArgumentException(ServerActivator.L10N.getString("TP.SERVER.RESOURCE.MISSING.REQUEST.OUT.URL"));
		}
		this.cfg = cfg;
	}

}