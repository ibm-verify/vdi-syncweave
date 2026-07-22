/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.wizards;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.ide.IDE;

import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.natures.TDINature;
import com.ibm.tdi.eclipse.wizards.pages.ConfigTypePage;

public class NewComponentBaseWizard extends Wizard implements INewWizard {
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	protected IWorkbench workbench;
	protected IStructuredSelection selection;
	private ConfigTypePage page;

	protected TDIConfigurationFile tdiConfigurationFile;
	private BaseConfiguration configObject;
	private String extension;
	private String type;
	private boolean showTypes = true;
	private boolean chooseFileName = true;
	private boolean nameRequested = false;
	private boolean modeRequested = false;
	private boolean includeNullSelection = false;
	private boolean changeConnectionOnlyOption = true;
	private String name;
	private String mode;
	private BaseConfiguration initialSelection = null;
	private IInputValidator nameValidator;
	private String[] modeFilter = null;

	private String resTitle;

	/**
	 * Base implementation of the INewWizard
	 * 
	 * @param extension
	 *            The file extension of the new file
	 * @param type
	 *            BaseConfiguration type (MetamergeConfig.DEFAULT_<TYPE>_FOLDER)
	 * @param resTitle
	 *            The resource used for the window title
	 */
	public NewComponentBaseWizard(String extension, String type, String resTitle) {
		super();
		this.extension = (extension.startsWith(".") ? extension : "." + extension); //$NON-NLS-1$ //$NON-NLS-2$
		this.type = type;
		this.resTitle = resTitle;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.workbench = workbench;
		this.selection = selection;
		createConfigObject();
		setWindowTitle(Messages.getString(resTitle));
	}

	public void createConfigObject() {
		try {
			tdiConfigurationFile = new TDIConfigurationFile() {
				final static long serialVersionUID = 2L;

				public IProject getProject() {
					return getSelectionProject();
				}
			};
			setConfigObject(tdiConfigurationFile.newInstanceOf(type));
		} catch (Exception e) {
			// This cannot happen?
			EclipseAppender.logerror("Error", e, getShell()); //$NON-NLS-1$
		}
	}

	@Override
	public void addPages() {
		if (configObject != null) {
			page = new ConfigTypePage("TypePage", getType()); //$NON-NLS-1$
			page.setShowTypes(isShowTypes());
			page.setModeRequested(isModeRequested());
			page.setNameRequested(isNameRequested() || isChooseFileName());
			page.setSuggestName(isNameRequested() || isChooseFileName());
			page.setConfigObject(getConfigObject());
			page.setModeFilters(modeFilter);
			page.setIncludeNullSelection(isIncludeNullSelection());
			page.setChangeConnectionOnlyOption(isChangeConnectionOnlyOption());
			page.setNameValidator(getNameValidator());
			addPage(page);
		}
	}

	@Override
	public boolean performFinish() {
		if (page != null) {
			mode = page.getMode();
			name = page.getComponentName();
		}

		updateConfigObject();

		if (isChooseFileName()) {
			try {
				IFile file = createNewFile(name);
				openEditorForFile(file);
			} catch (Exception e) {
				EclipseAppender.logerror(e.toString(), e, getShell());
				return false;
			}
		}
		return true;
	}
	
	protected void openEditorForFile(IFile file) throws Exception {
		if (file != null)
			IDE.openEditor(workbench.getActiveWorkbenchWindow()
					.getActivePage(), file);
	}

	protected void updateConfigObject() {
		// this is a callback method for all the extender classes that need to
		// do some on the fly updates of their configurations.

		if (getConfigObject() instanceof ConnectorConfig) {
			ConnectorConfig cc = (ConnectorConfig) getConfigObject();
			if (Utils.hasParserRequirements(cc) && page != null)
				cc.getParserConfig().setInheritsFromRef(
						page.getConfigObject().getInheritsFromRef());
			if (mode != null)
				cc.setMode(mode);
		}
	}

	public BaseConfiguration getConfigObject() {
		return configObject;
	}

	public void setConfigObject(BaseConfiguration config) {
		this.configObject = config;
		if (page != null)
			page.setConfigObject(config);
	}

	/**
	 * Decides whether the types page is shown with available templates.
	 * 
	 * @return true to show the types page
	 */
	protected boolean isShowTypes() {
		return showTypes;
	}

	public void setShowTypes(boolean value) {
		showTypes = value;
	}

	public boolean isChooseFileName() {
		return chooseFileName;
	}

	public void setChooseFileName(boolean chooseFileName) {
		this.chooseFileName = chooseFileName;
	}

