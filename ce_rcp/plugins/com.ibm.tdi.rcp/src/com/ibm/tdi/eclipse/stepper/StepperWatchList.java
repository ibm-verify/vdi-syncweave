/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.stepper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.swt.IFocusService;

import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.AttributeValue;
import com.ibm.di.entry.Entry;
import com.ibm.di.util.NotSerializable;
import com.ibm.di.util.NullValue;
import com.ibm.icu.util.StringTokenizer;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.debugger.DebugClient;
import com.ibm.tdi.eclipse.debugger.DebugClientEvent;
import com.ibm.tdi.eclipse.debugger.DebugClientListener;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.util.TDIToolBar;
import com.ibm.tdi.eclipse.widget.BaseWidget;

public class StepperWatchList extends BaseWidget implements DebugClientListener {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private final static String JAVASCRIPT_VARS = "ScriptEngine";

	private TreeViewer tree;

	/**
	 * The Set of expressions we are currently watching
	 */
	private Set<String> watchExpressions = new HashSet<String>();

	/**
	 * The evaluated results of all expressions we are watching.
	 */
	private Map<String, Object> watchList = new Hashtable<String,Object>();

	/**
	 * All variables found in the JavaScript engine
	 */
	private Map<String, Object> javascriptVars = new Hashtable<String,Object>(); //temporary

	/**
	 * remember all expressions that have been entered by the user
	 */
	private List<String> expressions = new ArrayList<String>();

	private static List<String> defaultVars = Arrays.asList("work", "conn");

	private TreeNode varNode = new TreeNode(Messages.getString("WatchListWidget.other.vars"), null, null);
	private TreeNode watchNode = new TreeNode(Messages.getString("Debugger.Watch.List"), null, null);
	private TreeNode workNode = new TreeNode("work", null, null);
	private TreeNode connNode = new TreeNode("conn", null, null);

	private Action editList;

	private DebugClient client;

	public StepperWatchList(Composite parent, DebugClient client) {
		super(parent, 0, null);
		setLayout(new FillLayout());
		this.client = client;
		updateWatchList();
		client.addDebugListener(this);
		createUI(this);
	}

	private void createUI(Composite parent) {

		Form form = createForm(parent, null);
		TDIToolBar bar = new TDIToolBar(form);
		bar.setText(Messages.getString("Debugger.Watch.List"));

		GridLayout gl = new GridLayout(1, true);
		gl.marginWidth = 0;
		gl.marginHeight = 0;
		form.getBody().setLayout(gl);

		tree = new TreeViewer(form.getBody(), SWT.MULTI | SWT.FULL_SELECTION);
		WatchListProvider provider = new WatchListProvider();
		tree.setContentProvider(provider);
		tree.setLabelProvider(provider);
		tree.getTree().setLinesVisible(true);
		tree.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		if (getEditor() != null)
			getEditor().addSelectionProvider(tree);

		// 
		TreeColumn col = new TreeColumn(tree.getTree(), SWT.LEFT);
		col.setText(Messages.getString("Debugger.Name.Expression")); //$NON-NLS-1$
		col.setWidth(200);

		col = new TreeColumn(tree.getTree(), SWT.LEFT);
		col.setText(Messages.getString("Debugger.Value")); //$NON-NLS-1$
		col.setWidth(200);

		col = new TreeColumn(tree.getTree(), SWT.LEFT);
		col.setText(Messages.getString("SchemaEditor.3")); //$NON-NLS-1$
		col.setWidth(200);

		tree.getTree().setHeaderVisible(true);
		
		if(getEditor() != null) {
			IFocusService fs = (IFocusService) getEditor().getSite().getService(IFocusService.class);
			if(fs != null) {
				fs.addFocusTracker(tree.getControl(), "com.ibm.tdi.debugger.watchlist");
				tree.getControl().setData("com.ibm.tdi.widget", this);
			}
		}

		editList = new Action() {
			@Override
			public String getText() {
				return Messages.getString("Debugger.toolbar.WatchList.name");
			}
			@Override
			public void run() {
				editWatchList();
			}		
		};

		bar.add(editList);
		editList.setEnabled(false);
		tree.setInput(watchList);
	}

