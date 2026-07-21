/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.editors.form;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.forms.widgets.Form;

import com.ibm.di.config.interfaces.FormConfig;
import com.ibm.di.config.interfaces.FormSection;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.editors.BaseEditor;
import com.ibm.tdi.eclipse.widget.BaseWidget;

public class FormSectionEditor extends BaseWidget {

	private FormSection section;
	private Text titleText;
	private TableViewer table;

	public FormSectionEditor(Composite parent, int style, FormSection editingConfig, BaseEditor editor) {
		super(parent, style, editingConfig, editor);
		this.section = editingConfig;
		createUI();
	}

	private void createUI() {
		setLayout(new FillLayout());
		Form frm = createForm(this, null);
		frm.setText(section.getShortName());

		Composite hc = new Composite(frm.getHead(), SWT.NONE);
		hc.setLayout(new GridLayout(2, false));

		// -- Title
		new Label(hc, SWT.LEFT).setText(Messages.getString("FormEditorWidget.section.title")); //$NON-NLS-1$
		titleText = new Text(hc, SWT.BORDER);
		titleText.setText(section.getTitle() != null ? section.getTitle() : ""); //$NON-NLS-1$
		titleText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				section.setTitle(titleText.getText());
			}
		});
		titleText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// -- Initially expanded
		new Label(hc, SWT.LEFT).setText(""); //$NON-NLS-1$
		Button b = new Button(hc, SWT.CHECK);
		b.setText(Messages.getString("FormEditorWidget.section.expanded")); //$NON-NLS-1$
		b.setSelection(section.initiallyExpanded());
		b.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				section.setInitiallyExpanded(((Button) e.widget).getSelection());
			}
		});

		frm.setHeadClient(hc);

		Composite fields = frm.getBody();
		fields.setBackground(getParent().getBackground());
		Utils.setGridLayout(fields, 1, false);

		table = new TableViewer(fields, SWT.BORDER | SWT.V_SCROLL);
		table.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				return section.getNames().toArray();
			}
			public void dispose() {
			}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
		});
		table.setInput(section);
		table.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite tools = new Composite(fields, SWT.NONE);
		tools.setLayout(new GridLayout(99, false));
		tools.setLayoutData(new GridData(SWT.CENTER, SWT.DEFAULT, true, false));
		b = new Button(tools, SWT.PUSH);
		b.setText(Messages.getString("FormEditorWidget.button.selectfields")); //$NON-NLS-1$
		b.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FormConfig fc = (FormConfig) section.getParent();
				List<String> list = new ArrayList<String>();
				for(String str : fc.getFormItemNames()) {
					if(!str.toLowerCase().startsWith("$global."))
						list.add(str);
				}
				Collections.sort(list);

				// -- Add system defined fields
				try {
					FormConfig global = Utils.getSystemForm("__GLOBAL__");
					if(global != null) {
						Iterator<String> iter = global.getLocalFormItemNames();
						while(iter.hasNext()) {
							list.add("$GLOBAL." + iter.next());
						}
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				
				ListSelectionDialog dlg = new ListSelectionDialog(getShell(), list,
						new ArrayContentProvider(), new LabelProvider(), Messages.getString("FormEditorWidget.select.fields")) { //$NON-NLS-1$

				};
				if (dlg.open() == Window.OK) {
					Vector<String> names = section.getNames();
					for (Object obj : dlg.getResult()) {
						if (!names.contains(obj)) {
							names.add((String) obj);
						}
					}
					section.setModified(true);
					table.refresh();
				}
			}
		});

		b = new Button(tools, SWT.PUSH);
		b.setText(Messages.getString("FormEditorWidget.button.deletefield")); //$NON-NLS-1$
		b.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				for (Object obj : ((IStructuredSelection) table.getSelection()).toArray()) {
					section.getNames().remove(obj);
				}
				section.setModified(true);
				table.refresh();
			}
		});

		b = new Button(tools, SWT.PUSH);
		b.setText(Messages.getString("FormEditorWidget.button.moveup")); //$NON-NLS-1$
		b.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Object obj = ((IStructuredSelection) table.getSelection()).getFirstElement();
				if (obj != null) {
					int index = section.getNames().indexOf(obj);
					if (index > 0) {
						section.getNames().remove(index);
						section.getNames().insertElementAt((String) obj, index - 1);
						section.setModified(true);
						table.refresh();
					}
				}
			}
		});

		b = new Button(tools, SWT.PUSH);
		b.setText(Messages.getString("FormEditorWidget.button.movedown")); //$NON-NLS-1$
		b.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Object obj = ((IStructuredSelection) table.getSelection()).getFirstElement();
				if (obj != null) {
					int index = section.getNames().indexOf(obj);
					if (index < (section.getNames().size() - 1)) {
						section.getNames().remove(index);
						section.getNames().insertElementAt((String) obj, index + 1);
						section.setModified(true);
						table.refresh();
					}
				}
			}
		});
	}

	public void refresh() {
		table.setInput(section);
	}
}
