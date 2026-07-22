/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.widget;

import org.eclipse.jface.text.DefaultTextDoubleClickStrategy;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.URLHyperlinkDetector;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

public class TextEditorWidget extends Composite {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private TextViewer text;

	public TextEditorWidget(Composite parent, int style) {
		super(parent, style);
		
		setLayout(new FormLayout());
		
		text = new TextViewer(this, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		text.setHyperlinkDetectors(new IHyperlinkDetector[] {new URLHyperlinkDetector()}, 0);
		text.enableOperation(ITextOperationTarget.CUT, true);
		text.enableOperation(ITextOperationTarget.COPY, true);
		text.enableOperation(ITextOperationTarget.PASTE, true);
		text.enableOperation(ITextOperationTarget.DELETE, true);
		text.enableOperation(ITextOperationTarget.SELECT_ALL, true);
		text.enableOperation(ITextOperationTarget.PRINT, true);
		text.enableOperation(ITextOperationTarget.SHIFT_LEFT, true);
		text.enableOperation(ITextOperationTarget.SHIFT_RIGHT, true);
		
		text.setTextDoubleClickStrategy(new DefaultTextDoubleClickStrategy(), IDocument.DEFAULT_CONTENT_TYPE);

		FormData fd = new FormData();
		fd.left = new FormAttachment(0,0);
		fd.top = new FormAttachment(0,0);
		fd.bottom = new FormAttachment(100,0);
		fd.right = new FormAttachment(100,0);
		text.getControl().setLayoutData(fd);
	}

	public void setDocument(Document document) {
		text.setDocument(document);
	}

	public void addTextListener(ITextListener listener) {
		text.addTextListener(listener);
	}

	public IDocument getDocument() {
		return text.getDocument();
	}

	public void setEditable(boolean b) {
		text.setEditable(b);
	}

	public TextViewer getTextViewer() {
		return text;
	}

}
