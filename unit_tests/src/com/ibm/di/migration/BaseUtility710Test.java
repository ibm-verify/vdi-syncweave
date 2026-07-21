package com.ibm.di.migration;

import static junit.framework.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.ibm.di.test.utils.TestUtils;

public class BaseUtility710Test {
	
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private BaseMigrationUtility dummy;

	@Before
	public void createInstances() {
		dummy = new DummyBaseUtility();
	}

	@Test
	public void test_isSwitch_Method_With_Null() {
		assertFalse(dummy.isSwitch(null));
	}

	@Test
	public void test_isSwitch_Method_With_Dash() {
		assertFalse(dummy.isSwitch("-"));
	}

	@Test
	public void test_isSwitch_Method_With_Illegal_Switch() {
		assertFalse(dummy.isSwitch("dummyValue"));
	}

	@Test
	public void test_isSwitch_Method_With_MultiChar_Switch() {
		assertTrue(dummy.isSwitch("-realSwitch"));
	}

	@Test
	public void test_isSwitch_Method_With_SingleChar_Switch() {
		assertTrue(dummy.isSwitch("-s"));
	}

	@Test
	public void test_CommandLine_Parsing_Of_Null_Arguments() {
		dummy.parseArgs(null);

		assertNotNull(dummy.commandLineOptions);
	}

	@Test
	public void test_CommandLine_Parsing_Of_Standalone_Switches() {

		dummy.parseArgs(new String[] { "-v", "-s ", " -d" });

		assertEquals(3, dummy.commandLineOptions.size());
		assertEquals("", dummy.commandLineOptions.get("-v"));
		assertEquals("", dummy.commandLineOptions.get("-s"));
		assertEquals("", dummy.commandLineOptions.get("-d"));
	}

	@Test
	public void test_CommandLine_Parsing_Of_Standalone_Values() {

		String[] args = new String[] { "v", "ds ", " 3d", "-" };
		dummy.parseArgs(args);

		TestUtils.compareLists(args, dummy.commandValuesList);
	}

	@Test
	public void test_CommandLine_Parsing() {

		dummy.parseArgs(new String[] { "-v", "-s ", "val1", " -d", "a val2", "val3", "-switch", "", "val4" });

		assertEquals(4, dummy.commandLineOptions.size());
		assertEquals("", dummy.commandLineOptions.get("-v"));
		assertEquals("val1", dummy.commandLineOptions.get("-s"));
		assertEquals("a val2", dummy.commandLineOptions.get("-d"));
		assertEquals("", dummy.commandLineOptions.get("-switch"));

		TestUtils.compareLists(new String[] { "val3", "val4" }, dummy.commandValuesList);
	}

	@Test
	public void test_On_Demand_Creation_Of_commandValuesList() {
		assertNull(dummy.commandValuesList);
		assertNotNull(dummy.getCommandStandaloneValuesList());
		assertNotNull(dummy.commandValuesList);
	}

	@Test
	public void test_parseCommandLineOptions_With_Help_Switch_Only() {
		assertFalse(dummy.printHelp);

		dummy.commandLineOptions = new HashMap<String, String>();
		dummy.commandLineOptions.put(BaseMigrationUtility.SWITCH_HELP, "");

		dummy.interpretCommandLineOptions();

		assertTrue(dummy.printHelp);
	}

	@Test
	public void test_parseCommandLineOptions_With_Help_And_Src_Config_File() {
		assertFalse(dummy.printHelp);
		assertNull(dummy.migFileSrcPath);

		dummy.commandLineOptions = new HashMap<String, String>();
		dummy.commandLineOptions.put(BaseMigrationUtility.SWITCH_HELP, "");
		dummy.commandLineOptions.put(BaseMigrationUtility.SWITCH_MIG_FILE, "someFile");

		dummy.interpretCommandLineOptions();

		assertTrue(dummy.printHelp);
		assertNull(dummy.migFileSrcPath);
	}

	@Test
	public void test_parseCommandLineOptions_With_No_Help() {
		assertFalse(dummy.printHelp);
		assertNull(dummy.migFileSrcPath);

		dummy.commandLineOptions = new HashMap<String, String>();
		dummy.commandLineOptions.put(BaseMigrationUtility.SWITCH_VERBOSE, "");
		dummy.commandLineOptions.put(BaseMigrationUtility.SWITCH_MIG_FILE, "someFile1");
		dummy.commandLineOptions.put(BaseMigrationUtility.SWITCH_MIG_FILE_BAKUP, "someFile2");
		dummy.commandLineOptions.put(BaseMigrationUtility.SWITCH_MIG_FILE_NEW, "someFile3");

		dummy.interpretCommandLineOptions();

		assertFalse(dummy.printHelp);
		assertTrue(dummy.verboseMode);
		assertEquals("someFile1", dummy.migFileSrcPath);
		assertEquals("someFile2", dummy.migFileBakPath);
		assertEquals("someFile3", dummy.migFileDestPath);
	}

	@Test
	public void test_findEndOfLineCharacterSequence_Linux() {
		assertEquals("\n", dummy.findEndOfLineCharacterSequence(new StringBuilder("firstLine\nsecondLine\n")));
	}

	@Test
	public void test_findEndOfLineCharacterSequence_Mac() {
		assertEquals("\r", dummy.findEndOfLineCharacterSequence(new StringBuilder("firstLine\rsecondLine\r")));
	}

	@Test
	public void test_findEndOfLineCharacterSequence_Win() {
		assertEquals("\r\n", dummy.findEndOfLineCharacterSequence(new StringBuilder("firstLine\r\nsecondLine\r\n")));
	}

	@Test(expected = IllegalArgumentException.class)
	public void test_validateCommandLineOptions_Missing_Src_File() {
		dummy.migFileSrcPath = "missing.file.path";
		dummy.validateCommandLineOptions();
	}

	@Test
	public void test_parseConfigFile_Reads_Empty_File() throws IOException {
		Map<String, String> cfg = dummy.parseFile(new ByteArrayInputStream("".getBytes()));

		assertNotNull(cfg);
		assertEquals(0, cfg.size());
	}

	@Test
	public void test_parseConfigFile_Reads_Single_Line_Property() throws IOException {
		Map<String, String> cfg = dummy.parseFile(new ByteArrayInputStream("propertyKey=propertyValue\n".getBytes()));

		assertNotNull(cfg);
		assertEquals(1, cfg.size());
		assertEquals("propertyValue", cfg.get("propertyKey"));
	}

	@Test
	public void test_parseConfigFile_Reads_Multi_Line_Property() throws IOException {
		Map<String, String> cfg = dummy.parseFile(new ByteArrayInputStream(
				"propertyKey=propertyValue\\\npropertyValue2\\\npropertyValue3\n".getBytes()));

		assertNotNull(cfg);
		assertEquals(1, cfg.size());
		assertEquals("propertyValuepropertyValue2propertyValue3", cfg.get("propertyKey"));
	}

