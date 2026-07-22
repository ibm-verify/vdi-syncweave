/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.databrowser;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.SizeLimitExceededException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.progress.UIJob;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.connector.Connector;
import com.ibm.di.entry.Entry;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;

public class LDAPDataBrowser extends DataBrowser {

	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private InitialLdapContext ctx;

	public LDAPDataBrowser(Composite parent, int style,
			BaseConfiguration editingConfig) {
		super(parent, style, editingConfig);
	}

	@Override
	protected void doInitialDiscovery() throws Exception {
		Connector conn = getConnectorInstance();
		Method m = conn.getClass().getMethod("getLdapContext", (Class[]) null); //$NON-NLS-1$
		ctx = (InitialLdapContext) m.invoke(conn, (Object[]) null);
	}

	@Override
	protected IContentProvider getNavigatorContentProvider() {
		return new LDAPContentProvider();
	}

	@Override
	protected Object getNavigatorInput() {
		return ctx;
	}

	@Override
	protected IBaseLabelProvider getNavigatorLabelProvider() {
		return new LDAPLabelProvider();
	}

	@Override
	protected void handleNavigatorSelectionChanged(SelectionChangedEvent event) {
		if (event.getSelection().isEmpty())
			return;

		Object obj = ((IStructuredSelection) event.getSelection())
				.getFirstElement();
		if (obj instanceof InitialLdapContext) {
			try {
				Method m = getConnectorInstance().getClass().getMethod(
						"getServerInfo", (Class[]) null); //$NON-NLS-1$
				Entry info = (Entry) m.invoke(getConnectorInstance(),
						(Object[]) null);
				setDetailsData(Messages.getString("LDAPDataBrowser.server.info"), entryString(info));
			} catch (Exception e) {
				setDetailsData("", Utils.exceptionText(e));
			}
		} else if (obj instanceof LDAPSchemaNode) {
			((LDAPSchemaNode) obj).getEntry();

		} else if (obj instanceof LDAPNode) {
			String dn = ((LDAPNode) obj).getDn();
			try {
				readLDAPEntry(dn);
			} catch (Exception e) {
				setDetailsData("", Utils.exceptionText(e));
			}
		}
	}

	private String entryString(Entry info) {
		StringBuffer buf = new StringBuffer();
		for (String str : info.getAttributeNames()) {
			buf.append("[" + str + "]" + "\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			for (int i = 0; i < info.getAttribute(str).size(); i++) {
				buf
						.append("\t[" + i + "] = " + info.getAttribute(str).getValue(i) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		}
		return buf.toString();
	}

	/**
	 * Retrieves the top level objects to display in the navigator
	 * 
	 * @param ctx
	 * @return The top level Objects
	 */
	public Object[] getTopLevelObjects(InitialLdapContext ctx) {
		ArrayList<Object> list = new ArrayList<Object>();
		try {
			list.add(ctx);
			Attributes attrs = ctx.getAttributes(""); //$NON-NLS-1$
			Attribute schema = attrs.get("subschemasubentry"); //$NON-NLS-1$
			Attribute bases = attrs.get("namingcontexts"); //$NON-NLS-1$
			for (int i = 0; i < bases.size(); i++) {
				if (schema != null
						&& bases.get(i).toString().equalsIgnoreCase(
								schema.get().toString()))
					list.add(new LDAPSchemaNode(null,
							"" + bases.get(i), null, false)); //$NON-NLS-1$
				else
					list.add(new LDAPNode("" + bases.get(i), null)); //$NON-NLS-1$
			}
		} catch (NamingException e) {
			e.printStackTrace();
			list.add(e);
		}
		return list.toArray();
	}

	private class LDAPContentProvider implements ITreeContentProvider {

		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof InitialLdapContext)
				return getTopLevelObjects((InitialLdapContext) parentElement);
			else if (parentElement instanceof LDAPNode)
				return ((LDAPNode) parentElement).getChildren();
			else
				return null;
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			if (element instanceof LDAPNode)
				return ((LDAPNode) element).hasChildren();
			else
				return false;
		}

		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

	}

	private class LDAPSchemaNode extends LDAPNode {

		private DirContext dc;
		private boolean placeholder;

		public LDAPSchemaNode(DirContext dc, String dn, LDAPNode parent) {
			this(dc, dn, parent, false);
		}

		public void getEntry() {
			if (getParent() == null)
				return;

			if (getParent().getRdn().equals(
					Messages.getString("LDAPDataBrowser.19"))) {
				readLDAPEntry("AttributeDefinition/" + getRdn());
			} else if (getParent().getRdn().equals(
					Messages.getString("LDAPDataBrowser.18"))) {
				readLDAPEntry("ClassDefinition/" + getRdn());
			}
		}

