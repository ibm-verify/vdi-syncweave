/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.widget;

import java.util.Hashtable;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.ibm.di.config.base.BaseConfigurationImpl;
import com.ibm.di.config.base.InternalSchema;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.DeltaConfig;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.HookConfig;
import com.ibm.di.config.interfaces.HooksConfig;
import com.ibm.di.config.interfaces.LinkCriteriaConfig;
import com.ibm.di.config.interfaces.LinkCriteriaItem;
import com.ibm.di.config.interfaces.LoopConfig;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.di.config.interfaces.MetamergeConfigChangeListener;
import com.ibm.di.config.interfaces.ParserConfig;
import com.ibm.di.config.interfaces.PoolDefConfig;
import com.ibm.di.config.interfaces.PoolInstanceConfig;
import com.ibm.di.config.interfaces.RawConnectorConfig;
import com.ibm.di.config.interfaces.RawFunctionConfig;
import com.ibm.di.config.interfaces.ReconnectConfig;
import com.ibm.di.config.interfaces.ReconnectRuleConfig;
import com.ibm.tdi.eclipse.Activator;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.builders.ComponentValidator;
import com.ibm.tdi.eclipse.editors.BaseEditor;
import com.ibm.tdi.eclipse.log.EclipseAppender;

public class ConnectorWidget extends BaseWidget {
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public static final String ID = "com.ibm.tdi.editors.ConnectorEditor"; //$NON-NLS-1$

	private String[] buttons = new String[] { Messages.getString("ConnectorWidget.InputAttributeMap"), //$NON-NLS-1$
			Messages.getString("ConnectorWidget.OutputAttributeMap"), //$NON-NLS-1$
			Messages.getString("ConnectorTreeUI.Localized.Hooks"), //$NON-NLS-1$
			Messages.getString("ConnectorTreeUI.Localized.LinkCriteria"), //$NON-NLS-1$
			Messages.getString("ConnectorTreeUI.Localized.Delta"), //$NON-NLS-1$
			Messages.getString("ConnectorTreeUI.Localized.Connection"), //$NON-NLS-1$
			Messages.getString("ConnectorTreeUI.Localized.Parser"), //$NON-NLS-1$
			Messages.getString("ConnectorUI.Reconnect.label"), //$NON-NLS-1$
			Messages.getString("ConnectorUI.Pool.label"), //$NON-NLS-1$
			Messages.getString("LoopConfig.connectorParams.label"), //$NON-NLS-1$
	};

	private final static int TAB_INPUT_MAP = 0;
	private final static int TAB_OUTPUT_MAP = 1;
	private final static int TAB_HOOKS = 2;
	private final static int TAB_LINK = 3;
	private final static int TAB_DELTA = 4;
	private final static int TAB_CONNECTION = 5;
	private final static int TAB_PARSER = 6;
	private final static int TAB_RECONNECT = 7;
	private final static int TAB_POOL = 8;
	private final static int TAB_LOOP_PARAMS = 9;

	private Hashtable<String, Composite> panels = new Hashtable<String, Composite>();
	private CTabFolder tabs;

	private MetamergeConfigChangeListener listener;

	private OperationsWidget operations;

	private LoopConfig loopConfig = null;

	private ConnectorConfig loopConnector;

	private Composite controlsHeader;

	public ConnectorWidget(Composite parent, int style, BaseConfiguration editingConfig, BaseEditor editor) {
		super(parent, style, editingConfig, editor);
		setLayout(new FillLayout());
		createPartControl(this);
	}