	@Test
	public void test_parseConfigFile_Reads_Mixed_Line_Property() throws IOException {
		Map<String, String> cfg = dummy.parseFile(new ByteArrayInputStream(
				"propertyKey=propertyValue\\\npropertyValue2\\\npropertyValue3\npropertyKey2:propertyValue4".getBytes()));

		assertNotNull(cfg);
		assertEquals(2, cfg.size());
		assertEquals("propertyValuepropertyValue2propertyValue3", cfg.get("propertyKey"));
		assertEquals("propertyValue4", cfg.get("propertyKey2"));
	}

	@Test
	public void test_applyChanges_Uncommenting_Commented_Property() throws IOException {

		// define the changes over the source configuration file
		dummy = new ChangesCounter() {

			@Override
			protected List<ChangeDescription> defineChanges(Map<String, String> props) {
				List<ChangeDescription> changes = new ArrayList<ChangeDescription>();

				changes.add(new ChangeDescription("prop", ChangeDescription.TYPE_UNCOMMENT));
				return changes;
			}
		};

		// define the configuration file content
		String cfgFile = "#prop=value";
		dummy.eol = "\n";

		StringBuilder sb = new StringBuilder(cfgFile);
		Map<String, String> props = dummy.parseFile(new ByteArrayInputStream(cfgFile.getBytes()));
		List<ChangeDescription> changes = dummy.defineChanges(props);

		dummy.applyChanges(sb, props, changes);

		assertEquals(1, ((ChangesCounter) dummy).uncommentedCount);
		assertEquals(0, ((ChangesCounter) dummy).commentedCount);
		assertEquals(0, ((ChangesCounter) dummy).addedCount);
		assertEquals(0, ((ChangesCounter) dummy).modifiedCount);
		assertEquals(0, ((ChangesCounter) dummy).deletedCount);
	}

	@Test
	public void test_applyChanges_Uncommenting_Uncommented_Property() throws IOException {

		// define the changes over the source configuration file
		dummy = new ChangesCounter() {

			@Override
			protected List<ChangeDescription> defineChanges(Map<String, String> props) {
				List<ChangeDescription> changes = new ArrayList<ChangeDescription>();

				changes.add(new ChangeDescription("prop", ChangeDescription.TYPE_UNCOMMENT));
				return changes;
			}
		};

		// define the configuration file content
		String cfgFile = "prop=value";
		dummy.eol = "\n";

		StringBuilder sb = new StringBuilder(cfgFile);
		Map<String, String> props = dummy.parseFile(new ByteArrayInputStream(cfgFile.getBytes()));
		List<ChangeDescription> changes = dummy.defineChanges(props);

		dummy.applyChanges(sb, props, changes);

		assertEquals(0, ((ChangesCounter) dummy).uncommentedCount);
		assertEquals(0, ((ChangesCounter) dummy).commentedCount);
		assertEquals(0, ((ChangesCounter) dummy).addedCount);
		assertEquals(0, ((ChangesCounter) dummy).modifiedCount);
		assertEquals(0, ((ChangesCounter) dummy).deletedCount);
	}

	@Test
	public void test_applyChanges_Commenting_Uncommented_Property() throws IOException {

		// define the changes over the source configuration file
		dummy = new ChangesCounter() {

			@Override
			protected List<ChangeDescription> defineChanges(Map<String, String> props) {
				List<ChangeDescription> changes = new ArrayList<ChangeDescription>();

				changes.add(new ChangeDescription("prop", ChangeDescription.TYPE_COMMENT));
				return changes;
			}
		};

		// define the configuration file content
		String cfgFile = "prop=value";
		dummy.eol = "\n";

		StringBuilder sb = new StringBuilder(cfgFile);
		Map<String, String> props = dummy.parseFile(new ByteArrayInputStream(cfgFile.getBytes()));
		List<ChangeDescription> changes = dummy.defineChanges(props);

		dummy.applyChanges(sb, props, changes);

		assertEquals(0, ((ChangesCounter) dummy).uncommentedCount);
		assertEquals(1, ((ChangesCounter) dummy).commentedCount);
		assertEquals(0, ((ChangesCounter) dummy).addedCount);
		assertEquals(0, ((ChangesCounter) dummy).modifiedCount);
		assertEquals(0, ((ChangesCounter) dummy).deletedCount);
	}

	@Test
	public void test_applyChanges_Commenting_Commented_Property() throws IOException {

		// define the changes over the source configuration file
		dummy = new ChangesCounter() {

			@Override
			protected List<ChangeDescription> defineChanges(Map<String, String> props) {
				List<ChangeDescription> changes = new ArrayList<ChangeDescription>();

				changes.add(new ChangeDescription("prop", ChangeDescription.TYPE_COMMENT));
				return changes;
			}
		};

		// define the configuration file content
		String cfgFile = "!prop=value";
		dummy.eol = "\n";

		StringBuilder sb = new StringBuilder(cfgFile);
		Map<String, String> props = dummy.parseFile(new ByteArrayInputStream(cfgFile.getBytes()));
		List<ChangeDescription> changes = dummy.defineChanges(props);

		dummy.applyChanges(sb, props, changes);

		assertEquals(0, ((ChangesCounter) dummy).uncommentedCount);
		assertEquals(0, ((ChangesCounter) dummy).commentedCount);
		assertEquals(0, ((ChangesCounter) dummy).addedCount);
		assertEquals(0, ((ChangesCounter) dummy).modifiedCount);
		assertEquals(0, ((ChangesCounter) dummy).deletedCount);
	}

	@Test
	public void test_applyChanges_Adding_Missing_Property() throws IOException {

		// define the changes over the source configuration file
		dummy = new ChangesCounter() {

			@Override
			protected List<ChangeDescription> defineChanges(Map<String, String> props) {
				List<ChangeDescription> changes = new ArrayList<ChangeDescription>();

				changes.add(new ChangeDescription("prop", ChangeDescription.TYPE_ADD, "value"));
				return changes;
			}
		};

		// define the configuration file content
		String cfgFile = "";
		dummy.eol = "\n";

		StringBuilder sb = new StringBuilder(cfgFile);
		Map<String, String> props = dummy.parseFile(new ByteArrayInputStream(cfgFile.getBytes()));
		List<ChangeDescription> changes = dummy.defineChanges(props);

		dummy.applyChanges(sb, props, changes);

		assertEquals(0, ((ChangesCounter) dummy).uncommentedCount);
		assertEquals(0, ((ChangesCounter) dummy).commentedCount);
		assertEquals(1, ((ChangesCounter) dummy).addedCount);
		assertEquals(0, ((ChangesCounter) dummy).modifiedCount);
		assertEquals(0, ((ChangesCounter) dummy).deletedCount);
	}

