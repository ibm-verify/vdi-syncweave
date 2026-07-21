/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.editors.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
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

import com.ibm.di.config.base.FormConfigImpl;
import com.ibm.di.config.base.FormSectionImpl;
import com.ibm.di.config.base.InternalSchema;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.FormConfig;
import com.ibm.di.config.interfaces.FormItemConfig;
import com.ibm.di.config.interfaces.FormSection;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.tdi.eclipse.Activator;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.editors.BaseEditor;
import com.ibm.tdi.eclipse.editors.FormsEditor;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.widget.BaseWidget;
import com.ibm.tdi.eclipse.widget.FormWidget2;
import com.ibm.tdi.eclipse.widget.SimpleTextEditor;

/**
 * This widget provides basic editing functionality for FormConfig objects. Using this class
 * you can edit sections and fields as well as the scripts for the form.
 */
public class FormEditorWidget extends BaseWidget {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private Composite editorArea;

	private TreeViewer tree;

	private FormConfigImpl fc;

	private String[] topLevelObjects = new String[] { Messages.getString("FormsEditor.forminit"), //$NON-NLS-1$
			Messages.getString("FormsEditor.formevents"), Messages.getString("FormsEditor.formsections"), //$NON-NLS-1$ //$NON-NLS-2$
			Messages.getString("FormsEditor.formfields"), }; //$NON-NLS-1$

