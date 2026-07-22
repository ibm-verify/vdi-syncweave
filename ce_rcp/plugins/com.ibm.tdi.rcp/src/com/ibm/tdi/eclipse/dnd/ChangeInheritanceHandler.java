/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.dnd;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;

import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.log.EclipseAppender;

public class ChangeInheritanceHandler extends DropTargetAdapter {
	@SuppressWarnings("unused") //$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private BaseConfiguration config;

	public ChangeInheritanceHandler(BaseConfiguration config) {
		this.config = config;
	}

	@Override
	public void drop(DropTargetEvent event) {
		IStructuredSelection sel = (IStructuredSelection) event.data;
		Object obj = sel.getFirstElement();
		if (! (obj instanceof IFile))
			return; 
		
		if(!MessageDialog.openConfirm(event.widget.getDisplay().getActiveShell(), 
				Messages.getString("HooksWidget.0"),  //$NON-NLS-1$
				Messages.getMessage("HooksWidget.1", obj.toString()))) //$NON-NLS-1$
			return;
		try {
			String ref = ((TDIConfigurationFile)config.getMetamergeConfig()).addReference((IFile)obj, null);
			config.setInheritsFromRef(ref);
		} catch (Exception e){
			EclipseAppender.logerror(e.getMessage(), e);
		}
	}

	@Override
	public void dragEnter(DropTargetEvent event) {
		if(LocalSelectionTransfer.getTransfer().isSupportedType(event.currentDataType)) {
			event.detail = DND.DROP_COPY;
		}
	}

	@Override
	public void dragOver(DropTargetEvent event) {
		if(LocalSelectionTransfer.getTransfer().isSupportedType(event.currentDataType)) {
			if(event.item == null)
				event.feedback = DND.FEEDBACK_NONE;
			else
				event.detail = DND.DROP_COPY;
		}
	}

}
