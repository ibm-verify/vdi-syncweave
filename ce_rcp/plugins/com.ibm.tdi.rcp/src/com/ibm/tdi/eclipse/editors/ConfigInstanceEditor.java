/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.editors;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.ibm.di.function.SystemFunctions;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.server.RestServerAPI;
import com.ibm.tdi.eclipse.stepper.StepperEvent;
import com.ibm.tdi.eclipse.stepper.StepperListener;
import com.ibm.tdi.eclipse.stepper.StepperThread;
import com.ibm.tdi.eclipse.widget.RunALWidget;

public class ConfigInstanceEditor extends BaseEditor {
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public static final String EDITOR_ID = "com.ibm.tdi.eclipse.configinstance"; //$NON-NLS-1$

	private RunAssemblyLineInput input;

	private RestServerAPI api;

	private CTabFolder tabs;

	private StepperThread st;

	private FormToolkit tk;

	private Form form;

	//
	// -- We store a reference to started config instance ids in this table
	// -- Content is ProjectName.ModifyDate=ConfigID
	// -- We can only reuse a running config instance if the config did not
	// change.
	//
	private Hashtable<String, String> instanceMap = new Hashtable<String, String>();
	
	// Remember all the RunALWidgets we have started, so that we can stop debugging
	private Set<RunALWidget> runALWidgets = new HashSet<RunALWidget>();

	@Override
	public void createPartControl(Composite parent) {

		tk = new FormToolkit(parent.getDisplay());
		form = tk.createForm(parent);
		tk.decorateFormHeading(form);

		Utils.setGridLayout(form.getBody(), 1, false);
		Composite c = form.getBody();
		if (input != null)
			setPartName(input.getProject().getName());
		else
			setPartName(api.getAddress());

		form.setText(getPartName());

		tabs = new CTabFolder(c, SWT.NULL);
		tabs.setSimple(false);
		tabs.setBorderVisible(true);
		tabs.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		try {
			st = new StepperThread(getPartName());
			st.addStepperListener(new StepperListener() {
				public void handleEvent(StepperEvent event) {
					switch (event.getCommand()) {
					case StepperEvent.SS_CONNECT:
						handleDebugConnect(event);
						break;
					case StepperEvent.SS_ERROR:
						((Throwable) event.getData()).printStackTrace();
						break;
					}
				}
			});
			st.start();

			//
			// If there is no specific config instance associated with the input
			// then we target debugging for all instances to ourselves.
			//
			if (input == null) {
				debugServer();
			}

		} catch (Exception e) {
			e.printStackTrace();
			form.setMessage(e.toString(), IMessageProvider.ERROR);
		}

		// Add a Listener to dispose() RunALWidgets when their subtab is closed.
		// Why is this not default?
		tabs.addCTabFolder2Listener(new CTabFolder2Adapter() {
			@Override
			public void close(CTabFolderEvent event) {
				if (event.item instanceof CTabItem && 
						((CTabItem)event.item).getControl() instanceof RunALWidget) {
					RunALWidget raw = (RunALWidget) ((CTabItem)event.item).getControl();
					raw.dispose();
					runALWidgets.remove(raw);
				}
			}
		});
	}

	private void debugServer() {
		form.setMessage(null);
		try {
			enableServerDebug(true);
			form.setText(api.getAddress());
		} catch (Exception e) {
			form.setMessage(e.toString(), IMessageProvider.ERROR);
		}
	}

	private void enableServerDebug(boolean enable) throws Exception {
		String serverDebugString = "";
		if (enable) {
			serverDebugString = st.getHostName() + "," + //$NON-NLS-1$
					st.getLocalPort() + "," + //$NON-NLS-1$
					"false"; //$NON-NLS-1$
		}
		api.setProperty("com.ibm.tdi.autodebug", serverDebugString); //$NON-NLS-1$
	}


	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
		if (input instanceof RunAssemblyLineInput) {
			this.input = (RunAssemblyLineInput) input;

		} else if (input instanceof IFileEditorInput) {
			try {
				api = RestServerAPI.createInstance(((IFileEditorInput) input).getFile());
			} catch (Exception e) {
				throw new PartInitException("RestServerAPI", e); //$NON-NLS-1$
			}
		} else {
			throw new PartInitException("Can only edit RunAssemblyLineInput or IFileEditorInput"); //$NON-NLS-1$
		}
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void setFocus() {
		if (tabs != null)
			tabs.setFocus();
	}

	@Override
	public void dispose() {
		
		// Stop all debugging, to let the ALs continue running 
		for (RunALWidget raw: runALWidgets) {
			raw.dispose();
		}
		runALWidgets.clear();
		
		if (input == null) {
			try {
				enableServerDebug(false);
			} catch (Exception e) {
				//No problem if we could not set this variable
				SystemFunctions.doNothing();
			}
		}

		IRunnableWithProgress gc = new IRunnableWithProgress() {

			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				Collection<String> values = instanceMap.values();
				monitor.beginTask("", values.size()); //$NON-NLS-1$
				for (String cid : values) {
					try {
						monitor.subTask(cid);
						api.stopConfigInstance(cid);
					} catch (Exception ignore) {
						EclipseAppender.logerror(ignore.toString(), ignore);
					}
					monitor.worked(1);
				}
				monitor.done();
			}
		};
		try {
			getSite().getWorkbenchWindow().run(false, true, gc);
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e);
		}

		if (tk != null) {
			tk.dispose();
			tk = null;
		}

		super.dispose();
	}

	protected void addDebugTab(StepperEvent event) {
		final CTabItem t = new CTabItem(tabs, SWT.CLOSE);
		t.setText(event.getThread().getName());

		RunALWidget runWidget = new RunALWidget(tabs, api, this);
		runWidget.setTerminateOnDispose(false);
		try {
			runWidget.handleExternalDebugSession(event, t);
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e);
		}
		t.setControl(runWidget);
		tabs.setSelection(t);
		runALWidgets.add(runWidget);
	}

	/**
	 * Call addDebugTab in the display thread.
	 */
	public void handleDebugConnect(final StepperEvent event) {
		try {
			tabs.getDisplay().syncExec(new Runnable() {
				public void run() {
					addDebugTab(event);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
