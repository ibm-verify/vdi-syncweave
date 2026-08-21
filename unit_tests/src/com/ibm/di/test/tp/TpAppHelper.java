package com.ibm.di.test.tp;

import static com.ibm.di.test.utils.atom.AtomUtils.createInstAtomEntry;
import static com.ibm.di.test.utils.atom.AtomUtils.deserializeEntry;
import static com.ibm.di.test.utils.atom.AtomUtils.deserializeFeed;
import static com.ibm.di.test.utils.atom.AtomUtils.serializeEntry;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;

import com.ibm.di.web.common.atom.AtomEntry;
import com.ibm.di.web.common.atom.AtomFeed;
import com.ibm.di.web.common.atom.AtomLink;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.w3c.dom.Element;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.test.http.HttpClientContext;
import com.ibm.di.test.utils.atom.AtomAppHelper;
import com.ibm.di.tp.server.Constants;
import com.ibm.di.tp.server.config.node.TdiNodeConfig;
import com.ibm.di.tp.server.config.security.EncryptedString;
import com.ibm.di.tp.server.model.TouchpointRole;
import com.ibm.di.tp.server.model.config.Destination;
import com.ibm.di.tp.server.model.config.DestinationData;
import com.ibm.di.tp.server.model.config.EnumAdminState;
import com.ibm.di.tp.server.model.config.EnumOpState;
import com.ibm.di.tp.server.model.config.InstanceData;
import com.ibm.di.tp.server.model.config.ObjectFactory;
import com.ibm.di.tp.server.model.config.Property;
import com.ibm.di.tp.server.model.config.PropertySheet;
import com.ibm.di.tp.server.model.config.StatusData;
import com.ibm.di.tp.server.model.config.TouchpointStatus;
import com.ibm.di.tp.server.model.exception.SCMPException;
import com.ibm.di.tp.server.model.impl.tdi.TemplateConfigLoader;
import com.ibm.di.tp.server.model.impl.tdi.TouchpointTypeScheme;
import com.ibm.di.tp.server.util.AtomUtils;
import com.ibm.di.tp.server.util.SCMPUtils;

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
public class TpAppHelper extends AtomAppHelper {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * @param ctx
	 */
	public TpAppHelper(HttpClientContext ctx) {
		super(ctx);
	}

	public String getNodeFeedURL() throws Exception {
		return getCollectionURLByCategory(com.ibm.di.test.utils.atom.AtomUtils.winkCatToInternal(Constants.CAT_CONN_PROVIDER));
	}

	public MockHttpServletResponse getNodeFeed() throws Exception {
		MockHttpServletRequest request = testCtx.constructMockRequest(HttpMethod.GET, getNodeFeedURL(), MediaType.WILDCARD);
		return testCtx.invoke(request);
	}

	public String getNodeEntryURL(String nodeId) throws Exception {
		MockHttpServletResponse response = getNodeFeed();

		checkSuccess(response);
		if (response.getContentAsString() == null || response.getContentAsString().trim().length() == 0) {
			throw new RuntimeException("Missing node feed document.");
		}

		AtomFeed feed = deserializeFeed(response.getContentAsString());
		return getResourceUrlByEntryIdFromFeed(feed, nodeId, Constants.REL_SELF);
	}

	public MockHttpServletResponse getNodeEntry(String nodeId) throws Exception {
		MockHttpServletRequest request = testCtx.constructMockRequest(HttpMethod.GET, getNodeEntryURL(nodeId), MediaType.WILDCARD);
		return testCtx.invoke(request);
	}

	public String getTypeFeedURL(String nodeId) throws Exception {
		MockHttpServletResponse response = getNodeEntry(nodeId);

		checkSuccess(response);
		if (response.getContentAsString() == null || response.getContentAsString().trim().length() == 0) {
			throw new RuntimeException("Missing node entry document.");
		}

		AtomEntry entry = deserializeEntry(response.getContentAsString());
		return getResourceUrlFromEntry(entry, Constants.REL_TOUCHPOINT);
	}

