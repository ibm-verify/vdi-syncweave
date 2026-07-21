package com.ibm.di.test.utils.func;

import static com.ibm.di.test.utils.atom.AtomUtils.atomCategoryComparator;
import static com.ibm.di.test.utils.atom.AtomUtils.containsInAnyOrder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.wink.common.model.app.AppCategories;
import org.apache.wink.common.model.app.AppCollection;
import org.apache.wink.common.model.app.AppService;
import org.apache.wink.common.model.app.AppWorkspace;

import com.ibm.di.api.APIEngine;
import com.ibm.di.api.remote.Session;
import com.ibm.di.api.remote.SessionFactory;
import com.ibm.di.security.Crypto;
import com.ibm.di.security.CryptoFactory;
import com.ibm.di.server.StashFile;
import com.ibm.di.test.utils.atom.AtomUtils;
import com.ibm.di.test.utils.ProcessRunner;
import com.ibm.di.tp.server.Constants;
import com.ibm.di.util.PropertiesFile;

/**
 * 
 */
public class TDIServer {

	public static final String BACKUP_FOLDER = "testbackup";

	public static final File TDI_INSTALL_CONFIG_FILE = new File("./tdi_install_dir.properties");

	public static final String TDI_INSTALL_CONFIG_FILE_ENCODING = "UTF-8";

	public static final int CONNECT_RETRY_COUNT = 30;

	public static final int CONNECT_DELAY_MILLIS = 2000;

	private File installDir;
	private File solutionDir;

	private File solutionPropertiesFile;
	private PropertiesFile solutionProperties;

	private ProcessRunner tdiServer = new ProcessRunner("TDIServer");

	private static PropertiesFile propsFile = null;

	public static File getDefaultInstallDir() throws Exception {

		String tdiInstallPath = null;
		if (propsFile != null) {
			tdiInstallPath = propsFile.getProperty("installdir");
		} else {
			if (TDI_INSTALL_CONFIG_FILE.exists()) {
				propsFile = new PropertiesFile(null, TDI_INSTALL_CONFIG_FILE.getCanonicalPath(), true);
				tdiInstallPath = propsFile.getProperty("installdir");
			}
		}
		if (tdiInstallPath != null && tdiInstallPath.trim().length() > 0) {
			File tdiInstallDir = new File(tdiInstallPath);
			if (!tdiInstallDir.exists() || !tdiInstallDir.isDirectory()) {
				throw new Exception("The TDI install directory '" + tdiInstallDir.getAbsolutePath() + "' specified in file '"
						+ TDI_INSTALL_CONFIG_FILE.getAbsolutePath() + "' does not exist.");
			}
			return tdiInstallDir;
		}

		// check if we are in unit_tests
		File currentDir = new File(".").getCanonicalFile();
		if (!"unit_tests".equals(currentDir.getName())) {
			throw new Exception(
					"The working folder of the CVT test runner must be <install_dir>/unit_tests. The current folder is: "
							+ currentDir.getAbsolutePath());
		}
		return currentDir.getParentFile();
	}

	public static File getDefaultSolutionDir() throws Exception {
		String tdiSolutionPath = null;
		if (propsFile != null) {
			tdiSolutionPath = propsFile.getProperty("solutiondir");
		} else {
			if (TDI_INSTALL_CONFIG_FILE.exists()) {
				propsFile = new PropertiesFile(null, TDI_INSTALL_CONFIG_FILE.getCanonicalPath(), true);
				tdiSolutionPath = propsFile.getProperty("solutiondir");
			}
		}
		if (tdiSolutionPath == null || tdiSolutionPath.trim().length() == 0) {
			File tdiInstallDir = getDefaultInstallDir();
			tdiSolutionPath = getSolutionDirFromScriptFile(new File(tdiInstallDir, "bin"));
			if (tdiSolutionPath != null) {
				if (tdiSolutionPath.startsWith("\"") && tdiSolutionPath.endsWith("\"")) {
					tdiSolutionPath = tdiSolutionPath.substring(1, tdiSolutionPath.length() - 1);
				}
			} else {
				tdiSolutionPath = tdiInstallDir.getCanonicalPath();
			}
		}

		File tdiSolutionDir = new File(tdiSolutionPath);
		if (!tdiSolutionDir.exists() || !tdiSolutionDir.isDirectory()) {
			throw new Exception("The TDI solution directory '" + tdiSolutionDir.getAbsolutePath() + "' specified in file '"
					+ TDI_INSTALL_CONFIG_FILE.getAbsolutePath() + "' does not exist.");
		}
		return tdiSolutionDir;
	}

