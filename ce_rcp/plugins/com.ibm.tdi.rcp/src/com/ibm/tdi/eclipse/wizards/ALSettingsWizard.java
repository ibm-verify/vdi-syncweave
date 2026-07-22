/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.wizards;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.widget.ALSimulationWidget;
import com.ibm.tdi.eclipse.widget.FormWidget2;
import com.ibm.tdi.eclipse.widget.HooksWidget;
import com.ibm.tdi.eclipse.widget.LoggingWidget;
import com.ibm.tdi.eclipse.widget.OperationsWidget;

public class ALSettingsWizard extends Wizard {
	
	@SuppressWarnings("unused") //$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public final static String SETTINGS_PAGE = "Settings"; //$NON-NLS-1$
	public final static String LOG_PAGE = "Log"; //$NON-NLS-1$
	public final static String HOOKS_PAGE = "Hooks"; //$NON-NLS-1$
	public final static String OPERATIONS_PAGE = "Operations"; //$NON-NLS-1$
	public final static String SIMULATION_PAGE = "Simulation"; //$NON-NLS-1$
	
	private AssemblyLineConfig alc;
	private String startPage;
	
	public ALSettingsWizard(AssemblyLineConfig alc, String startPage) {
		super();
		this.alc = alc;
		this.startPage = startPage;
	}

	@Override
	public boolean performFinish() {
		return true;
	}

	@Override
	public void addPages() {
		addPage(new SettingsPage(SETTINGS_PAGE));
		addPage(new LogPage(LOG_PAGE));
		addPage(new HooksPage(HOOKS_PAGE));
		addPage(new OperationsPage(OPERATIONS_PAGE));
		addPage(new SimulationPage(SIMULATION_PAGE));
	}

	private class HooksPage extends WizardPage {

		private HooksWidget widget;

		public HooksPage(String pageName) {
			super(pageName);
			setTitle(Messages.getString("assemblyline.tabs.hooks.label")); //$NON-NLS-1$
			setDescription(Messages.getString("assemblyline.tabs.hooks.tooltip")); //$NON-NLS-1$
		}

		public void createControl(Composite parent) {
			widget = new HooksWidget(alc, parent, SWT.NONE);
			setControl(widget);
		}

		@Override
		public void dispose() {
			if(widget != null)
				widget.dispose();
			
			super.dispose();
		}
	}
	
	private class LogPage extends WizardPage {
		public LogPage(String pageName) {
			super(pageName);
			setTitle(Messages.getString("assemblyline.tabs.logging.label")); //$NON-NLS-1$
			setDescription(Messages.getString("assemblyline.tabs.logging.tooltip")); //$NON-NLS-1$
		}

		public void createControl(Composite parent) {
			setControl(new LoggingWidget(parent, SWT.NONE, alc));
		}
	}
	
	private class SettingsPage extends WizardPage {
		public SettingsPage(String pageName) {
			super(pageName);
			setTitle(Messages.getString("assemblyline.tabs.settings.label")); //$NON-NLS-1$
			setDescription(Messages.getString("assemblyline.tabs.settings.tooltip")); //$NON-NLS-1$
		}

		public void createControl(Composite parent) {
			try {
				setControl(
						new FormWidget2(parent, SWT.TITLE, alc.getSettings(), MetamergeConfigFactory.STDFORMS_NAMESPACE + ":/Forms/AL Settings") //$NON-NLS-1$
				);
			} catch (Exception e) {
				e.printStackTrace();
			}
			setTitle(Messages.getString("assemblyline.tabs.settings.tooltip")); //$NON-NLS-1$
		}
	}

	private class OperationsPage extends WizardPage {
		public OperationsPage(String pageName) {
			super(pageName);
			setTitle(Messages.getString("assemblyline.tabs.callreturn.label")); //$NON-NLS-1$
			setDescription(Messages.getString("assemblyline.tabs.callreturn.tooltip")); //$NON-NLS-1$
		}

		public void createControl(Composite parent) {
			try {
				setControl(
						new OperationsWidget(parent, SWT.TITLE, alc)
				);
			} catch (Exception e) {
				e.printStackTrace();
			}
			setTitle(Messages.getString("assemblyline.tabs.callreturn.label")); //$NON-NLS-1$
		}
	}
	
	private class SimulationPage extends WizardPage {
		public SimulationPage(String pageName) {
			super(pageName);
			setTitle("Configure Simulation");
		}

		public void createControl(Composite parent) {
			try {
				setControl(
						new ALSimulationWidget(parent, SWT.TITLE, alc)
				);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public IWizardPage getStartingPage() {
		if(startPage != null)
			return getPage(startPage);
		else
			return super.getStartingPage();
	}

	@Override
	public String getWindowTitle() {
		return Messages.getString("assemblyline.tabs.settings.tooltip"); //$NON-NLS-1$
	}
}
