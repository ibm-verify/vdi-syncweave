/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.eclipse.core.resources.IFolder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ScriptConfig;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.function.UserFunctions;
import com.ibm.di.server.StashFile;
import com.ibm.di.util.FileUtils;
import com.ibm.di.util.PropertiesFile;
import com.ibm.tdi.eclipse.Activator;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.log.EclipseAppender;

public class ServerUtils {
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public static final String TDI_WORKDIR = "workdir"; //$NON-NLS-1$

	public static final String TDI_INSTALL = "install"; //$NON-NLS-1$

	public static final String TDI_ADDRESS = "address"; //$NON-NLS-1$

	public static final String TDI_API = "apion"; //$NON-NLS-1$

	public static final String TDI_SSL = "ssl"; //$NON-NLS-1$

	/**
	 * Sets the TDI_WORKDIR parameter on the configuration based on the TDI_SOLDIR environment variable or
	 * the "set tdi_soldir=*" from the batch file (ibmdisrv). If the solution directory is "." it also creates
	 * the solution directory files in the current working directory. 
	 * 
	 * @param sc
	 * @param path
	 * @throws Exception
	 */
	public static void updateSoldirFromDirectory(ScriptConfig sc, String path) throws Exception {

		String soldir = System.getenv("TDI_SOLDIR"); //$NON-NLS-1$
		if (soldir == null) {
			File win = new File(path, "ibmdisrv.bat"); //$NON-NLS-1$
			File unx = new File(path, "ibmdisrv"); //$NON-NLS-1$
			if (win.exists())
				soldir = updateSoldirFromFile(sc, win);
			else if (unx.exists())
				soldir = updateSoldirFromFile(sc, unx);
		}

		if (soldir != null) {
			if (soldir.startsWith("\"") || soldir.startsWith("'")) //$NON-NLS-1$ //$NON-NLS-2$
				soldir = soldir.substring(1, soldir.length() - 1);
			if (soldir.equals(".")) //$NON-NLS-1$
				soldir = createServerWorkDir(sc.getShortName());

			sc.setParameter(TDI_WORKDIR, soldir);
		}
	}

