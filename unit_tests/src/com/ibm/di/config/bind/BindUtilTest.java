package com.ibm.di.config.bind;

import static com.ibm.di.test.config.bind.Match.*;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.hamcrest.Matcher;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.test.utils.ConfigUtils;

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
public class BindUtilTest {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static MetamergeConfig orgCfg;
	private static SolutionBinding orgBind;

	private static MetamergeConfig prodCfg;
	private static SolutionBinding prodBind;

	@BeforeClass
	public static void setUpClass() throws Exception {
		orgCfg = ConfigUtils.deserializeConfig(new File("resources/config/bind/test.xml"));
		orgBind = BindUtil.fromMetamergeConfig(orgCfg);

		// in order to test toMC we use the fromMC for which we have a set of
		// Matchers to verify.
		prodCfg = BindUtil.toMetamergeConfig(orgBind);
		prodBind = BindUtil.fromMetamergeConfig(prodCfg);
	}

	@Test
	public void test_Direct_Folder_Distribution() throws Exception {
		test_Folder_Distribution(orgBind);
	}

	@Test
	public void test_Indirect_Folder_Distribution() throws Exception {
		test_Folder_Distribution(prodBind);
	}

	public void test_Folder_Distribution(SolutionBinding bind) throws Exception {
		assertContainsInAnyOrder(bind.getContainers(), list(container("Connectors"), container("Parsers"), container("Functions"),
				container("Includes"), container("Properties"), container("AttributeMaps"), container("AssemblyLines"),
				container("Scripts")));
	}

	@Test
	public void test_Direct_Connectors_Folder_Contains_Correct_Configs() {
		test_Connectors_Folder_Contains_Correct_Configs(orgBind);
	}

	@Test
	public void test_Indirect_Connectors_Folder_Contains_Correct_Configs() {
		test_Connectors_Folder_Contains_Correct_Configs(prodBind);
	}

	public void test_Connectors_Folder_Contains_Correct_Configs(SolutionBinding bind) {
		ContainerBinding cb = (ContainerBinding) find("Connectors", bind.getContainers());
		assertContainsInAnyOrder(cb.getConfigs(), list(name("testAddOnly"), name("testDelete"), name("testIterator"),
				name("testLookup"), name("testUpdate"), name("testDelta")));
	}

	@Test
	public void test_Direct_AddOnlyConnector() {
		test_AddOnlyConnector(orgBind);
	}

	@Test
	public void test_Indirect_AddOnlyConnector() {
		test_AddOnlyConnector(prodBind);
	}

	@SuppressWarnings("unchecked")
	public void test_AddOnlyConnector(SolutionBinding bind) {
		ContainerBinding cb = (ContainerBinding) find("Connectors", bind.getContainers());
		ConnectorBinding conn = (ConnectorBinding) find("testAddOnly", cb.getConfigs());

		assertThat(conn,
				connector("testAddOnly", "system:/Connectors/ibmdi.LDAP",
				// raw config
						jclass(null, BaseConfiguration.INHERIT_PARENT, null, param("debug", "false"), param("ldapUrl",
								"ldap://test:389"), param("ldapUsername", ""), param("userComment", "")),
						// parser
						parser(null, BaseConfiguration.INHERIT_PARENT, jclass(null, BaseConfiguration.INHERIT_PARENT, null,
								(Matcher[]) null), list(
						// Input
								schema("Input", BaseConfiguration.INHERIT_PARENT, (Matcher[]) null),
								// Output
								schema("Output", BaseConfiguration.INHERIT_PARENT, (Matcher[]) null))),
						// schemas
						new Matcher[] { schema("Input", BaseConfiguration.INHERIT_PARENT, (Matcher<SchemaItemBinding>[]) null),
								schema("Output", BaseConfiguration.INHERIT_PARENT, (Matcher<SchemaItemBinding>[]) null) },
						// maps
						new Matcher[] {
								map("Input", BaseConfiguration.INHERIT_PARENT, (Matcher<AttributeMapItemBinding>[]) null),
								map("Output", "/AttributeMaps/testMap", mItem("newAttr", null, AttributeMapItemTypeEnum.SIMPLE,
										"newAttr")) },
						// hooks
						hooks(null, BaseConfiguration.INHERIT_PARENT, hook("before_add", null, false, ""), hook("override_add",
								null, true, "test")),
						// mode
						ConnectorModeEnum.ADD_ONLY, null,
						// link
						null,
						// delta
						null,
						// poolDef
						poolDef(true, 10, 10, 10, 10, 10),
						// poolInst
						is(notNullValue(PoolInstanceBinding.class))));
	}

	@Test
	public void test_Direct_DeleteConnector() {
		test_DeleteConnector(orgBind);
	}

	@Test
	public void test_Indirect_DeleteConnector() {
		test_DeleteConnector(prodBind);
	}

