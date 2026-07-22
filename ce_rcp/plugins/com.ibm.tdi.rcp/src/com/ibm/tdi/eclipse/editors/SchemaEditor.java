/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.editors;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;

import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.SchemaConfig;
import com.ibm.di.config.interfaces.SchemaItemConfig;
import com.ibm.tdi.eclipse.Activator;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.actions.CopyConfigAction;
import com.ibm.tdi.eclipse.actions.CutConfigAction;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.natures.TDINature;
import com.ibm.tdi.eclipse.util.TDIToolBar;
import com.ibm.tdi.eclipse.validators.ConfigParameterValidator;
import com.ibm.tdi.eclipse.widget.DiscoverSchemaWidget;

public class SchemaEditor extends BaseEditor {
	@SuppressWarnings("unused") //$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public static final String SCHEMA_DESIGN  = Activator.TDI_PLUGIN_ID + ".schemadesign.";
	
	public static final String SCHEMA_DESIGN_NAME = "@@ce.schema.design@@";

	private DiscoverSchemaWidget dsw;
	

	public SchemaEditor() {
		super();
		
		//
		// -- global action handlers
		//
		registerAction(ActionFactory.CUT.getId(), new CutConfigAction("Cut", null)); //$NON-NLS-1$
		registerAction(ActionFactory.COPY.getId(), new CopyConfigAction("Copy")); //$NON-NLS-1$
		registerAction(ActionFactory.DELETE.getId(), new CutConfigAction("Delete", null)); //$NON-NLS-1$
	}

	@Override
	public void createPartControl(Composite parent) {
		Composite c = new Composite(parent, SWT.NULL);
		c.setLayout(new FillLayout());
		
		dsw = new DiscoverSchemaWidget(c, 0, (ConnectorConfig) getTDIConfiguration(), this);
		
		// -- toolbar
		TDIToolBar toolbar = new TDIToolBar(dsw.getForm(), SWT.LEFT|SWT.SINGLE);
		toolbar.add(new Action() {

			@Override
			public String getText() {
				// TODO Auto-generated method stub
				return Messages.getString("SchemaEditor.14");
			}

			@Override
			public String getToolTipText() {
				return Messages.getString("SchemaEditor.15");
			}

			@Override
			public void run() {
				addItem();
			}
			
		});
		toolbar.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
	}
	
	public void addItem() {
		try {
			SchemaConfig sc = getSchemaConfig();
			InputDialog dlg = new InputDialog(dsw.getShell(), Messages.getString("SchemaEditor.17"), Messages.getString("SchemaEditor.18"), "", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					new ConfigParameterValidator(sc, ConfigParameterValidator.MUST_NOT_EXIST));
			if (dlg.open() == InputDialog.OK) {
				SchemaItemConfig sic = sc.newItem(dlg.getValue());
				sic.setAttributeName(dlg.getValue());
				sic.setJavaClass("String"); //$NON-NLS-1$
			}
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e, getSite().getShell());
		}
	}
	
	private SchemaConfig getSchemaConfig() {
		return ((ConnectorConfig)getTDIConfiguration()).getSchema(true);
	}
	
	@Override
	public void setFocus() {
		if(dsw != null)
			dsw.setFocus();

		getEditorSite().getActionBars().updateActionBars();
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
		//
		// -- selection provider
		//
		getEditorSite().setSelectionProvider(getSelectionProvider());
	}

	/**
	 * Returns the design schema for a SchemaConfig object
	 * 
	 * @param config
	 * @return
	 */
	public static SchemaConfig getDesignSchema(BaseConfiguration config) {
		try {
			String design = getDesignSchemaName(config);
			IFile file = getDesignSchemaFile(Utils.getProjectFor(config), design);
			if(file != null) {
				return ((ConnectorConfig)TDIConfigurationFile.load(file)).getSchema(true);
			}
			return null;
			
		} catch (Exception e) {
			return null;
		}
	}
	
	public static SchemaConfig getDesignSchema(IProject project, String fileName) {
		try {
			IFile file = getDesignSchemaFile(project, fileName);
			if(file != null) {
				return ((ConnectorConfig)TDIConfigurationFile.load(file)).getSchema(true);
			}
			return null;
			
		} catch (Exception e) {
			return null;
		}
	}
	
	public static String getDesignSchemaName(BaseConfiguration config) {
		if(config instanceof SchemaConfig) {
			SchemaItemConfig sic = ((SchemaConfig)config).getItem(SCHEMA_DESIGN_NAME);
			if(sic != null)
				return sic.getExternalSyntax();
		}
		return null;
	}

	public static void setDesignSchemaName(BaseConfiguration config, String design) {
		if(config instanceof SchemaConfig) {
			if(design == null) {
				((SchemaConfig)config).removeItem(SCHEMA_DESIGN_NAME);
				return;
			}
			
			SchemaItemConfig sic = ((SchemaConfig)config).getItem(SCHEMA_DESIGN_NAME);
			if(sic == null && design != null) {
				try {
					sic = ((SchemaConfig)config).newItem(SCHEMA_DESIGN_NAME);
				} catch (Exception e) {
					return;
				}
			}
			sic.setExternalSyntax(design);
		}
	}
	
	/**
	 * Returns the named schema file in a project or null if it does not exist.
	 * 
	 * @param project
	 * @param fileName
	 * @return
	 */
	public static IFile getDesignSchemaFile(IProject project, String fileName) {
		try {
			if(project == null || fileName == null)
				return null;
			
			IFolder folder = project.getFolder(TDINature.RESOURCES_FOLDER).getFolder("Schema");
			if(!folder.exists())
				return null;
			
			IFile file = folder.getFile(fileName);
			if(file.exists())
				return file;
			else
				return null;
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Returns a list of design schema files in a project
	 * 
	 * @param project
	 * @return
	 */
	public static ArrayList<IFile> getDesignSchemaFiles(IProject project) {
		try {
			if(project == null)
				return null;
			
			IFolder folder = project.getFolder(TDINature.RESOURCES_FOLDER).getFolder("Schema");
			if(!folder.exists())
				return null;
			
			ArrayList<IFile> files = new ArrayList<IFile>();
			for(IResource res : folder.members()) {
				if(res instanceof IFile && "schema".equals(res.getFileExtension()))
					files.add((IFile) res);
			}
	
			if(files.size() > 0)
				return files;
			else
				return null;
		} catch (Exception e) {
			return null;
		}
	}

}
