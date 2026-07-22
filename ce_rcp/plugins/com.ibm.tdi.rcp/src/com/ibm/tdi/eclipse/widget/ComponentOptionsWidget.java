/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.widget;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Form;

import com.ibm.di.config.base.InternalSchema;
import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.ALMappingConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.LoopConfig;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.di.config.interfaces.MetamergeConfigChangeListener;
import com.ibm.tdi.eclipse.Activator;
import com.ibm.tdi.eclipse.ConfigUtils;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.dialogs.GenericFormDialog;
import com.ibm.tdi.eclipse.editors.BaseEditor;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.wizards.NewConnectorWizard;
import com.ibm.tdi.eclipse.wizards.NewFunctionWizard;

public class ComponentOptionsWidget extends BaseWidget implements MetamergeConfigChangeListener {
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private ConnectorConfig cc;

	private LoopConfig loopConfig;

	private boolean isExpanded = false;

	private Composite headerTop;

	private Composite headerBottom;

	private Combo connectorState;
	
	private Combo mode;
	
	private boolean updatingModeCombo;
	
	public ComponentOptionsWidget(Composite parent, int style, BaseConfiguration editingConfig, BaseEditor editor) {
		super(parent, style, editingConfig, editor);
		if (editingConfig instanceof LoopConfig)
			loopConfig = (LoopConfig) editingConfig;

		if (loopConfig != null) {
			try {
				cc = loopConfig.getLoopConnector();
			} catch (Exception e) {
				EclipseAppender.logerror(e.toString(), e, getShell());
				return;
			}
		} else {
			cc = (ConnectorConfig) getEditingConfig();
		}

		setLayout(new FillLayout());

		createUI(this);
	}

	public void selectConnector() {
		NewConnectorWizard wiz = new NewConnectorWizard();
		wiz.init(null, new StructuredSelection(cc!= null ? cc :getEditingConfig())); //condition modified defect 13746
		if (loopConfig != null)
			wiz.setModeFilter(new String[] { ConnectorConfig.ITERATOR_MODE, ConnectorConfig.LOOKUP_MODE });
		wiz.setModeRequested(false);
		wiz.setNameRequested(false);
		wiz.setChooseFileName(false);

		WizardDialog dlg = new WizardDialog(getShell(), wiz);
		if (dlg.open() == Window.OK) {
			updateInheritsFrom(cc, wiz.getConfigObject().getInheritsFromRef(), wiz.getConfigTypePage().isConnectionOnly());
		}
	}

	public void selectFunction() {
		NewFunctionWizard wiz = new NewFunctionWizard();
		wiz.init(null, new StructuredSelection(getEditingConfig()));
		wiz.setChooseFileName(false);

		WizardDialog dlg = new WizardDialog(getShell(), wiz);
		if (dlg.open() == Window.OK) {
			updateInheritsFrom((ConnectorConfig) getEditingConfig(), wiz.getConfigObject().getInheritsFromRef(), wiz.getConfigTypePage().isConnectionOnly());
		}
	}
	