	@SuppressWarnings("unchecked")
	public void test_DeleteConnector(SolutionBinding bind) {
		ContainerBinding cb = (ContainerBinding) find("Connectors", bind.getContainers());
		ConnectorBinding conn = (ConnectorBinding) find("testDelete", cb.getConfigs());

		assertThat(conn,
				connector("testDelete", "system:/Connectors/ibmdi.LDAP",
				// raw config
						jclass(null, BaseConfiguration.INHERIT_PARENT, null, param("debug", "false"), param("ldapUrl",
								"ldap://test:389"), param("userComment", "")),
						// parser
						parser(null, BaseConfiguration.INHERIT_PARENT, jclass(null, BaseConfiguration.INHERIT_PARENT, null,
								(Matcher[]) null), list(
						// Input
								schema("Input", BaseConfiguration.INHERIT_PARENT, (Matcher[]) null),
								// Output
								schema("Output", BaseConfiguration.INHERIT_PARENT, (Matcher[]) null))),
						// schemas
						new Matcher[] { schema("Input", BaseConfiguration.INHERIT_PARENT, (Matcher<SchemaItemBinding>[]) null),
								schema("Output", BaseConfiguration.INHERIT_PARENT, (Matcher<SchemaItemBinding>[]) null) },
						// maps
						new Matcher[] {
								map("Output", BaseConfiguration.INHERIT_PARENT, (Matcher<AttributeMapItemBinding>[]) null),
								map("Input", "/AttributeMaps/testMap", mItem("newAttr", null, AttributeMapItemTypeEnum.SIMPLE,
										"newAttr")) },
						// hooks
						hooks(null, BaseConfiguration.INHERIT_PARENT, hook("override_delete", null, true, "test")),
						// mode
						ConnectorModeEnum.DELETE,
						// mode config
						is(notNullValue(ConnectorModeBinding.class)),
						// link
						link(null, BaseConfiguration.INHERIT_PARENT, true, "testScript"),
						// delta
						null,
						// poolDef
						is(notNullValue(PoolDefinitionBinding.class)),
						// poolInst
						is(notNullValue(PoolInstanceBinding.class))));
	}

	@Test
	public void test_Direct_IteratorConnector() {
		test_IteratorConnector(orgBind);
	}

	@Test
	public void test_Indirect_IteratorConnector() {
		test_IteratorConnector(prodBind);
	}

	@SuppressWarnings("unchecked")
	public void test_IteratorConnector(SolutionBinding bind) {
		ContainerBinding cb = (ContainerBinding) find("Connectors", bind.getContainers());
		ConnectorBinding conn = (ConnectorBinding) find("testIterator", cb.getConfigs());

		assertThat(conn,
				connector("testIterator", "system:/Connectors/ibmdi.LDAP",
				// raw config
						jclass(null, BaseConfiguration.INHERIT_PARENT, null, param("debug", "false"), param("ldapUrl",
								"ldap://test:389"), param("userComment", "")),
						// parser
						parser(null, BaseConfiguration.INHERIT_PARENT, jclass(null, BaseConfiguration.INHERIT_PARENT, null,
								(Matcher[]) null), list(
						// Input
								schema("Input", BaseConfiguration.INHERIT_PARENT, (Matcher[]) null),
								// Output
								schema("Output", BaseConfiguration.INHERIT_PARENT, (Matcher[]) null))),
						// schemas
						new Matcher[] { schema("Input", BaseConfiguration.INHERIT_PARENT, (Matcher<SchemaItemBinding>[]) null),
								schema("Output", BaseConfiguration.INHERIT_PARENT, (Matcher<SchemaItemBinding>[]) null) },
						// maps
						new Matcher[] {
								map("Output", BaseConfiguration.INHERIT_PARENT, (Matcher<AttributeMapItemBinding>[]) null),
								map("Input", "/AttributeMaps/testMap", mItem("newAttr", null, AttributeMapItemTypeEnum.SIMPLE,
										"newAttr")) },
						// hooks
						hooks(null, BaseConfiguration.INHERIT_PARENT, hook("override_getnext", null, true, "test")),
						// mode
						ConnectorModeEnum.ITERATOR,
						// mode config
						null,
						// link
						null,
						// delta
						delta(true, "test", "test", true, true, true, true, true, DeltaCommitEnum.ON_AL_CYCLE,
								DeltaRowLockingEnum.READ_COMMITED, DeltaChangeDetectionModeEnum.IGNORE_ATTRIBUTES, "test, test"),
						// poolDef
						poolDef(true, 10, 10, 10, 10, 10),
						// poolInst
						is(notNullValue(PoolInstanceBinding.class))));
	}

	@Test
	public void test_Direct_LookupConnector() {
		test_LookupConnector(orgBind);
	}

	@Test
	public void test_Indirect_LookupConnector() {
		test_LookupConnector(prodBind);
	}

	@SuppressWarnings("unchecked")
	public void test_LookupConnector(SolutionBinding bind) {
		ContainerBinding cb = (ContainerBinding) find("Connectors", bind.getContainers());
		ConnectorBinding conn = (ConnectorBinding) find("testLookup", cb.getConfigs());

		assertThat(conn, connector("testLookup", "system:/Connectors/ibmdi.LDAP",
		// raw config
				jclass(null, BaseConfiguration.INHERIT_PARENT, null, param("debug", "false"), param("ldapUrl", "ldap://test:389"),
						param("userComment", "")),
				// parser
				parser(null, BaseConfiguration.INHERIT_PARENT, jclass(null, BaseConfiguration.INHERIT_PARENT, null,
						(Matcher[]) null), list(
				// Input
						schema("Input", BaseConfiguration.INHERIT_PARENT, (Matcher[]) null),
						// Output
						schema("Output", BaseConfiguration.INHERIT_PARENT, (Matcher[]) null))),
				// schemas
				new Matcher[] { schema("Input", BaseConfiguration.INHERIT_PARENT, (Matcher<SchemaItemBinding>[]) null),
						schema("Output", BaseConfiguration.INHERIT_PARENT, (Matcher<SchemaItemBinding>[]) null) },
				// maps
				new Matcher[] {
						map("Output", BaseConfiguration.INHERIT_PARENT, (Matcher<AttributeMapItemBinding>[]) null),
						map("Input", BaseConfiguration.INHERIT_NONE, mItem("newAttr", null, AttributeMapItemTypeEnum.SIMPLE,
								"newAttr")) },
				// hooks
				hooks(null, BaseConfiguration.INHERIT_PARENT, hook("override_lookup", null, true, "test")),
				// mode
				ConnectorModeEnum.LOOKUP,
				// mode config
				is(notNullValue(ConnectorModeBinding.class)),
				// link
				link(null, BaseConfiguration.INHERIT_PARENT, new Matcher[] {
						lItem("12b14079c2c", "advanced", "greater than", "{javascript ret.value = value}"),
						lItem("12b1417a88e", "simple", "less than", "value"),
						lItem("12b14182488", "propety", "equals", "{property.Global-Properties:api.on}") }, false),
				// delta
				null,
				// poolDef
				poolDef(true, 10, 10, 10, 10, 10),
				// poolInst
				is(notNullValue(PoolInstanceBinding.class))));
	}

