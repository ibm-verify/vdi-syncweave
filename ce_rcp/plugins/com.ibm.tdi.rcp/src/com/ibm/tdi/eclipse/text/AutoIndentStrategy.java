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
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextUtilities;

import com.ibm.di.function.SystemFunctions;

public class AutoIndentStrategy extends DefaultIndentLineAutoEditStrategy {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public AutoIndentStrategy() {
		super();
	}

	/**
	 * Copies the indentation of the previous line.
	 * 
	 * @param d
	 *            the document to work on
	 * @param c
	 *            the command to deal with
	 */
	private void autoIndentAfterNewLine(IDocument d, DocumentCommand c) {

		if (c.offset == -1 || d.getLength() == 0)
			return;

		try {
			// find start of line
			int p = (c.offset == d.getLength() ? c.offset - 1 : c.offset);
			IRegion info = d.getLineInformationOfOffset(p);
			int start = info.getOffset();

			/* Some code to handle special case comment blocks */
			if (p+1 == d.getLength() && looksLikeCommentBlock(d, p)) {
				c.text += " * \n */";
				c.caretOffset = c.offset + c.text.length() - 4; 
				c.shiftsCaret = false;
				return;
			} else if (d.get(start, info.getLength()).equals(" */")) {
				return;
			}

			// find white spaces
			int end = findEndOfWhiteSpace(d, start, c.offset);

			StringBuffer buf = new StringBuffer(c.text);
			if (end > start) {
				// append to input
				buf.append(d.get(start, end - start));
			}

			// get last character and indent further if it is a curly brace
			char ch = d.getChar(c.offset - 1);
			if (ch == '{') {
				String indent = buf.toString();
				buf.append("\t");
				if (needsCloseBracer(d, c.offset)) {
					c.caretOffset = c.offset + buf.length();
					c.shiftsCaret = false;
					buf.append(indent);
					buf.append("}");
				}
			}

			c.text = buf.toString();

		} catch (BadLocationException excp) {
			// stop work
			SystemFunctions.doNothing();
		}
	}


	/**
	 * Work around a bug in eclipse, it is unable to do proper scanning of the last line in a Document
	 * @param d
	 * @return
	 */
	private boolean looksLikeCommentBlock(IDocument d, int i) {
		try {
			char next = d.getChar(i);
			while (--i >= 0) {
				char c = d.getChar(i);
				if (c == '/' && next == '*')
					return true;
				if (c == '*' && next == '/')
					return false;
				next = c;
			}
		} catch (BadLocationException e) {
			return false;
		}
		return false;
	}

	private boolean needsCloseBracer(IDocument d, int offset) throws BadLocationException {
		int openBracer = 0;
		for (int i = 0; i<offset; i++) {
			char c = d.getChar(i);
			if ( c == '{' )
				openBracer++;
			else if (c == '}' && openBracer > 0)
				openBracer--;
		}
		for (int i = offset, n = d.getLength(); i<n; i++) {
			char c = d.getChar(i);
			if (c == '{') {
				openBracer++;
			} else if (c == '}') {
				openBracer--;
				if (openBracer <=0 )
					return false;
			}
		}
		return openBracer > 0;
	}
	
	private void decreaseIndentation(IDocument d, DocumentCommand c) {
		if (c.offset < 1 || d.getLength() == 0)
			return;
		
		try {
			char prev = d.getChar(c.offset-1);
			if(prev == ' ' || prev == '\t') {
				c.offset--;
			}
		} catch (BadLocationException e) {
			// stop work
		}
	}
	
