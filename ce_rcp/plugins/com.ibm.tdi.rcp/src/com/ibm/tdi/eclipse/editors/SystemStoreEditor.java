/*
 * Copyright IBM Corp. 2008, 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.editors;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.viewers.OwnerDrawLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.progress.UIJob;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.PropertyManager;
import com.ibm.di.connector.ConnectorInterface;
import com.ibm.di.entry.Entry;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.store.DeltaStore;
import com.ibm.di.store.PropertyStore;
import com.ibm.di.store.StoreFactory;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.server.RMIServerAPI;
import com.ibm.tdi.eclipse.server.RestServerAPI;
import com.ibm.tdi.eclipse.util.SystemStore;
import com.ibm.tdi.eclipse.widget.BaseWidget;
import com.ibm.tdi.eclipse.widget.ValueEditorWidget;

public class SystemStoreEditor extends EditorPart {
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public static final String EDITOR_ID = "com.ibm.tdi.eclipse.editors.systemstore";

	private Combo store;

	private Button openButton;

	private Button closeButton;

	private Connection connection;

	private ArrayList<IResource> storeList;

	private TreeViewer tree;

	private TableViewer table;

	private JDBCTable currentTable;

	private Label tableName;

	private Action deleteTable;

	private Label storeName;

	private Button addBtn;

	private Button delBtn;

	private Button editBtn;

	private Button refreshBtn;
	
	protected IResource currentServer;

	private String currentStore;

	private Text textFilter;
	
	private Button startButton;
	
	private Button stopButton;
	
	private Button testConnectionButton;
	
	private String derbyURL;

	private static String[] treeNodes = new String[] {
			Messages.getString("SystemStoreEditor.property"),
			Messages.getString("SystemStoreEditor.delta"),
			Messages.getString("PropertyStoreUI.Localized.Global-Properties"),
			Messages.getString("PropertyStoreUI.Localized.Solution-Properties"),
			Messages.getString("PropertyStoreUI.Localized.Java-Properties") };

	private static String[] untranslatedNames = new String[] { "", "",
			PropertyManager.STDCOLL_GLOBAL, PropertyManager.STDCOLL_SOLUTION,
			PropertyManager.STDCOLL_JAVA };

	public SystemStoreEditor() {
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);
		setPartName(input.getName());
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {

		BaseWidget base = new BaseWidget(parent, SWT.NONE);
		base.setLayout(new FillLayout());
		base.setLayoutData(new GridData(GridData.FILL_BOTH));

		Form form = base.createForm(base, null);
		form.setText(Messages.getString("SystemStoreEditor.title"));
		form.setHeadClient(createHeadClient(form.getHead()));

		Composite c = form.getBody();
		c.setLayout(new FillLayout());

		SashForm sash = new SashForm(c, SWT.HORIZONTAL);
		sash.setBackground(form.getBackground());
		createTreeViewer(sash);
		createTableViewer(sash);

		updateButtonStates();

		sash.setWeights(new int[] { 30, 70 });
	}

	private void createTableViewer(SashForm sash) {
		Composite c = new Composite(sash, SWT.NONE);
		c.setBackground(sash.getBackground());
		Utils.setGridLayout(c, 1, false);

		createToolbar(c);

		tableName = new Label(c, SWT.LEFT);
		tableName.setBackground(c.getBackground());
		tableName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		table = new TableViewer(c, SWT.FULL_SELECTION | SWT.BORDER);
		table.getTable().setHeaderVisible(true);
		table.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));

		table.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				return ((ArrayList<?>) inputElement).toArray();
			}

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
			}
		});

		table.setLabelProvider(new OwnerDrawLabelProvider() {

			private String getLine(Event event, Object element) {
				String line = "";

				if (element instanceof HashMap) {
					HashMap<Integer, Object> map = (HashMap<Integer, Object>) element;
					Object obj = map.get(event.index + 1);
					if (obj != null)
						line = obj.toString();
				}

				return line;
			}

			@Override
			protected void measure(Event event, Object element) {
				event.width = table.getTable().getColumn(event.index).getWidth();
				if (event.width == 0)
					return;

				String line = getLine(event, element);
				Point  size = event.gc.textExtent(line);

				event.height = size.y;
			}

			@Override
			protected void paint(Event event, Object element) {
				event.gc.drawText(getLine(event, element), event.x, event.y, true);
			}
		});
		
		// -- Editing support for
		table.addOpenListener(new IOpenListener() {
			public void open(OpenEvent event) {
				// -- Only prop stores can be edited
				if (currentTable != null
						&& currentTable.getTable().startsWith(
								PropertyStore.TABLE_PREFIX))
					editTableItem();
				else if (currentStore != null)
					editProperty();
			}
		});

		// -- Update buttons
		table.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				// -- Only prop stores can be edited
				boolean enabled;
				if (currentTable != null)
					enabled = currentTable.getTable().startsWith(PropertyStore.TABLE_PREFIX);
				else
					enabled = currentStore != null && currentServer != null;

				addBtn.setEnabled(enabled);
				editBtn.setEnabled(enabled && !table.getSelection().isEmpty());
				enabled |= (currentTable != null
						&& currentTable.getTable().startsWith(DeltaStore.TABLE_PREFIX));
				delBtn.setEnabled(enabled && !table.getSelection().isEmpty());
				refreshBtn.setEnabled(currentTable != null || currentStore != null);
				textFilter.setEnabled(currentTable != null || currentStore != null);
			}
		});
		
		// -- Table view 
		ViewerFilter tableFilter = new ViewerFilter() {
			@SuppressWarnings("unchecked")
			@Override
			public boolean select(Viewer viewer, Object parentElement,
					Object element) {
				
				String str = textFilter.getText().trim().toLowerCase();
				if(str.length() == 0)
					return true;
				
				if (element instanceof HashMap) {
					HashMap<Integer, Object> map = (HashMap<Integer, Object>) element;
					Object obj = map.get(1);
					if(obj.toString().toLowerCase().indexOf(str) != -1)
						return true;
					obj = map.get(2);
					if(obj != null && obj.toString().toLowerCase().indexOf(str) != -1)
						return true;
					
					obj = map.get(3);
					if(obj != null && obj.toString().toLowerCase().indexOf(str) != -1)
						return true;
					
					return false;
				}
					
				return true;
			}
		};
		table.setFilters(new ViewerFilter[]{tableFilter});
	}

	private void addTableRow() {
		InputDialog id = new InputDialog(getSite().getShell(), getPartName(),
				Messages.getString("general.insert.label"), "", null) {
			protected int getShellStyle() {
				return super.getShellStyle() | SWT.RESIZE;
			}
		};
		if (id.open() == Window.OK) {
			String key = id.getValue();
			if (key != null && key.trim().length() > 0) {
				PreparedStatement st = null;
				try {
					st = connection.prepareStatement("insert into "
									+ currentTable.table + " values (?,?)");
					st.setString(1, key);
					st.setObject(2, StoreFactory.serializeObject(""));
					st.executeUpdate();
					connection.commit();
				} catch (Exception e1) {
					EclipseAppender.logerror(e1.toString(), e1, getSite()
							.getShell());
				} finally {
					try {
						if (st != null)
							st.close();
					} catch (Exception e1) {
						EclipseAppender.logerror(e1.toString(), e1, getSite()
								.getShell());
					}
				}

				updateTable(currentTable);
			}
		}
	}

	private void addProperty() {
		InputDialog id = new InputDialog(getSite().getShell(), getPartName(),
				Messages.getString("general.inesrt.label"), "", null) {
					protected int getShellStyle() {
						return super.getShellStyle() | SWT.RESIZE;
					}
			
		};
		if (id.open() == Window.OK) {
			String key = id.getValue();
			if (key != null && key.trim().length() > 0) {
				try {
					RestServerAPI api = null;
					if(currentServer instanceof IProject) {
						api = RMIServerAPI.createInstance(Utils.getTDIServer((IProject)currentServer));
					} else {
						api = RMIServerAPI.createInstance((IFile)currentServer);
					}
							
					Entry entry = new Entry();
					entry.setAttribute(key, "");
					api.setProperties(currentStore, entry, null);
					api.close();
				} catch (Exception e1) {
					EclipseAppender.logerror(e1.toString(), e1, getSite()
							.getShell());
				}
				updateTable(currentStore);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void deleteTableRow() {
		if (!MessageDialog.openConfirm(getSite().getShell(), getPartName(),
				Messages.getString("general.delete.tooltip")))
			return;
		PreparedStatement st = null;
		try {
			try {
				st = connection.prepareStatement("delete from "
					+ currentTable.table + " where id = ?");
			} catch (SQLException e) {
				if (!currentTable.table.equals(currentTable.table.toUpperCase())) {
					st = connection.prepareStatement("delete from \""
						+ currentTable.table + "\" where id = ?");				
				} else {
					throw e;
				}
			}
			
			for (Object row : ((IStructuredSelection) table.getSelection())
					.toArray()) {
				HashMap<Integer, Object> map = (HashMap<Integer, Object>) row;
				st.setObject(1, map.get(1));
				st.executeUpdate();
				connection.commit();
			}
		} catch (SQLException e1) {
			EclipseAppender.logerror(e1.toString(), e1, getSite().getShell());
		} finally {
			try {
				if (st != null)
					st.close();
			} catch (SQLException e1) {
				EclipseAppender.logerror(e1.toString(), e1, getSite().getShell());
			}
		}

		updateTable(currentTable);
	}

	@SuppressWarnings("unchecked")
	private void deleteProperty() {
		if (!MessageDialog.openConfirm(getSite().getShell(), getPartName(),
				Messages.getString("general.delete.tooltip")))
			return;
		RestServerAPI api = null;
		try {
			if(currentServer instanceof IProject) {
				api = RMIServerAPI.createInstance(Utils.getTDIServer((IProject)currentServer));
			} else {
				api = RMIServerAPI.createInstance((IFile)currentServer);
			}
			for (Object row : ((IStructuredSelection) table.getSelection())
					.toArray()) {
				HashMap<Integer, Object> map = (HashMap<Integer, Object>) row;
				Entry entry = new Entry();
				entry.newAttribute("" + map.get(1));
				api.setProperties(currentStore, entry, null);
			}
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e, getSite().getShell());
		} finally {
			if (api != null)
				api.close();
		}
		updateTable(currentStore);
	}

	private void createToolbar(Composite c) {

		// -- Toolbar when we edit property stores
		Composite tools = new Composite(c, SWT.NONE);
		tools.setBackground(c.getBackground());
		tools.setLayout(new GridLayout(9, false));
		tools.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		addBtn = new Button(tools, SWT.PUSH);
		addBtn.setText(Messages.getString("general.insert.label"));
		addBtn.setToolTipText(Messages.getString("general.insert.tooltip"));
		addBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (currentTable != null)
					addTableRow();
				else
					addProperty();
			}
		});
		addBtn.setEnabled(false);

		delBtn = new Button(tools, SWT.PUSH);
		delBtn.setText(Messages.getString("general.delete.label"));
		delBtn.setToolTipText(Messages.getString("general.delete.tooltip"));
		delBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (currentTable != null)
					deleteTableRow();
				else
					deleteProperty();
			}
		});
		delBtn.setEnabled(false);

		editBtn = new Button(tools, SWT.PUSH);
		editBtn.setText(Messages.getString("BranchingConfig.Edit.label"));
		editBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (currentTable != null)
					editTableItem();
				else
					editProperty();
			}
		});
		editBtn.setEnabled(false);
		
		refreshBtn = new Button(tools, SWT.PUSH);
		refreshBtn.setText(Messages.getString("serverview.refresh"));
		refreshBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (currentTable != null)
					updateTable(currentTable);
				else if (currentStore != null)
					updateTable(currentStore);
			}
		});
		refreshBtn.setEnabled(false);
		
		Label label = new Label(tools, SWT.LEFT);
		label.setBackground(tools.getBackground());
		label.setText(Messages.getString("WorkEntryWidget.9"));
		
		textFilter = new Text(tools, SWT.BORDER);
		textFilter.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				table.refresh();
			}
		});
		textFilter.setEnabled(false);
		GridData gd = new GridData();
		gd.widthHint = 200;
		textFilter.setLayoutData(gd);

	}

	private void createTreeViewer(SashForm sash) {
		Composite c = new Composite(sash, SWT.NONE);
		c.setBackground(sash.getBackground());
		Utils.setGridLayout(c, 1, false);

		storeName = new Label(c, SWT.LEFT);
		storeName.setBackground(c.getBackground());
		storeName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		tree = new TreeViewer(c, SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.BORDER);
		tree.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		tree.setContentProvider(new ITreeContentProvider() {
			public Object[] getChildren(Object parentElement) {
				if (parentElement instanceof Connection) {
					return treeNodes;
				}
				try {
					String str = (String) parentElement;
					ArrayList<Object> list = new ArrayList<Object>();
					if (str.equals(treeNodes[0])) {
						// IDI_PS_%
						ResultSet rs = connection.getMetaData().getTables(null,
								null, "IDI_PS_%", null);
						while (rs.next()) {
							list.add(new JDBCTable(rs.getString(3), rs
									.getString(4)));
						}
						rs.close();

					} else if (str.equals(treeNodes[1])) {
						// IDI_PS_%
						ResultSet rs = connection.getMetaData().getTables(null,
								null, "IDI_DS_%", null);
						while (rs.next()) {
							list.add(new JDBCTable(rs.getString(3), rs
									.getString(4)));
						}
						rs.close();
					}
					return list.toArray();
				} catch (Exception e) {
					SystemFunctions.doNothing();
				}
				return null;
			}

			public Object getParent(Object element) {
				return null;
			}

			public boolean hasChildren(Object element) {
				return element instanceof String
						&& (treeNodes[0].equals(element) || treeNodes[1]
								.equals(element));
			}

			public Object[] getElements(Object inputElement) {
				return getChildren(inputElement);
			}

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
			}
		});

		tree.addOpenListener(new IOpenListener() {
			public void open(OpenEvent event) {
				IStructuredSelection sel = (IStructuredSelection) tree
						.getSelection();
				if (sel.isEmpty())
					return;
				if (sel.getFirstElement() instanceof JDBCTable) {
					updateTable((JDBCTable) sel.getFirstElement());
				} else if (sel.getFirstElement() instanceof String) {
					String str = (String) sel.getFirstElement();
					if (treeNodes[0].equals(str) || treeNodes[1].equals(str))
						return;

					tableName.setText(str);
					// Untranslate the name
					for (int i = 2; i < treeNodes.length; i++) {
						if (treeNodes[i].equals(str)) {
							updateTable(untranslatedNames[i]);
							return;
						}
					}
				}
			}
		});

		tree.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateButtonStates();
			}
		});

		MenuManager mm = new MenuManager();
		tree.getTree().setMenu(mm.createContextMenu(tree.getTree()));

		deleteTable = new Action() {
			@Override
			public String getText() {
				return Messages.getString("SystemStoreEditor.deleteTable");
			}

			@Override
			public String getToolTipText() {
				return Messages
						.getString("SystemStoreEditor.deleteTable.tooltip");
			}

			@Override
			public void run() {
				JDBCTable jdbcTable = (JDBCTable) ((IStructuredSelection) tree
						.getSelection()).getFirstElement();
				String table = jdbcTable.getTable();
				String sql = "DELETE FROM " + table;
				String sql2 = null;
				if (table.startsWith(DeltaStore.TABLE_PREFIX)) {
					if (! table.equals(table.toUpperCase())) {
						// Handle mixed case names
						sql = "DROP TABLE \"" + table + "\"";
						sql2 = "DROP TABLE " + table;
					} else {
						sql = "DROP TABLE " + table;						
					}
				}
				if (!MessageDialog.openConfirm(getSite().getShell(), Messages
						.getString("SystemStoreEditor.deleteTable"), table
						+ "\n\nSQL: " + sql))
					return;

				Statement st = null;
				try {
					st = connection.createStatement();
					st.executeUpdate(sql);
				} catch (Exception err) {
					if (st != null && sql2 != null) {
						try {
							st.executeUpdate(sql2);
							err = null;
						} catch (Exception err2) {}
					}
					if (err != null)
						EclipseAppender.logerror(err.toString(), err, getSite().getShell());
				} finally {			
					try {
						if (st != null)
							st.close();
						connection.commit();
					} catch (Exception err) {
						EclipseAppender.logerror(err.toString(), err, getSite()
								.getShell());
					}
				}
				if (table.startsWith(DeltaStore.TABLE_PREFIX)) {
					tableRemoved(jdbcTable);
					return;
				}
				if (currentTable != null
						&& table.equals(currentTable.getTable()))
					updateTable(jdbcTable);
			}
		};
		mm.add(deleteTable);
	}

	protected void tableRemoved(JDBCTable jdbcTable) {
		if (table != null) {
			for (TableColumn col : table.getTable().getColumns())
				col.dispose();
			table.setInput(null);
			table.setSelection(StructuredSelection.EMPTY);
		}

		if (tree != null) {
			tree.setInput(connection);
			tree.expandAll();
		}

	}

	@SuppressWarnings("unchecked")
	protected void editTableItem() {
		IStructuredSelection sel = (IStructuredSelection) table.getSelection();
		if (sel.isEmpty())
			return;
		if (sel.getFirstElement() instanceof HashMap) {
			HashMap<Integer, Object> map = (HashMap<Integer, Object>) sel
					.getFirstElement();
			Object value = map.get(2);
			if (value == null)
				value = "";

			PreparedStatement ps = null;
			Object result = null;
			try {
				result = ValueEditorWidget.openValueEditorDialog(
						getSite().getShell(), value);
				if (result == null)
					return;

				ps = connection.prepareStatement("UPDATE "
						+ currentTable.getTable()
						+ " SET ENTRY = ? WHERE ID = ?");
				ps.setBytes(1, StoreFactory.serializeObject(result));
				ps.setString(2, (String) map.get(1));
				ps.executeUpdate();
			} catch (Throwable e) {
				EclipseAppender.logerror(e.toString(), e, getSite().getShell());
				return;
			} finally {
				if (ps != null) {
					try{
						ps.close();
						connection.commit();
					} catch (Throwable e) {
						EclipseAppender.logerror(e.toString(), e, getSite().getShell());
					}
				}
			}
			map.put(2, result);
			table.refresh(map);
		}
	}

	@SuppressWarnings("unchecked")
	protected void editProperty() {
		IStructuredSelection sel = (IStructuredSelection) table.getSelection();
		if (sel.isEmpty())
			return;
		if (sel.getFirstElement() instanceof HashMap) {
			HashMap<Integer, Object> map = (HashMap<Integer, Object>) sel
					.getFirstElement();
			String key = (String) map.get(1);
			Object value = map.get(2);
			if (value == null)
				value = "";

			try {
				Object result = ValueEditorWidget.openValueEditorDialog(
						getSite().getShell(), value);
				if (result != null) {
					RestServerAPI api = null;
					if(currentServer instanceof IProject) {
						api = RMIServerAPI.createInstance(Utils.getTDIServer((IProject)currentServer));
					} else {
						api = RMIServerAPI.createInstance((IFile)currentServer);
					}
					Entry entry = new Entry();
					entry.setAttribute(key, result);
					api.setProperties(currentStore, entry, null);
					map.put(2, result);
					table.refresh(map);
					api.close();
				}
			} catch (Throwable e) {
				EclipseAppender.logerror(e.toString(), e, getSite().getShell());
			}
		}
	}

	protected void updateTable(JDBCTable t) {
		try {
			currentTable = t;
			Statement st = connection.createStatement();
			try {
				ResultSet rs;
				try {
					rs = st.executeQuery("select * from \"" + t.getTable() + "\"");
				} catch (Exception e) {
					rs = st.executeQuery("select * from " + t.getTable());				
				}
				try {
					ResultSetMetaData md = rs.getMetaData();

					for (TableColumn col : table.getTable().getColumns())
						col.dispose();

					for (int i = 1; i <= md.getColumnCount(); i++) {
						TableColumn tc = new TableColumn(table.getTable(), SWT.LEFT);
						String label = md.getColumnLabel(i);
						if (label == null || label.length() == 0)
							label = md.getColumnName(i);
						tc.setText(label);
						tc.setWidth(200);
					}

					tableName.setText(currentTable.getTable() + " ("
							+ currentTable.getType() + ")");

					ArrayList<Object> list = new ArrayList<Object>();
					while (rs.next()) {
						HashMap<Integer, Object> arr = new HashMap<Integer, Object>();
						for (int i = 1; i <= md.getColumnCount(); i++) {
							Object obj = rs.getObject(i);
							if (obj != null) {
								try {
									Object newobj = StoreFactory.deserializeObject(obj);
									if (newobj != null)
										obj = newobj;
								} catch (Exception e) {
									SystemFunctions.doNothing();
								}
								arr.put(i, obj);
							}
						}
						list.add(arr);
					}
					table.setInput(list);
					table.setSelection(StructuredSelection.EMPTY);
				} finally {
					rs.close();
				}
			} finally {
				st.close();				
			}
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e, getSite().getShell());
		}
	}

	protected void updateTable(String store) {
		currentTable = null;
		currentStore = store;

		for (TableColumn col : table.getTable().getColumns())
			col.dispose();

		// -- make the two columns equal in size
		int size = (table.getTable().getBounds().width - 25) / 2;
		
		TableColumn tc = new TableColumn(table.getTable(), SWT.LEFT);
		tc.setText(Messages.getString("PropertyStoreUI.Localized.Name"));
		tc.setWidth(size);

		tc = new TableColumn(table.getTable(), SWT.LEFT);
		tc.setText(Messages.getString("PropertyStoreUI.Localized.Value"));
		tc.setWidth(size);

		Job job = new Job(store) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					String store = getName();
					final ArrayList<HashMap<Integer, Object>> list = new ArrayList<HashMap<Integer, Object>>();
					RestServerAPI serverAPI = null;
					if(currentServer instanceof IProject) {
						serverAPI = RMIServerAPI.createInstance(Utils.getTDIServer((IProject)currentServer));
					} else {
						serverAPI = RMIServerAPI.createInstance((IFile)currentServer);
					}
					Entry entry = serverAPI.getProperties(store, null);
					for (String attr : entry.getAttributeNames()) {
						HashMap<Integer, Object> arr = new HashMap<Integer, Object>();
						arr.put(1, attr);
						arr.put(2, entry.getAttribute(attr) == null ? "(null)"
								: entry.getString(attr));
						list.add(arr);
					}
					serverAPI.close();
					
					Collections.sort(list,
							new Comparator<HashMap<Integer, Object>>() {
								public int compare(HashMap<Integer, Object> a,
										HashMap<Integer, Object> b) {
									return a.get(1).toString()
											.compareToIgnoreCase(
													b.get(1).toString());
								}
							});
					
					UIJob upd = new UIJob(getName()) {
						public IStatus runInUIThread(IProgressMonitor monitor) {
							if(table != null && !table.getControl().isDisposed()) {
								table.setInput(list);
								table.setSelection(StructuredSelection.EMPTY);
							}
							return Status.OK_STATUS;
						}
					};
					upd.schedule();
					return Status.OK_STATUS;
				} catch (Exception e) {
					return EclipseAppender.statusException(e);
				}
			}
		};
		job.schedule();

	}

	private Control createHeadClient(Composite parent) {
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayout(new GridLayout(7, false));
		new Label(c, SWT.LEFT).setText(Messages
				.getString("SystemStoreEditor.store"));

		store = new Combo(c, SWT.DROP_DOWN | SWT.READ_ONLY);
		store.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateButtonStates();
			}
		});

		IFile preferred = getEditorInput() instanceof IFileEditorInput ? ((IFileEditorInput) getEditorInput())
				.getFile()
				: null;
		try {
			storeList = SystemStore.getAvailableSystemStores();
			int prefSelection = 0;

			Collections.sort(storeList, new Comparator<IResource>() {
				public int compare(IResource a, IResource b) {
					return a.getName().compareTo(b.getName());
				}
			});

			for (int i = 0; i < storeList.size(); i++) {
				IResource resource = storeList.get(i);

				if (preferred != null && preferred.equals(resource))
					prefSelection = i;

				store.add(resource.getName());
			}

			if (storeList.size() > 0)
				store.select(prefSelection);

		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e, getSite().getShell());
		}

		openButton = new Button(c, SWT.PUSH);
		openButton.setText(Messages.getString("SystemStoreEditor.open"));
		openButton.setToolTipText(Messages
				.getString("SystemStoreEditor.open.tooltip"));
		openButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				openSystemStore(storeList.get(store.getSelectionIndex()));
				updateViewers();
				updateButtonStates();
			}
		});

		closeButton = new Button(c, SWT.PUSH);
		closeButton.setText(Messages.getString("SystemStoreEditor.close"));
		closeButton.setToolTipText(Messages
				.getString("SystemStoreEditor.close.tooltip"));
		closeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				closeSystemStore();
				updateViewers();
				updateButtonStates();
				if (storeName != null)
					storeName.setText("");
				if (tableName != null)
					tableName.setText("");
			}
		});

		testConnectionButton = new Button(c, SWT.PUSH);
		testConnectionButton.setText(Messages.getString("ConfigSettingsEditor.TestConnection"));
		testConnectionButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				testConnectionButton.setEnabled(false);
				final IResource res = storeList.get(store.getSelectionIndex());
				new Thread(new Runnable() {
					public void run() {   
						startDB(res, 0);
					}
				}).start();
			}			
		});

		startButton = new Button(c, SWT.PUSH);
		startButton.setText(Messages.getString("ConfigSettingsEditor.Start"));
		startButton.setToolTipText(Messages.getString("ConfigSettingsEditor.Start.tooltip"));
		startButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				startButton.setEnabled(false);
				final IResource res = storeList.get(store.getSelectionIndex());
				new Thread(new Runnable() {
					public void run() {   
						startDB(res, 1);
					}
				}).start();
			}			
		});

		stopButton = new Button(c, SWT.PUSH);
		stopButton.setText(Messages.getString("ConfigSettingsEditor.Stop"));
		stopButton.setToolTipText(Messages.getString("ConfigSettingsEditor.Stop.tooltip"));
		stopButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				stopButton.setEnabled(false);
				final IResource res = storeList.get(store.getSelectionIndex());
				new Thread(new Runnable() {
					public void run() {   
						startDB(res, 2);
					}
				}).start();
			}			
		});

		return c;
	}

	protected void openSystemStore(IResource res) {

		storeName.setText("");

		final IResource resource = res;
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor)
					throws InvocationTargetException, InterruptedException {
				try {
					monitor.beginTask(resource.getName(), 2);
					monitor.subTask(Messages
							.getString("SystemStoreEditor.open.config"));
					BaseConfiguration cfg = SystemStore
							.getServerStore(resource);
					monitor.worked(1);

					String driver = getStringParameter(cfg, ConfigSettingsEditor.DRIVER);
					boolean derby = StoreFactory.isDerbyDriver(driver);

					Class.forName(driver);
					String url = ConfigSettingsEditor.getDatabase(cfg);
					if (url == null || url.trim().length() == 0)
						throw new Exception(Messages.getMessage(
								"SystemStoreEditor.missing.parameter", ConfigSettingsEditor.DATABASE));

					String user = getStringParameter(cfg, ConfigSettingsEditor.USER,
							(derby ? "APP" : null));
					String pass = getStringParameter(cfg, ConfigSettingsEditor.PASSWORD,
							(derby ? "APP" : null));
					monitor.subTask(Messages.getString("SystemStoreEditor.open.connect"));

					if (!url.startsWith("jdbc:"))
						url = getStringParameter(cfg, "com.ibm.di.store.jdbc.urlprefix")
								+ url;
					
					//
					// If we connect to a remote host (e.g. a different machine) and we get a jdbc:derby://localhost url
					// we have to substitute the "localhost" part with the real ip-address/hostname.
					//
					if(url.contains("localhost") && resource instanceof IFile) {
						RMIServerAPI api = (RMIServerAPI) RMIServerAPI.createInstance((IFile)resource);
						String addr = api.getAddress();
						if(addr.indexOf(":") > 0) {
							String remoteHost = addr.substring(0, addr.indexOf(":"));
							url = url.replaceAll("localhost", remoteHost);
						}
					}

					Connection c = DriverManager.getConnection(url, user, pass);
					if (c != null) {
						closeSystemStore();
						connection = c;
						currentServer = resource;
					}
					monitor.worked(1);
					monitor.done();
				} catch (Exception e) {
					throw new InvocationTargetException(e);
				}
			}

		};

		ProgressMonitorDialog pmd = new ProgressMonitorDialog(getSite()
				.getShell());
		try {
			pmd.run(true, false, runnable);
			storeName.setText(res.getName());
		} catch (InvocationTargetException e) {
			EclipseAppender.logerror(e.getCause().toString(), e.getCause(),
					getSite().getShell());
		} catch (InterruptedException e) {
			SystemFunctions.doNothing();
		}
	}

	private void updateViewers() {
		tree.setInput(connection);
		if (connection == null) {
			for (TableColumn col : table.getTable().getColumns())
				col.dispose();
			table.setInput(null);
			table.setSelection(StructuredSelection.EMPTY);
		}
	}

	private void closeSystemStore() {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			connection = null;
		}
		currentTable = null;
	}

	protected void updateButtonStates() {
		openButton.setEnabled((connection == null)
				&& (store.getSelectionIndex() != -1));
		closeButton.setEnabled(connection != null);
		boolean deleteEnabled = false;
		if (connection != null) {
			Object obj = ((IStructuredSelection) tree.getSelection())
					.getFirstElement();
			if (obj instanceof JDBCTable) {
				JDBCTable jt = (JDBCTable) obj;
				deleteEnabled = jt.getTable().startsWith(
						PropertyStore.TABLE_PREFIX)
						|| jt.getTable().startsWith(DeltaStore.TABLE_PREFIX);
			}
		}
		if (deleteTable != null)
			deleteTable.setEnabled(deleteEnabled);
		
		textFilter.setEnabled(false);
		addBtn.setEnabled(false);
	}

	@Override
	public void setFocus() {
		if (store != null)
			store.setFocus();
	}

	private static class JDBCTable {
		private String table;
		private String type;

		public JDBCTable(String table, String type) {
			super();
			this.table = table;
			this.type = type;
		}

		public String getTable() {
			return table;
		}

		public String getType() {
			return type;
		}

		@Override
		public String toString() {
			if (type != null)
				return table + " (" + type + ")";
			else
				return table;
		}
	}

	@Override
	public void dispose() {
		closeSystemStore();
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				SystemFunctions.doNothing();
			}
		}
		super.dispose();
	}

	private static SystemStoreEditorInput systemStoreEditorInput = new SystemStoreEditorInput();

	public static IEditorInput createEditorInput() {
		return systemStoreEditorInput;
	}

	public static IEditorInput createEditorInput(IFile file) {
		return new FileEditorInput(file);
	}

	private static class SystemStoreEditorInput implements IEditorInput {
		public boolean exists() {
			return false;
		}

		public ImageDescriptor getImageDescriptor() {
			return null;
		}

		public String getName() {
			return Messages.getString("SystemStoreEditor.title");
		}

		public IPersistableElement getPersistable() {
			return null;
		}

		public String getToolTipText() {
			return getName();
		}

		@SuppressWarnings("rawtypes")
		public Object getAdapter(Class adapter) {
			return null;
		}
	}
	
	/**
	 * Attempt to test/start/stop a networked derby server.
	 * @param mode - 0: Test Connection, 1: start, 2: stop.
	 */
	private void startDB(IResource res, final int mode) {
		derbyURL = "";
		try {
			BaseConfiguration cfg = SystemStore.getServerStore(res);

			// Try to parse the host and port from the database almost URL -like String.
			
			derbyURL = ConfigSettingsEditor.getDatabase(cfg);
			int i = derbyURL.indexOf("//");
			if ( i == -1 )
				throw new Exception(Messages.getMessage("ConfigSettingsEditor.parse.urlprefix", derbyURL));
			String host = derbyURL.substring(i+2);
			if (host.indexOf("/")> 0)
				host = host.substring(0, host.indexOf("/"));

			String port = "1527";
			i = host.indexOf(":");
			if ( i > 0) {
				port = host.substring(i+1);
				host = host.substring(0, i);
			}
				
			String driver = getStringParameter(cfg, ConfigSettingsEditor.DRIVER);
			boolean derby = StoreFactory.isDerbyDriver(driver);

			String user = getStringParameter(cfg, ConfigSettingsEditor.USER, (derby ? "APP" : null));
			String pass = getStringParameter(cfg, ConfigSettingsEditor.PASSWORD, (derby ? "APP" : null));

			if (user != null)
				System.setProperty( ConfigSettingsEditor.USER, user);
			if (pass != null)
				System.setProperty( ConfigSettingsEditor.PASSWORD, pass); 
            StoreFactory.setDerbyUserPassword(user, pass);

            if (mode == 0) {
				ConnectorConfig cc = (ConnectorConfig) MetamergeConfigFactory.lookup(null, "system:/Connectors/ibmdi.JDBC"); //$NON-NLS-1$
				cc = (ConnectorConfig) cc.getClone();
				ConnectorInterface jdbc = SystemFunctions.loadConnector(cc);
				jdbc.setParam("jdbcSource", derbyURL); //$NON-NLS-1$ 
				jdbc.setParam("jdbcDriver", driver); 
				jdbc.setParam("jdbcLogin", user); 
				jdbc.setParam("jdbcPassword", pass); 
				jdbc.initialize(null);
				jdbc.terminate();
				Display.getDefault().syncExec(new Runnable() {
					public void run() {   
						MessageDialog.openInformation(getSite().getShell(), 
								Messages.getString("ConfigSettingsEditor.TestConnection"),
								Messages.getMessage("ConfigSettingsEditor.TestConnection.OK", derbyURL));
					}
				});
           } else if (mode == 1) {
				StoreFactory.startDerbyServer(host, port, true);
			} else {
				StoreFactory.stopDerbyServer(host, Integer.valueOf(port));
			}
		} catch (final Exception e) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					if (mode == 0)
						EclipseAppender.showError(
								Messages.getMessage("ConfigSettingsEditor.TestConnection.failed", derbyURL),
								e, getSite().getShell());
					else
						
						EclipseAppender.logerror(e.getMessage(), e, getSite().getShell());
				}
			});
		}
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {   
				startButton.setEnabled(true);
				stopButton.setEnabled(true);
				testConnectionButton.setEnabled(true);
			}
		});
	}
	
	private static String getStringParameter(BaseConfiguration cfg,	String string) throws Exception {
		return getStringParameter(cfg, string, null);
	}

	private static String getStringParameter(BaseConfiguration cfg,
			String string, String defval) throws Exception {
		String param = cfg.getStringParameter(string);
		if (param == null && defval != null)
			return defval;
		else if (param == null || param.trim().length() == 0)
			throw new Exception(Messages.getMessage(
					"SystemStoreEditor.missing.parameter", string));
		else
			return param;
	}

}
