/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.widget;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.Form;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.tdi.eclipse.ConfigUtils;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.editors.BaseEditor;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.util.TDIToolBar;
import com.ibm.tdi.eclipse.wizards.NewAttributeMapWizard;

public class TitledAttributeMapWidget extends BaseWidget {
    @SuppressWarnings("unused")
    private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

    private Button inhButton;

	private TDIToolBar bar;
    
    public TitledAttributeMapWidget(Composite parent, int style, ConnectorConfig bc, BaseEditor editor) {
		super(parent, style, bc, editor);
		setLayout(new FillLayout());
		Form f = createForm(this, null);
		bar = new TDIToolBar(f);
		bar.setText(bc.getShortName());
		bar.setImage(bc);
		addInheritsFromControls(bar);
		f.getBody().setLayout(new FillLayout());
		new AttributeMapWidget(f.getBody(), style, bc, WorkMapWidget.MAP_MODE_INPUT, editor);
	}
    
	private void addInheritsFromControls(TDIToolBar bar) {
		bar.addLabel(SWT.LEAD).setText(Messages.getString("HooksWidget.0"));
		final TDIToolBar toolbar = bar;
		inhButton = bar.add(new Action() {
			@Override
			public String getText() {
				return getEditingConfig().getInheritsFromRef();
			}

			@Override
			public void run() {
				NewAttributeMapWizard wiz = new NewAttributeMapWizard();
				wiz.init(null, new StructuredSelection(getEditingConfig()));
				wiz.setChooseFileName(false);
				wiz.setShowTypes(true);
				wiz.setIncludeNullSelection(true);

				WizardDialog dlg = new WizardDialog(getShell(), wiz);
				if (dlg.open() == Window.OK) {
					try {
						String ref = ConfigUtils.SYSTEM_ATTRIBUTE_MAP;
						if(wiz.getConfigObject().getInheritsFrom() != null)
							ref = wiz.getConfigObject().getInheritsFromRef();
						if (ConfigUtils.SYSTEM_ATTRIBUTE_MAP.equals(ref))
							ref = BaseConfiguration.INHERIT_NONE;
						getEditingConfig().setInheritsFromRef(ref);
						inhButton.setText(ref);
						toolbar.layout(true, true);
					} catch (Exception e) {
						EclipseAppender.logerror(e.toString(), e, getShell());
					}					
				}
			}
		});
	}


}
