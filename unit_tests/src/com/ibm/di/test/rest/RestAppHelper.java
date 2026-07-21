package com.ibm.di.test.rest;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;

import com.ibm.di.web.common.atom.AtomEntry;
import com.ibm.di.web.common.atom.AtomFeed;
import org.springframework.mock.web.MockHttpServletResponse;

import com.ibm.di.api.bind.CreateConfig;
import com.ibm.di.api.bind.CustomNotification;
import com.ibm.di.api.rest.internal.AppConstants;
import com.ibm.di.test.http.HttpClientContext;
import com.ibm.di.test.utils.atom.AtomAppHelper;
import com.ibm.di.test.utils.atom.AtomUtils;
import com.ibm.di.util.JAXBUtils;

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
public class RestAppHelper extends AtomAppHelper {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * @param ctx
	 */
	public RestAppHelper(HttpClientContext ctx) {
		super(ctx);
	}

	public String getCIFeedURL() throws Exception {
		return getCollectionURLByCategory(AppConstants.CAT_RES_CI);
	}

	public String getConfigurationFeedURL() throws Exception {
		return getCollectionURLByCategory(AppConstants.CAT_RES_CONFIG);
	}

	public String getListenerFeedURL() throws Exception {
		return getCollectionURLByCategory(AppConstants.CAT_RES_LISTENER);
	}

	public String getServerFeedURL() throws Exception {
		return getCollectionURLByCategory(AppConstants.CAT_RES_SERVER);
	}

	public AtomFeed getServerFeed() throws Exception {
		MockHttpServletResponse resp = testCtx.invoke(testCtx.constructMockRequest(HttpMethod.GET, getServerFeedURL(),
				MediaType.APPLICATION_ATOM_XML));

		checkSuccess(resp);
		return AtomUtils.deserializeFeed(resp.getContentAsString());
	}

	public String getComponentsFeedURL() throws Exception {
		return getResourceUrlByCategoryFromFeed(getServerFeed(), AppConstants.CAT_SRV_INFO, AppConstants.REL_COMPONENT);
	}

	public AtomFeed getComponentsFeed() throws Exception {
		MockHttpServletResponse resp = testCtx.invoke(testCtx.constructMockRequest(HttpMethod.GET, getComponentsFeedURL(),
				MediaType.APPLICATION_ATOM_XML));

		checkSuccess(resp);
		return AtomUtils.deserializeFeed(resp.getContentAsString());
	}

	public String getNotificationURL() throws Exception {
		return getResourceUrlByCategoryFromFeed(getServerFeed(), AppConstants.CAT_SRV_NOTIFICATION, AppConstants.REL_NOTIFY);
	}

	public String getNotificationEntryURL() throws Exception {
		return getResourceUrlByCategoryFromFeed(getServerFeed(), AppConstants.CAT_SRV_NOTIFICATION, AppConstants.REL_SELF);
	}

	public AtomEntry getNotificationEntry() throws Exception {
		MockHttpServletResponse resp = testCtx.invoke(testCtx.constructMockRequest(HttpMethod.GET, getNotificationEntryURL(),
				MediaType.WILDCARD));

		checkSuccess(resp);
		return AtomUtils.deserializeEntry(resp.getContentAsString());
	}

	public void sendCustomNotification(CustomNotification cn) throws Exception {
		MockHttpServletResponse resp = testCtx.invoke(testCtx.constructMockRequest(HttpMethod.POST, getNotificationURL(),
				MediaType.WILDCARD, AppConstants.MT_SERVER_NOTIFY_XML, JAXBUtils.serializeObjectToBytes(cn)));
		checkSuccess(resp);
	}

	public AtomFeed getConfigurationFeed() throws Exception {
		MockHttpServletResponse resp = testCtx.invoke(testCtx.constructMockRequest(HttpMethod.GET, getConfigurationFeedURL(),
				MediaType.APPLICATION_ATOM_XML));

		System.out.println(resp.getContentAsString());
		checkSuccess(resp);
		return AtomUtils.deserializeFeed(resp.getContentAsString());
	}

