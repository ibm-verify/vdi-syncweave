/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.widget;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Form;

import com.ibm.di.script.ScriptEngine;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;

public class ValueEditorWidget extends BaseWidget {
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final String TYPE_JAVA_SCRIPT_EVALUATION = "JavaScript";
	private static final String TYPE_BOOLEAN = "Boolean";
	private static final String TYPE_INTEGER = "Integer";
	private static final String TYPE_STRING = "String";
	
	protected String type;
	private Object value;
	private Composite editorArea;
	private Text stringEditor;
	private SimpleTextEditor jsEditor;
	private Composite jsContainer;

	public static Object openValueEditorDialog(Shell shell, Object value) throws Throwable {
		ValueEditorDialog dlg = new ValueEditorDialog(shell, value);
		if(dlg.open() == Window.OK)
			return dlg.getValue();
		else
			return null;
	}

	public ValueEditorWidget(Composite parent, Object value) {
		super(parent, SWT.NONE);
		this.value = value;
		
		setLayout(new FillLayout());
		Form f = createForm(this, null);
		
		// -- The editor type
		f.setHeadClient(addClassSelection(f.getHead()));

		// -- The editor area
		f.getBody().setLayout(new FillLayout());
		editorArea = new Composite(f.getBody(), SWT.NONE);
		editorArea.setLayout(new StackLayout());
		createEditors(editorArea);
		selectEditor();
	}

	public Object getValue() throws Exception {
		String val;
		if(TYPE_JAVA_SCRIPT_EVALUATION.equals(type))
			val = jsEditor.getText();
		else
			val = stringEditor.getText();
		
		if(TYPE_INTEGER.equals(type))
			return Integer.valueOf(val);
		else if(TYPE_BOOLEAN.equals(type))
			return Boolean.valueOf(val);
		else if(TYPE_STRING.equals(type))
			return val;
		
		// -- javascript eval
		ScriptEngine se = new ScriptEngine(null);
		se.declareBean("currentValue", value);
		return se.eval(val);
	}
	
	private Composite addClassSelection(Composite parent) {
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayout(new GridLayout(6, false));

		SelectionAdapter listener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Button b = (Button) e.widget;
				type = b.getText();
				selectEditor();
			}
		};

		Button b = new Button(c, SWT.RADIO);
		b.setText(TYPE_STRING);
		b.addSelectionListener(listener);
		b.setSelection(value instanceof String);
		if(b.getSelection())
			type = TYPE_STRING;

		b = new Button(c, SWT.RADIO);
		b.setText(TYPE_INTEGER);
		b.addSelectionListener(listener);
		b.setSelection(value instanceof Integer);
		if(b.getSelection())
			type = TYPE_INTEGER;

		b = new Button(c, SWT.RADIO);
		b.setText(TYPE_BOOLEAN);
		b.addSelectionListener(listener);
		b.setSelection(value instanceof Boolean);
		if(b.getSelection())
			type = TYPE_BOOLEAN;

		b = new Button(c, SWT.RADIO);
		b.setText(TYPE_JAVA_SCRIPT_EVALUATION);
		b.addSelectionListener(listener);
		if(b.getSelection())
			type = TYPE_JAVA_SCRIPT_EVALUATION;

		c.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return c;
	}

	private void createEditors(Composite parent) {
		stringEditor = new Text(parent, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		if(value != null)
			stringEditor.setText(value.toString());
		
		jsContainer = new Composite(parent, SWT.NONE);
		jsContainer.setLayout(new GridLayout(1,false));
		new Label(jsContainer, SWT.LEFT).setText(Messages.getString("ValueEditorWidget.javascript.info"));
		jsEditor = new SimpleTextEditor(jsContainer, SWT.NONE);
		if(value != null) {
			value = value.toString().replaceAll("\\n", "\n// ");
			jsEditor.setText("// " + value.toString() + "\n\nreturn currentValue;\n");
			
		}
		jsEditor.setLayoutData(new GridData(GridData.FILL_BOTH));
	}

	protected void selectEditor() {
		if (TYPE_JAVA_SCRIPT_EVALUATION.equals(type))
			((StackLayout) editorArea.getLayout()).topControl = jsContainer;
		else
			((StackLayout) editorArea.getLayout()).topControl = stringEditor;
		editorArea.layout(true);
	}

	private static class ValueEditorDialog extends Dialog {
		
		private ValueEditorWidget editor;
		private Object value;

		public ValueEditorDialog(Shell parentShell, Object value) {
			super(parentShell);
			this.value = value;
		}

		protected Control createDialogArea(Composite parent) {
			Composite composite = (Composite) super.createDialogArea(parent);
			Utils.setGridLayout(composite, 1, false);
			editor = new ValueEditorWidget(composite, value);
			editor.setLayoutData(new GridData(GridData.FILL_BOTH));
			getShell().setText("Value Editor");
			return composite;
		}

		@Override
		protected void okPressed() {
			try {
				this.value = editor.getValue();
			} catch (Exception e) {
				this.value = e;
			}
			super.okPressed();
		}

		public Object getValue() throws Throwable {
			if(value instanceof Throwable)
				throw (Throwable)value;
			else
				return value;
		}

		@Override
		protected Point getInitialSize() {
			return new Point(500,300);
		}
		
		protected int getShellStyle() {
			return super.getShellStyle() | SWT.RESIZE;
		}
	}
}
