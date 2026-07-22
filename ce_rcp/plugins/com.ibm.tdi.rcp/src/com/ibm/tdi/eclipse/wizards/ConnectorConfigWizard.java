/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.wizards;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.IPageChangingListener;
import org.eclipse.jface.dialogs.PageChangingEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;

import com.ibm.di.config.interfaces.ALMappingConfig;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.BranchingConfig;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.LoopConfig;
import com.ibm.di.config.interfaces.ScriptConfig;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.TDI;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.wizards.pages.AddConditionPage;
import com.ibm.tdi.eclipse.wizards.pages.ConnectorConfigPage;
import com.ibm.tdi.eclipse.wizards.pages.ConnectorPage;
import com.ibm.tdi.eclipse.wizards.pages.ParserConfigPage;
import com.ibm.tdi.eclipse.wizards.pages.SelectComponentPage;
import com.ibm.tdi.eclipse.wizards.pages.SelectNameAndLocationPage;

/**
 * This wizard is used to configure an existing component (e.g. no save to workspace)
 */
public class ConnectorConfigWizard extends Wizard implements IPageChangingListener {
	@SuppressWarnings("unused") //$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public static final String DISCOVER_PAGE = "Discover"; //$NON-NLS-1$
	public static final String LINK_PAGE = "Link"; //$NON-NLS-1$
	public static final String PARSER_PAGE = "Parser"; //$NON-NLS-1$
	public static final String CONNECTOR_PAGE = "Connector"; //$NON-NLS-1$
	public static final String LOCATION_PAGE = "Location"; //$NON-NLS-1$
	public static final String MAIN_PAGE = "Main"; //$NON-NLS-1$
	private static final String CONDITION_PAGE = "Condition"; //$NON-NLS-1$

	private BaseConfiguration cc;
	private String startPage;
	private String[] showPages;
	private BaseConfiguration location;

	private ISelection updateSelection;

	private boolean feedFlowEnabled;
	
	private IInputValidator validator;
	
	private Composite pageContainer;
	
	public ConnectorConfigWizard(BaseConfiguration cc, String startPage) {
		super();
		this.cc = cc;
		this.startPage = startPage;
		setWindowTitle(Messages.getString("general.insert.tooltip")); //$NON-NLS-1$
	}

	@Override
	public boolean performFinish() {
		BaseConfiguration bc = getConfigObject();
		
		// Check if a file source was chosen - e.g. inheritsFromRef is not system
//		if(bc.getInheritsFromRef() != null && bc.getInheritsFromRef().indexOf(":") == -1) {
//			TDIConfigurationFile cfg = (TDIConfigurationFile) getLocation().getMetamergeConfig();
//			IFile path = cfg.getProject().getFile(new Path(TDINature.RESOURCES_FOLDER + "/" + bc.getInheritsFromRef()));
//			if(path != null && path.exists()) {
//				System.out.println("Add reference to: " + path.getLocation().toOSString());
//			}
//		}
		
		// Only one page for these config types
		if(bc instanceof BranchingConfig || bc instanceof LoopConfig ||
				bc instanceof ScriptConfig || bc instanceof ALMappingConfig)
			return true;

//		if(! useDataBrowser(bc) ) {
//			DiscoverSchemaPage dsp = (DiscoverSchemaPage) getPage(DISCOVER_PAGE);
//			if (dsp.getControl() != null)
//				dsp.createAttributeMaps();
//		}
		return true;
	}

	@Override
	public void addPages() {
		if(startPage == null) {
			addPage(MAIN_PAGE);
//			addPage(LOCATION_PAGE);
		}
		
		if(showPages == null)
			showPages = new String[]{CONNECTOR_PAGE, PARSER_PAGE, DISCOVER_PAGE, LINK_PAGE, CONDITION_PAGE};

		for(String str : showPages)
			addPage(str);
		
		hookPages();
	}
	