	/**
	 * Returns the solution directory setting from the ibmdisrv batch file (if it's set there)
	 * @param sc
	 * @param f
	 * @return
	 */
	private static String updateSoldirFromFile(ScriptConfig sc, File f) {
		BufferedReader inp = null;
		try {
			String pat = "set tdi_soldir="; //$NON-NLS-1$
			inp = new BufferedReader(new FileReader(f));
			String str;
			while ((str = inp.readLine()) != null) {
				if (str.trim().toLowerCase().startsWith(pat)) {
					return str.trim().substring(pat.length());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (inp != null) {
				try {
					inp.close();
				} catch (Exception ignore) {
				}
			}
		}
		return null;
	}

	/**
	 * Creates a directory in the workspace servers project and returns the full path to it.
	 * 
	 * @param shortName
	 * @return
	 * @throws Exception
	 */
	private static String createServerWorkDir(String shortName) throws Exception {
		IFolder file = Utils.getTDIServersProject(true).getFolder(
				shortName + com.ibm.tdi.eclipse.Messages.getString("ServerUtils.18")); //$NON-NLS-1$
		if (!file.exists())
			file.create(true, true, null);

		return file.getLocation().toOSString();
	}

	/**
	 * Reads current values from a solution directory. Solution directory is
	 * either a directory with "solution.properties" or an install directory
	 * with "etc/global.properties" present.
	 * 
	 * @param config
	 *            Configuration to receive current values
	 * @return true if configuration was updated, false if directory is not a
	 *         tdi solution directory
	 * @throws Exception
	 */
	public static boolean readSolutionDirectory(BaseConfiguration config) throws Exception {
		String install = config.getStringParameter(RestServerAPI.TDI_INSTALL);
		String workdir = config.getStringParameter(RestServerAPI.TDI_WORKDIR);

		config.setModified(false);

		// -- Read global properties first
		if (install != null && install.length() > 0) {
			readConnectionProperties(config, new File(install, "etc/global.properties")); //$NON-NLS-1$
		}

		// -- Then solution properties
		if (workdir != null && workdir.length() > 0) {
			readConnectionProperties(config, new File(workdir, "solution.properties")); //$NON-NLS-1$
		}

		return config.getModified();
	}

	/**
	 * Reads the connection parameters from the provided file. File must be a
	 * properties format file (e.g. global.properties).
	 * 
	 * @param config
	 *            Config to receive values from properties file
	 * @param file
	 *            File to read
	 * @throws Exception
	 */
	private static void readConnectionProperties(BaseConfiguration config, File file) throws Exception {
		if (file.exists()) {
			PropertiesFile propsFile = new PropertiesFile(null, file.getAbsolutePath(), true);
			config.setStringParameter(RestServerAPI.TDI_TYPE, RestServerAPI.TYPE_RMI);
			String namingPort = propsFile.getProperty("api.remote.naming.port"); //$NON-NLS-1$ 
			if (namingPort != null && namingPort.length() > 0) {
				String address = config.getStringParameter(RestServerAPI.TDI_ADDRESS);
				if (address == null || address.length() == 0)
					address = "localhost:";
				else if (address.indexOf(':') < 0)
					address += ":";
				else
					address = address.substring(0, address.lastIndexOf(':') + 1);
				config.setStringParameter(RestServerAPI.TDI_ADDRESS, address + namingPort);
			}
			String useSSL = propsFile.getProperty("api.remote.ssl.on"); //$NON-NLS-1$
			if (useSSL != null && useSSL.length() > 0)
				config.setStringParameter(RestServerAPI.TDI_SSL, useSSL);
		}
	}

	/**
	 * Creates or updates the solution directory based on the values in the
	 * configuration. See ({@link RestServerAPI} for parameter names. If working
	 * directory points to an installation directory we update the
	 * etc/global.properties.
	 * 
	 * @param config
	 *            The configuration values for the solution directory
	 * @return true if solution directory was created, false if only files were
	 *         updated
	 */
	public static boolean createSolutionDirectory(BaseConfiguration config) throws Exception {
		File installDir = new File(config.getStringParameter(RestServerAPI.TDI_INSTALL));
		File workDir = new File(config.getStringParameter(RestServerAPI.TDI_WORKDIR));
		File sp = new File(workDir, "solution.properties"); //$NON-NLS-1$
		boolean created = !workDir.exists();

		// -- Sanity check
		if (workDir.exists() && workDir.isFile()) {
			throw new Exception(Messages.getMessage("ServerUtils.is.a.file", workDir.getAbsolutePath())); //$NON-NLS-1$
		} else if ((!workDir.exists()) && (!workDir.mkdirs())) {
			throw new Exception(Messages.getMessage("ServerUtils.cannot.create.directory", workDir.getAbsolutePath())); //$NON-NLS-1$
		}

		// -- Make sure workDir has solution.properties and other files/dirs
		copySolutionDirectoryFiles(installDir, workDir);

		//
		// -- Read the contents of the current solution properties file
		// -- In 7.0 and 7.1 we always copy global to solution even though installdir=workdir
		//
		String contents = Utils.loadTextFile(sp);

		// -- Get the address and port from the template
		String address = config.getStringParameter(RestServerAPI.TDI_ADDRESS);
		String port = null;
		if (address != null && address.indexOf(":") != -1) //$NON-NLS-1$
			port = address.substring(address.lastIndexOf(":") + 1); //$NON-NLS-1$

		//
		// Local server api must be on
		//
		contents = contents.replaceAll("api.on=false", "api.on=true"); //$NON-NLS-1$ //$NON-NLS-2$
		if (contents.indexOf("\napi.on") == -1) { //$NON-NLS-1$
			contents += "\napi.on=true"; //$NON-NLS-1$
		}

		boolean ssl = config.getBooleanParameter(RestServerAPI.TDI_SSL, false);

		if (port == null || port.length() == 0)
			port = "1099"; //$NON-NLS-1$

		// Turn on remote api
		contents = contents.replaceAll("api.remote.on=false", "api.remote.on=true"); //$NON-NLS-1$ //$NON-NLS-2$
		if (contents.indexOf("\napi.remote.on") == -1) { //$NON-NLS-1$
			contents += "\napi.remote.on=true"; //$NON-NLS-1$
		}

		// Toggle SSL on/off
		if (contents.indexOf("\napi.remote.ssl.on=") == -1) //$NON-NLS-1$
			contents += "\napi.remote.ssl.on=true"; //$NON-NLS-1$
		String str = "\napi.remote.ssl.on=" + (ssl ? "false" : "true"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		String nstr = "\napi.remote.ssl.on=" + (ssl ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		contents = contents.replaceAll(str, nstr);

		if (contents.indexOf("\napi.remote.naming.port") != -1) { //$NON-NLS-1$
			str = "\napi.remote.naming.port=" + port; //$NON-NLS-1$
			contents = contents.replaceAll("\napi\\.remote\\.naming\\.port=\\d*", str); //$NON-NLS-1$
		} else {
			contents += "\napi.remote.naming.port=" + port; //$NON-NLS-1$
		}

		// -- Rewrite global/solution properties file
		FileWriter fw = new FileWriter(sp);
		try {
			fw.write(contents);
		} finally {
			fw.close();
		}

		updateActiveMQFile(workDir, config);

		return created;
	}

	/**
	 * Copies required files from the installation directory to the solution
	 * directory.
	 * 
	 * @param tdiDir
	 * @param file
	 * @throws Exception
	 */
	public static void copySolutionDirectoryFiles(File tdiDir, File file) throws Exception {

		UserFunctions uf = new UserFunctions();
		File source;
		File target;

		// solution.properties
		source = new File(tdiDir + "/etc/", "global.properties"); //$NON-NLS-1$ //$NON-NLS-2$
		target = new File(file, "solution.properties"); //$NON-NLS-1$
		if ( ! target.exists() && 
				UserFunctions.copyFile(source.getAbsolutePath(), target.getAbsolutePath(), false)) {
			EclipseAppender.loginfo(Messages.getMessage("ServerUtils.created", target.getAbsolutePath())); //$NON-NLS-1$
		}

		// -- No need to copy more if install directory is also the solution directory
		if(!tdiDir.equals(file)) {
			// the stash file (idisrv.sth)
			source = new File(tdiDir, StashFile.STASH_FILE_NAME);
			target = new File(file, StashFile.STASH_FILE_NAME);
			if (UserFunctions.copyFile(source.getAbsolutePath(), target.getAbsolutePath(), false))
				EclipseAppender.loginfo(Messages.getMessage("ServerUtils.created", target.getAbsolutePath())); //$NON-NLS-1$

			// Copy key store files
			for (String prop: new String[] {"api.keystore", "api.truststore", "com.ibm.di.server.keystore", "com.ibm.di.server.encryption.keystore"}) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				String name = System.getProperty(prop);
				if (name == null)
					name = "testserver.jks"; //$NON-NLS-1$
				source = new File(tdiDir, name);
				target = new File(file, name); 
				if (source.exists() && ! target.exists())
					UserFunctions.copyFile(source.getAbsolutePath(), target.getAbsolutePath(), false);	
			}

			// copy the etc directory into the soln dir
			File etcDir = new File(file, "etc"); //$NON-NLS-1$
			if (!etcDir.exists()) {
				uf.copyDirectory(tdiDir.getAbsolutePath() + File.separator + "etc", etcDir.getAbsolutePath(), true, true, null); //$NON-NLS-1$
				// remove this one as it tends to confuse people
				new File(file, "etc/global.properties").delete(); //$NON-NLS-1$
			}

			File apiDir = new File(file, "serverapi"); //$NON-NLS-1$
			if (!apiDir.exists()) {
				uf.copyDirectory(tdiDir.getAbsolutePath() + File.separator + "serverapi", apiDir.getAbsolutePath(), true, true, //$NON-NLS-1$
						null);
				//RTC Defect#10740
				File cryptoutils = null;
				if(System.getProperty("os.name").toLowerCase().contains("win")){
					cryptoutils = new File("serverapi"+File.separator+"cryptoutils.bat");
				} else {
					cryptoutils = new File("serverapi"+File.separator+"cryptoutils.sh");
				}
				if(cryptoutils.exists() && cryptoutils.isFile()){
					cryptoutils.delete();					
				}
			}
			// copy the osgi directory into the solution directory
			File osgiInstDir = new File(tdiDir, "osgi");
			if (osgiInstDir.exists()) {
				File osgiSolDir = new File(file, "osgi");

				if (!osgiSolDir.exists()) {
					FileUtils.mkdir(osgiSolDir);
				}

				File consoleIniInst = new File(osgiInstDir, "console.ini");
				File consoleIniSol = new File(osgiSolDir, "console.ini");
				if (consoleIniInst.exists())
					UserFunctions.copyFile(consoleIniInst, consoleIniSol, false);

				File launchIniInst = new File(osgiInstDir, "launch.ini");
				File launchIniSol = new File(osgiSolDir, "launch.ini");
				if (launchIniInst.exists())
					UserFunctions.copyFile(launchIniInst, launchIniSol, false);

				// copy the osgi directory into the solution directory
				File osgiConfigInstDir = new File(osgiInstDir, "configuration");
				if (osgiConfigInstDir.exists()) {
					File osgiConfigSolDir = new File(osgiSolDir, "configuration");

					if (!osgiConfigSolDir.exists()) {
						FileUtils.mkdir(osgiConfigSolDir);
					}

					File configIniInst = new File(osgiConfigInstDir, "config.ini");
					File configIniSol = new File(osgiConfigSolDir, "config.ini");
					if (configIniInst.exists())
						UserFunctions.copyFile(configIniInst, configIniSol, false);
				}
			}

			File scimDir = new File(file, "SCIM"); //$NON-NLS-1$
			if (!scimDir.exists()) {
				uf.copyDirectory(tdiDir.getAbsolutePath() + File.separator + "SCIM", scimDir.getAbsolutePath(), true, true, //$NON-NLS-1$
						null);
			}

		}

		// -- Create logs directory
		File logDir = new File(file, "logs"); //$NON-NLS-1$
		if (!logDir.exists() && !logDir.mkdirs())
			throw new Exception(Messages.getMessage("ServerUtils.cannot.create.directory", logDir.getAbsolutePath())); //$NON-NLS-1$

		// -- Create configs directory
		String configsDir = System.getProperty("api.config.folder");
		if(configsDir == null || configsDir.trim().length() == 0 )
			configsDir = "configs";
		File configs = new File(file, configsDir); //$NON-NLS-1$
		if (!configs.exists() && !configs.mkdirs())
			throw new Exception(Messages.getMessage("ServerUtils.cannot.create.directory", configs.getAbsolutePath())); //$NON-NLS-1$

		// -- Create libs directory
		File libs = new File(file, "libs"); //$NON-NLS-1$
		if (!libs.exists() && !libs.mkdirs())
			throw new Exception(Messages.getMessage("ServerUtils.cannot.create.directory", libs.getAbsolutePath())); //$NON-NLS-1$

	}

	/**
	 * Returns the TDI Server's RMI address based on the global properties in the current installation directory or "localhost:1099"
	 * if the global properties file was not found or did not contain the api.remote.naming.port property.
	 * 
	 * @param dir
	 * @return
	 */
	public static String getGlobalPropAddress() {
		BufferedReader inp = null;
		String port = System.getProperty("api.remote.naming.port");
		if (port == null) {
			try {
				File props = new File(Activator.getInstallPath(), "etc/global.properties");
				inp = new BufferedReader(new FileReader(props));
				String str;
				while ((str = inp.readLine()) != null) {
					if (str.startsWith("api.remote.naming.port") && str.indexOf('=') > 0) { //$NON-NLS-1$
						port = str.substring(str.indexOf('=')+1);
						Integer.parseInt(port);
						break;
					}
				}
			} catch (Exception nfe) {
				port = null;
			} finally {
				try {
					if (inp != null)
						inp.close();
				} catch (Exception ignore) {
					SystemFunctions.doNothing();				
				}
			}
		}
		if (port == null || port.trim().length() == 0)
			port = "1099";
		return "localhost:" + port;
	}

	private static void updateActiveMQFile(File workdir, BaseConfiguration config) {

		String mPort = config.getStringParameter("activeMQ.management.port");
		if (mPort != null && mPort.length() == 0)
			mPort = null;
		String tPort = config.getStringParameter("activeMQ.transport.port");
		if (tPort != null && tPort.length() == 0)
			tPort = null;
		if (mPort == null && tPort == null)
			return;

		File configFile = getActiveMQConfigFile(workdir);

		try {
			DocumentBuilder docbuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			XPath xpath = XPathFactory.newInstance().newXPath();
			Document xmldoc;

			FileInputStream is = new FileInputStream(configFile);
			try {
				xmldoc = docbuilder.parse(is);
			} finally {
				is.close();
			}

			if (mPort != null) {
				Node node = (Node) xpath.evaluate("//managementContext[@connectorPort]", xmldoc, XPathConstants.NODE);
				if (node instanceof Element) {
					Element elem = (Element) node;
					elem.setAttribute("connectorPort", mPort);
				}
			}

			if (tPort != null) {
				Node node = (Node) xpath.evaluate("//transportConnector[@name='openwire']", xmldoc, XPathConstants.NODE);
				if (node instanceof Element) {
					Element elem = (Element) node;
					String uri = elem.getAttribute("uri");
					if (uri != null && uri.indexOf(':') > 0) 
						elem.setAttribute("uri", uri.substring(0, uri.lastIndexOf(':') + 1) + tPort);
				}
			}

			Transformer t = TransformerFactory.newInstance().newTransformer();
			t.setOutputProperty(OutputKeys.INDENT, "yes");
			t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

			FileOutputStream fos = new FileOutputStream(configFile);
			try {
				t.transform(new DOMSource(xmldoc.getDocumentElement()), new StreamResult(fos));
			} finally {
				fos.close();
			}
		} catch (Exception e) {
			EclipseAppender.logerror(e.getLocalizedMessage(), e);
		}


	}


	private static File getActiveMQConfigFile(File workdir) {

		String file = null;

		try {
			PropertiesFile pf = new PropertiesFile(workdir.getAbsolutePath() + File.separator + "solution.properties", false);
			file = pf.getProperty("api.rest.jmsdriver.param.jms.broker");
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e);
		}

		if(file == null || file.length() == 0)
			file = System.getProperty("api.rest.jmsdriver.param.jms.broker");
		if (file != null && file.contains("xbean:"))
			file = file.substring(file.indexOf("xbean:") + 6);
		else
			file = null;

		if(file == null || file.length() == 0)
			file = "etc/activemq.xml";

		return new File(workdir, file);
	}
	
	public static void readFromActiveMQFile(BaseConfiguration config) {

		String workdir = config.getStringParameter(RestServerAPI.TDI_WORKDIR);
		if (workdir == null || workdir.length() == 0)
			return;
		File configFile = getActiveMQConfigFile(new File(workdir));
		if (!configFile.exists())
			return;

		try {
			DocumentBuilder docbuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document xmldoc;

			FileInputStream is = new FileInputStream(configFile);
			try {
				xmldoc = docbuilder.parse(is);
			} finally {
				is.close();
			}

			XPath xpath = XPathFactory.newInstance().newXPath();
			Node node = (Node) xpath.evaluate("//managementContext[@connectorPort]", xmldoc, XPathConstants.NODE);
			if (node instanceof Element) {
				Element elem = (Element) node;
				String port = elem.getAttribute("connectorPort");
				if (port != null)
					config.setStringParameter("activeMQ.management.port", port);
			}

			node = (Node) xpath.evaluate("//transportConnector[@name='openwire']", xmldoc, XPathConstants.NODE);
			if (node instanceof Element) {
				Element elem = (Element) node;
				String uri = elem.getAttribute("uri");
				if (uri != null && uri.indexOf(':') > 0) 
					config.setStringParameter("activeMQ.transport.port", uri.substring(uri.lastIndexOf(':') + 1) );
			}
		} catch (Exception e) {
			EclipseAppender.logerror(e.getLocalizedMessage(), e);
		}
	}

}
