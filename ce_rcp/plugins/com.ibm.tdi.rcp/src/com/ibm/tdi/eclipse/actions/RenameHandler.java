/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.HandlerEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import com.ibm.tdi.eclipse.Messages;

/**
 * This handler is active only when the TDI Navigator panel is active. See the plugin.xml file
 * for details on activation.
 *
 */
public class RenameHandler extends AbstractHandler {
	
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private IResource selectedFile;
	private IAction renameAction;
	
	public RenameHandler() {
		super();
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().addSelectionListener(new ISelectionListener() {
			public void selectionChanged(IWorkbenchPart part, ISelection selection) {
				selectedFile = null;
				if( ! selection.isEmpty() &&
					selection instanceof IStructuredSelection) {
					IStructuredSelection ss = (IStructuredSelection) selection;
					if(ss.size() != 1) {
						selectedFile = null;
					} else if(ss.getFirstElement() instanceof IResource) {
						selectedFile = (IResource) ss.getFirstElement();
					}
				}
				// -- make sure workbench knows we can handle it
				fireHandlerChanged(new HandlerEvent(RenameHandler.this, true, false));
			}
		});
		
		renameAction = new Action() {
			public String getText() {
				return Messages.getString("action.label.1");
			}
		};
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		RenameResourceAction ren = new RenameResourceAction();
		ren.init(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
		ren.setSelection(new StructuredSelection(selectedFile));
		ren.run(renameAction);
		return null;
	}

	public boolean isEnabled() {
		return (selectedFile != null);
	}

	public boolean isHandled() {
		return true;
	}

}
