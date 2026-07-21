/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.wizards.pages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;

import javax.naming.Binding;
import javax.naming.Name;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.ibm.di.config.base.BaseConfigurationImpl;
import com.ibm.di.config.eclipse.MetamergeConfigCE;
import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.ALMappingConfig;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.MetamergeFolder;
import com.ibm.di.config.interfaces.NamespaceConfig;
import com.ibm.di.config.interfaces.ScriptConfig;
import com.ibm.di.function.SystemFunctions;
import com.ibm.tdi.eclipse.Activator;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.TDI;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.providers.ConfigLabelProvider;
import com.ibm.tdi.eclipse.wizards.NewAssemblyLineWizard;
import com.ibm.tdi.eclipse.wizards.NewComponentBaseWizard;
import com.ibm.tdi.eclipse.wizards.NewConnectorWizard;
import com.ibm.tdi.eclipse.wizards.NewFunctionWizard;

public class ConfigTypePage extends BasePage {
	@SuppressWarnings("unused") //$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	
	private TreeViewer tree;
	private Text name;
	private Combo mode;
	private Text filter;
	private ConfigLabelProvider provider;
	private String defaultName;
	private boolean nameRequested;
	private boolean suggestName;
	private boolean modeRequested;
	private boolean showTypes;
	private boolean showFilter = true;
	private boolean changeConnectionOnlyOption = true;
	private boolean autoSelect = true;
	private String[] modeFilters;

	private boolean includeNullSelection;
	private BaseConfiguration nullSelection;

	/**
	 * Set when creating a new AssemblyLine
	 */
	private boolean simple = false;

	/**
	 * Set when choosing a connector for inheritance
	 */
	protected boolean connectionOnly = false;

	public ConfigTypePage(String pageName, String type) {
		super(pageName, type);
	}
	
	public String[] getModeFilters() {
		return modeFilters;
	}

	public void setModeFilters(String[] modeFilters) {
		this.modeFilters = modeFilters;
	}

	public boolean isNameRequested() {
		return nameRequested;
	}

	public void setNameRequested(boolean nameRequested) {
		this.nameRequested = nameRequested;
	}
	
	public void setSuggestName(boolean value) {
		suggestName = value;
	}
	
	public void setShowTypes(boolean value) {
		showTypes = value;
	}

	public boolean getShowTypes() {
		return showTypes;
	}
	
	public boolean isShowFilter() {
		return showFilter;
	}

	public void setShowFilter(boolean showFilter) {
		this.showFilter = showFilter;
	}

	@Override
	public boolean isPageComplete() {
		boolean complete = true;
		if (tree != null){
			if (tree.getSelection() == null)
				complete = false;
			else
				complete = !tree.getSelection().isEmpty();
		}
		
		if(complete && name != null) {
			if(name.getText().trim().length() == 0)
				complete = false;
			else if(getNameValidator() != null)
				complete = (getNameValidator().isValid(name.getText()) == null);
		}		
		return complete;
	}

	public String getComponentName() {
		if (name != null)
			return name.getText();
		else
			return null;
	}
	
	public String getMode() {
		if(mode != null)
			return Utils.internalMode(mode.getText());
		else
			return null;
	}

	public void createControl(Composite parent) {
		if (getShowTypes()) {
			setTitle(Messages.getString("ConfigTypePage.1")); //$NON-NLS-1$
			setDescription(Messages.getString("ConfigTypePage.2")); //$NON-NLS-1$
		} else {
			setTitle(Messages.getString("ConfigTypePage.5")); //$NON-NLS-1$
			setDescription(Messages.getString("ConfigTypePage.6")); //$NON-NLS-1$
		}
		
		try {
			Composite c = new Composite(parent, SWT.NULL);
			c.setLayout(new GridLayout(2, false));
			
			if (getShowTypes()) {

				// -- Filter
				if(isShowFilter()) {
					new Label(c, SWT.LEFT).setText(Messages.getString("ConfigTypePage.3")); //$NON-NLS-1$
					filter = new Text(c, SWT.BORDER);
					filter.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
	
					filter.addKeyListener(new KeyAdapter() {
						public void keyReleased(KeyEvent e) {
							tree.refresh();
							if(autoSelect && tree.getTree().getItemCount() == 1) {
								tree.getTree().setSelection(tree.getTree().getItem(0));
								updateConfigObject();
							}
						}
					});
				}

				tree = new TreeViewer(c, SWT.BORDER);
				GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
				gd.horizontalSpan = 2;
				tree.getTree().setLayoutData(gd);

				IProject project = ((NewComponentBaseWizard)getWizard()).getSelectionProject();
				provider = new FileConfigLabelProvider(project);
				provider.setConsultingInfFiles(true);
				provider.setSimpleConnectorIcon(true);
				tree.setLabelProvider(provider);

				tree.setContentProvider(new ComponentContentProvider(provider));

				// Get names from packages
				List<MetamergeConfig> treeInput = MetamergeConfigFactory.getPackages();
				treeInput.add(MetamergeConfigFactory.getNamespace(MetamergeConfigFactory.SYSTEM_NAMESPACE));
				if(project != null) {
					try {
						MetamergeConfig mc = Utils.getProjectMC(project);
						treeInput.add(mc);
						MetamergeFolder nameSpaces = (MetamergeFolder) mc.lookup(MetamergeConfig.DEFAULT_NAMESPACE_FOLDER);
						Enumeration<Binding> l = nameSpaces.list();
						while (l.hasMoreElements()) {
							NamespaceConfig nc = (NamespaceConfig) l.nextElement().getObject();
							treeInput.add((MetamergeConfigFactory.loadNamespace(nc)));
						}			
					} catch (Exception e) {
						EclipseAppender.logerror(e.toString(), e, getShell());
					}
				}
				tree.setInput(treeInput);

				tree.addSelectionChangedListener(new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent event) {
						updateConfigObject();
					}
				});

				if(getConfigObject() != null) {
					tree.setSelection(new StructuredSelection(getConfigObject()));
				}

				if(filter != null) {
					tree.addFilter(new ViewerFilter() {
						@Override
						public boolean select(Viewer viewer, Object parentElement, Object element) {
							if(filter.getText().length() == 0)
								return true;
	
							String str = provider.getText(element);
							return str.toLowerCase().indexOf(filter.getText().toLowerCase()) != -1;
						}
	
					});
				}
			}
			
