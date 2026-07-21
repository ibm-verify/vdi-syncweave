/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.wizards;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableColumn;

import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.di.config.interfaces.MetamergeConfigChangeListener;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.MetamergeFolder;
import com.ibm.di.config.interfaces.OperationConfig;
import com.ibm.di.config.interfaces.OperationsConfig;
import com.ibm.di.config.interfaces.ParserConfig;
import com.ibm.di.config.xml.MetamergeConfigXML;
import com.ibm.di.server.VersionInfoInterface;
import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.server.RestServerAPI;
import com.ibm.tdi.eclipse.widget.FormWidget2;

public class PublishALWizard extends Wizard  {

	private BaseConfiguration alc;
	private MetamergeConfig mc;
	private Page1 page1;
	private BaseConfiguration pkg;

	public PublishALWizard(IFile file) throws Exception {
		super();

		String name = file.getName().substring(0, file.getName().lastIndexOf(".")); //$NON-NLS-1$

		// -- Create MC for published al in memory
		String folder = TDIConfigurationFile.getFolderForExtension(file.getFileExtension());
		alc = (BaseConfiguration) Utils.getProjectMC(file.getProject()).lookup(folder + "/" + name); //$NON-NLS-1$
		mc = publish(alc, "");

		// -- Create the info section
		ContainerConfig config = (ContainerConfig) mc.lookup("Package"); //$NON-NLS-1$
		pkg = config.getConfig("Info");
		if (pkg == null) {
			pkg = new com.ibm.di.config.base.BaseConfigurationImpl();
			try {
				pkg.setName("Info");
			} catch (Exception err) {
			}
			config.addConfig(pkg);
			pkg.setParameter("date", new SimpleDateFormat("yyyy-MM-dd")
					.format(new Date()));
		}
	}

