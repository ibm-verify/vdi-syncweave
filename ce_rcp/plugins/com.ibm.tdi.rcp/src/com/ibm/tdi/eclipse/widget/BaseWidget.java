/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.widget;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.xml.Factories;
import com.ibm.tdi.eclipse.actions.ChangeInheritanceAction;
import com.ibm.tdi.eclipse.actions.SaveConfigSectionAction;
import com.ibm.tdi.eclipse.editors.BaseEditor;

public class BaseWidget extends Canvas implements ISelectionProvider, SelectionListener {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	
	public static final String TDI_WIDGET_NAME = "TDI_WIDGET_NAME";

	private BaseConfiguration editingConfig;
	
	protected ArrayList<Listener> listeners = new ArrayList<Listener>();
	private ArrayList<ISelectionChangedListener> selectionListeners = new ArrayList<ISelectionChangedListener>();
	private ArrayList<IOpenListener> openListeners = new ArrayList<IOpenListener>();
	private ISelection selection;
	private BaseEditor editor;
	private boolean updating;
	private boolean expanded;
	private boolean selected;
	private FormToolkit formToolKit;
	private Form form;

	public BaseWidget(Composite parent, int style) {
		this(parent, style, null);
	}
	
	public BaseWidget(Composite parent, int style, BaseConfiguration editingConfig) {
		super(parent, style);
		setEditingConfig(editingConfig);
	}

	public BaseWidget(Composite parent, int style, BaseConfiguration editingConfig, BaseEditor editor) {
		this(parent, style, editingConfig);
		setEditor(editor);
	}
	
	//
	// FormToolKit
	//
	/**
	 * Crates the form toolkit and a form with this as parent.
	 * The standard actions for a form heading is added (e.g. drag/drop, inheritance etc)
	 * 
	 * @return Newly created form
	 */
	public Form createForm(Composite parent, BaseConfiguration bc) {
		formToolKit = new FormToolkit(getDisplay());
		form = formToolKit.createForm(parent);
		formToolKit.decorateFormHeading(form);
		
		// Add load/save behavior including DnD
		if(bc != null) {
			addStandardFormDND(bc);
		}
		
		return form;
	}

	/**
	 * This method will add standard DnD behavior to the current Form if the editing config
	 * is a type that can be serialized to a standalone config file.
	 * @param bc 
	 * 
	 */
	private void addStandardFormDND(BaseConfiguration bc) {
		if(getForm() == null)
			return;
		
		ChangeInheritanceAction cia = new ChangeInheritanceAction(bc);
		form.getMenuManager().add(cia);
		// ChangeInheritanceAction only pretended to support drag and drop
		// Better to not show that until it is implemented.
		// And we can use another class.
		//getForm().addTitleDropSupport(DND.DROP_COPY, new Transfer[]{LocalSelectionTransfer.getTransfer()}, cia);
		
		// Only if we can serialize the object
		if(Factories.getClassTag(bc) != null) {
			SaveConfigSectionAction ssa = new SaveConfigSectionAction(bc);
			getForm().getMenuManager().add(ssa);
			getForm().addTitleDragSupport(DND.DROP_COPY, new Transfer[]{FileTransfer.getInstance()}, ssa);
		}
		
		// Update the menu bar
		form.getMenuManager().update(true);
		form.getToolBarManager().update(true);
		
	}

	/**
	 * Returns the form toolkit object
	 */
	public FormToolkit getFormToolKit() {
		return formToolKit;
	}

	/**
	 * Returns the form object
	 */
	public Form getForm() {
		return form;
	}

	@Override
	public void dispose() {
		if(formToolKit != null) {
			formToolKit.dispose();
			formToolKit = null;
		}
		super.dispose();
	}

	//
	// ---------------- SELECTION PROVIDERS
	//
	public void addAsSelectionProvider(ISelectionProvider provider) {
		if(getEditor() != null) {
			getEditor().addSelectionProvider(provider);
		} else {
			//EclipseAppender.loginfo("Warning: Editor is not set for " + getClass().getName());
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			if(page != null && page.getActiveEditor() instanceof BaseEditor)
				 ((BaseEditor)page.getActiveEditor()).addSelectionProvider(provider);
		}
	}
	
	//
	// ---------------- OPEN LISTENERS
	//
	public void addOpenListener(IOpenListener listener) {
		if(openListeners.contains(listener))
			return;
		else
			openListeners.add(listener);
	}
	
	public void removeOpenListener(IOpenListener listener) {
		openListeners.remove(listener);
	}
	
	public void fireOpenEvent(OpenEvent event) {
		for(IOpenListener l : openListeners)
			l.open(event);
	}

	//
	// ------------------- CLOSE LISTENERS
	//
	private ArrayList<Listener> closeListeners = new ArrayList<Listener>();

	private IMemento memento;
	public void addCloseListener(Listener listener) {
		if(closeListeners.contains(listener))
			return;
		else
			closeListeners.add(listener);
	}
	
