/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.navigator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonMenuConstants;

import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.natures.TDINature;

/**
 * This class provides a Paste handler when the clipboard contains
 * BaseConfiguration items. The plugin.xml MUST contain a dependency on the
 * eclipse.ui.navigator.resources.actions.EditActions so we can override the
 * Paste behavior in that action provider.
 * 
 */
public class EditActions extends CommonActionProvider {

	@SuppressWarnings("unused") //$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private OverridePaste pasteAction = new OverridePaste();
	private PasteHandler pasteHandler = new PasteHandler();
	private final static String pasteText = Messages.getString("common.Paste.name"); //$NON-NLS-1$
	private final static String pasteId = PlatformUI.PLUGIN_ID + ".PasteAction"; //$NON-NLS-1$
	
	public EditActions() {
	}

	@Override
	public void init(ICommonActionExtensionSite site) {
		super.init(site);

		ISharedImages images = PlatformUI.getWorkbench().getSharedImages();
		pasteAction.setDisabledImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE_DISABLED));
		pasteAction.setImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));
		pasteAction.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_PASTE);
		pasteAction.setText(pasteText);
		pasteAction.setEnabled(false);
	}

	@Override
	public void dispose() {
		if (pasteHandler != null) {
			pasteHandler.dispose();
			pasteHandler = null;
		}
		super.dispose();
	}

	@Override
	public void fillActionBars(IActionBars actionBars) {
		super.fillActionBars(actionBars);

		// -- Check if clipboard contains a top-level BaseConfiguration object
		pasteAction.setEnabled(checkClipboardData());
		if (pasteAction.isEnabled()) {
			actionBars.setGlobalActionHandler(ActionFactory.PASTE.getId(), pasteAction);
			actionBars.updateActionBars();
		}
	}

	@Override
	public void fillContextMenu(IMenuManager menu) {
		pasteAction.setEnabled(checkClipboardData());
		if (pasteAction.isEnabled()) {
			// Remove the other paste to avoid confusion
			menu.remove(pasteId);
			menu.appendToGroup(ICommonMenuConstants.GROUP_EDIT, pasteAction);

		} else {
			// Use a translated name for Paste, why do we have to do this? It should be translated.
			IContributionItem x = menu.find(pasteId);
			if (x instanceof ActionContributionItem)
				((ActionContributionItem)x).getAction().setText(pasteText);
		}
	}

	/**
	 * Update the cbConfigs array with BaseConfiguration objects found in the
	 * clipboard that we can create files from.
	 * 
	 * @return true if objects can be pasted in to selection's project
	 */
	private boolean checkClipboardData() {
		if (! pasteHandler.checkClipboardData())
			return false;

		// -- Check if we have a target to which the paste can go
		if(! pasteHandler.setTarget(getContext().getSelection()))
			return false;

		// Check that the nature is TDI_NATURE
		IResource target = pasteHandler.getTarget();
        try {
            IProject project = target.getProject();
            if (project != null && project.hasNature(TDINature.TDI_NATURE_ID))
                    return true;
        } catch (Exception e) {
            EclipseAppender.logerror(e.toString(), e);
        }
		return false;
	}

	public class OverridePaste extends Action {
		@Override
		public void run() {
			try {
				pasteHandler.execute(null);
			} catch (Exception e) {
				EclipseAppender.logerror(e.toString(), e, getActionSite().getViewSite().getShell());				
			}
		}
	}
	
}
