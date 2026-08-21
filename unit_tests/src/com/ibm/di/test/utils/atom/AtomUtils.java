package com.ibm.di.test.utils.atom;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.ibm.di.web.common.atom.app.AppService;
import com.ibm.di.web.common.atom.AtomCategory;
import com.ibm.di.web.common.atom.AtomContent;
import com.ibm.di.web.common.atom.AtomEntry;
import com.ibm.di.web.common.atom.AtomFeed;
import com.ibm.di.web.common.atom.AtomLink;
import com.ibm.di.web.common.atom.AtomPerson;
import com.ibm.di.web.common.atom.AtomText;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.di.tp.server.Constants;
import com.ibm.di.tp.server.config.TPServerConfig;
import com.ibm.di.tp.server.config.node.TdiNodeConfig;
import com.ibm.di.tp.server.handler.node.TPNodeFeed;
import com.ibm.di.tp.server.handler.type.TPTypeFeed;
import com.ibm.di.tp.server.model.TouchpointRole;
import com.ibm.di.tp.server.model.config.DestinationData;
import com.ibm.di.tp.server.model.config.EnumAdminState;
import com.ibm.di.tp.server.model.config.InstanceData;
import com.ibm.di.tp.server.model.config.ObjectFactory;
import com.ibm.di.tp.server.model.config.Property;
import com.ibm.di.tp.server.model.config.PropertySheet;
import com.ibm.di.util.JAXBUtils;

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
public abstract class AtomUtils {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static Document doc;
	static {
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			// should not happen
		}
	}

	public static abstract class ObjectComparator<T> {
		public void assertEquals(T actual, T expected) {
			assertEquals(actual, expected, false);
		}

		/**
		 * @param actual
		 *            Actual object.
		 * @param expected
		 *            Expected object.
		 * @param ignoreContextualDiffs
		 *            Set to false to ignore differences due to
		 *            runtime-generated values such as the base URL of the
		 *            application. These are usually values which the Atom
		 *            Server embeds when expanding links.
		 */
		public abstract void assertEquals(T actual, T expected, boolean ignoreContextualDiffs);

		public void assertEquals(String message, T expected, T actual, boolean ignoreContextualDiffs) {
			try {
				assertEquals(actual, expected, ignoreContextualDiffs);
			} catch (IgnorableAssertionException ex) {
				throw new ImportantAssertionException(message, ex);
			}
		}
	}

	public static class AtomCategoryComparator extends ObjectComparator<AtomCategory> {

		@Override
		public void assertEquals(AtomCategory actual, AtomCategory expected, boolean ignoreContextualDiffs) {
			if (expected == null ^ actual == null) {
				ignorableFail("AtomCategory mismatch - expected: " + expected + " received: " + actual);
			} else if (expected != null && actual != null) {
				if (!areObjectsEqualOrBothNull(expected.getTerm(), actual.getTerm())) {
					ignorableFail("AtomCategory mismatch in \"Term\" - expected: " + expected.getTerm() + " received: "
							+ actual.getTerm());
				}

				if (!areObjectsEqualOrBothNull(expected.getScheme(), actual.getScheme())) {
					ignorableFail("AtomCategory mismatch in \"Schema\" - expected: " + expected.getScheme() + " received: "
							+ actual.getScheme());
				}

				// most important values match... consider the same objects...
				if (!areObjectsEqualOrBothNull(expected.getBase(), actual.getBase()) && !ignoreContextualDiffs) {
					importantFail("AtomCategory mismatch in \"Base\" - expected: " + expected.getBase() + " received: "
							+ actual.getBase());
				}

				if (!areObjectsEqualOrBothNull(expected.getLabel(), actual.getLabel())) {
					importantFail("AtomCategory mismatch in \"Label\" - expected: " + expected.getLabel() + " received: "
							+ actual.getLabel());
				}

				if (!areObjectsEqualOrBothNull(expected.getLang(), actual.getLang())) {
					importantFail("AtomCategory mismatch in \"Lang\" - expected: " + expected.getLang() + " received: "
							+ actual.getLang());
				}
			}
		}
	};

	public static final ObjectComparator<AtomCategory> atomCategoryComparator = new AtomCategoryComparator();

	public static class AtomPersonComparator extends ObjectComparator<AtomPerson> {

		@Override
		public void assertEquals(AtomPerson actual, AtomPerson expected, boolean ignoreContextualDiffs) {
			if (expected == null ^ actual == null) {
				ignorableFail("AtomPerson mismatch - expected: " + expected + " received: " + actual);
			} else if (expected != null && actual != null) {

				if (!areObjectsEqualOrBothNull(expected.getEmail(), actual.getEmail())) {
					ignorableFail("AtomPerson mismatch in \"Email\" - expected: " + expected.getEmail() + " received: "
							+ actual.getEmail());
				}

				if (!areObjectsEqualOrBothNull(expected.getName(), actual.getName())) {
					ignorableFail("AtomPerson mismatch in \"Name\" - expected: " + expected.getName() + " received: "
							+ actual.getName());
				}

				if (!areObjectsEqualOrBothNull(expected.getUri(), actual.getUri())) {
					ignorableFail("AtomPerson mismatch in \"Uri\" - expected: " + expected.getUri() + " received: "
							+ actual.getUri());
				}

				containsInAnyOrder("AtomPerson mismatch in \"Any\"", elementComparator, ignoreContextualDiffs, false, actual
						.getAny(), expected.getAny());
			}
		}
	};

	public static final ObjectComparator<AtomPerson> atomPersonComparator = new AtomPersonComparator();

	public static class AtomLinkComparator extends ObjectComparator<AtomLink> {

		@Override
		public void assertEquals(AtomLink actual, AtomLink expected, boolean ignoreContextualDiffs) {
			if (expected == null ^ actual == null) {
				ignorableFail("AtomLink mismatch - expected: " + expected + " received: " + actual);
			} else if (expected != null && actual != null) {
				if (!areObjectsEqualOrBothNull(expected.getRel(), actual.getRel())) {
					ignorableFail("AtomLink mismatch in \"Rel\" - expected: " + expected.getRel() + " actual: " + actual.getRel());
				}
				if (!areObjectsEqualOrBothNull(expected.getType(), actual.getType())) {
					ignorableFail("AtomLink mismatch in \"Type\" - expected: " + expected.getType() + " actual: "
							+ actual.getType());
				}

				if (!areObjectsEqualOrBothNull(expected.getBase(), actual.getBase()) && !ignoreContextualDiffs) {
					importantFail("AtomLink mismatch in \"Base\" - expected: " + expected.getBase() + " actual: "
							+ actual.getBase());
				}
				if (!areObjectsEqualOrBothNull(expected.getHref(), actual.getHref()) && !ignoreContextualDiffs) {
					importantFail("AtomLink mismatch in \"Href\" - expected: " + expected.getHref() + " actual: "
							+ actual.getHref());
				}
				if (!areObjectsEqualOrBothNull(expected.getHreflang(), actual.getHreflang())) {
					importantFail("AtomLink mismatch in \"Hreflang\" - expected: " + expected.getHreflang() + " actual: "
							+ actual.getHreflang());
				}
				if (!areObjectsEqualOrBothNull(expected.getLang(), actual.getLang())) {
					importantFail("AtomLink mismatch in \"Lang\" - expected: " + expected.getLang() + " actual: "
							+ actual.getLang());
				}
				if (!areObjectsEqualOrBothNull(expected.getLength(), actual.getLength())) {
					importantFail("AtomLink mismatch in \"Length\" - expected: " + expected.getLength() + " actual: "
							+ actual.getLength());
				}
				if (!areObjectsEqualOrBothNull(expected.getTitle(), actual.getTitle())) {
					importantFail("AtomLink mismatch in \"Title\" - expected: " + expected.getTitle() + " actual: "
							+ actual.getTitle());
				}
			}
		}
	};

	public static final ObjectComparator<AtomLink> atomLinkComparator = new AtomLinkComparator();

	public static class AtomTextComparator extends ObjectComparator<AtomText> {

		@Override
		public void assertEquals(AtomText actual, AtomText expected, boolean ignoreContextualDiffs) {
			if (expected == null ^ actual == null) {
				ignorableFail("AtomText mismatch - expected: " + expected + " received: " + actual);
			} else if (expected != null && actual != null) {
				if (!areObjectsEqualOrBothNull(expected.getValue(), actual.getValue())) {
					ignorableFail("AtomText mismatch in \"Value\" - expected: " + expected.getValue() + " actual: "
							+ actual.getValue());
				}
				if (!areObjectsEqualOrBothNull(expected.getType(), actual.getType())) {
					ignorableFail("AtomText mismatch in \"Type\" - expected: " + expected.getType() + " actual: "
							+ actual.getType());
				}

				if (!areObjectsEqualOrBothNull(expected.getBase(), actual.getBase()) && !ignoreContextualDiffs) {
					importantFail("AtomText mismatch in \"Base\" - expected: " + expected.getBase() + " actual: "
							+ actual.getBase());
				}
				if (!areObjectsEqualOrBothNull(expected.getLang(), actual.getLang())) {
					importantFail("AtomText mismatch in \"Lang\" - expected: " + expected.getLang() + " actual: "
							+ actual.getLang());
				}

			}
		}
	};

	public static final ObjectComparator<AtomText> atomTextComparator = new AtomTextComparator();

	public static class AtomContentComparator extends ObjectComparator<AtomContent> {

		@Override
		public void assertEquals(AtomContent actual, AtomContent expected, boolean ignoreContextualDiffs) {
			if (expected == null ^ actual == null) {
				ignorableFail("AtomContent mismatch - expected: " + expected + " received: " + actual);
			} else if (expected != null && actual != null) {
				if (!areObjectsEqualOrBothNull(expected.getValue(), actual.getValue())) {
					ignorableFail("AtomContent mismatch in \"Value\" - expected: " + expected.getValue() + " actual: "
							+ actual.getValue());
				}
				if (!areObjectsEqualOrBothNull(expected.getType(), actual.getType())) {
					ignorableFail("AtomContent mismatch in \"Type\" - expected: " + expected.getType() + " actual: "
							+ actual.getType());
				}
				if (!areObjectsEqualOrBothNull(expected.getSrc(), actual.getSrc()) && !ignoreContextualDiffs) {
					ignorableFail("AtomContent mismatch in \"Src\" - expected: " + expected.getSrc() + " actual: "
							+ actual.getSrc());
				}

				if (!areObjectsEqualOrBothNull(expected.getBase(), actual.getBase()) && !ignoreContextualDiffs) {
					importantFail("AtomContent mismatch in \"Base\" - expected: " + expected.getBase() + " actual: "
							+ actual.getBase());
				}
				if (!areObjectsEqualOrBothNull(expected.getLang(), actual.getLang())) {
					importantFail("AtomContent mismatch in \"Lang\" - expected: " + expected.getLang() + " actual: "
							+ actual.getLang());
				}
			}
		}
	};

	public static final ObjectComparator<AtomContent> atomContentComparator = new AtomContentComparator();

	public static class AtomEntryComparator extends ObjectComparator<AtomEntry> {

		@Override
		public void assertEquals(AtomEntry actual, AtomEntry expected, boolean ignoreContextualDiffs) {
			if (expected == null ^ actual == null) {
				ignorableFail("AtomEntry mismatch - expected: " + expected + " received: " + actual);
			} else if (expected != null && actual != null) {
				if (!areObjectsEqualOrBothNull(expected.getId(), actual.getId()) && !ignoreContextualDiffs) {
					ignorableFail("AtomEntry mismatch in \"Id\" - expected: " + expected.getId() + " actual: " + actual.getId());
				}

				if (!areObjectsEqualOrBothNull(expected.getBase(), actual.getBase()) && !ignoreContextualDiffs) {
					importantFail("AtomEntry mismatch in \"Base\" - expected: " + expected.getBase() + " actual: "
							+ actual.getBase());
				}

				atomContentComparator.assertEquals("Actual entry: " + AtomUtils.toString(actual) + "\n mismatch in \"Content\"",
						expected.getContent(), actual.getContent(), ignoreContextualDiffs);

				if (!areObjectsEqualOrBothNull(expected.getLang(), actual.getLang())) {
					importantFail("AtomEntry mismatch in \"Lang\" - expected: " + expected.getLang() + " actual: "
							+ actual.getLang());
				}

				atomTextComparator.assertEquals("AtomEntry(" + expected.getId() + ") mismatch in \"Summary\"", expected
						.getSummary(), actual.getSummary(), ignoreContextualDiffs);
				atomTextComparator.assertEquals("AtomEntry(" + expected.getId() + ") mismatch in \"Title\"", expected.getTitle(),
						actual.getTitle(), ignoreContextualDiffs);
				containsInAnyOrder("AtomEntry(" + expected.getId() + ") mismatch in \"Authors\"", atomPersonComparator,
						ignoreContextualDiffs, true, actual.getAuthors(), expected.getAuthors());
				containsInAnyOrder("AtomEntry(" + expected.getId() + ") mismatch in \"Contributors\"", atomPersonComparator,
						ignoreContextualDiffs, true, actual.getContributors(), expected.getContributors());
				containsInAnyOrder("AtomEntry(" + expected.getId() + ") mismatch in \"Categories\"", atomCategoryComparator,
						ignoreContextualDiffs, true, actual.getCategories(), expected.getCategories());
				containsInAnyOrder("AtomEntry(" + expected.getId() + ") mismatch in \"Links\"", atomLinkComparator,
						ignoreContextualDiffs, true, actual.getLinks(), expected.getLinks());
				containsInAnyOrder("AtomEntry(" + expected.getId() + ") mismatch in \"Any\"", elementComparator,
						ignoreContextualDiffs, true, actual.getAny(), expected.getAny());
			}
		}
	};

	public static final ObjectComparator<AtomEntry> atomEntryComparator = new AtomEntryComparator();

	public static class AtomFeedComparator extends ObjectComparator<AtomFeed> {

		@Override
		public void assertEquals(AtomFeed actual, AtomFeed expected, boolean ignoreContextualDiffs) {
			if (expected == null ^ actual == null) {
				ignorableFail("AtomFeed mismatch - expected: " + expected + " received: " + actual);
			} else if (expected != null && actual != null) {
				if (!areObjectsEqualOrBothNull(expected.getId(), actual.getId()) && !ignoreContextualDiffs) {
					ignorableFail("AtomFeed mismatch in \"Id\" - expected: " + expected.getId() + " actual: " + actual.getId());
				}

				if (!areObjectsEqualOrBothNull(expected.getBase(), actual.getBase()) && !ignoreContextualDiffs) {
					importantFail("AtomFeed mismatch in \"Base\" - expected: " + expected.getBase() + " actual: "
							+ actual.getBase());
				}

				if (!areObjectsEqualOrBothNull(expected.getItemsPerPage(), actual.getItemsPerPage())) {
					importantFail("AtomFeed mismatch in \"ItemsPerPage\" - expected: " + expected.getItemsPerPage() + " actual: "
							+ actual.getItemsPerPage());
				}

				if (!areObjectsEqualOrBothNull(expected.getIcon(), actual.getIcon())) {
					importantFail("AtomFeed mismatch in \"Icon\" - expected: " + expected.getIcon() + " actual: "
							+ actual.getIcon());
				}

				if (!areObjectsEqualOrBothNull(expected.getLogo(), actual.getLogo())) {
					importantFail("AtomFeed mismatch in \"Logo\" - expected: " + expected.getLogo() + " actual: "
							+ actual.getLogo());
				}

				if (!areObjectsEqualOrBothNull(expected.getStartIndex(), actual.getStartIndex())) {
					importantFail("AtomFeed mismatch in \"StartIndex\" - expected: " + expected.getStartIndex() + " actual: "
							+ actual.getStartIndex());
				}

				if (!areObjectsEqualOrBothNull(expected.getTotalResults(), actual.getTotalResults())) {
					importantFail("AtomFeed mismatch in \"TotalResults\" - expected: " + expected.getTotalResults() + " actual: "
							+ actual.getTotalResults());
				}

				atomTextComparator.assertEquals("AtomFeed(" + expected.getId() + ") mismatch in \"Subtitle\"", expected
						.getSubtitle(), expected.getSubtitle(), ignoreContextualDiffs);
				atomTextComparator.assertEquals("AtomFeed(" + expected.getId() + ") mismatch in \"Rights\"", expected.getRights(),
						expected.getRights(), ignoreContextualDiffs);
				atomTextComparator.assertEquals("AtomFeed(" + expected.getId() + ") mismatch in \"Title\"", expected.getTitle(),
						actual.getTitle(), ignoreContextualDiffs);
				containsInAnyOrder("AtomFeed(" + expected.getId() + ") mismatch in \"Entries\"", atomEntryComparator,
						ignoreContextualDiffs, true, actual.getEntries(), expected.getEntries());
				containsInAnyOrder("AtomFeed(" + expected.getId() + ") mismatch in \"Authors\"", atomPersonComparator,
						ignoreContextualDiffs, true, actual.getAuthors(), expected.getAuthors());
				containsInAnyOrder("AtomFeed(" + expected.getId() + ") mismatch in \"Contributors\"", atomPersonComparator,
						ignoreContextualDiffs, true, actual.getContributors(), expected.getContributors());
				containsInAnyOrder("AtomFeed(" + expected.getId() + ") mismatch in \"Categories\"", atomCategoryComparator,
						ignoreContextualDiffs, true, actual.getCategories(), expected.getCategories());
				containsInAnyOrder("AtomFeed(" + expected.getId() + ") mismatch in \"Links\"", atomLinkComparator,
						ignoreContextualDiffs, true, actual.getLinks(), expected.getLinks());
			}
		}
	};

	public static final ObjectComparator<AtomFeed> atomFeedComparator = new AtomFeedComparator();

	public static class AttrComparator extends ObjectComparator<Attr> {

		@Override
		public void assertEquals(Attr actual, Attr expected, boolean ignoreContextualDiffs) {
			if (expected == null ^ actual == null) {
				ignorableFail("Attr mismatch - expected: " + expected + " received: " + actual);
			} else if (expected != null && actual != null) {
				if (!areObjectsEqualOrBothNull(expected.getLocalName(), actual.getLocalName())) {
					ignorableFail("Attr mismatch in \"LocalName\" - expected: " + expected.getLocalName() + " actual: "
							+ actual.getLocalName());
				}
				if (!areObjectsEqualOrBothNull(expected.getNamespaceURI(), actual.getNamespaceURI())) {
					ignorableFail("Attr mismatch in \"NamespaceURI\" - expected: " + expected.getNamespaceURI() + " actual: "
							+ actual.getNamespaceURI());
				}

				if (!areObjectsEqualOrBothNull(expected.getValue(), actual.getValue())) {

					boolean ignoreSchemaLocation = "schemaLocation".equals(actual.getLocalName()) && ignoreContextualDiffs;

					if (!ignoreSchemaLocation) {
						importantFail("Attr mismatch in \"Value\" - expected: " + expected.getValue() + " actual: "
								+ actual.getValue());
					}
				}
			}
		}
	};

	public static final ObjectComparator<Attr> attrComparator = new AttrComparator();

	public static class ElementComparator extends ObjectComparator<Element> {

		@Override
		public void assertEquals(Element actual, Element expected, boolean ignoreContextualDiffs) {
			if (!areObjectsEqualOrBothNull(expected.getNodeType(), actual.getNodeType())) {
				ignorableFail("Element (" + getNodeName(expected) + ") mismatch in \"NodeType\" - expected: "
						+ expected.getNodeType() + " actual: " + actual.getNodeType());
			}
			if (!areObjectsEqualOrBothNull(expected.getLocalName(), actual.getLocalName())) {
				ignorableFail("Element (" + getNodeName(expected) + ") mismatch in \"LocalName\" - expected: "
						+ expected.getLocalName() + " actual: " + actual.getLocalName());
			}
			if (!areObjectsEqualOrBothNull(expected.getNamespaceURI(), actual.getNamespaceURI())) {
				ignorableFail("Element (" + getNodeName(expected) + ") mismatch in \"NamespaceURI\" - expected: "
						+ expected.getNamespaceURI() + " actual: " + actual.getNamespaceURI());
			}

			if (!areObjectsEqualOrBothNull(expected.getBaseURI(), actual.getBaseURI())) {
				importantFail("Element (" + getNodeName(expected) + ") mismatch in \"BaseURI\" - expected: "
						+ expected.getBaseURI() + " actual: " + actual.getBaseURI());
			}

			String expText = getFirstLevelTextChildrenContent(expected);
			String actText = getFirstLevelTextChildrenContent(actual);
			if (!areObjectsEqualOrBothNull(expText, actText)) {
				importantFail("Element (" + getNodeName(expected) + ") mismatch in \"TextContent\" - expected: " + expText
						+ " actual: " + actText);
			}

			NamedNodeMap expAttrs = expected.getAttributes();
			NamedNodeMap actAttrs = actual.getAttributes();

			Attr attr1 = null;
			Attr attr2 = null;
			for (int i = 0; i < expAttrs.getLength(); i++) {
				attr1 = (Attr) expAttrs.item(0);
				attr2 = (Attr) actAttrs.getNamedItemNS(attr1.getNamespaceURI(), attr1.getLocalName());
				if (attr2 != null) {
					attrComparator.assertEquals(attr1, attr2, ignoreContextualDiffs);
				} else if (!(XMLConstants.XMLNS_ATTRIBUTE.equals(attr1.getNodeName()) && XMLConstants.XMLNS_ATTRIBUTE_NS_URI
						.equals(attr1.getNamespaceURI()))) {
					importantFail("Element (" + getNodeName(expected) + ") missing expected attribute: " + getNodeName(attr1));
				}
			}

			Map<QName, Integer> elemPos = new HashMap<QName, Integer>();

			NodeList children = expected.getChildNodes();
			Element child = null;
			Integer childPos = 0;
			QName qName = null;

			List<Element> expectedList = new ArrayList<Element>();
			List<Element> actualList = new ArrayList<Element>();

			for (int i = 0; i < children.getLength(); i++) {
				if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
					child = (Element) children.item(i);
					qName = new QName(child.getNamespaceURI(), child.getLocalName());
					childPos = elemPos.get(qName);

					if (childPos == null) {
						childPos = 0;
					}

					// first time for this element
					elemPos.put(qName, ++childPos);

					Element actChild = findElementByLocalQNameAndPostion(actual, qName, childPos);
					if (actChild != null) {
						expectedList.add(child);
						actualList.add(actChild);
					} else {
						importantFail("Element (" + getNodeName(expected) + ") missing expected element position(" + childPos
								+ "): " + getNodeName(child));
					}
				}
			}

			containsInOrder("Element (" + getNodeName(expected) + ") mismatch in \"Element\"", elementComparator, false,
					actualList, expectedList);
		}
	};

	public static final ObjectComparator<Element> elementComparator = new ElementComparator();

	public static <T> boolean containsInAnyOrder(ObjectComparator<T> comparator, boolean ignoreContextualDiffs,
			boolean ignoreImportantExceptions, List<T> actual, T... expected) {
		return containsInAnyOrder(comparator, ignoreContextualDiffs, ignoreImportantExceptions, actual, Arrays.asList(expected));
	}

	public static <T> boolean containsInAnyOrder(ObjectComparator<T> comparator, boolean ignoreContextualDiffs,
			boolean ignoreImportantExceptions, List<T> actual, List<T> expected) {
		List<T> actClone = new ArrayList<T>(actual);
		List<T> left = new ArrayList<T>(expected.size());
		left.addAll(expected);

		int completed = 0;
		for (T obj1 : expected) {
			for (T obj2 : actClone) {
				try {
					comparator.assertEquals(obj2, obj1, ignoreContextualDiffs);
					left.remove(obj1);
					actClone.remove(obj2);
					completed = 0;
					break;
				} catch (IgnorableAssertionException re) {
					// this will occur when the objects don't match.
				} catch (ImportantAssertionException e) {
					if (!ignoreImportantExceptions && ++completed == actClone.size()) {
						throw e;
					}
				}
			}
		}
		return left.size() == 0;
	}

	public static <T> void containsInOrder(String message, ObjectComparator<T> comparator, boolean ignoreImportantExceptions,
			List<T> actual, List<T> expected) {
		try {
			containsInOrder(comparator, ignoreImportantExceptions, actual, expected);
		} catch (IgnorableAssertionException iae) {
			throw new IgnorableAssertionException(message + " - expected: " + toString(expected) + " actual: " + toString(actual),
					iae);
		} catch (ImportantAssertionException iae) {
			throw new ImportantAssertionException(message + " - expected: " + toString(expected) + " actual: " + toString(actual),
					iae);
		}
	}

	public static <T> void containsInOrder(ObjectComparator<T> comparator, boolean ignoreContextualDiffs,
			boolean ignoreImportantExceptions, List<T> actual, T... expected) {
		containsInAnyOrder(comparator, ignoreContextualDiffs, ignoreImportantExceptions, actual, Arrays.asList(expected));
	}

	public static <T> void containsInOrder(ObjectComparator<T> comparator, boolean ignoreImportantExceptions, List<T> actual,
			List<T> expected) {
		for (int i = 0; i < expected.size(); i++) {
			comparator.assertEquals(actual.get(i), expected.get(i));
		}
	}

	public static <T> void containsInAnyOrder(String message, ObjectComparator<T> comparator, boolean ignoreContextualDiffs,
			boolean ignoreImportantExceptions, List<T> actual, List<T> expected) {
		if (!containsInAnyOrder(comparator, ignoreContextualDiffs, ignoreImportantExceptions, actual, expected)) {
			importantFail(message + " - expected: " + toString(expected) + " actual: " + toString(actual));
		}
	}

	public static <T> boolean containsAllInAnyOrder(ObjectComparator<T> comparator, boolean ignoreContextualDiffs, List<T> actual,
			T... expected) {
		return containsAllInAnyOrder(comparator, ignoreContextualDiffs, false, actual, Arrays.asList(expected));
	}

	public static <T> boolean containsAllInAnyOrder(ObjectComparator<T> comparator, boolean ignoreContextualDiffs,
			boolean ignoreImportantExceptions, List<T> actual, T... expected) {
		return containsAllInAnyOrder(comparator, ignoreContextualDiffs, ignoreImportantExceptions, actual, Arrays.asList(expected));
	}

	public static <T> boolean containsAllInAnyOrder(ObjectComparator<T> comparator, boolean ignoreContextualDiffs,
			boolean ignoreImportantExceptions, List<T> actual, List<T> expected) {
		return actual != null && expected != null && actual.size() == expected.size()
				&& containsInAnyOrder(comparator, ignoreContextualDiffs, ignoreImportantExceptions, actual, expected);
	}

	private static boolean areObjectsEqualOrBothNull(Object obj1, Object obj2) {
		return (obj1 != null && obj1.equals(obj2)) || (obj1 == null && obj2 == null);
	}

	public static AtomFeed createNodeFeed(String rootURI, TPServerConfig config) {
		AtomFeed feed = new AtomFeed();

		feed.setId(TPNodeFeed.URL);
		feed.getCategories().add(winkCatToInternal(Constants.CAT_CONN_PROVIDER));

		for (TdiNodeConfig tdi : config.getNodeConfigs().getTdiNodeConfigs()) {
			feed.getEntries().add(createNodeEntryFor(rootURI, tdi));
		}

		return feed;
	}

	public static AtomEntry createNodeEntryFor(String rootURI, TdiNodeConfig allValuesCfg) {
		AtomEntry e = new AtomEntry();
		e.setId(allValuesCfg.getId());
		AtomPerson ap = new AtomPerson();

		ap.setEmail(allValuesCfg.getEmail());
		ap.setName(allValuesCfg.getAuthor());
		e.getAuthors().add(ap);

		e.setSummary(new AtomText(allValuesCfg.getSummary()));
		e.setTitle(new AtomText(allValuesCfg.getTitle()));

		URI selfLocation = URI.create(rootURI + "/" + TPNodeFeed.URL + "/" + e.getId());

		AtomLink al = new AtomLink();
		al.setHref(selfLocation.toString());
		al.setType(Constants.TYPE_APPLICATION_ATOM_XML_ENTRY);
		al.setRel(Constants.REL_SELF);
		e.getLinks().add(al);

		al = new AtomLink();
		al.setHref(selfLocation.toString() + "/" + TPTypeFeed.URL);
		al.setType(Constants.TYPE_APPLICATION_ATOM_XML_FEED);
		al.setRel(Constants.REL_TOUCHPOINT);
		e.getLinks().add(al);

		Element data = doc.createElementNS(Constants.NS_SCMP, "scmp:data");
		data.setAttributeNS(Constants.NS_SCHEMA_INSTANCE, "xsi:schemaLocation",
				"http://www.ibm.com/xmlns/prod/scmp /schema/tdi-connectivity-provider.xsd");

		Element cp = doc.createElementNS(Constants.NS_SCMP, "scmp:connectivity-provider");
		data.appendChild(cp);

		Element t = doc.createElementNS(Constants.NS_SCMP, "scmp:type");
		t.appendChild(doc.createTextNode(Constants.CONNECTIVITY_PROVIDER_TDI_TYPE));
		cp.appendChild(t);

		t = doc.createElementNS(Constants.NS_SCMP, "scmp:location");
		t.appendChild(doc.createTextNode(allValuesCfg.getLocation()));
		cp.appendChild(t);

		t = doc.createElementNS(Constants.NS_SCMP, "scmp:organization");
		t.appendChild(doc.createTextNode(allValuesCfg.getOrganization()));
		cp.appendChild(t);

		t = doc.createElementNS(Constants.NS_SCMP, "scmp:contact");
		t.appendChild(doc.createTextNode(allValuesCfg.getContact()));
		cp.appendChild(t);

		e.getAny().add(data);

		return e;
	}

	/**
	 * Create an instance Entry. Each setter of the AtomEntry is called with
	 * value corresponding the the setter name plus the provided suffix. If the
	 * setter is setId the value provided is ("Id" + suffix). No published or
	 * updated fields are set.
	 * 
	 * The Provider InstanceConfig looks like this:
	 * 
	 * <pre>
	 * &lt;InstanceConfig&gt;
	 * 	&lt;role&gt;provider&lt;/role&gt;
	 * &lt;/InstanceConfig&gt;
	 * </pre>
	 * 
	 * The Requestor InstanceConfig looks like this:
	 * 
	 * <pre>
	 * &lt;InstanceConfig&gt;
	 * 	&lt;role&gt;requestor&lt;/role&gt;
	 * &lt;/InstanceConfig&gt;
	 * </pre>
	 * 
	 * @param suffix
	 *            the sufix the set.
	 * @param isProvider
	 *            whether the instance will be a provider tp or a requestor
	 * @return the AtomEntry
	 * @throws JAXBException
	 */
	public static AtomEntry createInstAtomEntry(String suffix, TouchpointRole tr, boolean enabled, Map<String, String> properties)
			throws JAXBException {
		AtomEntry e = new AtomEntry();
		e.setId("Id" + suffix);

		AtomPerson ap = new AtomPerson();
		ap.setEmail("Email" + suffix);
		ap.setName("Name" + suffix);
		e.getAuthors().add(ap);

		e.setSummary(new AtomText("Summary" + suffix));
		e.setTitle(new AtomText("Title" + suffix));

		switch (tr) {
		case PROVIDER:
			e.getCategories().add(winkCatToInternal(Constants.CAT_ROLE_PROVIDER));
			break;
		case INITIATOR:
			e.getCategories().add(winkCatToInternal(Constants.CAT_ROLE_INITIATOR));
			break;
		case INTERMEDIARY:
			e.getCategories().add(winkCatToInternal(Constants.CAT_ROLE_INTERMEDIARY));
			break;
		}

		InstanceData data = new InstanceData();
		data.getTouchpoint().setAdminState(enabled ? EnumAdminState.ENABLED : EnumAdminState.DISABLED);

		PropertySheet sheet = new PropertySheet();
		if (properties != null) {
			for (Entry<String, String> prop : properties.entrySet()) {
				Property p = new Property();
				p.setPropertyName(prop.getKey());
				p.getValue().add(prop.getValue());
				sheet.getProperty().add(p);
			}
		}
		data.getTouchpoint().setPropertySheet(sheet);

		data.getTouchpoint().setTouchpointID("TouchpointID" + suffix);
		data.getTouchpoint().setVersion("Version" + suffix);

		e.getAny().add(ObjectFactory.toElement(data));

		return e;
	}

	public static AtomEntry createDestAtomEntry(String suffix, URL reqOutUrl) throws JAXBException {
		AtomEntry e = new AtomEntry();
		e.setId("Id" + suffix);

		AtomPerson ap = new AtomPerson();
		ap.setEmail("Email" + suffix);
		ap.setName("Name" + suffix);
		e.getAuthors().add(ap);

		e.setSummary(new AtomText("Summary" + suffix));
		e.setTitle(new AtomText("Title" + suffix));

		DestinationData data = new DestinationData();
		data.getDestination().setRequestOut(reqOutUrl.toExternalForm());

		e.getAny().add(ObjectFactory.toElement(data));
		return e;
	}

	private static String getNodeName(Node n) {
		return n.getNamespaceURI() != null ? "{" + n.getNamespaceURI() + "}" + n.getNodeName() : n.getNodeName();
	}

	/**
	 * Finds an element child of the specified parent.
	 * 
	 * @param parent
	 *            the parent element which children to list
	 * @param qName
	 *            the fully qualified name to lookFor.
	 * @param postion
	 *            the n-th element matching the qName criteria in the list of
	 *            chil.dren. The positioning is <b>1-based</b>.
	 * @return the found element or null.
	 */
	private static Element findElementByLocalQNameAndPostion(Element parent, QName qName, int postion) {
		assertThat(parent, is(notNullValue()));

		int pos = 0;
		NodeList children = parent.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			if (children.item(i).getNodeType() == Node.ELEMENT_NODE && children.item(i).getLocalName().equals(qName.getLocalPart())
					&& areObjectsEqualOrBothNull(children.item(i).getNamespaceURI(), qName.getNamespaceURI())) {
				if (postion == ++pos) {
					return (Element) children.item(i);
				}
			}
		}
		return null;
	}

	private static String getFirstLevelTextChildrenContent(Element parent) {
		if (parent != null) {
			StringBuilder sb = new StringBuilder();
			NodeList children = parent.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				if (children.item(i).getNodeType() == Node.TEXT_NODE || children.item(i).getNodeType() == Node.CDATA_SECTION_NODE) {
					sb.append(children.item(i).getNodeValue().trim());
				}
			}
			return sb.toString();
		}
		return null;
	}

	public static String serializeEntry(AtomEntry entry) throws JAXBException {
		return JAXBUtils.serializeObject(new JAXBElement<AtomEntry>(new QName("http://www.w3.org/2005/Atom", "entry"),
				AtomEntry.class, entry), AtomEntry.getMarshaller());
	}

	public static String serializeFeed(AtomFeed feed) throws JAXBException {
		return JAXBUtils.serializeObject(new JAXBElement<AtomFeed>(new QName("http://www.w3.org/2005/Atom", "feed"),
				AtomFeed.class, feed), AtomFeed.getMarshaller());
	}

	public static AtomEntry deserializeEntry(String entry) {
		return (AtomEntry) JAXBUtils.deserializeObject(entry, AtomEntry.getUnmarshaller());
	}

	public static AtomFeed deserializeFeed(String feed) {
		return (AtomFeed) JAXBUtils.deserializeObject(feed, AtomFeed.getUnmarshaller());
	}

	public static AppService deserializeService(String service) {
		return (AppService) JAXBUtils.deserializeObject(service, AppService.getUnmarshaller());
	}

	public static class IgnorableAssertionException extends RuntimeException {

		private static final long serialVersionUID = 1L;

		public IgnorableAssertionException(String message) {
			super(message);
		}

		public IgnorableAssertionException(String message, Throwable cause) {
			super(message, cause);
		}
	}

	public static class ImportantAssertionException extends RuntimeException {

		private static final long serialVersionUID = 1L;

		public ImportantAssertionException(String message) {
			super(message);
		}

		public ImportantAssertionException(String message, Throwable cause) {
			super(message, cause);
		}
	}

	private static final void ignorableFail(String message) {
		throw new IgnorableAssertionException(message);
	}

	private static final void importantFail(String message) {
		throw new ImportantAssertionException(message);
	}

	/** Find links by rel value in an internal-typed AtomLink list (no Wink dependency). */
	public static List<AtomLink> findLinksByRel(List<AtomLink> links, String rel) {
		List<AtomLink> result = new LinkedList<AtomLink>();
		for (AtomLink link : links) {
			if (rel.equals(link.getRel())) {
				result.add(link);
			}
		}
		return result;
	}

	public static AtomEntry createReferenceAtomEntry(AtomEntry fullEntry, boolean ignoreMissingSelfLinks) {
		List<AtomLink> selfLink = findLinksByRel(fullEntry.getLinks(), Constants.REL_SELF);

		if (selfLink.size() == 0) {
			selfLink = findLinksByRel(fullEntry.getLinks(), Constants.REL_EDIT);
		}

		if (selfLink.size() == 0 && ignoreMissingSelfLinks) {
			selfLink = new LinkedList<AtomLink>();
			AtomLink dummy = new AtomLink();
			dummy.setHref("dummySelfLink");
			selfLink.add(dummy);
		}

		if (selfLink.size() == 0) {
			throw new ImportantAssertionException("Missing self/edit link");
		}

		return createReferenceAtomEntry(fullEntry, selfLink.get(0).getHref());
	}

	public static AtomEntry createReferenceAtomEntry(AtomEntry fullEntry, String selfHref) {
		AtomEntry entry = new AtomEntry();
		entry.setId(fullEntry.getId());
		entry.setTitle(fullEntry.getTitle());
		entry.setUpdated(fullEntry.getUpdated());

		AtomLink ref = new AtomLink();
		ref.setType(Constants.TYPE_APPLICATION_ATOM_XML_ENTRY);
		ref.setRel(Constants.REL_SELF);
		ref.setHref(selfHref);
		entry.getLinks().add(ref);

		return entry;
	}

	public static AtomFeed createReferenceAtomFeed(AtomFeed fullFeed, boolean ignoreMissingSelfLinks) {
		AtomFeed feed = new AtomFeed();
		feed.setId(fullFeed.getId());
		feed.setTitle(fullFeed.getTitle());
		feed.setUpdated(fullFeed.getUpdated());
		feed.setLinks(new java.util.ArrayList<AtomLink>(fullFeed.getLinks()));
		feed.setCategories(new java.util.ArrayList<AtomCategory>(fullFeed.getCategories()));

		List<AtomEntry> refEntries = new java.util.ArrayList<AtomEntry>(fullFeed.getEntries().size());
		for (AtomEntry e : fullFeed.getEntries()) {
			refEntries.add(createReferenceAtomEntry(e, ignoreMissingSelfLinks));
		}
		feed.setEntries(refEntries);

		return feed;
	}

	@SuppressWarnings("unchecked")
	public static void containsRelations(List links, String... relations) {
		if (!containsAllInAnyOrder(new ObjectComparator() {
			@Override
			public void assertEquals(Object actual, Object expected, boolean ignoreContextualDiffs) {
				AtomLink actLink = (AtomLink) actual;
				String exp = expected.toString();

				if (!exp.equals(actLink.getRel())) {
					throw new IgnorableAssertionException("Expected relation was not found: " + actLink.getRel());
				}
			}
		}, false, false, links, Arrays.asList(relations))) {

			throw new RuntimeException("Missing relationships\nExpected: " + toString(links) + " \nActual: "
					+ Arrays.toString(relations));
		}
	}

	public static List<String> toString(List<Object> list) {
		List<String> result = new ArrayList<String>(list.size());

		for (Object elem : list) {
			result.add(toString(elem));
		}

		return result;
	}

	public static String toString(Object obj) {
		if (obj instanceof AtomLink) {
			final AtomLink link = (AtomLink) obj;
			return "rel: \"" + link.getRel() + "\" type: \"" + link.getType() + "\" href: \"" + link.getHref() + "\"";
		} else if (obj instanceof AtomEntry) {
			try {
				return serializeEntry((AtomEntry) obj);
			} catch (JAXBException e) {
				e.printStackTrace();
			}
		} else if (obj instanceof AtomCategory) {
			final AtomCategory cat = (AtomCategory) obj;
			return "term: \"" + cat.getTerm() + "\" scheme: \"" + cat.getScheme() + "\"";
		} else if (obj instanceof AtomFeed) {
			try {
				return serializeFeed((AtomFeed) obj);
			} catch (JAXBException e) {
				e.printStackTrace();
			}
		}
		return "" + obj;
	}

	/**
		* Converts a Wink AtomCategory to the internal AtomCategory type by term/scheme/label.
		* Used at call sites where Constants.CAT_* (Wink) must be passed to comparators expecting
		* the internal com.ibm.di.web.common.atom.AtomCategory type.
		*/
	public static AtomCategory winkCatToInternal(org.apache.wink.common.model.atom.AtomCategory wink) {
		AtomCategory cat = new AtomCategory();
		cat.setTerm(wink.getTerm());
		cat.setScheme(wink.getScheme());
		cat.setLabel(wink.getLabel());
		return cat;
	}
}
