/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.wizards;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.wizards.pages.ConnectorConfigPage;
import com.ibm.tdi.eclipse.wizards.pages.DiscoverSchemaPage;
import com.ibm.tdi.eclipse.wizards.pages.LinkCriteriaPage;
import com.ibm.tdi.eclipse.wizards.pages.ParserConfigPage;
import com.ibm.tdi.eclipse.wizards.pages.SelectComponentPage;
//import com.ibm.tdi.eclipse.wizards.pages.SelectNameAndLocationPage;

public class NewComponentWizard extends Wizard implements INewWizard {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private BaseConfiguration obj;
	private SelectComponentPage main;
//	private SelectNameAndLocationPage location;

	public NewComponentWizard() {
		main = new SelectComponentPage("Main");
//		location = new SelectNameAndLocationPage("Location");
	}

	@Override
	public boolean performFinish() {
		obj = null;
		try {
			obj = getSelectedObject();
			return true;
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e);
//			EclipseAppender.showError(location.getName(), e, getShell());
			return false;
		}
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	@Override
	public void addPages() {
		super.addPages();
		addPage(main);
//		addPage(location);
//		location.setSelection(selection);
//		main.addSelectionChangedListener(new ISelectionChangedListener() {
//			public void selectionChanged(SelectionChangedEvent event) {
//				location.setComponentName(main.getName());
//			}
//		});
		addPage(new ConnectorConfigPage("Connector"));
		addPage(new ParserConfigPage("Parser"));
		addPage(new LinkCriteriaPage("Link"));
		addPage(new DiscoverSchemaPage("Discover"));
	}
	

	public BaseConfiguration getSelectedObject() throws Exception {
		if(obj != null)
			return obj;
		
		BaseConfiguration b = main.getSelectedObject();
		if(b == null)
			return null;
		
		Object ns = MetamergeConfigFactory.getNamespaceFor(b);
		BaseConfiguration cb;
		if(ns != null) {
			cb = b.getClass().newInstance();
			cb.setInheritsFromRef(ns + ":/" + b.getName());
		} else {
			cb = (BaseConfiguration) b.getClone();
		}
//		cb.setName(location.getName());
		cb.init();

		if(cb instanceof ConnectorConfig) {
			ConnectorConfig cc = (ConnectorConfig) cb;
	        cc.getConnectionConfig().setInheritsFromRef ( BaseConfiguration.INHERIT_PARENT );
	        cc.getParserConfig().setInheritsFromRef ( BaseConfiguration.INHERIT_PARENT );
	        // D919 - inherit everything but Delta settings
	        cc.getSchema(ConnectorConfig.SCHEMA_INPUT).setInheritsFromRef ( BaseConfiguration.INHERIT_PARENT );
	        cc.getSchema(ConnectorConfig.SCHEMA_OUTPUT).setInheritsFromRef ( BaseConfiguration.INHERIT_PARENT );
	        cc.getAttributeMap(true).setInheritsFromRef ( BaseConfiguration.INHERIT_PARENT );
	        cc.getAttributeMap(false).setInheritsFromRef ( BaseConfiguration.INHERIT_PARENT );
	        cc.getLinkCriteria().setInheritsFromRef ( BaseConfiguration.INHERIT_PARENT );
	        cc.getHooks().setInheritsFromRef ( BaseConfiguration.INHERIT_PARENT );
			
			DiscoverSchemaPage dsp = (DiscoverSchemaPage) getPage("Discover");
			if (dsp != null)
				dsp.createAttributeMaps();			
		}

		cb.setupInheritanceChain();		

		return cb;
	}

	public void setNameValidator(IInputValidator validator) {
		main.setNameValidator(validator);
	}

	@Override
	public boolean canFinish() {
		return main.isPageComplete();
	}

}