	public MockHttpServletResponse getTypeFeed(String nodeId) throws Exception {
		MockHttpServletRequest request = testCtx.constructMockRequest(HttpMethod.GET, getTypeFeedURL(nodeId), MediaType.WILDCARD);
		return testCtx.invoke(request);
	}

	public String getTypeEntryURL(String nodeId, String typeId) throws Exception {
		MockHttpServletResponse response = getTypeFeed(nodeId);

		checkSuccess(response);
		if (response.getContentAsString() == null || response.getContentAsString().trim().length() == 0) {
			throw new RuntimeException("Missing type feed document.");
		}

		AtomFeed feed = deserializeFeed(response.getContentAsString());
		return getResourceUrlByCategoryFromFeed(feed, typeId, Constants.REL_SELF);
	}

	public MockHttpServletResponse getTypeEntry(String nodeId, String typeId) throws Exception {
		MockHttpServletRequest request = testCtx.constructMockRequest(HttpMethod.GET, getTypeEntryURL(nodeId, typeId),
				MediaType.WILDCARD);
		return testCtx.invoke(request);
	}

	public String getInstFeedURL(String nodeId, String typeId) throws Exception {
		MockHttpServletResponse response = getTypeEntry(nodeId, typeId);

		checkSuccess(response);
		if (response.getContentAsString() == null || response.getContentAsString().trim().length() == 0) {
			throw new RuntimeException("Missing type entry document.");
		}

		AtomEntry entry = deserializeEntry(response.getContentAsString());
		return getResourceUrlFromEntry(entry, Constants.REL_INSTANCE_FEED);
	}

	public MockHttpServletResponse getInstFeed(String nodeId, String typeId) throws Exception {
		MockHttpServletRequest request = testCtx.constructMockRequest(HttpMethod.GET, getInstFeedURL(nodeId, typeId),
				MediaType.WILDCARD);
		return testCtx.invoke(request);
	}

	public MockHttpServletResponse createInstEntry(String nodeId, String typeId, AtomEntry instEntry) throws Exception {
		String instFeedURL = getInstFeedURL(nodeId, typeId);
		MockHttpServletRequest request = testCtx.constructMockRequest(HttpMethod.POST, instFeedURL, MediaType.APPLICATION_ATOM_XML,
				MediaType.APPLICATION_ATOM_XML, serializeEntry(instEntry).getBytes("UTF-8"));
		MockHttpServletResponse response = testCtx.invoke(request);
		checkSuccess(response);
		return response;
	}

	public String getInstEntryURL(String nodeId, String typeId, String instId) throws Exception {
		MockHttpServletResponse response = getInstFeed(nodeId, typeId);

		checkSuccess(response);
		if (response.getContentAsString() == null || response.getContentAsString().trim().length() == 0) {
			throw new RuntimeException("Missing instance feed document.");
		}

		AtomFeed feed = deserializeFeed(response.getContentAsString());
		return getResourceUrlByEntryIdFromFeed(feed, instId, Constants.REL_SELF);
	}

	public MockHttpServletResponse getInstEntry(String nodeId, String typeId, String instId) throws Exception {
		MockHttpServletRequest request = testCtx.constructMockRequest(HttpMethod.GET, getInstEntryURL(nodeId, typeId, instId),
				MediaType.WILDCARD);
		return testCtx.invoke(request);
	}

	public final void deleteInstEntry(AtomEntry instEntry) throws Exception {
		String entryUrl = com.ibm.di.test.utils.atom.AtomUtils.findLinksByRel(instEntry.getLinks(), Constants.REL_EDIT).get(0).getHref();
		MockHttpServletRequest request = testCtx.constructMockRequest(HttpMethod.DELETE, entryUrl, MediaType.APPLICATION_ATOM_XML);
		checkSuccess(testCtx.invoke(request));
	}

