/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.net.ServerSocketFactory;

import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.LogManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.ibm.di.api.security.CryptoUtils;
import com.ibm.di.config.base.ScriptConfigImpl;
import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.ALMappingConfig;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.AttributeMapItem;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.BranchingConfig;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.HookConfig;
import com.ibm.di.config.interfaces.HooksConfig;
import com.ibm.di.config.interfaces.InstanceConfig;
import com.ibm.di.config.interfaces.LibraryConfig;
import com.ibm.di.config.interfaces.LinkCriteriaConfig;
import com.ibm.di.config.interfaces.LogConfig;
import com.ibm.di.config.interfaces.LoopConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.MetamergeFolder;
import com.ibm.di.config.interfaces.ParserConfig;
import com.ibm.di.config.interfaces.RawConnectorConfig;
import com.ibm.di.config.interfaces.SchemaConfig;
import com.ibm.di.config.interfaces.SchemaItemConfig;
import com.ibm.di.config.interfaces.ScriptConfig;
import com.ibm.di.config.interfaces.SequenceConfig;
import com.ibm.di.config.interfaces.SolutionInterface;
import com.ibm.di.config.interfaces.TombstonesConfig;
import com.ibm.di.config.xml.MetamergeConfigXML;
import com.ibm.di.connector.Connector;
import com.ibm.di.entry.Entry;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.loader.IDILoader;
import com.ibm.di.osgi.OSGiContainerHandle;
import com.ibm.di.osgi.RuntimeEnvironment;
import com.ibm.di.security.Crypto;
import com.ibm.di.server.AssemblyLine;
import com.ibm.di.server.BindAddressPolicy;
import com.ibm.di.server.BindAddressPolicyImpl;
import com.ibm.di.server.FIPSCompliantMode;
import com.ibm.di.server.Log;
import com.ibm.di.server.RSInterface;
import com.ibm.di.server.ServerSocketFactoryEX;
import com.ibm.di.server.StashFile;
import com.ibm.di.util.PropertiesFile;
import com.ibm.tdi.eclipse.actions.RunReportAction;
import com.ibm.tdi.eclipse.console.AssemblyLineConsole;
import com.ibm.tdi.eclipse.editors.BaseEditor;
import com.ibm.tdi.eclipse.jobs.StartLocalServerJob;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.natures.TDINature;
import com.ibm.tdi.eclipse.preferences.PreferenceConstants;
import com.ibm.tdi.eclipse.server.RMIServerAPI;
import com.ibm.tdi.eclipse.server.RestServerAPI;
import com.ibm.tdi.eclipse.server.ServerUtils;

/**
 * The main plugin class to be used in the desktop.
 */
public class Activator extends AbstractUIPlugin {
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public final static String TDI_PLUGIN_ID = "com.ibm.tdi.rcp"; //$NON-NLS-1$

	public static final String TDI_PROBLEM_MARKER_ID = TDI_PLUGIN_ID + ".tdiproblem"; //$NON-NLS-1$

	public static final String TDI_PROBLEM_MARKER_SYMPTOM = TDI_PROBLEM_MARKER_ID + ".symptom"; //$NON-NLS-1$

	// The shared instance.
	private static Activator plugin;
	private MetamergeConfig system;
	private static HashMap<String, Image> images = new HashMap<String, Image>();
	private static Set<String> noImage = new HashSet<String>();

