/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions.operations;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.ibm.di.config.interfaces.BaseConfiguration;

public class SetConfigParameterOperation extends AbstractOperation {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private String name;
	private Object value;
	private Object oldValue;
	private BaseConfiguration config;

	public SetConfigParameterOperation(String string, BaseConfiguration config, String name, Object value) {
		super(string);
		this.name = name;
		this.value = value;
		this.config = config;
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		return redo(monitor, info);
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		oldValue = config.getParameterRaw(name);
		config.setParameter(name, value);
		return Status.OK_STATUS;
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		value = config.getParameter(name);
		config.setParameter(name, oldValue);
		return Status.OK_STATUS;
	}

}
