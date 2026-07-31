/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.widget;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.editors.BaseEditor;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.server.RestServerAPI;
import com.ibm.tdi.eclipse.server.ServerUtils;
import com.ibm.tdi.eclipse.util.TDIToolBar;

public class ServerWidget extends BaseWidget {

	@SuppressWarnings("unused") //$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private Text server;

	private Text user;

	private Text password;

	private Text install;

	private BaseConfiguration infoRecord;

	private Button ssl;

	private Text workdir;

	private Action soldir;
	
	private String oldAddr;
	
	private Text managementPort;
	
	private Text transportPort;

	public ServerWidget(Composite parent, int style, BaseConfiguration editingConfig) {
		this(parent, style, editingConfig, null);
	}

	public ServerWidget(Composite parent, int style, BaseConfiguration editingConfig, BaseEditor editor) {
		super(parent, style, editingConfig, editor);
		createUI();
	}

	private void createUI() {
		setLayout(new FillLayout());
		createForm(this, null);
		infoRecord = getEditingConfig();
		createControl();
	}

	private void createControl() {
		Label l;

		FormToolkit tk = getFormToolKit();
		tk.decorateFormHeading(getForm());
		TDIToolBar bar = new TDIToolBar(getForm());
		bar.setText(Messages.getString("ServerWidget.1")); //$NON-NLS-1$

		Composite c = getForm().getBody();
		c.setLayout(new GridLayout(3, false));

		// Add some code to try to force painting of borders on old Windows...
		tk.setBorderStyle(SWT.BORDER);
		tk.paintBordersFor(c);

		// -- Server address
		l = tk.createLabel(c, Messages.getString("ServerWidget.2")); //$NON-NLS-1$
		l.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		String addr = getStringParameter(RestServerAPI.TDI_ADDRESS);
		if (addr == null || addr.length() == 0) {
			addr = ServerUtils.getGlobalPropAddress();
			infoRecord.setParameter(RestServerAPI.TDI_ADDRESS, addr);
		}
		oldAddr = addr;

		server = tk.createText(c, addr, SWT.BORDER);
		server.setToolTipText(Messages.getString("ServerWidget.4")); //$NON-NLS-1$
		server.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		server.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				infoRecord.setParameter(RestServerAPI.TDI_ADDRESS, server.getText());
				updateManagementPort();
				updateButtonStates();
			}

		});

		tk.createLabel(c, ""); //$NON-NLS-1$

		// -- SSL
		l = tk.createLabel(c, Messages.getString("ServerWidget.10")); //$NON-NLS-1$
		l.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		ssl = tk.createButton(c, "", SWT.CHECK); //$NON-NLS-1$
		ssl.setToolTipText(Messages.getString("ServerWidget.12")); //$NON-NLS-1$
		ssl.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		ssl.setSelection(infoRecord.getBooleanParameter(RestServerAPI.TDI_SSL, false));
		ssl.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				infoRecord.setParameter(RestServerAPI.TDI_SSL, "" + ssl.getSelection()); //$NON-NLS-1$
			}
		});

		tk.createLabel(c, ""); //$NON-NLS-1$

		// -- Username
		l = tk.createLabel(c, Messages.getString("ServerWidget.15")); //$NON-NLS-1$
		l.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		user = tk.createText(c, getStringParameter(RestServerAPI.TDI_USERNAME), SWT.BORDER);
		user.setToolTipText(Messages.getString("ServerWidget.16")); //$NON-NLS-1$
		user.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		user.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				infoRecord.setParameter(RestServerAPI.TDI_USERNAME, user.getText());
			}
		});

		tk.createLabel(c, ""); //$NON-NLS-1$

		// -- Password
		l = tk.createLabel(c, Messages.getString("ServerWidget.18")); //$NON-NLS-1$
		l.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		password = tk.createText(c, getStringParameter(RestServerAPI.TDI_PASSWORD), SWT.BORDER);
		password.setEchoChar('*');
		password.setToolTipText(""); //$NON-NLS-1$
		password.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		password.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				infoRecord.setParameter(RestServerAPI.TDI_PASSWORD, password.getText());
			}
		});

		tk.createLabel(c, ""); //$NON-NLS-1$

		// -- Install directory
		l = tk.createLabel(c, Messages.getString("ServerWidget.21")); //$NON-NLS-1$
		l.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		install = tk.createText(c, "" + getStringParameter(RestServerAPI.TDI_INSTALL), SWT.BORDER); //$NON-NLS-1$
		install.setToolTipText(Messages.getString("ServerWidget.23")); //$NON-NLS-1$
		install.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		install.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				infoRecord.setParameter(RestServerAPI.TDI_INSTALL, install.getText());
				updateButtonStates();
			}
		});

		Button b = tk.createButton(c, Messages.getString("ServerWidget.24"), SWT.PUSH); //$NON-NLS-1$
		b.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dd = new DirectoryDialog(getShell());
				String dir = dd.open();
				if (dir != null) {
					install.setText(dir);
					updateAddressFromDirectory(dir + "/etc/global.properties"); //$NON-NLS-1$
					if (workdir.getText().length() > 0)
						updateAddressFromDirectory(workdir.getText() + "/solution.properties"); //$NON-NLS-1$
				}
			}

		});

		// -- Solution directory address
		l = tk.createLabel(c, Messages.getString("ServerWidget.soldir"));
		l.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		// -- working directory
		workdir = tk.createText(c, getStringParameter(RestServerAPI.TDI_WORKDIR), SWT.BORDER);
		workdir.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		workdir.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				infoRecord.setParameter(RestServerAPI.TDI_WORKDIR, workdir.getText());
				if (workdir.getText().length() > 0)
					updateAddressFromDirectory(workdir.getText() + "/solution.properties"); //$NON-NLS-1$
				updateMQvariables();
				updateButtonStates();
			}
		});

		b = tk.createButton(c, Messages.getString("ServerWidget.29"), SWT.PUSH); //$NON-NLS-1$
		b.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dd = new DirectoryDialog(getShell());
				String dir = dd.open();
				if (dir != null) {
					workdir.setText(dir);
					updateAddressFromDirectory(dir + "/solution.properties"); //$NON-NLS-1$
					updateMQvariables();
				}
			}
		});

		// -- ActiveMQ Transport port
		l = tk.createLabel(c, Messages.getString("ServerWidget.amqtport")); //$NON-NLS-1$
		l.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		transportPort = tk.createText(c, getStringParameter("activeMQ.transport.port"), SWT.BORDER);
		transportPort.setToolTipText(Messages.getString("ServerWidget.amqtport.tooltip")); //$NON-NLS-1$
		transportPort.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		transportPort.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				infoRecord.setParameter("activeMQ.transport.port", transportPort.getText());
			}
		});

		tk.createLabel(c, ""); //$NON-NLS-1$


		// -- ActiveMQ Management port
		l = tk.createLabel(c, Messages.getString("ServerWidget.amqmport")); //$NON-NLS-1$
		l.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		managementPort = tk.createText(c, getStringParameter("activeMQ.management.port"), SWT.BORDER);
		managementPort.setToolTipText(Messages.getString("ServerWidget.amqmport.tooltip")); //$NON-NLS-1$
		managementPort.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		managementPort.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				infoRecord.setParameter("activeMQ.management.port", managementPort.getText());
			}
		});
		updateManagementPort();
		updateMQvariables();
		
		tk.createLabel(c, ""); //$NON-NLS-1$


		// -- Create soldir button
		soldir = new Action() {
			public String getText() {
				return Messages.getString("ServerWidget.32"); //$NON-NLS-1$
			}

			@Override
			public String getToolTipText() {
				return Messages.getString("ServerWidget.33"); //$NON-NLS-1$
			}

			@Override
			public void run() {
				try {
					updateSolutionDirectory();
					getEditor().doSave(null);
				} catch (Exception err) {
					EclipseAppender.logerror(err.toString(), err, getShell());
				}
				try {
					Utils.getTDIServersProject(true).refreshLocal(2, null);
				} catch (Exception ignore) {}
				updateButtonStates();
			}
		};
		bar.add(soldir);

		updateButtonStates();
	}

	private void updateManagementPort() {
		String addr = getStringParameter(RestServerAPI.TDI_ADDRESS);
		String p = managementPort.getText();
		if (p.isEmpty() || getPort(oldAddr).equals(p) ) {
			String port = getPort(addr);
			managementPort.setText(port);
		}
		oldAddr = addr;
	}

	private String getPort(String a) {
		int i = a != null ? a.indexOf(':') : 0;
		return (i > 0) ? a.substring(i+1) : "";		
	}
	
	private void updateButtonStates() {
		soldir.setEnabled(workdir.getText().length() > 0 && install.getText().length() > 0);
	}

	private void updateAddressFromDirectory(String dir) {

		try {
			BufferedReader inp = new BufferedReader(new FileReader(dir));
			String str;
			String address = null;
			Boolean sslon = null;

			while ((str = inp.readLine()) != null) {

				if (str.startsWith("api.remote.naming.port=")) //$NON-NLS-1$
					address = "localhost:" + str.substring(23); //$NON-NLS-1$
				else if (str.startsWith("api.remote.ssl.on=")) //$NON-NLS-1$
					sslon = Boolean.valueOf(str.substring(18));

			}
			inp.close();

			if(address != null) {
				server.setText(address);
			}
			if(sslon != null) {
				ssl.setSelection(sslon);
			}

		} catch (FileNotFoundException nfe) {
			// Do Nothing...
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void addWorkingDirectory() {
		try {
			String def;
			if (getEditor() != null)
				def = getEditor().getPartName();
			else
				def = infoRecord.getShortName();

			def = def.replaceAll("tdiserver", RestServerAPI.TDI_WORKDIR); //$NON-NLS-1$
			if (!def.endsWith(".workdir")) //$NON-NLS-1$
				def += ".workdir"; //$NON-NLS-1$

			InputDialog id = new InputDialog(getShell(), Messages.getString("ServerWidget.59"), Messages.getString("ServerWidget.60"), def, new CheckFolderValidator()); //$NON-NLS-1$ //$NON-NLS-2$
			if (id.open() != Window.OK)
				return;

			IFolder fld = Utils.getTDIServersProject(true).getFolder(id.getValue());
			IPath p = new Path(getStringParameter(RestServerAPI.TDI_WORKDIR));
			fld.createLink(p, 0, null);
			return;
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e, getShell());
			return;
		}
	}

	protected boolean addSystemNS(String str) {
		while (true) {
			try {
				String def = infoRecord.getShortName();
				def = def.replaceAll("tdiserver", "system"); //$NON-NLS-1$ //$NON-NLS-2$
				if (!def.endsWith(".system")) //$NON-NLS-1$
					def += ".system"; //$NON-NLS-1$

				IFile fld = Utils.getTDIServersProject(true).getFile(def);
				if (fld.exists())
					fld.delete(true, null);

				fld.create(new ByteArrayInputStream(str.getBytes()), true, null);
				return true;
			} catch (Exception e) {
				EclipseAppender.logerror(e.toString(), e, getShell());
				return false;
			}
		}
	}

	private String getStringParameter(String param) {
		String str = infoRecord.getStringParameter(param);
		if (str == null)
			return ""; //$NON-NLS-1$
		else
			return str;
	}

	private static class CheckFolderValidator implements IInputValidator {
		public String isValid(String newText) {
			try {
				IFolder fld = Utils.getTDIServersProject(true).getFolder(newText);
				if (fld.exists()) {
					return Messages.getMessage("ServerWidget.62", newText); //$NON-NLS-1$
				}
				return null;
			} catch (Exception err) {
				return err.toString();
			}
		}
	}

	public void updateSolutionDirectory() {
		try {
			if(ServerUtils.createSolutionDirectory(infoRecord)) {
				// Created
				MessageDialog.openInformation(getShell(), soldir.getText(), Messages.getString("ServerWidget.37")); //$NON-NLS-1$
			} else {
				// Updated
				MessageDialog.openInformation(getShell(), soldir.getText(), Messages.getString("ServerWidget.34")); //$NON-NLS-1$
			}

		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e, getShell());
		}
	}

	public boolean hasSolutionDirectory() {
		String str = infoRecord.getStringParameter(RestServerAPI.TDI_WORKDIR);
		if(str != null && new File(str).exists())
			return true;
		else
			return false;
	}
	
	private void updateMQvariables() {
		ServerUtils.readFromActiveMQFile(infoRecord);
		transportPort.setText(getStringParameter("activeMQ.transport.port"));
		managementPort.setText(getStringParameter("activeMQ.management.port"));
		
	}

}
