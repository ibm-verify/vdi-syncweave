/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.editors;

import java.util.Hashtable;
import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.FormConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.tdi.eclipse.ConfigUtils;
import com.ibm.tdi.eclipse.editors.form.FormEditorWidget;
import com.ibm.tdi.eclipse.log.EclipseAppender;

public class XMLFormEditor extends BaseEditor {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private MetamergeConfig mc;
	private IFile resource;
	private FormEditorWidget ed;

	public XMLFormEditor() {
		super();
	}

	private BaseConfiguration getComponent() throws Exception {
		Vector<String> list = new Vector<String>();
		ConfigUtils.addConnectorNames(mc, list, false);
		if(list.size() == 1)
			return mc.getConnector(list.get(0));
		else
			return null;
	}
	
	@Override
	public void createPartControl(Composite parent) {
	
		try {
			BaseConfiguration comp = getComponent();
			FormConfig fc = null;
			if(comp instanceof ConnectorConfig) {
				fc = (FormConfig) mc.lookup("/Forms/" + ((ConnectorConfig)comp).getConnectionConfig().getJavaClass());
				ed = new FormEditorWidget(parent, SWT.NONE, fc);
			}
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e, getSite().getShell());
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		try {
			if(mc != null && mc.getModified()) {
				mc.commitChanges(null);
				resource.refreshLocal(1, monitor);
			}
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e, getSite().getShell());
		}
	}

	@Override
	public boolean isDirty() {
		return (mc != null && mc.getModified());
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		
		setSite(site);
		setInput(input);
		
		try {
			resource = ((IFileEditorInput) input).getFile();

			// -- Make sure file is synchronized
			if (!resource.isSynchronized(1))
				resource.refreshLocal(1, null);
			
			Hashtable<String, Object> env = new Hashtable<String, Object>();
			env.put(MetamergeConfigFactory.MC_URL, resource.getLocation().toOSString());
			env.put(MetamergeConfigFactory.MC_NO_DEFAULT_FOLDERS, "true");
			mc = MetamergeConfigFactory.getInstance(env);
			
			setModified(false);
		} catch (Exception e) {
			e.printStackTrace();
			EclipseAppender.logerror(e.toString(), e);
			throw new PartInitException(e.toString());
		}
	}

	@Override
	public void dispose() {
		if(mc != null)
			MetamergeConfigFactory.unregisterNamespace(mc.toString());
		if (ed != null)
			ed.dispose();
	}

	@Override
	public void setModified(boolean modified) {
		if(mc != null)
			mc.setModified(false);
		
	}

}
