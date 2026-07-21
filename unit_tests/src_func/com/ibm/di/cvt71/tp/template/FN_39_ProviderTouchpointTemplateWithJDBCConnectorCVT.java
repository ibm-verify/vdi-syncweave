package com.ibm.di.cvt71.tp.template;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
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
import com.ibm.di.test.utils.func.tp.ProviderTouchpoint;
import com.ibm.di.test.utils.func.tp.ProviderTouchpointResponse;
import com.ibm.di.test.utils.func.tp.TouchpointData;
import com.ibm.di.test.utils.func.tp.TouchpointFactory;
import com.ibm.di.util.FileUtils;

/**
 * Test the provider touchpoint template configuration. The touchpoint is
 * configured with a JDBC Connector.
 */
@CVTComponent(name = "tpserver")
public class FN_39_ProviderTouchpointTemplateWithJDBCConnectorCVT {

	private static final String TABLE_NAME = "people";

	private static final Person JOHN = new Person("john", "smith", 50, 185);

	private static final Person MARY = new Person("mary", "smith", 40, 175);

	private static final Person PETER = new Person("peter", "smith", 20, 180);

	private static final String QUERY_JOHN = "firstname=john&lastname=smith";

	private static final String QUERY_SMITHS = "lastname=smith";

	private static TDIServer tdi = null;

	private static DerbyServer derby = null;

	private static ProviderTouchpoint tp = null;

	private static TouchpointFactory tf = null;

	private static FileRecorder recorder;

	private PersonDAO dao = null;

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