	private void updateInheritsFrom(ConnectorConfig cc, String inherit, boolean connectionOnly) {
		try {
			if(connectionOnly) {
				if(cc instanceof FunctionConfig) {
					((FunctionConfig)cc).getFunctionConfig().updateInheritsFrom(inherit);
				} else {
					cc.getConnectionConfig().updateInheritsFrom(inherit);					
				}
			} else {
				cc.getConnectionConfig().updateInheritsFrom(BaseConfiguration.INHERIT_PARENT);
				cc.updateInheritsFrom(inherit);
			}
			cc.setupInheritanceChain();
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e, getShell());
		}
	}

	private void createUI(Composite parent) {

		// Label title = new Label(parent, SWT.LEFT);
		// title.setText(cc.getShortName());
		// title.setFont(JFaceResources.getHeaderFont());
		// title.setToolTipText(cc.getInheritsFromRef());
		
		isExpanded = shouldShowExpanded();
		
		Composite header = new Composite(parent, SWT.NONE);
		header.setLayout(new ExpandableLayout());

		headerTop = new Composite(header, SWT.NONE);
		Utils.setGridLayout(headerTop, 99, false);
		headerTop.setLayoutData(new GridData(GridData.FILL_BOTH));

		headerBottom = new Composite(header, SWT.NONE);
		Utils.setGridLayout(headerBottom, 99, false);
		headerBottom.setLayoutData(new GridData(GridData.FILL_BOTH));

		createTopRow(headerTop);
		createBottomRow(headerBottom);

		if(!(cc instanceof ALMappingConfig)) {
			Button more = new Button(headerTop, SWT.PUSH);
			if(isExpanded)
				more.setText(Messages.getString("ComponentOptionsWidget.less"));
			else
				more.setText(Messages.getString("ComponentOptionsWidget.more"));
			more.setToolTipText(Messages.getString("ComponentOptionsWidget.more.tooltip"));
			more.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					isExpanded = !isExpanded;
					Button more = (Button) e.widget;
					if(isExpanded)
						more.setText(Messages.getString("ComponentOptionsWidget.less"));
					else
						more.setText(Messages.getString("ComponentOptionsWidget.more"));
					Form f = Utils.getParentConfig(ComponentOptionsWidget.this, Form.class);
					if(f != null) {
						f.layout(true, true);
					}
				}
			});
		}
	}

	private void createTopRow(Composite parent) {
		if (cc instanceof FunctionConfig) {
			addStateSelector(parent);
			addInheritsFromControls(parent);
		} else if (cc instanceof ALMappingConfig) {
			addStateSelector(parent);
			addInheritsFromControls(parent);
		} else if (cc != null) {
			addMode(parent);
			if (loopConfig == null)			
				addStateSelector(parent);
			addInheritsFromControls(parent);
		} else {
			if (loopConfig == null)
				addStateSelector(parent);
			addInheritsFromControls(parent);
		}
	}

	private void createBottomRow(Composite parent) {
		if (cc instanceof FunctionConfig) {
			addInitializeOptions(parent);
			addMasterInheritsFrom(parent);
		} else if (cc instanceof ALMappingConfig) {
		} else if (cc != null) {
			addMaxIterationsControl(parent);
			addModeSpecificControl(parent);
			addInitializeOptions(parent);
			addMasterInheritsFrom(parent);
		} else {
			addInitializeOptions(parent);
		}
	}

	private void addMaxIterationsControl(Composite parent) {
		final boolean link = Utils.hasLinkRequirements(cc);
		if (ConnectorConfig.ITERATOR_MODE.equals(cc.getMode()) || ConnectorConfig.SERVER_MODE.equals(cc.getMode()) || link) {

			Label label = new Label(parent, SWT.LEFT);
			if (link)
				label.setText(Messages.getString("ComponentOptionsWidget.MaxLookup"));
			else
				label.setText(Messages.getString("ComponentOptionsWidget.MaxIter"));

			final Text maxLimit = new Text(parent, SWT.BORDER | SWT.SINGLE);
			maxLimit.setTextLimit(10);

			String toolTip;
			if (link)
				toolTip = Messages.getString("ComponentOptionsWidget.MaxLookup.tooltip");
			else
				toolTip = Messages.getString("ComponentOptionsWidget.MaxIter.tooltip");

			maxLimit.setToolTipText(toolTip);
			label.setToolTipText(toolTip);
			
			String defValue = cc.getLimitOption();
			if (defValue == null || defValue.length() == 0) {
				if (link)
					defValue = loopConfig != null ? "999999999" : "10";
				else
					defValue = "";
			}

			maxLimit.setText(defValue);

			maxLimit.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					cc.setLimitOption(maxLimit.getText());
				}
			});
			maxLimit.addVerifyListener(new VerifyListener() {
				public void verifyText(VerifyEvent e) {
					if (e.text.length() > 0) {
						try {
							Integer.parseInt(e.text);
						} catch (Exception err) {
							e.doit = false;
						}
					}
				}
			});
			maxLimit.setLayoutData(new GridData(20, SWT.DEFAULT));
		}
	}

	private void addInheritsFromControls(Composite parent) {
		Label label = new Label(parent, SWT.LEFT);
		label.setText(Messages.getString("HooksWidget.0"));
		final Button inherit = new Button(parent, SWT.PUSH);
		String ref = cc.getInheritsFromRef();
		if(ref == null) {
			inherit.setToolTipText("");
			ref = Messages.getString("ConfigChooser.Localized.Inherit.None");
		} else {
			inherit.setToolTipText(ref);
			if (ref.startsWith("system:/Connectors/"))
				ref = ref.substring(19);
		}
		inherit.setText(ref);
		inherit.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (cc instanceof FunctionConfig)
					selectFunction();
				else
					selectConnector();
			}
		});
		DropTarget dt = new DropTarget(inherit, DND.DROP_COPY);
		dt.setTransfer(new Transfer[] { LocalSelectionTransfer.getTransfer() });
		dt.addDropListener(new DropTargetAdapter() {
			@Override
			public void dragOver(DropTargetEvent event) {
				if (! LocalSelectionTransfer.getTransfer().isSupportedType(event.currentDataType))
					return;
				event.detail = DND.DROP_COPY;
			}

			@Override
			public void drop(DropTargetEvent event) {
				if (! LocalSelectionTransfer.getTransfer().isSupportedType(event.currentDataType))
					return;
				if (! (event.data instanceof IStructuredSelection ))
					return;
				IStructuredSelection sel = (IStructuredSelection) event.data;
				if (sel.size() != 1)
					return;
				Object theDrop = sel.getFirstElement();
				if (theDrop instanceof IFile)
					setInheritanceFrom((IFile) theDrop);
			}
		});
	}

	/**
	 * Set cc to inherit from the IFile that was dropped on the inheritance button
	 * @param file
	 */
	private void setInheritanceFrom(IFile file) {
		try {
			BaseConfiguration bc = TDIConfigurationFile.loadFile(file).getDefaultConfigObject();
			if (! (bc instanceof ConnectorConfig))
				return;
			ConnectorConfig config = (ConnectorConfig) bc;
			BaseConfiguration obj = ConfigUtils.createInheritedComponent(cc.getMetamergeConfig(), config);
			String ref = obj.getInheritsFromRef();
			
			if ( cc instanceof FunctionConfig ) {
				if (config instanceof FunctionConfig)
					cc.updateInheritsFrom(ref);
				return;
			} 
			if ( cc instanceof ALMappingConfig ) {
				if (config instanceof ALMappingConfig)
					cc.updateInheritsFrom(ref);
				return;
			}
			if (! (config instanceof ALMappingConfig) 
				&& ! (config instanceof FunctionConfig)
				&& ! ConnectorConfig.SCRIPT_MODE.equals(config.getMode())) {
				cc.updateInheritsFrom(ref);
			}
		} catch (Exception e) {
			EclipseAppender.logerror(e.getMessage(), e);
		}
	}

	private void addMasterInheritsFrom(Composite parent) {
		final Button inherit = new Button(parent, SWT.PUSH);
		inherit.setText(Messages.getString("InheritanceUtil.1"));
		inherit.setImage(Activator.getImage("Branch",true));
		inherit.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				configureInheritance();
			}
		});
	}

	private void configureInheritance() {
		BaseConfiguration b = new com.ibm.di.config.base.BaseConfigurationImpl();
		b.setParameter("Base", getRef(cc));
		String formName;
		if (cc instanceof FunctionConfig ) {
			FunctionConfig fc = (FunctionConfig) cc;
			b.setParameter("Function", getRef(fc.getFunctionConfig()));
			formName = "FunctionInherits";
		} else {
			b.setParameter("Connection", getRef(cc.getConnectionConfig()));
			b.setParameter("Parser", getRef(cc.getParserConfig()));			
			b.setParameter("LinkCriteria", getRef(cc.getLinkCriteria()));
			b.setParameter("Delta", getRef(cc.getDeltaConfig()));
			formName = "ConnectorInherits";
		}
		b.setParameter("Hooks", getRef(cc.getHooks()));
		b.setParameter("InputAttributeMap", getRef(cc.getAttributeMap(true)));
		b.setParameter("OutputAttributeMap", getRef(cc.getAttributeMap(false)));
		b.setParameter("Schema:Input", getRef(cc.getSchema(true)));
		b.setParameter("Schema:Output", getRef(cc.getSchema(false)));
		b.setMetamergeConfig(cc.getMetamergeConfig());
		b.setName(cc.getName());
		b.setParent(cc);
		GenericFormDialog dlg = new GenericFormDialog(getShell(), formName, b);
		if (dlg.open() == Dialog.OK) {
			try {
				setRef(b, cc, "Base", true);
				if (cc instanceof FunctionConfig ) {
					FunctionConfig fc = (FunctionConfig) cc;
					setRef(b, fc.getFunctionConfig(), "Function", false);
				} else {
					setRef(b, cc.getConnectionConfig(), "Connection", true);
					setRef(b, cc, "Base", true);
					setRef(b, cc.getParserConfig(), "Parser", false);
					setRef(b, cc.getLinkCriteria(), "LinkCriteria", false);
					setRef(b, cc.getDeltaConfig(), "Delta", false);
				}
				setRef(b, cc.getHooks(), "Hooks", false);
				setRef(b, cc.getAttributeMap(true), "InputAttributeMap", false);
				setRef(b, cc.getAttributeMap(false), "OutputAttributeMap", false);
				setRef(b, cc.getSchema(true), "Schema:Input", false);
				setRef(b, cc.getSchema(false), "Schema:Output", false);
				cc.setupInheritanceChain();
			} catch (Exception e) {
				if (isDisposed())
					EclipseAppender.logerror("setup inheritance", e);
				else
					EclipseAppender.logerror("setup inheritance", e, getShell());								}
		}
	}

	private Object getRef(BaseConfiguration bc) {
		return bc.getParameterRaw(InternalSchema.INHERITS_FROM);
	}

	private void setRef(BaseConfiguration src, BaseConfiguration target, String name, boolean isUpdate) throws Exception {
		Object ref = src.getParameterRaw(name);
		if (ref == null)
			return; //Cannot happen?
		if (isUpdate && ! ref.toString().startsWith("@SUBSTITUTE")) {
			target.updateInheritsFrom(ref.toString());
		} else {
			target.setParameter(InternalSchema.INHERITS_FROM, ref);
		}
	}

	private void addInitializeOptions(Composite header) {
		Label l = new Label(header, SWT.RIGHT);
		l.setText(Messages.getString("ConnectorUI.toolbar.InitOptions.label")); //$NON-NLS-1$
		// l.setBackground(header.getBackground());

		GridData gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
		l.setLayoutData(gd);

		Combo initopts = new Combo(header, SWT.READ_ONLY);
		if (loopConfig != null) {
			initopts.add(Messages.getString("LoopConfig.connector.options.none.label")); //$NON-NLS-1$
			initopts.add(Messages.getString("LoopConfig.connector.options.init.label")); //$NON-NLS-1$
			initopts.add(Messages.getString("LoopConfig.connector.options.select.label")); //$NON-NLS-1$
			initopts.select(loopConfig.getInitConnectorOption());
			initopts.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					loopConfig.setInitConnectorOption(((Combo) e.widget).getSelectionIndex());
				}
			});
		} else {
			initopts.add(Messages.getString("ConnectorUI.InitOptions.compInitDefault.label")); //$NON-NLS-1$
			initopts.add(Messages.getString("ConnectorUI.InitOptions.compInitOnUse.label")); //$NON-NLS-1$
			initopts.add(Messages.getString("ConnectorUI.InitOptions.compInitDelta.label")); //$NON-NLS-1$
			initopts.add(Messages.getString("ConnectorUI.InitOptions.compInitAlways.label")); //$NON-NLS-1$
			initopts.select(cc.getInitializeOption());
			initopts.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					cc.setInitializeOption(((Combo) e.widget).getSelectionIndex());
				}
			});
		}
		gd = new GridData();
		if (cc instanceof FunctionConfig) {
			gd.horizontalSpan = 2;
		} else {
			gd.horizontalSpan = 3;
		}
		initopts.setLayoutData(gd);
	}

	private void addMode(Composite header) {

		//
		// -- Connector Mode
		//
		Label l = new Label(header, SWT.RIGHT);
		l.setText(Messages.getString("Connector.ModeCB.label")); //$NON-NLS-1$
		// l.setBackground(header.getBackground());
		GridData gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
		l.setLayoutData(gd);

		mode = new Combo(header, SWT.READ_ONLY);
		mode.setTextLimit(40);

		setModeValues();
		
		mode.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (! updatingModeCombo)
					cc.setMode(Utils.internalMode(mode.getText()));
			}
		});

		gd = new GridData();
		mode.setLayoutData(gd);
	}

	private void addModeSpecificControl(Composite header) {
		//
		// -- Update mode has Compute Changes
		//
		if (ConnectorConfig.UPDATE_MODE.equals(cc.getMode())) {
			Button computeChanges = new Button(header, SWT.CHECK | SWT.CENTER);
			// computeChanges.setBackground(header.getBackground());
			computeChanges.setSelection(cc.getComputeChanges());
			computeChanges.setText(Messages.getString("Connector.ComputeChanges.label"));
			computeChanges.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					cc.setComputeChanges(((Button) e.widget).getSelection());
				}
			});
		}

		//
		// -- Update and Delete may have skip lookup
		// -- Place control right of "Compute Changes" if that is present (c2 !=
		// null)
		//
		if ((ConnectorConfig.UPDATE_MODE.equals(cc.getMode()) || ConnectorConfig.DELETE_MODE.equals(cc.getMode()))
				&& cc.supportsSkipLookup()) {
			Button skipLookup = new Button(header, SWT.CHECK | SWT.CENTER);
			// skipLookup.setBackground(header.getBackground());
			skipLookup.setSelection(cc.getSkipLookup());
			skipLookup.setText(Messages.getString("Connector.SkipLookup.label"));
			skipLookup.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					boolean enable = ((Button) e.widget).getSelection();
					if (enable)
						MessageDialog.openInformation(getShell(), Messages
								.getString("ConnectorUI.SkipLookup.Selected.Warning.Title"), Messages
								.getString("ConnectorUI.SkipLookup.Selected.Warning"));
					cc.setSkipLookup(enable);
				}
			});
		}

		//
		// -- Delta mode has an extra dialog
		//
		if (ConnectorConfig.DELTA_MODE.equals(cc.getMode())) {
			Button deltaMode = new Button(header, SWT.PUSH);
			deltaMode.setText(Messages.getString("ConnectorTreeUI.Localized.Delta"));
			deltaMode.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					Dialog dlg = new Dialog(getShell()) {
						private Button normal;
						private Button nodel;
						private Button strict;

						protected Control createDialogArea(Composite parent) {
							Composite c = (Composite) super.createDialogArea(parent);
							Group group = new Group(c, SWT.NONE);
							group.setText(Messages.getString("DeltaBehavior.Title"));
							group.setLayout(new GridLayout(1, false));

							normal = new Button(group, SWT.RADIO);
							normal.setText(Messages.getString("DeltaBehavior.DeltaNormal.label"));
							normal.setToolTipText(Messages.getString("DeltaBehavior.DeltaNormal.tooltip"));
							normal.setSelection(cc.getDeltaBehavior() == ConnectorConfig.DELTA_NORMAL);

							nodel = new Button(group, SWT.RADIO);
							nodel.setText(Messages.getString("DeltaBehavior.DeltaNoDelete.label"));
							nodel.setToolTipText(Messages.getString("DeltaBehavior.DeltaNoDelete.tooltip"));
							nodel.setSelection(cc.getDeltaBehavior() == ConnectorConfig.DELTA_NO_DELETE);

							strict = new Button(group, SWT.CHECK);
							strict.setText(Messages.getString("DeltaBehavior.Strict.label"));
							strict.setToolTipText(Messages.getString("DeltaBehavior.Strict.tooltip"));
							strict.setSelection(cc.getDeltaStrict());

							getShell().setText(Messages.getString("DeltaBehavior.label"));
							return c;
						}

						@Override
						protected void okPressed() {
							cc.setDeltaStrict(strict.getSelection());
							if (normal.getSelection())
								cc.setDeltaBehavior(ConnectorConfig.DELTA_NORMAL);
							else
								cc.setDeltaBehavior(ConnectorConfig.DELTA_NO_DELETE);

							super.okPressed();
						}
					};
					dlg.open();
				}
			});
		}
	}

	private void addStateSelector(Composite header) {
		Label l = new Label(header, SWT.RIGHT);
		l.setText(Messages.getString("Connector.StateCB.label")); //$NON-NLS-1$
		// l.setBackground(header.getBackground());
		GridData gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
		l.setLayoutData(gd);

		connectorState = new Combo(header, SWT.READ_ONLY);
		connectorState.setTextLimit(40);
		connectorState.add(Messages.getString("Localized.Enabled")); //$NON-NLS-1$
		connectorState.add(Messages.getString("Localized.Disabled")); //$NON-NLS-1$
		connectorState.add(Messages.getString("Localized.Passive")); //$NON-NLS-1$
		setConnectorState();
		
		connectorState.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				switch (((Combo) e.widget).getSelectionIndex()) {
				case 0:
					cc.setState(ConnectorConfig.ENABLED_STATE);
					break;
				case 1:
					cc.setState(ConnectorConfig.DISABLED_STATE);
					break;
				case 2:
					cc.setState(ConnectorConfig.PASSIVE_STATE);
					break;
				}
			}
		});
		
		cc.addListener(this);
		
		gd = new GridData();
		gd.horizontalSpan = 3;
		connectorState.setLayoutData(gd);
	}

	private void setConnectorState() {
		if (ConnectorConfig.PASSIVE_STATE.equals(cc.getState()))
			connectorState.select(2);
		else if (ConnectorConfig.ENABLED_STATE.equals(cc.getState()))
			connectorState.select(0);
		else
			connectorState.select(1);
	}
	
	private boolean shouldShowExpanded() {
		if (cc instanceof FunctionConfig) {
			return false;
		} else if (cc instanceof ALMappingConfig) {
			return false;
		} else if (cc != null) {
			if(!cc.getComputeChanges())
				return true;
			if(cc.getInitializeOption() != ConnectorConfig.COMP_INIT_DEFAULT)
				return true;
		}
		return false;
	}

	private class ExpandableLayout extends Layout {

		@Override
		protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
			Point p1 = headerTop.computeSize(wHint, hHint, flushCache);
			Point p2 = headerBottom.computeSize(wHint, hHint, flushCache);
			if(isExpanded)
				return new Point(p1.x+p2.x, p1.y+p2.y+3);
			else
				return new Point(p1.x, p1.y);
		}

		@Override
		protected void layout(Composite composite, boolean flushCache) {
			Rectangle area = composite.getClientArea();
			Point p1 = headerTop.computeSize(area.width, SWT.DEFAULT);
			headerTop.setBounds(0, 0, area.width, p1.y);
			if(isExpanded) {
				Point p2 = headerBottom.computeSize(area.width, SWT.DEFAULT);
				headerBottom.setBounds(0, p1.y+3, area.width, p1.y+3+p2.y);
			}
		}
	}
	public void configurationChanged(MetamergeConfigChange mcc) {
		if (isDisposed() || connectorState == null || connectorState.isDisposed()) {
			cc.removeListener(this);
		} else if (mcc.getSource() == cc && InternalSchema.CONNECTOR_STATE.equals(mcc.getKey())){
			getDisplay().asyncExec(new Runnable() {
				public void run() {
					setConnectorState();
				}
			});
		} else if ("assemblyLine".equals(mcc.getKey()) && mode != null) {
			getDisplay().asyncExec(new Runnable() {
				public void run() {
					setModeValues();
				}
			});
		}
	}

	private void setModeValues() {
		if (isDisposed())
			return;
		updatingModeCombo = true;
		mode.removeAll();
		
		for (String str : Utils.getSupportedModes(cc)) {
			if (loopConfig == null || ConnectorConfig.LOOKUP_MODE.equals(str) || ConnectorConfig.ITERATOR_MODE.equals(str))
				mode.add(Utils.externalMode(str));
		}

		String curmode = Utils.externalMode(cc.getMode());
		int sel = mode.indexOf(curmode);
		if (sel == -1) {
			if (mode.getItemCount() > 0)
				cc.setMode(Utils.internalMode(mode.getItem(0)));
			if (isDisposed())
				return;
			sel = 0;
		}
		mode.select(sel);
		updatingModeCombo = false;
	}

	@Override
	public void dispose() {
		if (cc != null)
			cc.removeListener(this);
		super.dispose();
	}	
}
