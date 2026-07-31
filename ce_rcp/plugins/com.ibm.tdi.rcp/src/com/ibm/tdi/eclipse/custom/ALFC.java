/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.custom;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.ibm.tdi.eclipse.ConfigUtils;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.natures.TDINature;
import com.ibm.tdi.eclipse.widget.FormWidget2;

import com.ibm.di.fc.AssemblyLineFC;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.FormConfig;
import com.ibm.di.config.interfaces.FormItemConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.di.config.interfaces.MetamergeConfigChangeListener;
import com.ibm.di.config.interfaces.SchemaConfig;
import com.ibm.di.config.interfaces.SchemaItemConfig;
import com.ibm.di.config.base.FormConfigImpl;
import com.ibm.di.server.ResourceHash;

import com.ibm.di.api.remote.Session;
import com.ibm.di.api.remote.ConfigInstance;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.naming.NameNotFoundException;

public class ALFC extends Canvas implements MetamergeConfigChangeListener {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final String PROPERTIES_FILE = "assemblylinefc";
	private static ResourceHash sResHash = ResourceHash.getHash(PROPERTIES_FILE);
	private static final String AL_PREFIX = "/" + MetamergeConfig.DEFAULT_ASSEMBLYLINE_FOLDER + "/";

	private BaseConfiguration config;
	private Control widget;
	private BaseConfiguration listenFrom;

	public ALFC(FormWidget2 form, Composite parent, BaseConfiguration editingConfig, String name) {
		super(parent, SWT.BORDER);
		this.config = editingConfig;
		setLayout(new FillLayout());
	}

	public void updateSchema(SchemaConfig schema) {
		if (widget != null) {
			widget.dispose();
			widget = null;
		}
		if (listenFrom != null)
			listenFrom.removeListener(this);

		if (schema == null) {
			layout(true, true);
			return;
		}

		listenFrom = schema.getParent();
		if (listenFrom != null)
			listenFrom.addListener(this);
		List<String> list = schema.getItemNames();
		FormConfig form = new FormConfigImpl();
		try {
			for (String param:list) {
				SchemaItemConfig sic = schema.getItem(param);
				String type = sic.getExternalSyntax();
				if (type == null)
					type = "string";

				FormItemConfig fic = form.newFormItem(AssemblyLineFC.OPERATION_INIT_PREFIX + param);
				fic.setLabel(param);
				fic.setSyntax(type);
				fic.setRequired(sic.isRequired());
				fic.setToolTip(sic.getUserComment());
			}

			widget = new FormWidget2(this, config, form);
			layout(true, true);

		} catch (Exception err) {
			EclipseAppender.logerror(err.getMessage(), err);
			widget = new FormToolkit(getDisplay()).createText(this, err.getMessage(), SWT.BORDER);
		}
	}

	public static String selectOperation (BaseConfiguration config, FormWidget2 form) {
		String alName = config.getStringParameter(AssemblyLineFC.ASSEMBLYLINE);
		if ( alName == null || alName.length() == 0 )
			return null;
		if ( alName.indexOf((MetamergeConfig.DEFAULT_SEQUENCE_FOLDER + "/")) >= 0)
			return null;

		if ( alName.indexOf('/') < 0 )
			alName = AL_PREFIX + alName;

		try {
			AssemblyLineFC fc = new AssemblyLineFC();
			fc.setConfiguration(config);
			String server = config.getStringParameter(AssemblyLineFC.SERVER);
			Session session = null;
			try {
				session = fc.connectServer(server);
			} catch (Exception err) {
				String msg = sResHash.getString("ALFC.NO.SERVER.INSTANCE", err.getMessage());
				EclipseAppender.logerror(msg, err);
				form.alert(msg);
				return null;
			}

			AssemblyLineConfig alc = null;
			String configName = "" + config.getParameter(AssemblyLineFC.CONFIG);
			if ( session != null ) {
				ConfigInstance ci = session.getConfigInstance(configName);
				if ( ci == null ) {
					form.alert(sResHash.getString("ALFC.NO.CONFIG.INSTANCE",
							new Object[] {configName, server}));
					return null;
				}
				alc = (AssemblyLineConfig)ci.getConfiguration().lookup(alName);
			} else {
				MetamergeConfig local = config.getMetamergeConfig();
				if (configName.length() > 0) {
					try {
						IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(configName);
						if (project != null)
							local = Utils.getProjectMC(project);
					} catch (Exception e) {
						EclipseAppender.logerror(e.getMessage(), e);						
					}
				}
				alc = (AssemblyLineConfig)local.lookup(alName);
			}

			ContainerConfig cc = alc.getOperations();
			Vector<String> v = new Vector<String>();
			for (int i = 0; i < cc.size(); i++) {
				v.add(cc.getConfig(i).getShortName());
			}

			return (String)form.chooseFromList (Messages.getString("ALFC.Operation"), v);

		} catch (Exception err) {
			EclipseAppender.logerror(err.getMessage(), err, form.getShell());
		}
		return null;
	}

