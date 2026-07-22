/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.stepper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.forms.widgets.Form;

import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.debugger.DebugClient;
import com.ibm.tdi.eclipse.debugger.DebugClientEvent;
import com.ibm.tdi.eclipse.debugger.DebugClientListener;
import com.ibm.tdi.eclipse.debugger.DebugClient.DebugBreak;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.providers.AssemblyLineContentProvider3;
import com.ibm.tdi.eclipse.providers.ConfigLabelProvider;
import com.ibm.tdi.eclipse.widget.BaseWidget;

public class StepperOutline extends BaseWidget implements DebugClientListener {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private TreeViewer tree;
	private TableViewer table;
	private DebugClient client;
	private String currentBreak;
	private String lastBreak;

	public StepperOutline(Composite parent, DebugClient client) throws Exception {
		super(parent, 0);
		this.client = client;
		setLayout(new FillLayout());
		SashForm sash = new SashForm(this, SWT.VERTICAL);
		createOutlineWidget(sash, 0);
		createWorkListWidget(sash, 0);
		sash.setWeights(new int[]{40, 60});
		client.addWatch("work");
		client.addDebugListener(this);
	}

	public void selectComponent(final String component) {
		AssemblyLineConfig alc = (AssemblyLineConfig) tree.getInput();
		final Object comp = alc.getComponent(component);
		if (comp != null) {
			getDisplay().syncExec(new Runnable() {
				public void run() {
					lastBreak = currentBreak;
					currentBreak = component;
					tree.setSelection(new StructuredSelection(comp));
					tree.refresh(comp, true);
					if(lastBreak != null) {
						AssemblyLineConfig alc = (AssemblyLineConfig) tree.getInput();
						Object last = alc.getComponent(lastBreak);
						tree.refresh(last, true);
					}
				}
			});
		}
	}

	public void syncBreakpoint(final BaseConfiguration config) {
		getDisplay().syncExec(new Runnable() {
			public void run() {

				// -- Breakpoints like hooks and attmaps are not shown here
				if(config != null) {
					tree.setSelection(new StructuredSelection(config));
					if(tree.getSelection().isEmpty()) {
						if(currentBreak != null) {
							lastBreak = currentBreak;
							currentBreak = null;
						}
						return;
					}
				}



				AssemblyLineConfig alc = (AssemblyLineConfig) tree.getInput();
				lastBreak = currentBreak;
				currentBreak = config != null ? config.getShortName() : null;
				if(currentBreak != null) {
					Object comp = alc.getComponent(currentBreak);
					if(comp != null) {
						tree.setSelection(new StructuredSelection(comp));
						tree.refresh(comp, true);
					}
				}
				if(lastBreak != null) {
					Object last = alc.getComponent(lastBreak);
					tree.refresh(last, true);
				}
			}
		});
	}

	@Override
	public void setEditingConfig(BaseConfiguration editingConfig) {
		super.setEditingConfig(editingConfig);
		if(tree != null) {
			tree.setInput(editingConfig);
			tree.expandAll();
		}
	}

	private void createOutlineWidget(Composite parent, int style) {
		tree = new TreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		//tree.setContentProvider(new AssemblyLineContentProvider3());
		AssemblyLineContentProvider3 alcp = new AssemblyLineContentProvider3();
		alcp.setIncludeLoopPlaceHolders(false);
		tree.setContentProvider(alcp);
		tree.setLabelProvider(new ConfigLabelProvider() {

			@Override
			public String getColumnText(Object element, int columnIndex) {
				String str = super.getColumnText(element, columnIndex);
				if(columnIndex == 0 && str != null && str.equals(currentBreak)) {
					str = ">" + str;
				}
				return str;
			}
		});
	}

	private void createWorkListWidget(Composite parent, int style) {

		Form frm = createForm(parent, null);
		frm.setText(Messages.getString("ColumnDataFlow.WorkBucket"));
		frm.getBody().setLayout(new FillLayout());

		table = new TableViewer(frm.getBody(), SWT.FULL_SELECTION);
		table.getTable().setFont(JFaceResources.getTextFont());

		TableColumn tc = new TableColumn(table.getTable(), SWT.LEFT);
		tc.setText(Messages.getString("RunOptionsWidget.10"));
		tc.setWidth(100);

		tc = new TableColumn(table.getTable(), SWT.LEFT);
		tc.setText(Messages.getString("RunOptionsWidget.11"));
		tc.setWidth(200);

		table.getTable().setHeaderVisible(true);

		table.setContentProvider(new ArrayContentProvider() {
			@Override
			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof Entry) {
					Entry entry = (Entry) inputElement;
					List<String> coll = new ArrayList<String>();
					coll.addAll(entry.getAttributeCollection());
					Collections.sort(coll);
					ArrayList<Object> list = new ArrayList<Object>();
					for (String str : coll) {
						list.add(entry.getAttribute(str));
					}
					return list.toArray();
				}
				return super.getElements(inputElement);
			}

		});

		table.setLabelProvider(new ITableLabelProvider() {
			public void removeListener(ILabelProviderListener listener) {
			}

			public boolean isLabelProperty(Object element, String property) {
				return false;
			}

			public void dispose() {
			}

			public void addListener(ILabelProviderListener listener) {
			}

			public Image getColumnImage(Object element, int columnIndex) {
				return null;
			}

			public String getColumnText(Object element, int columnIndex) {
				Attribute a = (Attribute) element;
				if (columnIndex == 0)
					return a.getName();
				else
					return a.getValue();
			}
		});
	}

	public void setWorkEntry(Entry work) {
		table.setInput(work);
		table.refresh();
	}

	public void handleEvent(final DebugClientEvent event) {
		if(isDisposed()) {
			client.removeDebugListener(this);
			return;
		}
		switch(event.getCommand()) {
		case DebugClientEvent.EVAL:
			final String expr = event.getData().toString();
			final Object value = client.getWatchValue(expr);
			if("work".equals(expr)) {
				getDisplay().syncExec(new Runnable() {
					public void run() {
						setWorkEntry(value instanceof Entry ? (Entry)value : new Entry());
					}
				});
			}
			break;

		case DebugClientEvent.BREAK:
			if(event.getData() instanceof DebugBreak) {
				DebugBreak db = (DebugBreak) event.getData();
				String bp = db.getBreakpoint();
				if(bp.indexOf(".default_ok") != -1) {
					selectComponent(db.getComponent());
				}
			}

		}
	}

	public void setDebugClient(DebugClient client) {
		this.client = client;
		try {
			client.addWatch("work");
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e);
		}
		client.addDebugListener(this);
	}

	public BaseConfiguration getBreak() {
		AssemblyLineConfig alc = (AssemblyLineConfig) tree.getInput();
		if(currentBreak != null)
			return alc.getComponent(currentBreak);
		else
			return null;
	}

}
