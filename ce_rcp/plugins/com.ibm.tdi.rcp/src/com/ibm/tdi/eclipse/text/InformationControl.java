/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.text;

import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

public class InformationControl implements IInformationControl {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private Composite control;
	private StyledText text;
	
	public InformationControl(Shell parent, IInformationControlCreator informationControlCreator) {
		control = new Composite(parent, SWT.BORDER);
		control.setLayout(new FillLayout());
		text = new StyledText(control, SWT.MULTI);
	}

	public void addDisposeListener(DisposeListener listener) {
		control.addDisposeListener(listener);
	}

	public void addFocusListener(FocusListener listener) {
		control.addFocusListener(listener);
	}

	public Point computeSizeHint() {
		return new Point(300,200);
	}

	public void dispose() {
		control.dispose();
	}

	public boolean isFocusControl() {
		return false;
	}

	public void removeDisposeListener(DisposeListener listener) {
		control.removeDisposeListener(listener);
	}

	public void removeFocusListener(FocusListener listener) {
		control.removeFocusListener(listener);
	}

	public void setBackgroundColor(Color background) {
		text.setBackground(background);
	}

	public void setFocus() {
		text.setFocus();
	}

	public void setForegroundColor(Color foreground) {
		text.setForeground(foreground);
	}

	public void setInformation(String information) {
		
	}

	public void setLocation(Point location) {
		control.setLocation(location);
	}

	public void setSize(int width, int height) {
		control.setSize(width, height);
	}

	public void setSizeConstraints(int maxWidth, int maxHeight) {
	}

	public void setVisible(boolean visible) {
		control.setVisible(visible);
	}

}
