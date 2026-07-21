package com.ibm.di.cvt71.tp.template;

import static org.hamcrest.collection.IsIn.isIn;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ibm.di.api.APIEngine;
import com.ibm.di.api.remote.impl.rmi.Constants;
import com.ibm.di.entry.Entry;
import com.ibm.di.test.CVTComponent;
import com.ibm.di.test.CVTTest;
import com.ibm.di.test.utils.FileRecorder;
import com.ibm.di.test.utils.func.DerbyServer;
import com.ibm.di.test.utils.func.PortProbe;
import com.ibm.di.test.utils.func.TDIServer;
import com.ibm.di.test.utils.func.tp.InitiatorTouchpoint;
import com.ibm.di.test.utils.func.tp.IntermediaryTouchpoint;
import com.ibm.di.test.utils.func.tp.ProviderTouchpoint;
import com.ibm.di.test.utils.func.tp.ProviderTouchpointResponse;
import com.ibm.di.test.utils.func.tp.TouchpointData;
import com.ibm.di.test.utils.func.tp.TouchpointFactory;
import com.ibm.di.tp.server.model.impl.tdi.TouchpointTypeLocator;
import com.ibm.di.util.FileUtils;

/**
 * Test the intermediary touchpoint template configuration. The touchpoint is
 * configured with a JDBC Connector.
 */
@CVTComponent(name = "tpserver")
public class FN_39_IntermediaryTouchpointTemplateWithJDBCConnectorCVT {

	private static final String TABLE_NAME = "people";

	private static final Person JOHN = new Person("john", "smith", 50, 185);

	private static final Person MARY = new Person("mary", "smith", 40, 175);

	private static final Person PETER = new Person("peter", "smith", 20, 180);

	private static final String QUERY_JOHN = "firstname=john&lastname=smith";

	private static final String QUERY_SMITHS = "lastname=smith";

	private static TDIServer tdi;

	private static DerbyServer derby;

	private static TouchpointFactory tf;

	private static List<InitiatorTouchpoint> initTPs = new LinkedList<InitiatorTouchpoint>();

	private static List<IntermediaryTouchpoint> intTPs = new LinkedList<IntermediaryTouchpoint>();

	private static List<ProviderTouchpoint> provTPs = new LinkedList<ProviderTouchpoint>();

	private static FileRecorder recorder;

	private List<PersonDAO> dao = new LinkedList<PersonDAO>();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		tdi = new TDIServer();
		recorder = new FileRecorder(new File(tdi.getSolutionDir(), "tpbackup"));

		File tpXml = new File(tdi.getSolutionDir(), "etc/tp.xml");
		recorder.recordModifyFile(tpXml);
		FileUtils.copyFile(new File("resources/tp/server/tp.xml"), tpXml, true);

		int tpServerPort = PortProbe.getAvailablePort();

		tdi.setProperty(APIEngine.PROP_TP_SERVER_ON, "true");
		tdi.setProperty("web.server.port", Integer.toString(tpServerPort));
		tdi.setProperty(Constants.PROP_API_REMOTE_SSL_ON, "false");
		tdi.startServer();

		derby = new DerbyServer(PortProbe.getAvailablePort(), "testuser", "testpass");

		URL serviceDocUrl = new URL("http://localhost:" + tpServerPort + "/tp/");
		tf = new TouchpointFactory(serviceDocUrl);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		if (derby != null) {
			derby.close();
			derby = null;
		}

		if (tdi != null) {
			tdi.close();
			tdi = null;
		}
		