			// -- Component name
			if(isNameRequested()) {
				Label l = new Label(c, SWT.LEFT);
				l.setText(Messages.getString("ConfigTypePage.4")); //$NON-NLS-1$
				
				name = new Text(c, SWT.BORDER);
				name.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
				name.addFocusListener(new FocusListener() {
					public void focusGained(FocusEvent e) {
						name.selectAll();
					}
					public void focusLost(FocusEvent e) {
					}
				});
				name.addKeyListener(new KeyAdapter() {
					public void keyReleased(KeyEvent e) {
						setPageComplete(isPageComplete());
					}
				});
				
				name.addModifyListener(new ModifyListener() {
					public void modifyText(ModifyEvent e) {
						if(getNameValidator() != null) {
							setErrorMessage(getNameValidator().isValid(name.getText()));
						}
					}
				});
				
				if(getWizard() instanceof NewAssemblyLineWizard) {
					new Label(c, SWT.LEFT).setText("");
					final Button simpleb = new Button(c, SWT.CHECK);
					simpleb.setText(Messages.getString("AssemblyLineWizard.simple"));
					simpleb.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
					simpleb.addSelectionListener(new SelectionAdapter() {
						public void widgetSelected(SelectionEvent e) {
							simple = simpleb.getSelection();
						}
					});
				}
			}

