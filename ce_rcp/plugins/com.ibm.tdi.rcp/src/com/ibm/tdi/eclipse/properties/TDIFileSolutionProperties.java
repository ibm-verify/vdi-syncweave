/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

import com.ibm.di.function.SystemFunctions;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.TDI;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.natures.TDINature;

public class TDIFileSolutionProperties extends PropertyPage {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final String DEFAULT_SERVER_TITLE = Messages.getString("ServerWidget.1") + ":";
	private static final String DEFAULT_VALUE = "Default.tdiserver";

	private Combo defaultServerText;

	/**
	 * Constructor for SamplePropertyPage.
	 */
	public TDIFileSolutionProperties() {
		super();
	}

	private void addDefaultServerCombo(Composite parent) {
		Composite composite = createDefaultComposite(parent);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setLayout(new GridLayout(3,false));

		// Label for owner field
		Label ownerLabel = new Label(composite, SWT.NONE);
		ownerLabel.setText(DEFAULT_SERVER_TITLE);

		// Server text field
		defaultServerText = new Combo(composite, SWT.DROP_DOWN|SWT.READ_ONLY); 
		GridData gd = new GridData();
		gd.verticalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalSpan = 2;
		defaultServerText.setLayoutData(gd);
		
		try {
			IProject project = Utils.getTDIServersProject(true);
			for(IResource res : project.members()) {
				if(res instanceof IFile) {
					IFile file = (IFile) res;
					if("tdiserver".equals(file.getFileExtension()))
						defaultServerText.add(file.getName());
				}
			}
		} catch (Exception e1) {
			EclipseAppender.logerror(e1.toString(), e1);
		}
		
		// Populate server text field
		try {
			String server = ((IResource) getElement()).getPersistentProperty(TDI.PROJECT_PREF_SERVER_QNAME);
			if(server == null || server.trim().length() == 0)
				server = DEFAULT_VALUE;
			
			if(defaultServerText.indexOf(server) == -1)
				defaultServerText.add(server);
			
			defaultServerText.select(defaultServerText.indexOf(server));
		} catch (Exception e) {
			defaultServerText.setText(DEFAULT_VALUE);
		}
		
		// Linked resource text field
		Label linkLabel = new Label(composite, SWT.NONE);
		linkLabel.setText(Messages.getString("ProjectPage.server.link"));
		final Text linkValue = new Text(composite, SWT.BORDER|SWT.SINGLE);
		try {
			String link = ((IResource) getElement()).getPersistentProperty(TDINature.TDI_EXTERNAL_CONFIG);
			if(link != null)
				linkValue.setText(link);
		} catch (Exception e) {
			SystemFunctions.doNothing();
		}
		linkValue.setToolTipText(Messages.getString("ProjectPage.server.link.info"));
		
		linkValue.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				try {
					((IResource)getElement()).setPersistentProperty(TDINature.TDI_EXTERNAL_CONFIG, ((Text)e.widget).getText());
				} catch (Exception err) {
					EclipseAppender.logerror(err.toString(), err, getShell());
				}
			}
		});
		linkValue.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Button linkButton = new Button(composite, SWT.PUSH);
		linkButton.setText(Messages.getString("NewIncludeWizard.select"));
		linkButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FileDialog fd = new FileDialog(getShell(), SWT.OPEN);
				String path = fd.open();
				if(path != null) {
					linkValue.setText(path);
				}
			}
		});
		
	}

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);

		addDefaultServerCombo(composite);
		return composite;
	}

	private Composite createDefaultComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);

		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(data);

		return composite;
	}

	protected void performDefaults() {
		// Populate the owner text field with the default value
		defaultServerText.setText(DEFAULT_VALUE);
	}
	
	public boolean performOk() {
		// store the value in the owner text field
		try {
			((IResource) getElement()).setPersistentProperty(TDI.PROJECT_PREF_SERVER_QNAME,
				defaultServerText.getText());
		} catch (CoreException e) {
			EclipseAppender.logerror(e.toString(), e, getShell());
			return false;
		}
		return true;
	}

}
