/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.editors;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;

import javax.naming.NameNotFoundException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.FilteredResourcesSelectionDialog;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.swt.IFocusService;

import com.ibm.di.config.base.BaseConfigurationImpl;
import com.ibm.di.config.base.ContainerConfigImpl;
import com.ibm.di.config.base.InstanceConfigImpl;
import com.ibm.di.config.base.InternalSchema;
import com.ibm.di.config.base.LogConfigImpl;
import com.ibm.di.config.base.SolutionInterfaceImpl;
import com.ibm.di.config.base.TombstonesConfigImpl;
import com.ibm.di.config.eclipse.MetamergeConfigCE;
import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.InstanceConfig;
import com.ibm.di.config.interfaces.LibraryConfig;
import com.ibm.di.config.interfaces.LogConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.PropertyManager;
import com.ibm.di.config.interfaces.RawConnectorConfig;
import com.ibm.di.config.interfaces.ScriptConfig;
import com.ibm.di.config.interfaces.SolutionInterface;
import com.ibm.di.config.interfaces.TombstonesConfig;
import com.ibm.di.connector.ConnectorInterface;
import com.ibm.di.entry.Entry;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.store.StoreFactory;
import com.ibm.di.util.PropertiesFile;
import com.ibm.tdi.eclipse.ConfigUtils;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.actions.DeleteViewerItemAction;
import com.ibm.tdi.eclipse.commands.CommandHandlerProxy;
import com.ibm.tdi.eclipse.commands.CommandID;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.natures.TDINature;
import com.ibm.tdi.eclipse.navigator.LabelProvider;
import com.ibm.tdi.eclipse.server.RestServerAPI;
import com.ibm.tdi.eclipse.util.TDIToolBar;
import com.ibm.tdi.eclipse.widget.FormWidget2;
import com.ibm.tdi.eclipse.widget.JavaLibrariesWidget;
import com.ibm.tdi.eclipse.widget.LoggingWidget;
import com.ibm.tdi.eclipse.widget.SolutionInterfaceWidget;

public class ConfigSettingsEditor extends BaseEditor {

	@SuppressWarnings("unused") 
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public static final String ID = ConfigSettingsEditor.class.getName();

	private MetamergeConfigCE mc;

	private IFile file;

	private CTabFolder tabs;

	// Preserve the same order in these arrays.
	public final static String[] PROPERTY_KEYS = new String[] { 
			"com.ibm.di.store.database",  //$NON-NLS-1$
			"com.ibm.di.store.jdbc.driver", //$NON-NLS-1$
			"com.ibm.di.store.jdbc.urlprefix", //$NON-NLS-1$
			"com.ibm.di.store.jdbc.user", //$NON-NLS-1$
			"com.ibm.di.store.create.delta.systable",  //$NON-NLS-1$
			"com.ibm.di.store.create.delta.store", //$NON-NLS-1$
			"com.ibm.di.store.create.property.store", //$NON-NLS-1$
			"com.ibm.di.store.create.sandbox.store",  //$NON-NLS-1$
			"com.ibm.di.store.create.recal.conops", //$NON-NLS-1$
			"com.ibm.di.store.jdbc.password", //$NON-NLS-1$
			"user.dir" }; //$NON-NLS-1$

	private final static String[] DERBY_NETWORKED = new String[] {
			"jdbc:derby://localhost:1527/TDISysStore;create=true", //$NON-NLS-1$
			"org.apache.derby.jdbc.ClientDriver", //$NON-NLS-1$
			"jdbc:derby://localhost:1527/", //$NON-NLS-1$
			"APP", //$NON-NLS-1$
			"CREATE TABLE {0} (ID VARCHAR(VARCHAR_LENGTH) NOT NULL, SEQUENCEID int, VERSION int);ALTER TABLE {0} ADD CONSTRAINT IDI_CS_{UNIQUE} PRIMARY KEY (ID)", //$NON-NLS-1$
			"CREATE TABLE {0} (ID VARCHAR(VARCHAR_LENGTH) NOT NULL, SEQUENCEID int, ENTRY BLOB );ALTER TABLE {0} ADD CONSTRAINT IDI_DS_{UNIQUE} Primary Key (ID)", //$NON-NLS-1$
			"CREATE TABLE {0} (ID VARCHAR(VARCHAR_LENGTH) NOT NULL, ENTRY BLOB );ALTER TABLE {0} ADD CONSTRAINT IDI_PS_{UNIQUE} Primary Key (ID)", //$NON-NLS-1$
			"CREATE TABLE {0} (ID VARCHAR(VARCHAR_LENGTH) NOT NULL, ENTRY BLOB )", //$NON-NLS-1$
			"CREATE TABLE {0} (METHOD varchar(VARCHAR_LENGTH), RESULT BLOB, ERROR BLOB)", //$NON-NLS-1$
			"APP", //$NON-NLS-1$
			""}; //$NON-NLS-1$

	private final static String[] DERBY_EMBEDDED = new String[] {
			"jdbc:derby:TDISysStore;create=true", //$NON-NLS-1$
			"org.apache.derby.jdbc.EmbeddedDriver", //$NON-NLS-1$
			"jdbc:derby:/", //$NON-NLS-1$
			"APP", //$NON-NLS-1$
			"CREATE TABLE {0} (ID VARCHAR(VARCHAR_LENGTH) NOT NULL, SEQUENCEID int, VERSION int);ALTER TABLE {0} ADD CONSTRAINT IDI_CS_{UNIQUE} PRIMARY KEY (ID)", //$NON-NLS-1$
			"CREATE TABLE {0} (ID VARCHAR(VARCHAR_LENGTH) NOT NULL, SEQUENCEID int, ENTRY BLOB );ALTER TABLE {0} ADD CONSTRAINT IDI_DS_{UNIQUE} Primary Key (ID)", //$NON-NLS-1$
			"CREATE TABLE {0} (ID VARCHAR(VARCHAR_LENGTH) NOT NULL, ENTRY BLOB );ALTER TABLE {0} ADD CONSTRAINT IDI_PS_{UNIQUE} Primary Key (ID)", //$NON-NLS-1$
			"CREATE TABLE {0} (ID VARCHAR(VARCHAR_LENGTH) NOT NULL, ENTRY BLOB )", //$NON-NLS-1$
			"CREATE TABLE {0} (METHOD varchar(VARCHAR_LENGTH), RESULT BLOB, ERROR BLOB)", //$NON-NLS-1$
			"APP", //$NON-NLS-1$
			""}; //$NON-NLS-1$