	@Test
	public void test_Direct_UpdateConnector() {
		test_UpdateConnector(orgBind);
	}

	@Test
	public void test_Indirect_UpdateConnector() {
		test_UpdateConnector(prodBind);
	}

	@SuppressWarnings("unchecked")
	public void test_UpdateConnector(SolutionBinding bind) {
		ContainerBinding cb = (ContainerBinding) find("Connectors", bind.getContainers());
		ConnectorBinding conn = (ConnectorBinding) find("testUpdate", cb.getConfigs());

		assertThat(conn,
				connector("testUpdate", "system:/Connectors/ibmdi.LDAP",
				// raw config
						jclass(null, BaseConfiguration.INHERIT_PARENT, null, param("debug", "false"), param("ldapUrl",
								"ldap://test:389"), param("userComment", "")),
						// parser
						parser(null, BaseConfiguration.INHERIT_PARENT, jclass(null, BaseConfiguration.INHERIT_PARENT, null,
								(Matcher[]) null), list(
						// Input
								schema("Input", BaseConfiguration.INHERIT_PARENT, (Matcher[]) null),
								// Output
								schema("Output", BaseConfiguration.INHERIT_PARENT, (Matcher[]) null))),
						// schemas
						new Matcher[] { schema("Input", BaseConfiguration.INHERIT_PARENT, (Matcher<SchemaItemBinding>[]) null),
								schema("Output", BaseConfiguration.INHERIT_PARENT, (Matcher<SchemaItemBinding>[]) null) },
						// maps
						new Matcher[] {
								map("Input", BaseConfiguration.INHERIT_PARENT, (Matcher<AttributeMapItemBinding>[]) null),
								map("Output", BaseConfiguration.INHERIT_PARENT, mItem("newAttr", null,
										AttributeMapItemTypeEnum.SIMPLE, "newAttr")) },
						// hooks
						hooks(null, BaseConfiguration.INHERIT_PARENT, hook("update_multiple", null, true, "test")),
						// mode
						ConnectorModeEnum.UPDATE,
						// mode config
						mode(true),
						// link
						link(null, BaseConfiguration.INHERIT_PARENT,
								new Matcher[] { lItem("12b141afc15", "simple", "equals", null) }, true),
						// delta
						null,
						// poolDef
						poolDef(true, 10, 10, 10, 10, 10),
						// poolInst
						is(notNullValue(PoolInstanceBinding.class))));
	}

	@Test
	public void test_Direct_DeltaConnector() {
		test_DeltaConnector(orgBind);
	}

	@Test
	public void test_Indirect_DeltaConnector() {
		test_DeltaConnector(prodBind);
	}

	@SuppressWarnings("unchecked")
	public void test_DeltaConnector(SolutionBinding bind) {
		ContainerBinding cb = (ContainerBinding) find("Connectors", bind.getContainers());
		ConnectorBinding conn = (ConnectorBinding) find("testDelta", cb.getConfigs());

		assertThat(conn, connector("testDelta", "system:/Connectors/ibmdi.LDAP",
		// raw config
				jclass(null, BaseConfiguration.INHERIT_PARENT, null, param("debug", "false"), param("ldapUrl", "ldap://test:389"),
						param("userComment", "")),
				// parser
				parser(null, BaseConfiguration.INHERIT_PARENT, jclass(null, BaseConfiguration.INHERIT_PARENT, null,
						(Matcher[]) null), list(
				// Input
						schema("Input", BaseConfiguration.INHERIT_PARENT, (Matcher[]) null),
						// Output
						schema("Output", BaseConfiguration.INHERIT_PARENT, (Matcher[]) null))),
				// schemas
				new Matcher[] { schema("Input", BaseConfiguration.INHERIT_PARENT, (Matcher<SchemaItemBinding>[]) null),
						schema("Output", BaseConfiguration.INHERIT_PARENT, (Matcher<SchemaItemBinding>[]) null) },
				// maps
				new Matcher[] { map("Input", BaseConfiguration.INHERIT_PARENT, (Matcher<AttributeMapItemBinding>[]) null),
						map("Output", "/Connectors/testAddOnly", (Matcher<AttributeMapItemBinding>[]) null) },
				// hooks
				hooks(null, BaseConfiguration.INHERIT_PARENT, hook("override_delta", null, true, "test")),
				// mode
				ConnectorModeEnum.DELTA,
				// mode config
				mode(false, false),
				// link
				link(null, BaseConfiguration.INHERIT_PARENT, new Matcher[] { lItem("12b14030720", "test", "not equals", "value") },
						true),
				// delta
				null,
				// poolDef
				poolDef(true, 10, 10, 10, 10, 10),
				// poolInst
				is(notNullValue(PoolInstanceBinding.class))));
	}

	@Test
	public void test_Direct_Parsers_Folder_Contains_Correct_Configs() {
		test_Parsers_Folder_Contains_Correct_Configs(orgBind);
	}

	@Test
	public void test_Indirect_Parsers_Folder_Contains_Correct_Configs() {
		test_Parsers_Folder_Contains_Correct_Configs(prodBind);
	}

