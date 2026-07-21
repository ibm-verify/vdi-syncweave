
package com.ibm.di.cvt71.tp.container;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import com.ibm.di.api.APIEngine;
import com.ibm.di.test.CVTComponent;
import com.ibm.di.test.CVTTest;
import com.ibm.di.test.http.FuncTestHttpClientContext;
import com.ibm.di.test.tp.TpAppHelper;
import com.ibm.di.test.utils.TestUtils;
import com.ibm.di.test.utils.func.PortProbe;
import com.ibm.di.test.utils.func.TDIServer;
import com.ibm.di.util.FileUtils;

/**
 * Tests the authentication capabilities of the TP Server's default container
 * (jetty).
 * 
 * @since 7.1
 */
@CVTComponent(name = "tpserver")
public class FN_39_TP_Server_Authentication_CVT {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final String USER_NAME = "CN=SDI, OU=Security, O=IBM, L=test, ST=Test, C=US";

	private static final String USER_PASS = "mypass";

	private static final String SCRIPT_TDI_AUTH = //
	"" + //
			"main.logmsg(\"Authenticating User: \" + userdata.username);" + //
			"if (userdata.username == \"" + USER_NAME + "\" && userdata.password == \"" + USER_PASS + "\") {" + //
			"ret.auth = true;" + //
			"main.logmsg(\"User authentication successful.\");" + //
			"} else {" + //
			"ret.auth = false;" + //
			"ret.errordescr = \"Wrong username or password!\";" + //
			"main.logmsg(\"User authentication failed.\");" + //
			"}";

	private static TDIServer tdi;
	private static int remoteApiPort;
	private static int tpServerPort;
	private static URL keyStore;
	private static URL trustStore;
	private static String keyStorePass;
	private static String trustStorePass;

	private static File tempDir;
	private static File authScriptFile;

	private FuncTestHttpClientContext client;

	@BeforeClass
	public static void beforeTestClass() throws Exception {
		remoteApiPort = PortProbe.getAvailablePort();
		tpServerPort = PortProbe.getAvailablePort();

		tdi = new TDIServer();
		tdi.setProperty(APIEngine.PROP_API_REMOTE_ON, "true");
		tdi.setProperty(APIEngine.PROP_API_REMOTE_NAMING_PORT, Integer.toString(remoteApiPort));
		tdi.setProperty(APIEngine.PROP_API_REMOTE_SSL_ON, "true");
		tdi.setProperty(APIEngine.PROP_API_REMOTE_SSL_CLIENT_AUTH_ON, "true");
		tdi.setProperty(APIEngine.PROP_TP_SERVER_ON, "true");
		tdi.setProperty("web.server.port", Integer.toString(tpServerPort));
		tdi.setProperty("tp.server.auth", "false");

		String storePath = tdi.getProperty("javax.net.ssl.keyStore");
		keyStore = storePath != null ? new File(tdi.getSolutionDir(), storePath).toURI().toURL() : null;
		keyStorePass = tdi.getProperty("javax.net.ssl.keyStorePassword");

		storePath = tdi.getProperty("javax.net.ssl.trustStore");
		trustStore = storePath != null ? new File(tdi.getSolutionDir(), storePath).toURI().toURL() : null;
		trustStorePass = tdi.getProperty("javax.net.ssl.trustStorePassword");

		prepareAuthScript();
	}

