package com.ibm.di.tp.server.handler;

import static com.ibm.di.test.tp.mock.ServerAPIMockFactory.createNodeEntryContext;
import static com.ibm.di.test.tp.mock.ServerAPIMockFactory.createServerAPIActiveProviderServerContext;
import static com.ibm.di.test.tp.mock.ServerAPIMockFactory.createServerAPITypesContext;
import static com.ibm.di.test.utils.atom.AtomUtils.atomFeedComparator;
import static com.ibm.di.test.utils.atom.AtomUtils.createNodeFeed;
import static com.ibm.di.test.utils.atom.AtomUtils.createReferenceAtomFeed;
import static com.ibm.di.test.utils.atom.AtomUtils.deserializeFeed;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;
import static org.hamcrest.number.OrderingComparison.lessThan;
import static org.hamcrest.text.IsEmptyString.isEmptyOrNullString;
import static org.junit.Assert.assertThat;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;

import com.ibm.di.web.common.atom.AtomFeed;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.ibm.di.test.api.mock.ServerAPIMock;
import com.ibm.di.test.tp.UnitTestTPClientContext;
import com.ibm.di.tp.server.config.node.TdiNodeConfig;

/**
 * <p>
 * The unit tests should extend this class in order to simulate the servlet
 * invocation.
 * <p>
 * The method <tt>invoke</tt> invokes the servlet call.
 */
public class TPNodeFeedTest extends UnitTestTPClientContext {

	@Override
	protected boolean isAutoInit() {
		return false;
	}

	@Test
	public void test_Verify_TP_Node_Feed_Contains_Expected_Categories() throws Exception {
		TdiNodeConfig[] cfgs = createNodeEntryContext(getTPServerConfig(), 1);
		ServerAPIMock m1 = createServerAPIActiveProviderServerContext(null, cfgs[0].getProviderPort());
		m1 = createServerAPITypesContext(m1, new String[0]);
		setServerAPIConnection(m1);
		m1.activateMocks();
		initContext();

		UnitAndFuncSharedTests.verify_TP_Node_Feed_Contains_Expected_Categories(this);
	}

	@Test
	public void test_GET_Node_Feed_With_Empty_Config() throws Exception {
		initContext();
		MockHttpServletResponse response = app.getNodeFeed();

		AtomFeed expFeed = createNodeFeed(CONTEXT_ROOT_USED_BY_WINK, getTPServerConfig());
		AtomFeed actFeed = deserializeFeed(response.getContentAsString());

		atomFeedComparator.assertEquals(actFeed, expFeed, true);
	}

	@Test
	public void test_GET_Node_Feed_With_Two_Local_TDI_Node_Configs() throws Exception {
		TdiNodeConfig[] cfgs = createNodeEntryContext(getTPServerConfig(), 2);

		ServerAPIMock m1 = createServerAPIActiveProviderServerContext(null, cfgs[0].getProviderPort());
		m1 = createServerAPITypesContext(m1, new String[0]);
		cfgs[0].setLocal(false);
		setServerAPIConnection(cfgs[0].getHost(), cfgs[0].getPort(), m1);
		m1.activateMocks();

		ServerAPIMock m2 = createServerAPIActiveProviderServerContext(null, cfgs[1].getProviderPort());
		m2 = createServerAPITypesContext(m2, new String[0]);
		cfgs[1].setLocal(false);
		setServerAPIConnection(cfgs[1].getHost(), cfgs[1].getPort(), m2);
		m2.activateMocks();
		initContext();

		AtomFeed expFeed = createNodeFeed(CONTEXT_ROOT_USED_BY_WINK, getTPServerConfig());
		expFeed = createReferenceAtomFeed(expFeed, true);

		String contentAsString = app.getNodeFeed().getContentAsString();
		AtomFeed actFeed = deserializeFeed(contentAsString);

		atomFeedComparator.assertEquals(actFeed, expFeed, true);
	}

	@Test
	public void test_GET_Node_Feed_With_Exact_If_Match_Header() throws Exception {
		initContext();

		String tpNodeFeedURL = app.getNodeFeedURL();
		MockHttpServletRequest request = constructMockRequest(HttpMethod.GET, tpNodeFeedURL, MediaType.APPLICATION_ATOM_XML);

		MockHttpServletResponse response = invoke(request);
		AtomFeed expFeed = deserializeFeed(response.getContentAsString());

		// get the eTag value
		String eTag = (String) response.getHeader("ETag");

		// send another request with If-Match tag.
		request = constructMockRequest(HttpMethod.GET, tpNodeFeedURL, MediaType.APPLICATION_ATOM_XML);
		request.addHeader("If-Match", eTag);

		response = invoke(request);

		// so we should get the same representation if both eTags match as per
		// our precondition
		AtomFeed actFeed = deserializeFeed(response.getContentAsString());

		atomFeedComparator.assertEquals(actFeed, expFeed);
		assertThat(response.getStatus(), is(greaterThanOrEqualTo(200)));
		assertThat(response.getStatus(), is(lessThan(300)));
	}

