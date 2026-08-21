package com.ibm.di.tp.server.handler;

import static com.ibm.di.test.tp.mock.ServerAPIMockFactory.createNodeEntryContext;
import static com.ibm.di.test.tp.mock.ServerAPIMockFactory.createServerAPIActiveProviderServerContext;
import static com.ibm.di.test.tp.mock.ServerAPIMockFactory.createServerAPIMissingProviderHandlerContext;
import static com.ibm.di.test.tp.mock.ServerAPIMockFactory.createServerAPITPInstanceContext;
import static com.ibm.di.test.tp.mock.ServerAPIMockFactory.createServerAPITypesContext;
import static com.ibm.di.test.utils.atom.AtomUtils.atomEntryComparator;
import static com.ibm.di.test.utils.atom.AtomUtils.createInstAtomEntry;
import static com.ibm.di.test.utils.atom.AtomUtils.deserializeEntry;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.text.IsEmptyString.isEmptyOrNullString;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;

import com.ibm.di.web.common.atom.AtomEntry;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.test.api.mock.ServerAPIMock;
import com.ibm.di.test.tp.TpAppHelper;
import com.ibm.di.test.tp.UnitTestTPClientContext;
import com.ibm.di.test.tp.mock.ConfigInstanceRecorderMock;
import com.ibm.di.test.tp.mock.TPInstanceServerAPIReactor;
import com.ibm.di.test.utils.ConfigUtils;
import com.ibm.di.tp.server.Constants;
import com.ibm.di.tp.server.config.node.TdiNodeConfig;
import com.ibm.di.tp.server.model.TouchpointRole;
import com.ibm.di.tp.server.model.impl.tdi.TouchpointTypeLocator;

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
public class TPInstEntryTest extends UnitTestTPClientContext {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	@Override
	protected boolean isAutoInit() {
		return false;
	}

	@Test
	public void test_GET_Inst_Entry_Returns_Expected_Result() throws Exception {
		TdiNodeConfig nodeCfg = createNodeEntryContext(getTPServerConfig(), 1)[0];
		ServerAPIMock conn = createServerAPITypesContext(null, new String[] { "system:/Connectors/ibmdi.LDAP" });
		conn = createServerAPIActiveProviderServerContext(conn, nodeCfg.getProviderPort());
		setServerAPIConnection(conn);
		conn.activateMocks();
		initContext();

		// Create the entry.
		AtomEntry instEntry = createInstAtomEntry("0", TouchpointRole.PROVIDER, false, null);
		String instId = (String) app.createInstEntry(nodeCfg.getId(), "system:/Connectors/ibmdi.LDAP", instEntry).getHeader(
				"Location");

		// get the created instance entry
		MockHttpServletResponse response = app.getInstEntry(nodeCfg.getId(), "system:/Connectors/ibmdi.LDAP", instId);
		AtomEntry actEntry = deserializeEntry(response.getContentAsString());

		atomEntryComparator.assertEquals(actEntry, instEntry, true);
	}

	@Test
	public void test_DELETE_Inst_Entry_With_No_Precondition() throws Exception {
		TdiNodeConfig nodeCfg = createNodeEntryContext(getTPServerConfig(), 1)[0];
		ServerAPIMock conn = createServerAPITypesContext(null, new String[] { "system:/Connectors/ibmdi.LDAP" });
		conn = createServerAPIActiveProviderServerContext(conn, nodeCfg.getProviderPort());
		conn = createServerAPIMissingProviderHandlerContext(conn, "system:/Connectors/ibmdi.LDAP");
		setServerAPIConnection(conn);
		conn.activateMocks();
		initContext();

		// Create the entry.
		AtomEntry instEntry = createInstAtomEntry("0", TouchpointRole.PROVIDER, false, null);
		MockHttpServletResponse response = app.createInstEntry("Id0", "system:/Connectors/ibmdi.LDAP", instEntry);
		AtomEntry actEntry = deserializeEntry(response.getContentAsString());

		// the presence of such link is guarded by a different test.
		String editUrl = TpAppHelper.getResourceUrlFromEntry(actEntry, Constants.REL_EDIT);

		// the entry is surely created and that is verified in another test. Now
		// try to delete it.
		MockHttpServletRequest request = constructMockRequest(HttpMethod.DELETE, editUrl, MediaType.APPLICATION_ATOM_XML);
		response = invoke(request);

		assertThat(response.getStatus(), is(200));
	}

