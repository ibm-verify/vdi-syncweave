/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions;

import java.util.ArrayList;

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.AttributeMapItem;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.HookConfig;
import com.ibm.di.util.HookTree;
import com.ibm.tdi.eclipse.log.EclipseAppender;


public class CopyConfigAction extends CutConfigAction {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public CopyConfigAction(String text) {
		super(text, null);
	}

	
	@Override
	protected boolean setSelection(ISelection selection) {
		items = new ArrayList<BaseConfiguration>();
		if(selection instanceof IStructuredSelection) {
			Object[] sel = ((IStructuredSelection)selection).toArray();
			for(Object o : sel) {
				if(o instanceof BaseConfiguration) {
					BaseConfiguration bc = (BaseConfiguration) o;
					if(bc instanceof ContainerConfig) {
						if(! (bc.getParent() instanceof AssemblyLineConfig))
							items.add(bc);
					} else if (bc instanceof AttributeMapItem){
						if (bc.getShortName() != null)
							items.add(bc);
					} else {
						items.add(bc);
					}
				} else if (o instanceof HookTree) {
					HookConfig hc = ((HookTree)o).getHookConfig(false);
					if (hc != null)
						items.add(hc);
				}
			}
			
			if(sel.length > 0 && sel.length == items.size())
				return true;
		}
		return false;
	}


	@Override
	public void run() {
		Display display = PlatformUI.getWorkbench().getDisplay();
		Clipboard cb = new Clipboard(display);
		LocalSelectionTransfer transfer = LocalSelectionTransfer.getTransfer();
		ArrayList<BaseConfiguration> list = new ArrayList<BaseConfiguration>();
		for(BaseConfiguration b : getItems()) {
			try {
				BaseConfiguration copy = (BaseConfiguration) b.getClone();
				copy.setMetamergeConfig(null);
				copy.setParent(null);
				list.add(copy);
			} catch (Exception e) {
				EclipseAppender.logerror(e.toString(), e);
			}
		}
		if(list.size() == 0) {
			cb.dispose();
			display.beep();
			return;
		}
		
		StructuredSelection sel = new StructuredSelection(list);
		transfer.setSelection(sel);
		cb.setContents(new Object[]{sel}, new Transfer[]{transfer});
	}

}
