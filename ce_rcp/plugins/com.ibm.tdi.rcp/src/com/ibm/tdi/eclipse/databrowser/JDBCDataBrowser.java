/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.databrowser;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.progress.UIJob;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.connector.Connector;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.log.EclipseAppender;

public class JDBCDataBrowser extends DataBrowser {

	@SuppressWarnings("unused")  //$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	
	private Connection jdbcConn;
	private Vector<Object> navigatorInput;

	public JDBCDataBrowser(Composite parent, int style, BaseConfiguration editingConfig) {
		super(parent, style, editingConfig);
	}

	@Override
	protected void doInitialDiscovery() throws Exception {
		Connector conn = getConnectorInstance();
		if (conn == null) {
			return;  //Impossible?
		}
		Method method = conn.getClass().getMethod("getConnection"); //$NON-NLS-1$
		if (method == null) {
			return;
		}
		jdbcConn = (Connection) method.invoke(conn, (Object[]) null);
		if (jdbcConn != null) {
			final String info = getJDBCDetails();
			getDisplay().syncExec(new Runnable() {
				public void run() {
					setDetailsData(Messages.getString("JDBCDataBrowser.info"), info);
				}
			});
		}

		navigatorInput = new Vector<Object>();
		navigatorInput.add(jdbcConn);
		try {
			Vector<String> tables = conn.queryTables();
			navigatorInput.addAll(tables);
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e);
		}
	}

	private String getJDBCDetails() {
		StringBuffer details = new StringBuffer();
		DatabaseMetaData db;
		String[] prefix = new String[] { "supports", "get", "is" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		try {
			db = jdbcConn.getMetaData();
			details.append(db.getDatabaseProductName() + " - " + db.getDatabaseProductVersion() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
			details.append(db.getDriverName() + " - " + db.getDriverVersion() + "\n\n"); //$NON-NLS-1$ //$NON-NLS-2$

			ArrayList<Method> list = new ArrayList<Method>();
			for (Method m : db.getClass().getMethods()) {
				list.add(m);
			}
			Collections.sort(list, new Comparator<Method>() {
				public int compare(Method arg0, Method arg1) {
					return arg0.getName().compareTo(arg1.getName());
				}
			});
			for (Method m : list) {
				Class<?>[] params = m.getParameterTypes();
				if (params != null && params.length > 0)
					continue;

				Class<?> retcls = m.getReturnType();
				if (String.class == retcls || Integer.class == retcls || Boolean.class == retcls || boolean.class == retcls
						|| int.class == retcls) {
					String name = m.getName();
					for (String pref : prefix) {
						if (name.startsWith(pref)) {
							details.append(name);
							details.append(":\t"); //$NON-NLS-1$
							try {
								details.append(m.invoke(db, (Object[]) null));
							} catch (Exception e) {
								details.append(e.toString());
							}
							details.append("\n"); //$NON-NLS-1$
						}
					}
				}
			}
		} catch (SQLException e) {
			details.append(Utils.exceptionText(e));
		}

		return details.toString();
	}

	@Override
	protected IContentProvider getNavigatorContentProvider() {
		return new JDBCContentProvider();
	}

	@Override
	protected Object getNavigatorInput() {
		// -- make sure we show the navigator
		if(navigatorInput == null)
			navigatorInput = new Vector<Object>();
		return navigatorInput;
	}

	@Override
	protected IBaseLabelProvider getNavigatorLabelProvider() {
		return null;
	}

	@Override
	protected void handleNavigatorSelectionChanged(SelectionChangedEvent event) {
		if (event.getSelection().isEmpty())
			return;

		Object obj = ((IStructuredSelection) event.getSelection()).getFirstElement();
		if (obj instanceof Connection) {
			UIJob job = new UIJob(Messages.getString("JDBCDataBrowser.12")) { //$NON-NLS-1$
				public IStatus runInUIThread(IProgressMonitor monitor) {
					final String info = getJDBCDetails();
					getDisplay().syncExec(new Runnable() {
						public void run() {
							setDetailsData(Messages.getString("JDBCDataBrowser.connection.info"), info);
						}
					});
					return Status.OK_STATUS;
				}
			};
			job.schedule();
			
		} else if (obj instanceof ColumnDef) {
			ColumnDef cd = (ColumnDef) obj;
			setDetailsData(Messages.getString("JDBCDataBrowser.column.info"), cd.toPrintableString());
			
		} else if (obj instanceof String) {
			StringBuffer buf = new StringBuffer();
			buf.append(obj + "\n\n"); //$NON-NLS-1$
			for(Object cd : getTableSyntax(obj.toString())) {
				if (cd instanceof ColumnDef)
					buf.append(((ColumnDef)cd).toPrintableString());
				else
					buf.append(cd.toString());
			}
			setDetailsData(Messages.getString("JDBCDataBrowser.table.info"), buf.toString());
		}
	}

	@Override
	protected String getNavigatorFormText() {
		return Messages.getString("JDBCDataBrowser.14"); //$NON-NLS-1$
	}

	private class JDBCContentProvider implements ITreeContentProvider {

		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof Vector)
				return ((Vector<?>) parentElement).toArray();
			else if (parentElement instanceof ColumnDef)
				return ((ColumnDef) parentElement).getChildren();
			else if (parentElement instanceof String)
				return getTableSyntax((String) parentElement);
			else
				return new Object[0];
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			if (element instanceof ColumnDef)
				return ((ColumnDef) element).hasChildren();
			else
				return element instanceof String;
		}

		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

	}

	private static class ColumnDef {
		private String name;
		private ArrayList<ColumnItem> children = new ArrayList<ColumnItem>();

		public ColumnDef(String name) {
			super();
			this.name = name;
		}

		public String toPrintableString() {
			StringBuffer buf = new StringBuffer();
			buf.append(name + " [\n"); //$NON-NLS-1$
			for(ColumnItem item : children) {
				buf.append("\t" + item.toString() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			buf.append("]\n"); //$NON-NLS-1$
			return buf.toString();
		}

		public void addChild(ColumnItem child) {
			children.add(child);
		}

		public Object[] getChildren() {
			Collections.sort(children, new Comparator<ColumnItem>() {
				public int compare(ColumnItem arg0, ColumnItem arg1) {
					return arg0.toString().compareTo(arg1.toString());
				}
			});
			return children.toArray();
		}
		
		public boolean hasChildren() {
			return false;
		}
		
		public String toString() {
			return name;
		}
	}

	private static class ColumnItem {
		private String name;
		private Object value;
		public ColumnItem(String name, Object value) {
			super();
			this.name = name;
			this.value = value;
		}
		@Override
		public String toString() {
			return name + ": " + value; //$NON-NLS-1$
		}
		
	}
	
	public Object[] getTableSyntax(String table) {
		ArrayList<Object> list = new ArrayList<Object>();
		try {
			ResultSet cols = jdbcConn.getMetaData().getColumns(jdbcConn.getCatalog(), null, table, null);
			ResultSetMetaData md = cols.getMetaData();
			while (cols.next()) {
				ColumnDef coldef = new ColumnDef(cols.getString("COLUMN_NAME")); //$NON-NLS-1$
				for (int i = 1; i <= md.getColumnCount(); i++) {
					try {
						Object obj = cols.getObject(i);
						if (obj != null)
							coldef.addChild(new ColumnItem(md.getColumnName(i), obj));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				list.add(coldef);
			}
			cols.close();
		} catch (SQLException e) {
			list.add(e);
		}
		return list.toArray();
	}

	@Override
	protected Menu createNavigatorContextMenu(TreeViewer navigator) {
		Menu menu = super.createNavigatorContextMenu(navigator);
		MenuItem item = new MenuItem(menu, SWT.PUSH);
		item.setText(Messages.getString("JDBCDataBrowser.usetable"));
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection sel = (IStructuredSelection) getNavigator().getSelection();
				if(sel.isEmpty())
					return;
				((ConnectorConfig)getEditingConfig()).getConnectionConfig().setParameter("jdbcTable", sel.getFirstElement().toString());
				resetConnectorForm();
			}
		});
		return menu;
	}
}