	@Test
	public void test_DELETE_Inst_Entry_With_Valid_Precondition_If_Match() throws Exception {
		TdiNodeConfig nodeCfg = createNodeEntryContext(getTPServerConfig(), 1)[0];
		ServerAPIMock conn = createServerAPITypesContext(null, new String[] { "system:/Connectors/ibmdi.LDAP" });
		conn = createServerAPIActiveProviderServerContext(conn, nodeCfg.getProviderPort());
		conn = createServerAPIMissingProviderHandlerContext(conn, "system:/Connectors/ibmdi.LDAP");
		setServerAPIConnection(conn);
		conn.activateMocks();
		initContext();

		// Create the entry.
		AtomEntry instEntry = createInstAtomEntry("0", TouchpointRole.PROVIDER, false, null);
		MockHttpServletResponse response = app.createInstEntry("Id0", "system:/Connectors/ibmdi.LDAP", instEntry);

		AtomEntry actEntry = deserializeEntry(response.getContentAsString());
		String eTag = (String) response.getHeader("ETag");

		// the presence of such link is guarded by a different test.
		String editUrl = TpAppHelper.getResourceUrlFromEntry(actEntry, Constants.REL_EDIT);

		// the entry is surely created and that is verified in another test. Now
		// try to delete it.
		MockHttpServletRequest request = constructMockRequest(HttpMethod.DELETE, editUrl, MediaType.APPLICATION_ATOM_XML);
		request.addHeader("If-Match", eTag);
		response = invoke(request);

		assertThat(response.getStatus(), is(200));
	}

	@Test
	public void test_DELETE_Inst_Entry_With_Invalid_Precondition_If_Match() throws Exception {
		TdiNodeConfig nodeCfg = createNodeEntryContext(getTPServerConfig(), 1)[0];
		ServerAPIMock conn = createServerAPITypesContext(null, new String[] { "system:/Connectors/ibmdi.LDAP" });
		conn = createServerAPIActiveProviderServerContext(conn, nodeCfg.getProviderPort());
		conn = createServerAPIMissingProviderHandlerContext(conn, "system:/Connectors/ibmdi.LDAP");
		setServerAPIConnection(conn);
		conn.activateMocks();
		initContext();

		// Create the entry.
		AtomEntry instEntry = createInstAtomEntry("0", TouchpointRole.PROVIDER, false, null);
		MockHttpServletResponse response = app.createInstEntry("Id0", "system:/Connectors/ibmdi.LDAP", instEntry);

		AtomEntry actEntry = deserializeEntry(response.getContentAsString());
		String eTag = (String) response.getHeader("ETag");

		// the presence of such link is guarded by a different test.

		String editUrl = TpAppHelper.getResourceUrlFromEntry(actEntry, Constants.REL_EDIT);

		// the entry is surely created and that is verified in another test. Now
		// try to delete it.
		MockHttpServletRequest request = constructMockRequest(HttpMethod.DELETE, editUrl, MediaType.APPLICATION_ATOM_XML);
		// pass in an invalid eTag
		request.addHeader("If-Match", eTag.substring(eTag.length() - 1) + "invalidSuffix\"");
		response = invoke(request);

		// precondition failed
		assertThat(response.getStatus(), is(412));
	}

