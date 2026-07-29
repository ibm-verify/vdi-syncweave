/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.editors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.PlatformUI;

import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.function.SystemFunctions;
import com.ibm.tdi.easyetl.ColumnDataFlow;
import com.ibm.tdi.eclipse.Activator;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.server.RestServerAPI;
import com.ibm.tdi.eclipse.views.ServerView;
import com.ibm.tdi.eclipse.widget.RunALWidget;

public class RunAssemblyLineEditor extends BaseEditor implements ISaveablePart2 {
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public final static String EDITOR_ID = "com.ibm.tdi.eclipse.editors.RunAssemblyLineEditor"; //$NON-NLS-1$

	public static final QualifiedName SAVED_BK_NAME = new QualifiedName(
			"http://www.ibm.com", Activator.TDI_PLUGIN_ID + ".saved.breakpoints"); //$NON-NLS-1$ //$NON-NLS-2$

	private RunALWidget widget;
	private RestServerAPI api;
	private ColumnDataFlow dataFlow;

	private IResourceChangeListener resourceListener;

	public RunAssemblyLineEditor() {
	}

	@Override
	public void createPartControl(Composite parent) {
		Utils.setGridLayout(parent, 1, false);
		try {
			if (getEditorInput() instanceof RunRemoteAssemblyLineInput)
				createWidgetRemote(parent, (RunRemoteAssemblyLineInput) getEditorInput());
			else
				createWidget(parent, (RunAssemblyLineInput) getEditorInput());

			if (widget != null)
				widget.setTerminateOnDispose(false);
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e);
		}
	}

	private void createWidgetRemote(Composite parent, final RunRemoteAssemblyLineInput input) throws Exception {
		widget = new RunALWidget(parent, getTDIConfiguration(), input, this);
		widget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		widget.restoreState(getMemento());
		widget.createLoggerObject(input);
		api = input.getApi();
	}

	private void createWidget(Composite parent, final RunAssemblyLineInput input) {

		if (input.getDebugMode() == 1) {
			dataFlow = new ColumnDataFlow(parent, 0, getTDIConfiguration(), this);
			dataFlow.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		} else {
			widget = new RunALWidget(parent, getTDIConfiguration(), input, null);
			widget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			widget.setEditor(this);
			widget.restoreState(getMemento());

			Job job = new Job(getTitle()) {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						if (api == null)
							api = input.getApi();
						if (api == null)
							api = RestServerAPI.createInstance(input.getProject());
						widget.startAssemblyLine(api, input);
						refreshServer();
						return Status.OK_STATUS;
					} catch (Exception e) {
						return EclipseAppender.statusException(e);
					}
				}
			};
			job.schedule();

			/* L3 code checked in for defect 13705 */
			if (input.getConfig() != null && input.getConfig().getMetamergeConfig() instanceof TDIConfigurationFile) {
				resourceListener = new IResourceChangeListener() {
					public void resourceChanged(IResourceChangeEvent event) {
						IResourceDelta delta = event.getDelta();
						if (delta != null) {
							IFile file = ((TDIConfigurationFile) ((RunAssemblyLineInput) getEditorInput()).getConfig()
									.getMetamergeConfig()).getFile();
							IResourceDelta resDelta = delta.findMember(file.getFullPath());
							if (resDelta != null && (resDelta.getFlags() & IResourceDelta.MOVED_TO) > 0) {
								IResource nfile = ResourcesPlugin.getWorkspace().getRoot().getFile(resDelta.getMovedToPath());
								setPartName(nfile.getName().substring(0, nfile.getName().lastIndexOf(".")));
								firePropertyChange(PROP_TITLE);
							}
						}
					}
				};
				ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceListener);
			}
		}
	}

	@Override
	public void dispose() {
		if (widget != null)
			widget.dispose();

		if (dataFlow != null)
			dataFlow.dispose();
		/* Code added for defect 13705 */
		if (resourceListener != null)
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceListener);

		super.dispose();
	}

	@Override
	public boolean isSaveOnCloseNeeded() {
		return true;
	}

	@Override
	public void setModified(boolean modified) {
		firePropertyChange(IEditorPart.PROP_DIRTY);
	}

	@Override
	public boolean isDirty() {
		// We generally say dirty, to get a question when we try to close the
		// window
		return isRunning();
	}

	public boolean isRunning() {
		if (widget != null)
			return widget.isRunning();
		else if (dataFlow != null)
			return dataFlow.isRunning();
		else
			return false;

	}

	/**
	 * Restart the AssemblyLine with a new RunAssemblyLineInput
	 * 
	 * @param input
	 * @return true if we could restart
	 */
	public boolean restart(RunAssemblyLineInput input) {
		if (widget == null || widget.isRunning())
			return false;
		Composite parent = widget.getParent();
		stopAL(true);
		widget.dispose();
		setInput(input);
		api = null;
		input.initApi();
		createWidget(parent, input);
		parent.layout();
		return true;
	}

	public int promptToSaveOnClose() {
		if (widget != null) {
			widget.saveState(getMemento());
		}

		if (isRunning()) {
			String msg = Messages.getMessage("RunAssemblyLineEditor.killonclose", null);
			MessageDialog dialog = new MessageDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(), Messages
					.getString("LBL.CLOSE"), null, // icon
					msg, MessageDialog.QUESTION, new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL,
							IDialogConstants.CANCEL_LABEL }, 0);

			switch (dialog.open()) {
			case 0:
				// Yes - stop al and close this editor
				stopAL(true);
				break;
			case 1:
				// No - close editor but leave al running
				break;
			case 2:
				// Cancel - don't close editor
				return ISaveablePart2.CANCEL;
			}
		}
		return ISaveablePart2.NO;
	}

	/**
	 * This method starts a background job that sends a terminate signal to the
	 * remote AL.
	 */
	public void stopAL(boolean stopConfigInstance) {
		if (widget != null) {
			new StopRemoteAL(widget, stopConfigInstance).schedule();
			refreshServer();
		}
	}

	private void refreshServer() {
		// -- Refresh server view status
		try {
			IFile file = api.getTDIConfigurationFile().getFile();
			ServerView view = (ServerView) getEditorSite().getPage().findView(ServerView.VIEW_ID);
			if (view != null)
				view.refreshServer(file);
		} catch (Exception e) {
			SystemFunctions.doNothing();
		}
	}

	private static class StopRemoteAL extends Job {

		private RunALWidget widget;
		private boolean stopConfigInstance;

		public StopRemoteAL(RunALWidget widget2, boolean stopConfigInstance) {
			super("");
			this.widget = widget2;
			this.stopConfigInstance = stopConfigInstance;
		}

		@Override
		public IStatus run(IProgressMonitor monitor) {
			try {
				if (widget.stopAssemblyLine() && stopConfigInstance)
					widget.stopConfigInstance();

				return Status.OK_STATUS;
			} catch (Exception e) {
				return EclipseAppender.statusException(e);
			}
		}
	}

}