	public String getStatusEntryURL(String nodeId, String typeId, String instId) throws Exception {
		MockHttpServletResponse response = getInstEntry(nodeId, typeId, instId);

		checkSuccess(response);
		if (response.getContentAsString() == null || response.getContentAsString().trim().length() == 0) {
			throw new RuntimeException("Missing instance feed document.");
		}

		AtomEntry entry = deserializeEntry(response.getContentAsString());
		return getResourceUrlFromEntry(entry, Constants.REL_STATUS);
	}

	public MockHttpServletResponse getStatusEntry(String nodeId, String typeId, String instId) throws Exception {
		MockHttpServletRequest request = testCtx.constructMockRequest(HttpMethod.GET, getStatusEntryURL(nodeId, typeId, instId),
				MediaType.WILDCARD);
		return testCtx.invoke(request);
	}

	public String getDestinationFeedURL(String nodeId, String typeId, String instId) throws Exception {
		MockHttpServletResponse response = getInstEntry(nodeId, typeId, instId);

		checkSuccess(response);
		if (response.getContentAsString() == null || response.getContentAsString().trim().length() == 0) {
			throw new RuntimeException("Missing instance feed document.");
		}

		AtomEntry entry = deserializeEntry(response.getContentAsString());
		return getResourceUrlFromEntry(entry, Constants.REL_DESTINATION_FEED);
	}

	public MockHttpServletResponse getDestinationFeed(String nodeId, String typeId, String instId) throws Exception {
		MockHttpServletRequest request = testCtx.constructMockRequest(HttpMethod.GET,
				getDestinationFeedURL(nodeId, typeId, instId), MediaType.WILDCARD);
		return testCtx.invoke(request);
	}

	public MockHttpServletResponse createDestinationEntry(String nodeId, String typeId, String instId, AtomEntry destEntry)
			throws Exception {
		String clientFeedURL = getDestinationFeedURL(nodeId, typeId, instId);
		MockHttpServletRequest request = testCtx.constructMockRequest(HttpMethod.POST, clientFeedURL,
				MediaType.APPLICATION_ATOM_XML, MediaType.APPLICATION_ATOM_XML, serializeEntry(destEntry).getBytes("UTF-8"));
		MockHttpServletResponse response = testCtx.invoke(request);
		checkSuccess(response);
		return response;
	}

	public AtomEntry createTPInstance(String nodeId, String typeId, TouchpointRole tr, Map<String, String> properties,
			boolean enabled) throws Exception {
		// create the TP Instance entry
		AtomEntry expEntry = createInstAtomEntry("0", tr, enabled, properties);
		MockHttpServletResponse response = createInstEntry(nodeId, typeId, expEntry);
		checkSuccess(response);

		String instEntryUrl = (String) response.getHeader("Location");
		assertNotNull(instEntryUrl);
		AtomEntry instEntry = deserializeEntry(response.getContentAsString());

		return instEntry;
	}

	public void assertInstEntryConfigAppliedToTemplate(MetamergeConfig mc, AtomEntry entry, String tpType, TouchpointRole tr)
			throws Exception {

		Element elem = SCMPUtils.getDataElement(entry.getAny());
		assertThat(elem, is(notNullValue()));
		// make sure text and cdata vals are all the same.

		InstanceData data = ObjectFactory.createInstanceData(elem);
		PropertySheet propertySheet = data.getTouchpoint().getPropertySheet();

		ConnectorConfig conn = (ConnectorConfig) mc.lookup(MetamergeConfig.DEFAULT_CONNECTOR_FOLDER + "/"
				+ TemplateConfigLoader.CONN_SERVICE);
		assertThat(conn, is(notNullValue()));

		BaseConfiguration rawCfg = conn instanceof FunctionConfig ? ((FunctionConfig) conn).getFunctionConfig() : conn
				.getConnectionConfig();
		assertThat(rawCfg, is(notNullValue()));

		Object val = null;
		for (Property prop : propertySheet.getProperty()) {
			if (!Constants.PROP_INIT_MODE.equals(prop.getPropertyName())) {
				val = rawCfg.getParameter(prop.getPropertyName());
				assertThat(val, is(notNullValue()));
				assertThat(val.toString(), is(equalTo(prop.getValue().get(0))));
			} else {
				// if the initMode is set, the connector should have it set.
				assertThat(conn.getMode(), is(equalTo(prop.getValue().get(0))));
			}
		}

		if (TouchpointTypeScheme.fromString(tpType) == TouchpointTypeScheme.SYSTEM) {
			assertThat(conn.getInheritsFromRef(), is(tpType));
		}
	}

