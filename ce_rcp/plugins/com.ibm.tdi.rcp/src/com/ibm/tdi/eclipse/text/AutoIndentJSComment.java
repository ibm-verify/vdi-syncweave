/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.text;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextUtilities;

/**
 * This class is used to auto format comment sections
 *
 */
public class AutoIndentJSComment extends DefaultIndentLineAutoEditStrategy {
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	@Override
	public void customizeDocumentCommand(IDocument d, DocumentCommand c) {
		if(c.text == null)
			return;

		try {
			if (c.length == 0 && TextUtilities.endsWith(d.getLegalLineDelimiters(), c.text) != -1) {
				if(shouldAddCommentEnd(d, c.offset)) {
					c.text += " * \n */";
					c.caretOffset = c.offset + c.text.length() - 4; 
					c.shiftsCaret = false;
				} else {
					c.text += " * ";
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean shouldAddCommentEnd(IDocument d, int start) {
		try {
			boolean seenEOL = false;
			int end = d.getLength() - 1;
			for (int i = start; i < end; i++) {
				char c = d.getChar(i);
				if (c == '\n')
					seenEOL = true;
				if (c == '*' && d.getChar(i+1) == '/')
					return false;
				if (c == '/' && d.getChar(i+1) == '*')
					return true;
				if (!seenEOL && ! Character.isWhitespace(c))
					return false;
			}
		} catch (BadLocationException e) {
			return true;
		}
		return true;
	}
}