	public void removeCloseListener(Listener listener) {
		closeListeners.remove(listener);
	}
	
	public void fireCloseEvent(Event event) {
		for(Listener l : closeListeners)
			l.handleEvent(event);
	}

	public void fireEvent(Event event) {
		for(Listener l : listeners)
			l.handleEvent(event);
	}

	public IFile getTDIConfigFile() {
		if(getEditingConfig().getMetamergeConfig() instanceof TDIConfigurationFile)
			return ((TDIConfigurationFile)getEditingConfig().getMetamergeConfig()).getFile();
		else
			return null;
	}
	
	public BaseConfiguration getEditingConfig() {
		return editingConfig;
	}

	public void setEditingConfig(BaseConfiguration editingConfig) {
		this.editingConfig = editingConfig;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		selectionListeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
	 */
	public ISelection getSelection() {
		return selection;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		selectionListeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
	 */
	public void setSelection(ISelection selection) {
		SelectionChangedEvent event = new SelectionChangedEvent(this, selection);
		setSelection(event);
	}

	/**
	 * @param event
	 */
	public void setSelection(SelectionChangedEvent event) {
		this.selection = event.getSelection();
		for(ISelectionChangedListener s : selectionListeners)
			s.selectionChanged(event);
	}
	
	/**
	 * @param data
	 */
	public void notifySelectionChange(Object data) {
		if(data == null)
			setSelection(StructuredSelection.EMPTY);
		else
			setSelection(new StructuredSelection(data));
	}
	
	/**
	 * @param selected
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
		redraw();
	}
	
	/**
	 * @return true if selected
	 */
	public boolean isSelected() {
		return this.selected;
	}
	
	/**
	 * @param editor
	 */
	public void setEditor(BaseEditor editor) {
		this.editor = editor;
	}
	
	/**
	 * @return the BaseEditor
	 */
	public BaseEditor getEditor() {
		if(this.editor != null)
			return this.editor;
		
		BaseWidget parent = getParentBaseWidget();
		if(parent != null)
			return parent.getEditor();
		
		return null;
	}
	
	/**
	 * Returns the "parent"t BaseWidget in the hierarchy. This BaseWidget may be placed inside other SWT/JFace components so
	 * this method traverses the tree upwards until it finds a BaseWidget instance.
	 * 
	 * @return
	 */
	public BaseWidget getParentBaseWidget() {
		Composite c = getParent();
		while(c != null) {
			if(c instanceof BaseWidget)
				return (BaseWidget) c;
			else
				c = c.getParent();
		}
		return null;
	}
	
	/**
	 * Invoke widgetSelected(e)
	 */
	public void widgetDefaultSelected(SelectionEvent e) {
	}

	/* 
	 * Dummy handler for selectionlistener
	 * (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent e) {
	}

	public boolean isUpdating() {
		return updating;
	}

	public void setUpdating(boolean updating) {
		this.updating = updating;
	}

	public void updateGlobalActionBars() {
		if(getEditor() != null)
			getEditor().updateActionBars();
	}

	public BaseConfiguration getConfigSelection(ISelection selection) {
		if(selection.isEmpty())
			return null;
		
		if(selection instanceof IStructuredSelection) {
			IStructuredSelection sel = (IStructuredSelection) selection;
			if(sel.getFirstElement() instanceof BaseConfiguration)
				return (BaseConfiguration) sel.getFirstElement();
		}
		
		return null;
	}

	/**
	 * This method should select the most relevant UI object based on the relative path
	 * of objects in the list.
	 * 
	 * @param list List of baseconfiguration objects (top-down)
	 */
	public void selectConfigObject(ArrayList<BaseConfiguration> list) {
	}

	public boolean isExpanded() {
		return expanded;
	}

	public void setExpanded(boolean expanded) {
		this.expanded = expanded;
	}

	/**
	 * Tells the widget to reveal the UI element for the specified configuration object.
	 * 
	 * @param config
	 * @return true if config was revealed
	 */
	public boolean revealConfigUI(Object config) {
		return false;
	}
	
	/**
	 * Save the widget state to the memento.
	 * 
	 * @param memento
	 */
	public void saveState(IMemento memento) {
	}
	
	/**
	 * Restore the widget state from the memento.
	 * 
	 * @param memento
	 */
	public void restoreState(IMemento memento) {		
	}
	
	/**
	 * Returns the IMemento for this widget. If no explicit memento is defined, the parent BaseWidget is consulted. If neither this
	 * nor a parent BaseWidget has a memento defined the BaseEditor is consulted.
	 *  
	 * @return
	 */
	public IMemento getMemento() {
		IMemento imem = memento;
		if(imem == null && getParentBaseWidget() != null)
			imem = getParentBaseWidget().getMemento();
		if(imem == null && getEditor() != null)
			imem = getEditor().getMemento();
			
		return imem;
	}
	
	/**
	 * Sets the IMemento for this widget.
	 * 
	 * @param memento
	 */
	public void setMemento(IMemento memento) {
		this.memento = memento;
	}
}