		public LDAPSchemaNode(DirContext dc, String dn, LDAPNode parent,
				boolean placeholder) {
			super(dn, parent);
			this.dc = dc;
			this.placeholder = placeholder;
		}

		@Override
		public Object[] getChildren() {
			try {
				if (dc == null) {
					DirContext classes = (DirContext) ctx
							.getSchema("").lookup("ClassDefinition"); //$NON-NLS-1$ //$NON-NLS-2$
					DirContext attrs = (DirContext) ctx
							.getSchema("").lookup("AttributeDefinition"); //$NON-NLS-1$ //$NON-NLS-2$
					return new Object[] {
							new LDAPSchemaNode(
									classes,
									Messages.getString("LDAPDataBrowser.18"), this, true), //$NON-NLS-1$
							new LDAPSchemaNode(
									attrs,
									Messages.getString("LDAPDataBrowser.19"), this, true), //$NON-NLS-1$
					};
				} else {
					ArrayList<LDAPSchemaNode> names = new ArrayList<LDAPSchemaNode>();
					NamingEnumeration<NameClassPair> result = dc.list(""); //$NON-NLS-1$
					while (result.hasMore()) {
						NameClassPair sr = result.next();
						names.add(new LDAPSchemaNode(dc, sr.getName(), this));
					}
					Collections.sort(names, new Comparator<LDAPSchemaNode>() {
						public int compare(LDAPSchemaNode o1, LDAPSchemaNode o2) {
							return o1.getRdn().compareTo(o2.getRdn());
						}
					});
					return names.toArray();
				}
			} catch (Exception e) {
				return new Object[] { e };
			}
		}

		@Override
		public boolean hasChildren() {
			return placeholder || (!(getParent() instanceof LDAPSchemaNode));
		}

	}

	private class LDAPNode {
		private String dn;
		private LDAPNode parent;
		private ArrayList<Object> children;

		public LDAPNode(String dn, LDAPNode parent) {
			super();
			this.dn = dn;
			this.parent = parent;
		}

		public boolean hasChildren() {
			if (children == null) {
				return true;
			}
			return children.size() > 0;
		}

		public String getDn() {
			return dn + getDNSuffix();
		}

		protected String getRdn() {
			return dn;
		}

		public Object[] getChildren() {
			if (children == null) {
				getLDAPChildren();
				return new Object[] { Messages.getString("LDAPDataBrowser.21") }; //$NON-NLS-1$
			} else {
				Collections.sort(children, new Comparator<Object>() {
					public int compare(Object arg0, Object arg1) {
						if (arg0 instanceof Exception
								|| arg1 instanceof Exception)
							return 1;
						else
							return arg0.toString().compareTo(arg1.toString());
					}
				});
				return children.toArray();
			}
		}

		private void getLDAPChildren() {
			Job job = new Job(dn) {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						SearchControls controls = new SearchControls();
						controls.setCountLimit(100);
						children = new ArrayList<Object>();
						NamingEnumeration<SearchResult> sr = ctx.search(
								getDn(), "objectclass=*", null, controls); //$NON-NLS-1$
						while (sr.hasMore()) {
							SearchResult result = sr.next();
							children.add(new LDAPNode(result.getName(),
									LDAPNode.this));
						}
					} catch (SizeLimitExceededException slee) {
						children.add(slee);
					} catch (Exception e) {
						e.printStackTrace();
					}
					return Status.OK_STATUS;
				}

			};
			job.schedule();

