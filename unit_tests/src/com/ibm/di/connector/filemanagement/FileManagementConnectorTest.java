
package com.ibm.di.connector.filemanagement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ibm.di.config.base.ConnectorConfigImpl;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.connector.ConnectorInterface;
import com.ibm.di.connector.filemanagement.FileManagementConnector;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.Log;
import com.ibm.di.server.SearchCriteria;
import com.ibm.di.test.utils.RSMock;
import com.ibm.di.test.utils.TestUtils;

public class FileManagementConnectorTest {
	public static final String COMPONENT_NAME = "FileManagementConnector";

	/**
	 * Component parameter Start Directory Path name.
	 */
	private static final String START_DIRECTORY_PARAM_NAME = "startDirectory";

	/**
	 * Component parameter Maximum Depth name.
	 */
	private static final String DEPTH_PARAM_NAME = "depth";

	/**
	 * Component parameter User RegEx Flag name.
	 */
	private static final String REGULAR_EXPRESSION_PARAM_NAME = "regexFilter";

	/**
	 * Component parameter File List Filter name.
	 */
	private static final String FILTER_PARAM_NAME = "filter";

	/**
	 * Component parameter Return Follow Symbolics Links Flag name.
	 */
	private static final String FOLLOW_SYMBOLICS_LINK_PARAM_NAME = "followSymbolicLinks";

	/**
	 * Component parameter Return File Only Flag name.
	 */
	private static final String FILE_ONLY_PARAM_NAME = "fileOnly";

	/**
	 * Component parameter Force Delete Flag name.
	 */
	private static final String FORCE_DELETE_PARAM_NAME = "forceDelete";

	/**
	 * Component parameter Keep Original Files or Directories Flag name.
	 */
	private static final String KEEP_ORIGINAL_PARAM_NAME = "keepOriginal";
	
	/**
	 * Component parameter Create File Flag name.
	 */
	private static final String CREATE_FILE_PARAM_NAME = "createFile";

	/**
	 * Entry attribute containing the file's local name.
	 */
	private static final String NAME_ATTR = "name";

	/**
	 * Entry attribute containing the file's parent path.
	 */
	private static final String PARENT_ATTR = "parent";

	/**
	 * Entry attribute containing Read Only flag.
	 */
	private static final String IS_READ_ONLY_ATTR = "isReadOnly";

	/**
	 * Entry attribute determining if a file is hidden or not.
	 */
	private static final String IS_HIDDEN_ATTR = "isHidden";

	/**
	 * Entry attribute used for distinguishing files from directories.
	 */
	private static final String IS_DIRECTORY_ATTR = "isDirectory";

	/**
	 * Entry attribute containing the full path to the file.
	 */
	private static final String FULL_PATH_ATTR = "fullPath";
	

	/**
	 * Entry attribute containing the content to by written.
	 */
	private static final String CONTENT_ATTR = "content";
	

	/**
	 * Entry attribute containing the char set of the content.
	 */
	private static final String CHAR_SET_ATTR = "charSet";

	public static final char FILE_SEP = File.separatorChar;
	public static final String RESOURCES = "resources";
	public static final String FMC = "FileMngmntConnector";
	public static final String RESOURCES_PLUS_FMC = RESOURCES + FILE_SEP + FMC + FILE_SEP;

	public static final String INPUT_DIR = RESOURCES_PLUS_FMC + "Input";
	public static final String OUTPUT_DIR = RESOURCES_PLUS_FMC + "Output";
	public static final String DEFAULT_INPUT_DIR = RESOURCES_PLUS_FMC + "DefaultInput";

	@Test
	public void test_Create_Empty_Dir_By_Parent_And_Name_Attributes() throws Exception {
		String testDir = "TestDir";
		Entry entry = new Entry();
		entry.addAttributeValue(NAME_ATTR, testDir);
		entry.addAttributeValue(PARENT_ATTR, OUTPUT_DIR);
		entry.addAttributeValue(IS_DIRECTORY_ATTR, Boolean.TRUE);

		Map<String, String> params = new HashMap<String, String>();
		params.put(START_DIRECTORY_PARAM_NAME, INPUT_DIR);

		ConnectorInterface fmc = createConnector(ConnectorConfig.ADDONLY_MODE, params);
		fmc.initialize(null);
		fmc.putEntry(entry);
		fmc.terminate();

		File file = new File(OUTPUT_DIR, "testDir");
		assertTrue(file.exists());
		assertTrue(file.isDirectory());
	}

	@Test
	public void test_Create_File_By_Name_And_Read_Only_Attributes() throws Exception {
		String testFile = "TestFile";
		Entry entry = new Entry();
		entry.addAttributeValue(NAME_ATTR, testFile);
		entry.addAttributeValue(IS_DIRECTORY_ATTR, Boolean.FALSE);
		entry.addAttributeValue(IS_READ_ONLY_ATTR, Boolean.TRUE);

		Map<String, String> params = new HashMap<String, String>();
		params.put(START_DIRECTORY_PARAM_NAME, OUTPUT_DIR);

		ConnectorInterface fmc = createConnector(ConnectorConfig.ADDONLY_MODE, params);
		fmc.initialize(null);
		fmc.putEntry(entry);
		fmc.terminate();

		File file = new File(OUTPUT_DIR, testFile);
		assertTrue(file.exists());
		assertTrue(file.isFile());
		assertTrue(file.canRead());
		assertFalse(file.canWrite());
	}