	private static String getSolutionDirFromScriptFile(File tdiBinDir) throws Exception {
		String tdiSolutionPath = null;
		File propSolution = new File(tdiBinDir, "defaultSolDir.bat");
		if (propSolution.exists()) {
			PropertiesFile propSolutionDir = new PropertiesFile(null, propSolution.getCanonicalPath(), true);
			tdiSolutionPath = propSolutionDir.getProperty("set TDI_SOLDIR");
		} else {
			propSolution = new File(tdiBinDir, "defaultSolDir.sh");
			if (propSolution.exists()) {
				PropertiesFile propSolutionDir = new PropertiesFile(null, propSolution.getCanonicalPath(), true);
				tdiSolutionPath = propSolutionDir.getProperty("TDI_SOLDIR");
			}
		}
		return tdiSolutionPath;
	}

	public TDIServer() throws Exception {
		this(getDefaultInstallDir(), getDefaultSolutionDir());
	}

	public TDIServer(File installDir, File solutionDir) throws Exception {
		if (installDir == null) {
			throw new NullPointerException("Install directory must not be null.");
		}
		if (!installDir.exists()) {
			throw new IllegalArgumentException("Install directory does not exits: " + installDir.getAbsolutePath());
		}
		if (!installDir.isDirectory()) {
			throw new IllegalArgumentException("Install directory is not directory: " + installDir.getAbsolutePath());
		}
		if (solutionDir == null) {
			throw new NullPointerException("Solution directory must not be null.");
		}
		if (!solutionDir.exists()) {
			throw new IllegalArgumentException("Solution directory does not exits: " + solutionDir.getAbsolutePath());
		}
		if (!solutionDir.isDirectory()) {
			throw new IllegalArgumentException("Solution directory is not directory: " + solutionDir.getAbsolutePath());
		}

		this.installDir = installDir.getCanonicalFile();
		this.solutionDir = solutionDir.getCanonicalFile();

		setupSolutionFolder();
		backup();
		this.solutionPropertiesFile = new File(solutionDir, "solution.properties");
		this.solutionProperties = readServerProperties(solutionPropertiesFile, new File(solutionDir, "idisrv.sth"));
	}

	private PropertiesFile readServerProperties(File propsFile, File stashFile) throws Exception {

		// read once without decryption to find out the encryption settings
		PropertiesFile props = new PropertiesFile(null, propsFile.getAbsolutePath(), true);

		String keyStorePath = props.getProperty("com.ibm.di.server.encryption.keystore");
		String keyStoreType = props.getProperty("com.ibm.di.server.encryption.keystoretype");
		String transformation = props.getProperty("com.ibm.di.server.encryption.transformation");
		String keyAlias = props.getProperty("com.ibm.di.server.encryption.key.alias");

		Vector<String> passwords = StashFile.readPasswordsFromFile(stashFile.getAbsolutePath());

		String keyStorePass = passwords.get(0);
		String keyPass = passwords.size() > 1 ? passwords.get(1) : keyStorePass;

		File keyStoreFile = new File(keyStorePath);
		if (!keyStoreFile.isAbsolute()) {
			// resolve relative paths based on the solution folder
			keyStoreFile = new File(getSolutionDir(), keyStorePath);
		}

		Crypto decryptor = CryptoFactory.createCrypto(keyStoreFile.getAbsolutePath(), keyStorePass, keyStoreType, keyAlias,
				keyPass, transformation, null);

		// read again
		PropertiesFile propsDecrypted = new PropertiesFile(decryptor, propsFile.getAbsolutePath(), true);

		return propsDecrypted;
	}

	public File getInstallDir() {
		return installDir;
	}

	public File getSolutionDir() {
		return solutionDir;
	}

	private void configureDefaultJSSEKeystores() {
		File testadmin = new File(getInstallDir(), "serverapi/testadmin.jks");
		System.setProperty("javax.net.ssl.trustStore", testadmin.getAbsolutePath());
		System.setProperty("javax.net.ssl.trustStorePassword", "administrator");
		System.setProperty("javax.net.ssl.trustStoreType", "jks");
		System.setProperty("javax.net.ssl.keyStore", testadmin.getAbsolutePath());
		System.setProperty("javax.net.ssl.keyStorePassword", "administrator");
		System.setProperty("javax.net.ssl.keyStoreType", "jks");
	}

	public Session getServerAPISession() throws Exception {
		configureDefaultJSSEKeystores();
		SessionFactory sessionFactory = (SessionFactory) Naming.lookup("rmi://localhost:" + getRMIServerAPIPort()
				+ "/SessionFactory");
		Session session = sessionFactory.createSession();
		return session;
	}

