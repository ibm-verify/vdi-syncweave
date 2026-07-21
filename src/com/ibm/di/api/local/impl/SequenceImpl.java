/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.local.impl;

import com.ibm.di.api.APIAuditor;
import com.ibm.di.api.APIEngine;
import com.ibm.di.api.AuthorizationException;
import com.ibm.di.api.DIException;
import com.ibm.di.api.local.ConfigInstance;
import com.ibm.di.api.local.Sequence;
import com.ibm.di.entry.Entry;
import com.ibm.di.exceptions.AbortALException;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.TaskStatistics;

public class SequenceImpl implements Sequence {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * The real Sequence.
	 */
	private com.ibm.di.server.Sequence sequence;
	
	/**
	 * {@link ConfigInstanceImpl} instance
	 */
	private ConfigInstanceImpl configInstance = null;

	/**
	 * Represents the local session.
	 */
	private SessionImpl session = null;

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private final static ResourceHash sResHash = APIEngine.getResHash();

	/**
	 * Represents the name of the corresponding interface. It is used as part of
	 * the mechanism to filter authorization audit notifications.
	 */
	private final static String interfaceName = "Sequence";

	/**
	 * Class constructor.
	 * 
	 * @throws DIException
	 *             if any of the parameter is <code>null</code>
	 */
	public SequenceImpl(com.ibm.di.server.Sequence sequence,
			ConfigInstanceImpl configInstance, SessionImpl session)
			throws DIException {
		if (sequence == null) {
			throw new DIException(sResHash
					.getString("SEVER.API.RAW.ASSEMBLYLINE.OBJECT.IS.NULL"));
		}
		if (configInstance == null) {
			throw new DIException(sResHash
					.getString("SEVER.API.CONFIG.INSTANCE.OBJECT.IS.NULL"));
		}
		if (session == null) {
			throw new DIException(sResHash
					.getString("SEVER.API.SESSION.OBJECT.IS.NULL"));
		}

		this.sequence = sequence;
		this.configInstance = configInstance;
		this.session = session;
	}

	public ConfigInstance getConfigInstance() throws DIException {
		return configInstance;
	}

	public String getName() throws DIException {
		return sequence.getName();
	}

	public Entry getResult() throws DIException {
		return sequence.getResult();
	}

	public TaskStatistics getStatistics() throws DIException {
		return sequence.getStats();
	}

	public int getUniqueCode() throws DIException {
		return sequence.hashCode();
	}

	public boolean isActive() throws DIException {
		return sequence.isAlive();
	}

	public void stop() throws DIException {
		String methodExtension = "stop";
		boolean authSuccessful = session.getIdentity().canExecuteAL(
				configInstance.getConfigId(), getName());
		APIAuditor.sendSessionAuditData(session.getIdentity().getUserId(),
				configInstance.getConfiguration().getPath(), getName(),
				authSuccessful, interfaceName, methodExtension, getName(),
				configInstance.getConfigId());
		if (!authSuccessful) {
			throw new AuthorizationException();
		}

		sequence.shutdown();
	}

	public void stop(boolean sync) throws DIException {
		String methodExtension = "stop";
		boolean authSuccessful = session.getIdentity().canExecuteAL(
				configInstance.getConfigId(), getName());
		APIAuditor.sendSessionAuditData(session.getIdentity().getUserId(),
				configInstance.getConfiguration().getPath(), getName(),
				authSuccessful, interfaceName, methodExtension, getName(),
				configInstance.getConfigId());
		if (!authSuccessful) {
			throw new AuthorizationException();
		}
		try {
			sequence.shutdown(sync);
		} catch (AbortALException aae) {
			// Cannot happen.
			SystemFunctions.doNothing();
		}
	}

}
