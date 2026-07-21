/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.editors.form;

import java.util.List;
import java.util.Vector;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.di.config.base.FormItemConfigImpl;
import com.ibm.di.config.interfaces.FormItemConfig;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.editors.BaseEditor;
import com.ibm.tdi.eclipse.widget.BaseWidget;

public class FormFieldEditor extends BaseWidget {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final String STRING_SYNTAX = "string"; //$NON-NLS-1$
	private static final String DROPDOWN_SYNTAX = "dropedit"; //$NON-NLS-1$
	private static final String DROPLIST_SYNTAX = "droplist"; //$NON-NLS-1$
	private static final String BOOLEAN_SYNTAX = "boolean"; //$NON-NLS-1$
	private static final String TEXTAREA_SYNTAX = "textarea"; //$NON-NLS-1$
	private static final String STATIC_SYNTAX = "static"; //$NON-NLS-1$
	private static final String PASSWORD_SYNTAX = "password"; //$NON-NLS-1$
	private static final String SCRIPT_SYNTAX = "script"; //$NON-NLS-1$
	private static final String COMPONENT_SYNTAX = "component"; //$NON-NLS-1$
	private static final String PANEL_SYNTAX = "panel"; //$NON-NLS-1$

	private static String[] syntaxList = new String[] { STRING_SYNTAX, DROPDOWN_SYNTAX, DROPLIST_SYNTAX, BOOLEAN_SYNTAX,
			TEXTAREA_SYNTAX, STATIC_SYNTAX, PASSWORD_SYNTAX, SCRIPT_SYNTAX, COMPONENT_SYNTAX, PANEL_SYNTAX};