	private final static String[] DB2 = new String[] {
			"jdbc:db2://localhost:50000/ididb", //$NON-NLS-1$
			"com.ibm.db2.jcc.DB2Driver", //$NON-NLS-1$
			"jdbc:db2:", //$NON-NLS-1$ 
			"db2admin", //$NON-NLS-1$
			"CREATE TABLE {0} (ID VARCHAR(VARCHAR_LENGTH) NOT NULL, SEQUENCEID int, VERSION int); " //$NON-NLS-1$
			+ "ALTER TABLE {0} ADD CONSTRAINT IDI_MYCONSTRAINT_{UNIQUE} PRIMARY KEY (ID)", //$NON-NLS-1$
			"CREATE TABLE {0} (ID VARCHAR(VARCHAR_LENGTH) NOT NULL, SEQUENCEID int, ENTRY BLOB ); " //$NON-NLS-1$
			+ "ALTER TABLE {0} ADD CONSTRAINT IDI_DS_{UNIQUE} Primary Key (ID)",  //$NON-NLS-1$
			"CREATE TABLE {0} (ID VARCHAR(VARCHAR_LENGTH) NOT NULL, ENTRY BLOB ) ;ALTER TABLE {0} ADD CONSTRAINT IDI_PS_{UNIQUE} Primary Key (ID)", //$NON-NLS-1$
			"CREATE TABLE {0} (ID VARCHAR(VARCHAR_LENGTH) NOT NULL, ENTRY BLOB )", //$NON-NLS-1$
			"CREATE TABLE {0} (METHOD VARCHAR(VARCHAR_LENGTH), RESULT BLOB, ERROR BLOB)", //$NON-NLS-1$
			"", //$NON-NLS-1$
			""}; //$NON-NLS-1$

	private final static String[] ORACLE = new String[] {
			"jdbc:oracle:thin:@localhost:1521:TDISysStore", //$NON-NLS-1$
			"oracle.jdbc.OracleDriver", //$NON-NLS-1$
			"jdbc:oracle:thin:", //$NON-NLS-1$
			"SYSTEM", //$NON-NLS-1$
			"CREATE TABLE {0} (ID VARCHAR(VARCHAR_LENGTH) NOT NULL, SEQUENCEID int, VERSION int);ALTER TABLE {0} ADD CONSTRAINT IDI_CS_{UNIQUE} PRIMARY KEY (ID)", //$NON-NLS-1$
			"CREATE TABLE {0} (ID VARCHAR(VARCHAR_LENGTH) NOT NULL, SEQUENCEID int, ENTRY BLOB );ALTER TABLE {0} ADD CONSTRAINT IDI_DS_{UNIQUE} Primary Key (ID)", //$NON-NLS-1$
			"CREATE TABLE {0} (ID VARCHAR(VARCHAR_LENGTH) NOT NULL, ENTRY BLOB );ALTER TABLE {0} ADD CONSTRAINT IDI_PS_{UNIQUE} Primary Key (ID)", //$NON-NLS-1$
			"CREATE TABLE {0} (ID VARCHAR(VARCHAR_LENGTH) NOT NULL,ENTRY BLOB )", //$NON-NLS-1$
			"CREATE TABLE {0} (METHOD varchar(VARCHAR_LENGTH), RESULT BLOB, ERROR BLOB)", //$NON-NLS-1$
			"", //$NON-NLS-1$
			""}; //$NON-NLS-1$

	private final static String[] MSSQL = new String[] {
			"jdbc:sqlserver://localhost:1433;DatabaseName=name;selectMethod=cursor;", //$NON-NLS-1$
			"com.microsoft.sqlserver.jdbc.SQLServerDriver", //$NON-NLS-1$
			"jdbc:sqlserver:", //$NON-NLS-1$
			"sa", //$NON-NLS-1$
			"CREATE TABLE {0} (ID VARCHAR(VARCHAR_LENGTH) NOT NULL, SEQUENCEID int, VERSION int);ALTER TABLE {0} ADD CONSTRAINT IDI_MYCONSTRAINT_{UNIQUE} PRIMARY KEY (ID)", //$NON-NLS-1$
			"CREATE TABLE {0} (ID VARCHAR(VARCHAR_LENGTH) NOT NULL, SEQUENCEID int, ENTRY VARBINARY(MAX) );ALTER TABLE {0} ADD CONSTRAINT IDI_DS_{UNIQUE} Primary Key (ID)", //$NON-NLS-1$
			"CREATE TABLE {0} (ID VARCHAR(VARCHAR_LENGTH) NOT NULL, ENTRY VARBINARY(MAX));ALTER TABLE {0} ADD CONSTRAINT IDI_PS_{UNIQUE} Primary Key (ID)", //$NON-NLS-1$
			"CREATE TABLE {0} (ID VARCHAR(VARCHAR_LENGTH) NOT NULL, ENTRY VARBINARY(MAX))", //$NON-NLS-1$
			"CREATE TABLE {0} (METHOD VARCHAR(VARCHAR_LENGTH), RESULT VARBINARY(MAX), ERROR VARBINARY(MAX))", //$NON-NLS-1$
			"", //$NON-NLS-1$
			"" }; //$NON-NLS-1$

	private final static String[] SOLID_DB = new String[] { "jdbc:solid://localhost:1964", //$NON-NLS-1$
			"solid.jdbc.SolidDriver", //$NON-NLS-1$
			"jdbc:solid:", //$NON-NLS-1$
			"dba", //$NON-NLS-1$
			"CREATE TABLE {0} (ID VARCHAR(VARCHAR_LENGTH) PRIMARY KEY NOT NULL, SEQUENCEID int, VERSION int)", //$NON-NLS-1$
			"CREATE TABLE {0} (ID VARCHAR(VARCHAR_LENGTH) PRIMARY KEY NOT NULL, SEQUENCEID int, ENTRY BLOB)", //$NON-NLS-1$
			"CREATE TABLE {0} (ID VARCHAR(VARCHAR_LENGTH) PRIMARY KEY NOT NULL, ENTRY BLOB)", //$NON-NLS-1$
			"CREATE TABLE {0} (ID VARCHAR(VARCHAR_LENGTH) NOT NULL, ENTRY BLOB)", //$NON-NLS-1$
			"CREATE TABLE {0} (METHOD VARCHAR(VARCHAR_LENGTH), RESULT BLOB, ERROR BLOB)", //$NON-NLS-1$
			"", //$NON-NLS-1$
			"" }; //$NON-NLS-1$

