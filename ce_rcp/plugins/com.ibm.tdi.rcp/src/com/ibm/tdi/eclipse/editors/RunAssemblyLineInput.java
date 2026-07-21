/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.editors;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.UUID;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.SequenceConfig;
import com.ibm.di.config.xml.MetamergeConfigXML;
import com.ibm.di.entry.Entry;
import com.ibm.tdi.easyetl.ETLEditor;
import com.ibm.tdi.eclipse.Activator;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.builders.ProjectRuntimeDirectory;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.server.RestServerAPI;
import com.ibm.tdi.eclipse.util.CustomEditorSettings;

public class RunAssemblyLineInput implements IEditorInput {
	@SuppressWarnings("unused") //$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public static final int RUNMODE_NORMAL = 0;
	public static final int RUNMODE_RECORD = 1;
	public static final int RUNMODE_PLAYBACK = 2;
	
	private AssemblyLineConfig config;
	private int stepMode;
	private String operation;
	private Entry entry;
	private Entry initParams;
	private boolean simulateMode;
	private IProject project;
	private MetamergeConfigXML metamergeConfig;
	private String address;
	private boolean foundMatch = false;
	private String breakPoint = null;
	private boolean debug = false;
	private boolean localServer;
	private boolean collectingWork = false;
	private SequenceConfig sConfig;
	private String regressionInputName;
	private String regressionOutputName;
	private int debugMode;
	RestServerAPI api;
	
	public RunAssemblyLineInput() {
	}
	
	public RunAssemblyLineInput(AssemblyLineConfig config) {
		super();
		this.config = config;
		this.project = Utils.getProjectFor(config);
		
		// -- Check if there is an editor open so we can use that version instead
		if(config.getMetamergeConfig() instanceof TDIConfigurationFile) {
			try {
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				IEditorPart editor = page.findEditor(new FileEditorInput(((TDIConfigurationFile)config.getMetamergeConfig()).getFile()));
				if(editor instanceof AssemblyLineEditor3 || editor instanceof ETLEditor) {
					this.config = (AssemblyLineConfig) ((BaseEditor)editor).getTDIConfiguration();
				}
			} catch (Exception e) {
				EclipseAppender.logerror(e.toString(), e);
			}
		}
		initApi();
	}

	public RunAssemblyLineInput(SequenceConfig config) {
		super();
		sConfig = config;
		project = Utils.getProjectFor(config);
		initApi();
	}

	public Object getAdapter(Class adapter) {
		return null;
	}

	public AssemblyLineConfig getConfig() {
		return config;
	}

	public SequenceConfig getSequence() {
		return sConfig;
	}

	public boolean exists() {
		return false;
	}

	public ImageDescriptor getImageDescriptor() {
		return Activator.getImageDescriptor("/icons/Step_16.gif"); //$NON-NLS-1$
	}

	public String getName() {
		if (sConfig != null)
			return sConfig.getShortName();
		return config.getShortName();
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		StringBuilder buf = new StringBuilder();
		if (project != null) {
			buf.append(project.getName());
			buf.append(": ");
		}
		buf.append(config != null ? config.getName() : sConfig.getName());
		return buf.toString();
	}
	
	public RestServerAPI getApi() {
		return api;
	}

	public int getStepMode() {
		return stepMode;
	}

	public String getOperation() {
		return operation;
	}

	public Entry getWorkEntry() {
		return entry;
	}

	public IProject getProject() {
		return project;
	}

	public void setStepMode(int stepMode) {
		this.stepMode = stepMode & 0x0f;
	}

	public void setConfiguration(AssemblyLineConfig alc) {
		config = alc;
	}

