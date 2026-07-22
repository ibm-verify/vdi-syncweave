/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.SizeLimitExceededException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.progress.UIJob;

import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.widget.LDAPSearchBaseWidget.LDAPContentLabelProvider.LDAPNode;

/**
 * This class is used by the LDAP form to select a search base.
 *
 */
public class LDAPSearchBaseWidget {
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private Object result = null;

	public static String selectSearchBase(Shell shell, InitialLdapContext ctx) {
		LDAPSearchBaseWidget wid = new LDAPSearchBaseWidget(ctx, shell);
		return wid.getSearchBase();
	}
	
	public LDAPSearchBaseWidget(InitialLdapContext ctx, Shell shell) {
		super();
		LDAPContentLabelProvider provider = new LDAPContentLabelProvider();
		TreeViewerDialog dlg = new TreeViewerDialog(shell, provider, provider, ctx);
		if(dlg.open() == Window.OK) {
			result = dlg.getSelection();
		}
	}
	
	public String getSearchBase() {
		if(result instanceof LDAPNode)
			return ((LDAPNode)result).getDn();
		else
			return null;
	}

	private static class TreeViewerDialog extends Dialog {
		
		private ILabelProvider labelProvider;
		private ITreeContentProvider contentProvider;
		private Object input;
		private Object selection = null;

		public TreeViewerDialog(Shell shell, ILabelProvider labelProvider, ITreeContentProvider contentProvider, Object input) {
			super(shell);
			this.labelProvider =labelProvider;
			this.contentProvider = contentProvider;
			this.input = input;
		}
		
		public Object getSelection() {
			return selection;
		}

		@Override
		protected int getShellStyle() {
			return super.getShellStyle() | SWT.RESIZE;
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			getShell().setText(Messages.getString("LDAPDataBrowser.usesearchbase"));
			Composite c = (Composite) super.createDialogArea(parent);
			c.setLayout(new FillLayout());
			TreeViewer viewer = new TreeViewer(c);
			viewer.setLabelProvider(labelProvider);
			viewer.setContentProvider(contentProvider);
			viewer.setInput(input);
			if(selection != null)
				viewer.setSelection(new StructuredSelection(selection));
			viewer.addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					IStructuredSelection sel = (IStructuredSelection) event.getSelection();
					if(sel.isEmpty())
						selection = null;
					else
						selection = sel.getFirstElement();
				}
			});
			return c;
		}
		
	}	

	/**
	 * This class is used as a content and label provider for a TreeViewer. Input should the be initial ldap context from which all children
	 * are derived. Only normal top level objects (e.g. ldap contexts) and their children are shown.
	 *
	 */
	public static class LDAPContentLabelProvider implements ITreeContentProvider, ILabelProvider {

		protected InitialLdapContext ctx;
		private ArrayList<ILabelProviderListener> listeners = new ArrayList<ILabelProviderListener>();
		private Viewer viewer;
		
		//
		// ------- Label provider methods
		//
		public Image getImage(Object element) {
			return null;
		}

		public String getText(Object element) {
			if(element instanceof LDAPNode) {
				LDAPNode node = (LDAPNode) element;
				return node.getRdn();
			} else {
				return "" + element;
			}
		}

		public void addListener(ILabelProviderListener listener) {
			if(!listeners.contains(listener))
				listeners.add(listener);
		}

		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
			listeners.remove(listener);
		}

		//
		// ------- Content provider methods
		//
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof InitialLdapContext)
				return getTopLevelObjects((InitialLdapContext) parentElement);
			else if (parentElement instanceof LDAPNode)
				return ((LDAPNode) parentElement).getChildren();
			else
				return null;
		}

		public Object getParent(Object element) {
			if(element instanceof LDAPNode)
				return ((LDAPNode)element).getParent();
			else
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
			ctx = null;
			if(newInput instanceof InitialLdapContext)
				ctx = (InitialLdapContext) newInput;
			
			this.viewer = viewer;
		}
		
		private Object[] getTopLevelObjects(InitialLdapContext ctx) {
			ArrayList<Object> list = new ArrayList<Object>();
			try {
				String[] attNames = new String[] {"subschemasubentry", "namingcontexts"};  //$NON-NLS-1$ //$NON-NLS-2$ //added by L3 for defect 14402
				Attributes attrs = ctx.getAttributes("", attNames); //$NON-NLS-1$
				Attribute schema = attrs.get(attNames[0]); //$NON-NLS-1$
				Attribute bases = attrs.get(attNames[1]); //$NON-NLS-1$
				//null check added by L3 for  defect 14402
				if(bases == null) 
					return list.toArray();
				
				for (int i = 0; i < bases.size(); i++) {
					if (schema == null ||
							! bases.get(i).toString().equalsIgnoreCase(
									schema.get().toString()))
						list.add(new LDAPNode("" + bases.get(i), null)); //$NON-NLS-1$
				}
			} catch (NamingException e) {
				e.printStackTrace();
				list.add(e);
			}
			return list.toArray();
		}
		

		public class LDAPNode {
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
						if (viewer.getControl().isDisposed())
							return;
						UIJob update = new UIJob(dn) {
							@Override
							public IStatus runInUIThread(IProgressMonitor monitor) {
								if(viewer instanceof TreeViewer)
									((TreeViewer)viewer).refresh(LDAPNode.this);
								else if (viewer instanceof TableViewer)
									((TableViewer)viewer).refresh(LDAPNode.this);
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
		
	}
}