	final static String DATABASE = PROPERTY_KEYS[0];
	final static String DRIVER = PROPERTY_KEYS[1];
	final static String USER = PROPERTY_KEYS[3];
	final static String PASSWORD = PROPERTY_KEYS[9];
	final static String CWD = PROPERTY_KEYS[10];
	
	private final static String DEFAULT_DERBY_PORT = "1527";
	
	private Hashtable<String, String[]> templates = new Hashtable<String, String[]>();

	private FormWidget2 sysstoreWidget;

	private TDIToolBar sysstoreBar;
	
	private FormToolkit toolkit;

	private ListViewer autostartList;

	private BaseConfiguration config;

	private Action uploadAction;

	private Action downloadAction;

	private Job job;

	private JobChangeAdapter jobListener;

	private Action startDerby;
	
	private Action stopDerby;
	
	private boolean isDownLoading = false;

	private Composite parent;
	
	private String myTitleToolTip;
	
	public ConfigSettingsEditor() {
		templates.put("Derby Embedded", DERBY_EMBEDDED); //$NON-NLS-1$
		templates.put("Derby Networked", DERBY_NETWORKED); //$NON-NLS-1$
		templates.put("DB2", DB2); //$NON-NLS-1$
		templates.put("Oracle", ORACLE); //$NON-NLS-1$
		templates.put("MS SQL Server 2005+", MSSQL); //$NON-NLS-1$
		templates.put("Solid DB", SOLID_DB); //$NON-NLS-1$
	}

