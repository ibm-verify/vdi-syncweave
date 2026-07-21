
package com.ibm.di.test.tp;

import java.io.File;
import java.util.List;

import org.apache.http.client.config.RequestConfig;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.ibm.di.api.APIEngine;
import com.ibm.di.api.remote.impl.rmi.Constants;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.function.UserFunctions;
import com.ibm.di.test.http.FuncTestHttpClientContext;
import com.ibm.di.test.http.HttpClientContext;
import com.ibm.di.test.utils.FileRecorder;
import com.ibm.di.test.utils.func.PortProbe;
import com.ibm.di.test.utils.func.TDIServer;
import com.ibm.di.tp.server.config.TPServerConfig;
import com.ibm.di.tp.server.config.TPServerConfigFile;
import com.ibm.di.tp.server.config.node.TdiNodeConfig;
import com.ibm.di.util.FileUtils;

/**
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public class FuncTestTPClientContext {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final String ID_CUSTOM = "Custom.SCMP.Test";

	private static TDIServer tdi;
	private static int tpServerPort;

	private static final FuncTestHttpClientContext httpCtx = new FuncTestHttpClientContext();

	private static TPServerConfigFile tpConfigFile;

	private static FileRecorder recorder;

	protected static final String ID_NODE_REMOTE = "remoteNode";
	protected static final String ID_NODE_LOCAL = "localNode";

	protected static final String ID_TYPE_CONNECTOR = "system:/Connectors/ibmdi.ScriptConnector";

	protected static final String ID_TYPE_CUSTOM = "file:" + ID_CUSTOM;

	protected static final String ID_TYPE_INTERMEDIARY = "virtual://Intermediary";

	private static void prereqConfigureTPServerToStartWithTDI() throws Exception {
		tpServerPort = PortProbe.getAvailablePort();

		tdi.setProperty(APIEngine.PROP_TP_SERVER_ON, "true");
		tdi.setProperty("web.server.port", Integer.toString(tpServerPort));
		tdi.setProperty(Constants.PROP_API_REMOTE_SSL_ON, "false");
	}

	private static void prereqConfigureTPServerToHaveTwoNodes() throws Exception {
		List<TdiNodeConfig> cfgs = tpConfigFile.getTPServerConfig().getNodeConfigs().getTdiNodeConfigs();

		// remove them all.
		cfgs.clear();

		TdiNodeConfig localNode = TpAppHelper.createTdiNodeConfg("");
		localNode.setId(ID_NODE_LOCAL);
		localNode.setLocal(true);
		localNode.setUser(null);
		localNode.setPassword(null);
		localNode.setProviderHost("localhost");
		localNode.setProviderPort(PortProbe.getAvailablePort());

		TdiNodeConfig remoteNode = TpAppHelper.createTdiNodeConfg("");
		remoteNode.setId(ID_NODE_REMOTE);
		remoteNode.setLocal(false);
		remoteNode.setProviderHost("localhost");
		remoteNode.setProviderPort(PortProbe.getAvailablePort());
		remoteNode.setHost("localhost");
		remoteNode.setPort(Integer.parseInt(tdi.getProperty("api.remote.naming.port")));
		remoteNode.setUser(null);
		remoteNode.setPassword(null);

		cfgs.add(localNode);
		cfgs.add(remoteNode);
	}

	private static void prereqConfigureTPServerToHaveCustomTemplatesDir() {
		if (tpConfigFile.getTPServerConfig().getTemplateConfig().getCustomTemplatesDir() == null) {
			tpConfigFile.getTPServerConfig().getTemplateConfig().setCustomTemplatesDir("templates");
		}
	}

	private static void prereqCleanUpPersistenceDirectory() throws Exception {
		if (tpConfigFile.getTPServerConfig().getPersistenceConfig().getLocation() != null) {
			File toDelete = getFullPath(tdi.getSolutionDir(), tpConfigFile.getTPServerConfig().getPersistenceConfig().getLocation());
			if (toDelete.exists()) {
				FileUtils.deleteRecursively(toDelete);
			}
		}
	}

	private static void prereqCreateCustomTemplate() throws Exception {
		File tmplDir = getFullPath(tdi.getSolutionDir(), tpConfigFile.getTPServerConfig().getTemplateConfig()
				.getCustomTemplatesDir());

		if (!tmplDir.exists()) {
			tmplDir = recorder.recordCreateDir(tmplDir);
			tmplDir.mkdirs();
		}

		File tmplFile = new File(tmplDir, ID_CUSTOM + ".xml");
		recorder.recordModifyFile(tmplFile);

		File baseFile = getFullPath(tdi.getSolutionDir(), tpConfigFile.getTPServerConfig().getTemplateConfig().getBaseTemplate());

		if (!UserFunctions.copyFile(baseFile, tmplFile, true)) {
			throw new IllegalStateException("Error Copying file: "
					+ tpConfigFile.getTPServerConfig().getTemplateConfig().getBaseTemplate() + " to: " + tmplFile);
		}

		MetamergeConfig tmpl = (MetamergeConfig) MetamergeConfigFactory.loadNamespace(tmplFile.getAbsolutePath());

		ConnectorConfig conn = tmpl.getConnector("GenericServiceConnector");
		conn.setInheritsFromRef(ID_TYPE_CONNECTOR);

		tmpl.commitChanges(tmplFile.getAbsolutePath());
		tmpl.closeConfig();
	}

	/**
	 * @return
	 * @throws Exception
	 */
	private static TPServerConfigFile getTPServerConfigFile() throws Exception {
		String cfgPath = tdi.getProperty("tp.server.config");

		if (cfgPath == null) {
			cfgPath = "etc/tp.xml";
			tdi.setProperty("tp.server.config", cfgPath);
		}

		File cfgFile = getFullPath(tdi.getSolutionDir(), cfgPath);
		recorder.recordModifyFile(cfgFile);

		return new TPServerConfigFile(cfgFile);
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		tdi = new TDIServer();
		recorder = new FileRecorder(new File(tdi.getSolutionDir(), "tpbackup"));

		redirectStreams();

		prereqConfigureTPServerToStartWithTDI();
		httpCtx.setHttpRootUri("http://localhost:" + tpServerPort + "/tp/");

		tpConfigFile = getTPServerConfigFile();
		prereqConfigureTPServerToHaveTwoNodes();

		prereqConfigureTPServerToHaveCustomTemplatesDir();

		prereqCleanUpPersistenceDirectory();

		prereqCreateCustomTemplate();

		tpConfigFile.store();
		tdi.startServer();
	}

	/**
	 * @throws Exception
	 */
	private static void redirectStreams() throws Exception {
		// redirect stdOut
		File output = getFullPath(tdi.getSolutionDir(), "output.txt");
		output.delete();
		tdi.redirectStdOut(output, true);
		// redirect stdErr
		tdi.redirectStdErr(output, true);
	}

	@AfterClass
	public static void tearDownAfterClass() {
		tdi.close();
		tdi = null;
		tpConfigFile = null;
		recorder.rewind();
		recorder.destroy();
		recorder = null;
	}

	/**
	 * @return the httpctx
	 */
	public static HttpClientContext getClientContext() {
		return httpCtx;
	}

	private static File getFullPath(File baseDir, String relativeOrAbsolutePath) {
		File relativeOrAbsolute = new File(relativeOrAbsolutePath);
		if (!relativeOrAbsolute.isAbsolute()) {
			relativeOrAbsolute = new File(baseDir, relativeOrAbsolutePath);
		}

		return relativeOrAbsolute;
	}

	protected TPServerConfig getTPServerConfig() {
		return tpConfigFile.getTPServerConfig();
	}

	protected TDIServer getTDIServer() {
		return tdi;
	}
}
