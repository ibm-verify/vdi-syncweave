/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.widget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.Form;

import com.ibm.di.config.base.InternalSchema;
import com.ibm.di.config.eclipse.MetamergeConfigCE;
import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.LinkCriteriaConfig;
import com.ibm.di.config.interfaces.LinkCriteriaItem;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.di.config.interfaces.MetamergeConfigChangeListener;
import com.ibm.di.config.interfaces.SchemaConfig;
import com.ibm.di.config.interfaces.SchemaItemConfig;
import com.ibm.tdi.eclipse.Activator;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.editors.SchemaEditor;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.providers.WorkEntryAttributesProvider;
import com.ibm.tdi.eclipse.util.TextEditorContextMenu;

public class LinkCriteriaWidget extends BaseWidget {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private final static String[] OPERATORS = new String[] {
			Messages.getString("LinkCriteriaLabelProvider.Oper.0"),
			Messages.getString("BranchingConfig.Conditions.less"),
			Messages.getString("BranchingConfig.Conditions.lessequal"),
			Messages.getString("BranchingConfig.Conditions.greater"),
			Messages.getString("BranchingConfig.Conditions.greaterequal"),

			Messages.getString("LinkCriteriaLabelProvider.Oper.1"),
			Messages.getString("LinkCriteriaLabelProvider.Oper.2"),
			Messages.getString("LinkCriteriaLabelProvider.Oper.3"),
			Messages.getString("LinkCriteriaLabelProvider.Oper.4"),
			};

	private final static String[] OPERATOR_VALUES = new String[] {
			LinkCriteriaItem.LC_EXACT,
			LinkCriteriaItem.LC_LESS_THAN,
			LinkCriteriaItem.LC_LESS_THAN_OR_EQUAL,
			LinkCriteriaItem.LC_GREATER_THAN,
			LinkCriteriaItem.LC_GREATER_THAN_OR_EQUAL,

			LinkCriteriaItem.LC_SUBSTRING,
			LinkCriteriaItem.LC_INITIAL,
			LinkCriteriaItem.LC_FINAL,
			LinkCriteriaItem.LC_NOT,
			};

	private Composite stack;

	private SimpleTextEditor linkEditor;

	private ScrolledComposite body;

	private Composite content;

	private Form frm;

	private Form scriptForm;

	public LinkCriteriaWidget(LinkCriteriaConfig config, Composite parent, int style) {
		this(config, parent, style, true);
	}

	public LinkCriteriaWidget(LinkCriteriaConfig config, Composite parent, int style, boolean showCustom) {
		super(parent, style, config);
		setLayout(new FillLayout());
		createUI(this, showCustom);
	}

	private LinkCriteriaConfig getLinkConfig() {
		return (LinkCriteriaConfig) getEditingConfig();
	}

	private void createUI(Composite parent, boolean showCustom) {

		Composite c = new Composite(parent, SWT.FILL);
		c.setLayout(new GridLayout(1, false));

		//
		// -- Toolbar with checks and buttons
		//
		if(showCustom) {
			Composite tools = new Composite(c, SWT.NULL);
			tools.setLayout(new GridLayout(6, false));
			tools.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));