	/*
	 * @see org.eclipse.jface.text.IAutoEditStrategy#customizeDocumentCommand(org.eclipse.jface.text.IDocument,
	 *      org.eclipse.jface.text.DocumentCommand)
	 */
	public void customizeDocumentCommand(IDocument d, DocumentCommand c) {
		if(c.text == null)
			return;
		/**
		 * Commented out in defect 15034
		if(isMultiLineText(c.text)) {
			formatMultiLineText(d, c);
		} else 
		*/
		if (c.length == 0 && TextUtilities.endsWith(d.getLegalLineDelimiters(), c.text) != -1)
			autoIndentAfterNewLine(d, c);
		else if(c.text.equals("}"))
			decreaseIndentation(d, c);
		else if(c.text.equals("\""))
			addCompletion(d, c, c.text);
		else if(c.text.equals("'"))
			addCompletion(d, c, c.text);
		else if(c.text.equals("("))
			addCompletion(d, c, ")");
		else if(c.text.equals("["))
			addCompletion(d, c, "]");
		
		// -- these tests are to avoid double end characters when the user types them
		else if(c.text.equals(")"))
			skipCompletion(d, c, ")");
		else if(c.text.equals("]"))
			skipCompletion(d, c, "]");
	}

// Commented out in defect 15034
//	/**
//	 * Returns true if text contains more than one LF character.
//	 *  
//	 * @param text
//	 * @return
//	 */
//	private boolean isMultiLineText(String text) {
//		int firstnl = text.indexOf("\n");
//		if(firstnl == -1)
//			return false;
//		int lastnl = text.lastIndexOf("\n");
//		
//		return firstnl != lastnl;
//	}
//
//	/**
//	 * Takes the new text and reformats it according to the current line's indentation.
//	 * 
//	 * @param d
//	 * @param c
//	 */
//	private void formatMultiLineText(IDocument d, DocumentCommand c) {
//		try {
//			int line = d.getLineOfOffset(c.offset);
//			IRegion info = d.getLineInformation(line);
//			String firstLine = d.get(info.getOffset(), info.getLength());
//			String indent = "";
//			
//			for(int i = 0; i < firstLine.length(); i++) {
//				if(Character.isWhitespace(firstLine.charAt(i)))
//					indent += firstLine.charAt(i);
//				else
//					break;
//			}
//
//			// insert at the end of the indentation, to try to get indentation correct for first line.
//			int offset = info.getOffset() + indent.length();
//			if (c.offset <= offset) {
//				if (c.text.trim().startsWith("}") && indent.length() > 0)
//					offset--;
//				c.offset = offset;
//			}
//
//			StringBuffer buf = new StringBuffer();
//			boolean iscomment = false;
//			
//			for(String s : c.text.split("\n", -1)) {
//				String str = s.trim();
//				
//				//
//				// Javascript multiline comments
//				//
//				if(str.startsWith("/*")) {
//					buf.append(str);
//					buf.append("\n");
//					iscomment = !str.endsWith("*/");
//					continue;
//					
//				} else if(str.startsWith("*") && iscomment) {
//					buf.append(" ");
//					buf.append(str);
//					buf.append("\n");
//					iscomment = !str.endsWith("*/");
//					continue;
//				}
//				
//				
//				//
//				// General indentation for code
//				//
//				if ((str.startsWith("}") || str.endsWith("}")) && indent.length() > 0)
//					indent = indent.substring(1);
//	
//				if (buf.length() > 0)
//					buf.append(indent);
//
//				if (str.endsWith("{"))
//					indent = "\t" + indent;
//
//				buf.append(str);
//				buf.append("\n");
//			}
//
//			// Remove last linefeed
//			buf.deleteCharAt(buf.length() - 1);
//			c.text = buf.toString();
//			
//		} catch (BadLocationException e) {
//			e.printStackTrace();
//		}
//	}

	private void skipCompletion(IDocument d, DocumentCommand c, String endChar) {
		if(hasMatchingStartChar(d, c, endChar.charAt(0))) {
			c.text = "";
			c.caretOffset = c.offset + 1;
		}
	}
	
	private void addCompletion(IDocument d, DocumentCommand c, String endChar) {
		// -- check for open strings before adding an extra quote
		if(endChar.equals("'") || endChar.equals("\"")) {
			try {
				// -- open at this point means user types the final quote
				if(isStringOpen(d, c, endChar.charAt(0))) {
					// -- if the character following current position is a quote then
					// -- we skip over the quote
					try {
						if(d.get(c.offset, 1).equals(endChar)) {
							c.text = "";
							c.caretOffset = c.offset + 1;
						}
					} catch (BadLocationException e) {
						SystemFunctions.doNothing();
					}
					return;
				}
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
		c.text = c.text + endChar;
		c.caretOffset = c.offset + 1;
		c.shiftsCaret = false;
	}
	
	/**
	 * Returns true if we are in an open string (e.g. after the first single quote). Only works on the current line.
	 * 
	 * @throws BadLocationException 
	 */
	private boolean isStringOpen(IDocument d, DocumentCommand c, char ch) throws BadLocationException {
		IRegion r = d.getLineInformationOfOffset(c.offset);
		char[] str = d.get(r.getOffset(), c.offset - r.getOffset()).toCharArray();
		int count = 0;
		for(int i = 0; i < str.length; i++) {
			if(str[i] == ch) {
				if(i > 0 && str[i-1] == '\\') {
					continue;
				}
				count++;
			}
		}
		
		return (count % 2) != 0;
	}
	
	/**
	 * Returns true if the character (", ), ') has  matching being before it. We only scan backwards
	 * once and breaks out on LF, the same char (ch) and eventually the matching character.
	 * 
	 * @param d
	 * @param ch
	 * @return
	 */
	private boolean hasMatchingStartChar(IDocument d, DocumentCommand c, char ch) {
		
		try {
			if(c.offset >= d.getLength())
				return false;
			if(d.getChar(c.offset) != ch)
				return false;
		} catch (Exception e) {
			SystemFunctions.doNothing();
			return false;
		}
		
		char match;
		if(ch == '"')
			match = '"';
		else if (ch == ']')
			match = '[';
		else if (ch == ')')
			match = '(';
		else if (ch == '\'')
			match = '\'';
		else
			return false;
		
		// -- Scan backwards to see if we have an opening brace/quote/parens
		int start = c.offset-1;
		try {
			while(start > 0) {
				char next = d.getChar(start);
				// -- match
				if(next == match)
					return true;
				// -- enough testing one line
				else if (next == '\n')
					return false;
				start--;
			}
		} catch (Exception e) {
			SystemFunctions.doNothing();
		}
		return false;
	}

}