	public void createPartControl(Composite top) {

		createForm(top, null);
		String title = getEditingConfig().getShortName();
		getForm().setText(title);
		getForm().setImage(Activator.getImage(getEditingConfig()));

		Composite parent = getForm().getBody();
		parent.setLayout(new FillLayout());

		if (getEditingConfig() instanceof LoopConfig) {
			loopConfig = (LoopConfig) getEditingConfig();
			try {
				loopConnector = loopConfig.getLoopConnector();
			} catch (Exception e) {
				EclipseAppender.logerror(e.toString(), e, getShell());
				return;
			}
		}

		if (getEditingConfig() instanceof AssemblyLineConfig) {
			operations = new OperationsWidget(parent, SWT.NONE, (AssemblyLineConfig) getEditingConfig(), getEditor());
		} else {
			Composite header = new Composite(getForm().getHead(), SWT.NONE);
			header.setLayout(new GridLayout(1, false));
			controlsHeader = new Composite(header, SWT.NONE);
			controlsHeader.setLayout(new StackLayout());
			controlsHeader.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
			addControlsHeader();
			getForm().setHeadClient(header);

			tabs = new CTabFolder(parent, SWT.LEFT | SWT.FLAT);
			tabs.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			tabs.setMinimumCharacters(40); // Basically turn off compressed tabs...
			createTabItems(tabs);
		}
	}

	private void addControlsHeader() {
		getDisplay().syncExec(new Runnable() {
			public void run() {
				_addControlsHeader();
			}
		});
	}
	
	protected void _addControlsHeader() {
		if (controlsHeader.getChildren().length > 0)
			controlsHeader.getChildren()[0].dispose();

		ComponentOptionsWidget controls = new ComponentOptionsWidget(controlsHeader, SWT.NONE, getEditingConfig(), getEditor());
		((StackLayout) controlsHeader.getLayout()).topControl = controls;
		controlsHeader.layout(true, true);
		getForm().layout(true, true);
		
		checkPoolSettings();
	}

	private void checkPoolSettings() {
		BaseConfiguration config = getEditingConfig();
		if(config instanceof ConnectorConfig && config.getInheritsFrom() instanceof ConnectorConfig) {
			ConnectorConfig cc = (ConnectorConfig) config.getInheritsFrom();
			boolean canBePooled = false;

			if (! (cc instanceof FunctionConfig) && ! ConnectorConfig.SERVER_MODE.equals(cc.getMode())) 
			{
				PoolDefConfig defConfig = cc.getPoolDefConfig();
				if (defConfig != null)
					canBePooled = defConfig.getPoolEnabled();
			}
			// -- Clear pool-enabled if inherited isn't pool enabled.
			cc = (ConnectorConfig) config;
			if(!canBePooled && cc.getPoolInstanceConfig() != null && cc.getPoolInstanceConfig().getPoolEnabled()) {
				cc.getPoolInstanceConfig().setParameter(InternalSchema.CONNECTOR_POOL_INSTANCE_ENABLED, "false", false);
			}
			
		}
	}