	@Test
	public void test_Delete_Empty_Dir_And_File() throws Exception {
		String testDir = "EmptyDir";
		String testFile = "NormalFile";
		Map<String, String> params = new HashMap<String, String>();
		params.put(START_DIRECTORY_PARAM_NAME, INPUT_DIR);
		params.put(FOLLOW_SYMBOLICS_LINK_PARAM_NAME, "true");

		ConnectorInterface fmc = createConnector(ConnectorConfig.DELETE_MODE, params);
		fmc.initialize(null);

		File dirToDelete = new File(INPUT_DIR, testDir);
		SearchCriteria search = new SearchCriteria();
		search.addCriteria(FULL_PATH_ATTR, SearchCriteria.EXACT, dirToDelete.getCanonicalPath());
		Entry entry = fmc.findEntry(search);
		fmc.deleteEntry(entry, null);

		File fileToDelete = new File(INPUT_DIR, testFile);
		search.replaceCriteria(FULL_PATH_ATTR, fileToDelete.getCanonicalPath());
		entry = fmc.findEntry(search);
		fmc.deleteEntry(entry, null);
		fmc.terminate();

		assertFalse(dirToDelete.exists());
		assertFalse(fileToDelete.exists());
	}

	@Test
	public void test_Delete_Non_Empty_Dir_Without_Force_Delete() throws Exception {
		String testDir = "NonEmptyDir";
		Map<String, String> params = new HashMap<String, String>();
		params.put(START_DIRECTORY_PARAM_NAME, INPUT_DIR);
		params.put(FORCE_DELETE_PARAM_NAME, "false");
		params.put(FOLLOW_SYMBOLICS_LINK_PARAM_NAME, "true");

		ConnectorInterface fmc = createConnector(ConnectorConfig.DELETE_MODE, params);
		fmc.initialize(null);

		SearchCriteria search = new SearchCriteria();
		File dirToDelete = new File(INPUT_DIR, testDir);
		search.addCriteria(FULL_PATH_ATTR, SearchCriteria.EXACT, dirToDelete.getCanonicalPath());
		Entry entry = fmc.findEntry(search);
		boolean isExceptionThrown = false;
		try {
			fmc.deleteEntry(entry, null);
		} catch (Exception e) {
			isExceptionThrown = true;
		}
		fmc.terminate();

		assertTrue(isExceptionThrown);
		assertTrue(dirToDelete.exists());
		assertTrue(dirToDelete.list().length > 0);
	}

	@Test
	public void test_Delete_Read_Only_File_Without_Force_Delete() throws Exception {
		String testFile = "ReadOnlyFile";
		Map<String, String> params = new HashMap<String, String>();
		params.put(START_DIRECTORY_PARAM_NAME, INPUT_DIR);
		params.put(FORCE_DELETE_PARAM_NAME, "false");
		params.put(FOLLOW_SYMBOLICS_LINK_PARAM_NAME, "true");

		ConnectorInterface fmc = createConnector(ConnectorConfig.DELETE_MODE, params);
		fmc.initialize(null);

		SearchCriteria search = new SearchCriteria();
		File fileToDelete = new File(INPUT_DIR, testFile);
		fileToDelete.setReadOnly();
		search.addCriteria(FULL_PATH_ATTR, SearchCriteria.EXACT, fileToDelete.getCanonicalPath());
		Entry entry = fmc.findEntry(search);
		boolean isExceptionThrown = false;
		try {
			fmc.deleteEntry(entry, null);
		} catch (Exception e) {
			isExceptionThrown = true;
		}
		fmc.terminate();

		assertTrue(isExceptionThrown);
		assertTrue(fileToDelete.exists());
	}

	@Test
	public void test_Delete_Non_Empty_Dir_With_Force_Delete() throws Exception {
		String testDir = "NonEmptyDir";
		Map<String, String> params = new HashMap<String, String>();
		params.put(START_DIRECTORY_PARAM_NAME, INPUT_DIR);
		params.put(FORCE_DELETE_PARAM_NAME, "true");
		params.put(FOLLOW_SYMBOLICS_LINK_PARAM_NAME, "true");

		ConnectorInterface fmc = createConnector(ConnectorConfig.DELETE_MODE, params);
		fmc.initialize(null);

		SearchCriteria search = new SearchCriteria();
		File dirToDelete = new File(INPUT_DIR, testDir);
		search.addCriteria(FULL_PATH_ATTR, SearchCriteria.EXACT, dirToDelete.getCanonicalPath());
		Entry entry = fmc.findEntry(search);
		fmc.deleteEntry(entry, null);
		fmc.terminate();

		assertFalse(dirToDelete.exists());
	}

	@Test
	public void test_Delete_Read_Only_File_With_Force_Delete() throws Exception {
		String testFile = "ReadOnlyFile";
		Map<String, String> params = new HashMap<String, String>();
		params.put(START_DIRECTORY_PARAM_NAME, INPUT_DIR);
		params.put(FORCE_DELETE_PARAM_NAME, "true");
		params.put(FOLLOW_SYMBOLICS_LINK_PARAM_NAME, "true");

		ConnectorInterface fmc = createConnector(ConnectorConfig.DELETE_MODE, params);
		fmc.initialize(null);

		SearchCriteria search = new SearchCriteria();
		File fileToDelete = new File(INPUT_DIR, testFile);
		fileToDelete.setReadOnly();
		search.addCriteria(FULL_PATH_ATTR, SearchCriteria.EXACT, fileToDelete.getCanonicalPath());
		Entry entry = fmc.findEntry(search);
		fmc.deleteEntry(entry, null);
		fmc.terminate();

		assertFalse(fileToDelete.exists());
	}

	@Test
	public void test_Find_All_Files_And_Dir_In_Root_Start_Dir_With_Glob_One_Asterisk() throws Exception {
		String filter = "*.tbf";
		Map<String, String> params = new HashMap<String, String>();
		params.put(START_DIRECTORY_PARAM_NAME, INPUT_DIR);
		params.put(FILTER_PARAM_NAME, filter);
		params.put(FOLLOW_SYMBOLICS_LINK_PARAM_NAME, "true");

		ConnectorInterface fmc = createConnector(ConnectorConfig.ITERATOR_MODE, params);
		fmc.initialize(null);
		fmc.selectEntries();
		Entry entry = null;
		int count = 0;
		while ((entry = fmc.getNextEntry()) != null) {
			count++;
			assertTrue(entry.getAttribute(FULL_PATH_ATTR).getValue().endsWith(".tbf"));
		}
		fmc.terminate();

		assertEquals(count, 4);
	}

