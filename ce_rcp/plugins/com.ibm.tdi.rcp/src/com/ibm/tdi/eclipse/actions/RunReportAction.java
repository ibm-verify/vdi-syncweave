/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import javax.naming.Name;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.menus.AbstractContributionFactory;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.services.IServiceLocator;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.FormConfig;
import com.ibm.di.config.interfaces.FormItemConfig;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.xml.Factories;
import com.ibm.di.config.xml.MetamergeConfigXML;
import com.ibm.di.report.IReport;
import com.ibm.di.report.ReportFactory;
import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.tdi.eclipse.Activator;
import com.ibm.tdi.eclipse.ConfigUtils;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.log.EclipseAppender;

public class RunReportAction extends BaseAction {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private String TEMPLATE_BASE_PATH = Activator.getInstallPath() + "/XSLT/ConfigReports";

	public RunReportAction() {
	}

	public void run(IAction action) {
		FileDialog fd = new FileDialog(getShell(), SWT.OPEN);
		fd.setFilterPath(TEMPLATE_BASE_PATH);
		fd.setFilterExtensions(new String[] { "*.xsl" });
		fd.setText(Messages.getString("miadmin.filechooser.report.templates.title"));
		String path = fd.open();
		if (path == null) {
			return;
		}

		try {
			IFile file = (IFile) getSelectionItems()[0];
			create(file, path);
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e, getShell());
		}
	}

	public void create(IFile config, String template) throws Exception {

		if (template.toLowerCase().endsWith(".xml")) {
			createReport(config, template);
			return;
		}
		
		IFile rsfile = Utils.getRuntimeRS(config.getProject());
		Hashtable<String, Object> env = new Hashtable<String, Object>();
		env.put(MetamergeConfigFactory.MC_URL, rsfile.getLocation().toOSString());
		MetamergeConfigXML mc = new MetamergeConfigXML(env);
		String name = config.getName();
		name = name.substring(0, name.lastIndexOf("."));
		BaseConfiguration cfg = (BaseConfiguration) mc.lookupInFolder(MetamergeConfig.DEFAULT_ASSEMBLYLINE_FOLDER, name);

		MetamergeConfig source;
		if (cfg instanceof MetamergeConfig)
			source = (MetamergeConfig) cfg;
		else
			source = cfg.getMetamergeConfig();

		Document doc = mc.getDocument();
		maskPasswords(doc);

		Transformer transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(new File(template)));
		setParameters(transformer, cfg, source);
		StreamSource xmlsource = new StreamSource(new StringReader(documentToString(doc)));

		IFolder repfolder = config.getProject().getFolder("Reports");
		if (!repfolder.exists())
			repfolder.create(true, true, null);  //2nd argument changed from false to true, defect 12959

		StringWriter sw = new StringWriter();
		transformer.transform(xmlsource, new StreamResult(sw));

		String date = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());

		File tmplt = new File(template);
		String type = tmplt.getName().substring(0, tmplt.getName().length() - 4);
		IFile report = repfolder.getFile(cfg.getShortName() + "-" + type + "-" + date + ".html");
		ByteArrayInputStream bis = new ByteArrayInputStream(sw.getBuffer().toString().getBytes());
		if (report.exists())
			report.setContents(bis, IFile.FORCE, null);
		else
			report.create(bis, true, null);

		ConfigUtils.showURL("file:///" + report.getLocation().toPortableString());
	}

	//---- added by Yavor -------------------------------------------------
	
	/**
	 * 
	 * @param config
	 * @param template
	 * @throws Exception
	 */
	public void createReport(IFile config, String template) throws Exception {

		IFile rsfile = Utils.getRuntimeRS(config.getProject());
		Hashtable<String, Object> env = new Hashtable<String, Object>();
		env.put(MetamergeConfigFactory.MC_URL, rsfile.getLocation().toOSString());
		MetamergeConfigXML mc = new MetamergeConfigXML(env);
		String name = config.getName();
		name = name.substring(0, name.lastIndexOf("."));
		BaseConfiguration cfg = (BaseConfiguration) mc.lookupInFolder(MetamergeConfig.DEFAULT_ASSEMBLYLINE_FOLDER, name);

		MetamergeConfig source;
		if (cfg instanceof MetamergeConfig)
			source = (MetamergeConfig) cfg;
		else
			source = cfg.getMetamergeConfig();
				
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(ReportFactory.TDI_INSTALL_PATH, Activator.getInstallPath());
		properties.put(ReportFactory.TDI_LOCALE, getCurrentLocale());
		ReportFactory reportFactory = ReportFactory.newInstance(properties);
		String html = "";
		IReport reportInstance = reportFactory.createReport(template);
		html = reportInstance.generate(cfg, source);

		// Save report
		IFolder repfolder = config.getProject().getFolder("Reports");
		if (!repfolder.exists())
			repfolder.create(true, true, null);  //2nd argument changed from false to true, defect 12959

		StringWriter sw = new StringWriter();
		sw.write(html);
		String date = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());

		File tmplt = new File(template);
		String type = tmplt.getName().substring(0, tmplt.getName().length() - 4);
		IFile report = repfolder.getFile(cfg.getShortName() + "-" + type + "-" + date + ".html");
		ByteArrayInputStream bis = new ByteArrayInputStream(sw.getBuffer().toString().getBytes());
		if (report.exists()) {
			report.setContents(bis, IFile.FORCE, null);
		} else {
			report.create(bis, true, null);
		}

		ConfigUtils.showURL("file:///" + report.getLocation().toPortableString());
	}

	/**
	 * 
	 * @return
	 */
	private String getCurrentLocale() {
		String localeString = System.getProperty("com.ibm.di.admin.configreport.translation");
		Locale locale = Locale.getDefault();
		String lang = locale.getLanguage();
		String country = locale.getCountry();
		File transDir = new File(TEMPLATE_BASE_PATH + File.separator + "translation");

		File f = null;
		if (localeString != null && localeString.length() > 0) {
			f = new File(transDir, localeString);
		}

		if (f == null || !f.exists()) {
			localeString = lang + "_" + country;
			f = new File(transDir, localeString);
		}

		if (!f.exists()) {
			localeString = lang;
			f = new File(transDir, localeString);
		}

		if (!f.exists()) {
			localeString = "en";
		}
		return localeString;
	}	
	
	//---- end added by Yavor -------------------------------------------------

	private String documentToString(Document doc) throws Exception {
		OutputFormat format = new OutputFormat("xml", "UTF-8", true); // Serialize
		// DOM
		format.setOmitXMLDeclaration(true);
		StringWriter bf = new StringWriter();
		XMLSerializer serial = new XMLSerializer(bf, format);
		serial.asDOMSerializer();
		bf.flush();
		serial.serialize(doc.getDocumentElement());
		return bf.toString();
	}

	public void maskPasswords(Document doc) {
		NodeList nl = doc.getElementsByTagName(Factories.INHERIT_TAG);
		if (nl == null)
			return;

		HashSet<String> pw = new HashSet<String>();
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if (n.getFirstChild() == null)
				continue;
			String inheritFrom = n.getFirstChild().getNodeValue();
			if (inheritFrom != null && !inheritFrom.equals(BaseConfiguration.INHERIT_PARENT)
					&& !inheritFrom.equals(BaseConfiguration.INHERIT_NONE)) {
				try {
					BaseConfiguration b = (BaseConfiguration) MetamergeConfigFactory.lookup(null, inheritFrom);
					String cls = getClassString(b);
					pw.addAll(getPasswordParameterNames(cls));
				} catch (Exception ignore) {
				}
			}
		}

		NodeList params = doc.getElementsByTagName(Factories.PARAMETER_TAG);
		if (params == null)
			return;

		for (int j = 0; j < params.getLength(); j++) {
			NamedNodeMap nm = params.item(j).getAttributes();
			Node child = params.item(j).getFirstChild();
			if (nm == null || child == null)
				continue;
			for (int k = 0; k < nm.getLength(); k++) {
				String value = nm.item(k).getNodeValue();
				if (value != null && pw.contains(value))
					child.setNodeValue("*****");
			}
		}
	}

	public Vector<String> getPasswordParameterNames(String aJavaClassName) {
		Vector<String> passwordParameters = new Vector<String>();
		if (aJavaClassName == null)
			return passwordParameters;

		try {
			FormConfig form = (FormConfig) MetamergeConfigFactory.lookup(null, "system:/Forms/" + aJavaClassName);
			for (String name : form.getFormItemNames()) {
				FormItemConfig fic = form.getFormItem(name);
				if (fic != null && "password".equalsIgnoreCase(fic.getSyntax()))
					passwordParameters.add(name);
			}
		} catch (Exception ignore) {
		}

		return passwordParameters;
	}

	private String getClassString(BaseConfiguration b) {
		if (b instanceof ConnectorConfig)
			return ((ConnectorConfig) b).getConnectionConfig().getJavaClass();
		if (b instanceof FunctionConfig)
			return ((FunctionConfig) b).getJavaClass();
		return null;
	}

	private void setParameters(Transformer transformer, BaseConfiguration cfg, MetamergeConfig mc) {

		transformer.setParameter("tdiObjectName", cfg.getShortName());

		Name name = cfg.getName();
		String type = "config-level";
		if (cfg != mc && name != null && name.size() >= 2) {
			type = name.get(0);
			if (type.endsWith("s"))
				type = type.substring(0, type.length() - 1);
		}
		transformer.setParameter("tdiObjectType", type);

		transformer.setParameter("tdiToday", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));

		transformer.setParameter("tdiConfigName", mc.toString());

		String localeString = getCurrentLocale();

		transformer.setParameter("tdiLocal", localeString);
		transformer.setParameter("tdiHome", Activator.getInstallPath());
	}

	public static class ALReportAction extends Action {

		private IFile file;
		private File template;

		public ALReportAction(File template) {
			super("");
			this.template = template;

			if(template != null) {
				String name = template.getName().substring(0, template.getName().length() - 4);
				String translated = Messages.getString("ConfigReport.fileName." + name);
				setText(translated != null ? translated : name);
			} else {
				setText(Messages.getString("util.filechooser.title") + "...");
			}
		}

		public void setFile(IFile file) {
			this.file = file;
		}

		@Override
		public void run() {
			IStructuredSelection sel = (IStructuredSelection)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
			if(sel.isEmpty())
				return;

			if(sel.getFirstElement() instanceof IFile)
				file = (IFile)sel.getFirstElement();

			if(file == null)
				return;

			RunReportAction rra = new RunReportAction();
			if(template == null) {
				rra.init(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
				rra.setSelection(new StructuredSelection(file));
				rra.run(this);
			} else {
				try {
					rra.create(file, template.getAbsolutePath());
				} catch (Exception e) {
					EclipseAppender.logerror(e.toString(), e, PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
				}
			}
		}

	}

	public static void installStandardReports(IMenuService menuService) {
		AbstractContributionFactory factory = new AbstractContributionFactory("popup:com.ibm.tdi.eclipse.navigator#PopupMenu?after=group.tdi", null) {
			@Override
			public void createContributionItems(IServiceLocator serviceLocator, IContributionRoot additions) {
				MenuManager mm = new MenuManager(Messages.getString("action.label.26"));

				File path = new File(Activator.getInstallPath() + "/XSLT/ConfigReports");
				if(path.exists()) {
					for(File file : path.listFiles()) {
						if(file.getName().endsWith(".xsl")) {
							mm.add(new ALReportAction(file));
						} else if(file.getName().endsWith(".xml")) {
							mm.add(new ALReportAction(file));
						}
					}
				}
				mm.add(new ALReportAction(null));

				Expression expr = new Expression() {
					public EvaluationResult evaluate(IEvaluationContext context) throws CoreException {
						Object defVar = context.getDefaultVariable();
						if(defVar instanceof List) {
							List arr = (List) context.getDefaultVariable();
							if(arr.size() == 1 && arr.get(0) instanceof IFile) {
								IFile file = (IFile) arr.get(0);
								if("assemblyline".equals(file.getFileExtension()))
									return EvaluationResult.TRUE;
							}
						}
						return EvaluationResult.FALSE;
					}
				};
				additions.addContributionItem(mm, expr);
			}
		};
		menuService.addContributionFactory(factory);
	}
}