	protected void createTabItems(CTabFolder tabFolder) {
		for (String b : buttons) {
			Control c = showPanel(b, tabFolder);
			if (c == null)
				continue;

			CTabItem item = new CTabItem(tabFolder, SWT.NONE);
			item.setText(b);
			item.setControl(c);
		}
		tabFolder.setSelection(0);

		listener = new MetamergeConfigChangeListener() {
			public void configurationChanged(MetamergeConfigChange changeEvent) {
				Object key = changeEvent.getKey();
				Object src = changeEvent.getSource();
				if (src instanceof ConnectorConfig && InternalSchema.CONNECTOR_MODE.equals(key)) {
					addControlsHeader();
					checkTab(buttons[TAB_OUTPUT_MAP], false);
					checkTab(buttons[TAB_RECONNECT], false);
					updateTabs();
					getDisplay().asyncExec(new Runnable() {
						public void run() {
							tabs.setSelection(0);
						}
					});
				}
				if (src instanceof RawConnectorConfig && "setInheritsFrom".equals(changeEvent.getUserObject()) || 
						InternalSchema.CONNECTOR_CONNECTOR_JAVACLASS.equals(key)) {
					BaseConfiguration bc = (loopConnector != null ? loopConnector.getConnectionConfig()
							: ((ConnectorConfig) getEditingConfig()).getConnectionConfig());
					if (src == bc && bc != null && ! (bc.getParent() instanceof FunctionConfig)) {
						// -- remove current tab
						checkTab(buttons[TAB_CONNECTION], false);
						checkTab(buttons[TAB_RECONNECT], false);
						addControlsHeader();
						updateTabs();
					}
				}
				if (src instanceof RawFunctionConfig && "setInheritsFrom".equals(changeEvent.getUserObject())) {
					BaseConfiguration bc = ((FunctionConfig) getEditingConfig()).getFunctionConfig();
					if (src == bc) {
						addControlsHeader();
						updateTabs();
					}
				}
				if (src instanceof ParserConfig && "setInheritsFrom".equals(changeEvent.getUserObject())) {
					// -- Reset parser tab
					if(buttons.length >= TAB_PARSER){ //if condition added as a fix for defect 13041
						getDisplay().asyncExec(new Runnable() {
							public void run() {
								int sel = tabs.getSelectionIndex();
								ConnectorConfig cc = loopConnector != null ? loopConnector : (ConnectorConfig) getEditingConfig();
								_checkTab(buttons[TAB_PARSER], false);
								_checkTab(buttons[TAB_PARSER], 
										Utils.hasParserRequirements(cc) && !Utils.isPooledConnector(cc));
								if (sel >=0)
									tabs.setSelection(sel);
							}
						});
					}
					return;
				}
				if (src instanceof ReconnectConfig && "setInheritsFrom".equals(changeEvent.getUserObject())) {
					// -- Reset reconnect tab
					checkTab(buttons[TAB_RECONNECT], false);
					checkTab(buttons[TAB_RECONNECT], true);
				}
				if (src instanceof PoolInstanceConfig && 
						InternalSchema.CONNECTOR_POOL_DEF_ENABLED.equals(key) &&
						panels.get(buttons[TAB_POOL]) != null) {
					// -- Reset connection tab
					updateTabs();
				}
				if (src instanceof LinkCriteriaConfig && "setInheritsFrom".equals(changeEvent.getUserObject())) {
					// -- Reset link tab
					getDisplay().asyncExec(new Runnable() {
						public void run() {
							int sel = tabs.getSelectionIndex();
							ConnectorConfig cc = loopConnector != null ? loopConnector : (ConnectorConfig) getEditingConfig();
							_checkTab(buttons[TAB_LINK], false);
							_checkTab(buttons[TAB_LINK], Utils.hasLinkRequirements(cc));
							if (sel >=0)
								tabs.setSelection(sel);
						}
					});
					return;
				}
				if (src == getEditingConfig()) {
					if (InternalSchema.ENABLED.equals(key)
						|| InternalSchema.CONNECTOR_STATE.equals(key)
						|| InternalSchema.CONNECTOR_MODE.equals(key))
						getForm().setImage(Activator.getImage(getEditingConfig()));
					else if (BaseConfigurationImpl.NAME.equals(key))
						getForm().setText(getEditingConfig().getShortName());
					else if ("setInheritsFrom".equals(changeEvent.getUserObject()))
						addControlsHeader();
				}
			}
		};
		if (loopConnector != null)
			loopConnector.addListener(listener);

		getEditingConfig().addListener(listener);
	}
	
	protected void updateTabs() {
		if (getEditingConfig() instanceof FunctionConfig) {
			return;
		}

		ConnectorConfig cc;
		if (loopConnector != null)
			cc = loopConnector;
		else
			cc = (ConnectorConfig) getEditingConfig();

		checkTab(buttons[TAB_INPUT_MAP], Utils.isInputConnector(cc));
		checkTab(buttons[TAB_OUTPUT_MAP], Utils.isOutputConnector(cc));
		checkTab(buttons[TAB_LINK], Utils.hasLinkRequirements(cc));
		checkTab(buttons[TAB_DELTA], ConnectorConfig.ITERATOR_MODE.equals(cc.getMode()));
		checkTab(buttons[TAB_CONNECTION], !Utils.isPooledConnector(cc));
		checkTab(buttons[TAB_PARSER], (Utils.hasParserRequirements(cc) && !Utils.isPooledConnector(cc)));
		checkTab(buttons[TAB_RECONNECT], true);
		checkTab(buttons[TAB_POOL], Utils.canPoolConnector(cc));
		/*new condition added by L3 defect 13746*/
		if (loopConnector!= null)
			checkTab(buttons[TAB_LOOP_PARAMS], cc.getConnectionConfig() != null && 
					cc.getConnectionConfig().getJavaClass() != null &&
					!cc.getConnectionConfig().getJavaClass().startsWith("@"));
	}