	/**
	 * Sends a request to obtain the Service Document of the TP Server.
	 * 
	 * @return the service document or the default UNAUTHRORIZED page if
	 *         authentication is enabled.
	 * @throws Exception
	 */
	private byte[] getTouchpointServiceDocument() throws Exception {

		boolean useSSL = "true".equalsIgnoreCase(getProperty(APIEngine.PROP_API_REMOTE_SSL_ON));

		final int timeoutMillis = 60000;
		RequestConfig requestConfig = RequestConfig.custom()
				.setConnectTimeout(timeoutMillis)
				.setSocketTimeout(timeoutMillis)
				.build();
		
		CloseableHttpClient httpClient = HttpClients.custom()
				.setDefaultRequestConfig(requestConfig)
				.build();

		try {
			String url;
			if (useSSL) {
				configureDefaultJSSEKeystores();
				url = "https://localhost:" + getRestServerAPIPort() + "/tp/";
			} else {
				url = "http://localhost:" + getRestServerAPIPort() + "/tp/";
			}

			HttpGet get = new HttpGet(url);

			CloseableHttpResponse response = httpClient.execute(get);
			try {
				int responseCode = response.getStatusLine().getStatusCode();

				if (responseCode < 200
						|| (responseCode > 299 && !(responseCode == 401 && Boolean.parseBoolean(solutionProperties
								.getProperty("tp.server.auth"))))) {
					throw new IOException("REST API returned error response code " + responseCode + " for URL " + url);
				}

				return EntityUtils.toByteArray(response.getEntity());
			} finally {
				response.close();
			}
		} finally {
			httpClient.close();
		}
	}

	public String getProperty(String key) throws Exception {
		return solutionProperties.getProperty(key);
	}

	public void setProperty(String key, String value) throws Exception {
		System.out.println("Setting " + key + "=" + value);
		solutionProperties.setProperty(key, value);
		solutionProperties.store(solutionPropertiesFile.getAbsolutePath(), null, null);
	}

	private boolean isRMIServerAPIEnabled() throws Exception {
		return "true".equalsIgnoreCase(getProperty(APIEngine.PROP_API_ON))
				&& "true".equalsIgnoreCase(getProperty(APIEngine.PROP_API_REMOTE_ON));
	}

	private boolean isRestAPIEnabled() throws Exception {
		return "true".equalsIgnoreCase(getProperty(APIEngine.PROP_TP_SERVER_ON));
	}

	private int getRMIServerAPIPort() throws Exception {
		int port = Integer.parseInt(getProperty(APIEngine.PROP_API_REMOTE_NAMING_PORT));
		return port;
	}

	private int getRestServerAPIPort() throws Exception {
		int port = Integer.parseInt(getProperty("web.server.port"));
		return port;
	}

	private boolean isServerRunning() {
		return tdiServer.processRunning();
	}

	private void waitForRMIServerAPI(int retryCount, int retryDelayMillis) throws Exception {

		boolean connected = false;
		for (int i = 0; i < retryCount && !connected; ++i) {
			if (!isServerRunning()) {
				throw new Exception("The server process is dead.");
			}
			try {
				getServerAPISession();
				connected = true;
			} catch (RemoteException re) {
				;
			} catch (NotBoundException nbo) {
				;
			}
			Thread.sleep(retryDelayMillis);
		}

		if (!connected) {
			throw new Exception("Could not connect to the RMI Server API.");
		}
	}

	private void waitForRestServerAPI(int retryCount, int retryDelayMillis) throws Exception {

		boolean connected = false;
		for (int i = 0; i < retryCount && !connected; ++i) {
			if (!isServerRunning()) {
				throw new Exception("The server process is dead.");
			}
			String srvcXml = null;
			try {
				srvcXml = new String(getTouchpointServiceDocument());
				if (srvcXml.contains("html") && srvcXml != null && srvcXml.contains("UNAUTHORIZED")) {
					// known scenario used in
					// com.ibm.di.cvt71.tp.container.FN_39_TP_Server_Authentication_CVT
					connected = true;
					break;
				}

				AppService service = AtomUtils.deserializeService(srvcXml);
				if (service != null && service.getWorkspace() != null) {
					for (AppWorkspace wspace : service.getWorkspace()) {
						if (wspace.getCollection() != null) {
							for (AppCollection col : wspace.getCollection()) {
								if (col.getCategories() != null) {
									for (AppCategories cat : col.getCategories()) {
										if (containsInAnyOrder(atomCategoryComparator, true, true, cat.getCategory(),
												Constants.CAT_CONN_PROVIDER)) {
											connected = col.getHref() != null;
										}
									}
								}
							}
						}
					}
				}
			} catch (IOException io) {
				;
			}
			Thread.sleep(retryDelayMillis);
		}

		if (!connected) {
			throw new Exception("Could not connect to the REST Server API.");
		}
	}

