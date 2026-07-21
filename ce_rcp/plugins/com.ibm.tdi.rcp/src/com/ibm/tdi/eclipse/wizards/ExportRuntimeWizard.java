/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.wizards;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;

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
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

import com.ibm.di.api.remote.Session;
import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.TDI;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.natures.TDINature;
import com.ibm.tdi.eclipse.server.RMIServerAPI;
import com.ibm.tdi.eclipse.server.RestServerAPI;

public class ExportRuntimeWizard extends Wizard implements IExportWizard {
	@SuppressWarnings("unused") //$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	private SelectLocationPage page;

	@Override
	public void addPages() {
		super.addPages();
		page = new SelectLocationPage();
		addPage(page);
	}

	private IStructuredSelection selection;
	protected String targetServerDocument;
	protected String targetServerSolname;
	protected String targetFileName;
	protected String targetPassword;
	protected String sourceProject;
	public boolean targetIsFile;
	private IFile targetServerFile;
//	public Button protect;

	public ExportRuntimeWizard() {
		setWindowTitle(Messages.getString("wizard.name.10"));
	}

	@Override
	public boolean performFinish() {
		if (targetIsFile) {
			return exportToFile(targetFileName, targetPassword);
		} else {
			return exportToServer(targetServerDocument, targetServerSolname); //protect.getSelection());
		}
	}

