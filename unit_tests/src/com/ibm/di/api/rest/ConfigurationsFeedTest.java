
package com.ibm.di.api.rest;

import static org.hamcrest.beans.HasProperty.*;
import static org.hamcrest.beans.HasPropertyWithValue.*;
import static org.hamcrest.beans.SamePropertyValuesAs.*;
import static org.hamcrest.collection.IsArray.*;
import static org.hamcrest.collection.IsArrayContaining.*;
import static org.hamcrest.collection.IsArrayContainingInAnyOrder.*;
import static org.hamcrest.collection.IsArrayContainingInOrder.*;
import static org.hamcrest.collection.IsArrayWithSize.*;
import static org.hamcrest.collection.IsCollectionWithSize.*;
import static org.hamcrest.collection.IsEmptyCollection.*;
import static org.hamcrest.collection.IsEmptyIterable.*;
import static org.hamcrest.collection.IsIn.*;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.*;
import static org.hamcrest.collection.IsIterableContainingInOrder.*;
import static org.hamcrest.collection.IsIterableWithSize.*;
import static org.hamcrest.collection.IsMapContaining.*;
import static org.hamcrest.core.AllOf.*;
import static org.hamcrest.core.AnyOf.*;
import static org.hamcrest.core.DescribedAs.*;
import static org.hamcrest.core.Is.*;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsAnything.*;
import static org.hamcrest.core.IsEqual.*;
import static org.hamcrest.core.IsInstanceOf.*;
import static org.hamcrest.core.IsNot.*;
import static org.hamcrest.core.IsNull.*;
import static org.hamcrest.core.IsSame.*;
import static org.hamcrest.number.IsCloseTo.*;
import static org.hamcrest.number.OrderingComparison.*;
import static org.hamcrest.object.HasToString.*;
import static org.hamcrest.object.IsCompatibleType.*;
import static org.hamcrest.object.IsEventFrom.*;
import static org.hamcrest.text.IsEmptyString.*;
import static org.hamcrest.text.IsEqualIgnoringCase.*;
import static org.hamcrest.text.IsEqualIgnoringWhiteSpace.*;
import static org.hamcrest.text.StringContainsInOrder.*;
import static org.hamcrest.xml.HasXPath.*;
import static org.junit.Assert.*;
import static org.junit.Assert.assertThat;

import java.util.List;

import javax.servlet.ServletException;

import com.ibm.di.web.common.atom.AtomEntry;
import com.ibm.di.web.common.atom.AtomFeed;
import org.junit.Test;

import com.ibm.di.api.DIException;
import com.ibm.di.api.bind.CreateConfig;
import com.ibm.di.api.rest.internal.AppConstants;
import com.ibm.di.config.bind.SolutionBinding;
import com.ibm.di.test.api.mock.ServerAPIMock;
import com.ibm.di.test.api.mock.ServerAPIMockBuilder;
import com.ibm.di.test.rest.UnitTestRestClientContext;
import com.ibm.di.test.utils.atom.AtomAppHelper;

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
public class ConfigurationsFeedTest extends UnitTestRestClientContext {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	@Test
	public void test_Recursive_Config_Folder_Traversal() throws Exception {
		setIServerAPIConnection(new ServerAPIMockBuilder().configDir("a\\b/c").configFile("a/b\\cfg.xml").build());

		// list "" and get directory "a"
		AtomFeed confDir = app.getConfigurationFeed();
		List<AtomEntry> confDirs = AtomAppHelper.getResourcesByCategoryFromFeed(confDir, AppConstants.CAT_CONFIG_DIR);
		assertThat(confDirs.size(), is(1));

		List<AtomEntry> confFiles = AtomAppHelper.getResourcesByCategoryFromFeed(confDir, AppConstants.CAT_CONFIG_FILE, true);
		assertThat(confFiles.size(), is(0));

		// list "a" and get directory "b"
		AtomFeed aDir = app.navigateToConfigDir("a");
		List<AtomEntry> aDirs = AtomAppHelper.getResourcesByCategoryFromFeed(aDir, AppConstants.CAT_CONFIG_DIR);
		assertThat(aDirs.size(), is(1));

		List<AtomEntry> aFiles = AtomAppHelper.getResourcesByCategoryFromFeed(aDir, AppConstants.CAT_CONFIG_FILE, true);
		assertThat(aFiles.size(), is(0));

		// list "a/b" and get directory "c" and file cfg.xml
		AtomFeed bDir = app.navigateToConfigDir("a/b");
		List<AtomEntry> bDirs = AtomAppHelper.getResourcesByCategoryFromFeed(bDir, AppConstants.CAT_CONFIG_DIR);
		assertThat(bDirs.size(), is(1));

		List<AtomEntry> bFiles = AtomAppHelper.getResourcesByCategoryFromFeed(bDir, AppConstants.CAT_CONFIG_FILE, false);
		assertThat(bFiles.size(), is(1));
	}