	public void test_Parsers_Folder_Contains_Correct_Configs(SolutionBinding bind) {
		ContainerBinding pb = (ContainerBinding) find("Parsers", bind.getContainers());
		assertThat(pb.getConfigs(), contains(name("CSV")));
	}

	@Test
	public void test_Direct_CSVParser() throws Exception {
		test_CSVParser(orgBind);
	}

	@Test
	public void test_Indirect_CSVParser() throws Exception {
		test_CSVParser(prodBind);
	}

	@SuppressWarnings("unchecked")
	public void test_CSVParser(SolutionBinding bind) throws Exception {
		ContainerBinding pb = (ContainerBinding) find("Parsers", bind.getContainers());
		ParserBinding parser = (ParserBinding) find("CSV", pb.getConfigs());
		assertThat(parser, parser("CSV", "system:/Parsers/ibmdi.CSV", jclass(null, BaseConfiguration.INHERIT_PARENT, null,
				(Matcher[]) null), list(schema("Input", BaseConfiguration.INHERIT_PARENT, (Matcher[]) null), schema("Output",
				BaseConfiguration.INHERIT_PARENT, (Matcher[]) null))));
	}

	@Test
	public void test_Direct_Scripts_Folder_Contains_Correct_Configs() {
		test_Scripts_Folder_Contains_Correct_Configs(orgBind);
	}

	@Test
	public void test_Indirect_Scripts_Folder_Contains_Correct_Configs() {
		test_Scripts_Folder_Contains_Correct_Configs(prodBind);
	}

	public void test_Scripts_Folder_Contains_Correct_Configs(SolutionBinding bind) {
		ContainerBinding cb = (ContainerBinding) find("Scripts", bind.getContainers());
		assertThat(cb.getConfigs(), contains(name("testScript")));
	}

	@Test
	public void test_Direct_Script() throws Exception {
		test_Script(orgBind);
	}

	@Test
	public void test_Indirect_Script() throws Exception {
		test_Script(prodBind);
	}

	public void test_Script(SolutionBinding bind) throws Exception {
		ContainerBinding cb = (ContainerBinding) find("Scripts", bind.getContainers());
		ScriptBinding script = (ScriptBinding) find("testScript", cb.getConfigs());
		assertThat(script, script("testScript", null, "test", true));
	}

	@Test
	public void test_Direct_Functions_Folder_Contains_Correct_Configs() {
		test_Functions_Folder_Contains_Correct_Configs(orgBind);
	}

	@Test
	public void test_Indirect_Functions_Folder_Contains_Correct_Configs() {
		test_Functions_Folder_Contains_Correct_Configs(prodBind);
	}

	public void test_Functions_Folder_Contains_Correct_Configs(SolutionBinding bind) {
		ContainerBinding cb = (ContainerBinding) find("Functions", bind.getContainers());
		assertThat(cb.getConfigs(), contains(name("ParserFC")));
	}

	@Test
	public void test_Direct_ParserFC() throws Exception {
		test_ParserFC(orgBind);
	}

	@Test
	public void test_Indirect_ParserFC() throws Exception {
		test_ParserFC(prodBind);
	}

	@SuppressWarnings("unchecked")
	public void test_ParserFC(SolutionBinding bind) throws Exception {
		ContainerBinding cb = (ContainerBinding) find("Functions", bind.getContainers());
		FunctionBinding func = (FunctionBinding) find("ParserFC", cb.getConfigs());

		assertThat(func, function(
		// name,
				"ParserFC",
				// inheritFrom,
				"system:/Functions/ibmdi.ParserFC",
				// jclass,
				jclass(null, BaseConfiguration.INHERIT_PARENT, null, param("debug", "false"), param("userComment", "")),
				// parser,
				parser(null, "system:/Functions/ibmdi.ParserFC", jclass(null, BaseConfiguration.INHERIT_PARENT, null,
						(Matcher[]) null), list(
				// Input
						schema("Input", BaseConfiguration.INHERIT_PARENT, (Matcher[]) null),
						// Output
						schema("Output", BaseConfiguration.INHERIT_PARENT, (Matcher[]) null))),
				// schemas,
				new Matcher[] { schema("Input", BaseConfiguration.INHERIT_PARENT, (Matcher<SchemaItemBinding>[]) null),
						schema("Output", BaseConfiguration.INHERIT_PARENT, (Matcher<SchemaItemBinding>[]) null) },
				// maps,
				new Matcher[] {
						map("Input", BaseConfiguration.INHERIT_PARENT, mItem("inputAttr", null, AttributeMapItemTypeEnum.SIMPLE,
								"inputAttr")),
						map("Output", BaseConfiguration.INHERIT_PARENT, mItem("outputAttr", null, AttributeMapItemTypeEnum.SIMPLE,
								"outputAttr")) },
				// hooks
				hooks(null, BaseConfiguration.INHERIT_PARENT, hook("before_functioncall", null, true, "test"))));
	}

	@Test
	public void test_Direct_AttributeMaps_Folder_Contains_Correct_Configs() {
		test_AttributeMaps_Folder_Contains_Correct_Configs(orgBind);
	}

	@Test
	public void test_Indirect_AttributeMaps_Folder_Contains_Correct_Configs() {
		test_AttributeMaps_Folder_Contains_Correct_Configs(prodBind);
	}

	public void test_AttributeMaps_Folder_Contains_Correct_Configs(SolutionBinding bind) {
		ContainerBinding cb = (ContainerBinding) find("AttributeMaps", bind.getContainers());
		assertThat(cb.getConfigs(), contains(name("testMap")));
	}

	@Test
	public void test_Direct_AttributeMap() throws Exception {
		test_AttributeMap(orgBind);
	}

	@Test
	public void test_Indirect_AttributeMap() throws Exception {
		test_AttributeMap(prodBind);
	}

