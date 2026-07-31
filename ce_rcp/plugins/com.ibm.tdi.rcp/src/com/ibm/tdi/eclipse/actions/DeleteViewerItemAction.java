/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.actions.ActionFactory;

import com.ibm.tdi.eclipse.Messages;

/**
 * Deletes an item (row) in a Viewer Component (e.g. Table, List, etc.). This
 * class is abstract and does not provide an implementation of the 'run()'
 * method. All of its subclasses should implement this with the needed logic.
 */
public abstract class DeleteViewerItemAction extends Action {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getActionDefinitionId() {
		return ActionFactory.DELETE.getCommandId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getId() {
		return getActionDefinitionId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getText() {
		return Messages.getString("general.delete.label"); //$NON-NLS-1$
	}

}
