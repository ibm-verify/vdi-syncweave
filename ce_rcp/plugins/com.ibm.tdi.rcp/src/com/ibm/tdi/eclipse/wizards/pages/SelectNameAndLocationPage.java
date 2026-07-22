/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.wizards.pages;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.tdi.eclipse.TDI;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.providers.AssemblyLineContentProvider;
import com.ibm.tdi.eclipse.providers.ConfigLabelProvider;

public class SelectNameAndLocationPage extends WizardPage  {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private AssemblyLineConfig alc;
	private BaseConfiguration selection;
	private int insertionPoint = 0;
	private String compName = null;
	private String compMode = ConnectorConfig.ITERATOR_MODE;
	private Text name;
	private TreeViewer tree;
	private Button before;
	private Button after;
	private Button into;
	private Button feedFlow;
	protected boolean feedFlowEnabled;
	
	public SelectNameAndLocationPage(String pageName) {
		super(pageName);
	}
	
	public void setSelection(BaseConfiguration selection) {
		this.selection = selection;
		this.alc = (AssemblyLineConfig) Utils.getParentConfig(selection, AssemblyLineConfig.class);
		if(selection instanceof AssemblyLineConfig)
			this.selection = alc.getDataFlowComponents();
		
		if(tree != null) {
			tree.setInput(alc);
			if(selection != null)
				tree.setSelection(new StructuredSelection(selection));
		}
		
		updateButtons();
	}

	private void updateButtons() {
		if(before == null)
			return;

		into.setEnabled((selection instanceof ContainerConfig));
		boolean pc = (selection.getParent() instanceof ContainerConfig);
		before.setEnabled(pc);
		after.setEnabled(pc);
		
		if(!before.isEnabled()) {
			this.insertionPoint = TDI.INSERT_INTO;
			into.setSelection(true);
			before.setSelection(false);
			after.setSelection(false);
		} else if(!into.isEnabled() && into.getSelection()) {
			this.insertionPoint = TDI.INSERT_AFTER;
			after.setSelection(true);
			into.setSelection(false);
			before.setSelection(false);
		}
		
	}

	public void createControl(Composite parent) {
		
		setTitle("Select Location");
		setDescription("Select the location for the component");
		
		Composite c = new Composite(parent, SWT.NULL);
		c.setLayout(new GridLayout(2, false));
		
		tree = new TreeViewer(c, SWT.BORDER);
		AssemblyLineContentProvider alcp = new AssemblyLineContentProvider(false, false);
		tree.setLabelProvider(new ConfigLabelProvider());
		tree.setContentProvider(alcp);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.horizontalSpan = 2;
		tree.getControl().setLayoutData(gd);
		
		if(alc != null) {
			tree.setInput(alc);
			if(selection != null)
				tree.setSelection(new StructuredSelection(selection));
		}
		tree.expandAll();
		
		tree.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection sel = (IStructuredSelection) event.getSelection();
					if (sel.getFirstElement() instanceof BaseConfiguration) {
						selection = (BaseConfiguration) sel.getFirstElement();
						updateButtons();
					}
				}
			}
		});
		
		Composite c2 = new Composite(c, SWT.NULL);
		c2.setLayout(new GridLayout(2, false));
		Label label = new Label(c2, SWT.LEFT);
		label.setText("Name: ");
		
		name = new Text(c2, SWT.BORDER);
		gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
		gd.widthHint = 200;
		name.setLayoutData(gd);
		name.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				compName = ((Text)e.widget).getText();
			}
		});
		name.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		if(selection != null) {
			compName = selection.getShortName();
			name.setText(compName);
		}
		
		new Label(c2, SWT.LEFT).setText("Insertion point: ");		
		before = new Button(c2, SWT.RADIO);
		before.setText("Before component");
		before.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				insertionPoint = 0;
			}
		});
		
		new Label(c2, SWT.LEFT).setText("");
		after = new Button(c2, SWT.RADIO);
		after.setText("After component");
		after.setSelection(true);
		after.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				insertionPoint = 1;
			}
		});

		new Label(c2, SWT.LEFT).setText("");
		into = new Button(c2, SWT.RADIO);
		into.setText("Into component");
		into.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				insertionPoint = 2;
			}
		});
		
		new Label(c2, SWT.LEFT).setText("");
		feedFlow = new Button(c2, SWT.CHECK);
		feedFlow.setText("Enable feed/flow sections");
		feedFlow.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				feedFlowEnabled = feedFlow.getSelection();
			}
		});
		
		updateButtons();
		
		setControl(c);
	}

	public void setComponentName(String componentName) {
		name.setText(componentName);
	}

	public String getComponentName() {
		return compName;
	}

	public int getInsertionPoint() {
		return insertionPoint;
	}

	public BaseConfiguration getLocation() {
		return selection;
	}

	public String getMode() {
		return compMode;
	}

	public void setMode(String str) {
		compMode = str;
	}

}