	@SuppressWarnings("unchecked")
	public void test_AttributeMap(SolutionBinding bind) throws Exception {
		ContainerBinding cb = (ContainerBinding) find("AttributeMaps", bind.getContainers());
		AttributeMapBinding map = (AttributeMapBinding) find("testMap", cb.getConfigs());

		assertThat(map, map("testMap", BaseConfiguration.INHERIT_PARENT, list(mItem("advanced", null,
				AttributeMapItemTypeEnum.ADVANCED, "ret.value = work.advanced;"), mItem("simple", null,
				AttributeMapItemTypeEnum.SIMPLE, "simple"), mItem("substitution", null, AttributeMapItemTypeEnum.SUBSTITUTION,
				"{work.substitution}")), nullB("Null", null, "AbsentAttribute")));
	}

	@Test
	public void test_Direct_PropertyStores() throws Exception {
		test_PropertyStores(orgBind);
	}

	@Test
	public void test_Indirect_PropertyStores() throws Exception {
		test_PropertyStores(prodBind);
	}

	@SuppressWarnings("unchecked")
	public void test_PropertyStores(SolutionBinding bind) throws Exception {
		PropertyStoresBinding props = (PropertyStoresBinding) find("Properties", bind.getContainers());
		assertThat(props, propStores("Properties", "System-Properties", "test", list(

		propStore("Solution-Properties", null, "key", "value", false, true, 0, jclass(null, "system:/Connectors/ibmdi.Properties",
				null, param("collectionType", "Solution-Properties")), jclass(null, null, null, (Matcher[]) null)),

		propStore("Global-Properties", null, "key", "value", false, true, 0, jclass(null, "system:/Connectors/ibmdi.Properties",
				null, param("collectionType", "Global-Properties")), jclass(null, null, null, (Matcher[]) null)),

		propStore("System-Properties", null, "key", "value", false, true, 0, jclass(null, "system:/Connectors/ibmdi.Properties",
				null, param("collectionType", "System-Properties")), jclass(null, null, null, (Matcher[]) null)),

		propStore("Java-Properties", null, "key", "value", false, true, 0, jclass(null, "system:/Connectors/ibmdi.Properties",
				null, param("collectionType", "Java-Properties"), param("collection",
						"D:\\prj\\workspace\\unit_tests\\resources\\config\\bind/test.properties")), jclass(null, null, null,
				(Matcher[]) null)),

		propStore("test", null, "key", "value", false, true, 0, jclass(null, "system:/Connectors/ibmdi.Properties", null, param(
				"collection", "D:\\prj\\workspace\\unit_tests\\resources\\config\\bind/test.properties"), param("collectionType",
				"test")), jclass(null, null, null, (Matcher[]) null)))));

	}

	@Test
	public void test_Direct_AssemblyLines_Folder_Contains_Correct_Configs() {
		test_AssemblyLines_Folder_Contains_Correct_Configs(orgBind);
	}

	@Test
	public void test_Indirect_AssemblyLines_Folder_Contains_Correct_Configs() {
		test_AssemblyLines_Folder_Contains_Correct_Configs(prodBind);
	}

	public void test_AssemblyLines_Folder_Contains_Correct_Configs(SolutionBinding bind) {
		ContainerBinding cb = (ContainerBinding) find("AssemblyLines", bind.getContainers());
		assertThat(cb.getConfigs(), contains(name("testAl")));
	}

	@Test
	public void test_Direct_AssemblyLine_testAl() throws Exception {
		test_AssemblyLine_testAl(orgBind);
	}

	@Test
	public void test_Indirect_AssemblyLine_testAl() throws Exception {
		test_AssemblyLine_testAl(prodBind);
	}

