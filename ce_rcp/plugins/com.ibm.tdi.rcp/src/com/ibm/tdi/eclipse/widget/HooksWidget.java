/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.widget;

import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.forms.widgets.Form;

import com.ibm.di.config.base.InternalSchema;
import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.HookConfig;
import com.ibm.di.config.interfaces.HooksConfig;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.di.config.interfaces.MetamergeConfigChangeListener;
import com.ibm.di.util.HookTree;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.TDI;
import com.ibm.tdi.eclipse.actions.ChangeInheritanceAction;
import com.ibm.tdi.eclipse.actions.RestoreInheritanceAction;
import com.ibm.tdi.eclipse.editors.AssemblyLineEditor3;
import com.ibm.tdi.eclipse.editors.BaseEditor;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.providers.HooksContentProvider;

public class HooksWidget extends BaseWidget 
implements ISelectionChangedListener, MetamergeConfigChangeListener, DisposeListener {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private SashForm sash;
	private CheckboxTreeViewer hooks;
	private Composite itemArea;
	private HashMap<Object, HookItemWidget> itemMap = new HashMap<Object, HookItemWidget>();

	private HooksConfig hc;
	
	private HashMap<String, HookTree> hookMap = new HashMap<String, HookTree>();

	private HooksContentProvider hcp;

	public HooksWidget(BaseConfiguration config, Composite parent, int style) {
		this(config, parent, style, null);
	}

	public HooksWidget(BaseConfiguration config, Composite parent, int style, BaseEditor editor) {
		super(parent, style, config, editor);
		setLayout(new FillLayout());

		hc = null;
		if (config instanceof ConnectorConfig)
			hc = ((ConnectorConfig) config).getHooks();
		else if (config instanceof AssemblyLineConfig)
			hc = ((AssemblyLineConfig) config).getHooks();
		else if (config instanceof HooksConfig)
			hc = (HooksConfig) config;

		if(hc != null)
			config.addListener(this);
		addDisposeListener(this);
		
		Form hform = createForm(this, hc);
		hform.setText(Messages.getString("Hooks"));
		hform.getBody().setLayout(new FillLayout());
		
		sash = new SashForm(hform.getBody(), SWT.HORIZONTAL);
		createUI(sash);
		sash.setWeights(new int[] { 30, 70 });
	}

	private void createUI(Composite parent) {
		
		hcp = new HooksContentProvider();
		hooks = new CheckboxTreeViewer(parent, SWT.BORDER|SWT.MULTI);
		hooks.setContentProvider(hcp);
		hooks.setLabelProvider(hcp);
		hooks.addSelectionChangedListener(this);
		hooks.setInput(getEditingConfig());
		hooks.expandAll();
		
		ColumnViewerToolTipSupport.enableFor(hooks);
		
		// Update checks
		for(Object obj : hcp.getChildren(getEditingConfig())) {
			if(obj instanceof HookTree) {
				HookTree h = (HookTree) obj;
				updateCheck(hooks, h);
			}
		}
		
		hooks.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				BaseConfiguration target = getEditingConfig();
				if(event.getElement() instanceof HookTree) {
					HookTree ht = (HookTree) event.getElement();
					if(ht.hasChildren() || ht.getName().equals("input_attribute_map") || ht.getName().equals("output_attribute_map")) {
						if(event.getChecked())
							hooks.setChecked(ht, false);
						return;
					}
					if(target instanceof ConnectorConfig) {
						((ConnectorConfig)target).getHooks().getHook(ht.getName()).setEnabled(event.getChecked());
					} else if (target instanceof AssemblyLineConfig) {
						((AssemblyLineConfig)target).getHooks().getHook(ht.getName()).setEnabled(event.getChecked());
					} else if (target instanceof HooksConfig) {
						((HooksConfig)target).getHook(ht.getName()).setEnabled(event.getChecked());
					}
					hooks.setSelection(new StructuredSelection(event.getElement()));
				}
			}
		});
		
		DropTargetAdapter dta = new DropTargetAdapter() {
			public void dragOver(DropTargetEvent event) {
				event.feedback = DND.FEEDBACK_NONE;
				if(!(event.item instanceof TreeItem))
					return;

				if((LocalSelectionTransfer.getTransfer().isSupportedType(event.currentDataType)) &&
						(((IStructuredSelection)LocalSelectionTransfer.getTransfer().getSelection()).getFirstElement() instanceof IFile)) {
					event.detail = DND.DROP_COPY;
				}
			}

			public void drop(DropTargetEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.data;
				IFile obj = (IFile) sel.getFirstElement();
				
				if(!MessageDialog.openConfirm(event.widget.getDisplay().getActiveShell(), Messages.getString("HooksWidget.0"), 
						Messages.getMessage("HooksWidget.1", obj.getName())))
					return;
				
				try {
					HookConfig hc = ((HookTree)((TreeItem)event.item).getData()).getHookConfig(true);
					String internal = ((TDIConfigurationFile)hc.getMetamergeConfig()).addReference(obj, null);
					hc.updateInheritsFrom(internal);
					hc.removeParameter(InternalSchema.HC_SCRIPT);
					hc.setEnabled(true);
					HookItemWidget widget = itemMap.get(hc.getHookName());
					if(widget != null) {
						widget.configurationChanged(new MetamergeConfigChange(this, InternalSchema.HC_SCRIPT, 0));
						
					}
				} catch (Exception e) {
					EclipseAppender.logerror(e.toString(), e, getShell());
				}
			}
		};
		hooks.addDropSupport(DND.DROP_COPY, new Transfer[]{LocalSelectionTransfer.getTransfer()}, dta); 
		
		itemArea = new Composite(parent, SWT.FILL);
		itemArea.setLayout(new StackLayout());

		//
		// -- Provide workbench selection
		//
		if(getEditor() != null)
			getEditor().addSelectionProvider(hooks);
		
		//
		// -- Create a registered context menu on the tree control (for object
		// contributions)
		//
		if(getEditor() != null) {
			getEditor().registerContextMenu(hooks, "hooks");
			getEditor().getMenuManager().appendToGroup(TDI.GROUP_TDI, new Action() {
				public String getText() {
					return Messages.getString("action.label.22");
				}
				public void run() {
					RestoreInheritanceAction ria = new RestoreInheritanceAction();
					ria.init(getEditor().getSite().getWorkbenchWindow());
					ria.selectionChanged(this, hooks.getSelection());
					ria.run(this);
				}
			});
			Action inheritAction = new Action() {
				public String getText() {
					return Messages.getString("action.label.24");
				}
				public void run() {
					String hook = getSelectedHook();
					if(hook != null) {
						ChangeInheritanceAction cia = new ChangeInheritanceAction(hc.getHook(hook));
						cia.run();
					}
				}
				@Override
				public boolean isEnabled() {
					IStructuredSelection sel = (IStructuredSelection) hooks.getSelection();
					return sel.size() == 1;
				}
				
			};
			getEditor().getMenuManager().appendToGroup(TDI.GROUP_TDI, new ActionContributionItem(inheritAction) {
				@Override
				public boolean isDynamic() {
					return true;
				}
			}); 
		}
		
		if(getEditor() instanceof AssemblyLineEditor3) {
			getEditor().getMenuManager().appendToGroup(TDI.GROUP_TDI, new Action() {
				public String getText() {
					return Messages.getString("Debugger.Run.and.break");
				}
				public void run() {
					AssemblyLineEditor3 editor = (AssemblyLineEditor3) getEditor();
					Object obj = ((IStructuredSelection)hooks.getSelection()).getFirstElement();
					if(obj instanceof HookConfig) {
						editor.runAssemblyLine(getEditingConfig().getShortName() + "." + ((HookConfig)obj).getHookName());
					} else if (obj instanceof HookTree) {
						editor.runAssemblyLine(getEditingConfig().getShortName() + "." + ((HookTree)obj).getName());
					}
				}
			});
		}
		
	}

	private void updateCheck(CheckboxTreeViewer viewer, HookTree h) {
		hookMap.put(h.getName(), h);
		viewer.setChecked(h, h.isEnabled());

		if(h.hasChildren()) {
			for(Object obj : h.getChildrenArray()) {
				if(obj instanceof HookTree)
					updateCheck(viewer, (HookTree) obj);
			}			
		}
			
	}
	protected HooksConfig getHooks() {
		return ((ConnectorConfig) getEditingConfig()).getHooks();
	}

	public void selectionChanged(SelectionChangedEvent event) {
		IStructuredSelection sel = (IStructuredSelection) hooks.getSelection();
		if (sel.isEmpty())
			return;
		else
			selectItem(sel.getFirstElement());
	}
	
	private void selectItem(Object item) {	
		String hook = null;
		
		if (item instanceof String)
			hook = (String) item;
		else if (item instanceof HookTree) {
			HookTree ht = (HookTree) item;
			if (ht.hasChildren())
				return;
			else
				hook = ht.getName();
		} else if (item instanceof HookConfig) {
			hook = "" + ((HookConfig) item).getHookName();
		}

		if (hook != null) {
			if (hook.startsWith("!"))
				return;
			if(hook.equals("input_attribute_map") || hook.equals("output_attribute_map"))
				return;

			HookConfig hc;
			if (getEditingConfig() instanceof AssemblyLineConfig)
				hc = ((AssemblyLineConfig) getEditingConfig()).getHook(hook);
			else if (getEditingConfig() instanceof ConnectorConfig)
				hc = ((ConnectorConfig) getEditingConfig()).getHooks().getHook(hook);
			else
				return;

			HookItemWidget widget = itemMap.get(hc.getHookName());
			if (widget == null) {
				widget = new HookItemWidget(hc, itemArea, SWT.FILL);
				itemMap.put(hc.getHookName(), widget);
			}
			((StackLayout) itemArea.getLayout()).topControl = widget;
			itemArea.layout();
		}
	}

	public String getSelectedHook() {
		IStructuredSelection sel = (IStructuredSelection) hooks.getSelection();
		if (sel.isEmpty())
			return null;

		Object elem = sel.getFirstElement();
		if (elem instanceof String) {
			return (String) elem;
		} else if (elem instanceof HookTree) {
			HookTree ht = (HookTree) elem;
			if (ht.hasChildren())
				return null;
			else
				return ht.getName();
		} else if (elem instanceof HookConfig) {
			return ((HookConfig) elem).getHookName().toString();
		} else {
			return null;
		}
	}

	@Override
	public void dispose() {
		for(HookItemWidget widget : itemMap.values()) {
			widget.dispose();
		}
		itemMap.clear();
		if(getEditingConfig() != null)
			getEditingConfig().removeListener(this);
		super.dispose();
	}

	public void configurationChanged(MetamergeConfigChange changeEvent) {
		if (isDisposed())
			return;

		if (changeEvent.getSource() instanceof HookConfig 
				|| changeEvent.getSource() instanceof ConnectorConfig
				|| changeEvent.getSource() instanceof HooksConfig) {

			final MetamergeConfigChange mcc = changeEvent;
			getDisplay().asyncExec(new Runnable() {
				public void run() {
					updateXX(mcc);
				}
			});
		}
	}
	
	private void updateXX(MetamergeConfigChange changeEvent) {
		if (isDisposed())
			return;
		if(changeEvent.getSource() instanceof HookConfig) {
			HookConfig hc = (HookConfig) changeEvent.getSource();
			HookTree ht = hookMap.get(hc.getHookName());
			if(ht == null)
				return;
			
			if(InternalSchema.HC_SCRIPT.equals(changeEvent.getKey())) {
				// any mod to the text enables the hook (ref HookItemWidget)
				hooks.setChecked(ht, true);
				hooks.update(ht, null);
			} else if (InternalSchema.HC_ENABLED.equals(changeEvent.getKey())) {
				hooks.setChecked(ht, hc.getEnabled());
			}
		} else if(changeEvent.getSource() instanceof ConnectorConfig) {
			if(!InternalSchema.CONNECTOR_MODE.equals(changeEvent.getKey()))
				return;
			hooks.refresh();
			//L3 code, a for loop added for Update checks defect 13265
			for(Object obj : hcp.getChildren(getEditingConfig())) {
				if(obj instanceof HookTree)
					updateCheck(hooks, (HookTree) obj);
			}
			hooks.expandAll();
		} else if(changeEvent.getSource() instanceof HooksConfig) {
			HookItemWidget widget = null;
			if(changeEvent.getOperation() == MetamergeConfigChange.MCC_REMOVE)
				widget = itemMap.get(changeEvent.getKey());
			if(widget != null) {
				// -- clear current view if it's being displayed 
				if( ((StackLayout)itemArea.getLayout()).topControl == widget ) {
					((StackLayout)itemArea.getLayout()).topControl = null;
				}
				// -- dispose the widget and update tree view
				widget.dispose();
				itemMap.remove(changeEvent.getKey());
				hooks.refresh();
				// Update checks
				for(Object obj : hcp.getChildren(getEditingConfig())) {
					if(obj instanceof HookTree) {
						HookTree h = (HookTree) obj;
						updateCheck(hooks, h);
					}
				}
			}
		}
	}
	
	public void widgetDisposed(DisposeEvent e) {
		if(getEditingConfig() != null)
			getEditingConfig().removeListener(this);
		// Force a focus lost, to update Focus Service properly
		if (hooks != null)
			hooks.getControl().forceFocus();
	}

	@Override
	public boolean revealConfigUI(Object config) {
		hooks.setSelection(new StructuredSelection(config));
		return !hooks.getSelection().isEmpty();
	}
}
