package com.ibm.di.test.utils.atom;

import static com.ibm.di.test.utils.atom.AtomUtils.atomCategoryComparator;
import static com.ibm.di.test.utils.atom.AtomUtils.containsInAnyOrder;
import static com.ibm.di.test.utils.atom.AtomUtils.deserializeEntry;
import static com.ibm.di.test.utils.atom.AtomUtils.deserializeFeed;
import static com.ibm.di.test.utils.atom.AtomUtils.deserializeService;
import static com.ibm.di.test.utils.atom.AtomUtils.findLinksByRel;
import static com.ibm.di.test.utils.atom.AtomUtils.serializeEntry;
import static com.ibm.di.test.utils.atom.AtomUtils.serializeFeed;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;

import com.ibm.di.web.common.atom.app.AppCollection;
import com.ibm.di.web.common.atom.app.AppService;
import com.ibm.di.web.common.atom.app.AppWorkspace;
import com.ibm.di.web.common.atom.AtomCategory;
import com.ibm.di.web.common.atom.AtomEntry;
import com.ibm.di.web.common.atom.AtomFeed;
import com.ibm.di.web.common.atom.AtomLink;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.ibm.di.test.http.HttpClientContext;
import com.ibm.di.tp.server.util.AtomUtils;

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
public abstract class AtomAppHelper {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	protected final HttpClientContext testCtx;

	public AtomAppHelper(HttpClientContext ctx) {
		this.testCtx = ctx;
	}

	public MockHttpServletResponse getServiceDocument() throws Exception {
		MockHttpServletRequest request = testCtx.constructMockRequest(HttpMethod.GET, testCtx.getHttpRootURI(), MediaType.WILDCARD);
		return testCtx.invoke(request);
	}

	public AppService getServiceDocumentBinded() throws Exception {
		MockHttpServletResponse response = getServiceDocument();

		checkSuccess(response);
		if (response.getContentAsString() == null || response.getContentAsString().trim().length() == 0) {
			throw new RuntimeException("Missing service document.");
		}

		return deserializeService(response.getContentAsString());
	}

	public String getCollectionURLByCategory(AppService service, AtomCategory targetCat) throws Exception {
		for (AppWorkspace wspace : service.getWorkspace()) {
			for (AppCollection col : wspace.getCollection()) {
				if (col.getCategories() != null
						&& containsInAnyOrder(atomCategoryComparator, true, true,
								col.getCategories().getCategory(), targetCat)) {
					return col.getHref();
				}
			}
		}
		throw new RuntimeException("Could not obtain the collection url for Category: {" + targetCat.getScheme() + "}"
				+ targetCat.getTerm());
	}

	public String getCollectionURLByCategory(AtomCategory targetCat) throws Exception {
		return getCollectionURLByCategory(getServiceDocumentBinded(), targetCat);
	}

	public static String getResourceUrlByEntryIdFromFeed(AtomFeed feed, String entryId, String rel) throws JAXBException {
		AtomEntry entry = null;

		for (AtomEntry e : feed.getEntries()) {
			if (e.getId().endsWith(entryId)) {
				entry = e;
				break;
			}
		}

		if (entry == null) {
			throw new RuntimeException("Missing entry with id: " + entryId + " Feed: \n" + serializeFeed(feed));
		}
		return getResourceUrlFromEntry(entry, rel);
	}

	public static String getResourceUrlByCategoryFromFeed(AtomFeed feed, String catTerm, String rel) {
		AtomCategory ac = new AtomCategory();
		ac.setTerm(catTerm);
		return getResourceUrlByCategoryFromFeed(feed, ac, rel);
	}

	public static String getResourceUrlByCategoryFromFeed(AtomFeed feed, AtomCategory cat, String rel) {
		// System.out.println(serializeFeed(feed));
		for (AtomEntry e : feed.getEntries()) {
			for (AtomCategory eCat : e.getCategories()) {
				if (eCat.getTerm().equals(cat.getTerm()) && (cat.getScheme() == null || cat.getScheme().equals(eCat.getScheme()))) {
					return getResourceUrlFromEntry(e, rel);
				}
			}
		}

		throw new RuntimeException("Missing entry with category: " + com.ibm.di.test.utils.atom.AtomUtils.toString(cat) + " Feed: "
				+ com.ibm.di.test.utils.atom.AtomUtils.toString(feed));
	}

	public static List<AtomEntry> getResourcesByCategoryFromFeed(AtomFeed feed, AtomCategory cat) {
		return getResourcesByCategoryFromFeed(feed, cat, false);
	}