	public void setConfiguration(SequenceConfig sc) {
		sConfig = sc;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setWorkEntry(Entry entry) {
		this.entry = entry;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	@Override
	public String toString() {
		return getProject().getName();
	}

	public void setMetamergeConfig(MetamergeConfigXML mx) {
		this.metamergeConfig = mx;
	}

	public MetamergeConfigXML getMetamergeConfig() {
		if (metamergeConfig != null)
			return metamergeConfig;

		// -- Create a temporary config file and update with the provided assemblyline config 
		try {
			IFile rt = Utils.getRuntimeRS(getProject());
			Hashtable<String, Object> env = new Hashtable<String, Object>();
			env.put(MetamergeConfigFactory.MC_URL, rt.getLocation().toOSString());
			MetamergeConfigXML mx = new MetamergeConfigXML(env);
			if (isSequence()) {
				BaseConfiguration copy = (BaseConfiguration) getSequence().getClone();
				mx.rebind(MetamergeConfig.DEFAULT_SEQUENCE_FOLDER + "/" + copy.getShortName(), copy); //$NON-NLS-1$		
			} else {
				BaseConfiguration copy = (BaseConfiguration) getConfig().getClone();
				mx.rebind(MetamergeConfig.DEFAULT_ASSEMBLYLINE_FOLDER + "/" + copy.getShortName(), copy); //$NON-NLS-1$
			}
			// -- when we start an AL on a local server we redirect the config.directory to the project's runtime directory
			if(isLocalServer())
				mx.getRootElement().setAttribute(MetamergeConfigFactory.MC_CONFIG_DIRECTORY, new ProjectRuntimeDirectory(getProject()).getFolder().getLocation().toPortableString());
			return mx;
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e, Display.getCurrent().getActiveShell());
			return null;
		}
	}

	/**
	 * Returns true if the server this object is configured for resides on a local hard drive.
	 * 
	 * @return
	 */
	public boolean isLocalServer() {
		return localServer;
	}

	/**
	 * Call this method to get a config instance ID for running an AL.
	 * 
	 * @param api The api to use
	 * @return ID for existing or newly started TDI config instance
	 * @throws Exception
	 */
	public String getConfigForInput(RestServerAPI api) throws Exception {
		// Make sure we have a unique configID based on project
		String configID = getProject().getName() + "_" + UUID.randomUUID().toString();
		StringBuffer buf = new StringBuffer();
		for(int i = 0; i < configID.length(); i++) {
			if(Character.isJavaIdentifierPart(configID.charAt(i)))
				buf.append(configID.charAt(i));
		}
		configID = buf.toString();
		
		InputStream contents = null;
		MetamergeConfigXML mx = getMetamergeConfig();
		if (mx != null) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			mx.commitChanges(bos);
			contents = new ByteArrayInputStream(bos.toByteArray());
		} else {
			contents = Utils.getRuntimeRS(getProject()).getContents();
		}
		String remoteConfigID = api.startTempConfig(configID, contents);
		
		return remoteConfigID;
	}

	public String getAddress() {
		return address;
	}

	public boolean isSimulateMode() {
		return simulateMode;
	}

	public void setSimulateMode(boolean simulateMode) {
		this.simulateMode = simulateMode;
	}

	boolean isEqual(RunAssemblyLineInput other) {
		if (config != other.config)
			return false;
		if (sConfig != other.sConfig)
			return false;
		if (address == null ? other.address != null : !address.equals(other.address))
			return false;
		if (project == null ? other.project != null : !project.equals(other.project))
			return false;
		return true;
	}
	
	boolean hasFoundMatch() {
		return foundMatch;
	}

	void setFoundMatch() {
		this.foundMatch = true;
	}
	
	public String getBreakPoint() {
		return breakPoint;
	}

	public void setBreakPoint(String breakPoint) {
		this.breakPoint = breakPoint;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebugMode(int i) {
		debugMode = i;
	}
	
	public int getDebugMode() {
		return debugMode;
	}
	
	public boolean isCollectingWork() {
		return collectingWork;
	}

	public void setCollectingWork(boolean collectingWork) {
		this.collectingWork = collectingWork;
	}

	/**
	 * Initializes the server api session 
	 */
	public void initApi() {
		api = null;
		try {
			CustomEditorSettings settings = new CustomEditorSettings(config != null ? config : sConfig);
			settings.loadSettings();
			String prefServer = settings.getString(CustomEditorSettings.TARGET_SERVER, null);
			if(prefServer != null)
				api = RestServerAPI.createInstance(prefServer);
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e);
			api = null;
		}
		try {
			if(api == null)
				api = RestServerAPI.createInstance(project);
			this.address = api.getAddress();
			this.localServer = (api.getInstall() != null ? api.getInstall().length() > 0 : false);
		} catch (Exception e) {
			this.address = project.getName();
		}
	}

	public Entry getInitParams() {
		return initParams;
	}

	public void setInitParams(Entry initParams) {
		this.initParams = initParams;
	}

	public boolean isSequence() {
		return sConfig != null;
	}
	

	public String getRegressionInputName() {
		return regressionInputName;
	}

	public void setRegressionInputName(String regressionInputName) {
		this.regressionInputName = regressionInputName;
	}

	public String getRegressionOutputName() {
		return regressionOutputName;
	}

	public void setRegressionOutputName(String regressionOutputName) {
		this.regressionOutputName = regressionOutputName;
	}

}
