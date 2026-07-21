/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.component.base;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.naming.Binding;
import javax.naming.NameNotFoundException;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;

import com.ibm.di.component.IntegrationComponent;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.FormConfig;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.LogConfigItem;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.MetamergeFolder;
import com.ibm.di.config.interfaces.ParserConfig;

/**
 * Defines service properties:
 * <ul>
 * <li>comp.config - specifies a path to a MetamergeConfig xml defining two
 * elements - default config (under Functions, Connectors, Parsers folder) and a
 * form config (under Forms folder). Note: the form within the configuration
 * file must have the same name as the value of the "comp.id" property.</li>
 * <li>comp.class - specifies the Java class of this component. This will
 * override any class specified in the default config and will be used during
 * instantiation.</li>
 * <ul>
 * <br>
 * <br>
 * 
 * 
 * @since 7.2
 */
public class BaseIntegrationComponent implements IntegrationComponent {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;
	protected BaseConfiguration defaultConfig;
	protected FormConfig formConfig;
	protected Class<?> compClass;
	protected ServiceReference sr;

	protected void activate(final ComponentContext cc) throws Exception {
		sr = cc.getServiceReference();
		String mcRef = (String) cc.getProperties().get("comp.config");
		URL url = getUrlFromBundle(mcRef, cc.getBundleContext().getBundle());
		if (url == null) {
			throw new InvalidResourceReferenceException("comp.config");
		}
		try {
			MetamergeConfig mc = parserMetamergeConfig(url);
			defaultConfig = getDefaultConfig(mc);
			formConfig = getFormConfig(mc);
			ResourceBundleLoader resourceBundleLoader = AccessController.doPrivileged(new PrivilegedAction<ResourceBundleLoader>() {
				public ResourceBundleLoader run() {
					return new ResourceBundleLoader(cc.getBundleContext().getBundle());
				}
			});
			formConfig.setTranslationClassLoader(resourceBundleLoader);
		} catch (Exception ex) {
			throw new ResourceLoadException(url.toString(), ex);
		}

		String className = (String) cc.getProperties().get("comp.class");
		if (className != null) {
			compClass = cc.getBundleContext().getBundle().loadClass(className);
		}
	}

	/**
	 * @param mc
	 * @return
	 * @throws Exception
	 */
	protected FormConfig getFormConfig(MetamergeConfig mc) throws Exception {
		String connId = (String) getProperty("component.name");
		try {
			return (FormConfig) mc.lookup(MetamergeConfig.DEFAULT_FORM_FOLDER + "/" + connId);
		} catch (NameNotFoundException e) {
			return null;
		}
	}

	/**
	 * @param mc
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	protected BaseConfiguration getDefaultConfig(MetamergeConfig mc) throws Exception {
		Enumeration<Binding> folders = mc.list();
		while (folders.hasMoreElements()) {
			BaseConfiguration cfg = (BaseConfiguration) folders.nextElement().getObject();
			if (!MetamergeConfig.DEFAULT_FORM_FOLDER.equals(cfg.getShortName()) && cfg instanceof MetamergeFolder) {
				Enumeration<?> list = ((MetamergeFolder) cfg).list();
				while (list.hasMoreElements()) {
					Object obj = list.nextElement();
					if (obj instanceof Binding) {
						obj = ((Binding) obj).getObject();
					}
					return (BaseConfiguration) obj;
				}
			}
		}
		return null;
	}

	/**
	 * @param url
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	protected MetamergeConfig parserMetamergeConfig(URL url) throws Exception {
		Hashtable env = new Hashtable();
		env.put(MetamergeConfigFactory.MC_DRIVER, "com.ibm.di.config.xml.MetamergeConfigXML");
		env.put(MetamergeConfigFactory.MC_CREATE, false);
		env.put(MetamergeConfigFactory.MC_URL, url);
		return MetamergeConfigFactory.getInstance(env);
	}

	protected URL getUrlFromBundle(String mcRef, Bundle b) {
		URL url = b.getResource(mcRef);
		if (url == null) {
			url = getUrlByString(mcRef);
		}
		return url;
	}

	protected URL getUrlByString(String mcRef) {
		try {
			URI absCheck = new URI(mcRef);
			if (absCheck.isAbsolute()) {
				return absCheck.toURL();
			} else {
				File f = new File(mcRef);
				if (f.exists()) {
					return f.toURI().toURL();
				}
			}
		} catch (URISyntaxException e) {
			return null;
		} catch (MalformedURLException e) {
			return null;
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.component.IntegrationComponent#getDefaultConfig()
	 */
	public BaseConfiguration getDefaultConfig() {
		return defaultConfig;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.component.IntegrationComponent#getFormConfig()
	 */
	public FormConfig getFormConfig() {
		return formConfig;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.component.IntegrationComponent#newInstance(com.ibm.di.config
	 * .interfaces.BaseConfiguration)
	 */
	public Object newInstance() throws Throwable {
		if (compClass != null) {
			return compClass.newInstance();
		}

		String cls = getJavaClass(defaultConfig);
		if (cls == null) {
			String compId = (String) getProperty("component.name");
			throw new MissingJavaClassException(compId);
		}

		return Class.forName(cls).newInstance();
	}

	private String getJavaClass(BaseConfiguration config) {
		if (config instanceof ParserConfig) {
			return ((ParserConfig) config).getJavaClass();
		} else if (config instanceof FunctionConfig) {
			return ((FunctionConfig) config).getJavaClass();
		} else if (config instanceof ConnectorConfig) {
			return ((ConnectorConfig) config).getConnectionConfig().getJavaClass();
		} else if (config instanceof LogConfigItem) {
			return config.getStringParameter("com.ibm.di.log.interface");
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.component.IntegrationComponent#getProperty(java.lang.String)
	 */
	public Object getProperty(String key) {
		return sr.getProperty(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.component.IntegrationComponent#getPropertyKeys()
	 */
	public String[] getPropertyKeys() {
		return sr.getPropertyKeys();
	}

	protected static class InvalidResourceReferenceException extends Exception {

		private static final long serialVersionUID = -1454760552779664305L;

		public InvalidResourceReferenceException(String resource) {
			super(resource);
		}
	}

	protected static class ResourceLoadException extends Exception {

		private static final long serialVersionUID = -613243022376110732L;

		public ResourceLoadException(String resource, Exception ex) {
			super(resource, ex);
		}
	}

	protected static class MissingJavaClassException extends Exception {

		private static final long serialVersionUID = -1454760552779664305L;

		public MissingJavaClassException(String compId) {
			super(compId);
		}
	}

	protected static class ResourceBundleLoader extends ClassLoader {
		private final Bundle delegate;

		public ResourceBundleLoader(Bundle delegate) {
			this.delegate = delegate;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.ClassLoader#findResource(java.lang.String)
		 */
		@Override
		protected URL findResource(String resName) {
			return delegate.getResource(resName);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.ClassLoader#getResources(java.lang.String)
		 */
		@Override
		@SuppressWarnings("unchecked")
		public Enumeration<URL> getResources(String resName) throws IOException {
			return delegate.getResources(resName);
		}
	}
}