			Button b = new Button(tools, SWT.CHECK);
			b.setText(Messages.getString("LinkCriteriaUI.Advanced.label"));
			b.setSelection(getLinkConfig().getAdvancedLinkMode());
			b.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
				}

				public void widgetSelected(SelectionEvent e) {
					getLinkConfig().setAdvancedLinkMode(((Button) e.getSource()).getSelection());
					updateStack();
				}

			});
		}

		stack = new Composite(c, SWT.NULL);
		stack.setLayout(new StackLayout());
		stack.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		createLinkItemComponent(stack);
		createLinkScriptEditor(stack);

		if (getLinkConfig().getAdvancedLinkMode() && showCustom) {
			((StackLayout) stack.getLayout()).topControl = scriptForm;
		} else {
			((StackLayout) stack.getLayout()).topControl = frm;
		}
		stack.layout();
	}

	private void createLinkItemComponent(Composite parent) {

		frm = createForm(parent, null);
		frm.setLayout(new FillLayout());

		Composite tools = new Composite(frm.getHead(), SWT.NONE);
		tools.setLayout(new GridLayout(2, false));

		Button addButton = getFormToolKit().createButton(tools, Messages.getString("LinkCriteriaUI.toolbar.Add.label"), SWT.PUSH);
//		Button addButton = new Button(tools, SWT.PUSH);
//		addButton.setText(Messages.getString("LinkCriteriaUI.toolbar.Add.label"));
		addButton.setToolTipText(Messages.getString("LinkCriteriaUI.toolbar.Add.tooltip"));
		addButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				try {
					LinkCriteriaItem lc = getLinkConfig().newCriteria(null);
					new LinkItemWidget(content, lc);
					content.layout(true);
					updateMinSize();
				} catch (Exception err) {
					EclipseAppender.logerror(err.toString(), err, getShell());
				}
			}
		});

		Button b = new Button(tools, SWT.CHECK);
		b.setText(Messages.getString("LinkCriteriaUI.MatchAny.label"));
		b.setToolTipText(Messages.getString("BranchingConfig.matchany.tooltip"));
		b.setSelection(getLinkConfig().getMatchAny());
		b.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				getLinkConfig().setMatchAny(((Button) e.getSource()).getSelection());
			}
		});

		frm.setHeadClient(tools);

		frm.getBody().setLayout(new FillLayout());

		body = new ScrolledComposite(frm.getBody(), SWT.V_SCROLL | SWT.H_SCROLL);
		body.setLayout(new FillLayout());
		body.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				updateMinSize();
			}
		});
		body.setBackground(frm.getBody().getBackground());

		content = new Composite(body, SWT.NONE);
		content.setLayout(new GridLayout(1, false));
		content.setBackground(body.getBackground());

		for (Object str : getLinkConfig().getCriteriaNames()) {
			new LinkItemWidget(content, getLinkConfig().getCriteria(str));
		}

		body.setContent(content);
		body.setExpandHorizontal(true);
		body.setExpandVertical(true);
		updateMinSize();
	}

	/**
	 * Update the minimum size so the parent container can refresh scroll bars.
	 */
	private void updateMinSize() {
		content.layout();
		body.setMinSize(content.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		body.layout(true);
	}

	private class LinkItemWidget extends Composite implements MetamergeConfigChangeListener {

		private LinkCriteriaItem lci;
		private Combo attribute;
		private Combo oper;
		private Combo value;
		private ConnectorConfig cc = null;
		private boolean batchChange = false;
		private boolean updatingAttribute = false;
		private Button delete;
		private Button enable;
		private SchemaConfig sc = null;

		public LinkItemWidget(Composite parent, LinkCriteriaItem item) {
			super(parent, 0);
			this.lci = item;
			setLayout(new GridLayout(6, false));
			setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
			setBackground(parent.getBackground());
			cc = (ConnectorConfig) Utils.getParentConfig(item, ConnectorConfig.class);
			createUI();
			layout();

			// Update combo with schema changes
			if (cc != null)
				cc.addListener(this);
		}

		@Override
		public void dispose() {
			if (cc != null)
				cc.removeListener(this);
			super.dispose();
		}

		/**
		 * Create the UI for a link criteria item.
		 */
		private void createUI() {
			// -- Attribute Name
			attribute = new Combo(this, SWT.DROP_DOWN);
			if (cc != null) {
				sc = cc.getSchema(Utils.isInputConnector(cc));
				setAttributeValues();
			}
			attribute.setLayoutData(new GridData(150, SWT.DEFAULT));
			attribute.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					if (!updatingAttribute && !attribute.getText().equals(lci.getAttribute()))
						if (wantToBreakInheritance(lci, InternalSchema.LC_ATTRIBUTE)) {
							lci.setAttribute(attribute.getText());
							setControlColor(attribute, lci, InternalSchema.LC_ATTRIBUTE);
							delete.setEnabled(true);
						} else {
							setAttributeValues();
						}
				}
			});
			attribute.setToolTipText(Messages.getString("LinkCriteriaWidget.attributeToolTip"));
			Utils.setName(attribute, "LinkCriteriaWidget.Attribute.name");

			new TextEditorContextMenu(attribute);

			// -- Operator
			oper = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
			for (String str : OPERATORS)
				oper.add(str);
			setOperValue();
			oper.setLayoutData(new GridData(60, SWT.DEFAULT));
			oper.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (wantToBreakInheritance(lci, InternalSchema.LC_OPERATOR)) {
						lci.setOper(OPERATOR_VALUES[oper.getSelectionIndex()]);
						setControlColor(oper, lci, InternalSchema.LC_OPERATOR);
						delete.setEnabled(true);
					} else {
						setOperValue();
					}
				}
			});
			oper.setToolTipText(Messages.getString("LinkCriteriaWidget.operToolTip"));
			Utils.setName(attribute, "LinkCriteriaWidget.Operator.name");

			// -- Value
			value = new Combo(this, SWT.DROP_DOWN);
			value.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			setValueValue();
			value.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					String newValue = value.getText();
					if (!newValue.equals(lci.getValue())) {
						if (wantToBreakInheritance(lci, InternalSchema.LC_VALUE)) {
							lci.setValue(newValue);
							setControlColor(value, lci, InternalSchema.LC_VALUE);
							delete.setEnabled(true);
						} else {
							setValueValue();
						}
					}
				}
			});

			value.addFocusListener(new FocusAdapter() {
				@Override
				public void focusGained(FocusEvent e) {
					// update the list every time we get focus
					AssemblyLineConfig alc = (AssemblyLineConfig) Utils.getParentConfig(getLinkConfig(), AssemblyLineConfig.class);
					if (alc != null) {
						WorkEntryAttributesProvider wep = new WorkEntryAttributesProvider( cc != null ? cc.getShortName() : null);
						wep.inputChanged(null, null, alc);
						List<String>current = Arrays.asList(value.getItems());
						for (String name : wep.getSortedAttributes())
							if (current.indexOf("$" + name) < 0)
								value.add("$" + name);
					}
					super.focusGained(e);
				}
			});

			final String valueToolTip = Messages.getString("BranchConditionWidget.valueToolTip");
			value.setToolTipText(valueToolTip);
			Utils.setName(attribute, "LinkCriteriaWidget.Value.name");
			new TextEditorContextMenu(value);

			// -- This button brings up the param subst editor
			Button ps = new Button(this, SWT.PUSH);
			ps.setImage(Activator.getImage("Evaluate"));
			ps.setToolTipText(Messages.getString("AddCaseUI.PSE.tooltip"));
			ps.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					String str = ParameterSubstitutionWidget.openPSDialog(getShell(),
							lci, InternalSchema.LC_VALUE,
							valueToolTip, Messages.getString("BranchConditionWidget.valueLabel"));
					if (str != null && wantToBreakInheritance(lci, InternalSchema.LC_VALUE)) {
						lci.setValue(str);
						setValueValue();
					}
				}
			});

			// -- This button deletes or restores this criteria item
			delete = new Button(this, SWT.PUSH);
			if (lci.getInheritsFrom() == null) {
				delete.setText(Messages.getString("LinkCriteriaUI.toolbar.Delete.label"));
			} else {
				delete.setText(Messages.getString("LinkCriteriaWidget.Restore.label"));
				delete.setToolTipText(Messages.getString("LinkCriteriaWidget.Restore.tooltip"));
				delete.setEnabled(lci.getData().size() > 0);
			}
			delete.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (lci.getInheritsFrom() == null) {
						if (!MessageDialog.openConfirm(getShell(),
								Messages.getString("miadmin.menu.Object.DeleteItem.label"),								
								lci.getAttribute() + " " + lci.getOper() + " " + lci.getValue()))
							return;
						getLinkConfig().removeCriteria(lci.getShortName());
						dispose();
						content.layout(true);
						updateMinSize();
					} else {
						if (!MessageDialog.openConfirm(getShell(), 
								Messages.getString("LinkCriteriaWidget.Restore.label"),
								Messages.getString("LinkCriteriaWidget.Restore.tooltip")))
							return;
						lci.removeParameter(InternalSchema.LC_ATTRIBUTE);
						lci.removeParameter(InternalSchema.LC_OPERATOR);
						lci.removeParameter(InternalSchema.LC_VALUE);
						lci.removeParameter(InternalSchema.ENABLED);
						setAttributeValues();
						setOperValue();
						setValueValue();
						enable.setSelection(lci.getEnabled());
						delete.setEnabled(false);
					}
				}
			});
			
			// -- Enabled checkbox
			enable = new Button(this, SWT.CHECK);
			enable.setToolTipText(Messages.getString("LinkCriteriaWidget.Enabled.tooltip"));
			enable.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					lci.setEnabled(enable.getSelection());
					delete.setEnabled(true);
				}
			});
			enable.setSelection(lci.getEnabled());
		}

		private void setControlColor(Control control, LinkCriteriaItem lci, String attrName) {
			if (lci.hasParameter(attrName))
				control.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
			else
				control.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLUE));
		}

		private void setAttributeValues() {
			updatingAttribute = true;
			attribute.removeAll();
			if (sc != null) {
				List<String> itemNames = new ArrayList<String>();
				for (String name : sc.getItemNames()) {
					if(!SchemaEditor.SCHEMA_DESIGN_NAME.equals(name))
						addSchemaItemNames(itemNames, sc.getItem(name));
				}

				for (String name : itemNames)
					attribute.add(name);
			}
			attribute.setText((lci.getAttribute() == null ? "" : lci.getAttribute().toString()));
			updatingAttribute = false;
			setControlColor(attribute, lci, InternalSchema.LC_ATTRIBUTE);
		}

		private void setOperValue() {
			for (int i = 0; i < OPERATOR_VALUES.length; i++) {
				if (OPERATOR_VALUES[i].equals(lci.getOper()))
					oper.select(i);
			}
			setControlColor(oper, lci, InternalSchema.LC_OPERATOR);
		}

		private void setValueValue() {
			value.setText((lci.getValue() == null ? "" : lci.getValue().toString()));
			setControlColor(value, lci, InternalSchema.LC_VALUE);
		}


		public void configurationChanged(final MetamergeConfigChange changeEvent) {
			if (isDisposed()) {
				if (cc != null)
					cc.removeListener(this);
				return;
			}
			getDisplay().asyncExec( new Runnable() {
				public void run() {
					configurationChangedAsync(changeEvent);
				}
			});
		}

		public void configurationChangedAsync(MetamergeConfigChange changeEvent) {
			if (isDisposed()) {
				if (cc != null)
					cc.removeListener(this);
				return;
			}

			int op = changeEvent.getOperation();
			if (op == MetamergeConfigChange.BEGIN_CHANGES) {
				batchChange = true;
				return;
			} else if (op == MetamergeConfigChange.END_CHANGES) {
				batchChange = false;
				if (changeEvent.getSource() == sc) {
					setAttributeValues();
				}
				return;
			} else if (batchChange) {
				return;
			}

			if (changeEvent.getSource() == sc) {
				updatingAttribute = true;
				switch (changeEvent.getOperation()) {
				case MetamergeConfigChange.MCC_REMOVE:
					int index = attribute.indexOf("" + changeEvent.getKey());
					if (index != -1)
						attribute.remove(index);
					break;
				case MetamergeConfigChange.MCC_SET:
					attribute.add("" + changeEvent.getKey());
					break;
				}
				attribute.setText((lci.getAttribute() == null ? "" : lci.getAttribute().toString()));
				updatingAttribute = false;
			}
		}
	}

	/**
	 * This creates the javascript editor
	 *
	 * @param parent
	 */
	private void createLinkScriptEditor(Composite parent) {
		//
		// -- Advanced link editor
		//
		scriptForm = getFormToolKit().createForm(parent);
		getFormToolKit().decorateFormHeading(scriptForm);
		scriptForm.setText(Messages.getString("wizard.name.3"));
		scriptForm.getBody().setLayout(new FillLayout());

		linkEditor = new SimpleTextEditor(scriptForm.getBody(), SWT.BORDER, getEditingConfig());
		linkEditor.setText(getLinkConfig().getAdvancedLinkCriteria());
		linkEditor.getDocument().addDocumentListener(new IDocumentListener() {
			public void documentAboutToBeChanged(DocumentEvent event) {
			}

			public void documentChanged(DocumentEvent event) {
				getLinkConfig().setAdvancedLinkCriteria(event.getDocument().get());
			}
		});
	}

	@Override
	public void dispose() {
		if (scriptForm != null) {
			scriptForm.dispose();
			scriptForm = null;
		}
		super.dispose();
	}

	private void updateStack() {
		if (getLinkConfig().getAdvancedLinkMode())
			((StackLayout) stack.getLayout()).topControl = scriptForm;
		else
			((StackLayout) stack.getLayout()).topControl = frm; // table.getControl();
		stack.layout();
	}

	private boolean wantToBreakInheritance(LinkCriteriaItem item, String name) {
		if (item.hasParameter(name))
			return true;
		
		BaseConfiguration inh = item.getInheritsFrom();
		while (inh instanceof LinkCriteriaItem) {
			if (inh.hasParameter(name))
				break;
			inh = inh.getInheritsFrom();
		}
		if (inh == null)
			return true;

		String[] buttons;
		
		MetamergeConfig inhMC = inh.getMetamergeConfig();
		if (inhMC instanceof MetamergeConfigCE || inhMC instanceof TDIConfigurationFile) {
			buttons = new String[] { IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL,
					Messages.getString("ConfigBinding.EditOriginal") };
		} else {
			buttons = new String[] { IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL };
		}

		MessageDialog dialog = new MessageDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), Messages
				.getString("ConfigBinding.BreakInh.Title"), null, Messages.getString("EditorUI.breakInheritance"), MessageDialog.QUESTION,
				buttons, 0);
		int i = dialog.open();

		if (i == 0)
			return true;
		else if (i == 2)
			Utils.openEditorFor(inh);

		return false;
	}
	
	public static void addSchemaItemNames(List<String> list, SchemaItemConfig sic) {
		if (sic == null)
			return;
		list.add(Utils.getScriptName(sic));
		for (BaseConfiguration child : sic.getChildSchemaList().getConfigurations(null)) {
			if (child instanceof SchemaItemConfig)
				addSchemaItemNames(list, (SchemaItemConfig) child);
		}
	}
}
