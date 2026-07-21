/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.util;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextViewer;

import com.ibm.di.function.SystemFunctions;
import com.ibm.tdi.eclipse.widget.SQLStatementEditor;

/**
 * Scans and detects for keywords in the user provided text. Used for the
 * auto-completion feature of {@link SQLStatementEditor}.
 */
public class WordPartDetector {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * The word can contains following chars.
	 */
	public static final String ALLOW_CHARS = "{!=><&|";

	/**
	 * Part of the word after offset.
	 */
	private String wordPart = "";
	
	/**
	 * Document offset.
	 */
	private int docOffset;

	/**
	 * Method WordPartDetector.
	 * 
	 * @param viewer
	 *            is a text viewer
	 * @param documentOffset
	 *            into the SQL document
	 */
	public WordPartDetector(ITextViewer viewer, int documentOffset) {
		docOffset = documentOffset - 1;
		try {
			while ((docOffset) >= viewer.getTopIndexStartOffset() && isValidChar(viewer, docOffset)) {
				docOffset--;
			}
			// we've been one step too far : increase the offset
			docOffset++;
			wordPart = viewer.getDocument().get(docOffset, documentOffset - docOffset);
		} catch (BadLocationException e) {
			SystemFunctions.doNothing();
		}
	}

	/**
	 * Check if char on this offset is valid char.
	 * 
	 * @param viewer
	 *            to be checked
	 * @param offset
	 *            position to be checked
	 * @return true if in this position stay valid char, else false.
	 * @throws BadLocationException
	 *             is an error occurs
	 */
	private boolean isValidChar(ITextViewer viewer, int offset) throws BadLocationException {
		return Character.isLetterOrDigit(viewer.getDocument().getChar(offset))
				|| (offset >= 0 && ALLOW_CHARS.contains("" + viewer.getDocument().getChar(offset)));
	}

	/**
	 * Return part of the word after offset.
	 * 
	 * @return String
	 */
	public String getString() {
		return wordPart;
	}

	/**
	 * Return document offset
	 * 
	 * @return the document offset
	 */
	public int getOffset() {
		return docOffset;
	}

}