	@Override
   public void createPageControls(Composite pageContainer) {
		this.pageContainer = pageContainer;
		// Create control only for first page
		//the other controls will be created later in getNextPage()
		IWizardPage page = getStartingPage();
		if ( page != null)
			page.createControl(pageContainer);
     }

	private void addPage(String page) {
		if(MAIN_PAGE.equals(page))
			addPage(new SelectComponentPage(page));
		else if(LOCATION_PAGE.equals(page))
			addPage(new SelectNameAndLocationPage(page));
		else if(CONNECTOR_PAGE.equals(page))
			addPage(new ConnectorConfigPage(page));
		else if(PARSER_PAGE.equals(page))
			addPage(new ParserConfigPage(page));
		// If Link Pages are added back in, also need to modify ConnectorConfigPage.canFlipToNextPage()
//		else if(LINK_PAGE.equals(page))
//			addPage(new LinkCriteriaPage(page));
//		else if(DISCOVER_PAGE.equals(page))
//			addPage(new DiscoverSchemaPage(page));
		else if(CONDITION_PAGE.equals(page))
			addPage(new AddConditionPage(page));
		
		if(LOCATION_PAGE.equals(page))
			((SelectNameAndLocationPage)getPage(LOCATION_PAGE)).setSelection(location);
	}

	public void hookPages() {
		if(cc != null && getPage(LOCATION_PAGE) != null)
			((SelectNameAndLocationPage)getPage(LOCATION_PAGE)).setSelection(cc);
		if(getPage(MAIN_PAGE) instanceof SelectComponentPage) {
			SelectComponentPage scp = (SelectComponentPage) getPage(MAIN_PAGE);
			scp.addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
//					((SelectNameAndLocationPage)getPage(LOCATION_PAGE)).setComponentName(
//							((SelectComponentPage)getPage(MAIN_PAGE)).getComponentName());
					updatePageConfigurations(event.getSelection());
				}
			});
			scp.setNameValidator(validator);
		}
	}

	protected void updatePageConfigurations(ISelection selection) {
		updateSelection = selection;
	}

	public BaseConfiguration getConnectorConfig() {
		if (cc == null && updateSelection instanceof IStructuredSelection ) {
			Object o = ((IStructuredSelection)updateSelection).getFirstElement();
			if ( o instanceof ConnectorConfig )
				return (ConnectorConfig) o; 
		}
		return cc;
	}
	
	public BaseConfiguration getConfigObject() {
		if(cc != null)
			return cc;
		else
			return ((SelectComponentPage)getPage(MAIN_PAGE)).getSelectedObject();
	}
	public void setConfigObject(BaseConfiguration cc) {
		this.cc = cc;
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		BaseConfiguration cc = getConfigObject();
		String p = page.getName();
		String next = null;
		
		// Only one page for these config types
		if(cc instanceof LoopConfig ||	cc instanceof ScriptConfig || cc instanceof ALMappingConfig)
			return null;
		
		if(cc instanceof BranchingConfig && p.equals(MAIN_PAGE)) {
			BranchingConfig bc = (BranchingConfig) cc;
			if(bc.getBranchType() == BranchingConfig.BRANCH_IF || bc.getBranchType() == BranchingConfig.BRANCH_ELSEIF)
				next = CONDITION_PAGE;
		} else if (p.equals(CONNECTOR_PAGE)) {
			if(Utils.hasParserRequirements(cc))
				next = PARSER_PAGE;
			else if (Utils.hasLinkRequirements(cc))
				next = LINK_PAGE;
		} else if (p.equals(PARSER_PAGE)) {
			if (Utils.hasLinkRequirements(cc))
				next = LINK_PAGE;
		} else if (p.equals(MAIN_PAGE)) {
			next = CONNECTOR_PAGE;
		} else if (p.equals(LOCATION_PAGE)) {
			next = CONNECTOR_PAGE;
		}
		
		if(next == null)
			return null;
		
		IWizardPage iwp = getPage(next);
		if (iwp != null && iwp.getControl() == null) {
			iwp.createControl(pageContainer);
		}
		return iwp;
	}

	@Override
	public IWizardPage getPreviousPage(IWizardPage page) {
		BaseConfiguration cc = getConfigObject();
		String p = page.getName();
		String prev = null;
		if(p.equals(DISCOVER_PAGE) || p.equals(LINK_PAGE) ) {
			if (Utils.hasParserRequirements(cc))
				prev = PARSER_PAGE;
			else
				prev = CONNECTOR_PAGE;
			
		} else if(p.equals(PARSER_PAGE)) {
			prev = CONNECTOR_PAGE;
			
		} else if(p.equals(CONNECTOR_PAGE)) {
//			prev = LOCATION_PAGE;
			prev = MAIN_PAGE;
			
		} else if(p.equals(MAIN_PAGE))
			prev = null;
		
		else if(p.equals(LOCATION_PAGE))
			prev = MAIN_PAGE;
		
		else if(p.equals(CONDITION_PAGE))
			prev = MAIN_PAGE;
		
		if(prev != null)
			return getPage(prev);
		else
			return null;
	}

	@Override
	public IWizardPage getStartingPage() {
		if(startPage == null)
			return super.getStartingPage();
		else
			return getPage(startPage);
	}

	public int getInsertionPoint() {
		return TDI.INSERT_INTO;
//		SelectNameAndLocationPage page = (SelectNameAndLocationPage) getPage(LOCATION_PAGE);
//		return page.getInsertionPoint();
	}

	public BaseConfiguration getLocation() {
		SelectNameAndLocationPage page = (SelectNameAndLocationPage) getPage(LOCATION_PAGE);
		if(page != null)
			return page.getLocation();
		
		if(location instanceof AssemblyLineConfig)
			return ((AssemblyLineConfig)location).getDataFlowComponents();
		else
			return location;
	}

	public void setShowPages(String[] showPages) {
		this.showPages = showPages;
	}

	public String getComponentName() {
		//return ((SelectNameAndLocationPage)getPage(LOCATION_PAGE)).getComponentName();
		return ((SelectComponentPage)getPage(MAIN_PAGE)).getComponentName();
	}
	