	@Test
	public void test_DELETE_Inst_Entry_With_Invalid_Precondition_If_None_Match() throws Exception {
		TdiNodeConfig nodeCfg = createNodeEntryContext(getTPServerConfig(), 1)[0];
		ServerAPIMock conn = createServerAPITypesContext(null, new String[] { "system:/Connectors/ibmdi.LDAP" });
		conn = createServerAPIActiveProviderServerContext(conn, nodeCfg.getProviderPort());
		conn = createServerAPIMissingProviderHandlerContext(conn, "system:/Connectors/ibmdi.LDAP");
		setServerAPIConnection(conn);
		conn.activateMocks();
		initContext();

		// Create the entry.
		AtomEntry instEntry = createInstAtomEntry("0", TouchpointRole.PROVIDER, false, null);
		MockHttpServletResponse response = app.createInstEntry("Id0", "system:/Connectors/ibmdi.LDAP", instEntry);

		AtomEntry actEntry = deserializeEntry(response.getContentAsString());
		String eTag = (String) response.getHeader("ETag");

		// the presence of such link is guarded by a different test.
		String editUrl = TpAppHelper.getResourceUrlFromEntry(actEntry, Constants.REL_EDIT);

		// the entry is surely created and that is verified in another test. Now
		// try to delete it.
		MockHttpServletRequest request = constructMockRequest(HttpMethod.DELETE, editUrl, MediaType.APPLICATION_ATOM_XML);
		request.addHeader("If-None-Match", eTag);
		response = invoke(request);

		assertThat(response.getStatus(), is(412));
	}

	@Test
	public void test_DELETE_Inst_Entry_With_Valid_Precondition_If_None_Match() throws Exception {
		TdiNodeConfig nodeCfg = createNodeEntryContext(getTPServerConfig(), 1)[0];
		ServerAPIMock conn = createServerAPITypesContext(null, new String[] { "system:/Connectors/ibmdi.LDAP" });
		conn = createServerAPIActiveProviderServerContext(conn, nodeCfg.getProviderPort());
		conn = createServerAPIMissingProviderHandlerContext(conn, "system:/Connectors/ibmdi.LDAP");
		setServerAPIConnection(conn);
		conn.activateMocks();
		initContext();

		// Create the entry.
		AtomEntry instEntry = createInstAtomEntry("0", TouchpointRole.PROVIDER, false, null);
		MockHttpServletResponse response = app.createInstEntry("Id0", "system:/Connectors/ibmdi.LDAP", instEntry);

		AtomEntry actEntry = deserializeEntry(response.getContentAsString());
		String eTag = (String) response.getHeader("ETag");

		// the presence of such link is guarded by a different test.
		String editUrl = TpAppHelper.getResourceUrlFromEntry(actEntry, Constants.REL_EDIT);

		// the entry is surely created and that is verified in another test. Now
		// try to delete it.
		MockHttpServletRequest request = constructMockRequest(HttpMethod.DELETE, editUrl, MediaType.APPLICATION_ATOM_XML);
		// pass in an invalid eTag
		request.addHeader("If-None-Match", eTag.substring(eTag.length() - 1) + "invalidSuffix\"");
		response = invoke(request);

		// precondition failed
		assertThat(response.getStatus(), is(200));
	}

	@Test
	public void test_Params_Of_AL_For_Provider_Are_Correctly_Set() throws Exception {
		TdiNodeConfig nodeCfg = createNodeEntryContext(getTPServerConfig(), 1)[0];
		TPInstanceServerAPIReactor robMock = createServerAPITPInstanceContext(getTPServerConfig(), TouchpointRole.PROVIDER, false,
				false, true, "system:/Connectors/ibmdi.LDAP", nodeCfg);
		ConfigInstanceRecorderMock cfgMock = new ConfigInstanceRecorderMock(robMock);
		setServerAPIConnection(cfgMock);
		cfgMock.activateMocks();
		initContext();

		Map<String, String> props = new HashMap<String, String>();
		props.put("param1", "val1");
		props.put("param2", "val2");
		props.put("param3", "val3");
		props.put("param4", "val4");

		// create the instance
		AtomEntry instEntry = app.createTPInstance("Id0", "system:/Connectors/ibmdi.LDAP", TouchpointRole.PROVIDER, props, false);
		TpAppHelper.setInstEnabled(instEntry, true);
		app.putAtomEntry(instEntry);

		// obtain the config
		assertThat(cfgMock.getCiTpMetamergeConfigStr(), not(isEmptyOrNullString()));
		MetamergeConfig mc = ConfigUtils.deserializeConfig(cfgMock.getCiTpMetamergeConfigStr());

		app.assertInstEntryConfigAppliedToTemplate(mc, instEntry, "system:/Connectors/ibmdi.LDAP", TouchpointRole.PROVIDER);
	}