	@Test
	public void test_applyChanges_Adding_Existing_Property() throws IOException {

		// define the changes over the source configuration file
		dummy = new ChangesCounter() {

			@Override
			protected List<ChangeDescription> defineChanges(Map<String, String> props) {
				List<ChangeDescription> changes = new ArrayList<ChangeDescription>();

				changes.add(new ChangeDescription("prop", ChangeDescription.TYPE_ADD, "value"));
				return changes;
			}
		};

		// define the configuration file content
		String cfgFile = "prop:val";
		dummy.eol = "\n";

		StringBuilder sb = new StringBuilder(cfgFile);
		Map<String, String> props = dummy.parseFile(new ByteArrayInputStream(cfgFile.getBytes()));
		List<ChangeDescription> changes = dummy.defineChanges(props);

		dummy.applyChanges(sb, props, changes);

		assertEquals(0, ((ChangesCounter) dummy).uncommentedCount);
		assertEquals(0, ((ChangesCounter) dummy).commentedCount);
		assertEquals(0, ((ChangesCounter) dummy).addedCount);
		assertEquals(0, ((ChangesCounter) dummy).modifiedCount);
		assertEquals(0, ((ChangesCounter) dummy).deletedCount);
	}

	@Test
	public void test_applyChanges_Modifying_Existing_Property() throws IOException {

		// define the changes over the source configuration file
		dummy = new ChangesCounter() {

			@Override
			protected List<ChangeDescription> defineChanges(Map<String, String> props) {
				List<ChangeDescription> changes = new ArrayList<ChangeDescription>();

				changes.add(new ChangeDescription("prop", ChangeDescription.TYPE_MODIFY, "val"));
				return changes;
			}
		};

		// define the configuration file content
		String cfgFile = "prop=value";
		dummy.eol = "\n";

		StringBuilder sb = new StringBuilder(cfgFile);
		Map<String, String> props = dummy.parseFile(new ByteArrayInputStream(cfgFile.getBytes()));
		List<ChangeDescription> changes = dummy.defineChanges(props);

		dummy.applyChanges(sb, props, changes);

		assertEquals(0, ((ChangesCounter) dummy).uncommentedCount);
		assertEquals(0, ((ChangesCounter) dummy).commentedCount);
		assertEquals(0, ((ChangesCounter) dummy).addedCount);
		assertEquals(1, ((ChangesCounter) dummy).modifiedCount);
		assertEquals(0, ((ChangesCounter) dummy).deletedCount);
	}

	@Test
	public void test_applyChanges_Modifying_Missing_Property() throws IOException {

		// define the changes over the source configuration file
		dummy = new ChangesCounter() {

			@Override
			protected List<ChangeDescription> defineChanges(Map<String, String> props) {
				List<ChangeDescription> changes = new ArrayList<ChangeDescription>();

				changes.add(new ChangeDescription("prop", ChangeDescription.TYPE_MODIFY, "val"));
				return changes;
			}
		};

		// define the configuration file content
		String cfgFile = "!prop=value";
		dummy.eol = "\n";

		StringBuilder sb = new StringBuilder(cfgFile);
		Map<String, String> props = dummy.parseFile(new ByteArrayInputStream(cfgFile.getBytes()));
		List<ChangeDescription> changes = dummy.defineChanges(props);

		dummy.applyChanges(sb, props, changes);

		assertEquals(0, ((ChangesCounter) dummy).uncommentedCount);
		assertEquals(0, ((ChangesCounter) dummy).commentedCount);
		assertEquals(0, ((ChangesCounter) dummy).addedCount);
		assertEquals(1, ((ChangesCounter) dummy).modifiedCount);
		assertEquals(0, ((ChangesCounter) dummy).deletedCount);
	}

	@Test
	public void test_applyChanges_Deleting_Existing_Propertry() throws IOException {

		// define the changes over the source configuration file
		dummy = new ChangesCounter() {

			@Override
			protected List<ChangeDescription> defineChanges(Map<String, String> props) {
				List<ChangeDescription> changes = new ArrayList<ChangeDescription>();

				changes.add(new ChangeDescription("prop", ChangeDescription.TYPE_DELETE));
				return changes;
			}
		};

		// define the configuration file content
		String cfgFile = "prop=value";
		dummy.eol = "\n";

		StringBuilder sb = new StringBuilder(cfgFile);
		Map<String, String> props = dummy.parseFile(new ByteArrayInputStream(cfgFile.getBytes()));
		List<ChangeDescription> changes = dummy.defineChanges(props);

		dummy.applyChanges(sb, props, changes);

		assertEquals(0, ((ChangesCounter) dummy).uncommentedCount);
		assertEquals(0, ((ChangesCounter) dummy).commentedCount);
		assertEquals(0, ((ChangesCounter) dummy).addedCount);
		assertEquals(0, ((ChangesCounter) dummy).modifiedCount);
		assertEquals(1, ((ChangesCounter) dummy).deletedCount);
	}

	@Test
	public void test_applyChanges_Deleting_Missing_Propertry() throws IOException {

		// define the changes over the source configuration file
		dummy = new ChangesCounter() {

			@Override
			protected List<ChangeDescription> defineChanges(Map<String, String> props) {
				List<ChangeDescription> changes = new ArrayList<ChangeDescription>();

				changes.add(new ChangeDescription("prop", ChangeDescription.TYPE_DELETE));
				return changes;
			}
		};

		// define the configuration file content
		String cfgFile = "#prop=value";
		dummy.eol = "\n";

		StringBuilder sb = new StringBuilder(cfgFile);
		Map<String, String> props = dummy.parseFile(new ByteArrayInputStream(cfgFile.getBytes()));
		List<ChangeDescription> changes = dummy.defineChanges(props);

		dummy.applyChanges(sb, props, changes);

		assertEquals(0, ((ChangesCounter) dummy).uncommentedCount);
		assertEquals(0, ((ChangesCounter) dummy).commentedCount);
		assertEquals(0, ((ChangesCounter) dummy).addedCount);
		assertEquals(0, ((ChangesCounter) dummy).modifiedCount);
		assertEquals(1, ((ChangesCounter) dummy).deletedCount);
	}

	@Test
	public void test_performPropertyUncommenting_Of_Existing_Property() throws IOException {
		// define the configuration file content
		String cfgFile = "#prop=value";
		dummy.eol = "\n";

		StringBuilder sb = new StringBuilder(cfgFile);
		Map<String, String> props = dummy.parseFile(new ByteArrayInputStream(cfgFile.getBytes()));

		dummy.performPropertyUncommenting(sb, props, new ChangeDescription("prop", ChangeDescription.TYPE_UNCOMMENT));
		cfgFile = sb.toString();

		assertEquals("prop=value", cfgFile);
	}

	@Test
	public void test_performPropertyUncommenting_Of_Existing_Property_With_Several_Comment_Chars() throws IOException {
		// define the configuration file content
		String cfgFile = "!#prop=value\n";
		dummy.eol = "\n";

		StringBuilder sb = new StringBuilder(cfgFile);
		Map<String, String> props = dummy.parseFile(new ByteArrayInputStream(cfgFile.getBytes()));

		dummy.performPropertyUncommenting(sb, props, new ChangeDescription("prop", ChangeDescription.TYPE_UNCOMMENT));
		cfgFile = sb.toString();

		assertEquals("prop=value\n", cfgFile);
	}

