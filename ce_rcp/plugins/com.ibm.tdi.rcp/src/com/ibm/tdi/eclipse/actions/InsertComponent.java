/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

import com.ibm.di.config.interfaces.ALMappingConfig;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.tdi.eclipse.ConfigUtils;
import com.ibm.tdi.eclipse.TDI;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.validators.IllegalCharValidator;
import com.ibm.tdi.eclipse.wizards.ConnectorConfigWizard;

public class InsertComponent extends TDIAction {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private Shell shell;
	private BaseConfiguration config;
	private String[] pages;
	private String startPage;
	private boolean feedFlowEnabled;
	private BaseConfiguration component = null;
	private ContainerConfig location = null;

	public InsertComponent() {
	}
	
	public InsertComponent(Shell shell, BaseConfiguration config) {
		this.shell = shell;
		this.config = config;
	}

	public ContainerConfig getLocation() {
		return location;
	}

	public void setLocation(ContainerConfig location) {
		this.location = location;
	}

	public InsertComponent(Shell shell, BaseConfiguration config, String startPage, String[] pages) {
		this.config = config;
		this.shell = shell;
		this.pages = pages;
		this.startPage = startPage;
	}

	public InsertComponent(Shell shell, BaseConfiguration configuration, boolean feedFlowEnabled) {
		this(shell, configuration);
		this.feedFlowEnabled = feedFlowEnabled;
	}

	@Override
	public void run() {
		ConnectorConfigWizard ccw = new ConnectorConfigWizard(null, startPage);
		if(pages != null)
			ccw.setShowPages(pages);
		
		if(location != null)
			ccw.setLocation(location);
		else
			ccw.setLocation(config);
		
		ccw.setFeedFlowEnabled(feedFlowEnabled);

		AssemblyLineConfig alc = (AssemblyLineConfig) Utils.getParentConfig(config, AssemblyLineConfig.class);
		if (alc != null)
			ccw.setNameValidator(new IllegalCharValidator());

		WizardDialog wiz = new WizardDialog(shell, ccw);
		wiz.addPageChangingListener(ccw);
		wiz.setPageSize(600, 400);
		if(wiz.open() != Window.OK)
			return;
		
		component = ccw.getConfigObject();
		try {
			if(component.getInheritsFrom() == null && ! (component instanceof ALMappingConfig))
				component = ConfigUtils.createInheritedComponent(config.getMetamergeConfig(), component);
			
			String str = ccw.getComponentName().trim();
			
			if(alc != null) {
				int i = 1;
				while(alc.getComponent(str) != null) {
					str = ccw.getComponentName().trim() + "_" + i;
					i++;
				}
			}
			component.setName(str);
			
		} catch (Exception e1) {
			EclipseAppender.logerror(e1.toString(), e1, shell);
			return;
		}
		//
		// Server mode connectors go into feed
		// Iterator mode connectors go into feed if feed/flow is enabled and target is AL,
		//  Component not added to branch after Loop creation
		if(component instanceof ConnectorConfig && alc != null && 
				(location == null || location == alc.getEntryFeedComponents())) {
			ConnectorConfig conn = (ConnectorConfig) component;
			// Try to guess the mode...
			if (Utils.isEntryFeedMode(Utils.verifyMode(conn))) {
				alc.getEntryFeedComponents().addConfig(conn);
				return;			
			}
		}
		
		
		BaseConfiguration loc = ccw.getLocation();
		ContainerConfig cc = null;
		if (loc instanceof ContainerConfig)
			cc = (ContainerConfig) loc;

		if (cc == null && loc != null)
			cc = (ContainerConfig) Utils.getParentConfig(loc.getParent(), ContainerConfig.class);
		
		// Nothing into feed
		if (alc != null && (cc == null || cc == alc.getEntryFeedComponents()))
			cc = alc.getDataFlowComponents();
		
		if (cc == null)
			return;
		
		switch(ccw.getInsertionPoint()) {
		case TDI.INSERT_BEFORE:
			cc.insertConfig(component, cc.indexOf(config));
			break;
		case TDI.INSERT_AFTER:
			cc.insertConfig(component, cc.indexOf(config)+1);
			break;
		case TDI.INSERT_INTO:
			cc.addConfig(component);
		}
	}

	/**
	 * Returns the component that was created by this action.
	 * 
	 * @return  the component that was created by this action.
	 */
	public BaseConfiguration getComponent() {
		return component;
	}

}
