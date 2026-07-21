/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.osgi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.Manifest;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NameNotFoundException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.FormConfig;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.ParserConfig;
import com.ibm.di.config.xml.MetamergeConfigXML.LazyConfig;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.util.DOMUtils;

/**
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.2
 */
public class RuntimeEnvironment {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private RuntimeEnvironment() {
	}

	public static void attachIntegrationComponentConfigs(MetamergeConfig mc) {
		OSGiContainerHandle handle = OSGiContainerHandle.getHandle(false);
		if (handle != null) {
			// Used when running in an osgi environment
			findICsUsingServiceRegistry(mc, handle);
		} else {
			// Used when running outside of an osgi environment
			findICsUsingFileSystem(mc);
		}
	}

	/**
	 * @param mc
	 */
	private static void findICsUsingServiceRegistry(MetamergeConfig mc, OSGiContainerHandle osgi) {
		try {
			// make sure the components are started first.
			osgi.startBundle("com.ibm.di.component");
			Object[] services = osgi.getServices("com.ibm.di.component.ConnectorComponent", null);
			processServiceConfigs(mc, services);
			services = osgi.getServices("com.ibm.di.component.FunctionComponent", null);
			processServiceConfigs(mc, services);
			services = osgi.getServices("com.ibm.di.component.ParserComponent", null);
			processServiceConfigs(mc, services);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private static void processServiceConfigs(MetamergeConfig mc, Object[] services) throws NoSuchMethodException,
			IllegalAccessException, InvocationTargetException, Exception {
		if (services == null || services.length == 0) {
			return;
		}
		Method meth;
		BaseConfiguration config;
		for (Object serv : services) {
			meth = serv.getClass().getMethod("getDefaultConfig", (Class[]) null);
			config = (BaseConfiguration) meth.invoke(serv, (Object[]) null);
			if (config != null) {
				config = (BaseConfiguration) config.getClone();
				config.setMetamergeConfig(mc);
				mc.rebind(config.getName(), config);
				if (config instanceof ConnectorConfig) {
					config.setupInheritanceChain();
				}
			}

			meth = serv.getClass().getMethod("getFormConfig", (Class[]) null);
			config = (BaseConfiguration) meth.invoke(serv, (Object[]) null);
			if (config != null) {
				config = (BaseConfiguration) config.getClone();
				config.setMetamergeConfig(mc);
				mc.rebind(config.getName(), config);
			}
		}
	}

	/**
	 * @param mc
	 */
	private static void findICsUsingFileSystem(MetamergeConfig mc) {
		File pluginsDir = new File(System.getProperty("com.ibm.di.installdir"), "osgi/plugins");
		if (!pluginsDir.exists()) {
			return;
		}

		List<URL> compXml = getComponentXmls(pluginsDir);
		for (URL comp : compXml) {
			InputStream is = null;
			try {
				is = comp.openStream();
				Document doc = DOMUtils.getDOMParser().parse(is);
				NodeList provides = doc.getElementsByTagName("provide");
				for (int i = 0; i < provides.getLength(); i++) {
					Node ifaceNode = provides.item(i).getAttributes().getNamedItem("interface");
					if (ifaceNode != null
							&& (("com.ibm.di.component.ConnectorComponent".equals(ifaceNode.getNodeValue()))
									|| "com.ibm.di.component.FunctionComponent".equals(ifaceNode.getNodeValue()) || "com.ibm.di.component.ParserComponent"
									.equals(ifaceNode.getNodeValue()))) {

						String compId = doc.getDocumentElement().getAttribute("name");
						if (compId != null) {
							registerComponent(mc, ifaceNode.getNodeValue(), compId);
						}
					}
				}
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private static void registerComponent(MetamergeConfig mc, String serviceIface, String compId) throws InvalidNameException,
			Exception {
		Class<?> cfgIface = null;
		String folder = getDefaultFolder(serviceIface);

		if (MetamergeConfig.DEFAULT_CONNECTOR_FOLDER.equals(folder)) {
			cfgIface = ConnectorConfig.class;
		} else if (MetamergeConfig.DEFAULT_FUNCTION_FOLDER.equals(folder)) {
			cfgIface = FunctionConfig.class;
		} else if (MetamergeConfig.DEFAULT_PARSER_FOLDER.equals(folder)) {
			cfgIface = ParserConfig.class;
		}

		Name fullName = MetamergeConfigFactory.parseName(folder + "/" + compId);
		mc.bind(fullName, Proxy.newProxyInstance(RuntimeEnvironment.class.getClassLoader(), new Class[] { cfgIface },
				new OSGiLazyComponentConfig(serviceIface, fullName, compId, true)));

		fullName = MetamergeConfigFactory.parseName("Forms/" + compId);
		mc.bind(fullName, Proxy.newProxyInstance(RuntimeEnvironment.class.getClassLoader(), new Class[] { FormConfig.class },
				new OSGiLazyComponentConfig(serviceIface, fullName, compId, false)));
	}

	private static List<URL> getComponentXmls(File pluginsDir) {
		Pattern commaPat = Pattern.compile(",");
		List<URL> compFiles = new LinkedList<URL>();
		for (File plugin : pluginsDir.listFiles()) {
			try {
				Manifest mf = openManifest(plugin);
				if (mf != null) {
					String svcComp = mf.getMainAttributes().getValue("Service-Component");
					if (svcComp != null) {
						String[] comps = commaPat.split(svcComp);
						for (String comp : comps) {
							if ((comp = comp.trim()).length() > 0) {
								if (plugin.isDirectory()) {
									compFiles.add(new File(plugin, comp).toURI().toURL());
								} else if (plugin.getName().endsWith(".jar") || plugin.getName().endsWith(".zip")) {
									compFiles.add(new URL("jar:" + plugin.toURI() + "!/" + comp));
								}
							}
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return compFiles;
	}

	/**
	 * @param plugin
	 * @return
	 * @throws IOException
	 */
	private static Manifest openManifest(File plugin) throws IOException {
		Manifest mf = null;
		if (plugin.isDirectory()) {
			File mfFile = new File(plugin, "META-INF/MANIFEST.MF");
			if (mfFile.exists()) {
				FileInputStream fis = new FileInputStream(mfFile);
				try {
					mf = new Manifest(fis);
				} finally {
					if (fis != null) {
						fis.close();
					}
				}
			}
		} else if (plugin.isFile() && (plugin.getName().endsWith(".jar") || plugin.getName().endsWith(".zip"))) {
			ZipFile zf = new ZipFile(plugin);
			ZipEntry entry = zf.getEntry("META-INF/MANIFEST.MF");
			if (entry != null) {
				InputStream is = null;
				try {
					is = zf.getInputStream(entry);
					mf = new Manifest(is);
				} finally {
					if (is != null) {
						is.close();
					}
				}
			}

		}
		return mf;
	}

	private static String getDefaultFolder(String componentInterface) {
		if ("com.ibm.di.component.ConnectorComponent".equals(componentInterface)) {
			return MetamergeConfig.DEFAULT_CONNECTOR_FOLDER;
		} else if ("com.ibm.di.component.FunctionComponent".equals(componentInterface)) {
			return MetamergeConfig.DEFAULT_FUNCTION_FOLDER;
		} else if ("com.ibm.di.component.ParserComponent".equals(componentInterface)) {
			return MetamergeConfig.DEFAULT_PARSER_FOLDER;
		}
		return null;
	}

	private static class OSGiLazyComponentConfig extends LazyConfig {

		private final String compId;

		private final boolean defaultCfg;

		private final String serviceIface;

		private volatile Object lazyObj;

		public OSGiLazyComponentConfig(String serviceIface, Name fullName, String compId, boolean defaultCfg) {
			super(fullName);
			this.serviceIface = serviceIface;
			this.compId = compId;
			this.defaultCfg = defaultCfg;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.ibm.di.config.xml.MetamergeConfigXML.LazyConfig#loadConfig()
		 */
		@Override
		public void loadConfig() throws Throwable {
			if (lazyObj == null) {
				synchronized (this) {
					if (lazyObj == null) {
						OSGiContainerHandle handle = OSGiContainerHandle.getHandle(true);
						handle.startBundle("com.ibm.di.component");
						Object[] services = handle.getServices(serviceIface, "(component.name=" + compId + ")");
						if (services == null || services.length == 0 || services[0] == null) {
							throw new NameNotFoundException(compId);
						}
						if (defaultCfg) {
							lazyObj = services[0].getClass().getMethod("getDefaultConfig", (Class[]) null).invoke(services[0],
									(Object[]) null);
						} else {
							lazyObj = services[0].getClass().getMethod("getFormConfig", (Class[]) null).invoke(services[0],
									(Object[]) null);
							if (lazyObj == null) {
								// when the default config is inherited and no
								// form is provided walk up the hierarchy and
								// find the first formConfig of one of the
								// parents.
								MetamergeConfig system = MetamergeConfigFactory
										.getNamespace(MetamergeConfigFactory.SYSTEM_NAMESPACE);
								try {
									BaseConfiguration cfg = (BaseConfiguration) system.lookup(getDefaultFolder(serviceIface) + "/"
											+ compId);
									for (BaseConfiguration bc = cfg; bc != null && lazyObj == null; bc = bc.getInheritsFrom()) {
										try {
											lazyObj = system.lookup("Forms/" + bc.getShortName());
										} catch (Exception e) {
											// Continue searching.
											SystemFunctions.doNothing();
										}
									}
								} catch (Exception e) {
									SystemFunctions.doNothing();
								}
							}
						}

						if (lazyObj == null) {
							throw new NullPointerException(getName().toString());
						}

						lazyObj = ((BaseConfiguration) lazyObj).getClone();

						((BaseConfiguration) lazyObj).setMetamergeConfig(MetamergeConfigFactory
								.getNamespace(MetamergeConfigFactory.SYSTEM_NAMESPACE));
						((BaseConfiguration) lazyObj).setName(getName());
						((BaseConfiguration) lazyObj).setModified(getModified());
						((BaseConfiguration) lazyObj).setupInheritanceChain();
					}
				}
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
		 * java.lang.reflect.Method, java.lang.Object[])
		 */
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if (method.getParameterTypes().length == 0) {
				if ("getName".equals(method.getName())) {
					return getName();
				} else if ("getShortName".equals(method.getName())) {
					return compId;
				} else if ("getModified".equals(method.getName()) && !isLoaded()) {
					return getModified();
				} else if ("setupInheritanceChain".equals(method.getName()) && !isLoaded()) {
					// skip that if not loaded yet. Will do it the first time
					// the object is loaded
					return null;
				}
			} else if (method.getParameterTypes().length == 1) {
				if ("setName".equals(method.getName())) {
					setName(args[0]);
					return null;
				} else if ("setModified".equals(method.getName()) && args[0] instanceof Boolean) {
					setModified((Boolean) args[0]);
					return null;
				}
			}

			loadConfig();
			return method.invoke(lazyObj, args);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.ibm.di.config.xml.MetamergeConfigXML.LazyConfig#isLoaded()
		 */
		@Override
		public boolean isLoaded() {
			synchronized (this) {
				return lazyObj != null;
			}
		}
	}
}
