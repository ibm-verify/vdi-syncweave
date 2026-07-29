/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.editors.BaseEditor;
import com.ibm.tdi.eclipse.util.CustomEditorSettings;
import com.ibm.tdi.eclipse.widget.RunOptionsWidget;

public class RunOptionsDialog extends Dialog {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private RunOptionsWidget widget;
	private BaseConfiguration config;
	private BaseEditor editor;
	private CustomEditorSettings settings;

	public RunOptionsDialog(Shell parentShell, BaseConfiguration config, BaseEditor editor,
			CustomEditorSettings settings) {
		super(parentShell);
		this.config = config;
		this.editor = editor;
		this.settings = settings;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite c = (Composite) super.createDialogArea(parent);

		Label label = new Label(c, SWT.LEFT);
		label.setFont(JFaceResources.getBannerFont());
		label.setText(Messages.getString("RunOptionsDialog.1"));
		getShell().setText(Messages.getString("RunOptionsDialog.1"));
		
		widget = new RunOptionsWidget(c, SWT.NULL, config, editor, settings);
		widget.setLayoutData(new GridData(GridData.FILL_BOTH));
		return c;
	}

	@Override
	protected void okPressed() {
		widget.saveSettings();
		super.okPressed();
	}

	@Override
	protected Point getInitialSize() {
		return new Point(500,700);
	}

	@Override
	protected int getShellStyle() {
		return super.getShellStyle() | SWT.RESIZE;
	}
}