	@Test
	public void test_Find_All_Files_And_Dir_In_Start_Dir_With_Glob_Two_Asterisks() throws Exception {
		String filter = "**.tbf";
		Map<String, String> params = new HashMap<String, String>();
		params.put(START_DIRECTORY_PARAM_NAME, INPUT_DIR);
		params.put(FILTER_PARAM_NAME, filter);
		params.put(FOLLOW_SYMBOLICS_LINK_PARAM_NAME, "true");

		ConnectorInterface fmc = createConnector(ConnectorConfig.ITERATOR_MODE, params);
		fmc.initialize(null);
		fmc.selectEntries();
		Entry entry = null;
		int count = 0;
		while ((entry = fmc.getNextEntry()) != null) {
			count++;
			assertTrue(entry.getAttribute(FULL_PATH_ATTR).getValue().endsWith(".tbf"));
		}
		fmc.terminate();

		assertEquals(count, 7);
	}

	@Test
	public void test_Find_All_Files_And_Dir_In_Start_Dir_With_Regex_Filter() throws Exception {
		String filter = ".*file.*\\.tbf";
		Map<String, String> params = new HashMap<String, String>();
		params.put(START_DIRECTORY_PARAM_NAME, INPUT_DIR);
		params.put(FILTER_PARAM_NAME, filter);
		params.put(REGULAR_EXPRESSION_PARAM_NAME, "true");
		params.put(FOLLOW_SYMBOLICS_LINK_PARAM_NAME, "true");

		ConnectorInterface fmc = createConnector(ConnectorConfig.ITERATOR_MODE, params);
		fmc.initialize(null);
		fmc.selectEntries();
		Entry entry = null;
		int count = 0;
		String fullPath = null;
		boolean matchPattern = false;
		while ((entry = fmc.getNextEntry()) != null) {
			count++;
			fullPath = entry.getAttribute(FULL_PATH_ATTR).getValue();
			if (fullPath.endsWith(".tbf")) {
				matchPattern = true;
			}
			if (fullPath.contains("file")) {
				matchPattern = true;
			}

		}
		fmc.terminate();

		assertTrue(matchPattern);
		assertEquals(count, 4);
	}

	@Test
	public void test_Find_All_Files_And_Dirs_In_Level0_And_Level1_In_Start_Dir() throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		params.put(START_DIRECTORY_PARAM_NAME, INPUT_DIR);
		params.put(DEPTH_PARAM_NAME, "1");
		params.put(FOLLOW_SYMBOLICS_LINK_PARAM_NAME, "true");