	private static String[] syntaxLabels = new String[] { Messages.getString("FormEditorWidget.syntax.string"), Messages.getString("FormEditorWidget.syntax.dropedit"), Messages.getString("FormEditorWidget.syntax.droplist"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			Messages.getString("FormEditorWidget.syntax.boolean"), Messages.getString("FormEditorWidget.syntax.text"), Messages.getString("FormEditorWidget.syntax.static"), Messages.getString("FormEditorWidget.syntax.password"), Messages.getString("FormEditorWidget.syntax.script"), Messages.getString("FormEditorWidget.syntax.custom"), "Custom Panel"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$

	protected FormItemConfig fic;

	public FormFieldEditor(Composite parent, int style, FormItemConfig editingConfig, BaseEditor editor) {
		super(parent, style, editingConfig, editor);
		this.fic = editingConfig;
		createUI();
	}

	private void createUI() {
		setLayout(new FillLayout());
		Form frm = createForm(this, null);
		FormToolkit tk = getFormToolKit();
		
		int flags = 0;
		if("__GLOBAL__".equals(fic.getParent().getShortName())) {
			flags = SWT.READ_ONLY;
			frm.setText("$GLOBAL." + fic.getShortName());
		} else {
			frm.setText(fic.getShortName());
		}
		
		
		Composite hc = new Composite(frm.getHead(), SWT.NONE);
		hc.setLayout(new GridLayout(2, false));

		// -- Label
		createLabel(hc, Messages.getString("FormEditorWidget.field.label")); //$NON-NLS-1$
		Text t = new Text(hc, SWT.BORDER|flags);
//		String label = fic.getStringParameter(InternalSchema.FORM_LABEL);
//		if(label != null)
//			t.setText(label);
		t.setText(fic.getLabel() != null ? fic.getLabel() : ""); //$NON-NLS-1$
		t.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				fic.setLabel(((Text) e.widget).getText());
			}
		});
		t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// -- No Label
		createLabel(hc, ""); //$NON-NLS-1$
		Button b = createButton(hc, Messages.getString("FormEditorWidget.NoLabel"), SWT.CHECK); //$NON-NLS-1$
		b.setSelection(fic.getBooleanParameter("noLabel", false));
		b.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				fic.setStringParameter("noLabel", ""+((Button) e.widget).getSelection());
			}
		});
		
		// -- Tooltip
		createLabel(hc, Messages.getString("FormEditorWidget.field.tooltip")); //$NON-NLS-1$
		t = new Text(hc, SWT.BORDER|flags);
		t.setText(fic.getToolTip() != null ? fic.getToolTip() : ""); //$NON-NLS-1$
		t.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				fic.setToolTip(((Text) e.widget).getText());
			}
		});
		t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// -- Syntax
		createLabel(hc, Messages.getString("FormEditorWidget.field.type")); //$NON-NLS-1$
		Combo combo = new Combo(hc, SWT.DROP_DOWN | SWT.READ_ONLY);
		for (String str : syntaxLabels)
			combo.add(str);
		String syntax = fic.getSyntax();
		if (syntax == null)
			syntax = STRING_SYNTAX;
		for (int i = 0; i < syntaxList.length; i++) {
			if (syntaxList[i].equals(syntax))
				combo.select(i);
		}

		combo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Combo combo = (Combo) e.widget;
				String syntax = syntaxList[combo.getSelectionIndex()];
				fic.setSyntax(syntax);
			}
		});

		// -- Modes selection
		createLabel(hc, Messages.getString("FormEditorWidget.modes")); //$NON-NLS-1$
		t = new Text(hc, SWT.BORDER|flags);
		String str = fic.getStringParameter("modes"); //$NON-NLS-1$
		t.setText(str == null ? "" : str);
		t.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				fic.setStringParameter("modes", ((Text) e.widget).getText());
			}
		});
		t.setToolTipText(Messages.getString("FormEditorWidget.modes.tooltip"));
		t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));


		// -- Required param
		createLabel(hc, ""); //$NON-NLS-1$
		b = createButton(hc, Messages.getString("FormEditorWidget.field.required"), SWT.CHECK); //$NON-NLS-1$
		b.setSelection(fic.isRequired());
		b.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				fic.setRequired(((Button) e.widget).getSelection());
			}
		});
		
		frm.setHeadClient(hc);

		hc = frm.getBody();
		hc.setLayout(new FillLayout());

		TabFolder tabs = new TabFolder(hc, SWT.TOP);
		TabItem item = new TabItem(tabs, SWT.LEFT);
		item.setText(Messages.getString("FormEditorWidget.field.buttons")); //$NON-NLS-1$
		item.setControl(createButtonsPanel(tabs, tk));

		item = new TabItem(tabs, SWT.LEFT);
		item.setText(Messages.getString("FormEditorWidget.field.values")); //$NON-NLS-1$
		item.setControl(createValueListPanel(tabs, tk));

		item = new TabItem(tabs, SWT.LEFT);
		item.setText(Messages.getString("FormEditorWidget.field.custom")); //$NON-NLS-1$
		item.setControl(createCustomComponent(tabs, tk));
		
		item = new TabItem(tabs, SWT.LEFT);
		item.setText("Custom Panel"); //$NON-NLS-1$
		item.setControl(createCustomPanel(tabs, tk));
	}
	
	private Button createButton(Composite hc, String string, int style) {
		Button b = new Button(hc, style);
		b.setText(string);
		return b;
	}

	private Control createValueListPanel(Composite parent, FormToolkit tk) {
		Composite hc = new Composite(parent, SWT.NONE);
		hc.setLayout(new GridLayout(1, false));

		final TableViewer table = new TableViewer(hc, SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL);
		List<String> values = fic.getValues();
		if (values == null) {
			values = new Vector<String>();
			fic.setValues((Vector<String>) values);
		}

		table.setContentProvider(new ArrayContentProvider());
		table.setInput(values);
		table.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite c = new Composite(hc, SWT.NONE);
		c.setLayout(new GridLayout(99, false));

		Button add = createButton(c, Messages.getString("FormEditorWidget.button.addvalue"), SWT.PUSH); //$NON-NLS-1$
		add.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				InputDialog id = new InputDialog(getShell(), getEditingConfig().getShortName(), Messages.getString("FormEditorWidget.enter.value"), "", null); //$NON-NLS-1$ //$NON-NLS-2$
				if (id.open() == Window.OK) {
					fic.getValues().add(id.getValue());
					fic.setModified(true);
					table.setInput(fic.getValues());
				}
				fic.notifyChange(fic, "", MetamergeConfigChange.MCC_MODIFY); 
			}
		});

		Button delete = createButton(c, Messages.getString("FormEditorWidget.button.deletevalue"), SWT.PUSH); //$NON-NLS-1$
		delete.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				for (Object obj : ((IStructuredSelection) table.getSelection()).toArray()) {
					fic.getValues().remove(obj);
				}
				fic.setModified(true);
				table.setInput(fic.getValues());
				fic.notifyChange(fic, "", MetamergeConfigChange.MCC_MODIFY); 
			}
		});

		Button b = new Button(c, SWT.PUSH);
		b.setText(Messages.getString("FormEditorWidget.button.moveup")); //$NON-NLS-1$
		b.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Object obj = ((IStructuredSelection) table.getSelection()).getFirstElement();
				if (obj != null) {
					int index = fic.getValues().indexOf(obj);
					if (index > 0) {
						fic.getValues().remove(index);
						fic.getValues().add(index - 1, (String) obj);
						table.refresh();
						fic.setModified(true);
					}
				}
				fic.notifyChange(fic, "", MetamergeConfigChange.MCC_MODIFY); 
			}
		});

		b = new Button(c, SWT.PUSH);
		b.setText(Messages.getString("FormEditorWidget.button.movedown")); //$NON-NLS-1$
		b.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Object obj = ((IStructuredSelection) table.getSelection()).getFirstElement();
				if (obj != null) {
					int index = fic.getValues().indexOf(obj);
					if (index < (fic.getValues().size() - 1)) {
						fic.getValues().remove(index);
						fic.getValues().add(index + 1, (String) obj);
						table.refresh();
						fic.setModified(true);
					}
				}
				fic.notifyChange(fic, "", MetamergeConfigChange.MCC_MODIFY);
			}
		});

		return hc;
	}

	private Composite createButtonsPanel(Composite parent, FormToolkit tk) {

		Composite hc = new Composite(parent, SWT.NONE);
		hc.setLayout(new GridLayout(2, false));

		//
		// -- Button 1
		//
		final FormItemConfig config = fic;

		// -- Button1 label
		createLabel(hc, Messages.getString("FormEditorWidget.label.button1")); //$NON-NLS-1$
		Text t = new Text(hc, SWT.BORDER);
		t.setText(fic.getScriptLabel() != null ? fic.getScriptLabel() : ""); //$NON-NLS-1$
		t.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				((FormItemConfigImpl) config).setScriptLabel(((Text) e.widget).getText());
			}
		});
		t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// -- Button1 script
		createLabel(hc, Messages.getString("FormEditorWidget.label.script1")); //$NON-NLS-1$
		t = new Text(hc, SWT.BORDER);
		t.setText(fic.getScript() != null ? fic.getScript() : ""); //$NON-NLS-1$
		t.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				((FormItemConfigImpl) config).setScript(((Text) e.widget).getText());
			}
		});
		t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		//
		// -- Button 2
		//
		createLabel(hc, Messages.getString("FormEditorWidget.label.button2")); //$NON-NLS-1$
		t = new Text(hc, SWT.BORDER);
		t.setText(fic.getScriptLabel2() != null ? fic.getScriptLabel2() : ""); //$NON-NLS-1$
		t.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				((FormItemConfigImpl) config).setScriptLabel2(((Text) e.widget).getText());
			}
		});
		t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// -- Button1 script
		createLabel(hc, Messages.getString("FormEditorWidget.label.script2")); //$NON-NLS-1$
		t = new Text(hc, SWT.BORDER);
		t.setText(fic.getScript2() != null ? fic.getScript2() : ""); //$NON-NLS-1$
		t.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				((FormItemConfigImpl) config).setScript2(((Text) e.widget).getText());
			}
		});
		t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		return hc;
	}

	private Label createLabel(Composite parent, String string) {
		Label label = new Label(parent, SWT.LEFT);
		label.setText(string);
		return label;
	}

	private Composite createCustomComponent(Composite parent, FormToolkit tk) {

		Composite hc = new Composite(parent, SWT.NONE);
		hc.setLayout(new GridLayout(1, false));

		//
		// -- Button 1
		//
		StringBuffer info = new StringBuffer();
		info.append(Messages.getString("FormEditorWidget.custom.javaclass")); //$NON-NLS-1$
		info.append(Messages.getString("FormEditorWidget.custom.info")); //$NON-NLS-1$
		info.append(Messages.getString("FormEditorWidget.custom.info1")); //$NON-NLS-1$
		createLabel(hc, info.toString());

		Text t = new Text(hc, SWT.BORDER);
		t.setText(fic.getScript2() != null ? fic.getScript2() : ""); //$NON-NLS-1$
		t.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				((FormItemConfigImpl) fic).setStringParameter("component", (((Text) e.widget).getText())); //$NON-NLS-1$
			}
		});
		t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		return hc;
	}

	private Control createCustomPanel(TabFolder tabs, FormToolkit tk) {
		final Text text = new Text(tabs, SWT.BORDER|SWT.MULTI|SWT.V_SCROLL);
		String str = fic.getStringParameter("panel");
		if(str == null)
			str = "";
		text.setText(str);
		text.setFont(JFaceResources.getTextFont());
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String str = text.getText();
				// XML behaves badly if we store lines with CRLF, so replace with single LF
				if (str.indexOf('\r')>=0) {
					str = str.replaceAll("\r\n", "\n");
					str = str.replaceAll("\r", "\n"); // macosx
				}
				fic.setStringParameter("panel", str);
			}
		});
		return text;
	}

}