	protected void checkTab(String tab, boolean isRequired) {
		final String name = tab;
		final boolean reqd = isRequired;
		getDisplay().syncExec(new Runnable() {
			public void run() {
				_checkTab(name, reqd);
			}
		});
	}

	private void _checkTab(String tab, boolean isRequired) {
		Object panel = panels.get(tab);
		if (isRequired && panel == null) {
			Control c = showPanel(tab, tabs);
			if(c == null)
				return;
			int index = findTabIndex(tab);
			CTabItem item = new CTabItem(tabs, SWT.NONE, index);
			item.setText(tab);
			item.setControl(c);
		} else if (!isRequired && panel != null) {
			for (CTabItem item : tabs.getItems()) {
				if (item.getControl() == panel) {
					item.dispose();
					panels.remove(tab).dispose();
				}
			}
		}
	}

	private int internalTabIndex(String tab) {
		for (int i = 0; i < buttons.length; i++) {
			if (buttons[i].equals(tab))
				return i;
		}
		return -1;
	}

	private int findTabIndex(String tab) {
		int index = internalTabIndex(tab);

		for (int i = 0; i < tabs.getItemCount(); i++) {
			CTabItem item = tabs.getItem(i);
			if (internalTabIndex(item.getText()) > index)
				return i;
		}
		return tabs.getItemCount();
	}

	public void setTabButtonNames(String[] buttons) {
		this.buttons = buttons;
	}

