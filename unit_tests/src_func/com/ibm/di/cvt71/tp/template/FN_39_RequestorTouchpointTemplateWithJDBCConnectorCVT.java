package com.ibm.di.cvt71.tp.template;

import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
import com.ibm.di.test.utils.func.tp.DestinationService;
import com.ibm.di.test.utils.func.tp.InitiatorTouchpoint;
import com.ibm.di.test.utils.func.tp.TouchpointData;
import com.ibm.di.test.utils.func.tp.TouchpointFactory;
import com.ibm.di.util.FileUtils;

/**
 * Test the requestor touchpoint template configuration. The touchpoint is
 * configured with a JDBC Connector.
 */
@CVTComponent(name = "tpserver")
public class FN_39_RequestorTouchpointTemplateWithJDBCConnectorCVT {

	private static final String TABLE_NAME = "people";

	private static final Person JOHN = new Person("john", "smith", 50, 185);

	private static final Person MARY = new Person("mary", "smith", 40, 175);

	private static final Person PETER = new Person("peter", "smith", 20, 180);

	private static TDIServer tdi = null;

	private static DerbyServer derby = null;

	private static TouchpointFactory tf = null;

	private static FileRecorder recorder;

	private InitiatorTouchpoint tp;

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

	@Before
	public void setUp() throws Exception {
		dao = new PersonDAO(derby.getConnection(), TABLE_NAME);

		String jdbcConnectorType = "system:/Connectors/ibmdi.JDBC";
		Map<String, String> jdbcParams = new HashMap<String, String>();
		jdbcParams.put("jdbcDriver", derby.getJdbcDriverClassName());
		jdbcParams.put("jdbcLogin", derby.getUsername());
		jdbcParams.put("jdbcPassword", derby.getPassword());
		jdbcParams.put("jdbcSource", derby.getJdbcUrl());
		jdbcParams.put("jdbcTable", TABLE_NAME);

		tp = tf.createInitiatorTouchpoint("default", jdbcConnectorType, jdbcParams);
	}

	@After
	public void tearDown() throws Exception {
		if (dao != null) {
			dao.destroy();
			dao = null;
		}

		if (tp != null) {
			tp.deleteTouchpoint();
			tp = null;
		}
	}

	/**
	 * Verify that the template initiator touchpoint sends to the service all
	 * entries from a data source.
	 */
	@CVTTest(name = "CVT_FN-39_TP_Server_Initiator_TP_Template_TC01")
	@Test
	public void test_consume_all_entries_and_send_them_to_single_destinations() throws Exception {
		dao.add(JOHN);
		dao.add(MARY);
		dao.add(PETER);

		DestinationService destService = tp.createDestinationService(PortProbe.getAvailablePort());

		Set<Person> people = getPeople(destService.consume(3));

		assertTrue(people.contains(JOHN));
		assertTrue(people.contains(MARY));
		assertTrue(people.contains(PETER));
	}

	/**
	 * Verify that the template initiator touchpoint sends to multiple services
	 * all entries from a data source.
	 */
	@CVTTest(name = "CVT_FN-39_TP_Server_Initiator_TP_Template_TC02")
	@Test
	public void test_consume_all_entries_and_send_them_to_all_destinations() throws Exception {
		dao.add(JOHN);
		dao.add(MARY);
		dao.add(PETER);

		tp.setEnabled(false);
		DestinationService destService1 = tp.createDestinationService(PortProbe.getAvailablePort());
		DestinationService destService2 = tp.createDestinationService(PortProbe.getAvailablePort());
		DestinationService destService3 = tp.createDestinationService(PortProbe.getAvailablePort());
		tp.setEnabled(true);

		Set<Person> people1 = getPeople(destService1.consume(3));
		Set<Person> people2 = getPeople(destService2.consume(3));
		Set<Person> people3 = getPeople(destService3.consume(3));

		assertThat(people1, containsInAnyOrder(JOHN, MARY, PETER));
		assertThat(people2, containsInAnyOrder(JOHN, MARY, PETER));
		assertThat(people3, containsInAnyOrder(JOHN, MARY, PETER));
	}

	/**
	 * Verify that the template initiator touchpoint sends to multiple services
	 * all entries from a data source and ignores unavailable destinations
	 */
	@CVTTest(name = "CVT_FN-39_TP_Server_Initiator_TP_Template_TC03")
	@Test
	public void test_consume_all_entries_and_send_them_to_all_destinations_and_ignore_invalid_urls() throws Exception {
		dao.add(JOHN);
		dao.add(MARY);
		dao.add(PETER);

		tp.setEnabled(false);
		DestinationService destService1 = tp.createDestinationService(PortProbe.getAvailablePort());
		DestinationService destService2 = tp.createDestinationService(PortProbe.getAvailablePort());
		DestinationService destService3 = tp.createDestinationService(PortProbe.getAvailablePort());

		// stop the server
		destService2.close();
		tp.setEnabled(true);

		Set<Person> people1 = getPeople(destService1.consume(3));
		Set<Person> people3 = getPeople(destService3.consume(3));

		assertThat(people1, containsInAnyOrder(JOHN, MARY, PETER));
		assertThat(people3, containsInAnyOrder(JOHN, MARY, PETER));
	}

	private Set<Person> getPeople(Collection<TouchpointData> dataItems) {
		Set<Person> people = new HashSet<Person>();
		for (TouchpointData data : dataItems) {
			List<Entry> entries = data.getEntries();
			assertEquals(1, entries.size());
			Person person = new Person(entries.get(0));
			people.add(person);
		}
		return people;
	}

}
