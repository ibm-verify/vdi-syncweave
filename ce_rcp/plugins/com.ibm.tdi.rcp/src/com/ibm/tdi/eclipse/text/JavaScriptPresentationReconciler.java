/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.text;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.swt.graphics.RGB;

public class JavaScriptPresentationReconciler extends PresentationReconciler {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public static final RGB MULTI_LINE_COMMENT = new RGB(128, 0, 0);
	public static final RGB SINGLE_LINE_COMMENT = new RGB(128, 128, 0);
	public static final RGB KEYWORD = new RGB(0, 0, 128);
	public static final RGB TYPE = new RGB(0, 0, 128);
	public static final RGB STRING = new RGB(0, 128, 0);
	public static final RGB DEFAULT = new RGB(0, 0, 0);
	public static final RGB JAVADOC_KEYWORD = new RGB(0, 128, 0);
	public static final RGB JAVADOC_TAG = new RGB(128, 128, 128);
	public static final RGB JAVADOC_LINK = new RGB(128, 128, 128);
	public static final RGB JAVADOC_DEFAULT = new RGB(0, 128, 128);

	private ColorManager colorManager = new ColorManager();

	public JavaScriptPresentationReconciler() {
		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(new SingleTokenScanner(new TextAttribute(colorManager
				.getColor(SINGLE_LINE_COMMENT))));
		setDamager(dr, JavaScriptPartitionScanner.JAVASCRIPT_COMMENT);
		setRepairer(dr, JavaScriptPartitionScanner.JAVASCRIPT_COMMENT);

		dr = new DefaultDamagerRepairer(new SingleTokenScanner(new TextAttribute(colorManager.getColor(MULTI_LINE_COMMENT))));
		setDamager(dr, JavaScriptPartitionScanner.JAVASCRIPT_MULTILINE_COMMENT);
		setRepairer(dr, JavaScriptPartitionScanner.JAVASCRIPT_MULTILINE_COMMENT);

		dr = new DefaultDamagerRepairer(new SingleTokenScanner(new TextAttribute(colorManager.getColor(STRING))));
		setDamager(dr, JavaScriptPartitionScanner.JAVASCRIPT_CONST_STRING);
		setRepairer(dr, JavaScriptPartitionScanner.JAVASCRIPT_CONST_STRING);

		dr = new DefaultDamagerRepairer(new SingleTokenScanner(new TextAttribute(colorManager.getColor(KEYWORD))));
		setDamager(dr, JavaScriptPartitionScanner.JAVASCRIPT_CONST_KEYWORD);
		setRepairer(dr, JavaScriptPartitionScanner.JAVASCRIPT_CONST_KEYWORD);
		
		dr = new DefaultDamagerRepairer(new SingleTokenScanner(new TextAttribute(colorManager.getColor(DEFAULT))));
		setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
	}

}
