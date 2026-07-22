/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.wizards;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NameNotFoundException;

import org.apache.xpath.XPathAPI;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.MetamergeFolder;
import com.ibm.di.config.xml.Factories;
import com.ibm.di.config.xml.MetamergeConfigXML;
import com.ibm.di.function.SystemFunctions;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.natures.TDINature;
import com.ibm.tdi.eclipse.providers.ConfigLabelProvider;
import com.ibm.tdi.eclipse.providers.MetamergeFolderContentProvider;
import com.ibm.tdi.eclipse.server.RMIServerAPI;
import com.ibm.tdi.eclipse.util.ImportUtils;

public class ImportConfigWizard extends Wizard implements INewWizard {
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private IStructuredSelection selection;
	private Object[] includes = null;
	private MetamergeConfig remoteXML;
	private String project = null;
	private IProject target;
	private MetamergeConfigXML sourceXML;
	private boolean overwrite = false;
	private String initialFileName;
	private String importedFilename;

	private ConfigFileBrowser page;

	private boolean initialLinkFile;

	@Override
	public void addPages() {
		page = new ConfigFileBrowser("SelectFile");
		addPage(page); //$NON-NLS-1$
	}

	public ImportConfigWizard() {
		super();
	}

	/**
	 * Returns the filename that was imported
	 * @return
	 */
	public String getImportedFileName() {
		return importedFilename;
	}