	public String getShortId(String uri) {
		int slash = uri.lastIndexOf('/');
		return slash == -1 ? uri : uri.substring(slash + 1);
	}

	public Destination getDestination(AtomEntry destinationEntry) throws Exception {
		Element destinationElem = SCMPUtils.getDataElement(destinationEntry.getAny());
		DestinationData data = ObjectFactory.createDestinationData(destinationElem);
		Destination destination = data.getDestination();
		return destination;
	}

	public void setDestination(AtomEntry destinationEntry, Destination destination) throws Exception {
		DestinationData data = new DestinationData();
		data.setDestination(destination);
		Element dataElem = ObjectFactory.toElement(data);

		destinationEntry.getAny().clear();
		destinationEntry.getAny().add(dataElem);
	}

	public InstanceData getInstanceData(AtomEntry instanceEntry) throws Exception {
		Element dataElem = SCMPUtils.getDataElement(instanceEntry.getAny());
		InstanceData data = ObjectFactory.createInstanceData(dataElem);
		return data;
	}

	public void setInstanceData(AtomEntry instanceEntry, InstanceData data) throws Exception {
		Element dataElem = ObjectFactory.toElement(data);
		instanceEntry.getAny().clear();
		instanceEntry.getAny().add(dataElem);
	}

	public String getTouchpointRoleCategoryTerm(TouchpointRole touchpointRole) {
		String term;
		switch (touchpointRole) {
		case INITIATOR:
			term = "initiator-tp";
			break;
		case PROVIDER:
			term = "provider-tp";
			break;
		case INTERMEDIARY:
			term = "intermediary-tp";
			break;
		default:
			throw new UnsupportedOperationException();
		}
		return term;
	}

	public AtomEntry getTouchpointInstanceStatusEntry(AtomEntry instanceEntry) throws Exception {
		List<AtomLink> statusLinkList = instanceEntry.getLinksByRelation(Constants.REL_STATUS);
		assertEquals(1, statusLinkList.size());
		AtomLink statusLink = statusLinkList.get(0);
		String statusEntryUrl = statusLink.getHref();
		AtomEntry statusEntry = getAtomEntry(statusEntryUrl);
		assertNotNull(statusEntry);
		return statusEntry;
	}

	public TouchpointStatus getTouchpointStatus(AtomEntry statusEntry) throws SCMPException, JAXBException {
		Element element = SCMPUtils.getDataElement(statusEntry.getAny());
		StatusData data = ObjectFactory.createStatusData(element);
		return data.getTouchpointStatus();
	}

	public boolean isTouchpointInstanceStatusAvailable(AtomEntry statusEntry) throws SCMPException, JAXBException {
		TouchpointStatus touchpointStatus = getTouchpointStatus(statusEntry);
		EnumOpState opState = touchpointStatus.getOpState();
		boolean available = opState == EnumOpState.AVAILABLE;
		return available;
	}

	public boolean isTouchpointInstanceAvailable(AtomEntry instanceEntry) throws Exception {
		AtomEntry statusEntry = getTouchpointInstanceStatusEntry(instanceEntry);
		return isTouchpointInstanceAvailable(statusEntry);
	}

	public AtomEntry addDestination(String destinationFeedUrl, URL requestOut) throws Exception {
		return addDestination(destinationFeedUrl, requestOut, null);
	}

