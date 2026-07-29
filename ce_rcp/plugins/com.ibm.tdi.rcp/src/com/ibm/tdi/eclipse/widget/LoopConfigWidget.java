/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.widget;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.LoopConfig;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.editors.BaseEditor;
import com.ibm.tdi.eclipse.providers.WorkEntryAttributesProvider;
import com.ibm.tdi.eclipse.util.TDIToolBar;
import com.ibm.tdi.eclipse.util.TextFieldController;

public class LoopConfigWidget extends BranchWidget {
	@SuppressWarnings("unused") //$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private LabelFieldWidget collectionWidget;

	private LoopConfig lc;
	private Combo loopInitOptions = null;
	private Combo workAttribute;

	public LoopConfigWidget(Composite parent, int style, LoopConfig lc) {
		this(parent, style, lc, null);
	}
	public LoopConfigWidget(Composite parent, int style, LoopConfig lc, BaseEditor editor) {
		super(parent, style, lc, editor);
	}

	@Override
	protected Control createComponent(Composite parent) {
		lc = (LoopConfig) getEditingConfig();
		Control c = null;
		switch (lc.getLoopType()) {
		case LoopConfig.LOOP_COLLECTION:
			getToolbar().setText(Messages.getString("LoopConfig.select.entry.label")); //$NON-NLS-1$
			c = createCollectionWidget(parent);
			break;
		case LoopConfig.LOOP_CONDITIONS:
			c = super.createComponent(parent);
			getToolbar().setText(Messages.getString("LoopConfig.select.conditions.label")); //$NON-NLS-1$
			return c;
		case LoopConfig.LOOP_CONNECTOR_FC:
			c = createConnectorWidget(parent);
			getToolbar().setText(lc.getShortName());
			break;
		}

		if (getToolbar() != null) {
			getToolbar().add(new Action() {
				public String getText() {
					return Messages.getString("LBL.CLOSE"); //$NON-NLS-1$
				}

				public String getActionDefinitionId() {
					return "com.ibm.tdi.rcp.quickeditor.close"; //$NON-NLS-1$
				}

				public void run() {
					getEditor().quickEdit(new StructuredSelection(getEditingConfig()));
				}
			});
		}
		if(c == null) {
			Label l = new Label(parent, SWT.LEFT);
			return l;
		}

		return c;
	}

	private Control createConnectorWidget(Composite parent) {
		Composite c = new Composite(parent, SWT.NULL);
		c.setLayout(new FillLayout());
		try {
			ConnectorWidget cw = new ConnectorWidget(c, SWT.NONE, getEditingConfig(), getEditor());
			toolBar = new TDIToolBar(cw.getForm());
			cw.getForm().setText(null);
			cw.getForm().setImage(null);
		} catch (Exception e) {
			return Utils.exceptionWidget(parent, e);
		}
		return c;
	}

	public void changeInitOption() {
		if (loopInitOptions != null) {
			int sel = loopInitOptions.getSelectionIndex();
			if (sel != lc.getInitConnectorOption())
				lc.setInitConnectorOption(sel);
		}
	}

	private Control createCollectionWidget(Composite parent) {
		if (collectionWidget != null)
			return collectionWidget;

		collectionWidget = new LabelFieldWidget(parent, SWT.NONE);
		collectionWidget.addDescription(Messages.getString("LoopConfigWidget.attributeloop")); //$NON-NLS-1$

		// -- Work Attribute Name
		workAttribute = collectionWidget.addCombo(Messages.getString("LoopConfigWidget.work"), SWT.DROP_DOWN | SWT.BORDER);
		Utils.setName(workAttribute, "LoopConfigWidget.work");
		setAttributeValues();

		workAttribute.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (!workAttribute.getText().equals(lc.getWorkAttributeName()))
					lc.setWorkAttributeName(workAttribute.getText());
			}
		});

		Text loopAttributeName = collectionWidget.addTextField(Messages.getString("LoopConfigWidget.loop"), "", SWT.SINGLE //$NON-NLS-1$ //$NON-NLS-2$
				| SWT.BORDER);
		loopAttributeName.setData("CONTROLLER", new TextFieldController(loopAttributeName, lc, "setLoopAttributeName()")); //$NON-NLS-1$ //$NON-NLS-2$

		return collectionWidget;
	}

	private void setAttributeValues() {
		String val = lc.getWorkAttributeName();
		AssemblyLineConfig alc = Utils.getParentConfig(lc, AssemblyLineConfig.class);
		if (alc != null) {
			WorkEntryAttributesProvider wep = new WorkEntryAttributesProvider( lc.getShortName());
			wep.inputChanged(null, null, alc);
			for (String name : wep.getSortedAttributes())
				workAttribute.add(name);
		}
		workAttribute.setText( val != null ? val : "");
	}
}
