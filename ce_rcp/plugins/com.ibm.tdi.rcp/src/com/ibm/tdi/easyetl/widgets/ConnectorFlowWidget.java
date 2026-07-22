/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.easyetl.widgets;

import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.FormConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.tdi.easyetl.ETLEditor;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.editors.BaseEditor;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.widget.BaseWidget;
import com.ibm.tdi.eclipse.wizards.NewConnectorWizard;

public class ConnectorFlowWidget extends BaseWidget {
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static String[] TYPES_REF = new String[] { 
		"system:/Connectors/ibmdi.FileSystem", //$NON-NLS-1$
		"system:/Connectors/ibmdi.LDAP", //$NON-NLS-1$
		"system:/Connectors/ibmdi.Database" //$NON-NLS-1$
	};

	private ConnectorConfig cc;
	private Combo connectorTitle;
	private Map<String,String> titleToRef = new Hashtable<String,String>();

	private Button configureButton;

	private Button simulate;

	public ConnectorFlowWidget(Composite parent, int style, ConnectorConfig connectorConfig, BaseEditor editor) {
		super(parent, style, connectorConfig, editor);
		setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
		this.cc = connectorConfig;
		createUI();
	}

	private void createUI() {

		setLayout(new FillLayout());

		Group group = new Group(this, SWT.SHADOW_NONE);
		RowLayout layout = new RowLayout(SWT.VERTICAL);
		layout.marginLeft = 10;
		layout.marginRight = 10;
		layout.marginBottom = 5;
		layout.marginTop = 5;
		group.setLayout(layout);
		group.setBackground(getBackground());
		
		Label title = new Label(group, SWT.LEFT);
		title.setBackground(getBackground());
		if (Utils.isEntryFeedConnector(cc))
			title.setText(Messages.getString("ConnectorFlowWidget_source"));
		else
			title.setText(Messages.getString("ConnectorFlowWidget_target"));

		Composite hc = new Composite(group, SWT.NONE);
		hc.setBackground(group.getBackground());
		GridLayout glayout = new GridLayout(Utils.isOutputConnector(cc) ? 4 : 3, false);
		glayout.horizontalSpacing = 15;
		hc.setLayout(glayout);

		Label label = new Label(hc, SWT.LEFT);
		label.setBackground(hc.getBackground());
		label.setText(Messages.getString("ColumnDataFlow_type_label"));

		connectorTitle = new Combo(hc, SWT.DROP_DOWN | SWT.READ_ONLY);
		for (String str : TYPES_REF) {
			try {
				BaseConfiguration cfg = (BaseConfiguration) MetamergeConfigFactory.lookup(null, str);
				connectorTitle.add(getConnectorTitle(cfg));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		connectorTitle.add(Messages.getString("Util.SelectComponent.2") + "...");

		connectorTitle.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = connectorTitle.getSelectionIndex();
				if (index < 0)
					return;
				if(!Utils.isInputConnector(cc)) {
					// -- clear link criteria
					for(Object obj : cc.getLinkCriteria().getCriteriaNames())
						cc.getLinkCriteria().removeCriteria(obj);

					// -- default is add only until user adds a link criteria
					cc.setMode(ConnectorConfig.ADDONLY_MODE);
				}

				try {
					if(index >= connectorTitle.getItemCount() - 1) {
						updateConnectorTitle();
						NewConnectorWizard wiz = new NewConnectorWizard();
						WizardDialog dlg = new WizardDialog(getShell(), wiz);
						wiz.setModeRequested(false);
						wiz.setChangeConnectionOnlyOption(false);
						wiz.setChooseFileName(false);
						wiz.setConfigObject(cc);
						wiz.setWindowTitle(Messages.getString("Util.SelectComponent.2"));

						if(Utils.isInputConnector(cc)) {
							wiz.setModeFilter(new String[]{ConnectorConfig.ITERATOR_MODE});
						} else {
							wiz.setModeFilter(new String[]{ConnectorConfig.ADDONLY_MODE, ConnectorConfig.UPDATE_MODE});
						}

						if(dlg.open() != Window.OK)
							return;

						cc.updateInheritsFrom(wiz.getConfigObject().getInheritsFromRef());
					} else {
						String ref;
						if (index < TYPES_REF.length)
							ref = TYPES_REF[index];
						else
							ref = titleToRef.get(connectorTitle.getText());
						cc.updateInheritsFrom(ref);
					}

					updateConnectorTitle();
					clearSchemaAndMap(cc);
					((ETLEditor) getEditor()).refreshTable();
					if(simulate != null && simulate.getSelection()) {
						simulate.setSelection(false);
						cc.setState(ConnectorConfig.ENABLED_STATE);
					}
					openConfigureDialog();
				} catch (Exception err) {
					EclipseAppender.logerror(err.toString(), err, getShell());
				}
			}
		});

		// connectorTitle.setFont(JFaceResources.getBannerFont());