	/**
	 * Return expressions in the Watch List.
	 * Clear all evaluated results to prepare for new list.
	 * @return  expressions in the Watch List.
	 */
	public List<String> getExpressions() {
		List<String> ret = new ArrayList<String> (watchExpressions);
		ret.add(JAVASCRIPT_VARS);
		watchList.clear();
		return ret;
	}

	public void updateExpression(String ref, Object value) {
		if (! ref.equals(JAVASCRIPT_VARS)) {
			watchList.put(ref, value);
			return;
		}
		if (! (value instanceof Map))
			return;

		javascriptVars = (Map<String,Object>) value;
		
		if (isDisposed())
			return;

		getDisplay().syncExec(new Runnable() {
			public void run() {
				boolean autoExpand = false;
				if(workNode.value == null && javascriptVars.get("work") != null)
					autoExpand = true;
				Object[] elements = tree.getExpandedElements();
				tree.refresh();
				tree.setExpandedElements(elements);
				if(autoExpand) {
					tree.setExpandedState(workNode, true);
					tree.setExpandedState(connNode, true);
				}
			}
		});
	}

	public boolean isWatching(String name) {
		if (name.equals(JAVASCRIPT_VARS) || watchExpressions.contains(name))
			return true;
		if (name.indexOf(" >> ") > 0)
			name = name.substring(0, name.indexOf(" >> "));
		if (expressions.indexOf(name) == -1)
			expressions.add(name);
		return watchExpressions.contains(name);
	}

	public void setEnabledEdit(boolean value) {
		editList.setEnabled(value);
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		if(enabled) {
			updateWatchList();
		}
	}