		ConnectorInterface fmc = createConnector(ConnectorConfig.ITERATOR_MODE, params);
		fmc.initialize(null);
		fmc.selectEntries();
		int count = 0;
		while ((fmc.getNextEntry()) != null) {
			count++;
		}
		assertEquals(count, 18);
		fmc.terminate();
	}

	@Test
	public void test_Find_All_Files_And_Not_Dirs_In_All_Subdirs_In_Start_Dir() throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		params.put(START_DIRECTORY_PARAM_NAME, INPUT_DIR);
		params.put(FILE_ONLY_PARAM_NAME, "true");
		params.put(FOLLOW_SYMBOLICS_LINK_PARAM_NAME, "true");

		ConnectorInterface fmc = createConnector(ConnectorConfig.ITERATOR_MODE, params);
		fmc.initialize(null);
		fmc.selectEntries();
		Entry entry = null;
		int count = 0;
		boolean isDirectory = false;
		while ((entry = fmc.getNextEntry()) != null) {
			count++;
			isDirectory = (Boolean) entry.getObject(IS_DIRECTORY_ATTR);
		}
		fmc.terminate();

		assertFalse(isDirectory);
		assertEquals(count, 12);
	}

	/**
	 * This test uses platform dependent way to make a file hidden.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test_Find_Specific_File_With_Complex_Link_Criteria() throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		params.put(START_DIRECTORY_PARAM_NAME, INPUT_DIR);
		params.put(FOLLOW_SYMBOLICS_LINK_PARAM_NAME, "true");

		ConnectorInterface fmc = createConnector(ConnectorConfig.LOOKUP_MODE, params);
		fmc.initialize(null);

		File fileToFind = new File(INPUT_DIR, "NonEmptyDir" + FILE_SEP + ".HiddenFile.one");
		fileToFind.setReadOnly();
		setHidden(fileToFind);

		SearchCriteria search = new SearchCriteria();
		// search.addCriteria(NAME_ATTR, SearchCriteria.INITIAL_STRING,
		// ".Hidden");
		search.addCriteria(NAME_ATTR, SearchCriteria.FINAL_STRING, "one");
		search.addCriteria(IS_HIDDEN_ATTR, SearchCriteria.EXACT, "true");
		search.addCriteria(IS_READ_ONLY_ATTR, SearchCriteria.EXACT, "true");
		search.addCriteria(IS_DIRECTORY_ATTR, SearchCriteria.EXACT, "false");

		Entry entry = fmc.findEntry(search);

		assertNotNull(entry);
		assertTrue(entry.getAttribute(NAME_ATTR).getValue().equals(".HiddenFile.one"));
		assertTrue(((Boolean) entry.getObject(IS_HIDDEN_ATTR)));
		assertTrue(((Boolean) entry.getObject(IS_READ_ONLY_ATTR)));
		assertFalse(((Boolean) entry.getObject(IS_DIRECTORY_ATTR)));

		fmc.terminate();
	}

	@Test
	public void test_Copy_Dir_Content_To_Another_Dir() throws Exception {

		Map<String, String> params = new HashMap<String, String>();
		params.put(START_DIRECTORY_PARAM_NAME, INPUT_DIR);
		params.put(FOLLOW_SYMBOLICS_LINK_PARAM_NAME, "true");
		params.put(KEEP_ORIGINAL_PARAM_NAME, "true");

		ConnectorInterface fmc = createConnector(ConnectorConfig.UPDATE_MODE, params);
		fmc.initialize(null);

		File dirToCopy = new File(INPUT_DIR, "NonEmptyDir" + FILE_SEP + "DirToBeCopy");
		String dirCanonicalPath = dirToCopy.getCanonicalPath();

		SearchCriteria search = new SearchCriteria();
		search.addCriteria(FULL_PATH_ATTR, SearchCriteria.EXACT, dirCanonicalPath);

		String newDirCanonicalPath = dirToCopy.getCanonicalFile().getParent() + FILE_SEP + "NewDir1" + FILE_SEP + "NewDir2";
		File newDir = new File(newDirCanonicalPath);

		Entry newEntry = new Entry();
		newEntry.addAttributeValue(FULL_PATH_ATTR, newDirCanonicalPath);

		fmc.modEntry(newEntry, search);

		fmc.terminate();

		assertTrue(dirToCopy.exists());
		assertTrue(new File(dirToCopy, "Dir5").exists());
		assertTrue(new File(dirToCopy, "file5").exists());
		assertTrue(new File(dirToCopy, "Dir6" + FILE_SEP + "file7").exists());

		assertTrue(newDir.exists());
		assertTrue(new File(newDir, "Dir5").exists());
		assertTrue(new File(newDir, "file5").exists());
		assertTrue(new File(newDir, "Dir6" + FILE_SEP + "file7").exists());
	}

	@Test
	public void test_Move_Dir_Content_To_Another_Dir() throws Exception {

		Map<String, String> params = new HashMap<String, String>();
		params.put(START_DIRECTORY_PARAM_NAME, INPUT_DIR);
		params.put(FOLLOW_SYMBOLICS_LINK_PARAM_NAME, "true");
		params.put(KEEP_ORIGINAL_PARAM_NAME, "false");

		ConnectorInterface fmc = createConnector(ConnectorConfig.UPDATE_MODE, params);
		fmc.initialize(null);

		File dirToCopy = new File(INPUT_DIR, "NonEmptyDir" + FILE_SEP + "DirToBeCopy");
		String dirCanonicalPath = dirToCopy.getCanonicalPath();

		SearchCriteria search = new SearchCriteria();
		search.addCriteria(FULL_PATH_ATTR, SearchCriteria.EXACT, dirCanonicalPath);

		String newDirCanonicalPath = dirToCopy.getCanonicalFile().getParent() + FILE_SEP + "NewDir1" + FILE_SEP + "NewDir2";
		File newDir = new File(newDirCanonicalPath);

		Entry newEntry = new Entry();
		newEntry.addAttributeValue(FULL_PATH_ATTR, newDirCanonicalPath);

		fmc.modEntry(newEntry, search);
		fmc.terminate();

		assertFalse(dirToCopy.exists());

		assertTrue(newDir.exists());
		assertTrue(new File(newDir, "Dir5").exists());
		assertTrue(new File(newDir, "file5").exists());
		assertTrue(new File(newDir, "Dir6" + FILE_SEP + "file7").exists());
	}

	@Test
	public void test_Copy_Dir_To_Another_Dir_In_Same_Dir() throws Exception {

		Map<String, String> params = new HashMap<String, String>();
		params.put(START_DIRECTORY_PARAM_NAME, INPUT_DIR);
		params.put(FOLLOW_SYMBOLICS_LINK_PARAM_NAME, "true");
		params.put(KEEP_ORIGINAL_PARAM_NAME, "true");

		ConnectorInterface fmc = createConnector(ConnectorConfig.UPDATE_MODE, params);
		fmc.initialize(null);

		File dirToCopy = new File(INPUT_DIR, "NonEmptyDir" + FILE_SEP + "DirToBeCopy");
		String dirCanonicalPath = dirToCopy.getCanonicalPath();

		SearchCriteria search = new SearchCriteria();
		search.addCriteria(FULL_PATH_ATTR, SearchCriteria.EXACT, dirCanonicalPath);

		String newDirCanonicalPath = dirToCopy.getCanonicalFile().getParent() + FILE_SEP + "NewDir1";
		File newDir = new File(newDirCanonicalPath);

		Entry newEntry = new Entry();
		newEntry.addAttributeValue(NAME_ATTR, newDir.getName());

		fmc.modEntry(newEntry, search);
		fmc.terminate();

		assertTrue(dirToCopy.exists());
		assertTrue(new File(dirToCopy, "Dir5").exists());
		assertTrue(new File(dirToCopy, "file5").exists());
		assertTrue(new File(dirToCopy, "Dir6" + FILE_SEP + "file7").exists());

		assertTrue(newDir.exists());
		assertTrue(new File(newDir, "Dir5").exists());
		assertTrue(new File(newDir, "file5").exists());
		assertTrue(new File(newDir, "Dir6" + FILE_SEP + "file7").exists());
	}

	@Test
	public void test_Rename_Dir() throws Exception {

		Map<String, String> params = new HashMap<String, String>();
		params.put(START_DIRECTORY_PARAM_NAME, INPUT_DIR);
		params.put(FOLLOW_SYMBOLICS_LINK_PARAM_NAME, "true");
		params.put(KEEP_ORIGINAL_PARAM_NAME, "false");

		ConnectorInterface fmc = createConnector(ConnectorConfig.UPDATE_MODE, params);
		fmc.initialize(null);

		File dirToCopy = new File(INPUT_DIR, "NonEmptyDir" + FILE_SEP + "DirToBeCopy");
		String dirCanonicalPath = dirToCopy.getCanonicalPath();

		SearchCriteria search = new SearchCriteria();
		search.addCriteria(FULL_PATH_ATTR, SearchCriteria.EXACT, dirCanonicalPath);

		String newDirCanonicalPath = dirToCopy.getCanonicalFile().getParent() + FILE_SEP + "NewDir1";
		File newDir = new File(newDirCanonicalPath);

		Entry newEntry = new Entry();
		newEntry.addAttributeValue(NAME_ATTR, newDir.getName());

		fmc.modEntry(newEntry, search);
		fmc.terminate();

		assertFalse(dirToCopy.exists());

		assertTrue(newDir.exists());
		assertTrue(new File(newDir, "Dir5").exists());
		assertTrue(new File(newDir, "file5").exists());
		assertTrue(new File(newDir, "Dir6" + FILE_SEP + "file7").exists());
	}

	@Test
	public void test_Copy_Dir_And_Its_Content_To_Another_Dir() throws Exception {

		Map<String, String> params = new HashMap<String, String>();
		params.put(START_DIRECTORY_PARAM_NAME, INPUT_DIR);
		params.put(FOLLOW_SYMBOLICS_LINK_PARAM_NAME, "true");
		params.put(KEEP_ORIGINAL_PARAM_NAME, "true");

		ConnectorInterface fmc = createConnector(ConnectorConfig.UPDATE_MODE, params);
		fmc.initialize(null);

		File dirToCopy = new File(INPUT_DIR, "NonEmptyDir" + FILE_SEP + "DirToBeCopy");
		String dirCanonicalPath = dirToCopy.getCanonicalPath();

		SearchCriteria search = new SearchCriteria();
		search.addCriteria(FULL_PATH_ATTR, SearchCriteria.EXACT, dirCanonicalPath);

		String newDirCanonicalPath = dirToCopy.getCanonicalFile().getParent() + FILE_SEP + "NewDir1";
		File newDir = new File(newDirCanonicalPath);

		Entry newEntry = new Entry();
		newEntry.addAttributeValue(PARENT_ATTR, newDirCanonicalPath);

		fmc.modEntry(newEntry, search);
		fmc.terminate();

		assertTrue(dirToCopy.exists());
		assertTrue(new File(dirToCopy, "Dir5").exists());
		assertTrue(new File(dirToCopy, "file5").exists());
		assertTrue(new File(dirToCopy, "Dir6" + FILE_SEP + "file7").exists());

		assertTrue(newDir.exists());
		assertTrue(new File(newDir, "DirToBeCopy" + FILE_SEP + "Dir5").exists());
		assertTrue(new File(newDir, "DirToBeCopy" + FILE_SEP + "file5").exists());
		assertTrue(new File(newDir, "DirToBeCopy" + FILE_SEP + "Dir6" + FILE_SEP + "file7").exists());
	}

	@Test
	public void test_Move_Dir_And_Its_Content_To_Another_Dir() throws Exception {

		Map<String, String> params = new HashMap<String, String>();
		params.put(START_DIRECTORY_PARAM_NAME, INPUT_DIR);
		params.put(FOLLOW_SYMBOLICS_LINK_PARAM_NAME, "true");
		params.put(KEEP_ORIGINAL_PARAM_NAME, "false");

		ConnectorInterface fmc = createConnector(ConnectorConfig.UPDATE_MODE, params);
		fmc.initialize(null);

		File dirToCopy = new File(INPUT_DIR, "NonEmptyDir" + FILE_SEP + "DirToBeCopy");
		String dirCanonicalPath = dirToCopy.getCanonicalPath();

		SearchCriteria search = new SearchCriteria();
		search.addCriteria(FULL_PATH_ATTR, SearchCriteria.EXACT, dirCanonicalPath);

		String newDirCanonicalPath = dirToCopy.getCanonicalFile().getParent() + FILE_SEP + "NewDir1";
		File newDir = new File(newDirCanonicalPath);

		Entry newEntry = new Entry();
		newEntry.addAttributeValue(PARENT_ATTR, newDirCanonicalPath);

		fmc.modEntry(newEntry, search);
		fmc.terminate();

		assertFalse(dirToCopy.exists());

		assertTrue(newDir.exists());
		assertTrue(new File(newDir, "DirToBeCopy" + FILE_SEP + "Dir5").exists());
		assertTrue(new File(newDir, "DirToBeCopy" + FILE_SEP + "file5").exists());
		assertTrue(new File(newDir, "DirToBeCopy" + FILE_SEP + "Dir6" + FILE_SEP + "file7").exists());
	}

	@Test
	public void test_Copy_File_To_Another_File() throws Exception {

		Map<String, String> params = new HashMap<String, String>();
		params.put(START_DIRECTORY_PARAM_NAME, INPUT_DIR);
		params.put(FOLLOW_SYMBOLICS_LINK_PARAM_NAME, "true");
		params.put(KEEP_ORIGINAL_PARAM_NAME, "true");

		ConnectorInterface fmc = createConnector(ConnectorConfig.UPDATE_MODE, params);
		fmc.initialize(null);

		File fileToCopy = new File(INPUT_DIR, "NonEmptyDir" + FILE_SEP + "fileToBeCopy");
		String fileCanonicalPath = fileToCopy.getCanonicalPath();

		SearchCriteria search = new SearchCriteria();
		search.addCriteria(FULL_PATH_ATTR, SearchCriteria.EXACT, fileCanonicalPath);

		String newFileCanonicalPath = fileToCopy.getCanonicalFile().getParent() + FILE_SEP + "NewDir1" + FILE_SEP + "newFile1";
		File newFile = new File(newFileCanonicalPath);

		Entry newEntry = new Entry();
		newEntry.addAttributeValue(FULL_PATH_ATTR, newFileCanonicalPath);

		fmc.modEntry(newEntry, search);
		fmc.terminate();

		assertTrue(fileToCopy.exists());

		assertTrue(newFile.isFile());
		assertTrue(newFile.exists());
	}

	@Test
	public void test_Move_File_To_Another_File() throws Exception {

		Map<String, String> params = new HashMap<String, String>();
		params.put(START_DIRECTORY_PARAM_NAME, INPUT_DIR);
		params.put(FOLLOW_SYMBOLICS_LINK_PARAM_NAME, "true");
		params.put(KEEP_ORIGINAL_PARAM_NAME, "false");

		ConnectorInterface fmc = createConnector(ConnectorConfig.UPDATE_MODE, params);
		fmc.initialize(null);

		File fileToMove = new File(INPUT_DIR, "NonEmptyDir" + FILE_SEP + "fileToBeCopy");
		String fileCanonicalPath = fileToMove.getCanonicalPath();

		SearchCriteria search = new SearchCriteria();
		search.addCriteria(FULL_PATH_ATTR, SearchCriteria.EXACT, fileCanonicalPath);

		String newFileCanonicalPath = fileToMove.getCanonicalFile().getParent() + FILE_SEP + "NewDir1" + FILE_SEP + "newFile1";
		File newFile = new File(newFileCanonicalPath);

		Entry newEntry = new Entry();
		newEntry.addAttributeValue(FULL_PATH_ATTR, newFileCanonicalPath);

		fmc.modEntry(newEntry, search);
		fmc.terminate();

		assertFalse(fileToMove.exists());

		assertTrue(newFile.isFile());
		assertTrue(newFile.exists());
	}

	@Test
	public void test_Copy_File_To_Another_File_In_Same_Dir() throws Exception {

		Map<String, String> params = new HashMap<String, String>();
		params.put(START_DIRECTORY_PARAM_NAME, INPUT_DIR);
		params.put(FOLLOW_SYMBOLICS_LINK_PARAM_NAME, "true");
		params.put(KEEP_ORIGINAL_PARAM_NAME, "true");

		ConnectorInterface fmc = createConnector(ConnectorConfig.UPDATE_MODE, params);
		fmc.initialize(null);

		File fileToCopy = new File(INPUT_DIR, "NonEmptyDir" + FILE_SEP + "fileToBeCopy");
		String fileCanonicalPath = fileToCopy.getCanonicalPath();

		SearchCriteria search = new SearchCriteria();
		search.addCriteria(FULL_PATH_ATTR, SearchCriteria.EXACT, fileCanonicalPath);

		String newFileCanonicalPath = fileToCopy.getCanonicalFile().getParent() + FILE_SEP + "newFile1";
		File newFile = new File(newFileCanonicalPath);

		Entry newEntry = new Entry();
		newEntry.addAttributeValue(NAME_ATTR, newFile.getName());

		fmc.modEntry(newEntry, search);
		fmc.terminate();

		assertTrue(fileToCopy.exists());

		assertTrue(newFile.isFile());
		assertTrue(newFile.exists());
	}

	@Test
	public void test_Rename_File() throws Exception {

		Map<String, String> params = new HashMap<String, String>();
		params.put(START_DIRECTORY_PARAM_NAME, INPUT_DIR);
		params.put(FOLLOW_SYMBOLICS_LINK_PARAM_NAME, "true");
		params.put(KEEP_ORIGINAL_PARAM_NAME, "false");

		ConnectorInterface fmc = createConnector(ConnectorConfig.UPDATE_MODE, params);
		fmc.initialize(null);

		File fileToCopy = new File(INPUT_DIR, "NonEmptyDir" + FILE_SEP + "fileToBeCopy");
		String fileCanonicalPath = fileToCopy.getCanonicalPath();

		SearchCriteria search = new SearchCriteria();
		search.addCriteria(FULL_PATH_ATTR, SearchCriteria.EXACT, fileCanonicalPath);

		String newFileCanonicalPath = fileToCopy.getCanonicalFile().getParent() + FILE_SEP + "newFile1";
		File newFile = new File(newFileCanonicalPath);

		Entry newEntry = new Entry();
		newEntry.addAttributeValue(NAME_ATTR, newFile.getName());

		fmc.modEntry(newEntry, search);
		fmc.terminate();

		assertFalse(fileToCopy.exists());

		assertTrue(newFile.isFile());
		assertTrue(newFile.exists());
	}

	@Test
	public void test_Copy_File_To_Dir() throws Exception {

		Map<String, String> params = new HashMap<String, String>();
		params.put(START_DIRECTORY_PARAM_NAME, INPUT_DIR);
		params.put(FOLLOW_SYMBOLICS_LINK_PARAM_NAME, "true");
		params.put(KEEP_ORIGINAL_PARAM_NAME, "true");

		ConnectorInterface fmc = createConnector(ConnectorConfig.UPDATE_MODE, params);
		fmc.initialize(null);

		File fileToCopy = new File(INPUT_DIR, "NonEmptyDir" + FILE_SEP + "fileToBeCopy");
		String fileCanonicalPath = fileToCopy.getCanonicalPath();

		SearchCriteria search = new SearchCriteria();
		search.addCriteria(FULL_PATH_ATTR, SearchCriteria.EXACT, fileCanonicalPath);

		String newDirCanonicalPath = fileToCopy.getCanonicalFile().getParent() + FILE_SEP + "NewDir1";
		File newDir = new File(newDirCanonicalPath);

		Entry newEntry = new Entry();
		newEntry.addAttributeValue(PARENT_ATTR, newDirCanonicalPath);

		fmc.modEntry(newEntry, search);
		fmc.terminate();

		assertTrue(fileToCopy.exists());

		File newFile = new File(newDir, fileToCopy.getName());
		assertTrue(newFile.isFile());
		assertTrue(newFile.exists());
	}

	@Test
	public void test_Move_File_To_Dir() throws Exception {

		Map<String, String> params = new HashMap<String, String>();
		params.put(START_DIRECTORY_PARAM_NAME, INPUT_DIR);
		params.put(FOLLOW_SYMBOLICS_LINK_PARAM_NAME, "true");
		params.put(KEEP_ORIGINAL_PARAM_NAME, "false");

		ConnectorInterface fmc = createConnector(ConnectorConfig.UPDATE_MODE, params);
		fmc.initialize(null);

		File fileToCopy = new File(INPUT_DIR, "NonEmptyDir" + FILE_SEP + "fileToBeCopy");
		String fileCanonicalPath = fileToCopy.getCanonicalPath();

		SearchCriteria search = new SearchCriteria();
		search.addCriteria(FULL_PATH_ATTR, SearchCriteria.EXACT, fileCanonicalPath);

		String newDirCanonicalPath = fileToCopy.getCanonicalFile().getParent() + FILE_SEP + "NewDir1";
		File newDir = new File(newDirCanonicalPath);

		Entry newEntry = new Entry();
		newEntry.addAttributeValue(PARENT_ATTR, newDirCanonicalPath);

		fmc.modEntry(newEntry, search);
		fmc.terminate();

		assertFalse(fileToCopy.exists());

		File newFile = new File(newDir, fileToCopy.getName());
		assertTrue(newFile.isFile());
		assertTrue(newFile.exists());
	}
	
	@Test
	public void test_Create_File_By_Name_Without_IsDirectory() throws Exception {
		String testFile = "TestFile";
		Entry entry = new Entry();
		entry.addAttributeValue(NAME_ATTR, testFile);

		Map<String, String> params = new HashMap<String, String>();
		params.put(START_DIRECTORY_PARAM_NAME, OUTPUT_DIR);
		params.put(CREATE_FILE_PARAM_NAME, "true");

		ConnectorInterface fmc = createConnector(ConnectorConfig.ADDONLY_MODE, params);
		fmc.initialize(null);
		fmc.putEntry(entry);
		fmc.terminate();

		File file = new File(OUTPUT_DIR, testFile);
		assertTrue(file.exists());
		assertTrue(file.isFile());
	}
	
	@Test
	public void test_Create_Empty_Dir_By_FullPath_Without_IsDirectory() throws Exception {
		String testDir = "TestDir";
		File file = new File(OUTPUT_DIR, testDir);
		Entry entry = new Entry();
		entry.addAttributeValue(FULL_PATH_ATTR, file.getCanonicalPath());

		Map<String, String> params = new HashMap<String, String>();
		params.put(START_DIRECTORY_PARAM_NAME, OUTPUT_DIR);
		params.put(CREATE_FILE_PARAM_NAME, "false");

		ConnectorInterface fmc = createConnector(ConnectorConfig.ADDONLY_MODE, params);
		fmc.initialize(null);
		fmc.putEntry(entry);
		fmc.terminate();

		assertTrue(file.exists());
		assertTrue(file.isDirectory());
	}
	
	@Test
	public void test_Create_File_By_Name_Content_Byte_Array() throws Exception {
		String testFile = "TestFile";
		String content = "Test content";
		Entry entry = new Entry();
		entry.addAttributeValue(NAME_ATTR, testFile);
		entry.addAttributeValue(CONTENT_ATTR, content.getBytes(Charset.defaultCharset()));
		
		Map<String, String> params = new HashMap<String, String>();
		params.put(START_DIRECTORY_PARAM_NAME, OUTPUT_DIR);
		params.put(CREATE_FILE_PARAM_NAME, "true");

		ConnectorInterface fmc = createConnector(ConnectorConfig.ADDONLY_MODE, params);
		fmc.initialize(null);
		fmc.putEntry(entry);
		fmc.terminate();

		File file = new File(OUTPUT_DIR, testFile);
		assertTrue(file.exists());
		assertTrue(file.isFile());
		TestUtils.checkFileContains(file, content);
	}
	
	@Test
	public void test_Create_File_By_Name_Content_String_Default_CharSet() throws Exception {
		String testFile = "TestFile";
		String content = "Test content";
		Entry entry = new Entry();
		entry.addAttributeValue(NAME_ATTR, testFile);
		entry.addAttributeValue(CONTENT_ATTR, content);
		
		Map<String, String> params = new HashMap<String, String>();
		params.put(START_DIRECTORY_PARAM_NAME, OUTPUT_DIR);
		params.put(CREATE_FILE_PARAM_NAME, "true");

		ConnectorInterface fmc = createConnector(ConnectorConfig.ADDONLY_MODE, params);
		fmc.initialize(null);
		fmc.putEntry(entry);
		fmc.terminate();

		File file = new File(OUTPUT_DIR, testFile);
		assertTrue(file.exists());
		assertTrue(file.isFile());
		TestUtils.checkFileContains(file, content);
	}
	
	@Test
	public void test_Create_File_By_Name_Content_String_CharSet_Windows1251() throws Exception {
		String testFile = "TestFile";
		String content = "\u00d2\u00e5\u00f1\u00f2";
		Entry entry = new Entry();
		entry.addAttributeValue(NAME_ATTR, testFile);
		entry.addAttributeValue(CONTENT_ATTR, content);
		entry.addAttributeValue(CHAR_SET_ATTR, "windows-1251");
		
		Map<String, String> params = new HashMap<String, String>();
		params.put(START_DIRECTORY_PARAM_NAME, OUTPUT_DIR);
		params.put(CREATE_FILE_PARAM_NAME, "true");

		ConnectorInterface fmc = createConnector(ConnectorConfig.ADDONLY_MODE, params);
		fmc.initialize(null);
		fmc.putEntry(entry);
		fmc.terminate();

		File file = new File(OUTPUT_DIR, testFile);
		assertTrue(file.exists());
		assertTrue(file.isFile());
		TestUtils.checkFileContains(file, content);
	}
	
	@Test
	public void test_Copy_File_To_Another_File_In_Same_Dir_Conten_Byte_Array() throws Exception {
		
		Map<String, String> params = new HashMap<String, String>();
		params.put(START_DIRECTORY_PARAM_NAME, INPUT_DIR);
		params.put(FOLLOW_SYMBOLICS_LINK_PARAM_NAME, "true");
		params.put(KEEP_ORIGINAL_PARAM_NAME, "true");

		ConnectorInterface fmc = createConnector(ConnectorConfig.UPDATE_MODE, params);
		fmc.initialize(null);

		File fileToCopy = new File(INPUT_DIR, "NonEmptyDir" + FILE_SEP + "fileToBeCopy");
		String fileCanonicalPath = fileToCopy.getCanonicalPath();

		SearchCriteria search = new SearchCriteria();
		search.addCriteria(FULL_PATH_ATTR, SearchCriteria.EXACT, fileCanonicalPath);

		String newFileCanonicalPath = fileToCopy.getCanonicalFile().getParent() + FILE_SEP + "newFile1";
		File newFile = new File(newFileCanonicalPath);

		Entry newEntry = new Entry();
		newEntry.addAttributeValue(NAME_ATTR, newFile.getName());
		String content = "Test content";
		newEntry.addAttributeValue(CONTENT_ATTR, content.getBytes(Charset.defaultCharset()));

		fmc.modEntry(newEntry, search);
		fmc.terminate();

		assertTrue(fileToCopy.exists());

		assertTrue(newFile.isFile());
		assertTrue(newFile.exists());
		TestUtils.checkFileContains(newFile, content);
	}
	
	@Test
	public void test_Update_Conten_String_CharSet_Windows1251() throws Exception {
		
		Map<String, String> params = new HashMap<String, String>();
		params.put(START_DIRECTORY_PARAM_NAME, INPUT_DIR);
		params.put(FOLLOW_SYMBOLICS_LINK_PARAM_NAME, "true");
		params.put(KEEP_ORIGINAL_PARAM_NAME, "true");

		ConnectorInterface fmc = createConnector(ConnectorConfig.UPDATE_MODE, params);
		fmc.initialize(null);

		File oldFile = new File(INPUT_DIR, "NonEmptyDir" + FILE_SEP + "fileToBeCopy");
		String fileCanonicalPath = oldFile.getCanonicalPath();

		SearchCriteria search = new SearchCriteria();
		search.addCriteria(FULL_PATH_ATTR, SearchCriteria.EXACT, fileCanonicalPath);

		Entry newEntry = new Entry();
		String content = "\u00d2\u00e5\u00f1\u00f2";
		newEntry.addAttributeValue(CONTENT_ATTR, content);
		newEntry.addAttributeValue(CHAR_SET_ATTR, "windows-1251");

		fmc.modEntry(newEntry, search);
		fmc.terminate();

		assertTrue(oldFile.exists());
		assertTrue(oldFile.isFile());

		TestUtils.checkFileContains(oldFile, content);
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ZipFile inputZip = new ZipFile(RESOURCES_PLUS_FMC + "DefaultInput.zip");
		File homeDir = new File(RESOURCES_PLUS_FMC);
		unzipFileIntoDirectory(inputZip, homeDir);
	}

	public static void unzipFileIntoDirectory(ZipFile zipFile, File homeDir) throws Exception {
		Enumeration<? extends ZipEntry> files = zipFile.entries();
		File newFile = null;
		FileOutputStream fileOS = null;

		while (files.hasMoreElements()) {

			ZipEntry entry = (ZipEntry) files.nextElement();
			InputStream entryIS = zipFile.getInputStream(entry);
			byte[] buffer = new byte[1024];
			int bytesRead = 0;

			newFile = new File(homeDir.getAbsolutePath() + File.separator + entry.getName());

			if (entry.isDirectory()) {
				newFile.mkdirs();
				continue;
			} else {
				newFile.getParentFile().mkdirs();
				newFile.createNewFile();
			}

			fileOS = new FileOutputStream(newFile);

			while ((bytesRead = entryIS.read(buffer)) != -1) {
				fileOS.write(buffer, 0, bytesRead);
			}
		}
		if (fileOS != null) {
			fileOS.close();
		}
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		File homeDir = new File(DEFAULT_INPUT_DIR);
		if (homeDir.exists()) {
			org.apache.commons.io.FileUtils.forceDelete(homeDir);
		}
	}

	@Before
	public void createInputOutputDirs() throws Exception {
		createInputDir();
		File output = new File(OUTPUT_DIR);
		output.mkdirs();
	}

	private void createInputDir() throws Exception {
		File input = new File(INPUT_DIR);
		File defaultInput = new File(DEFAULT_INPUT_DIR);

		org.apache.commons.io.FileUtils.copyDirectory(defaultInput, input, true);
	}

	@After
	public void cleanInputOutputDirs() throws Exception {
		File input = new File(INPUT_DIR);
		File output = new File(OUTPUT_DIR);
		if (input.exists()) {
			org.apache.commons.io.FileUtils.forceDelete(input);
		}
		if (output.exists()) {
			org.apache.commons.io.FileUtils.forceDelete(output);
		}
	}

	private ConnectorInterface createConnector(String mode, Map<String, String> params) throws Exception {
		ConnectorInterface conn = new FileManagementConnector();
		ConnectorConfig cc = new ConnectorConfigImpl();
		cc.init();
		cc.setState(ConnectorConfig.ENABLED_STATE);
		cc.setMode(mode);
		cc.getConnectionConfig().setJavaClass(FileManagementConnector.class.getName());
		for (Map.Entry<String, String> param : params.entrySet()) {
			cc.getConnectionConfig().setParameter(param.getKey(), param.getValue());
		}
		conn.setConfiguration(cc);

		conn.setLog(new Log(""));
		conn.setRSInterface(new RSMock());
		return conn;
	}

	private void setHidden(File file) throws Exception {
		boolean isDos = (FILE_SEP == '\\');
		if (isDos) {
			Runtime.getRuntime().exec("attrib +H \"" + file.getCanonicalPath() + "\"");

			// Wait for the File System to apply the change.
			Thread.sleep(1000);
		}
	}
}