	private void setupSolutionFolder() throws Exception {
		File solutionProps = new File(getSolutionDir(), "solution.properties");
		if (!solutionProps.exists()) {
			// if solution.properties does not exist, run "ibmdirsv -g" to
			// create it
			List<String> cmd = getServerExecutableCmd();
			cmd.add("-g");
			ProcessRunner runner = new ProcessRunner("TDIServer", getSolutionDir(), cmd);
			runner.startProcess();
			runner.waitForProcess();
			if (!solutionProps.exists()) {
				throw new Exception("Could not setup solution folder '" + solutionDir.getCanonicalPath() + "'");
			}
		}
	}

	private void copyFile(File src, File dst) throws Exception {
		FileInputStream inp = new FileInputStream(src);
		try {
			FileOutputStream out = new FileOutputStream(dst);
			try {
				int c;
				while ((c = inp.read()) != -1) {
					out.write(c);
				}
			} finally {
				out.close();
			}
		} finally {
			inp.close();
		}
	}

	private void backup() throws Exception {

		// backup solution.properties
		File backupDir = new File(getSolutionDir(), BACKUP_FOLDER);
		if (backupDir.exists() && !backupDir.isDirectory()) {
			throw new Exception("Could not create backup folder. File already exists " + backupDir.getCanonicalPath());
		}
		backupDir.mkdirs();

		File solutionProps = new File(getSolutionDir(), "solution.properties");
		File solutionPropsBackup = new File(backupDir, "solution.properties");

		copyFile(solutionProps, solutionPropsBackup);
	}

	private void restore() {
		File backupDir = new File(getSolutionDir(), BACKUP_FOLDER);
		File solutionProps = new File(getSolutionDir(), "solution.properties");
		File solutionPropsBackup = new File(backupDir, "solution.properties");
		try {
			copyFile(solutionPropsBackup, solutionProps);
			solutionPropsBackup.delete();
			backupDir.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private List<String> getServerExecutableCmd() {
		List<String> cmd = new ArrayList<String>();
		if (System.getProperty("os.name").toLowerCase().indexOf("windows") != -1) {
			cmd.add(new File(getInstallDir(), "ibmdisrv.bat").getAbsolutePath());
		} else {
			cmd.add(new File(getInstallDir(), "ibmdisrv").getAbsolutePath());
		}
		return cmd;
	}

	public void startServer() throws Exception {
		startServer(new String[] { "-d" });

		// wait until the RMI Server API becomes available
		if (isRMIServerAPIEnabled()) {
			waitForRMIServerAPI(CONNECT_RETRY_COUNT, CONNECT_DELAY_MILLIS);
		}

		// wait until the REST Server API becomes available
		if (isRestAPIEnabled()) {
			waitForRestServerAPI(CONNECT_RETRY_COUNT, CONNECT_DELAY_MILLIS);
		}
	}

	public void startServer(String[] args) throws Exception {
		// stop Server if we have started it
		stopServer();

		// start a new Server process
		List<String> cmd = getServerExecutableCmd();
		Collections.addAll(cmd, args);

		tdiServer.setCmd(cmd);
		tdiServer.setWorkDir(getSolutionDir());
		tdiServer.startProcess();
	}

	public void waitFor() throws Exception {
		if (tdiServer.processRunning()) {
			// wait for the server process to end
			// this means that all assembly lines have finished
			tdiServer.waitForProcess();
		}
	}

	public void stopServer() throws InterruptedException {
		if (tdiServer.processRunning()) {
			/*
			 * we have direct handle only to the shell process (cmd.exe) but not
			 * to its child java process, so we need to use the Server API to
			 * stop the Server
			 */
			try {
				getServerAPISession().shutDownServer(0);
			} catch (Exception e) {
				e.printStackTrace();
			}
			tdiServer.waitForProcess();
		}
	}

	public void close() {
		try {
			stopServer();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		restore();
	}

	/**
	 * @param output
	 * @param b
	 */
	public void redirectStdOut(File to, boolean append) {
		tdiServer.redirectStdOut(to, append);
	}

	/**
	 * @param output
	 * @param b
	 */
	public void redirectStdErr(File to, boolean append) {
		tdiServer.redirectStdErr(to, append);
	}
}