	public static List<AtomEntry> getResourcesByCategoryFromFeed(AtomFeed feed, AtomCategory cat, boolean allowEmpty) {
		// System.out.println(serializeFeed(feed));
		List<AtomEntry> list = new ArrayList<AtomEntry>();
		for (AtomEntry e : feed.getEntries()) {
			if (isCategorySet(e, cat)) {
				list.add(e);
			}
		}
		if (!allowEmpty && list.size() == 0) {
			throw new RuntimeException("Missing entry with category: " + com.ibm.di.test.utils.atom.AtomUtils.toString(cat)
					+ " Feed: " + com.ibm.di.test.utils.atom.AtomUtils.toString(feed));
		}
		return list;
	}

	public static boolean isCategorySet(AtomEntry e, AtomCategory cat) {
		for (AtomCategory eCat : e.getCategories()) {
			if (eCat.getTerm().equals(cat.getTerm()) && (cat.getScheme() == null || cat.getScheme().equals(eCat.getScheme()))) {
				return true;
			}
		}
		return false;
	}

	public static String getResourceUrlFromEntry(AtomEntry entry, String rel) {
		String link = getResourceUrlFromEntryNoException(entry, rel);
		if (link == null) {
			throw new RuntimeException("Missing \"" + rel + "\" link for entry: "
					+ com.ibm.di.test.utils.atom.AtomUtils.toString(entry));

		}
		return link;
	}

	public static String getResourceUrlFromEntryNoException(AtomEntry entry, String rel) {
		List<AtomLink> typeFeedLink = findLinksByRel(entry.getLinks(), rel);

		return (typeFeedLink == null || typeFeedLink.size() == 0) ? null : typeFeedLink.get(0).getHref();
	}

	public static void checkSuccess(MockHttpServletResponse response) throws UnsupportedEncodingException {
		if (!(response.getStatus() >= 200 && response.getStatus() < 300)) {
			throw new RuntimeException(response.getStatus() + " " + response.getContentAsString());
		}
	}

	public static String getSelfUrl(AtomEntry entry) throws Exception {
		List<AtomLink> list = entry.getLinksByRelation("self");
		String selfUrl = null;
		if (list.size() > 0) {
			assertEquals(1, list.size());
			selfUrl = list.get(0).getHref();

		}
		return selfUrl;
	}

	public static String getEditUrl(AtomEntry entry) throws Exception {
		List<AtomLink> list = entry.getLinksByRelation("edit");
		String editUrl = null;
		if (list.size() > 0) {
			assertEquals(1, list.size());
			editUrl = list.get(0).getHref();

		}
		return editUrl;
	}

	public void putAtomEntry(AtomEntry entry) throws Exception {
		String editUrl = getEditUrl(entry);
		assertNotNull(editUrl);
		putAtomEntry(entry, editUrl);
	}

	public void putAtomEntry(AtomEntry entry, String entryUrl) throws Exception {
		MockHttpServletRequest request = testCtx.constructMockRequest(HttpMethod.PUT, entryUrl, MediaType.APPLICATION_ATOM_XML,
				MediaType.APPLICATION_ATOM_XML, serializeEntry(entry).getBytes("UTF-8"));
		MockHttpServletResponse response = testCtx.invoke(request);
		checkSuccess(response);
	}

	public AtomEntry getAtomEntry(String entryUrl) throws Exception {
		MockHttpServletRequest request = testCtx.constructMockRequest(HttpMethod.GET, entryUrl, MediaType.WILDCARD);
		MockHttpServletResponse response = testCtx.invoke(request);
		checkSuccess(response);
		AtomEntry entry = deserializeEntry(response.getContentAsString());
		return entry;
	}

	public AtomFeed getAtomFeed(String feedUrl) throws Exception {
		MockHttpServletRequest request = testCtx.constructMockRequest(HttpMethod.GET, feedUrl, MediaType.WILDCARD);
		MockHttpServletResponse response = testCtx.invoke(request);
		checkSuccess(response);
		AtomFeed feed = deserializeFeed(response.getContentAsString());
		return feed;
	}

	public AtomEntry getAtomEntrySelf(AtomEntry entry) throws Exception {
		String selfUrl = getSelfUrl(entry);
		assertNotNull(selfUrl);
		AtomEntry selfEntry = getAtomEntry(selfUrl);
		assertThat(selfEntry, is(notNullValue()));
		return selfEntry;
	}
}
