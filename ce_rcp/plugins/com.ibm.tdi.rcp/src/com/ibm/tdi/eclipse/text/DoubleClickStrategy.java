/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.text;

import java.util.Stack;

import org.eclipse.jface.text.DefaultTextDoubleClickStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

public class DoubleClickStrategy extends DefaultTextDoubleClickStrategy {
	
	@Override
	protected IRegion findExtendedDoubleClickSelection(IDocument document,
			int offset) {
		
		try {
			
			String s = document.get();
			int end = offset;
			if (end >= s.length())
				end--;
			while (end > 0 && Character.isWhitespace(s.charAt(end)))
				end--;

			// We only want to do something if the user points at an end brace.
			if (end <= 0 || s.charAt(end) != '}')
				return null;

			/**
			 * Very simple parsing of the JavaScript.
			 * Unfortunately, it is almost impossible to know what constitutes a comment
			 * without reading from the start, so we read the entire document, and use
			 * a stack to remember positions of open and end braces.
			 */
			Stack<Integer> stack = new Stack<Integer>();
			
			boolean apo = false;
			boolean quote = false;
			boolean comment = false;
			boolean eolComment = false;
			
			for (int pos = 0; pos <= end; pos++) {
				char c = s.charAt(pos);
				if (comment) {
					if (c == '*' && s.charAt(pos + 1) == '/') {
						comment = false;
						pos++;
					}
				} else if (eolComment) {
					if (c == '\n')
						eolComment = false;
				} else if (quote) {
					if (c == '\\') {
						pos++;
					} else if (c == '"') {
						quote = false;
					}
				} else if (apo) {
					if (c == '\\') {
						pos++;
					} else if (c == '\'') {
						apo = false;
					}				
				} else if ( c == '}') {
					stack.push(-pos);
				} else if (c == '{') {
					stack.push(pos);
				} else if (c == '\'') {
					apo = true;
				} else if (c == '"') {
					quote = true;
				} else if (c == '/') {
					pos++;
					c = s.charAt(pos);
					if (c == '/')
						eolComment = true;
					else if (c == '*')
						comment = true;
				}
			}
			if (stack.empty())
				return null; // No braces found, which is kind of odd
			int i = stack.pop();
			if (i != -end)
				return null; // We did not end up with the same end brace we saw earlier

			int count = 1;
			while (!stack.empty()) {
				i = stack.pop();
				if (i < 0) {
					count++;
				} else {
					count--;
					if (count == 0)
						return new Region(i, end - i + 1);						
				}
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}

}
