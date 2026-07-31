/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions.operations;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.tdi.eclipse.Activator;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.editors.PropertiesEditor;

public class RemoveConfigOperation extends AbstractOperation {
	@SuppressWarnings("unused") //$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private ArrayList<BaseConfiguration> items;
	private ArrayList<BaseConfiguration> undoItems;
	private HashMap<BaseConfiguration, Integer> positions = new HashMap<BaseConfiguration, Integer>();

	public RemoveConfigOperation(String label) {
		super(label);
	}

	public RemoveConfigOperation(String label, BaseConfiguration item) {
		super(label);
		items = new ArrayList<BaseConfiguration>();
		undoItems = new ArrayList<BaseConfiguration>();
		items.add(item);
	}
	
	public RemoveConfigOperation(String label, ArrayList<BaseConfiguration> items, ArrayList<BaseConfiguration> undoItems) {
		super(label);
		this.items = items;
		this.undoItems = new ArrayList<BaseConfiguration>();
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		return redo(monitor, info);
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		for(BaseConfiguration b : items) {
			if(!b.detachFromParent()) {
				if (PropertiesEditor.isProperty(b)) {
					b.setParameter(PropertiesEditor.DELETED, "true");
					undoItems.add(b);
				} else if(b.getParent() instanceof ContainerConfig) {
					ContainerConfig cc = (ContainerConfig) b.getParent();
					int position = cc.indexOf(b);
					cc.removeConfig(b);
					positions.put(b, position);
					undoItems.add(b);
				}
			} else {
				undoItems.add(b);
			}
		}
		
		return Status.OK_STATUS;
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		while(undoItems.size() > 0) {
			BaseConfiguration b = undoItems.remove(0);
			if(!b.reattachToParent(-1)) {
				if (PropertiesEditor.isProperty(b)) {
					b.removeParameter(PropertiesEditor.DELETED);
				} else if(b.getParent() instanceof ContainerConfig) {
					ContainerConfig cc = (ContainerConfig) b.getParent();
					int position = positions.containsKey(b) ? positions.get(b) : -1;
					cc.insertConfig(b, position);
				} else {
					return new Status(Status.ERROR, Activator.TDI_PLUGIN_ID, 0, Messages.getMessage("RemoveConfigOperation.1", "" + b.getClass()), null); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}
		positions.clear();
		monitor.done();
		return Status.OK_STATUS;
	}

	@Override
	public boolean canExecute() {
		return items.size() > 0 && !canUndo();
	}

	@Override
	public boolean canRedo() {
		return canExecute();
	}

	@Override
	public boolean canUndo() {
		return undoItems.size() > 0;
	}

}
