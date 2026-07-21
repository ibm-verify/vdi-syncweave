/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.widget;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.ALMappingConfig;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.HookConfig;
import com.ibm.di.config.interfaces.SimulationConfig;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.log.EclipseAppender;

public class ALSimulationWidget extends BaseWidget {

	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private SimpleTextEditor scriptEditor;
	private StateContentProvider provider;
	private Composite scriptContainer;
	private Label noEditor;
	private HookConfig currentHook;
	private AssemblyLineConfig alc;

	private Button updateProxyAL;

	public ALSimulationWidget(Composite parent, int title,
			AssemblyLineConfig alc) {
		super(parent, title, alc);
		this.alc = alc;
		setLayout(new FillLayout());
		createUI(this);
	}

	private void createUI(ALSimulationWidget simulationWidget) {

		Form frm = createForm(this, null);
		frm.setText(Messages.getString("ALSimulationWidget.title")); //$NON-NLS-1$
		frm.setLayout(new FillLayout());

		FormToolkit tk = getFormToolKit();

		Composite c = frm.getBody();
		c.setLayout(new GridLayout(1, false));

		SimulationConfig sc = null;
		try {
			sc = alc.getSimulationConfig();
		} catch (Exception e2) {
			EclipseAppender.logerror(e2.toString(), e2);
			return;
		}

		Section s1 = tk.createSection(c, 0);
		s1.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		s1.setLayout(new FillLayout());
		s1.setText(Messages.getString("ALSimulationWidget.ProxyAL"));

		Composite c1 = tk.createComposite(s1);
		c1.setLayout(new GridLayout(2, false));

		// -- Server name
		tk.createLabel(c1, Messages.getString("ALSimulationWidget.server")); //$NON-NLS-1$
		Text text = tk.createText(c1, safeValue(sc.getProxyALServer()),
				SWT.BORDER); //$NON-NLS-1$
		text.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				try {
					alc.getSimulationConfig().setProxyALServer(
							((Text) e.widget).getText());
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		// -- Configuration Name/ID
		tk.createLabel(c1, Messages.getString("ALSimulationWidget.config")); //$NON-NLS-1$
		text = tk.createText(c1, safeValue(sc.getProxyALConfigInstance()),
				SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				try {
					alc.getSimulationConfig().setProxyALConfigInstance(
							((Text) e.widget).getText());
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		// -- AssemblyLine
		tk.createLabel(c1, Messages
				.getString("ALSimulationWidget.assemblyline")); //$NON-NLS-1$
		text = tk.createText(c1, safeValue(sc.getProxyALName()), SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				try {
					alc.getSimulationConfig().setProxyALName(
							((Text) e.widget).getText());
					updateProxyAL.setEnabled(((Text) e.widget).getText()
							.length() > 0);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		tk.createLabel(c1, "");
		updateProxyAL = tk.createButton(c1, Messages
				.getString("ALSimulationWidget.update"), SWT.PUSH);
		updateProxyAL.setToolTipText(Messages
				.getString("ALSimulationWidget.update.tooltip"));
		updateProxyAL.setEnabled(text.getText().length() > 0);
		updateProxyAL.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				try {
					IFile target = ((IFolder) getTDIConfigFile().getParent())
							.getFile(alc.getSimulationConfig().getProxyALName()
									+ ".assemblyline");
					if (target.exists() && 
							!MessageDialog.openConfirm(getShell(), 
									Messages.getString("ALSimulationWidget.title"),
									Messages.getMessage("general.resource.exists",
										target.getProjectRelativePath(),
										Utils.dateToString(target.getLocalTimeStamp()))))
							return;
				
					TDIConfigurationFile cfg = new TDIConfigurationFile(target);
					AssemblyLineConfig proxy = alc.getSimulationConfig()
							.createOrUpdateProxyAL();

					cfg.setDefaultConfigObject(proxy.getShortName(), proxy);
					cfg.commitChanges(null, true);

					MessageDialog.openInformation(getShell(), Messages
							.getString("ALSimulationWidget.title"), Messages
							.getMessage(
									"ALSimulationWidget.assemblyline.updated",
									null));
				} catch (Exception err) {
					EclipseAppender.logerror(err.toString(), err, getShell());
				}
			}
		});

		s1.setClient(c1);

		// -- Table of connectors/states
		Section s2 = tk.createSection(c, 0);
		s2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		s2.setLayout(new FillLayout());
		s2.setText(Messages.getString("ALSimulationWidget.Simulation.Settings"));

		Composite c2 = tk.createComposite(s2);
		c2.setLayout(new GridLayout(1, false));

		Composite comp = new Composite(c2, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		TableColumnLayout layout = new TableColumnLayout();
		comp.setLayout(layout);

		TableViewer table = new TableViewer(comp, SWT.BORDER
				| SWT.FULL_SELECTION);
		table.getTable().setHeaderVisible(true);
		table.getTable().setLinesVisible(true);
		table.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				Object element = ((IStructuredSelection) event.getSelection())
						.getFirstElement();
				updateScriptViewer(element, SimulationConfig.SIM_SCRIPTED_STATE
						.equals(getState(element)));
			}
		});

		TableViewerColumn tv = new TableViewerColumn(table, SWT.NONE);
		tv.getColumn().setText(
				Messages.getString("ALSimulationWidget.connector")); //$NON-NLS-1$
		tv.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((BaseConfiguration) element).getShortName();
			}
		});
		layout.setColumnData(tv.getColumn(), new ColumnWeightData(50));

		tv = new TableViewerColumn(table, SWT.NONE);
		tv.getColumn().setText(Messages.getString("ALSimulationWidget.state")); //$NON-NLS-1$
		tv.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				String name = ((BaseConfiguration) element).getShortName();
				SimulationConfig sc;
				try {
					sc = ((AssemblyLineConfig) getEditingConfig())
							.getSimulationConfig();
					String value = sc.getComponentSimState(name);
					for(int i = 0; i < fullValues.length; i++) {
						if(fullValues[i].equalsIgnoreCase(value))
							return fullValuesTranslated[i];
					}
					return value;
				} catch (Exception e) {
					return e.toString();
				}
			}
		});
		tv.setEditingSupport(new StateColumnEditor(table));
		layout.setColumnData(tv.getColumn(), new ColumnWeightData(50));

		provider = new StateContentProvider();
		table.setContentProvider(provider);
		table.setInput(getEditingConfig());

		// --
		scriptContainer = tk.createComposite(c2);
		scriptContainer.setLayout(new StackLayout());
		scriptContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true));

		scriptEditor = new SimpleTextEditor(scriptContainer, SWT.BORDER);
		scriptEditor.getSourceViewer().getTextWidget().addModifyListener(
				new ModifyListener() {
					public void modifyText(ModifyEvent e) {
						if (currentHook != null)
							currentHook.setScript(scriptEditor.getText());
					}
				});
		noEditor = tk.createLabel(scriptContainer, ""); //$NON-NLS-1$

		s2.setClient(c2);

		updateScriptViewer(null, false);
	}

	private String safeValue(String str) {
		return (str == null ? "" : str);
	}

	protected void setState(Object element, String state) {
		BaseConfiguration cc = (BaseConfiguration) element;
		SimulationConfig sc;
		try {
			sc = ((AssemblyLineConfig) getEditingConfig())
					.getSimulationConfig();
			sc.setComponentSimState(cc.getShortName(), state);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected String getState(Object element) {
		BaseConfiguration cc = (BaseConfiguration) element;
		SimulationConfig sc;
		try {
			sc = ((AssemblyLineConfig) getEditingConfig())
					.getSimulationConfig();
			return sc.getComponentSimState(cc.getShortName());
		} catch (Exception e) {
			return e.toString();
		}
	}

	/**
	 * Values to show in dropdowns.
	 * The arrays must be sorted the same way, and
	 * the limited values must be first, to make the index the
	 * same in all arrays.
	 */
	private final static String[] fullValues = new String[] {
			SimulationConfig.SIM_DISABLED_STATE,
			SimulationConfig.SIM_ENABLED_STATE,
			SimulationConfig.SIM_SIMULATED_STATE,
			SimulationConfig.SIM_SCRIPTED_STATE,
			SimulationConfig.SIM_PROXY_STATE };

	private final static String[] fullValuesTranslated = new String[] {
			Messages.getString("ALSimulationWidget.SimValue.0"),
			Messages.getString("ALSimulationWidget.SimValue.1"),
			Messages.getString("ALSimulationWidget.SimValue.2"),
			Messages.getString("ALSimulationWidget.SimValue.3"),
			Messages.getString("ALSimulationWidget.SimValue.4") };

	private final static String[] limitedValuesTranslated = new String[] {
		Messages.getString("ALSimulationWidget.SimValue.0"),
		Messages.getString("ALSimulationWidget.SimValue.1")};

	private class StateColumnEditor extends EditingSupport {

		private ComboBoxCellEditor fullCellEditor;
		private ComboBoxCellEditor limitedCellEditor;


		public StateColumnEditor(ColumnViewer viewer) {
			super(viewer);
			fullCellEditor = new ComboBoxCellEditor((Composite) viewer
					.getControl(), fullValuesTranslated, SWT.READ_ONLY | SWT.DROP_DOWN);

			limitedCellEditor = new ComboBoxCellEditor((Composite) viewer
					.getControl(), limitedValuesTranslated, SWT.READ_ONLY | SWT.DROP_DOWN);
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected CellEditor getCellEditor(Object element) {

			if (element instanceof ConnectorConfig
					&& !(element instanceof ALMappingConfig)
					&& !((ConnectorConfig) element).getMode().equals(
							ConnectorConfig.SERVER_MODE)) {
				return fullCellEditor;
			}

			return limitedCellEditor;

		}

		@Override
		protected Object getValue(Object element) {

			String value = getState(element);

			int n = fullValues.length;
			if (getCellEditor(element)==limitedCellEditor)
				n = limitedValuesTranslated.length;
			
			for (int i = 0; i < n; i++) {
				if (value.equals(fullValues[i]))
					return i;
			}
			return 0;
		}

		@Override
		protected void setValue(Object element, Object value) {

			Integer i = (Integer) value;
			setState(element, fullValues[i]);
			getViewer().update(element, null);
			updateScriptViewer(element, i == 3);
		}
	}

	private class StateContentProvider implements IStructuredContentProvider {

		public Object[] getElements(Object inputElement) {
			AssemblyLineConfig alc = (AssemblyLineConfig) getEditingConfig();

			ArrayList<BaseConfiguration> elements = new ArrayList<BaseConfiguration>();

			alc.getEntryFeedComponents().getConfigurations(elements);
			alc.getDataFlowComponents().getConfigurations(elements);

			return elements.toArray();
		}

		public void dispose() {
			// TODO Auto-generated method stub

		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// TODO Auto-generated method stub

		}
	}

	public void updateScriptViewer(Object element, boolean b) {
		if (!b) {
			((StackLayout) scriptContainer.getLayout()).topControl = noEditor;
		} else {
			SimulationConfig sc;
			try {
				sc = ((AssemblyLineConfig) getEditingConfig())
						.getSimulationConfig();
				currentHook = sc.getHook(((BaseConfiguration) element)
						.getShortName());
				scriptEditor
						.setText(currentHook.getScript() == null ? "" : currentHook.getScript()); //$NON-NLS-1$
			} catch (Exception e) {
				e.printStackTrace();
			}
			((StackLayout) scriptContainer.getLayout()).topControl = scriptEditor;
		}
		scriptContainer.layout(true);
	}
}