	private boolean exportToServer(String server, String solname) {
		try {
			IProject source = ResourcesPlugin.getWorkspace().getRoot().getProject(sourceProject);
			RMIServerAPI api = (RMIServerAPI) RestServerAPI.createInstance(server);
			Session sess = api.getSession();
			MetamergeConfig mc = Utils.loadRuntimeRS(source);
			String targetName = source.getName();
			if(solname != null && solname.trim().length() > 0) {
				targetName = solname;
			}
			
			if(targetName.endsWith(".xml")) {
				targetName = targetName.substring(0, targetName.indexOf(".xml"));
			}
			String fileName = targetName + ".xml";
			
			boolean exists = true;
			try {
				ArrayList<String> list = sess.listAllConfigurations();
				if (!list.contains(targetName) && !list.contains(fileName))
					exists = false;
			} catch (Exception e) {
				EclipseAppender.logerror(e.toString(), e, getShell());
			}
			
			if(exists) {
				if(!MessageDialog.openConfirm(getShell(), getWindowTitle(), Messages.getMessage("general.resource.exists", targetName, "")))
					return false;
				
				String pwd = null;
				while(true) {
					try {
						sess.checkOutConfiguration(fileName, pwd);
						break;
					} catch (Exception e) {
						if(e.getMessage().indexOf("CTGDKD037E") != -1) {
							InputDialog id = new InputDialog(getShell(), getWindowTitle(), e.getLocalizedMessage(), pwd, null);
							switch(id.open()) {
							case Window.CANCEL:
								return false;
							case Window.OK:
								pwd = id.getValue();
							}
						} else {
							throw e;
						}
					}
					
				}
			} else {
				sess.createNewConfiguration(fileName, true);
			}
			
			mc.getSolutionInterface().setInstanceID(targetName);
			sess.checkInConfiguration(mc, fileName);
			return true;
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e, getShell());
		}
		return false;
	}

	private boolean exportToFile(String path, String password) {
		File test = new File(path);
		if (test.exists() && !MessageDialog.openQuestion(
				getShell(),
				Messages.getString("general.save.label"),
				Messages.getMessage("general.resource.exists", 
						test.getAbsolutePath(), Utils.dateToString(test.lastModified()))))
			return false;	
		
		try {
			IProject source = ResourcesPlugin.getWorkspace().getRoot().getProject(sourceProject);
			MetamergeConfig mc = Utils.getProjectMC(source);
			Hashtable<String,Object> env = new Hashtable<String,Object>();
			env.put(MetamergeConfigFactory.MC_DRIVER, "com.ibm.di.config.xml.MetamergeConfigXML");
			env.put(MetamergeConfigFactory.MC_CREATE, "true");
			env.put(javax.naming.Context.PROVIDER_URL, path);
			if (password != null && password.length() > 0)
				env.put(javax.naming.Context.SECURITY_CREDENTIALS, password);

			MetamergeConfig dest =  MetamergeConfigFactory.getInstance ( env );

			MetamergeConfigFactory.copy(mc, dest, null, true);
			// When we save to file we unset the identifier
			// Defect 14277: Do not clear the instance id
			//dest.getSolutionInterface().setInstanceID("");
			dest.commitChanges(null, true);

			return true;
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e, getShell());
		}
		return false;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}

	public class SelectLocationPage extends WizardPage {

		private Combo targetServer;
		private Text fileName;
		private Text password;
		private Button fileButton;
		private Button serverButton;
		private Button chooseButton;
		private Combo sourceProjectCombo;
		private Combo solname;
		protected ArrayList serverConfigsList;
		private Composite stack;
		private Composite serverStack;
		private Composite fileStack;

		public SelectLocationPage() {
			super("Location", Messages.getString("wizard.name.10"), null); //$NON-NLS-1$ //$NON-NLS-2$
			setDescription(Messages.getString("ExportRuntimeWizard.target.description")); //$NON-NLS-1$
		}
		
		public void createControl(Composite parent) {
			Composite c = new Composite(parent, SWT.NONE);
			c.setLayout(new GridLayout(3, false));

			//
			// Source project
			//
			Label label = new Label(c, SWT.LEFT);
			label.setText(Messages.getString("ExportRuntimeWizard.project")); //$NON-NLS-1$
			sourceProjectCombo = new Combo(c, SWT.READ_ONLY | SWT.DROP_DOWN);
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			try {
				for (IResource res : root.members()) {
					if (res instanceof IProject) {
						IProject p = (IProject) res;
						if (p.isOpen() && p.hasNature(TDINature.TDI_NATURE_ID))
							sourceProjectCombo.add(p.getName());
					}
				}
			} catch (Exception e) {
			}
			
			sourceProjectCombo.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					int index = sourceProjectCombo.getSelectionIndex();
					if (index != -1)
						sourceProject = sourceProjectCombo.getItem(index);
					else
						sourceProject = null;
					
					if(sourceProject != null) {
						updateServerSelection(sourceProject);
					}
					setPageComplete(isPageComplete());
				}
			});

			String targetServerSel = null;
			
			if (!selection.isEmpty()) {
				Object sel = selection.getFirstElement();
				if (sel instanceof IProject) {
					sourceProject = ((IProject) sel).getName();
				} else if (sel instanceof IResource) {
					if("tdiserver".equals(((IResource)sel).getFileExtension()))
						targetServerSel = ((IResource)sel).getName();
					else
						sourceProject = ((IResource) sel).getProject().getName();
				} else if (sel instanceof BaseConfiguration) {
					BaseConfiguration bc = (BaseConfiguration) sel;
					if (bc.getMetamergeConfig() instanceof TDIConfigurationFile) {
						TDIConfigurationFile cfg = (TDIConfigurationFile) bc.getMetamergeConfig();
						if (cfg.getProject() != null)
							sourceProject = cfg.getProject().getName();
					}
				}
				if (sourceProject != null && sourceProjectCombo.indexOf(sourceProject) != -1)
					sourceProjectCombo.select(sourceProjectCombo.indexOf(sourceProject));
				else
					sourceProject = null;
			}
			
			if(targetServerSel == null && targetServerFile != null)
				targetServerSel = targetServerFile.getName();

			new Label(c, SWT.NONE).setText(""); //$NON-NLS-1$

			//
			// Target server
			//
			serverButton = new Button(c, SWT.RADIO);
			serverButton.setText(Messages.getString("wizard.name.6")); //$NON-NLS-1$
			serverButton.setSelection(targetServerSel != null);
			serverButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					enableFile(false);
					setPageComplete(isPageComplete());
				}
			});
			
			fileButton = new Button(c, SWT.RADIO);
			fileButton.setText(Messages.getString("SearchPage.file")); //$NON-NLS-1$
			fileButton.setSelection(targetServerSel == null);
			fileButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					enableFile(true);
					setPageComplete(isPageComplete());
				}
			});
			
			new Label(c, SWT.NONE).setText(""); //$NON-NLS-1$
			
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 3;
			new Label(c, SWT.SEPARATOR|SWT.HORIZONTAL).setLayoutData(gd);

			//
			// Stack composite showing either server or file controls
			//
			stack = new Composite(c, SWT.NONE);
			stack.setLayout(new StackLayout());
			gd = new GridData(GridData.FILL_BOTH);
			gd.horizontalSpan = 3;
			stack.setLayoutData(gd);
			
			//
			// Server controls
			//
			serverStack = new Composite(stack, SWT.NONE);
			serverStack.setLayout(new GridLayout(2, false));
			
			new Label(serverStack, SWT.LEFT).setText(Messages.getString("ExportRuntimeWizard.toserver"));
			
			targetServer = new Combo(serverStack, SWT.DROP_DOWN | SWT.READ_ONLY);
			gd = new GridData();
			gd.verticalAlignment = GridData.FILL;
			gd.grabExcessHorizontalSpace = true;
			targetServer.setLayoutData(gd);
			targetServer.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					int index = targetServer.getSelectionIndex();
					if (index != -1)
						targetServerDocument = targetServer.getItem(index);
					else
						targetServerDocument = null;
					updateSolnameCombo();
					setPageComplete(isPageComplete());
				}
			});

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
			
			
			// Solution/file name
			new Label(serverStack, SWT.LEFT).setText(Messages.getString("SolutionInterfaceUI.InstanceID"));
			solname = new Combo(serverStack, SWT.DROP_DOWN);
			solname.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			solname.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					targetServerSolname = solname.getText();
				}
			});
			solname.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					targetServerSolname = solname.getText();
				}
			});
			
			if(targetServerSel != null) {
				targetServer.select(targetServer.indexOf(targetServerSel));
				updateSolnameCombo();
			}
			
			//
			// Target file system
			//
			fileStack = new Composite(stack, SWT.NONE);
			fileStack.setLayout(new GridLayout(3, false));

			new Label(fileStack, SWT.LEFT).setText(Messages.getString("ExportRuntimeWizard.tofile")); //$NON-NLS-1$
			
			fileName = new Text(fileStack, SWT.BORDER);
			fileName.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
			fileName.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					targetFileName = fileName.getText();
					setPageComplete(isPageComplete());
				}
			});

			chooseButton = new Button(fileStack, SWT.PUSH);
			chooseButton.setText("..."); //$NON-NLS-1$
			chooseButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					FileDialog fd = new FileDialog(getShell(), SWT.SAVE);
					fd.setFilterExtensions(new String[]{"*.xml", "*.*"});
					String str = fd.open();
					if (str != null) {
						fileName.setText(str);
						setPageComplete(isPageComplete());
					}
				}
			});
			
			new Label(fileStack, SWT.LEFT).setText(Messages.getString("ExportRunTimeWizard.password")); //$NON-NLS-1$
			password = new Text(fileStack, SWT.BORDER);
			password.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
			password.setEchoChar('*');
			password.setToolTipText(Messages.getString("ExportRunTimeWizard.password.tooltip"));
			password.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					targetPassword = password.getText();
				}
			});
			new Label(fileStack, SWT.LEFT).setText(""); //$NON-NLS-1$

			updateServerSelection(sourceProject);

			enableFile(targetServerSel == null);

			setControl(c);

		}

		protected void updateServerSelection(String sourceProject) {
			try {
				IProject sp = ResourcesPlugin.getWorkspace().getRoot().getProject(sourceProject);
				if(sp != null) {
					String str = sp.getPersistentProperty(TDI.PROJECT_PREF_SERVER_QNAME);
					if(str != null && str.length() > 0) {
						int index = targetServer.indexOf(str);
						if(index != -1) {
							targetServer.select(index);
							targetServerDocument = str;
						}
					}
				}
			} catch (Exception e) {}
		}

		protected void enableFile(boolean b) {
			targetServer.setEnabled(!b);
			solname.setEnabled(!b);
			fileName.setEnabled(b);
			password.setEnabled(b);
			chooseButton.setEnabled(b);
			targetIsFile = b;
			((StackLayout)stack.getLayout()).topControl = b ? fileStack : serverStack;
			stack.layout(true, true);
		}

		@Override
		public boolean isPageComplete() {
			if (sourceProject == null)
				return false;

			if (serverButton.getSelection())
				return targetServer.getSelectionIndex() != -1;
			else
				return fileName.getText().length() > 0;
		}

		protected void updateSolnameCombo() {
			String server = targetServer.getText();
			if (server.length() == 0) {
				solname.removeAll();
				return;
			}

			targetServer.setEnabled(false);
			solname.setEnabled(false);

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
							solname.setEnabled(true);
							solname.removeAll();
							if (serverConfigsList != null) {
								for(Object obj : serverConfigsList)
									solname.add(obj.toString());
							}
						}
					});
				}
			});
			job.schedule();
		}
	}

	public void setTargetServer(IFile selectedFile) {
		targetServerFile = selectedFile;
	}

}