	@Override
	public boolean performFinish() {
		String id = page1.getId();
		String path = page1.getPath();
		try {
			File file = new File(path);
			if(!file.exists()) {
				if(!file.mkdirs()) {
					throw new IOException(path);
				}
			}
			file = new File(path + File.separator + id + ".xml");
			if (file.exists() && !MessageDialog.openQuestion(
					getShell(),
					Messages.getString("general.save.label"),
					Messages.getMessage("general.resource.exists", file.getAbsolutePath(),
							Utils.dateToString(file.lastModified()))))
				return false;

			mc.setDriverParameter(MetamergeConfigFactory.MC_URL, file.getAbsolutePath());
			mc.commitChanges(null, true);
			MetamergeConfigFactory.addPackage(file.getAbsolutePath());
			return true;
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e, getShell());
			return false;
		}
	}

	@Override
	public void addPages() {
		page1 = new Page1();
		addPage(page1);
		setWindowTitle(page1.getTitle());
		getShell().setText(Messages.getString("assemblyline.popup.Publish.label"));
	}

	private class Page1 extends WizardPage implements MetamergeConfigChangeListener {

		private Combo target;
		private String id;
		private String path;
		private String title;

		public Page1() {
			super("PackageInfo"); //$NON-NLS-1$
		}

		public String getTitle() {
			return title;
		}
		public void createControl(Composite parent) {
			try {
				Composite c = new Composite(parent, SWT.NONE);
				c.setLayout(new GridLayout(1,false));
				c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

				FormWidget2 form = new FormWidget2(c, 0, pkg, "PackageInfo"); //$NON-NLS-1$
				title = form.getTitle();
				form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

				Composite c2 = new Composite(c, SWT.NONE);
				c2.setLayout(new GridLayout(2,false));
				c2.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));

				new Label(c2, SWT.LEFT).setText(Messages.getString("ExportRuntimeWizard.tofile")); //$NON-NLS-1$
				target = new Combo(c2, SWT.DROP_DOWN);

				IProject project = Utils.getTDIServersProject(true);
				for(IResource res : project.members()) {
					if("tdiserver".equals(res.getFileExtension())) { //$NON-NLS-1$
						try {
							RestServerAPI api = RestServerAPI.createInstance((IFile)res);
							String install = api.getInstall();
							if(install != null && install.length() > 0) {
								target.add(install + File.separator + "packages");
							}
							String wkdir = api.getWorkdir();
							if(wkdir != null && wkdir.length() > 0) {
								target.add(wkdir + File.separator + "packages");
							}
						}catch (Exception e) {}
					}
				}
				if(target.getItemCount() > 0)
					target.select(0);

				pkg.addListener(this);
				updateComplete();

				setControl(c);
			} catch (Exception e) {
				EclipseAppender.logerror(e.toString(), e, getShell());
			}
		}

		public void configurationChanged(MetamergeConfigChange changeEvent) {
			updateComplete();
		}

		private void updateComplete() {
			id = pkg.getStringParameter("packageid");
			path = target.getText();
			setPageComplete(id != null && id.length() > 0 && path != null && path.length() > 0);
		}

		@Override
		public void dispose() {
			pkg.removeListener(this);
			super.dispose();
		}

		public String getId() {
			return id;
		}

		public String getPath() {
			return path;
		}

	}

	/**
	 * Publish an item
	 *
	 * @param item
	 *            The item to publish
	 * @param url
	 *            The file or url to publish to
	 *
	 * @return The config with the published item
	 * @throws Exception
	 */
	public static MetamergeConfig publish(BaseConfiguration item, String url)
			throws Exception {
		Hashtable<String,Object> env = new Hashtable<String,Object>();
		env.put(MetamergeConfigFactory.MC_CREATE, "true");
		env.put(MetamergeConfigFactory.MC_URL, url);
		MetamergeConfig mc = new MetamergeConfigXML(env);

		MetamergeConfig sc = item.getMetamergeConfig();

		// Copy published item
		BaseConfiguration bc  = (BaseConfiguration) item.getClone();
		mc.bind(bc.getName(), bc);
		List<String> list = bc.getReferences(null);
		bc.flatten(new ArrayList<String>());

		if ( sc != null ) {
			MetamergeFolder scripts = sc.getDefaultFolder(MetamergeConfig.SCRIPT_FOLDER);
			MetamergeConfigFactory.copyFolder(scripts, mc, MetamergeConfigFactory.parseName(MetamergeConfig.DEFAULT_SCRIPT_FOLDER), false);

			MetamergeFolder props = sc.getDefaultFolder(MetamergeConfig.PROPSTORE_FOLDER);
						if (props != null) {
							MetamergeFolder propsCopy = (MetamergeFolder) props.getClone();
							propsCopy.setMetamergeConfig(mc);
							mc.rebind(MetamergeConfig.DEFAULT_PROPSTORE_FOLDER, propsCopy);
						}

		}

		/**
		 *
		 * <Package>
		 *
		 * <Operations> <Operation> <Name/> <Public/> </Operation> <Operation>
		 *
		 * <Resources> <Resource> <Type/> <Class/> <Version/> </Resource>
		 * </Resources>
		 *
		 * </Package>
		 */

		// Create package container
		ContainerConfig cc;
		try {
			cc = (ContainerConfig) mc.lookup("Package");
		} catch (javax.naming.NameNotFoundException nfe) {
			cc = new com.ibm.di.config.base.ContainerConfigImpl();
			cc.setName("Package");
			cc.setMetamergeConfig(mc);
			mc.bind(cc.getName(), cc);
		}

		// Create resource container
		ContainerConfig rc = (ContainerConfig) cc.getConfig("Resources");
		if (rc == null) {
			rc = new com.ibm.di.config.base.ContainerConfigImpl();
			rc.setName("Resources");
			cc.addConfig(rc);
		}

		// Create resource usage records
		for (int i = 0; i < list.size(); i++) {
			String ref = (String) list.get(i);
			if (ref.startsWith("@"))
				continue;
			BaseConfiguration b = (BaseConfiguration) sc.lookup(ref);
			if (b instanceof ConnectorConfig) {
				addVersionInfo(ref, ((ConnectorConfig) b)
						.getConnectionConfig().getJavaClass(), rc);
			} else if (b instanceof ParserConfig) {
				addVersionInfo(ref, ((ParserConfig) b).getJavaClass(), rc);
			}
		}

		// Operations
		ContainerConfig operations = (ContainerConfig) cc
				.getConfig("Operations");
		if (operations == null) {
			operations = new com.ibm.di.config.base.ContainerConfigImpl();
			operations.setName("Operations");
			cc.addConfig(operations);
		}

		// Create operation records
		if (item instanceof OperationsConfig) {

			ContainerConfig ops = ((OperationsConfig) bc).getOperations();
			for (int j = 0; j < ops.size(); j++)
				addOperation(operations, (OperationConfig) ops.getConfig(j));
		}

		return mc;
	}

	/**
	 * Adds an Operation section to a container
	 *
	 * @param cc
	 *            The receiving container
	 * @param oc
	 *            The operation source
	 * @throws Exception
	 */
	public static void addOperation(ContainerConfig cc, OperationConfig oc)
			throws Exception {
		BaseConfiguration info = new com.ibm.di.config.base.BaseConfigurationImpl();
		info.setName("Operation");
		info.setStringParameter("name", oc.getShortName());
		info.setBooleanParameter("public", oc.isPublic());
		cc.addConfig(info);
	}

	/**
	 * Adds a VersionInfo record for a component to a container
	 *
	 * @param name
	 *            The name of the component
	 * @param cls
	 *            The component's java class name
	 * @param cc
	 *            The receiving container
	 * @throws Exception
	 */
	public static void addVersionInfo(String name, String cls,
			ContainerConfig cc) throws Exception {
		if (cls == null)
			return;
		String version = "";
		try {
			Class<?> c = Class.forName(cls);
			if (VersionInfoInterface.class.isAssignableFrom(c)) {
				VersionInfoInterface vi = (VersionInfoInterface) c.newInstance();
				version = vi.getVersion();
			}
			cls = c.getName();
		} catch (Throwable t) {
			// The class cannot be loaded, probably some missing libraries.
			version = "";
		}

		BaseConfiguration info = new com.ibm.di.config.base.BaseConfigurationImpl();
		info.setName("Resource");
		info.setStringParameter("type", "Resource");
		info.setStringParameter("class", cls);
		info.setStringParameter("version", version);
		info.setStringParameter("name", name);

		cc.addConfig(info);
	}

	public static class Resources extends Composite {

		private ContainerConfig config;
		private ContainerConfig ops;
		private TableColumnLayout layout;

		public Resources(FormWidget2 form, Composite parent, BaseConfiguration config, String paramName) {
			super(parent, 0);
			this.config = (ContainerConfig) config.getParent();
			ops = (ContainerConfig) this.config.getConfig("Resources");
			layout = new TableColumnLayout();
			setLayout(layout);
			createUI();
		}

		private void createUI() {
			TableViewer table = new TableViewer(this, SWT.BORDER);
			table.setContentProvider(new IStructuredContentProvider() {
				public Object[] getElements(Object inputElement) {
					return ops.getConfigurations(null).toArray();
				}
				public void dispose() {}
				public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				}

			});
			table.setLabelProvider(new ITableLabelProvider() {
				public String getColumnText(Object element, int columnIndex) {
					if(columnIndex == 0)
						return ((BaseConfiguration)element).getStringParameter("name");
					else
						return ((BaseConfiguration)element).getStringParameter("version");
				}

				public Image getColumnImage(Object element, int columnIndex) {
					return null;
				}
				public void addListener(ILabelProviderListener listener) {}
				public void dispose() {}
				public boolean isLabelProperty(Object element, String property) {
					return false;
				}
				public void removeListener(ILabelProviderListener listener) {}
			});

			TableColumn tc = new TableColumn(table.getTable(), SWT.LEFT);
			tc.setText(Messages.getString("AssemblyLine.Operations.Table.Name"));
			layout.setColumnData( tc, new ColumnWeightData( 60 ) );
			tc = new TableColumn(table.getTable(), SWT.LEFT);
			tc.setText(Messages.getString("miadmin.title.information"));
			layout.setColumnData( tc, new ColumnWeightData( 40 ) );

			table.getTable().setHeaderVisible(true);

			table.setInput(ops);
		}
	}

	public static class Operations extends Composite {

		private ContainerConfig config;
		private ContainerConfig ops;

		public Operations(FormWidget2 form, Composite parent, BaseConfiguration config, String paramName) {
			super(parent, 0);
			this.config = (ContainerConfig) config.getParent();
			ops = (ContainerConfig) this.config.getConfig("Operations");
			setLayout(new FillLayout());
			createUI();
		}

		private void createUI() {

			CheckboxTableViewer ct = CheckboxTableViewer.newCheckList(this, SWT.BORDER);
			ct.setContentProvider(new ArrayContentProvider());
			ct.setLabelProvider(new ITableLabelProvider() {
				public String getColumnText(Object element, int columnIndex) {
					return ((BaseConfiguration)element).getStringParameter("name");
				}

				public Image getColumnImage(Object element, int columnIndex) {
					return null;
				}
				public void addListener(ILabelProviderListener listener) {}
				public void dispose() {}
				public boolean isLabelProperty(Object element, String property) {
					return false;
				}
				public void removeListener(ILabelProviderListener listener) {}
			});
			ct.setInput(ops.getConfigurations(null).toArray());

			for(Object obj : ops.getConfigurations(null)) {
				BaseConfiguration b = (BaseConfiguration) obj;
				ct.setChecked(b, b.getBooleanParameter("public", true));
			}

			ct.addCheckStateListener(new ICheckStateListener() {
				public void checkStateChanged(CheckStateChangedEvent event) {
					BaseConfiguration b = (BaseConfiguration) event.getElement();
					b.setParameter("public", event.getChecked());
				}
			});

			TableColumn tc = new TableColumn(ct.getTable(), SWT.LEFT);
			tc.setText(Messages.getString("AssemblyLine.Operations.Table.Public") + "/" + Messages.getString("AssemblyLine.Operations.Table.Name"));
			tc.setWidth(400);

			ct.getTable().setHeaderVisible(true);
		}
	}

	@Override
	public String getWindowTitle() {
		return Messages.getString("assemblyline.popup.Publish.label");
	}
}
