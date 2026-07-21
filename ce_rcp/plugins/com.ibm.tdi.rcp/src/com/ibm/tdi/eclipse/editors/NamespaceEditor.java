/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.editors;

import java.util.Hashtable;

import javax.naming.Context;

import org.eclipse.jface.dialogs.MessageDialog;
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

import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.NamespaceConfig;
import com.ibm.di.function.SystemFunctions;
import com.ibm.tdi.eclipse.Messages;

public class NamespaceEditor extends BaseEditor {

	private Label message;
	private Text url;
	private Text password;
	private Composite composite;
	
	public NamespaceEditor() {
	}

	@Override
	public void createPartControl(Composite parent) {
		createUI(parent);
	}
	
	private void createUI(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(3,false);
		layout.marginHeight = 30;
		layout.marginWidth = 30;
		composite.setLayout(layout);
		
//		// namespace
//		new Label(c, SWT.LEAD).setText(Messages.getString("IncludesWidget.3")); //$NON-NLS-1$
//		name = new Text(c, SWT.BORDER);
//		name.setText(getNamespaceConfig().getShortName());
//		GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
//		gd.horizontalSpan = 2;
//		name.setLayoutData(gd);
//		name.addModifyListener(new ModifyListener() {
//			public void modifyText(ModifyEvent e) {
//				try {
//					getNamespaceConfig().setName(name.getText());
//				} catch (Exception e1) {
//					EclipseAppender.showError(e1.toString(), e1, getSite().getShell());
//				}
//			}
//		});
//		
		// URL
		new Label(composite, SWT.LEAD).setText(Messages.getString("IncludesWidget.4")); //$NON-NLS-1$
		url = new Text(composite, SWT.BORDER);
		url.setText(getNamespaceConfig().getURL() == null ? "" : getNamespaceConfig().getURL());
		url.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		url.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				getNamespaceConfig().setURL(url.getText());
			}
		});
		
		Button b = new Button(composite, SWT.PUSH);
		b.setText(Messages.getString("NewIncludeWizard.select"));
		b.setToolTipText(Messages.getString("NewIncludeWizard.select.tooltip"));
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String s = selectFile();
				if (s!= null) {
					url.setText(s);
					getNamespaceConfig().setURL(url.getText());
				}
			}
		});

		// Password
		new Label(composite, SWT.LEAD).setText(Messages.getString("ImportConfigWizard.4")); //$NON-NLS-1$
		password = new Text(composite, SWT.BORDER);
		password.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		password.setToolTipText(Messages.getString("ImportConfigWizard.5"));
		password.setEchoChar('*');
		String str = getNamespaceConfig().getStringParameter(Context.SECURITY_CREDENTIALS);
		if(str != null)
			password.setText(str);
		password.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				getNamespaceConfig().setParameter(Context.SECURITY_CREDENTIALS, password.getText());
			}
		});

		GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
		gd.horizontalSpan = 2;
		password.setLayoutData(gd);
	
		
		Button verify = new Button(composite, SWT.PUSH);
		message = new Label(composite, SWT.LEAD);
		message.setLayoutData(gd);
		verify.setText(Messages.getString("NewIncludeWizard.verify.label"));
		verify.setToolTipText(Messages.getString("NewIncludeWizard.verify.tooltip"));
		verify.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String msg = verifyNamespace();
				MessageDialog.openInformation(getSite().getShell(), Messages.getString("NewIncludeWizard.verify.label"), msg);
				message.setText(msg);
			}
		});
	}

	private String verifyNamespace() {
		try {
			NamespaceConfig nc = getNamespaceConfig();
			String url = nc.getURL();
			if (url == null || url.length() == 0)
				return Messages.getString("NewIncludeWizard.NoURL");
			
			try {
				if(getTDIConfigProject().getWorkspace().getRoot().getProject(url).exists())
					return Messages.getString("NewIncludeWizard.OK");
			} catch (Exception e) {
				SystemFunctions.doNothing();
			}
			
			MetamergeConfigFactory.removeNamespace(url);
			Hashtable<String, Object> env = new Hashtable<String, Object>();

			env.put(Context.PROVIDER_URL, url);

			Object o = nc.getParameter(Context.SECURITY_CREDENTIALS);
			if (o != null)
				env.put(Context.SECURITY_CREDENTIALS, o);

			env.put(MetamergeConfigFactory.MC_CREATE, "false");

			MetamergeConfigFactory.getInstance(env);
			MetamergeConfigFactory.removeNamespace(url);
		} catch (Exception err) {
			return err.toString();
		}
		return Messages.getString("NewIncludeWizard.OK");
	}
	
	private NamespaceConfig getNamespaceConfig() {
		return (NamespaceConfig) getTDIConfiguration();
	}

	private String selectFile() {
		FileDialog fd = new FileDialog(getSite().getShell(), SWT.OPEN);
		fd.setFilterExtensions(new String[]{"*.xml", "*.*"});
		return fd.open();
	}

	@Override
	public void setFocus() {
		if(composite != null)
			composite.setFocus();
	}

}