	@Test
	public void test_performPropertyUncommenting_Of_Non_Existing_Property() throws IOException {
		// define the configuration file content
		String cfgFile = "#prop1=value\n";
		dummy.eol = "\n";

		StringBuilder sb = new StringBuilder(cfgFile);
		Map<String, String> props = dummy.parseFile(new ByteArrayInputStream(cfgFile.getBytes()));

		dummy.performPropertyUncommenting(sb, props, new ChangeDescription("prop", ChangeDescription.TYPE_UNCOMMENT));
		cfgFile = sb.toString();

		assertEquals("#prop1=value\n", cfgFile);
	}

	@Test
	public void test_performPropertyUncommenting_Of_Existing_Property_With_Multi_Line_Values_With_EOL() throws IOException {
		// define the configuration file content
		String cfgFile = "#prop=value1\\\n#value2\\\n#value3\n";
		dummy.eol = "\n";

		StringBuilder sb = new StringBuilder(cfgFile);
		Map<String, String> props = dummy.parseFile(new ByteArrayInputStream(cfgFile.getBytes()));

		dummy.performPropertyUncommenting(sb, props, new ChangeDescription("prop", ChangeDescription.TYPE_UNCOMMENT));
		cfgFile = sb.toString();

		assertEquals("prop=value1\\\nvalue2\\\nvalue3\n", cfgFile);
	}

	@Test
	public void test_performPropertyUncommenting_Of_Existing_Property_With_Multi_Line_Values_Without_EOL() throws IOException {
		// define the configuration file content
		String cfgFile = "#prop=value1\\\n#value2\\\n#value3";
		dummy.eol = "\n";

		StringBuilder sb = new StringBuilder(cfgFile);
		Map<String, String> props = dummy.parseFile(new ByteArrayInputStream(cfgFile.getBytes()));

		dummy.performPropertyUncommenting(sb, props, new ChangeDescription("prop", ChangeDescription.TYPE_UNCOMMENT));
		cfgFile = sb.toString();

		assertEquals("prop=value1\\\nvalue2\\\nvalue3", cfgFile);
	}

	@Test
	public void test_performPropertyUncommenting_Of_Existing_Property_With_Multi_Line_Values_And_Another_Property_After()
			throws IOException {
		// define the configuration file content
		String cfgFile = "#prop=value1\\\n#value2\\\n#value3\n#prop2=val2\n";
		dummy.eol = "\n";

		StringBuilder sb = new StringBuilder(cfgFile);
		Map<String, String> props = dummy.parseFile(new ByteArrayInputStream(cfgFile.getBytes()));

		dummy.performPropertyUncommenting(sb, props, new ChangeDescription("prop", ChangeDescription.TYPE_UNCOMMENT));
		cfgFile = sb.toString();

		assertEquals("prop=value1\\\nvalue2\\\nvalue3\n#prop2=val2\n", cfgFile);
	}

	@Test
	public void test_performPropertyCommenting_Of_Existing_Property() throws IOException {
		// define the configuration file content
		String cfgFile = "prop=value\n";
		dummy.eol = "\n";

		StringBuilder sb = new StringBuilder(cfgFile);
		Map<String, String> props = dummy.parseFile(new ByteArrayInputStream(cfgFile.getBytes()));

		dummy.performPropertyCommenting(sb, props, new ChangeDescription("prop", ChangeDescription.TYPE_COMMENT));
		cfgFile = sb.toString();

		assertEquals("#prop=value\n", cfgFile);
	}

	@Test
	public void test_performPropertyCommenting_Of_Existing_Property_With_No_Value() throws IOException {
		// define the configuration file content
		String cfgFile = "prop=\n";
		dummy.eol = "\n";

		StringBuilder sb = new StringBuilder(cfgFile);
		Map<String, String> props = dummy.parseFile(new ByteArrayInputStream(cfgFile.getBytes()));

		dummy.performPropertyCommenting(sb, props, new ChangeDescription("prop", ChangeDescription.TYPE_COMMENT));
		cfgFile = sb.toString();

		assertEquals("#prop=\n", cfgFile);
	}

	@Test
	public void test_performPropertyCommenting_Of_Existing_Property_With_No_Value_And_No_Assign_Char() throws IOException {
		// define the configuration file content
		String cfgFile = "prop\n";
		dummy.eol = "\n";

		StringBuilder sb = new StringBuilder(cfgFile);
		Map<String, String> props = dummy.parseFile(new ByteArrayInputStream(cfgFile.getBytes()));

		dummy.performPropertyCommenting(sb, props, new ChangeDescription("prop", ChangeDescription.TYPE_COMMENT));
		cfgFile = sb.toString();

		assertEquals("#prop\n", cfgFile);
	}

	@Test
	public void test_performPropertyCommenting_Of_Existing_Property_With_Values_On_Multiple_Lines() throws IOException {
		// define the configuration file content
		String cfgFile = "prop=value1\\\nval2\\\nval3\n\n";
		dummy.eol = "\n";

		StringBuilder sb = new StringBuilder(cfgFile);
		Map<String, String> props = dummy.parseFile(new ByteArrayInputStream(cfgFile.getBytes()));

		dummy.performPropertyCommenting(sb, props, new ChangeDescription("prop", ChangeDescription.TYPE_COMMENT));
		cfgFile = sb.toString();

		assertEquals("#prop=value1\\\n#val2\\\n#val3\n\n", cfgFile);
	}

	@Test
	public void test_performPropertyCommenting_Of_Existing_Property_With_Values_On_Multiple_Lines_And_Has_Other_Properties_After_The_Value()
			throws IOException {
		// define the configuration file content
		String cfgFile = "prop=value1\\\nval2\\\nval3\nprop2=val\n";
		dummy.eol = "\n";

		StringBuilder sb = new StringBuilder(cfgFile);
		Map<String, String> props = dummy.parseFile(new ByteArrayInputStream(cfgFile.getBytes()));

		dummy.performPropertyCommenting(sb, props, new ChangeDescription("prop", ChangeDescription.TYPE_COMMENT));
		cfgFile = sb.toString();

		assertEquals("#prop=value1\\\n#val2\\\n#val3\nprop2=val\n", cfgFile);
	}

	@Test
	public void test_performPropertyAddition_In_Empty_File_With_No_EOLs() throws IOException {
		// define the configuration file content
		String cfgFile = "";
		dummy.eol = "\n";

		StringBuilder sb = new StringBuilder(cfgFile);
		Map<String, String> props = dummy.parseFile(new ByteArrayInputStream(cfgFile.getBytes()));

		dummy.performPropertyAddition(sb, props, new ChangeDescription("prop", ChangeDescription.TYPE_ADD, "val"));
		cfgFile = sb.toString();

		assertEquals("\nprop=val", cfgFile);
	}

