/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.providers;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.MetamergeFolder;
import com.ibm.tdi.eclipse.log.EclipseAppender;

public class SolutionSchemaProvider extends MetamergeFolderContentProvider {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private MetamergeConfig mc;
	private IProject current;

	public SolutionSchemaProvider() {
		super();
	}
	
	public void useProject(IProject project) {
		if(current == project)
			return;
		
		current = project;
		if(project == null) {
			mc = null;
			return;
		}
		
		try {
			IFile ss = (IFile) project.getFile("Solution.schema");
			if(mc instanceof TDIConfigurationFile && ((TDIConfigurationFile)mc).getFile() != null )
				MetamergeConfigFactory.unregisterNamespace(((TDIConfigurationFile)mc).getFile().getLocation().toOSString());

			mc = null;
			
			if(ss != null && ss.exists()) {
				mc = MetamergeConfigFactory.loadNamespace(ss.getLocation().toOSString());
			} else {
				mc = new TDIConfigurationFile(ss);
				mc.createFolder("Schema");
				mc.commitChanges(null);
				if ( ss != null )
					MetamergeConfigFactory.registerNamespace(ss.getLocation().toOSString(), mc);
			}
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e);
		}
	}
	
	public MetamergeFolder getSchemaFolder() throws Exception {
		if(mc != null)
			return (MetamergeFolder) mc.lookup("Schema");
		else
			return null;
	}

	@Override
	public void dispose() {
		if(mc != null)
			MetamergeConfigFactory.unregisterNamespace(((TDIConfigurationFile)mc).getFile().getLocation().toOSString());
		mc = null;
		super.dispose();
	}
	
}
