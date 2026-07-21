/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.wizards.pages;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.log.EclipseAppender;

public class FileNamePage extends WizardNewFileCreationPage {
	@SuppressWarnings("unused") //$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	
	private BaseConfiguration configObject;

	public FileNamePage(String pageName, IStructuredSelection selection, String extension) {
		super(pageName, selection);
		if(extension != null)
			setFileExtension(extension.startsWith(".") ? extension.substring(1) : extension); //$NON-NLS-1$
		setTitle(Messages.getString("FileNamePage.2")); //$NON-NLS-1$
	}

	@Override
	protected InputStream getInitialContents() {
		try {
			BaseConfiguration b = getConfigObject();
			b.setName(getFileName());
			TDIConfigurationFile tdiConfigurationFile = (TDIConfigurationFile) b.getMetamergeConfig();
			if(tdiConfigurationFile == null)
				tdiConfigurationFile = new TDIConfigurationFile();
			tdiConfigurationFile.setDefaultConfigObject(getCompName(), b);
			return new ByteArrayInputStream(tdiConfigurationFile.toXML().getBytes());
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e);
			return super.getInitialContents();
		}
	}

	private String getCompName() {
		String name = getFileName();
		if(name.indexOf(".") == -1) //$NON-NLS-1$
			return name;
		else
			return name.substring(0, name.lastIndexOf(".")); //$NON-NLS-1$
	}

	public BaseConfiguration getConfigObject() {
		return configObject;
	}

	public void setConfigObject(BaseConfiguration configObject) {
		this.configObject = configObject;
	}

}