	private static void prepareAuthScript() throws IOException {
		tempDir = TestUtils.createTempDir();
		authScriptFile = new File(tempDir, "authScript.js");

		FileWriter writer = new FileWriter(authScriptFile);
		try {
			writer.write(SCRIPT_TDI_AUTH);
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	@AfterClass
	public static void afterTestClass() {
		if (tdi != null) {
			tdi.close();
			tdi = null;
		}

		try {
			FileUtils.deleteRecursively(tempDir);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Before
	public void beforeTest() {
		client = new FuncTestHttpClientContext();
		client.setHttpRootUri("https://localhost:" + tpServerPort + "/tp");
	}

	@After
	public void afterTest() throws Exception {
		if (tdi != null) {
			tdi.stopServer();
		}
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Authentication_TC01")
	public void test_HTTP_Client_Authenticates_Successfully_To_TPServer_With_SSL_Client_Auth() throws Exception {
		tdi.startServer();
		SSLContext ctx = createSSLContext(keyStore, keyStorePass, trustStore, trustStorePass);
		Socket s = ctx.getSocketFactory().createSocket("localhost", tpServerPort);
		s.getOutputStream().write(111);
	}

	@Test(expected = IOException.class)
	@CVTTest(name = "CVT_FN-39_TP_Server_Authentication_TC02")
	public void test_HTTP_Client_Cannot_Authenticate_To_TPServer_With_SSL_Client_Auth() throws Exception {
		tdi.startServer();
		SSLContext ctx = createSSLContext(new File("resources/tp/server/invalid.jks").toURI().toURL(), "secret", trustStore,
				trustStorePass);
		Socket s = ctx.getSocketFactory().createSocket("localhost", tpServerPort);
		s.getOutputStream().write(111);
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Authentication_TC03")
	public void test_HTTP_Client_Authenticates_Successfully_To_TPServer_With_Basic_Auth() throws Exception {
		tdi.setProperty("tp.server.auth", "true");
		tdi.setProperty("api.custom.authentication", authScriptFile.getAbsolutePath());
		tdi.startServer();

		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(
				new AuthScope("localhost", tpServerPort),
				new UsernamePasswordCredentials(USER_NAME, USER_PASS));
		client.setCredentialsProvider(credsProvider);

		MockHttpServletResponse resp = new TpAppHelper(client).getServiceDocument();
		TpAppHelper.checkSuccess(resp);
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Authentication_TC04")
	public void test_HTTP_Client_Cannot_Authenticate_To_TPServer_With_Basic_Auth() throws Exception {
		tdi.setProperty("tp.server.auth", "true");
		tdi.setProperty("api.custom.authentication", authScriptFile.getAbsolutePath());
		tdi.startServer();

		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(
				new AuthScope("localhost", tpServerPort),
				new UsernamePasswordCredentials(USER_NAME, "invalidPass"));
		client.setCredentialsProvider(credsProvider);

		MockHttpServletResponse resp = new TpAppHelper(client).getServiceDocument();
		assertThat(resp.getErrorMessage(), resp.getStatus(), is(401));
	}

	private static SSLContext createSSLContext(URL ks, String ksPass, URL ts, String tsPass) throws IOException {
		try {
			KeyManager[] keymanagers = null;
			TrustManager[] trustmanagers = null;
			KeyStore keystore = null;

			if (ks != null) {
				keystore = createKeyStore(ks, ksPass);
				keymanagers = createKeyManagers(keystore, ksPass);
			}

			if (ts != null) {
				keystore = createKeyStore(ts, tsPass);
				trustmanagers = createTrustManagers(keystore);
			}

			SSLContext sslcontext = SSLContext.getInstance("TLSv1");
			sslcontext.init(keymanagers, trustmanagers, null);
			return sslcontext;
		} catch (NoSuchAlgorithmException e) {
			throw new IOException("Unsupported algorithm exception: " + e.getMessage(), e);
		} catch (KeyStoreException e) {
			throw new IOException("Keystore exception: " + e.getMessage(), e);
		} catch (GeneralSecurityException e) {
			throw new IOException("Key management exception: " + e.getMessage(), e);
		} catch (IOException e) {
			throw new IOException("I/O error reading keystore/truststore file: " + e.getMessage(), e);
		}
	}

	private static KeyStore createKeyStore(final URL url, final String password) throws KeyStoreException,
			NoSuchAlgorithmException, CertificateException, IOException {
		if (url == null) {
			throw new IllegalArgumentException("Keystore url may not be null");
		}
		KeyStore keystore = KeyStore.getInstance("jks");
		InputStream is = null;
		try {
			is = url.openStream();
			keystore.load(is, password != null ? password.toCharArray() : null);
		} finally {
			if (is != null)
				is.close();
		}
		return keystore;
	}

	private static KeyManager[] createKeyManagers(final KeyStore keystore, final String password) throws KeyStoreException,
			NoSuchAlgorithmException, UnrecoverableKeyException {
		if (keystore == null) {
			throw new IllegalArgumentException("Keystore may not be null");
		}
		KeyManagerFactory kmfactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		kmfactory.init(keystore, password != null ? password.toCharArray() : null);
		return kmfactory.getKeyManagers();
	}

	private static TrustManager[] createTrustManagers(final KeyStore keystore) throws KeyStoreException, NoSuchAlgorithmException {
		if (keystore == null) {
			throw new IllegalArgumentException("Keystore may not be null");
		}
		TrustManagerFactory tmfactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmfactory.init(keystore);
		return tmfactory.getTrustManagers();
	}
}