	@Override
	public void createPartControl(Composite parent) {
		this.parent = parent;
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayout(new FillLayout());

		toolkit = new FormToolkit(parent.getDisplay());
		
		//
		// If we open a "sysprops" file or server document just show the system
		// store properties
		// settings.
		//
		if (getTDIConfiguration() instanceof ContainerConfig || getTDIConfiguration() instanceof ScriptConfig) {
			try {
				if (getTDIConfiguration() instanceof ContainerConfig) {
					config = ((ContainerConfig) getTDIConfiguration()).getConfig(0);
					createSysStoreWidget(c, config);
					addLoadSaveButtons();
					addSysStoreTemplates();
				} else {
					config = new BaseConfigurationImpl();
					config.init();
					createRemoteSysPropsWidget(c, config);
					addLoadSaveButtons();
					addSysStoreTemplates();
					downloadAction.run();
				}

			} catch (Exception e) {
				EclipseAppender.logerror(e.toString(), e, getSite().getShell());
			}
			return;
		}

		//
		// Solution settings file
		//
		tabs = new CTabFolder(c, SWT.TOP);

		CTabItem item;

		// -- Logging
		item = new CTabItem(tabs, SWT.LEFT);
		item.setText(Messages.getString("miadmin.foldernames.Logging")); //$NON-NLS-1$
		try {
			LoggingWidget jl = new LoggingWidget(tabs, SWT.NONE, getLogFolder());
			item.setControl(jl);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// -- Solution Interface
		item = new CTabItem(tabs, SWT.LEFT);
		item.setText(Messages.getString("miadmin.foldernames.SolutionInterface")); //$NON-NLS-1$
		try {
			item.setControl(new SolutionInterfaceWidget(tabs, getSolutionInterface()));
		} catch (Exception e) {
			e.printStackTrace();
		}

		// -- Auto start
		item = new CTabItem(tabs, SWT.LEFT);
		item.setText(Messages.getString("miadmin.foldernames.AutoStart")); //$NON-NLS-1$
		try {
			item.setControl(getAutostartUI(tabs));
		} catch (Exception e) {
			e.printStackTrace();
		}

		// -- TombStones
		item = new CTabItem(tabs, SWT.LEFT);
		item.setText(Messages.getString("miadmin.foldernames.Tombstones")); //$NON-NLS-1$
		mc.addListener(this);
		try {
			FormWidget2 fw = new FormWidget2(tabs, SWT.NONE, getTombStones(), "com.ibm.di.config.base.TombstonesConfigImpl"); //$NON-NLS-1$
			fw.getFormToolkit().decorateFormHeading(fw.getForm());
			TDIToolBar bar = new TDIToolBar(fw.getForm());
			bar.setText(Messages.getString("miadmin.foldernames.Tombstones")); //$NON-NLS-1$
			bar.addHelpButton("TOMBSTONES"); //$NON-NLS-1$
			item.setControl(fw);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// -- Java Libraries
		item = new CTabItem(tabs, SWT.LEFT);
		item.setText(Messages.getString("miadmin.foldernames.JavaLibraries")); //$NON-NLS-1$
		try {
			JavaLibrariesWidget jl = new JavaLibrariesWidget(tabs, SWT.NONE, getJavaScriptLibraries());
			item.setControl(jl);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// -- Config Instance System Store
		item = new CTabItem(tabs, SWT.LEFT);
		item.setText(Messages.getString("miadmin.foldernames.SystemStore")); //$NON-NLS-1$
		try {
			createSysStoreWidget(tabs, getSystemStoreSettings(), "CustomSystemStoreSettings"); //$NON-NLS-1$
			addLoadSaveButtons();
			addSysStoreTemplates();

			item.setControl(sysstoreWidget);
		} catch (Exception e) {
			e.printStackTrace();
		}

//		// -- Includes
//		item = new CTabItem(tabs, SWT.LEFT);
//		item.setText(Messages.getString("miadmin.foldernames.Includes")); //$NON-NLS-1$
//		try {
//			item.setControl(new IncludesWidget(tabs, SWT.NONE, getIncludes()));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
		tabs.setSelection(0);
		try {
			if(mc != null)
				mc.commitChanges(new ByteArrayOutputStream(), true);
		} catch (Exception e) {
			//Cannot happen
			SystemFunctions.doNothing();
		}
		
		setModified(false);
	}

	private void createSysStoreWidget(Composite parent, BaseConfiguration cfg) throws Exception {
		createSysStoreWidget(parent, cfg, "SystemStoreSettings"); //$NON-NLS-1$
	}
	
	private void createSysStoreWidget(Composite parent, BaseConfiguration cfg, String formName) throws Exception {
		sysstoreWidget= new FormWidget2(parent, SWT.NONE, cfg, formName, true); 
		sysstoreBar = new TDIToolBar(sysstoreWidget.getForm());
	}
	
	/**
	 * Adds the load/save template buttons to the form menu
	 */
	private void addLoadSaveButtons() {
		Action button = new Action(Messages.getString("ConfigSettingsEditor.Load")) {
			@Override
			public void run() {
				loadConfigurationFromWS();
			}
		};
		sysstoreWidget.getForm().getMenuManager().add(button);

		button = new Action(Messages.getString("ConfigSettingsEditor.Save.As")) {
			@Override
			public void run() {
				saveConfigurationToWS();
			}
		};
		sysstoreWidget.getForm().getMenuManager().add(button);

		button = new Action() {
			@Override
			public String getText() {
				return Messages.getString("ConfigSettingsEditor.TestConnection");
			}

			@Override
			public void run() {
				BaseConfiguration bc = config;
				if (bc == null) {
					try {
						bc = getSystemStoreSettings();
					} catch (Exception e) {
						EclipseAppender.logerror(e.toString(), e, getSite().getShell());
						return;
					}
				}
				setUserPasswordProperties(bc);
				String database = getDatabase(bc);

				try {
					ConnectorConfig cc = (ConnectorConfig) MetamergeConfigFactory.lookup(null, "system:/Connectors/ibmdi.JDBC"); //$NON-NLS-1$
					cc = (ConnectorConfig) cc.getClone();

					String user = bc.getStringParameter(USER);
					String password = bc.getStringParameter(PASSWORD);
					String driver = bc.getStringParameter(DRIVER);
					if(driver != null && driver.startsWith("org.apache.derby")) {
						if(user == null || user.equals(""))
							user = "APP";
						if(password == null || password.equals(""))
							password = "APP";
					}
					
					ConnectorInterface jdbc = SystemFunctions.loadConnector(cc);
					jdbc.setParam("jdbcSource", database); //$NON-NLS-1$ 
					jdbc.setParam("jdbcDriver", driver); 
					jdbc.setParam("jdbcLogin", user); 
					jdbc.setParam("jdbcPassword", password); 
					jdbc.initialize(null);
					jdbc.terminate();
					MessageDialog.openInformation(getSite().getShell(), getText(), 
						Messages.getMessage("ConfigSettingsEditor.TestConnection.OK", database));
				} catch (Exception e) {
					EclipseAppender.showError(
						Messages.getMessage("ConfigSettingsEditor.TestConnection.failed", database),
						e, getSite().getShell());
				}
			}

		};
		sysstoreBar.add(button);

		// -- Browse data action
		Action browseAction = new Action() {
			@Override
			public String getText() {
				return Messages.getString("DataBrowser.title");
			}

			@Override
			public String getToolTipText() {
				return Messages.getString("DataBrowser.tooltip");
			}

			public void run() {
				BaseConfiguration bc = config;
				if (bc == null) {
					try {
						bc = getSystemStoreSettings();
					} catch (Exception e) {
						EclipseAppender.logerror(e.toString(), e, getSite().getShell());
						return;
					}
				}
				setUserPasswordProperties(bc);
				String database = getDatabase(bc);

				try {
					ConnectorConfig cc = (ConnectorConfig) MetamergeConfigFactory.lookup(null, "system:/Connectors/ibmdi.JDBC"); //$NON-NLS-1$
					cc = (ConnectorConfig) cc.getClone();
					RawConnectorConfig ccf = cc.getConnectionConfig();
					
					String driver = getStringParameter(bc, DRIVER, null);
					boolean derby = StoreFactory.isDerbyDriver(driver);
					
					String user = getStringParameter(bc, USER, (derby ? "APP" : null));
					String pass = getStringParameter(bc, PASSWORD, (derby ? "APP" : null));
					
					ccf.setParameter("jdbcSource", database); 
					ccf.setParameter("jdbcDriver", driver); 
					ccf.setParameter("jdbcLogin", user); 
					ccf.setParameter("jdbcPassword", pass);
					ccf.setParameter("jdbcTable", "IDI_PS_DEFAULT");
					TDIConfigurationFile cfg = new TDIConfigurationFile();
					cfg.setDefaultConfigObject(cc.getShortName(), cc);
					cfg.setFile(getTDIConfigFile());
					cc.setMetamergeConfig(cfg);
					
					TDIConfigEditorInput input = new TDIConfigEditorInput(cc, ID){
						@Override
						public String getTitle() {
							return Messages.getString("miadmin.foldernames.SystemStore") + 
							"/"	+ Messages.getString("DataBrowser.title");
						}

						@Override
						public String getToolTipText() {
							StringBuilder ret = new StringBuilder();
							if (getProject() != null) {
								ret.append(getProject().getName());
								ret.append("/");
							}
							ret.append(getTitle());
							return ret.toString();
						}						
					};
					IDE.openEditor(getSite().getPage(), input, DataBrowserEditor.EDITOR_ID);
				} catch (Exception e) {
					EclipseAppender.logerror(
						Messages.getMessage("ConfigSettingsEditor.TestConnection.failed", database),
						e, getSite().getShell());
				}
			}
			
			private String getStringParameter(BaseConfiguration cfg, String string, String defval) throws Exception {
				String param = cfg.getStringParameter(string);
				if ((param == null || param.length() == 0) && defval != null)
					return defval;
				else
					return param;
			}
		};
		
		sysstoreBar.add(browseAction);
		
		startDerby = new Action() {
			@Override
			public String getText() {
				return Messages.getString("ConfigSettingsEditor.Start");
			}
			@Override
			public String getToolTipText() {
				return Messages.getString("ConfigSettingsEditor.Start.tooltip");
			}
			@Override
			public void run() {
				startDerby.setEnabled(false);
				new Thread(new Runnable() {
					public void run() {   
						startDB(true);
					}
				}).start();
			}
		};
		sysstoreBar.add(startDerby);

		stopDerby = new Action() {
			@Override
			public String getText() {
				return Messages.getString("ConfigSettingsEditor.Stop");
			}
			@Override
			public String getToolTipText() {
				return Messages.getString("ConfigSettingsEditor.Stop.tooltip");
			}
			@Override
			public void run() {
				startDB(false);
			}
		};
		sysstoreBar.add(stopDerby);

		enableDerbyButtons();
		
		sysstoreBar.addHelpButton("MANAGESYSSTORE"); //$NON-NLS-1$

		sysstoreBar.update();
	}

	/**
	 * Adds the predefined templates to the form menu
	 */
	private void addSysStoreTemplates() {
		String[] tmpl = new String[] { "Derby Embedded", "Derby Networked", "DB2", "Oracle", "MS SQL Server 2005+", "Solid DB" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		for (String str : tmpl) {
			Action a = new Action(str) {
				@Override
				public void run() {
					String[] values = templates.get(getText());
					if (values == null)
						return;
					for (int i = 0; i < values.length; i++) {
						String key = PROPERTY_KEYS[i];
						try {
							getSystemStoreSettings().setStringParameter(key, values[i]);
							if (sysstoreWidget.getControl(key) != null)
								sysstoreWidget.updateControl(key);
						} catch (Exception e) {
							EclipseAppender.logerror(e.toString(), e, getSite().getShell());
						}
					}
				}
			};
			sysstoreWidget.getForm().getMenuManager().add(a);
		}
		sysstoreWidget.getForm().getMenuManager().update(true);
	}

	/**
	 * Returns the DATABASE parameter, with substitution if needed.
	 * @param bc
	 * @return
	 */
	static String getDatabase(BaseConfiguration bc) {
		String database = bc.getStringParameter(DATABASE);
		
		// Replace the $soldir$ token if necessary
		if (database == null || ! database.contains(StoreFactory.SOLUTION_DIR))
			return database;
		
		String cwd = bc.getStringParameter(CWD);
		if (cwd == null || cwd.length() == 0)
			cwd = System.getProperty(CWD);
		return database.replace(StoreFactory.SOLUTION_DIR, cwd);	
	}

	/**
	 * Creates a widget for editing system store properties based on a server
	 * document file. If we have access to install/solution directories we
	 * read/write directly to/from those. Otherwise we read/write properties via
	 * the server api.
	 * 
	 * @param c
	 *            Parent container
	 * @param config2
	 *            The configuration object where values are stored
	 * @throws Exception
	 */
	private void createRemoteSysPropsWidget(Composite c, BaseConfiguration config2) throws Exception {

		createSysStoreWidget(c, config2);

		downloadAction = new Action() {
			@Override
			public String getText() {
				return Messages.getString("PropertiesEditor.8"); //$NON-NLS-1$
			}

			@Override
			public String getToolTipText() {
				return Messages.getString("PropertiesEditor.9"); //$NON-NLS-1$
			}

			@Override
			public void run() {
				loadConfigurationFromServer();
			}
		};
		sysstoreBar.add(downloadAction);
		new CommandHandlerProxy(getEditorSite(), downloadAction, CommandID.CNFSETTINGS_EDITOR_DOWNLOAD);

		uploadAction = new Action() {
			@Override
			public String getText() {
				return Messages.getString("PropertiesEditor.15"); //$NON-NLS-1$
			}

			@Override
			public String getToolTipText() {
				return Messages.getString("PropertiesEditor.16"); //$NON-NLS-1$
			}

			@Override
			public void run() {
				saveConfigurationToServer();
			}
		};
		new CommandHandlerProxy(getEditorSite(), uploadAction, CommandID.CNFSETTINGS_EDITOR_UPLOAD);
		
		sysstoreBar.add(uploadAction);
		sysstoreBar.update();

	}

	/**
	 * If we have a reference to the solution directory we write that file
	 * directly. Otherwise we contact the server for the update.
	 */
	protected void saveConfigurationToServer() {
		job = new Job(Messages.getString("PropertiesEditor.15")) { //$NON-NLS-1$
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					RestServerAPI api = RestServerAPI.createInstance(getTDIConfigFile());
					String sd = api.getWorkdir();
					if (sd != null && sd.length() > 0) {
						PropertiesFile propsFile = new PropertiesFile(sd + "/solution.properties", true); //$NON-NLS-1$
						for (String str : PROPERTY_KEYS) {
							if (str.equals(CWD))
								continue;
							String value = config.getStringParameter(str);
							if (value != null) {
								value = value.replaceAll("\r", "");
								value = value.replaceAll("\n", " ");
								propsFile.setProperty(str, value);
							}
						}
						propsFile.setPropertyEncrypted(PASSWORD, true);
						propsFile.store(sd + "/solution.properties", null, null); //$NON-NLS-1$
					} else {
						Entry e = new Entry();
						for (String str : PROPERTY_KEYS) {
							String value = config.getStringParameter(str);
							if (value != null) {
								value = value.replaceAll("\r", "");
								value = value.replaceAll("\n", " ");
								e.setAttribute(str, value);
							}
						}
						e.setProperty(PASSWORD, "true"); //$NON-NLS-1$
						api.setProperties(PropertyManager.STDCOLL_SOLUTION, e, null);
					}
				} catch (Exception e) {
					if(!monitor.isCanceled())
						return EclipseAppender.statusException(e);
					else
						return Status.CANCEL_STATUS;
				}

				return Status.OK_STATUS;
			}
		};
		setServerButtonsEnabled(false);
		sysstoreWidget.getForm().setMessage(Messages.getString("PropertiesEditor.17")); //$NON-NLS-1$
		jobListener = new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				getSite().getShell().getDisplay().asyncExec(new Runnable() {
					public void run() {
						sysstoreWidget.getForm().setMessage(null);
						setServerButtonsEnabled(true);
						MessageDialog.openInformation(getSite().getShell(), 
								Messages.getString("PropertiesEditor.16"), Messages.getMessage("PropertiesEditor.reload", null));
					}
				});
			}
		};
		job.addJobChangeListener(jobListener);
		job.schedule();
	}