	@Test
	public void test_GET_Node_Feed_With_Different_If_Match_Header() throws Exception {
		initContext();

		String tpNodeFeedURL = app.getNodeFeedURL();
		MockHttpServletRequest request = constructMockRequest(HttpMethod.GET, tpNodeFeedURL, MediaType.APPLICATION_ATOM_XML);

		MockHttpServletResponse response = invoke(request);

		// get the eTag value
		String eTag = (String) response.getHeader("ETag");

		// send another request with If-Match tag.
		request = constructMockRequest(HttpMethod.GET, tpNodeFeedURL, MediaType.APPLICATION_ATOM_XML);
		request.addHeader("If-Match", eTag.substring(0, eTag.length() - 1) + "invalidSuffix\"");

		response = invoke(request);

		// so we should get 412 meaning that the precondition has failed.
		assertThat(response.getStatus(), is(412));
	}

	@Test
	public void test_GET_Node_Feed_With_Wildcard_If_Match_Header() throws Exception {
		initContext();

		String tpNodeFeedURL = app.getNodeFeedURL();
		MockHttpServletRequest request = constructMockRequest(HttpMethod.GET, tpNodeFeedURL, MediaType.APPLICATION_ATOM_XML);
		request.addHeader("If-Match", "\"*\"");
		MockHttpServletResponse response = invoke(request);

		// so we should get the same representation
		assertThat(response.getStatus(), is(greaterThanOrEqualTo(200)));
		assertThat(response.getStatus(), is(lessThan(300)));
		assertThat(response.getContentAsString(), is(not(isEmptyOrNullString())));
	}

	@Test
	public void test_GET_Node_Feed_With_Exact_If_None_Match_Header() throws Exception {
		initContext();

		String tpNodeFeedURL = app.getNodeFeedURL();
		MockHttpServletRequest request = constructMockRequest(HttpMethod.GET, tpNodeFeedURL, MediaType.APPLICATION_ATOM_XML);
		MockHttpServletResponse response = invoke(request);

		// get the eTag value
		String eTag = (String) response.getHeader("ETag");

		// send another request with If-None-Match tag.
		request = constructMockRequest(HttpMethod.GET, tpNodeFeedURL, MediaType.APPLICATION_ATOM_XML);
		request.addHeader("If-None-Match", eTag);

		response = invoke(request);

		// we should get 304 - not modified response
		assertThat(response.getStatus(), is(304));
	}

	@Test
	public void test_GET_Node_Feed_With_Different_If_None_Match_Header() throws Exception {
		initContext();

		String tpNodeFeedURL = app.getNodeFeedURL();
		MockHttpServletRequest request = constructMockRequest(HttpMethod.GET, tpNodeFeedURL, MediaType.APPLICATION_ATOM_XML);
		MockHttpServletResponse response = invoke(request);
		AtomFeed expFeed = deserializeFeed(response.getContentAsString());

		// get the eTag value
		String eTag = (String) response.getHeader("ETag");

		// send another request with If-None-Match tag.
		request = constructMockRequest(HttpMethod.GET, tpNodeFeedURL, MediaType.APPLICATION_ATOM_XML);
		request.addHeader("If-None-Match", eTag.substring(0, eTag.length() - 1) + "invalidSuffix\"");

		response = invoke(request);
		AtomFeed actFeed = deserializeFeed(response.getContentAsString());

		// so we should get 2xx meaning that the entity has changed.
		assertThat(response.getStatus(), is(greaterThanOrEqualTo(200)));
		assertThat(response.getStatus(), is(lessThan(300)));

		// the two feeds should match.
		atomFeedComparator.assertEquals(actFeed, expFeed);
	}

	@Test
	public void test_GET_Node_Feed_With_Wildcard_If_None_Match_Header() throws Exception {
		initContext();

		String tpNodeFeedURL = app.getNodeFeedURL();
		MockHttpServletRequest request = constructMockRequest(HttpMethod.GET, tpNodeFeedURL, MediaType.APPLICATION_ATOM_XML);
		request.addHeader("If-None-Match", "\"*\"");
		MockHttpServletResponse response = invoke(request);

		// we should get 304 - not modified response
		assertThat(response.getStatus(), is(304));
	}
}