	public AtomEntry createConfigurationEntry(CreateConfig create) throws Exception {
		MockHttpServletResponse resp = testCtx.invoke(testCtx.constructMockRequest(HttpMethod.POST, getConfigurationFeedURL(),
				MediaType.WILDCARD, AppConstants.MT_API_CONFIG_XML, JAXBUtils.serializeObjectToBytes(create)));
		checkSuccess(resp);
		return AtomUtils.deserializeEntry(resp.getContentAsString());
	}

	public AtomEntry navigateToConfigFile(String relativePath) throws Exception {
		MockHttpServletResponse resp = testCtx.invoke(testCtx.constructMockRequest(HttpMethod.GET, getConfigurationFeedURL() + "/"
				+ getConfigRelPath(relativePath, true), MediaType.WILDCARD));
		checkSuccess(resp);
		return AtomUtils.deserializeEntry(resp.getContentAsString());
	}

	public AtomFeed navigateToConfigDir(String relativePath) throws Exception {
		MockHttpServletResponse resp = testCtx.invoke(testCtx.constructMockRequest(HttpMethod.GET, getConfigurationFeedURL() + "/"
				+ getConfigRelPath(relativePath, false), MediaType.WILDCARD));
		checkSuccess(resp);
		return AtomUtils.deserializeFeed(resp.getContentAsString());
	}

	// KK: fix these to use the ConfigLock
//	public SolutionBinding checkOutConfig(String configFileRelPath, CheckOutConfig co) throws JAXBException, Exception {
//		AtomEntry e = navigateToConfigFile(configFileRelPath);
//		String coUrl = getResourceUrlFromEntry(e, AppConstants.REL_CHECK_OUT);
//
//		MockHttpServletResponse resp = testCtx.invoke(testCtx.constructMockRequest(HttpMethod.POST, coUrl, MediaType.WILDCARD,
//				AppConstants.MT_API_CONFIG_XML, JAXBUtils.serializeObjectToBytes(co)));
//		checkSuccess(resp);
//		return JAXBUtils.deserializeObject(resp.getContentAsString(), SolutionBinding.class);
//	}
//
//	public void checkInConfig(AtomEntry checkedOutEntry, CheckInConfig ci) throws JAXBException, Exception {
//		String coUrl = getResourceUrlFromEntry(checkedOutEntry, AppConstants.REL_CHECK_IN);
//
//		MockHttpServletResponse resp = testCtx.invoke(testCtx.constructMockRequest(HttpMethod.POST, coUrl, MediaType.WILDCARD,
//				AppConstants.MT_API_CONFIG_XML, JAXBUtils.serializeObjectToBytes(ci)));
//		checkSuccess(resp);
//	}
//
//	public void unlockConfig(String configFileRelPath, UnlockConfig unlockConfig) throws JAXBException, Exception {
//		AtomEntry cfg = navigateToConfigFile(configFileRelPath);
//		String coUrl = getResourceUrlFromEntry(cfg, AppConstants.REL_LOCK);
//
//		MockHttpServletResponse resp = testCtx.invoke(testCtx.constructMockRequest(HttpMethod.POST, coUrl, MediaType.WILDCARD,
//				AppConstants.MT_API_CONFIG_XML, JAXBUtils.serializeObjectToBytes(unlockConfig)));
//		checkSuccess(resp);
//	}

	private String getConfigRelPath(String relPath, boolean entry) {
		int slash = relPath.lastIndexOf('/');
		if (slash > -1) {
			relPath = relPath.substring(0, slash + 1) + (entry ? "e:" : "f:") + relPath.substring(slash + 1);
		} else {
			relPath = (entry ? "e:" : "f:") + relPath;
		}

		return relPath;
	}
}
