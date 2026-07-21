/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.wizards.pages;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.actions.operations.RefactorOperation;
import com.ibm.tdi.eclipse.log.EclipseAppender;

public class RefactorComponentPage extends WizardPage {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private IResource resource;
	private CheckboxTableViewer table;
	private ArrayList<IFile> list;

	private ArrayList<IFile> editors;

	public RefactorComponentPage(String pageName, IResource resource) {
		super(pageName);
		this.resource = resource;
		setTitle(Messages.getString("rename.update.refs"));
		setDescription(Messages.getString("rename.update.refs.desc"));
		findReferences();
	}

	public void createControl(Composite parent) {
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayout(new GridLayout(1, true));
		Label l = new Label(c, SWT.LEFT);
		l.setText(Messages.getString("rename.update.refs.filter"));

		table = CheckboxTableViewer.newCheckList(c, SWT.BORDER);
		table.setContentProvider(new ArrayContentProvider());
		table.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		table.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				IFile file = (IFile) element;
				String str = "/" + file.getParent().getName() + "/" + file.getName().substring(0, file.getName().lastIndexOf("."));
				if(editors.contains(file)) {
					str += " " + Messages.getString("rename.update.refs.editing");
				}
				return str;
			}
		});

		table.setInput(list);
		table.setAllChecked(true);
		for(IFile file : editors)
			table.setChecked(file, false);

		Composite butts = new Composite(c, SWT.NONE);
		butts.setLayout(new RowLayout(SWT.HORIZONTAL));
		butts.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Button b = new Button(butts, SWT.PUSH);
		b.setText(Messages.getString("DiscoverSchemaWidget.6"));
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				table.setAllChecked(true);
			}
		});

		b = new Button(butts, SWT.PUSH);
		b.setText(Messages.getString("DiscoverSchemaWidget.8"));
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				table.setAllChecked(false);
			}
		});

		setControl(c);
	}

	public List<IFile> getRefactorItems() {

		ArrayList<IFile> refactor = new ArrayList<IFile>();
		for (IFile str : list) {
			if (table.getChecked(str))
				refactor.add(str);
		}

		return refactor;
	}

	public void findReferences() {
		String name = resource.getName().substring(0, resource.getName().lastIndexOf("."));
		String folder = TDIConfigurationFile.getFolderForExtension(resource.getFileExtension());
		String ref = "/" + folder + "/" + name;
		list = new ArrayList<IFile>();
		editors = new ArrayList<IFile>();
		String scriptRef = TDIConfigurationFile.XT_SCRIPT.equals(resource.getFileExtension()) ? name : null;
		

		try {
			for (IFile res : Utils.getAllConfigFiles(resource.getProject())) {
				if (RefactorOperation.hasReferenceTo(res, ref, scriptRef)) {
					if (checkFile(res))
						list.add(res);
					else
						editors.add(res);
				}
			}
		} catch (Exception e) {
			EclipseAppender.logerror(e.getLocalizedMessage(), e, getShell());
		}

		list.addAll(editors);
	}

	private boolean checkFile(IFile res) {
		if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() == null)
			return true;

		FileEditorInput input = new FileEditorInput(res);
		if (PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findEditor(input) != null) {
			return false;
		}
		return true;
	}

	@Override
	public boolean isPageComplete() {
		return true;
	}

	public boolean hasReferences() {
		return list != null && list.size() > 0;
	}
}