//	public String getComponentMode() {
//		return ((SelectNameAndLocationPage)getPage(LOCATION_PAGE)).getMode();
//	}

	public void setLocation(BaseConfiguration config) {
		this.location = config;
	}
	
	public AssemblyLineConfig getAssemblyLineConfig() {
		return (AssemblyLineConfig) Utils.getParentConfig(getLocation(), AssemblyLineConfig.class);
	}

	public boolean isFeedFlowLocation() {
		return (location instanceof AssemblyLineConfig) && feedFlowEnabled;
	}

	public void handlePageChanging(PageChangingEvent event) {
		WizardPage page = (WizardPage) event.getTargetPage();
		if (updateSelection instanceof IStructuredSelection) {
			Object o = ((IStructuredSelection)updateSelection).getFirstElement();
			if ( o instanceof ConnectorConfig && page instanceof ConnectorPage)
				((ConnectorPage)page).setConfiguration((ConnectorConfig)o);
			if ( o instanceof BranchingConfig && page instanceof AddConditionPage)
				((AddConditionPage)page).setConfiguration((BranchingConfig)o);
		}
	}

	public void setFeedFlowEnabled(boolean feedFlowEnabled) {
		this.feedFlowEnabled = feedFlowEnabled;
		
	}

//	private boolean useDataBrowser(BaseConfiguration bc) {
//		if (bc instanceof FunctionConfig || Utils.isAssemblyLine(bc))
//			return false;
//		if (! (bc instanceof ConnectorConfig))
//			return false;
//		RawConnectorConfig rcc = ((ConnectorConfig)bc).getConnectionConfig();
//		if( rcc != null && "com.ibm.di.connector.HTTPServerConnector".equals(rcc.getJavaClass()))
//				return false;
//		return true;
//	}
//	@Override
//	public String getWindowTitle() {
//		return Messages.getString("general.insert.tooltip");
//	}

	public void setNameValidator(IInputValidator validator) {
		this.validator = validator;
	}
}