	@Test
	public void test_POST_Creates_A_One_Level_Config_File_No_Existing() throws Exception {
		ServerAPIMock mock = new ServerAPIMockBuilder().createCfg("cfg.xml", false).build();
		setIServerAPIConnection(mock);

		AtomFeed confDir = app.getConfigurationFeed();
		List<AtomEntry> confDirs = AtomAppHelper.getResourcesByCategoryFromFeed(confDir, AppConstants.CAT_CONFIG_DIR, true);
		assertThat(confDirs.size(), is(0));

		List<AtomEntry> confFiles = AtomAppHelper.getResourcesByCategoryFromFeed(confDir, AppConstants.CAT_CONFIG_FILE, true);
		assertThat(confFiles.size(), is(0));

		AtomEntry cfg = app.createConfigurationEntry(getCreateConfig("cfg.xml", false, false, false));

		mock.verifyMockCalls();

		confDir = app.getConfigurationFeed();
		confDirs = AtomAppHelper.getResourcesByCategoryFromFeed(confDir, AppConstants.CAT_CONFIG_DIR, true);
		assertThat(confDirs.size(), is(0));

		confFiles = AtomAppHelper.getResourcesByCategoryFromFeed(confDir, AppConstants.CAT_CONFIG_FILE);
		assertThat(confFiles.size(), is(1));
	}

	@Test(expected = ServletException.class)
	public void test_POST_Creates_A_One_Level_Config_File_Existing_Not_Overwriting() throws Exception {
		ServerAPIMock mock = new ServerAPIMockBuilder().configFile("cfg.xml").createCfg("cfg.xml", false).build();
		setIServerAPIConnection(mock);

		AtomEntry cfg = app.createConfigurationEntry(getCreateConfig("cfg.xml", false, false, false));
	}

	@Test
	public void test_POST_Creates_A_One_Level_Config_File_Existing_Overwriting() throws Exception {
		ServerAPIMock mock = new ServerAPIMockBuilder().configFile("cfg.xml").createCfg("cfg.xml", true).build();
		setIServerAPIConnection(mock);

		AtomFeed confDir = app.getConfigurationFeed();
		List<AtomEntry> confDirs = AtomAppHelper.getResourcesByCategoryFromFeed(confDir, AppConstants.CAT_CONFIG_DIR, true);
		assertThat(confDirs.size(), is(0));

		List<AtomEntry> confFiles = AtomAppHelper.getResourcesByCategoryFromFeed(confDir, AppConstants.CAT_CONFIG_FILE, true);
		assertThat(confFiles.size(), is(1));

		AtomEntry cfg = app.createConfigurationEntry(getCreateConfig("cfg.xml", true, false, false));

		mock.verifyMockCalls();

		confDir = app.getConfigurationFeed();
		confDirs = AtomAppHelper.getResourcesByCategoryFromFeed(confDir, AppConstants.CAT_CONFIG_DIR, true);
		assertThat(confDirs.size(), is(0));

		confFiles = AtomAppHelper.getResourcesByCategoryFromFeed(confDir, AppConstants.CAT_CONFIG_FILE);
		assertThat(confFiles.size(), is(1));
	}

	@Test
	public void test_POST_Creates_A_Multi_Level_With_Missing_Path() throws Exception {
		ServerAPIMock mock = new ServerAPIMockBuilder().configDir("a").createCfg("a/b/c/cfg.xml", false).build();
		setIServerAPIConnection(mock);

		AtomEntry cfg = app.createConfigurationEntry(getCreateConfig("a/b/c/cfg.xml", false, false, false));
		mock.verifyMockCalls();

		// list "" and get directory "a"
		AtomFeed confDir = app.getConfigurationFeed();
		List<AtomEntry> confDirs = AtomAppHelper.getResourcesByCategoryFromFeed(confDir, AppConstants.CAT_CONFIG_DIR);
		assertThat(confDirs.size(), is(1));

		List<AtomEntry> confFiles = AtomAppHelper.getResourcesByCategoryFromFeed(confDir, AppConstants.CAT_CONFIG_FILE, true);
		assertThat(confFiles.size(), is(0));

		// list "a" and get directory "b"
		AtomFeed aDir = app.navigateToConfigDir("a");
		List<AtomEntry> aDirs = AtomAppHelper.getResourcesByCategoryFromFeed(aDir, AppConstants.CAT_CONFIG_DIR);
		assertThat(aDirs.size(), is(1));

		List<AtomEntry> aFiles = AtomAppHelper.getResourcesByCategoryFromFeed(aDir, AppConstants.CAT_CONFIG_FILE, true);
		assertThat(aFiles.size(), is(0));

		// list "a/b" and get directory "c"
		AtomFeed bDir = app.navigateToConfigDir("a/b");
		List<AtomEntry> bDirs = AtomAppHelper.getResourcesByCategoryFromFeed(bDir, AppConstants.CAT_CONFIG_DIR);
		assertThat(bDirs.size(), is(1));

		List<AtomEntry> bFiles = AtomAppHelper.getResourcesByCategoryFromFeed(bDir, AppConstants.CAT_CONFIG_FILE, true);
		assertThat(bFiles.size(), is(0));

		// list "a/b/c" and get file cfg.xml
		AtomFeed cDir = app.navigateToConfigDir("a/b/c");
		List<AtomEntry> cDirs = AtomAppHelper.getResourcesByCategoryFromFeed(cDir, AppConstants.CAT_CONFIG_DIR, true);
		assertThat(cDirs.size(), is(0));

		List<AtomEntry> cFiles = AtomAppHelper.getResourcesByCategoryFromFeed(cDir, AppConstants.CAT_CONFIG_FILE);
		assertThat(cFiles.size(), is(1));
	}

	private static CreateConfig getCreateConfig(String path, boolean overwrite, boolean leaveCheckOut, Boolean encrypt) {
		CreateConfig cc = new CreateConfig();
		cc.setName(path);
		cc.setOverwrite(overwrite);
		cc.setLeaveCheckOut(leaveCheckOut);
		cc.setEncrypt(encrypt);
		cc.setSolution(new SolutionBinding());
		return cc;
	}
}