	public AtomEntry addDestination(String destinationFeedUrl, URL requestOut, URL requestError) throws Exception {
		DestinationData data = new DestinationData();
		data.getDestination().setRequestOut(requestOut.toExternalForm());
		if (requestError != null) {
			data.getDestination().setRequestError(requestError.toExternalForm());
		}

		AtomEntry destEntry = new AtomEntry();
		destEntry.getAny().add(ObjectFactory.toElement(data));
		MockHttpServletResponse resp = testCtx.invoke(testCtx.constructMockRequest(HttpMethod.POST, destinationFeedUrl,
				MediaType.WILDCARD, MediaType.APPLICATION_ATOM_XML, serializeEntry(destEntry).getBytes("UTF-8")));
		AtomAppHelper.checkSuccess(resp);

		// destEntry
		return com.ibm.di.test.utils.atom.AtomUtils.deserializeEntry(resp.getContentAsString());
	}

	public static String getDestinationFeedUrl(AtomEntry instanceEntry) throws Exception {
		String feedUrl = null;
		List<AtomLink> list = instanceEntry.getLinksByRelation(Constants.REL_DESTINATION_FEED);
		if (list.size() > 0) {
			assertEquals(1, list.size());
			feedUrl = list.get(0).getHref();
		}
		return feedUrl;
	}

	public AtomEntry getDestinationEntry(String destinationFeedUrl, String requestOutUrl) throws Exception {

		AtomEntry matchingEntry = null;

		AtomFeed feed = getAtomFeed(destinationFeedUrl);
		for (AtomEntry referenceEntry : feed.getEntries()) {
			AtomEntry destinationEntry = getAtomEntrySelf(referenceEntry);
			Destination destination = getDestination(destinationEntry);
			if (requestOutUrl.equals(destination.getRequestOut())) {
				matchingEntry = destinationEntry;
				break;
			}
		}

		return matchingEntry;
	}

	public static boolean getInstEnabled(AtomEntry instEntry) throws Exception {
		Element element = SCMPUtils.getDataElement(instEntry.getAny());
		InstanceData data = ObjectFactory.createInstanceData(element);
		EnumAdminState adminState = data.getTouchpoint().getAdminState();
		boolean enabled = adminState == EnumAdminState.ENABLED;
		return enabled;
	}

	public static void setInstEnabled(AtomEntry instEntry, boolean enabled) throws Exception {
		Element element = SCMPUtils.getDataElement(instEntry.getAny());
		InstanceData data = ObjectFactory.createInstanceData(element);
		data.getTouchpoint().setAdminState(enabled ? EnumAdminState.ENABLED : EnumAdminState.DISABLED);
		int i = instEntry.getAny().indexOf(element);
		instEntry.getAny().set(i, ObjectFactory.toElement(data));
	}
	
	/**
	 * Get TDI config where each setter name is used when calling it to set a
	 * string value plus the suffix. If the setter is setUser the value provided
	 * is ("User" + suffix). Integer values are always null and booleans are
	 * always true.
	 * 
	 * 
	 * @param sufix
	 * @return
	 */
	public static TdiNodeConfig createTdiNodeConfg(String suffix) {
		TdiNodeConfig tc = new TdiNodeConfig();

		tc.setId("Id" + suffix);
		tc.setAuthor("Author" + suffix);
		tc.setConatct("Contact" + suffix);
		tc.setEmail("Email" + suffix);
		tc.setHost("Host" + suffix);
		tc.setPort(0);
		tc.setLocal(true);
		tc.setLocation("Location" + suffix);
		tc.setOrganization("Organization" + suffix);
		EncryptedString encString = new EncryptedString();
		encString.setValue("Password" + suffix);
		encString.setEncrypted(true);
		encString.setProtect(true);
		tc.setPassword(encString);
		tc.setSummary("Summary" + suffix);
		tc.setTitle("Title" + suffix);
		tc.setUser("User" + suffix);
		tc.setProviderPort(0);

		return tc;
	}
}
