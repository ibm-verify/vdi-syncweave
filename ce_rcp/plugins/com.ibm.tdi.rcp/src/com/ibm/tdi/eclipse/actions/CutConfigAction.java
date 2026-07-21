/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions;

import java.util.ArrayList;

import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.AttributeMapItem;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.di.config.interfaces.SchemaConfig;
import com.ibm.di.config.interfaces.SchemaItemConfig;
import com.ibm.di.util.HookTree;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.actions.operations.RemoveConfigOperation;
import com.ibm.tdi.eclipse.log.EclipseAppender;

public class CutConfigAction extends Action implements ISelectionListener, IObjectActionDelegate {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	ArrayList<BaseConfiguration> items = null; 
	private IWorkbenchPart part;
	
	public CutConfigAction() {
		super();
	}
	
	public CutConfigAction(String text, ImageDescriptor image) {
		this(text);
	}

	public CutConfigAction(String text) {
		super(text);
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().addSelectionListener(this);
		setEnabled(false);
	}

	public void init(IWorkbenchWindow window) {
		setEnabled(false);
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		this.part = part;
		setEnabled(setSelection(selection));
	}

	public void selectionChanged(IAction action, ISelection selection) {
		action.setEnabled(setSelection(selection));	
	}

	protected boolean setSelection(ISelection selection) {
		items = new ArrayList<BaseConfiguration>();
		ArrayList<BaseConfiguration> mapItems = new ArrayList<BaseConfiguration>();
		if(selection instanceof IStructuredSelection) {
			Object[] sel = ((IStructuredSelection)selection).toArray();
			for(Object o : sel) {
				if(o instanceof BaseConfiguration) {
					if(o instanceof ContainerConfig) {
						ContainerConfig cc = (ContainerConfig) o;
						if(cc.getParent() instanceof AssemblyLineConfig)
							continue;
						items.add(cc);
					} else if ( o instanceof AttributeMapConfig) {
						ConnectorConfig cc = Utils.getParentConfig(o, ConnectorConfig.class);
						if ( cc != null && cc.getParent() != null)
							mapItems.add((BaseConfiguration) o);
					} else if ( o instanceof AttributeMapItem ) {
						AttributeMapItem ami = (AttributeMapItem) o;
						if (ami.getParent() != null && ami.getParent().getParameterRaw(ami.getShortName()) != null)
							items.add(ami);
					} else if ( o instanceof SchemaItemConfig ) {
						SchemaItemConfig sic = (SchemaItemConfig) o;
						if (sic.getParent() instanceof SchemaConfig) {
							if (sic.getParent().hasParameter(sic.getShortName()))
								items.add(sic);
						} else {
							//TODO: Should check if hierarchical schema items are inherited.
							items.add(sic);
						}
					} else {
						items.add((BaseConfiguration) o);
					}
				} else if (o instanceof HookTree) {
					HookTree h = (HookTree)o;
					if (h.getHookConfig(false) != null)
						items.add(h.getHookConfig(false));
				}
			}
			
			// -- Allow deletion of attributemaps (e.g. delete connector)
			if(mapItems.size() > 0 && items.size() == 0)
				items = mapItems;
			
			return items.size() > 0;
		}
		return false;
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.part = targetPart;
	}
	
	public void setSelectedConfigItem(BaseConfiguration config) {
		items = new ArrayList<BaseConfiguration>();
		items.add(config);
		setEnabled(true);
	}
	
	@Override
	public void run() {
		boolean optimize = canOptimizeDelete();
		
		String msg = optimize ? Messages.getString("CutConfigAction.Delete.optimized")
				: Messages.getString("SimpleListUI.prompt.Delete");
		
		
		if(items.get(0) instanceof AttributeMapConfig) {
			msg = Messages.getString("CutConfigAction.indirect.delete");
			ArrayList<BaseConfiguration> newlist = new ArrayList<BaseConfiguration>();
			for(BaseConfiguration b : items) {
				ConnectorConfig cc = Utils.getParentConfig(b, ConnectorConfig.class);
				msg += "\n --" + cc.getShortName();
				newlist.add(cc);
			}
			items = newlist;
		}
		
		if(!MessageDialog.openConfirm(part.getSite().getShell(), Messages.getString("miadmin.menu.Object.DeleteItem.label"), msg))
			return;
		
		if (optimize) {
			optimizedDelete();
			return;
		}
		
		IUndoableOperation operation = new RemoveConfigOperation("Cut", items, items);
		try {
			LocalSelectionTransfer transfer = LocalSelectionTransfer.getTransfer();
			Clipboard cb = new Clipboard(part.getSite().getShell().getDisplay());
			ArrayList<BaseConfiguration> copy = new ArrayList<BaseConfiguration>();
			for(BaseConfiguration b : items)
				copy.add((BaseConfiguration) b.getClone());
			
			StructuredSelection sel = new StructuredSelection(copy);
			transfer.setSelection(sel);
			cb.setContents(new Object[]{sel}, new Transfer[]{transfer});
			
			IUndoContext undoContext = part.getSite().getWorkbenchWindow().getWorkbench().getOperationSupport().getUndoContext();
			operation.addContext(undoContext);
			OperationHistoryFactory.getOperationHistory().execute(operation, null, null);
			
		} catch (Exception e) {
			EclipseAppender.logerror("Cut", e);
		}
	}
	
	public void run(IAction action) {
		run();
	}

	public ArrayList<BaseConfiguration> getItems() {
		return items;
	}

	public IWorkbenchPart getPart() {
		return part;
	}

	/**
	 * Check if we can optimize delete when lots of items being deleted.
	 * There will be no undo, but on the other hand, we do not run out of memory.
	 */
	private boolean canOptimizeDelete() {
		BaseConfiguration first = items.get(0);
		if (items.size() < 50 || ! (first instanceof AttributeMapItem || first instanceof SchemaItemConfig))
			return false;
		
		BaseConfiguration parent = first.getParent();		
		if (! (parent instanceof AttributeMapConfig || parent instanceof SchemaConfig))
			return false;

		for (BaseConfiguration item: items) {
			if (item.getParent() != parent)
				return false;
		}
		return true;			
	}

	/**
	 * Optimize delete when lots of items being deleted.
	 * There will be no undo, but on the other hand, we do not run out of memory.
	 */
	private void optimizedDelete() {
		BaseConfiguration first = items.get(0);
		if (first instanceof AttributeMapItem) {
			AttributeMapConfig amc = (AttributeMapConfig)first.getParent();
			amc.notifyChange(amc, "", MetamergeConfigChange.BEGIN_CHANGES);
			for (BaseConfiguration item:items) {
				amc.removeAttributeMapItem(item);
			}
			amc.notifyChange(amc, "", MetamergeConfigChange.END_CHANGES);
		} else if (first instanceof SchemaItemConfig) {
			SchemaConfig sc = (SchemaConfig)first.getParent();
			sc.notifyChange(sc, "", MetamergeConfigChange.BEGIN_CHANGES);
			for (BaseConfiguration item:items) {
				sc.removeItem(((SchemaItemConfig)item).getAttributeName());
			}
			sc.notifyChange(sc, "", MetamergeConfigChange.END_CHANGES);
		}
	}
}
