/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.wizards;

import java.util.Hashtable;

import javax.naming.Context;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.ibm.di.config.base.NamespaceConfigImpl;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.NamespaceConfig;
import com.ibm.tdi.eclipse.Messages;

public class NewIncludeWizard extends Wizard {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private Text name;
	private Text url;
	private Text password;
	private Label message;
	
	private String nameSpace = "";
	private String theURL = "";
	private String thePassword = "";
	private String theMessage = "";

	private final static String OK_MESSAGE = Messages.getString("NewIncludeWizard.OK");
	
	public NewIncludeWizard() {
		super();
		setWindowTitle(Messages.getString("NewIncludeWizard.Title"));
	}

	@Override
	public boolean performFinish() {
		return true;
	}

	@Override
	public void addPages() {
		super.addPages();
		addPage(new NameAndLibPage("1")); //$NON-NLS-1$
	}

	@Override
	public boolean canFinish() {
		
		return theURL.length() > 0 && nameSpace.length() > 0 &&
		("".equals(theMessage) || OK_MESSAGE.equals(theMessage));
	}
	
	public NamespaceConfig getNamespaceConfig() throws Exception {
		NamespaceConfig nc = new NamespaceConfigImpl();
		nc.setURL(theURL);
		nc.setName(MetamergeConfig.DEFAULT_NAMESPACE_FOLDER + "/" + nameSpace);
		if (thePassword.length() > 0)
			nc.setParameter(Context.SECURITY_CREDENTIALS, thePassword);
		return nc;

	}
	
	private String verifyNamespace() {
		try {
			NamespaceConfig nc = getNamespaceConfig();
			String url = nc.getURL();
			if (url == null || url.length() == 0)
				return Messages.getString("NewIncludeWizard.NoURL");
			MetamergeConfigFactory.removeNamespace(url);
			Hashtable<String, Object> env = new Hashtable<String, Object>();

			env.put(Context.PROVIDER_URL, url);

			Object o = nc.getParameter(Context.SECURITY_CREDENTIALS);
			if (o != null)
				env.put(Context.SECURITY_CREDENTIALS, o);

			env.put(MetamergeConfigFactory.MC_CREATE, "false");

			MetamergeConfigFactory.getInstance(env);
			MetamergeConfigFactory.removeNamespace(url);
			return OK_MESSAGE;
		} catch (Exception err) {
			return err.toString();
		}
	}
	
	private String selectFile() {
		FileDialog fd = new FileDialog(getShell(), SWT.OPEN);
		fd.setFilterExtensions(new String[]{"*.xml", "*.*"});
		return fd.open();
	}

	private class NameAndLibPage extends WizardPage {

		public NameAndLibPage(String pageName) {
			super(pageName);
			setTitle(Messages.getString("NewIncludeWizard.1")); //$NON-NLS-1$
			setDescription(Messages.getString("NewIncludeWizard.description")); //$NON-NLS-1$
		}
		
		public void createControl(Composite parent) {
			Composite c = new Composite(parent, SWT.NONE);
			c.setLayout(new GridLayout(3,false));
			
			// namespace
			new Label(c, SWT.LEAD).setText(Messages.getString("IncludesWidget.3")); //$NON-NLS-1$
			name = new Text(c, SWT.BORDER);
			GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
			gd.horizontalSpan = 2;
			name.setLayoutData(gd);
			
			// URL
			new Label(c, SWT.LEAD).setText(Messages.getString("IncludesWidget.4")); //$NON-NLS-1$
			url = new Text(c, SWT.BORDER);
			url.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
			Button b = new Button(c, SWT.PUSH);
			b.setText(Messages.getString("NewIncludeWizard.select"));
			b.setToolTipText(Messages.getString("NewIncludeWizard.select.tooltip"));
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					String s = selectFile();
					if (s!= null) {
						url.setText(s);
					}
				}
			});

			// Password
			new Label(c, SWT.LEAD).setText(Messages.getString("ImportConfigWizard.4")); //$NON-NLS-1$
			password = new Text(c, SWT.BORDER);
			password.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
			password.setToolTipText(Messages.getString("ImportConfigWizard.5"));
			password.setEchoChar('*');
			gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
			gd.horizontalSpan = 2;
			password.setLayoutData(gd);
		
			setControl(c);
			ModifyListener ml = new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					// Got to get the text before the widget is disposed
					nameSpace = name.getText();
					theURL = url.getText();
					thePassword = password.getText();
					theMessage = "";
					message.setText(theMessage);
					getContainer().updateButtons();
				}
			};
			name.addModifyListener(ml);
			url.addModifyListener(ml);
			password.addModifyListener(ml);
			
			Button verify = new Button(c, SWT.PUSH);
			message = new Label(c, SWT.LEAD);
			message.setLayoutData(gd);
			message.setText(theMessage);
			verify.setText(Messages.getString("NewIncludeWizard.verify.label"));
			verify.setToolTipText(Messages.getString("NewIncludeWizard.verify.tooltip"));
			verify.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					theMessage = verifyNamespace();
					message.setText(theMessage);
					getContainer().updateButtons();
				}
			});
		}
		
	}

}
