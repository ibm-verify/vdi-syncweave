/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.editors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.server.RMIServerAPI;
import com.ibm.tdi.eclipse.server.RestServerAPI;

public class DashboardEditor extends BaseEditor {
	
	public final static String EDITOR_ID = "com.ibm.tdi.rcp.dashboard";

	private Browser browser;
	private String url;

	public DashboardEditor() {
		super();
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
		try {
			RMIServerAPI api = (RMIServerAPI) RestServerAPI.createInstance(getTDIConfigFile());
			String port = api.getSession().getJavaProperty("web.server.port");
			boolean ssl = Boolean.valueOf(api.getSession().getJavaProperty("web.server.ssl"));
			url = (ssl ? "https" : "http") + "://" + api.getSession().getServerInfo().getHostName() + ":" + port + "/dashboard";
		} catch (Exception e) {
			EclipseAppender.logerror(e.getLocalizedMessage(), e, site.getShell());
			url = getTDIConfiguration().getStringParameter(RMIServerAPI.TDI_ADDRESS);
			if(url.indexOf(":") != -1)
				url = url.substring(0, url.indexOf(":"));
			url = "http://" + url + ":1098/dashboard";
		}
		
		setPartName(url.substring(0, url.indexOf("/dashboard")));
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {
		browser = new Browser(parent, SWT.NONE);
		browser.setUrl(url);
	}

	@Override
	public void setFocus() {
		if(browser != null)
			browser.setFocus();
	}

}