	public Control showPanel(String panel, Composite parent) {
		Composite c = panels.get(panel);
		ConnectorConfig cc;
		if (loopConnector != null)
			cc = loopConnector;
		else
			cc = (ConnectorConfig) getEditingConfig();

		if (c == null) {
			try {
				switch (panelIndex(panel)) {
				case TAB_INPUT_MAP: // Input map
					if (!Utils.isInputConnector(cc))
						return null;
					// Check for a special form for this map
					String formName1 = Utils.getFormName(cc, true);
					if (formName1 != null) {
						c = new FormWidget2(parent, SWT.NONE, cc.getAttributeMap(true), formName1);
					} else {
						c = new AttributeMapWidget(parent, SWT.NONE, cc, WorkMapWidget.MAP_MODE_INPUT, getEditor());
					}
					break;

				case TAB_OUTPUT_MAP: // Input map
					if (!Utils.isOutputConnector(cc))
						return null;
					// Check for a special form for this map
					String formName2 = Utils.getFormName(cc, false);
					if (formName2 != null) {
						c = new FormWidget2(parent, SWT.NONE, cc.getAttributeMap(false), formName2);
					} else {
						c = new AttributeMapWidget(parent, SWT.NONE, cc, WorkMapWidget.MAP_MODE_OUTPUT, getEditor());
					}
					break;

				case TAB_HOOKS: // Hooks
					try {
						c = new HooksWidget(cc, parent, SWT.NONE, getEditor());
					} catch (Throwable t) {
						EclipseAppender.logerror(Messages.getString("ConnectorTreeUI.Localized.Hooks"), t);
						return null;
					}
					break;

				case TAB_CONNECTION: // Config
					if(Utils.isPooledConnector(cc) || isReusingConnector(cc))
						return null;
					
					if (cc instanceof FunctionConfig)
						c = new RawConnectorWidget(parent, SWT.NONE, ((FunctionConfig) cc).getFunctionConfig(), false);
					else
						c = new RawConnectorWidget(parent, SWT.NONE, cc.getConnectionConfig(), false, loopConfig == null);
					break;

				case TAB_PARSER: // Parser
					if (!Utils.hasParserRequirements(cc) || Utils.isPooledConnector(cc) || isReusingConnector(cc))
						return null;
					c = new ParserWidget(parent, SWT.NONE, cc.getParserConfig());
					break;

				case TAB_RECONNECT: // Reconnect
					if (isReusingConnector(cc) || cc instanceof FunctionConfig)
						return null;
					c = new FormWidget2(parent, SWT.NONE, cc.getReconnectConfig(), "ConnectorReconnect"); //$NON-NLS-1$
					Form form = ((FormWidget2) c).getForm();
					form.setText(Messages.getString("ConnectorUI.Reconnect.label")); //$NON-NLS-1$
					FormToolkit ftk = ((FormWidget2) c).getFormToolkit();
					ftk.decorateFormHeading(form);

					// Add a readonly section with the built-in reconnect rules
					// of the connector.
					String contents = reconnectRulesToText(cc);
					ftk.createLabel(form.getBody(), Messages.getString("ReconnectConfigUI.reconnect.rules.label"), SWT.RIGHT) //$NON-NLS-1$
							.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

					ftk.createText(form.getBody(), contents, SWT.MULTI | SWT.READ_ONLY | SWT.BORDER | SWT.V_SCROLL).setLayoutData(
							new GridData(SWT.FILL, SWT.FILL, true, true));
					break;

				case TAB_LINK: // Link
					if (!Utils.hasLinkRequirements(cc))
						return null;
					c = new LinkCriteriaWidget(cc.getLinkCriteria(), parent, SWT.NONE);
					break;

				case TAB_DELTA: // Delta
					if (!ConnectorConfig.ITERATOR_MODE.equals(cc.getMode()))
						return null;
					c = new FormWidget2(parent, SWT.TITLE, cc.getDeltaConfig(), "Delta Configuration"); //$NON-NLS-1$
					break;

				case TAB_POOL: // Pool
					if (cc instanceof FunctionConfig || ConnectorConfig.SERVER_MODE.equals(cc.getMode()) || isReusingConnector(cc))
						return null;
					
					if (Utils.getParentConfig(cc, AssemblyLineConfig.class) != null) {
						if(!Utils.canPoolConnector(cc)) {
							return null;
						}
						
						c = new FormWidget2(parent, SWT.TITLE, cc.getPoolInstanceConfig(), "Connector Pool Instance"); //$NON-NLS-1$
						Composite body = ((FormWidget2) c).getForm().getBody();
						Button butt = new Button(body, SWT.PUSH);
						butt.setText(Messages.getString("ConnectorWidget.open.poolconn"));
						butt.setToolTipText(Messages.getString("ConnectorWidget.open.poolconn.tooltip"));
						final ConnectorConfig bcc = cc; 
						butt.addSelectionListener(new SelectionAdapter() {
							public void widgetSelected(SelectionEvent e) {
								Utils.openEditorFor(bcc.getInheritsFrom());
							}
						});
					} else {
						c = new FormWidget2(parent, SWT.TITLE, cc.getPoolDefConfig(), "Connector Pool Definition"); //$NON-NLS-1$
					}
					break;

				case TAB_LOOP_PARAMS: 
					// Output map is used to set params in the loop connector
					if (loopConnector == null || loopConnector.getConnectionConfig() == null)
						return null;
					// Cannot change parameters for a re-used connector.
					if (isReusingConnector(cc))
						return null;
					c = new AttributeMapWidget(parent, SWT.NONE, cc, WorkMapWidget.MAP_MODE_OUTPUT, getEditor());
					break;

				}

				if (c != null)
					panels.put(panel, c);
				else
					throw new Exception("Unknown panel type: " + panel); //$NON-NLS-1$
			} catch (Exception e) {
				EclipseAppender.logerror(e.toString(), e, parent.getShell());
				return null;
			}
		}

		return c;
	}

	private int panelIndex(String panel) {
		for (int i = 0; i < buttons.length; i++) {
			if (buttons[i].equalsIgnoreCase(panel))
				return i;
		}
		return 0;
	}