	@Test
	public void test_performPropertyAddition_Check_Appending_To_The_End_Of_The_file() throws IOException {
		// define the configuration file content
		String cfgFile = "prop1=val1\nprop2=val2";
		dummy.eol = "\n";

		StringBuilder sb = new StringBuilder(cfgFile);
		Map<String, String> props = dummy.parseFile(new ByteArrayInputStream(cfgFile.getBytes()));

		dummy.performPropertyAddition(sb, props, new ChangeDescription("prop", ChangeDescription.TYPE_ADD, "val"));
		cfgFile = sb.toString();

		assertEquals("prop1=val1\nprop2=val2\nprop=val", cfgFile);
	}

	@Test
	public void test_performPropertyAddition_Between_Existing_Properties() throws IOException {
		// define the configuration file content
		String cfgFile = "prop1=val1\nprop2=val2";
		dummy.eol = "\n";

		StringBuilder sb = new StringBuilder(cfgFile);
		Map<String, String> props = dummy.parseFile(new ByteArrayInputStream(cfgFile.getBytes()));

		dummy.performPropertyAddition(sb, props, new ChangeDescription("prop", ChangeDescription.TYPE_ADD, "val", "prop1", null, 0,
				0));
		cfgFile = sb.toString();

		assertEquals("prop1=val1\nprop=val\nprop2=val2", cfgFile);
	}

	@Test
	public void test_performPropertyAddition_Between_Existing_Properties_With_Comment() throws IOException {
		// define the configuration file content
		String cfgFile = "prop1=val1\nprop2=val2";
		dummy.eol = "\n";

		StringBuilder sb = new StringBuilder(cfgFile);
		Map<String, String> props = dummy.parseFile(new ByteArrayInputStream(cfgFile.getBytes()));

		dummy.performPropertyAddition(sb, props, new ChangeDescription("prop", ChangeDescription.TYPE_ADD, "val", "prop1",
				new String[] { "testComment1", "testComment2" }, 0, 0));
		cfgFile = sb.toString();

		assertEquals("prop1=val1\n#testComment1\n#testComment2\nprop=val\nprop2=val2", cfgFile);
	}

	@Test
	public void test_performPropertyAddition_Between_Existing_Properties_With_Comment_And_EOLs_Before_And_After()
			throws IOException {
		// define the configuration file content
		String cfgFile = "prop1=val1\nprop2=val2";
		dummy.eol = "\n";

		StringBuilder sb = new StringBuilder(cfgFile);
		Map<String, String> props = dummy.parseFile(new ByteArrayInputStream(cfgFile.getBytes()));

		dummy.performPropertyAddition(sb, props, new ChangeDescription("prop", ChangeDescription.TYPE_ADD, "val", "prop1",
				new String[] { "testComment1", "testComment2" }, 2, 1));
		cfgFile = sb.toString();

		assertEquals("prop1=val1\n\n\n#testComment1\n#testComment2\nprop=val\n\nprop2=val2", cfgFile);
	}

	@Test
	public void test_performPropertyAddition_In_Empty_File_With_2_EOL_Before() throws IOException {
		// define the configuration file content
		String cfgFile = "";
		dummy.eol = "\n";

		StringBuilder sb = new StringBuilder(cfgFile);
		Map<String, String> props = dummy.parseFile(new ByteArrayInputStream(cfgFile.getBytes()));

		dummy
				.performPropertyAddition(sb, props, new ChangeDescription("prop", ChangeDescription.TYPE_ADD, "val", null, null, 2,
						0));
		cfgFile = sb.toString();

		assertEquals("\n\n\nprop=val", cfgFile);
	}

	@Test
	public void test_performPropertyAddition_In_Empty_File_With_2_EOL_After() throws IOException {
		// define the configuration file content
		String cfgFile = "";
		dummy.eol = "\n";

		StringBuilder sb = new StringBuilder(cfgFile);
		Map<String, String> props = dummy.parseFile(new ByteArrayInputStream(cfgFile.getBytes()));

		dummy
				.performPropertyAddition(sb, props, new ChangeDescription("prop", ChangeDescription.TYPE_ADD, "val", null, null, 0,
						2));
		cfgFile = sb.toString();

		assertEquals("\nprop=val\n\n", cfgFile);
	}

	@Test
	public void test_performPropertyModification_Of_Non_Existing_Property() throws IOException {
		// define the configuration file content
		String cfgFile = "prop1=val";
		dummy.eol = "\n";

		StringBuilder sb = new StringBuilder(cfgFile);
		Map<String, String> props = dummy.parseFile(new ByteArrayInputStream(cfgFile.getBytes()));

		dummy.performPropertyModification(sb, props, new ChangeDescription("prop", ChangeDescription.TYPE_MODIFY, "val1"));
		cfgFile = sb.toString();

		assertEquals("prop1=val", cfgFile);
	}

	@Test
	public void test_performPropertyModification_Of_Existing_Property_Not_Commented() throws IOException {
		// define the configuration file content
		String cfgFile = "prop=val";
		dummy.eol = "\n";

		StringBuilder sb = new StringBuilder(cfgFile);
		Map<String, String> props = dummy.parseFile(new ByteArrayInputStream(cfgFile.getBytes()));

		dummy.performPropertyModification(sb, props, new ChangeDescription("prop", ChangeDescription.TYPE_MODIFY, "val1"));
		cfgFile = sb.toString();

		assertEquals("prop=val1", cfgFile);
	}

	@Test
	public void test_performPropertyModification_Of_Existing_Property_Commented() throws IOException {
		// define the configuration file content
		String cfgFile = "#prop=val";
		dummy.eol = "\n";

		StringBuilder sb = new StringBuilder(cfgFile);
		Map<String, String> props = dummy.parseFile(new ByteArrayInputStream(cfgFile.getBytes()));

		dummy.performPropertyModification(sb, props, new ChangeDescription("prop", ChangeDescription.TYPE_MODIFY, "val1"));
		cfgFile = sb.toString();

		assertEquals("#prop=val1", cfgFile);
	}

	@Test
	public void test_performPropertyModification_Of_Existing_Property_With_No_Value_And_No_Assign_Char() throws IOException {
		// define the configuration file content
		String cfgFile = "prop";
		dummy.eol = "\n";

		StringBuilder sb = new StringBuilder(cfgFile);
		Map<String, String> props = dummy.parseFile(new ByteArrayInputStream(cfgFile.getBytes()));

		dummy.performPropertyModification(sb, props, new ChangeDescription("prop", ChangeDescription.TYPE_MODIFY, "val1"));
		cfgFile = sb.toString();

		assertEquals("prop=val1", cfgFile);
	}