	/**
	 * Sets the name of the file that was imported
	 * 
	 * @param importedFilename
	 */
	protected void setImportedFileName(String importedFilename) {
		this.importedFilename = importedFilename;
	}

	
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
		setWindowTitle(Messages.getString("ImportConfigWizard.1")); //$NON-NLS-1$
	}
	
	/**
	 * Returns the project that was the target for the import
	 * 
	 * @return
	 */
	public IProject getProject() {
		return target;
	}
	
	/**
	 * Returns true if a new project was created after importing the config
	 * 
	 * @return
	 */
	public boolean isProjectNew() {
		return overwrite;
	}
	
	/**
	 * When set the wizard will use File import and the provided file as default
	 * 
	 * @param path
	 */
	public void setInitialFilename(String path) {
		initialFileName = path;
	}

	/**
	 * Returns the initial filename for import
	 * @return
	 */
	public String getInitialFilename() {
		return initialFileName;
	}
	
	@Override
	public boolean performFinish() {

		target = null;

		if (project == null || project.equals("")) {
			NewProject wiz = new NewProject();
			WizardDialog dlg = new WizardDialog(getShell(), wiz);
			wiz.init(null, null);
			if (dlg.open() == Window.CANCEL)
				return false;
			target = wiz.getProject();
			// overwrite any auto-created files (like the default property
			// store)
			overwrite = true;
		} else {
			target = ResourcesPlugin.getWorkspace().getRoot().getProject(project);
			overwrite = page.getOverwrite();
		}

		String extPath1 = null;
		try {
			extPath1 = target.getPersistentProperty(TDINature.TDI_EXTERNAL_CONFIG);

		} catch (CoreException e1) {

			EclipseAppender.logerror(e1.toString(), e1);
		}
		final String extPath = extPath1;

		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				ImportUtils imp = new ImportUtils(target, sourceXML);
				try {

					target.setPersistentProperty(TDINature.TDI_EXTERNAL_CONFIG, "");

					imp.importConfig(resolveDependencies(), overwrite);
				} catch (Exception e) {
					EclipseAppender.logerror(e.toString(), e);
				} finally {
					// -- reset ext path
					try {
						target.setPersistentProperty(TDINature.TDI_EXTERNAL_CONFIG, extPath);
					} catch (CoreException e) {
						EclipseAppender.logerror(e.toString(), e);
					}
				}

			}
		};
		try {
			getContainer().run(false, true, op);
			if(page.isLinked() && isProjectNew() && getImportedFileName() != null) {
				getProject().setPersistentProperty(TDINature.TDI_EXTERNAL_CONFIG, getImportedFileName());				
			}
			return true;
		} catch (Exception e) {
			MessageDialog.openError(getShell(), null, Utils.exceptionText(e));
			return false;
		}
	}

	public class ConfigFileBrowser extends WizardPage implements ICheckStateListener, ModifyListener {
		@SuppressWarnings("unused")
		private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

		private Text filepath;
		private Text password;
		private ContainerCheckedTreeViewer tree;
		private Combo projectname;

		private Button serverButton;

		private Combo targetServer;

		private Composite stack;

		private Composite server;

		private Composite file;

		private TableViewer serverConfigs;

		@SuppressWarnings("rawtypes")
		protected ArrayList serverConfigsList;

		private Button linked;
		
		private Button overWrite;
		
		private String NEW_PROJECT = Messages.getString("ImportConfigWizard.23"); 

		public ConfigFileBrowser(String pageName) {
			super(pageName);
			setTitle(Messages.getString("ImportConfigWizard.2")); //$NON-NLS-1$
			setDescription(Messages.getString("ImportConfigWizard.3")); //$NON-NLS-1$
		}
		
		public boolean isLinked() {
			return linked.getSelection();
		}

		public boolean getOverwrite() {
			return overWrite.getSelection();
		}

		public void createControl(Composite parent) {
			Composite c = new Composite(parent, SWT.NONE);
			c.setLayout(new GridLayout(3, false));

			//
			// -- Project selection
			//
			new Label(c, SWT.LEFT).setText(Messages.getString("ImportConfigWizard.22")); //$NON-NLS-1$

			projectname = new Combo(c, SWT.READ_ONLY);
			projectname.add(NEW_PROJECT); //$NON-NLS-1$
			for (IProject p : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
				try {
					if (p.isAccessible() && p.hasNature(TDINature.TDI_NATURE_ID))
						projectname.add(p.getName());
				} catch (CoreException e1) {
					EclipseAppender.logerror(e1.toString(), e1);
				}
			}
			projectname.select(0);

			projectname.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					project = projectname.getText();
					if(NEW_PROJECT.equals(project))
						project = null;
					updateComplete();
				}
			});

			//
			// -- Target server
			//
			String targetServerSel = null;

			if (selection.isEmpty()) {
				projectname.select(0);
			} else if (selection.getFirstElement() instanceof IResource) {
				if ("tdiserver".equals(((IResource) selection.getFirstElement()).getFileExtension()))
					targetServerSel = ((IResource) selection.getFirstElement()).getName();
				else
					projectname.select(projectname.indexOf(((IResource) selection.getFirstElement()).getProject().getName()));
			} else {
				projectname.select(0);
			}

			project = projectname.getText();
			if(NEW_PROJECT.equals(project))
				project = null;

			new Label(c, SWT.LEFT).setText(""); //$NON-NLS-1$

			serverButton = new Button(c, SWT.RADIO);
			serverButton.setText(Messages.getString("wizard.name.6")); //$NON-NLS-1$
			serverButton.setSelection(targetServerSel != null);
			serverButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					enableFile(false);
					setPageComplete(isPageComplete());
				}
			});

			Button fileButton = new Button(c, SWT.RADIO);
			fileButton.setText(Messages.getString("SearchPage.file")); //$NON-NLS-1$
			fileButton.setSelection(targetServerSel != null);
			fileButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					enableFile(true);
					setPageComplete(isPageComplete());
				}
			});

			new Label(c, SWT.LEFT).setText(""); //$NON-NLS-1$

			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 3;
			new Label(c, SWT.SEPARATOR|SWT.HORIZONTAL).setLayoutData(gd);
			
			//
			// -- Stack Composite to show file/server selection
			//
			stack = new Composite(c, 0);
			stack.setLayout(new StackLayout());
			gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 3;
			stack.setLayoutData(gd);

			//
			// -- Server controls
			//
			server = new Composite(stack, 0);
			server.setLayout(new GridLayout(2, false));

			new Label(server, SWT.LEFT).setText(Messages.getString("ExportRuntimeWizard.toserver"));

			targetServer = new Combo(server, SWT.DROP_DOWN | SWT.READ_ONLY);
			targetServer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			targetServer.add("");
			try {
				IProject project = Utils.getTDIServersProject(true);
				for (IResource res : project.members()) {
					if (res instanceof IFile) {
						IFile file = (IFile) res;
						if ("tdiserver".equals(file.getFileExtension())) //$NON-NLS-1$
							targetServer.add(file.getName());
					}
				}
			} catch (Exception e1) {
				EclipseAppender.logerror(e1.toString(), e1);
			}

			if (targetServerSel != null) {
				targetServer.select(targetServer.indexOf(targetServerSel));
			} else {
				targetServer.select(0);
			}

			// Table to show available configs
			serverConfigs = new TableViewer(server, SWT.BORDER | SWT.SINGLE);
			gd = new GridData(GridData.FILL_BOTH);
			gd.horizontalSpan = 2;
			serverConfigs.getTable().setLayoutData(gd);
			serverConfigs.setContentProvider(new ArrayContentProvider());
			serverConfigs.setLabelProvider(new LabelProvider());

			targetServer.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					updateServerConfigsTable();
				}
			});
			updateServerConfigsTable();

			serverConfigs.addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					IStructuredSelection sel = (IStructuredSelection) event.getSelection();
					if (sel.isEmpty()) {
						tree.setInput(null);
					} else {
						loadServerConfig(sel.getFirstElement().toString());
					}
				}
			});

			//
			// -- Import from file
			//
			file = new Composite(stack, 0);
			file.setLayout(new GridLayout(3, false));

			new Label(file, SWT.LEFT).setText(Messages.getString("ImportConfigWizard.19")); //$NON-NLS-1$
			filepath = new Text(file, SWT.BORDER);
			filepath.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
			if(getInitialFilename() != null)
				filepath.setText(getInitialFilename());
			filepath.addModifyListener(this);
			Button choose = new Button(file, SWT.PUSH);
			choose.setText("..."); //$NON-NLS-1$
			choose.setToolTipText(Messages.getString("ImportConfigWizard.27")); //$NON-NLS-1$
			choose.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					FileDialog fd = new FileDialog(getShell());
					fd.setFileName(System.getProperty("user.dir"));
					fd.setFilterExtensions(new String[] { "*.xml", "*.*" });
					String sel = fd.open();
					if (sel != null) {
						filepath.setText(sel);
						modifyText(null);
					}
				}
			});

			new Label(file, SWT.LEFT).setText(Messages.getString("ImportConfigWizard.4")); //$NON-NLS-1$
			password = new Text(file, SWT.BORDER);
			password.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
			password.setEchoChar('*');
			password.setToolTipText(Messages.getString("ImportConfigWizard.5"));
			password.addModifyListener(this);
			new Label(file, SWT.LEFT).setText(""); //$NON-NLS-1$

			new Label(file, SWT.LEFT).setText(Messages.getString("ProjectPage.server.link")); //$NON-NLS-1$
			linked = new Button(file, SWT.CHECK);
			linked.setToolTipText(Messages.getString("ProjectPage.server.link.info"));
			linked.setSelection(initialLinkFile);
			new Label(file, SWT.LEFT).setText(""); //$NON-NLS-1$
			
			new Label(file, SWT.LEFT).setText(Messages.getString("ImportConfigWizard.Overwrite")); //$NON-NLS-1$
			overWrite = new Button(file, SWT.CHECK);
			overWrite.setToolTipText(Messages.getString("ImportConfigWizard.Overwrite.tooltip"));
			overWrite.setSelection(false);		
			new Label(file, SWT.LEFT).setText(""); //$NON-NLS-1$

			//
			// -- Contents tree
			//
			Composite treeComp = new Composite(c, 0);
			treeComp.setLayout(new FillLayout());
			tree = new ContainerCheckedTreeViewer(treeComp, SWT.BORDER);
			tree.setContentProvider(new MetamergeFolderContentProvider());
			tree.setLabelProvider(new ConfigLabelProvider());

			gd = new GridData(SWT.FILL, SWT.FILL, true, true);
			gd.horizontalSpan = 3;
			treeComp.setLayoutData(gd);
			// tree.getControl().setLayoutData(gd);
			tree.addCheckStateListener(this);

			// Choose initial view based on selection
			serverButton.setSelection(targetServerSel != null);
			fileButton.setSelection(targetServerSel == null);
			enableFile(targetServerSel == null);

			setControl(c);

			updateComplete();
		}

		protected void updateServerConfigsTable() {
			String server = targetServer.getText();
			if (server.length() == 0) {
				serverConfigs.setInput(null);
				serverConfigs.refresh();
				return;
			}

			targetServer.setEnabled(false);
			serverConfigs.getTable().setEnabled(false);

			Job job = new Job(server) {
				protected IStatus run(IProgressMonitor monitor) {
					try {
						RMIServerAPI api = (RMIServerAPI) RMIServerAPI.createInstance(getName());
						serverConfigsList = api.getSession().listAllConfigurations();
					} catch (Exception e) {
						EclipseAppender.logerror(e.toString(), e, getShell());
					}
					return Status.OK_STATUS;
				}
			};
			job.addJobChangeListener(new JobChangeAdapter() {
				@Override
				public void done(IJobChangeEvent event) {
					getShell().getDisplay().syncExec(new Runnable() {
						public void run() {
							targetServer.setEnabled(true);
							serverConfigs.getTable().setEnabled(true);
							serverConfigs.setSelection(StructuredSelection.EMPTY);
							serverConfigs.setInput(serverConfigsList);
							serverConfigs.refresh();
						}
					});
				}
			});
			job.schedule();
		}

		protected void loadServerConfig(final String config) {
			targetServer.setEnabled(false);
			serverConfigs.getTable().setEnabled(false);

			final String server = targetServer.getText();

			Job job = new Job(server) {
				protected String pwd = null;

				protected IStatus run(IProgressMonitor monitor) {
					RMIServerAPI api = null;
					remoteXML = null;
					try {
						api = (RMIServerAPI) RMIServerAPI.createInstance(server);
						while (remoteXML == null) {
							try {
								remoteXML = api.getSession().checkOutConfiguration(config, pwd);
							} catch (Exception e) {
								if (e.getMessage().indexOf("CTGDKD037E") != -1) {
									getShell().getDisplay().syncExec(new Runnable() {
										public void run() {
											InputDialog id = new InputDialog(getShell(), getWindowTitle(), Messages
													.getString("ImportConfigWizard.4"), pwd, null) {
												@Override
												protected Control createDialogArea(Composite parent) {
													Control c = super.createDialogArea(parent);
													for (Control child : ((Composite) c).getChildren()) {
														if (child instanceof Text && (child.getStyle() & SWT.READ_ONLY) == 0) {
															((Text) child).setEchoChar('*');
														}
													}
													return c;
												}

											};
											switch (id.open()) {
											case Window.OK:
												pwd = id.getValue();
												break;
											default:
												pwd = null;
											}
										}
									});
									remoteXML = null;
									if (pwd == null)
										return Status.OK_STATUS;
								} else {
									throw e;
								}
							}
						}
					} catch (Exception e) {
						EclipseAppender.logerror(e.toString(), e, getShell());
					} finally {
						if (api != null && config != null) {
							try {
								api.getSession().releaseConfigurationLock(config);
							} catch (Exception e) {
								SystemFunctions.doNothing();
							}
						}
					}
					return Status.OK_STATUS;
				}
			};
			job.addJobChangeListener(new JobChangeAdapter() {
				@Override
				public void done(IJobChangeEvent event) {
					getShell().getDisplay().syncExec(new Runnable() {
						public void run() {
							targetServer.setEnabled(true);
							serverConfigs.getTable().setEnabled(true);
							tree.setInput(remoteXML);
							if(remoteXML == null)
								serverConfigs.setSelection(StructuredSelection.EMPTY);
							tree.setAllChecked(true);
							includes = tree.getCheckedElements();
							updateComplete();
						}
					});
				}
			});
			job.schedule();
		}

		protected void enableFile(boolean b) {
			if (b) {
				((StackLayout) stack.getLayout()).topControl = file;
				modifyText(null);
				serverConfigs.setSelection(StructuredSelection.EMPTY);
			} else {
				((StackLayout) stack.getLayout()).topControl = server;
				tree.setInput(null);
				includes = null;
			}
			updateComplete();
			stack.layout();
		}

		public void modifyText(ModifyEvent e) {
			setErrorMessage(null);
			setPageComplete(false);
			String sel = filepath.getText();

			tree.setInput(null);
			includes = null;

			if (sel == null || sel.length() == 0) {
				updateComplete();
				return;
			}

			if (!new File(sel).exists()) {
				updateComplete();
				return;
			}

			if (sel.endsWith(File.separatorChar + ".rs.xml")) {
				setErrorMessage(Messages.getString("ImportConfigWizard.6"));
				updateComplete();
				return;
			}

			Hashtable<String, Object> env = new Hashtable<String, Object>();
			env.put(MetamergeConfigFactory.MC_URL, sel);
			String p = password.getText();
			if (p != null && p.length() > 0)
				env.put(javax.naming.Context.SECURITY_CREDENTIALS, p);

			try {
				sourceXML = new MetamergeConfigXML(env);
				tree.setInput(sourceXML);
				tree.setAllChecked(true);
				includes = tree.getCheckedElements();
			} catch (Exception err) {
				setErrorMessage(err.toString());
				tree.setInput(null);
				includes = null;
			}

			setImportedFileName(sel);

			updateComplete();
		}

		protected void updateComplete() {
			setPageComplete(includes != null && includes.length > 0);
		}

		public void checkStateChanged(CheckStateChangedEvent event) {
			includes = tree.getCheckedElements();
			updateComplete();
		}

	}

	public void setInitialLinkFile(boolean b) {
		this.initialLinkFile = true;
	}
	
	private BaseConfiguration[] resolveDependencies() {

		List<BaseConfiguration> ret = new ArrayList<BaseConfiguration>();

		List<BaseConfiguration> inc = new ArrayList<BaseConfiguration>();
		for (Object o:includes) {
			if (o instanceof BaseConfiguration && ! (o instanceof MetamergeFolder))
				inc.add((BaseConfiguration)o);
		}

		if (sourceXML == null && remoteXML instanceof MetamergeConfigXML)
			sourceXML = (MetamergeConfigXML) remoteXML;
		if (sourceXML == null)
			return inc.toArray(new BaseConfiguration[inc.size()]);

		Map<BaseConfiguration, Set<BaseConfiguration>> allDeps = new HashMap<BaseConfiguration, Set<BaseConfiguration>>();
		Factories f = new Factories();
		Element root = sourceXML.getRootElement();

		for (int i = 0; i<inc.size(); i++) {
			BaseConfiguration b = inc.get(i);
			if (b.getName() == null) {
				ret.add(b); // Cannot happen?
				continue;
			}
			Set<BaseConfiguration> deps = new HashSet<BaseConfiguration>();
			try{                    
				NodeList result = XPathAPI.selectNodeList(sourceXML.findByName(root, b.getName()), ".//InheritFrom"); //$NON-NLS-1$
				for (int j = 0; j < result.getLength(); j++) {
					String internal = f.getNodeText(result.item(j));
					if (internal == null || 
							internal.length() == 0 ||
							internal.startsWith("system:") || //$NON-NLS-1$ 
							internal.startsWith("[") || //$NON-NLS-1$
							internal.startsWith("file:") || //$NON-NLS-1$
							!internal.contains("/")) //$NON-NLS-1$
						continue;
					if (!internal.startsWith("/")) //$NON-NLS-1$
						internal = "/" + internal; //$NON-NLS-1$
					try {
						BaseConfiguration inh = (BaseConfiguration) sourceXML.lookup(internal);
						if (inc.indexOf(inh) != -1)
							deps.add(inh);
					} catch (NameNotFoundException e) {
						SystemFunctions.doNothing();
					}
				}
			} catch (Exception e) {
				SystemFunctions.doNothing();
			}
			allDeps.put(b, deps);
		}

		while (allDeps.size() > 0) {
			boolean found = false;
			for (int size = 0; ! found; size++) {
				Iterator<Map.Entry<BaseConfiguration, Set<BaseConfiguration>>> it = allDeps.entrySet().iterator();
				while(it.hasNext() && (!found || size == 0)) {
					Map.Entry<BaseConfiguration, Set<BaseConfiguration>> e = it.next();
					if (e.getValue().size() > size)
						continue;
					BaseConfiguration b = e.getKey();
					ret.add(b);
					it.remove();
					found = true;
					for (Set<BaseConfiguration> set: allDeps.values())
						set.remove(b);
				}
			}
		}
		return ret.toArray(new BaseConfiguration[ret.size()]);
	}


}
