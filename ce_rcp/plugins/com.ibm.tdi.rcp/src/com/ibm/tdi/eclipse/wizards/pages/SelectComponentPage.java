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
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.ALMappingConfig;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.BranchingConfig;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.LoopConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.MetamergeFolder;
import com.ibm.di.config.interfaces.NamespaceConfig;
import com.ibm.di.config.interfaces.ScriptConfig;
import com.ibm.di.function.SystemFunctions;
import com.ibm.tdi.eclipse.ConfigUtils;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.TDI;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.providers.ConfigLabelProvider;
import com.ibm.tdi.eclipse.providers.MetamergeFolderContentProvider;
import com.ibm.tdi.eclipse.wizards.ConnectorConfigWizard;

public class SelectComponentPage extends WizardPage implements SelectionListener, ISelectionProvider {
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private ArrayList<Button> buttons = new ArrayList<Button>();

	private TableViewer table;

	private Text search;

	private MetamergeFolderContentProvider provider;

	private ConfigLabelProvider labelProvider;

	private String name;

	private BaseConfiguration selectedObject;

	private ArrayList<ISelectionChangedListener> listeners = new ArrayList<ISelectionChangedListener>();

	private FormToolkit tk;

	private Text compName;

	private Combo modeCombo;

	private Label modeLabel;

	private boolean showButtons = true;
	
	private IInputValidator validator;
	
	private final static String PLACEHOLDER = Messages.getString("SelectComponentPage.search.placeholder");

	private final static String MODIFIED = "modified"; // A property name
	
	private final static String EMPTY_SCRIPT = Messages.getString("TDI.ScriptLabel.0").toLowerCase();
	private final static String EMPTY_MAP = Messages.getString("Localized.AttributeMap").toLowerCase();
	
	public SelectComponentPage(String pageName) {
		super(pageName);
		setTitle(Messages.getString("SelectComponentPage.1")); //$NON-NLS-1$
		setDescription(Messages.getString("SelectComponentPage.2")); //$NON-NLS-1$
		setPageComplete(false);
	}

	public String getComponentName() {
		return name;
	}

