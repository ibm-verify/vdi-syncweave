/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.model.descriptor;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.naming.NameNotFoundException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import com.ibm.di.config.base.InternalSchema;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.FormConfig;
import com.ibm.di.config.interfaces.FormItemConfig;
import com.ibm.di.config.interfaces.FormSection;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.ParserConfig;
import com.ibm.di.config.interfaces.RawConnectorConfig;
import com.ibm.di.config.xml.MetamergeConfigXML;
import com.ibm.di.connector.Connector;
import com.ibm.di.util.ResourceLocator;

/**
 * This class provides generation/manipulation of Descriptor objects -
 * {@link ConnectorDescriptor}, {@link FunctionComponentDescriptor},
 * {@link ParserDescriptor}. The class has dependency on the system
 * {@link MetamergeConfig} and the Global {@link FormConfig} so it should only
 * be used in the context of the Tivoli Directory Integrator Server. <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public class DescriptorUtils {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	// -- Global prefix for form item names
	private static final String GLOBAL_PREFIX = "$GLOBAL.";

	// -- Special form syntax entries
	private static final String SYNTAX_DROPLIST = "droplist";

	// -- Special form syntax entries
	private static final String SYNTAX_DROPEDIT = "dropedit";

	private static final String RESOURCE_BUNDLE_EXTENTION = ".properties";

	private static final FormConfig globalForm;
	static {
		FormConfig frm = null;
		try {
			frm = getGlobalForm();
		} catch (Exception ex) {
			ex.printStackTrace();
			frm = null;
		}
		globalForm = frm;
	}

	/**
	 * Contains a map between the supported by the Server {@link Locale}s and
	 * their corresponding string representations (in the syntax specified by
	 * RFC 1766). Note that some components might support a wider range of
	 * locales and we won't know it as we are using the global form's NLS file
	 * to find out the supported locales.
	 */
	private static final Map<Locale, String> supportedLocales;
	static {
		Map<Locale, String> locs = null;
		try {
			if (globalForm != null) {
				locs = getAvailableLocalesFor(globalForm.getTranslationFile());
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (locs == null) {
			locs = new HashMap<Locale, String>();
			locs.put(Locale.ENGLISH, toRFC1766String(Locale.ENGLISH));
		}
		supportedLocales = locs;
	}

	/**
	 * Generates a {@link ConnectorDescriptor} based on the tdi.xml for the
	 * connector specified by the provided parameters.
	 * 
	 * @param connId
	 *            the connector name under which the {@link ConnectorConfig}
	 *            object is mapped in the {@link MetamergeConfig}
	 * @param cfg
	 *            the config object corresponding to the provided name.
	 * @return the connector descriptor of the specified {@link ConnectorConfig}
	 * @throws Exception
	 */
	public static ConnectorDescriptor getConnectorDescriptor(String connId, ConnectorConfig cfg) throws Exception {

		String name = connId;
		if(connId != null && connId.indexOf("/") != -1)
			name = connId.substring(connId.lastIndexOf("/")+1);

		//
		// 1. Try extensions to standard form (class name)
		// 2. Try class name
		// 3. Try simple name
		FormConfig form = getFormForConfig(cfg.getConnectionConfig().getJavaClass() + "." + name, cfg);
		if(form == null)
			form = getFormForConfig(cfg.getConnectionConfig().getJavaClass(), cfg);
		if(form == null)
			form = getFormForConfig(name, cfg);

		ConnectorDescriptor result = new ConnectorDescriptor();
		setComponentData(result, connId, cfg.getConnectionConfig().getJavaClass(), form);

		switch (cfg.getConnectionConfig().getParserOption()) {
		case RawConnectorConfig.PARSER_REQUIRED:
			result.setUseParser(UseParserEnum.REQUIRED);
			break;
		case RawConnectorConfig.PARSER_OPTIONAL:
			result.setUseParser(UseParserEnum.OPTIONAL);
			break;
		case RawConnectorConfig.PARSER_USELESS:
		default:
			result.setUseParser(UseParserEnum.PROHIBIT);
			break;
		}

		List<ModeOption> modes = result.getSupportedModes();
		for (String mode : getConnectorModes(cfg)) {
			ModeOption m = new ModeOption();
			m.setValue(ConnectorModesEnum.fromValue(mode));
			m.getLabels().add(new Label(mode));
			modes.add(m);
		}

		fillInParameterMap(result.getParameterMapDescriptor(), form, modes);

		return result;
	}

	private static void fillInParameterMap(ParameterMapDescriptor paramMap, FormConfig form, List<ModeOption> connModes)
			throws IOException, URISyntaxException {
		fillInParameters(paramMap.getParameterDescriptors(), form);

		for (String name : form.getSectionNames()) {
			FormSection sec = form.getSection(name);

			if (sec != null) {
				createSectionDescriptor(paramMap, name, sec, form);
			} else if (name.startsWith("$Mode-") && connModes != null) {
				name = name.substring(5);
				for (ModeOption mode : connModes) {
					String newName = mode.getValue().value() + name;
					sec = form.getSection(newName);
					if (sec != null) {
						createSectionDescriptor(paramMap, newName, sec, form);
					}
				}
			}
		}
	}

	private static void createSectionDescriptor(ParameterMapDescriptor paramMap, String name, FormSection sec, FormConfig form) {
		SectionDescriptor sd = new SectionDescriptor();
		sd.setId(sec.getShortName());
		
		ArrayList<Locale> locales = new ArrayList<Locale>();
		for (Locale loc : supportedLocales.keySet()) {
			if(form != null && form.getTranslationFile() != null)
				locales.add(loc);
		}
		
		
		for(Locale loc : locales) {
			form.setTranslationLocale(loc.toString());
			String title = sec.getTitle();
			if(title != null) {
				sd.getLabels().add(new Label(title, loc.toString()));
			} else {
				sd.getLabels().add(new Label(sd.getId(), loc.toString()));
			}
			
			String desc = sec.getDescription();
			if (desc != null) {
				sd.getDescriptions().add(new Label(desc, loc.toString()));
			}
		}
		
		sd.setExpanded(sec.initiallyExpanded());
		paramMap.getSectionDescriptors().add(sd);

		for (String shortName : (List<String>) sec.getParameter("parameterlist")) {
			for (ParameterDescriptor pd : paramMap.getParameterDescriptors()) {
				if (shortName.startsWith("$GLOBAL.")) {
					shortName = shortName.substring(8);
				}
				if (shortName.equals(pd.getKey())) {
					pd.setSection(name);
				}
			}
		}
	}

	private static void setComponentData(ComponentDescriptor dest, String compId, String javaClass, FormConfig compForm) {
		dest.setId(compId);
		dest.setJavaClass(javaClass);

		ArrayList<Locale> locales = new ArrayList<Locale>();
		for (Locale loc : supportedLocales.keySet()) {
			if(compForm != null && compForm.getTranslationFile() != null)
				locales.add(loc);
			else
				System.out.println("No form or translation file for: " + compId + "; " + javaClass + "; form=" + compForm);
		}

		if (compForm == null)
			return;

		String nameKey = compForm.getStringParameter("title");
		if (nameKey != null) {
			List<Label> names = dest.getName();
			for (Locale loc : locales) {
				compForm.setTranslationLocale(loc.toString());
				names.add(new Label(compForm.translate(nameKey), loc.toString()));
			}
		}

		String descKey = compForm.getStringParameter("description");
//		if (descKey == null) {
//			descKey = "CONN_DESC";
//		}
		if (descKey != null) {
			List<Label> desc = dest.getDescription();
			for (Locale loc : locales) {
				compForm.setTranslationLocale(loc.toString());
				String str = compForm.translate(descKey);
				if(str != null)
					desc.add(new Label(compForm.translate(descKey), loc.toString()));
			}
		}
	}

	/**
	 * Generates a {@link FunctionComponentDescriptor} based on the tdi.xml for
	 * the FC specified by the provided parameters.
	 * 
	 * @param connId
	 *            the FC name under which the {@link FunctionConfig} object is
	 *            mapped in the {@link MetamergeConfig}
	 * @param cfg
	 *            the config object corresponding to the provided name.
	 * @return the FC descriptor of the specified {@link FunctionConfig}
	 * @throws Exception
	 */
	public static FunctionComponentDescriptor getFunctionComponentDescriptor(String connId, FunctionConfig cfg) throws Exception {
		FormConfig form = getFormForConfig(cfg.getJavaClass(), cfg);

		FunctionComponentDescriptor result = new FunctionComponentDescriptor();
		setComponentData(result, connId, cfg.getJavaClass(), form);

		fillInParameterMap(result.getParameterMapDescriptor(), form, null);

		return result;
	}

	/**
	 * Generates a {@link ParserDescriptor} based on the tdi.xml for the parser
	 * specified by the provided parameters.
	 * 
	 * @param connId
	 *            the parser name under which the {@link ParserConfig} object is
	 *            mapped in the {@link MetamergeConfig}
	 * @param cfg
	 *            the config object corresponding to the provided name.
	 * @return the parser descriptor of the specified {@link ParserConfig}
	 * @throws Exception
	 */
	public static ParserDescriptor getParserDescriptor(String connId, ParserConfig cfg) throws Exception {
		FormConfig form = getFormForConfig(cfg.getJavaClass(), cfg);

		ParserDescriptor result = new ParserDescriptor();
		setComponentData(result, connId, cfg.getJavaClass(), form);

		fillInParameterMap(result.getParameterMapDescriptor(), form, null);

		return result;
	}

	/**
	 * @param translationFile
	 * @return
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	private static Map<Locale, String> getAvailableLocalesFor(String translationFile) throws URISyntaxException, IOException {
		Map<Locale, String> locales = null;
		URL url = ResourceLocator.getResourceURL(translationFile + RESOURCE_BUNDLE_EXTENTION);

		if (url != null && ("zip".equalsIgnoreCase(url.getProtocol()) || "jar".equalsIgnoreCase(url.getProtocol()))) {
			String f = url.getFile();
			URI uri = new URI(f.substring(0, f.lastIndexOf('!')));
			ZipFile zf = new ZipFile(new File(uri));
			locales = getAvailableLocalesFromZip(translationFile, zf);
		}

		if (locales == null) {
			throw new IllegalStateException();
		}

		return locales;
	}

	/**
	 * @param translationFile
	 * @param zf
	 * @return
	 */
	private static Map<Locale, String> getAvailableLocalesFromZip(String translationFile, ZipFile zf) {
		Map<Locale, String> locales = new HashMap<Locale, String>();
		Locale loc = Locale.ENGLISH;
		locales.put(loc, toRFC1766String(loc));

		Enumeration<? extends ZipEntry> entries = zf.entries();
		if (entries != null) {
			while (entries.hasMoreElements()) {
				String name = entries.nextElement().getName();
				if (name.startsWith(translationFile) && name.charAt(translationFile.length()) == '_'
						&& name.endsWith(RESOURCE_BUNDLE_EXTENTION)) {
					loc = parseLocale(name.substring(translationFile.length() + 1, name.length()
							- RESOURCE_BUNDLE_EXTENTION.length()));
					locales.put(loc, toRFC1766String(loc));
				}
			}
		}

		return locales;
	}

	/**
	 * @param base
	 * @param nextElement
	 * @return
	 */
	private static Locale parseLocale(String locale) {
		int pos = locale.indexOf('_');
		String l = pos != -1 ? locale.substring(0, pos) : locale;
		String c = pos != -1 ? locale.substring(pos + 1) : null;

		return c != null ? new Locale(l, c) : new Locale(l);
	}

	private static void fillInParameters(List<ParameterDescriptor> params, FormConfig form) throws IOException, URISyntaxException {

		// cash up the global resource bundles
		Map<Locale, ResourceBundle> gRes = new HashMap<Locale, ResourceBundle>();
//		for (Locale loc : supportedLocales.keySet()) {
//			gRes.put(loc, ResourceBundle.getBundle(globalForm.getTranslationFile(), loc));
//		}

		// cash up the form's resource bundles
		Map<Locale, ResourceBundle> res = new HashMap<Locale, ResourceBundle>();
//		for (Locale loc : supportedLocales.keySet()) {
//			res.put(loc, ResourceBundle.getBundle(form.getTranslationFile(), loc));
//		}
		
		// 
		// -- Some forms include items in section names but not in the complete item list.
		//
		ArrayList<String> list = new ArrayList<String>();
		for(String str : form.getSectionNames()) {
			if(form.getSection(str) != null) {
				for(String str2 : form.getSection(str).getNames()) {
					if(!list.contains(str2))
						list.add(str2);
				}
			}
		}
		
		// now add remainder of items (not specified in sections or form has no sections)
		for(String str :form.getFormItemNames()) {
			if(!list.contains(str))
				list.add(str);
		}

		for (String str : list) {
			FormItemConfig formItem;
			if (str.startsWith(GLOBAL_PREFIX)) {
				if (globalForm != null) {
					formItem = globalForm.getFormItem(str.substring(GLOBAL_PREFIX.length()));
					if(formItem != null) {
						ParameterDescriptor param = new ParameterDescriptor();
						fillInParameter(param, formItem, gRes);
						params.add(param);
					}
				}
			} else {
				formItem = form.getFormItem(str);
				if(formItem != null) {
					ParameterDescriptor param = new ParameterDescriptor();
					fillInParameter(param, formItem, res);
					params.add(param);
				}
			}
		}
	}

	private static void fillInParameter(ParameterDescriptor param, FormItemConfig cfg, Map<Locale, ResourceBundle> res) {
		param.setKey(cfg.getShortName());
		param.setDefaultValue(cfg.getDefaultValue());
		param.setHidden(false);
		param.setRequired(cfg.isRequired());

		// Need form and getForm() is not in the interface def
		FormConfig form = null;
		try {
			form = (FormConfig) cfg.getClass().getMethod("getForm").invoke(cfg);
		} catch (Exception e) {
			// this should never happen
			e.printStackTrace();
		}

		String label = cfg.getStringParameter(InternalSchema.FORM_LABEL);
		if (label != null) {
			List<Label> labels = param.getLabels();
			for (Entry<Locale, String> loc : supportedLocales.entrySet()) {
				form.setTranslationLocale(loc.getKey().toString());
				labels.add(new Label(form.translate(label), loc.getKey().toString()));
//				labels.add(new Label(translateString(res.get(loc.getKey()), label), loc.getValue()));
			}
		} else {
			param.getLabels().add(new Label(param.getKey()));
		}

		String desc = cfg.getStringParameter(InternalSchema.FORM_TOOLTIP);
		if (desc != null) {
			List<Label> descs = param.getDescriptions();
			for (Entry<Locale, String> loc : supportedLocales.entrySet()) {
				form.setTranslationLocale(loc.getKey().toString());
				descs.add(new Label(form.translate(desc), loc.getKey().toString()));
//				descs.add(new Label(translateString(res.get(loc.getKey()), desc), loc.getValue()));
			}
		}
		
		String modes = cfg.getStringParameter("modes");
		if(modes != null)
			param.setModes(modes);

		String syntax = cfg.getSyntax();
		syntax = syntax == null || syntax.trim().length() == 0 ? "string" : syntax;
		syntax = syntax.toLowerCase();

		if (SYNTAX_DROPLIST.equals(syntax) || SYNTAX_DROPEDIT.equals(syntax)) {
			syntax = "string";

			if (cfg.getValues() != null && cfg.getValues().size() > 0) {
				Map<String, String> locMap = getValuesLocalizationMap(cfg);
				List<Option> options = param.getOptions();
				String val = null;
				String locVal = null;

				for (int i = 0; i < cfg.getValues().size(); i++) {
					val = cfg.getValues().get(i);
					Option o = new Option();
					o.setValue(val);
					
					if (locMap != null && (locVal = locMap.get(val)) != null) {
						for (Entry<Locale, String> loc : supportedLocales.entrySet()) {
							form.setTranslationLocale(loc.getKey().toString());
							o.getLabels().add(new Label(form.translate(locVal), loc.getKey().toString()));
//							o.getLabels().add(new Label(translateString(res.get(loc.getKey()), locVal), loc.getValue()));
						}
					} else {
						o.getLabels().add(new Label(val));
					}
					options.add(o);
				}
			}
		}

		// -- index based
		param.setIndexBased(cfg.isIndexBased());
		
		// -- lead text
		param.setLeadText(cfg.getLeadText());
		
		String script = cfg.getScript();
		if(script != null && script.length() > 0) {
			param.setScript(script);
			
			label = cfg.getStringParameter(InternalSchema.FORM_SCRIPT_LABEL);
			if (label != null) {
				List<Label> labels = param.getScriptLabels();
				for (Entry<Locale, String> loc : supportedLocales.entrySet()) {
					form.setTranslationLocale(loc.getKey().toString());
					labels.add(new Label(form.translate(label), loc.getKey().toString()));
				}
			} else {
				param.getScriptLabels().add(new Label(param.getKey()));
			}
		}
		
		String script2 = cfg.getScript2();
		if(script2 != null && script2.length() > 0) {
			param.setScript2(script2);
			label = cfg.getStringParameter(InternalSchema.FORM_SCRIPT_LABEL+"2");
			if (label != null) {
				List<Label> labels = param.getScriptLabels2();
				for (Entry<Locale, String> loc : supportedLocales.entrySet()) {
					form.setTranslationLocale(loc.getKey().toString());
					labels.add(new Label(form.translate(label), loc.getKey().toString()));
				}
			} else {
				param.getScriptLabels2().add(new Label(param.getKey()));
			}
		}
		
		String panel = cfg.getStringParameter("panel");
		if(panel != null && panel.length() > 0) {
			param.setPanel(panel);
		}
		
		param.setNoLabel(cfg.getBooleanParameter("noLabel", false));
		
		param.setType(syntax);

	}

	private static String translateString(ResourceBundle res, String key) {
		try {
			return res.getString(key);
		} catch (MissingResourceException me) {
			return key;
		}
	}

	/**
	 * Represents the locale in RFC 1766 form, i.e. using "-" as separator not
	 * "_".
	 * 
	 * @param loc
	 * @return
	 */
	private static String toRFC1766String(Locale loc) {
		return loc.getLanguage() + (loc.getCountry().length() > 0 ? "-" + loc.getCountry() : "");
	}

	/**
	 * Gets the mapping between an option's key to it's l10n key.
	 * 
	 * @param cfg
	 *            the parameter configuration
	 * @return map which has all the options for this parameter where the key is
	 *         the option's identifier and the value is the NLS key.
	 */
	@SuppressWarnings("unchecked")
	private static Map<String, String> getValuesLocalizationMap(FormItemConfig cfg) {
		Object o = cfg.getParameter(InternalSchema.FORM_LOCALIZEDVALUES);
		Map<String, String> result = null;
		if (o instanceof Map) {
			result = (Map<String, String>) o;
		} else if (o instanceof List) {
			result = new HashMap<String, String>();
			List<String> list = (List) o;
			for (String s : list) {
				int i = s.indexOf(':');
				if (i > 0) {
					result.put(s.substring(0, i), s.substring(i + 1));
				}
			}
		}
		return result;
	}

	/**
	 * Returns the supported modes for the connector
	 * 
	 * @param config
	 * @return
	 */
	private static String[] getConnectorModes(ConnectorConfig config) {
		try {
			String className = config.getConnectionConfig().getJavaClass();
			Class<?> cls = Class.forName(className);
			Object conn = cls.newInstance();
			if (conn instanceof Connector) {
				Vector<String> modes = ((Connector) conn).getModes(config);
				return modes.toArray(new String[modes.size()]);
			}
		} catch (Throwable e) {
			return Connector.ALL_MODES;
		}
		return Connector.ALL_MODES;
	}

	private static FormConfig getFormForConfig(String javaClass, BaseConfiguration cfg) throws Exception {
		
		//
		// -- Custom embedded form
		//
		if(cfg instanceof ConnectorConfig) {
			ConnectorConfig cc = (ConnectorConfig) cfg;
			String embeddedForm = cc.getConnectionConfig().getStringParameter("$form$");
			if(embeddedForm != null && embeddedForm.trim().length() > 0) {
				Hashtable<String, Object> env = new Hashtable<String, Object>();
				env.put(MetamergeConfigFactory.MC_URL, embeddedForm.trim().getBytes("UTF-8"));
				try {
					return (FormConfig) new MetamergeConfigXML(env).lookup("Form");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		try {
			return (FormConfig) MetamergeConfigFactory.getNamespace(MetamergeConfigFactory.SYSTEM_NAMESPACE).lookup(
					"/Forms/" + javaClass);
		} catch (NameNotFoundException nfn) {
			try {
				return (FormConfig) MetamergeConfigFactory.getNamespace(MetamergeConfigFactory.SYSTEM_NAMESPACE).lookup(
						"/Forms/" + javaClass + "." + cfg.getName());
			} catch (NameNotFoundException e) {

				return null;
			}
		}
	}

	private static FormConfig getGlobalForm() throws Exception {
		return (FormConfig) MetamergeConfigFactory.getNamespace(MetamergeConfigFactory.SYSTEM_NAMESPACE)
				.lookup("/Forms/__GLOBAL__");
	}

	/**
	 * Produces XML for the provided {@link BaseDescriptor}
	 * 
	 * @param desc
	 *            the {@link BaseDescriptor} to serialize as XML
	 * @return the XML as String
	 * @throws JAXBException
	 */
	public static String toXMLString(BaseDescriptor desc) throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance(DescriptorUtils.class.getPackage().getName());
		Marshaller m = jc.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		StringWriter sw = new StringWriter();
		m.marshal(desc, sw);
		return sw.toString();
	}
}