	/**
	 * If we have a reference to the install directory and solution directory we
	 * read properties from those locations. Otherwise we contact the server for
	 * a list.
	 */
	protected void loadConfigurationFromServer() {
		job = new Job(Messages.getString("PropertiesEditor.8")) { //$NON-NLS-1$
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					isDownLoading = true;
					RestServerAPI api = RestServerAPI.createInstance(getTDIConfigFile());
					String id = api.getInstall();
					String sd = api.getWorkdir();
					if (sd != null && sd.length() > 0)
						config.setStringParameter(CWD, sd);

					if (id != null && id.length() > 0) {
						File file = new File(id, "etc/global.properties"); //$NON-NLS-1$
						if (file.exists()) {
							PropertiesFile propsFile = new PropertiesFile(file.getAbsolutePath(), true);
							for (String str : PROPERTY_KEYS) {
								if (monitor.isCanceled())
									return Status.CANCEL_STATUS;
								String value = propsFile.getProperty(str);
								if (value != null)
									config.setStringParameter(str, value);
							}
						}
					} else {
						Entry e = api.getProperties(PropertyManager.STDCOLL_GLOBAL, null, Arrays.asList(PROPERTY_KEYS));
						for (String str : PROPERTY_KEYS) {
							if (monitor.isCanceled())
								return Status.CANCEL_STATUS;
							String value = e.getString(str);
							if (value != null)
								config.setStringParameter(str, value);
						}
					}

					if (sd != null && sd.length() > 0) {
						File file = new File(sd, "solution.properties"); //$NON-NLS-1$
						if (file.exists()) {
							PropertiesFile propsFile = new PropertiesFile(file.getAbsolutePath(), true);
							for (String str : PROPERTY_KEYS) {
								if (monitor.isCanceled())
									return Status.CANCEL_STATUS;
								String value = propsFile.getProperty(str);
								if (value != null)
									config.setStringParameter(str, value);
							}
						}
					} else {
						Entry e = api.getProperties(PropertyManager.STDCOLL_SOLUTION, null, Arrays.asList(PROPERTY_KEYS));
						for (String str : PROPERTY_KEYS) {
							if (monitor.isCanceled())
								return Status.CANCEL_STATUS;
							String value = e.getString(str);
							if (value != null)
								config.setStringParameter(str, value);
						}
					}
				} catch (Exception e) {
					if (!monitor.isCanceled())
						return EclipseAppender.statusException(e);
				} finally {
					isDownLoading = false;
					enableDerbyButtons();
				}

				return Status.OK_STATUS;
			}
		};
		
		setServerButtonsEnabled(false);
		sysstoreWidget.getForm().setMessage(Messages.getString("PropertiesEditor.10")); //$NON-NLS-1$
		job.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				getSite().getShell().getDisplay().asyncExec(new Runnable() {
					public void run() {
						sysstoreWidget.getForm().setMessage(null);
						setServerButtonsEnabled(true);
						for (String str : PROPERTY_KEYS) {
							String value = config.getStringParameter(str);
							if (value != null && sysstoreWidget.getControl(str) != null) {
								sysstoreWidget.updateControl(str);
							}
						}
					}
				});
			}
		});
		job.schedule();
	}

	/**
	 * enable/disable the download/upload buttons
	 * 
	 * @param enabled
	 */
	private void setServerButtonsEnabled(boolean enabled) {
		downloadAction.setEnabled(enabled);
		uploadAction.setEnabled(enabled);
		sysstoreBar.update();
	}

	/**
	 * Saves the current property set to a file in the workspace
	 */
	protected void saveConfigurationToWS() {
		SaveAsDialog sad = new SaveAsDialog(getSite().getShell());
		sad.setOriginalName(file.getProject().getName() + ".sysprops"); //$NON-NLS-1$
		if (sad.open() != Window.OK)
			return;

		try {
			IFile path = ResourcesPlugin.getWorkspace().getRoot().getFile(sad.getResult());
			TDIConfigurationFile cfg = new TDIConfigurationFile(path);
			BaseConfiguration clone = (BaseConfiguration) getSystemStoreSettings().getClone();
			ContainerConfig cc = new ContainerConfigImpl();
			cc.init();
			cfg.setDefaultConfigObject("SystemStoreProperties", cc); //$NON-NLS-1$
			cc.addConfig(clone);
			cfg.commitVersion();
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e, getSite().getShell());
		}
	}

	/**
	 * Replaces the current property set with those from a file in the workspace
	 */
	protected void loadConfigurationFromWS() {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		FilteredResourcesSelectionDialog fs = new FilteredResourcesSelectionDialog(getSite().getShell(), false, root,
				IResource.FILE);
		fs.setInitialPattern("*.sysprop"); //$NON-NLS-1$
		if (fs.open() != Window.OK)
			return;

		IResource source = (IResource) fs.getResult()[0];
		try {
			TDIConfigurationFile cfg = TDIConfigurationFile.loadFile((IFile) source);
			ContainerConfig cc = (ContainerConfig) cfg.getDefaultConfigObject();
			BaseConfiguration bc = cc.getConfig(0);
			for (String key : PROPERTY_KEYS) {
				String value = bc.getStringParameter(key);
				if (value == null)
					value = ""; //$NON-NLS-1$
				getSystemStoreSettings().setStringParameter(key, value);
				if (sysstoreWidget.getControl(key) != null) {
					sysstoreWidget.updateControl(key);
				}
			}
			getSystemStoreSettings().setBooleanParameter(InternalSchema.ENABLED, bc.getEnabled());
			sysstoreWidget.updateControl(InternalSchema.ENABLED);
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e, getSite().getShell());
		}
	}

	private TombstonesConfig getTombStones() throws Exception {
		// IDIServer - Tombstones
		TombstonesConfig ts;
		String name = MetamergeConfig.DEFAULT_SERVER_FOLDER + "/" //$NON-NLS-1$
			+ MetamergeConfig.DEFAULT_SERVER_TOMBSTONES;
		try {
			ts = (TombstonesConfig) mc.lookup(name);
		} catch (NameNotFoundException nnfe) {
			ts = new TombstonesConfigImpl();
			ts.init();
			ts.setName(name);
			ts.setMetamergeConfig(mc);
			mc.bind(name, ts);
			ts.setModified(false);
		}

		ts.addListener(this);
		return ts;
	}

	private LibraryConfig getJavaScriptLibraries() throws Exception {
		LibraryConfig lc = (LibraryConfig) mc.lookup(MetamergeConfig.DEFAULT_LIBRARY_FOLDER);
		lc.addListener(this);
		return lc;
	}

	private InstanceConfig getAutoStartFolder() throws Exception {
		// IDIServer - autostart
		InstanceConfig ic;
		String name = MetamergeConfig.DEFAULT_SERVER_FOLDER + "/"  //$NON-NLS-1$
			+ MetamergeConfig.DEFAULT_SERVER_AUTOSTART;
		try {
			ic = (InstanceConfig) mc.lookup(name);
		} catch (NameNotFoundException nnfe) {
			ic = new InstanceConfigImpl();
			ic.init();
			ic.setName(name);
			ic.setMetamergeConfig(mc);
			mc.bind(name, ic);
			ic.setModified(false);
		}
		
		ic.addListener(this);
		return ic;
	}

	private LogConfig getLogFolder() throws Exception {
		// IDIServer - logger
		LogConfig lc;
		String name = MetamergeConfig.DEFAULT_SERVER_FOLDER + "/" //$NON-NLS-1$
			+ MetamergeConfig.DEFAULT_SERVER_LOG;
		try {
			lc = (LogConfig) mc.lookup(name);
		} catch (NameNotFoundException nnfe) {
			lc = new LogConfigImpl();
			lc.setName(name);
			lc.setMetamergeConfig(mc);
			mc.bind(name, lc);
		}

		lc.addListener(this);
		return lc;
	}

	private SolutionInterface getSolutionInterface() throws Exception {
		// IDIServer - logger
		SolutionInterface sol;
		String name = MetamergeConfig.DEFAULT_SERVER_FOLDER + "/" //$NON-NLS-1$
			+ MetamergeConfig.DEFAULT_SOLUTION_INTERFACE;
		try {
			sol = (SolutionInterface) mc.lookup(name);
		} catch (NameNotFoundException nnfe) {
			sol = new SolutionInterfaceImpl();
			sol.setName(name);
			sol.setMetamergeConfig(mc);
			mc.bind(name, sol);
		}

		sol.addListener(this);
		return sol;
	}

	private BaseConfiguration getSystemStoreSettings() throws Exception {
		// System store

		if (config != null) {
			config.addListener(this);
			return config;
		}

		BaseConfiguration bc = null;
		String name = MetamergeConfig.DEFAULT_SERVER_FOLDER + "/SystemStore"; //$NON-NLS-1$
		try {
			ContainerConfig cc = (ContainerConfig) mc.lookup(name); 
			bc = cc.getConfig(0);
		} catch (NameNotFoundException nnfe) {
			ContainerConfig cc = new ContainerConfigImpl();
			cc.setName(name);
			cc.init();
			cc.setMetamergeConfig(mc);
			bc = new BaseConfigurationImpl();
			bc.setName("Default"); //$NON-NLS-1$
			cc.addConfig(bc);
			mc.bind(name, cc); 
		}

		bc.addListener(this);
		return bc;
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.setSite(site);
		super.setInput(input);
		try {
			if (input instanceof IFileEditorInput) {
				file = ((IFileEditorInput) input).getFile();
				if (TDINature.SOLUTION_SETTINGS_FILE.equals(file.getName())) { //$NON-NLS-1$
					mc = new MetamergeConfigCE(file);
					mc.addListener(this);
					String translated = Messages.getString("miadmin.filename.SolutionSettings");
					if(translated == null)
						translated = file.getName();
					setPartName(translated + " (" + getProject().getName() + ")");
					myTitleToolTip = getProject().getName() + "/" + translated;
					setTitleToolTip(myTitleToolTip);
				} else {
					super.init(site, input);
				}
			}
		} catch (Exception e) {
			throw new PartInitException(e.toString(), e);
		}
	}

	@Override
	public boolean isDirty() {
		if (mc != null) {
			return mc.getModified();
		} else {
			return super.isDirty();
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		if (mc == null) {
			super.doSave(monitor);
		} else {
			try {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				mc.commitChanges(bos, true);
				if (file.exists())
					file.setContents(new ByteArrayInputStream(bos.toByteArray()), IResource.FORCE, monitor);
				else
					file.create(new ByteArrayInputStream(bos.toByteArray()), IResource.FORCE, monitor);

				firePropertyChange(PROP_DIRTY);
			} catch (Exception e) {
				EclipseAppender.logerror(e.toString(), e, getSite().getShell());
			}
		}
	}

	/**
	 * Creates the auto-start widget.
	 * 
	 * @param parent
	 *            Parent composite
	 * @return Form control
	 * @throws Exception
	 */
	private Control getAutostartUI(Composite parent) throws Exception {

		Form form = toolkit.createForm(parent);
		toolkit.decorateFormHeading(form);

		TDIToolBar bar = new TDIToolBar(form);
		bar.setText(Messages.getString("config.instance.autostart")); //$NON-NLS-1$
		form.getBody().setLayout(new FillLayout());

		autostartList = new ListViewer(form.getBody(), SWT.SINGLE);
		autostartList.setContentProvider(new AutoStartContentProvider());
		autostartList.setLabelProvider(new AutoStartLabelProvider());
		autostartList.setInput(getAutoStartFolder().getStartupItems());

		Action insertAction = new Action() {
			@Override
			public String getText() {
				return Messages.getString("general.insert.label"); //$NON-NLS-1$
			}

			@Override
			public void run() {

				try {

					ElementListSelectionDialog dialog = new ElementListSelectionDialog(getSite().getShell(), new LabelProvider());
					Vector<String> v = ConfigUtils.getAvailableSystemComponents(Utils.getProjectMC(getProject()), MetamergeConfig.ASSEMBLYLINE_FOLDER);
					dialog.setElements(v.toArray());
					dialog.setMultipleSelection(true);
					dialog.setTitle(Messages.getString("SelectComponentPage.1"));
					dialog.setMessage(Messages.getString("ALFC.Choose.AL"));
					if (dialog.open() != Window.OK)
						return;
					
					ContainerConfig items = getAutoStartFolder().getStartupItems();
					for (Object res : dialog.getResult()) {
						String name = (String) res;
						if (containsConfig(items, name))
							continue;

						BaseConfiguration add = new com.ibm.di.config.base.BaseConfigurationImpl();
						add.setName(name);
						if (!name.startsWith(MetamergeConfig.DEFAULT_ASSEMBLYLINE_FOLDER) && 
								name.indexOf(':') < 0)
							name = MetamergeConfig.DEFAULT_ASSEMBLYLINE_FOLDER + "/" + name;
						add.setParameter(InstanceConfig.AUTOSTART_NAME, name);
						items.addConfig(add);
					}
					autostartList.refresh();

				} catch (Exception e) {
					EclipseAppender.logerror(e.toString(), e, getSite().getShell());
				}
			}
			
			private boolean containsConfig(ContainerConfig items, String name) {
				for ( BaseConfiguration bc : items.getConfigurations(null)) {
					String s = bc.getStringParameter(InstanceConfig.AUTOSTART_NAME);
					if (s == null)
						continue;
					if (s.startsWith(MetamergeConfig.DEFAULT_ASSEMBLYLINE_FOLDER))
						s = s.substring(MetamergeConfig.DEFAULT_ASSEMBLYLINE_FOLDER.length() + 1);
					if (s.equals(name))
						return true;
				}
				return false;
			}

		};
		bar.add(insertAction);

		Action deleteAction = new DeleteViewerItemAction() {
			@Override
			public void run() {
				if (autostartList == null || autostartList.getSelection() == null || autostartList.getSelection().isEmpty()) {
					return;
				}

				if (!MessageDialog.openConfirm(autostartList.getControl().getShell(), Messages.getString("miadmin.foldernames.AutoStart"),
						Messages.getString("CutConfigAction.Delete.optimized")))
					return;

				IStructuredSelection sel = (IStructuredSelection) autostartList.getSelection();
				for (Object obj : sel.toArray()) {
					try {
						getAutoStartFolder().getStartupItems().removeConfig((BaseConfiguration) obj);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				autostartList.refresh();
			}
		};
		bar.add(deleteAction);
		bar.addHelpButton("CFGINST_AUTOSTART"); //$NON-NLS-1$

		// register the table in the focus service and associate Handler with it
		IFocusService fs = (IFocusService) this.getSite().getService(IFocusService.class);
		if (fs != null) {
			fs.addFocusTracker(autostartList.getControl(), "com.ibm.tdi.configsettings.list.delete");
			autostartList.getControl().setData("com.ibm.tdi.action", deleteAction); //$NON-NLS-1$
		}

		return form;
	}

	@Override
	public void setModified(boolean modified) {
		if (mc != null) {
			mc.setModified(modified);
			firePropertyChange(PROP_DIRTY);
		} else {
			super.setModified(modified);
		}
	}

	@Override
	public void configurationChanged(MetamergeConfigChange changeEvent) {
		if (isDownLoading)
			return;
		if (DRIVER.equals(changeEvent.getKey())) {
			enableDerbyButtons();
		}
		firePropertyChange(PROP_DIRTY);
	}

	/**
	 * Label provider for auto-start container
	 */
	private static class AutoStartContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return ((ContainerConfig) inputElement).getConfigurations(null).toArray();
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	/**
	 * Content provider for auto-start container
	 */
	private static class AutoStartLabelProvider implements ITableLabelProvider, ILabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			String s = ((BaseConfiguration) element).getStringParameter(InstanceConfig.AUTOSTART_NAME);
			if (s.startsWith(MetamergeConfig.DEFAULT_ASSEMBLYLINE_FOLDER))
				return s.substring(MetamergeConfig.DEFAULT_ASSEMBLYLINE_FOLDER.length() + 1);
			else
				return s;
		}

		public void addListener(ILabelProviderListener listener) {
		}

		public void dispose() {
		}

		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
		}

		public Image getImage(Object element) {
			return null;
		}

		public String getText(Object element) {
			return getColumnText(element, 0);
		}
	};
	
	@Override
	public void setFocus() {
		if(tabs != null)
			tabs.setFocus();
		else if(sysstoreWidget != null)
			sysstoreWidget.setFocus();
		else if(parent != null)
			parent.setFocus();
		else
			super.setFocus();
	}

	@Override
	public void dispose() {			
		if(job != null) {
			if(jobListener != null)
				job.removeJobChangeListener(jobListener);
			job.cancel();
			job = null;
		}
  		super.dispose();
	}

	private void enableDerbyButtons() {
		try {
			boolean enable = DERBY_NETWORKED[1].equals(
					getSystemStoreSettings().getStringParameter(DRIVER));
			startDerby.setEnabled(enable);
			stopDerby.setEnabled(enable);
		} catch (Exception e) {
			EclipseAppender.logerror(e.getMessage(), e);
		}
	}
	
	/**
	 * Attempt to start a networked derby server.
	 * if start if false, stop it instead.
	 */
	private void startDB(boolean start) {
		try { 
			// Try to parse the host and port from the database almost URL -like String.
			String urlPrefix = getSystemStoreSettings().getStringParameter(PROPERTY_KEYS[2]);
			int i = urlPrefix.indexOf("//");
			if ( i == -1 )
				throw new Exception(Messages.getMessage("ConfigSettingsEditor.parse.urlprefix", urlPrefix));
			String host = urlPrefix.substring(i+2);
			if (host.indexOf("/")> 0)
				host = host.substring(0, host.indexOf("/"));
			String port = DEFAULT_DERBY_PORT;
			i = host.indexOf(":");
			if ( i > 0) {
				port = host.substring(i+1);
				host = host.substring(0, i);
			}
				
			setUserPasswordProperties(getSystemStoreSettings());
			if (start) {
				StoreFactory.startDerbyServer(host, port, true);
			} else {
				StoreFactory.stopDerbyServer(host, Integer.valueOf(port));
			}
		} catch (final Exception e) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {   
					EclipseAppender.logerror(e.getMessage(), e, getSite().getShell());
				}
			});
		}
		if (start) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {   
					startDerby.setEnabled(true);
				}
			});
		}
	}
	
	private void setUserPasswordProperties(BaseConfiguration bc) {
		String user = bc.getStringParameter(USER);
		if(user == null || user.equals(""))
			user = "APP";
		String password = bc.getStringParameter(PASSWORD);
		if(password == null || password.equals(""))
			password = "APP";
		System.setProperty(USER, user);
		System.setProperty(PASSWORD, password);	
		StoreFactory.setDerbyUserPassword(user, password);
	}


	private IProject getProject() {
		return file.getProject();
	}
	
	public IFile getTDIConfigFile() {
		IFile f = super.getTDIConfigFile();
		if (f != null)
			return f;
		return file;
	}

	@Override
	public String getTitleToolTip() {
		if (myTitleToolTip != null)
			return myTitleToolTip;
		return super.getTitleToolTip();
	}

	
}
