/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.search;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.search.ui.ISearchPage;
import org.eclipse.search.ui.ISearchPageContainer;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.function.SystemFunctions;
import com.ibm.tdi.eclipse.Activator;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.editors.BaseEditor;

public class SearchPage extends DialogPage implements ISearchPage {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private Text text;

	private ISearchPageContainer searchPageContainer;

	private int searchScope;

	protected IStructuredSelection selection;

	private Button caseSensitive;

	private Object currentSelection;

	protected IResource currentFile;

//	private Button regularExpression;

	public SearchPage() {
	}

	public SearchPage(String title) {
		super(title);
	}

	public SearchPage(String title, ImageDescriptor image) {
		super(title, image);
	}

	public boolean performAction() {
		NewSearchUI.runQueryInBackground(new SearchQuery(text.getText(), searchScope, caseSensitive.getSelection(), false, selection));
		return true;
	}

	public void setContainer(ISearchPageContainer container) {
		this.searchPageContainer = container;
	}

	public void createControl(Composite parent) {
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayoutData(new GridData(GridData.FILL_BOTH));
		c.setLayout(new GridLayout(2, false));

		Label label = new Label(c, SWT.LEFT);
		label.setText(Messages.getString("SearchPage.text"));
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);

		text = new Text(c, SWT.BORDER | SWT.SINGLE);
		String str = Activator.getDefault().getPreferenceStore().getString("tdisearch.last.text");
		if(str != null)
			text.setText(str);
		
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		text.setFocus();
		
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				Activator.getDefault().getPreferenceStore().setValue("tdisearch.last.text", text.getText());
			}
		});
		
		caseSensitive = new Button(c, SWT.CHECK);
		caseSensitive.setText(Messages.getString("SearchPage.casesensitive"));

		Group g = new Group(c, SWT.NONE);
		g.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		g.setText(Messages.getString("SearchPage.scope"));
		g.setLayout(new GridLayout(1, false));
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		g.setLayoutData(gd);
		
		Button b;
		
		if (searchPageContainer != null) {
			if(searchPageContainer.getSelection() instanceof IStructuredSelection) {
				IStructuredSelection sel = (IStructuredSelection) searchPageContainer.getSelection();
				if (sel != null && sel.size() == 1 &&
						(sel.getFirstElement() instanceof IResource || 
						 sel.getFirstElement() instanceof TDIConfigurationFile))
					currentSelection = sel.getFirstElement();
			}
			
			// -- check if active editor is a TDI file
			if(currentSelection == null) {
				try {
					IEditorPart activeEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
					if(activeEditor instanceof BaseEditor)
						currentSelection = ((BaseEditor)activeEditor).getFile();
				} catch (Exception e) {
					SystemFunctions.doNothing();
				}
			}
			
			if(currentSelection != null) {
				if(currentSelection instanceof TDIConfigurationFile)
					currentFile = ((TDIConfigurationFile)currentSelection).getFile();
				else
					currentFile = (IResource)currentSelection;
				
				b = new Button(g, SWT.RADIO);
				if(currentFile instanceof IFile)
					b.setText(Messages.getMessage("SearchPage.scope.file", currentFile.getName()));
				else
					b.setText(Messages.getString("SearchPage.scope.selection"));
				
				b.setSelection(true);
				b.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						selection = new StructuredSelection(currentSelection);
						searchScope = ISearchPageContainer.SELECTION_SCOPE;
					}
				});
				selection = new StructuredSelection(currentSelection);
				searchScope = ISearchPageContainer.SELECTION_SCOPE;

				if(!(currentFile instanceof IProject)) {
					b = new Button(g, SWT.RADIO);
					b.setText(Messages.getMessage("SearchPage.scope.project", currentFile.getProject().getName()));
					b.addSelectionListener(new SelectionAdapter() {
						public void widgetSelected(SelectionEvent e) {
							selection = new StructuredSelection(currentFile.getProject());
							searchScope = ISearchPageContainer.SELECTION_SCOPE;
						}
					});
				}
			}
		}

		b = new Button(g, SWT.RADIO);
		b.setText(Messages.getString("SearchPage.scope.all"));
		b.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				searchScope = ISearchPageContainer.WORKSPACE_SCOPE;
			}
		});
	
		if (searchPageContainer == null || currentSelection == null) {
			searchScope = ISearchPageContainer.WORKSPACE_SCOPE;			
			b.setSelection(true);
		}
		setControl(c);		
	}
}