	@Override
	public void dispose() {
		for (Composite c : panels.values())
			c.dispose();
		panels.clear();

		if (operations != null) {
			operations.dispose();
			operations = null;
		}

		if (listener != null) {
			if (loopConnector != null)
				loopConnector.removeListener(listener);
			getEditingConfig().removeListener(listener);
		}

		super.dispose();
	}

	private static String reconnectRulesToText(ConnectorConfig cc) throws Exception {

		final String RECONNECT_RULE_PARTS_DELIMETER = ":"; //$NON-NLS-1$

		String connectorClassName = cc.getConnectionConfig().getJavaClass();

		StringBuilder result = new StringBuilder();

		ContainerConfig reconnectRules = cc.getReconnectConfig().getReconnectRules();
		List<BaseConfiguration> allRules = reconnectRules.getConfigurations(null);
		reconnectRules.getInheritedConfigurations(allRules);

		for (BaseConfiguration b : allRules) {
			if (!(b instanceof ReconnectRuleConfig))
				continue;
			ReconnectRuleConfig rule = (ReconnectRuleConfig) b;

			String action = rule.getAction();
			if (action == null) {
				action = ""; //$NON-NLS-1$
			}
			String exClass = rule.getExceptionClass();
			if (exClass == null) {
				exClass = ""; //$NON-NLS-1$
			}
			String exRegExp = rule.getExceptionMessageRegExp();
			if (exRegExp == null) {
				exRegExp = ""; //$NON-NLS-1$
			}

			// if it is not the first rule, append a newline
			if (result.length() > 0) {
				result.append("\n"); //$NON-NLS-1$
			}
			result.append(connectorClassName);
			result.append(RECONNECT_RULE_PARTS_DELIMETER);
			// no Connector name
			result.append(RECONNECT_RULE_PARTS_DELIMETER);
			result.append(exClass);
			result.append(RECONNECT_RULE_PARTS_DELIMETER);
			result.append(action);
			result.append(RECONNECT_RULE_PARTS_DELIMETER);
			result.append(exRegExp);

		}

		return result.toString();
	}

	public void gotoMarker(IMarker marker) {
		try {
			String location = (String) marker.getAttribute(IMarker.LOCATION);
			BaseConfiguration loc = getEditingConfig().getChildForPath(location);
			if (loc == null)
				return;
			String problem = (String) marker.getAttribute(IMarker.PROBLEM);
			int tabIndex = 0;
			if (ComponentValidator.SCHEMA_NOT_DEFINED.equals(problem))
				tabIndex = 0;
			else if (loc instanceof HookConfig || loc instanceof HooksConfig)
				tabIndex = TAB_HOOKS;
			else if (loc instanceof RawConnectorConfig)
				tabIndex = TAB_CONNECTION;
			else if (loc instanceof ParserConfig)
				tabIndex = TAB_PARSER;
			else if (loc instanceof LinkCriteriaConfig || loc instanceof LinkCriteriaItem)
				tabIndex = TAB_LINK;
			else if (loc instanceof DeltaConfig)
				tabIndex = TAB_DELTA;
			else if (loc instanceof ReconnectConfig)
				tabIndex = TAB_RECONNECT;
			else if (loc instanceof PoolDefConfig)
				tabIndex = TAB_POOL;
			int index = panelIndex(buttons[tabIndex]);
			tabs.setSelection(index);
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e);
		}
	}

	@Override
	public boolean revealConfigUI(Object config) {
		if (config instanceof HookConfig) {
			for (CTabItem tab : tabs.getItems()) {
				if (tab.getControl() instanceof HooksWidget) {
					tabs.setSelection(tab);
					return ((HooksWidget) tab.getControl()).revealConfigUI(config);
				}
			}
		}
		return super.revealConfigUI(config);
	}

	private boolean isReusingConnector(ConnectorConfig cc) {
		if (cc == null || cc instanceof FunctionConfig)
			return false;
		String s = cc.getConnectionConfig().getJavaClass();
		if (s != null && s.startsWith("@"))
			return true;
		return false;
	}
}
