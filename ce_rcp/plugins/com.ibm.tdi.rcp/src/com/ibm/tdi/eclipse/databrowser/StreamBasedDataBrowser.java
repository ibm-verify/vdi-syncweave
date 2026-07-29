/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.databrowser;

import java.io.BufferedReader;
import java.io.StringWriter;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.progress.UIJob;

import com.ibm.di.config.base.BaseConfigurationImpl;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.ParserConfig;
import com.ibm.di.function.SystemFunctions;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.providers.ConfigLabelProvider;
import com.ibm.tdi.eclipse.providers.MetamergeFolderContentProvider;
import com.ibm.tdi.eclipse.widget.ParserWidget;

public class StreamBasedDataBrowser extends DataBrowser {

	@SuppressWarnings("unused")  //$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	
	private Label emptyContent = null;
	private ParserWidget widget = null;
	private BaseConfiguration noparser;

	public StreamBasedDataBrowser(Composite parent, int style, BaseConfiguration editingConfig) {
		super(parent, style, editingConfig);
	}

	@Override
	protected IContentProvider getNavigatorContentProvider() {
		return new MetamergeFolderContentProvider();
	}

	@Override
	protected IBaseLabelProvider getNavigatorLabelProvider() {
		ConfigLabelProvider provider = new ConfigLabelProvider();
		provider.setConsultingInfFiles(true);
		return provider;
	}

	@Override
	protected Object getNavigatorInput() {
		noparser = new BaseConfigurationImpl();
		try {
			noparser.setName(Messages.getString("ParserWidget.undefined"));
		} catch (Exception e1) {
			// Ignore this exception
			SystemFunctions.doNothing();
		}
		try {
			return new Object[]{
					noparser,
					MetamergeConfigFactory.getNamespace("system").lookup(MetamergeConfig.DEFAULT_PARSER_FOLDER) //$NON-NLS-1$
			};
		} catch (Exception e) {
			return e;
		}		
	}
	
	@Override
	protected void doInitialDiscovery() throws Exception {
		closeConnector();

		BufferedReader reader = null;
		
		// -- Some parsers eat the input before we get a chance to grab
		// -- the input stream. Use the dummy parser so we get to the stream.
		try {
			DummyParser parser = new DummyParser();
			if (getEditingConfig() instanceof ConnectorConfig) {
				String charSet = ((ConnectorConfig)getEditingConfig()).getParserConfig().getStringParameter("characterSet");
				if (charSet != null && charSet.length() > 0)
					parser.setParam("characterSet", charSet);
			}
			getConnectorInstance().setParser(parser);
			selectEntries();
			reader = parser.getReader();
		} catch (Exception e) {
			SystemFunctions.doNothing();
		}
		
		final StringWriter sw = new StringWriter();

		if(reader != null) {
			try {
				for (int i = 0; i < 5000 && reader.ready(); i++) {
					int ch = reader.read();
					if(ch == -1)
						break;
					else
						sw.write(ch);
				}
				reader.close();
				closeConnector();
				// -- reopen the connector
				selectEntries();
			} catch (Exception e) {
				sw.write(Utils.exceptionText(e));
			} finally {
				if(reader != null)
					reader.close();
				closeConnector();
			}
		}
		
		UIJob job = new UIJob("Refresh") { //$NON-NLS-1$

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if (isDisposed())
					return Status.OK_STATUS;
				ParserConfig pc = ((ConnectorConfig)getEditingConfig()).getParserConfig();
				if(pc != null) {
					if(pc.getJavaClass() == null || pc.getInheritsFrom() == null)
						getNavigator().setSelection(new StructuredSelection(noparser));
					else
						getNavigator().setSelection(new StructuredSelection(pc.getInheritsFrom()));
				} else {
					getNavigator().setSelection(new StructuredSelection(noparser));
				}
				setDetailsData(Messages.getString("StreamDataBrowser.data"), sw.toString(), true);
				return Status.OK_STATUS;
			}
			
		};
		job.schedule();
	}

	@Override
	void doConnect() {
		try {
			closeConnector();
			selectEntries();
		} catch (Exception e) {
			EclipseAppender.logerror(e.getMessage(), e);
		}
	}
	
	@Override
	protected void handleNavigatorSelectionChanged(SelectionChangedEvent event) {
		clearWidgets();
		Object sel = ((IStructuredSelection)event.getSelection()).getFirstElement();
		if(!(sel instanceof ParserConfig)) {
			emptyContent = new Label(getDetailsTabFolder(), SWT.NONE);
			setDetailsTabContents(Messages.getString("ConnectorTreeUI.Localized.Parser"), emptyContent); //$NON-NLS-1$
		} else {			
			// Must close connector since we read the stream first
			closeConnector();

			ParserConfig pc = (ParserConfig) sel;
			String ns = (String) MetamergeConfigFactory.getNamespaceFor(pc);
			String inheritFrom = ns + ":/" + pc.getName(); //$NON-NLS-1$
			ParserConfig current = ((ConnectorConfig)getEditingConfig()).getParserConfig();
			if(inheritFrom != null && !inheritFrom.equals(current.getInheritsFromRef())) {
				current.setInheritsFromRef(inheritFrom);
				try {
					current.setupInheritanceChain();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			widget = new ParserWidget(getDetailsTabFolder(), SWT.NONE, current);
			setDetailsTabContents(Messages.getString("ConnectorTreeUI.Localized.Parser"), widget, true); //$NON-NLS-1$
			
			// -- Reopen the 
			try {
				selectEntries();
			} catch (Exception e) {
				SystemFunctions.doNothing();
			}
		}
	}

	@Override
	protected String getNavigatorFormText() {
		return Messages.getString("Util.SelectComponent.3"); //$NON-NLS-1$
	}

	private void clearWidgets() {
		if(emptyContent != null)
			emptyContent.dispose();
		emptyContent = null;
		if(widget != null)
			widget.dispose();
		widget = null;
	}
	
	@Override
	public void dispose() {
		clearWidgets();
		super.dispose();
	}
	
}
