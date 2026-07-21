package com.ibm.di.server;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import org.junit.Test;

import com.ibm.di.function.UserFunctions;
import com.ibm.di.test.utils.TestUtils;

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
public class ResourceHashTest {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	@Test
	public void test_ResourceHash_Is_Able_To_Find_Resource() throws Exception {
		ResourceHash resHash = ResourceHash.getHash("ResourceHashTest/resourcehash");
		assertThat(resHash.getString("key.name.1"), is("value1"));
	}

	@Test
	public void test_ResourceHash_Is_Not_Able_To_Find_Resource() throws Exception {
		File tmpDir = TestUtils.createTempDir();
		File resSrc = new File("resources/ResourceHashTest/resourcehash.properties");
		File resDst = new File(tmpDir, "missingResource.properties");
		UserFunctions.copyFile(resSrc, resDst, true);

		ResourceHash resHash = ResourceHash.getHash("missingResource");
		assertThat(resHash.getString("key.name.1"), is("key.name.1"));

		resDst.delete();
	}

	@Test
	public void test_ResourceHash_Is_Able_To_Find_Resource_Using_Context_Loader() throws Exception {
		File tmpDir = TestUtils.createTempDir();
		File resSrc = new File("resources/ResourceHashTest/resourcehash.properties");
		File resDst = new File(tmpDir, "newResource.properties");
		UserFunctions.copyFile(resSrc, resDst, true);

		// make sure the context loader has that new file on the classpath.
		URLClassLoader ucl = new URLClassLoader(new URL[] { tmpDir.toURI().toURL() });

		ClassLoader original = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(ucl);

		ResourceHash resHash = ResourceHash.getHash("newResource");
		assertThat(resHash.getString("key.name.1"), is("value1"));

		Thread.currentThread().setContextClassLoader(original);
		resDst.delete();
	}
}
