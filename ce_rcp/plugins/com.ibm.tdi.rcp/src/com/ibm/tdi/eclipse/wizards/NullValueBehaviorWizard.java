/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.wizards;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.AttributeMapItem;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.server.ServerConstants;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;

public class NullValueBehaviorWizard extends Wizard {
	@SuppressWarnings("unused") //$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	
	private BaseConfiguration config;
	protected String nullBehavior;
	protected String nullBehaviorValue;
	protected String nullDefinition;
	protected String nullDefinitionValue;
	
	public NullValueBehaviorWizard(BaseConfiguration config) {
		super();
		this.config = config;
		nullBehavior = config.getNullBehavior();
		if(nullBehavior == null || nullBehavior.length() == 0)
			nullBehavior = ServerConstants.NVB_BEHAVIOR[0];
		nullBehaviorValue = config.getNullBehaviorValue();
		nullDefinition = config.getNullDefinition();
		if(nullDefinition == null || nullDefinition.length() == 0)
			nullDefinition = ServerConstants.NVD_DEFINITION[0];
		nullDefinitionValue = config.getNullDefinitionValue();
	}

	@Override
	public boolean performFinish() {
		config.setNullBehavior(nullBehavior);
		config.setNullBehaviorValue(nullBehaviorValue);
		config.setNullDefinition(nullDefinition);
		config.setNullDefinitionValue(nullDefinitionValue);
		return true;
	}

	@Override
	public void addPages() {
		addPage(new NVBPage());
	}

	private class NVBPage extends WizardPage {

		private FormToolkit tk;
		private Text value;
		private Text ndvalue;

		public NVBPage() {
			super("NVB"); //$NON-NLS-1$
			//setTitle(Messages.getString("NullBehavior.popup.Title")); //$NON-NLS-1$
			String type = null;
			if(config instanceof AssemblyLineConfig) {
				type = Messages.getString("wizard.name.1") + ": " + config.getShortName();
			} else if (config instanceof AttributeMapConfig) {
				type = Messages.getString("wizard.name.8") + ": " + Utils.getParentConfig(config, ConnectorConfig.class).getShortName() + "." + config.getShortName();
			} else if (config instanceof AttributeMapItem) {
				ConnectorConfig cc = Utils.getParentConfig(config, ConnectorConfig.class);
				if(cc != null) {
					type = Messages.getString("LinkCriteriaUI.0.label") + ": " + cc.getShortName() + "." + config.getShortName();
				} else {
					ContainerConfig coc = Utils.getParentConfig(config, ContainerConfig.class);
					if(coc != null)
						type = Messages.getString("LinkCriteriaUI.0.label") + ": " + coc.getShortName() + "." + config.getShortName();
				}
			}

			if(type != null)
				setTitle(type);
			else
				setTitle(config.getShortName());
			
			setDescription(Messages.getString("NullBehavior.definition.Title"));
		}

		public void createControl(Composite parent) {
			
			tk = new FormToolkit(parent.getDisplay());
			Form frm = tk.createForm(parent);
			frm.getBody().setLayout(new GridLayout(2, true));

			Section s1 = tk.createSection(frm.getBody(), Section.TITLE_BAR);
			s1.setText(Messages.getString("NullBehavior.popup.Title")); //$NON-NLS-1$
			s1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			s1.setLayout(new FillLayout());

			Composite c1 = tk.createComposite(s1);
			c1.setLayout(new GridLayout(1, false));

			String[] items = new String[]{"Default", "Delete", "Null", "Empty", "Error", "Value"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			for(int i = 0; i < items.length; i++) {
				Button b = tk.createButton(c1, Messages.getString("NullBehavior.popup." + items[i] + ".label"), SWT.RADIO); //$NON-NLS-1$ //$NON-NLS-2$
				b.setData(ServerConstants.NVB_BEHAVIOR[i]);
				b.setSelection(ServerConstants.NVB_BEHAVIOR[i].equals(nullBehavior));
				b.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						nullBehavior = (String) ((Button)e.widget).getData();
						value.setEnabled(ServerConstants.NVB_BEHAVIOR[5].equals(nullBehavior)); //$NON-NLS-1$
						if(value.isEnabled())
							value.setFocus();
					}
				});
			}
			
			value = new Text(c1, SWT.BORDER);
			value.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					nullBehaviorValue = value.getText();
				}
			});
			value.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
			if(nullBehaviorValue != null)
				value.setText(config.getNullBehaviorValue());
			value.setEnabled(ServerConstants.NVB_BEHAVIOR[5].equals(nullBehavior)); //$NON-NLS-1$
			
			s1.setClient(c1);
			
			
			Section s2 = tk.createSection(frm.getBody(), Section.TITLE_BAR);
			s2.setText(Messages.getString("NullBehavior.definition.Title")); //$NON-NLS-1$
			s2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			s2.setLayout(new FillLayout());

			Composite c2 = tk.createComposite(s2);
			c2.setLayout(new GridLayout(1, false));
		
			for(String str:ServerConstants.NVD_DEFINITION) {
				Button b = tk.createButton(c2, Messages.getString("NullBehavior.definition." + str + ".label"), SWT.RADIO); //$NON-NLS-1$ //$NON-NLS-2$
				b.setData(str);
				b.setSelection(str.equals(nullDefinition));
				b.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						nullDefinition = (String) ((Button)e.widget).getData();
						ndvalue.setEnabled(ServerConstants.NVD_DEFINITION[4].equals(nullDefinition)); //$NON-NLS-1$
						if(ndvalue.isEnabled())
							ndvalue.setFocus();
					}
				});
			}
			
			ndvalue = new Text(c2, SWT.BORDER);
			ndvalue.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					nullDefinitionValue = ndvalue.getText();
				}
			});
			ndvalue.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
			if(nullDefinitionValue != null)
				ndvalue.setText(nullDefinitionValue);
			ndvalue.setEnabled(ServerConstants.NVD_DEFINITION[4].equals(nullDefinition)); //$NON-NLS-1$
			
			s2.setClient(c2);
			
			setControl(frm);
		}

		@Override
		public void dispose() {
			if(tk != null)
				tk.dispose();
		}
		
	}

	@Override
	public String getWindowTitle() {
		return Messages.getString("NullBehavior.popup.Title"); //$NON-NLS-1$
	}
}