		tp = createOperationalTouchpoint();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {

		if (tp != null) {
			tp.deleteTouchpoint();
			tp = null;
		}

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

	@Before
	public void setUp() throws Exception {
		dao = new PersonDAO(derby.getConnection(), TABLE_NAME);
	}

	@After
	public void tearDown() throws Exception {
		if (dao != null) {
			dao.destroy();
			dao = null;
		}
	}

	/**
	 * Verify that the template provider touchpoint returns HTTP 404 code when
	 * no entry is found for HTTP GET request with no query parameters.
	 */
	@CVTTest(name = "CVT_FN-39_TP_Server_Provider_TP_Template_TC01")
	@Test
	public void test_http_get_with_no_url_query_params_no_entry_found() throws Exception {

		ProviderTouchpointResponse response = tp.get();

		assertEquals(404, response.getResponseCode());
	}

	/**
	 * Verify that the template provider touchpoint returns the corresponding
	 * entry when one entry is found for HTTP GET request with no query
	 * parameters.
	 */
	@CVTTest(name = "CVT_FN-39_TP_Server_Provider_TP_Template_TC02")
	@Test
	public void test_http_get_with_no_url_query_params_one_entry_found() throws Exception {

		dao.add(JOHN);

		Set<Person> people = getPeople(tp.get());

		assertEqualSets(people, JOHN);
		assertEqualSets(dao.list(), JOHN);
	}

	/**
	 * Verify that HTTP GET request with no query parameters is idempotent for
	 * the template provider touchpoint.
	 */
	@CVTTest(name = "CVT_FN-39_TP_Server_Provider_TP_Template_TC03")
	@Test
	public void test_http_get_with_no_url_query_params_is_idempotent() throws Exception {

		dao.add(JOHN);

		final int attempts = 3;
		for (int i = 0; i < attempts; ++i) {
			Set<Person> people = getPeople(tp.get());

			assertEqualSets(people, JOHN);
			assertEqualSets(dao.list(), JOHN);
		}
	}

	/**
	 * Verify that the template provider touchpoint returns the corresponding
	 * entries when multiple entries are found for HTTP GET request with no
	 * query parameters.
	 */
	@CVTTest(name = "CVT_FN-39_TP_Server_Provider_TP_Template_TC04")
	@Test
	public void test_http_get_with_no_url_query_params_three_entries_found() throws Exception {

		dao.add(JOHN);
		dao.add(MARY);
		dao.add(PETER);

		Set<Person> people = getPeople(tp.get());

		assertEqualSets(people, JOHN, MARY, PETER);
		assertEqualSets(dao.list(), JOHN, MARY, PETER);
	}

	/**
	 * Verify that the template provider touchpoint does not exceed the size
	 * limit when multiple entries are found for HTTP GET request with no query
	 * parameters.
	 */
	@CVTTest(name = "CVT_FN-39_TP_Server_Provider_TP_Template_TC05")
	@Test
	public void test_http_get_with_no_url_query_params_three_entries_found_sizelimit_is_two() throws Exception {

		Set<Person> smiths = new HashSet<Person>();
		smiths.add(JOHN);
		smiths.add(MARY);
		smiths.add(PETER);
		for (Person p : smiths) {
			dao.add(p);
		}

		final int sizeLimit = 2;
		Set<Person> people = getPeople(tp.get(sizeLimit));

		assertEquals(sizeLimit, people.size());
		for (Person p : people) {
			assertTrue(smiths.contains(p));
		}
	}

	/**
	 * Verify that the template provider touchpoint returns HTTP 404 code when
	 * no entry is found for HTTP GET request with query parameters.
	 */
	@CVTTest(name = "CVT_FN-39_TP_Server_Provider_TP_Template_TC06")
	@Test
	public void test_http_get_with_url_query_params_no_entry_found() throws Exception {

		ProviderTouchpointResponse response = tp.get(QUERY_JOHN);

		assertEquals(404, response.getResponseCode());
	}

	/**
	 * Verify that HTTP GET request with query parameters is idempotent for the
	 * template provider touchpoint.
	 */
	@CVTTest(name = "CVT_FN-39_TP_Server_Provider_TP_Template_TC07")
	@Test
	public void test_http_get_with_url_query_params_is_idempotent() throws Exception {

		dao.add(JOHN);

		final int attempts = 3;
		for (int i = 0; i < attempts; ++i) {
			Set<Person> people = getPeople(tp.get(QUERY_JOHN));

			assertEqualSets(people, JOHN);
			assertEqualSets(dao.list(), JOHN);
		}
	}

	/**
	 * Verify that the template provider touchpoint returns the corresponding
	 * entry when one entry is found for HTTP GET request with query parameters.
	 */
	@CVTTest(name = "CVT_FN-39_TP_Server_Provider_TP_Template_TC08")
	@Test
	public void test_http_get_with_url_query_params_one_entry_found() throws Exception {

		dao.add(JOHN);

		Set<Person> people = getPeople(tp.get(QUERY_JOHN));

		assertEqualSets(people, JOHN);
	}

	/**
	 * Verify that the template provider touchpoint returns the corresponding
	 * entries when multiple entries are found for HTTP GET request with query
	 * parameters.
	 */
	@CVTTest(name = "CVT_FN-39_TP_Server_Provider_TP_Template_TC09")
	@Test
	public void test_http_get_with_url_query_params_three_entries_found() throws Exception {

		dao.add(JOHN);
		dao.add(MARY);
		dao.add(PETER);

		Set<Person> people = getPeople(tp.get(QUERY_SMITHS));

		assertEqualSets(people, JOHN, MARY, PETER);
	}

	/**
	 * Verify that the template provider touchpoint does not exceed the size
	 * limit when multiple entries are found for HTTP GET request with query
	 * parameters.
	 */
	@CVTTest(name = "CVT_FN-39_TP_Server_Provider_TP_Template_TC10")
	@Test
	public void test_http_get_with_url_query_params_three_entries_found_sizelimit_is_two() throws Exception {

		Set<Person> smiths = new HashSet<Person>();
		smiths.add(JOHN);
		smiths.add(MARY);
		smiths.add(PETER);
		for (Person p : smiths) {
			dao.add(p);
		}

		final int sizeLimit = 2;
		Set<Person> people = getPeople(tp.get(QUERY_SMITHS, sizeLimit));

		assertEquals(sizeLimit, people.size());
		for (Person p : people) {
			assertTrue(smiths.contains(p));
		}
	}

	/**
	 * Verify that the template provider touchpoint returns only entries matched
	 * by the query parameters for HTTP GET request.
	 */
	@CVTTest(name = "CVT_FN-39_TP_Server_Provider_TP_Template_TC11")
	@Test
	public void test_http_get_with_url_query_params_return_only_entries_that_match_the_query() throws Exception {

		dao.add(JOHN);
		dao.add(MARY);
		dao.add(PETER);

		Set<Person> people = getPeople(tp.get(QUERY_JOHN));

		assertEqualSets(people, JOHN);
	}

	/**
	 * Verify that the template provider touchpoint creates new entry for HTTP
	 * POST request.
	 */
	@CVTTest(name = "CVT_FN-39_TP_Server_Provider_TP_Template_TC12")
	@Test
	public void test_http_post_create_new_entry() throws Exception {

		dao.add(MARY);

		TouchpointData data = new TouchpointData(JOHN.toEntry());

		ProviderTouchpointResponse response = tp.post(data);

		assertEquals(201, response.getResponseCode());

		assertEqualSets(dao.list(), JOHN, MARY);
	}

	/**
	 * Verify that the template provider touchpoint creates new entry for HTTP
	 * PUT request when no existing entry matches the query parameters from the
	 * request URL.
	 */
	@CVTTest(name = "CVT_FN-39_TP_Server_Provider_TP_Template_TC13")
	@Test
	public void test_http_put_create_new_entry() throws Exception {

		dao.add(MARY);

		TouchpointData data = new TouchpointData(JOHN.toEntry());

		ProviderTouchpointResponse response = tp.put(QUERY_JOHN, data);

		assertEquals(201, response.getResponseCode());

		assertEqualSets(dao.list(), JOHN, MARY);
	}

	/**
	 * Verify that the template provider touchpoint performs update for HTTP PUT
	 * request when existing entry matches the query parameters from the request
	 * URL.
	 */
	@CVTTest(name = "CVT_FN-39_TP_Server_Provider_TP_Template_TC14")
	@Test
	public void test_http_put_update_entry() throws Exception {

		dao.add(JOHN);
		dao.add(MARY);
		dao.add(PETER);

		final int newAge = JOHN.getAge() + 1;
		Person newJohn = new Person(JOHN.getFirstName(), JOHN.getLastName(), newAge, JOHN.getHeight());

		Entry modifications = new Entry();
		modifications.setAttribute("age", "" + newAge);
		TouchpointData data = new TouchpointData(modifications);

		ProviderTouchpointResponse response = tp.put(QUERY_JOHN, data);

		assertEquals(204, response.getResponseCode());

		assertEqualSets(dao.list(), newJohn, MARY, PETER);
	}

	/**
	 * Verify that the template provider touchpoint performs update for HTTP PUT
	 * request when query parameters contain value that starts with dollar sign.
	 */
	@CVTTest(name = "CVT_FN-39_TP_Server_Provider_TP_Template_TC15")
	@Test
	public void test_http_put_update_entry_using_value_with_dollar_sign_in_query() throws Exception {

		final int age = -1;
		final int newAge = -5;
		final int height = -1;
		Person person = new Person("$$$", "$$$", age, height);
		Person newPerson = new Person("$$$", "$$$", newAge, height);

		dao.add(person);
		dao.add(JOHN);

		Entry modifications = new Entry();
		modifications.setAttribute("age", newAge);
		TouchpointData data = new TouchpointData(modifications);

		ProviderTouchpointResponse response = tp.put("firstname=$$$", data);

		assertEquals(204, response.getResponseCode());

		assertEqualSets(dao.list(), newPerson, JOHN);
	}

	/**
	 * Verify that the template provider touchpoint returns an error for HTTP
	 * PUT request when query parameters match multiple entries.
	 */
	@CVTTest(name = "CVT_FN-39_TP_Server_Provider_TP_Template_TC16")
	@Test
	public void test_http_put_return_error_when_multiple_entries_found() throws Exception {

		dao.add(JOHN);
		dao.add(MARY);
		dao.add(PETER);

		Entry modifications = new Entry();
		modifications.setAttribute("age", "100");
		TouchpointData data = new TouchpointData(modifications);

		ProviderTouchpointResponse response = tp.put(QUERY_SMITHS, data);

		assertTrue(response.isError());

		assertEqualSets(dao.list(), JOHN, MARY, PETER);
	}

	/**
	 * Verify that the template provider touchpoint deletes an entry for HTTP
	 * DELETE request when query parameters match single entry.
	 */
	@CVTTest(name = "CVT_FN-39_TP_Server_Provider_TP_Template_TC17")
	@Test
	public void test_http_delete_one_entry() throws Exception {

		dao.add(JOHN);
		dao.add(MARY);
		dao.add(PETER);

		ProviderTouchpointResponse response = tp.delete(QUERY_JOHN);
		assertEquals("" + response.getErrorMessage(), 204, response.getResponseCode());

		assertEqualSets(dao.list(), MARY, PETER);
	}

	/**
	 * Verify that the template provider touchpoint returns no error for HTTP
	 * DELETE request when query parameters match no entries.
	 */
	@CVTTest(name = "CVT_FN-39_TP_Server_Provider_TP_Template_TC18")
	@Test
	public void test_http_delete_return_no_error_when_no_entry_found() throws Exception {

		dao.add(MARY);

		ProviderTouchpointResponse response = tp.delete(QUERY_JOHN);

		assertFalse(response.getErrorMessage(), response.isError());

		assertEqualSets(dao.list(), MARY);
	}

	/**
	 * Verify that the template provider touchpoint returns an error for HTTP
	 * DELETE request when query parameters match multiple entries.
	 */
	@CVTTest(name = "CVT_FN-39_TP_Server_Provider_TP_Template_TC19")
	@Test
	public void test_http_delete_return_error_when_multiple_entries_found() throws Exception {

		dao.add(JOHN);
		dao.add(MARY);
		dao.add(PETER);

		ProviderTouchpointResponse response = tp.delete(QUERY_SMITHS);

		assertTrue(response.isError());

		assertEqualSets(dao.list(), JOHN, MARY, PETER);
	}

	/**
	 * Verify that HTTP DELETE request with query parameters is idempotent for
	 * the template provider touchpoint.
	 */
	@CVTTest(name = "CVT_FN-39_TP_Server_Provider_TP_Template_TC20")
	@Test
	public void test_http_delete_is_idempotent() throws Exception {

		dao.add(JOHN);
		dao.add(MARY);

		ProviderTouchpointResponse response1 = tp.delete(QUERY_JOHN);
		assertFalse(response1.getErrorMessage(), response1.isError());

		ProviderTouchpointResponse response2 = tp.delete(QUERY_JOHN);
		assertFalse(response2.getErrorMessage(), response2.isError());

		assertEqualSets(dao.list(), MARY);
	}

	/**
	 * Verify that the template provider touchpoint returns an error for an HTTP
	 * GET request when the Connector fails on initialize.
	 */
	@CVTTest(name = "undocumented")
	@Test
	public void test_http_get_return_error_when_connector_fail_on_initialize() throws Exception {
		String jdbcConnectorType = "system:/Connectors/ibmdi.JDBC";
		Map<String, String> jdbcParams = new HashMap<String, String>();
		// leave the jdbc url empty, so that the Connector fails on initialize
		jdbcParams.put("jdbcDriver", "");

		ProviderTouchpoint broken = tf.createProviderTouchpoint(null, jdbcConnectorType, null, jdbcParams);
		ProviderTouchpointResponse response = broken.get();
		broken.deleteTouchpoint();
		assertTrue(response.isError());
		assertNotNull(response.getErrorMessage());
		// must have some explanation of the error
		assertTrue(response.getErrorMessage().trim().length() > 0);
	}

	/**
	 * Verify that once a provider touchpoint is deleted, it no longer responds
	 * to its original URL.
	 */
	@CVTTest(name = "undocumented")
	@Test
	public void test_deleted_touchpoint_does_not_respond_on_its_url() throws Exception {

		ProviderTouchpoint tp;
		try {
			tp = createOperationalTouchpoint();
			tp.deleteTouchpoint();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}

		// expect only this call to throw
		TouchpointData data = new TouchpointData(JOHN.toEntry());
		ProviderTouchpointResponse response = tp.post(data);
		assertEquals(404, response.getResponseCode());
		assertNotNull(response.getErrorMessage());
		assertTrue(response.getErrorMessage().trim().length() > 0);
	}

	private Set<Person> getPeople(ProviderTouchpointResponse response) {
		assertEquals(200, response.getResponseCode());
		TouchpointData data = response.getData();
		assertNotNull(data);
		Set<Person> people = new HashSet<Person>();
		for (Entry entry : data.getEntries()) {
			Person person = new Person(entry);
			people.add(person);
		}
		return people;
	}

	private void assertEqualSets(Set<?> actualSet, Object... expectedObjects) {
		assertEquals("Set " + actualSet + " is not of size " + expectedObjects.length, expectedObjects.length, actualSet.size());
		for (Object o : expectedObjects) {
			assertTrue("Set " + actualSet + " does not contain object " + o, actualSet.contains(o));
		}
	}

	private static ProviderTouchpoint createOperationalTouchpoint() throws Exception {

		String jdbcConnectorType = "system:/Connectors/ibmdi.JDBC";
		Map<String, String> jdbcParams = new HashMap<String, String>();
		jdbcParams.put("jdbcDriver", derby.getJdbcDriverClassName());
		jdbcParams.put("jdbcLogin", derby.getUsername());
		jdbcParams.put("jdbcPassword", derby.getPassword());
		jdbcParams.put("jdbcSource", derby.getJdbcUrl());
		jdbcParams.put("jdbcTable", TABLE_NAME);

		return tf.createProviderTouchpoint(null, jdbcConnectorType, null, jdbcParams);
	}

}
