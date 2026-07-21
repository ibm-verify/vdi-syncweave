/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions;

import java.util.ArrayList;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ResourceTransfer;

/**
 * An abstract Paste action that recognizes LocalSelectionTransfer and
 * FileTransfer types.
 */
public abstract class PasteConfigAction extends CutConfigAction {

	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public PasteConfigAction(String text) {
		super(text, null);
		setEnabled(true);
	}

	@Override
	public void run() {
		Clipboard cb = new Clipboard(PlatformUI.getWorkbench().getDisplay());
		LocalSelectionTransfer transfer = LocalSelectionTransfer.getTransfer();
		TextTransfer textTransfer = TextTransfer.getInstance();
		FileTransfer fileTransfer = FileTransfer.getInstance();
		ResourceTransfer resourceTransfer = ResourceTransfer.getInstance();
		ArrayList<Object> list = new ArrayList<Object>();
		for (TransferData type : cb.getAvailableTypes()) {
			if (transfer.isSupportedType(type)) {
				IStructuredSelection selection = (IStructuredSelection) cb.getContents(transfer);
				for (Object obj : selection.toArray()) {
					if (!validatePaste(obj))
						return;
					else
						list.add(obj);
				}
				break;

			} else if (fileTransfer.isSupportedType(type)) {
				Object obj = cb.getContents(fileTransfer);
				if (!validatePaste(obj))
					return;

				list.add(obj);
				break;

			} else if (textTransfer.isSupportedType(type)) {
				Object obj = cb.getContents(textTransfer);
				if (!validatePaste(obj))
					return;

				list.add(obj);
				break;

			} else if (resourceTransfer.isSupportedType(type)) {
				Object obj = cb.getContents(resourceTransfer);
				for (Object res : ((IResource[]) obj)) {
					if (!validatePaste(res))
						return;
					else
						list.add(res);
				}
				break;
			}

		}

		if (list.size() == 0)
			return;

		IStructuredSelection sel = new StructuredSelection(list);
		performPaste(sel);
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
	}

	//
	// -- Users subclass and implement the following methods
	//
	protected abstract void performPaste(IStructuredSelection selection);

	protected abstract boolean validatePaste(Object obj);

}