	@Test
	public void test_performPropertyModification_Of_Existing_Property_With_No_Value_But_With_Assign_Char() throws IOException {
		// define the configuration file content
		String cfgFile = "prop=";
		dummy.eol = "\n";

		StringBuilder sb = new StringBuilder(cfgFile);
		Map<String, String> props = dummy.parseFile(new ByteArrayInputStream(cfgFile.getBytes()));

		dummy.performPropertyModification(sb, props, new ChangeDescription("prop", ChangeDescription.TYPE_MODIFY, "val1"));
		cfgFile = sb.toString();

		assertEquals("prop=val1", cfgFile);
	}

	@Test
	public void test_performPropertyModification_Of_Existing_Property_Commented_With_No_Value_But_With_Assign_Char()
			throws IOException {
		// define the configuration file content
		String cfgFile = "#prop=";
		dummy.eol = "\n";

		StringBuilder sb = new StringBuilder(cfgFile);
		Map<String, String> props = dummy.parseFile(new ByteArrayInputStream(cfgFile.getBytes()));

		dummy.performPropertyModification(sb, props, new ChangeDescription("prop", ChangeDescription.TYPE_MODIFY, "val1"));
		cfgFile = sb.toString();

		assertEquals("#prop=val1", cfgFile);
	}

	@Test
	public void test_performPropertyDeletion_Of_Existing_Property_Commented_Without_Deleting_Comments() throws IOException {
		// define the configuration file content
		String cfgFile = "#prop=val";
		dummy.eol = "\n";

		StringBuilder sb = new StringBuilder(cfgFile);
		Map<String, String> props = dummy.parseFile(new ByteArrayInputStream(cfgFile.getBytes()));

		dummy.performPropertyDeletion(sb, props, new ChangeDescription("prop", ChangeDescription.TYPE_DELETE, false));
		cfgFile = sb.toString();

		assertEquals("", cfgFile);
	}

	@Test
	public void test_performPropertyDeletion_Of_Existing_Property_With_No_Value() throws IOException {
		// define the configuration file content
		String cfgFile = "prop=";
		dummy.eol = "\n";

		StringBuilder sb = new StringBuilder(cfgFile);
		Map<String, String> props = dummy.parseFile(new ByteArrayInputStream(cfgFile.getBytes()));

		dummy.performPropertyDeletion(sb, props, new ChangeDescription("prop", ChangeDescription.TYPE_DELETE, false));
		cfgFile = sb.toString();

		assertEquals("", cfgFile);
	}

	@Test
	public void test_performPropertyDeletion_Of_Existing_Property_With_No_Value_And_No_Assign_Char() throws IOException {
		// define the configuration file content
		String cfgFile = "prop";
		dummy.eol = "\n";

		StringBuilder sb = new StringBuilder(cfgFile);
		Map<String, String> props = dummy.parseFile(new ByteArrayInputStream(cfgFile.getBytes()));

		dummy.performPropertyDeletion(sb, props, new ChangeDescription("prop", ChangeDescription.TYPE_DELETE, false));
		cfgFile = sb.toString();

		assertEquals("", cfgFile);
	}

	@Test
	public void test_performPropertyDeletion_Of_Existing_Property_Commented_With_Deleting_Comments() throws IOException {
		// define the configuration file content
		String cfgFile = "prop1=propval\n#comment1\n#comment2\n#prop=val";
		dummy.eol = "\n";

		StringBuilder sb = new StringBuilder(cfgFile);
		Map<String, String> props = dummy.parseFile(new ByteArrayInputStream(cfgFile.getBytes()));

		dummy.performPropertyDeletion(sb, props, new ChangeDescription("prop", ChangeDescription.TYPE_DELETE, true));
		cfgFile = sb.toString();

		assertEquals("prop1=propval\n", cfgFile);
	}

	@Test
	public void test_performPropertyDeletion_Of_Existing_Property_With_Deleting_Comments_And_A_New_Line_After_The_Value()
			throws IOException {
		// define the configuration file content
		String cfgFile = "#comment\nprop=true\n\n";
		dummy.eol = "\n";

		StringBuilder sb = new StringBuilder(cfgFile);
		Map<String, String> props = dummy.parseFile(new ByteArrayInputStream(cfgFile.getBytes()));

		dummy.performPropertyDeletion(sb, props, new ChangeDescription("prop", ChangeDescription.TYPE_DELETE, true));
		cfgFile = sb.toString();

		assertEquals("\n", cfgFile);
	}

	@Test
	public void test_performPropertyDeletion_Of_Existing_Property_Not_Commented_Without_Deleting_Comments() throws IOException {
		// define the configuration file content
		String cfgFile = "prop=val";
		dummy.eol = "\n";

		StringBuilder sb = new StringBuilder(cfgFile);
		Map<String, String> props = dummy.parseFile(new ByteArrayInputStream(cfgFile.getBytes()));

		dummy.performPropertyDeletion(sb, props, new ChangeDescription("prop", ChangeDescription.TYPE_DELETE, false));
		cfgFile = sb.toString();

		assertEquals("", cfgFile);
	}

	@Test
	public void test_performPropertyDeletion_Of_Existing_Property_Not_Commented_With_Deleting_Comments_But_Without_Comments_Block()
			throws IOException {
		// define the configuration file content
		String cfgFile = "prop1=val1\nprop=val";
		dummy.eol = "\n";

		StringBuilder sb = new StringBuilder(cfgFile);
		Map<String, String> props = dummy.parseFile(new ByteArrayInputStream(cfgFile.getBytes()));

		dummy.performPropertyDeletion(sb, props, new ChangeDescription("prop", ChangeDescription.TYPE_DELETE, true));
		cfgFile = sb.toString();

		assertEquals("prop1=val1\n", cfgFile);
	}

	@Test
	public void test_performPropertyDeletion_Of_Existing_Property_Not_Commented_With_Deleting_Comments_With_Comments_Block()
			throws IOException {
		// define the configuration file content
		String cfgFile = "prop1=val1\n#comment1\n\n#comment2\nprop=val";
		dummy.eol = "\n";

		StringBuilder sb = new StringBuilder(cfgFile);
		Map<String, String> props = dummy.parseFile(new ByteArrayInputStream(cfgFile.getBytes()));

		dummy.performPropertyDeletion(sb, props, new ChangeDescription("prop", ChangeDescription.TYPE_DELETE, true));
		cfgFile = sb.toString();

		assertEquals("prop1=val1\n#comment1\n\n", cfgFile);
	}

	@Test
	public void test_performPropertyDeletion_Of_Non_Existing_Property_Without_Deleting_Comments() throws IOException {
		// define the configuration file content
		String cfgFile = "";
		dummy.eol = "\n";

		StringBuilder sb = new StringBuilder(cfgFile);
		Map<String, String> props = dummy.parseFile(new ByteArrayInputStream(cfgFile.getBytes()));

		dummy.performPropertyDeletion(sb, props, new ChangeDescription("prop", ChangeDescription.TYPE_DELETE, false));
		cfgFile = sb.toString();

		assertEquals("", cfgFile);
	}

