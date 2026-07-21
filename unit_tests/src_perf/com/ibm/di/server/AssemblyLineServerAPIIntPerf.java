package com.ibm.di.server;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ibm.di.api.APIEngine;
import com.ibm.di.api.DIException;
import com.ibm.di.api.local.ConfigInstance;
import com.ibm.di.api.local.Session;
import com.ibm.di.test.framework.perf.RepeatConstants;

/**
 * This class takes care of performance testing the AssemblyLine. This includes
 * measuring the speed of starting the AL as well as testing it with components
 * like Dummy Iterator, AddOnly, Scripts, Loops, If-Else Branches, mapping and
 * so forth.
 * 
 */
public class AssemblyLineServerAPIIntPerf {

	private static Session session;
	private static ConfigInstance configInstance;

	@BeforeClass
	public static void initialize() throws DIException, InterruptedException {
		session = APIEngine.getLocalSession();
		configInstance = session.startConfigInstance("unit_tests/configs/perf/AL_dummy_iterator_with_component.xml");
	}

	@Test
	public void test_speed_of_starting_AL() throws Exception {
		for (int i = 0; i < RepeatConstants.get250k(); i++) {
			configInstance.startAssemblyLine("AL_speed_start", true);
		}
	}

	@Test
	public void test_AL_with_dummy_iterator() throws DIException {
		configInstance.startAssemblyLine("AL_with_Dummy_Iterator", true);
	}

	@Test
	public void test_AL_with_dummy_iterator_and_AddOnly() throws DIException {
		configInstance.startAssemblyLine("Dummy_Iterator_with_AddOnly", true);
	}

	@Test
	public void test_AL_with_dummy_iterator_and_Delete() throws DIException {
		configInstance.startAssemblyLine("Dummy_Iterator_with_Delete", true);
	}

	@Test
	public void test_AL_with_dummy_iterator_and_Update() throws DIException {
		configInstance.startAssemblyLine("Dummy_Iterator_with_Update", true);
	}

	@Test
	public void test_AL_with_dummy_iterator_and_Lookup() throws DIException {
		configInstance.startAssemblyLine("Dummy_Iterator_with_Lookup", true);
	}

	@Test
	public void test_AL_with_dummy_iterator_and_IF_branch_resolved_to_True() throws DIException {
		configInstance.startAssemblyLine("Dummy_Iterator_with_IF_Branch_TRUE", true);
	}

	@Test
	public void test_AL_with_dummy_iterator_and_IF_branch_resolved_to_False() throws DIException {
		configInstance.startAssemblyLine("Dummy_Iterator_with_IF_Branch_FALSE", true);
	}

	@Test
	public void test_AL_with_dummy_iterator_and_IF_ELSE_branch() throws DIException {
		configInstance.startAssemblyLine("Dummy_Iterator_with_IF_ELSE_Branch", true);
	}

	@Test
	public void test_AL_with_dummy_iterator_and_LOOP() throws DIException {
		configInstance.startAssemblyLine("Dummy_Iterator_with_LOOP", true);
	}

	@Test
	public void test_AL_with_Loop_over_Dummy_Iterator() throws DIException {
		configInstance.startAssemblyLine("AL_with_Loop_over_Dummy_Iterator", true);
	}

	@Test
	public void test_AL_with_dummy_iterator_and_ScriptComponent() throws DIException {
		configInstance.startAssemblyLine("Dummy_Iterator_with_Script_Component", true);
	}

	@Test
	public void test_AL_with_dummy_iterator_and_AttributeMapping_Simple() throws DIException {
		configInstance.startAssemblyLine("Dummy_Iterator_with_AttributeMapping_Simple", true);
	}

	@Test
	public void test_AL_with_dummy_iterator_and_AttributeMapping_MapStar() throws DIException {
		configInstance.startAssemblyLine("Dummy_Iterator_with_AttributeMapping_MAP_STAR", true);
	}

	@Test
	public void test_AL_with_dummy_iterator_and_AttributeMapping_Advenced() throws DIException {
		configInstance.startAssemblyLine("Dummy_Iterator_with_AttributeMapping_Advanced", true);
	}

	@AfterClass
	public static void closeResouces() throws DIException, InterruptedException {
		String configID = configInstance.getConfigId();
		configInstance.stop();
		// wait until configInstance is really stopped
		while (session.getConfigInstance(configID) != null) {
			Thread.sleep(300);
		}
	}
}