	/**
	 * The constructor.
	 */
	public Activator() {
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation.
	 */
	public void start(BundleContext context) throws Exception {

		super.start(context);
		try {
			loadResources(context);
			createDefaultServer();
			addPackages();
			createRSInterfaceProxy();
			createDynamicMenuItems();
		} catch (Throwable t) {
			EclipseAppender.logerror(t.toString(), t);
			t.printStackTrace();
		}

		// -- Check -tdishutdown command line option
		for (String str : Platform.getCommandLineArgs()) {
			if ("-tdishutdown".equals(str)) {
				shutdownServersAndExit();
			}
		}

		createDefaultP2Profile();

		startDefaultServer();
		startThreadDetectingIPChange(5000);
	}

	/**
	 * Create a default p2 profile.
	 * The entire string is hard coded, we just need something as
	 * a starting point.
	 */
	private void createDefaultP2Profile() {
		try {
			File eclipse = new File(getInstallPath(), "ce/eclipsece");
			File registry = new File(eclipse, 
					"p2/org.eclipse.equinox.p2.engine/profileRegistry/DefaultProfile.profile");
			if (!registry.exists() && ! registry.mkdirs())
				return; // Unable to create the directory
			File profile = new File(registry, "1327398765432.profile");
			if (profile.exists())
				return; // No need to create the file, it already exists.
			FileWriter fw = new FileWriter(profile);
			try {
				fw.write("<?xml version='1.0' encoding='UTF-8'?>\n" +
					"<?profile version='1.0.0'?>\n" +
					"<profile id='DefaultProfile' timestamp='1327398765432'>\n" +
					"  <properties size='2'>\n" +
					"    <property name='org.eclipse.equinox.p2.flavor' value='tooling'/>\n" +
					"    <property name='org.eclipse.equinox.p2.installFolder' value='" +
								eclipse.getCanonicalPath() + "'/>\n" +
					"  </properties>\n" +
					"</profile>\n");
			} finally {
				fw.close();
			}
		} catch (Exception e) {
			SystemFunctions.doNothing();
		}
	}

	/**
	 * This method attempts to start the default server if auto-start pref is
	 * true. There is a small timing issue for creating the default server doc,
	 * so we keep waiting for the file to be created before starting.
	 */
	private void startDefaultServer() {
		IPreferenceStore store = getPreferenceStore();
		if (!store.getBoolean(PreferenceConstants.P_AUTOSTART_SERVER))
			return;

		new Job(TDINature.DEFAULT_SERVER_NAME) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					IFile server = (IFile) Utils.getTDIServer(TDINature.DEFAULT_SERVER_NAME);
					if (server == null) {
						// Try again later
						schedule(1000);
						return Status.OK_STATUS;
					}

					new StartLocalServerJob(server).schedule();

				} catch (Exception e1) {
					return EclipseAppender.statusException(e1);
				}
				return Status.OK_STATUS;
			}
		}.schedule(500);
	}

	/**
	 * Create menu contributions for the AL reports.
	 */
	private void createDynamicMenuItems() {
		Object obj = PlatformUI.getWorkbench().getService(IMenuService.class);
		if (obj instanceof IMenuService)
			RunReportAction.installStandardReports((IMenuService) obj);
	}

	private void shutdownServersAndExit() throws Exception {

		IProject project = Utils.getTDIServersProject(false);
		if (project == null || !project.exists())
			return;

		File myInstallPath = new File(getInstallPath());

		for (IResource res : project.members()) {
			if ("tdiserver".equalsIgnoreCase(res.getFileExtension())) {
				try {
					RestServerAPI api = RestServerAPI.createInstance((IFile) res);
					if (api.getInstall() == null || api.getInstall().length() == 0)
						continue;

					File target = new File(api.getInstall());
					if (myInstallPath.equals(target)) {
						api.stopServer();
					}
				} catch (Exception e) {
					System.err.println(res.getName() + ": " + e.toString());
				}
			}
		}
		System.exit(0);
	}

	public void updateSystemNamespace(IFile file) {
		try {
			Hashtable<String, Object> env = new Hashtable<String, Object>();
			env.put(MetamergeConfigFactory.MC_NO_DEFAULT_FOLDERS, ""); //$NON-NLS-1$
			env.put(MetamergeConfigFactory.MC_URL, file.getContents());
			MetamergeConfigXML mx = new MetamergeConfigXML(env);
			MetamergeConfig sysns = MetamergeConfigFactory.getNamespace("system"); //$NON-NLS-1$
			MetamergeConfigFactory.copy(mx, sysns, null, true);
		} catch (Exception e) {
			EclipseAppender.logerror("copy to system namespace: " + e.toString(), e); //$NON-NLS-1$
		}
	}

	/**
	 * Ensures that the current working directory is a valid solution directory
	 * and that we have a Default server document present.
	 * 
	 * @throws Exception
	 */
	private void createDefaultServer() throws Exception {
		IFile file = (IFile) Utils.getTDIServer("Default"); //$NON-NLS-1$
		if (file.exists())
			return;

		ScriptConfigImpl server = new ScriptConfigImpl();

		// -- Set default values first
		server.setParameter(RestServerAPI.TDI_ADDRESS, ServerUtils.getGlobalPropAddress());
		server.setParameter(RestServerAPI.TDI_INSTALL, getInstallPath());

		server.setParameter(RestServerAPI.TDI_WORKDIR, System.getProperty("user.dir")); //$NON-NLS-1$
		server.setParameter(RestServerAPI.TDI_TYPE, RestServerAPI.TYPE_RMI);
		server.setParameter(RestServerAPI.TDI_SSL, "true");

		// -- Read current values if solution directory already exists with a
		// file set
		ServerUtils.readSolutionDirectory(server);

		// -- Update solution directory (best-effort — failure must not prevent
		// the Default server entry from being written to the workspace)
		try {
			ServerUtils.createSolutionDirectory(server);
		} catch (Exception e) {
			EclipseAppender.logerror("createDefaultServer: createSolutionDirectory failed: " + e, e); //$NON-NLS-1$
		}

		// -- Create Default server entry in TDI Servers project
		Utils.createServerEntry("Default", server); //$NON-NLS-1$

	}

	public static Class<?> loadClass(String clsname) throws Exception {
		if (clsname != null)
			return IDILoader.getInstance().loadClass(clsname);

		return null;
	}

	public InputStream getResource(String path) throws Exception {
		Bundle bundle = Platform.getBundle(TDI_PLUGIN_ID);
		return bundle.getEntry(path).openStream();
	}

	public void loadResources(BundleContext context) throws Exception {
		Bundle bundle = Platform.getBundle(TDI_PLUGIN_ID);

		Bundle systemBundle = (Bundle) OSGiContainerHandle.findBundle(context, BundleContext.class, "org.eclipse.osgi");
		if (systemBundle != null) {
			// provide the TDI code an OSGi runtime so it does not start a new
			// container.
			OSGiContainerHandle.setSystemBundleContext(systemBundle.getBundleContext(), BundleContext.class);
			OSGiContainerHandle osgi = OSGiContainerHandle.getHandle();
			try {
				osgi.startBundle("org.apache.felix.scr");
				osgi.startBundle("com.ibm.di.component");
			} catch (Throwable e) {
				EclipseAppender.logerror(e.getMessage(), e);
			}
		}

		//
		// On a MAC system the working directory is changed by the miadmin
		// binary, which in turn impacts the user.dir property.  The working
		// directory should be the solution directory and so if the current
		// user.dir property doesn't equal the solution directory we change
		// the property here.
		//

        if (!System.getProperty("os.name").startsWith("Windows")) {
            String solDir = System.getenv("TDI_SOLDIR");
            if (solDir != null && !solDir.equals(System.getProperty("user.dir"))) {
                System.setProperty("user.dir", solDir);
            }
        }

		// -- jlog
		System.setProperty("jlog.configuration", new File(getInstallPath(), "etc/jlog.properties").getAbsolutePath()); //$NON-NLS-1$ //$NON-NLS-2$
		System.setProperty("jlog.logger", "jlog.logger.config-editor");

		// -- log4j to eclipse error logger
		EclipseAppender logger = new EclipseAppender(bundle, TDI_PLUGIN_ID);
		if (LogManager.getRootLogger() instanceof Logger)
			((Logger)LogManager.getRootLogger()).addAppender(logger);
		

		//
		// -- Read global and solution properties unencrypted
		//
		try {
			loadProps(new PropertiesFile(null, getInstallPath() + "/etc/global.properties", true)); //$NON-NLS-1$
			File props = new File("solution.properties");
			if (props.exists()) {
				loadProps(new PropertiesFile(null, "solution.properties", true)); //$NON-NLS-1$
			}
		} catch (Exception e) {
			EclipseAppender.logerror("global.properties", e); //$NON-NLS-1$
		}

		// Populate the entire solution directory, to be able to find key stores and such.
		ServerUtils.copySolutionDirectoryFiles(new File(getInstallPath()), new File("."));

		// -- Get all system configurations
		System.setProperty("com.ibm.di.loader.IDILoader.path", getInstallPath()); //$NON-NLS-1$

		//ServerLauncher.initClassLoader();
		addSDIJars();
		addUserJars();

		// System namespace
		system = MetamergeConfigFactory.createSysInstance(IDILoader.getAllSysConfigs());
		RuntimeEnvironment.attachIntegrationComponentConfigs(system);
		MetamergeConfigFactory.registerNamespace(MetamergeConfigFactory.SYSTEM_NAMESPACE, system);
		SystemFunctions.setupSystemConnectorInheritance();

		// Load the packages
		MetamergeConfigFactory.addPackages(IDILoader.getInstalledPackages());

		// Add extra objects to system namespace
		TDI.addSystemNameSpaceExtras();

		//
		// -- Read global and solution properties encrypted values
		//
		try {
			initializeSecurity();

			Crypto crypto = CryptoUtils.getDefaultCrypto();

			// -- Global properties
			loadProps(new PropertiesFile(crypto, getInstallPath() + "/etc/global.properties", true)); //$NON-NLS-1$

			// -- Re-read solution properties
			File props = new File("solution.properties");
			if (props.exists()) {
				loadProps(new PropertiesFile(crypto, "solution.properties", true)); //$NON-NLS-1$
			}

		} catch (Exception e) {
			EclipseAppender.logerror("global.properties", e); //$NON-NLS-1$
		}
	}

	/**
	 * Load properties from a PropertesFile into System properties
	 * 
	 * @param propsFile
	 */
	private void loadProps(PropertiesFile propsFile) throws Exception {
		if (propsFile == null)
			return;
		Iterator<String> it = propsFile.keys();
		while (it.hasNext()) {
			String key = (String) it.next();
			String value = propsFile.getProperty(key);
			System.setProperty(key, value);
		}
	}

        /**
         * Add the SDI jars.
         */

        private static final String TDI_HOME_DIR    = "TDI_HOME_DIR";
        private static final String TDI_LOADER_PATH = "com.ibm.di.loader.IDILoader.path";

        private void addSDIJars() {

            String base = System.getProperty(TDI_HOME_DIR);

            if (base == null || base.length() == 0) {
                base = System.getProperty(TDI_LOADER_PATH);

                if (base == null || base.length() == 0) {
                    base = System.getenv(TDI_HOME_DIR);
                }
            }

            base = base + "/jars/";

            String paths[] = { "common", "connectors", "functions", "parsers" };

            for (String path : paths) {
                String absPath = base + path;

                IDILoader.getInstance().addFiles(absPath);
            }
        }

	/**
	 * Add jars from user defined folders, so that we can display the forms for
	 * custom Connectors
	 */
	private void addUserJars() {

		String userJars = System.getProperty("com.ibm.di.loader.userjars");
		if (userJars != null && userJars.length() > 0) {
			for (String userDir : userJars.split(System.getProperty("path.separator"))) {
				IDILoader.getInstance().addFiles(userDir);
			}
		}
	}

	/**
	 * Returns the TDI installation path we use to obtain components etc
	 * 
	 * @return the TDI installation path.
	 */
	public static String getInstallPath() {

		String installPath = System.getProperty("com.ibm.di.installdir"); //$NON-NLS-1$
		if (installPath != null)
			return installPath;
		
		installPath = System.getProperty("com.ibm.di.loader.IDILoader.path"); //$NON-NLS-1$
		if (installPath == null || installPath.length() == 0)
			installPath = System.getProperty("TDI_HOME_DIR");

		if (installPath == null || installPath.length() == 0 || !new File(installPath).exists()) {
			DirectoryDialog dd = new DirectoryDialog(new Shell());
			dd.setText(Messages.getString("chooseinstall.title")); //$NON-NLS-1$
			dd.setMessage(Messages.getString("chooseinstall.prompt")); //$NON-NLS-1$
			installPath = dd.open();
			if (installPath == null) {
				MessageDialog.openError(new Shell(), "", Messages.getString("chooseinstall.bad.err")); //$NON-NLS-1$ //$NON-NLS-2$
				return ""; //$NON-NLS-1$
			}
		}

		File installFile = new File(installPath);
		if (installFile.exists()) {
			try {
				installPath = installFile.getCanonicalPath();			
			} catch (Exception e) {
				installFile = null; // Do nothing
			}
		}
		System.setProperty("com.ibm.di.installdir", installPath); //$NON-NLS-1$
		return installPath;
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		if (getPreferenceStore().getBoolean(PreferenceConstants.P_AUTOSTOP_SERVER)) {
			stopDevelopmentServer();
		}
		super.stop(context);
		AssemblyLineConsole.setStopped();
		plugin = null;
	}

	private void stopDevelopmentServer() {
		for (Job job:Job.getJobManager().find(StartLocalServerJob.ID)) {
			StartLocalServerJob sJob = (StartLocalServerJob) job;
			if (sJob.isStarting())
				sJob.killServer();
			sJob.cancel();
		}

		if ( !StartLocalServerJob.hasStartedServer() )
			return;

		try {
			RestServerAPI api = RestServerAPI.createInstance("Default"); //$NON-NLS-1$
			if (api == null)
				return;
			if (api.getInstall() == null || api.getInstall().length() == 0)
				return;
			/** For now, we do not need to verify the install path
			if (! new File(getInstallPath()).equals(new File(api.getInstall())))
				return;		
			*/
			if (api instanceof RMIServerAPI) {
				String userDir = ((RMIServerAPI)api).getSession().getJavaProperty("user.dir");
				String workDir = api.getWorkdir();
				if (workDir == null || workDir.length() == 0)
					workDir = api.getInstall();
				if (! new File(userDir).equals(new File(workDir)))
					return;
			}
			api.stopServer();

		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e);
			StartLocalServerJob.forciblyTerminateLastProcess();
		}
	}

	/**
	 * Returns the shared instance.
	 * 
	 * @return the shared instance.
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path.
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(TDI_PLUGIN_ID, path);
	}

	/**
	 * Returns the image for a given path.
	 * 
	 * @param path
	 * @param enabled
	 * @return The image associcated with path
	 */
	public static Image getImage(String path, boolean enabled) {
		return getImage(path, enabled, false);
	}

	/**
	 * Returns the image for a given path.
	 * 
	 * @param path
	 * @param enabled
	 * @param passive
	 * @return The image associcated with path
	 */
	public static Image getImage(String path, boolean enabled, boolean passive) {
		Image img = getImage(path + (enabled ? "_Enabled" : passive ? "_Passive" : "_Disabled")); //$NON-NLS-1$ //$NON-NLS-2$
		if (img != null)
			return img;

		img = getImage(path);
		if (img == null) {
			// some icons are named Connector_Mode_Enabled which equiv to
			// Connector_Mode
			img = getImage(path + "_Enabled");
		}

		// -- If disabled image requested for a normal image we diffuse the
		// image
		// -- and add a red cross to further hightlight its disabled status.
		if (!enabled && img != null) {
			Image disabledImage = new Image(img.getDevice(), img, SWT.IMAGE_DISABLE);
			if (passive) {
				images.put(path + "_Passive", disabledImage);
				return disabledImage;
			}
			int width = img.getImageData().width;
			Image dd = new Image(img.getDevice(), width, img.getImageData().height);
			GC gc = new GC(dd);
			gc.drawImage(disabledImage, 0, 0);
			gc.setForeground(img.getDevice().getSystemColor(SWT.COLOR_RED));
			gc.setLineWidth(3);
			gc.drawLine(width, 0, width - 8, 8);
			gc.drawLine(width - 8, 0, width, 8);
			gc.dispose();
			disabledImage.dispose();
			images.put(path + "_Disabled", dd); //$NON-NLS-1$
			return dd;
		}

		return img;
	}

	public static Image getImage(String path) {
		Image image = images.get(path);
		if (image == null) {
			if (noImage.contains(path))
				return null;
			ImageDescriptor id = getImageDescriptor(path);
			if (id == null)
				id = getImageDescriptor("/icons/" + path + ".gif"); //$NON-NLS-1$ //$NON-NLS-2$
			if (id == null)
				id = getImageDescriptor("/icons/" + path + "_16.gif"); //$NON-NLS-1$ //$NON-NLS-2$
			if (id == null) {
				noImage.add(path);
				return null;
			}
			image = id.createImage(true);
			images.put(path, image);
		}

		return image;
	}

	public static ImageDescriptor getImageDescriptorRelative(String path) {
		ImageDescriptor id = getImageDescriptor(path);
		if (id == null)
			id = getImageDescriptor("/icons/" + path + ".gif"); //$NON-NLS-1$ //$NON-NLS-2$
		if (id == null)
			id = getImageDescriptor("/icons/" + path + "_16.gif"); //$NON-NLS-1$ //$NON-NLS-2$

		return id;
	}

	/**
	 * Returns the image for a given configuration object class
	 * 
	 * @param config
	 *            The configuration object
	 * @return The image associated with path
	 */
	public static Image getImage(BaseConfiguration config) {
		if (config == null)
			return null;

		boolean enabled = isConfigEnabled(config);

		if (config instanceof ScriptConfig)
			return getImage("Script_16", enabled); //$NON-NLS-1$

		if (config instanceof AssemblyLineConfig)
			return getImage("AssemblyLine", true); //$NON-NLS-1$

		else if (config instanceof ALMappingConfig)
			return getImage("AttributeMap", enabled); //$NON-NLS-1$

		else if (config instanceof AttributeMapItem)
			return getImage("AttributeMap", enabled); //$NON-NLS-1$

		else if (config instanceof FunctionConfig) {
			FunctionConfig fc = (FunctionConfig) config;
			return getImage("FC", enabled, FunctionConfig.PASSIVE_STATE.equals(fc.getState())); //$NON-NLS-1$
		}

		else if (config instanceof ConnectorConfig) {
			ConnectorConfig cc = (ConnectorConfig) config;
			String mode = ConfigUtils.getStdMode(cc);
			if (mode.equals(ConnectorConfig.SCRIPT_MODE))
				return getImage("Script", enabled); //$NON-NLS-1$

			return getImage("Connector_" + mode, enabled, ConnectorConfig.PASSIVE_STATE.equals(cc.getState()));

		} else if (config instanceof RawConnectorConfig) {
			return getImage("Connector", enabled); //$NON-NLS-1$

		} else if (config instanceof HooksConfig) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);

		} else if (config instanceof HookConfig) {
			return getImage("Script", enabled); //$NON-NLS-1$

		} else if (config instanceof AttributeMapConfig) {
			return getImage("AttributeMap", true); //$NON-NLS-1$

		} else if (config instanceof SchemaConfig) {
			return getImage("Schema", true); //$NON-NLS-1$

		} else if (config instanceof ParserConfig)
			return getImage("Parser", enabled); //$NON-NLS-1$

		else if (config instanceof LoopConfig)
			return getImage("Connector_Loop", enabled); //$NON-NLS-1$

		else if (config instanceof BranchingConfig)
			return getImage("Branch", enabled); //$NON-NLS-1$
			// return getImage("Branch" + ((BranchingConfig)
			// config).getBranchType(), enabled); //$NON-NLS-1$

		else if (config instanceof SequenceConfig)
			return getImage("Settings", true); //$NON-NLS-1$

		else if (config instanceof ContainerConfig)
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);

		else if (config instanceof LinkCriteriaConfig)
			return getImage("LinkCriteria", enabled); //$NON-NLS-1$

		else if (config instanceof SchemaItemConfig)
			return getImage("Empty"); //$NON-NLS-1$

		else if (config instanceof AttributeMapItem)
			return getImage("Script", enabled); //$NON-NLS-1$

		Image i = null;
		if (config.getShortName() != null)
			i = getImage(config.getShortName());
		if (i != null)
			return i;

		if (config instanceof MetamergeFolder || config instanceof LibraryConfig || config instanceof TombstonesConfig
				|| config instanceof InstanceConfig || config instanceof LogConfig || config instanceof SolutionInterface)
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);

		return null;
	}

	/**
	 * This method checks if the config is disabled or if one of the parents is
	 * disabled.
	 * 
	 * @param config
	 * @return false if this config is disabled or any of its parents are
	 *         disabled
	 */
	private static boolean isConfigEnabled(BaseConfiguration config) {
		if (config == null)
			return false;

		// A Parser is not disabled
		if (config instanceof ParserConfig)
			return true;

		// -- Explicitly disabled
		if (!config.getEnabled())
			return false;

		// -- Check if parent container is disabled
		BaseConfiguration parent = config.getParent();
		while (parent != null) {
			if (parent instanceof ConnectorConfig) {
				if (((ConnectorConfig)parent).getState().equals(ConnectorConfig.DISABLED_STATE))
					return false;				
			} else if (!parent.getEnabled() && !"DataFlowContainer".equals(parent.getShortName())
					&& !"EntryFeedContainer".equals(parent.getShortName()) && !(parent instanceof AttributeMapConfig)
					&& !(parent instanceof HooksConfig) && !(parent instanceof AssemblyLineConfig)) {
				return false;
			}
			parent = parent.getParent();
		}

		return true;
	}

	/**
	 * Returns the identifier (extension id) for the editor that can edit a
	 * specific configuration object.
	 * 
	 * @param configuration
	 * @return the identifier for the editor
	 * @throws Exception
	 */
	public static String getEditorFor(Object configuration) throws Exception {
		Object config = configuration;
		if (config instanceof TDIConfigurationFile)
			config = ((TDIConfigurationFile) config).getDefaultConfigObject();
		if (config instanceof AssemblyLineConfig)
			return "com.ibm.tdi.eclipse.editors.AssemblyLineEditor2"; //$NON-NLS-1$
		else if (config instanceof ALMappingConfig)
			return "com.ibm.tdi.eclipse.editors.AttributeMapEditor"; //$NON-NLS-1$
		else if (config instanceof FunctionConfig)
			return "com.ibm.tdi.eclipse.editors.FunctionEditor"; //$NON-NLS-1$
		else if (config instanceof ConnectorConfig)
			return "com.ibm.tdi.editors.ConnectorEditor";
		else if (config instanceof ParserConfig)
			return "com.ibm.tie.editors.ParserEditor"; //$NON-NLS-1$
		else if (config instanceof ScriptConfig)
			return "com.ibm.tie.editors.ScriptEditor"; //$NON-NLS-1$
		else
			return null;

	}

	/**
	 * Returns the preference store for the TDI plugin
	 * 
	 * @return
	 */
	public static IPreferenceStore getPrefs() {
		return Activator.getDefault().getPreferenceStore();
	}

	/**
	 * Returns the IDILoader we use to load resources from a TDI server
	 * installation
	 * 
	 * @return the IDILoader we use.
	 */
	public static IDILoader getLoader() {
		return IDILoader.getInstance();
	}

	/**
	 * This method returns the URL to the Intro directory where the intro files
	 * reside. If the directory doesn't exist we create it (in the plugin's
	 * state location) and copies the intro files from the bundle. We have to
	 * copy it to the file system since browsers don't understand the
	 * "bundleresource" protocol.
	 * 
	 * @return URL path to intro directory
	 * @throws Exception
	 */
	public static String getIntroURL() throws Exception {
		Activator def = getDefault();
		File intro = new File(getDefault().getStateLocation().toPortableString() + "/intro"); //$NON-NLS-1$
		if (!intro.exists()) {
			if (!intro.mkdirs())
				throw new Exception(Messages.getMessage("ServerUtils.cannot.create.directory", intro.getAbsolutePath())); //$NON-NLS-1$
			File images = new File(intro, "images"); //$NON-NLS-1$
			if (!images.mkdirs())
				throw new Exception(Messages.getMessage("ServerUtils.cannot.create.directory", images.getAbsolutePath())); //$NON-NLS-1$
			String[] paths = new String[] { "index.htm", "style.css", "images/bg-main-repeat.gif", "images/btmcurve-1.gif", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					"images/task_icon.gif", "images/task_icon2.gif", "images/task_plain.gif", "images/topcurve-1.gif", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					"images/train_icon.gif", "images/train_icon1.gif", "images/train_plain.gif" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			for (String str : paths) {
				File out = new File(intro, str);
				FileOutputStream fos = new FileOutputStream(out);
				InputStream inp = def.getResource("/intro/" + str); //$NON-NLS-1$
				byte[] buffer = new byte[2048];
				int ch = 0;
				while (ch != -1) {
					ch = inp.read(buffer);
					if (ch > 0)
						fos.write(buffer, 0, ch);

				}
				fos.close();
				inp.close();
			}
		}

		intro = new File(intro, "index.htm"); //$NON-NLS-1$
		return new URL("file", "", intro.getAbsolutePath()).toExternalForm(); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Reads the stash file from the installation path and initializes
	 * CryptoUtils with the password(s)
	 * 
	 */
	private void initializeSecurity() {

		// setup the JVM's cryptographic stack for FIPS mode, if needed
		if (Boolean.getBoolean("com.ibm.di.server.fipsmode.on") && !FIPSCompliantMode.isFIPSenabled()) {
			try {
				FIPSCompliantMode.initializeFIPSMode();
			} catch (Exception e) {
				EclipseAppender.logerror(e.toString(), e);
			}
		}

		// read keystore passwords
		Vector<String> stashFilePasswords = null;
		try {
			// Try reading from solution dir, fall back on install dir.
			File f = new File(StashFile.STASH_FILE_NAME);
			if (!f.exists())
				f = new File(getInstallPath(), StashFile.STASH_FILE_NAME);
			if (f.exists()) {
				stashFilePasswords = StashFile.readPasswords(f.getAbsolutePath());
			} else {
				// Let StashFile generate the Exception.
				stashFilePasswords = StashFile.readPasswords();
			}
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e);
			return;
		}

		if (stashFilePasswords == null || stashFilePasswords.size() == 0) {
			return;
		}

		String keyStorePassword = stashFilePasswords.get(0);
		String keyPassword = null;
		if (stashFilePasswords.size() > 1) {
			keyPassword = stashFilePasswords.get(1);
		} else {
			keyPassword = keyStorePassword;
		}

		// safely distribute passwords to only those components that need them;
		// it is responsibility of each component that gets passwrods to protect
		// them from public access
		try {
			com.ibm.di.api.security.CryptoUtils.init(keyStorePassword, keyPassword);
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e);
		}
	}

	/**
	 * Adds packages from each TDI server's install and work directory. These
	 * are not picked up by IDILoader so we add them manually here.
	 * 
	 * @throws Exception
	 */
	private void addPackages() throws Exception {
		IProject project = Utils.getTDIServersProject(true);
		ArrayList<String> list = new ArrayList<String>();
		for (IResource res : project.members()) {
			if ("tdiserver".equals(res.getFileExtension())) { //$NON-NLS-1$
				try {
					RestServerAPI api = RestServerAPI.createInstance((IFile) res);
					String install = api.getInstall();
					if (install != null && install.length() > 0) {
						addPackageFiles(install + File.separator + "packages", list);
					}
					String wkdir = api.getWorkdir();
					if (wkdir != null && wkdir.length() > 0) {
						addPackageFiles(wkdir + File.separator + "packages", list);
					}
				} catch (Exception e) {
					SystemFunctions.doNothing();
				}
			}
		}
		MetamergeConfigFactory.addPackages(list);
	}

	private void addPackageFiles(String dir, ArrayList<String> list) {
		for (String file : new File(dir).list()) {
			if (file.endsWith(".xml")) //$NON-NLS-1$
				list.add(dir + File.separator + file);
		}
	}

	private void createRSInterfaceProxy() {
		SystemFunctions.setServer(new RSInterface() {

			AssemblyLineConsole console = new AssemblyLineConsole("main.logmsg");
			IWorkbenchWindow lastWindow;

			public void dump(Object o) {
				EclipseAppender.loginfo("" + o);
			}

			public void dumpEntry(Entry e) {
				if (e != null)
					logmsg(e.toDeltaString());
			}

			public AttributeMapConfig getAttributeMap(String name) {
				try {
					return getConnector(name).getAttributeMap();
				} catch (Exception err) {
					return null;
				}
			}

			public String getConfigPath() {
				return null;
			}

			public Object getConfiguration(String name) {
				try {
					return getMetamergeConfig().lookup(name == null ? "" : name);
				} catch (Exception err) {
					return null;
				}
			}

			public ConnectorConfig getConnector(String name) {
				try {
					return getMetamergeConfig().getConnector(name);
				} catch (Exception err) {
					SystemFunctions.doNothing();
				}

				try {
					return system.getConnector(name);
				} catch (Exception err) {
					return null;
				}
			}

			public FunctionConfig getFunction(String name) throws Exception {
				try {
					return (FunctionConfig) getMetamergeConfig().getFunction(name);
				} catch (Exception trySystem) {
					return (FunctionConfig) system.getFunction(name);
				}
			}

			public LibraryConfig getLibraries() {
				try {
					return (LibraryConfig) getMetamergeConfig().lookup(MetamergeConfig.DEFAULT_LIBRARY_FOLDER);
				} catch (Exception err) {
					return null;
				}
			}

			public Object getLibrary(String name) {
				try {
					return getMetamergeConfig().lookup("Libraries/" + name);
				} catch (Exception err) {
					return null;
				}
			}

			public MetamergeConfig getMetamergeConfig() {
				IWorkbenchWindow window = getWindow();
				if (window == null)
					return null;

				IEditorPart editor = window.getActivePage().getActiveEditor();
				if (!(editor instanceof BaseEditor))
					return null;
				IProject project = ((BaseEditor) editor).getTDIConfigProject();
				if (project == null)
					return null;
				try {
					return Utils.getProjectMC(project);
				} catch (Exception e) {
					return null;
				}
			}

			private IWorkbenchWindow getWindow() {
				// The following call only works if called from an UI thread,
				// due to some Eclipse backwards compatibility issues.
				// Try first without creating a new Thread.
				IWorkbenchWindow win = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				if (win != null)
					return win;

				// As it failed, try to use a UI Thread.
				PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
					public void run() {
						lastWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					}

				});

				return lastWindow;
			}

			public String getName() {
				return null;
			}

			public String getNullBehavior() {
				String s = System.getProperty("rsadmin.attribute.nullBehavior");
				if (s == null || s.equals("Default Behavior"))
					return "Delete";
				return s;
			}

			public String getNullBehaviorValue() {
				return System.getProperty("rsadmin.attribute.nullBehaviorValue");
			}

			public String getNullDefinition() {
				String s = System.getProperty("rsadmin.attribute.nullDefinition");
				if (s == null || s.equals("Default"))
					return "AbsentAttribute";
				return s;
			}

			public String getNullDefinitionValue() {
				return System.getProperty("rsadmin.attribute.nullDefinitionValue");
			}

			public ParserConfig getParser(String name) {
				try {
					return getMetamergeConfig().getParser(name);
				} catch (Exception err) {
					SystemFunctions.doNothing();
				}

				try {
					return system.getParser(name);
				} catch (Exception err) {
					return null;
				}
			}

			public ScriptConfig getScript(String name) {
				try {
					return getMetamergeConfig().getScript(name);
				} catch (Exception err) {
					return null;
				}
			}

			public String getSysProp(String name) {
				return System.getProperty(name);       
			}

			public AssemblyLineConfig getTask(String name) {
				try {
					return getMetamergeConfig().getAssemblyLine(name);
				} catch (Exception err) {
					return null;
				}
			}

			public void logerror(String msg) {
				logmsg(msg);
			}

			public void logmsg(String level, String msg) {
				logmsg(msg);
			}

			public void logmsg(String msg) {
				try {
					console.logmsg(msg + "\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			public void persistConfiguration() throws Exception {
			}

			public void reload() throws Exception {
			}

			public AssemblyLine restartAL(String assemblyLine, String checkpointID) throws Exception {
				return null;
			}

			public void setConfigPath(String path) {
			}

			public void setMetamergeConfig(MetamergeConfig config) {
			}

			public void shutdownServer() {
			}

			public void shutdownServer(int exitCode) {
			}

			public AssemblyLine startAL(String assemblyLine, Connector connector, Entry work) throws Exception {
				return null;
			}

			public AssemblyLine startAL(String assemblyLine, Object io) throws Exception {
				return null;
			}

			public AssemblyLine startAL(String assemblyLine) throws Exception {
				return null;
			}

			public Log getLog() {
				return null;
			}

			public ServerSocketFactory getServerSocketFactory(boolean useSSL) {
				BindAddressPolicy bindAddrPolicy = new BindAddressPolicyImpl(System.getProperties());
				return new ServerSocketFactoryEX(bindAddrPolicy, useSSL);
			}
		});
	}
	
	/**
	 * If the local IP address changes, we need to set the property java.rmi.server.hostname,
	 * otherwise RMI callbacks (e.g. logging from AssemblyLines) may not work.
	 * @param time
	 */
	private void startThreadDetectingIPChange(final long time) {

		new Thread("IPAddressChangeDetector") {
			@Override
			public void run() {
				try {
					String localAddress = InetAddress.getLocalHost().getHostAddress();
					while (true) {
						Thread.sleep(time);
						String currAddress = InetAddress.getLocalHost().getHostAddress();
						if (!currAddress.equals(localAddress)) {
							System.setProperty("java.rmi.server.hostname", currAddress);
							localAddress = currAddress;
						}
					}
				} catch (Exception e) {
					return;
				}
			}
		}.start();
	}
}
