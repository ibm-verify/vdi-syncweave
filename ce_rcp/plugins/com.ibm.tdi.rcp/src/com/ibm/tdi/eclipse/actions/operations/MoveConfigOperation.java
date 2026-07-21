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
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.TDI;

public class MoveConfigOperation extends AbstractOperation {
	@SuppressWarnings("unused") //$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private BaseConfiguration item;
	private BaseConfiguration target;
	private int position;
	private int oldPosition = -1;
	private ContainerConfig oldContainer;
	private boolean relative = true;

	public MoveConfigOperation(BaseConfiguration item, BaseConfiguration target, int position) {
		super("MoveConfig"); //$NON-NLS-1$
		this.item = item;
		this.position = position;
		this.target = target;
		this.relative = true;
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		return redo(monitor, info);
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		if(!item.detachFromParent()) {
			if(item.getParent() instanceof ContainerConfig) {
				// Do some safety checking
				// First, verify that we do not try to insert an object into itself.
				if (isParentOf(item, target))
					return Status.CANCEL_STATUS;
				// The rest of the checking may not be needed, but better safe than sorry
				if (relative && position == TDI.INSERT_INTO && 
						! (target instanceof ContainerConfig))
					return Status.CANCEL_STATUS;
				if (relative && position != TDI.INSERT_INTO && 
						! (target.getParent() instanceof ContainerConfig))
					return Status.CANCEL_STATUS;
				if (!relative && ! (target instanceof ContainerConfig))
					return Status.CANCEL_STATUS;

				oldContainer = (ContainerConfig) item.getParent();
				oldPosition = oldContainer.indexOf(item);
				oldContainer.removeConfig(item);
				ContainerConfig cc;
				if(relative) {
					switch(position) {
					case TDI.INSERT_BEFORE:
						cc = (ContainerConfig) target.getParent();
						cc.insertConfig(item, cc.indexOf(target));
						break;
					case TDI.INSERT_AFTER:
						cc = (ContainerConfig) target.getParent();
						cc.insertConfig(item, cc.indexOf(target)+1);
						break;
					case TDI.INSERT_INTO:
						cc = (ContainerConfig)target;
						cc.addConfig(item);
						break;
					}
					relative = false;
				} else {
					cc = (ContainerConfig)target;
					cc.insertConfig(item, position);
				}
			} else {
				throw new ExecutionException(Messages.getString("MoveConfigOperation.2")); //$NON-NLS-1$
			}
		}
		return Status.OK_STATUS;
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		if(!item.reattachToParent(oldPosition)) {
			target = oldContainer;
			position = oldPosition;
			relative = false;
			redo(monitor, info);
		}
		monitor.done();
		return Status.OK_STATUS;
	}

	@Override
	public boolean canExecute() {
		return item != null && target != null;
	}

	@Override
	public boolean canRedo() {
		return false;
	}

	@Override
	public boolean canUndo() {
		return true;
	}
/**
 * Return true if the first parameter is equal to or in the parent chain of the second parameter.
 * @param a The potential parent
 * @param b See if this is an offspring of a
 * @return True if a is parent or grand...parent of b
 */
	private boolean isParentOf(BaseConfiguration a, BaseConfiguration b){
		while (b != null) {
			if ( a == b)
				return true;
			b = b.getParent();
		}
		return false;
	}
}