	private void editWatchList() {

		Dialog dlg = new Dialog(getShell()) {

			private CheckboxTableViewer table;
			private Combo text;
			private Button addbutton;

			private Set<String> tempWatchList;

			@Override
			protected Control createDialogArea(Composite parent) {
				Composite c = (Composite) super.createDialogArea(parent);

				table = CheckboxTableViewer.newCheckList(c, SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL);
				table.setContentProvider(new ArrayContentProvider());

				table.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				table.setInput(expressions);
				tempWatchList = new HashSet<String>(watchExpressions);
				for (String str : tempWatchList) {
					table.setChecked(str, true);
				}

				table.addCheckStateListener(new ICheckStateListener() {
					public void checkStateChanged(CheckStateChangedEvent event) {
						if (!(event.getElement() instanceof String))
							return;
						String expr = (String) event.getElement();
						if (event.getChecked()) {
							tempWatchList.add(expr);
						} else {
							tempWatchList.remove(expr);
						}
					}
				});

				new Label(c, SWT.LEFT).setText(Messages.getString("Debugger.Breakpoint.2"));

				Composite cc = new Composite(c, SWT.NULL);
				cc.setLayout(new GridLayout(2, false));

				text = new Combo(cc, SWT.BORDER);
				text.add("");
				for (String s:expressions)
					text.add(s);
				text.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
				text.addModifyListener(new ModifyListener() {
					public void modifyText(ModifyEvent e) {
						String str = text.getText().trim();
						if(expressions.contains(str) || str.length() == 0) {
							addbutton.setEnabled(false);
							getShell().setDefaultButton(getButton(IDialogConstants.OK_ID));
						} else {
							addbutton.setEnabled(true);
							getShell().setDefaultButton(addbutton);
						}
					}
				});
				addbutton = new Button(cc, SWT.PUSH);
				addbutton.setText(Messages.getString("general.insert.label"));
				addbutton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						String str = text.getText().trim();
						expressions.add(str);
						table.refresh(true);
						table.setChecked(str, true);
						addbutton.setEnabled(false);
						getShell().setDefaultButton(getButton(IDialogConstants.OK_ID));
						tempWatchList.add(str);
					}
				});
				addbutton.setEnabled(false);
				cc.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));

				getShell().setText(Messages.getString("Debugger.toolbar.WatchList.name"));
				return c;
			}

			@Override
			protected void okPressed() {
				watchExpressions = tempWatchList;
				updateWatchList();
				super.okPressed();
			}

			@Override
			protected Point getInitialSize() {
				return new Point(500, 600);
			}

			@Override
			protected void createButtonsForButtonBar(Composite parent) {
				// create only OK button by default
				createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
						true);
			}

		};
		dlg.open();
	}

	protected void updateWatchList() {
		for(String str : watchExpressions) {
			client.removeWatch(str);
		}
		
		for (String str : getExpressions()) {
			try {
				client.addWatch(str);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static class TreeNode {
		String name;
		Object value;
		TreeNode parent;
		Map<String, TreeNode> children;

		public TreeNode(String name, Object value, TreeNode parent) {
			super();
			this.name = name;
			this.value = value;
			this.parent = parent;
			children = new Hashtable<String, TreeNode>();
		}

		public TreeNode getChild(String key, Object value) {
			if (key == null)
				key = "";
			TreeNode child = children.get(key);
			if (child != null) {
				child.value = value;
			} else {
				child = new TreeNode(key, value, this);
				children.put(key,child);
			}
			return child;
		}
		
		public String toString() {
			return name;
		}
	}

	private class WatchListProvider implements ITreeContentProvider, ITableLabelProvider {

		public Object[] getElements(Object inputElement) {
			ArrayList<Object> list = new ArrayList<Object> ();
			workNode.value = javascriptVars.get("work");
			list.add(workNode);
			connNode.value = javascriptVars.get("conn");
			list.add(connNode);
			list.add(varNode);
			list.add(watchNode);
			return list.toArray();
		}

		public Object[] getChildren(Object parentElement) {
			ArrayList<Object> list = new ArrayList<Object> ();
			if ( parentElement == varNode) {
				List<String> keys = new ArrayList<String>(javascriptVars.keySet());
				Collections.sort(keys);
				for (String key:keys) {
					if ( defaultVars.indexOf(key) >= 0)
						continue;
					list.add(varNode.getChild(key, javascriptVars.get(key)));
				}
			} else if (parentElement == watchNode) {
				List<String> watchKeys = new ArrayList<String>(watchList.keySet());
				Collections.sort(watchKeys);
				for (String key:watchKeys) {
					list.add(watchNode.getChild(key, watchList.get(key)));
				}		
			} else if (parentElement instanceof TreeNode) {
				TreeNode node = (TreeNode) parentElement;
				return getChildren(node.value, node);
			}
			return list.toArray();
		}

		public Object[] getChildren(Object obj, TreeNode parent) {
			ArrayList<Object> list = new ArrayList<Object> ();
			if (obj instanceof Entry) {
				Entry e = (Entry) obj;
				List<String> attrNames = Arrays.asList(e.getAttributeNames());
				Collections.sort(attrNames);
				for (String attr: attrNames)
					list.add(parent.getChild(attr, e.getAttribute(attr)));

				List<String> propNames = Arrays.asList(e.getPropertyNames());
				Collections.sort(propNames);
				for (String prop:propNames)
					list.add(parent.getChild("Property: "+prop, e.getProperty(prop)));

			} else if (obj instanceof Attribute) {
				Attribute a = (Attribute) obj;
				for (int i=0; i < a.size(); i++) {
					Object value = a.getValueAV(i);
					list.add(parent.getChild("" + i, value));
				}
			} else if (obj instanceof NotSerializable) {
				NotSerializable ns = (NotSerializable)obj;
				for (int i=0; i < ns.numChildren(); i++) {
					list.add(parent.getChild(ns.getChildName(i), ns.getChild(i)));
				}
			} else if (obj instanceof Map) {
				Map<?, ?> map = (Map) obj;
				for (Map.Entry<?,?> e: map.entrySet()) {
					list.add(parent.getChild(e.getKey().toString(), e.getValue()));
				}
			} else if (obj instanceof List) {
				List<?> l= (List)obj;
				for (int i=0;i<l.size();i++) {
					list.add(parent.getChild(String.valueOf(i), l.get(i)));
				}
			} else if (obj instanceof Object[]) {
				Object[] l = (Object[])obj;
				for (int i=0;i<l.length;i++) {
					list.add(parent.getChild(String.valueOf(i), l[i]));
				}
			} else if (obj != null ) {
				try {
					List<Field> fieldList = Arrays.asList(obj.getClass().getFields());			
					Collections.sort(fieldList, new Comparator<Field>() {
						public int compare(Field f1, Field f2) {
							return f1.getName().compareTo(f2.getName());
						}
					});

					for (Field f: fieldList) {
						list.add(parent.getChild(f.getName(), f.get(obj)));
					}
				} catch (Exception e) {
					EclipseAppender.logerror(e.getMessage(), e);
				}
			}
			return list.toArray();
		}


		public Object getParent(Object element) {
			if (element instanceof TreeNode)
				return ((TreeNode)element).parent;
			return null;
		}

		public boolean hasChildren(Object element) {
			if (! (element instanceof TreeNode))
				return false;
			if ( element == watchNode || element == varNode)
				return true;
			Object o = ((TreeNode)element).value;
			if ( o == null || o instanceof NullValue)
				return false;
			if ( o instanceof NotSerializable)
				return ((NotSerializable)o).numChildren() > 0;
			if (o instanceof String ||
					o instanceof Integer ||
					o instanceof Long ||
					o instanceof Short ||
					o instanceof Byte ||
					o instanceof Double ||
					o instanceof Character ||
					o instanceof Float ||
					o instanceof Boolean ||
					o instanceof Exception)
				return false;
			return true;
		}

		public Image getColumnImage(Object element, int columnIndex) {
			if (columnIndex != 0)
				return null;
			if (element == watchNode || element == varNode)
				return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof TreeNode) {
				TreeNode node = (TreeNode)element;
				Object o = node.value;
				if (columnIndex == 0) {
					if (o instanceof Attribute) {
						Attribute a = (Attribute)o;
						if (a.getOper() != Attribute.ATTRIBUTE_REPLACE)
							return node.name + " (" + a.getOperation() + ")";
					}
					if (o instanceof AttributeValue) {
						AttributeValue av = (AttributeValue)o;
						if (av.getOper() != AttributeValue.AV_UNDEFINED)
							return node.name + " (" + av.getOperation() + ")";
					}
					return node.name;	
				}
				if (o == null)
					return "";
				if (columnIndex == 1) {
					if ( o instanceof Throwable)
						return ((Throwable)o).getMessage();
					return o.toString();
				}
				if (columnIndex == 2) {
					if ( o instanceof NotSerializable) 
						return ((NotSerializable)o).getClassName();
					if (o instanceof NullValue)
						return "Null";
					if (o instanceof AttributeValue) {
						AttributeValue av = (AttributeValue)o;
						o = av.getValue();
					}
					return o.getClass().getName();
				}
			}
			return null;
		}

		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		public void addListener(ILabelProviderListener listener) {
		}

		public void removeListener(ILabelProviderListener listener) {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

	}

	@Override
	public void restoreState(IMemento memento) {
		if(memento == null)
			return;
		
		String str = memento.getTextData();
		if(str == null || str.length() == 0)
			return;
		
		StringTokenizer st = new StringTokenizer(str, "\n");
		while(st.hasMoreTokens()) {
			String xp = st.nextToken();
			expressions.add(xp);
			watchExpressions.add(xp);
			watchList.put(xp, "");
		}
		
		if(tree != null) {
			tree.refresh();
			tree.setExpandedState(watchNode, true);
		}
	}

	@Override
	public void saveState(IMemento memento) {
		if(memento == null)
			return;
		StringBuffer buf = new StringBuffer();
		for(String str : watchExpressions)
			buf.append(str + "\n");
		memento.putTextData(buf.toString());
	}

	public void handleEvent(DebugClientEvent event) {
		switch(event.getCommand()) {
		case DebugClientEvent.EVAL:
			String e = event.getEval();
			if(isWatching(e))
				updateExpression(e, client.getWatchValue(e));
			break;
			
		case DebugClientEvent.STATE_CHANGE:
			// Don't react to this since StepperPanel is enabling/disabling the watch
			// button based on whether we are breaking or not.
			break;
		}
	}

	public void deleteSelectedItems() {
		IStructuredSelection sel = (IStructuredSelection) tree.getSelection();
		for(Object obj : sel.toArray()) {
			watchExpressions.remove(obj.toString());
			watchList.remove(obj.toString());
			client.removeWatch(obj.toString());
		}
		tree.refresh(watchNode);
	}

}
