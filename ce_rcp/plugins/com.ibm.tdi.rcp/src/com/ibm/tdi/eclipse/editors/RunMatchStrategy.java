/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
 package com.ibm.tdi.eclipse.editors;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorMatchingStrategy;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;

public class RunMatchStrategy implements IEditorMatchingStrategy {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public boolean matches(IEditorReference editorRef, IEditorInput input) {
		IEditorPart ed = editorRef.getEditor(false);
		if (!(ed instanceof RunAssemblyLineEditor))
			return false;
		RunAssemblyLineEditor editor = (RunAssemblyLineEditor) ed;
		if (editor.isRunning())
			return false;
		
		if (! (input instanceof RunAssemblyLineInput))
			return false;
		RunAssemblyLineInput newInput = (RunAssemblyLineInput) input;
		if (newInput.hasFoundMatch())
			return false;
		
		RunAssemblyLineInput oldInput = (RunAssemblyLineInput) editor.getEditorInput();

		if (newInput == oldInput) {
			newInput.setFoundMatch();
			return true;
		}
		if (!oldInput.isEqual(newInput))
			return false;
		
		//Looks like the same input. Restart the AssemblyLine with the new input
		if (editor.restart(newInput)) {
			newInput.setFoundMatch();
			return true;			
		}
		return false;
	}
}