		configureButton = new Button(hc, SWT.PUSH);
		configureButton.setText(Messages.getString("ConnectorFlowWidget_configure"));
		configureButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				openConfigureDialog();
			}
		});
		
		if(Utils.isOutputConnector(cc)) {
			simulate = new Button(hc, SWT.CHECK);
			simulate.setText(Messages.getString("ConnectorFlowWidget.simulate"));
			simulate.setToolTipText(Messages.getString("ConnectorFlowWidget.simulate.tooltip"));
			simulate.setSelection(ConnectorConfig.DISABLED_STATE.equals(cc.getState()));
			simulate.setBackground(hc.getBackground());
			simulate.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					cc.setState(simulate.getSelection() ? ConnectorConfig.DISABLED_STATE : ConnectorConfig.ENABLED_STATE);
				}
			});
		}
		
		updateConnectorTitle();
	}

	protected void openConfigureDialog() {
		if (openWizardFor(this, cc)) {
			updateConnectorTitle();
			((ETLEditor) getEditor()).refreshTable();
		}
	}

	private void updateConnectorTitle() {
		String str = getConnectorTitle(cc);
		if(cc.getInheritsFromRef() != null) {
			if(connectorTitle.indexOf(str) == -1) {
				connectorTitle.add(str, connectorTitle.getItemCount() - 1);
				titleToRef.put(str, cc.getInheritsFromRef());
			}
			connectorTitle.setText(str);
			configureButton.setEnabled(true);
		} else {
			configureButton.setEnabled(false);
		}
	}

	protected void clearSchemaAndMap(ConnectorConfig conn) {
		for(String str : conn.getSchema(true).getItemNames())
			conn.getSchema(true).removeItem(str);
		for(String str : conn.getSchema(false).getItemNames())
			conn.getSchema(false).removeItem(str);
		
		for(String str : conn.getAttributeMap(true).getAttributeNames())
			conn.getAttributeMap(true).removeAttributeMapItem(str);

		// -- Do not remove the output attributes. A user may choose to discover the output schema
		// -- from a database and then turn around to write the same attributes to a file.
	}

	public boolean openWizardFor(Composite parent, ConnectorConfig config) {
		
		final ConnectorConfig cc = config;
		
		Dialog dlg = new Dialog(parent.getShell()) {

			private ConnectorWidget wid;
			private Button b;

			@Override
			protected Control createDialogArea(Composite parent) {
				Composite c = (Composite) super.createDialogArea(parent);
				wid = new ConnectorWidget(c, cc);
				wid.setLayoutData(new GridData(GridData.FILL_BOTH));
				wid.addCompleteListener(new Listener() {
					public void handleEvent(Event event) {
						getDisplay().syncExec(new Runnable() {
							public void run() {
								if(wid.canClose()) {
									getShell().setDefaultButton(getButton(OK));
								} else {
									getShell().setDefaultButton(b);
								}
							}
						});
					}
				});
				getShell().setText(connectorTitle.getText());
				return c;
			}
			
			@Override
			protected Point getInitialSize() {
				return new Point(750,800);
			}

			@Override
			protected void createButtonsForButtonBar(Composite parent) {
				super.createButtonsForButtonBar(parent);
				b = createButton(parent, 99, Messages.getString("ColumnDataFlow_connect"), false);
				b.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						final Job job = wid.readConnectorEntry();
						IRunnableWithProgress reader = new IRunnableWithProgress() {
							public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
								int timeout = 30;
								monitor.beginTask(Messages.getString("ColumnDataFlow_connecting"), timeout);
								while(job.getState() == Job.WAITING || job.getState() == Job.RUNNING) {
									if(monitor.isCanceled()) {
										job.cancel();
										break;
									}
									if(timeout-- < 0)
										break;
									monitor.worked(1);
									Thread.sleep(1000);
								}
								monitor.done();
							}
						};
						
						// -- job may complete quickly so we check if it's still waiting/running
						if(job.getState() == Job.WAITING || job.getState() == Job.RUNNING) {
							ProgressMonitorDialog pmd = new ProgressMonitorDialog(getShell());
							try {
								pmd.run(true, true, reader);
								IStatus status = job.getResult();
								if(status != null && !status.isOK())
									EclipseAppender.logerror(status.getException().toString(), job.getResult().getException(), getShell());
							} catch (Exception e1) {
								EclipseAppender.logerror(e1.toString(), e1, getShell());
							}
						}
					}
				});
				if(wid.canClose())
					getShell().setDefaultButton(getButton(OK));
				else
					getShell().setDefaultButton(b);
			}

			@Override
			public boolean close() {
				if(wid != null)
					wid.dispose();
				return super.close();
			}

			@Override
			protected void okPressed() {
				if(wid != null)
					wid.mapAllAttributes();
				super.okPressed();
			}

			@Override
			protected int getShellStyle() {
				return super.getShellStyle() | SWT.RESIZE | SWT.MAX;
			}
		};
		return dlg.open() == Window.OK;
	}

	public String getConnectorTitle(BaseConfiguration element) {
		ConnectorConfig cc = (ConnectorConfig) element;
		String javaclass = Utils.getFormName(cc.getConnectionConfig());
		
		if(javaclass == null)
			return element.getShortName();
		
		if (javaclass.startsWith("@"))
			return Messages.getMessage("ConfigLabelPriver.reusing", javaclass.substring(1));
		
		try {
			FormConfig inf = (FormConfig) MetamergeConfigFactory.getNamespace(MetamergeConfigFactory.SYSTEM_NAMESPACE).lookup("Forms/" + javaclass);
			if(inf != null && inf.getTitle() != null) 
				return inf.getTitle();
			else
				return element.getShortName();
		} catch (Exception e) {
//			EclipseAppender.logerror(e.toString(), e);
			return element.getShortName();
		}
		
	}

	public void setEditable(boolean editable) {
		if(connectorTitle != null)
			connectorTitle.setEnabled(editable);
		if(configureButton != null)
			configureButton.setEnabled(editable);
		if(simulate != null)
			simulate.setEnabled(editable);
	}
}