			job.addJobChangeListener(new JobChangeAdapter() {
				public void done(IJobChangeEvent event) {
					UIJob update = new UIJob(dn) {
						@Override
						public IStatus runInUIThread(IProgressMonitor monitor) {
							getNavigator().refresh(LDAPNode.this, true);
							return Status.OK_STATUS;
						}
					};
					update.schedule();
				}
			});
		}

		public LDAPNode getParent() {
			return parent;
		}

		private String getDNSuffix() {
			if (parent != null)
				return ", " + parent.getDn(); //$NON-NLS-1$
			else
				return ""; //$NON-NLS-1$
		}

		@Override
		public String toString() {
			return dn;
		}
	}

	private static class LDAPLabelProvider implements ITableLabelProvider,
			ILabelProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof InitialLdapContext)
				return Messages.getString("LDAPDataBrowser.25"); //$NON-NLS-1$
			else if (element instanceof SizeLimitExceededException)
				return Messages.getString("LDAPDataBrowser.26"); //$NON-NLS-1$
			else
				return "" + element; //$NON-NLS-1$
		}

		public void addListener(ILabelProviderListener listener) {
		}

		public void dispose() {
		}

		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
		}

		public Image getImage(Object element) {
			return getColumnImage(element, 0);
		}

		public String getText(Object element) {
			return getColumnText(element, 0);
		}
	}

	private void readLDAPEntry(String dn) {
		Job job = new Job(dn) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					SearchControls controls = new SearchControls();
					controls.setSearchScope(SearchControls.OBJECT_SCOPE);

					String dn = getName();

					if (dn.startsWith("AttributeDefinition/")
							|| dn.startsWith("ClassDefinition/")) {

						String prefix = dn.startsWith("Attribute") ? "AttributeDefinition/"
								: "ClassDefinition/";
						dn = dn.substring(prefix.length());

						final Entry entry = new Entry();
						DirContext def = (DirContext) ctx.getSchema("").lookup(
								prefix + dn);

						Entry info = new Entry();
						for (NamingEnumeration<? extends Attribute> ea = def
								.getAttributes("").getAll(); ea.hasMore();) {
							BasicAttribute ba = (BasicAttribute) ea.next();
							com.ibm.di.entry.Attribute a = new com.ibm.di.entry.Attribute(
									ba.getID());
							for (NamingEnumeration<?> ev = ba.getAll(); ev
									.hasMore();) {
								a.addValue(ev.next());
							}
							info.setAttribute(a);
						}
						final String details = info.toDeltaString();

						while (def != null) {
							Attribute may = def.getAttributes("").get("MAY");
							if (may != null) {
								for (int i = 0; i < may.size(); i++) {
									entry.setAttribute(
											may.get(i) == null ? null : may
													.get(i).toString(), "MAY ("
													+ dn + ")");
								}
							}

							Attribute must = def.getAttributes("").get("MUST");
							if (must != null) {
								for (int i = 0; i < must.size(); i++) {
									entry.setAttribute(
											must.get(i) == null ? null : must
													.get(i).toString(),
											"MUST (" + dn + ")");
								}
							}

							Attribute parent = def.getAttributes("").get("SUP");
							if (parent != null && parent.size() == 1) {
								try {
									dn = (String) parent.get(0);
									def = (DirContext) ctx.getSchema("")
											.lookup(prefix + dn);
								} catch (Exception e) {
									def = null;
								}
							} else {
								def = null;
							}
						}

						UIJob upd = new UIJob(dn) {
							public IStatus runInUIThread(
									IProgressMonitor monitor) {
								setNextEntry(entry);
								setDetailsData(Messages.getString("LDAPDataBrowser.schema"), details);
								return Status.OK_STATUS;
							}

						};
						upd.schedule();

					} else {
						NamingEnumeration<SearchResult> sr = ctx.search(dn,
								"objectclass=*", null, controls); //$NON-NLS-1$
						while (sr.hasMore()) {
							SearchResult result = sr.next();
							Method m = getConnectorInstance().getClass()
									.getMethod("entry2at", SearchResult.class); //$NON-NLS-1$
							final Entry entry = (Entry) m.invoke(
									getConnectorInstance(), result);
							UIJob upd = new UIJob(getName()) {
								public IStatus runInUIThread(
										IProgressMonitor monitor) {
									setDetailsData(Messages.getString("LDAPDataBrowser.entry"), entry == null ? "" : entry.toDeltaString()); //$NON-NLS-1$
									return Status.OK_STATUS;
								}

							};
							upd.schedule();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return Status.OK_STATUS;
			}

		};
		job.schedule();
	}

	@Override
	protected String getNavigatorFormText() {
		return Messages.getString("view.name.0"); //$NON-NLS-1$
	}

	@Override
	protected Menu createNavigatorContextMenu(TreeViewer navigator) {
		Menu menu = super.createNavigatorContextMenu(navigator);
		MenuItem item = new MenuItem(menu, SWT.PUSH);
		item.setText(Messages.getString("LDAPDataBrowser.usesearchbase"));
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection sel = (IStructuredSelection) getNavigator()
						.getSelection();
				if (!(sel.getFirstElement() instanceof LDAPNode))
					return;
				LDAPNode node = (LDAPNode)sel.getFirstElement();
				((ConnectorConfig) getEditingConfig()).getConnectionConfig()
						.setParameter("ldapSearchBase",
								node.getDn());
				resetConnectorForm();
			}
		});
		getNavigator().addSelectionChangedListener(new EnableMenuItem(item));
		return menu;
	}
	
	private static class EnableMenuItem implements ISelectionChangedListener {
		private MenuItem menuItem;
		public EnableMenuItem (MenuItem item) {
			menuItem = item;
		}
		public void selectionChanged(SelectionChangedEvent event) {
			ISelection sel = event.getSelection();
			Object o = null;
			if (sel instanceof IStructuredSelection)
				o = ((IStructuredSelection)sel).getFirstElement();
			menuItem.setEnabled(o instanceof LDAPNode);
		}
	}
}