	private String[] topLevelIcons = new String[] { "Script", "Script", "Branch_Enabled", "Schema" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

	private SimpleTextEditor initEditor;

	private SimpleTextEditor eventsEditor;

	private HashMap<String, FormSectionEditor> sectionEditors = new HashMap<String, FormSectionEditor>();

	private HashMap<String, FormFieldEditor> fieldEditors = new HashMap<String, FormFieldEditor>();

	public FormEditorWidget(Composite parent, int style, BaseConfiguration editingConfig) {
		this(parent, style, editingConfig, null);
	}

	public FormEditorWidget(Composite parent, int style, BaseConfiguration editingConfig, BaseEditor editor) {
		super(parent, style, editingConfig, editor);
		createUI();
	}

	private void createUI() {
		fc = (FormConfigImpl) getEditingConfig();
		
		setLayout(new FillLayout());

		createForm(this, null);
		getForm().setText(getEditingConfig().getShortName());

		getForm().setHeadClient(createHeadClient(getForm().getHead()));

		Composite main = getForm().getBody();
		main.setLayout(new FillLayout());

		SashForm sash = new SashForm(main, SWT.HORIZONTAL);
		createTreeViewer(sash);

		editorArea = new Composite(sash, SWT.NONE);
		editorArea.setLayout(new StackLayout());

		sash.setWeights(new int[] { 40, 60 });

		createEditors(editorArea);
		
	}

	private void createEditors(Composite parent) {
		initEditor = new SimpleTextEditor(parent, SWT.BORDER) {
			public void updateConfiguration() {
				fc.setStringParameter("formscript", initEditor.getText()); //$NON-NLS-1$
			}
		};
		//initEditor.setUpdateOnFocusOut(true);
		initEditor.setAutoUpdate(true);
		if (fc.getFormScript() != null)
			initEditor.setText(fc.getFormScript());

		eventsEditor = new SimpleTextEditor(parent, SWT.BORDER) {
			public void updateConfiguration() {
				fc.setStringParameter(InternalSchema.FORM_EVENT_HANDLER, eventsEditor.getText());
			}
		};
		//eventsEditor.setUpdateOnFocusOut(true);
		eventsEditor.setAutoUpdate(true);
		if (fc.getFormEventHandler() != null)
			eventsEditor.setText(fc.getFormEventHandler());
	}

	private Control createHeadClient(Composite head) {
		Composite c = new Composite(head, SWT.NONE);
		c.setLayout(new GridLayout(2, false));
		new Label(c, SWT.LEFT).setText(Messages.getString("FormEditorWidget.title")); //$NON-NLS-1$
		final Text t = new Text(c, SWT.BORDER);
		t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		if (fc.getTitle() != null)
			t.setText(fc.getTitle());

		t.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				fc.setStringParameter("title", t.getText()); //$NON-NLS-1$
			}
		});

		new Label(c, SWT.LEFT).setText(Messages.getString("FormEditorWidget.translationFile")); //$NON-NLS-1$
		final Text t2 = new Text(c, SWT.BORDER);
		t2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		if (fc.getTranslationFile() != null)
			t2.setText(fc.getTranslationFile());
		t2.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				fc.setTranslationFile(t2.getText());
				fc.notifyChange(fc, "", MetamergeConfigChange.MCC_MODIFY);  // newly added 
			}
		});

		new Label(c, SWT.LEFT).setText(""); //$NON-NLS-1$

		Composite buttonbar = new Composite(c, SWT.NONE);
		buttonbar.setLayout(new GridLayout(99,false));
		Button b = new Button(buttonbar, SWT.PUSH);
		b.setText(Messages.getString("FormEditorWidget.button.test")); //$NON-NLS-1$
		b.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				try {
					
					// Check for empty sections
					StringBuffer buf = new StringBuffer();
					for(String str : fc.getSectionNames()) {
						FormSection section = fc.getSection(str);
						if(section.getNames().size() == 0) {
							if(buf.length() > 0)
								buf.append(",");
							buf.append(str);
						}
					}
					if(buf.length() > 0 &&
						!MessageDialog.openConfirm(getShell(), getEditor() != null ? getEditor().getPartName() : "", 
								Messages.getMessage("FormEditorWidget.empty.section", buf.toString())))
							return;
						
					BaseConfiguration bc = getEditor() != null ? getEditor().getTDIConfiguration() : getEditingConfig();
					if(bc instanceof FunctionConfig)
						bc = ((FunctionConfig)bc).getFunctionConfig();
					else if(bc instanceof ConnectorConfig)
						bc = ((ConnectorConfig)bc).getConnectionConfig();
					Dialog dlg = new ResizableDialog(bc, fc, getShell());
					dlg.open();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
 			}
		});

		Button remove = new Button(buttonbar, SWT.PUSH);
		remove.setText(Messages.getString("FormEditorWidget.button.delete")); //$NON-NLS-1$
		remove.setToolTipText(Messages.getString("FormEditorWidget.button.delete.tooltip")); //$NON-NLS-1$
		remove.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if(MessageDialog.openConfirm(getShell(), getEditor().getPartName(), Messages.getString("FormEditorWidget.confirm.delete"))) { //$NON-NLS-1$
					((FormsEditor)getEditor()).deleteForm();
				}
			}
		});
		
		return c;
	}

	private int headerIndex(String str) {
		for (int i = 0; i < topLevelObjects.length; i++) {
			if (topLevelObjects[i].equals(str))
				return i;
		}
		return -1;
	}

	private void createTreeViewer(Composite container) {
		
		Composite parent = new Composite(container, SWT.NONE);
		Utils.setGridLayout(parent, 1, false);

		tree = new TreeViewer(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL);
		tree.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		tree.setContentProvider(new ITreeContentProvider() {
			public Object[] getChildren(Object parentElement) {
				if (parentElement instanceof FormConfig) {
					return topLevelObjects;
				} else if (parentElement instanceof String) {
					ArrayList<Object> list = new ArrayList<Object>();
					switch (headerIndex((String) parentElement)) {
					case 2:
						for (String str : fc.getSectionNames()) {
							list.add(fc.getSection(str));
						}
						break;
					case 3:
						for (String str : fc.getFormItemNames()) {
							// -- No editing of read-only fields
							// -- These must be added to sections
							if(str.toUpperCase().startsWith("$GLOBAL.")) {
								continue;
							} else {
								list.add(fc.getFormItem(str));
							}
						}
					}
					return list.toArray();
				}
				return null;
			}

			public Object getParent(Object element) {
				if (element instanceof String)
					return fc;
				else if (element instanceof FormSection)
					return topLevelObjects[2];
				else if (element instanceof FormItemConfig)
					return topLevelObjects[3];
				else
					return null;
			}

			public boolean hasChildren(Object element) {
				if (element instanceof FormConfig)
					return true;
				if (element instanceof String) {
					return headerIndex((String) element) > 1;
				}
				return false;
			}

			public Object[] getElements(Object inputElement) {
				return getChildren(inputElement);
			}

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
		});

		tree.setLabelProvider(new LabelProvider() {

			@Override
			public Image getImage(Object element) {
				if (element instanceof String) {
					int index = headerIndex((String) element);
					if (index != -1)
						return Activator.getImage(topLevelIcons[index]);
				}
				return super.getImage(element);
			}

			@Override
			public String getText(Object element) {
				if(element instanceof FormItemConfig) {
					FormItemConfig fic = (FormItemConfig)element;
					if("__GLOBAL__".equals(fic.getParent().getShortName()))
						return "$GLOBAL." + fic.getShortName();
				}
				return super.getText(element);
			}

		});

		tree.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				if (sel.isEmpty())
					return;
				Object obj = sel.getFirstElement();
				if (obj instanceof String) {
					switch (headerIndex((String) obj)) {
					case 0:
						showEditor(initEditor);
						break;
					case 1:
						showEditor(eventsEditor);
						break;
					default:
						showEditor(null);
					}
				} else if (obj instanceof FormSection) {
					showEditor(getFormSectionEditor((FormSection) obj));

				} else if (obj instanceof FormItemConfig) {
					showEditor(getFormItemEditor((FormItemConfig) obj));

				}
			}
		});
		tree.setInput(fc);
		tree.expandAll();

		// -- toolbar
		Composite tools = new  Composite(parent, SWT.NONE);
		tools.setLayout(new GridLayout(99,false));
		tools.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Button b = new Button(tools, SWT.PUSH);
		b.setText(Messages.getString("FormEditorWidget.button.addsection"));
		b.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				InputDialog id = new InputDialog(getShell(), getEditingConfig().getShortName(), Messages.getString("FormEditorWidget.section.name"), "", null); //$NON-NLS-1$ //$NON-NLS-2$
				if (id.open() == Window.OK) {
					try {
						FormSectionImpl section = new FormSectionImpl();
						String name = id.getValue();
						section.setName(name);
						fc.addSection(section);
						fc.getSectionNames().add(name);
						tree.refresh(topLevelObjects[2]);
						tree.setSelection(new StructuredSelection(section), true);
					} catch (Exception err) {
						EclipseAppender.logerror(err.toString(), err, getShell());
					}
				}
			}
		});
		
		b = new Button(tools, SWT.PUSH);
		b.setText(Messages.getString("FormEditorWidget.button.addfield"));
		b.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				InputDialog id = new InputDialog(getShell(), getEditingConfig().getShortName(), Messages.getString("FormEditorWidget.field.name"), "", null); //$NON-NLS-1$ //$NON-NLS-2$
				if (id.open() == Window.OK) {
					try {
						FormItemConfig fic = fc.newFormItem(id.getValue());
						tree.refresh(topLevelObjects[3]);
						tree.setSelection(new StructuredSelection(fic), true);
						fc.notifyChange(fc, "", MetamergeConfigChange.MCC_MODIFY); // newly added. 
					} catch (Exception err) {
						EclipseAppender.logerror(err.toString(), err, getShell());
					}
				}
			}
		});

		b = new Button(tools, SWT.PUSH);
		b.setText(Messages.getString("FormEditorWidget.button.deletevalue"));
		b.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Object[] values = ((IStructuredSelection)tree.getSelection()).toArray();
				if(values.length == 0)
					return;
				String editorName = getEditor() != null ? getEditor().getPartName() : "Jens";
				if(MessageDialog.openConfirm(getShell(), editorName, Messages.getString("SimpleListUI.prompt.Delete"))) {
					for(Object obj : values) {
						if(obj instanceof FormItemConfig) {
							FormItemConfig fic = (FormItemConfig) obj;
							fc.removeFormItem(fic.getShortName());
							Composite c = fieldEditors.get(fic.getShortName());
							if(c != null) {
								c.dispose();
								fieldEditors.remove(fic.getShortName());
							}
							for(String s : fc.getSectionNames()) {
								FormSection section = fc.getSection(s);
								section.getNames().remove(fic.getShortName());
								if(sectionEditors.get(s) != null)
									sectionEditors.get(s).refresh();
							}
							fc.notifyChange(fc, "", MetamergeConfigChange.MCC_MODIFY); 
						} else if (obj instanceof FormSection) {
							FormSection section = (FormSection) obj;
							fc.getSectionNames().remove(section.getShortName());
							Composite c = sectionEditors.get(section.getShortName());
							if(c != null) {
								c.dispose();
								sectionEditors.remove(section.getShortName());
							}
							fc.notifyChange(fc, "", MetamergeConfigChange.MCC_MODIFY);
						}
					}
					tree.refresh(topLevelObjects[2]);
					tree.refresh(topLevelObjects[3]);
					showEditor(null);
				}
			}
		});
		
		b = new Button(tools, SWT.PUSH);
		b.setText(Messages.getString("FormEditorWidget.button.moveup")); //$NON-NLS-1$
		b.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Object obj = ((IStructuredSelection) tree.getSelection()).getFirstElement();
				if (obj instanceof FormItemConfig) {
					FormItemConfig fic = (FormItemConfig) obj;
					List<String> list = fc.getFormItemNames();
					int index = list.indexOf(fic.getShortName());
					if (index > 0) {
						list.remove(index);
						list.add(index - 1, fic.getShortName());
						tree.refresh(topLevelObjects[3]);
						fic.notifyChange(fic, "", MetamergeConfigChange.MCC_MODIFY);
					}
				}
				if (obj instanceof FormSection) {
					FormSection fs = (FormSection) obj;
					List<String> list = fc.getSectionNames();
					int index = list.indexOf(fs.getShortName());
					if (index > 0) {
						list.remove(index);
						list.add(index - 1, fs.getShortName());
						tree.refresh(topLevelObjects[2]);
						fs.notifyChange(fs, "", MetamergeConfigChange.MCC_MODIFY);
					}
				}
			}
		});

		b = new Button(tools, SWT.PUSH);
		b.setText(Messages.getString("FormEditorWidget.button.movedown")); //$NON-NLS-1$
		b.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Object obj = ((IStructuredSelection) tree.getSelection()).getFirstElement();
				if (obj instanceof FormItemConfig) {
					FormItemConfig fic = (FormItemConfig) obj;
					List<String> list = fc.getFormItemNames();
					int index = list.indexOf(fic.getShortName());
					if (index >= 0 && index < list.size() - 1) {
						list.remove(index);
						list.add(index + 1, fic.getShortName());
						tree.refresh(topLevelObjects[3]);
						fic.notifyChange(fic, "", MetamergeConfigChange.MCC_MODIFY);
					}
				}
				if (obj instanceof FormSection) {
					FormSection fs = (FormSection) obj;
					List<String> list = fc.getSectionNames();
					int index = list.indexOf(fs.getShortName());
					if (index >= 0 && index < list.size() - 1) {
						list.remove(index);
						list.add(index + 1, fs.getShortName());
						tree.refresh(topLevelObjects[2]);
						fs.notifyChange(fs, "", MetamergeConfigChange.MCC_MODIFY);
					}
				}
			}
		});

	}

	protected Composite getFormItemEditor(FormItemConfig fic) {
		String name = fic.getShortName();
		FormFieldEditor c = fieldEditors.get(name);
		if (c == null) {
			c = new FormFieldEditor(editorArea, SWT.NONE, fic, getEditor());
			fieldEditors.put(name, c);
		}
		return c;
	}

	protected Composite getFormSectionEditor(FormSection fs) {
		String name = fs.getShortName();
		FormSectionEditor c = sectionEditors.get(name);
		if (c == null) {
			c = new FormSectionEditor(editorArea, SWT.NONE, fs, getEditor());
			sectionEditors.put(name, c);
		}
		return c;
	}

	protected void showEditor(Composite editor) {
		((StackLayout) editorArea.getLayout()).topControl = editor;
		editorArea.layout(true);
	}

	private static class ResizableDialog extends Dialog {

		private FormConfig fc;
		private BaseConfiguration config;

		public ResizableDialog(BaseConfiguration config, FormConfig fc, Shell parentShell) {
			super(parentShell);
			setShellStyle(getShellStyle() | SWT.RESIZE);
			this.fc = fc;
			this.config = config;
		}

		protected Control createDialogArea(Composite parent) {
			try {
				FormWidget2 fw = new FormWidget2(parent, config, fc);
				fw.setLayoutData(new GridData(GridData.FILL_BOTH));
				if (fc.getTitle() != null)
					getShell().setText(fc.getTitle());
				return fw;
			} catch (Exception e) {
				e.printStackTrace();
				return Utils.exceptionWidget(parent, e);
			}
		}

		@Override
		protected Point getInitialSize() {
			return new Point(600, 400);
		}
		
	}
}
