/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.commands;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;

/**
 * A proxy object that binds & activates a handler for a given command ID and invokes
 * the run() method of the target action.
 */
public class CommandHandlerProxy {
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	
	private IHandlerService service;
	private IHandlerActivation token;
	
	public CommandHandlerProxy(IWorkbenchPartSite site, IAction action) {
		this(site, action, action.getActionDefinitionId());
	}
	
	public CommandHandlerProxy(IWorkbenchPartSite site, IAction action, String actionID) {
		action.setActionDefinitionId(actionID);
		service = (IHandlerService)site.getService(IHandlerService.class);
		if (service != null)
			token = service.activateHandler(action.getActionDefinitionId(), new ActionHandler(action));
	}

	public void dispose() {
		if (service != null)
			service.deactivateHandler(token);
	}
		
}