	@Test
	public void test_performPropertyDeletion_Of_Non_Existing_Property_With_Deleting_Comments() throws IOException {
		// define the configuration file content
		String cfgFile = "#comment";
		dummy.eol = "\n";

		StringBuilder sb = new StringBuilder(cfgFile);
		Map<String, String> props = dummy.parseFile(new ByteArrayInputStream(cfgFile.getBytes()));

		dummy.performPropertyDeletion(sb, props, new ChangeDescription("prop", ChangeDescription.TYPE_DELETE, true));
		cfgFile = sb.toString();

		assertEquals("#comment", cfgFile);
	}

	@Test
	public void test_firstIndexOfPropertyKey_When_Property_Does_Not_Exist_And_Is_Not_Commented() {
		dummy.eol = "\n";
		assertEquals(-1, dummy.firstIndexOfPropertyKey(new StringBuilder("prop1=val1\n\nprop2=val2"), "prop", false));
	}

	@Test
	public void test_firstIndexOfPropertyKey_When_Property_Does_Not_Exist_And_Is_Commented() {
		dummy.eol = "\n";
		assertEquals(-1, dummy.firstIndexOfPropertyKey(new StringBuilder("prop1=val1\n\nprop2=val2"), "prop", true));
	}

	@Test
	public void test_firstIndexOfPropertyKey_When_Property_Is_Commented_With_Many_Comment_Chars() {
		dummy.eol = "\n";
		assertEquals(12, dummy.firstIndexOfPropertyKey(new StringBuilder("prop1=val1\n\n# # #prop=val2"), "prop", true));
	}

	@Test
	public void test_firstIndexOfPropertyKey_When_Property_Exists_And_Is_Not_Commented() {
		dummy.eol = "\n";
		assertEquals(0, dummy.firstIndexOfPropertyKey(new StringBuilder("prop=val1\n\nprop2=val2"), "prop", false));
	}

	@Test
	public void test_firstIndexOfPropertyKey_When_Property_Exists_And_Is_Commented() {
		dummy.eol = "\n";
		assertEquals(12, dummy.firstIndexOfPropertyKey(new StringBuilder("prop1=val1\n\n#prop=val2"), "prop", true));
	}

	@Test
	public void test_firstIndexOfPropertyKey_When_Property_Exists_And_Is_Not_Commented_But_Is_Prefixed_With_One_That_Is_Commented() {
		dummy.eol = "\n";
		assertEquals(24, dummy.firstIndexOfPropertyKey(new StringBuilder("prop1=val1\n\n#prop=val2\n\nprop=val\n"), "prop", false));
	}

	@Test
	public void test_firstIndexOfPropertyKey_When_Property_Exists_Has_No_Value_And_Is_Commented() {
		dummy.eol = "\n";
		assertEquals(12, dummy.firstIndexOfPropertyKey(new StringBuilder("prop1=val1\n\n#prop\n"), "prop", true));
	}

	@Test
	public void test_firstIndexOfPropertyKey_When_Property_Exists_Has_No_Value_And_Is_Not_Commented() {
		dummy.eol = "\n";
		assertEquals(12, dummy.firstIndexOfPropertyKey(new StringBuilder("prop1=val1\n\nprop\n"), "prop", false));
	}

	@Test
	public void test_containsChar_Results_To_True() {
		assertTrue(dummy.containsChar(new StringBuilder("\n#2345=789\n#\n"), 3, 10, '='));
	}

	@Test
	public void test_containsChar_Results_To_False() {
		// end index is exclusive
		assertFalse(dummy.containsChar(new StringBuilder("\n#2345=789\n#\n"), 3, 10, '#'));
	}

	@Test
	public void test_indexOf_Missing_Character_Results_To_Minus_One() {
		assertEquals(-1, dummy.indexOf(new StringBuilder("\n#2345=789\n#\n"), 3, 10, 'm'));
	}

	@Test
	public void test_indexOf_Does_Not_Include_The_Last_Index() {
		assertEquals(-1, dummy.indexOf(new StringBuilder("\n#2345=789\n#\n"), 3, 10, '#'));
	}

	@Test
	public void test_indexOf_Is_Limited_By_The_Length_Of_The_String_Builder() {
		assertEquals(-1, dummy.indexOf(new StringBuilder("\n#2345=789\n#\n"), 3, 100, 'm'));
	}

	@Test
	public void test_indexOf_Is_Finding_The_Character_Index_In_The_Range() {
		assertEquals(6, dummy.indexOf(new StringBuilder("\n#=234=6789\n#\n"), 3, 10, '='));
	}

	@Test
	public void test_firstIndexOfPropertyValue_When_Value_Ends_With_EOL() {
		dummy.eol = "\n";
		assertEquals(17, dummy.firstIndexOfPropertyValue(new StringBuilder("prop1=val1\n\nprop=val\n"), 12, 4));
	}

	@Test
	public void test_firstIndexOfPropertyValue_When_Value_Does_Not_End_With_EOL() {
		dummy.eol = "\n";
		assertEquals(17, dummy.firstIndexOfPropertyValue(new StringBuilder("prop1=val1\n\nprop=val"), 12, 4));
	}

	@Test
	public void test_firstIndexOfPropertyValue_When_Value_Has_Preceiding_Space_Characters() throws IOException {
		dummy.eol = "\n";

		// we consider the first value after the assignment character as the
		// value char (even if it is white-space char).
		assertEquals(18, dummy.firstIndexOfPropertyValue(new StringBuilder("prop1=val1\n\nprop = val"), 12, 4));
	}

	@Test
	public void test_firstIndexOfPropertyValue_When_Value_Is_Assigned_With_Colon_Char() {
		dummy.eol = "\n";
		assertEquals(17, dummy.firstIndexOfPropertyValue(new StringBuilder("prop1=val1\n\nprop:val\n"), 12, 4));
	}

	@Test
	public void test_firstIndexOfPropertyValue_When_Value_Is_Missing_But_There_Is_An_Assignment_Char() {
		dummy.eol = "\n";
		assertEquals(-1, dummy.firstIndexOfPropertyValue(new StringBuilder("prop1=val1\n\nprop:\n"), 12, 4));
	}

	@Test
	public void test_firstIndexOfPropertyValue_When_Value_Is_Missing_But_There_Is_No_Assignment_Char() {
		dummy.eol = "\n";
		assertEquals(-1, dummy.firstIndexOfPropertyValue(new StringBuilder("prop1=val1\n\nprop\n"), 12, 4));
	}

	@Test
	public void test_firstIndexOfPropertyValue_When_Start_Index_Is_Negative() {
		dummy.eol = "\n";
		assertEquals(-1, dummy.firstIndexOfPropertyValue(new StringBuilder("prop1=val1\n\nprop\n"), -1, 4));
	}

	@Test
	public void test_lastIndexOfPropertyValue_When_Value_Start_Is_Negative() {
		dummy.eol = "\n";
		assertEquals(-1, dummy.lastIndexOfPropertyValue(new StringBuilder("prop1=val1\n\nprop\n"), -1));
	}

