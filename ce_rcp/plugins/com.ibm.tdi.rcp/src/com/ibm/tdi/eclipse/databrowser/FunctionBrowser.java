/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.databrowser;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.entry.Entry;
import com.ibm.di.fc.FunctionInterface;
import com.ibm.di.function.SystemFunctions;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.editors.BaseEditor;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.providers.MetamergeFolderContentProvider;
import com.ibm.tdi.eclipse.widget.RawConnectorWidget;

public class FunctionBrowser extends DataBrowser {
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private RawConnectorWidget functionWidget;

	public FunctionBrowser(Composite parent, int style, BaseConfiguration editingConfig, BaseEditor editor) {
		super(parent, style, editingConfig, editor);
	}

	public FunctionBrowser(Composite parent, int style, BaseConfiguration editingConfig) {
		super(parent, style, editingConfig);
	}

	public FunctionBrowser(Composite parent, int style) {
		super(parent, style);
	}

	@Override
	protected void doInitialDiscovery() throws Exception {
	}

	@Override
	protected IContentProvider getNavigatorContentProvider() {
		return new MetamergeFolderContentProvider();
	}

	@Override
	protected Job getDiscoverJob() {
		return new Job("") {
			protected IStatus run(IProgressMonitor monitor) {
				return Status.OK_STATUS;
			}
		};
	}

	public void resetConnectorForm() {
		if (functionWidget != null) {
			functionWidget.dispose();
		}
		
		functionWidget = new RawConnectorWidget(getDetailsTabFolder(), SWT.NONE, ((FunctionConfig) getEditingConfig()).getFunctionConfig(), true);
		setDetailsTabContents(Messages.getString("ConnectorTreeUI.Localized.Connection"), functionWidget); //$NON-NLS-1$
		((FunctionConfig) getEditingConfig()).getFunctionConfig().addListener(this);

	}
	
	@Override
	protected Object getNavigatorInput() {
		return null;
	}

	@Override
	protected IBaseLabelProvider getNavigatorLabelProvider() {
		return null;
	}

	@Override
	protected GetNextJob getGetNextJob() {
		return new GetNextJob(this) {

			private FunctionInterface function = null;
			private Object entry;
			
			@Override
			public void close() {
				if(function != null) {
					try {
						function.terminate();
					} catch (Exception e) {
						EclipseAppender.logerror(e.toString(), e);
					}
					function = null;
				}
			}

			@Override
			public Entry getNextEntry() {
				if(entry instanceof Entry)
					return (Entry) entry;
				else
					return null;
			}

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					if(function == null) {
						FunctionConfig config = (FunctionConfig) getBrowser().getEditingConfig();
						function = SystemFunctions.loadFunction(config);
						function.initialize(null);
					}
					entry = function.perform(null);
				} catch (Exception e) {
					return EclipseAppender.statusException(e);
				}
				return Status.OK_STATUS;
			}
			
		};
	}

	@Override
	protected void handleNavigatorSelectionChanged(SelectionChangedEvent event) {
	}

	@Override
	public void dispose() {
		if (functionWidget != null) {
			functionWidget.dispose();
			functionWidget = null;
		}

		super.dispose();
	}

}
