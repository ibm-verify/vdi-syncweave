/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.local.impl;

import java.io.Serializable;

import com.ibm.di.api.APIEngine;
import com.ibm.di.api.DIException;
import com.ibm.di.api.local.AssemblyLine;
import com.ibm.di.api.local.AssemblyLineHandler;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.ResourceHash;

/**
 * This class implements several methods to handle an AssemblyLine.
 */
public class AssemblyLineHandlerImpl implements AssemblyLineHandler {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Local assembly line to handle.
	 */
	private AssemblyLine mAssemblyLine = null;

	/**
	 * Assembly line thread object.
	 */
	private com.ibm.di.server.AssemblyLine mRawAssemblyLine = null;

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private final static ResourceHash sResHash = APIEngine.getResHash();

	/**
	 * Constructor
	 * 
	 * @param aAssemblyLine
	 *            {@link AssemblyLine} instance
	 * @param aRawAssemblyLine
	 *            {@link com.ibm.di.server.AssemblyLine} instance
	 * @param aSession
	 *            {@link SessionImpl} instance
	 * @throws DIException
	 *             if some of the parameters is <code>null</code>.
	 */
	public AssemblyLineHandlerImpl(AssemblyLine aAssemblyLine, com.ibm.di.server.AssemblyLine aRawAssemblyLine, SessionImpl aSession)
			throws DIException {
		if (aAssemblyLine == null) {
			throw new DIException(sResHash.getString("SEVER.API.ASSEMBLYLINE.OBJECT.IS.NULL"));
		}
		if (aRawAssemblyLine == null) {
			throw new DIException(sResHash.getString("SEVER.API.RAW.ASSEMBLYLINE.OBJECT.IS.NULL"));
		}
		if (aSession == null) {
			throw new DIException(sResHash.getString("SEVER.API.SESSION.OBJECT.IS.NULL"));
		}

		mAssemblyLine = aAssemblyLine;
		mRawAssemblyLine = aRawAssemblyLine;
	}

	// No explicit security checks are performed in the interface methods.
	// We assume that if someone has the necessary rights to obtain this object,
	// he is
	// allowed to execute all its methods.
	/**
	 * {@inheritDoc}
	 */
	public AssemblyLine getAssemblyLine() throws DIException {
		return mAssemblyLine;
	}

	/**
	 * {@inheritDoc}
	 */
	public Entry executeCycle(Entry aEntry, boolean aProcessTCB) throws DIException {
		Entry alEntry = null;
		try {
			alEntry = mRawAssemblyLine.executeCycle(aEntry, aProcessTCB);
		} catch (Throwable e) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.COULD.NOT.EXECUTE.ASSEMBLYLINE.CYCLE.1"), e);
		}
		return alEntry;
	}

	/**
	 * {@inheritDoc}
	 */
	public Entry executeCycle(Entry aEntry) throws DIException {
		Entry alEntry = null;
		try {
			alEntry = mRawAssemblyLine.executeCycle(aEntry);
		} catch (Throwable e) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.COULD.NOT.EXECUTE.ASSEMBLYLINE.CYCLE.2"), e);
		}
		return alEntry;
	}

	/**
	 * {@inheritDoc}
	 */
	public Entry executeCycle() throws DIException {
		Entry alEntry = null;
		try {
			alEntry = mRawAssemblyLine.executeCycle();
		} catch (Throwable e) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.COULD.NOT.EXECUTE.ASSEMBLYLINE.CYCLE.3"), e);
		}
		return alEntry;
	}

	/**
	 * {@inheritDoc}
	 */
	public void close() throws DIException {
		try {
			mRawAssemblyLine.executeTerminateAL();
		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.COULD.NOT.CLOSE.ASSEMBLYLINE"), e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Serializable eval(String script) throws DIException {
		Object result;
		try {
			result = mRawAssemblyLine.getScriptEngine().eval(script);
		} catch (Exception e) {
			throw new DIException(e);
		}

		return result instanceof Serializable ? (Serializable) result : (result == null ? null : result.toString());
	}
}