	@Test
	public void test_lastIndexOfPropertyValue_When_Does_Not_End_With_EOL() {
		dummy.eol = "\n";
		assertEquals(4, dummy.lastIndexOfPropertyValue(new StringBuilder("p=val"), 2));
	}

	@Test
	public void test_lastIndexOfPropertyValue_When_Ends_With_EOL() {
		dummy.eol = "\n";
		assertEquals(4, dummy.lastIndexOfPropertyValue(new StringBuilder("p=val\n"), 2));
	}

	@Test
	public void test_lastIndexOfPropertyValue_When_Value_Is_Split_On_Multiple_Lines() {
		dummy.eol = "\n";
		assertEquals(13, dummy.lastIndexOfPropertyValue(new StringBuilder("p=val1 \\ \nval2\n"), 2));
	}

	@Test
	public void test_lastIndexOfPropertyValue_When_Value_Is_Split_On_Multiple_Lines_With_No_EOL() {
		dummy.eol = "\n";
		assertEquals(19, dummy.lastIndexOfPropertyValue(new StringBuilder("p=val1 \\ \nval2\\\nval3"), 2));
	}

	@Test
	public void test_indexOfAssignmentCharacter_When_Character_Is_Equal_Sign() {
		dummy.eol = "\n";
		assertEquals(1, dummy.indexOfAssignmentCharacter(new StringBuilder("p=val1"), 1, 2));
	}

	@Test
	public void test_indexOfAssignmentCharacter_When_Character_Is_Colon() {
		dummy.eol = "\n";
		assertEquals(1, dummy.indexOfAssignmentCharacter(new StringBuilder("p:val1"), 1, 2));
	}

	@Test
	public void test_indexOfAssignmentCharacter_When_End_Index_Is_Bigger_Than_The_String_Buffer() {
		dummy.eol = "\n";
		assertEquals(1, dummy.indexOfAssignmentCharacter(new StringBuilder("p=val1"), 1, 100));
	}

	@Test
	public void test_indexOfAssignmentCharacter_When_No_Such_Char_Is_Available() {
		dummy.eol = "\n";
		assertEquals(-1, dummy.indexOfAssignmentCharacter(new StringBuilder("p=val1"), 2, 10));
	}

	@Test
	public void test_firstIndexOfCommentChar_When_Comment_Char_Is_First_On_The_Line() {
		dummy.eol = "\n";
		assertEquals(0, dummy.firstIndexOfCommentChar(new StringBuilder("#p=val1"), 0, 10));
	}

	@Test
	public void test_firstIndexOfCommentChar_When_Comment_Char_Is_First_On_The_Line_And_Is_Exclaimation_Sign() {
		dummy.eol = "\n";
		assertEquals(0, dummy.firstIndexOfCommentChar(new StringBuilder("!p=val1"), 0, 10));
	}

	@Test
	public void test_firstIndexOfCommentChar_When_Comment_Char_Has_White_Space_Chars_Preceeding_It() {
		dummy.eol = "\n";
		assertEquals(2, dummy.firstIndexOfCommentChar(new StringBuilder("  #p=val1"), 0, 10));
	}

	@Test
	public void test_firstIndexOfCommentChar_When_Comment_Char_Has_White_Space_Chars_Preceeding_It_And_Is_Exclaimation_Sign() {
		dummy.eol = "\n";
		assertEquals(2, dummy.firstIndexOfCommentChar(new StringBuilder("  !p=val1"), 0, 10));
	}

	@Test
	public void test_firstIndexOfCommentChar_When_Comment_Char_Has_Non_White_Space_Chars_Preceeding_It() {
		dummy.eol = "\n";
		assertEquals(-1, dummy.firstIndexOfCommentChar(new StringBuilder("  prop#p=val1"), 0, 10));
	}

	@Test
	public void test_firstIndexOfCommentChar_When_Comment_Char_Has_Non_White_Space_Chars_Preceeding_It_And_Is_Exclaimation_Sign() {
		dummy.eol = "\n";
		assertEquals(-1, dummy.firstIndexOfCommentChar(new StringBuilder("  prop!p=val1"), 0, 10));
	}

	@Test
	public void test_firstIndexOfCommentBlock_When_End_Is_Negative() {
		dummy.eol = "\n";
		assertEquals(-1, dummy.firstIndexOfCommentBlock(new StringBuilder("prop=val\n\n#comment\nprop2=val2\n"), -1));
	}

	@Test
	public void test_firstIndexOfCommentBlock_When_End_Is_After_The_EOL_Char() {
		dummy.eol = "\n";
		assertEquals(10, dummy.firstIndexOfCommentBlock(new StringBuilder("prop=val\n\n#comment\nprop2=val2\n"), 19));
	}

	@Test
	public void test_firstIndexOfCommentBlock_When_End_Is_At_The_EOL_Char() {
		dummy.eol = "\n";
		assertEquals(10, dummy.firstIndexOfCommentBlock(new StringBuilder("prop=val\n\n#comment\nprop2=val2\n"), 18));
	}

	@Test
	public void test_firstIndexOfCommentBlock_When_End_Is_Before_The_EOL_Char() {
		dummy.eol = "\n";
		assertEquals(-1, dummy.firstIndexOfCommentBlock(new StringBuilder("prop=val\n\n#comment\nprop2=val2\n"), 17));
	}

	@Test
	public void test_getEndIndex() {
		assertEquals(10, dummy.getEndIndex(-1, 10));
		assertEquals(3, dummy.getEndIndex(3, 10));
		assertEquals(10, dummy.getEndIndex(13, 10));
	}

	@Test
	public void test_getStartIndex() {
		assertEquals(0, dummy.getStartIndex(-1));
		assertEquals(3, dummy.getStartIndex(3));
	}

	private static class DummyBaseUtility extends BaseMigrationUtility {

		@Override
		protected List<ChangeDescription> defineChanges(Map<String, String> props) {
			return null;
		}
	}

	private static abstract class ChangesCounter extends BaseMigrationUtility {

		int uncommentedCount;
		int commentedCount;
		int addedCount;
		int modifiedCount;
		int deletedCount;

		@Override
		protected void performPropertyUncommenting(StringBuilder sb, Map<String, String> props, ChangeDescription change) {
			uncommentedCount++;
		}

		@Override
		protected void performPropertyCommenting(StringBuilder sb, Map<String, String> props, ChangeDescription change) {
			commentedCount++;
		}

		@Override
		protected void performPropertyAddition(StringBuilder sb, Map<String, String> props, ChangeDescription change) {
			addedCount++;
		}

		@Override
		protected void performPropertyModification(StringBuilder sb, Map<String, String> props, ChangeDescription change) {
			modifiedCount++;
		}

		@Override
		protected void performPropertyDeletion(StringBuilder sb, Map<String, String> propsAvailable, ChangeDescription change) {
			deletedCount++;
		}
	}
}