	@SuppressWarnings("unchecked")
	public void test_AssemblyLine_testAl(SolutionBinding bind) throws Exception {
		ContainerBinding cb = (ContainerBinding) find("AssemblyLines", bind.getContainers());
		AssemblyLineBinding al = (AssemblyLineBinding) find("testAl", cb.getConfigs());

		assertThat(
				al,
				al(
				// name
						"testAl",
						// settings
						params(null, null,
						// params
								param("ALPoolSettingsDialog", "false"), param("automapattributes", "true"), param(
										"createTombstones", "true"), param("debug", "true"), param("findreturncount", "10"), param(
										"get_history", "test"), param("includeGlobalPrologs", "true"), param("includePrologs",
										"testScript"), param("maxerr", "10"), param("maxread", "10"), param("nullBehaviorDialog",
										"false"), param("set_history", "test"), param("verbose", "10")),

						// hooks
						hooks(null, null, hook("startcycle", null, true, "test")),
						// sandbox
						sandbox("sandbox"),
						// simulation
						simulation(proxyAl("test", "test", "test", ProxyALModeEnum.SYNC, false)),
						// logging
						logs(
						// appender
								log("CustomAppender", "system:/Loggers/ibmdi.CustomAppender",
										param("com.ibm.di.log.level", "INFO"), param("enabled", "true")),
								// appender
								log("ConsoleAppender", "system:/Loggers/ibmdi.ConsoleAppender", param("Pattern.ConversionPattern",
										"%d{DEFAULT} %-5p [%c] - %m%n"), param("com.ibm.di.log.layout", "Pattern"), param(
										"com.ibm.di.log.level", "INFO"), param("enabled", "false"))),
						// containers
						list(
						// feed container
								alContainer("EntryFeedContainer",
								// complex component
										complex("SNMPServerConnector",
												// connector config
												connector(
												// name
														null,
														// inherit from
														"system:/Connectors/ibmdi.SNMPServerConnector",
														// raw config
														jclass(null, BaseConfiguration.INHERIT_PARENT, null,
																param("debug", "false"), param("userComment", "")),
														// parser
														parser(null, BaseConfiguration.INHERIT_PARENT, jclass(null,
																BaseConfiguration.INHERIT_PARENT, null, (Matcher[]) null),
																list(
																// Input
																		schema("Input", BaseConfiguration.INHERIT_PARENT,
																				(Matcher[]) null),
																		// Output
																		schema("Output", BaseConfiguration.INHERIT_PARENT,
																				(Matcher[]) null))),
														// schemas
														list(
														// Input
																schema("Input", BaseConfiguration.INHERIT_PARENT,
																// items
																		list(
																				sItem("snmp.remoteip", 0, "1", null,
																						SchemaItemTypeEnum.ATTRIBUTE, null,
																						"String", null), sItem("snmp.request-id",
																						0, "1", null, SchemaItemTypeEnum.ATTRIBUTE,
																						null, "Integer", null))),
																// Output
																schema("Output", BaseConfiguration.INHERIT_PARENT,
																// items
																		sItem("testAttr", 0, "1", sItem("childAttr", 0, "1")))),
														// maps
														list(map("Input", BaseConfiguration.INHERIT_PARENT), map("Output",
																BaseConfiguration.INHERIT_PARENT)),
														// hooks
														hooks(null, BaseConfiguration.INHERIT_PARENT),
														// mode
														ConnectorModeEnum.SERVER,
														// mode config
														null,
														// link
														null,
														// delta
														null,
														// poolDef
														is(notNullValue(PoolDefinitionBinding.class)),
														// poolInst
														is(notNullValue(PoolInstanceBinding.class))), "Enabled",
												ALComponentInitializeEnum.ON_STARTUP, "Enabled", false, false)),
								// flow container
								alContainer(
										"DataFlowContainer",
										// testAddOnly
										complex(
												"testAddOnly",
												// connector config
												connector(
												// name
														null,
														// inherit from
														"/Connectors/testAddOnly",
														// raw config
														jclass(null, BaseConfiguration.INHERIT_PARENT, null, (Matcher[]) null),
														// parser
														parser(null, BaseConfiguration.INHERIT_PARENT, jclass(null,
																BaseConfiguration.INHERIT_PARENT, null, (Matcher[]) null),
																list(
																// Input
																		schema("Input", BaseConfiguration.INHERIT_PARENT,
																				(Matcher[]) null),
																		// Output
																		schema("Output", BaseConfiguration.INHERIT_PARENT,
																				(Matcher[]) null))),
														// schemas
														list(
														// Input
																schema("Input", BaseConfiguration.INHERIT_PARENT, (Matcher[]) null),
																// Output
																schema("Output", BaseConfiguration.INHERIT_PARENT, (Matcher[]) null)),
														// maps
														list(map("Input", BaseConfiguration.INHERIT_PARENT), map("Output",
																BaseConfiguration.INHERIT_PARENT)),
														// hooks
														hooks(null, BaseConfiguration.INHERIT_PARENT),
														// mode
														ConnectorModeEnum.ADD_ONLY,
														// mode config
														null,
														// link
														null,
														// delta
														null,
														// poolDef
														is(notNullValue(PoolDefinitionBinding.class)),
														// poolInst
														poolInst(true, PoolInstanceExhaustedEnum.WAIT)), "Passive",
												ALComponentInitializeEnum.ON_EVERY_USE, "Scripted", false, false),
										// IF
										composite("IF",
										// if branch
												branch(BranchTypeEnum.IF,
												// condition
														condition(false,
														// items
																cItem("test", "equals", "value", true, false))),
												// composite state
												ALComponentStateEnum.ENABLED,
												// children
												complex("http",
														// connector config
														connector(
														// name
																null,
																// inherit from
																"system:/Connectors/ibmdi.HTTPClient",
																// raw config
																jclass(null, BaseConfiguration.INHERIT_PARENT, null, param("debug",
																		"false"), param("userComment", "")),
																// parser
																parser(null, "system:/Parsers/ibmdi.Simple", jclass(null,
																		BaseConfiguration.INHERIT_PARENT, null, param("debug",
																				"false"), param("omitxmldeclaration", "false"),
																		param("userComment", "")), list(
																// Input
																		schema("Input", BaseConfiguration.INHERIT_PARENT,
																				(Matcher[]) null),
																		// Output
																		schema("Output", BaseConfiguration.INHERIT_PARENT,
																				(Matcher[]) null))),
																// schemas
																list(
																// Input
																		schema("Input", BaseConfiguration.INHERIT_PARENT,
																				(Matcher[]) null),
																		// Output
																		schema("Output", BaseConfiguration.INHERIT_PARENT,
																				(Matcher[]) null)),
																// maps
																list(map("Input", BaseConfiguration.INHERIT_PARENT), map("Output",
																		BaseConfiguration.INHERIT_PARENT)),
																// hooks
																hooks(null, BaseConfiguration.INHERIT_PARENT),
																// mode
																ConnectorModeEnum.CALL_REPLY,
																// mode config
																null,
																// link
																null,
																// delta
																null,
																// poolDef
																is(notNullValue(PoolDefinitionBinding.class)),
																// poolInst
																is(notNullValue(PoolInstanceBinding.class))), "Enabled",
														ALComponentInitializeEnum.ON_STARTUP, "Enabled", true, true)),
										// ELSEIF
										composite("ELSEIF",
										// else-if branch
												branch(BranchTypeEnum.ELSE_IF,
												// condition
														condition("ret.value = true;")),
												// composite state
												ALComponentStateEnum.ENABLED,
												// children
												complex("testDelete",
														// connector config
														connector(
														// name
																null,
																// inherit from
																"/Connectors/testDelete",
																// raw config
																jclass(null, BaseConfiguration.INHERIT_PARENT, null,
																		(Matcher[]) null),
																// parser
																parser(null, BaseConfiguration.INHERIT_PARENT, jclass(null,
																		BaseConfiguration.INHERIT_PARENT, null, (Matcher[]) null),
																		list(
																		// Input
																				schema("Input", BaseConfiguration.INHERIT_PARENT,
																						(Matcher[]) null),
																				// Output
																				schema("Output", BaseConfiguration.INHERIT_PARENT,
																						(Matcher[]) null))),
																// schemas
																list(
																// Input
																		schema("Input", BaseConfiguration.INHERIT_PARENT,
																				(Matcher[]) null),
																		// Output
																		schema("Output", BaseConfiguration.INHERIT_PARENT,
																				(Matcher[]) null)),
																// maps
																list(map("Input", BaseConfiguration.INHERIT_PARENT), map("Output",
																		BaseConfiguration.INHERIT_PARENT)),
																// hooks
																hooks(null, BaseConfiguration.INHERIT_PARENT),
																// mode
																ConnectorModeEnum.DELETE,
																// mode config
																mode(false),
																// link
																link(null, BaseConfiguration.INHERIT_PARENT, null),
																// delta
																null,
																// poolDef
																is(notNullValue(PoolDefinitionBinding.class)),
																// poolInst
																is(notNullValue(PoolInstanceBinding.class))), "Enabled",
														ALComponentInitializeEnum.ON_STARTUP, "Simulated", false, false)),
										// ELSE
										composite("ELSE",
										// else-if branch
												branch(BranchTypeEnum.ELSE,
												// condition
														condition(false, (Matcher[]) null)),
												// composite state
												ALComponentStateEnum.ENABLED,
												// children
												(Matcher[]) null),
										// SWITCH
										composite("Switch",
										// switch branch
												branch(BranchTypeEnum.SWITCH,
												// condition
														condition(false, cItem(null, null, "{work.newAttr}"))),
												// composite state
												ALComponentStateEnum.ENABLED,
												// children
												// CASE
												composite("Switch_test",
												// case branch
														branch(BranchTypeEnum.CASE,
														// condition
																condition(false, cItem(null, null, "test"))),
														// composite state
														ALComponentStateEnum.ENABLED,
														// children
														simple("testScript", script(null, null, "test", false),
																ALComponentStateEnum.ENABLED, "Enabled")),
												// CASE
												composite("Switch_default",
												// default branch
														branch(BranchTypeEnum.CASE,
														// condition
																condition(false, cItem(null, "*", null))),
														// composite state
														ALComponentStateEnum.ENABLED,
														// children
														simple("testScript", script(null, "/Scripts/testScript", "test", true),
																ALComponentStateEnum.ENABLED, "Enabled"))),
										// Loop
										composite("collectionLoop",
										// loop
												loop(
												// collection
												collection("newAttr", "test")),
												// composite state
												ALComponentStateEnum.ENABLED,
												// children
												complex("testDelete_1",
														// connector config
														connector(
														// name
																null,
																// inherit from
																"/Connectors/testDelete",
																// raw config
																jclass(null, BaseConfiguration.INHERIT_PARENT, null,
																		(Matcher[]) null),
																// parser
																parser(null, BaseConfiguration.INHERIT_PARENT, jclass(null,
																		BaseConfiguration.INHERIT_PARENT, null, (Matcher[]) null),
																		list(
																		// Input
																				schema("Input", BaseConfiguration.INHERIT_PARENT,
																						(Matcher[]) null),
																				// Output
																				schema("Output", BaseConfiguration.INHERIT_PARENT,
																						(Matcher[]) null))),
																// schemas
																list(
																// Input
																		schema("Input", BaseConfiguration.INHERIT_PARENT,
																				(Matcher[]) null),
																		// Output
																		schema("Output", BaseConfiguration.INHERIT_PARENT,
																				(Matcher[]) null)),
																// maps
																list(map("Input", BaseConfiguration.INHERIT_PARENT), map("Output",
																		BaseConfiguration.INHERIT_PARENT)),
																// hooks
																hooks(null, BaseConfiguration.INHERIT_PARENT),
																// mode
																ConnectorModeEnum.DELETE,
																// mode config
																mode(false),
																// link
																link(null, BaseConfiguration.INHERIT_PARENT, null),
																// delta
																null,
																// poolDef
																is(notNullValue(PoolDefinitionBinding.class)),
																// poolInst
																is(notNullValue(PoolInstanceBinding.class))), "Enabled",
														ALComponentInitializeEnum.ON_FIRST_USE, "Simulated", false, false),
												// children
												// connector loop
												composite(
														"connectorLoop",
														// loop
														loop(loopConn(
																// connector
																connector(
																// name
																		null,
																		// inherit
																		// from
																		"/Connectors/testIterator",
																		// raw
																		// config
																		jclass(null, BaseConfiguration.INHERIT_PARENT, null, param(
																				"debug", "false"), param("userComment", "")),
																		// parser
																		parser(null, BaseConfiguration.INHERIT_PARENT, jclass(null,
																				BaseConfiguration.INHERIT_PARENT, null,
																				(Matcher[]) null), list(
																		// Input
																				schema("Input", BaseConfiguration.INHERIT_PARENT,
																						(Matcher[]) null),
																				// Output
																				schema("Output", BaseConfiguration.INHERIT_PARENT,
																						(Matcher[]) null))),
																		// schemas
																		list(
																		// Input
																				schema("Input", BaseConfiguration.INHERIT_PARENT,
																						(Matcher[]) null),
																				// Output
																				schema(
																						"Output",
																						BaseConfiguration.INHERIT_NONE,
																						list(
																								sItem(
																										"automapADPassword",
																										0,
																										"1",
																										null,
																										SchemaItemTypeEnum.ATTRIBUTE,
																										null, null, "boolean"),
																								sItem("connectorFlags"),
																								sItem(
																										"ldapReferrals",
																										0,
																										"1",
																										null,
																										SchemaItemTypeEnum.ATTRIBUTE,
																										null, null, "droplist"),
																								sItem(
																										"ldapReturnAttributes",
																										0,
																										"1",
																										null,
																										SchemaItemTypeEnum.ATTRIBUTE,
																										null, null, "textarea")))),
																		// maps
																		list(map("Input", BaseConfiguration.INHERIT_PARENT,
																				(Matcher[]) null), map("Output", null, mItem(
																				"ldapUrl", null, AttributeMapItemTypeEnum.ADVANCED,
																				"\"ldap://test:389\""))),
																		// hooks
																		hooks(null, BaseConfiguration.INHERIT_PARENT),
																		// mode
																		ConnectorModeEnum.ITERATOR,
																		// mode
																		// config
																		null,
																		// link
																		null,
																		// delta
																		delta(false, "", null, false, false, false, false, false,
																				DeltaCommitEnum.ON_EVERY_OP,
																				DeltaRowLockingEnum.SERIALIZABLE,
																				DeltaChangeDetectionModeEnum.DETECT_ALL, null),
																		// poolDef
																		poolDef(true, 10, 10, 10, 10, 10),
																		// poolInst
																		poolInst(false, PoolInstanceExhaustedEnum.WAIT)),
																ConnectorLoopInitializeEnum.ON_EVERY_USE,
																ConnectorLoopSelectEntriesEnum.ON_INITIALIZE)),
														// composite state
														ALComponentStateEnum.ENABLED,
														// children
														// Loop
														composite("ConditionalLoop",
														// loop
																loop(
																// condition
																condition("ret.value = true;")),
																// composite
																// state
																ALComponentStateEnum.ENABLED,
																// children
																// simple map
																simple("testMap", map(null, "/AttributeMaps/testMap",
																		(Matcher[]) null), ALComponentStateEnum.ENABLED)))))),
						// null
						nullB("Null", "", "AbsentAttribute", ""),
						// threading
						params("ThreadOptions", null, param("assemblyline.ehc.executeProlog", "true"), param(
								"assemblyline.ehc.maxInstance", "20"), param("assemblyline.ehc.minPrepare", "15")),
						// operations
						operations(operation("test",
						// schemas
								list(
								// Input
										schema("Input", null, sItem("advanced"), sItem("newAttr"), sItem("simple"),
												sItem("substitution"), sItem("test")),
										// Output
										schema("Output", null, sItem("advanced"), sItem("newAttr"), sItem("simple"),
												sItem("substitution"), sItem("test"))),
								// maps
								list(
								// Input
										map("Input", null, mItem("newAttr", null, AttributeMapItemTypeEnum.SIMPLE, "newAttr"),
												mItem("simple", null, AttributeMapItemTypeEnum.SIMPLE, "simple")),
										// Output
										map("Output", null, mItem("newAttr", null, AttributeMapItemTypeEnum.SIMPLE, "newAttr"),
												mItem("substitution", null, AttributeMapItemTypeEnum.SIMPLE, "substitution"))))),
						// init params
						alInitParams(schema("AssemblyLineInitParams", null, sItem("newAttr")))));
	}