		recorder.destroy();
	}

	@After
	public void tearDown() throws Exception {
		for (InitiatorTouchpoint initTp : initTPs) {
			try {
				if (initTp != null) {
					initTp.deleteTouchpoint();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		initTPs.clear();

		for (IntermediaryTouchpoint intTp : intTPs) {
			try {
				if (intTp != null) {
					intTp.deleteTouchpoint();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		intTPs.clear();

		for (ProviderTouchpoint provTp : provTPs) {
			try {
				if (provTp != null) {
					provTp.deleteTouchpoint();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		provTPs.clear();

		for (PersonDAO obj : dao) {
			if (obj != null) {
				obj.destroy();
			}
		}
		dao.clear();
	}

	/**
	 * Verify that GET request with no query params sent to intermediary with
	 * one destination (another provider with no entries in its back-end store)
	 * will return 404
	 */
	@CVTTest(name = "CVT_FN-39_TP_Server_Intermediary_TP_Template_TC01")
	@Test
	public void test_GET_With_No_Query_Params_To_Single_Destination_Which_Contains_No_Entries() throws Exception {
		dao.add(new PersonDAO(derby.getConnection(), TABLE_NAME));

		provTPs.add(createProviderTouchpoint(TABLE_NAME));
		intTPs.add(createIntermediaryWithDestinations(provTPs));

		ProviderTouchpointResponse response = intTPs.get(0).get();
		assertThat(response.getErrorMessage(), response.getResponseCode(), is(404));
	}

	/**
	 * Verify that GET request with no query params sent to intermediary with
	 * one destination (another provider with a single entry in its back-end
	 * store) will return 200
	 */
	@CVTTest(name = "CVT_FN-39_TP_Server_Intermediary_TP_Template_TC02")
	@Test
	public void test_GET_With_No_Query_Params_To_Single_Destination_Which_Contains_Single_Entry() throws Exception {
		dao.add(new PersonDAO(derby.getConnection(), TABLE_NAME).add(JOHN));

		provTPs.add(createProviderTouchpoint(TABLE_NAME));
		intTPs.add(createIntermediaryWithDestinations(provTPs));

		Set<Person> people = getPeopleSet(intTPs.get(0).get());
		assertThat(people, containsInAnyOrder(JOHN));
	}

	/**
	 * Verify that GET request with no query params sent to intermediary with
	 * one destination (another provider with multiple entries in its back-end
	 * store) will returns 2xx and all the entries found.
	 */
	@CVTTest(name = "CVT_FN-39_TP_Server_Intermediary_TP_Template_TC03")
	@Test
	public void test_GET_With_No_Query_Params_To_Single_Destination_Which_Contains_Multiple_Entry() throws Exception {
		dao.add(new PersonDAO(derby.getConnection(), TABLE_NAME).add(JOHN, PETER));

		provTPs.add(createProviderTouchpoint(TABLE_NAME));
		intTPs.add(createIntermediaryWithDestinations(provTPs));

		Set<Person> people = getPeopleSet(intTPs.get(0).get());
		assertThat(people, containsInAnyOrder(JOHN, PETER));
	}

	/**
	 * Verify that GET request with no query params sent to intermediary with
	 * two destination (both providers with no entries in their back-end stores)
	 * will return 500
	 */
	@CVTTest(name = "CVT_FN-39_TP_Server_Intermediary_TP_Template_TC04")
	@Test
	public void test_GET_With_No_Query_Params_To_Two_Destination_Which_Contains_No_Entries() throws Exception {
		dao.add(new PersonDAO(derby.getConnection(), TABLE_NAME + 1));
		dao.add(new PersonDAO(derby.getConnection(), TABLE_NAME + 2));

		provTPs.add(createProviderTouchpoint(TABLE_NAME + 1));
		provTPs.add(createProviderTouchpoint(TABLE_NAME + 2));
		intTPs.add(createIntermediaryWithDestinations(provTPs));

		ProviderTouchpointResponse response = intTPs.get(0).get();
		assertThat(response.isError(), is(true));
	}

	/**
	 * Verify that GET request with no query params sent to intermediary with
	 * two destination (both providers - first has entries, second don't) will
	 * returns 2xx
	 */
	@CVTTest(name = "CVT_FN-39_TP_Server_Intermediary_TP_Template_TC05")
	@Test
	public void test_GET_With_No_Query_Params_To_Two_Destination_The_First_Containing_Entries_Second_Dont() throws Exception {
		dao.add(new PersonDAO(derby.getConnection(), TABLE_NAME + 1).add(JOHN, PETER));

		provTPs.add(createProviderTouchpoint(TABLE_NAME + 1));
		provTPs.add(createProviderTouchpoint(TABLE_NAME + 2));
		intTPs.add(createIntermediaryWithDestinations(provTPs));

		ProviderTouchpointResponse response = intTPs.get(0).get();
		assertThat(response.isError(), is(false));

		Set<Person> people = getPeopleSet(response);
		assertThat(people, containsInAnyOrder(JOHN, PETER));
	}

	/**
	 * Verify that GET request with no query params sent to intermediary with
	 * two destination (both providers containing entries in their back-end
	 * stores) will return 2xx and all the entries.
	 */
	@CVTTest(name = "CVT_FN-39_TP_Server_Intermediary_TP_Template_TC06")
	@Test
	public void test_GET_With_No_Query_Params_To_Two_Destination_Both_Containing_Entries() throws Exception {
		dao.add(new PersonDAO(derby.getConnection(), TABLE_NAME + 1).add(JOHN, PETER));
		dao.add(new PersonDAO(derby.getConnection(), TABLE_NAME + 2).add(MARY, PETER));

		provTPs.add(createProviderTouchpoint(TABLE_NAME + 1));
		provTPs.add(createProviderTouchpoint(TABLE_NAME + 2));
		intTPs.add(createIntermediaryWithDestinations(provTPs));

		ProviderTouchpointResponse response = intTPs.get(0).get();
		assertThat(response.isError(), is(false));

		List<Person> people = getPeopleList(response);
		assertThat(people, containsInAnyOrder(JOHN, MARY, PETER, PETER));
	}

	/**
	 * Verify that GET request with query params matching single entry sent to
	 * intermediary with a single destination (a provider containing one entry
	 * in its back-end store) will return 2xx and the entry.
	 */
	@CVTTest(name = "CVT_FN-39_TP_Server_Intermediary_TP_Template_TC07")
	@Test
	public void test_GET_With_Query_Params_With_Single_Destination_Matching_Single_Entry() throws Exception {
		dao.add(new PersonDAO(derby.getConnection(), TABLE_NAME).add(JOHN, MARY));

		provTPs.add(createProviderTouchpoint(TABLE_NAME));
		intTPs.add(createIntermediaryWithDestinations(provTPs));

		Set<Person> people = getPeopleSet(intTPs.get(0).get(QUERY_JOHN));

		assertThat(people, containsInAnyOrder(JOHN));
	}

	/**
	 * Verify that GET request with query params matching no entries sent to
	 * intermediary with a single destination (a provider containing one entry
	 * in its back-end store) will return error status.
	 */
	@CVTTest(name = "CVT_FN-39_TP_Server_Intermediary_TP_Template_TC08")
	@Test
	public void test_GET_With_Query_Params_With_Single_Destination_Matching_No_Entries() throws Exception {
		dao.add(new PersonDAO(derby.getConnection(), TABLE_NAME).add(MARY));

		provTPs.add(createProviderTouchpoint(TABLE_NAME));
		intTPs.add(createIntermediaryWithDestinations(provTPs));

		ProviderTouchpointResponse response = intTPs.get(0).get(QUERY_JOHN);
		assertThat(response.isError(), is(true));
	}

	/**
	 * Verify that GET request with query params matching multiple entries sent
	 * to intermediary with a single destination (a provider containing multiple
	 * entries in its back-end store) will return 2xx and the entry.
	 */
	@CVTTest(name = "CVT_FN-39_TP_Server_Intermediary_TP_Template_TC09")
	@Test
	public void test_GET_With_Query_Params_With_Single_Destination_Matching_Multiple_Entries() throws Exception {
		dao.add(new PersonDAO(derby.getConnection(), TABLE_NAME).add(JOHN, MARY, PETER));

		provTPs.add(createProviderTouchpoint(TABLE_NAME));
		intTPs.add(createIntermediaryWithDestinations(provTPs));

		Set<Person> people = getPeopleSet(intTPs.get(0).get(QUERY_SMITHS));

		assertThat(people, containsInAnyOrder(JOHN, MARY, PETER));
	}

	/**
	 * Verify that GET request with query params matching single entry sent to
	 * intermediary with a multiple destination (providers containing a single
	 * entry in their back-end stores) will return 2xx and the two entry.
	 */
	@CVTTest(name = "CVT_FN-39_TP_Server_Intermediary_TP_Template_TC10")
	@Test
	public void test_GET_With_Query_Params_With_Multiple_Destinations_Both_Matching_Single_Entry() throws Exception {
		dao.add(new PersonDAO(derby.getConnection(), TABLE_NAME + 1).add(JOHN, MARY));
		dao.add(new PersonDAO(derby.getConnection(), TABLE_NAME + 2).add(JOHN, PETER));

		provTPs.add(createProviderTouchpoint(TABLE_NAME + 1));
		provTPs.add(createProviderTouchpoint(TABLE_NAME + 2));
		intTPs.add(createIntermediaryWithDestinations(provTPs));

		List<Person> people = getPeopleList(intTPs.get(0).get(QUERY_JOHN));

		assertThat(people, containsInAnyOrder(JOHN, JOHN));
	}

	/**
	 * Verify that GET request with query params matching no entries sent to
	 * intermediary with multiple destinations (providers containing a single
	 * entry in their back-end store) will return error status.
	 */
	@CVTTest(name = "CVT_FN-39_TP_Server_Intermediary_TP_Template_TC11")
	@Test
	public void test_GET_With_Query_Params_With_Multiple_Destinations_Matching_No_Entries() throws Exception {
		dao.add(new PersonDAO(derby.getConnection(), TABLE_NAME + 1).add(MARY));
		dao.add(new PersonDAO(derby.getConnection(), TABLE_NAME + 2).add(PETER));

		provTPs.add(createProviderTouchpoint(TABLE_NAME + 1));
		provTPs.add(createProviderTouchpoint(TABLE_NAME + 2));
		intTPs.add(createIntermediaryWithDestinations(provTPs));

		ProviderTouchpointResponse response = intTPs.get(0).get(QUERY_JOHN);
		assertThat(response.isError(), is(true));
	}

	/**
	 * Verify that GET request with query params matching single entry in the
	 * first destination and none in the other (both destinations are providers
	 * containing a single entry in their back-end stores) will return 2xx and
	 * only one entry.
	 */
	@CVTTest(name = "CVT_FN-39_TP_Server_Intermediary_TP_Template_TC12")
	@Test
	public void test_GET_With_Query_Params_With_Multiple_Destinations_First_Of_Which_Matches_Single_Entry_The_Other_Maches_No_Entries()
			throws Exception {
		dao.add(new PersonDAO(derby.getConnection(), TABLE_NAME + 1).add(JOHN, MARY));
		dao.add(new PersonDAO(derby.getConnection(), TABLE_NAME + 2).add(PETER));

		provTPs.add(createProviderTouchpoint(TABLE_NAME + 1));
		provTPs.add(createProviderTouchpoint(TABLE_NAME + 2));
		intTPs.add(createIntermediaryWithDestinations(provTPs));

		List<Person> people = getPeopleList(intTPs.get(0).get(QUERY_JOHN));

		assertThat(people, containsInAnyOrder(JOHN));
	}

	/**
	 * Verify that GET request with query params matching multiple entries sent
	 * to intermediary with a multiple destination (providers containing
	 * multiple entries in their back-end stores) will return 2xx and the
	 * entries.
	 */
	@CVTTest(name = "CVT_FN-39_TP_Server_Intermediary_TP_Template_TC13")
	@Test
	public void test_GET_With_Query_Params_With_Multiple_Destination_Matching_Multiple_Entries() throws Exception {
		dao.add(new PersonDAO(derby.getConnection(), TABLE_NAME + 1).add(JOHN, MARY));
		dao.add(new PersonDAO(derby.getConnection(), TABLE_NAME + 2).add(JOHN, PETER));

		provTPs.add(createProviderTouchpoint(TABLE_NAME + 1));
		provTPs.add(createProviderTouchpoint(TABLE_NAME + 2));
		intTPs.add(createIntermediaryWithDestinations(provTPs));

		List<Person> people = getPeopleList(intTPs.get(0).get(QUERY_SMITHS));

		assertThat(people, containsInAnyOrder(JOHN, MARY, JOHN, PETER));
	}

	/**
	 * Verify that GET request with no query params matching multiple entries
	 * sent to intermediary with a single destination (a provider containing
	 * multiple entries in its back-end store) and having size limit header will
	 * return 2xx and all the entries
	 */
	@CVTTest(name = "CVT_FN-39_TP_Server_Intermediary_TP_Template_TC14")
	@Test
	public void test_GET_With_Query_Params_With_Single_Destination_Matching_Multiple_Entries_Limited_By_Size() throws Exception {
		Person[] persons = new Person[] { JOHN, MARY, PETER };
		dao.add(new PersonDAO(derby.getConnection(), TABLE_NAME).add(persons));

		provTPs.add(createProviderTouchpoint(TABLE_NAME));
		intTPs.add(createIntermediaryWithDestinations(provTPs));

		Set<Person> people = getPeopleSet(intTPs.get(0).get(2));

		assertThat(people.size(), is(2));
		for (Person p : people) {
			assertThat(p, isIn(persons));
		}
	}

	/**
	 * Verify POST request is forwarded to a single destination (provider) and
	 * creates a new entry.
	 */
	@CVTTest(name = "CVT_FN-39_TP_Server_Intermediary_TP_Template_TC15")
	@Test
	public void test_POST_Creates_New_Entry_In_A_Single_Destination() throws Exception {
		dao.add(new PersonDAO(derby.getConnection(), TABLE_NAME).add(MARY));

		TouchpointData data = new TouchpointData(JOHN.toEntry());

		provTPs.add(createProviderTouchpoint(TABLE_NAME));
		intTPs.add(createIntermediaryWithDestinations(provTPs));

		ProviderTouchpointResponse response = intTPs.get(0).post(data);

		assertEquals(response.getErrorMessage(), 201, response.getResponseCode());

		assertThat(dao.get(0).list(), containsInAnyOrder(JOHN, MARY));
	}

	/**
	 * Verify POST request is forwarded to multiple destinations (providers) and
	 * creates both entries in their stores.
	 */
	@CVTTest(name = "CVT_FN-39_TP_Server_Intermediary_TP_Template_TC16")
	@Test
	public void test_POST_Creates_New_Entries_In_Multiple_Destinations() throws Exception {
		dao.add(new PersonDAO(derby.getConnection(), TABLE_NAME + 1).add(MARY));
		dao.add(new PersonDAO(derby.getConnection(), TABLE_NAME + 2).add(PETER));

		TouchpointData data = new TouchpointData(JOHN.toEntry());

		provTPs.add(createProviderTouchpoint(TABLE_NAME + 1));
		provTPs.add(createProviderTouchpoint(TABLE_NAME + 2));
		intTPs.add(createIntermediaryWithDestinations(provTPs));

		ProviderTouchpointResponse response = intTPs.get(0).post(data);

		assertEquals(200, response.getResponseCode());

		assertThat(dao.get(0).list(), containsInAnyOrder(JOHN, MARY));
		assertThat(dao.get(1).list(), containsInAnyOrder(JOHN, PETER));
	}

	/**
	 * Verify POST request is forwarded to multiple destinations (providers)
	 * first of which is available, the second not. This should return success
	 * and create the entry.
	 */
	@CVTTest(name = "CVT_FN-39_TP_Server_Intermediary_TP_Template_TC17")
	@Test
	public void test_POST_Creates_New_Entry_In_The_Available_Destination_And_Dont_In_The_Unavailable_One() throws Exception {
		dao.add(new PersonDAO(derby.getConnection(), TABLE_NAME + 1).add(MARY));
		dao.add(new PersonDAO(derby.getConnection(), TABLE_NAME + 2).add(PETER));

		TouchpointData data = new TouchpointData(JOHN.toEntry());

		ProviderTouchpoint unreachable = createProviderTouchpoint(TABLE_NAME + 2);

		provTPs.add(createProviderTouchpoint(TABLE_NAME + 1));
		provTPs.add(unreachable);
		intTPs.add(createIntermediaryWithDestinations(provTPs));

		unreachable.deleteTouchpoint();
		provTPs.remove(1);
		ProviderTouchpointResponse response = intTPs.get(0).post(data);

		assertEquals(200, response.getResponseCode());

		assertThat(dao.get(0).list(), containsInAnyOrder(JOHN, MARY));
		assertThat(dao.get(1).list(), containsInAnyOrder(PETER));
	}

	/**
	 * Verify PUT request is forwarded to a single destination (provider) and
	 * updates a new entry.
	 */
	@CVTTest(name = "CVT_FN-39_TP_Server_Intermediary_TP_Template_TC18")
	@Test
	public void test_PUT_Updates_An_Entry_In_A_Single_Destination() throws Exception {

		dao.add(new PersonDAO(derby.getConnection(), TABLE_NAME).add(JOHN, MARY, PETER));

		final int newAge = JOHN.getAge() + 1;
		Person newJohn = new Person(JOHN.getFirstName(), JOHN.getLastName(), newAge, JOHN.getHeight());

		Entry modifications = new Entry();
		modifications.setAttribute("age", "" + newAge);
		TouchpointData data = new TouchpointData(modifications);

		provTPs.add(createProviderTouchpoint(TABLE_NAME));
		intTPs.add(createIntermediaryWithDestinations(provTPs));

		ProviderTouchpointResponse response = intTPs.get(0).put(QUERY_JOHN, data);

		assertEquals(response.getErrorMessage(), 204, response.getResponseCode());
		assertThat(dao.get(0).list(), containsInAnyOrder(newJohn, MARY, PETER));
	}

	/**
	 * Verify PUT request is forwarded to multiple destinations (providers) and
	 * updates both entries in their stores.
	 */
	@CVTTest(name = "CVT_FN-39_TP_Server_Intermediary_TP_Template_TC19")
	@Test
	public void test_PUT_Updates_New_Entries_In_Multiple_Destinations() throws Exception {
		dao.add(new PersonDAO(derby.getConnection(), TABLE_NAME + 1).add(JOHN, MARY));
		dao.add(new PersonDAO(derby.getConnection(), TABLE_NAME + 2).add(JOHN, PETER));

		final int newAge = JOHN.getAge() + 1;
		Person newJohn = new Person(JOHN.getFirstName(), JOHN.getLastName(), newAge, JOHN.getHeight());

		Entry modifications = new Entry();
		modifications.setAttribute("age", "" + newAge);
		TouchpointData data = new TouchpointData(modifications);

		provTPs.add(createProviderTouchpoint(TABLE_NAME + 1));
		provTPs.add(createProviderTouchpoint(TABLE_NAME + 2));
		intTPs.add(createIntermediaryWithDestinations(provTPs));

		ProviderTouchpointResponse response = intTPs.get(0).put(QUERY_JOHN, data);

		assertEquals(response.getErrorMessage(), 200, response.getResponseCode());
		assertThat(dao.get(0).list(), containsInAnyOrder(newJohn, MARY));
		assertThat(dao.get(1).list(), containsInAnyOrder(newJohn, PETER));
	}

	/**
	 * Verify PUT request is forwarded to multiple destinations (providers)
	 * first of which is available, the second is not. This should return
	 * success and the entry should be updated.
	 */
	@CVTTest(name = "CVT_FN-39_TP_Server_Intermediary_TP_Template_TC20")
	@Test
	public void test_PUT_Updates_New_Entry_In_The_Available_Destination_And_Dont_In_The_Unavailable_One() throws Exception {
		dao.add(new PersonDAO(derby.getConnection(), TABLE_NAME + 1).add(JOHN, MARY));
		dao.add(new PersonDAO(derby.getConnection(), TABLE_NAME + 2).add(JOHN, PETER));

		final int newAge = JOHN.getAge() + 1;
		Person newJohn = new Person(JOHN.getFirstName(), JOHN.getLastName(), newAge, JOHN.getHeight());

		Entry modifications = new Entry();
		modifications.setAttribute("age", "" + newAge);
		TouchpointData data = new TouchpointData(modifications);

		ProviderTouchpoint unavailable = createProviderTouchpoint(TABLE_NAME + 2);

		provTPs.add(createProviderTouchpoint(TABLE_NAME + 1));
		provTPs.add(unavailable);
		intTPs.add(createIntermediaryWithDestinations(provTPs));

		unavailable.deleteTouchpoint();
		provTPs.remove(unavailable);

		ProviderTouchpointResponse response = intTPs.get(0).put(QUERY_JOHN, data);

		assertEquals(response.getErrorMessage(), 200, response.getResponseCode());
		assertThat(dao.get(0).list(), containsInAnyOrder(newJohn, MARY));
		assertThat(dao.get(1).list(), containsInAnyOrder(JOHN, PETER));
	}

	/**
	 * Verify PUT request is forwarded to a destination (provider) but multiple
	 * entries are matched. This should return error status.
	 */
	@CVTTest(name = "CVT_FN-39_TP_Server_Intermediary_TP_Template_TC21")
	@Test
	public void test_PUT_Fails_When_Multiple_Entries_Are_Matched_For_Single_Destination() throws Exception {
		dao.add(new PersonDAO(derby.getConnection(), TABLE_NAME).add(JOHN, MARY, PETER));

		Entry modifications = new Entry();
		modifications.setAttribute("age", "100");
		TouchpointData data = new TouchpointData(modifications);

		provTPs.add(createProviderTouchpoint(TABLE_NAME));
		intTPs.add(createIntermediaryWithDestinations(provTPs));

		ProviderTouchpointResponse response = intTPs.get(0).put(QUERY_SMITHS, data);
		assertThat(response.isError(), is(true));
		assertThat(dao.get(0).list(), containsInAnyOrder(JOHN, MARY, PETER));
	}

	/**
	 * Verify PUT request is forwarded to all available destinations (provider)
	 * and ignores multiple matches in some of the destinations. This should
	 * return success and only single-entry-matching-destinations should be
	 * modified.
	 */
	@CVTTest(name = "CVT_FN-39_TP_Server_Intermediary_TP_Template_TC22")
	@Test
	public void test_PUT_Updates_Entries_In_Destinations_Which_Dont_Match_Multiple_Entries() throws Exception {
		dao.add(new PersonDAO(derby.getConnection(), TABLE_NAME + 1).add(JOHN));
		dao.add(new PersonDAO(derby.getConnection(), TABLE_NAME + 2).add(JOHN, MARY, PETER));

		final int newAge = JOHN.getAge() + 1;
		Person newJohn = new Person(JOHN.getFirstName(), JOHN.getLastName(), newAge, JOHN.getHeight());

		Entry modifications = new Entry();
		modifications.setAttribute("age", "" + newAge);
		TouchpointData data = new TouchpointData(modifications);

		provTPs.add(createProviderTouchpoint(TABLE_NAME + 1));
		provTPs.add(createProviderTouchpoint(TABLE_NAME + 2));
		intTPs.add(createIntermediaryWithDestinations(provTPs));

		ProviderTouchpointResponse response = intTPs.get(0).put(QUERY_SMITHS, data);

		assertEquals(response.getErrorMessage(), 200, response.getResponseCode());
		assertThat(dao.get(0).list(), containsInAnyOrder(newJohn));
		assertThat(dao.get(1).list(), containsInAnyOrder(JOHN, MARY, PETER));
	}

	/**
	 * Verify DELETE request is forwarded to a single destination and that the
	 * matched entry is deleted successfully.
	 */
	@CVTTest(name = "CVT_FN-39_TP_Server_Intermediary_TP_Template_TC23")
	@Test
	public void test_DELETE_Removes_The_Matched_Entry_In_Single_Destination() throws Exception {
		dao.add(new PersonDAO(derby.getConnection(), TABLE_NAME).add(JOHN, MARY, PETER));

		provTPs.add(createProviderTouchpoint(TABLE_NAME));
		intTPs.add(createIntermediaryWithDestinations(provTPs));

		ProviderTouchpointResponse response = intTPs.get(0).delete(QUERY_JOHN);
		assertEquals(response.getErrorMessage(), 204, response.getResponseCode());

		assertThat(dao.get(0).list(), containsInAnyOrder(MARY, PETER));
	}

	/**
	 * Verify DELETE request removes the matched entry in all destinations
	 * (providers) and ignores when a destination finds no/multiple entries in
	 * their backend stores.
	 */
	@CVTTest(name = "CVT_FN-39_TP_Server_Intermediary_TP_Template_TC24")
	@Test
	public void test_DELETE_Removes_The_Matched_Entry_In_All_Destinations_And_Ignores_Erros_When_Destination_Contains_No_Entries_Or_Multiple_Are_Found()
			throws Exception {

		Person notMatching = new Person("not", "matching", 12, 13);

		dao.add(new PersonDAO(derby.getConnection(), TABLE_NAME + 1).add(JOHN));
		dao.add(new PersonDAO(derby.getConnection(), TABLE_NAME + 2).add(JOHN, MARY, PETER));
		dao.add(new PersonDAO(derby.getConnection(), TABLE_NAME + 3).add(notMatching));
		dao.add(new PersonDAO(derby.getConnection(), TABLE_NAME + 4).add(JOHN));

		ProviderTouchpoint unavailable = createProviderTouchpoint(TABLE_NAME + 4);

		provTPs.add(createProviderTouchpoint(TABLE_NAME + 1));
		provTPs.add(createProviderTouchpoint(TABLE_NAME + 2));
		provTPs.add(createProviderTouchpoint(TABLE_NAME + 3));
		provTPs.add(unavailable);
		intTPs.add(createIntermediaryWithDestinations(provTPs));

		provTPs.remove(unavailable);
		unavailable.deleteTouchpoint();

		ProviderTouchpointResponse response = intTPs.get(0).delete(QUERY_SMITHS);
		assertEquals(response.getErrorMessage(), 200, response.getResponseCode());

		assertThat(dao.get(0).list().size(), is(0));
		assertThat(dao.get(1).list(), containsInAnyOrder(JOHN, MARY, PETER));
		assertThat(dao.get(2).list(), containsInAnyOrder(notMatching));
		assertThat(dao.get(3).list(), containsInAnyOrder(JOHN));
	}

	/**
	 * Same as {@link #getPeopleSet(ProviderTouchpointResponse)} but does not
	 * ignore repeats.
	 * 
	 * @param response
	 * @return
	 */
	private List<Person> getPeopleList(ProviderTouchpointResponse response) {
		assertEquals(response.getErrorMessage(), 200, response.getResponseCode());
		TouchpointData data = response.getData();
		assertNotNull(data);
		List<Person> people = new LinkedList<Person>();
		for (Entry entry : data.getEntries()) {
			Person person = new Person(entry);
			people.add(person);
		}
		return people;
	}

	private Set<Person> getPeopleSet(ProviderTouchpointResponse response) {
		assertEquals(response.getErrorMessage(), 200, response.getResponseCode());
		TouchpointData data = response.getData();
		assertNotNull(data);
		Set<Person> people = new HashSet<Person>();
		for (Entry entry : data.getEntries()) {
			Person person = new Person(entry);
			people.add(person);
		}
		return people;
	}

	private static ProviderTouchpoint createProviderTouchpoint(String tableName) throws Exception {

		String jdbcConnectorType = "system:/Connectors/ibmdi.JDBC";
		Map<String, String> jdbcParams = new HashMap<String, String>();
		jdbcParams.put("jdbcDriver", derby.getJdbcDriverClassName());
		jdbcParams.put("jdbcLogin", derby.getUsername());
		jdbcParams.put("jdbcPassword", derby.getPassword());
		jdbcParams.put("jdbcSource", derby.getJdbcUrl());
		jdbcParams.put("jdbcTable", tableName);

		return tf.createProviderTouchpoint(null, jdbcConnectorType, null, jdbcParams);
	}

	private static IntermediaryTouchpoint createIntermediaryWithDestinations(List<ProviderTouchpoint> dests) throws Exception {
		IntermediaryTouchpoint intTp = tf.createIntermediaryTouchpoint(null, TouchpointTypeLocator.TYPE_VIRTUAL_INTERMEDIARY, null);

		intTp.setEnabled(false);

		for (ProviderTouchpoint dest : dests) {
			intTp.createExternalDestination(dest.getRequestInUrl());
		}

		intTp.setEnabled(true);

		return intTp;
	}
}