	public boolean isNameRequested() {
		return nameRequested;
	}

	public void setNameRequested(boolean nameRequested) {
		this.nameRequested = nameRequested;
	}

	public void setModeFilter(String[] modes) {
		modeFilter = modes;
	}

	public String getName() {
		return name;
	}

	public String getMode() {
		return mode;
	}

	public BaseConfiguration getInitialSelection() {
		return initialSelection;
	}

	public void setInitialSelection(BaseConfiguration initialSelection) {
		this.initialSelection = initialSelection;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isModeRequested() {
		return modeRequested;
	}

	public void setModeRequested(boolean modeRequested) {
		this.modeRequested = modeRequested;
	}

	public void setNameValidator(IInputValidator validator) {
		this.nameValidator = validator;
	}

	public IInputValidator getNameValidator() {
		return nameValidator;
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	public IStructuredSelection getSelection() {
		return selection;
	}

	/**
	 * Returns the IProject in which the selection is located
	 * 
	 * @return IProject
	 */
	public IProject getSelectionProject() {
		IStructuredSelection sel = getSelection();
		IProject project = null;
		if (sel != null && !sel.isEmpty()) {
			Object obj = sel.getFirstElement();
			if (obj instanceof BaseConfiguration) {
				BaseConfiguration bc = (BaseConfiguration) obj;
				if (bc.getMetamergeConfig() instanceof TDIConfigurationFile)
					project = ((TDIConfigurationFile) bc.getMetamergeConfig())
							.getProject();
			} else if (obj instanceof IResource) {
				project = ((IResource) obj).getProject();
			} else if (obj instanceof IAdaptable) {
				IResource res = (IResource) ((IAdaptable) obj)
						.getAdapter(IResource.class);
				if (res != null)
					project = res.getProject();
			}
		}
		return project;
	}

	/**
	 * Create the new file
	 */
	private IFile createNewFile(String name) throws Exception {

		BaseConfiguration bc = getConfigObject();

		if (name.endsWith(extension))
			name = name.substring(0, name.length() - extension.length());

		IFile file;
		if (type.length() == 0) {
			// Servers are in the top folder of their own project
			try {
				file = Utils.getTDIServersProject(true).getFile(
						name + extension);
			} catch (Exception e) {
				EclipseAppender.logerror("getTDIServersProject", e);
				return null;
			}
		} else {
			// -- TDI Project
			IProject project = getSelectionProject();
			if (project == null)
				return null;

			IFolder path = null;
			if (MetamergeConfig.DEFAULT_ASSEMBLYLINE_FOLDER.equals(type)) {
				// AssemblyLines are not in the Resources folder
				path = project.getFolder(TDINature.getDefaultFolder(bc));
			} else {
				// -- Subfolder in Resources directory
				path = project.getFolder(TDINature.RESOURCES_FOLDER);
				if (!path.exists())
					path.create(true, true, null);
				String folder = TDINature.getDefaultFolder(bc);
				// Have to specify the folder for Properties
				if (folder == null)
					folder = TDINature.PROPERTIES_FOLDER;
				path = path.getFolder(folder);
			}
			if (!path.exists())
				path.create(true, true, null);
			file = path.getFile(name + extension);
		}

		// -- Check if file exists and open confirmation dialog
		if (file.exists()
				&& !MessageDialog.openQuestion(getShell(),
						Messages.getString("general.save.library.label"),
						Messages.getMessage("general.resource.exists",
								file.getFullPath().toOSString(),
								Utils.dateToString(file.getLocalTimeStamp()))))
			return null;

		// -- Create/Overwrite the file
		TDIConfigurationFile cfg = new TDIConfigurationFile(file);
		cfg.setDefaultConfigObject(name, bc);
		cfg.commitVersion(true);
		/**
		 * For some strange reason, commitVersion() may return before the new
		 * content is flushed. Add a small sleep here to help with the problem.
		 */
		Thread.sleep(200);
		return file;
	}

	public boolean isIncludeNullSelection() {
		return includeNullSelection;
	}

	public void setIncludeNullSelection(boolean includeNullSelection) {
		this.includeNullSelection = includeNullSelection;
	}
	
	public ConfigTypePage getConfigTypePage() {
		return page;
	}

	public boolean isChangeConnectionOnlyOption() {
		return changeConnectionOnlyOption;
	}

	public void setChangeConnectionOnlyOption(boolean changeConnectionOnlyOption) {
		this.changeConnectionOnlyOption = changeConnectionOnlyOption;
	}

}
