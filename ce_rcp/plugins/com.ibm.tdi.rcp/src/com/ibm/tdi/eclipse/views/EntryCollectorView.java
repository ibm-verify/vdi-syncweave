/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.views;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.swt.IFocusService;

import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.entry.Entry;
import com.ibm.di.function.SystemFunctions;
import com.ibm.tdi.easyetl.ETLEditor;
import com.ibm.tdi.easyetl.ALDebugger.ALDebuggerEvent;
import com.ibm.tdi.eclipse.Activator;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.actions.CopyTableContentsAction;
import com.ibm.tdi.eclipse.editors.BaseEditor;
import com.ibm.tdi.eclipse.editors.RunAssemblyLineEditor;
import com.ibm.tdi.eclipse.preferences.PreferenceConstants;

public class EntryCollectorView extends ViewPart {
	/**
	 * 
	 */
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public final static String VIEW_ID = "com.ibm.tdi.rcp.entry.collector";
	private StackLayout stack;
	private Composite root;
	private HashMap<BaseEditor, CollectorView> maps = new HashMap<BaseEditor, CollectorView>();
	private String title;
	private long tableLimit = Activator.getPrefs().getLong(PreferenceConstants.P_DATA_COLLECTOR_BUFFER_SIZE);

	public EntryCollectorView() {
		title = Messages.getString("DataCollector.title");
		Activator.getPrefs().addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(PreferenceConstants.P_DATA_COLLECTOR_BUFFER_SIZE))
					tableLimit = Activator.getPrefs().getLong(PreferenceConstants.P_DATA_COLLECTOR_BUFFER_SIZE);
			}
		});
	}

	private void deleteView(BaseEditor etl) {
		CollectorView map = maps.get(etl);
		if (map != null) {
			maps.remove(etl);
			map.dispose();
		}
		// -- only change view if it's currently on top
		if (stack.topControl == map)
			setActiveView(null, false);
	}

	private void setActiveView(BaseEditor editor, boolean activate) {
		if (editor != null) {
			CollectorView map = getEditorView(editor);
			if (activate) {
				stack.topControl = map;
				root.layout(true);
			} else {
				stack.topControl = null;
				root.layout(true);
			}
		} else {
			stack.topControl = null;
			root.layout(true);
		}
		if (stack.topControl == null)
			setPartName(title);
		else if (editor != null)
			setPartName(title + " (" + editor.getPartName() + ")");
	}

	private CollectorView getEditorView(BaseEditor editor) {
		CollectorView map = maps.get(editor);
		if (map == null) {
			map = new CollectorView(root, editor);
			maps.put(editor, map);
		}
		return map;
	}

	@Override
	public void createPartControl(Composite parent) {
		root = new Composite(parent, SWT.NONE);
		stack = new StackLayout();
		root.setLayout(stack);

		// -- if view is loaded after workbench page is activated we need to catch that
		try {
			IWorkbenchPage page = getSite().getWorkbenchWindow().getActivePage();
			if(page != null) {
				IWorkbenchPart part = page.getActivePart();
				if (part instanceof ETLEditor || part instanceof RunAssemblyLineEditor)
					setActiveView((BaseEditor) part, true);
				addPartListener(page);
			}
		} catch (Exception e) {
			SystemFunctions.doNothing();
		}
	}

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		getSite().getWorkbenchWindow().addPageListener(new IPageListener() {
			public void pageOpened(IWorkbenchPage page) {
			}

			public void pageClosed(IWorkbenchPage page) {
			}

			public void pageActivated(IWorkbenchPage page) {
				addPartListener(page);
			}
		});

		addActionBarItems();
	}

	private void addPartListener(IWorkbenchPage page) {
		page.addPartListener(new IPartListener() {
			public void partOpened(IWorkbenchPart part) {
			}

			public void partDeactivated(IWorkbenchPart part) {
			}

			public void partClosed(IWorkbenchPart part) {
				if (part instanceof ETLEditor || part instanceof RunAssemblyLineEditor)
					deleteView((BaseEditor) part);
			}

			public void partBroughtToTop(IWorkbenchPart part) {
				if (part instanceof ETLEditor || part instanceof RunAssemblyLineEditor)
					setActiveView((BaseEditor) part, true);
			}

			public void partActivated(IWorkbenchPart part) {
				if (part instanceof ETLEditor || part instanceof RunAssemblyLineEditor)
					setActiveView((BaseEditor) part, true);
			}
		});
	}

	private void addActionBarItems() {
		Action limitAction = new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return Activator.getImageDescriptor("icons/Settings_16.gif");
			}

			@Override
			public void run() {
				PreferencesUtil.createPreferenceDialogOn(getSite().getShell(), "com.ibm.tdi.eclipse.preferences.TDIPreferencePage",
						null, null).open();
			}

			@Override
			public String getToolTipText() {
				return Messages.getString("DataCollector.buffer.size");
			}

		};
		Action clearAction = new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return Activator.getImageDescriptor("icons/ClearAll.gif");
			}

			@Override
			public void run() {
				if (stack.topControl != null)
					((CollectorView) stack.topControl).clearAll();
			}

			@Override
			public String getToolTipText() {
				return Messages.getString("DataCollector.clear");
			}

		};
		getViewSite().getActionBars().getToolBarManager().add(limitAction);
		getViewSite().getActionBars().getToolBarManager().add(clearAction);
	}

	/**
	 * This method is called from the ETLEditor (ColumnDataFlow) to add an entry to the collector
	 * table
	 * 
	 * @param editor
	 * @param event
	 */
	public void addEntry(BaseEditor editor, ALDebuggerEvent event, long cycle) {
		CollectorView view = getEditorView(editor);
		if (view != null) {
			view.addEntry(event.getComponentName(), (Entry) event.getValue(), cycle);
		}
	}

	/**
	 * This method is called from the StepperColumnsWidget to add an entry to the
	 * collector table
	 */
	public void addEntry(BaseEditor editor, String component, Entry value, long cycle) {
		CollectorView view = getEditorView(editor);
		if (view != null) {
			view.addEntry(component, value, cycle);
		}
	}

	/**
	 * This method is called from the RunALWidget to add an entry to the
	 * collector table
	 * 
	 * @param editor
	 * @param event
	 */
	public void addEntry(final BaseEditor editor, final Entry entry, final long cycle) {
		getSite().getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				CollectorView view = getEditorView(editor);
				if (view != null) {
					view.addEntry("Work Entry", entry, cycle);
				}
			}
		});
	}

	/**
	 * This method is called from the RunALWidget to clear the Entries in the
	 * collector table
	 * 
	 * @param editor
	 * @param event
	 */
	public void clearAll(final BaseEditor editor) {
		getSite().getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				CollectorView view = getEditorView(editor);
				if (view != null) {
					view.clearAll();
				}
			}
		});
	}

	/**
	 * This class contains two tables that displays entry collections.
	 * 
	 */
	private class CollectorView extends Composite {

		private SashForm sash;
		private TableViewer table;
		private Composite container;
		private StackLayout stack;
		private ArrayList<String> list = new ArrayList<String>();
		private HashMap<String, EntryCollectorTable> tables = new HashMap<String, EntryCollectorTable>();
		private BaseEditor editor;
		private HashMap<String, String> displayMap = new HashMap<String, String>();

		public CollectorView(Composite parent, BaseEditor editor) {
			super(parent, 0);
			this.editor = editor;
			setLayout(new FillLayout());
			setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));

			sash = new SashForm(this, SWT.HORIZONTAL);

			table = new TableViewer(sash, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
			table.getTable().setHeaderVisible(true);
			table.setContentProvider(ArrayContentProvider.getInstance());
			table.setInput(list);
			table.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
			
			final TableViewerColumn tvc = new TableViewerColumn(table, SWT.LEFT);
			tvc.getColumn().setText("Component");
			tvc.setLabelProvider(new CellLabelProvider() {
				@Override
				public void update(ViewerCell cell) {
					String str = cell.getElement().toString();
					if (displayMap.get(str) != null)
						str = displayMap.get(str);
					if (str == null)
						str = "";
					cell.setText(str);
				}
			});

			table.getTable().addControlListener(new ControlListener() {
				public void controlResized(ControlEvent e) {
					tvc.getColumn().setWidth(table.getTable().getClientArea().width);
				}

				public void controlMoved(ControlEvent e) {
				}
			});

			table.addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					String tab = (String) ((IStructuredSelection) table.getSelection()).getFirstElement();
					if (tab != null)
						showTable(tab);
				}
			});

			container = new Composite(sash, SWT.NONE);
			stack = new StackLayout();
			container.setLayout(stack);

			sash.setWeights(new int[] { 10, 90 });

			if (isIgnoringTable("work"))
				sash.setMaximizedControl(container);
		}

		public void clearAll() {
			stack.topControl = null;
			container.layout(true);
			for (EntryCollectorTable coll : tables.values())
				coll.dispose();
			list.clear();
			tables.clear();
			table.refresh();
		}

		private EntryCollectorTable addTabView(String name) {
			EntryCollectorTable coll = new EntryCollectorTable(name, container, 0);
			tables.put(name, coll);
			list.add(name);
			table.add(name);
			table.setSelection(new StructuredSelection(name));
			return coll;
		}

		private void showTable(String tab) {
			stack.topControl = tables.get(tab);
			container.layout();
		}

		public void addEntry(String tab, Entry entry, long cycle) {

			if (isIgnoringTable(tab)) {
				sash.setMaximizedControl(container);
				return;
			} else if (tab.equals("work")) {
				sash.setMaximizedControl(null);
			}

			EntryCollectorTable table = tables.get(tab);
			if (table == null) {
				table = addTabView(tab);
				showTable(tab);
			}
			if (table != null) {
				table.addEntry(entry, cycle);
			}
		}

		private boolean isIgnoringTable(String tab) {
			if (!(editor instanceof ETLEditor))
				return false;
			if (!"work".equals(tab))
				return false;

			AssemblyLineConfig alc = (AssemblyLineConfig) editor.getTDIConfiguration();
			ConnectorConfig cc = (ConnectorConfig) alc.getDataFlowComponents().getConfig(0);
			for (String str : cc.getAttributeMap(false).getAttributeNames()) {
				if (cc.getAttributeMap(false).getAttributeMapItem(str).isAdvanced()) {
					return false;
				}
			}
			return true;
		}

		public void setComponentDisplayName(String realName, String displayName) {
			displayMap.put(realName, displayName);
		}

	}

	private class EntryCollectorTable extends Composite {

		protected TableViewer table;
		private ArrayList<Entry> data = new ArrayList<Entry>();
		private HashMap<String, TableViewerColumn> columns = new HashMap<String, TableViewerColumn>();

		public EntryCollectorTable(String name, Composite parent, int style) {
			super(parent, style);
			setLayout(new FillLayout());
			table = new TableViewer(this);
			table.getTable().setHeaderVisible(true);
			table.setContentProvider(ArrayContentProvider.getInstance());
			table.setInput(data);
			table.getTable().addControlListener(new ControlListener() {
				public void controlResized(ControlEvent e) {
					adjustColumnSizes();
				}

				public void controlMoved(ControlEvent e) {
				}
			});
			
			MenuManager mm = new MenuManager();
			Menu menu = mm.createContextMenu(table.getTable());
			table.getTable().setMenu(menu);
			mm.add(new CopyTableContentsAction(table.getTable()));
			((IFocusService)getSite().getService(IFocusService.class)).addFocusTracker(table.getTable(), "com.ibm.tdi.etl.table");
		}

		protected void adjustColumnSizes() {
			if (columns.size() == 0)
				return;

			int width = table.getTable().getClientArea().width / columns.size();
			if (width < 50)
				width = 100;
			for (TableViewerColumn tvc : columns.values())
				tvc.getColumn().setWidth(width);
		}

		private CellLabelProvider labelProvider = new CellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				String str = table.getTable().getColumn(cell.getColumnIndex()).getText();
				Entry element = (Entry) cell.getElement();
				String value = element.getString(str);
				if (value == null)
					value = "";
				cell.setFont(JFaceResources.getTextFont());
				cell.setText(value);
			}
		};

		public void addEntry(Entry entry, long cycle) {

			Entry e = entry.clone();
			e.setProperty("cycle", cycle);

			// -- update or add?
			boolean add = true;
			if (data.size() > 0) {
				Entry lastentry = data.get(data.size() - 1);
				Object lastcycle = lastentry.getProperty("cycle");
				if (lastcycle != null && lastcycle.equals(cycle)) {
					lastentry.removeAllAttributes();
					lastentry.merge(e);
					e = lastentry;
					add = false;
				}
			}

			boolean updatesize = false;
			for (String str : e.getAttributeNames()) {
				if (columns.get(str) == null) {
					TableViewerColumn tvc = new TableViewerColumn(table, SWT.LEFT);
					tvc.getColumn().setText(str);
					tvc.setLabelProvider(labelProvider);
					columns.put(str, tvc);
					updatesize = true;
				}
			}

			if (updatesize) {
				adjustColumnSizes();
			}

			if (add) {
				// -- push first item off the table if we have reached the limit
				if (data.size() >= tableLimit) {
					table.remove(data.remove(0));
				}

				data.add(e);
				table.add(e);
			} else {
				table.refresh(e, true);
			}
			table.reveal(e);
		}
	}

	/**
	 * Sets the display name mapping for a collector table entry (e.g. work ->
	 * "Work Entry" etc)
	 * 
	 * @param realName
	 * @param displayName
	 */
	public void setComponentDisplayName(BaseEditor editor, String realName, String displayName) {
		CollectorView view = getEditorView(editor);
		if (view != null)
			view.setComponentDisplayName(realName, displayName);
	}

	@Override
	public void setFocus() {
	}

}