			// -- Mode is requested for new connectors
			if(isModeRequested()) {
				Label ml = new Label(c, SWT.LEFT);
				ml.setText(Messages.getString("Connector.ModeCB.label")); //$NON-NLS-1$
				mode = new Combo(c, SWT.DROP_DOWN|SWT.READ_ONLY);
				mode.setText("                 "); //$NON-NLS-1$
			} else if (isChangeConnectionOnlyOption() &&
					(getWizard() instanceof NewConnectorWizard || getWizard() instanceof NewFunctionWizard)) {
				new Label(c, SWT.LEFT).setText("");
				final Button simpleb = new Button(c, SWT.CHECK);
				simpleb.setText(Messages.getString("ConfigTypePage.connection.only"));
				simpleb.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
				simpleb.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						setConnectionOnly(simpleb.getSelection());
					}
				});
			}

			setControl(c);
			
		} catch (Exception e) {
			EclipseAppender.logerror(Messages.getMessage("configtype.createcontrol.err", getType(), e.toString()) , e); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	private void updateConfigObject() {
		IStructuredSelection sel = (IStructuredSelection) tree.getSelection();
		if(!sel.isEmpty()) {
			Object o = sel.getFirstElement();
			if ( o instanceof String ) {
				if(getConfigObject() != null)
					getConfigObject().setInheritsFromRef((String)o);
				setPageComplete(isPageComplete());
				return;
			}
			if (! (o instanceof BaseConfiguration))
				return;
			BaseConfiguration sc = (BaseConfiguration) o;
			String ns = (String) MetamergeConfigFactory.getNamespaceFor(sc);
			String inheritFrom = ns + ":/" + sc.getName(); //$NON-NLS-1$

			// Check if there is a local namespace we should use instead
			IProject selproject = ((NewComponentBaseWizard)getWizard()).getSelectionProject();
			if(selproject != null) {
				try {
					MetamergeConfig mc = Utils.getProjectMC(selproject);
					Object lns = MetamergeConfigFactory.getLocalNamespaceFor(mc, sc);
					if (lns instanceof String)
						inheritFrom = lns.toString()+ ":/" + sc.getName();
				} catch (Exception e) {
					EclipseAppender.logerror(e.toString(), e, getShell());
				}
			}

			if(sc.getMetamergeConfig() instanceof MetamergeConfigCE || sc.getMetamergeConfig() instanceof TDIConfigurationFile) {
				IProject project;
				if(sc.getMetamergeConfig() instanceof TDIConfigurationFile)
					project = ((TDIConfigurationFile)sc.getMetamergeConfig()).getProject();
				else
					project = ((MetamergeConfigCE)sc.getMetamergeConfig()).getProject();
				if(project == selproject) {
					inheritFrom = "/" + sc.getName(); //$NON-NLS-1$
				}
			}
			
			if (sc == nullSelection )
				inheritFrom = BaseConfiguration.INHERIT_NONE;

			((NewComponentBaseWizard) getWizard()).createConfigObject();

			if(getConfigObject() != null)
				getConfigObject().setInheritsFromRef(inheritFrom);
			
			if(mode != null) {
				mode.removeAll();
				if(sc instanceof FunctionConfig) {
					mode.setEnabled(false);
				} else {
					mode.setEnabled(true);
					for(String str : Utils.getSupportedModes((ConnectorConfig) getConfigObject()))
						mode.add(Utils.externalMode(str));
					if(mode.getItemCount() > 0)
						mode.select(0);
				}
				mode.getParent().layout();
			}

			if (name != null && suggestName) {
				String s = name.getText();
				if(s.length() == 0 || s.equals(defaultName)) {
					setDefaultName(sc.getShortName());
				}
			}
		}
		setPageComplete(isPageComplete());
	}

	private void setDefaultName(String shortName) {
		String str = shortName;
		if(str == null)
			return;
		
		if(str.indexOf(".") != -1) //$NON-NLS-1$
			str = str.substring(str.indexOf(".")+1); //$NON-NLS-1$
		
		str = str.replaceAll("[ -.]", "_"); //$NON-NLS-1$ //$NON-NLS-2$
		defaultName = str;
		name.setText(str);
	}

	public boolean isModeRequested() {
		return modeRequested;
	}

	public void setModeRequested(boolean modeRequested) {
		this.modeRequested = modeRequested;
	}

	@Override
	public void setType(String type) {
		super.setType(type);
		try {
			tree.setInput(
				MetamergeConfigFactory.getNamespace(MetamergeConfigFactory.SYSTEM_NAMESPACE).lookup(getType()));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private class ComponentContentProvider implements ITreeContentProvider {

		private ConfigLabelProvider provider;
		public ComponentContentProvider(ConfigLabelProvider provider) {
			this.provider = provider;
		}

		public Object[] getChildren(Object parentElement) {
			ArrayList<BaseConfiguration> list = new ArrayList<BaseConfiguration>();
			if(parentElement instanceof List) {
				for(MetamergeConfig mc : (List<MetamergeConfig>) parentElement) {
					try {
						getFolderContents((MetamergeFolder) mc.lookup(getType()), list);
					} catch (Exception e) {
						continue;
					}
				}
			}
			Collections.sort(list, new Comparator<BaseConfiguration>() {
				public int compare(BaseConfiguration arg0, BaseConfiguration arg1) {
					String s1 = provider.getText(arg0).toLowerCase();
					String s2 = provider.getText(arg1).toLowerCase();
					MetamergeConfig ns = MetamergeConfigFactory.getNamespace(MetamergeConfigFactory.SYSTEM_NAMESPACE);
					if(arg0.getMetamergeConfig() == ns && arg1.getMetamergeConfig() != ns) {
						return 1;
					} else if (arg0.getMetamergeConfig() != ns && arg1.getMetamergeConfig() == ns) {
						return -1;
					} else {
						return s1.compareTo(s2);
					}
				}
			});

			
			if(getModeFilters() != null) {
				ArrayList<BaseConfiguration> filteredList = new ArrayList<BaseConfiguration>();
				for(BaseConfiguration b : list) {
					if(b instanceof ConnectorConfig) {
						ConnectorConfig cc = (ConnectorConfig) b;
						ArrayList<String> supported = Utils.getSupportedModes(cc);
						for(String mode : getModeFilters()) {
							if(supported.contains(mode)) {
								filteredList.add(cc);
								break;
							}
						}
					}
				}
				list = filteredList;
			}
			
			if(includeNullSelection) {
				nullSelection = new BaseConfigurationImpl();
				try {
					nullSelection.init();
					nullSelection.setName("(" + Messages.getString("RunOptionsWidget.20") + ")");
				} catch (Exception e) {
					e.printStackTrace();
				}
				list.add(0, nullSelection);
			}
			
			return addReuseConnectors(list);
		}
		
		private void getFolderContents(MetamergeFolder folder, List<BaseConfiguration> list) {
			try {
				for (Enumeration<Binding> e = folder.list(); e.hasMoreElements();)
					list.add((BaseConfiguration) e.nextElement().getObject());
			} catch (Exception err) {
				EclipseAppender.logerror(err.toString(), err);
			}
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			return element instanceof Object[];
		}

		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		public void dispose() {}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
	}

	private Object[] addReuseConnectors(List<BaseConfiguration> list) {
		if ( ! (getWizard() instanceof NewComponentBaseWizard))
			return list.toArray();
		
		IStructuredSelection sel = ((NewComponentBaseWizard)getWizard()).getSelection();
		if(sel != null && !sel.isEmpty()) {
			Object obj = sel.getFirstElement();
			if(isConnector(obj)) {
				ConnectorConfig cc = (ConnectorConfig) obj;
				AssemblyLineConfig alc = Utils.getParentConfig(cc, AssemblyLineConfig.class);
				if (alc != null) {
					List<Object> ret = new ArrayList<Object>(list);
					List<BaseConfiguration> connectors = alc.getEntryFeedComponents().getConfigurations(null);
					alc.getDataFlowComponents().getConfigurations(connectors);
					for (BaseConfiguration bc:connectors) {
						if (isConnector(bc) && bc != cc) {
							String s = bc.getInheritsFromRef();
							if (s!= null && ! s.startsWith("@"))
								ret.add(0, "@" + bc.getShortName());
						}
					}
					return ret.toArray();
				}
			}
		}
		return list.toArray();
	}
	
	private boolean isConnector(Object bc) {
		return bc instanceof ConnectorConfig 
		&& ! (bc instanceof FunctionConfig)
		&& ! (bc instanceof ALMappingConfig);
	}
	
	private class FileConfigLabelProvider extends ConfigLabelProvider {

		private MetamergeConfig baseMC;
		
		public FileConfigLabelProvider(IProject project) {
			super();
			if(project != null) {
				try {
					baseMC = Utils.getProjectMC(project);
				} catch (Exception e) {
					SystemFunctions.doNothing();
				}
			}
		}

		@Override
		public String getText(Object element) {
			if ( element instanceof String) {
				return Messages.getMessage("ConfigTable.Reuse", ((String)element).substring(1));
			}
			BaseConfiguration bc = (BaseConfiguration) element;
			
			// -- templates always use short name
			if(TDI.isTemplate(bc) || bc == nullSelection)
				return bc.getShortName();

			String title;
			if(bc instanceof ScriptConfig)
				title = Messages.getString("SelectComponentPage.7");
			else if(bc instanceof ALMappingConfig || bc instanceof AttributeMapConfig)
				title = Messages.getString("SelectComponentPage.8");
			else
				title = super.getText(element);
			
			Name ns = (Name) bc.getNamespace();
			if(ns != null && ns.get(0).equals(MetamergeConfigFactory.SYSTEM_NAMESPACE)) {
				// -- for system namespace we use the title from the tdi.xml file
				SystemFunctions.doNothing();
				
			} else {
				// -- everything else we use short name, with a parenthesis containing title and possibly a namespace
				// See also WI DI011115.
				String prefix = null;
				if (ns != null){
					if(baseMC != null)
						prefix = (String)MetamergeConfigFactory.getLocalNamespaceFor(baseMC, bc);
					else if (ns.size() > 0 && ! ns.get(0).equals(MetamergeConfigFactory.ADAPTERS_NAMESPACE))
						prefix = ns.get(0);
				}
				if(prefix != null)
					title = bc.getShortName()  + " (" + prefix + ":" + title + ")";
				else
					title = bc.getShortName()  + " (" + title + ")";
			}
			
			return title;
		}

		@Override
		public Image getImage(Object element) {
			if (element instanceof String) {
				return Activator.getImage("Connector", true);
			}
			return super.getImage(element);
		}
	}

	public void setIncludeNullSelection(boolean includeNullSelection) {
		this.includeNullSelection = includeNullSelection;
	}
	
	public boolean isSimpleAssemblyLine() {
		return simple;
	}
	
	public boolean isConnectionOnly() {
		return connectionOnly;
	}
	
	public void setConnectionOnly(boolean connectionOnly) {
		this.connectionOnly = connectionOnly;
	}
	
	public boolean isChangeConnectionOnlyOption() {
		return changeConnectionOnlyOption;
	}

	public void setChangeConnectionOnlyOption(boolean changeConnectionOnlyOption) {
		this.changeConnectionOnlyOption = changeConnectionOnlyOption;
	}

	public void setAutoSelect(boolean value) {
		autoSelect = value;
	}
}
