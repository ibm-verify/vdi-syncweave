package com.ibm.di.tp.server.handler;

import static com.ibm.di.test.utils.atom.AtomUtils.atomCategoryComparator;
import static com.ibm.di.test.utils.atom.AtomUtils.atomEntryComparator;
import static com.ibm.di.test.utils.atom.AtomUtils.atomPersonComparator;
import static com.ibm.di.test.utils.atom.AtomUtils.containsInAnyOrder;
import static com.ibm.di.test.utils.atom.AtomUtils.createInstAtomEntry;
import static com.ibm.di.test.utils.atom.AtomUtils.deserializeEntry;
import static com.ibm.di.test.utils.atom.AtomUtils.deserializeFeed;
import static com.ibm.di.test.utils.atom.AtomUtils.elementComparator;
import static com.ibm.di.test.utils.atom.AtomUtils.findLinksByRel;
import static com.ibm.di.test.utils.atom.AtomUtils.serializeEntry;
import static com.ibm.di.test.utils.atom.AtomUtils.winkCatToInternal;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBException;

import com.ibm.di.web.common.atom.app.AppCollection;
import com.ibm.di.web.common.atom.app.AppService;
import com.ibm.di.web.common.atom.app.AppWorkspace;
import com.ibm.di.web.common.atom.AtomCategory;
import com.ibm.di.web.common.atom.AtomEntry;
import com.ibm.di.web.common.atom.AtomFeed;
import com.ibm.di.web.common.atom.AtomLink;
import com.ibm.di.web.common.atom.AtomPerson;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.di.test.http.HttpClientContext;
import com.ibm.di.test.tp.TpAppHelper;
import com.ibm.di.test.utils.atom.AtomUtils;
import com.ibm.di.test.utils.func.PortProbe;
import com.ibm.di.test.utils.func.TDIServer;
import com.ibm.di.test.utils.func.tp.DestinationService;
import com.ibm.di.test.utils.func.tp.InitiatorTouchpoint;
import com.ibm.di.test.utils.func.tp.IntermediaryTouchpoint;
import com.ibm.di.test.utils.func.tp.ProviderTouchpoint;
import com.ibm.di.test.utils.func.tp.ProviderTouchpointResponse;
import com.ibm.di.test.utils.func.tp.Touchpoint;
import com.ibm.di.test.utils.func.tp.TouchpointData;
import com.ibm.di.test.utils.func.tp.TouchpointFactory;
import com.ibm.di.tp.server.Constants;
import com.ibm.di.tp.server.model.TouchpointRole;
import com.ibm.di.tp.server.model.config.DestinationData;
import com.ibm.di.tp.server.model.config.EnumOpState;
import com.ibm.di.tp.server.model.config.InstanceData;
import com.ibm.di.tp.server.model.config.ObjectFactory;
import com.ibm.di.tp.server.model.config.Property;
import com.ibm.di.tp.server.model.config.PropertySheet;
import com.ibm.di.tp.server.model.config.TouchpointStatus;
import com.ibm.di.tp.server.util.SCMPUtils;
import com.ibm.di.util.DOMUtils;