	public void createControl(Composite parent) {

		tk = new FormToolkit(parent.getDisplay());
		Form frm = tk.createForm(parent);
		frm.getBody().setLayout(new GridLayout(2, false));

		Section s1 = tk.createSection(frm.getBody(), Section.TITLE_BAR);
		s1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));

		Composite c1 = tk.createComposite(s1);
		c1.setLayout(new GridLayout(1, false));

		if (showButtons) {
			s1.setText(Messages.getString("SelectComponentPage.3")); //$NON-NLS-1$
			// All Components
			String[] items = new String[] {
					"SelectComponentPage.4",  //All Components
					"SelectComponentPage.5",  //Connectors
					"SelectComponentPage.Functions",  //Functions
					"SelectComponentPage.6",  //$NON-NLS-1$ 
					"SelectComponentPage.7",  //$NON-NLS-1$ 
			"SelectComponentPage.8" }; //$NON-NLS-1$ 
			for (int i = 0; i < items.length; i++) {
				String str = Messages.getString(items[i]);
				Button b = tk.createButton(c1, str, SWT.RADIO);
				b.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						selectButton(buttons.indexOf(e.widget));
					}
				});
				buttons.add(b);
			}
		} else {
			s1.setText(Messages.getString("SelectComponentPage.Filter")); //$NON-NLS-1$
		}
		
		// Search box
		createSearchBox(c1);

		s1.setClient(c1);

		Section s2 = tk.createSection(frm.getBody(), Section.TITLE_BAR);
		s2.setText(Messages.getString("SelectComponentPage.9")); //$NON-NLS-1$
		s2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite c2 = tk.createComposite(s2);
		c2.setLayout(new GridLayout(1, true));
		
		Composite c20 = tk.createComposite(c2);
		c20.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		TableColumnLayout layout = new TableColumnLayout();
		c20.setLayout(layout);
		table = new TableViewer(c20, SWT.BORDER | SWT.V_SCROLL);
		table.getTable().setHeaderVisible(true);
		table.getTable().setLinesVisible(true);
		table.addFilter(new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (search.getText().length() == 0 || search.getText().equals(PLACEHOLDER))
					return true;

				String str = labelProvider.getText(element);
				if (str == null)
					return true;
				else
					return str.toLowerCase().indexOf(search.getText().toLowerCase()) != -1;
			}

		});
		table.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection().isEmpty())
					updateDefaultName(null);
				else
					updateDefaultName((BaseConfiguration) ((IStructuredSelection) event.getSelection()).getFirstElement());
			}
		});

		labelProvider = new FileConfigLabelProvider(getBaseProject());
		labelProvider.setConsultingInfFiles(true);
		table.setLabelProvider(labelProvider);

		provider = new MetamergeFolderContentProvider();
		table.setContentProvider(provider);

		TableColumn col = new TableColumn(table.getTable(), SWT.LEFT);
		layout.setColumnData(col, new ColumnWeightData(100));
		col.setText(Messages.getString("SelectComponentPage.10")); //$NON-NLS-1$

		//
		// Component name
		//
		Composite c21 = tk.createComposite(c2);
		c21.setLayout(new GridLayout(2, false));
		c21.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));

		tk.createLabel(c21, Messages.getString("ConfigTable.Name")); //$NON-NLS-1$
		// Add some code to try to force painting of borders on old Windows...
		tk.setBorderStyle(SWT.BORDER);
		tk.paintBordersFor(c21);
		
		compName = tk.createText(c21, "", SWT.BORDER); //$NON-NLS-1$
		compName.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		compName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (name != null && name.equals(compName.getText()))
					return;
				name = compName.getText().trim();
				if (name.length() == 0) {
					compName.setData(MODIFIED, null); 
					setPageComplete(false);
					//TODO: We could set an error message here, "missing name" or something.
					setErrorMessage(null);
				} else {
					compName.setData(MODIFIED, Boolean.TRUE);
					setPageComplete(selectedObject != null);
					if (validator != null) {
						String msg = validator.isValid(name);
						setErrorMessage(msg);
						if (msg != null)
							setPageComplete(false);
					}
				}
			}
		});

		modeLabel = tk.createLabel(c21, Messages.getString("Connector.ModeCB.label")); //$NON-NLS-1$
		
		modeCombo = new Combo(c21, SWT.DROP_DOWN|SWT.READ_ONLY);
		modeCombo.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		modeCombo.addSelectionListener(new SelectionAdapter() {		
			public void widgetSelected(SelectionEvent e) {
				((ConnectorConfig)getSelectedObject()).setMode(Utils.internalMode(modeCombo.getText()));
			}
		});
		
		updateModeCombo();
		
		s2.setClient(c2);

		setControl(frm);

		if (showButtons)
			selectButton(0);
		else
			updateTableContent(1);
		
		// -- make search field have initial keyboard focus
		search.setFocus();
	}

	private IProject getBaseProject() {
		BaseConfiguration location = ((ConnectorConfigWizard) getWizard()).getLocation();
		if(location != null && location.getMetamergeConfig() instanceof TDIConfigurationFile)
			return ((TDIConfigurationFile)location.getMetamergeConfig()).getProject();
		else
			return null;
	}

	public Control createSearchBox(Composite parent) {

		Composite filt = new Composite(parent, 0);
		GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
		filt.setLayoutData(gd);
		filt.setLayout(new GridLayout(2, false));

		Label label = new Label(filt, SWT.LEFT);
		label.setText(Messages.getString("PropertyStoreUI.Search")); //$NON-NLS-1$
		
		search = new Text(filt, SWT.BORDER|SWT.SINGLE);
		search.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		search.setText(PLACEHOLDER);
		search.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
			}

			public void keyReleased(KeyEvent e) {
				table.refresh();
				if (table.getTable().getItemCount() == 1) {
					table.getTable().select(0);
					updateDefaultName((BaseConfiguration) ((IStructuredSelection) table.getSelection()).getFirstElement());
				}
			}
		});
		
		search.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				if(PLACEHOLDER.equals(search.getText()))
					search.setText("");
			}
			public void focusLost(FocusEvent e) {
				if(search.getText().length() == 0)
					search.setText(PLACEHOLDER);
			}
		});
		
		return search;

	}

	private void updateDefaultName(BaseConfiguration c) {
		if (c == null) {
			selectedObject = null;
			setPageComplete(false);
			updateModeCombo();
			return;
		}

		//
		// Generate the default name based on the component's title
		//
		String str = labelProvider.getText(c);
		if (str == null) {
			str = c.getShortName();
			if ((str != null) && (str.indexOf(".") != -1)) { //$NON-NLS-1$
				str = str.substring(str.indexOf(".") + 1); //$NON-NLS-1$
			}
		}
		if (str == null)
			str = "";

		str = str.replaceAll("[ ]", ""); //$NON-NLS-1$ //$NON-NLS-2$
		str = str.replaceAll("[-.]", "_"); //$NON-NLS-1$ //$NON-NLS-2$
		
		// -- chop off the (namespace:type) part
		if(str.endsWith(")")) {
			int last = str.lastIndexOf("(");
			if(last != -1)
				str = str.substring(0,last);
		}

		//
		// Make it unique
		//
		String unique = str;
		AssemblyLineConfig alc = ((ConnectorConfigWizard) getWizard()).getAssemblyLineConfig();
		if (alc != null) {
			int i = 1;
			while (alc.getComponent(unique) != null)
				unique = str + "_" + i++; //$NON-NLS-1$
		}

		try {
			BaseConfiguration location = ((ConnectorConfigWizard) getWizard()).getLocation();
			selectedObject = ConfigUtils.createInheritedComponent(location.getMetamergeConfig(), c);
			if(selectedObject == c)
				selectedObject = (BaseConfiguration) c.getClone();
			
			if (selectedObject instanceof ConnectorConfig) {
				selectedObject.setParent(location); // Temporarily, to make some
													// scripts work better
				((ConnectorConfig) selectedObject).setMode(((ConnectorConfig)c).getMode());
				((ConnectorConfig) selectedObject).setState(((ConnectorConfig)c).getState());
			}
			updateModeCombo();

		} catch (Exception e) {
			selectedObject = null;
			return;
		}

		if (unique != null && compName.getData(MODIFIED) == null) {
			name = unique;
			compName.setText(name);
		}

		setPageComplete((name != null && name.length() > 0 && selectedObject != null));

		// Notify
		SelectionChangedEvent event = new SelectionChangedEvent(this, new StructuredSelection(selectedObject));
		for (ISelectionChangedListener l : listeners) {
			l.selectionChanged(event);
		}
	}

	private void updateModeCombo() {
		boolean enable = getSelectedObject() instanceof ConnectorConfig;
		if (getSelectedObject() instanceof FunctionConfig || getSelectedObject() instanceof ALMappingConfig) {
			enable = false;
		}
		modeCombo.removeAll();
		modeCombo.setEnabled(enable);
		modeLabel.setEnabled(enable);
		if(!enable)
			return;
		
		try {
			for(String mode : Utils.getSupportedModes((ConnectorConfig) selectedObject))
				modeCombo.add(Utils.externalMode(mode));
			
			String cur = ((ConnectorConfig)getSelectedObject()).getMode();
			int index = 0;
			if(cur != null)
				index = modeCombo.indexOf(Utils.externalMode(cur));
			if (index == -1)
				index = 0;
			modeCombo.select(index);
			if (index < modeCombo.getItemCount())
				((ConnectorConfig)getSelectedObject()).setMode(Utils.internalMode(modeCombo.getItem(index)));
		} catch (Exception e) {
		}
	}

	public void widgetDefaultSelected(SelectionEvent e) {
	}

	public void widgetSelected(SelectionEvent e) {
		int index = buttons.indexOf(e.getSource());
		if (index != -1)
			selectButton(index);
	}

	private void selectButton(int index) {
		for (Button b : buttons)
			b.setSelection(false);
		if (buttons.size() > index) {
			buttons.get(index).setSelection(true);
			updateTableContent(index);
		}
	}

	private void updateTableContent(final int index) {
		try {
			ArrayList<BaseConfiguration> list = new ArrayList<BaseConfiguration>();
			MetamergeConfig system = MetamergeConfigFactory.getNamespace(MetamergeConfigFactory.SYSTEM_NAMESPACE);
			BaseConfiguration location = ((ConnectorConfigWizard) getWizard()).getLocation();
			MetamergeConfig project = null;
			if(location != null && location.getMetamergeConfig() instanceof TDIConfigurationFile)
				project = Utils.getProjectMC(((TDIConfigurationFile)location.getMetamergeConfig()).getProject());

			// Get names from packages
			List<MetamergeConfig> packages = MetamergeConfigFactory.getPackages();
			packages.add(system);
			if(project != null)
				packages.add(project);
			addIncludedNS(packages,project);
			
			MetamergeConfig[] mc = packages.toArray(new MetamergeConfig[0]); 

			switch (index) {
			case 0:
				addFiles(list, mc, MetamergeConfig.CONNECTOR_FOLDER, MetamergeConfig.FUNCTION_FOLDER,
						MetamergeConfig.ATTRIBUTEMAP_FOLDER, MetamergeConfig.SCRIPT_FOLDER );
				list.add((BaseConfiguration) system.lookup(TDI.DEFAULT_CONTROLFLOW_FOLDER));
				break;
			case 1:
				addFiles(list, mc, MetamergeConfig.CONNECTOR_FOLDER);
				break;

			case 2:
				addFiles(list, mc, MetamergeConfig.FUNCTION_FOLDER );
				break;

			case 3:
				list.add((BaseConfiguration) system.lookup(TDI.DEFAULT_CONTROLFLOW_FOLDER));
				break;

			case 4:
				addFiles(list, mc, MetamergeConfig.SCRIPT_FOLDER);
				break;
			case 5:
				addFiles(list, mc, MetamergeConfig.ATTRIBUTEMAP_FOLDER);
				break;
			}

			MetamergeFolderContentProvider tmp = new MetamergeFolderContentProvider();
			ArrayList<BaseConfiguration> expandedList = new ArrayList<BaseConfiguration>();
			for (Object obj : tmp.getChildren(list.toArray())) {
				expandedList.add((BaseConfiguration) obj);
			}

			Collections.sort(expandedList, new Comparator<BaseConfiguration>() {
				public int compare(BaseConfiguration arg0, BaseConfiguration arg1) {
					// -- Sort branches
					if(index == 3 && arg0 instanceof BranchingConfig && arg1 instanceof BranchingConfig) {
						int a = ((BranchingConfig)arg0).getBranchType();
						int b = ((BranchingConfig)arg1).getBranchType();
						if(a > 2)
							a += 3;
						if(b > 2)
							b += 3;
						if(arg0 instanceof LoopConfig)
							a += 3;
						if(arg1 instanceof LoopConfig)
							b += 3;
						if (a < b)
							return -1;
						if (b < a)
							return 1;
					}

					String s1 = labelProvider.getText(arg0).toLowerCase();
					String s2 = labelProvider.getText(arg1).toLowerCase();

					// -- Make sure "Script" appears first in the list of scripts
					if(index == 4 && arg0 instanceof ScriptConfig && arg1 instanceof ScriptConfig) {
						if(s1.equals(EMPTY_SCRIPT))
							return -1;
						else if (s2.equals(EMPTY_SCRIPT))
							return 1;
					}
					
					// -- Make sure empty AttributeMap is first
					if(index == 5 && arg0 instanceof ALMappingConfig && arg1 instanceof ALMappingConfig) {
						if(s1.equals(EMPTY_MAP))
							return -1;
						else if (s2.equals(EMPTY_MAP))
							return 1;
					}
					return s1.compareTo(s2);
				}
			});

			table.setInput(expandedList.toArray());
		} catch (Exception e) {
			EclipseAppender.logerror(e.getMessage(), e);
		}
	}

	private void addIncludedNS(List<MetamergeConfig> list, MetamergeConfig project) {
		if (project == null)
			return;
		try {
			MetamergeFolder mf = (MetamergeFolder) project.lookup(MetamergeConfig.DEFAULT_NAMESPACE_FOLDER);
			Enumeration<Binding> l = mf.list();
			while (l.hasMoreElements()) {
				NamespaceConfig nc = (NamespaceConfig) l.nextElement().getObject();
				list.add((MetamergeConfigFactory.loadNamespace(nc)));
			}			
		} catch (Exception e) {
			return;
		}
	}
	
	private void addFiles(ArrayList<BaseConfiguration> list, MetamergeConfig[] nslist, int... is) {
		for(MetamergeConfig mc : nslist) {
			if(mc == null)
				continue;
			for (int i : is) {
				try {
					list.add(mc.getDefaultFolder(i));
				} catch (Exception e) {
					EclipseAppender.logerror(e.toString(), e);
				}
			}
		}
	}

	@Override
	public void dispose() {
		super.dispose();
		if (tk != null) {
			try {
				tk.dispose();
			} catch (Throwable e) {
				EclipseAppender.logerror(e.toString(), e);
			} finally {			
				tk = null;
			}
		}
	}

	public BaseConfiguration getSelectedObject() {
		return selectedObject;
	}

	public void setNameValidator(IInputValidator validator) {
		this.validator = validator;
	}

	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}

	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.remove(listener);
	}

	public ISelection getSelection() {
		if (selectedObject != null)
			return new StructuredSelection(selectedObject);
		else
			return StructuredSelection.EMPTY;
	}

	public void setSelection(ISelection selection) {
	}

	public void setShowButtons(boolean showButtons) {
		this.showButtons = showButtons;
	}

	private static class FileConfigLabelProvider extends ConfigLabelProvider {

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
			BaseConfiguration bc = (BaseConfiguration) element;
			String title = super.getText(element);
			
			// -- templates always use short name
			if(TDI.isTemplate(bc))
				return bc.getShortName();

			if(bc instanceof ScriptConfig)
				title = Messages.getString("SelectComponentPage.7");
			else if(bc instanceof ALMappingConfig || bc instanceof AttributeMapConfig)
				title = Messages.getString("SelectComponentPage.8");
			
			Name ns = (Name) bc.getNamespace();
			if(ns != null && ns.get(0).equals(MetamergeConfigFactory.SYSTEM_NAMESPACE)) {
				// -- for system namespace we use the title from the tdi.xml file
				SystemFunctions.doNothing();
				
			} else if(ns != null && ns.get(0).equals(MetamergeConfigFactory.ADAPTERS_NAMESPACE)) {
				title = bc.getShortName() + " (" + title + ")";
				
			} else {
				// -- everything else we use short name, with a parenthesis containing title and possibly a namespace
				// See also WI DI011115.
				String prefix = null;
				if (ns != null){
					if(baseMC != null)
						prefix = (String)MetamergeConfigFactory.getLocalNamespaceFor(baseMC, bc);
					else if (ns.size() > 0)
						prefix = ns.get(0);
				}
				if(prefix != null)
					title = bc.getShortName()  + " (" + prefix + ":" + title + ")";
				else
					title = bc.getShortName()  + " (" + title + ")";
			}
			
			return title;
		}

	}

}