	@Test
	public void test_Direct_Solution_Context() {
		test_Solution_Context(orgBind);
	}

	@Test
	public void test_Indirect_Solution_Context() {
		test_Solution_Context(prodBind);
	}

	@SuppressWarnings("unchecked")
	public void test_Solution_Context(SolutionBinding bind) {
		assertThat(bind.getContext(), is(not(nullValue())));
		assertThat(bind.getContext(), context(
		// interface
				iface(true, "test", null, -1, null, new Matcher[] {
						expProp("api.config.folder", "api.config.folder", "config", "", null),
						expProp("api.jmx.on", "api.jmx.on", "api", "", null) }),
				// logging
				logs(log("ConsoleAppender", "system:/Loggers/ibmdi.ConsoleAppender", param("Pattern.ConversionPattern",
						"%d{DEFAULT} %-5p [%c] - %m%n"), param("com.ibm.di.log.layout", "Pattern"), param("com.ibm.di.log.level",
						"INFO"), param("enabled", "true")), log("FileAppender", "system:/Loggers/ibmdi.FileAppender", param(
						"File.Append", "false"), param("Pattern.ConversionPattern", "%d{DEFAULT} %-5p [%c] - %m%n"), param(
						"com.ibm.di.log.layout", "Pattern"), param("com.ibm.di.log.level", "INFO"), param("enabled", "true"))),
				// libraries
				library(new Matcher[] { param("testScript", "com.ibm.di.function.UserFunctions") }),
				// tombstone
				params("Tombstones", null, param("AssemblyLines", "true"), param("Configuration", "true")),
				// sys store
				params("SystemStore", null, param("enabled", "true"), param("com.ibm.di.store.database",
						"jdbc:oracle:thin:@localhost:1521:TDISysStore"), param("com.ibm.di.store.jdbc.driver",
						"oracle.jdbc.OracleDriver"), param("com.ibm.di.store.jdbc.user", "SYSTEM"), param(
						"com.ibm.di.store.jdbc.password", "")),
				// instance
				inst(null)));
	}

	private static NamedBinding find(String name, Iterable<? extends NamedBinding> list) {
		for (NamedBinding cc : list) {
			if (name.equals(cc.getName())) {
				return cc;
			}
		}
		throw new AssertionError("Missing container with name: " + name + " in " + list);
	}
}
