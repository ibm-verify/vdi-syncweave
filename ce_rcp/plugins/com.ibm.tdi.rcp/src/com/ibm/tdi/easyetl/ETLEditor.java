/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.easyetl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;

import com.ibm.di.config.base.ConnectorConfigImpl;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.tdi.easyetl.ALDebugger.ALDebuggerEvent;
import com.ibm.tdi.easyetl.ALDebugger.ALDebuggerEventListener;
import com.ibm.tdi.easyetl.widgets.ConnectorFlowWidget;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.editors.BaseEditor;

public class ETLEditor extends BaseEditor {
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public static final String EDITOR_ID = "com.ibm.tdi.easyetl.etleditor";
	private AssemblyLineConfig alc;
	private ConnectorConfig inputConnector;
	private ConnectorConfig outputConnector;
	private ConnectorFlowWidget inputWidget;
	private ConnectorFlowWidget outputWidget;
	private ColumnDataFlow dataFlow;

	public ETLEditor() {
	}

	@Override
	public void createPartControl(Composite parent) {
		ScrolledComposite fill = new ScrolledComposite(parent, SWT.H_SCROLL|SWT.V_SCROLL);
		fill.setLayout(new FillLayout());
		
		Composite container = new Composite(fill, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginLeft = 25;
		layout.marginRight = 25;
		layout.marginTop = 5;
		layout.marginBottom = 5;
		layout.verticalSpacing = 10;
		layout.horizontalSpacing = 30;
		container.setLayout(layout);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		// -- Title/heading
		Composite heading = new Composite(container, SWT.NONE);
		heading.setLayout(new GridLayout(1, false));
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		heading.setLayoutData(gd);

		Label title = new Label(heading, SWT.LEFT);
		title.setFont(JFaceResources.getBannerFont());
		title.setText(Messages.getString("ETLEditor.Simple.View"));
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		title.setLayoutData(gd);
		
		// -- Info
		Label info = new Label(heading, SWT.LEFT | SWT.WRAP);
		info.setText(Messages.getString("ETLEditor_info"));
		info.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
		info.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				
		// -- Input widget
		inputWidget = new ConnectorFlowWidget(container, SWT.NONE, inputConnector, this);
		inputWidget.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		// -- Output widget
		outputWidget = new ConnectorFlowWidget(container, SWT.NONE, outputConnector, this);
		outputWidget.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		// -- Data flow widget
		dataFlow = new ColumnDataFlow(container, 0, alc, this);
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		dataFlow.setLayoutData(gd);
		
		if(dataFlow.getViewMode() != ColumnDataFlow.VIEW_ETL) {
			inputWidget.dispose();
			outputWidget.dispose();
		} else {
			dataFlow.getDebugger().addEventListener(new ALDebuggerEventListener() {
				public void handleEvent(final ALDebuggerEvent event) {
					switch(event.getEvent()) {
					case ALDebuggerEvent.STARTED:
					case ALDebuggerEvent.TERMINATED:
						getSite().getShell().getDisplay().syncExec(new Runnable() {
							public void run() {
								inputWidget.setEditable(event.getEvent() == ALDebuggerEvent.TERMINATED);
								outputWidget.setEditable(event.getEvent() == ALDebuggerEvent.TERMINATED);
							}
						});
					}
				}
			});
		}
		
		fill.setContent(container);
		fill.setExpandHorizontal(true);
		fill.setExpandVertical(true);
		fill.setMinSize(container.computeSize(SWT.DEFAULT, SWT.DEFAULT));		
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);
		if (input instanceof IFileEditorInput) {
			IFile resource = ((IFileEditorInput) input).getFile();
			boolean create = !resource.exists();
			try {
				// -- check for zero contents
				if (!create) {
					InputStream is = resource.getContents();
					if (is.available() == 0)
						create = true;
					is.close();
				}
				if (create) {
					String def = "<MetamergeConfig version=\"7.0\"><AssemblyLine name=\"Default\"/></MetamergeConfig>";
					resource.setContents(new ByteArrayInputStream(def.getBytes()), 0, null);
				}
			} catch (Exception e) {
				throw new PartInitException(e.toString(), e);
			}
		}
		super.init(site, input);
		try {
			alc = (AssemblyLineConfig) getTDIConfiguration();
			if(alc.getEntryFeedComponents().size() == 0) {
				ConnectorConfigImpl cc = new ConnectorConfigImpl();
				cc.init();
				cc.setName("Input");
				cc.setMode(ConnectorConfig.ITERATOR_MODE);
				cc.getConnectionConfig().setInheritsFromRef(BaseConfiguration.INHERIT_PARENT);
				cc.getParserConfig().setInheritsFromRef(BaseConfiguration.INHERIT_PARENT);
				alc.getEntryFeedComponents().addConfig(cc);
			}
			inputConnector = (ConnectorConfig) alc.getEntryFeedComponents().getConfig(0);
			
			if(alc.getDataFlowComponents().size() == 0) {
				outputConnector = new ConnectorConfigImpl();
				outputConnector.init();
				outputConnector.setName("Output");
				outputConnector.setMode(ConnectorConfig.ADDONLY_MODE);
				outputConnector.setState(ConnectorConfig.DISABLED_STATE);
				outputConnector.getConnectionConfig().setInheritsFromRef(BaseConfiguration.INHERIT_PARENT);
				outputConnector.getParserConfig().setInheritsFromRef(BaseConfiguration.INHERIT_PARENT);
				alc.getDataFlowComponents().addConfig(outputConnector);
			}
			outputConnector = (ConnectorConfig) alc.getDataFlowComponents().getConfig(0);
			
		} catch (Exception e) {
			throw new PartInitException(e.toString(), e);
		}
	}

	public void refreshTable() {
		dataFlow.setInput(alc);
	}
}