	public static SchemaConfig getInitSchema(BaseConfiguration config, FormWidget2 form) {
		String assemblyLine = config.getStringParameter(AssemblyLineFC.ASSEMBLYLINE);
		if ( assemblyLine == null || assemblyLine.length() == 0 )
			return null;
		if ( assemblyLine.indexOf((MetamergeConfig.DEFAULT_SEQUENCE_FOLDER + "/")) >= 0)
			return null;

		if ( assemblyLine.indexOf('/') < 0 )
			assemblyLine = AL_PREFIX + assemblyLine;

		try {
			AssemblyLineFC fc = new AssemblyLineFC();
			fc.setConfiguration(config);
			String server = config.getStringParameter(AssemblyLineFC.SERVER);
			Session session = null;
			try {
				session = fc.connectServer(server);
			} catch (Exception err) {
				String msg = sResHash.getString("ALFC.NO.SERVER.INSTANCE2", err.getMessage());
				EclipseAppender.logerror(msg, err, form.getShell());
				return null;
			}

			AssemblyLineConfig alc = null;

			String configName = config.getStringParameter(AssemblyLineFC.CONFIG);
			
			if ( session != null) {
				ConfigInstance ci = session.getConfigInstance(configName);
				if ( ci == null ) {
					form.alert(sResHash.getString("ALFC.NO.CONFIG.INSTANCE2",
							new Object[] {configName, server}));
					return null;
				}
				alc = (AssemblyLineConfig)ci.getConfiguration().lookup(assemblyLine);
			} else {
				MetamergeConfig local = config.getMetamergeConfig();
				if (configName != null && configName.length() > 0) {
					try {
						IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(configName);
						if (project != null)
							local = Utils.getProjectMC(project);
					} catch (Exception e) {
						EclipseAppender.logerror(e.getMessage(), e);						
					}
				}
				alc = (AssemblyLineConfig)local.lookup(assemblyLine);
			}

			// Update the schema as well
			if(config.getParent() instanceof FunctionConfig) {
				fc.updateSchema((FunctionConfig)config.getParent());
			} else if(config.getParent() instanceof ConnectorConfig) {
				fc.updateSchemaConnector((ConnectorConfig)config.getParent());
			}

			return alc.getPublishedInitParams();

		} catch (NameNotFoundException nnfe) {
			return null;
		} catch (Exception err) {
			EclipseAppender.logerror(err.getMessage(), err, form.getShell());
			return null;
		}
	}

	public static String selectAL (BaseConfiguration config, FormWidget2 form) {
		return selectAL(config, form, false);
	}

	public static String selectAL (BaseConfiguration config, FormWidget2 form, boolean includeSeq) {

		try {
			AssemblyLineFC fc = new AssemblyLineFC();
			fc.setConfiguration(config);
			String server = config.getStringParameter(AssemblyLineFC.SERVER);
			Session session = null;
			try {
				session = fc.connectServer(server);
			} catch (Exception err) {
				String msg = sResHash.getString("ALFC.NO.SERVER.INSTANCE3", err.getMessage());
				EclipseAppender.logerror(msg, err, form.getShell());
				return null;
			}
			List<String> v = null;
			String configName = config.getStringParameter(AssemblyLineFC.CONFIG);
			if (configName == null)
				configName = "";
			else
				configName = configName.trim();

			if ( session != null ) {
				ConfigInstance ci = session.getConfigInstance(configName);
				if ( ci == null ) {
					form.alert(sResHash.getString("ALFC.NO.CONFIG.INSTANCE3",
							new Object[] {configName, server}));
					return null;
				}

				v = new Vector<String>();
				v.addAll(Arrays.asList(ci.getAssemblyLineNames()));
			} else {
				BaseConfiguration local = config;
				if (configName.length() > 0) {
					try {
						IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(configName);
						if (project != null)
							local = Utils.getProjectMC(project);
					} catch (Exception e) {
						EclipseAppender.logerror(e.getMessage(), e);						
					}
				}
				v = ConfigUtils.getAvailableSystemComponents(local, MetamergeConfig.ASSEMBLYLINE_FOLDER);
				if (includeSeq)
					v.addAll(ConfigUtils.getAvailableSystemComponents(local, MetamergeConfig.SEQUENCE_FOLDER, false));
			}

			Object answer = form.chooseFromList(Messages.getString("ALFC.Choose.AL"), v);
			if ( answer == null)
				return null;
			return answer.toString();

		} catch (Exception err) {
			EclipseAppender.logerror(err.getMessage(), err, form.getShell());
		}
		return null;
	}

	public static String selectConfig (BaseConfiguration config, FormWidget2 form) {
		try {
			AssemblyLineFC fc = new AssemblyLineFC();
			fc.setConfiguration(config);
			String server = config.getStringParameter(AssemblyLineFC.SERVER);
			Session session = fc.connectServer(server);
			Vector<String> v = new Vector<String>();
			try {
				if ( session == null ) {
					for (IProject p : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
						if (p.hasNature(TDINature.TDI_NATURE_ID))
							v.add(p.getName());
					}
				} else {
					for (ConfigInstance instance:session.getConfigInstances() )
						v.add(instance.getConfigId());
				}
			} catch (Exception err) {
				String msg = sResHash.getString("ALFC.EXCEPTION", err.toString());
				EclipseAppender.logerror(msg, err, form.getShell());
				return null;
			}

			String res = (String) form.chooseFromList (Messages.getString("ALFC.Config"), v);
			if ( res == null || res.length() == 0 )
				return null;

			return res;

		} catch (Exception err) {
			EclipseAppender.logerror(err.getMessage(), err, form.getShell());
		}
		return null;
	}

	public void configurationChanged(MetamergeConfigChange changeEvent) {
		if (changeEvent.getSource() instanceof AssemblyLineConfig &&
				"".equals(changeEvent.getKey()) && 
				MetamergeConfigChange.MCC_REPLACE == changeEvent.getOperation()) {
			if (isDisposed()) {
				if (listenFrom != null)
					listenFrom.removeListener(this);
				return;
			}
			final AssemblyLineConfig alc = (AssemblyLineConfig) changeEvent.getSource();
			getDisplay().asyncExec(new Runnable() {
				public void run() {
					updateSchema(alc.getPublishedInitParams());
				}		
			});
		}
	}
}	
