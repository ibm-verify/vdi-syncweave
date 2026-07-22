/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions.operations;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ContainerConfig;

public class InsertConfigOperation extends RemoveConfigOperation {
	@SuppressWarnings("unused") //$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private BaseConfiguration[] config;
	private ContainerConfig target;
	private int[] position;
	private boolean done = false;
	
	public InsertConfigOperation(BaseConfiguration config, ContainerConfig target, int position) {
		this(new BaseConfiguration[]{config}, target, new int[]{position});
	}

	public InsertConfigOperation(BaseConfiguration[] configs, ContainerConfig target, int[] positions) {
		super("InsertConfig"); //$NON-NLS-1$
		this.config = configs;
		this.target = target;
		this.position = positions;
		for(BaseConfiguration c : config)
			c.setParent(target);
	}
	
	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		int j = 0;
		for(int i = 0; i < config.length; i++) {
			j = (position.length > i ? position[i] : j);
			target.insertConfig(config[i], j);
		}
		done = true;
		return Status.OK_STATUS;
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		return execute(monitor, info);
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		for(int i = 0; i < config.length; i++)
			target.removeConfig(config[i]);
		done = false;
		return Status.OK_STATUS;
	}

	@Override
	public boolean canExecute() {
		return target != null && config != null;
	}

	@Override
	public boolean canRedo() {
		return !done;
	}

	@Override
	public boolean canUndo() {
		return done;
	}

}
