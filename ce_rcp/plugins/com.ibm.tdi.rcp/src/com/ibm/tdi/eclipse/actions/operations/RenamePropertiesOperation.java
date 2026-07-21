/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions.operations;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.naming.Binding;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

import com.ibm.di.config.base.BaseConfigurationImpl;
import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.LoopConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeFolder;
import com.ibm.di.function.SystemFunctions;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.editors.BaseEditor;
import com.ibm.tdi.eclipse.log.EclipseAppender;

/**
 * Changes property references after a rename operation.
 */
public class RenamePropertiesOperation extends AbstractOperation {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private IFile oldReference;
	private IFile newReference;
	private boolean hasRefactored;

	private String oldRef;
	private String newRef;
	private boolean changedSomething;
	
	public RenamePropertiesOperation(String label, IFile oldReference, IFile newReference) throws Exception {
		super(label);
		this.oldReference = oldReference;
		this.newReference = newReference;
	}
	
	public RenamePropertiesOperation(IFile file, String oldName, String name) {
		super(name);
		oldReference = file;
		String prefix = "{property." + oldReference.getName().substring(0, oldReference.getName().lastIndexOf(".")) + ":";
		oldRef = prefix + oldName + "}";
		newRef = prefix + name + "}";
	}

	@Override
	public boolean canUndo() {
		return hasRefactored;
	}

	@Override
	public boolean canExecute() {
		return !hasRefactored;
	}

	@Override
	public boolean canRedo() {
		return canExecute();
	}
	
	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		try {
			ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					doExecute(monitor);
				}
			}, ResourcesPlugin.getWorkspace().getRoot(), IWorkspace.AVOID_UPDATE, monitor);
		} catch (CoreException e) {
			throw new ExecutionException(e.getLocalizedMessage(), e);
		}
		return Status.OK_STATUS;
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		return execute(monitor, info);
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		return execute(monitor, info);
	}

	public void doExecute(IProgressMonitor monitor) throws CoreException {

		//
		// First rename the file
		//
		oldReference.move(newReference.getFullPath(), IResource.SHALLOW|IResource.KEEP_HISTORY, monitor);

		oldRef = "{property." + oldReference.getName().substring(0, oldReference.getName().lastIndexOf(".")) + ":";
		newRef = "{property." + newReference.getName().substring(0, newReference.getName().lastIndexOf(".")) + ":";
		processAllFilesInProject();
		
		hasRefactored = !hasRefactored;
		IFile tmp = oldReference;
		oldReference = newReference;
		newReference = tmp;
	}

	public void processAllFilesInProject() throws CoreException {
		List<IFile> errorFiles = new ArrayList<IFile>();
		IWorkbenchWindow workbench = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

		for (IFile file : Utils.getAllConfigFiles(oldReference.getProject())) {
			try {
				TDIConfigurationFile config = null;
				if (workbench != null) {
					FileEditorInput input = new FileEditorInput(file);
					IEditorPart part = workbench.getActivePage().findEditor(input);
					if ( part instanceof BaseEditor) {
						config = ((BaseEditor)part).getFile();
					}
				}

				boolean inEditor = false;
				changedSomething = false;

				if (config != null) {
					inEditor = true;
				} else {
					config = TDIConfigurationFile.loadFile(file);
				}

				fixProperties(config.getDefaultConfigObject());

				if (changedSomething && ! inEditor)
					config.commitChanges(null);
			} catch (Exception e) {
				EclipseAppender.logerror(e.getLocalizedMessage(), e);
				errorFiles.add(file);
			}
		}
		if (errorFiles.size() > 0)
			throw new CoreException(EclipseAppender.statusException(new Exception(
					Messages.getString("RefactorOperation.not.refactored"))));
	}

	@SuppressWarnings("unchecked")
	private void fixProperties(BaseConfiguration bc) {
		if (bc == null)
			return;
		for (String key: bc.getKeys(BaseConfiguration.ONE_LEVEL)) {
			Object o = bc.getParameterRaw(key);
			String pps = ((BaseConfigurationImpl)bc).getParameterPropertySourceFromValue(o);
			if (pps == null)
				continue;
			if (pps.contains(oldRef)) {
				bc.setParameterPropertySource(key, pps.replace(oldRef, newRef));
				changedSomething = true;
			}
		}

		try {
			if (bc instanceof MetamergeConfig) {
				fixProperties(((MetamergeConfig) bc).getDefaultFolder(MetamergeConfig.ASSEMBLYLINE_FOLDER));
			} else if (bc instanceof MetamergeFolder) {
				Enumeration<Binding> e = ((MetamergeFolder) bc).list();
				while (e.hasMoreElements())
					fixProperties((BaseConfiguration)e.nextElement().getObject());
			} else if (bc instanceof AssemblyLineConfig) {
				AssemblyLineConfig alc = (AssemblyLineConfig) bc;
				List<BaseConfiguration> assemblylineItems = alc.getEntryFeedComponents().getConfigurations(null);
				alc.getDataFlowComponents().getConfigurations(assemblylineItems);
				for (BaseConfiguration b : assemblylineItems)
					fixProperties(b);
			} else if (bc instanceof ConnectorConfig) {
				fixProperties(((ConnectorConfig)bc).getConnectionConfig());
				fixProperties(((ConnectorConfig)bc).getParserConfig());
				if (bc instanceof FunctionConfig)
					fixProperties(((FunctionConfig)bc).getFunctionConfig());
				fixProperties(((ConnectorConfig)bc).getAttributeMap(true));
				fixProperties(((ConnectorConfig)bc).getAttributeMap(false));
			} else if (bc instanceof AttributeMapConfig) {
				for (String name: bc.getChildNames()) {
					if (bc.hasParameter(name))
						fixProperties(bc.getChild(name));
				}
			} else if (bc instanceof LoopConfig) {
				LoopConfig loop = (LoopConfig)bc;
				if (loop.getLoopType() == LoopConfig.LOOP_CONNECTOR_FC)
					fixProperties(loop.getLoopConnector());
			}
		} catch (Exception e){
			SystemFunctions.doNothing();
		}
	}

}
