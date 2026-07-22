/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.progress.UIJob;

import com.ibm.di.api.remote.ServerInfo;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.server.RMIServerAPI;
import com.ibm.tdi.eclipse.server.RestServerAPI;

public class ViewServerComponents extends BaseAction {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public ViewServerComponents() {
	}

	public void run(IAction action) {
		final IFile file = (IFile) getFirstSelection();
		final String msgid = Messages.getString("actions.viewservercomponents");
		Job job = new Job(msgid + ":" + file.getName()) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					showVersionInfo();
				} catch (Exception e) {
					return EclipseAppender.statusException(e);
				}
				return Status.OK_STATUS;
			}
			
		};
		job.schedule();
	}

	protected void showVersionInfo() throws Exception {
		RestServerAPI api = RestServerAPI.createInstance((IFile) getFirstSelection());
		final StringBuffer buf = new StringBuffer();
		if (api instanceof RMIServerAPI) {
			ServerInfo info = ((RMIServerAPI) api).getSession().getServerInfo();
			String txt;

			// -- Server info
			buf.append("*** ");
			buf.append(Messages.getString("VersionTable.Server.Label"));
			buf.append(": ");
			buf.append(info.getServerVersion());
			buf.append(" - ");
			buf.append(info.getServerID());
			buf.append("\n");
			
			// -- Connectors
			buf.append("\n*** ");
			buf.append(Messages.getString("miadmin.foldernames.Connectors"));
			buf.append("\n");
			for (String str : info.getInstalledConnectorsNames()) {
				buf.append(str);
				buf.append("\n");
				try {
					txt = info.getConnectorVersionInfo(str);
					if (txt != null)
						buf.append(txt);
				} catch (Exception e) {
					buf.append(e.getMessage());
				}
				buf.append("\n\n");
			}

			// -- Functions
			buf.append("\n*** ");
			buf.append(Messages.getString("miadmin.foldernames.Functions"));
			buf.append("\n");
			for (String str : info.getInstalledFunctionComponentsNames()) {
				buf.append(str);
				buf.append("\n");
				try {
					txt = info.getFunctionComponentVersionInfo(str);
					if (txt != null)
						buf.append(txt);
				} catch (Exception e) {
					buf.append(e.getMessage());
				}
				buf.append("\n\n");
			}
			
			// -- Parsers
			buf.append("\n*** ");
			buf.append(Messages.getString("miadmin.foldernames.Parsers"));
			buf.append("\n");
			for (String str : info.getInstalledParsersNames()) {
				buf.append(str);
				buf.append("\n");
				try {
					txt = info.getParserVersionInfo(str);
					if (txt != null)
						buf.append(txt);
				} catch (Exception e) {
					buf.append(e.getMessage());
				}
				buf.append("\n\n");
			}

		}

		final IStorage storage = new IStorage() {
			public InputStream getContents() throws CoreException {
				return new ByteArrayInputStream(buf.toString().getBytes());
			}

			public IPath getFullPath() {
				return null;
			}

			public String getName() {
				return ((IFile) getFirstSelection()).getName();
			}

			public boolean isReadOnly() {
				return true;
			}

			@SuppressWarnings("rawtypes")
			public Object getAdapter(Class adapter) {
				return null;
			}

		};
		final IEditorInput input = new IStorageEditorInput() {

			public IStorage getStorage() throws CoreException {
				return storage;
			}

			public boolean exists() {
				return false;
			}

			public ImageDescriptor getImageDescriptor() {
				return null;
			}

			public String getName() {
				return storage.getName();
			}

			public IPersistableElement getPersistable() {
				return null;
			}

			public String getToolTipText() {
				return getName();
			}

			@SuppressWarnings("rawtypes")
			public Object getAdapter(Class adapter) {
				return null;
			}
		};
		UIJob job = new UIJob("") {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				try {
					IDE.openEditor(getWorkbench().getActiveWorkbenchWindow().getActivePage(), input, "org.eclipse.ui.DefaultTextEditor");
				} catch (Exception e) {
					return EclipseAppender.statusException(e);
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

}