	@Test
	public void test_Params_Of_AL_For_Initiator_Are_Correctly_Set() throws Exception {
		TdiNodeConfig nodeCfg = createNodeEntryContext(getTPServerConfig(), 1)[0];
		TPInstanceServerAPIReactor robMock = createServerAPITPInstanceContext(getTPServerConfig(), TouchpointRole.INITIATOR, false,
				false, true, "system:/Connectors/ibmdi.LDAP", nodeCfg);
		ConfigInstanceRecorderMock cfgMock = new ConfigInstanceRecorderMock(robMock);
		setServerAPIConnection(cfgMock);
		cfgMock.activateMocks();
		initContext();

		Map<String, String> props = new HashMap<String, String>();
		props.put("param1", "val1");
		props.put("param2", "val2");
		props.put("param3", "val3");
		props.put("param4", "val4");

		// create the instance
		AtomEntry instEntry = app.createTPInstance("Id0", "system:/Connectors/ibmdi.LDAP", TouchpointRole.INITIATOR, props, false);
		String destinationFeedUrl = TpAppHelper.getDestinationFeedUrl(instEntry);
		assertNotNull(destinationFeedUrl);
		app.addDestination(destinationFeedUrl, new URL("http://localhost:1111/tp"));
		TpAppHelper.setInstEnabled(instEntry, true);
		app.putAtomEntry(instEntry);

		// obtain the config
		assertThat(cfgMock.getCiTpMetamergeConfigStr(), not(isEmptyOrNullString()));
		MetamergeConfig mc = ConfigUtils.deserializeConfig(cfgMock.getCiTpMetamergeConfigStr());

		app.assertInstEntryConfigAppliedToTemplate(mc, instEntry, "system:/Connectors/ibmdi.LDAP", TouchpointRole.INITIATOR);
	}

	@Test
	public void test_Params_Of_AL_For_Intermediatery_Are_Correctly_Set() throws Exception {
		TdiNodeConfig nodeCfg = createNodeEntryContext(getTPServerConfig(), 1)[0];
		TPInstanceServerAPIReactor robMock = createServerAPITPInstanceContext(getTPServerConfig(), TouchpointRole.INTERMEDIARY,
				false, false, true, TouchpointTypeLocator.TYPE_VIRTUAL_INTERMEDIARY, nodeCfg);
		ConfigInstanceRecorderMock cfgMock = new ConfigInstanceRecorderMock(robMock);
		setServerAPIConnection(cfgMock);
		cfgMock.activateMocks();
		initContext();

		Map<String, String> props = new HashMap<String, String>();
		props.put("param1", "val1");
		props.put("param2", "val2");
		props.put("param3", "val3");
		props.put("param4", "val4");

		// create the instance
		AtomEntry instEntry = app.createTPInstance("Id0", TouchpointTypeLocator.TYPE_VIRTUAL_INTERMEDIARY,
				TouchpointRole.INTERMEDIARY, props, false);
		String destinationFeedUrl = TpAppHelper.getDestinationFeedUrl(instEntry);
		assertNotNull(destinationFeedUrl);
		app.addDestination(destinationFeedUrl, new URL("http://localhost:1111/tp"));
		TpAppHelper.setInstEnabled(instEntry, true);
		app.putAtomEntry(instEntry);

		// obtain the config
		assertThat(cfgMock.getCiTpMetamergeConfigStr(), not(isEmptyOrNullString()));
		MetamergeConfig mc = ConfigUtils.deserializeConfig(cfgMock.getCiTpMetamergeConfigStr());

		app.assertInstEntryConfigAppliedToTemplate(mc, instEntry, TouchpointTypeLocator.TYPE_VIRTUAL_INTERMEDIARY,
				TouchpointRole.INTERMEDIARY);
	}
}