/**
 * Shared class for both Unit Test framework and Function Test framework. <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public class UnitAndFuncSharedTests {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Prerequisite Marker Class<br>
	 * <br>
	 * <h3><b>Configure default TP Server to start with TDI</b><br>
	 * </h3> Edit the solution.properties file and set <b>api.rest.on</b>=true
	 */
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.SOURCE)
	private static @interface Configure_TP_Server_To_Start_With_TDI {
	}

	/**
	 * Prerequisite Marker Class<br>
	 * <br>
	 * <h3><b>Configure the TP Server to have two TP Provider nodes</b></h3><br>
	 * Edit the file specified by the <b>tp.server.config</b> property.
	 * 
	 * <h3><b>Define Local API node named "localNode"</b></h3><br>
	 * Configure the tdiNodeConfigs section to have a node called "localNode".
	 * Make sure it uses the local API. Make sure it has all the other
	 * parameters set: providerHost, providerPort, author, location, contact,
	 * etc.
	 * 
	 * <h3><b>Define Remote API node named "remoteNode"</b></h3><br>
	 * Configure the tdiNodeConfigs section to have a node called "remoteNode".
	 * Make sure it uses the remote API to the TDI server. Make sure it has all
	 * the other parameters set: providerHost, providerPort, author, location,
	 * contact, etc.
	 * 
	 */
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.SOURCE)
	private static @interface Configure_TDI_To_Have_Two_TP_Provider_Nodes {
	}

	/**
	 * Prerequisite Marker Class<br>
	 * <br>
	 * <h3><b>Configure the TP Server to have a custom templates directory</b></h3>
	 * <br>
	 * Configure the <b>customTemplatesDir</b> parameter to point to directory
	 * "templates"
	 */
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.SOURCE)
	private static @interface Configure_TP_Server_To_Have_Custom_Templates_Directory {
	}

	/**
	 * Prerequisite Marker Class<br>
	 * <br>
	 * <h3><b>Clean up the persistence store directory</b></h3><br>
	 * Make sure the TP Server has its persistence store activated and that the
	 * specified by the <b>location</b> parameter is empty
	 */
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.SOURCE)
	private static @interface Clean_Up_The_Persistence_Store_Directory {
	}

	/**
	 * Prerequisite Marker Class<br>
	 * <br>
	 * <h3><b>Create a test connector with id "ibmdi.SCMP.Test"</b></h3><br>
	 * Edit The connector must support Iterator and AddOnly modes. The connector
	 * should respect an integer parameter called itCount that specifies the
	 * numbers entries returned. When the idCount is configured with -1 the
	 * connector will keep feeding the same entry infinitely. When the idCount
	 * is configured with 0 the connector will not return any entries. Instead
	 * it will return EOD entry (null entry) on the first iteration. Every other
	 * value of idCount greater than 0 specifies the number of iterations this
	 * connector will do. Make sure this connector is placed inside the
	 * <tdi_install_dir>/jars/conectors folder if it is a standalone connector
	 * otherwise if you create it is a script connector makes sure it is placed
	 * in the TouchpointTemplate config.
	 * 
	 * <h3><b>Create custom template with connector "ibmdi.SCMP.Test"</b></h3> <br>
	 * Copy the Touchpoint Template specified by the baseTemplate to the
	 * directory specified by the customTemplatesDir parameter. Call the copied
	 * file Custom.SCMP.Test.xml. Make sure the template ServiceConnectors
	 * inherit from the "ibmdi.SCMP.Test" connector.
	 */
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.SOURCE)
	private static @interface Create_Test_Connector {
	}

	/**
	 * Prerequisite Marker Class<br>
	 * <br>
	 * <h3><b>Start TDI</b></h3><br>
	 * Start the TDI server in daemon mode. <b>ibmdisrv -d</b>
	 */
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.SOURCE)
	private static @interface Start_TDI {
	}

	protected static final String CONNECTOR_PARAM_SCRIPT = "" //
			+ "var counter = 0;" //
			+ "var itCountStr = connector.getParam(\"itCount\");" //
			+ "var itCount = itCountStr != null ? java.lang.Integer.parseInt(itCountStr) : 0;" //
			+ "" // 
			+ "function selectEntries() " //
			+ "{" // 
			+ "}" //
			+ "" //
			+ "function getNextEntry ()" //
			+ "{" //
			+ "	if (itCount != -1 && counter >= itCount) {" //
			+ "		result.setStatus (0);" //
			+ "		result.setMessage (\"End of input\");" //
			+ "		return;" + "	}" //
			+ "" //
			+ "	entry.setAttribute (\"counter\", counter);" //
			+ "	counter++;" //
			+ "}" //
			+ "" //
			+ "function modEntry() " //
			+ "{" // 
			+ "}" //
			+ "" //
			+ "function deleteEntry() " //
			+ "{" // 
			+ "}" //
			+ "" //
			+ "function findEntry() " //
			+ "{" // 
			+ "}" //
			+ "" //
			+ "function putEntry() " //
			+ "{" // 
			+ "}" //
			+ "" //
			+ "function queryReply() " //
			+ "{" // 
			+ "}" //
			+ "" //
			+ "function terminate() " //
			+ "{" // 
			+ "}" //
			+ "" //
			+ "function querySchema() " //
			+ "{" // 
			+ "}" //
	;

	@Configure_TP_Server_To_Start_With_TDI
	@Start_TDI
	public static void verify_Service_Document_Has_Link_To_TP_Node_Feed(HttpClientContext tpClientCtx) throws Exception {
		AppService sd = new TpAppHelper(tpClientCtx).getServiceDocumentBinded();

		String feedUrl = null;
		lookup: for (AppWorkspace wspace : sd.getWorkspace()) {
			for (AppCollection col : wspace.getCollection()) {
				if (col.getCategories() != null
						&& containsInAnyOrder(atomCategoryComparator, false, true,
								col.getCategories().getCategory(), winkCatToInternal(Constants.CAT_CONN_PROVIDER))) {
					feedUrl = col.getHref();
					break lookup;
				}
			}
		}

		assertThat("Found a collection with \"connectivity-provider\" category", feedUrl, is(not(nullValue())));

		// assert the link points to an existing resource.
		MockHttpServletRequest request = tpClientCtx.constructMockRequest(HttpMethod.GET, feedUrl, MediaType.WILDCARD);
		TpAppHelper.checkSuccess(tpClientCtx.invoke(request));
	}

	@Configure_TP_Server_To_Start_With_TDI
	@Start_TDI
	public static void verify_TP_Node_Feed_Contains_Expected_Categories(HttpClientContext tpClientCtx)
			throws UnsupportedEncodingException, Exception {
		MockHttpServletResponse response = new TpAppHelper(tpClientCtx).getNodeFeed();
		TpAppHelper.checkSuccess(response);
		String contentAsString = response.getContentAsString();
		AtomFeed feed = deserializeFeed(contentAsString);

		AtomUtils.containsInAnyOrder(atomCategoryComparator, false, false, feed.getCategories(), winkCatToInternal(Constants.CAT_CONN_PROVIDER));
	}

	@Configure_TP_Server_To_Start_With_TDI
	@Configure_TDI_To_Have_Two_TP_Provider_Nodes
	@Start_TDI
	public static void verify_TP_Node_Feed_Contains_Links_To_TP_Node_Entries(HttpClientContext tpClientCtx)
			throws UnsupportedEncodingException, Exception {
		MockHttpServletResponse response = new TpAppHelper(tpClientCtx).getNodeFeed();
		TpAppHelper.checkSuccess(response);
		String contentAsString = response.getContentAsString();
		AtomFeed feed = deserializeFeed(contentAsString);

		assertThat(feed.getEntries().size(), is(2));
		assertContainsLinks(feed.getEntries().get(0), Constants.REL_SELF);
		assertContainsLinks(feed.getEntries().get(1), Constants.REL_SELF);
	}

	@Configure_TP_Server_To_Start_With_TDI
	@Configure_TDI_To_Have_Two_TP_Provider_Nodes
	@Start_TDI
	public static void verify_TP_Node_Entry_Contains_The_Configured_Data_And_Link_To_TP_Type_Feed(HttpClientContext tpClientCtx,
			String nodeId, String expTitle, String expAuthor, String expEmail, String expSummary, String expContact,
			String expLocation, String expOrganization) throws UnsupportedEncodingException, Exception {

		MockHttpServletResponse response = new TpAppHelper(tpClientCtx).getNodeEntry(nodeId);
		TpAppHelper.checkSuccess(response);

		AtomEntry nodeEntry = deserializeEntry(response.getContentAsString());

		// check title
		assertThat(nodeEntry.getTitle().getValue(), is(expTitle));

		// check email and author
		AtomPerson ap = new AtomPerson();
		ap.setEmail(expEmail);
		ap.setName(expAuthor);
		containsInAnyOrder(atomPersonComparator, false, false, nodeEntry.getAuthors(), ap);

		// check summary
		assertThat(nodeEntry.getSummary().getValue(), is(expSummary));

		// check scmp data element
		Element actData = DOMUtils.getElementByName(nodeEntry.getAny(), "data", Constants.NS_SCMP);
		assertThat(actData, is(not(nullValue())));

		Element expData = SCMPUtils.createConnectivityProviderElement(Constants.CONNECTIVITY_PROVIDER_TDI_TYPE, expLocation,
				expOrganization, expContact);
		elementComparator.assertEquals(actData, expData, true);

		// check there is a link to tp type feed.
		assertContainsLinks(nodeEntry, Constants.REL_TOUCHPOINT);

		verifyDataElementHasXsiSchemaLocation(tpClientCtx, nodeEntry);
	}

	@Configure_TP_Server_To_Start_With_TDI
	@Configure_TDI_To_Have_Two_TP_Provider_Nodes
	@Configure_TP_Server_To_Have_Custom_Templates_Directory
	@Create_Test_Connector
	@Start_TDI
	public static void verify_TP_Type_Feed_Contains_Links_To_TP_Type_Entries(HttpClientContext tpClientCtx, String nodeId)
			throws Exception {
		MockHttpServletResponse response = new TpAppHelper(tpClientCtx).getTypeFeed(nodeId);
		TpAppHelper.checkSuccess(response);
		AtomFeed feed = deserializeFeed(response.getContentAsString());

		containsInAnyOrder(atomCategoryComparator, false, false, feed.getCategories(), winkCatToInternal(Constants.CAT_TOUCHPOINT));

		// ibmdi.SCMP.Test and Custom.SCMP.Test
		assertThat(feed.getEntries().size(), is(greaterThanOrEqualTo(2)));

		for (AtomEntry entry : feed.getEntries()) {
			assertContainsLinks(entry, Constants.REL_SELF);
		}
	}

	@Configure_TP_Server_To_Start_With_TDI
	@Configure_TDI_To_Have_Two_TP_Provider_Nodes
	@Configure_TP_Server_To_Have_Custom_Templates_Directory
	@Create_Test_Connector
	@Start_TDI
	public static void verify_TP_Type_Entry_Contains_Required_Categories_And_A_Link_To_TP_Inst_Feed(HttpClientContext tpClientCtx,
			String nodeId, String typeId) throws Exception {
		MockHttpServletResponse response = new TpAppHelper(tpClientCtx).getTypeEntry(nodeId, typeId);
		TpAppHelper.checkSuccess(response);

		AtomEntry typeEntry = deserializeEntry(response.getContentAsString());
		containsInAnyOrder(atomCategoryComparator, false, false, typeEntry.getCategories(), winkCatToInternal(Constants.CAT_RES_TYPE_ENTRY));
		assertContainsLinks(typeEntry, Constants.REL_INSTANCE_FEED);
	}

	@Configure_TP_Server_To_Start_With_TDI
	@Configure_TDI_To_Have_Two_TP_Provider_Nodes
	@Configure_TP_Server_To_Have_Custom_Templates_Directory
	@Create_Test_Connector
	@Clean_Up_The_Persistence_Store_Directory
	@Start_TDI
	public static void verify_TP_Inst_Feed_Contains_Required_Categories_And_Has_No_Entries(HttpClientContext tpClientCtx,
			String nodeId, String typeId) throws Exception {
		MockHttpServletResponse response = new TpAppHelper(tpClientCtx).getInstFeed(nodeId, typeId);
		TpAppHelper.checkSuccess(response);
		AtomFeed feed = deserializeFeed(response.getContentAsString());

		assertThat(feed.getEntries().size(), is(0));
		containsInAnyOrder(atomCategoryComparator, false, false, feed.getCategories(), winkCatToInternal(Constants.CAT_TOUCHPOINT));
	}

	@Configure_TP_Server_To_Start_With_TDI
	@Configure_TDI_To_Have_Two_TP_Provider_Nodes
	@Configure_TP_Server_To_Have_Custom_Templates_Directory
	@Create_Test_Connector
	@Clean_Up_The_Persistence_Store_Directory
	@Start_TDI
	public static AtomEntry verify_TP_Inst_Entry_Is_Accessible_From_TP_Inst_Feed_After_It_Is_Created(HttpClientContext tpClientCtx,
			String nodeId, String typeId, TouchpointRole touchpointRole) throws Exception {

		boolean enabled = false;
		AtomEntry expEntry = createInstAtomEntry("0", touchpointRole, enabled, null);
		MockHttpServletResponse response = new TpAppHelper(tpClientCtx).createInstEntry(nodeId, typeId, expEntry);
		TpAppHelper.checkSuccess(response);

		AtomEntry newEntry = deserializeEntry(response.getContentAsString());
		// the TP Server might change the id
		expEntry.setId(newEntry.getId());

		response = new TpAppHelper(tpClientCtx).getInstFeed(nodeId, typeId);
		TpAppHelper.checkSuccess(response);
		AtomFeed feed = deserializeFeed(response.getContentAsString());

		/*
		 * iterate through all entries in the feed and check if the self link of
		 * any one of them points to the instance entry which we created
		 */
		AtomEntry actEntry = null;
		for (AtomEntry referenceEntry : feed.getEntries()) {

			assertEntryFromTouchpointInstanceFeedIsCorrect(referenceEntry);

			AtomEntry e = new TpAppHelper(tpClientCtx).getAtomEntrySelf(referenceEntry);
			if (e.getId().equals(newEntry.getId())) {
				actEntry = e;
				break;
			}
		}

		assertThat(actEntry, is(not(nullValue())));

		return actEntry;
	}

	@Configure_TP_Server_To_Start_With_TDI
	@Configure_TDI_To_Have_Two_TP_Provider_Nodes
	@Configure_TP_Server_To_Have_Custom_Templates_Directory
	@Create_Test_Connector
	@Clean_Up_The_Persistence_Store_Directory
	@Start_TDI
	public static AtomEntry verify_TP_Inst_Entry_Is_Correct(HttpClientContext tpClientCtx, String nodeId, String typeId,
			TouchpointRole touchpointRole) throws Exception {

		boolean enabled = false;
		AtomEntry expEntry = createInstAtomEntry("0", touchpointRole, enabled, null);
		MockHttpServletResponse response = new TpAppHelper(tpClientCtx).createInstEntry(nodeId, typeId, expEntry);
		TpAppHelper.checkSuccess(response);

		String instEntryUrl = (String) response.getHeader("Location");
		assertThat(instEntryUrl, is(notNullValue()));

		response = tpClientCtx.invoke(tpClientCtx
				.constructMockRequest(HttpMethod.GET, instEntryUrl, MediaType.APPLICATION_ATOM_XML));
		TpAppHelper.checkSuccess(response);
		AtomEntry actEntry = deserializeEntry(response.getContentAsString());

		// the TP Server might change the id
		expEntry.setId(actEntry.getId());

		atomEntryComparator.assertEquals(actEntry, expEntry);

		verifyTochpointInstanceEntryIsCorrect(tpClientCtx, actEntry, touchpointRole, false);

		// test destination feed
		if (!TouchpointRole.PROVIDER.equals(touchpointRole)) {

			// add a new destination
			String destinationFeedUrl = TpAppHelper.getDestinationFeedUrl(actEntry);
			assertNotNull(destinationFeedUrl);
			URL requestOut = new URL("http://www.mytest.org");
			AtomEntry expDestEntry = new TpAppHelper(tpClientCtx).addDestination(destinationFeedUrl, requestOut);

			// verify the destination is there
			verifyDestinationFeedIsCorrect(tpClientCtx, actEntry, touchpointRole, expDestEntry);
		}

		return actEntry;
	}

	@Configure_TP_Server_To_Start_With_TDI
	@Configure_TDI_To_Have_Two_TP_Provider_Nodes
	@Configure_TP_Server_To_Have_Custom_Templates_Directory
	@Create_Test_Connector
	@Clean_Up_The_Persistence_Store_Directory
	@Start_TDI
	public static void verify_TP_Inst_Entry_Can_Be_Deleted(HttpClientContext tpClientCtx, String nodeId, String typeId,
			TouchpointRole touchpointRole) throws Exception {

		boolean enabled = false;
		AtomEntry expEntry = createInstAtomEntry("0", touchpointRole, enabled, null);
		MockHttpServletResponse response = new TpAppHelper(tpClientCtx).createInstEntry(nodeId, typeId, expEntry);
		TpAppHelper.checkSuccess(response);

		String instEntryUrl = (String) response.getHeader("Location");
		assertThat(instEntryUrl, is(notNullValue()));

		response = tpClientCtx.invoke(tpClientCtx
				.constructMockRequest(HttpMethod.GET, instEntryUrl, MediaType.APPLICATION_ATOM_XML));
		TpAppHelper.checkSuccess(response);
		AtomEntry newEntry = deserializeEntry(response.getContentAsString());

		String editLink = newEntry.getLinksByRelation(Constants.REL_EDIT).get(0).getHref();
		String selfLink = newEntry.getLinksByRelation(Constants.REL_SELF).get(0).getHref();

		response = tpClientCtx
				.invoke(tpClientCtx.constructMockRequest(HttpMethod.DELETE, editLink, MediaType.APPLICATION_ATOM_XML));
		TpAppHelper.checkSuccess(response);

		response = tpClientCtx.invoke(tpClientCtx.constructMockRequest(HttpMethod.GET, selfLink, MediaType.APPLICATION_ATOM_XML));
		assertThat(response.getStatus(), is(Status.GONE.getStatusCode()));
	}

	@Configure_TP_Server_To_Start_With_TDI
	@Configure_TDI_To_Have_Two_TP_Provider_Nodes
	@Configure_TP_Server_To_Have_Custom_Templates_Directory
	@Create_Test_Connector
	@Clean_Up_The_Persistence_Store_Directory
	@Start_TDI
	public static void verify_The_Newly_Created_Provider_TP_Instance_Is_Started_On_The_TDI_Server(HttpClientContext tpClientCtx,
			String nodeId, String typeId) throws Exception {

		URL serviceDocUrl = new URL(tpClientCtx.getHttpRootURI());
		TouchpointFactory tf = new TouchpointFactory(serviceDocUrl);

		Map<String, String> params = new HashMap<String, String>();
		params.put("itCount", "1");
		params.put("script", CONNECTOR_PARAM_SCRIPT);

		ProviderTouchpoint tp = tf.createProviderTouchpoint(nodeId, typeId, null, params);
		try {
			ProviderTouchpointResponse tpRes = tp.get();

			assertFalse(tpRes.getErrorMessage(), tpRes.isError());
			assertThat("More than one entry returned by provider touchpoint: " + tpRes.getData().getEntries(), tpRes.getData()
					.getEntries().size(), is(1));

		} finally {
			tp.deleteTouchpoint();
		}
	}

	@Configure_TP_Server_To_Start_With_TDI
	@Configure_TDI_To_Have_Two_TP_Provider_Nodes
	@Configure_TP_Server_To_Have_Custom_Templates_Directory
	@Create_Test_Connector
	@Clean_Up_The_Persistence_Store_Directory
	@Start_TDI
	public static void verify_The_Newly_Created_Initiator_TP_Instance_Is_Started_On_The_TDI_Server(HttpClientContext tpClientCtx,
			String nodeId, String typeId) throws Exception {

		URL serviceDocUrl = new URL(tpClientCtx.getHttpRootURI());
		TouchpointFactory tf = new TouchpointFactory(serviceDocUrl);

		Map<String, String> params = new HashMap<String, String>();
		params.put("itCount", "3");
		params.put("script", CONNECTOR_PARAM_SCRIPT);

		InitiatorTouchpoint tp = tf.createInitiatorTouchpoint(nodeId, typeId, params);
		try {
			DestinationService destService = tp.createDestinationService(PortProbe.getAvailablePort());
			try {
				List<TouchpointData> data = destService.consume(3);
				assertEquals(3, data.size());
			} finally {
				destService.close();
			}
		} finally {
			tp.deleteTouchpoint();
		}
	}

	@Configure_TP_Server_To_Start_With_TDI
	@Configure_TDI_To_Have_Two_TP_Provider_Nodes
	@Configure_TP_Server_To_Have_Custom_Templates_Directory
	@Create_Test_Connector
	@Clean_Up_The_Persistence_Store_Directory
	@Start_TDI
	public static void verify_The_Newly_Created_Intermediary_TP_Instance_Is_Started_On_The_TDI_Server(
			HttpClientContext tpClientCtx, String nodeId, String typeId, String providerTypeId) throws Exception {

		URL serviceDocUrl = new URL(tpClientCtx.getHttpRootURI());
		TouchpointFactory tf = new TouchpointFactory(serviceDocUrl);

		Map<String, String> params = new HashMap<String, String>();
		params.put("itCount", "3");
		params.put("script", CONNECTOR_PARAM_SCRIPT);

		ProviderTouchpoint providerTp = tf.createProviderTouchpoint(nodeId, providerTypeId, null, params);
		try {

			IntermediaryTouchpoint tp = tf.createIntermediaryTouchpoint(nodeId, typeId, params);
			try {

				// add the provider as destination of the intermediary
				tp.createExternalDestination(providerTp.getRequestInUrl());

				ProviderTouchpointResponse tpResponse = tp.get();
				assertFalse(tpResponse.getResponseCode() + " : " + tpResponse.getErrorMessage(), tpResponse.isError());
				assertEquals(3, tpResponse.getData().getEntries().size());

			} finally {
				tp.deleteTouchpoint();
			}
		} finally {
			providerTp.deleteTouchpoint();
		}
	}

	@Configure_TP_Server_To_Start_With_TDI
	@Configure_TDI_To_Have_Two_TP_Provider_Nodes
	@Configure_TP_Server_To_Have_Custom_Templates_Directory
	@Create_Test_Connector
	@Clean_Up_The_Persistence_Store_Directory
	@Start_TDI
	public static void verify_Provider_TP_Instance_Survives_Restart_Of_The_TP_Server(HttpClientContext tpClientCtx, String nodeId,
			String typeId, TDIServer tdi) throws Exception {

		URL serviceDocUrl = new URL(tpClientCtx.getHttpRootURI());
		TouchpointFactory tf = new TouchpointFactory(serviceDocUrl);

		Map<String, String> params = new HashMap<String, String>();
		params.put("itCount", "1");
		params.put("script", CONNECTOR_PARAM_SCRIPT);

		ProviderTouchpoint tp = tf.createProviderTouchpoint(nodeId, typeId, null, params);
		TpAppHelper app = new TpAppHelper(tpClientCtx);
		try {

			AtomEntry instanceEntryBeforeRestart = app.getAtomEntry(tp.getInstanceEntryUrl());

			// restart the TDI Server
			tdi.stopServer();
			tdi.startServer();

			// verify the touchpoint instance entry stays the same after the
			// restart
			AtomEntry instanceEntryAfterRestart = app.getAtomEntry(tp.getInstanceEntryUrl());
			atomEntryComparator.assertEquals(instanceEntryBeforeRestart, instanceEntryAfterRestart);

			// verify the touchpoint is running
			ProviderTouchpointResponse tpRes = tp.get();
			assertFalse(tpRes.getErrorMessage(), tpRes.isError());
			assertThat("More than one entry returned by provider touchpoint: " + tpRes.getData().getEntries(), tpRes.getData()
					.getEntries().size(), is(1));

		} finally {
			tp.deleteTouchpoint();
		}
	}

	@Configure_TP_Server_To_Start_With_TDI
	@Configure_TDI_To_Have_Two_TP_Provider_Nodes
	@Configure_TP_Server_To_Have_Custom_Templates_Directory
	@Create_Test_Connector
	@Clean_Up_The_Persistence_Store_Directory
	@Start_TDI
	public static void verify_Initiator_TP_Instance_Survives_Restart_Of_The_TP_Server(HttpClientContext tpClientCtx, String nodeId,
			String typeId, TDIServer tdi) throws Exception {

		URL serviceDocUrl = new URL(tpClientCtx.getHttpRootURI());
		TouchpointFactory tf = new TouchpointFactory(serviceDocUrl);

		Map<String, String> params = new HashMap<String, String>();
		params.put("itCount", "3");
		params.put("script", CONNECTOR_PARAM_SCRIPT);

		InitiatorTouchpoint tp = tf.createInitiatorTouchpoint(nodeId, typeId, params);
		TpAppHelper app = new TpAppHelper(tpClientCtx);
		try {
			DestinationService destService = tp.createDestinationService(PortProbe.getAvailablePort());
			try {
				List<TouchpointData> data = destService.consume(3);
				assertEquals(3, data.size());

				AtomEntry instanceEntryBeforeRestart = app.getAtomEntry(tp.getInstanceEntryUrl());

				// restart the TDI Server
				tdi.stopServer();
				tdi.startServer();

				// verify the touchpoint instance entry stays the same after the
				// restart
				AtomEntry instanceEntryAfterRestart = app.getAtomEntry(tp.getInstanceEntryUrl());
				atomEntryComparator.assertEquals(instanceEntryBeforeRestart, instanceEntryAfterRestart);

				data = destService.consume(3);
				assertEquals(3, data.size());
			} finally {
				destService.close();
			}
		} finally {
			tp.deleteTouchpoint();
		}
	}

	@Configure_TP_Server_To_Start_With_TDI
	@Configure_TDI_To_Have_Two_TP_Provider_Nodes
	@Configure_TP_Server_To_Have_Custom_Templates_Directory
	@Create_Test_Connector
	@Clean_Up_The_Persistence_Store_Directory
	@Start_TDI
	public static void verify_TP_Instance_Destinations_Survives_Restart_Of_The_TP_Server(HttpClientContext tpClientCtx,
			String nodeId, String typeId, TDIServer tdi) throws Exception {

		URL serviceDocUrl = new URL(tpClientCtx.getHttpRootURI());
		TouchpointFactory tf = new TouchpointFactory(serviceDocUrl);

		Map<String, String> params = new HashMap<String, String>();
		params.put("itCount", "3");
		params.put("script", CONNECTOR_PARAM_SCRIPT);

		InitiatorTouchpoint tp = tf.createInitiatorTouchpoint(nodeId, typeId, params);

		TpAppHelper app = new TpAppHelper(tpClientCtx);

		try {
			tp.setEnabled(false);

			String destFeedUrl = TpAppHelper.getResourceUrlFromEntry(app.getAtomEntry(tp.getInstanceEntryUrl()),
					Constants.REL_DESTINATION_FEED);

			int basePort = PortProbe.getAvailablePort();

			URL[] reqOut = { new URL("http://localhost:" + basePort++), new URL("http://localhost:" + basePort++) };
			URL[] reqErr = { new URL("http://localhost:" + basePort++), null };

			AtomEntry expDestEntry1 = app.addDestination(destFeedUrl, reqOut[0], reqErr[0]);
			AtomEntry expDestEntry2 = app.addDestination(destFeedUrl, reqOut[1], reqErr[1]);

			// restart the TDI Server
			tdi.stopServer();
			tdi.startServer();

			verifyDestinationFeedIsCorrect(tpClientCtx, app.getAtomEntry(tp.getInstanceEntryUrl()), TouchpointRole.INITIATOR,
					expDestEntry1, expDestEntry2);
		} finally {
			tp.deleteTouchpoint();
		}
	}

	@Configure_TP_Server_To_Start_With_TDI
	@Configure_TDI_To_Have_Two_TP_Provider_Nodes
	@Configure_TP_Server_To_Have_Custom_Templates_Directory
	@Create_Test_Connector
	@Clean_Up_The_Persistence_Store_Directory
	@Start_TDI
	public static void verify_Intermediary_TP_Instance_Survives_Restart_Of_The_TP_Server(HttpClientContext tpClientCtx,
			String nodeId, String typeId, TDIServer tdi, String providerTypeId) throws Exception {

		URL serviceDocUrl = new URL(tpClientCtx.getHttpRootURI());
		TouchpointFactory tf = new TouchpointFactory(serviceDocUrl);

		Map<String, String> params = new HashMap<String, String>();
		params.put("itCount", "3");
		params.put("script", CONNECTOR_PARAM_SCRIPT);

		ProviderTouchpoint providerTp = tf.createProviderTouchpoint(nodeId, providerTypeId, null, params);
		TpAppHelper app = new TpAppHelper(tpClientCtx);
		try {

			IntermediaryTouchpoint tp = tf.createIntermediaryTouchpoint(nodeId, typeId, params);
			try {

				// add the provider as destination of the intermediary
				tp.createExternalDestination(providerTp.getRequestInUrl());

				AtomEntry instanceEntryBeforeRestart = app.getAtomEntry(tp.getInstanceEntryUrl());

				// restart the TDI Server
				tdi.stopServer();
				tdi.startServer();

				// verify the touchpoint instance entry stays the same after the
				// restart
				AtomEntry instanceEntryAfterRestart = app.getAtomEntry(tp.getInstanceEntryUrl());
				atomEntryComparator.assertEquals(instanceEntryBeforeRestart, instanceEntryAfterRestart);

				ProviderTouchpointResponse tpResponse = tp.get();
				assertFalse(tpResponse.getResponseCode() + " : " + tpResponse.getErrorMessage(), tpResponse.isError());
				assertEquals(3, tpResponse.getData().getEntries().size());

			} finally {
				tp.deleteTouchpoint();
			}
		} finally {
			providerTp.deleteTouchpoint();
		}
	}

	@Configure_TP_Server_To_Start_With_TDI
	@Configure_TDI_To_Have_Two_TP_Provider_Nodes
	@Configure_TP_Server_To_Have_Custom_Templates_Directory
	@Create_Test_Connector
	@Clean_Up_The_Persistence_Store_Directory
	@Start_TDI
	public static void verify_Provider_TP_Instance_State_While_Waiting_For_Request(HttpClientContext tpClientCtx, String nodeId,
			String typeId) throws Exception {

		URL serviceDocUrl = new URL(tpClientCtx.getHttpRootURI());
		TouchpointFactory tf = new TouchpointFactory(serviceDocUrl);

		Map<String, String> params = new HashMap<String, String>();
		params.put("itCount", "1");
		params.put("script", CONNECTOR_PARAM_SCRIPT);

		ProviderTouchpoint tp = tf.createProviderTouchpoint(nodeId, typeId, null, params);
		try {

			assertTrue(tp.getEnabled());
			assertTrue(tp.getAvailable());

			// disable
			tp.setEnabled(false);

			assertFalse(tp.getEnabled());
			assertFalse(tp.getAvailable());

		} finally {
			tp.deleteTouchpoint();
		}
	}

	@Configure_TP_Server_To_Start_With_TDI
	@Configure_TDI_To_Have_Two_TP_Provider_Nodes
	@Configure_TP_Server_To_Have_Custom_Templates_Directory
	@Create_Test_Connector
	@Clean_Up_The_Persistence_Store_Directory
	@Start_TDI
	public static void verify_Intermediary_TP_Instance_State_If_Enabled(HttpClientContext tpClientCtx, String nodeId,
			String typeId, String providerTypeId) throws Exception {

		URL serviceDocUrl = new URL(tpClientCtx.getHttpRootURI());
		TouchpointFactory tf = new TouchpointFactory(serviceDocUrl);

		Map<String, String> params = new HashMap<String, String>();
		params.put("itCount", "3");
		params.put("script", CONNECTOR_PARAM_SCRIPT);

		ProviderTouchpoint providerTp = tf.createProviderTouchpoint(nodeId, providerTypeId, null, params);
		try {

			IntermediaryTouchpoint tp = tf.createIntermediaryTouchpoint(nodeId, typeId, params);
			try {

				// enabled and unavailable, if no destinations
				assertTrue(tp.getEnabled());
				assertFalse(tp.getAvailable());

				// add the provider as destination of the intermediary
				tp.createExternalDestination(providerTp.getRequestInUrl());

				// enabled and available, if has destinations
				assertTrue(tp.getEnabled());
				assertTrue(tp.getAvailable());

				// disable
				tp.setEnabled(false);

				// disabled and unavailable, if disabled
				assertFalse(tp.getEnabled());
				assertFalse(tp.getAvailable());

			} finally {
				tp.deleteTouchpoint();
			}
		} finally {
			providerTp.deleteTouchpoint();
		}
	}

	@Configure_TP_Server_To_Start_With_TDI
	@Configure_TDI_To_Have_Two_TP_Provider_Nodes
	@Configure_TP_Server_To_Have_Custom_Templates_Directory
	@Create_Test_Connector
	@Clean_Up_The_Persistence_Store_Directory
	@Start_TDI
	public static void verify_Initiator_TP_Instance_State_While_Sending_Data(HttpClientContext tpClientCtx, String nodeId,
			String typeId) throws Exception {

		URL serviceDocUrl = new URL(tpClientCtx.getHttpRootURI());
		TouchpointFactory tf = new TouchpointFactory(serviceDocUrl);

		Map<String, String> params = new HashMap<String, String>();
		params.put("itCount", "-1");
		params.put("script", CONNECTOR_PARAM_SCRIPT);

		InitiatorTouchpoint tp = tf.createInitiatorTouchpoint(nodeId, typeId, params);
		try {

			// enabled and unavailable when no destinations
			assertTrue(tp.getEnabled());
			assertFalse(tp.getAvailable());

			DestinationService destService = tp.createDestinationService(PortProbe.getAvailablePort());
			try {
				// make sure the touchpoint has started sending data
				destService.consume(1);

				assertTrue(tp.getEnabled());
				assertTrue(tp.getAvailable());

				// disable
				tp.setEnabled(false);

				assertFalse(tp.getEnabled());
				assertFalse(tp.getAvailable());

			} finally {
				destService.close();
			}
		} finally {
			tp.deleteTouchpoint();
		}
	}

	@Configure_TP_Server_To_Start_With_TDI
	@Configure_TDI_To_Have_Two_TP_Provider_Nodes
	@Configure_TP_Server_To_Have_Custom_Templates_Directory
	@Create_Test_Connector
	@Clean_Up_The_Persistence_Store_Directory
	@Start_TDI
	public static void verify_Initiator_TP_Instance_State_After_Exhausting_Data_Source(HttpClientContext tpClientCtx,
			String nodeId, String typeId) throws Exception {

		final int entryCount = 3;

		URL serviceDocUrl = new URL(tpClientCtx.getHttpRootURI());
		TouchpointFactory tf = new TouchpointFactory(serviceDocUrl);

		Map<String, String> params = new HashMap<String, String>();
		params.put("itCount", "" + entryCount);
		params.put("script", CONNECTOR_PARAM_SCRIPT);

		InitiatorTouchpoint tp = tf.createInitiatorTouchpoint(nodeId, typeId, params);
		try {

			// enabled and unavailable when no destinations
			assertTrue(tp.getEnabled());
			assertFalse(tp.getAvailable());

			DestinationService destService = tp.createDestinationService(PortProbe.getAvailablePort());
			try {
				// consume everything
				destService.consume(entryCount);

				assertTrue(tp.getEnabled());
				assertFalse(tp.getAvailable());

			} finally {
				destService.close();
			}
		} finally {
			tp.deleteTouchpoint();
		}
	}

	@Configure_TP_Server_To_Start_With_TDI
	@Configure_TDI_To_Have_Two_TP_Provider_Nodes
	@Configure_TP_Server_To_Have_Custom_Templates_Directory
	@Create_Test_Connector
	@Clean_Up_The_Persistence_Store_Directory
	@Start_TDI
	public static void verify_TP_Instance_State_When_Disabled(HttpClientContext tpClientCtx, String nodeId, String typeId,
			TouchpointRole touchpointRole) throws Exception {

		URL serviceDocUrl = new URL(tpClientCtx.getHttpRootURI());
		TouchpointFactory tf = new TouchpointFactory(serviceDocUrl);

		Map<String, String> params = new HashMap<String, String>();
		params.put("itCount", "-1");
		params.put("script", CONNECTOR_PARAM_SCRIPT);

		// create disabled touchpoint
		final boolean enabled = false;
		Touchpoint tp = tf.createTouchpoint(nodeId, typeId, params, touchpointRole, enabled);

		try {

			assertFalse(tp.getEnabled());
			assertFalse(tp.getAvailable());

			tp.setEnabled(true);

			assertTrue(tp.getEnabled());
			if (TouchpointRole.PROVIDER == touchpointRole) {
				assertTrue(tp.getAvailable());
			} else {
				// we have configured no destinations
				assertFalse(tp.getAvailable());
			}

			tp.setEnabled(false);

			assertFalse(tp.getEnabled());
			assertFalse(tp.getAvailable());

		} finally {
			tp.deleteTouchpoint();
		}
	}

	@Configure_TP_Server_To_Start_With_TDI
	@Configure_TDI_To_Have_Two_TP_Provider_Nodes
	@Configure_TP_Server_To_Have_Custom_Templates_Directory
	@Create_Test_Connector
	@Clean_Up_The_Persistence_Store_Directory
	@Start_TDI
	public static void verify_Property_Sheet_Definition_Exists(HttpClientContext tpClientCtx, String nodeId, String typeId)
			throws Exception {
		TpAppHelper app = new TpAppHelper(tpClientCtx);
		String typeEntryUrl = app.getTypeEntryURL(nodeId, typeId);
		AtomEntry typeEntry = app.getAtomEntry(typeEntryUrl);

		List<AtomLink> sheetDefinitionLinkList = typeEntry.getLinksByRelation(Constants.REL_PROPSHEET_DEF);
		assertEquals(1, sheetDefinitionLinkList.size());

		/*
		 * A Touchpoint Resource Type Entry SHOULD contain an "atom:link"
		 * element containing a reference to an XML document containing a
		 * propertySheetDefinition element, defined in
		 * <schemas/propertySheet.xsd> This defines the properties that can be
		 * set when creating instances of this type of touchpoint. If it does,
		 * the relationship attribute MUST contain the value
		 * "http://www.ibm.com/xmlns/prod/scmp#property-sheet-definition", and
		 * the type attribute MUST contain the value "text/xml".
		 */
		AtomLink sheetDefinitionLink = sheetDefinitionLinkList.get(0);
		assertNotNull(sheetDefinitionLink.getType());
		assertTrue(sheetDefinitionLink.getType().contains("text/xml"));

		String sheetDefinitionUrl = sheetDefinitionLink.getHref();

		MockHttpServletRequest request = tpClientCtx.constructMockRequest(HttpMethod.GET, sheetDefinitionUrl, MediaType.WILDCARD);
		MockHttpServletResponse response = tpClientCtx.invoke(request);
		TpAppHelper.checkSuccess(response);

		String propertySheetDefinitionString = response.getContentAsString();
		Element propertySheetDefinition = DOMUtils.parseString(propertySheetDefinitionString);

		assertEquals(Constants.NS_SCMP, propertySheetDefinition.getNamespaceURI());
		assertEquals("propertySheetDefinition", propertySheetDefinition.getLocalName());

		// verify the "debug" Connector parameter is described
		List<Element> list = DOMUtils.getAllElementsWithName(propertySheetDefinition, "propertyDefinition", Constants.NS_SCMP);
		Element propertyDefinition = DOMUtils.getElementWithAttribute(list, "propertyName", null, "debug");
		assertNotNull(propertyDefinition);
		assertEquals("false", propertyDefinition.getAttribute("required"));
		assertEquals("boolean", propertyDefinition.getAttribute("propertyType"));
	}

	@Configure_TP_Server_To_Start_With_TDI
	@Configure_TDI_To_Have_Two_TP_Provider_Nodes
	@Configure_TP_Server_To_Have_Custom_Templates_Directory
	@Create_Test_Connector
	@Clean_Up_The_Persistence_Store_Directory
	@Start_TDI
	public static void verify_TP_Instance_Entry_Can_Be_Updated(HttpClientContext tpClientCtx, String nodeId, String typeId)
			throws Exception {

		TpAppHelper app = new TpAppHelper(tpClientCtx);
		// create provider touchpoint
		boolean enabled = true;
		Map<String, String> params = new HashMap<String, String>();
		params.put("itCount", "1");
		params.put("script", CONNECTOR_PARAM_SCRIPT);
		AtomEntry instanceEntry = app.createTPInstance(nodeId, typeId, TouchpointRole.PROVIDER, params, enabled);

		try {

			AtomEntry instanceEntry2 = app.getAtomEntrySelf(instanceEntry);
			atomEntryComparator.assertEquals(instanceEntry2, instanceEntry);

			// update with the same entry
			app.putAtomEntry(instanceEntry);
			AtomEntry instanceEntry3 = app.getAtomEntrySelf(instanceEntry);
			atomEntryComparator.assertEquals(instanceEntry3, instanceEntry);

			/*
			 * update with a modified entry: add new property in the property
			 * sheet and update the instance
			 */
			InstanceData data = app.getInstanceData(instanceEntry);
			final String propertyName = "mytestproperty";
			Property newProperty = new Property();
			newProperty.setPropertyName(propertyName);
			PropertySheet propertySheet = data.getTouchpoint().getPropertySheet();
			List<Property> propertyList = propertySheet.getProperty();
			propertyList.add(newProperty);

			// verify the instance is updated
			app.setInstanceData(instanceEntry, data);
			app.putAtomEntry(instanceEntry);
			AtomEntry instanceEntry4 = app.getAtomEntrySelf(instanceEntry);
			atomEntryComparator.assertEquals(instanceEntry4, instanceEntry);

		} finally {
			app.deleteInstEntry(instanceEntry);
		}
	}

	@Configure_TP_Server_To_Start_With_TDI
	@Configure_TDI_To_Have_Two_TP_Provider_Nodes
	@Configure_TP_Server_To_Have_Custom_Templates_Directory
	@Create_Test_Connector
	@Clean_Up_The_Persistence_Store_Directory
	@Start_TDI
	public static void verify_TP_Destination_Entry_Can_Be_Updated(HttpClientContext tpClientCtx, String nodeId, String typeId)
			throws Exception {

		URL serviceDocUrl = new URL(tpClientCtx.getHttpRootURI());
		TouchpointFactory tf = new TouchpointFactory(serviceDocUrl);

		Map<String, String> params = new HashMap<String, String>();
		params.put("itCount", "-1");
		params.put("script", CONNECTOR_PARAM_SCRIPT);

		InitiatorTouchpoint tp = tf.createInitiatorTouchpoint(nodeId, typeId, params);
		try {
			DestinationService oldService = tp.createDestinationService(PortProbe.getAvailablePort());
			try {
				// ensure the touchpoint is running
				oldService.consume(1);

				DestinationService newService = tp.replaceDestinationService(oldService, PortProbe.getAvailablePort());
				try {

					// verify the new service receives data
					final int expectedDataCount = 10;
					List<TouchpointData> list = newService.consume(expectedDataCount);
					assertEquals(expectedDataCount, list.size());

					// drain the old service
					oldService.consume(-1, 100);

					// verify the old service is no longer receiving anything
					list = oldService.consume(-1, 100);
					assertEquals(0, list.size());

				} finally {
					newService.close();
				}
			} finally {
				oldService.close();
			}
		} finally {
			tp.deleteTouchpoint();
		}
	}

	@Configure_TP_Server_To_Start_With_TDI
	@Configure_TDI_To_Have_Two_TP_Provider_Nodes
	@Configure_TP_Server_To_Have_Custom_Templates_Directory
	@Create_Test_Connector
	@Clean_Up_The_Persistence_Store_Directory
	@Start_TDI
	public static void verify_TP_Destination_Entry_Can_Be_Deleted(HttpClientContext tpClientCtx, String nodeId, String typeId)
			throws Exception {

		URL serviceDocUrl = new URL(tpClientCtx.getHttpRootURI());
		TouchpointFactory tf = new TouchpointFactory(serviceDocUrl);

		Map<String, String> params = new HashMap<String, String>();
		params.put("itCount", "-1");
		params.put("script", CONNECTOR_PARAM_SCRIPT);

		InitiatorTouchpoint tp = tf.createInitiatorTouchpoint(nodeId, typeId, params);
		try {
			DestinationService destService = tp.createDestinationService(PortProbe.getAvailablePort());
			try {
				// ensure the touchpoint is running
				destService.consume(1);

				// delete the destination
				tp.deleteDestinationService(destService);

				// drain the service
				destService.consume(-1, 100);

				// verify the service is no longer receiving anything
				List<TouchpointData> list = destService.consume(-1, 100);
				assertEquals(0, list.size());

			} finally {
				destService.close();
			}
		} finally {
			tp.deleteTouchpoint();
		}
	}

	@Configure_TP_Server_To_Start_With_TDI
	@Configure_TDI_To_Have_Two_TP_Provider_Nodes
	@Configure_TP_Server_To_Have_Custom_Templates_Directory
	@Create_Test_Connector
	@Clean_Up_The_Persistence_Store_Directory
	@Start_TDI
	public static void verify_Error_Document_Is_Returned_For_Missing_TP_Role(HttpClientContext tpClientCtx, String nodeId,
			String typeId) throws Exception {

		boolean enabled = false;
		AtomEntry instanceEntry = createInstAtomEntry("0", TouchpointRole.PROVIDER, enabled, null);
		// remove the category which defines the touchpoint role
		instanceEntry.getCategories().clear();

		String instFeedURL = new TpAppHelper(tpClientCtx).getInstFeedURL(nodeId, typeId);
		MockHttpServletRequest request = tpClientCtx.constructMockRequest(HttpMethod.POST, instFeedURL,
				MediaType.APPLICATION_ATOM_XML, MediaType.APPLICATION_ATOM_XML, serializeEntry(instanceEntry).getBytes("UTF-8"));
		MockHttpServletResponse response = tpClientCtx.invoke(request);

		// verify we received an error http response code
		assertTrue(400 <= response.getStatus() && response.getStatus() < 600);

		// verify there is "error" element
		String content = response.getContentAsString();
		Element error = DOMUtils.parseString(content);
		assertEquals(Constants.NS_SCMP, error.getNamespaceURI());
		assertEquals("error", error.getLocalName());

		// verify there is "code" element
		List<Element> list = DOMUtils.getAllElementsWithName(error, "code", Constants.NS_SCMP);
		assertEquals(1, list.size());
		assertNotNull(list.get(0).getTextContent());
	}

	private static void assertContainsLinks(AtomEntry entry, String... linkRelations) {
		for (String rel : linkRelations) {
			assertThat("Found links with relation \"" + rel + "\"",
					findLinksByRel(entry.getLinks(), rel).size(), is(greaterThan(0)));
		}
	}

	private static void assertContainsCategory(AtomEntry entry, String categoryScheme, String categoryTerm) throws JAXBException {
		boolean found = false;
		for (AtomCategory cat : entry.getCategories()) {
			boolean schemeMatched = categoryScheme == null || categoryScheme.equals(cat.getScheme());
			boolean termMatched = categoryTerm == null || categoryTerm.equals(cat.getTerm());
			if (schemeMatched && termMatched) {
				found = true;
			}
		}
		assertTrue("Did not find category with scheme " + categoryScheme + " and term " + categoryTerm + " in entry: "
				+ AtomUtils.serializeEntry(entry), found);
	}

	private static void assertEntryFromTouchpointInstanceFeedIsCorrect(AtomEntry entry) throws JAXBException {
		/*
		 * All "atom:entry" elements within an Instance Feed MUST be linking to
		 * Resource Instance documents of the same type and not contain a
		 * "scmp:data" element.
		 */
		assertThat(entry.getAny().size(), is(0));

		/*
		 * Each "atom:entry" element for an instance entry that accepts PUT or
		 * DELETE, MUST contain an "atom:link" element with relationship
		 * attribute value "edit". It MAY contain an "atom:link" element with
		 * relationship attribute value "self", in which case the URL MUST be
		 * the same as on the "edit" link.
		 */
		List<AtomLink> selfLinks = entry.getLinksByRelation("self");
		List<AtomLink> editLinks = entry.getLinksByRelation("edit");
		assertThat("Entry in touchpoint instance feed must contain exactly one self link: " + AtomUtils.serializeEntry(entry),
				selfLinks.size(), is(1));
		assertThat("Entry in touchpoint instance feed must contain exactly one edit link: " + AtomUtils.serializeEntry(entry),
				editLinks.size(), is(1));
		assertEquals(selfLinks.get(0).getHref(), editLinks.get(0).getHref());
	}

	private static void verifyDestinationFeedIsCorrect(HttpClientContext testCtx, AtomEntry instanceEntry,
			TouchpointRole touchpointRole, AtomEntry... expDestEntry) throws Exception {

		TpAppHelper app = new TpAppHelper(testCtx);
		String destinationFeedUrl = TpAppHelper.getDestinationFeedUrl(instanceEntry);

		AtomFeed destinationFeed = app.getAtomFeed(destinationFeedUrl);
		assertNotNull(destinationFeed);

		assertEquals(expDestEntry.length, destinationFeed.getEntries().size());

		// verify the destination entries are correct
		for (int i = 0; i < expDestEntry.length; ++i) {
			AtomEntry actDestEntry = app.getAtomEntry(TpAppHelper.getResourceUrlByEntryIdFromFeed(destinationFeed, expDestEntry[i]
					.getId(), Constants.REL_SELF));
			assertNotNull(actDestEntry);

			DestinationData expData = ObjectFactory.createDestinationData(SCMPUtils.getDataElement(expDestEntry[i].getAny()));
			verifyDestinationEntryIsCorrect(testCtx, actDestEntry, expData.getDestination().getRequestOut(), expData
					.getDestination().getRequestError());
		}
	}

	private static void verifyDestinationEntryIsCorrect(HttpClientContext testCtx, AtomEntry destinationEntry,
			String expectedRequestOut, String expectedRequestError) throws Exception {

		assertContainsCategory(destinationEntry, Constants.SCHEME_RESOURCE, "tp-destination");

		Element dataElem = SCMPUtils.getDataElement(destinationEntry.getAny());
		DestinationData dd = com.ibm.di.tp.server.model.config.ObjectFactory.createDestinationData(dataElem);

		assertNotNull(dd.getDestination());
		assertEquals(expectedRequestOut, dd.getDestination().getRequestOut());
		assertEquals(expectedRequestError, dd.getDestination().getRequestError());

		verifyDataElementHasXsiSchemaLocation(testCtx, destinationEntry);
	}

	private static void verifyStatusEntryIsCorrect(HttpClientContext testCtx, AtomEntry instanceEntry,
			TouchpointRole touchpointRole, boolean touchpointIsAvailable) throws Exception {

		TpAppHelper app = new TpAppHelper(testCtx);

		List<AtomLink> statusEntryLinkList = instanceEntry.getLinksByRelation(Constants.REL_STATUS);
		assertEquals("More than one status entry link found in the touchpoint instance entry: "
				+ AtomUtils.serializeEntry(instanceEntry), 1, statusEntryLinkList.size());

		String statusEntryUrl = statusEntryLinkList.get(0).getHref();

		AtomEntry statusEntry = app.getAtomEntry(statusEntryUrl);

		assertContainsCategory(statusEntry, Constants.SCHEME_RESOURCE, "touchpoint");
		assertContainsCategory(statusEntry, Constants.SCHEMA_ASPECT, "status");

		verifyDataElementHasXsiSchemaLocation(testCtx, statusEntry);

		TouchpointStatus touchpointStatus = app.getTouchpointStatus(statusEntry);

		EnumOpState expectedOpState = touchpointIsAvailable ? EnumOpState.AVAILABLE : EnumOpState.UNAVAILABLE;
		assertEquals(expectedOpState, touchpointStatus.getOpState());

		String requestIn = touchpointStatus.getRequestIn();
		if (TouchpointRole.INITIATOR.equals(touchpointRole)) {
			assertNull(requestIn);
		} else {
			assertNotNull(requestIn);
			URL requestInUrl = new URL(requestIn);
			requestInUrl.toString();
		}
	}

	private static void verifyDataElementHasXsiSchemaLocation(HttpClientContext testCtx, AtomEntry entry) throws Exception {

		Element data = SCMPUtils.getDataElement(entry.getAny());
		assertNotNull(data);

		String schemaLocation = data.getAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation");
		assertNotNull(schemaLocation);

		String[] tokens = schemaLocation.split("\\s");

		/*
		 * tokens in schemaLocation come in pairs - namespace/schema, so the
		 * total number must be even
		 */
		assertTrue(tokens.length % 2 == 0);
		// must have at least one namespace defined in schemaLocation
		assertTrue(tokens.length >= 2);

		for (int i = 0; i < tokens.length; i += 2) {

			// pairs : namespace URI & schema URI
			int namespaceIndex = i;
			int schemaIndex = i + 1;

			assertTrue(schemaIndex < tokens.length);

			String namespaceURI = tokens[namespaceIndex];
			String schemaURI = tokens[schemaIndex];

			assertNotNull(namespaceURI);
			assertNotNull(schemaURI);

			// both must be accessible through HTTP/HTTPS
			assertTrue(new URI(namespaceURI).getScheme().startsWith("http"));
			assertTrue(new URI(schemaURI).getScheme().startsWith("http"));

			verifyXSDIsCorrect(testCtx, schemaURI);
		}
	}

	private static void verifyXSDIsCorrect(HttpClientContext testCtx, String schemaUrl) throws Exception {

		MockHttpServletRequest request = testCtx.constructMockRequest(HttpMethod.GET, schemaUrl, MediaType.WILDCARD);
		MockHttpServletResponse response = testCtx.invoke(request);
		TpAppHelper.checkSuccess(response);

		Element schema = DOMUtils.parseString(response.getContentAsString());

		final String xsdNS = "http://www.w3.org/2001/XMLSchema";
		assertEquals(xsdNS, schema.getNamespaceURI());
		assertEquals("schema", schema.getLocalName());

		// iterate through all references to other schema documents
		NodeList childNodeList = schema.getChildNodes();
		for (int i = 0; i < childNodeList.getLength(); ++i) {
			Node node = childNodeList.item(i);
			if (Node.ELEMENT_NODE == node.getNodeType()) {
				if ("include".equals(node.getLocalName()) || "import".equals(node.getLocalName())
						|| "redefine".equals(node.getLocalName())) {

					Element elem = (Element) node;
					Attr schemaLocationAttr = elem.getAttributeNodeNS(xsdNS, "schemaLocation");
					if (schemaLocationAttr == null) {
						schemaLocationAttr = elem.getAttributeNode("schemaLocation");
					}
					String referredSchemaUrl = schemaLocationAttr.getValue();

					// recursively follow references to other schemas
					verifyXSDIsCorrect(testCtx, referredSchemaUrl);
				}
			}
		}
	}

	private static void verifyTochpointInstanceEntryIsCorrect(HttpClientContext testCtx, AtomEntry instanceEntry,
			TouchpointRole touchpointRole, boolean touchpointIsAvailable) throws Exception {

		TpAppHelper app = new TpAppHelper(testCtx);

		// verify links
		assertContainsLinks(instanceEntry, Constants.REL_SELF, Constants.REL_EDIT, Constants.REL_RESOURCE_TYPE,
				Constants.REL_STATUS);

		// verify the self link can be resolved
		String selfUrl = TpAppHelper.getSelfUrl(instanceEntry);
		AtomEntry selfEntry = app.getAtomEntry(selfUrl);
		assertNotNull(selfEntry);

		// verify the edit link can be resolved
		String editUrl = TpAppHelper.getEditUrl(instanceEntry);
		AtomEntry editEntry = app.getAtomEntry(editUrl);
		assertNotNull(editEntry);

		// verify the self and the edit links are the same
		assertEquals(selfUrl, editUrl);

		// verify categories
		assertContainsCategory(instanceEntry, Constants.SCHEME_RESOURCE, "touchpoint");
		assertContainsCategory(instanceEntry, Constants.SCHEME_TP_ROLE, app.getTouchpointRoleCategoryTerm(touchpointRole));
		assertContainsCategory(instanceEntry, Constants.SCHEME_TP_TYPE, null);

		// verify schema location
		verifyDataElementHasXsiSchemaLocation(testCtx, instanceEntry);

		// verify status entry
		verifyStatusEntryIsCorrect(testCtx, instanceEntry, touchpointRole, touchpointIsAvailable);
	}

}
